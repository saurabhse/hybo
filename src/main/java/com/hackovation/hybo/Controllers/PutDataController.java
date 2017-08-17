package com.hackovation.hybo.Controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.hack17.hybo.domain.Fund;
import com.hack17.hybo.domain.IndexPrice;
import com.hack17.hybo.domain.MarketStatus;
import com.hack17.hybo.domain.MarketWeight;
import com.hack17.hybo.domain.Portfolio;
import com.hack17.hybo.repository.PortfolioRepository;

@RestController
@RequestMapping(value="/process")
@Transactional
public class PutDataController {

	@Autowired
	PortfolioRepository portfolioRepository;
	
	@RequestMapping(method=RequestMethod.GET,value="/index")
	public void putIndexData() {
		System.out.println("Started processing historical data");
		StopWatch stopWatch = new StopWatch("Historical Data Persistence");
		stopWatch.start();
		processFiles();
		stopWatch.stop();
		System.out.println(stopWatch.shortSummary());
	}
	@RequestMapping(method=RequestMethod.GET,value="/mcap")
	public void putMarketCapData() {
		System.out.println("Started processing market cap data");
		StopWatch stopWatch = new StopWatch("Market Cap Persistence");
		stopWatch.start();
		processMarketCap();
		stopWatch.stop();
		System.out.println(stopWatch.shortSummary());
	}
	@RequestMapping(method=RequestMethod.GET,value="/fund")
	public void putFundTickers() {
		System.out.println("Started processing fund tickers");
		StopWatch stopWatch = new StopWatch("Fund Tickers Persistence");
		stopWatch.start();
		putFund();
		stopWatch.stop();
		System.out.println(stopWatch.shortSummary());
	}
	
	public void processFiles(){
		processFileAndPushInDatabase("CRSP_US_Large_Cap_Historical_Rates.csv","CRSPTM1");
		processFileAndPushInDatabase("CRSP_US_Mid_Cap_Historical_Rates.csv","CRSPLC1");
		processFileAndPushInDatabase("CRSP_US_Small_Cap_Historical_Rates.csv","CRSPMI1");
		processFileAndPushInDatabase("CRSP_US_Total_Market_Historical_Rates.csv","CRSPSC1");
		processFileAndPushInDatabaseOverloaded("shv_Historical_Rates.csv","SHV");
		processFileAndPushInDatabaseOverloaded("lqd_Historical_Rates.csv","LQD");
	}
	public void processMarketCap(){
		List<Integer> yearList = Arrays.asList(2000,2001,2002,2003,2004,2005,2006,2007,2008,2009,2010,2011,2012,2013,2014,2015,2016,2017);
		Map<String,Double> dataMap = new LinkedHashMap<>();
		dataMap.put("VTI",27755725d);
		dataMap.put("VTV",23745840d);
		dataMap.put("VOE",4037360d);
		dataMap.put("VBR",3744125d);
		dataMap.put("SHV",13908000d);
		dataMap.put("LQD",175000d);
		Set<Entry<String,Double>> entrySet = dataMap.entrySet();
		for(Entry<String,Double> entry:entrySet){
			for(Integer year:yearList){
				MarketWeight cap = new MarketWeight();
				cap.setYear(year);
				cap.setEtf(entry.getKey());
				cap.setWeight(entry.getValue());
				portfolioRepository.persist(cap);
			}
		}
		MarketStatus mw = new MarketStatus();
		mw.setGoingDown(false);
		mw.setFluctuating(false);
		mw.setGoingUp(true);
		portfolioRepository.persist(mw);
	}
	
	public void putFund(){
		List<String> tickers = Arrays.asList("VTI","VTV","VOE","VBR","SHV","LQD");
		for(String tick:tickers){
			Fund f = new Fund(tick);
			portfolioRepository.persist(f);
		}
	}
	public void processFileAndPushInDatabase(String fileName,String index){
		try{
			SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
			DecimalFormat df = new DecimalFormat("##########,###################.#############");
			ClassLoader cl = getClass().getClassLoader();
			File file = new File(cl.getResource(fileName).getFile());
			String l = "D:\\MATERIAL\\Hackathon\\Hackovation 2.0\\selected\\hybo\\Workspace\\hybo\\target\\classes\\";
			BufferedReader f = new BufferedReader(new FileReader(l+fileName));
			String ln=null;
			f.readLine();
			while((ln=f.readLine())!=null){
				String[] data = ln.split("\",\"");
				Date date = sdf.parse(removeQuote(data[0]));
				double price = df.parse(removeQuote(data[1])).doubleValue();
				double open = df.parse(removeQuote(data[2])).doubleValue();
				double high = df.parse(removeQuote(data[3])).doubleValue();
				double low = df.parse(removeQuote(data[4])).doubleValue();
				int volumn = Integer.valueOf(removeQuote(data[5]));
				double change = df.parse(removeQuote(data[6])).doubleValue();
				IndexPrice indexPrice = new IndexPrice();
				indexPrice.setDate(date);
				indexPrice.setChange(change);
				indexPrice.setHigh(high);
				indexPrice.setLow(low);
				indexPrice.setOpen(open);
				indexPrice.setPrice(price);
				indexPrice.setVolumn(volumn);
				indexPrice.setIndex(index);
				portfolioRepository.persist(indexPrice);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public void processFileAndPushInDatabaseOverloaded(String fileName,String index){
		try{
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
			DecimalFormat df = new DecimalFormat("##########,###################.#############");
			ClassLoader cl = getClass().getClassLoader();
			File file = new File(cl.getResource(fileName).getFile());
			String l = "D:\\MATERIAL\\Hackathon\\Hackovation 2.0\\selected\\hybo\\Workspace\\hybo\\target\\classes\\";
			BufferedReader f = new BufferedReader(new FileReader(l+fileName));
			String ln=null;
			f.readLine();
			while((ln=f.readLine())!=null){
				String[] data = ln.split(",");
				Date date = sdf.parse(removeQuote(data[0]));
				double price = df.parse(removeQuote(data[4])).doubleValue();
				double open = df.parse(removeQuote(data[1])).doubleValue();
				double high = df.parse(removeQuote(data[2])).doubleValue();
				double low = df.parse(removeQuote(data[3])).doubleValue();
				int volumn = Integer.valueOf(removeQuote(data[5]));
			//	double change = df.parse(removeQuote(data[6])).doubleValue();
				IndexPrice indexPrice = new IndexPrice();
				indexPrice.setDate(date);
				indexPrice.setChange(0);
				indexPrice.setHigh(high);
				indexPrice.setLow(low);
				indexPrice.setOpen(open);
				indexPrice.setPrice(price);
				indexPrice.setVolumn(volumn);
				indexPrice.setIndex(index);
				portfolioRepository.persist(indexPrice);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private String removeQuote(String s){
		return s.replaceAll("\"","");
	}
}
