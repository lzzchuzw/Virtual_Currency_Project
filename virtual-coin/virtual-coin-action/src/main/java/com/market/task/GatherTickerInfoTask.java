package com.market.task;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.alibaba.fastjson.JSON;
import com.market.action.TradingPlatformAction;
import com.market.model.MarketTickerInfo;
import com.market.pojo.TradingPlatform;
import com.utils.jedis.SerializableUtils;
import com.utils.json.JacksonUtils;
import com.utils.json.JsonUtils;

import redis.clients.jedis.Jedis;

/**
 * 
* @ClassName: GatherTickerInfoTask
* @Description: 周期性采集TikcerInfo
* @author: leisure
* @date: 2018年6月5日 下午3:17:53
 */
public class GatherTickerInfoTask extends TimerTask{
	
	public static final long DELAY = 1000*5;
	public static final long PEROID = 3000;
	public static final String REDIS_HOST = "192.168.5.106";
	public static final int REDIS_PORT = 6379;
	
	private List<FetchTickerTask> fttList;
	private List<String> tradingAssetList;
	private TradingPlatformAction tradingPlatformAction;
	/**
	 * 存储的是每个周期采集到的TickerInfo信息,结构:<"eth_btc",<"Okex",mti>>
	 * 
	 */
	private Map<String,Map<String,MarketTickerInfo>> gatherTickerInfoMap;
	
	public GatherTickerInfoTask() {
		
	}
	
    public GatherTickerInfoTask(List<FetchTickerTask> fttList,List<String> tradingAssetList) {
		this.fttList = fttList;
		this.tradingAssetList = tradingAssetList;
		this.gatherTickerInfoMap = new HashMap<String,Map<String,MarketTickerInfo>>();
		if(null!=tradingAssetList && 0!=tradingAssetList.size()) {
			for(String tradingAsset:tradingAssetList) {
				if(null!=tradingAsset) {
					Map<String,MarketTickerInfo> map = new HashMap<String,MarketTickerInfo>();
					gatherTickerInfoMap.put(tradingAsset, map);
				}
			}
		}
	}
	
