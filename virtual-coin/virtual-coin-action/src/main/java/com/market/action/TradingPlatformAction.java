package com.market.action;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;

import com.market.handler.GeneralHttpClientRequestManager;
import com.market.kline.IKLineParser;
import com.market.model.MarketKLineInfo;
import com.market.model.MarketTickerInfo;
import com.market.pojo.PlatformTickerRelevance;
import com.market.pojo.TradingPlatform;
import com.market.service.IPlatformTickerRelevanceService;
import com.market.service.ITradingPlatformService;
import com.market.service.imp.PlatformTickerRelevanceService;
import com.market.service.imp.TradingPlatformService;
import com.utils.regex.RegexUtils;
import com.utils.request.HttpClientRequestHandler;
import com.utils.request.HttpRequestHeaderGenerator;
import com.utils.request.ResponseRet;

public class TradingPlatformAction {

	private ITradingPlatformService tradingPlatformService;
	private TradingPlatform tp;
	public static boolean DEBUG = false;

	public TradingPlatformAction() {
		init();
	}

	public TradingPlatformAction(String name) {
		init();
		this.tp = this.tradingPlatformService.getObject("where o.name = '" + name + "'");
		
	}

	public TradingPlatformAction(TradingPlatform tp) {
		init();
		this.tp = tp;
		
	}

	public void init() {
		this.tradingPlatformService = new TradingPlatformService();
		
	}

	/******************************************************************************************/
	/**
	 * 
	 * @Title: getPlatformListFromDB
	 * @Description: 按照给定的ptNameList从数据库中读取相应的Platform的list
	 * @param ptNameList
	 * @return List<TradingPlatform>
	 * @author leisure
	 * @date 2018年6月5日下午12:51:56
	 */
	public List<TradingPlatform> getPlatformListFromDB(List<String> ptNameList) {
		String whereCondition = "where o.name in (:alist)";
		List<TradingPlatform> ptList = this.tradingPlatformService.findObjectsByCollections(whereCondition, ptNameList);
		return ptList;
	}

	/**
	 * 
	 * @Title: getTradingAssetPairListFromDB
	 * @Description: 从本地数据库中获取tp所有的交易对
	 * @param tp
	 * @return List<String>
	 * @author leisure
	 * @date 2018年5月29日上午11:04:41
	 */
	public List<String> getTradingAssetPairListFromDB(TradingPlatform tp) {
		if (null == tp) {
			return null;
		}
		String hql = "SELECT b.symbol as symbol " + "FROM TradingPlatform a,PlatformTickerRelevance b "
				+ "WHERE a.name = '" + tp.getName() + "' " + "AND a.id = b.tradingPlatformId ";
		List<Map<String, Object>> listMap = this.tradingPlatformService.findListMapByHql2(hql);
		if (null == listMap || 0 == listMap.size()) {
			//System.out.println("get list map is null");
			return null;
		}
		List<String> symbolList = new ArrayList<String>();
		for (Map<String, Object> map : listMap) {
			if (null != map && 0 < map.size()) {
				Iterator<Map.Entry<String, Object>> iterator = map.entrySet().iterator();
				while (iterator.hasNext()) {
					Map.Entry<String, Object> entry = iterator.next();
					symbolList.add(String.valueOf(entry.getValue()));
				}
			}
		}
		return symbolList;

	}
	
	public void fetchAndSaveCommonSymbols() {
		
	}
	
	//public void fetchAndSave

