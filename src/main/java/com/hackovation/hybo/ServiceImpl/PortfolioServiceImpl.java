package com.hackovation.hybo.ServiceImpl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.algo.finance.FinanceUtils;
import org.algo.finance.data.GoogleSymbol;
import org.algo.finance.data.GoogleSymbol.Data;
import org.algo.finance.portfolio.BlackLittermanModel;
import org.algo.finance.portfolio.MarketEquilibrium;
import org.algo.matrix.BasicMatrix;
import org.algo.matrix.BigMatrix;
import org.algo.series.CalendarDateSeries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hack17.hybo.domain.Allocation;
import com.hack17.hybo.domain.Fund;
import com.hack17.hybo.domain.InvestorProfile;
import com.hack17.hybo.domain.Portfolio;
import com.hack17.hybo.repository.PortfolioRepository;
import com.hackovation.hybo.ReadFile;
import com.hackovation.hybo.Util.EtfIndexMap;
import com.hackovation.hybo.services.PortfolioService;

@Service
@Transactional
public class PortfolioServiceImpl implements PortfolioService{

	@Autowired
	PortfolioRepository portfolioRepository;

	Map<String,String> indexToEtfMap;
	Map<String,String> EtfToIndexMap;

	@Override
	public Map<String,Portfolio> buildPortfolio(String clientId,boolean dummy) {
		System.out.println("Building Portfolio Started "+clientId);
		EtfToIndexMap = EtfIndexMap.getEtfToIndexMapping();
		indexToEtfMap = EtfIndexMap.getIndexToEtfMapping();
		// Step 1. Calculate Covariance Matrix
		BasicMatrix covarianceMatrix = getCovarianceMatrix();
		System.out.println("--------------Covariance Matrix Calculation "+clientId);
//		System.out.println(covarianceMatrix);

		// Step 2. Calculate Lambda (Risk Aversion Factor)
		Double lambda = 2.5d;
		
		
		String[] tickers = getAssetsTickers();
//		double[][] marketWeight = {{0.25},{3},{0.25},{0.25}};
		System.out.println("--------------Fetching Market Weight Calculation"+clientId);
		double[][] marketWeight = getMarketWeight();
		BasicMatrix marketWeightMatrix =  BigMatrix.FACTORY.rows(marketWeight);
		MarketEquilibrium marketEquilibrium = new MarketEquilibrium(tickers, covarianceMatrix, lambda);
		BlackLittermanModel bl  = new BlackLittermanModel(marketEquilibrium, marketWeightMatrix);
		List<BigDecimal> weights = new ArrayList<>();
		weights.add(new BigDecimal(30));
		weights.add(new BigDecimal(20));
		weights.add(new BigDecimal(25));
		weights.add(new BigDecimal(25));
		bl.addViewWithBalancedConfidence(weights, 0.26);
		
/*		System.out.println("--------------Asset Return Matrix---------------------");
		System.out.println(bl.getAssetReturns());
*/	
		System.out.println("--------------Asset Weights Calculation Started "+clientId);
		BasicMatrix finalAssetWeights = bl.getAssetWeights();
		System.out.println("--------------Asset Weights "+finalAssetWeights);
		LinkedHashMap<String, Double> assetClassWiseWeight = new LinkedHashMap<>();
		long i = 0;
		for(String assetClass:indexToEtfMap.keySet()){
			assetClassWiseWeight.put(assetClass, finalAssetWeights.doubleValue(i++));
		}
		Map<String,Portfolio> map = buildPortfolio(30000,assetClassWiseWeight,clientId,dummy);
		System.out.println("Building Portfolio Done "+clientId);
		return map;		
	}

