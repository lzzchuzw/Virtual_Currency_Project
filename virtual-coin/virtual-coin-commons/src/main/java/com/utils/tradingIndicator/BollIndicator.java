package com.utils.tradingIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.market.model.KLine;

public class BollIndicator {
	
	
	public static List<Map<String, String>> calculateBoll(List<KLine> klineList,final int shortPeriod,
			final int middlePeriod,final int longPeriod,final int decimalWidth ){
		if(null==klineList || 0==klineList.size()) {
			System.out.println("klineList is null or size is 0");
			return null;
		}
		List<Map<String, String>> bollList = new ArrayList<Map<String, String>>();
		return bollList;
	}

}
