package com.hackovation.hybo.Controllers;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.algo.finance.data.GoogleSymbol;
import org.algo.finance.data.GoogleSymbol.Data;
import org.algo.series.CalendarDateSeries;


public class Portfolio implements Serializable{

	double investment;
	String ETF;
	int number;
	double cost;
	
	public Portfolio() {
	}
	Portfolio(double investment){
		this.investment=investment;
	}
	
	Map<String,Portfolio> buildPortfolio(LinkedHashMap<String,String> assetClassETFMap,LinkedHashMap<String, Double> assetClassWiseWeight){
		Map<String, Portfolio> portfolioMap = new HashMap<>();
		for(String assetClass:assetClassETFMap.keySet()){
			Portfolio portfolio = new Portfolio();
	 		GoogleSymbol gs = new GoogleSymbol(assetClassETFMap.get(assetClass));
	 		List<Data> dataList = gs.getHistoricalPrices();
	 		Double cost = investment/assetClassWiseWeight.get(assetClass);
	 		if(dataList != null && dataList.size()>0){
	 			Data data = dataList.get(0);
	 			double perIndexCost = data.getPrice();
	 			NumberFormat nf = NumberFormat.getInstance();
	 			System.out.println(cost +" --- "+perIndexCost);
	 			portfolio.number = Double.valueOf((cost/perIndexCost)).intValue();
	 			portfolio.cost=portfolio.number*perIndexCost;
	 			portfolio.ETF = assetClassETFMap.get(assetClass);
	 			portfolio.investment=this.investment;
	 			portfolioMap.put(assetClass, portfolio);
	 		}
		}
		return portfolioMap;
	}
	public double getInvestment() {
		return investment;
	}
	public void setInvestment(double investment) {
		this.investment = investment;
	}
	public String getETF() {
		return ETF;
	}
	public void setETF(String eTF) {
		ETF = eTF;
	}
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	public double getCost() {
		return cost;
	}
	public void setCost(double cost) {
		this.cost = cost;
	}
}
