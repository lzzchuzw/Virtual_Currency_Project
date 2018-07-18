package com.okex.future.imp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.apache.http.HttpException;

import com.market.handler.GeneralHttpClientRequestManager;
import com.okex.future.IFutureRestApi;
import com.utils.encryption.MD5Util;
import com.utils.proxy.ProxyUtils;
import com.utils.request.HttpClientRequestHandler;
import com.utils.string.StringUtils;

public class FutureRestApi implements IFutureRestApi {

	private String api_key;

	private String secret_key;

	private final String url_prex = "https://www.okex.com";

	private HttpClientRequestHandler requestHandler;

	private final String fileDirPath = "F:/testFolder/virtual_coin/okex/future/";

	/**
	 * 期货行情URL
	 */
	private final String FUTURE_TICKER_URL = "/api/v1/future_ticker.do";
	/**
	 * 期货指数查询URL
	 */
	private final String FUTURE_INDEX_URL = "/api/v1/future_index.do";
	/**
	 * 期货K线查询URL
	 */
	private final String FUTURE_KLINE_URL = "/api/v1/future_kline";

	/**
	 * 期货交易记录查询URL
	 */
	private final String FUTURE_TRADES_URL = "/api/v1/future_trades.do";

	/**
	 * 期货市场深度查询URL
	 */
	private final String FUTURE_DEPTH_URL = "/api/v1/future_depth.do";
	/**
	 * 美元-人民币汇率查询URL
	 */
	private final String FUTURE_EXCHANGE_RATE_URL = "/api/v1/exchange_rate.do";

	/**
	 * 期货取消订单URL
	 */
	private final String FUTURE_CANCEL_URL = "/api/v1/future_cancel.do";

	/**
	 * 期货下单URL
	 */
	private final String FUTURE_TRADE_URL = "/api/v1/future_trade.do";

	/**
	 * 期货账户信息URL
	 */
	private final String FUTURE_USERINFO_URL = "/api/v1/future_userinfo.do";

	/**
	 * 逐仓期货账户信息URL
	 */
	private final String FUTURE_USERINFO_4FIX_URL = "/api/v1/future_userinfo_4fix.do";

	/**
	 * 期货持仓查询URL
	 */
	private final String FUTURE_POSITION_URL = "/api/v1/future_position.do";

	/**
	 * 期货逐仓持仓查询URL
	 */
	private final String FUTURE_POSITION_4FIX_URL = "/api/v1/future_position_4fix.do";

	/**
	 * 用户期货订单信息查询URL
	 */
	private final String FUTURE_ORDER_INFO_URL = "/api/v1/future_order_info.do";

	/**************************************************************************************/

	public FutureRestApi(String api_key, String secret_key, HttpClientRequestHandler requestHandler) {
		this.api_key = api_key;
		this.secret_key = secret_key;

		this.requestHandler = requestHandler;
	}

	public FutureRestApi(HttpClientRequestHandler requestHandler) {
		this.requestHandler = requestHandler;
		this.api_key = "4c732d57-4b7d-41cd-b561-831d6622fcf7";
		this.secret_key = "219A1D5C7DCF14C75C68F41CE0581C38";

	}

	/**************************************************************************************/
	@Override
	public String future_ticker(String symbol, String contractType) {
		StringBuilder sb = new StringBuilder(url_prex).append(FUTURE_TICKER_URL).append("?").append("symbol=")
				.append(symbol).append("&contract_type=").append(contractType);
		String url = new String(sb);
		System.out.println("future_ticker,url = " + url);

		String responseString = GeneralHttpClientRequestManager.httpClientRequest(requestHandler, "GET", url, null,
				null, fileDirPath, "future_ticker");

		System.out.println("future_ticker,responseString = " + responseString);
		return responseString;

	}

