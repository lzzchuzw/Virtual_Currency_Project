package com.market.kline;


import com.market.model.MarketKLineInfo;
/**
 * 
* @ClassName: KLineParser
* @Description: 解析从交易平台上获取的KLine数据的接口,每个交易平台的解析是不一样的,需要分别实现该接口
* @author: leisure
* @date: 2018年6月13日 下午4:56:52
 */
public interface IKLineParser {
	
	
	public MarketKLineInfo parserKLine(String responseString);

}
