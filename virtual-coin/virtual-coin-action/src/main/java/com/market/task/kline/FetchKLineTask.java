package com.market.task.kline;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;
import com.market.action.TradingPlatformAction;
import com.market.kline.IKLineParser;
import com.market.model.KLine;
import com.market.model.MarketKLineInfo;
import com.market.pojo.TradingPlatform;
import com.utils.request.HttpClientRequestHandler;

/**
 * 
* @ClassName: FetchKLineTask
* @Description: 从交易平台获取KLine数据的Task
* @author: leisure
* @date: 2018年6月13日 下午4:16:26
 */
public class FetchKLineTask implements Callable<MarketKLineInfo>{
	//交易对
	private String tradingAsset;
	//交易平台名
	private String platformName;
	//取KLine的周期
	private String period;
	//获取KLine的条数
	private String size;
	private Map<String,String> paramMap;
	//获取KLine时的当前时间 
	private Date  fetchDate;
	private IKLineParser klineParse;
	private MarketKLineInfo mKLineInfo;
	//requestHandler
	private HttpClientRequestHandler requestHandler;
	private TradingPlatform tradingPlatform;
	private TradingPlatformAction tradingPlatformAction;
	private ExecutorService es;
	
	public FetchKLineTask(String tradingAsset,String platformName,String period,Map<String,String> paramMap,HttpClientRequestHandler requestHandler) {
		this.tradingAsset = tradingAsset;
		this.platformName = platformName;
		this.period = period;
		this.paramMap = paramMap;
		this.requestHandler = requestHandler;
		this.tradingPlatformAction = new TradingPlatformAction(platformName);
		this.tradingPlatform = this.tradingPlatformAction.getTp();
		this.es = Executors.newCachedThreadPool();
	}
	
	public FetchKLineTask(HttpClientRequestHandler requestHandler,String tradingAsset,TradingPlatformAction tradingPlatformAction,Map<String,String> paramMap,IKLineParser klineParse) {
		this.tradingAsset = tradingAsset;
		this.paramMap = paramMap;
		this.requestHandler = requestHandler;
		this.klineParse = klineParse;
		this.tradingPlatformAction = tradingPlatformAction;
		this.tradingPlatform = this.tradingPlatformAction.getTp();
		this.platformName = this.tradingPlatform.getName();
		this.es = Executors.newCachedThreadPool();
		
	}
	
	
	
	/*************************************************************************/
	@Override
	public MarketKLineInfo call() throws Exception {
		
		/*CompletableFuture<String> comFuture = CompletableFuture.supplyAsync(new Supplier<String>() {

			@Override
			public String get() {
				String responseString = tradingPlatformAction.fetchKLineInfo(requestHandler,tradingPlatform, paramMap);
				return responseString;
			}
			
		},es);
				
		comFuture.thenAccept(new Consumer<String>() {

			@Override
			public void accept(String t) {
				if(null==t) {
					return;
				}
				mKLineInfo = tradingPlatformAction.parseKLineInfoString(t, klineParse);
				if(null!=mKLineInfo) {
					   System.out.println("tradingPlatform = "+mKLineInfo.getTradingPlatformName()+"---tradingAsset = "+mKLineInfo.getTradingAssetPairName());
					   List<KLine> klineList = mKLineInfo.getKlineList();
					   for(int index=0;index<klineList.size();index++) {
						   KLine kline = klineList.get(index);
						   System.out.println("index = "+index+"---closePrice = "+kline.getClosePrice());
					   }
					}
			}
			
		});	*/
		String responseString = tradingPlatformAction.fetchKLineInfo(requestHandler,tradingPlatform, paramMap);
		mKLineInfo = tradingPlatformAction.parseKLineInfoString(responseString, klineParse);
		return mKLineInfo;
	}
	/****************************************************************************/
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
	public String getPeriod() {
		return period;
	}
	public void setPeriod(String period) {
		this.period = period;
	}
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
	public Date getFetchDate() {
		return fetchDate;
	}
	public void setFetchDate(Date fetchDate) {
		this.fetchDate = fetchDate;
	}
	public IKLineParser getKlineParse() {
		return klineParse;
	}
	public void setKlineParse(IKLineParser klineParse) {
		this.klineParse = klineParse;
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
	public ExecutorService getEs() {
		return es;
	}
	public void setEs(ExecutorService es) {
		this.es = es;
	}

	
	
	
	
	
}
