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
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.market.action.TradingPlatformAction;
import com.market.model.MarketTickerInfo;
import com.market.pojo.TradingPlatform;
import com.utils.request.HttpClientRequestHandler;
/**
 * 
* @ClassName: FetchTickerTask
* @Description: 统一的获取Ticker数据的Task  包含了同时获取全部交易对的Ticker和单独获取指定交易对的Ticker
*               由fetchTickerType字段来指定是全部获取还是指定获取
* @author: leisure
* @date: 2018年6月13日 下午4:14:48
 */
public class FetchTickerTask extends TimerTask {
	public static final int DEQUE_SIZE = 1000;

	private String tradingAsset;
	private String platformName;
	private int fetchTickerType;
	private HttpClientRequestHandler requestHandler;
	private TradingPlatform tradingPlatform;
	private TradingPlatformAction tradingPlatformAction;
	private ConcurrentHashMap<String, LinkedBlockingDeque<MarketTickerInfo>> tickerInfoMap;
	private ExecutorService es;

	public FetchTickerTask() {

	}

	public FetchTickerTask(HttpClientRequestHandler requestHandler, String tradingAsset,
			TradingPlatform tradingPlatform) {
		this.requestHandler = requestHandler;
		this.tradingAsset = tradingAsset;
		this.tradingPlatform = tradingPlatform;
		this.platformName = tradingPlatform.getName();
		this.fetchTickerType = tradingPlatform.getFetchTickerType();
		this.tradingPlatformAction = new TradingPlatformAction(tradingPlatform);
		this.tickerInfoMap = new ConcurrentHashMap<String, LinkedBlockingDeque<MarketTickerInfo>>();
	
		if(0==tradingPlatform.getFetchTickerType()) {
			List<String> symbolList = this.tradingPlatformAction.getTradingAssetPairListFromDB(this.tradingPlatformAction.getTp());
			if(null!=symbolList && 0<symbolList.size()) {
				for(String s:symbolList) {
					
					LinkedBlockingDeque<MarketTickerInfo> lbd = new LinkedBlockingDeque<MarketTickerInfo>(DEQUE_SIZE);
					this.tickerInfoMap.put(s, lbd);
				}
				System.out.println("tickerInfoMap.size = "+tickerInfoMap.size());
			}
		}else {
			LinkedBlockingDeque<MarketTickerInfo> lbd = new LinkedBlockingDeque<MarketTickerInfo>(DEQUE_SIZE);
			this.tickerInfoMap.put(tradingAsset, lbd);
		}
		this.es = Executors.newCachedThreadPool();
	}

	public FetchTickerTask(HttpClientRequestHandler requestHandler, String tradingAsset, String platformName) {
		this.requestHandler = requestHandler;
		this.tradingAsset = tradingAsset;
		this.platformName = platformName;
		this.tradingPlatformAction = new TradingPlatformAction(platformName);
		this.tradingPlatform = this.tradingPlatformAction.getTp();
		this.tickerInfoMap = new ConcurrentHashMap<String, LinkedBlockingDeque<MarketTickerInfo>>();
		LinkedBlockingDeque<MarketTickerInfo> lbd = new LinkedBlockingDeque<MarketTickerInfo>(DEQUE_SIZE);
		this.tickerInfoMap.put(tradingAsset, lbd);
		this.es = Executors.newCachedThreadPool();
	}
	
	

	/******************************************************************************************************/
	@Override
	public void run() {
		CompletableFuture<Map<String, MarketTickerInfo>> comFuture = CompletableFuture
				.supplyAsync(new Supplier<Map<String, MarketTickerInfo>>() {

					@Override
					public Map<String, MarketTickerInfo> get() {
						boolean enforcingSingle = (0==fetchTickerType?false:true);
						Map<String, MarketTickerInfo> mtiMap = tradingPlatformAction.fetchTickerInfoAndParse(requestHandler, tradingAsset, tradingPlatform, enforcingSingle);
						return mtiMap;
					}

				}, es);

		comFuture.thenAccept(new Consumer<Map<String, MarketTickerInfo>>() {
			@Override
			public void accept(Map<String, MarketTickerInfo> t) {
				if (null != t && null != t) {
					Iterator<Map.Entry<String,MarketTickerInfo>> iterator = t.entrySet().iterator();
					while(iterator.hasNext()) {
						Map.Entry<String,MarketTickerInfo> entry = iterator.next();
						String key = entry.getKey();
						MarketTickerInfo mti = entry.getValue();
					    //System.out.println("--------------------------------fetch ticker task---------------------");
					    //System.out.println("key = "+key);
					    //System.out.println("key = "+key+"---mti.bidPrice = "+mti.getBidPrice()+"---mti.askPrice = "+mti.getAskPrice());
					if (null != key && null != mti) {
						LinkedBlockingDeque<MarketTickerInfo> mLbq = tickerInfoMap.get(key);
						
						ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
						lock.writeLock().lock();
						try {
						if (null != mLbq) {
							// 从头删除元素直到空间够
							while (0 == mLbq.remainingCapacity()) {
								mLbq.pollFirst();
							}
							try {
								mLbq.putLast(mti);
								//System.out.println("put last node, size = "+mLbq.size());
							} catch (InterruptedException e) {

								e.printStackTrace();
							}
						} else {
							// System.out.println("get mLbq is null");
						}
						}catch(Exception e) {
							e.printStackTrace();
						}finally {
						   lock.writeLock().unlock();
						}
					}
					}
				}else {
					//System.out.println("fetch ticker task get map null");
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

	public int getFetchTickerType() {
		return fetchTickerType;
	}

	public void setFetchTickerType(int fetchTickerType) {
		this.fetchTickerType = fetchTickerType;
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
