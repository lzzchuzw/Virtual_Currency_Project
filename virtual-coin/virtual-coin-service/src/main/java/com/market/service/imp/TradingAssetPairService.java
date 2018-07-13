package com.market.service.imp;

import com.market.dao.ITradingAssetPairDao;
import com.market.dao.imp.TradingAssetPairDao;
import com.market.pojo.TradingAssetPair;
import com.market.service.ITradingAssetPairService;
import com.utils.service.BaseService;

public class TradingAssetPairService extends BaseService<TradingAssetPair> implements ITradingAssetPairService{
	private ITradingAssetPairDao tradingAssetPairDao;
	
	public TradingAssetPairService() {
		this.baseDao = this.tradingAssetPairDao = new TradingAssetPairDao();
	}

	public ITradingAssetPairDao getTradingAssetPairDao() {
		return tradingAssetPairDao;
	}

	public void setTradingAssetPairDao(ITradingAssetPairDao tradingAssetPairDao) {
		this.baseDao = this.tradingAssetPairDao = tradingAssetPairDao;
	}

}
