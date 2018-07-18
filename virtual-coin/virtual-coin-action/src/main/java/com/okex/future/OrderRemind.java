package com.okex.future;


import java.io.FileNotFoundException;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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



public class OrderRemind extends TimerTask{
    private IFutureRestApi futureRestApi;
    //btc_usd:比特币    ltc_usd :莱特币
    private String symbol;
    //1min/3min/5min/15min/30min/1day/3day/1week/1hour/2hour/4hour/6hour/12hour
    private String klinePeriod;   
    //合约类型: this_week:当周 next_week:下周 quarter:季度
    private String contractType;
    private static Log log = LogFactory.getLog(OrderRemind.class);
    
    public static final String FILE_DIRECTORY = "F:/testFolder/virtual_coin/okex/alert/";
    public OrderRemind() {
    	ProxyUtils proxy = new ProxyUtils("127.0.0.1", 1080, null, null);
		HttpClientRequestHandler requestHandler = new HttpClientRequestHandler(proxy.generateConnectionManager(),
				proxy.getSocketProxy());
		this.futureRestApi = new FutureRestApi(requestHandler);
		this.symbol = "etc_usd";
		this.klinePeriod = "15min";
		this.contractType = "this_week";
    }
    
    public OrderRemind(IFutureRestApi futureRestApi,String klinePeriod) {
    	this.futureRestApi = futureRestApi;
    	this.klinePeriod = klinePeriod;
    }
    
    public OrderRemind(String symbol,String klinePeriod,String contractType) {
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
		//获取最后一个点
		Map<String,String> MALastPoint = MA30ListMap.get(MA30ListMap.size()-1);
		Double closePrice = Double.valueOf(MALastPoint.get("CLOSEPRICE"));
		Double MAPrice = Double.valueOf(MALastPoint.get("MA30"));
		//需要寻找的是 closePrice < MAPrice的点
		if (closePrice >= MAPrice) {
			return;
		}
		
		// 计算MACD
		List<Map<String, String>> macdList = MacdIndicator.calculateMACD(klineList, 12, 26, 9, 3);
		Map<String, String> MACDLastPoint = macdList.get(macdList.size()-1);
		Double MACD = Double.valueOf(MACDLastPoint.get("MACD"));
		if(MACD>=0) {
			return;
		}
		// 计算KDJ
		List<Map<String, String>> kdjList = KDJIndicator.calculateKDJ(klineList, 9, 3, 3, 2);
		Map<String, String> KDJLastPoint = kdjList.get(kdjList.size()-1);
		Map<String, String> KDJComparePoint = kdjList.get(kdjList.size()-2);
		System.out.println("KDJLastPoint.TIME = "+KDJLastPoint.get("TIME")+"----KDJLastPoint.D = "+KDJLastPoint.get("D")+"----KDJLastPoint.J = "+KDJLastPoint.get("J"));
		System.out.println("KDJComparePoint.TIME = "+KDJComparePoint.get("TIME")+"----KDJComparePoint.D = "+KDJComparePoint.get("D")+"----KDJComparePoint.J = "+KDJComparePoint.get("J"));
		if ((Double.valueOf(KDJLastPoint.get("D")) >= Double.valueOf(KDJLastPoint.get("J")))
		 && (Double.valueOf(KDJComparePoint.get("D")) <= Double.valueOf(KDJComparePoint.get("J")))) {
			System.out.println("TIME = "+KDJLastPoint.get("TIME"));
			//Create and set up the window.
	       /* JFrame frame = new JFrame("ShowMessageDialog");
	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        JOptionPane.showMessageDialog(
	        		frame,
	        		this.symbol+"_"+this.klinePeriod,
	                "OKEX下单提醒",	                
	                JOptionPane.INFORMATION_MESSAGE
	        );
	        frame.setVisible(true);*/
			String filePath = FILE_DIRECTORY+symbol+"_"+klinePeriod+".txt";
			log.fatal(filePath);
			System.out.println("filePath = "+filePath);
			FileWriter fw = null;
			/*PrintWriter pw = null;
	        try {
				//File file = new File(filePath);
				fw = new FileWriter(filePath, true);
				pw = new PrintWriter(fw);
				pw.println(KDJLastPoint.get("TIME"));
				pw.flush();
				fw.flush();
			} catch (IOException e) {
				log.error("new FileWriter Exception---"+e.getMessage());
				e.printStackTrace();
			}finally {
				pw.close();
				try {
					fw.close();
				} catch (IOException e) {
					System.out.println("close FileWriter Exception---"+e.getMessage());
					e.printStackTrace();
				}
			}*/
			try {
				fw = new FileWriter(filePath,true);
				fw.write(KDJLastPoint.get("TIME")+"\r\n");
				fw.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally {
				try {
					fw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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
		String filePath = FILE_DIRECTORY+this.symbol+"_"+this.klinePeriod+".txt";
		System.out.println("filePath = "+filePath);
		FileWriter fw = null;
		PrintWriter pw = null;
        try {
			//File file = new File(filePath);
			fw = new FileWriter(filePath, true);
			pw = new PrintWriter(fw);
			//pw.println(KDJLastPoint.get("TIME"));
			pw.println("TIME");
			pw.flush();
			fw.flush();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
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
