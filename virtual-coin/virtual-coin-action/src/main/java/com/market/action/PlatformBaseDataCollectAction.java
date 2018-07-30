package com.market.action;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.core.sym.Name;
import com.market.handler.GeneralHttpClientRequestManager;
import com.market.pojo.PlatformSymbolRelevance;
import com.market.pojo.TradingAssetPair;
import com.market.pojo.TradingPlatform;
import com.market.pojo.VirtualCoin;
import com.market.service.IPlatformSymbolRelevanceService;
import com.market.service.ITradingAssetPairService;
import com.market.service.ITradingPlatformService;
import com.market.service.IVirtualCoinService;
import com.market.service.imp.PlatformSymbolRelevanceService;
import com.market.service.imp.TradingAssetPairService;
import com.market.service.imp.TradingPlatformService;
import com.market.service.imp.VirtualCoinService;
import com.market.task.dataCollect.GainCommonCoinTask;
import com.market.task.dataCollect.GainPlatformSymbolRelevanceTask;
import com.market.task.dataCollect.GainPlatformTask;
import com.utils.jedis.SerializableUtils;
import com.utils.proxy.ProxyUtils;
import com.utils.regex.RegexUtils;
import com.utils.request.HttpClientRequestHandler;
import com.utils.request.HttpRequestHeaderGenerator;
import com.utils.request.ResponseRet;

/**
 * 
 * @ClassName: PlatformBaseDataCollectAction
 * @Description: 从交易平台采集基本数据信息 经解析后保存到mysql数据库中
 * @author: leisure
 * @date: 2018年6月7日 上午11:24:34
 */
public class PlatformBaseDataCollectAction {
	public static final String BASIC_URL = "https://www.feixiaohao.com";
	public static final String GET_VIRTUAL_COIN_URL_PREFIX = "https://www.feixiaohao.com/list_";
	public static final String GET_CURRENCY_URL_PREFIX = "https://www.feixiaohao.com/currencies/list_";
	public static final String GET_TOKEN_URL_PREFIX = "https://www.feixiaohao.com/assets/list_";
	public static final String GET_PLATFORM_URL_PREFIX = "https://www.feixiaohao.com/exchange/list_";
	public static final String SERIALIZABLE_OLD_TRADING_PLATFORM_INFO_PATH = "D:/testFolder/serializable/otplist.txt";
	public static final String SERIALIZABLE_NEW_TRADING_PLATFORM_INFO_PATH = "D:/testFolder/serializable/ntplist.txt";
	public static final String SERIALIZABLE_PLATFORM_SYMBOL_RELEVANCE_INFO_PATH = "D:/testFolder/serializable/psrlist.txt";//PlatformSymbolRelevance
	public static final String GET_PLATFORM_SYMBOL_RELEVANCE_PREFIX = BASIC_URL+"/exchange/";
	public static final String COMMON_COIN_REGEX = "";
	public static final String EXTRACT_ALL_CURRENCY_REGEX = "\\[(.*?)\\]";
	public static final String PARSE_CURRENCY_REGEX = "\"(.*?)\"";

	private ITradingPlatformService tradingPlatformService;
	private IVirtualCoinService virtualCoinService;
	private IPlatformSymbolRelevanceService psrService;
	private ITradingAssetPairService tradingAssetPairService;
	private TradingPlatform tp;
	public static boolean DEBUG = false;

	public PlatformBaseDataCollectAction() {
		this.tradingPlatformService = new TradingPlatformService();
		this.virtualCoinService = new VirtualCoinService();
		this.psrService = new PlatformSymbolRelevanceService();
		this.tradingAssetPairService = new TradingAssetPairService();
	}

	/********************************************************************************************************/
   
