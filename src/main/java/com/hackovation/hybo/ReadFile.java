package com.hackovation.hybo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.algo.series.CalendarDateSeries;
import org.algo.type.CalendarDate;
import org.algo.type.CalendarDateUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hack17.hybo.domain.IndexPrice;
import com.hack17.hybo.repository.PortfolioRepository;


@Transactional
@Service
public class ReadFile {
	
	@Autowired
	PortfolioRepository portfolioRepository;
	
	public CalendarDateSeries<Double> getCalendarDataSeriesFromFile(String path,String symbol){
		CalendarDateSeries<Double> series = new CalendarDateSeries<Double>(CalendarDateUnit.DAY).name(symbol);
		try{
			ClassLoader cl = getClass().getClassLoader();
			File file = new File(cl.getResource(path).getFile());
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = null;
			while((line=br.readLine())!=null){
				String[] data = line.split("###");
				DateFormat dateFormat = DateFormat.getDateInstance();
				Date dateKey = dateFormat.parse(data[0]);
				CalendarDate key = new CalendarDate(dateKey);
				
				NumberFormat numberFormat = NumberFormat.getInstance(java.util.Locale.US);
				Number value = numberFormat.parse(data[4]);
				series.put(key, value.doubleValue());
				
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return series;
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
	public Double getETFPriceForDate(String path,String symbol,Date date){
		Double price = new Double(0);
		try{
			BufferedReader br = new BufferedReader(new FileReader(new File(path)));
			String line = null;
			while((line=br.readLine())!=null){
				String[] data = line.split("###");
				DateFormat dateFormat = DateFormat.getDateInstance();
				Date dateKey = dateFormat.parse(data[0]);
				if(data.equals(dateKey)){
					NumberFormat numberFormat = NumberFormat.getInstance(java.util.Locale.US);
					Number value = numberFormat.parse(data[4]);
					price = value.doubleValue();
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return price;
	}

}
