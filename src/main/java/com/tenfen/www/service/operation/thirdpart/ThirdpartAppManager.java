package com.tenfen.www.service.operation.thirdpart;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springside.modules.orm.Page;

import com.tenfen.cache.CacheFactory;
import com.tenfen.cache.services.ICacheClient;
import com.tenfen.entity.operation.thirdpart.TThirdApp;
import com.tenfen.util.LogUtil;
import com.tenfen.util.Utils;
import com.tenfen.www.dao.operation.thirdpart.ThirdpartAppDao;

@Component
@Transactional
public class ThirdpartAppManager {
	
	@Autowired
	private ThirdpartAppDao thirdpartAppDao;
	@Autowired
	private CacheFactory cacheFactory;
	
	public TThirdApp getThirdAppByProperty(String propertyName, Object value) {
		TThirdApp tThirdApp = null;
		try {
			List<TThirdApp> tThirdApps = thirdpartAppDao.findBy(propertyName, value);
			if (tThirdApps.size() > 0) {
				tThirdApp = tThirdApps.get(0);
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return tThirdApp;
	}
	
	public Page<TThirdApp> findAppPage(final Page<TThirdApp> page) {
		Page<TThirdApp> appPage = thirdpartAppDao.getAll(page);
		return appPage;
	}
	
	public Page<TThirdApp> findAppPage(String appName, final Page<TThirdApp> page) {
		Page<TThirdApp> appPage = thirdpartAppDao.findAppByProperties(appName, page);
		return appPage;
	}
	
	public List<TThirdApp> getAll() {
		return thirdpartAppDao.getAll();
	}
	
	/**
	 * 获取信息
	 * @param id
	 * @return
	 */
	public TThirdApp get(Integer id) {
		String key = "ThirdpartAppEntity_"+id;
		ICacheClient mc = cacheFactory.getCommonCacheClient();
		TThirdApp tThirdApp = (TThirdApp)mc.getCache(key);
		if (Utils.isEmpty(tThirdApp)) {
			tThirdApp = thirdpartAppDao.get(id);
			TThirdApp newThirdApp = new TThirdApp();
			newThirdApp.setId(tThirdApp.getId());
			newThirdApp.setName(tThirdApp.getName());
			newThirdApp.setMerchantId(tThirdApp.getMerchantId());
			newThirdApp.setThirdAppId(tThirdApp.getThirdAppId());
			newThirdApp.setThirdAppMch(tThirdApp.getThirdAppMch());
			newThirdApp.setThirdAppSecret(tThirdApp.getThirdAppSecret());
			newThirdApp.setCallbackUrl(tThirdApp.getCallbackUrl());
			
			tThirdApp = newThirdApp;
			mc.setCache(key, tThirdApp, CacheFactory.HOUR * 6);
		}
		return tThirdApp;
	}
	
	/**
	 * 保存信息
	 * @param entity
	 */
	public void save(TThirdApp entity) {
		thirdpartAppDao.save(entity);
		
		//删除缓存
		String key = "ThirdpartAppEntity_"+entity.getId();
		ICacheClient mc = cacheFactory.getCommonCacheClient();
		LogUtil.log("删除app缓存："+mc.deleteCache(key));
	}
	
	/**
	 * 获取信息
	 * @param id
	 * @return
	 */
	public TThirdApp getEntity(Integer id) {
		return thirdpartAppDao.get(id);
	}
	
	/**
	 * 删除信息
	 * @param id
	 */
	public void delete(Integer id) {
		thirdpartAppDao.delete(id);
	}
	
}
