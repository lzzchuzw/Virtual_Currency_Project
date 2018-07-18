package com.market.service.imp;

import com.market.dao.IVirtualCoinDao;
import com.market.dao.imp.VirtualCoinDao;
import com.market.pojo.VirtualCoin;
import com.market.service.IVirtualCoinService;
import com.utils.service.BaseService;

public class VirtualCoinService extends BaseService<VirtualCoin> implements IVirtualCoinService{
	private IVirtualCoinDao virtualCoinDao;
	
	public VirtualCoinService() {
		this.baseDao = this.virtualCoinDao = new VirtualCoinDao();
	}

	public IVirtualCoinDao getVirtualCoinDao() {
		return virtualCoinDao;
	}

	public void setVirtualCoinDao(IVirtualCoinDao virtualCoinDao) {
		this.baseDao = this.virtualCoinDao = virtualCoinDao;
	}
	

}
