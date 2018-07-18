package com.market.service.imp;

import com.market.dao.IPlatformSymbolRelevanceDao;
import com.market.dao.imp.PlatformSymbolRelevanceDao;
import com.market.pojo.PlatformSymbolRelevance;
import com.market.service.IPlatformSymbolRelevanceService;
import com.utils.service.BaseService;

public class PlatformSymbolRelevanceService extends BaseService<PlatformSymbolRelevance> implements IPlatformSymbolRelevanceService{
     private IPlatformSymbolRelevanceDao platformSymbolRelevanceDao ;
     
     public PlatformSymbolRelevanceService() {
    	 this.baseDao = this.platformSymbolRelevanceDao = new PlatformSymbolRelevanceDao();
     }
	public IPlatformSymbolRelevanceDao getPlatformSymbolRelevanceDao() {
		return platformSymbolRelevanceDao;
	}

	public void setPlatformSymbolRelevanceDao(IPlatformSymbolRelevanceDao platformSymbolRelevanceDao) {
		this.baseDao = this.platformSymbolRelevanceDao = platformSymbolRelevanceDao;
	}
}
