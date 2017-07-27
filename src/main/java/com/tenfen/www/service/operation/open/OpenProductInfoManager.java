package com.tenfen.www.service.operation.open;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springside.modules.orm.Page;

import com.tenfen.entity.operation.open.TOpenProductInfo;
import com.tenfen.util.LogUtil;
import com.tenfen.www.dao.operation.open.OpenProductInfoDao;

@Component
@Transactional
public class OpenProductInfoManager {
	
	@Autowired
	private OpenProductInfoDao openProductInfoDao;
	
	public TOpenProductInfo getOpenProductInfoByProperty(String propertyName, Object value) {
		TOpenProductInfo tOpenProductInfo = null;
		try {
			List<TOpenProductInfo> tOpenProductInfos = openProductInfoDao.findBy(propertyName, value);
			if (tOpenProductInfos.size() > 0) {
				tOpenProductInfo = tOpenProductInfos.get(0);
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return tOpenProductInfo;
	}
	
	public Page<TOpenProductInfo> findProductPage(final Page<TOpenProductInfo> page) {
		Page<TOpenProductInfo> productPage = openProductInfoDao.getAll(page);
		return productPage;
	}
	
	public Page<TOpenProductInfo> findProductPage(String name, final Page<TOpenProductInfo> page) {
		Page<TOpenProductInfo> productPage = openProductInfoDao.findProductByProperties(name, page);
		return productPage;
	}
	
	public List<TOpenProductInfo> findOpenProductInfoByMerchantId(Integer merchantId) {
		return openProductInfoDao.findBy("merchantId", merchantId);
	}
	
	/**
	 * 保存信息
	 * @param entity
	 */
	public void save(TOpenProductInfo entity) {
		openProductInfoDao.save(entity);
	}
	
	/**
	 * 获取信息
	 * @param id
	 * @return
	 */
	public TOpenProductInfo get(Integer id) {
		return openProductInfoDao.get(id);
	}
	
	/**
	 * 删除信息
	 * @param id
	 */
	public void delete(Integer id) {
		openProductInfoDao.delete(id);
	}
	
}
