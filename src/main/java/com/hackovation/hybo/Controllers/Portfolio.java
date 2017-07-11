package com.hackovation.hybo.Controllers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.algo.finance.data.GoogleSymbol;
import org.algo.finance.data.GoogleSymbol.Data;
import org.algo.series.CalendarDateSeries;

public class Portfolio {

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
	 			portfolio.number = Integer.valueOf((cost/perIndexCost)+"");
	 			portfolio.cost=portfolio.number*perIndexCost;
	 			portfolio.ETF = assetClass;
	 			portfolioMap.put(ETF, portfolio);
	 		}
		}
		return portfolioMap;
	}
}
