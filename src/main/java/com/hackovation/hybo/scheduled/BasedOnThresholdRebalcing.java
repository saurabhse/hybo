package com.hackovation.hybo.scheduled;



import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.Set;

import org.apache.commons.collections4.map.MultiValueMap;
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
	boolean runRebalancing = false;
	
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
	
	public void toggleRebalancing(){
		runRebalancing = runRebalancing?false:true;
		System.out.println("Rebalancing "+runRebalancing);
	}
	
	@Scheduled(initialDelay=0,fixedDelay=10000)
	@Transactional
	public void cron(){
		CurrentDate existingDate = (CurrentDate)portfolioRepository.getEntity(1, CurrentDate.class);
		if(runRebalancing){
			System.out.println("Rebalancing Job : "+Calendar.getInstance().getTime());
			rebalance(existingDate.getDate());
		}
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
			System.out.println("-------------------------------------------------------------------");
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
		
		System.out.println("-----------------------------------------------------");
		System.out.println("Rebalancing Started Date:  "+date);
		EtfToIndexMap = HyboUtil.getEtfToIndexMapping();
		indexToEtfMap = HyboUtil.getIndexToEtfMapping();

		List<Portfolio> listOfPortfolios =  portfolioRepository.getAllPortfoliosBeforeDate(date);
		
		System.out.println("Fetched all portfolios. Size is : "+listOfPortfolios.size());
		Map<Portfolio,List<Allocation>> groupWisePortfolio = groupByUserId(listOfPortfolios);
		
		Set<Entry<Portfolio,List<Allocation>>> entrySet = groupWisePortfolio.entrySet();
		for(Entry<Portfolio,List<Allocation>> entry:entrySet){
			Portfolio portfolio = entry.getKey();
			System.out.println("Processing Portfolio for "+portfolio.getClientId());
			
			if(!shouldTriggerRebalance(portfolio,date))continue;
			
			List<Allocation> portfolioList = entry.getValue();
			System.out.println("	Rebalancing  Portfolio"+portfolio);
			rebalancePortfolio(portfolio,portfolioList,date);
			System.out.println("Completed Processing Portfolio for "+portfolio.getClientId());
			System.out.println("                 --------                     ");
		}
		
		System.out.println("Rebalancing Done !!! ");
		System.out.println("--------------------------------------------------------");
		
	}
	
	private boolean shouldTriggerRebalance(Portfolio portfolio,Date currentDate){
		boolean trigger = false;
		Calendar cal = Calendar.getInstance();
		cal.setTime(currentDate);
		if(cal.get(Calendar.MONTH)==Calendar.DECEMBER || cal.get(Calendar.MONTH)==Calendar.MARCH) return false;
		System.out.println("	Level 1 Trigger Check.");
		int horizonInMonths = portfolio.getInvestorProfile().getInvestmentHorizonInMonths();
		Date activeAllocationDate = null;
		List<Allocation> allocationList = getActiveAllocationList(portfolio.getAllocations());
		for(Allocation allocation:allocationList){
			if(allocation.getIsActive().equals("Y")){
				if(activeAllocationDate==null || allocation.getTransactionDate().after(activeAllocationDate))
					activeAllocationDate = allocation.getTransactionDate();
			}
		}
		int days = DateTimeUtil.getDateDifferenceInDays(currentDate, activeAllocationDate);
		if(days<30){
			System.out.println("	Checkpoint one failed. Days difference: "+days);
			System.out.println("	Portfolio should be atleast 30 days old");
		}else if(days>=30 && horizonInMonths>=60){
			System.out.println("	Checkpoint one passed. Days difference: "+days+" And Horizon is "+horizonInMonths+" months");
			trigger = true;
		}else if(days>=180){
			System.out.println("	Checkpoint one passed. Days difference: "+days);
			trigger = true;
		}else{
			trigger = false;
			System.out.println("	Checkpoint one failed because no condition match. Days difference: "+days+" And Horizon is "+horizonInMonths+" months");
		}
		
		if(trigger){
			System.out.println("	Checkpoint 2 limit started here.");
			Map<String,Double> existingPercentageMap = getTypeAllocationPercentage(allocationList);
			double P = 0.1;//getValueOfP(portfolio, allocationList);

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
					System.out.println("	Checkpoint 2 passed :"+key +" , "+currentDate);
					System.out.println("	"+existingPerc+" ---- "+latestPer);
					trigger = true;
					break;
				}
				
			}
			if(!trigger)
				System.out.println("	Checkpoint 2 failed. Therefore won't trigger Rebalancing!");
			else
				System.out.println("	Checkpoint 2 passed.");
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
			String ticker = object.getFund().getTicker();
			double value = object.getCostPrice()*object.getQuantity();
			if(allocationPercentage.containsKey(ticker)){
				value += allocationPercentage.get(ticker);
			}
			allocationPercentage.put(object.getFund().getTicker(), value);
		}
		
		for(String key:allocationPercentage.keySet()){
			double perc = allocationPercentage.get(key)/totalValue;
			allocationPercentage.put(key, perc);
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
	
	private Allocation cloneAllocation(Allocation allocation){
		Allocation clonedAllocation = new Allocation();
		clonedAllocation.setBuyDate(allocation.getBuyDate());
		clonedAllocation.setCostPrice(allocation.getCostPrice());
		clonedAllocation.setCreatedBy(allocation.getCreatedBy());
		clonedAllocation.setExpenseRatio(allocation.getExpenseRatio());
		clonedAllocation.setFund(allocation.getFund());
		clonedAllocation.setHoldTillDate(allocation.getHoldTillDate());
		clonedAllocation.setInvestment(allocation.getInvestment());
		clonedAllocation.setIsActive(allocation.getIsActive());
		clonedAllocation.setPercentage(allocation.getPercentage());
		clonedAllocation.setPortfolio(allocation.getPortfolio());
		clonedAllocation.setQuantity(allocation.getQuantity());
		clonedAllocation.setRebalanceDayPerc(allocation.getRebalanceDayPerc());
		clonedAllocation.setRebalanceDayPrice(allocation.getRebalanceDayPrice());
		clonedAllocation.setRebalanceDayQuantity(allocation.getRebalanceDayQuantity());
		clonedAllocation.setTransactionDate(allocation.getTransactionDate());
		clonedAllocation.setType(allocation.getType());
		return clonedAllocation;
	}
	public void persistAllocationInDatabase(Portfolio portfolio,List<Allocation> existingAllocationList,List<Allocation> newAllocationList,Date date){
		
		
		Map<String,Double> tickerWiseValue = new HashMap<>();
		List<Allocation> persistList = new ArrayList<>();
		MultiValueMap<String,Allocation> finalAllocationMap = new MultiValueMap<>();
		MultiValueMap<String,Allocation> persistAllocationMap = new MultiValueMap<>();
		for(Allocation allocation:existingAllocationList){
			if(allocation.getIsActive().equals("N"))continue;
			String ticker = allocation.getFund().getTicker();
			Calendar cal = Calendar.getInstance();
	 		cal.setTime(date);
	 		cal = trimTime(cal);
			double latestPrice = portfolioRepository.getIndexPriceForGivenDate(allocation.getFund().getTicker(), cal.getTime());
			double value = latestPrice*allocation.getQuantity();
			if(tickerWiseValue.containsKey(ticker))value+=tickerWiseValue.get(ticker);
			tickerWiseValue.put(ticker, value);
			Allocation clonedAllocation = cloneAllocation(allocation);
			clonedAllocation.setCreatedBy(CreatedBy.REBAL.name());
			clonedAllocation.setTransactionDate(date);
			finalAllocationMap.put(ticker, clonedAllocation);
		}
		
		
		System.out.println(" ------------------------AMAn           ");
		for(Allocation allocation:newAllocationList){
			System.out.println(allocation);
		}
		
		Calendar cal = Calendar.getInstance();
 		cal.setTime(date);
 		cal = trimTime(cal);
		Map<String,Allocation> newAllocationMap = getMapPerETF(newAllocationList);
		Set<String> keys = newAllocationMap.keySet();
		for(String ticker:keys){
			Allocation newAllocation = newAllocationMap.get(ticker);
			double newValue = newAllocation.getCostPrice()*newAllocation.getQuantity();
			double oldValue = tickerWiseValue.get(ticker);
			
			System.out.println(" Aman : "+ticker);
			System.out.println(newValue+","+oldValue);
			
			if(newValue>oldValue){
				double differenceValue = newValue-oldValue;
				int quantity = Double.valueOf(differenceValue/newAllocation.getCostPrice()).intValue();
				if(quantity>0){
					newAllocation.setQuantity(quantity);
					newAllocation.setTransactionDate(date);
					persistAllocationMap.put(ticker, newAllocation);
					ArrayList<Allocation> temp = (ArrayList<Allocation>)finalAllocationMap.get(ticker);
					for(Allocation tempAllocatio:temp)
						persistAllocationMap.put(ticker,tempAllocatio);
					dbLoggerService.logTransaction(newAllocation,0,null,quantity,Action.BUY,com.hack17.hybo.domain.CreatedBy.REBAL);
				}
			}
			else if(oldValue>newValue){
				
				double differenceValue = oldValue-newValue;
				ArrayList<Allocation> allocationList = (ArrayList<Allocation>)finalAllocationMap.get(ticker);
				Collections.sort(allocationList,new Comparator<Allocation>() {

					@Override
					public int compare(Allocation o1, Allocation o2) {
						return o1.getBuyDate().compareTo(o2.getBuyDate());
					}
				});
				Iterator<Allocation> iter = allocationList.iterator();
				int sellQuantity = 0;
				while(iter.hasNext()){
					Allocation allocation = iter.next();
					double value = allocation.getQuantity()*allocation.getCostPrice();
					if(value>differenceValue){
						sellQuantity = Double.valueOf(differenceValue/allocation.getCostPrice()).intValue();
						allocation.setQuantity(allocation.getQuantity()-sellQuantity);
						allocation.setTransactionDate(date);
						differenceValue -= sellQuantity*newAllocation.getCostPrice();
						persistAllocationMap.put(ticker,allocation);
						dbLoggerService.logTransaction(allocation,newAllocation.getCostPrice(),newAllocation.getTransactionDate(),sellQuantity,
								Action.SELL,com.hack17.hybo.domain.CreatedBy.REBAL);
					}else{
						sellQuantity = allocation.getQuantity();
						dbLoggerService.logTransaction(allocation,newAllocation.getCostPrice(),
								newAllocation.getTransactionDate(),sellQuantity,
								Action.SELL,com.hack17.hybo.domain.CreatedBy.REBAL);
						iter.remove();
					}
				}
				
						

			}
		}
//		persistList.addAll(existingAllocationList);
		for(String ticker:persistAllocationMap.keySet()){
			persistList.addAll(persistAllocationMap.getCollection(ticker));
		}
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
 		Map<String,Double> tickerWiseTotal = new HashMap<>();
 		for(Allocation existingAllocation:bondAllocationList){
 			String ticker = existingAllocation.getFund().getTicker();
 			double total = existingAllocation.getCostPrice()*existingAllocation.getQuantity();
 			totalOfBondAllocation += total;
 			if(tickerWiseTotal.containsKey(ticker)){
 				total+=tickerWiseTotal.get(ticker);
 			}
 			tickerWiseTotal.put(ticker, total);
 		}
 		double investment =0.0;
 		List<String> processedTickers = new ArrayList<>();
 		for(Allocation existingAllocation:bondAllocationList){
 			String ticker = existingAllocation.getFund().getTicker();
 			if(processedTickers.contains(ticker)) continue;
 			
			Allocation newAllocation = copyAllocationInNewObject(existingAllocation, currentDate);
			double perc = tickerWiseTotal.get(ticker)/totalOfBondAllocation*100;
			
			
			double latestPrice = portfolioRepository.getIndexPriceForGivenDate(newAllocation.getFund().getTicker(), cal.getTime());

			double amountToAssign = remainingAmountForBonds*perc/100;
			int count = new Double(amountToAssign/latestPrice).intValue();
			newAllocation.setQuantity(count);
			newAllocation.setCostPrice(latestPrice);
			investment += newAllocation.getCostPrice()*newAllocation.getQuantity();
			updatedBondAllocationList.add(newAllocation);
			processedTickers.add(ticker);
		}
		System.out.println("	Final Allocation in bond : "+investment);
 		return updatedAllocationList;
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
			
			if(profile.getInvestmentHorizonInMonths()<=36)
			{
				System.out.println("	Investment Horizon is very less. Therefore no need of rebalancing. "+profile.getInvestmentHorizonInMonths());
			}
			else if(riskTolerance.equals(RiskTolerance.VERY_HIGH) && marketStatus.isGoingUp){
				System.out.println("	Not rebalancing because RiskToleranc is High and Market is going up.");
			}
			else if(riskTolerance.equals(RiskTolerance.MODERATE) && marketStatus.isFluctuating){
				System.out.println("	Tiered Rebalncing because RiskToleranc is Moderate/Medium and Market is fluctuating.");
				newAllocationList = tieredBasedRebalancing(portfolio, equityAllocationList,bondAllocationList,date);
			}
			else if(riskTolerance.equals(RiskTolerance.HIGH) && (marketStatus.isGoingDown || marketStatus.isGoingUp)){
				System.out.println("	Formula Based Rebalncing because RiskToleranc is High and Market is not fluctuating.");
				newAllocationList = formulaBasedRebalancing(portfolio, equityAllocationList,bondAllocationList,date);
			}
			else if(riskTolerance.equals(RiskTolerance.LOW) && (marketStatus.isGoingDown || marketStatus.isGoingUp)){
				System.out.println("	Formula Based Rebalncing RiskTolerance is Low.");
				newAllocationList = formulaBasedRebalancing(portfolio, equityAllocationList,bondAllocationList,date);
			}			
			else{
				System.out.println("	Not rebalancing no condition match related to Risktolerance and Market Condition.");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return newAllocationList;
	}
	
	private List<Allocation> tieredBasedRebalancing(Portfolio portfolio, List<Allocation> equityAllocationList,List<Allocation> bondAllocationList,Date date){
		HashMap<String,Double> existPercMap = new HashMap<>();
		HashMap<String, Double> newPercMap = new HashMap<>();
		HashMap<String, Double> newPricesPerETF = new HashMap<>();
		double totalValue = 0;
		Calendar cal = Calendar.getInstance();
 		cal.setTime(date);
 		cal = trimTime(cal);
 		
		for(Allocation existingAllocation:bondAllocationList){
			int noOfETF = existingAllocation.getQuantity();
			double cost = existingAllocation.getCostPrice();
			remainingAmountForBonds +=cost*existingAllocation.getQuantity();
		}
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
				double oldPrice = allocation.getCostPrice()*allocation.getQuantity();
				double newPrice = newAllocation.getCostPrice()*newAllocation.getQuantity();
				remainingAmountForBonds += (oldPrice-newPrice);
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
				double oldPrice = allocation.getCostPrice()*allocation.getQuantity();
				double newPrice = newAllocation.getCostPrice()*newAllocation.getQuantity();
				remainingAmountForBonds += (newPrice-oldPrice);
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
		double equityValueOld = 0.0;
		double bondValueOld=0.0;
		if(equityAllocationList != null && equityAllocationList.size()>0)
			totalInvestment+= equityAllocationList.get(0).getInvestment();
		for(Allocation obj:equityAllocationList)
			equityValueOld += (obj.getCostPrice()*obj.getQuantity());
		for(Allocation obj:bondAllocationList)
			bondValueOld += (obj.getCostPrice()*obj.getQuantity());

		
		
		System.out.println("	Total Equity value old: "+equityValueOld+"\n");
		System.out.println("	Total Bond value old: "+bondValueOld+"\n");
	
		final int m = 3;
		Calendar cal = Calendar.getInstance();
 		cal.setTime(date);
 		cal = trimTime(cal);
 		
		double currentValueOfPortfolio = 0;
		double currentEquityValueOfPortfolio = 0;
		HashMap<String, Double> newPricesPerETF = new HashMap<>();
		HashMap<String,Double> existPercMap = new HashMap<>();
		for(Allocation existingAllocation:equityAllocationList){
			String ticker = existingAllocation.getFund().getTicker();
			double value = existingAllocation.getCostPrice()*existingAllocation.getQuantity();
			if(existPercMap.containsKey(ticker)){
				value += existPercMap.get(ticker);
			}
			existPercMap.put(ticker,value);
			currentEquityValueOfPortfolio += existingAllocation.getCostPrice()*existingAllocation.getQuantity();
		
			double latestPrice = portfolioRepository.getIndexPriceForGivenDate(existingAllocation.getFund().getTicker(), cal.getTime());
			double cost = latestPrice;
			currentValueOfPortfolio +=cost*existingAllocation.getQuantity();
			newPricesPerETF.put(existingAllocation.getFund().getTicker(), latestPrice);
			
		}
		System.out.println("	Total Equity value in current portfolio : "+currentEquityValueOfPortfolio+"\n");
		for(Allocation existingAllocation:bondAllocationList){
			
		
			double latestPrice = portfolioRepository.getIndexPriceForGivenDate(existingAllocation.getFund().getTicker(), cal.getTime());
			double cost = latestPrice;
			currentValueOfPortfolio +=cost*existingAllocation.getQuantity();
			newPricesPerETF.put(existingAllocation.getFund().getTicker(), latestPrice);
		}
		System.out.println("	Total value in current portfolio : "+currentValueOfPortfolio+"\n");
		
		Set<String> keys = existPercMap.keySet();
		for(String key:keys){
			existPercMap.put(key, 100*existPercMap.get(key)/currentEquityValueOfPortfolio);
		}
		
		
		final int floor = getFloorValue(riskTolerance, currentValueOfPortfolio);
		double equityPortion = m*(currentValueOfPortfolio-floor);
		
		System.out.println("	EquityPortion = m*(currentValueOfPortfolio-floor \n");
		System.out.println("	m = "+m+", current value of portfolio: "+currentValueOfPortfolio+", floor value: "+floor +"\n");
		
		equityPortion = Math.abs(equityPortion);
		remainingAmountForBonds = currentValueOfPortfolio-equityPortion;
		
		System.out.println("	equity portion :"+equityPortion+"\n");
		System.out.println("	bond portion :"+remainingAmountForBonds+"\n");
		Date currentDate = cal.getTime();
		double investment = 0;
		List<String> processedTickers = new ArrayList<>();
		for(Allocation allocation : equityAllocationList){
			String ticker = allocation.getFund().getTicker();
			if(processedTickers.contains(ticker)) continue;
			Allocation newAllocation = copyAllocationInNewObject(allocation, currentDate);
			double perc = existPercMap.get(ticker);
			double etfTodayPrice = newPricesPerETF.get(ticker);
			double cost = (equityPortion*perc)/100;
			int number = Double.valueOf(cost/etfTodayPrice).intValue();
			cost = etfTodayPrice;
			investment += cost*number;
			newAllocation.setRebalanceDayPrice(allocation.getCostPrice());
			newAllocation.setRebalanceDayQuantity(allocation.getQuantity());
			newAllocation.setCostPrice(cost);
			newAllocation.setPercentage(perc);
			newAllocation.setQuantity(number);
			newAllocation.setIsActive("Y");
			newAllocationList.add(newAllocation);
			log(allocation, newAllocation, currentDate);
			processedTickers.add(ticker);
		}
		return newAllocationList;
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
		newAllocation.setBuyDate(currentDate);
		newAllocation.setExpenseRatio(allocation.getExpenseRatio());
		newAllocation.setCostPrice(allocation.getCostPrice());
		newAllocation.setInvestment(allocation.getInvestment());
		newAllocation.setPercentage(allocation.getPercentage());
		newAllocation.setQuantity(allocation.getQuantity());
		newAllocation.setType(allocation.getType());
		newAllocation.setCreatedBy(CreatedBy.REBAL.name());
		newAllocation.setIsActive("Y");
		newAllocation.setRebalanceDayPrice(allocation.getRebalanceDayPrice());
		newAllocation.setRebalanceDayPerc(allocation.getRebalanceDayPerc());
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
