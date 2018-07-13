package com.market.service.imp;

import com.market.dao.ITradingPlatformDao;
import com.market.dao.imp.TradingPlatformDao;
import com.market.pojo.TradingPlatform;
import com.market.service.ITradingPlatformService;
import com.utils.service.BaseService;

public class TradingPlatformService extends BaseService<TradingPlatform> implements ITradingPlatformService{
    @SuppressWarnings("unused")
	private ITradingPlatformDao tradingPlatformDao;
    
    public TradingPlatformService() {
    	this.baseDao = this.tradingPlatformDao = new TradingPlatformDao();
    }
    
	public void setTradingPlatformDao(ITradingPlatformDao tradingPlatformDao) {
		this.baseDao = this.tradingPlatformDao = tradingPlatformDao;
	}
    
}
