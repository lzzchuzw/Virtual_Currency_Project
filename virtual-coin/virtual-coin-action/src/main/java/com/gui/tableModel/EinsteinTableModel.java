package com.gui.tableModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.swing.table.AbstractTableModel;

import com.market.model.MarketTickerInfo;
import com.market.task.PlatformTickerTask;

public class EinsteinTableModel extends AbstractTableModel{
	private Vector<String> columnNames;
	private Vector<Vector<Object>> data;
	private PlatformTickerTask platformTickerTask;
	
	/*public EinsteinTableModel() {
		
	}*/
	
	public EinsteinTableModel(PlatformTickerTask platformTickerTask) {
		this.columnNames = new Vector<String>();
		
		this.columnNames.add("交易对");		
		this.columnNames.add("交易平台");
		
		//this.columnNames.add("卖盘量");
		this.columnNames.add("买1价");
		this.columnNames.add("卖1价");
		//this.columnNames.add("买盘量");
		this.columnNames.add("顺逆");
		this.columnNames.add("价格差");
		this.columnNames.add("白分比");

		this.platformTickerTask = platformTickerTask;
		data = new Vector<Vector<Object>>();
	}
    
    /*public EinsteinTableModel(List<FetchMarketTickerDataTask> taskList) {
		this.columnNames.add("交易对");		
		this.columnNames.add("卖盘低平台");
		this.columnNames.add("卖1低价");
		//this.columnNames.add("卖盘量");
		this.columnNames.add("买盘高平台");
		this.columnNames.add("买1高价");
		//this.columnNames.add("买盘量");
		this.columnNames.add("顺逆");
		this.columnNames.add("价格差(白分比)");
		
		this.taskList = taskList;
		this.mtdList = new ArrayList<MarketTickerData>();
		data = fetchEinsteinModelData();	
		//data = fetchEinsteinMultiPlatformModelData();
	}*/
    
    public Vector<Vector<Object>> fetchEinsteinModelData(){
    	List<MarketTickerInfo> mtiList = fetchData(platformTickerTask);
    	data = generateData(mtiList);
		return data;
    }
    
    
    public Vector<Vector<Object>> fetchEinsteinMultiPlatformModelData(){
    	//return generateData(fetchData(taskMap));
    	return null;
    }
    
    
 
    
    public List<MarketTickerInfo> fetchData(PlatformTickerTask platformTickerTask){
    	if(null==platformTickerTask || null==platformTickerTask.getTickerInfoMap() || 0==platformTickerTask.getTickerInfoMap().size()) {
    		System.out.println("fetchData param error");
    		return null;
    	}
    	List<MarketTickerInfo> mtiList = new ArrayList<MarketTickerInfo>();
    	/*ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    	lock.readLock().lock();*/
    	ConcurrentHashMap<String, LinkedBlockingDeque<MarketTickerInfo>> tickerInfoMap = platformTickerTask.getTickerInfoMap();
    	Iterator<Map.Entry<String, LinkedBlockingDeque<MarketTickerInfo>>> iterator = tickerInfoMap.entrySet().iterator();
    	while(iterator.hasNext()) {
    		Map.Entry<String, LinkedBlockingDeque<MarketTickerInfo>> entry = iterator.next();
    		LinkedBlockingDeque<MarketTickerInfo> lbd = entry.getValue();
    		if(0==lbd.size()) {
    			System.out.println("fetchData get lbd.size is 0");
    			//return null;
    		}else {
    			System.out.println("fetchData get lbd.size = "+lbd.size());
    		}
    		mtiList.add(lbd.getLast());
    	}
    	//lock.readLock().unlock();
    	System.out.println("mtiList.size = "+mtiList.size());
		return mtiList;
	}
    
    public List<MarketTickerInfo> sortPriceAndAnalyze(List<MarketTickerInfo> mtiList){
    	if(null==mtiList || 0==mtiList.size()) {
    		System.out.println("sortPriceAndAnalyze param error");
    		return null;
    	}
    	for(int index=0;index<mtiList.size();index++) {
    		MarketTickerInfo mti = mtiList.get(index);
    		int state = 0;
    		/*double priceSpread = mti.getBidPrice()-mti.getAskPrice();   		
    		double margin = priceSpread / mti.getAskPrice()*100;//盈利百分比*/
    		
    		double priceSpread = mti.getAskPrice()-mti.getBidPrice();   		
    		double margin = priceSpread / mti.getBidPrice()*100;//盈利百分比
    		
    		if(priceSpread > 0) {
    			state = 1;
    		}else if(priceSpread < 0) {
    			state = -1;
    		}
    		mti.setPriceSpread(priceSpread);
    		mti.setState(state);
    		mti.setMargin(margin);
    	}
    	
    	
    	
    	//按照差价从大到小排列
    	Collections.sort(mtiList, new Comparator<MarketTickerInfo>() {

			@Override
			public int compare(MarketTickerInfo o1, MarketTickerInfo o2) {
				if(o1.getMargin()>o2.getMargin()) {
					return -1;
				}else if(o1.getMargin()<o2.getMargin()) {
					return 1;
				}
				return 0;
			}
		});
    	
    	int index = 0;
    	for(int i=0;i<mtiList.size();i++) {
    		System.out.println("mtiList["+i+"].radingAssetPairName = "+mtiList.get(i).getTradingAssetPairName());
    		if("etc_usdt".equals(mtiList.get(i).getTradingAssetPairName())) {
    			index = i;
    		}
    	}
    	MarketTickerInfo mti = mtiList.get(index);
    	for(int j=index;j>0;j--) {
    		MarketTickerInfo tmti = mtiList.get(j-1);
    		mtiList.set(j, tmti);
    	}
    	mtiList.set(0, mti);
    	return mtiList;
    }
	
