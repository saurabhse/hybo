package com.hackovation.hybo.ServiceImpl;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.algo.finance.FinanceUtils;
import org.algo.finance.data.GoogleSymbol.Data;
import org.algo.finance.portfolio.BlackLittermanModel;
import org.algo.finance.portfolio.MarketEquilibrium;
import org.algo.matrix.BasicMatrix;
import org.algo.matrix.BigMatrix;
import org.algo.series.CalendarDateSeries;
import org.algo.type.CalendarDate;
import org.algo.type.CalendarDateUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hack17.hybo.domain.Allocation;
import com.hack17.hybo.domain.Fund;
import com.hack17.hybo.domain.IndexPrice;
import com.hack17.hybo.domain.InvestorProfile;
import com.hack17.hybo.domain.MarketWeight;
import com.hack17.hybo.domain.Portfolio;
import com.hack17.hybo.domain.RiskTolerance;
import com.hack17.hybo.domain.Transaction;
import com.hack17.hybo.domain.UserClientMapping;
import com.hack17.hybo.repository.FundRepository;
import com.hack17.hybo.repository.PortfolioRepository;
import com.hackovation.hybo.AllocationType;
import com.hackovation.hybo.CreatedBy;
import com.hackovation.hybo.Util.HyboUtil;
import com.hackovation.hybo.bean.ProfileRequest;
import com.hackovation.hybo.services.PortfolioService;

@Service
@Transactional
public class PortfolioServiceImpl implements PortfolioService{

	@Autowired
	PortfolioRepository portfolioRepository;
	
	@Autowired
	FundRepository fundRepository;

	Map<String,String> indexToEtfMap;
	Map<String,String> EtfToIndexMap;
	Map<String,AllocationType> allocationTypeMap;
	String l = "D:\\MATERIAL\\Hackathon\\Hackovation 2.0\\selected\\hybo\\Workspace\\hybo\\target\\classes\\";
//	final static Logger logger = Logger.getLogger(PortfolioServiceImpl.class);
	
