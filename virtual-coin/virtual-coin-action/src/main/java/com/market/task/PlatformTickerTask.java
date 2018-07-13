package com.market.task;

import java.util.Iterator;
import java.util.List;
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

//public class PlatformTickerTask implements Runnable{
/**
 * 
* @ClassName: PlatformTickerTask
* @Description: 交易平台获取Ticker的周期性任务
* @author: leisure
* @date: 2018年6月5日 上午11:19:02
 */
public class PlatformTickerTask extends TimerTask{
	public static final int DEQUE_SIZE = 1000;
	private String platformName;
	private HttpClientRequestHandler requestHandler;
	private TradingPlatform tradingPlatform;
	private TradingPlatformAction tradingPlatformAction;
	private ConcurrentHashMap<String, LinkedBlockingDeque<MarketTickerInfo>> tickerInfoMap;
	private ExecutorService es;
	
	//private ConcurrentHashMap<String,ConcurrentHashMap<String,MarketTickerInfo>> globalTickerInfoMap;
    /****************************************************************************************************/
	public PlatformTickerTask() {
		
	}
	public PlatformTickerTask(HttpClientRequestHandler requestHandler,String platformName) {
		this.requestHandler = requestHandler;
		this.platformName = platformName;
		this.tradingPlatformAction = new TradingPlatformAction(platformName);
		this.tradingPlatform = this.tradingPlatformAction.getTp();
		
		List<String> symbolList = this.tradingPlatformAction.getTradingAssetPairListFromDB(this.tradingPlatformAction.getTp());
		System.out.println("symbolList.size = "+symbolList.size());
		this.tickerInfoMap = new ConcurrentHashMap<String, LinkedBlockingDeque<MarketTickerInfo>>();
		if(null!=symbolList && 0<symbolList.size()) {
			for(String s:symbolList) {
				LinkedBlockingDeque<MarketTickerInfo> lbd = new LinkedBlockingDeque<MarketTickerInfo>(DEQUE_SIZE);
				this.tickerInfoMap.put(s, lbd);
			}
			System.out.println("tickerInfoMap.size = "+tickerInfoMap.size());
		}
		this.es =  Executors.newCachedThreadPool();				
	}
	
	/****************************************************************************************************/
	public void init() {
		
	}
	@Override
	public void run() {
		CompletableFuture<Map<String,MarketTickerInfo>> comFuture = CompletableFuture.supplyAsync(new Supplier<Map<String,MarketTickerInfo>>(){

			@Override
			public Map<String, MarketTickerInfo> get() {
				Map<String,MarketTickerInfo> mtiMap = tradingPlatformAction.fetchTickersInfoAndParse(requestHandler, tradingPlatform);
				System.out.println("task get mtiMap.size = "+mtiMap.size());
				return mtiMap;
			}
			
		},es);
		
		comFuture.thenAccept(new Consumer<Map<String,MarketTickerInfo>>() {

			@Override
			public void accept(Map<String, MarketTickerInfo> t) {
				if(null!=tickerInfoMap && null!=t) {
					Iterator<Map.Entry<String,MarketTickerInfo>> iterator = t.entrySet().iterator();
					while(iterator.hasNext()) {
						Map.Entry<String,MarketTickerInfo> entry = iterator.next();
						String key = entry.getKey();
						MarketTickerInfo mti = entry.getValue();
						//System.out.println("key = "+key+"---mti.askPrice = "+mti.getAskPrice()+"---mti.bidPrice = "+mti.getBidPrice());
						if(null!=key && null!=mti) {
							LinkedBlockingDeque<MarketTickerInfo> mLbq = tickerInfoMap.get(key);
							ReentrantLock lock = new ReentrantLock();
							lock.lock();
							if(null!=mLbq) {
								//从头删除元素直到空间够
								/*do {
									mLbq.pollFirst();
									//System.out.println("poll first node, size = "+mLbq.size());
								}while(mLbq.remainingCapacity()==0);*/
								while(mLbq.remainingCapacity()==0) {
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
				
			}
		});
		
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
	
	
	
	

}
