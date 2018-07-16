package com.market.main;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.market.model.KLine;
import com.utils.date.DateUtils;
import com.utils.json.JacksonUtils;
import com.utils.poi.PoiUtils;
import com.utils.tradingIndicator.BollIndicator;
import com.utils.tradingIndicator.KDJIndicator;
import com.utils.tradingIndicator.MacdIndicator;
import com.utils.tradingIndicator.MovingAverageIndicator;
import com.utils.tradingIndicator.RSIIndicator;

import redis.clients.jedis.Jedis;

public class MacdIndicatorTest {
	public static final String REDIS_HOST = "192.168.5.109";
	public static final int REDIS_PORT = 6379;
	

	public static void main(String[] args) {
		//calculateMACD("D:/stock_kline.xls");
		//calculateMACD2("D:/stock_kline.xls");
		//StrategyCalculate();
		List<KLine> klineList = generateKLineList(1,0);
		//calculateRSI(klineList, 6, 12, 24, 2);
		calculateBOLL(klineList, 20, 2.0, 2);
		//test();
	}
    
	public static void test() {
		int index = 0;
		int count = 10;
		while(index++<count) {
			System.out.println("index = "+index);
		}
		/*for(;index<count;index++) {
			System.out.println("index = "+index);
		}*/
		
	}
	
	public static void calculateMACD(String excelFile) {
		List<Double> closedPriceList = new ArrayList<Double>();
		List<String> mList = PoiUtils.getExcelColumnListValues(excelFile, 0, 4, 1, 2);
		for (String s : mList) {
			closedPriceList.add(Double.parseDouble(s));
		}
		System.out.println("closedPriceList.size = " + closedPriceList.size());
		List<HashMap<String, Double>> macdList = MacdIndicator.calculateMACD(closedPriceList);
		int count = macdList.size();
		for (int index = 0; index < count; index++) {
			HashMap<String, Double> map = macdList.get(index);

			System.out.println("inde = " + index + "---MACD = " + map.get("MACD") + "---DIFF = " + map.get("DIFF")
					+ "---DEA = " + map.get("DEA"));

		}
	}

