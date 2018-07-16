package com.okex.future;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;

public class FutureExecute {
	
	public static void main(String[] args) {
		//testMonitor();
		futureMonitor();	
	}
	
	public static void appendWriteToFile() {
		
	}
	
	public static void testMonitor() {
		Timer mTimer = new Timer();
		OrderRemind orderRemind = new OrderRemind("etc_usd","1min","this_week");
		orderRemind.test();
		//mTimer.scheduleAtFixedRate(orderRemind, 1000, 3000);
	}
	
	public static void futureMonitor() {
		//List<Map<String,String>> symbolList = new ArrayList<Map<String,String>>();
		List<String> symbolList = new ArrayList<String>();
		symbolList.add("etc_usd");
		symbolList.add("btc_usd");
		symbolList.add("ltc_usd");
		symbolList.add("eth_usd");
		symbolList.add("bch_usd");
		symbolList.add("xrp_usd");
		symbolList.add("eos_usd");
		symbolList.add("btg_usd");
		
		for(int index=0;index<symbolList.size();index++) {
			Timer mTimer = new Timer();
			OrderRemind orderRemind = new OrderRemind(symbolList.get(index),"15min","this_week");
			mTimer.scheduleAtFixedRate(orderRemind, 1000, 5000);
		}
	}

}