	@Transactional
	private int getClientId(String userId){
		Object object = portfolioRepository.getEntityForAny(UserClientMapping.class,userId);
		if(object==null){
			Random random  = new Random();
			int clientId = random.nextInt(10000000);
			UserClientMapping newObject = new UserClientMapping();
			newObject.setClientId(clientId);
			newObject.setUserId(userId);
			portfolioRepository.persist(newObject);
			return clientId;
		}else{
			UserClientMapping existingObject = (UserClientMapping)object;
			return existingObject.getClientId();
		}
	}
	@Override
	public Map<String,Portfolio> buildPortfolio(InvestorProfile profile,String userId,boolean dummy,Date date,int investment) {
		System.out.println("######## Building Portfolio Started "+userId);
		
		int clientId = getClientId(userId);
	//	logger.info("Aman");
		EtfToIndexMap = HyboUtil.getEtfToIndexMapping();
		indexToEtfMap = HyboUtil.getIndexToEtfMapping();
		allocationTypeMap = HyboUtil.getAllocationTypeMap();
		
		
		// Step 1. Calculate Covariance Matrix
		System.out.println("######### Fetching Covariance Matrix "+userId);
		BasicMatrix covarianceMatrix = getCovarianceMatrix(date);
		System.out.println(covarianceMatrix);
		System.out.println("######### Done Fetching Covariance Matrix "+userId);
		System.out.println("\n\n\n\n\n");

		// Step 2. Calculate Lambda (Risk Aversion Factor)
		Double lambda = 2.5d;
		
		
		String[] tickers = getAssetsTickers();
//		double[][] marketWeight = {{0.25},{3},{0.25},{0.25}};
		double[][] marketWeight = getMarketWeight(date);
		BasicMatrix marketWeightMatrix =  BigMatrix.FACTORY.rows(marketWeight);
		System.out.println("###### Creating BlackLittermanObject ( -- Market Equilibrium");
		MarketEquilibrium marketEquilibrium = new MarketEquilibrium(tickers, covarianceMatrix, lambda);
		BlackLittermanModel bl  = new BlackLittermanModel(marketEquilibrium, marketWeightMatrix);
		System.out.println("\n");
		System.out.println("###### Adding User View with balanced Confidence");
		List<BigDecimal> weights = getMarketWeight(profile);
		bl.addViewWithBalancedConfidence(weights, 0.26);
		System.out.println("\n");
		
/*		System.out.println("--------------Asset Return Matrix---------------------");
		System.out.println(bl.getAssetReturns());
*/	
		System.out.println("######Asset Weights Calculation Started "+userId);
		BasicMatrix finalAssetWeights = bl.getAssetWeights();
		System.out.println("###### Asset Weights "+finalAssetWeights);
		System.out.println("\n");
		LinkedHashMap<String, Double> assetClassWiseWeight = new LinkedHashMap<>();
		long i = 0;
		for(String assetClass:indexToEtfMap.keySet()){
			if(finalAssetWeights.doubleValue(i)>0)
				assetClassWiseWeight.put(assetClass, finalAssetWeights.doubleValue(i));
			i++;
		}
		Map<String,Portfolio> map = buildPortfolio(profile,investment,assetClassWiseWeight,clientId,dummy,date);
		System.out.println(" ###### Building Portfolio Done "+userId);
		System.out.println(" Done !!!! ");
		return map;		
	}

	
	private List<BigDecimal> getMarketWeight(InvestorProfile profile){
		List<BigDecimal> weights = new ArrayList<>();
		if(profile.getRiskTolerance().equals(RiskTolerance.HIGH)){
			weights.add(new BigDecimal(0.9));
			weights.add(new BigDecimal(0.9));
			weights.add(new BigDecimal(0.9));
			weights.add(new BigDecimal(0.9));
			weights.add(new BigDecimal(0));
			weights.add(new BigDecimal(0));
		}
		else if(profile.getRiskTolerance().equals(RiskTolerance.MODERATE)){
				
			
				weights.add(new BigDecimal(0.9));
				weights.add(new BigDecimal(0.9));
				weights.add(new BigDecimal(0.9));
				weights.add(new BigDecimal(0.9));
				weights.add(new BigDecimal(0));
				weights.add(new BigDecimal(0));
		}
		else if(profile.getRiskTolerance().equals(RiskTolerance.LOW)){
				weights.add(new BigDecimal(0));
				weights.add(new BigDecimal(0));
				weights.add(new BigDecimal(0));
				weights.add(new BigDecimal(0));
				weights.add(new BigDecimal(0));
				weights.add(new BigDecimal(0));
		}
			
		return weights;
	}
	