	public static void calculateMACD2(String excelFile) {

		/*List<String> mList = PoiUtils.getExcelColumnListValues(excelFile, 0, 4, 1, 2);
		List<KLine> klineList = new ArrayList<KLine>();

		for (String s : mList) {
			KLine kline = new KLine();
			kline.setFormatDate(DateUtils.dateFormate(new Date().getTime()));
			kline.setClosePrice(Double.parseDouble(s));
			klineList.add(kline);
		}*/
		//苏宁易购.xls    D:/stock_kline.xls
		//List<Map<String,String>> mList = PoiUtils.getExcelColumnMapValues("D:/stock_kline.xls", 0, 1,0);
		List<Map<String,String>> mList = PoiUtils.getExcelColumnMapValues("D:/苏宁易购.xls", 0, 1,0);
		List<KLine> klineList = new ArrayList<KLine>();
		for(int index=0;index<mList.size();index++) {
			KLine kline = new KLine();
			Map<String,String> map = mList.get(index);
			kline.setFormatDate(map.get("column_0"));
			kline.setOpenPrice(Double.parseDouble(map.get("column_1")));
			kline.setHighPrice(Double.parseDouble(map.get("column_2")));
			kline.setLowPrice(Double.parseDouble(map.get("column_3")));
			kline.setClosePrice(Double.parseDouble(map.get("column_4")));
			klineList.add(kline);
		}
		/*
		 //连接本地的 Redis 服务
        Jedis jedis = new Jedis(REDIS_HOST,REDIS_PORT);
        System.out.println("连接成功");
        //查看服务是否运行
        System.out.println("服务正在运行: "+jedis.ping());
        String jackson_key = "suning_macd";
		 String jacksonContent = JacksonUtils.encode(macdList);
		
		 jedis.set(jackson_key, jacksonContent);
		 jedis.disconnect();
		
		for (int index = 0; index < count-2; index++) {
			Map<String, String> map = macdList.get(index);
			Map<String, String> map1 = macdList.get(index+1);
			Map<String, String> map2 = macdList.get(index+2);
			
			if(Double.valueOf(map.get("MACD"))<0.0 && Double.valueOf(map1.get("MACD"))>=0.0 && Double.valueOf(map2.get("MACD"))>=0.0) {
				System.out.println(map.get("TIME"));
			}
		}*/
	}
	public static void StrategyCalculate() {
		List<KLine> klineList = generateKLineList(1,0);
		//计算均线
		List<Map<String,String>> MA30ListMap = calculateMA(klineList,10,2);
		//判断closePrice在MA30线以下的点;
		List<Map<String,String>> MAStrategyPointList = new ArrayList<Map<String,String>>();
		List<String> MAStrategyTimeList = new ArrayList<String>();
		for(int index=0;index<MA30ListMap.size();index++) {
			Map<String,String> MA30Map = MA30ListMap.get(index);
			Double closePrice = Double.valueOf(MA30Map.get("CLOSEPRICE"));
			Double MA30Price = Double.valueOf(MA30Map.get("MA10"));
			if(closePrice<MA30Price) {
				MAStrategyPointList.add(MA30Map);
				MAStrategyTimeList.add(MA30Map.get("TIME"));
			}
		}
		//计算MACD
		List<Map<String,String>> macdList = calculateMACD(klineList, 12, 26, 9, 3);
		//计算MACD为负的点
		List<Map<String,String>> MACDStrategyPointList = new ArrayList<Map<String,String>>();
		List<String> MACDStrategyTimeList = new ArrayList<String>();
		for(int index=0;index<macdList.size();index++) {
			Map<String,String> MACDMap = macdList.get(index);
			Double MACD = Double.valueOf(MACDMap.get("MACD"));
			if(MACD<0) {
				MACDStrategyPointList.add(MACDMap);
				MACDStrategyTimeList.add(MACDMap.get("TIME"));
			}
		}
		//计算KDJ
	    List<Map<String,String>> kdjList = calculateKDJ(klineList, 9, 3, 3, 2);
	    List<String> KDJStrategyTimeList = new ArrayList<String>();
	    for(int index=1;index<kdjList.size();index++) {
	    	/*Map<String,String> map = kdjList.get(index);
	    	//Double K = Double.valueOf(map.get("K"));
	    	Double D = Double.valueOf(map.get("D"));
	    	Double J = Double.valueOf(map.get("J"));
	    	if(D>J) {
	    		KDJStrategyTimeList.add(map.get("TIME"));
	    	}*/
	    	Map<String, String> map = kdjList.get(index);
			Map<String, String> map1 = kdjList.get(index-1);
			/*Map<String, String> map2 = kdjList.get(index+2);
			Map<String, String> map3 = kdjList.get(index+3);
			Map<String, String> map4 = kdjList.get(index+4);*/
			/*if((Double.valueOf(map.get("D")) > Double.valueOf(map.get("J"))  )&& (
			Double.valueOf(map1.get("D")) < Double.valueOf(map1.get("J")) || 
			 (Double.valueOf(map2.get("D")) == Double.valueOf(map2.get("J")))){
			 || (Double.valueOf(map3.get("D")) == Double.valueOf(map3.get("J"))
			 || (Double.valueOf(map4.get("D")) == Double.valueOf(map4.get("J")))))) {
				KDJStrategyTimeList.add(map1.get("TIME"));
			}*/
			if((Double.valueOf(map.get("D")) > Double.valueOf(map.get("J"))) && (Double.valueOf(map1.get("D")) < Double.valueOf(map1.get("J")))) {
				KDJStrategyTimeList.add(map.get("TIME"));
				
			}
	    }
	    
	    //计算三个策略的交集
	     MAStrategyTimeList.retainAll(MACDStrategyTimeList);
	     
	     //s
	     System.out.println("MA 和MACD 交集后的点");
         for(int index=0;index<MAStrategyTimeList.size();index++) {
	    	 
	    	 System.out.println("inde = " + index +"---time = "+MAStrategyTimeList.get(index));
	     }
         List<String> ret1 = new ArrayList<String>();
         ret1.addAll(MAStrategyTimeList);
         ret1.retainAll(KDJStrategyTimeList);
	
	     System.out.println("------------------------最后的策略点-----------------");
	     for(int index=0;index<ret1.size();index++) {
	    	 
	    	 System.out.println("inde = " + index +"---time = "+ret1.get(index));
	     }
	}
	public static List<KLine> generateKLineList(int startColumn,int lastColumn) {
		//List<Map<String,String>> mList = PoiUtils.getExcelColumnMapValues("D:/stock_kline.xls", 0, 1,0);
		List<Map<String,String>> mList = PoiUtils.getExcelColumnMapValues("D:/SNYG.xls", 0, startColumn,lastColumn);
		List<KLine> klineList = new ArrayList<KLine>();
		for(int index=0;index<mList.size();index++) {
			KLine kline = new KLine();
			Map<String,String> map = mList.get(index);
			kline.setFormatDate(map.get("column_0"));
			kline.setOpenPrice(Double.parseDouble(map.get("column_1")));
			kline.setHighPrice(Double.parseDouble(map.get("column_2")));
			kline.setLowPrice(Double.parseDouble(map.get("column_3")));
			kline.setClosePrice(Double.parseDouble(map.get("column_4")));
			klineList.add(kline);
		}
		return klineList;
	}
	public static List<Map<String,String>> calculateMA(List<KLine> klineList,final int period,final int decimalWidth) {
		List<Map<String,String>> periodMAListMap = MovingAverageIndicator.calculateMAListMap(klineList, period,decimalWidth);
		for (int index = 0; index < periodMAListMap.size(); index++) {
			Map<String, String> map = periodMAListMap.get(index);
			System.out.println("inde = " + index +"---time = "+map.get("TIME")+"----closePrice = "+map.get("CLOSEPRICE")+"----MA"+period+" = "+map.get("MA"+period));
		}
		return periodMAListMap;
	}
	public static List<Map<String, String>> calculateMACD(List<KLine> klineList,final int shortPeriod,
			final int longPeriod, final int mDays,final int decimalWidth) {
		List<Map<String, String>> macdList = MacdIndicator.calculateMACD(klineList, shortPeriod, longPeriod, mDays, decimalWidth);
		int count = macdList.size();
		for (int index = 0; index < count; index++) {
			Map<String, String> map = macdList.get(index);

			System.out.println("inde = " + index +"---time = "+map.get("TIME")+ "---MACD = " + map.get("MACD") + "---DIFF = " + map.get("DIFF")
					+ "---DEA = " + map.get("DEA"));
		}
		return macdList;
	}
	public static List<Map<String, String>> calculateKDJ(List<KLine> klineList,final int period,
			final int paramA, final int paramB,final int decimalWidth) {
		List<Map<String, String>> kdjList = KDJIndicator.calculateKDJ(klineList, period, paramA, paramB, decimalWidth);
		int count = kdjList.size();
		for (int index = 0; index < count; index++) {
			Map<String, String> map = kdjList.get(index);
			System.out.println("inde = " + index +"---time = "+map.get("TIME")+ "---RSV = " + map.get("RSV") + "---K = " + map.get("K")
			+ "---D = " + map.get("D")+"---J = " + map.get("J"));

		}
		return kdjList;
	}
	
