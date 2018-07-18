package com.market.main;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.SwingUtilities;

import com.gui.frame.MarketTickerInfoDisplay;
import com.market.action.PlatformBaseDataCollectAction;
import com.market.action.TradingPlatformAction;
import com.market.kline.IKLineParser;
import com.market.kline.imp.OkexKLineParser;
import com.market.model.KLine;
import com.market.model.MarketKLineInfo;
import com.market.pojo.TradingPlatform;
import com.market.task.FetchTickerTask;
import com.market.task.GatherTickerInfoTask;
import com.market.task.PlatformTickerTask;
import com.market.task.kline.FetchKLineTask;
import com.utils.json.JacksonUtils;
import com.utils.proxy.ProxyUtils;
import com.utils.request.HttpClientRequestHandler;
import com.utils.tradingIndicator.MacdIndicator;

import redis.clients.jedis.Jedis;

public class PlatformActionExecute {
	//public static String platformName = "Okex";
	//public static String platformName = "Gateio";
	public static String platformName = "Binance";
	public static final long PLATFORM_TIMERTASK_DELAY = 1000;
	public static final long PLATFORM_TIMERTASK_PEROID = 3000;
	public static final long SINGEL_TIMERTASK_DELAY = 1000;
	public static final long SINGEL_TIMERTASK_PEROID = 3000;
	
	public static final long TASK_DELAY = 3000;
	public static final long TASK_PEROID = 5000;
	
	public static final String REDIS_HOST = "192.168.5.109";
	public static final int REDIS_PORT = 6379;
	
	public static void main(String[] args) {
		
		//fetchAndSavePlatformInfo(platformName);
		//displayAllTickers(platformName);
		//fetchTickersIndependent();
		//testSerializableTest();
		//fetchKLineInfo();
		fecthKlinAndCalculateMACD2Redis();
	}
	
	 public static void testSerializableTest() {
    	PlatformBaseDataCollectAction  dataCollectionAction = new PlatformBaseDataCollectAction();
    	String s = dataCollectionAction.serializeblePlatformDBData();
    	dataCollectionAction.readListObjectFromSerializableTxt(s);
	 }
	
