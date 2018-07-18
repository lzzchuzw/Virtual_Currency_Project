package com.market.task.dataCollect;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.market.action.PlatformBaseDataCollectAction;
import com.market.handler.GeneralHttpClientRequestManager;
import com.market.pojo.VirtualCoin;
import com.utils.request.HttpClientRequestHandler;
/**
 * 
* @ClassName: GainCommonCoinTask
* @Description: 从 非小号 官网抓取虚拟货币
* @author: leisure
* @date: 2018年6月11日 上午8:53:22
 */
public class GainCommonCoinTask implements Callable<List<VirtualCoin>> {
	private PlatformBaseDataCollectAction dataCollectAction;
	private HttpClientRequestHandler requestHandler;
	private ExecutorService es;
	private String url;
	private String methodType;
	private String fileName;
	private List<VirtualCoin> vcList;
	
	
	public GainCommonCoinTask() {
		
	}
	
	public GainCommonCoinTask(PlatformBaseDataCollectAction dataCollectAction,
			                  HttpClientRequestHandler requestHandler,ExecutorService es,
			                  String url,String methodType,String fileName) {
		this.dataCollectAction = dataCollectAction;
		this.requestHandler = requestHandler;
		this.es = es;
		this.url = url;
		this.methodType = methodType;
		this.fileName = fileName;
	}

	
		@Override
		public List<VirtualCoin> call() throws Exception {
			
			String responseString = GeneralHttpClientRequestManager.httpClientRequest(requestHandler, methodType, url,
					fileName);
			List<VirtualCoin> vcList = dataCollectAction.parseByJsoup(responseString);
			return vcList;

		}
		
	

    /***************************************************************************************************/
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

	public List<VirtualCoin> getVcList() {
		return vcList;
	}

	public void setVcList(List<VirtualCoin> vcList) {
		this.vcList = vcList;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	

}
