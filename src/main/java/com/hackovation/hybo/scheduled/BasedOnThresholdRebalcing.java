package com.hackovation.hybo.scheduled;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hack17.hybo.domain.Allocation;
import com.hack17.hybo.domain.CurrentDate;
import com.hack17.hybo.domain.InvestorProfile;
import com.hack17.hybo.domain.MarketStatus;
import com.hack17.hybo.domain.Portfolio;
import com.hack17.hybo.domain.RiskTolerance;
import com.hack17.hybo.repository.PortfolioRepository;
import com.hack17.hybo.service.DBLoggerService;
import com.hack17.hybo.util.DateTimeUtil;
import com.hackovation.hybo.AllocationType;
import com.hackovation.hybo.CreatedBy;
import com.hackovation.hybo.Util.HyboUtil;
import com.hackovation.hybo.Util.PathsAsPerAssetClass;
import com.hackovation.hybo.rebalance.Rebalance;

@Component
@Service
public class BasedOnThresholdRebalcing implements Rebalance{

	@Autowired
	PortfolioRepository portfolioRepository;
	@Autowired
	DBLoggerService dbLoggerService;
	final int threshold = 15;
	Map<String,String> indexToEtfMap;
	Map<String,String> EtfToIndexMap;
	final Map<Double,Double> assetClassTieredTable;
	
	
	{
		assetClassTieredTable = new HashMap<>();
		assetClassTieredTable.put(2.0, 1.0);
		assetClassTieredTable.put(5.0, 1.5);
		assetClassTieredTable.put(10.0, 2.5);
		assetClassTieredTable.put(20.0, 5.0);
		assetClassTieredTable.put(40.0, 6.0);
		assetClassTieredTable.put(60.0, 7.0);
		assetClassTieredTable.put(80.0, 7.0);
	}
	
	
	@Scheduled(cron="0 0/2 * * * *")
	@Transactional
	public void cron(){
		System.out.println("Cron Running -> "+Calendar.getInstance().getTime());
		CurrentDate existingDate = (CurrentDate)portfolioRepository.getEntity(1, CurrentDate.class);
		rebalance(existingDate.getDate());
	}
	@Override
	@Transactional
	public void rebalance(Date date) {
		System.out.println("Rebalancing Started ... "+date);
		EtfToIndexMap = HyboUtil.getEtfToIndexMapping();
		indexToEtfMap = HyboUtil.getIndexToEtfMapping();

		List<Portfolio> listOfPortfolios =  portfolioRepository.getAllPortfoliosBeforeDate(date);
		
		System.out.println("Fetched all portfolios... "+listOfPortfolios.size());
		System.out.println("Grouping Allocation based on portfolio..");
		Map<Portfolio,List<Allocation>> groupWisePortfolio = groupByUserId(listOfPortfolios);
		
		Set<Entry<Portfolio,List<Allocation>>> entrySet = groupWisePortfolio.entrySet();
		for(Entry<Portfolio,List<Allocation>> entry:entrySet){
			Portfolio portfolio = entry.getKey();
			
			if(!shouldTriggerRebalance(portfolio,date))continue;
			
			List<Allocation> portfolioList = entry.getValue();
			System.out.println("Rebalancing ... "+portfolio);
			rebalancePortfolio(portfolio,portfolioList,date);
		}
		
		System.out.println("Rebalancing Done !!! ");
		
	}
	
	private boolean shouldTriggerRebalance(Portfolio portfolio,Date currentDate){
		boolean trigger = false;
		
		int horizonInMonths = portfolio.getInvestorProfile().getInvestmentHorizonInMonths();
		Date activeAllocationDate = null;
		List<Allocation> allocationList = portfolio.getAllocations();
		for(Allocation allocation:allocationList){
			if(allocation.getIsActive().equals("Y")){
				activeAllocationDate = allocation.getTransactionDate();
			}
		}
		int days = DateTimeUtil.getDateDifferenceInDays(currentDate, activeAllocationDate);
		if(days<30){
			System.out.println("Not trigerring portfolio for "+portfolio.getClientId()+". Days difference: "+days);
		}else if(days>=30 && horizonInMonths>=60){
			System.out.println("Triggering portfolio for "+portfolio.getClientId()+". Days difference: "+days);
			trigger = true;
		}else if(days>=180){
			System.out.println("Triggering portfolio for "+portfolio.getClientId()+". Days difference: "+days);
			trigger = true;
		}else{
			trigger = false;
			System.out.println("Not trigerring portfolio for "+portfolio.getClientId()+". Days difference: "+days);
		}
		return trigger;
	}
	