	/**
	 * 
	 * @Title: fetchAndSaveTradingAssetPair
	 * @Description: 从平台获取交易对并保存到数据库
	 * @param requestHandler
	 * @param tp
	 *            void
	 * @author leisure
	 * @date 2018年5月29日上午9:31:12
	 */
	public void fetchAndSaveTradingAssetPair(final HttpClientRequestHandler requestHandler, TradingPlatform tp) {
		if (null == tp || null == tp.getFetchAllTickerUrl()) {
			return;
		}
		// 请求
		// String url = tp.getFetchAllSymbolUrl();
		String url = tp.getFetchAllTickerUrl();
		//System.out.println("url = " + url);

		RequestBuilder requestBuilder = RequestBuilder.get().setUri(url);
		// 设置访问的Header
		HttpRequestHeaderGenerator.setGetMarketDepthHeaders(requestBuilder, url);
		// 生成访问方法
		HttpUriRequest requestMethod = requestBuilder.build();
		// 保存访问方法
		requestHandler.setRequestMethod(requestMethod);
		// 发送请求
		ResponseRet responseRet = requestHandler.GetHttpResponse_parseMarketInfo(requestHandler, tp.getName());
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
		//System.out.println("responseString = " + responseString);
		parseInfoAndSave(responseString, tp);
	}

	/**
	 * 
	 * @Title: fetchAndSaveSingleTradingAssetPair
	 * @Description: 对于不能一次性获取全部Ticker数据的平台，按照给定的tradingAssetPairList分别单独请求
	 * @param requestHandler
	 * @param tp
	 * @param tradingAssetPairList
	 *            void
	 * @author leisure
	 * @date 2018年6月4日下午2:13:42
	 */
	public void fetchAndSaveSingleTradingAssetPair(final HttpClientRequestHandler requestHandler, TradingPlatform tp,
			List<String> tradingAssetPairList) {
		if (null == tp || null != tradingAssetPairList) {
			return;
		}
		// 请求
		// String url = tp.getFetchAllSymbolUrl();
		String url = tp.getFetchAllTickerUrl();
		//System.out.println("url = " + url);

		RequestBuilder requestBuilder = RequestBuilder.get().setUri(url);
		// 设置访问的Header
		HttpRequestHeaderGenerator.setGetMarketDepthHeaders(requestBuilder, url);
		// 生成访问方法
		HttpUriRequest requestMethod = requestBuilder.build();
		// 保存访问方法
		requestHandler.setRequestMethod(requestMethod);
		// 发送请求
		ResponseRet responseRet = requestHandler.GetHttpResponse_parseMarketInfo(requestHandler, tp.getName());
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
		//System.out.println("responseString = " + responseString);
		parseInfoAndSave(responseString, tp);
	}

