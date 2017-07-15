package com.hackovation.hybo.scheduled;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.EntityManager;

import org.algo.finance.FinanceUtils;
import org.algo.matrix.BasicMatrix;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.hackovation.hybo.ReadFile;
import com.hackovation.hybo.Util.PathsAsPerAssetClass;
import com.hackovation.hybo.bean.dao.PortfolioDao;
import com.hackovation.hybo.entities.PortfolioEntity;
import com.hackovation.hybo.enums.ETFEnum;
import com.hackovation.hybo.rebalance.Rebalance;

@Component
public class BasedOnThreshold implements Rebalance{

	@Autowired
	EntityManager entityManager;
	@Autowired
	PortfolioDao portfolioDao;
	
	final int threshold = 15;
	
	@Override
	@Scheduled(cron="0 0/1 * 1/1 * *")
	public void rebalance() {
		System.out.println("Cron Running");
		List<PortfolioEntity> listOfPortfolios =  portfolioDao.fetchAllPortfolio();
		Map<Long,List<PortfolioEntity>> groupWisePortfolio = groupByUserId(listOfPortfolios);
		Set<Entry<Long,List<PortfolioEntity>>> entrySet = groupWisePortfolio.entrySet();
		for(Entry<Long,List<PortfolioEntity>> entry:entrySet){
			long userId = entry.getKey();
			List<PortfolioEntity> portfolioList = entry.getValue();
			rebalancePortfolio(portfolioList);
		}
		
	}
	
	public void rebalancePortfolio(List<PortfolioEntity> actualPortfolioList){
		try{
			boolean rebalancePortfolio = false;
			long userId = 0;
			Map<ETFEnum,Integer> todaysWeight = getETFWiseWeight(actualPortfolioList);
			for(PortfolioEntity portfolio:actualPortfolioList){
				userId = portfolio.getClientId();
				int actualWeight = portfolio.getWeight();
				int today = todaysWeight.get(portfolio.getETF());
				int diff = Math.abs(today-actualWeight);
				int perc = (diff/actualWeight)*100;
				if(perc>=threshold){
					rebalancePortfolio = true;
					break;
				}
			}
			if(rebalancePortfolio){
				double totalInvestmentAsOfToday = 0;
				for(PortfolioEntity existingPortfolio:actualPortfolioList){
					int noOfETF = existingPortfolio.getNumber();
					Map<ETFEnum,String> paths = PathsAsPerAssetClass.getETFPaths();
					Calendar cal = Calendar.getInstance();
					cal.set(Calendar.HOUR, 0);
					cal.set(Calendar.MINUTE, 0);
					cal.set(Calendar.SECOND, 0);
					Date date = cal.getTime();
					ReadFile readFile = new ReadFile();
					totalInvestmentAsOfToday += noOfETF*readFile.getETFPriceForDate(paths.get(existingPortfolio.getETF()), existingPortfolio.getETF(), date);
				}

				
				
				
				List<PortfolioEntity> newPortfolio = new ArrayList<>();
				
				for(PortfolioEntity existingPortfolio:actualPortfolioList){
					int weight = todaysWeight.get(existingPortfolio.getETF());
					double etfWiseInvestment = (totalInvestmentAsOfToday*weight)/100;
					Map<ETFEnum,String> paths = PathsAsPerAssetClass.getETFPaths();

					Calendar cal = Calendar.getInstance();
					cal.set(Calendar.HOUR, 0);
					cal.set(Calendar.MINUTE, 0);
					cal.set(Calendar.SECOND, 0);
					Date date = cal.getTime();
					ReadFile readFile = new ReadFile();
					double costPerETF = readFile.getETFPriceForDate(paths.get(existingPortfolio.getETF()), existingPortfolio.getETF(), date);
					int noOfEtf = Double.valueOf(etfWiseInvestment/costPerETF).intValue();
					double cost = noOfEtf*costPerETF;
					
					PortfolioEntity portfolio = new PortfolioEntity();
					portfolio.setClientId(userId);
					portfolio.setCost(cost);
					portfolio.setClientId(userId);
					portfolio.setInvestment(totalInvestmentAsOfToday);
					portfolio.setWeight(weight);
					newPortfolio.add(portfolio);
				}
				if(newPortfolio.size()>0){
					portfolioDao.deletePortfolio(userId);
					newPortfolio.forEach(portfolio -> portfolioDao.addPortfolio(portfolio));
				}
				
			}
			
			
		}catch(Exception e){
			
		}
	}
	
	private Map<ETFEnum,Integer> getETFWiseWeight(List<PortfolioEntity> portfolioList){
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		Date date = cal.getTime();
		ReadFile readFile = new ReadFile();

		Map<ETFEnum, Integer> etfWiseWeight = new HashMap<>();
		Map<ETFEnum,String> paths = PathsAsPerAssetClass.getETFPaths();
		Map<String,Double> etfWisePrice = new HashMap<>();
		double totalPrice = 0;
		for(PortfolioEntity portfolio:portfolioList){
			String filePath = paths.get(ETFEnum.valueOf(portfolio.getETF()));
			double price = 0;
			price = readFile.getETFPriceForDate(filePath,ETFEnum.valueOf(portfolio.getETF()).name(),date);
			etfWisePrice.put(ETFEnum.valueOf(portfolio.getETF()).name(),price);
			totalPrice+=price;
		}
		Set<Entry<String, Double>> entrySet = etfWisePrice.entrySet();
		for(Entry<String,Double> entry:entrySet){
			Double weight = entry.getValue()/totalPrice;
			etfWiseWeight.put(ETFEnum.valueOf(entry.getKey()), weight.intValue());
		}
		
		return etfWiseWeight;
	}
	
	
}