	@Override
	public String future_index(String symbol) {
		StringBuilder sb = new StringBuilder(url_prex).append(FUTURE_INDEX_URL).append("?").append("symbol=")
				.append(symbol);

		String url = new String(sb);
		System.out.println("future_index,url = " + url);

		String responseString = GeneralHttpClientRequestManager.httpClientRequest(requestHandler, "GET", url, null,
				null, fileDirPath, "future_index");

		System.out.println("future_index,responseString = " + responseString);
		return responseString;
	}
	@Override
	public String future_kline(String symbol,String type,String contractType,Integer size, Long since) {
		
		//非必填参数规整化
		if(null==size ||0>size) {
			size = 0;
		}
        
		if(null==since || 0>since) {
			since = 0L;
		}
		//构造url
		StringBuilder sb = new StringBuilder(url_prex)
				               .append(FUTURE_KLINE_URL)
				               .append("?")
				               .append("symbol=")
				               .append(symbol)
				               .append("&type=")
				               .append(type)
				               .append("&contract_type=")
				               .append(contractType)
				               .append("&size=")
				               .append(size)
				               .append("&since=")
				               .append(since);
		                       
		String url = new String(sb);
		System.out.println("future_kline,url = " + url);

		String responseString = GeneralHttpClientRequestManager.httpClientRequest(requestHandler, "GET", url, null,
				null, fileDirPath, "future_kline_"+symbol);

		System.out.println("future_kline,responseString = " + responseString);
		return responseString;
	}
	@Override
	public String future_trades(String symbol, String contractType) {
		StringBuilder sb = new StringBuilder(url_prex).append(FUTURE_TRADES_URL).append("?").append("symbol=")
				.append(symbol).append("&contract_type=").append(contractType);
		String url = new String(sb);
		System.out.println("future_trades,url = " + url);

		String responseString = GeneralHttpClientRequestManager.httpClientRequest(requestHandler, "GET", url, null,
				null, fileDirPath, "future_trades");

		System.out.println("future_trades,responseString = " + responseString);
		return responseString;
	}

	@Override
	public String future_depth(String symbol, String contractType) {
		StringBuilder sb = new StringBuilder(url_prex).append(FUTURE_DEPTH_URL).append("?").append("symbol=")
				.append(symbol).append("&contract_type=").append(contractType);
		String url = new String(sb);
		System.out.println("future_depth,url = " + url);

		String responseString = GeneralHttpClientRequestManager.httpClientRequest(requestHandler, "GET", url, null,
				null, fileDirPath, "future_depth");

		System.out.println("future_depth,responseString = " + responseString);
		return responseString;
	}

	@Override
	public String exchange_rate() {
		StringBuilder sb = new StringBuilder(url_prex).append(FUTURE_EXCHANGE_RATE_URL);

		String url = new String(sb);
		String responseString = GeneralHttpClientRequestManager.httpClientRequest(requestHandler, "GET", url, null,
				null, fileDirPath, "exchange_rate");

		System.out.println("exchange_rate,responseString = " + responseString);
		return responseString;
	}