	/*
	 * There are 2 Levels of rebalancing.
	 * 1. Rebalancing based on Asset type (ETF,BOND)
	 * 2. Rebalacing based on Asset class types for ETF
	 */
	public void rebalancePortfolio(Portfolio portfolio,List<Allocation> portfolioList,Date date){
		HashMap<AllocationType,List<Allocation>> typeWiseAllocation = getAllocationBasedOnType(portfolioList);
		
		boolean triggerRebalancing = true;
		List<Allocation> eqAllocationList = typeWiseAllocation.get(AllocationType.EQ);
		List<Allocation> bondAllocationList = typeWiseAllocation.get(AllocationType.BOND);
		if(typeWiseAllocation.size()>1){
			int P = getValueOfP(portfolio, typeWiseAllocation);
			double initialEQSum = getTotalValue(eqAllocationList);
		}
		
		List<Allocation> newAllocationList = rebalanceEquity(portfolio,eqAllocationList,bondAllocationList,date);
		if(newAllocationList != null && newAllocationList.size()>0)
			rebalanceBond(portfolio, bondAllocationList,newAllocationList,date);
		
	}
	public void rebalanceBond(Portfolio portfolio,List<Allocation> bondAllocationList,List<Allocation>updatedAllocationList,Date date){
		List<Allocation> updatedBondAllocationList = updatedAllocationList;
		Calendar cal = Calendar.getInstance();
 		cal.setTime(date);
 		cal = trimTime(cal);
 		Date currentDate = cal.getTime();
 		for(Allocation existingAllocation:bondAllocationList){
			Allocation newAllocation = copyAllocationInNewObject(existingAllocation, currentDate);
			updatedBondAllocationList.add(newAllocation);
		}
 		persistPortfolio(portfolio, updatedAllocationList);
	}
	
	public HashMap<AllocationType, List<Allocation>> getAllocationBasedOnType(List<Allocation> portfolioList){
		HashMap<AllocationType,List<Allocation>> map = new HashMap<>();
		List<Allocation> activeAllocations = new ArrayList<>();
		for(Allocation allocation:portfolioList){
			if(allocation.getIsActive().equals("Y"))
				activeAllocations.add(allocation);
		}
		
		List<Allocation> EQList = activeAllocations.stream().filter(a->a.getType().equals(AllocationType.EQ.name())).collect(Collectors.toList());
		List<Allocation> BONDList = activeAllocations.stream().filter(a->a.getType().equals(AllocationType.BOND.name())).collect(Collectors.toList());
		map.put(AllocationType.EQ, EQList);
		map.put(AllocationType.BOND, BONDList);
		return map;
	}
	
	public int getValueOfP(Portfolio portfolio,HashMap<AllocationType,List<Allocation>> typeWiseAllocation){
		int P = 5;
		
		InvestorProfile profile = portfolio.getInvestorProfile();
		if(profile.getRiskTolerance().equals(RiskTolerance.HIGH)||profile.getRiskTolerance().equals(RiskTolerance.VERY_HIGH))
			P=10;
		else{ // if volatility is higer then we have to keep small range
			double eqCost = typeWiseAllocation.get(AllocationType.EQ).stream().mapToDouble(d->d.getCostPrice()).sum();
			double bondCost = typeWiseAllocation.get(AllocationType.BOND).stream().mapToDouble(d->d.getCostPrice()).sum();
			double perc = eqCost / (eqCost+bondCost);
			if(perc>30){
				P=3;
			}
		}
		return P;
	}
	
	public double getTotalValue(List<Allocation> allocationList){
		double sum = 0;
		Iterator<Allocation> iter = allocationList.iterator();
		while(iter.hasNext()){
			Allocation allocation = iter.next();
			sum+=allocation.getCostPrice();
		}
		return sum;
	}
	