	public Vector<Vector<Object>> generateData(List<MarketTickerInfo> mtiList){
		if(null==mtiList || 0==mtiList.size()) {
			System.out.println("generateData, param error");
    		return null;
    	}
		mtiList = sortPriceAndAnalyze(mtiList);
		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		//data.add(0, null);
		//int count = 1;
		  for(MarketTickerInfo mti:mtiList) {			
			Vector<Object> rowData = generateDataRow(mti);
			System.out.println();
			/*if("etc_usdt".equals(String.valueOf(rowData.get(0)))) {
				data.set(0, rowData);
				count++;
				continue;
			}
			data.add(rowData);
			data.add(count, rowData);
			count++;*/
			data.add(rowData);
			
		  }
		/*Collections.sort(data, new Comparator<Vector<Object>>() {

			@Override
			public int compare(Vector<Object> o1, Vector<Object> o2) {
				int index = o1.size()-1;
				
				return -1*Double.compare(Double.parseDouble(String.valueOf(o1.get(index))), Double.parseDouble(String.valueOf(o2.get(index))));
			}
		});*/
		
		System.out.println("------------------------------遍历data-----------------------------");
		//遍历data
		for(int i=0;i<data.size();i++) {
			Vector<Object> row = data.get(i);
			for(int j=0;j<row.size();j++) {
				System.out.println(String.valueOf(row.get(j)));
			}
		}
		return data;
	}
	
	
	
	public Vector<Object> generateDataRow(MarketTickerInfo mti){
		if(null==mti ) {
			return null;
		}	
		Vector<Object> rowData = new Vector<Object>();
    	//交易对
    	rowData.add(mti.getTradingAssetPairName());
    	//交易平台
    	rowData.add(mti.getTradingPlatformName());
    	
        //买一
    	rowData.add(String.valueOf(mti.getBidPrice()));
    	//卖一
    	rowData.add(String.valueOf(mti.getAskPrice()));
    	//rowData.add(mti.getMaxBidCount());
    	//盈亏状态
    	switch(mti.getState()) {
    	   case -1:
    		  rowData.add("亏");
    		  break;
    	   case 0:
    		  rowData.add("持平");
    		  break;
    	   case 1:
    		  rowData.add("盈");
    		  break;
    	   default:
    		  rowData.add("未知");
    		  break;
    	}
        //价格差
    	rowData.add(String.valueOf(mti.getPriceSpread()));
    	//价格差百分比
    	rowData.add(String.valueOf(mti.getMargin()));
		return rowData;
	}
    
    
	@Override
	public int getRowCount() {
		
		return data.size();
	}

	@Override
	public int getColumnCount() {
		
		return columnNames.size();
	}
	@Override
	public String getColumnName(int col) {
	     return columnNames.get(col);
	}
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		
		return data.get(rowIndex).get(columnIndex);
	}
	@Override
	public void setValueAt(Object value, int row, int col) {
	     data.get(row).set(col,data);
	     fireTableCellUpdated(row, col);
	}
	/*
	* JTable uses this method to determine the default renderer/
	* editor for each cell.  If we didn't implement this method,
	* then the last column would contain text ("true"/"false"),
	* rather than a check box.
	*/
	public Class getColumnClass(int c) {
		 Object t = getValueAt(0, c);
		 //System.out.println("getColumnClass t = "+t.toString()+"---instance of t = "+t.getClass());
	     return getValueAt(0, c).getClass();
	}
	/*
	* Don't need to implement this method unless your table's
	* editable.
	*/
	public boolean isCellEditable(int row, int col) {
		//Note that the data/cell address is constant,
		//no matter where the cell appears onscreen.
		/*if (col < 2) {
		return false;
		} else {
		return true;
		}*/
		return false;
	}
	@Override
	public String toString() {
		return "EinsteinTableModel";
	}
	/******************************************************************/
	public Vector<String> getColumnNames() {
		return columnNames;
	}
	public void setColumnNames(Vector<String> columnNames) {
		this.columnNames = columnNames;
	}
	public Vector<Vector<Object>> getData() {
		return data;
	}
	public void setData(Vector<Vector<Object>> data) {
		this.data = data;
	}
	
	
}
