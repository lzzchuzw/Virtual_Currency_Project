package com.market.task.dataCollect;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import com.market.action.PlatformBaseDataCollectAction;
import com.market.handler.GeneralHttpClientRequestManager;
import com.market.pojo.PlatformSymbolRelevance;
import com.market.pojo.TradingPlatform;
import com.utils.request.HttpClientRequestHandler;

public class GainPlatformSymbolRelevanceTask implements Callable<List<PlatformSymbolRelevance>>{
	private PlatformBaseDataCollectAction dataCollectAction;
	private HttpClientRequestHandler requestHandler;
	private ExecutorService es;
	private TradingPlatform tradingPlatform;
	private String url;
	private String methodType;
	private String fileName;
	
	public GainPlatformSymbolRelevanceTask() {
		
	}
	
	public GainPlatformSymbolRelevanceTask(PlatformBaseDataCollectAction dataCollectAction, HttpClientRequestHandler requestHandler,
			ExecutorService es, TradingPlatform tradingPlatform,String url, String methodType, String fileName) {
		this.dataCollectAction = dataCollectAction;
		this.requestHandler = requestHandler;
		this.es = es;
		this.tradingPlatform = tradingPlatform;
		this.url = url;
		this.methodType = methodType;
		this.fileName = fileName;
	}

	@Override
	public List<PlatformSymbolRelevance> call() throws Exception {
		String responseString = GeneralHttpClientRequestManager.httpClientRequest(requestHandler, methodType, url,
				fileName);
		List<PlatformSymbolRelevance> platformSymbolRelevanceList = dataCollectAction.parsePlatformHomePage(responseString, tradingPlatform);
		return platformSymbolRelevanceList;
	}
}
