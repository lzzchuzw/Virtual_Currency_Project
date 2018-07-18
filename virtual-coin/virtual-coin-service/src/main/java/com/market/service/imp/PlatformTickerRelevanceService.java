package com.market.service.imp;

import com.market.dao.IPlatformTickerRelevanceDao;
import com.market.dao.imp.PlatformTickerRelevanceDao;
import com.market.pojo.PlatformTickerRelevance;
import com.market.service.IPlatformTickerRelevanceService;
import com.utils.service.BaseService;

public class PlatformTickerRelevanceService extends BaseService<PlatformTickerRelevance> implements IPlatformTickerRelevanceService{
      private IPlatformTickerRelevanceDao platformTickerRelevanceDao;
      
      public PlatformTickerRelevanceService() {
    	 this.baseDao = this.platformTickerRelevanceDao = new PlatformTickerRelevanceDao();
      }

	public IPlatformTickerRelevanceDao getPlatformTickerRelevanceDao() {
		return platformTickerRelevanceDao;
	}

	public void setPlatformTickerRelevanceDao(IPlatformTickerRelevanceDao platformTickerRelevanceDao) {
		this.baseDao = this.platformTickerRelevanceDao = platformTickerRelevanceDao;
	}
}
