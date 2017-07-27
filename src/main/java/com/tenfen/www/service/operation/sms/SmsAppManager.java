package com.tenfen.www.service.operation.sms;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springside.modules.orm.Page;

import com.alibaba.fastjson.JSON;
import com.tenfen.cache.CacheFactory;
import com.tenfen.cache.services.ICacheClient;
import com.tenfen.entity.operation.sms.TSmsApp;
import com.tenfen.entity.operation.sms.TSmsAppLimit;
import com.tenfen.entity.operation.sms.TSmsProductInfo;
import com.tenfen.util.LogUtil;
import com.tenfen.util.Utils;
import com.tenfen.www.dao.operation.sms.SmsAppDao;
import com.tenfen.www.dao.operation.sms.SmsAppLimitDao;

@Component
@Transactional
public class SmsAppManager {
	
	@Autowired
	private SmsAppDao smsAppDao;
	@Autowired
	private CacheFactory cacheFactory;
	@Autowired
	private SmsAppLimitDao smsAppLimitDao;
	
	public TSmsApp getSmsAppByProperty(String propertyName, Object value) {
		TSmsApp tSmsApp = null;
		try {
			List<TSmsApp> tSmsApps = smsAppDao.findBy(propertyName, value);
			if (tSmsApps.size() > 0) {
				tSmsApp = tSmsApps.get(0);
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return tSmsApp;
	}
	
	public Page<TSmsApp> findAppPage(final Page<TSmsApp> page, Integer userType) {
//		Page<TSmsApp> appPage = smsAppDao.getAll(page);
		Page<TSmsApp> appPage = smsAppDao.getAppsByOperatorType(page, userType);
		return appPage;
	}
	
	public Page<TSmsApp> findAppPage(String appName, final Page<TSmsApp> page, Integer userType) {
		Page<TSmsApp> appPage = smsAppDao.findAppByProperties(appName, page, userType);
		return appPage;
	}
	
	public List<TSmsApp> getAll() {
		return smsAppDao.getAll();
	}
	
	/**
	 * 获取信息
	 * @param id
	 * @return
	 */
	public TSmsApp get(Integer id) {
		String key = "SmsAppEntity_"+id;
		ICacheClient mc = cacheFactory.getCommonCacheClient();
		TSmsApp tSmsApp = (TSmsApp)mc.getCache(key);
		if (Utils.isEmpty(tSmsApp)) {
			tSmsApp = smsAppDao.get(id);
			TSmsApp newSmsApp = new TSmsApp();
			newSmsApp.setId(tSmsApp.getId());
			newSmsApp.setName(tSmsApp.getName());
			newSmsApp.setMerchantId(tSmsApp.getMerchantId());
			newSmsApp.setAppKey(tSmsApp.getAppKey());
			newSmsApp.setAppSecret(tSmsApp.getAppSecret());
//			newSmsApp.setExcludeArea(tSmsApp.getExcludeArea());
			newSmsApp.setTips(tSmsApp.getTips());
			
			List<TSmsProductInfo> proList = tSmsApp.getProductList();
			String array = JSON.toJSONString(proList, true);
			List<TSmsProductInfo> newProList = JSON.parseArray(array, TSmsProductInfo.class);
			newSmsApp.setProductList(newProList);
			
			tSmsApp = newSmsApp;
			mc.setCache(key, tSmsApp, CacheFactory.HOUR * 6);
		}
		return tSmsApp;
	}
	
	/**
	 * 保存信息
	 * @param entity
	 */
	public void save(TSmsApp entity) {
		smsAppDao.save(entity);
		
		//删除缓存
		String key = "SmsAppEntity_"+entity.getId();
		ICacheClient mc = cacheFactory.getCommonCacheClient();
		LogUtil.log("删除app缓存："+mc.deleteCache(key));
	}
	
	/**
	 * 获取信息
	 * @param id
	 * @return
	 */
	public TSmsApp getEntity(Integer id) {
		return smsAppDao.get(id);
	}
	
	/**
	 * 删除信息
	 * @param id
	 */
	public void delete(Integer id) {
		smsAppDao.delete(id);
	}
	
	/**
	 * 根据pid查找所属app
	 * @param pid
	 * @return
	 */
	public List<TSmsApp> findAppByProductId(Integer pid) {
		List<TSmsApp> list = null;
		try {
			List<Integer> appIds = smsAppDao.findAppByProductId(pid);
			for (Integer appId : appIds) {
				list = new ArrayList<TSmsApp>();
				TSmsApp tSmsApp = get(appId);
				list.add(tSmsApp);
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return list;
	}
	
	/**
	 * 根据sellerId查询渠道限量规则
	 * @param sellerId
	 * @return
	 */
	public List<TSmsAppLimit> findAppLimits(Integer appId) {
		return smsAppLimitDao.findSmsAppByProperty(appId);
	}
	
	public TSmsAppLimit findAppLimitByProperty(Integer appId, String province) {
		TSmsAppLimit tSmsAppLimit = smsAppLimitDao.findSmsAppByProperty(appId, province);
		if (Utils.isEmpty(tSmsAppLimit)) {
			tSmsAppLimit = new TSmsAppLimit();
			tSmsAppLimit.setAppId(appId);
			tSmsAppLimit.setProvince(province);
			tSmsAppLimit.setDayLimit(-1);
			tSmsAppLimit.setMonthLimit(-1);
			tSmsAppLimit.setReduce(0);//默认不扣量
		}
		return tSmsAppLimit;
	}
	
	public void saveAppLimit(TSmsAppLimit entity) {
		smsAppLimitDao.save(entity);
	}
}