	/**
	 * 
	* @Title: fetchTickersIndependent
	* @Description: TODO
	* @return List<TradingPlatform>
	* @author leisure
	* @date 2018年6月5日下午3:01:36
	 */
	public static List<FetchTickerTask> fetchTickersIndependent() {
		List<String> platformNameList = new ArrayList<String>();
		platformNameList.add("Okex");
		platformNameList.add("Binance");
		//platformNameList.add("Huobi");
		//platformNameList.add("Gateio");
		List<String> tradingAssetList = new ArrayList<String>();
		tradingAssetList.add("eth_btc");
		
		//task list
		List<FetchTickerTask> fttList = new ArrayList<FetchTickerTask>();
		TradingPlatformAction tpa = new TradingPlatformAction();
		List<TradingPlatform> tpList = tpa.getPlatformListFromDB(platformNameList);
		ProxyUtils proxy = new ProxyUtils("127.0.0.1",1080,null,null);
		for(int index=0;index<tpList.size();index++) {
			TradingPlatform tradingPlatform = tpList.get(index);
			HttpClientRequestHandler requestHandler = new HttpClientRequestHandler(proxy.generateConnectionManager(),proxy.getSocketProxy());
			
			
			if(0==tradingPlatform.getFetchTickerType()) {
				     Timer timer = new Timer();
				     FetchTickerTask ftt = new FetchTickerTask(requestHandler, null, tradingPlatform);
				     fttList.add(ftt);
				     timer.scheduleAtFixedRate(ftt, PLATFORM_TIMERTASK_DELAY, PLATFORM_TIMERTASK_PEROID);
				     
			}else {
				 for(String tradingAsset:tradingAssetList) {
					 FetchTickerTask ftt = new FetchTickerTask(requestHandler,tradingAsset,tradingPlatform);
					 fttList.add(ftt);
					 Timer timer = new Timer();
					 timer.scheduleAtFixedRate(ftt, SINGEL_TIMERTASK_DELAY, SINGEL_TIMERTASK_PEROID);
				 }
			}
			
		}
		gatherTickerInfo(fttList,tradingAssetList,tpa);
		return fttList;
	}
	/**
	 * 
	* @Title: fecthKlinAndCalculateMACD2Redis
	* @Description: TODO 获取KLine,计算MACD指标 并存储到Redis中
	* @author leisure
	* @date 2018年6月15日上午11:30:40
	 */
	public static void fecthKlinAndCalculateMACD2Redis() {
		/*MarketKLineInfo mKLineInfo = fetchKLineInfo();
		if(null!=mKLineInfo) {
			List<KLine> klineList = mKLineInfo.getKlineList();
			if(null!=klineList && 0!=klineList.size()) {
				List<Map<String, String>> macdList = MacdIndicator.calculateMACD(klineList, 12, 26, 9, 8);
				 //连接本地的 Redis 服务
		         Jedis jedis = new Jedis(REDIS_HOST,REDIS_PORT);
		         System.out.println("连接成功");
		         //查看服务是否运行
		         System.out.println("服务正在运行: "+jedis.ping());
		         String jackson_key = "macd_key";
				 String jacksonContent = JacksonUtils.encode(macdList);
				
				 jedis.set(jackson_key, jacksonContent);
				 jedis.disconnect();
			}
		}*/
		 //连接本地的 Redis 服务
        Jedis jedis = new Jedis(REDIS_HOST,REDIS_PORT);
        System.out.println("连接成功");
        //查看服务是否运行
        System.out.println("服务正在运行: "+jedis.ping());
	}
	/**
	 * 
	* @Title: fetchKLineInfo
	* @Description: 获取KLine数据
	* @author leisure
	* @return MarketKLineInfo
	* @date 2018年6月14日上午10:24:00
	 */
	public static MarketKLineInfo fetchKLineInfo() {
		
		String tradingPlatformName = "okex";
		String tradingAssetPairName = "etc_btc";
		String period = "1day";
		TradingPlatformAction tpa = new TradingPlatformAction(tradingPlatformName);
		TradingPlatform tp = tpa.getTp();
		MarketKLineInfo mKLineInfo = null;
		if(null!=tp) {
			IKLineParser oKLineParser = new OkexKLineParser(tradingPlatformName, tradingAssetPairName);
			ProxyUtils proxy = new ProxyUtils("127.0.0.1",1080,null,null);
			HttpClientRequestHandler requestHandler = new HttpClientRequestHandler(proxy.generateConnectionManager(),proxy.getSocketProxy());
			Map<String,String> paramMap = new HashMap<String,String>();
			paramMap.put("symbolName", "symbol");
			paramMap.put("symbolValue", tradingAssetPairName);
			paramMap.put("typeName", "type");
			paramMap.put("typeValue", period);
			FetchKLineTask fKLineTask = new FetchKLineTask(requestHandler,tradingAssetPairName,tpa,paramMap,oKLineParser);
			ExecutorService es = Executors.newCachedThreadPool();
			Future<MarketKLineInfo> mFuture = es.submit(fKLineTask);
			try {
				mKLineInfo = mFuture.get();
				if(null!=mKLineInfo) {
				   System.out.println("tradingPlatform = "+mKLineInfo.getTradingPlatformName()+"---tradingAsset = "+mKLineInfo.getTradingAssetPairName());
					List<KLine> klineList = mKLineInfo.getKlineList();
				   for(int index=0;index<klineList.size();index++) {
					   KLine kline = klineList.get(index);
					   System.out.println("index = "+index+"---date:"+kline.getFormatDate()+"---closePrice = "+kline.getClosePrice());
				   }
				}else {
					System.out.println("get mKLineInfo is null");
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally {
				if(es.isTerminated()) {
					es.shutdown();
				}
			}
			
		}else {
			System.out.println("tp is null");
		}
		return mKLineInfo;
	}
	
	/**
	 * 
	* @Title: gatherTickerInfo
	* @Description: 周期性采集各个平台获取的TickerInfo数据
	* @param fttList
	* @param tradingAssetList void
	* @author leisure
	* @date 2018年6月5日下午4:30:50
	 */
	public static void gatherTickerInfo(List<FetchTickerTask> fttList,List<String> tradingAssetList,TradingPlatformAction tradingPlatformAction) {
		if(null==fttList || 0==fttList.size()) {
			return ;
		}
		GatherTickerInfoTask gtt = new GatherTickerInfoTask(fttList, tradingAssetList);
		gtt.setTradingPlatformAction(tradingPlatformAction);
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(gtt, GatherTickerInfoTask.DELAY,GatherTickerInfoTask.PEROID);	
	}
	
	public static void startFetchTickerTask(List<FetchTickerTask> fttList) {
		
	}
	
	public List<Object> initPlatform(){
		String hql = "";
		//I
		return null;
	}
	/**
	 * 
	* @Title: displayAllTickers
	* @Description: 展示给定平台的全部Tickers
	* @param platformName void
	* @author leisure
	* @date 2018年6月5日下午12:38:34
	 */
	public static void displayAllTickers(String platformName) {
		ProxyUtils proxy = new ProxyUtils("127.0.0.1",1080,null,null);
		HttpClientRequestHandler requestHandler = new HttpClientRequestHandler(proxy.generateConnectionManager(),proxy.getSocketProxy());
		PlatformTickerTask ptt = runPlatformTickerTask(requestHandler,platformName);
		
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				MarketTickerInfoDisplay  display = new MarketTickerInfoDisplay(ptt);
				display.createAndShow();
				
			}
		});
	}
	/**
	 * 
	* @Title: runPlatformTickerTask
	* @Description: 启动platformName交易平台的PlatformTask
	* @param requestHandler
	* @param platformName void
	* @author leisure
	* @date 2018年5月29日上午11:38:41
	 */
	public static PlatformTickerTask runPlatformTickerTask(HttpClientRequestHandler requestHandler,String platformName) {
		if(null==requestHandler || null==platformName) {
			return null;
		}
		PlatformTickerTask ptt = new PlatformTickerTask(requestHandler,platformName);
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(ptt, 1000, 10000);
		return ptt;
	}
	/**
	 * 
	* @Title: fetchAndSavePlatformInfo
	* @Description: 从platformName交易平台获取所有的Ticker信息,并保存到本地数据库
	* @param platformName void
	* @author leisure
	* @date 2018年5月29日上午11:38:08
	 */
	public static void fetchAndSavePlatformInfo(String platformName) {
		TradingPlatformAction tpa = new TradingPlatformAction();
		TradingPlatform tp = tpa.getTradingPlatformService().getObject("where o.name = '"+platformName+"'");
		if(null!=tp) {
			System.out.println("tp.id = "+tp.getId());
			System.out.println("tp.name = "+tp.getName());
		}else {
			System.out.println("get tp is null");
			return ;
		}
		ProxyUtils proxy = new ProxyUtils("127.0.0.1",1080,null,null);
		HttpClientRequestHandler requestHandler = new HttpClientRequestHandler(proxy.generateConnectionManager(),proxy.getSocketProxy());
		tpa.fetchAndSaveTradingAssetPair(requestHandler, tp);
	}

}