	/**
	 * 
	 * @Title: parseInfoAndSave
	 * @Description: 解析从平台获取的全部ticker信息并保存到数据库
	 * @param responseString
	 * @param tp
	 *            void
	 * @author leisure
	 * @date 2018年5月29日上午9:31:42
	 */
	public void parseInfoAndSave(String responseString, TradingPlatform tp) {
		if (null == responseString || null == tp) {
			return;
		}
		String src = responseString;
		// server date
		String s = "";
		String serverDate = null;
		List<String> symbol = null;
		List<String> openPrice = null;
		List<String> highPrice = null;
		List<String> lowPrice = null;
		List<String> lastPrice = null;
		List<String> bidPrice = null;
		List<String> bidCount = null;
		List<String> askPrice = null;
		List<String> askCount = null;
		s = tp.getParseServerDateRegex();
		IPlatformTickerRelevanceService ptrService = new PlatformTickerRelevanceService();
		if (null != s) {
			serverDate = RegexUtils.findString(src, s);
		}
		s = tp.getParseAllTickerRegex();
		if (null != s) {
			src = RegexUtils.findString(src, s);
		}

		s = tp.getParseSymbolRegex();
		if (null != s) {
			symbol = RegexUtils.findValueGroup(src, s);
		}
		s = tp.getParseOpenPriceRegex();
		if (null != s) {
			openPrice = RegexUtils.findValueGroup(src, s);
		}
		s = tp.getParseHighPriceRegex();
		if (null != s) {
			highPrice = RegexUtils.findValueGroup(src, s);
		}
		s = tp.getParseLowPriceRegex();
		if (null != s) {
			lowPrice = RegexUtils.findValueGroup(src, s);
		}
		s = tp.getParseLastPriceRegex();
		if (null != s) {
			lastPrice = RegexUtils.findValueGroup(src, s);
		}
		s = tp.getParseBidPriceRegex();
		if (null != s) {
			bidPrice = RegexUtils.findValueGroup(src, s);
		}
		s = tp.getParseBidCountRegex();
		if (null != s) {
			bidCount = RegexUtils.findValueGroup(src, s);
		}
		s = tp.getParseAskPriceRegex();
		if (null != s) {
			askPrice = RegexUtils.findValueGroup(src, s);
		}
		s = tp.getParseAskCountRegex();
		if (null != s) {
			askCount = RegexUtils.findValueGroup(src, s);
		}
		for (int index = 0; index < symbol.size(); index++) {
			PlatformTickerRelevance ptr = new PlatformTickerRelevance();
			ptr.setTradingPlatformId(tp.getId());
			if (null != serverDate) {
				if ("Okex".equals(tp.getName())) {
					ptr.setServerDate(new Date(Long.parseLong(serverDate) * 1000));
				}
			}
			if (DEBUG) {

				//System.out.println("-----------------index = " + index + "------------------");
				//System.out.println("serverDate = " + serverDate);
			}
			String value = "";
			if (null != symbol && null != (value = symbol.get(index))) {
				ptr.setSymbol(value);
				if (DEBUG) {
					//System.out.println("symbol = " + value + "---translate value = " + ptr.getSymbol());
				}
			}
			if (null != openPrice && null != (value = (openPrice.get(index).replace("\"", "")))) {
				ptr.setOpenPrice(Double.valueOf(value));
				if (DEBUG) {
					System.out.println("openPrice = " + value + "---translate value = " + ptr.getOpenPrice());
				}
			}
			if (null != highPrice && null != (value = (highPrice.get(index).replace("\"", "")))) {
				ptr.setHighPrice(Double.valueOf(value));
				if (DEBUG) {
					System.out.println("highPrice = " + value + "---translate value = " + ptr.getHighPrice());
				}
			}
			if (null != lowPrice && null != (value = (lowPrice.get(index)).replace("\"", ""))) {
				ptr.setLowPrice(Double.valueOf(value));
				if (DEBUG) {
					//System.out.println("lowPrice = " + value + "---translate value = " + ptr.getLowPrice());
				}
			}
			if (null != lastPrice && null != (value = (lastPrice.get(index).replace("\"", "")))) {
				ptr.setLastPrice(Double.valueOf(value));
				if (DEBUG) {
					//System.out.println("lastPrice = " + value + "---translate value = " + ptr.getLastPrice());
				}
			}
			if (null != bidPrice && null != (value = (bidPrice.get(index).replace("\"", "")))) {
				ptr.setBidPrice(Double.valueOf(value));
				if (DEBUG) {
					//System.out.println("bidPrice = " + value + "---translate value = " + ptr.getBidPrice());
				}
			}
			if (null != bidCount && null != (value = (bidCount.get(index).replace("\"", "")))) {
				ptr.setBidCount(Double.valueOf(value));
				if (DEBUG) {
					//System.out.println("bidCount = " + value + "---translate value = " + ptr.getBidCount());
				}
			}
			if (null != askPrice && null != (value = (askPrice.get(index).replace("\"", "")))) {
				ptr.setAskPrice(Double.valueOf(value));
				if (DEBUG) {
					//System.out.println("askPrice = " + value + "---translate value = " + ptr.getAskPrice());
				}
			}
			if (null != askCount && null != (value = (askCount.get(index).replace("\"", "")))) {
				ptr.setAskCount(Double.valueOf(value));
				if (DEBUG) {
					//System.out.println("askCount = " + value + "---translate value = " + ptr.getAskCount());
				}
			}
			if (null != ptr.getServerDate()) {
				ptr.setGmtCreate(ptr.getServerDate());
				ptr.setGmtModified(ptr.getServerDate());
			} else {
				Date date = new Date();
				ptr.setGmtCreate(date);
				ptr.setGmtModified(date);
			}

			ptrService.saveObject(ptr);
		}
	}

