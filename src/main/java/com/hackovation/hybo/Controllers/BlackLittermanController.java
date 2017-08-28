
package com.hackovation.hybo.Controllers;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hack17.hybo.domain.Allocation;
import com.hack17.hybo.domain.CurrentDate;
import com.hack17.hybo.domain.InvestorProfile;
import com.hack17.hybo.domain.Portfolio;
import com.hack17.hybo.domain.TLHRunPortfolioHistory;
import com.hack17.hybo.domain.UserClientMapping;
import com.hack17.hybo.repository.PortfolioRepository;
import com.hackovation.hybo.CreatedBy;
import com.hackovation.hybo.Util.HyboUtil;
import com.hackovation.hybo.bean.Data;
import com.hackovation.hybo.bean.DataVO;
import com.hackovation.hybo.bean.EtfParent;
import com.hackovation.hybo.bean.PortfolioResponse;
import com.hackovation.hybo.bean.Price;
import com.hackovation.hybo.bean.ProfileRequest;
import com.hackovation.hybo.bean.ProfileResponse;
import com.hackovation.hybo.bean.RebalanceResponse;
import com.hackovation.hybo.bean.TLHInternalResponse;
import com.hackovation.hybo.bean.TLHResponse;
import com.hackovation.hybo.bean.Value;
import com.hackovation.hybo.rebalance.Rebalance;
import com.hackovation.hybo.services.PortfolioService;

@RestController
@RequestMapping(value="/black")
@CrossOrigin
public class BlackLittermanController {
	
	@Autowired
	PortfolioRepository portfolioRepository;
	@Autowired
	PortfolioService portfolioService;
	
	@Autowired
	Rebalance rebalance;
	
	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	NumberFormat numFormat = new DecimalFormat("#########.##");
	SimpleDateFormat rebFor = new SimpleDateFormat("yyyy-MM");
	
	@RequestMapping(value="/validateUser", method=RequestMethod.GET,produces="application/json")
	public @ResponseBody String validateUser(@RequestParam(name="userId")String userId) throws JsonProcessingException{
		String str;
		Object object = portfolioRepository.getEntityForAny(UserClientMapping.class,userId);
		if(object==null){
			ProfileResponse response = new ProfileResponse();
			response.setLabel("0");
			ObjectMapper responseMapper = new ObjectMapper();
			str = responseMapper.writeValueAsString(response);
			return str;
		}else{
			UserClientMapping existingObject = (UserClientMapping)object;
			ProfileResponse response = new ProfileResponse();
			response.setLabel("1");
			ObjectMapper responseMapper = new ObjectMapper();
			str = responseMapper.writeValueAsString(response);
			return str;
		}
	}

