package com.market.main;

import com.market.action.PlatformBaseDataCollectAction;
import com.utils.proxy.ProxyUtils;
import com.utils.request.HttpClientRequestHandler;

public class DataCollectExecute {
	
	
	public static void main(String[] args) {
		
		gainVirtualCoinAndSave();
	}
	
	public static void gainVirtualCoinAndSave() {
		PlatformBaseDataCollectAction dataCollectionAction = new PlatformBaseDataCollectAction();
		/*ProxyUtils proxy = new ProxyUtils("127.0.0.1",1080,null,null);
		HttpClientRequestHandler requestHandler = new HttpClientRequestHandler(proxy.generateConnectionManager(),proxy.getSocketProxy());*/
		//dataCollectionAction.gainCommonCoinAndSaveToDB();
		//dataCollectionAction.gainPlatformsAndSaveToDB();
		//dataCollectionAction.visitPlatformHomePage("coinexchange");
		
		/*//从非小号获取交易平台数据并存储到本地硬盘
		String ntptxt = dataCollectionAction.savePlatformInfoSerializable();
		System.out.println("ntptxt = "+ntptxt);*/
		//从 非小号获取与交易平台相关的交易对数据并存储到本地硬盘
		//String psrtxt = dataCollectionAction.savePlatformSymbolRelevanceInfoSerializable();
		
		//dataCollectionAction.gainPlatformsAndSaveToDB();
		
		//dataCollectionAction.saveNPlatformInfoSerializableFromDB2HD();
		
		//dataCollectionAction.savePlatformSymbolRelevance2DB();
		
		dataCollectionAction.generateTradingAssetPairAndSave2DB();
	}

}