	@Override
	public String future_cancel(String symbol, String contractType, String orderId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String future_trade(String symbol, String contractType, String price, String amount, String type,
			String matchPrice) {
		StringBuilder sb = new StringBuilder(url_prex).append(FUTURE_TRADE_URL);

		String url = new String(sb);
		System.out.println("future_trade,url = " + url);
		// 构造参数签名
		Map<String, String> params = new HashMap<String, String>();
		if (!StringUtils.isEmpty(symbol )) {
			params.put("symbol", symbol);
		}
		if (!StringUtils.isEmpty(contractType )) {
			params.put("contract_type", contractType);
		}
		if (!StringUtils.isEmpty(api_key )) {
			params.put("api_key", api_key);
		}
		if (!StringUtils.isEmpty(price )) {
			params.put("price", price);
		}
		if (!StringUtils.isEmpty(amount )) {
			params.put("amount", amount);
		}
		if (!StringUtils.isEmpty(type )) {
			params.put("type", type);
		}
		if (!StringUtils.isEmpty(matchPrice )) {
			params.put("match_price", matchPrice);
		}
		String sign = MD5Util.buildMysignV1(params, secret_key);
		params.put("sign", sign);
		String responseString = GeneralHttpClientRequestManager.httpClientRequest(requestHandler, "POST", url, null,
				params, fileDirPath, "future_trade");

		System.out.println("future_trade,responseString = " + responseString);
		return responseString;
	}

	@Override
	public String future_userinfo() {

		StringBuilder sb = new StringBuilder(url_prex).append(FUTURE_USERINFO_URL);

		String url = new String(sb);
		System.out.println("future_userinfo,url = " + url);

		Map<String, String> params = new HashMap<String, String>();
		params.put("api_key", api_key);
		String sign = MD5Util.buildMysignV1(params, secret_key);
		params.put("sign", sign);

		String responseString = GeneralHttpClientRequestManager.httpClientRequest(requestHandler, "POST", url, null,
				params, fileDirPath, "future_userinfo");

		System.out.println("future_userinfo,responseString = " + responseString);
		return responseString;

	}

	@Override
	public String future_userinfo_4fix() {
		StringBuilder sb = new StringBuilder(url_prex).append(FUTURE_USERINFO_4FIX_URL);

		String url = new String(sb);
		System.out.println("future_userinfo,url = " + url);

		Map<String, String> params = new HashMap<String, String>();
		params.put("api_key", api_key);
		String sign = MD5Util.buildMysignV1(params, secret_key);
		params.put("sign", sign);

		String responseString = GeneralHttpClientRequestManager.httpClientRequest(requestHandler, "POST", url, null,
				params, fileDirPath, "future_userinfo");

		System.out.println("future_userinfo,responseString = " + responseString);
		return responseString;
	}

	@Override
	public String future_position(String symbol, String contractType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String future_position_4fix(String symbol, String contractType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String future_order_info(String symbol, String contractType, String orderId, String status,
			String currentPage, String pageLength) {
		StringBuilder sb = new StringBuilder(url_prex).append(FUTURE_ORDER_INFO_URL);

		String url = new String(sb);
		System.out.println("future_order_info,url = " + url);
		// 构造参数签名
		Map<String, String> params = new HashMap<String, String>();
		if (!StringUtils.isEmpty(contractType )) {
			params.put("contract_type", contractType);
		}
		if (!StringUtils.isEmpty(currentPage )) {
			params.put("current_page", currentPage);
		}
		if (!StringUtils.isEmpty(orderId )) {
			params.put("order_id", orderId);
		}
		if (!StringUtils.isEmpty(api_key )) {
			params.put("api_key", api_key);
		}
		if (!StringUtils.isEmpty(pageLength )) {
			params.put("page_length", pageLength);
		}
		if (!StringUtils.isEmpty(symbol )) {
			params.put("symbol", symbol);
		}
		if (!StringUtils.isEmpty(status )) {
			params.put("status", status);
		}
		String sign = MD5Util.buildMysignV1(params, secret_key);
		params.put("sign", sign);
		String responseString = GeneralHttpClientRequestManager.httpClientRequest(requestHandler, "POST", url, null,
				params, fileDirPath, "future_order_info");

		System.out.println("future_order_info,responseString = " + responseString);
		return responseString;
	}

	/**************************************************************************************/
	public String getApi_key() {
		return api_key;
	}

	public void setApi_key(String api_key) {
		this.api_key = api_key;
	}

	public String getSecret_key() {
		return secret_key;
	}

	public void setSecret_key(String secret_key) {
		this.secret_key = secret_key;
	}

	public HttpClientRequestHandler getRequestHandler() {
		return requestHandler;
	}

	public void setRequestHandler(HttpClientRequestHandler requestHandler) {
		this.requestHandler = requestHandler;
	}
}
