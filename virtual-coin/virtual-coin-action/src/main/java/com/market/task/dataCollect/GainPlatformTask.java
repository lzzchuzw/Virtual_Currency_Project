package com.market.task.dataCollect;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import com.market.action.PlatformBaseDataCollectAction;
import com.market.handler.GeneralHttpClientRequestManager;
import com.market.pojo.TradingPlatform;
import com.utils.request.HttpClientRequestHandler;

public class GainPlatformTask implements Callable<List<TradingPlatform>> {
	private PlatformBaseDataCollectAction dataCollectAction;
	private HttpClientRequestHandler requestHandler;
	private ExecutorService es;
	private String url;
	private String methodType;
	private String fileName;

	public GainPlatformTask() {

	}

	public GainPlatformTask(PlatformBaseDataCollectAction dataCollectAction, HttpClientRequestHandler requestHandler,
			ExecutorService es, String url, String methodType, String fileName) {
		this.dataCollectAction = dataCollectAction;
		this.requestHandler = requestHandler;
		this.es = es;
		this.url = url;
		this.methodType = methodType;
		this.fileName = fileName;
	}

	@Override
	public List<TradingPlatform> call() throws Exception {
		String responseString = GeneralHttpClientRequestManager.httpClientRequest(requestHandler, methodType, url,
					                                                                 fileName);
		List<TradingPlatform> mList = dataCollectAction.parsePlatformsByJSoup(responseString);
		
		
		return mList;
	}
    /*************************************************************************************/
	public PlatformBaseDataCollectAction getDataCollectAction() {
		return dataCollectAction;
	}

	public void setDataCollectAction(PlatformBaseDataCollectAction dataCollectAction) {
		this.dataCollectAction = dataCollectAction;
	}

	public HttpClientRequestHandler getRequestHandler() {
		return requestHandler;
	}

	public void setRequestHandler(HttpClientRequestHandler requestHandler) {
		this.requestHandler = requestHandler;
	}

	public ExecutorService getEs() {
		return es;
	}

	public void setEs(ExecutorService es) {
		this.es = es;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getMethodType() {
		return methodType;
	}

	public void setMethodType(String methodType) {
		this.methodType = methodType;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}
