package com.hackovation.hybo.scheduled;

import static org.mockito.Matchers.doubleThat;

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
import org.algo.finance.FinanceUtils;
import org.algo.finance.data.GoogleSymbol;
import org.algo.finance.data.GoogleSymbol.Data;
import org.algo.matrix.BasicMatrix;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.hack17.hybo.domain.Allocation;
import com.hack17.hybo.domain.InvestorProfile;
import com.hack17.hybo.domain.Portfolio;
import com.hack17.hybo.domain.RiskTolerance;
import com.hack17.hybo.repository.PortfolioRepository;
import com.hackovation.hybo.AllocationType;
import com.hackovation.hybo.ReadFile;
import com.hackovation.hybo.Util.EtfIndexMap;
import com.hackovation.hybo.Util.PathsAsPerAssetClass;
import com.hackovation.hybo.rebalance.Rebalance;

@Component
public class BasedOnThresholdRebalcing implements Rebalance{

	@Autowired
	PortfolioRepository portfolioRepository;
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
	
	
	@Override
//	@Scheduled(cron="0 0/1 * 1/1 * *")
	@Transactional
	public void rebalance() {
		System.out.println("Cron Running");
		System.out.println("Rebalancing Started!!!");
		EtfToIndexMap = EtfIndexMap.getEtfToIndexMapping();
		indexToEtfMap = EtfIndexMap.getIndexToEtfMapping();

		List<Portfolio> listOfPortfolios =  portfolioRepository.getAllPortfolios();
		
		System.out.println("Fetched all portfolios... "+listOfPortfolios.size());
		System.out.println("Grouping Allocation based on portfolio..");
		Map<Portfolio,List<Allocation>> groupWisePortfolio = groupByUserId(listOfPortfolios);
		
		Set<Entry<Portfolio,List<Allocation>>> entrySet = groupWisePortfolio.entrySet();
		for(Entry<Portfolio,List<Allocation>> entry:entrySet){
			Portfolio portfolio = entry.getKey();
			List<Allocation> portfolioList = entry.getValue();
			System.out.println("Rebalancing ... "+portfolio);
			rebalancePortfolio(portfolio,portfolioList);
		}
		
		System.out.println("Rebalancing Done !!! ");
		
	}
	
	/*
	 * There are 2 Levels of rebalancing.
	 * 1. Rebalancing based on Asset type (ETF,BOND)
	 * 2. Rebalacing based on Asset class types for ETF
	 */
	public void rebalancePortfolio(Portfolio portfolio,List<Allocation> portfolioList){
		HashMap<AllocationType,List<Allocation>> typeWiseAllocation = getAllocationBasedOnType(portfolioList);
		
		boolean triggerRebalancing = true;
		List<Allocation> eqAllocationList = typeWiseAllocation.get(AllocationType.EQ);
		List<Allocation> bondAllocationList = typeWiseAllocation.get(AllocationType.BOND);
		if(typeWiseAllocation.size()>1){
			int P = getValueOfP(portfolio, typeWiseAllocation);
			double initialEQSum = getTotalValue(eqAllocationList);
		}
		
		rebalanceEquity(portfolio,eqAllocationList);
		
	}
	
	
	public HashMap<AllocationType, List<Allocation>> getAllocationBasedOnType(List<Allocation> portfolioList){
		HashMap<AllocationType,List<Allocation>> map = new HashMap<>();
		List<Allocation> EQList = portfolioList.stream().filter(a->a.getType().equals(AllocationType.EQ.name())).collect(Collectors.toList());
		List<Allocation> BONDList = portfolioList.stream().filter(a->a.getType().equals(AllocationType.BOND.name())).collect(Collectors.toList());
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
	
	public void rebalanceEquity(Portfolio portfolio, List<Allocation> equityAllocationList){
		if(checkEligibilityForEQRebalancing(portfolio, equityAllocationList)){
			HashMap<String,Double> existPercMap = new HashMap<>();
			HashMap<String, Double> newPercMap = new HashMap<>();
			HashMap<String, Double> newPricesPerETF = new HashMap<>();
			double totalValue = 0;
			for(Allocation existingAllocation:equityAllocationList){
				int noOfETF = existingAllocation.getQuantity();
				Map<String,String> paths = PathsAsPerAssetClass.getETFPaths();
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.HOUR, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				Date date = cal.getTime();
		 		GoogleSymbol gs = new GoogleSymbol(existingAllocation.getFund().getTicker());
		 		List<Data> dataList = gs.getHistoricalPrices();
		 		Data data = dataList.get(0);
/*	 			double perIndexCost = data.getPrice();
	 			ReadFile readFile = new ReadFile();
*/				double latestPrice = data.getPrice();
				double cost = latestPrice*existingAllocation.getQuantity();
				totalValue +=cost;
				newPercMap.put(existingAllocation.getFund().getTicker(), cost);
				newPricesPerETF.put(existingAllocation.getFund().getTicker(), latestPrice);
				existPercMap.put(existingAllocation.getFund().getTicker(), existingAllocation.getPercentage());
			}
			newPercMap = updatePercentagesAsPerLatestCost(newPercMap, totalValue);
			List<Allocation> newAllocationList = new ArrayList<>();
			
			Date currentDate = new Date();
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
					cost = quantity*etfTodayPrice;
					newAllocation.setCostPrice(cost);
					newAllocation.setQuantity(quantity);
					allocation.setIsActive("N");
					newInvestment+=newAllocation.getCostPrice();
					newAllocationList.add(newAllocation);
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
				}
				
			}
			
			for(Allocation allocation:newAllocationList){
				allocation.setInvestment(newInvestment);
				allocation.setIsActive("Y");
			}
			
			for(Allocation allocation:equityAllocationList)
				allocation.setIsActive("N");
			newAllocationList.addAll(equityAllocationList);
			portfolio.setAllocations(newAllocationList);
			portfolioRepository.persist(portfolio);			
		}
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
		
		return newAllocation;
	}
	//Here we have to consider all conditions.... still pending for implementation
	public boolean checkEligibilityForEQRebalancing(Portfolio portfolio,List<Allocation> equityAllocationList){
		boolean doRebalanceing = true;
		return doRebalanceing;
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
