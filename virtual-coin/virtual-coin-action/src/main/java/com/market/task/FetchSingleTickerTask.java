package com.market.task;

import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.market.action.TradingPlatformAction;
import com.market.model.MarketTickerInfo;
import com.market.pojo.TradingPlatform;
import com.utils.request.HttpClientRequestHandler;
/**
 * 
* @ClassName: FetchSingleTickerTask
* @Description: 获取单条Ticker信息的周期性任务
* @author: leisure
* @date: 2018年6月5日 上午11:19:39
 */
public class FetchSingleTickerTask extends TimerTask{
	public static final int DEQUE_SIZE = 1000;
	
	private String tradingAsset;
	private String platformName;
	private HttpClientRequestHandler requestHandler;
	private TradingPlatform tradingPlatform;
	private TradingPlatformAction tradingPlatformAction;
	private ConcurrentHashMap<String, LinkedBlockingDeque<MarketTickerInfo>> tickerInfoMap;
	private ExecutorService es;
	
	
	
	
	
	/*******************************************************************************************************/
	public FetchSingleTickerTask() {
		
	}
    public FetchSingleTickerTask(HttpClientRequestHandler requestHandler,String tradingAsset,String platformName) {
    	this.requestHandler = requestHandler;
    	this.tradingAsset = tradingAsset;
		this.platformName = platformName;
		this.tradingPlatformAction = new TradingPlatformAction(platformName);
		this.tradingPlatform = this.tradingPlatformAction.getTp();
		this.tickerInfoMap = new ConcurrentHashMap<String, LinkedBlockingDeque<MarketTickerInfo>>();
		LinkedBlockingDeque<MarketTickerInfo> lbd = new LinkedBlockingDeque<MarketTickerInfo>(DEQUE_SIZE);
		this.tickerInfoMap.put(tradingAsset, lbd);   
		this.es =  Executors.newCachedThreadPool();	
	}
	/******************************************************************************************************/
	@Override
	public void run() {
		CompletableFuture<Map<String,MarketTickerInfo>> comFuture = CompletableFuture.supplyAsync(new Supplier<Map<String,MarketTickerInfo>>(){

			@Override
			public Map<String, MarketTickerInfo> get() {
				Map<String,MarketTickerInfo> mtiMap = tradingPlatformAction.fetchSingleTickerInfoAndParse(requestHandler, tradingAsset, tradingPlatform, true);
				return mtiMap;
			}
			
		},es);
		
		comFuture.thenAccept(new Consumer<Map<String,MarketTickerInfo>>() {
			@Override
			public void accept(Map<String, MarketTickerInfo> t) {
				if(null!=tickerInfoMap && null!=t) {
					String key = tradingAsset;
					MarketTickerInfo mti = t.get(tradingAsset);
					if(null!=key && null!=mti) {
						LinkedBlockingDeque<MarketTickerInfo> mLbq = tickerInfoMap.get(key);
						ReentrantLock lock = new ReentrantLock();
						lock.lock();
						if(null!=mLbq) {
							//从头删除元素直到空间够							
							while(0==mLbq.remainingCapacity()) {
								mLbq.pollFirst();
							}
							try {
								mLbq.putLast(mti);
								//System.out.println("put last node, size = "+mLbq.size());
							} catch (InterruptedException e) {
								
								e.printStackTrace();
							}		
						}else {
							//System.out.println("get mLbq is null");
						}
						lock.unlock();
					}
				}
				
			}
		});
		
		
	}
	/*****************************************************************************************************/
	public String getTradingAsset() {
		return tradingAsset;
	}
	public void setTradingAsset(String tradingAsset) {
		this.tradingAsset = tradingAsset;
	}
	public String getPlatformName() {
		return platformName;
	}
	public void setPlatformName(String platformName) {
		this.platformName = platformName;
	}
	public HttpClientRequestHandler getRequestHandler() {
		return requestHandler;
	}
	public void setRequestHandler(HttpClientRequestHandler requestHandler) {
		this.requestHandler = requestHandler;
	}
	public TradingPlatform getTradingPlatform() {
		return tradingPlatform;
	}
	public void setTradingPlatform(TradingPlatform tradingPlatform) {
		this.tradingPlatform = tradingPlatform;
	}
	public TradingPlatformAction getTradingPlatformAction() {
		return tradingPlatformAction;
	}
	public void setTradingPlatformAction(TradingPlatformAction tradingPlatformAction) {
		this.tradingPlatformAction = tradingPlatformAction;
	}
	public ConcurrentHashMap<String, LinkedBlockingDeque<MarketTickerInfo>> getTickerInfoMap() {
		return tickerInfoMap;
	}
	public void setTickerInfoMap(ConcurrentHashMap<String, LinkedBlockingDeque<MarketTickerInfo>> tickerInfoMap) {
		this.tickerInfoMap = tickerInfoMap;
	}
	public ExecutorService getEs() {
		return es;
	}
	public void setEs(ExecutorService es) {
		this.es = es;
	}

}