	public List<Allocation> rebalanceEquity(Portfolio portfolio, List<Allocation> equityAllocationList,List<Allocation> bondAllocationList,Date date){
		List<Allocation> newAllocationList = new ArrayList<>();
		try{
			MarketStatus marketStatus = (MarketStatus)portfolioRepository.getEntity(1, MarketStatus.class);
			InvestorProfile profile = portfolio.getInvestorProfile();
			RiskTolerance riskTolerance = profile.getRiskTolerance();
			
			if(riskTolerance.equals(RiskTolerance.VERY_HIGH) && marketStatus.isGoingUp){
				System.out.println(" Not rebalancing because RiskToleranc is High and Market is going up.");
			}
			else if(riskTolerance.equals(RiskTolerance.MODERATE) && marketStatus.isFluctuating){
				System.out.println(" Tiered Rebalncing because RiskToleranc is Moderate/Mediun and Market is fluctuating.");
				newAllocationList = tieredBasedRebalancing(portfolio, equityAllocationList,date);
			}
			else if(riskTolerance.equals(RiskTolerance.HIGH) && (marketStatus.isGoingDown || marketStatus.isGoingUp)){
				System.out.println(" Formula Based Rebalncing because RiskToleranc is High and Market is not fluctuating.");
				newAllocationList = formulaBasedRebalancing(portfolio, equityAllocationList,bondAllocationList,date);
			}else{
				System.out.println(" Not rebalancing no condition match.");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return newAllocationList;
	}
	
	private List<Allocation> tieredBasedRebalancing(Portfolio portfolio, List<Allocation> equityAllocationList,Date date){
		HashMap<String,Double> existPercMap = new HashMap<>();
		HashMap<String, Double> newPercMap = new HashMap<>();
		HashMap<String, Double> newPricesPerETF = new HashMap<>();
		double totalValue = 0;
		Calendar cal = Calendar.getInstance();
 		cal.setTime(date);
 		cal = trimTime(cal);
 		
		for(Allocation existingAllocation:equityAllocationList){
			int noOfETF = existingAllocation.getQuantity();
			Map<String,String> paths = PathsAsPerAssetClass.getETFPaths();
			double latestPrice = portfolioRepository.getIndexPriceForGivenDate(existingAllocation.getFund().getTicker(), cal.getTime());
			double cost = latestPrice;
			totalValue +=cost*existingAllocation.getQuantity();
			newPercMap.put(existingAllocation.getFund().getTicker(), cost);
			newPricesPerETF.put(existingAllocation.getFund().getTicker(), latestPrice);
			existPercMap.put(existingAllocation.getFund().getTicker(), existingAllocation.getPercentage());
		}
		newPercMap = updatePercentagesAsPerLatestCost(newPercMap, totalValue);
		List<Allocation> newAllocationList = new ArrayList<>();
		
		Date currentDate = cal.getTime();
		double newInvestment = 0.0;
		for(Allocation allocation:equityAllocationList){
			Allocation newAllocation = copyAllocationInNewObject(allocation, currentDate);
			String ticker = allocation.getFund().getTicker();
			double existingPerc = existPercMap.get(ticker);
			double newPerc = newPercMap.get(ticker);
			double adjustment = checkEligibilityForAssetClassRebalanncing(existingPerc,newPerc);
			if(adjustment == 0){
				newAllocationList.add(newAllocation);
				allocation.setIsActive("N");
				newInvestment+=newAllocation.getInvestment();
			}
			else if (adjustment < 0 ){
				double newPer =existingPerc-Math.abs(adjustment);
				double averagePerc = (existingPerc+newPer)/2;
				double cost = (totalValue*averagePerc)/100;
				double etfTodayPrice = newPricesPerETF.get(ticker);
				int quantity = new Double(cost/etfTodayPrice).intValue();
				cost = etfTodayPrice;
				newAllocation.setCostPrice(cost);
				newAllocation.setQuantity(quantity);
				allocation.setIsActive("N");
				newInvestment+=newAllocation.getCostPrice();
				newAllocationList.add(newAllocation);
				log(allocation, newAllocation, currentDate);
				newAllocation.setRebalanceDayQuantity(allocation.getQuantity());
				newAllocation.setRebalanceDayPrice(cost);
			}
			else if (adjustment > 0 ){
				double newPer =existingPerc+Math.abs(adjustment);
				double averagePerc = (existingPerc+newPer)/2;
				double cost = (totalValue*averagePerc)/100;
				double etfTodayPrice = newPricesPerETF.get(ticker);
				int quantity = new Double(cost/etfTodayPrice).intValue();
				cost = quantity*etfTodayPrice;
				newAllocation.setCostPrice(cost);
				newAllocation.setQuantity(quantity);
				allocation.setIsActive("N");
				newInvestment+=newAllocation.getCostPrice();
				newAllocationList.add(newAllocation);
				newAllocation.setRebalanceDayQuantity(allocation.getQuantity());
				newAllocation.setRebalanceDayPrice(cost);
				log(allocation, newAllocation, currentDate);
			}
			
		}
		updatePercCurrent(newAllocationList);
		updatePercLatest(newAllocationList);

		for(Allocation allocation:newAllocationList){
			allocation.setInvestment(newInvestment);
		}
		return newAllocationList;
	}
	
	public List<Allocation> formulaBasedRebalancing(Portfolio portfolio, List<Allocation> equityAllocationList,List<Allocation> bondAllocationList,Date date){
		double totalInvestment = 0.0;
		RiskTolerance riskTolerance = portfolio.getInvestorProfile().getRiskTolerance();
		List<Allocation> newAllocationList = new ArrayList<>();
		if(equityAllocationList != null && equityAllocationList.size()>0)
			totalInvestment+= equityAllocationList.get(0).getInvestment();
		
		/*if(bondAllocationList != null && bondAllocationList.size()>0)
			totalInvestment+= bondAllocationList.get(0).getInvestment();
		*/
		
		final int m = 2;
		final int floor = getFloorValue(riskTolerance, totalInvestment);
		Calendar cal = Calendar.getInstance();
 		cal.setTime(date);
 		cal = trimTime(cal);
 		
		double currentValueOfPortfolio = 0;
		HashMap<String, Double> newPricesPerETF = new HashMap<>();
		HashMap<String,Double> existPercMap = new HashMap<>();
		for(Allocation existingAllocation:equityAllocationList){
			int noOfETF = existingAllocation.getQuantity();
			Map<String,String> paths = PathsAsPerAssetClass.getETFPaths();
		
			double latestPrice = portfolioRepository.getIndexPriceForGivenDate(existingAllocation.getFund().getTicker(), cal.getTime());
			double cost = latestPrice;
			currentValueOfPortfolio +=cost*existingAllocation.getQuantity();
			newPricesPerETF.put(existingAllocation.getFund().getTicker(), latestPrice);
			existPercMap.put(existingAllocation.getFund().getTicker(), existingAllocation.getPercentage());
		}
		
		double equityPortion = m*(currentValueOfPortfolio-floor);
		Date currentDate = cal.getTime();
		double investment = 0;
		for(Allocation allocation : equityAllocationList){
			String ticker = allocation.getFund().getTicker();
			Allocation newAllocation = copyAllocationInNewObject(allocation, currentDate);
			double perc = existPercMap.get(ticker);
			double etfTodayPrice = newPricesPerETF.get(ticker);
			double cost = (equityPortion*perc)/100;
			int number = Double.valueOf(cost/etfTodayPrice).intValue();
			cost = etfTodayPrice;
			investment += cost*number;
			newAllocation.setCostPrice(cost);
			newAllocation.setPercentage(perc);
			newAllocation.setQuantity(number);
			newAllocation.setIsActive("Y");
			newAllocation.setRebalanceDayQuantity(allocation.getQuantity());
			newAllocation.setRebalanceDayPrice(cost);
			newAllocationList.add(newAllocation);
			log(allocation, newAllocation, currentDate);
		}
		updatePercCurrent(newAllocationList);
		updatePercLatest(newAllocationList);
		return newAllocationList;
	}
	
	private void updatePercCurrent(List<Allocation> allocationList){
		double portfolioValue = 0.0;
		for(Allocation allocation:allocationList){
			portfolioValue += allocation.getRebalanceDayPrice()*allocation.getRebalanceDayQuantity();
		}
		for(Allocation allocation:allocationList){
			double value = allocation.getRebalanceDayPrice()*allocation.getRebalanceDayQuantity();
			try{
				allocation.setRebalanceDayPerc(value*100/portfolioValue);
			}
			catch(Exception e){
				e.printStackTrace();
				allocation.setRebalanceDayPerc(0);
			}
		}
		
	}
	private void updatePercLatest(List<Allocation> allocationList){
		double portfolioValue = 0.0;
		for(Allocation allocation:allocationList){
			portfolioValue += allocation.getCostPrice()*allocation.getQuantity();
		}
		for(Allocation allocation:allocationList){
			double value = allocation.getCostPrice()*allocation.getQuantity();
			try{
				allocation.setPercentage(value*100/portfolioValue);
			}
			catch(Exception e){
				e.printStackTrace();
				allocation.setPercentage(0);
			}
		}
		
	}
	
	private int getFloorValue(RiskTolerance riskTolerance,double totalInvestment){
		int floorValue = 0;
		int perc = 0;
		if(riskTolerance.equals(RiskTolerance.HIGH) || riskTolerance.equals(RiskTolerance.VERY_HIGH)) perc = 25;
		if(riskTolerance.equals(RiskTolerance.LOW) || riskTolerance.equals(RiskTolerance.VERY_LOW)) perc = 75;
		if(riskTolerance.equals(RiskTolerance.MODERATE)) perc = 50;
		floorValue = Double.valueOf((totalInvestment*perc)/100).intValue();
		return floorValue;
	}
	private Allocation copyAllocationInNewObject(Allocation allocation,Date currentDate){
		Allocation newAllocation  = new Allocation();
		newAllocation.setFund(allocation.getFund());
		newAllocation.setTransactionDate(currentDate);
		newAllocation.setExpenseRatio(allocation.getExpenseRatio());
		newAllocation.setCostPrice(allocation.getCostPrice());
		newAllocation.setInvestment(allocation.getInvestment());
		newAllocation.setPercentage(allocation.getPercentage());
		newAllocation.setQuantity(allocation.getQuantity());
		newAllocation.setType(allocation.getType());
		newAllocation.setCreatedBy(CreatedBy.REBAL.name());
		newAllocation.setIsActive("Y");
		return newAllocation;
	}

	
	public HashMap<String,Double> updatePercentagesAsPerLatestCost(HashMap<String,Double> dataMap,double totalCost){
		HashMap<String,Double> resultMap = new HashMap<>();
		for(String key:dataMap.keySet()){
			double cost = dataMap.get(key);
			resultMap.put(key,(cost/totalCost)*100);
		}
		return resultMap;
	}
	
	public double checkEligibilityForAssetClassRebalanncing(double existingPerc,double newPerc){
		boolean rebalanceAssetClass = true;
		double diff = 0;
		double range = 0;
		for(Double key:assetClassTieredTable.keySet()){
			if(existingPerc>key)continue;
			if(existingPerc<=key)
				range = assetClassTieredTable.get(key);
		}
		diff = newPerc-existingPerc;
		if(Math.abs(diff)<=newPerc)rebalanceAssetClass = true;
		else rebalanceAssetClass = false;
		if(!rebalanceAssetClass)diff = 0;
		return diff;
	}
	
	public void log(Allocation existingAllocation,Allocation newAllocation,Date sellDate){
		int oldQuantity = existingAllocation.getQuantity();
		int newQuantity = newAllocation.getQuantity();
		if(oldQuantity>newQuantity){//SELL
			//dbLoggerService.logTransaction(existingAllocation, newAllocation.getCostPrice(), sellDate, oldQuantity-newQuantity);
		}else if(newQuantity>oldQuantity){//BUY
			//dbLoggerService.logTransaction(existingAllocation, newAllocation.getCostPrice(), sellDate, newQuantity-oldQuantity);
		}
	}
	
	private Calendar trimTime(Calendar cal){
		cal.set(Calendar.HOUR_OF_DAY,0);
		cal.set(Calendar.MINUTE,0);
		cal.set(Calendar.SECOND,0);
		cal.set(Calendar.MILLISECOND,0);
	//	cal.set(Calendar.ZONE_OFFSET,0);
		return cal;
	}
	
	private void persistPortfolio(Portfolio portfolio,List<Allocation> newAllocationList){
		List<Allocation> existingAllocations = portfolio.getAllocations();
		for(Allocation allocation:existingAllocations)allocation.setIsActive("N");
		existingAllocations.addAll(newAllocationList);
		portfolio.setAllocations(existingAllocations);
		portfolioRepository.persist(portfolio);
	}
	
	
/*	
 	public void rebalancePortfolio(Portfolio portfolio,List<Allocation> actualAllocationList){
		try{
			boolean rebalancePortfolio = false;
			Map<String,Integer> todaysWeight = getETFWiseWeight(actualAllocationList);
			for(Allocation allocation:actualAllocationList){
				double actualWeight = allocation.getPercentage();
				int today = todaysWeight.get(allocation.getFund().getTicker());
				double diff = Math.abs(today-actualWeight);
				double perc = (diff/actualWeight)*100;
				if(perc>=threshold){
					rebalancePortfolio = true;
					break;
				}
			}
			if(rebalancePortfolio){
				double totalInvestmentAsOfToday = 0;
				for(Allocation existingPortfolio:actualAllocationList){
					int noOfETF = existingPortfolio.getQuantity();
					Map<String,String> paths = PathsAsPerAssetClass.getETFPaths();
					Calendar cal = Calendar.getInstance();
					cal.set(Calendar.HOUR, 0);
					cal.set(Calendar.MINUTE, 0);
					cal.set(Calendar.SECOND, 0);
					Date date = cal.getTime();
					ReadFile readFile = new ReadFile();
					totalInvestmentAsOfToday += noOfETF*readFile.getETFPriceForDate(paths.get(existingPortfolio.getFund().getTicker()), existingPortfolio.getFund().getTicker(), date);
				}

				
				
				
				List<Allocation> newAllocationList = new ArrayList<>();
				
				for(Allocation existingAllocation:actualAllocationList){
					int weight = todaysWeight.get(existingAllocation.getFund().getTicker());
					double etfWiseInvestment = (totalInvestmentAsOfToday*weight)/100;
					Map<String,String> paths = PathsAsPerAssetClass.getETFPaths();

					Calendar cal = Calendar.getInstance();
					cal.set(Calendar.HOUR, 0);
					cal.set(Calendar.MINUTE, 0);
					cal.set(Calendar.SECOND, 0);
					Date date = cal.getTime();
					ReadFile readFile = new ReadFile();
					double costPerETF = readFile.getETFPriceForDate(paths.get(existingAllocation.getFund().getTicker()), existingAllocation.getFund().getTicker(), date);
					int noOfEtf = Double.valueOf(etfWiseInvestment/costPerETF).intValue();
//					double cost = noOfEtf*costPerETF;
					
					Allocation allocation = new Allocation();
					allocation.setCostPrice(costPerETF);
					allocation.setInvestment(totalInvestmentAsOfToday);
					allocation.setInvestment(totalInvestmentAsOfToday);
					allocation.setPercentage(weight);
					newAllocationList.add(allocation);
				}
				portfolio.setAllocations(newAllocationList);
				portfolioRepository.merge(portfolio);
				
			}
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private Map<String,Integer> getETFWiseWeight(List<Allocation> allocationList) throws Exception{
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		Date date = cal.getTime();
		ReadFile readFile = new ReadFile();

		Map<String, Integer> etfWiseWeight = new HashMap<>();
		Map<String,String> paths = PathsAsPerAssetClass.getETFPaths();
		Map<String,Double> etfWisePrice = new HashMap<>();
		double totalPrice = 0;
		for(Allocation allocation:allocationList){
			String filePath = paths.get(EtfToIndexMap.get(allocation.getFund().getTicker()));
			double price = 0;
			price = readFile.getETFPriceForDate(filePath,allocation.getFund().getTicker(),date);
			if(price==0){
				throw new Exception("Data is not present for today!!");
			}
			etfWisePrice.put(allocation.getFund().getTicker(),price);
			totalPrice+=price;
		}
		Set<Entry<String, Double>> entrySet = etfWisePrice.entrySet();
		for(Entry<String,Double> entry:entrySet){
			Double weight = entry.getValue()/totalPrice;
			etfWiseWeight.put(entry.getKey(), weight.intValue());
		}
		
		return etfWiseWeight;
	}
	
*/	
}