	/**
	 * 
	 * @Title: fetchSingleTickerInfoAndParse
	 * @Description: 获取单条TickerInfo
	 * @param requestHandler
	 * @param tradingAsset
	 * @param tp
	 * @param enforcingSingle
	 * @return Map<String,MarketTickerInfo>
	 * @author leisure
	 * @date 2018年6月5日上午11:14:05
	 */
	public Map<String, MarketTickerInfo> fetchSingleTickerInfoAndParse(final HttpClientRequestHandler requestHandler,
			String tradingAsset, TradingPlatform tp, boolean enforcingSingle) {
		if (null == requestHandler || null == tp || null == tradingAsset) {
			return null;
		}
		String responseString = getTickersInfo(requestHandler, tp, tradingAsset, enforcingSingle);
		Map<String, MarketTickerInfo> mtiMap = parseTickersInfo(responseString, tp, tradingAsset);
		return mtiMap;
	}

	/**
	 * 
	 * @Title: fetchTickersInfoAndParse
	 * @Description: 从交易平台一次获取全部的Ticker数据并解析
	 * @param requestHandler
	 * @param tp
	 *            交易平台
	 * @return Map<String,MarketTickerInfo> 全部Ticker信息
	 * @author leisure
	 * @date 2018年5月29日上午10:14:46
	 */
	public Map<String, MarketTickerInfo> fetchTickersInfoAndParse(final HttpClientRequestHandler requestHandler,
			TradingPlatform tp) {
		if (null == requestHandler || null == tp) {
			return null;
		}
		String responseString = getTickersInfo(requestHandler, tp, null, false);
		Map<String, MarketTickerInfo> mtiMap = parseTickersInfo(responseString, tp, null);
		return mtiMap;
	}

	/**
	 * 
	 * @Title: fetchTickerInfoAndParse
	 * @Description: 获取Ticker信息
	 * @param requestHandler
	 * @param tradingAsset
	 * @param tp
	 * @param enforcingSingle
	 * @return Map<String,MarketTickerInfo>
	 * @author leisure
	 * @date 2018年6月5日下午2:37:28
	 */
	public Map<String, MarketTickerInfo> fetchTickerInfoAndParse(final HttpClientRequestHandler requestHandler,
			String tradingAsset, TradingPlatform tp, boolean enforcingSingle) {

		if (null == requestHandler || null == tp || (enforcingSingle && null == tradingAsset)) {
			//System.out.println("fetchTickerInfoAndParse----param error");
			return null;
		}
		String responseString = getTickersInfo(requestHandler, tp, tradingAsset, enforcingSingle);
		Map<String, MarketTickerInfo> mtiMap = parseTickersInfo(responseString, tp, null);
		return mtiMap;
	}
	/**
	 * 
	* @Title: fetchKLineInfo
	* @Description: 从交易平台获取KLine 信息
	* @param requestHandler
	* @param tradingAssetParam 交易对参数
	* @param tp
	* @param periodParam 周期参数
	* @param sizeParam 获取条数参数
	* @return String
	* @author leisure
	* @date 2018年6月14日上午9:38:59
	 */
	public String fetchKLineInfo(final HttpClientRequestHandler requestHandler,TradingPlatform tradingPlatform,Map<String,String> paramMap) {
		if (null == requestHandler || null == tradingPlatform || null == paramMap || 0 == paramMap.size()) {
			//System.out.println("param is null");
			return null;
		}
		StringBuilder sb = new StringBuilder(tradingPlatform.getFetchKLineUrl())
				               .append("?")
				               .append(paramMap.get("symbolName"))
				               .append("=")
				               .append(paramMap.get("symbolValue"))
				               .append("&")
				               .append(paramMap.get("typeName"))
				               .append("=")
				               .append(paramMap.get("typeValue"));
		String url = new String(sb);
		
		//System.out.println("url = "+url);		
		String responseString = GeneralHttpClientRequestManager.httpClientRequest(requestHandler, "GET", url, tp.getName()+"_"+paramMap.get("symbolValue")+"_kline");
		return responseString;
		
	}
	/**
	 * 
	* @Title: parseKLineInfoString
	* @Description: 解析从交易平台获取的KLine数据
	* @param responseString
	* @param klineParser
	* @return MarketKLineInfo
	* @author leisure
	* @date 2018年6月14日上午9:49:39
	 */
	public MarketKLineInfo parseKLineInfoString(String responseString,IKLineParser klineParser) {
		if(null==responseString || null==klineParser) {
			//System.out.println("parseKLineInfoString---param is null");
			return null;
		}
		return klineParser.parserKLine(responseString);
	}

