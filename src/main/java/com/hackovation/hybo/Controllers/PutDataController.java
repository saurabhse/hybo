package com.hackovation.hybo.Controllers;

import static com.hack17.hybo.util.DateTimeUtil.getDatedd_MMM_yyyy;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.hack17.hybo.domain.CurrentDate;
import com.hack17.hybo.domain.Fund;
import com.hack17.hybo.domain.IndexPrice;
import com.hack17.hybo.domain.MarketStatus;
import com.hack17.hybo.domain.MarketWeight;
import com.hack17.hybo.domain.Portfolio;
import com.hack17.hybo.repository.PortfolioRepository;
import com.hack17.hybo.repository.ReferenceDataRepository;
import com.hackovation.hybo.Util.HyboUtil;

@RestController
@RequestMapping(value="/process")
@Transactional
@CrossOrigin
public class PutDataController {

	@Autowired
	PortfolioRepository portfolioRepository;
	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	
	@Autowired
	ReferenceDataRepository refDataRepo;
	
	@RequestMapping(method=RequestMethod.GET,value="/securityPrices")
	public void putSecurityPrices() {
		System.out.println("Started processing historical data");
		StopWatch stopWatch = new StopWatch("Historical Data Persistence");
		stopWatch.start();
		File priceDir = new File("./data/prices");
		for(File priceFile : priceDir.listFiles()){
			String fileName = priceFile.getName();
			String etf = fileName.substring(0, fileName.indexOf("."));
			System.out.println(etf);
			try(Stream<String> fileLines = Files.lines(Paths.get(priceFile.toURI()))){
				fileLines.forEach(line->{
					String[] lineData = line.split(",");
					if(lineData.length !=6)
						return;
					if(lineData[0].contains("Date"))
						return;
					refDataRepo.createPrice(etf.toUpperCase(), Double.parseDouble(lineData[4]), getDatedd_MMM_yyyy(lineData[0]));
				});
			} catch (IOException e) {
				System.out.println(e.getMessage());// TODO Auto-generated catch block
			}
		}
		stopWatch.stop();
		System.out.println(stopWatch.shortSummary());
	}
	
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
	@RequestMapping(method=RequestMethod.GET,value="/updateDate")
	public void putSystemDate(@RequestParam(name="date") String dateStr) throws ParseException {
		System.out.println("Started adding date");
		StopWatch stopWatch = new StopWatch("Started adding date : "+dateStr);
		stopWatch.start();
		CurrentDate date = new CurrentDate();
		date.setId(1);
		CurrentDate existingDate = (CurrentDate)portfolioRepository.getEntity(1, CurrentDate.class);
		if(existingDate!=null){
			date=existingDate;
		}
		Calendar cal = Calendar.getInstance();
		if(!StringUtils.isEmpty(dateStr))
		{
			cal.setTime(sdf.parse(dateStr));
		}
		
		cal = HyboUtil.trimTime(cal);
		date.setDate(cal.getTime());
		portfolioRepository.persist(date);
		stopWatch.stop();
		System.out.println(stopWatch.shortSummary());
	}
	@RequestMapping(method=RequestMethod.GET,value="/getCurrentDate")
	public @ResponseBody String getSystemDate() throws ParseException {
		CurrentDate existingDate = (CurrentDate)portfolioRepository.getEntity(1, CurrentDate.class);
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MMMMM-yyyy");
		String dateStr = "{\"label\":\""+sdf.format(existingDate.getDate())+"\"}";
		return dateStr;
	}

	@RequestMapping(method=RequestMethod.GET,value="/updateMarketStatus")
	public void updateMarketStatus(@RequestParam(name="fluc") String fluctuating,@RequestParam(name="down")String down,@RequestParam(name="up")String up) throws ParseException {
		System.out.println("Updating Market Status");
		StopWatch stopWatch = new StopWatch("Updating Market Status");
		stopWatch.start();
		MarketStatus marketStatus = (MarketStatus)portfolioRepository.getEntity(1, MarketStatus.class);
		marketStatus.setFluctuating(HyboUtil.getBoolean(fluctuating));
		marketStatus.setGoingDown(HyboUtil.getBoolean(down));
		marketStatus.setGoingUp(HyboUtil.getBoolean(up));
		portfolioRepository.persist(marketStatus);
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
		processFileAndPushInDatabaseOverloaded("vti.csv","VTI");
		processFileAndPushInDatabaseOverloaded("vtv.csv","VTV");
		processFileAndPushInDatabaseOverloaded("voe.csv","VOE");
		processFileAndPushInDatabaseOverloaded("vbr.csv","VBR");
		processFileAndPushInDatabaseOverloaded("iemg.csv","IEMG");
		processFileAndPushInDatabaseOverloaded("mub.csv","MUB");
		processFileAndPushInDatabaseOverloaded("schb.csv","SCHB");
		processFileAndPushInDatabaseOverloaded("schd.csv","SCHD");
		processFileAndPushInDatabaseOverloaded("schf.csv","SCHF");
		processFileAndPushInDatabaseOverloaded("schp.csv","SCHP");
		processFileAndPushInDatabaseOverloaded("tfi.csv","TFI");
		processFileAndPushInDatabaseOverloaded("vde.csv","VDE");
		processFileAndPushInDatabaseOverloaded("vea.csv","VEA");
		processFileAndPushInDatabaseOverloaded("vig.csv","VIG");
		processFileAndPushInDatabaseOverloaded("vtip.csv","VTIP");
		processFileAndPushInDatabaseOverloaded("vwo.csv","VWO");
		processFileAndPushInDatabaseOverloaded("xle.csv","XLE");
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
		refDataRepo.createFund("VEA");
		refDataRepo.createFund("VWO");
		refDataRepo.createFund("VIG");
		refDataRepo.createFund("XLE");
		refDataRepo.createFund("SCHP");
		refDataRepo.createFund("MUB");
		refDataRepo.createFund("SCHB");
		refDataRepo.createFund("SCHF");
		refDataRepo.createFund("IEMG");
		refDataRepo.createFund("SCHD");
		refDataRepo.createFund("VDE");
		refDataRepo.createFund("VTIP");
		refDataRepo.createFund("TFI");
		refDataRepo.createFund("IVE");
		refDataRepo.createFund("IWS");
		refDataRepo.createFund("IWN");
		refDataRepo.createFund("VCIT");
		refDataRepo.createCorrelatedFund("VTI", "SCHB");
		refDataRepo.createCorrelatedFund("VEA", "SCHF");
		refDataRepo.createCorrelatedFund("VWO", "IEMG");
		refDataRepo.createCorrelatedFund("VIG", "SCHD");
		refDataRepo.createCorrelatedFund("XLE", "VDE");
		refDataRepo.createCorrelatedFund("SCHP", "VTIP");
		refDataRepo.createCorrelatedFund("MUB", "TFI"); 
		refDataRepo.createCorrelatedFund("VTV", "IVE");
		refDataRepo.createCorrelatedFund("VOE", "IWS");
		refDataRepo.createCorrelatedFund("VBR", "IWN");
		refDataRepo.createCorrelatedFund("LQD", "VCIT");
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