	public static void calculateRSI(List<KLine> klineList, int shortPeriod, int middlePeriod, int longPeriod, int decimalWidth) {
		List<Map<String, String>> rsiList = RSIIndicator.calculateRSI(klineList, shortPeriod, middlePeriod, longPeriod, decimalWidth);
		int count = rsiList.size();
		for (int index = 0; index < count; index++) {
			Map<String, String> map = rsiList.get(index);
			System.out.println("inde = " + index +"---time = "+map.get("TIME")+ 
					"---RSI"+ shortPeriod+" = " + map.get("RSI"+shortPeriod) +
					"---RSI"+ middlePeriod+" = " + map.get("RSI"+middlePeriod) +
					"---RSI"+ longPeriod+" = " + map.get("RSI"+longPeriod)
			);

		}
	}
	public static void calculateBOLL(List<KLine> klineList, int period, final Double multiple , int decimalWidth) {
		List<Map<String, String>> rsiList = BollIndicator.calculateBoll(klineList, period, multiple, decimalWidth);
		int count = rsiList.size();
		for (int index = 0; index < count; index++) {
			Map<String, String> map = rsiList.get(index);
			System.out.println("inde = " + index +"---time = "+map.get("TIME")+ 
					"---MID = " + map.get("MID") +
					"---UPPER = " + map.get("UPPER") +
					"---LOWER = " + map.get("LOWER") 
			);

		}
	}
}
