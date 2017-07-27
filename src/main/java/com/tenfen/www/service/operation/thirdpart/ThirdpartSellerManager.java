package com.tenfen.www.service.operation.thirdpart;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springside.modules.orm.Page;

import com.alibaba.fastjson.JSON;
import com.tenfen.cache.CacheFactory;
import com.tenfen.cache.services.ICacheClient;
import com.tenfen.entity.operation.thirdpart.TThirdApp;
import com.tenfen.entity.operation.thirdpart.TThirdSeller;
import com.tenfen.entity.operation.thirdpart.TThirdSellerApps;
import com.tenfen.util.LogUtil;
import com.tenfen.util.Utils;
import com.tenfen.www.dao.operation.thirdpart.ThirdpartSellerAppDao;
import com.tenfen.www.dao.operation.thirdpart.ThirdpartSellerDao;

@Component
@Transactional
public class ThirdpartSellerManager {
	
	@Autowired
	private ThirdpartSellerDao thirdpartSellerDao;
	@Autowired
	private ThirdpartSellerAppDao thirdpartSellerAppDao;
	@Autowired
	private CacheFactory cacheFactory;
	
	public TThirdSeller getThirdSellerByProperty(String propertyName, Object value) {
		TThirdSeller tThirdSeller = null;
		try {
			List<TThirdSeller> tThirdSellers = thirdpartSellerDao.findBy(propertyName, value);
			if (tThirdSellers.size() > 0) {
				tThirdSeller = tThirdSellers.get(0);
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return tThirdSeller;
	}
	
	public List<TThirdSeller> findAllThirdSellerList(Integer operatorType) {
		return thirdpartSellerDao.getAllSellersByOperatorType(operatorType);
	}
	
	public Page<TThirdSeller> findSellerPage(final Page<TThirdSeller> page, Integer userType) {
		Page<TThirdSeller> sellerPage = thirdpartSellerDao.getSellersByOperatorType(page, userType);
		return sellerPage;
	}
	
	public Page<TThirdSeller> findSellerPage(String name, final Page<TThirdSeller> page, Integer userType) {
		Page<TThirdSeller> sellerPage = thirdpartSellerDao.findSellerByProperties(name, page, userType);
		return sellerPage;
	}
	
	/**
	 * 保存信息
	 * @param entity
	 */
	public void save(TThirdSeller entity) {
		thirdpartSellerDao.save(entity);
		
		//删除缓存
		String key = "ThirdpartSellerEntity_"+entity.getId();
		ICacheClient mc = cacheFactory.getCommonCacheClient();
		LogUtil.log("删除渠道缓存："+mc.deleteCache(key));
	}
	
	/**
	 * 获取信息
	 * @param id
	 * @return
	 */
	public TThirdSeller get(Integer id) {
		String key = "ThirdpartSellerEntity_"+id;
		ICacheClient mc = cacheFactory.getCommonCacheClient();
		TThirdSeller tThirdSeller = (TThirdSeller)mc.getCache(key);
		if (Utils.isEmpty(tThirdSeller)) {
			tThirdSeller = thirdpartSellerDao.get(id);
			TThirdSeller newThirdSeller = new TThirdSeller();
			newThirdSeller.setId(tThirdSeller.getId());
			newThirdSeller.setName(tThirdSeller.getName());
			newThirdSeller.setEmail(tThirdSeller.getEmail());
			newThirdSeller.setContact(tThirdSeller.getContact());
			newThirdSeller.setTelephone(tThirdSeller.getTelephone());
			newThirdSeller.setSellerKey(tThirdSeller.getSellerKey());
			newThirdSeller.setSellerSecret(tThirdSeller.getSellerSecret());
			newThirdSeller.setCallbackUrl(tThirdSeller.getCallbackUrl());
			newThirdSeller.setCompanyShow(tThirdSeller.getCompanyShow());
			newThirdSeller.setStatus(tThirdSeller.getStatus());
			
			List<TThirdSellerApps> newList = new ArrayList<TThirdSellerApps>();
			List<TThirdSellerApps> list = tThirdSeller.getSellerApps();
			for (TThirdSellerApps tThirdSellerApps : list) {
				TThirdApp tThirdApp = tThirdSellerApps.getThirdApp();
				String thirdAppJson = JSON.toJSONString(tThirdApp, true);
				TThirdApp newThirdApp = JSON.parseObject(thirdAppJson, TThirdApp.class);
				
				TThirdSellerApps newThirdSellerApps = new TThirdSellerApps();
				newThirdSellerApps.setThirdApp(newThirdApp);
				newList.add(newThirdSellerApps);
			}
			
			newThirdSeller.setSellerApps(newList);
			tThirdSeller = newThirdSeller;
			mc.setCache(key, tThirdSeller, CacheFactory.HOUR * 6);
		}
		return tThirdSeller;
	}
	
	public TThirdSeller getEntity(Integer id) {
		return thirdpartSellerDao.get(id);
	}
	
	/**
	 * 删除信息
	 * @param id
	 */
	public void delete(Integer id) {
		thirdpartSellerDao.delete(id);
	}
	
	public void delete(TThirdSeller entity) {
		thirdpartSellerDao.delete(entity);
	}
	
	public int deleteByProperty(TThirdSeller entity) {
		return thirdpartSellerAppDao.deleteByProperty("thirdSeller", entity);
	}
	
	
	public void resetAppToday() {
		thirdpartSellerAppDao.resetAppToday();
	}
	
	public void saveThirdSellerApps(Integer sellerId, Integer appId, Integer fee){
		thirdpartSellerAppDao.saveEntity(sellerId, appId, fee);
	}
	
}
