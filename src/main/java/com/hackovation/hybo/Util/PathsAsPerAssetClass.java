package com.hackovation.hybo.Util;

import java.util.HashMap;
import java.util.Map;

import com.hackovation.hybo.enums.ETFEnum;

public class PathsAsPerAssetClass {

	public static Map<ETFEnum, String> getETFPaths(){
		Map<ETFEnum, String> map = new HashMap<>();
		map.put(ETFEnum.A,"D:\\Wkspace\\Hacovation\\CRSP US Total Market.txt");
		map.put(ETFEnum.A,"D:\\Wkspace\\Hacovation\\CRSP US Large Cap Value.txt");
		map.put(ETFEnum.A,"D:\\Wkspace\\Hacovation\\CRSP US MID CAP VALUE.txt");
		map.put(ETFEnum.A,"D:\\Wkspace\\Hacovation\\CRSP US SMALL CAP VALUE.txt");
		return map;
	}
}
