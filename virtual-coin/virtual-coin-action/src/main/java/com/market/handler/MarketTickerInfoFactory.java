package com.market.handler;

import com.market.action.TradingPlatformAction;
import com.market.model.MarketTickerInfo;
import com.market.pojo.TradingAssetPair;
import com.market.pojo.TradingPlatform;
import com.utils.regex.RegexUtils;

public class MarketTickerInfoFactory {
	
	/**
	 * 
	* @Title: generateMarketTickerInfo
	* @Description: 生成MarketTickerInfo对象
	* @param tpf
	* @param tap
	* @return MarketTickerInfo
	* @author leisure
	* @date 2018年5月25日下午6:22:16
	 */
	public static MarketTickerInfo generateMarketTickerInfo(TradingPlatform tpf,TradingAssetPair tap) {
		MarketTickerInfo mti = new MarketTickerInfo();
		if(null!=tpf && null!=tap) {
			mti.setTradingPlatformId(tpf.getId());
			mti.setTradingPlatformName(tpf.getName());
			mti.setTradingAssetPairId(tap.getId());
			String tradingAsset = tap.getName();
			tradingAsset = RegexUtils.replaceString(tradingAsset, "_", tpf.getConnectingLineString());
			if(1==tpf.getIsUsd()) {
				tradingAsset = RegexUtils.replaceString(tradingAsset, "usdt", "usd");
			}
			if(1==tpf.getIsUpperCase()) {
				tradingAsset = tradingAsset.toUpperCase();
			}
			
			mti.setTradingAssetPairName(tradingAsset);
			//mti.setFetchDataUrl(tpf.getFetchTickerUrl()+mti.getTradingAssetPairName());
			mti.setFetchDataUrl(TradingPlatformAction.generateFetchTickerUlr(tpf, tradingAsset, false));
			mti.setParseServerDateRegex(tpf.getParseServerDateRegex());
			mti.setParseOpenPriceRegex(tpf.getParseOpenPriceRegex());
			mti.setParseHighPriceRegex(tpf.getParseHighPriceRegex());
			mti.setParseLowPriceRegex(tpf.getParseLowPriceRegex());
			mti.setParseLastPriceRegex(tpf.getParseLastPriceRegex());
			mti.setParseTransactionAmountRegex(tpf.getParseTransactionAmountRegex());
			mti.setParseTransactionCountRegex(tpf.getParseTransactionCountRegex());
			mti.setParseTransactionVolumeRegex(tpf.getParseTransactionVolumeRegex());
			mti.setParseBidPriceRegex(tpf.getParseBidPriceRegex());
			mti.setParseBidCountRegex(tpf.getParseBidCountRegex());
			mti.setParseAskPriceRegex(tpf.getParseAskPriceRegex());
			mti.setParseAskCountRegex(tpf.getParseAskCountRegex());
			
		}
		return mti;
	}
	

}
