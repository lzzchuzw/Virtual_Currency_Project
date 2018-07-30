package com.okex.future;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.market.kline.IKLineParser;
import com.market.kline.imp.OkexKLineParser;
import com.market.model.KLine;
import com.market.model.MarketKLineInfo;
import com.okex.future.imp.FutureRestApi;
import com.utils.json.JacksonUtils;
import com.utils.proxy.ProxyUtils;
import com.utils.request.HttpClientRequestHandler;
import com.utils.tradingIndicator.KDJIndicator;
import com.utils.tradingIndicator.MacdIndicator;
import com.utils.tradingIndicator.MovingAverageIndicator;

import redis.clients.jedis.Jedis;

public class FutureClient {

	public static final String REDIS_HOST = "192.168.5.109";
	public static final int REDIS_PORT = 6379;

	public static void main(String[] args) {
		ProxyUtils proxy = new ProxyUtils("127.0.0.1", 1080, null, null);
		HttpClientRequestHandler requestHandler = new HttpClientRequestHandler(proxy.generateConnectionManager(),
				proxy.getSocketProxy());
		IFutureRestApi futureRestApi = new FutureRestApi(requestHandler);

		// 期货行情信息
		// futureRestApi.future_ticker("btc_usd", "this_week");

		// 期货指数信息
		// futureRestApi.future_index("btc_usd");

		// K线信息
		String responseString = futureRestApi.future_kline("etc_usd", "15min", "this_week", 0, 0L);
		IKLineParser okexFutureKLineParser = new OkexKLineParser("okex", "etc_usd", 2);
		MarketKLineInfo mKLineInfo = okexFutureKLineParser.parserKLine(responseString);
		List<KLine> klineList = mKLineInfo.getKlineList();

		// 计算均线
		List<Map<String, String>> MA30ListMap = MovingAverageIndicator.calculateMAListMap(klineList, 30, 2);
		// 判断closePrice在MA30线以下的点;
		List<Map<String, String>> MAStrategyPointList = new ArrayList<Map<String, String>>();
		List<String> MAStrategyTimeList = new ArrayList<String>();
		for (int index = 0; index < MA30ListMap.size(); index++) {
			Map<String, String> MA30Map = MA30ListMap.get(index);
			Double closePrice = Double.valueOf(MA30Map.get("CLOSEPRICE"));
			Double MA30Price = Double.valueOf(MA30Map.get("MA30"));
			if (closePrice < MA30Price) {
				MAStrategyPointList.add(MA30Map);
				MAStrategyTimeList.add(MA30Map.get("TIME"));
			}
		}
		// 计算MACD
		List<Map<String, String>> macdList = MacdIndicator.calculateMACD(klineList, 12, 26, 9, 3);
		// 计算MACD为负的点
		List<Map<String, String>> MACDStrategyPointList = new ArrayList<Map<String, String>>();
		List<String> MACDStrategyTimeList = new ArrayList<String>();
		for (int index = 0; index < macdList.size(); index++) {
			Map<String, String> MACDMap = macdList.get(index);
			Double MACD = Double.valueOf(MACDMap.get("MACD"));
			if (MACD < 0) {
				MACDStrategyPointList.add(MACDMap);
				MACDStrategyTimeList.add(MACDMap.get("TIME"));
			}
		}
		// 计算KDJ
		List<Map<String, String>> kdjList = KDJIndicator.calculateKDJ(klineList, 9, 3, 3, 2);
		List<String> KDJStrategyTimeList = new ArrayList<String>();
		for (int index = 1; index < kdjList.size(); index++) {
			Map<String, String> map = kdjList.get(index);
			Map<String, String> map1 = kdjList.get(index - 1);
			if ((Double.valueOf(map.get("D")) > Double.valueOf(map.get("J")))
					&& (Double.valueOf(map1.get("D")) < Double.valueOf(map1.get("J")))) {
				KDJStrategyTimeList.add(map.get("TIME"));

			}
		}
		// 计算三个策略的交集
		MAStrategyTimeList.retainAll(MACDStrategyTimeList);
		List<String> ret1 = new ArrayList<String>();
		ret1.addAll(MAStrategyTimeList);
		ret1.retainAll(KDJStrategyTimeList);

//		System.out.println("------------------------最后的策略点-----------------");
//		for (int index = 0; index < ret1.size(); index++) {
//
//			System.out.println("inde = " + index + "---time = " + ret1.get(index));
//		}

		/*
		 * for (int index = 0; index < klineList.size(); index++) { KLine kline =
		 * klineList.get(index); System.out.println("index = " + index + "---date:" +
		 * kline.getFormatDate() + "---closePrice = " + kline.getClosePrice()); }
		 * List<Map<String, String>> macdList = MacdIndicator.calculateMACD(klineList,
		 * 12, 26, 9, 3); int count = macdList.size(); for (int index = 0; index < count
		 * - 2; index++) { Map<String, String> map = macdList.get(index); Map<String,
		 * String> map1 = macdList.get(index + 1); Map<String, String> map2 =
		 * macdList.get(index + 2);
		 * 
		 * System.out.println("inde = " + index +"---time = "+map.get("TIME")+
		 * "---MACD = " + map.get("MACD") + "---DIFF = " + map.get("DIFF") + "---DEA = "
		 * + map.get("DEA"));
		 * 
		 * 
		 * String DIFF = map.get("DIFF"); String MACD = map.get("MACD"); String DEA =
		 * map.get("DEA"); String time = map.get("TIME");
		 * 
		 * if(Double.valueOf(MACD)<=0.001) { System.out.println(time); }
		 * 
		 * if (Double.valueOf(map.get("MACD")) < 0.0 && Double.valueOf(map1.get("MACD"))
		 * >= 0.0 && Double.valueOf(map2.get("MACD")) >= 0.0) {
		 * System.out.println(map2.get("TIME")); } }
		 */
		// 连接本地的 Redis 服务
		/*
		 * Jedis jedis = new Jedis(REDIS_HOST,REDIS_PORT); System.out.println("连接成功");
		 * //查看服务是否运行 //System.out.println("服务正在运行: "+jedis.ping()); String jackson_key
		 * = "etc_future_macd"; String jacksonContent =
		 * JacksonUtils.encode(macdList.subList(macdList.size()-100,
		 * macdList.size()-1));
		 * 
		 * jedis.set(jackson_key, jacksonContent); jedis.disconnect();
		 */
		// 期货交易信息
		// futureRestApi.future_trades("btc_usd", "this_week");

		// 期货市场深度
		// futureRestApi.future_depth("btc_usd", "this_week");

		// 美元-人民币汇率
		// futureRestApi.exchange_rate();
		// 期货下单
		/*
		 * String tradeResult = futureRestApi.future_trade("btc_usd","this_week",
		 * "10.134", "1", "1", "0"); JSONObject tradeJSV1 =
		 * JSONObject.parseObject(tradeResult); String tradeOrderV1 =
		 * tradeJSV1.getString("order_id"); System.out.println(tradeOrderV1); //期货用户订单查询
		 * futureRestApi.future_order_info("btc_usd", "this_week",tradeOrderV1, "1",
		 * "1", "2");
		 */
		// 期货账户信息
		// futureRestApi.future_userinfo();
		// 逐仓期货账户信息
		// futureRestApi.future_userinfo_4fix();

	}

}
