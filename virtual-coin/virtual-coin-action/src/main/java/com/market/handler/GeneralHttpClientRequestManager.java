package com.market.handler;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;

import com.utils.request.HttpClientRequestHandler;
import com.utils.request.HttpRequestHeaderGenerator;
import com.utils.request.ResponseRet;

public class GeneralHttpClientRequestManager {
	
	
    /**
     * 
    * @Title: httpClientRequest
    * @Description: 通用的HttpClient请求获取返回数据的方法,适合REST API访问
    * @param requestHandler
    * @param methodType  GET POST
    * @param url
    * @param saveFileName
    * @return String  responseString
    * @author leisure
    * @date 2018年6月7日下午2:22:50
     */
	public static String httpClientRequest(HttpClientRequestHandler requestHandler, String methodType, String url,String saveFileName) {
		// 获取responseString
		String responseString = null;
		if (null == requestHandler || null == methodType || null == url) {
			return responseString;
		}
		RequestBuilder requestBuilder = RequestBuilder.get().setUri(url);

		// 设置访问的Header
		if("GET".equals(methodType)) {
		   HttpRequestHeaderGenerator.setGetMarketInfoeaders(requestBuilder, url);
		}else {//POST method
			HttpRequestHeaderGenerator.setPostMarketInfoHeaders(requestBuilder, url);
		}
		// 生成访问方法
		HttpUriRequest requestMethod = requestBuilder.build();
		
		// 保存访问方法
		requestHandler.setRequestMethod(requestMethod);
		// 发送请求
		ResponseRet responseRet = requestHandler.GetHttpResponse_parseMarketInfo(requestHandler, saveFileName);
		if (null == responseRet || null == responseRet.getRetContent()) {
			return responseString;
		}
		try {
			responseString = new String(responseRet.getRetContent(), "utf-8");
		} catch (UnsupportedEncodingException e) {
			// log.error("parse responseString error");
			e.printStackTrace();
		}
		return responseString;
	}
	/**
	 * 
	* @Title: httpClientRequest
	* @Description: 通用的HttpClient请求获取返回数据的方法,适合REST API访问  添加了参数paramMap适合捎上不同的参数
	* @param requestHandler
	* @param methodType
	* @param url
	* @param paramMap
	* @param saveFileName
	* @return String
	* @author leisure
	* @date 2018年6月14日上午10:15:47
	 */
	public static String httpClientRequest(HttpClientRequestHandler requestHandler, String methodType, String url,Map<String,String> paramMap,String saveFileName) {
		// 获取responseString
		String responseString = null;
		if (null == requestHandler || null == methodType || null == url ||null == paramMap || 0 == paramMap.size()) {
			return responseString;
		}
		RequestBuilder requestBuilder = null;

		// 设置访问的Header
		if("GET".equals(methodType)) {
			System.out.println("get method");
		   requestBuilder = RequestBuilder.get().setUri(url);
		   HttpRequestHeaderGenerator.setGetMarketInfoeaders(requestBuilder, url);
		   
		}else {//POST method
			System.out.println("post method");
			requestBuilder = RequestBuilder.post().setUri(url);
			HttpRequestHeaderGenerator.setPostMarketInfoHeaders(requestBuilder, url);
		}
		// 生成访问方法
		HttpUriRequest requestMethod = requestBuilder.build();
		Header[] header = HttpClientRequestHandler.translateMapToHeaderArray(paramMap);
		requestMethod.setHeaders(header);
		// 保存访问方法
		requestHandler.setRequestMethod(requestMethod);
		// 发送请求
		ResponseRet responseRet = requestHandler.GetHttpResponse_parseMarketInfo(requestHandler, saveFileName);
		if (null == responseRet || null == responseRet.getRetContent()) {
			return responseString;
		}
		try {
			responseString = new String(responseRet.getRetContent(), "utf-8");
		} catch (UnsupportedEncodingException e) {
			// log.error("parse responseString error");
			e.printStackTrace();
		}
		return responseString;
	}
	
	/**
	 * 
	* @Title: httpClientRequest
	* @Description: 通用访问方法  在post方法时可能会携带Header或者Entity
	* 携带Header类似于访问时  https://www.baidu.com?a=1&b=2
	* 携带Entity是post请求特有的,提交表单数据
	* @param requestHandler
	* @param methodType
	* @param url
	* @param headerMap
	* @param entityMap
	* @param saveFileName
	* @return String
	* @author leisure
	* @date 2018年7月4日下午3:56:24
	 */
	public static String httpClientRequest(HttpClientRequestHandler requestHandler, String methodType, String url,
			                               Map<String,String> headerMap,Map<String,String> entityMap,
			                               String fileDirPath,String saveFileName) {
		// 获取responseString
		String responseString = null;
		if (null == requestHandler || null == methodType || null == url ) {
			return responseString;
		}
		RequestBuilder requestBuilder = null;

		// 设置访问的Header
		if("GET".equals(methodType)) {
			//System.out.println("get method");
		   requestBuilder = RequestBuilder.get().setUri(url);
		   HttpRequestHeaderGenerator.setGetMarketInfoeaders(requestBuilder, url);
		   
		}else {//POST method
			//System.out.println("post method");
			requestBuilder = RequestBuilder.post().setUri(url);
			HttpRequestHeaderGenerator.setPostMarketInfoHeaders(requestBuilder, url);
		}
		//携带表单数据
		if(null!=entityMap && 0!=entityMap.size()) {
			try {
				requestBuilder.setEntity(new UrlEncodedFormEntity(
						HttpClientRequestHandler.generateListNameValuePairs(entityMap)));
			} catch (UnsupportedEncodingException e) {
				
				e.printStackTrace();
			}
		}
		
		// 生成访问方法
		HttpUriRequest requestMethod = requestBuilder.build();
		
		//携带Headers
		if(null!=headerMap&&0!=headerMap.size()) {
		    Header[] header = HttpClientRequestHandler.translateMapToHeaderArray(headerMap);
		    requestMethod.setHeaders(header);
		   
		}
		// 保存访问方法
		requestHandler.setRequestMethod(requestMethod);
		// 发送请求
		ResponseRet responseRet = requestHandler.GetHttpResponse_parseMarketInfo(requestHandler, fileDirPath,saveFileName);
		if (null == responseRet || null == responseRet.getRetContent()) {
			return responseString;
		}
		try {
			responseString = new String(responseRet.getRetContent(), "utf-8");
		} catch (UnsupportedEncodingException e) {
			// log.error("parse responseString error");
			e.printStackTrace();
		}
		return responseString;
	}


}
