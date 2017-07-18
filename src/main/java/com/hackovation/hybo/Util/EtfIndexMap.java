package com.hackovation.hybo.Util;

import java.util.HashMap;
import java.util.Map;

public class EtfIndexMap {
	public static Map<String,String> getIndexToEtfMapping(){
		Map<String,String> map = new HashMap<>();
		map.put("CRSPTM1", "VTI");
		map.put("CRSPLC1", "VTV");
		map.put("CRSPML1", "VOE");
		map.put("CRSPSC1", "VBR");
		return map;
	}

	public static Map<String,String> getEtfToIndexMapping(){
		Map<String,String> map = new HashMap<>();
		map.put("VTI","CRSPTM1");
		map.put("VTV","CRSPLC1");
		map.put("VOE","CRSPML1");
		map.put("VBR","CRSPSC1");
		return map;
	}
}
