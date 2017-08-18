package com.hackovation.hybo.Util;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.hackovation.hybo.AllocationType;

public class HyboUtil {
	public static Map<String,String> getIndexToEtfMapping(){
		Map<String,String> map = new HashMap<>();
		map.put("CRSPTM1", "VTI");
		map.put("CRSPLC1", "VTV");
		map.put("CRSPML1", "VOE");
		map.put("CRSPSC1", "VBR");
		map.put("SHV", "SHV");
		map.put("LQD", "LQD");
		return map;
	}

	public static Map<String,String> getEtfToIndexMapping(){
		Map<String,String> map = new HashMap<>();
		map.put("VTI","CRSPTM1");
		map.put("VTV","CRSPLC1");
		map.put("VOE","CRSPML1");
		map.put("VBR","CRSPSC1");
		map.put("SHV","SHV");
		map.put("LQD","LQD");
		return map;
	}
	
	public static Map<String,String> ETFToAssetClassMap(){
		Map<String,String> map = new HashMap<>();
		map.put("VTI","US Total Stock Market");
		map.put("VTV","US Large Cap");
		map.put("VOE","US Mid Cap");
		map.put("VBR","US Small Cap");
		map.put("SHV","Short Term Treasuries");
		map.put("LQD","US Investment Grade Bonds");
		return map;
	}
	public static Map<String,AllocationType> getAllocationTypeMap(){
		Map<String,AllocationType> allocationTypeMap = new HashMap<>();
		allocationTypeMap.put("VTI",AllocationType.EQ);
		allocationTypeMap.put("VTV",AllocationType.EQ);
		allocationTypeMap.put("VOE",AllocationType.EQ);
		allocationTypeMap.put("VBR",AllocationType.EQ);
		allocationTypeMap.put("SHV",AllocationType.BOND);
		allocationTypeMap.put("LQD",AllocationType.BOND);
		return allocationTypeMap;
	}
	public static Calendar trimTime(Calendar cal){
		cal.set(Calendar.HOUR_OF_DAY,0);
		cal.set(Calendar.MINUTE,0);
		cal.set(Calendar.SECOND,0);
		cal.set(Calendar.MILLISECOND,0);
	//	cal.set(Calendar.ZONE_OFFSET,0);
		return cal;
	}
	
	public static boolean getBoolean(String str){
		str = str.toUpperCase();
		if(str.equals("Y")) return true;
		else return false;
	}
}
