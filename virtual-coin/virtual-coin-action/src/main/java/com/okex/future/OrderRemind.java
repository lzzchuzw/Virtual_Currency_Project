package com.okex.future;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.market.kline.IKLineParser;
import com.market.kline.imp.OkexKLineParser;
import com.market.model.KLine;
import com.market.model.MarketKLineInfo;
import com.okex.future.imp.FutureRestApi;
import com.utils.proxy.ProxyUtils;
import com.utils.request.HttpClientRequestHandler;
import com.utils.tradingIndicator.KDJIndicator;
import com.utils.tradingIndicator.MacdIndicator;
import com.utils.tradingIndicator.MovingAverageIndicator;

public class OrderRemind extends TimerTask {
	// 已经生成数据的容器
	private volatile CopyOnWriteArraySet<String> container;
	
	private IFutureRestApi futureRestApi;
	// btc_usd:比特币 ltc_usd :莱特币
	private String symbol;
	// 1min/3min/5min/15min/30min/1day/3day/1week/1hour/2hour/4hour/6hour/12hour
	private String klinePeriod;
	// 合约类型: this_week:当周 next_week:下周 quarter:季度
	private String contractType;
	//private static Log log = LogFactory.getLog(OrderRemind.class);
	private static Logger log = LoggerFactory.getLogger(OrderRemind.class);
	public static final String FILE_DIRECTORY = "D:/testFolder/virtual_coin/okex/alert/";

	public OrderRemind() {
		ProxyUtils proxy = new ProxyUtils("127.0.0.1", 1080, null, null);
		HttpClientRequestHandler requestHandler = new HttpClientRequestHandler(proxy.generateConnectionManager(),
				proxy.getSocketProxy());
		this.futureRestApi = new FutureRestApi(requestHandler);
		this.symbol = "etc_usd";
		this.klinePeriod = "15min";
		this.contractType = "this_week";
	}

	public OrderRemind(IFutureRestApi futureRestApi, String klinePeriod) {
		this.futureRestApi = futureRestApi;
		this.klinePeriod = klinePeriod;
	}

	public OrderRemind(String symbol, String klinePeriod, String contractType) {
		ProxyUtils proxy = new ProxyUtils("127.0.0.1", 1080, null, null);
		HttpClientRequestHandler requestHandler = new HttpClientRequestHandler(proxy.generateConnectionManager(),
				proxy.getSocketProxy());
		this.futureRestApi = new FutureRestApi(requestHandler);
		this.symbol = symbol;
		this.klinePeriod = klinePeriod;
		this.contractType = contractType;
	}

	@Override
	public void run() {
		// K线信息
		String responseString = futureRestApi.future_kline(this.symbol, this.klinePeriod, this.contractType, 0, 0L);
		IKLineParser okexFutureKLineParser = new OkexKLineParser("okex", "btc_usd", 2);
		MarketKLineInfo mKLineInfo = okexFutureKLineParser.parserKLine(responseString);
		List<KLine> klineList = mKLineInfo.getKlineList();
		// 计算均线
		List<Map<String, String>> MA30ListMap = MovingAverageIndicator.calculateMAListMap(klineList, 30, 2);
		// 获取最后一个点
		Map<String, String> MALastPoint = MA30ListMap.get(MA30ListMap.size() - 1);
		Double closePrice = Double.valueOf(MALastPoint.get("CLOSEPRICE"));
		Double MAPrice = Double.valueOf(MALastPoint.get("MA30"));
		// 需要寻找的是 closePrice < MAPrice的点
		if (closePrice >= MAPrice) {
			return;
		}

		// 计算MACD
		List<Map<String, String>> macdList = MacdIndicator.calculateMACD(klineList, 12, 26, 9, 3);
		Map<String, String> MACDLastPoint = macdList.get(macdList.size() - 1);
		Double MACD = Double.valueOf(MACDLastPoint.get("MACD"));
		if (MACD >= 0) {
			return;
		}
		// 计算KDJ
		List<Map<String, String>> kdjList = KDJIndicator.calculateKDJ(klineList, 9, 3, 3, 2);
		Map<String, String> KDJLastPoint = kdjList.get(kdjList.size() - 1);
		Map<String, String> KDJComparePoint = kdjList.get(kdjList.size() - 2);
		if ((Double.valueOf(KDJLastPoint.get("D")) >= Double.valueOf(KDJLastPoint.get("J")))
				&& (Double.valueOf(KDJComparePoint.get("D")) <= Double.valueOf(KDJComparePoint.get("J")))) {
			String filePath = FILE_DIRECTORY + symbol + "_" + klinePeriod + ".txt";
			String validKey = KDJLastPoint.get("TIME")+klinePeriod;
			if(!container.contains(validKey)) {
				container.add(validKey);
				// trick tip
				log.error(KDJLastPoint.get("TIME")+"["+symbol+"]在"+klinePeriod+":可以了!");
				try {
					FileUtils.writeStringToFile(new File(filePath), KDJLastPoint.get("TIME") + "\r\n",Charset.forName("UTF-8"), true);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}

	}

	/**********************************************************************************************************/
	public IFutureRestApi getFutureRestApi() {
		return futureRestApi;
	}

	public void setFutureRestApi(IFutureRestApi futureRestApi) {
		this.futureRestApi = futureRestApi;
	}

	public String getKlinePeriod() {
		return klinePeriod;
	}

	public void setKlinePeriod(String klinePeriod) {
		this.klinePeriod = klinePeriod;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getContractType() {
		return contractType;
	}

	public void setContractType(String contractType) {
		this.contractType = contractType;
	}

	public void test() {
		String filePath = FILE_DIRECTORY + this.symbol + "_" + this.klinePeriod + ".txt";
		//System.out.println("filePath = " + filePath);
		FileWriter fw = null;
		PrintWriter pw = null;
		try {
			// File file = new File(filePath);
			fw = new FileWriter(filePath, true);
			pw = new PrintWriter(fw);
			// pw.println(KDJLastPoint.get("TIME"));
			pw.println("TIME");
			pw.flush();
			fw.flush();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			pw.close();
			try {
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
