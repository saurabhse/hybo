package com.hackovation.hybo.Controllers;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hack17.hybo.domain.Allocation;
import com.hack17.hybo.domain.IndexPrice;
import com.hack17.hybo.domain.One;
import com.hack17.hybo.domain.Portfolio;
import com.hack17.hybo.domain.Two;
import com.hack17.hybo.domain.UserClientMapping;
import com.hack17.hybo.repository.PortfolioRepository;
import com.hackovation.hybo.bean.ProfileResponse;
import com.hackovation.hybo.rebalance.Rebalance;
import com.hackovation.hybo.services.PortfolioService;

@RestController
@RequestMapping(value="/test")
@CrossOrigin
public class TestController {
	

	@Autowired
	PortfolioRepository portfolioRepository;
	@Autowired
	PortfolioService portfolioService;
	
	@Autowired
	Rebalance rebalance;
	
	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	NumberFormat numFormat = new DecimalFormat("#########.##");
	SimpleDateFormat rebFor = new SimpleDateFormat("yyyy-MM");	
	
	@RequestMapping(value="/test", method=RequestMethod.GET)
	@Transactional
	public void test(){
		One one= (One)portfolioRepository.getEntity(1, One.class);
		
		Set<Two> twoSet = new HashSet<>();
		Two twoOne = new Two();
		twoOne.setId(99);
		twoOne.setName("aman");
		twoSet.add(twoOne);
		one.setSetOfTwo(twoSet);
		portfolioRepository.merge(one);
	}
	
	
	@RequestMapping(value="/rebalanceTest", method=RequestMethod.GET)
	public void testRebalance(){
		rebalance.test();
	}
	@RequestMapping(value="/showPortfolioProgress", method=RequestMethod.GET,produces = "application/json")
	public void showPortfolioProgress(@RequestParam(name="userId") String userId){
		String str = "No Data To Display";
		try{
			int clientId = getClientId(userId);
			
			List<Portfolio> portfolioList = portfolioRepository.getPortfolio(clientId);
			Portfolio portfolio = portfolioList.get(0);
			Calendar systemDate = Calendar.getInstance();
			Calendar cal = Calendar.getInstance();
			Date date = portfolio.getTransactionDate();
			cal.setTime(date);
	 		cal = trimTime(cal);
	 		double totalValue=0.0;
	 		while(true){
				portfolioList = portfolioRepository.getPortfolioBeforeDate(Integer.valueOf(clientId),cal.getTime());
				portfolio = portfolioList.get(0);
				List<Allocation> allocationList = portfolio.getAllocations();
	 			totalValue = 0.0;
	 			String allocationStr = "";
				for(Allocation allocation:allocationList){
					if(allocation.getCostPrice()==0d || allocation.getIsActive().equals("N"))continue;
					double latestPrice = portfolioRepository.getIndexPriceForGivenDate(allocation.getFund().getTicker(), cal.getTime());
					ProfileResponse response = new ProfileResponse();
				//	System.out.println("Old Value "+allocation.getFund().getTicker()+","+allocation.getCostPrice());
					response.setClientId(Integer.valueOf(clientId));
					response.setLabel(allocation.getFund().getTicker()+"("+(allocation.getType().substring(0,1))+")");
					response.setValue(String.valueOf(latestPrice*allocation.getQuantity()));
					//System.out.println("	Data "+allocation.getFund().getTicker()+", Quantity: "+allocation.getQuantity()+
						//	", old price : "+allocation.getCostPrice()+", Latest price: "+latestPrice+", Value: "+allocation.getQuantity()*latestPrice);
					totalValue += allocation.getQuantity()*latestPrice;
					allocationStr += allocation.getFund().getTicker()+",";
				}
				System.out.println("Total value of portfolio for user "+userId+" as of date "+cal.getTime()+" is: "+totalValue);
				System.out.println("     "+allocationStr);
				cal.add(Calendar.MONTH, 1);
				if(cal.getTime().after(systemDate.getTime()))break;
	 		}
			
		}catch(Exception e){
			e.printStackTrace();
		}
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
	private Calendar trimTime(Calendar cal){
		cal.set(Calendar.HOUR_OF_DAY,0);
		cal.set(Calendar.MINUTE,0);
		cal.set(Calendar.SECOND,0);
		cal.set(Calendar.MILLISECOND,0);
	//	cal.set(Calendar.ZONE_OFFSET,0);
		return cal;
	}
	
	@RequestMapping(value="/reverse", method=RequestMethod.GET)
	@Transactional
	public void reverseDate(){
		List<IndexPrice> indexPriceList= portfolioRepository.getAllIndexPrice();
		for(IndexPrice index:indexPriceList){
			Calendar cal = Calendar.getInstance();
			cal.setTime(index.getDate());
			if(cal.get(Calendar.YEAR)==2014){
				int day = cal.get(Calendar.DAY_OF_YEAR);
				cal.set(Calendar.DAY_OF_YEAR, 365-day);
				index.setDate(cal.getTime());
				portfolioRepository.persist(index);
			}
		}
	}
	
	@RequestMapping(value="/reduce30", method=RequestMethod.GET)
	@Transactional
	public void reduce30Percent(){
		List<IndexPrice> indexPriceList= portfolioRepository.getAllIndexPrice();
		Map<String,Double> reducedPriceMap = new HashMap<>();
		reducedPriceMap.put("CRSPLC1",988.91);
		reducedPriceMap.put("CRSPMI1",1000.18);
		reducedPriceMap.put("CRSPSC1",985.0);
		reducedPriceMap.put("CRSPTM1",982.01);
		reducedPriceMap.put("IEMG",34.33);
		reducedPriceMap.put("IVE",60.54);
		reducedPriceMap.put("IWN",70.29);
		reducedPriceMap.put("IWS",47.73);
		reducedPriceMap.put("LQD",81.98);
		reducedPriceMap.put("MUB",74.97);
		reducedPriceMap.put("SCHB",31.86);
		reducedPriceMap.put("SCHD",25.7);
		reducedPriceMap.put("SCHF",21.98);
		reducedPriceMap.put("SCHP",37.86);
		reducedPriceMap.put("SHV",77.19);
		reducedPriceMap.put("TFI",32.58);
		reducedPriceMap.put("VBR",69.92);
		reducedPriceMap.put("VCIT",59.42);
		reducedPriceMap.put("VDE",90.0);
		reducedPriceMap.put("VEA",28.82);
		reducedPriceMap.put("VIG",52.44);
		reducedPriceMap.put("VOE",57.55);
		reducedPriceMap.put("VTI",67.96);
		reducedPriceMap.put("VTIP",34.54);
		reducedPriceMap.put("VTV",54.38);
		reducedPriceMap.put("VWO",28.41);
		reducedPriceMap.put("XLE",62.48);
		for(IndexPrice index:indexPriceList){
			Calendar cal = Calendar.getInstance();
			cal.setTime(index.getDate());
			if(cal.get(Calendar.YEAR)==2015 && cal.get(Calendar.MONTH)==Calendar.OCTOBER && cal.get(Calendar.DATE)==07){
				index.setPrice(reducedPriceMap.get(index.getIndex()));
				portfolioRepository.persist(index);
			}
		}
	}
	@RequestMapping(value="/reset", method=RequestMethod.GET)
	@Transactional
	public void reset(){
		List<IndexPrice> indexPriceList= portfolioRepository.getAllIndexPrice();
		Map<String,Double> reducedPriceMap = new HashMap<>();
		reducedPriceMap.put("CRSPLC1",1412.73);
		reducedPriceMap.put("CRSPMI1",1428.83);
		reducedPriceMap.put("CRSPSC1",1407.14);
		reducedPriceMap.put("CRSPTM1",1402.87);
		reducedPriceMap.put("IEMG",49.04);
		reducedPriceMap.put("IVE",86.48);
		reducedPriceMap.put("IWN",100.42);
		reducedPriceMap.put("IWS",68.19);
		reducedPriceMap.put("LQD",117.11);
		reducedPriceMap.put("MUB",107.1);
		reducedPriceMap.put("SCHB",45.52);
		reducedPriceMap.put("SCHD",36.71);
		reducedPriceMap.put("SCHF",31.4);
		reducedPriceMap.put("SCHP",54.08);
		reducedPriceMap.put("SHV",110.27);
		reducedPriceMap.put("TFI",46.54);
		reducedPriceMap.put("VBR",99.88);
		reducedPriceMap.put("VCIT",84.88);
		reducedPriceMap.put("VDE",128.57);
		reducedPriceMap.put("VEA",41.17);
		reducedPriceMap.put("VIG",74.92);
		reducedPriceMap.put("VOE",82.21);
		reducedPriceMap.put("VTI",97.09);
		reducedPriceMap.put("VTIP",49.34);
		reducedPriceMap.put("VTV",77.68);
		reducedPriceMap.put("VWO",40.58);
		reducedPriceMap.put("XLE",89.25);		
		for(IndexPrice index:indexPriceList){
			Calendar cal = Calendar.getInstance();
			cal.setTime(index.getDate());
			if(cal.get(Calendar.YEAR)==2015 && cal.get(Calendar.MONTH)==Calendar.OCTOBER && cal.get(Calendar.DATE)==07){
				index.setPrice(reducedPriceMap.get(index.getIndex()));
				portfolioRepository.persist(index);
			}
		}
		
	}
	
}
