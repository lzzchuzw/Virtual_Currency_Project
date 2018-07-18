package com.market.service.imp;

import com.market.dao.IPlatformCoinRelevanceDao;
import com.market.dao.imp.PlatformCoinRelevanceDao;
import com.market.pojo.PlatformCoinRelevance;
import com.market.service.IPlatformCoinRelevanceService;
import com.utils.service.BaseService;

public class PlatformCoinRelevanceService extends BaseService<PlatformCoinRelevance> implements IPlatformCoinRelevanceService{
    private IPlatformCoinRelevanceDao platformCoinRelevanceDao;
    
    public PlatformCoinRelevanceService() {
    	this.baseDao = this.platformCoinRelevanceDao = new PlatformCoinRelevanceDao();
    }

	public IPlatformCoinRelevanceDao getPlatformCoinRelevanceDao() {
		return platformCoinRelevanceDao;
	}

	public void setPlatformCoinRelevanceDao(IPlatformCoinRelevanceDao platformCoinRelevanceDao) {
		this.baseDao = this.platformCoinRelevanceDao = platformCoinRelevanceDao;
	}
}