	@Override
	public void run() {
		 if(null==fttList || 0==fttList.size() || null== tradingAssetList || 0==tradingAssetList.size()) {
			 return;
		 }
		 ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		 //连接本地的 Redis 服务
         Jedis jedis = new Jedis(REDIS_HOST,REDIS_PORT);
         System.out.println("连接成功");
         //查看服务是否运行
         System.out.println("服务正在运行: "+jedis.ping());
		 lock.readLock().lock();
		 
		 try {
			 for(FetchTickerTask ftt:fttList) {
				
				 if(null!=ftt) {
					 ConcurrentHashMap<String, LinkedBlockingDeque<MarketTickerInfo>> tickerInfoMap = ftt.getTickerInfoMap();
					 TradingPlatform tradingPlatform = ftt.getTradingPlatform();
					 String jsonString = tradingPlatform.getTradingAssetComparison();
					 Map<String,Object> tradingAssetComparisonMap = null;
					 if(null!=jsonString) {
						 tradingAssetComparisonMap = JsonUtils.json2Map(jsonString);
					 }
					 if(null!=tickerInfoMap && 0!=tickerInfoMap.size()) {
						 for(String tradingAsset:tradingAssetList) {
							 String encodeValue = tradingAsset;
							 System.out.println("tradingAsset = "+tradingAsset);
							 
							 if(null!=tradingAssetComparisonMap&&0!=tradingAssetComparisonMap.size()) {
								 encodeValue = String.valueOf(tradingAssetComparisonMap.get(tradingAsset));
								//System.out.println("tradingAsset = "+tradingAsset+"对应于："+tradingAssetComparisonMap.get(tradingAsset));
								 System.out.println("encodeValue = "+encodeValue);
								 
							 }
							 System.out.println("++tradingAsset = "+tradingAsset);
							 LinkedBlockingDeque<MarketTickerInfo> tradingAssetLbd = tickerInfoMap.get(encodeValue);
							 if(null!=tradingAssetLbd && !tradingAssetLbd.isEmpty()) {
								 MarketTickerInfo mti = tradingAssetLbd.getLast();
								 Map<String,MarketTickerInfo> tMap = gatherTickerInfoMap.get(tradingAsset);
								 if(null!=tMap) {
								    tMap.put(ftt.getPlatformName(), mti);
								    System.out.println("success");
								 }
							 }else {
								 System.out.println("get error");
							 }
						 }
					 }
				 }
			 }
			 Map<String,Map<String,Map<String,String>>> displayInfoMap = new HashMap<String,Map<String,Map<String,String>>>();
			 System.out.println("--------------------遍历displayInfoMap  begin----------------------------------");
			 //遍历Map<String,Map<String,MarketTickerInfo>>
			 Iterator<Map.Entry<String,Map<String,MarketTickerInfo>>> iterator = gatherTickerInfoMap.entrySet().iterator();			
			 while(iterator.hasNext()) {
				 Map.Entry<String,Map<String,MarketTickerInfo>> entry = iterator.next();
				 String coinPair = entry.getKey();
				 System.out.println("tradingAsset: "+coinPair+"=======================>");
				 Map<String,Map<String,String>> map2 = new HashMap<String,Map<String,String>>();
				 Map<String,MarketTickerInfo> tMap = entry.getValue();
				 Iterator<Map.Entry<String,MarketTickerInfo>> it = tMap.entrySet().iterator();				 
				 while(it.hasNext()) {
					 Map.Entry<String,MarketTickerInfo> entry2 = it.next();
					 String tPlatformName = entry2.getKey();
					 MarketTickerInfo mti = entry2.getValue();
					 Map<String,String> map3 = new HashMap<String,String>();
					 map3.put("bidPrice", String.valueOf(mti.getBidPrice()));
					 map3.put("askPrice", String.valueOf(mti.getAskPrice()));
					 map2.put(tPlatformName,map3);
					 System.out.println("platform: "+tPlatformName+"----bidPrice = "+mti.getBidPrice()+"----askPrice = "+mti.getAskPrice());
				 }
				 displayInfoMap.put(coinPair,map2);
			 }
			 System.out.println("--------------------遍历displayInfoMap  end----------------------------------");
			 if(null!=displayInfoMap) {
				 SerializableUtils<Map<String,Map<String,Map<String,String>>>> su = new SerializableUtils<Map<String,Map<String,Map<String,String>>>>();
				 byte[] bytes = su.serializableEncode(displayInfoMap);
				 SerializableUtils<Object[]> keySu = new SerializableUtils<Object[]>();
				 //jedis.append(keySu.serializableEncode(displayInfoMap.keySet()), bytes);
				 byte[] keyArray = keySu.serializableEncode(displayInfoMap.keySet().toArray());
				 String jsonContent = JsonUtils.Map2JsonString(displayInfoMap);
				 System.out.println("jsonContent = "+jsonContent);
				 //displayInfoMap.keySet().
				 String m_key = "key_tickerInfo";
				 String jackson_key = "jackson_key";
				 String jacksonContent = JacksonUtils.encode(displayInfoMap);
				 
				 //String json_key = "json_tickerInfo";
				 //jedis.set(keySu.serializableEncode(displayInfoMap.keySet().toArray()), bytes);
				 jedis.append(m_key.getBytes(), bytes);
				 jedis.append(jackson_key, jacksonContent);
				 //jedis.set(json_key, jsonContent);
				 //String m_s =  jedis.get("key_tickerInfo");
				 
				 //byte[] tt = jedis.get(keySu.serializableEncode(displayInfoMap.keySet().toArray()));
				 byte[] tt = jedis.get(m_key.getBytes());
				 String decode_jackjson_content = jedis.get(jackson_key);
				 //String decodeJson = jedis.get(json_key);
				 //Map<String,Map<String,Map<String,String>>>  saveInfoMap = JSON.parseObject(decodeJson, displayInfoMap.getClass());
				 
				 @SuppressWarnings("unchecked")
				Map<String,Map<String,Map<String,String>>>  saveInfoMap = JacksonUtils.decode(decode_jackjson_content, displayInfoMap.getClass());
				 
				  //Map<String,Map<String,Map<String,String>>>  saveInfoMap = su.serializableDecode(tt);
				 //Map<String,Map<String,Map<String,String>>>  saveInfoMap = JsonUtils.json2Map(jedis.get(json_key));
				 System.out.println("--------------------遍历saveInfoMap  begin----------------------------------");
				 Iterator<Map.Entry<String,Map<String,Map<String,String>>>> it = saveInfoMap.entrySet().iterator();
				 while(it.hasNext()) {
					 Map.Entry<String,Map<String,Map<String,String>>> entry = it.next();
					 String coinKey = entry.getKey();
					 System.out.println("coinKey = "+coinKey+"==================>");
					 Map<String,Map<String,String>> map2 = entry.getValue();
					 Iterator<Map.Entry<String,Map<String,String>>> it2 = map2.entrySet().iterator();
					 while(it2.hasNext()) {
						 Map.Entry<String,Map<String,String>> entry2 = it2.next();
						 String ptKey = entry2.getKey();
						 System.out.print("trading platform = "+ptKey);
						 Map<String,String> map3 = entry2.getValue();
						 Iterator<Map.Entry<String, String>> it3 = map3.entrySet().iterator();
						 while(it3.hasNext()) {
							 Map.Entry<String, String> entry3 = it3.next();
							 System.out.print("----"+entry3.getKey()+" = "+entry3.getValue()+"----");
						 }
						 System.out.println();
					 }
				 }
				 System.out.println("--------------------遍历saveInfoMap  end----------------------------------");
			 }
			 //jedis.set
			  
		 }catch(Exception e) {
			 e.printStackTrace();
		 }finally {
			 jedis.disconnect();
			 lock.readLock().unlock();
		 }
	}

	public List<FetchTickerTask> getFttList() {
		return fttList;
	}

	public void setFttList(List<FetchTickerTask> fttList) {
		this.fttList = fttList;
	}

	public List<String> getTradingAssetList() {
		return tradingAssetList;
	}

	public void setTradingAssetList(List<String> tradingAssetList) {
		this.tradingAssetList = tradingAssetList;
	}

	public TradingPlatformAction getTradingPlatformAction() {
		return tradingPlatformAction;
	}

	public void setTradingPlatformAction(TradingPlatformAction tradingPlatformAction) {
		this.tradingPlatformAction = tradingPlatformAction;
	}

	public Map<String, Map<String, MarketTickerInfo>> getGatherTickerInfoMap() {
		return gatherTickerInfoMap;
	}

	public void setGatherTickerInfoMap(Map<String, Map<String, MarketTickerInfo>> gatherTickerInfoMap) {
		this.gatherTickerInfoMap = gatherTickerInfoMap;
	}

}
