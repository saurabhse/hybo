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

import com.hack17.hybo.domain.Action;
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
	Calendar testCalendar = null;
	double remainingAmountForBonds = 0.0;
	
	
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
	
	
	@Scheduled(cron="0 0/1 * * * *")
	@Transactional
	public void cron(){
		CurrentDate existingDate = (CurrentDate)portfolioRepository.getEntity(1, CurrentDate.class);
		
		System.out.println("Cron Running -> "+Calendar.getInstance().getTime());
		//rebalance(existingDate.getDate());
	}
	
	public void test(){
		CurrentDate existingDate = (CurrentDate)portfolioRepository.getEntity(1, CurrentDate.class);
		testCalendar  = Calendar.getInstance();
		testCalendar.setTime(existingDate.getDate());
		Date systemDate = new Date();
		Date date = new Date();
		while(true){
			testCalendar.add(Calendar.DATE, 45);
			date = testCalendar.getTime();
			System.out.println("Rebalancing Started ... "+date);
			EtfToIndexMap = HyboUtil.getEtfToIndexMapping();
			indexToEtfMap = HyboUtil.getIndexToEtfMapping();

			List<Portfolio> listOfPortfolios =  portfolioRepository.getAllPortfoliosBeforeDate(date);
			Map<Portfolio,List<Allocation>> groupWisePortfolio = groupByUserId(listOfPortfolios);
			
			Set<Entry<Portfolio,List<Allocation>>> entrySet = groupWisePortfolio.entrySet();
			for(Entry<Portfolio,List<Allocation>> entry:entrySet){
				Portfolio portfolio = entry.getKey();
				if(!shouldTriggerRebalance(portfolio,date))continue;
				else System.out.println(" Will do rebalancing");
			}
			System.out.println("\n\n\n\n\n");
			if(date.after(systemDate))break;
		}
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
		
		
		System.out.println("Level 1 Trigger Check.");
		int horizonInMonths = portfolio.getInvestorProfile().getInvestmentHorizonInMonths();
		Date activeAllocationDate = null;
		List<Allocation> allocationList = getActiveAllocationList(portfolio.getAllocations());
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
		
		if(trigger){
			System.out.println("Level 2 Trigger Check since Level 1 passed.");
			//System.out.println(" ------------- Printing Value and Percentage as per the last processing date --- ");
			Map<String,Double> existingPercentageMap = getTypeAllocationPercentage(allocationList);
			double P = 0.1;//getValueOfP(portfolio, allocationList);
			//System.out.println(" ------------- Printing Value and Percentage as per the current date --- "+currentDate);
			List<Allocation> currentValueOfAllocations = listOfLatestValue(allocationList,currentDate);
			Map<String,Double> currentPercentageMap = getTypeAllocationPercentage(currentValueOfAllocations);
			
			for(String key:existingPercentageMap.keySet()){
				double existingPerc = existingPercentageMap.get(key);
				double min = existingPerc-(P*existingPerc);
				double max = existingPerc + (P*existingPerc);
				double latestPer = currentPercentageMap.get(key);
				//System.out.println(key+","+existingPerc+","+latestPer);
				if(latestPer>=min && latestPer<=max){
					trigger = false;
				}else{
					System.out.println(" -------------------------------------- Triggering rebalancing because of :"+key +" , "+currentDate);
					System.out.println(existingPerc+" ---- "+latestPer);
					trigger = true;
					break;
				}
				
			}
			if(!trigger)
				System.out.println(" ---------  Second level check for rebalancing failed!");
			else
				System.out.println("-------------- Second level check for rebalancing PASSED  !!! !");
		}
		return trigger;
	}
	
	public List<Allocation> getActiveAllocationList(List<Allocation> existinAllocationList){
		List<Allocation> activeAllocations = new ArrayList<>();
		for(Allocation allocation: existinAllocationList){
			if(allocation.getIsActive().equals("Y"))activeAllocations.add(allocation);
		}
		return activeAllocations;
	}
	private Map<String,Double> getTypeAllocationPercentage(List<Allocation> allocation){
		Map<String,Double> allocationPercentage= new HashMap<>();
		double totalValue = 0.0;
		for(Allocation object:allocation){
			totalValue += (object.getQuantity()*object.getCostPrice());
			//System.out.println(object.getFund().getTicker()+" -> "+object.getQuantity()+", "+object.getCostPrice()+","+(object.getQuantity()*object.getCostPrice()));
		}
		for(Allocation object:allocation){
			double value = object.getCostPrice()*object.getQuantity();
			allocationPercentage.put(object.getFund().getTicker(), value/totalValue*100);
			//System.out.println(object.getFund().getTicker()+" % -> "+value/totalValue*100);
		}
		return allocationPercentage;
	}
	
	private List<Allocation> listOfLatestValue(List<Allocation> allocation,Date date){
		List<Allocation> tempList = new ArrayList<>();
		Calendar cal = Calendar.getInstance();
 		cal.setTime(date);
 		cal = trimTime(cal);
		for(Allocation object:allocation){
			double latestPrice = portfolioRepository.getIndexPriceForGivenDate(object.getFund().getTicker(), cal.getTime());
			Allocation tempAllocation = copyAllocationInNewObject(object, cal.getTime());
			tempAllocation.setCostPrice(latestPrice);
			tempList.add(tempAllocation);
		}
		return tempList;
	}
	/*
	 * There are 2 Levels of rebalancing.
	 * 1. Rebalancing based on Asset type (ETF,BOND)
	 * 2. Rebalacing based on Asset class types for ETF
	 */
	public void rebalancePortfolio(Portfolio portfolio,List<Allocation> portfolioList,Date date){
		HashMap<AllocationType,List<Allocation>> typeWiseAllocation = getAllocationBasedOnType(portfolioList);
		
		List<Allocation> eqAllocationList = typeWiseAllocation.get(AllocationType.EQ);
		List<Allocation> bondAllocationList = typeWiseAllocation.get(AllocationType.BOND);
		remainingAmountForBonds = 0;
		List<Allocation> newAllocationList = rebalanceEquity(portfolio,eqAllocationList,bondAllocationList,date);
		if(newAllocationList != null && newAllocationList.size()>0){
			newAllocationList = rebalanceBond(portfolio, bondAllocationList,newAllocationList,date);
			persistAllocationInDatabase(portfolio,portfolioList,newAllocationList,date);
		}
	}
	
	public void persistAllocationInDatabase(Portfolio portfolio,List<Allocation> existingAllocationList,List<Allocation> newAllocationList,Date date){
		
		List<Allocation> persistList = new ArrayList<>();
		
		Calendar cal = Calendar.getInstance();
 		cal.setTime(date);
 		cal = trimTime(cal);
 		Date currentDate = cal.getTime();
		Map<String,Allocation> existingAllocationMap = getMapPerETF(existingAllocationList);
		Map<String,Allocation> newAllocationMap = getMapPerETF(newAllocationList);
		Set<String> keys = newAllocationMap.keySet();
		for(String ticker:keys){
			Allocation newAllocation = newAllocationMap.get(ticker);
			Allocation existingAllocation = existingAllocationMap.get(ticker);
			int newQuantity = newAllocation.getQuantity();
			int oldQuantity = existingAllocation.getQuantity();
			if(newQuantity>oldQuantity){
				newAllocation.setQuantity(existingAllocation.getQuantity());
				Allocation copiedAllocation = copyAllocationInNewObject(newAllocation, currentDate);
				copiedAllocation.setQuantity(newQuantity-oldQuantity);
				copiedAllocation.setBuyDate(currentDate);
				persistList.add(copiedAllocation);
				copiedAllocation.setPortfolio(portfolio);
				dbLoggerService.logTransaction(copiedAllocation,0,null,newQuantity-oldQuantity,Action.BUY,com.hack17.hybo.domain.CreatedBy.REBAL);
			}
			
			newAllocation.setBuyDate(existingAllocation.getBuyDate());
			if(oldQuantity>newQuantity){
				//just log it for TLH
				newAllocation.setPortfolio(portfolio);
				dbLoggerService.logTransaction(existingAllocation,newAllocation.getCostPrice(),newAllocation.getTransactionDate(),existingAllocation.getQuantity()-newAllocation.getQuantity(),
						Action.SELL,com.hack17.hybo.domain.CreatedBy.REBAL);
						

			}
		}
		persistList.addAll(newAllocationList);
//		persistList.addAll(existingAllocationList);
		updatePercLatest(persistList);
//		updatePercCurrent(persistList);
 		persistPortfolio(portfolio, persistList);

	}
	public Map<String,Allocation> getMapPerETF(List<Allocation> allocationList){
		Map<String,Allocation> dataMap = new HashMap<>();
		for(Allocation allocation:allocationList){
			String ticker = allocation.getFund().getTicker();
			if(dataMap.containsKey(ticker)){
				Allocation existingAllocation = dataMap.get(ticker);
				allocation.setQuantity(existingAllocation.getQuantity()+allocation.getQuantity());
			}
			dataMap.put(ticker,allocation);
				
		}
		return dataMap;
	}
	
	public List<Allocation> rebalanceBond(Portfolio portfolio,List<Allocation> bondAllocationList,List<Allocation>updatedAllocationList,Date date){
		List<Allocation> updatedBondAllocationList = updatedAllocationList;
		Calendar cal = Calendar.getInstance();
 		cal.setTime(date);
 		cal = trimTime(cal);
 		Date currentDate = cal.getTime();
 		double totalOfBondAllocation = 0.0;
 		for(Allocation existingAllocation:bondAllocationList)
 			totalOfBondAllocation += existingAllocation.getCostPrice()*existingAllocation.getQuantity();
 		
 		for(Allocation existingAllocation:bondAllocationList){
			Allocation newAllocation = copyAllocationInNewObject(existingAllocation, currentDate);
			double allocationPrice = newAllocation.getCostPrice()*newAllocation.getQuantity();
			double perc = allocationPrice/totalOfBondAllocation*100;
			
			
			double latestPrice = portfolioRepository.getIndexPriceForGivenDate(newAllocation.getFund().getTicker(), cal.getTime());

			double amountToAssign = remainingAmountForBonds*perc/100;
			int count = new Double(amountToAssign/latestPrice).intValue();
			newAllocation.setQuantity(count);
			newAllocation.setCostPrice(latestPrice);
			
			updatedBondAllocationList.add(newAllocation);
		}
 		return updatedAllocationList;
 		//persistPortfolio(portfolio, updatedAllocationList);
	}
	
	public HashMap<AllocationType, List<Allocation>> getAllocationBasedOnType(List<Allocation> allocationList){
		HashMap<AllocationType,List<Allocation>> map = new HashMap<>();
		List<Allocation> activeAllocations = getActiveAllocationList(allocationList);
		
		
		List<Allocation> EQList = activeAllocations.stream().filter(a->a.getType().equals(AllocationType.EQ.name())).collect(Collectors.toList());
		List<Allocation> BONDList = activeAllocations.stream().filter(a->a.getType().equals(AllocationType.BOND.name())).collect(Collectors.toList());
		map.put(AllocationType.EQ, EQList);
		map.put(AllocationType.BOND, BONDList);
		return map;
	}
	
	public double getValueOfP(Portfolio portfolio,HashMap<AllocationType,List<Allocation>> typeWiseAllocation){
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
		return 0.1;
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
				newAllocation.setRebalanceDayPrice(allocation.getCostPrice());
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
				newAllocation.setRebalanceDayPrice(allocation.getCostPrice());
				log(allocation, newAllocation, currentDate);
			}
			
		}
	

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
		
		final int m = 3;
		Calendar cal = Calendar.getInstance();
 		cal.setTime(date);
 		cal = trimTime(cal);
 		
		double currentValueOfPortfolio = 0;
		double currentEquityValueOfPortfolio = 0;
		HashMap<String, Double> newPricesPerETF = new HashMap<>();
		HashMap<String,Double> existPercMap = new HashMap<>();
		for(Allocation existingAllocation:equityAllocationList){
			int noOfETF = existingAllocation.getQuantity();
			Map<String,String> paths = PathsAsPerAssetClass.getETFPaths();
			existPercMap.put(existingAllocation.getFund().getTicker(), existingAllocation.getCostPrice()*existingAllocation.getQuantity());
			currentEquityValueOfPortfolio += existingAllocation.getCostPrice()*existingAllocation.getQuantity();
		
			double latestPrice = portfolioRepository.getIndexPriceForGivenDate(existingAllocation.getFund().getTicker(), cal.getTime());
			double cost = latestPrice;
			currentValueOfPortfolio +=cost*existingAllocation.getQuantity();
			newPricesPerETF.put(existingAllocation.getFund().getTicker(), latestPrice);
			
		}
		for(Allocation existingAllocation:bondAllocationList){
			int noOfETF = existingAllocation.getQuantity();
			Map<String,String> paths = PathsAsPerAssetClass.getETFPaths();
		
			double latestPrice = portfolioRepository.getIndexPriceForGivenDate(existingAllocation.getFund().getTicker(), cal.getTime());
			double cost = latestPrice;
			currentValueOfPortfolio +=cost*existingAllocation.getQuantity();
			newPricesPerETF.put(existingAllocation.getFund().getTicker(), latestPrice);
		}
		
		Set<String> keys = existPercMap.keySet();
		for(String key:keys){
			existPercMap.put(key, 100*existPercMap.get(key)/currentEquityValueOfPortfolio);
		}
		
		System.out.println(" Value of portfolio while running rebalancing "+currentValueOfPortfolio);
		
		final int floor = getFloorValue(riskTolerance, currentValueOfPortfolio);
		double equityPortion = m*(currentValueOfPortfolio-floor);
		equityPortion = Math.abs(equityPortion);
		remainingAmountForBonds = currentValueOfPortfolio-equityPortion;
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
			newAllocation.setRebalanceDayPrice(allocation.getCostPrice());
			newAllocationList.add(newAllocation);
			log(allocation, newAllocation, currentDate);
		}
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
		if(riskTolerance.equals(RiskTolerance.HIGH) || riskTolerance.equals(RiskTolerance.VERY_HIGH)) perc = 75;
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
		for(Allocation allocation:newAllocationList)allocation.setPortfolio(portfolio);
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