	@RequestMapping(value="/createProfile", method=RequestMethod.POST,produces = "application/json",consumes =  MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody String createProfileAndCreatePortfolio(HttpEntity<String> entity){
		String str = "";
		StopWatch stopWatch = new StopWatch("Started Building Profile!!!");
		stopWatch.start();
		try {
			String json = entity.getBody();
			ObjectMapper mapper = new ObjectMapper();
			ProfileRequest profileRequest;
			profileRequest = mapper.readValue(json, ProfileRequest.class);
			String userId = profileRequest.getUserId();
			InvestorProfile investorProfile = portfolioService.createProfile(profileRequest);
			Date date = new Date();
			if(profileRequest.getDate() != null && !profileRequest.getDate().isEmpty()){
				date = sdf.parse(profileRequest.getDate());
			}else{
				CurrentDate existingDate = (CurrentDate)portfolioRepository.getEntity(1, CurrentDate.class);
				date = existingDate.getDate();
			}
			Map<String,Portfolio> dataMap = createPortfolio(investorProfile, profileRequest.getAmount(),date,userId);
			
			double totalValue = 0.0;
			List<ProfileResponse> responseList = new ArrayList<>();
			Set<Entry<String,Portfolio>> entrySet = dataMap.entrySet();
			Map<String,String> etfAssetClassMap = HyboUtil.ETFToAssetClassMap();
			for(Entry<String,Portfolio> entry:entrySet){
				Portfolio portfolio= entry.getValue();
				List<Allocation> allocationList = portfolio.getAllocations();
				for(Allocation allocation:allocationList){
					ProfileResponse response = new ProfileResponse();
					response.setClientId(Integer.valueOf(entry.getKey()));
					response.setLabel(etfAssetClassMap.get(allocation.getFund().getTicker()));
					response.setValue(String.valueOf(allocation.getPercentage()));
					totalValue += allocation.getQuantity()*allocation.getCostPrice();
					responseList.add(response);
				}
			}
			PortfolioResponse response = new PortfolioResponse();
			response.setTotal(Double.valueOf(numFormat.format(totalValue)));
			response.setData(responseList);
			ObjectMapper responseMapper = new ObjectMapper();
			str = responseMapper.writeValueAsString(response);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		stopWatch.stop();
		System.out.println(stopWatch.shortSummary());
		return str;
	}
	
	@RequestMapping(value="/getAllocation", method=RequestMethod.GET,produces = "application/json")
	public @ResponseBody String getAllocation(@RequestParam(name="userId") String userId) throws JsonProcessingException{
		int clientId = getClientId(userId);
		String str = "No Data To Display";
		List<Portfolio>	portfolioList = portfolioRepository.getPortfolio(Integer.valueOf(clientId));
		Portfolio portfolio = portfolioList.get(0);
		List<ProfileResponse> responseList = new ArrayList<>();
		double totalValue = 0.0;
		Map<String,String> etfAssetClassMap = HyboUtil.ETFToAssetClassMap();
		List<Allocation> allocationList = portfolio.getAllocations();
		for(Allocation allocation:allocationList){
			ProfileResponse response = new ProfileResponse();
			response.setClientId(clientId);
			response.setLabel(etfAssetClassMap.get(allocation.getFund().getTicker()));
			response.setValue(String.valueOf(allocation.getPercentage()));
			totalValue += allocation.getQuantity()*allocation.getCostPrice();
			responseList.add(response);
		}
		PortfolioResponse response = new PortfolioResponse();
		response.setTotal(Double.valueOf(numFormat.format(totalValue)));
		response.setData(responseList);
		ObjectMapper responseMapper = new ObjectMapper();
		str = responseMapper.writeValueAsString(response);
		return str;
	}
	public Map<String,Portfolio> createPortfolio(InvestorProfile profile,int investment,Date date,String userId) throws Exception{
		System.out.println("Started creating portfolio for client: "+userId+" For Date: "+date );
		return portfolioService.buildPortfolio(profile,userId,false,date,investment);
	}
	
	@RequestMapping(value="/getPortfolioCid", method=RequestMethod.GET,produces = "application/json")
	public @ResponseBody String getPortfolioForClientIdAsPerAmount(@RequestParam(name="userId") String userId){
		String str = "No Data To Display";
		try{
			int clientId = getClientId(userId);
			List<Portfolio>	portfolioList = portfolioRepository.getPortfolio(Integer.valueOf(clientId));
			Portfolio portfolio = portfolioList.get(0);
			
			List<ProfileResponse> responseList = new ArrayList<>();
			List<Allocation> allocationList = portfolio.getAllocations();
			
			Calendar cal = Calendar.getInstance();
			CurrentDate existingDate = (CurrentDate)portfolioRepository.getEntity(1, CurrentDate.class);
			Date date = existingDate.getDate();
			cal.setTime(date);
	 		cal = trimTime(cal);
	 		double totalValue=0.0;
			for(Allocation allocation:allocationList){
				if(allocation.getCostPrice()==0d || allocation.getIsActive().equals("N"))continue;
				double latestPrice = portfolioRepository.getIndexPriceForGivenDate(allocation.getFund().getTicker(), cal.getTime());
				ProfileResponse response = new ProfileResponse();
			//	System.out.println("Old Value "+allocation.getFund().getTicker()+","+allocation.getCostPrice());
				response.setClientId(Integer.valueOf(clientId));
				response.setLabel(allocation.getFund().getTicker()+"("+(allocation.getType().substring(0,1))+")");
				response.setValue(String.valueOf(latestPrice*allocation.getQuantity()));
			//	System.out.println("New Value "+allocation.getFund().getTicker()+","+latestPrice);
				totalValue += allocation.getQuantity()*latestPrice;
				responseList.add(response);
			}
			PortfolioResponse response = new PortfolioResponse();
			response.setTotal(Double.valueOf(numFormat.format(totalValue)));
			response.setData(responseList);
			System.out.println("Total value of portfolio for user "+userId+" as of date "+cal.getTime()+" is: "+totalValue);
			ObjectMapper responseMapper = new ObjectMapper();
			str = responseMapper.writeValueAsString(response);

			
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return str;
	}

	
	/*@RequestMapping(value="/getTLHCid", method=RequestMethod.GET,produces = "application/json")
	public @ResponseBody String getTLHListGivenUser(@RequestParam(name="userId") String userId) throws JsonProcessingException{
		int clientId = getClientId(userId);
		
		List<Portfolio>	portfolioList = portfolioRepository.getPortfolio(Integer.valueOf(clientId));
		Portfolio portfolio = portfolioList.get(0);
		MyComparator comparator = new MyComparator();
		
		//Preparing data ticker wise and sorted by date
		Map<String,List<Allocation>> filteredMap = new HashMap<>();
		for(Allocation allocation:portfolio.getAllocations()){
			if(!allocation.getCreatedBy().equals(CreatedBy.TLH.name()))continue;
			String key = allocation.getFund().getTicker();
			List<Allocation> dataList = filteredMap.get(key);
			if(dataList==null) dataList = new LinkedList<>();
			dataList.add(allocation);
			Collections.sort(dataList,comparator);
			filteredMap.put(key,dataList);
		}
		
		
		List<TLHResponse> response = new ArrayList<>();
		
		processTLHData(response,filteredMap);
		ObjectMapper responseMapper = new ObjectMapper();
		String str = responseMapper.writeValueAsString(response);
		System.out.println(str);
		return str;
	}*/
	
	@RequestMapping(value="/getTLHCid", method=RequestMethod.GET,produces = "application/json")
	public @ResponseBody String getTLHListGivenUser(@RequestParam(name="userId") String userId) throws JsonProcessingException{
		int clientId = getClientId(userId);
		
		List<Portfolio>	portfolioList = portfolioRepository.getPortfolio(Integer.valueOf(clientId));
		Portfolio portfolio = portfolioList.get(0);
		
		List<TLHRunPortfolioHistory> tlhRunHistory = portfolioRepository.getTLHRunHistory(portfolio.getId());
		
		
		ObjectMapper responseMapper = new ObjectMapper();
		String str = responseMapper.writeValueAsString(tlhRunHistory);
		System.out.println(str);
		return str;
	}
	
	private void processTLHData(List<TLHResponse> response,Map<String,List<Allocation>> filteredMap){
		Set<String> keys = filteredMap.keySet();
		TLHResponse responseList = new TLHResponse();
		boolean processFurther = false;
		for(String etf:keys){
			List<Allocation> allocationList = filteredMap.get(etf);
			if(allocationList != null&& allocationList.size()>0){
				TLHInternalResponse responseObject = new TLHInternalResponse();
				responseObject.setTicker(etf);
				Date prevDate = null;
				Iterator<Allocation> iter = allocationList.iterator();
				while(iter.hasNext())
				{
					Allocation allocation = iter.next();
					Date transactionDate = allocation.getTransactionDate();
					if(prevDate != null && prevDate != transactionDate){
						processFurther=true;
						break;
					}
					responseObject.setPrice(numFormat.format(allocation.getRebalanceDayPrice()));
					responseObject.setDate(rebFor.format(allocation.getBuyDate()));
					responseObject.setValue(numFormat.format(allocation.getRebalanceDayQuantity()*allocation.getRebalanceDayPrice()));
					responseObject.setQuantity(String.valueOf(allocation.getRebalanceDayQuantity()));
					responseObject.setTlhPrice(numFormat.format(allocation.getCostPrice()));
					responseObject.setTlhValue(numFormat.format(allocation.getCostPrice()*allocation.getQuantity()));
					responseObject.setTlhQuantity(String.valueOf(allocation.getQuantity()));
					iter.remove();
					prevDate  =allocation.getTransactionDate();
				}
				responseList.getIternalList().add(responseObject);
			}
		}
		response.add(responseList);
		if(processFurther)
			processTLHData(response,filteredMap);
	}
	
	@RequestMapping(value="/getRebalanceCid", method=RequestMethod.GET,produces = "application/json")
	public @ResponseBody String getRebalancingListForGivenUser(@RequestParam(name="userId") String userId){
		String str = "No Data To Display";
		NumberFormat numFormat = new DecimalFormat("#########.##");
		NumberFormat percFormat = new DecimalFormat("##.##");
		StringBuffer sb = new StringBuffer("[");
		SimpleDateFormat rebFor = new SimpleDateFormat("yyyy-MM");
		try{
			int clientId = getClientId(userId);
			List<Portfolio>	portfolioList = portfolioRepository.getPortfolio(Integer.valueOf(clientId));
			Portfolio portfolio = portfolioList.get(0);
			
			MyComparator comparator = new MyComparator();
			
			//Preparing data ticker wise and sorted by date
			Map<String,List<Allocation>> filteredMap = new HashMap<>();
			for(Allocation allocation:portfolio.getAllocations()){
				if(allocation.getCreatedBy().equals(CreatedBy.TLH.name()))continue;
				String key = allocation.getFund().getTicker();
				List<Allocation> dataList = filteredMap.get(key);
				if(dataList==null) dataList = new LinkedList<>();
				dataList.add(allocation);
				Collections.sort(dataList,comparator);
				filteredMap.put(key,dataList);
			}
			Map<String,Set<DataVO>> rolledUpMap = new HashMap<>();
			Set<String> keySet = filteredMap.keySet();
			for(String etf:keySet){
				List<Allocation> allAllocations = filteredMap.get(etf);
				Allocation prevAllocation = null;
				Set<DataVO> dataSet = new LinkedHashSet<>();
				DataVO data = null;
				for(Allocation allocation:allAllocations){
					
					if(prevAllocation != null && prevAllocation.getCreatedBy().equals(allocation.getCreatedBy()) &&
							prevAllocation.getTransactionDate().equals(allocation.getTransactionDate())){
						data.setPerEtfPrice(data.getPerEtfPrice());
						data.setQuantity(data.getQuantity()+allocation.getQuantity());
						data.setValue(data.getValue()+ (allocation.getQuantity()*allocation.getCostPrice()));
						data.setPerc(data.getPerc()+allocation.getPercentage());
					}else{
						if(data!=null)
							dataSet.add(data);
						data = new DataVO();
						data.setEtf(allocation.getFund().getTicker());
						data.setQuantity(allocation.getQuantity());
						data.setPerc(allocation.getPercentage());
						data.setPerEtfPrice(allocation.getCostPrice());
						data.setValue(allocation.getQuantity()*allocation.getCostPrice());
						data.setTransactionDate(allocation.getTransactionDate());
					}
					
					prevAllocation = allocation;
				}
				if(data!=null)
					dataSet.add(data);
				/*
				double portfolioValue = 0.0;
				for(DataVO tempData  :dataSet){
					portfolioValue += tempData.getValue();
				}
				for(DataVO tempData  :dataSet){
					double value = tempData.getValue();
					try{
						tempData.setPerc(value*100/portfolioValue);
					}
					catch(Exception e){
						e.printStackTrace();
						tempData.setPerc(0);
					}
				}*/
				rolledUpMap.put(etf, dataSet);
				
			}
			
			
			keySet = rolledUpMap.keySet();
			RebalanceResponse response = new RebalanceResponse();
			List<EtfParent> parentList = response.getName();
			for(String etf:keySet){
				EtfParent parent = new EtfParent();
				parent.setName(etf);
				List<Data> dataList = parent.getData();
				List<com.hackovation.hybo.bean.Allocation> allocatiobList = parent.getAllocation();
				List<Price> priceList = parent.getPrice();
				List<Value> valueList = parent.getValue();
				Set<DataVO> dataVo =rolledUpMap.get(etf);
				int i=0;
				for(DataVO data:dataVo){
					Data dataObj = new Data();
					String dateAppender = rebFor.format(data.getTransactionDate());
					if(i!=0)
						dateAppender += "(Rebalance)";
					dataObj.setLabel(dateAppender);
					dataList.add(dataObj);
				}
				
				
				for(DataVO data:dataVo){
					Value valueObj = new Value();
					String value = numFormat.format(data.getValue());
					if(i!=0)
						value += "(Rebalance)";
					valueObj.setValue(value);
					valueList.add(valueObj);
				}
				
				for(DataVO data:dataVo){
					Price price = new Price();
					String value = numFormat.format(data.getPerEtfPrice());
					if(i!=0)
						value += "(Rebalance)";
					price.setValue(value);
					priceList.add(price);
				}
				
				for(DataVO data:dataVo){
					com.hackovation.hybo.bean.Allocation allocation = new com.hackovation.hybo.bean.Allocation();
					String value = percFormat.format(data.getPerc());
					if(i!=0)
						value += "Rebalance";
					allocation.setValue(value);
					allocatiobList.add(allocation);
				}
				parentList.add(parent);
			}
			ObjectMapper responseMapper = new ObjectMapper();
			str = responseMapper.writeValueAsString(parentList);

			System.out.println(str);
			
/*			List<ProfileResponse> responseList = new ArrayList<>();
			List<Allocation> allocationList = portfolio.getAllocations();
			for(Allocation allocation:allocationList){
				if(allocation.getCostPrice()==0d || allocation.getIsActive().equals("N"))continue;
				ProfileResponse response = new ProfileResponse();
				response.setClientId(Integer.valueOf(clientId));
				response.setLabel(allocation.getFund().getTicker());
				response.setValue(String.valueOf(allocation.getCostPrice()*allocation.getQuantity()));
				responseList.add(response);
			}
			ObjectMapper responseMapper = new ObjectMapper();
			str = responseMapper.writeValueAsString(responseList);

*/			
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return str;
	}	
	
	@RequestMapping(value="/getRebalanceCidOld", method=RequestMethod.GET,produces = "application/json")
	public @ResponseBody String getRebalancingListForGivenUserOld(@RequestParam(name="userId") String userId){
		String str = "No Data To Display";
		NumberFormat numFormat = new DecimalFormat("#########.##");
		NumberFormat percFormat = new DecimalFormat("##.##%");
		StringBuffer sb = new StringBuffer("[");
		SimpleDateFormat rebFor = new SimpleDateFormat("yyyy-MM");
		try{
			int clientId = getClientId(userId);
			List<Portfolio>	portfolioList = portfolioRepository.getPortfolio(Integer.valueOf(clientId));
			Portfolio portfolio = portfolioList.get(0);
			
			MyComparator comparator = new MyComparator();
			
			//Preparing data ticker wise and sorted by date
			Map<String,List<Allocation>> filteredMap = new HashMap<>();
			for(Allocation allocation:portfolio.getAllocations()){
				if(allocation.getCreatedBy().equals(CreatedBy.TLH.name()))continue;
				String key = allocation.getFund().getTicker();
				List<Allocation> dataList = filteredMap.get(key);
				if(dataList==null) dataList = new LinkedList<>();
				dataList.add(allocation);
				Collections.sort(dataList,comparator);
				filteredMap.put(key,dataList);
			}
			
			Set<String> keySet = filteredMap.keySet();
			for(String etf:keySet){
				sb.append("{\"ETF\":\"").append(etf).append("\"");
				List<Allocation> allocationList =filteredMap.get(etf);
				int i=0;
				boolean isRebalanceingDone = false;
				if(allocationList.size()>1)
					isRebalanceingDone=true;
				for(Allocation allocation:allocationList){
					String dateAppender = rebFor.format(allocation.getTransactionDate());
					sb.append(",\"Buying Price\":\"").append(numFormat.format(allocation.getRebalanceDayPrice())).append("\"");
					if(isRebalanceingDone && i!=0)
						sb.append(",\"Price On ").append(dateAppender).append("\":\"").append(numFormat.format(allocation.getCostPrice())).append("\"");
					i++;
				}
				i=0;
				for(Allocation allocation:allocationList){
					String dateAppender = rebFor.format(allocation.getTransactionDate());
					sb.append(",\"Buying Value\":\"").append(numFormat.format(allocation.getRebalanceDayPrice()*allocation.getRebalanceDayQuantity())).append("\"");
					if(isRebalanceingDone && i!=0){
						sb.append(",\"Value On ").append(dateAppender).append("\":\"").append(numFormat.format(allocation.getCostPrice()*allocation.getRebalanceDayQuantity())).append("\"");
						sb.append(",\"Rebalanced Value").append(dateAppender).append("\":\"").append(numFormat.format(allocation.getCostPrice()*allocation.getQuantity())).append("\"");
					}
					i++;
				}
				i=0;
				for(Allocation allocation:allocationList){
					String dateAppender = rebFor.format(allocation.getTransactionDate());
					sb.append(",\"Allocation %\":\"").append(percFormat.format(allocation.getRebalanceDayPerc()/100)).append("\"");
					if(isRebalanceingDone && i!=0){
						sb.append(",\"Allocation % On ").append(dateAppender).append("\":\"").append(percFormat.format(allocation.getRebalanceDayPerc())).append("\"");
						sb.append(",\"Rebalanced Allocation").append(dateAppender).append("\":\"").append(percFormat.format(allocation.getPercentage()/100)).append("\"");
					}
					i++;
				}
				sb.append("},");
			}
			str = sb.substring(0, sb.lastIndexOf(","));
			str +="]";
			sb.append("]");
			
/*			List<ProfileResponse> responseList = new ArrayList<>();
			List<Allocation> allocationList = portfolio.getAllocations();
			for(Allocation allocation:allocationList){
				if(allocation.getCostPrice()==0d || allocation.getIsActive().equals("N"))continue;
				ProfileResponse response = new ProfileResponse();
				response.setClientId(Integer.valueOf(clientId));
				response.setLabel(allocation.getFund().getTicker());
				response.setValue(String.valueOf(allocation.getCostPrice()*allocation.getQuantity()));
				responseList.add(response);
			}
			ObjectMapper responseMapper = new ObjectMapper();
			str = responseMapper.writeValueAsString(responseList);

*/			
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return str;
	}		
	
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

/*	@RequestMapping(method=RequestMethod.GET,value="/getPortfolio")
	public @ResponseBody Map<String,Portfolio> getPortfolio(@RequestParam(name="clientId") String clientId,@RequestParam(name="date") String dateString) throws Exception{
		ClassLoader cl = getClass().getClassLoader();
		File file = new File(cl.getResource("CRSP_US_Total_Market_IndividualMarketValue.txt").getFile());
		BufferedReader f = new BufferedReader(new FileReader(file));
		String ln=null;
		while((ln=f.readLine())!=null){
			//System.out.println(ln);;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		return portfolioService.buildPortfolio(clientId,false,sdf.parse(dateString));
	}
*/
	@RequestMapping(method=RequestMethod.GET,value="/rebalance")
	public void rebalancePortfolio(@RequestParam(name="date",required=false)String dateStr) throws ParseException{
		Date date = new Date();
		if(dateStr!=null){
			date = sdf.parse(dateStr);
		}else{
			CurrentDate existingDate = (CurrentDate)portfolioRepository.getEntity(1, CurrentDate.class);
			date = existingDate.getDate();			
		}
		System.out.println("Started rebalancing For Date: "+date );
		rebalance.rebalance(date);
	}
	
	@RequestMapping(method=RequestMethod.GET,value="/deleteAll")
	public void deleteAllPortfolio(){
		StopWatch stopWatch = new StopWatch("All Portoflio Deletion!!");
		stopWatch.start();
		portfolioService.deleteAllPortfolio();
		stopWatch.stop();
		System.out.println(stopWatch.shortSummary());
	}
	
class MyComparator implements Comparator<Allocation>{

	@Override
	public int compare(Allocation o1, Allocation o2) {
		
		return o1.getTransactionDate().compareTo(o2.getTransactionDate());
	}
	
}

/*	BasicMatrix getCovarianceMatrixOld(){
 		GoogleSymbol gs = new GoogleSymbol("AOR");
		Collection<CalendarDateSeries<Double>> col = new ArrayList<>();
		col.add(gs.getPriceSeries());
		gs = new GoogleSymbol("MDIV");
		col.add(gs.getPriceSeries());
		gs = new GoogleSymbol("AOM");
		col.add(gs.getPriceSeries());
		gs = new GoogleSymbol("PCEF");
		col.add(gs.getPriceSeries());
		gs = new GoogleSymbol("AOA");
		col.add(gs.getPriceSeries());
		BasicMatrix covarianceMatrix = FinanceUtils.makeCovarianceMatrix(col);
		return covarianceMatrix;
	}
	
	//Reference : https://www.fool.com/knowledge-center/how-to-calculate-the-historical-variance-of-stock.aspx
	Double getLambda(){
 		GoogleSymbol referenceData = new GoogleSymbol("OSPTX",CalendarDateUnit.DAY);
		List<Data> historicalPriceReference = referenceData.getHistoricalPrices();
		ArrayList<Double> historicalValuesRefernce = new ArrayList<>();
		for(Data d:historicalPriceReference)
			historicalValuesRefernce.add(d.getPrice());
		ArrayList<Double> logReturnOfMarketPortfolio = new ArrayList<>();
		ArrayList<Double> returnOfMarketPortfolio = new ArrayList<>();

		int size=historicalPriceReference.size();
		for(int i=0;i<size-1;i++){
			returnOfMarketPortfolio.add((historicalValuesRefernce.get(i+1)/historicalValuesRefernce.get(i))*100);
			logReturnOfMarketPortfolio.add(Math.log((historicalValuesRefernce.get(i+1)/historicalValuesRefernce.get(i))*100));
		}
		Double sumOfLogReturns = 0.0;
		for(Double d:logReturnOfMarketPortfolio){
			sumOfLogReturns+=d;
		}
		//1.1
		Double Er = sumOfLogReturns/logReturnOfMarketPortfolio.size();
		
		Double sumOfReturns=0.0;
		for(Double d:returnOfMarketPortfolio)
			sumOfReturns+=d;
		Double averageOfReturns = sumOfReturns/returnOfMarketPortfolio.size();
		ArrayList<Double> differenceBetweenReturnAndAverage = new ArrayList<>();
		for(Double d:returnOfMarketPortfolio){
			differenceBetweenReturnAndAverage.add(Math.pow(d-averageOfReturns,2));
		}
		Double sumOfSquaredDifference=0.0;
		for(Double d:differenceBetweenReturnAndAverage){
			sumOfSquaredDifference+=d;
		}
		//1.2
		Double varianceOfMarketPortfolio  = sumOfSquaredDifference/(Double.valueOf(differenceBetweenReturnAndAverage.size()));
		
		//1.3
		Double Rf = 2.0; // This value need to be retrieved from USA Government
		
		Double lambda = (Er-Rf)/varianceOfMarketPortfolio;
		System.out.println("Returns of Market Portfolio : "+Er);
		System.out.println("Variance of Market Portfolio : "+varianceOfMarketPortfolio);
		System.out.println("Lambda : "+lambda);
		return lambda;
	}
	
    public static BigDecimal getRiskAversionFactor() {
        return BigDecimal.valueOf(3.07);
    }
	
	
	
	// Source: http://stattrek.com/matrix-algebra/covariance-matrix.aspx
	public static void getCovarianceMatrixOld(double[][] dataArray){
		int rows = dataArray.length;
		// Step 1: Deviation Score
		int n=dataArray.length;
		double[][] sparseMatrix = getSparseMatrix(rows);
		double[][] multipliedMatrix = multiplyMatrix(sparseMatrix,dataArray,true,n);
		double[][] a = subtractMatrix(dataArray,multipliedMatrix);
		double[][] a_dash = inverseMatrix(a);
		double[][] V = multiplyMatrix(a_dash, a, true, n);
		printMatrix(V);
		System.out.println("Aman");
		System.out.println(multipliedMatrix);
		
	}
	
	public static double[][] getSparseMatrix(int n){
		double[][] matrix = new double[n][n];
		for(int i=0;i<n;i++){
			for(int j=0;j<n;j++){
				matrix[i][j]=1;
			}
		}
		return matrix;
	}
	public static double[][] multiplyMatrix(double[][] left,double[][] right,boolean divide,int div){
		int m = left.length;
		int n=right[0].length;
		int p=right.length;
		double[][] sol = new double[m][n];
		for(int i=0;i<m;i++){
			for(int j=0;j<n;j++){
				double sum=0;
				for(int k=0;k<p;k++){
					 sum = sum + left[i][k]*right[k][j];
				}
				if(divide)
					sol[i][j]=sum/div;
				else
					sol[i][j]=sum;
			}
		}
		return sol;
	}
	public static double[][] subtractMatrix(double[][] first,double[][] second){
		int m=first.length;
		int n=first[0].length;
		for(int i=0;i<m;i++){
			for(int j=0;j<n;j++){
				first[i][j]=first[i][j]-second[i][j];
			}
		}
		return first;
	}
	public static double[][] inverseMatrix(double[][] data){
		int m=data.length;
		int n=data[0].length;
		double[][] sol = new double[n][m];
		for(int i=0;i<m;i++){
			for(int j=0;j<n;j++){
				sol[j][i]=data[i][j];
			}
		}
		return sol;
	}
	
	public static void printMatrix(double[][] dataArray){
		int m=dataArray.length;
		int n=dataArray[0].length;
		for(int i=0;i<m;i++){
			for(int j=0;j<n;j++){
				System.out.print(dataArray[i][j]+",");;
			}
			System.out.print("\n");
		}
	}
	
	public static void deprecated(){
		String[] tickers = getAssetsTickersOld();
		
	// Step 1. Calculate Covariance Matrix
		BasicMatrix covarianceMatrix = getCovarianceMatrix();
		
		// Step 2. Calculate Lambda (Risk Aversion Factor)
		Double lambda = getLambda();
		
		
		double[][] marketWeight = {{0.25},{0.25},{0.25},{0.25}};
		BasicMatrix marketWeightMatrix =  BigMatrix.FACTORY.rows(marketWeight);
		MarketEquilibrium marketEquilibrium = new MarketEquilibrium(tickers, covarianceMatrix, lambda);
		BlackLittermanModel bl  = new BlackLittermanModel(marketEquilibrium, marketWeightMatrix);
		
		System.out.println("Assets Returns Matrix (Prior)"+bl.getAssetReturns());
		
		System.out.println("Till Here");
		
		
		
		
		
		
//		List<Data> data = gs.getHistoricalPrices();
		HashMap<CalendarDate, Double> aorMap = new HashMap<>();
		for(Data d:data)
			aorMap.put(d.getKey(), d.getValue());

		data = gs.getHistoricalPrices();
		HashMap<CalendarDate, Double> mdivMap = new HashMap<>();

		double[][] dataArray = {{4.0,2.0,0.60},
							{4.2,2.1,0.59},
							{3.9,2.0,0.58},
							{4.3,2.1,0.62},
							{4.1,2.2,0.63}};
		getCovarianceMatrix(dataArray);
	}
	
}
*/

	private Calendar trimTime(Calendar cal){
		cal.set(Calendar.HOUR_OF_DAY,0);
		cal.set(Calendar.MINUTE,0);
		cal.set(Calendar.SECOND,0);
		cal.set(Calendar.MILLISECOND,0);
	//	cal.set(Calendar.ZONE_OFFSET,0);
		return cal;
	}
}