	/**
	 * 
	 * @Title: getTickersInfo
	 * @Description: 从交易平台一次获取全部的Ticker信息
	 * @param requestHandler
	 * @param tp
	 *            void
	 * @author leisure
	 * @date 2018年5月29日上午9:39:08
	 */
	/**
	 * 
	 * @Title: getTickersInfo
	 * @Description: 从交易平台获取Ticker信息
	 * @param requestHandler
	 * @param tp
	 *            交易平台
	 * @param tradingAsset
	 *            交易对
	 * @param enforcingSingle
	 *            是否只请求获取单条交易对信息
	 * @return String 服务器返回得到的Ticker请求数据
	 * @author leisure
	 * @date 2018年6月5日上午11:15:16
	 */
	public String getTickersInfo(final HttpClientRequestHandler requestHandler, TradingPlatform tp, String tradingAsset,
			boolean enforcingSingle) {
		if (null == requestHandler || null == tp || (enforcingSingle && null == tradingAsset)) {
			//System.out.println("getTickersInfo----param error");
			return null;
		}
		String url = generateFetchTickerUlr(tp, tradingAsset, enforcingSingle);
		//System.out.println("url = " + url);

		RequestBuilder requestBuilder = RequestBuilder.get().setUri(url);
		// 设置访问的Header
		HttpRequestHeaderGenerator.setRequestMarketInfoHeaders(requestBuilder, url);
		// 生成访问方法
		HttpUriRequest requestMethod = requestBuilder.build();
		// 保存访问方法
		requestHandler.setRequestMethod(requestMethod);
		// 发送请求
		ResponseRet responseRet = requestHandler.GetHttpResponse_getMarketTickerDataInfo(requestHandler, tp.getName());
		// 获取responseString
		String responseString = null;
		if (null == responseRet.getRetContent()) {
			return null;
		}
		try {
			responseString = new String(responseRet.getRetContent(), "utf-8");
		} catch (UnsupportedEncodingException e) {
			// log.error("parse responseString error");
			e.printStackTrace();
		}
		if (null == responseString) {
			return null;
		}
		//System.out.println("responseString = " + responseString);
		return responseString;
	}

	/**
	 * 
	 * @Title: generateFetchTickerUlr
	 * @Description: 生成平台获取Ticker信息的url
	 * @param tp
	 * @param tradingAsset
	 * @param enforcingSingle
	 * @return String
	 * @author leisure
	 * @date 2018年6月4日下午2:48:41
	 */
	public static String generateFetchTickerUlr(TradingPlatform tp, String tradingAsset, boolean enforcingSingle) {
		if (null == tp) {
			return null;
		}
		tradingAsset = translateTradingAsset(tp, tradingAsset);
		String fetchTickerUrl = tp.getFetchTickerUrlPrefix() + tradingAsset;

		switch (tp.getFetchTickerType()) {
		case 0:
			if (!enforcingSingle) {
				fetchTickerUrl = tp.getFetchAllTickerUrl();
			}
			break;
		case 1:
			// fetchTickerUrl = tp.getFetchTickerUrlPrefix()+tradingAsset;
			break;
		case 2:
			fetchTickerUrl = fetchTickerUrl + tp.getFetchTickerUrlPostfix();
			break;
		default:
			break;
		}
		//System.out.println("fetchTickerUrl = " + fetchTickerUrl);
		return fetchTickerUrl;
	}