	/**
	 * 有三种方法获取 1,url = "https://www.feixiaohao.com/";非小号 官网 主页 2,url =
	 * "https://www.feixiaohao.com/currencies" 虚拟币 3,url =
	 * "https://www.feixiaohao.com/assets/" 代币
	 * 
	 * @Title: gainCommonCoinAndSaveToDB
	 * @Description: 通过访问 非小号 首页获取主流的Virtual Coin
	 * @param requestHandler
	 *            void
	 * @author leisure
	 * @date 2018年6月7日下午2:09:54
	 */
	public void gainCommonCoinAndSaveToDB() {
		
		// 请求 非小号 网站首页可以得到主流的Virtual Coin
		String url_prefix = "https://www.feixiaohao.com/list_";
		
		List<VirtualCoin> vcList = new ArrayList<VirtualCoin>();
		List<GainCommonCoinTask> gcctList = new ArrayList<GainCommonCoinTask>();
		ExecutorService executorService = Executors.newCachedThreadPool();
		
		for(int i=1;i<=21;i++) {
			//ExecutorService es = Executors.newCachedThreadPool();
			String url = url_prefix+i+".html";
			/*ProxyUtils proxy = new ProxyUtils("127.0.0.1",1080,null,null);
			HttpClientRequestHandler requestHandler = new HttpClientRequestHandler(proxy.generateConnectionManager(),proxy.getSocketProxy());*/
			HttpClientRequestHandler requestHandler = new HttpClientRequestHandler();
			GainCommonCoinTask gcct = new GainCommonCoinTask(new PlatformBaseDataCollectAction(),requestHandler,executorService,url,"GET","common_coin_"+i);
			//gcct.setVcList(vcList);
			//es.submit(gcct);
			
			gcctList.add(gcct);
		
		}
		
		try {
			List<Future<List<VirtualCoin>>>  futureList = executorService.invokeAll(gcctList);
			if(null!=futureList && 0!=futureList.size()) {
				for(Future<List<VirtualCoin>> mfuture:futureList) {
					List<VirtualCoin> mList = mfuture.get();
					vcList.addAll(mList);
				}
				System.out.println("vcList.size = "+vcList.size());
				//排序
				Collections.sort(vcList, new Comparator<VirtualCoin>() {

					@Override
					public int compare(VirtualCoin o1, VirtualCoin o2) {
						if(o1.getRanking()<o2.getRanking()) {
							return -1;
						}else if(o1.getRanking()>o2.getRanking()){
							return 1;
						}
						return 0;
					}
				});
				
				/*if(0!=vcList.size()) {
					for(int index=0;index<vcList.size();index++) {
						VirtualCoin vc = vcList.get(index);
						System.out.println("--------------------index = "+"----------------------");
						System.out.println("vc.id = "+vc.getId()+"----vc.ranking = "+vc.getRanking()+"----vc.name = "+vc.getName()+"---vc.des = "+vc.getDescription());
					   // this.virtualCoinService.saveObject(vc);
					}
				}else {
					System.out.println("vcList size 0");
				}*/
			}
			//this.virtualCoinService.saveListObject(vcList);
		} catch (InterruptedException | ExecutionException e) {
			
			e.printStackTrace();
		}
		if(executorService.isTerminated()) {
			System.out.println("executorService is terminated");
		    executorService.shutdown();
		}
		
		
		
	}

	
    /**
     * 
    * @Title: parseByJsoup
    * @Description: 解析非小号获取的 虚拟币
    * @param responseString
    * @return List<VirtualCoin>
    * @author leisure
    * @date 2018年6月8日下午2:06:24
     */
	public List<VirtualCoin> parseByJsoup(String responseString) {
		if (null == responseString) {
			return null;
		}
		Document doc = Jsoup.parse(responseString);
		if (null == doc) {
			return null;
		}
		List<VirtualCoin> vcList = new ArrayList<VirtualCoin>();
		
		Element coinTable = doc.getElementById("table");
		Element tableBody = coinTable.child(1);
		Elements coinTrs = tableBody.getElementsByTag("tr");
		for (int index = 0; index < coinTrs.size(); index++) {
			System.out.println("-----------index = " + index + "---------------");
			Element coin_tr = coinTrs.get(index);
			if (null != coin_tr) {
				String tr_id = coin_tr.attr("id");

				String rank = coin_tr.child(0).text();

				String des = coin_tr.child(1).text();
				//流通市值
				String marketCapitalization = coin_tr.child(2).text();
				//当前价格
				String price = coin_tr.child(3).text();
				//流通量  流通的数量
				String freeFloat = coin_tr.child(4).text();
				//成交额
				String turnover = coin_tr.child(5).text();
				//涨跌幅
				String priceChangeRatio = coin_tr.child(6).text();
				
				VirtualCoin vc = new VirtualCoin();
				vc.setName(tr_id);
				vc.setRanking(Integer.valueOf(rank));
				vc.setDescription(des);
				Date date = new Date();
				vc.setGmtCreate(date);
				vc.setGmtModified(date);

				System.out.println("tr_id = " + tr_id + "---rank = " + rank + "---des = " + des+"---price = "+price);
				System.out.println("流通市值 = "+marketCapitalization+"---流通量 = "+freeFloat+"---成交额 = "+turnover+"---涨跌幅 = "+priceChangeRatio);
				//this.virtualCoinService.saveObject(vc);
				vcList.add(vc);
			}
		}
		return vcList;
	}
	/**
	 * 
	* @Title: serializeblePlatformDBData
	* @Description: 将数据库中的 "trading_platform"表中的数据序列化并保存到本地硬盘中
	* @return String  保存到本地硬盘中的文件路径
	* @author leisure
	* @date 2018年6月11日下午2:18:55
	 */
	public String serializeblePlatformDBData() {
		List<TradingPlatform> tpList = this.tradingPlatformService.findObjects(" where 1 = 1");
		String filePath = "D:/testFolder/serializable/otplist.txt";
		File file = new File(filePath);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			SerializableUtils<List<TradingPlatform>> sUtils = new SerializableUtils<List<TradingPlatform>>();
			fos.write(sUtils.serializableEncode(tpList));
			fos.flush();
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}finally {
			if(null!=fos) {
				try {
					fos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return filePath;
	}
	/**
	 * 
	* @Title: readListObjectFromSerializableTxt
	* @Description: 将序列化到本地硬盘中的数据反序列化到内存中
	* @param filePath
	* @return List<TradingPlatform>
	* @author leisure
	* @date 2018年6月11日下午2:20:16
	 */
	public List<TradingPlatform> readListObjectFromSerializableTxt(String filePath){
		if(null==filePath) {
			return null;
		}
		SerializableUtils<List<TradingPlatform>> sUtils = new SerializableUtils<List<TradingPlatform>>();
		File file = new File(filePath);
		//FileInputStream fis = null;
		BufferedInputStream bis = null;
		ByteArrayOutputStream baos = null;
		List<TradingPlatform> list = null;
		byte[] buffer = new byte[1024];
		
		int count = 0;
		try {
			bis = new BufferedInputStream(new FileInputStream(file));
			baos = new ByteArrayOutputStream();
			while(-1!=(count = bis.read(buffer,0,buffer.length))){
				baos.write(buffer, 0, count);	
				baos.flush();
			}
			list = sUtils.serializableDecode(baos.toByteArray());
		} catch (FileNotFoundException e1) {
			
			e1.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		//遍历list
		/*for(int index=0;index<list.size();index++) {
			TradingPlatform tp = list.get(index);
			System.out.println("-----------index = "+index+"---------------");
			//SerializableTest.outputObject(tp);
			ReflectUtils.outputObject(tp);
		}*/
		
		return list;
		
	}
	/**
	 * 
	* @Title: gainPlatformsAndSaveToDB
	* @Description: TODO 从非小号 获取交易平台数据
	* @author leisure
	* @date 2018年6月11日上午10:59:47
	 */
	public void gainPlatformsAndSaveToDB() {
		//读取数据库中存有的数据
		//String filePath = serializeblePlatformDBData();
		//String filePath = "D:/testFolder/serializable/otplist.txt";
		List<TradingPlatform> otpList = readListObjectFromSerializableTxt(SERIALIZABLE_OLD_TRADING_PLATFORM_INFO_PATH);
		//从非小号 获取交易平台列表 并按照ranking字段排序
		/*List<TradingPlatform> ntpList = gainTradingPlatformListAndSort();
		
		//从非小号 获取与平台相关的交易对list
		List<PlatformSymbolRelevance> platformSymbolRelevanceList = gainPlatformSymbolRelevance(ntpList);*/
		
		List<TradingPlatform> ntpList = readNTPListFromSerializableTxt(SERIALIZABLE_NEW_TRADING_PLATFORM_INFO_PATH);
		//List<PlatformSymbolRelevance> platformSymbolRelevanceList = readPsrListFromSerializableTxt(SERIALIZABLE_PLATFORM_SYMBOL_RELEVANCE_INFO_PATH);
		
		//将以往数据存入
		for(int i=0;i<ntpList.size();i++) {
			TradingPlatform ntp = ntpList.get(i);
			//this.tradingPlatformService.getObject(" where o.name = "+tp.getName());
			for(int j=0;j<otpList.size();j++) {
				TradingPlatform otp = otpList.get(j);
				if(otp.getName().equalsIgnoreCase(ntp.getName())) {//交易对相同
					ntp.setIsUpperCase(otp.getIsUpperCase());
					ntp.setIsUsd(otp.getIsUsd());
					ntp.setConnectingLineString(otp.getConnectingLineString());
					ntp.setFetchAllCurrencyUrl(otp.getFetchAllCurrencyUrl());
					ntp.setFetchAllSymbolUrl(otp.getFetchAllSymbolUrl());
					ntp.setFetchTickerType(otp.getFetchTickerType());
					ntp.setFetchAllTickerUrl(otp.getFetchAllTickerUrl());
					ntp.setFetchTickerUrlPostfix(otp.getFetchTickerUrlPrefix());
					ntp.setFetchTickerUrlPostfix(otp.getFetchTickerUrlPostfix());
					ntp.setParseAllTickerRegex(otp.getParseAllTickerRegex());
					ntp.setParseSingleTickerRegex(otp.getParseSingleTickerRegex());
					ntp.setParseServerDateRegex(otp.getParseServerDateRegex());
					ntp.setParseSymbolRegex(otp.getParseSymbolRegex());
					ntp.setParseOpenPriceRegex(otp.getParseOpenPriceRegex());
					ntp.setParseHighPriceRegex(otp.getParseHighPriceRegex());
					ntp.setParseLowPriceRegex(otp.getParseLowPriceRegex());
					ntp.setParseLastPriceRegex(otp.getParseLastPriceRegex());
					ntp.setParseTransactionVolumeRegex(otp.getParseTransactionVolumeRegex());
					ntp.setParseBidPriceRegex(otp.getParseBidPriceRegex());
					ntp.setParseBidCountRegex(otp.getParseBidCountRegex());
					ntp.setParseAskPriceRegex(otp.getParseAskPriceRegex());
					ntp.setParseAskCountRegex(otp.getParseAskCountRegex());
				}
				//ReflectUtils.outputObject(otp);
				
			}
			//System.out.println("ntp.id = "+ntp.getId()+"---ntp.ranking = "+ntp.getRanking()+"---ntp.name = "+ntp.getName()+"---alias = "+ntp.getAlias());
		}
		
		tradingPlatformService.saveListObject(ntpList);	
	}
	
	/**
	 * 
	* @Title: saveNPlatformInfoSerializableFromDB2HD
	* @Description: 将数据库中TradingPlatform序列化存储到本地硬盘中
	* @return String
	* @author leisure
	* @date 2018年6月12日下午2:30:18
	 */
	public String saveNPlatformInfoSerializableFromDB2HD() {
		List<TradingPlatform> ntpList =  tradingPlatformService.findObjects(null);
		SerializableUtils<List<TradingPlatform>> tpsu = new SerializableUtils<List<TradingPlatform>>();
		String filePath = tpsu.serializableObject2HardDisk(ntpList, "D:/testFolder/serializable/ntplist_2.txt");
		return filePath;
	}
	/**
	 * 
	* @Title: savePlatformSymbolRelevance2DB
	* @Description: 将PlatformSymbolRelevance数据存储到数据库中
	* @author leisure
	* @date 2018年6月12日下午2:57:49
	 */
	public void savePlatformSymbolRelevance2DB() {
		SerializableUtils<List<TradingPlatform>> tpsu = new SerializableUtils<List<TradingPlatform>>();
		SerializableUtils<List<PlatformSymbolRelevance>> psrsu = new SerializableUtils<List<PlatformSymbolRelevance>>();
		List<TradingPlatform> ntpList = tpsu.readObjectFromSerializableTxt("D:/testFolder/serializable/ntplist_2.txt");
		
		Map<String,TradingPlatform> ntpmap = new HashMap<String,TradingPlatform>();
		for(TradingPlatform tp:ntpList) {
			ntpmap.put(tp.getName(), tp);
		}
		List<PlatformSymbolRelevance> platformSymbolRelevanceList = psrsu.readObjectFromSerializableTxt(SERIALIZABLE_PLATFORM_SYMBOL_RELEVANCE_INFO_PATH);
		for(PlatformSymbolRelevance psr:platformSymbolRelevanceList) {
			TradingPlatform ntp = ntpmap.get(psr.getRemark());
			psr.setTradingPlatformId(ntp.getId());
			psr.setRemark(null);
		}
		psrService.saveListObject(platformSymbolRelevanceList);
	}
	/**
	 * 
	* @Title: generateTradingAssetPairAndSave2DB
	* @Description: 运用Set的特性从数据库中解析并生成TradingAssetPair并保存到数据库
	* @author leisure
	* @date 2018年6月12日下午3:26:06
	 */
	public void generateTradingAssetPairAndSave2DB() {
		List<PlatformSymbolRelevance> platformSymbolRelevanceList = psrService.findObjects(null);
		//tradingAssetPairService
	    Set<String> symbolSet = new HashSet<String>();
	    List<Map<String,String>> list = new ArrayList<Map<String,String>>();
	    for(PlatformSymbolRelevance psr:platformSymbolRelevanceList) {
	    	
	    	String name = psr.getName();
	    	symbolSet.add(name); 	
	    }
	    Iterator<String> iterator = symbolSet.iterator();
	    int count = 1;
	    while(iterator.hasNext()) {
	    	String name = iterator.next();
	    	System.out.println("count = "+count+"----name = "+name);
	    	Map<String,String> symbolMap = new HashMap<String,String>();
	    	String[] symbolArray = name.split("/");
	    	symbolMap.put("name", name);
	    	symbolMap.put("orderCoin", symbolArray[0]);
	    	symbolMap.put("payCoin", symbolArray[1]);
	    	list.add(symbolMap);
	    	count++;
	    }
	    List<TradingAssetPair> tapList = new ArrayList<TradingAssetPair>();
	    for(int index=0;index<list.size();index++) {
	    	Map<String,String> symbolMap = list.get(index);
	    	/*System.out.println("------------index="+(index+1));
	    	System.out.println("name = "+symbolMap.get("name")+"---orderCoin = "+symbolMap.get("orderCoin")+"---payCoin = "+symbolMap.get("payCoin"));*/
	        TradingAssetPair tap = new TradingAssetPair();
	        tap.setName(symbolMap.get("name"));
	        tap.setOrderCurrencyName(symbolMap.get("orderCoin"));
	        tap.setPaymentCurrencyName(symbolMap.get("payCoin"));
	        tapList.add(tap);
	    }
	    tradingAssetPairService.saveListObject(tapList);
	}
	/**
	 * 
	* @Title: savePlatformInfoSerializable
	* @Description: 将从 非小号 获取的交易平台数据 序列化保存到本地硬盘  filePath
	* @return String
	* @author leisure
	* @date 2018年6月12日上午10:55:56
	 */
	public String savePlatformInfoSerializable() {
		List<TradingPlatform> ntpList = gainTradingPlatformListAndSort();
		SerializableUtils<List<TradingPlatform>> tpsu = new SerializableUtils<List<TradingPlatform>>();
		String filePath = tpsu.serializableObject2HardDisk(ntpList, "D:/testFolder/serializable/ntplist.txt");
		return filePath;
	}
	/**
	 * 
	* @Title: readNTPListFromSerializableTxt
	* @Description: 从本地硬盘序列化文件中反序列化读取List<TradingPlatform>
	* @param filePath
	* @return List<TradingPlatform>
	* @author leisure
	* @date 2018年6月12日上午11:00:52
	 */
	public List<TradingPlatform> readNTPListFromSerializableTxt(final String filePath){
		if(null==filePath) {
			return null;
		}
		SerializableUtils<List<TradingPlatform>> tpsu = new SerializableUtils<List<TradingPlatform>>();
		List<TradingPlatform> ntpList = tpsu.readObjectFromSerializableTxt(filePath);
		return ntpList;
	}
	/**
	 * 
	* @Title: savePlatformSymbolRelevanceInfoSerializable
	* @Description: 从 非小号 平台获取与平台关联的交易对
	* @return String
	* @author leisure
	* @date 2018年6月12日上午11:03:04
	 */
	public String savePlatformSymbolRelevanceInfoSerializable() {
		List<TradingPlatform> ntpList = readNTPListFromSerializableTxt("D:/testFolder/serializable/ntplist.txt");
		List<PlatformSymbolRelevance> platformSymbolRelevanceList = gainPlatformSymbolRelevance(ntpList);
		SerializableUtils<List<PlatformSymbolRelevance>> psrsu = new SerializableUtils<List<PlatformSymbolRelevance>>();
		String filePath = psrsu.serializableObject2HardDisk(platformSymbolRelevanceList, "D:/testFolder/serializable/psrlist.txt");
		return filePath;
	}
	/**
	 * 
	* @Title: readPsrListFromSerializableTxt
	* @Description: 从本地硬盘序列化文件中反序列化读取List<PlatformSymbolRelevance>
	* @param filePath
	* @return List<PlatformSymbolRelevance>
	* @author leisure
	* @date 2018年6月12日上午11:04:44
	 */
	public List<PlatformSymbolRelevance> readPsrListFromSerializableTxt(final String filePath){
		if(null==filePath) {
			return null;
		}
		SerializableUtils<List<PlatformSymbolRelevance>> psrsu = new SerializableUtils<List<PlatformSymbolRelevance>>();
		List<PlatformSymbolRelevance> platformSymbolRelevanceList = psrsu.readObjectFromSerializableTxt(filePath);
		return platformSymbolRelevanceList;
	}
	/**
	 * 
	* @Title: gainTradingPlatformListAndSort
	* @Description: 从 非小号 平台获取TradingPlatform的数据
	* @return List<TradingPlatform>
	* @author leisure
	* @date 2018年6月11日下午4:33:51
	 */
	public List<TradingPlatform> gainTradingPlatformListAndSort(){
		List<TradingPlatform> tpList = new ArrayList<TradingPlatform>();
		List<GainPlatformTask> gptList = new ArrayList<GainPlatformTask>();
		ExecutorService executorService = Executors.newCachedThreadPool();
		PlatformBaseDataCollectAction dataCollectAction = new PlatformBaseDataCollectAction();
		//生成GainPlatformTask list
		for(int i=1;i<=6;i++) {
			String url = GET_PLATFORM_URL_PREFIX+i+".html";
			HttpClientRequestHandler requestHandler = new HttpClientRequestHandler();
			GainPlatformTask gpt = new GainPlatformTask(dataCollectAction , requestHandler, executorService, url, "GET", "common_tradingPlatform"+i);
			gptList.add(gpt);
		}
		//提交所有的任务
		try {
			List<Future<List<TradingPlatform>>> futureList = executorService.invokeAll(gptList);
			if(null!=futureList && 0!=futureList.size()) {
				for(int index=0;index<futureList.size();index++) {
					Future<List<TradingPlatform>> mFuture = futureList.get(index);
					tpList.addAll(mFuture.get());
				}
			}
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		} catch (ExecutionException e) {
			
			e.printStackTrace();
		}
		//按照TradingPlatform的ranking字段重排序
		Collections.sort(tpList, new Comparator<TradingPlatform>() {

			@Override
			public int compare(TradingPlatform o1, TradingPlatform o2) {
				if(o1.getRanking()<o2.getRanking()) {
					return -1;
				}else if(o1.getRanking()>o2.getRanking()){
					return 1;
				}
				return 0;
			}
		});
		//输出tpList
		/*for(TradingPlatform tp:tpList) {
			ReflectUtils.outputObject(tp);
		}*/
		System.out.println("tpList.size = "+tpList.size()+"---last node ranking = "+tpList.get(tpList.size()-1).getRanking());
		return tpList;
	}
	/**
	 * 
	* @Title: parsePlatformsByJSoup
	* @Description: 解析交易平台数据
	* @param responseString
	* @return List<Map<String,String>>
	* @author leisure
	* @date 2018年6月11日上午10:59:29
	 */
	public List<TradingPlatform> parsePlatformsByJSoup(String responseString) {
		if (null == responseString) {
			return null;
		}
		Document doc = Jsoup.parse(responseString);
		if (null == doc) {
			return null;
		}
		//List<Map<String,String>> mList = new ArrayList<Map<String,String>>();
		List<TradingPlatform> ptList = new ArrayList<TradingPlatform>();
		Element mTable = doc.getElementsByClass("table exchange-table").first();
		Element tableBody = mTable.child(1);
		Elements trs = tableBody.getElementsByTag("tr");
		for (int index = 0; index < trs.size(); index++) {
			//Map<String,String> platformMap = new HashMap<String,String>();
			System.out.println("-----------index = " + index + "---------------");
			Element tr = trs.get(index);
			TradingPlatform tp = new TradingPlatform();
			//交易平台排名
			String ranking = tr.child(0).text();
			//交易平台
			Element t_name = tr.child(1);
			 //a标签
			 Element a_tag = t_name.child(0);
			 String href = a_tag.attr("href");
			 System.out.println("href = "+href+"---href.sub = "+href.substring(1));
			 String name2 = href.substring(href.indexOf("/",1)+1, href.length()-1);
			 //交易平台des
			String alias = t_name.text();
			//交易对数目
			String pairCount = tr.child(3).text();
			//交易所所在国家
			String countryName = tr.child(4).text();
			System.out.println("ranking = "+ranking+"---name = "+name2+"---des = "+alias);
			System.out.println("pairCount = "+pairCount+"---countryName = "+countryName);
			//交易类型
			Element tradeTypeElement = tr.child(5);
			  Elements  childrenE = tradeTypeElement.children();
			  String supportTrading = "";
			  for(Element e:childrenE) {
				  String href2 = e.attr("href");
				  String type2 = href2.substring(href2.length()-1);
				  /*for(Element e2:e.children()) {
				     String title = e2.attr("title");
				     System.out.println("href2 = "+href2+"---title = "+title);
				  }*/
				  String title = e.child(0).attr("title");
				  System.out.println("type = "+type2+"---title = "+title);
				  supportTrading += ","+type2;
			  }
			  if(supportTrading.length()>1) {
			     supportTrading = supportTrading.substring(1);
			  }
			 /* platformMap.put("ranking", ranking);
			  platformMap.put("name", name2);
			  platformMap.put("alias",alias);
			  platformMap.put("pairCount", pairCount);
			  platformMap.put("countryName",countryName);
             
              platformMap.put("supportTrading", supportTrading);*/
              
              tp.setRanking(Integer.parseInt(ranking));
              tp.setName(name2);
              tp.setAlias(alias);
              tp.setCountry(countryName); 
              if(!"?".equals(pairCount)) {
                 tp.setSymbolCount(Integer.parseInt(pairCount));
              }
              tp.setSupportTradingString(supportTrading);
              Date date = new Date();
              tp.setGmtCreate(date);
              tp.setGmtModified(date);
              //mList.add(platformMap);
              ptList.add(tp);
			System.out.println("supportTrading = "+supportTrading);
		}
		return ptList;
	}
	/**
	 * 
	* @Title: gainPlatformSymbols
	* @Description: 从 非小号 平台上分别 获取与交易平台关联的交易对
	* @param tpList 交易平台list
	* @author leisure
	* @date 2018年6月11日下午4:35:16
	 */
	public List<PlatformSymbolRelevance> gainPlatformSymbolRelevance(List<TradingPlatform> tpList  ) {
		if(null==tpList || 0==tpList.size()) {
			return null;
		}
		List<GainPlatformSymbolRelevanceTask> gpsrtList = new ArrayList<GainPlatformSymbolRelevanceTask>();
		List<PlatformSymbolRelevance> platformSymbolRelevanceList = new ArrayList<PlatformSymbolRelevance>();
		ExecutorService executorService = Executors.newCachedThreadPool();
		PlatformBaseDataCollectAction dataCollectAction = new PlatformBaseDataCollectAction();
		//生成tastList
		for(int index=0;index<tpList.size();index++) {
			TradingPlatform tp = tpList.get(index);			
			String url = GET_PLATFORM_SYMBOL_RELEVANCE_PREFIX+tp.getName();
			HttpClientRequestHandler requestHandler = new HttpClientRequestHandler();
			GainPlatformSymbolRelevanceTask gpsrt = new GainPlatformSymbolRelevanceTask(dataCollectAction,
					                                      requestHandler, executorService, tp,url, "GET", "common_platformSymbolRelevance_"+tp.getName()+"_"+tp.getRanking()+"_"+index);
			gpsrtList.add(gpsrt);
		}
		try {
			List<Future<List<PlatformSymbolRelevance>>> futureList = executorService.invokeAll(gpsrtList);
			if(null!=futureList && 0!=futureList.size()) {
				for(int index=0;index<futureList.size();index++) {
				   Future<List<PlatformSymbolRelevance>> mFuture = futureList.get(index);
				   platformSymbolRelevanceList.addAll(mFuture.get());
				}
			}
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		} catch (ExecutionException e) {
			
			e.printStackTrace();
		}
		return platformSymbolRelevanceList;
	}
	/**
	 * 
	* @Title: visitPlatformHomePage
	* @Description: 访问 非小号 的交易平台详情页
	* @param platformName void
	* @author leisure
	* @date 2018年6月11日下午2:31:48
	 */
	public void visitPlatformHomePage(String platformName) {
		StringBuilder sb = new StringBuilder(BASIC_URL)
				               .append("/exchange/")
				               .append(platformName);
		String url = new String(sb);
		ProxyUtils proxy = new ProxyUtils("127.0.0.1",1080,null,null);
		HttpClientRequestHandler requestHandler = new HttpClientRequestHandler(proxy.generateConnectionManager(),proxy.getSocketProxy());
		String responseString = GeneralHttpClientRequestManager.httpClientRequest(requestHandler, "GET", url,
				"exchange_"+platformName);
		
		parsePlatformHomePage(responseString,null);
	
	}

	/**
	 * 
	* @Title: parsePlatformHomePage
	* @Description: 解析访问非小号 交易平台详情页的数据
	* @param responseString
	* @param tradingPlatform 交易平台
	* @return List<PlatformSymbolRelevance>
	* @author leisure
	* @date 2018年6月11日下午5:09:55
	 */
	public List<PlatformSymbolRelevance> parsePlatformHomePage(String responseString,TradingPlatform tradingPlatform) {
		if (null == responseString ) {
			return null;
		}
		Document doc = Jsoup.parse(responseString);
		if (null == doc) {
			return null;
		}
		System.out.println("tradingPlatform.name  = "+tradingPlatform.getName());
		List<PlatformSymbolRelevance> platformSymbolRelevanceList = new ArrayList<PlatformSymbolRelevance>();
		String des = "";
		Elements artBoxElements = doc.getElementsByClass("artBox");
		if(null!=artBoxElements && !artBoxElements.isEmpty()) {
			Elements ptags = artBoxElements.first().getElementsByTag("p");
			if(null!=ptags && !ptags.isEmpty()) {
				des = ptags.first().text();
			}
		}		
		//平台的官网地址
		String officeCite = doc.getElementsByClass("web").first().child(0).text();
		//平台的交易对 对应的table body
		Element tableBody = doc.getElementsByClass("table noBg").first().child(1);
		Elements trs = tableBody.getElementsByTag("tr");
		
		
		System.out.println("officeCite = "+officeCite);
		System.out.println("description = "+des);
		
		tradingPlatform.setOfficialWebsite(officeCite);
		tradingPlatform.setDescription(des);
		
		for (int index = 0; index < trs.size(); index++) {
			PlatformSymbolRelevance psr = new PlatformSymbolRelevance();
			
			System.out.println("-----------index = " + index + "---------------");
			Element tr = trs.get(index);
			//在该交易所的所有交易对中按成交量排名
			String ranking = tr.child(0).text();
			//订单Coin名
			String orderCoinDes = tr.child(1).child(0).text();
			//交易对名
			String name = tr.child(2).text();
			//当前交易价格
			String price = tr.child(3).text();
			//占比
			String proportion = tr.child(6).text();
			//暂时将 交易平台 名存入remark中
			psr.setRemark(tradingPlatform.getName());
			psr.setRanking(Integer.parseInt(ranking));
			psr.setName(name);
			psr.setOrderCoinDes(orderCoinDes);
			psr.setPrice(price);
			psr.setProportion(proportion);
			System.out.println("ranking = "+ranking+"----orderCoinDes = "+orderCoinDes+"---name = "+name+"---price = "+price+"---proportion = "+proportion);
			platformSymbolRelevanceList.add(psr);
		}
		
		return platformSymbolRelevanceList;
		
	}
	
	/**
	 * 
	 * @Title: gainAllCurrencysAndSaveToDB
	 * @Description: 从交易平台获取平台支持的全部Currency,解析并保存到数据库
	 * @param requestHandler
	 * @param tp
	 *            void
	 * @author leisure
	 * @date 2018年6月7日上午11:25:20
	 */
	public void gainAllCurrencysAndSaveToDB(final HttpClientRequestHandler requestHandler, TradingPlatform tp) {
		if (null == tp || null == tp.getFetchAllTickerUrl()) {
			return;
		}
		String url = tp.getFetchAllCurrencyUrl();
		if (null == url || "" == url) {
			return;
		}
		RequestBuilder requestBuilder = RequestBuilder.get().setUri(url);

		// 设置访问的Header
		HttpRequestHeaderGenerator.setGetMarketInfoeaders(requestBuilder, url);
		// 生成访问方法
		HttpUriRequest requestMethod = requestBuilder.build();
		// 保存访问方法
		requestHandler.setRequestMethod(requestMethod);
		// 发送请求
		ResponseRet responseRet = requestHandler.GetHttpResponse_parseMarketInfo(requestHandler, tp.getName());
		if (null == responseRet || null == responseRet.getRetContent()) {
			return;
		}
		// 获取responseString
		String responseString = null;
		try {
			responseString = new String(responseRet.getRetContent(), "utf-8");
		} catch (UnsupportedEncodingException e) {
			// log.error("parse responseString error");
			e.printStackTrace();
		}
		if (null == responseString) {
			return;
		}

	}

	/**
	 * 
	 * @Title: parseAndSaveCurrencys
	 * @Description: 解析并保存Currency
	 * @param tp
	 * @param responseString
	 *            void
	 * @author leisure
	 * @date 2018年6月7日上午11:26:09
	 */
	public void parseAndSaveCurrencys(TradingPlatform tp, String responseString) {
		if (null == tp || null == responseString) {
			return;
		}
		String extractCurrencyString = RegexUtils.findString(responseString, EXTRACT_ALL_CURRENCY_REGEX);
		List<String> currencyStringList = RegexUtils.findValueGroup(extractCurrencyString, PARSE_CURRENCY_REGEX);
		if (null == currencyStringList || 0 == currencyStringList.size()) {
			return;
		}
		// virtualCoinService.saveObject(o);
	}

	/**
	 * 
	 * @Title: gainSymbolsAndSaveToDB
	 * @Description: 从交易平台获取平台支持的全部Symbol,解析并保存到数据库
	 * @param requestHandler
	 * @param tp
	 *            void
	 * @author leisure
	 * @date 2018年6月7日上午11:29:45
	 */
	public void gainSymbolsAndSaveToDB(final HttpClientRequestHandler requestHandler, TradingPlatform tp) {
		if (null == tp || null == tp.getFetchAllTickerUrl()) {
			return;
		}
		String url = tp.getFetchAllSymbolUrl();
		if (null == url || "" == url) {
			return;
		}
		RequestBuilder requestBuilder = RequestBuilder.get().setUri(url);

		// 设置访问的Header
		HttpRequestHeaderGenerator.setGetMarketInfoeaders(requestBuilder, url);
		// 生成访问方法
		HttpUriRequest requestMethod = requestBuilder.build();
		// 保存访问方法
		requestHandler.setRequestMethod(requestMethod);
		// 发送请求
		ResponseRet responseRet = requestHandler.GetHttpResponse_parseMarketInfo(requestHandler, tp.getName());
		if (null == responseRet || null == responseRet.getRetContent()) {
			return;
		}
		// 获取responseString
		String responseString = null;
		try {
			responseString = new String(responseRet.getRetContent(), "utf-8");
		} catch (UnsupportedEncodingException e) {
			// log.error("parse responseString error");
			e.printStackTrace();
		}
		if (null == responseString) {
			return;
		}
	}

	/**
	 * 
	 * @Title: parseAndSaveSymbols
	 * @Description: 解析并保存Symbols
	 * @param tp
	 * @param responseString
	 *            void
	 * @author leisure
	 * @date 2018年6月7日上午11:31:40
	 */
	public void parseAndSaveSymbols(TradingPlatform tp, String responseString) {
		if (null == tp || null == responseString) {
			return;
		}

	}

	public ITradingPlatformService getTradingPlatformService() {
		return tradingPlatformService;
	}

	public void setTradingPlatformService(ITradingPlatformService tradingPlatformService) {
		this.tradingPlatformService = tradingPlatformService;
	}

	public IVirtualCoinService getVirtualCoinService() {
		return virtualCoinService;
	}

	public void setVirtualCoinService(IVirtualCoinService virtualCoinService) {
		this.virtualCoinService = virtualCoinService;
	}

}
