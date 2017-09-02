package com.hackovation.hybo.Controllers;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
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
	
}
