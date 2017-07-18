package com.hackovation.hybo.Util;

import java.util.HashMap;
import java.util.Map;

public class PathsAsPerAssetClass {

	public static Map<String, String> getETFPaths(){
		Map<String, String> map = new HashMap<>();
		map.put("VTI","D:\\Wkspace\\Hacovation\\CRSP US Total Market.txt");
		map.put("VTV","D:\\Wkspace\\Hacovation\\CRSP US Large Cap Value.txt");
		map.put("CRSPML1","D:\\Wkspace\\Hacovation\\CRSP US MID CAP VALUE.txt");
		map.put("CRSPSC1","D:\\Wkspace\\Hacovation\\CRSP US SMALL CAP VALUE.txt");
		return map;
	}
}
