package com.hackovation.hybo.scheduled;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.algo.finance.FinanceUtils;
import org.algo.matrix.BasicMatrix;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.hack17.hybo.domain.Allocation;
import com.hack17.hybo.domain.InvestorProfile;
import com.hack17.hybo.domain.Portfolio;
import com.hack17.hybo.repository.PortfolioRepository;
import com.hackovation.hybo.ReadFile;
import com.hackovation.hybo.Util.EtfIndexMap;
import com.hackovation.hybo.Util.PathsAsPerAssetClass;
import com.hackovation.hybo.rebalance.Rebalance;

@Component
public class BasedOnThreshold implements Rebalance{

	@Autowired
	PortfolioRepository portfolioRepository;
	final int threshold = 15;
	Map<String,String> indexToEtfMap;
	Map<String,String> EtfToIndexMap;
	@Override
//	@Scheduled(cron="0 0/1 * 1/1 * *")
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
	
	
}