	private LinkedHashMap<String,String> getassetETFMap(){
		LinkedHashMap<String,String> assetETFMap = new LinkedHashMap<>();
		assetETFMap.put("CRSPTM1","VTI");
		assetETFMap.put("CRSPLC1","VTV");
		assetETFMap.put("CRSPMI1","VOE");
		assetETFMap.put("CRSPSC1","VBR");
		assetETFMap.put("SHV","SHV");
		assetETFMap.put("LQD","LQD");
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
		tickers[5] = "SHV";
		tickers[4] = "LQD";
		return tickers;
	}
	double[][] getMarketWeight(Date date){
		double[][] marketWeight = new double[6][1];
		double[] totalValue = new double[6]; 
		List<MarketWeight> marketCap = portfolioRepository.getMarketWeight(date.getYear()+1900);
		ArrayList<String> files = new ArrayList<>(6);
		files.add("VTI");
		files.add("VTV");
		files.add("VOE");
		files.add("VBR");
		files.add("SHV");
		files.add("LQD");
		int i = 0;
		for(String etf:files){
			for(MarketWeight cap:marketCap){
				if(cap.getEtf().equals(etf)){
					BigDecimal bd = new BigDecimal(cap.getWeight());
					totalValue[i] = bd.doubleValue();
				}
			}
			i++;
		}
		double total=0;
		System.out.println("####### Started Printing market cap #####");
		for(double d:totalValue){
			System.out.println(d);
			total+=d;
		}
		System.out.println("####### Done Printing market cap #####");
		System.out.println("\n");
		marketWeight[0][0]=totalValue[0]/total;
		marketWeight[1][0]=totalValue[1]/total;
		marketWeight[2][0]=totalValue[2]/total;
		marketWeight[3][0]=totalValue[3]/total;
		marketWeight[4][0]=totalValue[4]/total;
		marketWeight[5][0]=totalValue[5]/total;
		return marketWeight;
	}
	BasicMatrix getCovarianceMatrix(Date date){
		Collection<CalendarDateSeries<Double>> col = new ArrayList<>();
		col.add(getCalendarDataSeriesFromDatabase("CRSPTM1",date));
		col.add(getCalendarDataSeriesFromDatabase("CRSPLC1",date));
		col.add(getCalendarDataSeriesFromDatabase("CRSPMI1",date));
		col.add(getCalendarDataSeriesFromDatabase("CRSPSC1",date));
		col.add(getCalendarDataSeriesFromDatabase("SHV",date));
		col.add(getCalendarDataSeriesFromDatabase("LQD",date));
		BasicMatrix covarianceMatrix = FinanceUtils.makeCovarianceMatrix(col);
		return covarianceMatrix;
	}
	public CalendarDateSeries<Double> getCalendarDataSeriesFromDatabase(String symbol,Date portfolioDate){
		CalendarDateSeries<Double> series = new CalendarDateSeries<Double>(CalendarDateUnit.DAY).name(symbol);
		try{
			List<IndexPrice> indexPriceList = portfolioRepository.getIndexPrice(symbol,portfolioDate);
			for(IndexPrice indexPrice:indexPriceList){
				CalendarDate key = new CalendarDate(indexPrice.getDate());
				
				NumberFormat numberFormat = NumberFormat.getInstance(java.util.Locale.US);
				series.put(key, indexPrice.getPrice());
				
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return series;
	}

	public Map<String,Portfolio> buildPortfolio(InvestorProfile profile,int investment,LinkedHashMap<String, Double> assetClassWiseWeight,int clientId,boolean dummy,Date date){
		Portfolio portfolio = new Portfolio();
		portfolio.setClientId(clientId);
		portfolio.setTransactionDate(date);
		List<Allocation> allocationList = new ArrayList<>();
		Map<String, Portfolio> portfolioMap = new HashMap<>();
		for(String assetClass:indexToEtfMap.keySet()){
			if(!assetClassWiseWeight.containsKey(assetClass)) continue;
			Allocation allocation = new Allocation();
			String etf = indexToEtfMap.get(assetClass);
			Fund fund  = fundRepository.findFund(etf);
	 		Double cost = investment*assetClassWiseWeight.get(assetClass);
	 		Calendar cal = Calendar.getInstance();
	 		cal.setTime(date);
	 		cal = trimTime(cal);
			double perIndexCost = portfolioRepository.getIndexPriceForGivenDate(indexToEtfMap.get(assetClass), cal.getTime());
 			NumberFormat nf = NumberFormat.getInstance();
 			int quantity = Double.valueOf((cost/perIndexCost)).intValue();
 			System.out.println("Asset Class: "+assetClass+" Weight: "+assetClassWiseWeight.get(assetClass)+" Value: "+cost +" PerIndexCost: "+perIndexCost+" Quantity:"+quantity);
 			if(quantity==0)continue;
 			allocation.setQuantity(Double.valueOf((cost/perIndexCost)).intValue());
 			allocation.setCostPrice(perIndexCost);
 			allocation.setInvestment(investment);
 			allocation.setPercentage(assetClassWiseWeight.get(assetClass)*100);
 			allocation.setType(allocationTypeMap.get(etf).name());
 			allocation.setTransactionDate(date);
 			allocation.setBuyDate(date);
 			allocation.setIsActive("Y");
 			fund.setTicker(etf);
 			allocation.setFund(fund);
 			allocation.setRebalanceDayPrice(perIndexCost);
 			allocation.setRebalanceDayPerc(assetClassWiseWeight.get(assetClass)*100);
 			allocation.setRebalanceDayQuantity(quantity);
 			allocation.setCreatedBy(CreatedBy.PORT.name());
 			allocation.setPortfolio(portfolio);
 			allocationList.add(allocation);
		}
		portfolio.setAllocations(allocationList);
		
		portfolio.setInvestorProfile(profile);
		portfolioRepository.persist(portfolio);
		
		portfolioMap.put(clientId+"", portfolio);
		return portfolioMap;
	}

	private double getIndexPriceForDate(List<Data> dataList,Date date){
		double perIndexCost = 0d;
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DATE, -1);
		
		cal = trimTime(cal);
		for(Data data:dataList){
			Calendar dateCal = trimTime(data.getKey().toCalendar());
			dateCal = trimTime(dateCal);
			if(trimTime(data.getKey().toCalendar()).equals(cal)){
				perIndexCost = data.getPrice();
				System.out.println("price : "+perIndexCost);
				break;
			}
		}
		return perIndexCost;
	}
	@Override
	public void deleteAllPortfolio() {
		List<Portfolio> listOfPortfolios =  portfolioRepository.getAllPortfolios();
		
		for(Portfolio port:listOfPortfolios)portfolioRepository.delete(port);
		List<InvestorProfile> listInvestorProfiles =  portfolioRepository.getAllInvestorProfile();
		for(InvestorProfile obj:listInvestorProfiles)portfolioRepository.delete(obj);
		List<UserClientMapping> listUsers =  portfolioRepository.getAllUsers();
		for(UserClientMapping obj:listUsers)portfolioRepository.delete(obj);
		List<Transaction> listOfTransactions =  portfolioRepository.getAllTransactions();
		for(Transaction obj:listOfTransactions)portfolioRepository.delete(obj);
	}

	@Override
	public InvestorProfile createProfile(ProfileRequest profileRequest) {
		InvestorProfile profile = new InvestorProfile();
		profile.setAnnualIncome(profileRequest.getIncome());
		profile.setRiskTolerance(RiskTolerance.valueOf(profileRequest.getRisk().toUpperCase()));
		int horizonOffsetYear = profileRequest.getTime();
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, horizonOffsetYear);
		profile.setHorizonAsOfDate(cal.getTime());
		profile.setInvestmentHorizonInMonths(horizonOffsetYear*12);
		portfolioRepository.persist(profile);
		return profile;
	}
	
	private Calendar trimTime(Calendar cal){
		cal.set(Calendar.HOUR_OF_DAY,0);
		cal.set(Calendar.MINUTE,0);
		cal.set(Calendar.SECOND,0);
		cal.set(Calendar.MILLISECOND,0);
	//	cal.set(Calendar.ZONE_OFFSET,0);
		return cal;
	}
	
/*	public double getYearStartValueOfPortfolio(Portfolio portfolio,Date date){
		double yearStartValue = 0d;
		List<Allocation> allocationList = portfolio.getAllocations();
		for(Allocation allocation:allocationList){
			String ticker = allocation.getFund().getTicker();
			int quantity = allocation.getQuantity();
			GoogleSymbol gs = new GoogleSymbol(ticker);
	 		List<Data> dataList = gs.getHistoricalPrices();
 			double perIndexCost = getIndexPriceForDate(dataList, date);
 			yearStartValue+=perIndexCost*quantity;
		}
		
	}*/
}
