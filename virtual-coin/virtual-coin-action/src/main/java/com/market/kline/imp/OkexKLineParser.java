package com.market.kline.imp;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.DateUtil;

import com.market.kline.IKLineParser;
import com.market.model.KLine;
import com.market.model.MarketKLineInfo;
import com.utils.date.DateUtils;
import com.utils.regex.RegexUtils;

public class OkexKLineParser implements IKLineParser{
	/**
	 * 交易平台   币安网、火币网... and so on
	 */
	private String tradingPlatformName = "Okex";
	/**
	 * 交易对   比如火币网 :btcusdt, bchbtc, rcneth ...
	 */
	private String tradingAssetPairName;
	/**
	 * 交易类型  1:现货交易 2:期货交易(合约交易)
	 */
	private int tradingType;
	

	
	
	public OkexKLineParser(String tradingPlatformName,String tradingAssetPairName) {
		this.tradingPlatformName = tradingPlatformName;
		this.tradingAssetPairName = tradingAssetPairName;
	}
	
	public OkexKLineParser(String tradingPlatformName,String tradingAssetPairName,int tradingType) {
		this.tradingPlatformName = tradingPlatformName;
		this.tradingAssetPairName = tradingAssetPairName;
		this.tradingType = tradingType;
	}
	
	@Override
	public MarketKLineInfo parserKLine(String responseString) {
		if(null==responseString) {
			return null;
		}
		//去掉最外层中括号
		String src = responseString.substring(1, responseString.length()-1);
		//按照中括号将每条数据分隔开
		List<String> klineList = RegexUtils.findValueGroup(src, "(?<=\\[).*?(?=\\])");
		if(null==klineList || 0==klineList.size()) {
			return null;
		}
		MarketKLineInfo mKLineInfo = new MarketKLineInfo(tradingPlatformName,tradingAssetPairName);
		mKLineInfo.setResponseDataString(responseString);
		List<KLine> klineInfoList = new ArrayList<KLine>();
		for(String s:klineList) {
			List<String> dataList = RegexUtils.findValueGroup(s, "\\d+(\\.\\d+)?");
			KLine klineInfo = new KLine();
			int index = 0;
			//Date
			klineInfo.setFormatDate(DateUtils.dateFormate(Long.parseLong(dataList.get(index++))));
			//open price
			klineInfo.setOpenPrice(Double.parseDouble(dataList.get(index++)));
			//最高价
			klineInfo.setHighPrice(Double.parseDouble(dataList.get(index++)));
			//最低价
			klineInfo.setLowPrice(Double.parseDouble(dataList.get(index++)));
			//收盘价
			klineInfo.setClosePrice(Double.parseDouble(dataList.get(index++)));
			//交易量
			klineInfo.setTransactionAmount(Double.parseDouble(dataList.get(index++)));
			
			klineInfoList.add(klineInfo);
			
		}
		mKLineInfo.setKlineList(klineInfoList);
		
		return mKLineInfo;	
	}

	public String getTradingPlatformName() {
		return tradingPlatformName;
	}

	public void setTradingPlatformName(String tradingPlatformName) {
		this.tradingPlatformName = tradingPlatformName;
	}

	public String getTradingAssetPairName() {
		return tradingAssetPairName;
	}

	public void setTradingAssetPairName(String tradingAssetPairName) {
		this.tradingAssetPairName = tradingAssetPairName;
	}

	public int getTradingType() {
		return tradingType;
	}

	public void setTradingType(int tradingType) {
		this.tradingType = tradingType;
	}

	

}