	/**
	 * 
	 * @Title: translateTradingAsset
	 * @Description: 将统一的交易对转换为各平台自己的交易对
	 * @param tp
	 * @param tradingAsset
	 * @return String
	 * @author leisure
	 * @date 2018年6月4日下午3:12:24
	 */
	public static String translateTradingAsset(TradingPlatform tp, String tradingAsset) {
		if (null == tp || null == tradingAsset) {
			return null;
		}
		// 连接符的更换
		tradingAsset = RegexUtils.replaceString(tradingAsset, "_", tp.getConnectingLineString()).toUpperCase();
		// usdt 转 usd
		if (1 == tp.getIsUsd()) {
			tradingAsset = RegexUtils.replaceString(tradingAsset, "usdt", "usd");
		}
		// 是否大写
		if (1 == tp.getIsUpperCase()) {
			tradingAsset = tradingAsset.toUpperCase();
		}

		return tradingAsset;
	}
	
	

	/**
	 * 
	 * @Title: parseTickersInfo
	 * @Description: 解析Ticker数据
	 * @return Map<String,MarketTickerInfo> key:交易对名 value:解析Ticker数据,生成的Ticker对象
	 * @author leisure
	 * @date 2018年5月29日上午9:39:38
	 */
	public Map<String, MarketTickerInfo> parseTickersInfo(String responseString, TradingPlatform tp,
			String tradingAsset) {
		if (null == responseString || null == tp) {
			return null;
		}
		String src = responseString;
		//System.out.println("tradingAsset = "+tradingAsset);
		// server date
		String s = "";
		String serverDate = null;
		List<String> symbol = null;
		List<String> openPrice = null;
		List<String> highPrice = null;
		List<String> lowPrice = null;
		List<String> lastPrice = null;
		List<String> bidPrice = null;
		List<String> bidCount = null;
		List<String> askPrice = null;
		List<String> askCount = null;
		Map<String, MarketTickerInfo> mtiMap = new HashMap<String, MarketTickerInfo>();
		s = tp.getParseServerDateRegex();
		if (null != s) {
			serverDate = RegexUtils.findString(src, s);
		}
		s = tp.getParseAllTickerRegex();
		if (null != s) {
			src = RegexUtils.findString(src, s);
		}
		s = tp.getParseSymbolRegex();
		if (null != s) {
			symbol = RegexUtils.findValueGroup(src, s);
		}
		s = tp.getParseOpenPriceRegex();
		if (null != s) {
			openPrice = RegexUtils.findValueGroup(src, s);
		}
		s = tp.getParseHighPriceRegex();
		if (null != s) {
			highPrice = RegexUtils.findValueGroup(src, s);
		}
		s = tp.getParseLowPriceRegex();
		if (null != s) {
			lowPrice = RegexUtils.findValueGroup(src, s);
		}
		s = tp.getParseLastPriceRegex();
		if (null != s) {
			lastPrice = RegexUtils.findValueGroup(src, s);
		}
		s = tp.getParseBidPriceRegex();
		if (null != s) {
			bidPrice = RegexUtils.findValueGroup(src, s);
		}
		s = tp.getParseBidCountRegex();
		if (null != s) {
			bidCount = RegexUtils.findValueGroup(src, s);
		}
		s = tp.getParseAskPriceRegex();
		if (null != s) {
			askPrice = RegexUtils.findValueGroup(src, s);
		}
		s = tp.getParseAskCountRegex();
		if (null != s) {
			askCount = RegexUtils.findValueGroup(src, s);
		}
		// LinkedBlockingDeque<MarketTickerInfo> mtiDeque;
		for (int index = 0; index < symbol.size(); index++) {
			MarketTickerInfo mti = new MarketTickerInfo();
			mti.setTradingPlatformId(tp.getId());
			mti.setTradingPlatformName(tp.getName());
			// mti.setTradingAssetPairName(symbol.get(index));
			if (null != serverDate) {
				if ("Okex".equals(tp.getName())) {
					mti.setServerDate(new Date(Long.parseLong(serverDate) * 1000));
				}
			}
			// mti.setServerDate(new Date(Long.parseLong(serverDate)*1000));
			if (DEBUG) {
				//System.out.println("-----------------index = " + index + "------------------");
				//System.out.println("serverDate = " + serverDate);
			}
			String value = "";
			// tradingAsset
			if (0 == tp.getFetchTickerType()) {
				if (null != symbol && null != (value = symbol.get(index))) {
					
					mti.setTradingAssetPairName(value);
					
					if (DEBUG) {
						System.out
								.println("symbol = " + value + "---translate value = " + mti.getTradingAssetPairName());
					}
				}
			} else {
				
				//System.out.println("tp.getFetchTickerType() = "+tp.getFetchTickerType()+"----set tradingAsset = "+tradingAsset);
				mti.setTradingAssetPairName(tradingAsset);
			}
			if (null != openPrice && null != (value = (openPrice.get(index)).replace("\"", ""))) {
				mti.setOpenPrice(Double.valueOf(value));
				if (DEBUG) {
					//System.out.println("openPrice = " + value + "---translate value = " + mti.getOpenPrice());
				}
			}
			if (null != highPrice && null != (value = (highPrice.get(index)).replace("\"", ""))) {
				mti.setHighPrice(Double.valueOf(value));
				if (DEBUG) {
					//System.out.println("highPrice = " + value + "---translate value = " + mti.getHighPrice());
				}
			}
			if (null != lowPrice && null != (value = (lowPrice.get(index)).replace("\"", ""))) {
				mti.setLowPrice(Double.valueOf(value));
				if (DEBUG) {
					//System.out.println("lowPrice = " + value + "---translate value = " + mti.getLowPrice());
				}
			}
			if (null != lastPrice && null != (value = (lastPrice.get(index)).replace("\"", ""))) {
				mti.setLastPrice(Double.valueOf(value));
				if (DEBUG) {
					//System.out.println("lastPrice = " + value + "---translate value = " + mti.getLastPrice());
				}
			}
			if (null != bidPrice && null != (value = (bidPrice.get(index)).replace("\"", ""))) {
				mti.setBidPrice(Double.valueOf(value));
				if (DEBUG) {
					//System.out.println("bidPrice = " + value + "---translate value = " + mti.getBidPrice());
				}
			}
			if (null != bidCount && null != (value = (bidCount.get(index)).replace("\"", ""))) {
				mti.setBidCount(Double.valueOf(value));
				if (DEBUG) {
					//System.out.println("bidCount = " + value + "---translate value = " + mti.getBidCount());
				}
			}
			if (null != askPrice && null != (value = (askPrice.get(index)).replace("\"", ""))) {
				mti.setAskPrice(Double.valueOf(value));
				if (DEBUG) {
					//System.out.println("askPrice = " + value + "---translate value = " + mti.getAskPrice());
				}
			}
			if (null != askCount && null != (value = (askCount.get(index)).replace("\"", ""))) {
				mti.setAskCount(Double.valueOf(value));
				if (DEBUG) {
					//System.out.println("askCount = " + value + "---translate value = " + mti.getAskCount());
				}
			}

			if (null == mti.getServerDate()) {
				mti.setServerDate(new Date());
			}
			mtiMap.put(mti.getTradingAssetPairName(), mti);

		}
		// 遍历mtiMap
		if (DEBUG) {
			Iterator<Map.Entry<String, MarketTickerInfo>> iterator = mtiMap.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, MarketTickerInfo> entry = iterator.next();
				String key = entry.getKey();
				MarketTickerInfo mti = entry.getValue();
//				System.out.println("key = " + key + "---mti.askPrice = " + mti.getAskPrice() + "---mti.bidPrice = "
//						+ mti.getBidPrice());
			}
		}
		return mtiMap;
	}

	/***********************************************************************************************/
	public ITradingPlatformService getTradingPlatformService() {
		return tradingPlatformService;
	}

	public void setTradingPlatformService(ITradingPlatformService tradingPlatformService) {
		this.tradingPlatformService = tradingPlatformService;
	}

	public TradingPlatform getTp() {
		return tp;
	}

	public void setTp(TradingPlatform tp) {
		this.tp = tp;
	}

	

}
