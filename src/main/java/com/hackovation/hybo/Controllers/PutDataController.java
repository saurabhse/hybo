package com.hackovation.hybo.Controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.hack17.hybo.domain.IndexPrice;
import com.hack17.hybo.domain.Portfolio;
import com.hack17.hybo.repository.PortfolioRepository;

@RestController
@RequestMapping(value="/process")
public class PutDataController {

//	@Autowired
	PortfolioRepository portfolioRepository;
	
	@RequestMapping(method=RequestMethod.GET,value="/index")
	public void putIndexData() {
		processFiles();
	}
	
	@Transactional
	public void processFiles(){
		processFileAndPushInDatabase("CRSP US Large Cap Historical Rates - Investing.com India.csv","CRSPTM1");
		processFileAndPushInDatabase("CRSP US Mid Cap Historical Rates - Investing.com India.csv","CRSPLC1");
		processFileAndPushInDatabase("CRSP US Small Cap Historical Rates - Investing.com India.csv","CRSPMI1");
		processFileAndPushInDatabase("CRSP US Total Market Historical Rates - Investing.com India.csv","CRSPSC1");
	}
	public void processFileAndPushInDatabase(String fileName,String index){
		try{
			SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
			ClassLoader cl = getClass().getClassLoader();
			File file = new File(cl.getResource("fileName").getFile());
			BufferedReader f = new BufferedReader(new FileReader(file));
			String ln=null;
			f.readLine();
			while((ln=f.readLine())!=null){
				String[] data = ln.split("\",\"");
				Date date = sdf.parse(removeQuote(data[0]));
				double price = Double.valueOf(removeQuote(data[1]));
				double open = Double.valueOf(removeQuote(data[2]));
				double high = Double.valueOf(removeQuote(data[3]));
				double low = Double.valueOf(removeQuote(data[4]));
				int volumn = Integer.valueOf(removeQuote(data[5]));
				double change = Double.valueOf(removeQuote(data[6]));
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
	
	
	private String removeQuote(String s){
		return s.replaceAll("\"","");
	}
}
