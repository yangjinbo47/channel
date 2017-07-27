package com.tenfen.www.service.operation.open;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springside.modules.orm.Page;

import com.tenfen.cache.CacheFactory;
import com.tenfen.cache.services.ICacheClient;
import com.tenfen.entity.operation.open.TOpenApp;
import com.tenfen.entity.operation.open.TOpenAppLimit;
import com.tenfen.util.LogUtil;
import com.tenfen.util.Utils;
import com.tenfen.www.dao.operation.open.OpenAppDao;
import com.tenfen.www.dao.operation.open.OpenAppLimitDao;

@Component
@Transactional
public class OpenAppManager {
	
	@Autowired
	private OpenAppDao openAppDao;
	@Autowired
	private CacheFactory cacheFactory;
	@Autowired
	private OpenAppLimitDao openAppLimitDao;
	
	public TOpenApp getOpenAppByProperty(String propertyName, Object value) {
		TOpenApp tOpenApp = null;
		try {
			List<TOpenApp> tOpenApps = openAppDao.findBy(propertyName, value);
			if (tOpenApps.size() > 0) {
				tOpenApp = tOpenApps.get(0);
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return tOpenApp;
	}
	
	public Page<TOpenApp> findAppPage(final Page<TOpenApp> page, Integer userType) {
//		Page<TOpenApp> appPage = openAppDao.getAll(page);
		Page<TOpenApp> appPage = openAppDao.getAppsByOperatorType(page, userType);
		return appPage;
	}
	
	public Page<TOpenApp> findAppPage(String appName, final Page<TOpenApp> page, Integer userType) {
		Page<TOpenApp> appPage = openAppDao.findAppByProperties(appName, page, userType);
		return appPage;
	}
	
	public List<TOpenApp> getAll() {
		return openAppDao.getAll();
	}
	
	/**
	 * 保存信息
	 * @param entity
	 */
	public void save(TOpenApp entity) {
		openAppDao.save(entity);
		
		//删除缓存
		String key = "OpenAppEntity_"+entity.getId();
		ICacheClient mc = cacheFactory.getCommonCacheClient();
		LogUtil.log("删除app缓存："+mc.deleteCache(key));
	}
	
	/**
	 * 获取渠道信息
	 * @param id
	 * @return
	 */
	public TOpenApp get(Integer id) {
		String key = "OpenAppEntity_"+id;
		ICacheClient mc = cacheFactory.getCommonCacheClient();
		TOpenApp tOpenApp = (TOpenApp)mc.getCache(key);
		if (Utils.isEmpty(tOpenApp)) {
			tOpenApp = openAppDao.get(id);
			TOpenApp newOpenApp = new TOpenApp();
			newOpenApp.setId(tOpenApp.getId());
			newOpenApp.setName(tOpenApp.getName());
			newOpenApp.setMerchantId(tOpenApp.getMerchantId());
			newOpenApp.setAppKey(tOpenApp.getAppKey());
			newOpenApp.setAppSecret(tOpenApp.getAppSecret());
			newOpenApp.setCallbackUrl(tOpenApp.getCallbackUrl());
//			newOpenApp.setExcludeArea(tOpenApp.getExcludeArea());
			newOpenApp.setClientId(tOpenApp.getClientId());
			
			tOpenApp = newOpenApp;
			mc.setCache(key, tOpenApp, CacheFactory.HOUR * 6);
		}
		return tOpenApp;
	}
	
	/**
	 * 获取信息
	 * @param id
	 * @return
	 */
	public TOpenApp getEntity(Integer id) {
		return openAppDao.get(id);
	}
	
	/**
	 * 删除信息
	 * @param id
	 */
	public void delete(Integer id) {
		openAppDao.delete(id);
	}
	
	/**
	 * 根据sellerId查询渠道限量规则
	 * @param sellerId
	 * @return
	 */
	public List<TOpenAppLimit> findAppLimits(Integer appId) {
		return openAppLimitDao.findOpenAppByProperty(appId);
	}
	
	public TOpenAppLimit findAppLimitByProperty(Integer appId, String province) {
		TOpenAppLimit tOpenAppLimit = openAppLimitDao.findOpenAppByProperty(appId, province);
		if (Utils.isEmpty(tOpenAppLimit)) {
			tOpenAppLimit = new TOpenAppLimit();
			tOpenAppLimit.setAppId(appId);
			tOpenAppLimit.setProvince(province);
			tOpenAppLimit.setDayLimit(-1);
			tOpenAppLimit.setMonthLimit(-1);
			tOpenAppLimit.setReduce(0);//默认不扣量
		}
		return tOpenAppLimit;
	}
	
	public void saveAppLimit(TOpenAppLimit entity) {
		openAppLimitDao.save(entity);
	}
	
}