	private LinkedHashMap<String,String> getassetETFMap(){
		LinkedHashMap<String,String> assetETFMap = new LinkedHashMap<>();
		assetETFMap.put("CRSPTM1","VTI");
		assetETFMap.put("CRSPLC1","VTV");
		assetETFMap.put("CRSPML1","VOE");
		assetETFMap.put("CRSPSC1","VBR");
		return assetETFMap;
	}
	private String[] getAssetsTickers(){
		LinkedHashMap<String,String> assetETFMap = getassetETFMap();
		String[] arr = new String[assetETFMap.keySet().size()];
		assetETFMap.keySet().toArray(arr);
		return arr;
	}
	private String[] getAssetsTickersOld(){
		String[] tickers = new String[5];
		tickers[0] = "AOR";
		tickers[1] = "MDIV";
		tickers[2] = "AOM";
		tickers[3] = "PCEF";
		tickers[4] = "AOA";
		return tickers;
	}
	double[][] getMarketWeight(){
		double[][] marketWeight = new double[4][1];
		double[] totalValue = new double[4]; 
		try{
			ArrayList<String> files = new ArrayList<>(4);
			files.add("D:\\Wkspace\\Hacovation\\CRSP US Total Market_IndividualMarketValue.txt");
			files.add("D:\\Wkspace\\Hacovation\\CRSP US Large Cap Value_IndividualMarketValue.txt");
			files.add("D:\\Wkspace\\Hacovation\\CRSP US MID CAP VALUE_IndividualMarketValue.txt");
			files.add("D:\\Wkspace\\Hacovation\\CRSP US SMALL CAP VALUE_IndividualMarketValue.txt");
			int i = 0;
			for(String fileName:files){
				BufferedReader f = new BufferedReader(new FileReader(fileName));
				String ln=null;
				while((ln=f.readLine())!=null){
					totalValue[i] +=Double.valueOf(ln.substring(ln.lastIndexOf("#")+1));
				}
				i++;
				f.close();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		double total=0;
		for(double d:totalValue)
			total+=d;
		marketWeight[0][0]=totalValue[0]/total;
		marketWeight[1][0]=totalValue[1]/total;
		marketWeight[2][0]=totalValue[2]/total;
		marketWeight[3][0]=totalValue[3]/total;
		return marketWeight;
	}
	BasicMatrix getCovarianceMatrix(){
		Collection<CalendarDateSeries<Double>> col = new ArrayList<>();
		ReadFile readFile = new ReadFile();
		col.add(readFile.getCalendarDataSeries("D:\\Wkspace\\Hacovation\\CRSP US Total Market.txt","CRSPTM1"));
		col.add(readFile.getCalendarDataSeries("D:\\Wkspace\\Hacovation\\CRSP US Large Cap Value.txt","CRSPLC1"));
		col.add(readFile.getCalendarDataSeries("D:\\Wkspace\\Hacovation\\CRSP US MID CAP VALUE.txt","CRSPML1"));
		col.add(readFile.getCalendarDataSeries("D:\\Wkspace\\Hacovation\\CRSP US SMALL CAP VALUE.txt","CRSPSC1"));
		BasicMatrix covarianceMatrix = FinanceUtils.makeCovarianceMatrix(col);
		return covarianceMatrix;
	}
	public Map<String,Portfolio> buildPortfolio(double investment,LinkedHashMap<String, Double> assetClassWiseWeight,String clientId,boolean dummy){
		Portfolio portfolio = new Portfolio();
		List<Allocation> allocationList = new ArrayList<>();
		Map<String, Portfolio> portfolioMap = new HashMap<>();
		for(String assetClass:indexToEtfMap.keySet()){
			Allocation allocation = new Allocation();
			Fund fund = new Fund();
	 		GoogleSymbol gs = new GoogleSymbol(indexToEtfMap.get(assetClass));
	 		List<Data> dataList = gs.getHistoricalPrices();
	 		Double cost = investment*assetClassWiseWeight.get(assetClass);
	 		if(dataList != null && dataList.size()>0){
	 			Data data = dataList.get(0);
	 			double perIndexCost = data.getPrice();
	 			NumberFormat nf = NumberFormat.getInstance();
	 			System.out.println("Asset Class: "+assetClass+" Weight: "+assetClassWiseWeight.get(assetClass)+" Cost: "+cost +" PerIndexCost: "+perIndexCost);
	 			allocation.setQuantity(Double.valueOf((cost/perIndexCost)).intValue());
	 			allocation.setCostPrice(allocation.getQuantity()*perIndexCost);
	 			allocation.setInvestment(investment);
	 			allocation.setPercentage(assetClassWiseWeight.get(assetClass).intValue());
	 			fund.setTicker(indexToEtfMap.get(assetClass));
	 			allocation.setFund(fund);
	 			allocationList.add(allocation);
	 		}
 			portfolio.setAllocations(allocationList);
		}
		InvestorProfile profile = new InvestorProfile();
		profile.setId(Long.valueOf(clientId));
		portfolio.setInvestorProfile(profile);
		portfolioRepository.persist(portfolio);
		return portfolioMap;
	}
}
