package com.tenfen.www.service.operation.open;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springside.modules.orm.Page;

import com.tenfen.cache.CacheFactory;
import com.tenfen.cache.services.ICacheClient;
import com.tenfen.entity.operation.open.TOpenApp;
import com.tenfen.entity.operation.open.TOpenSeller;
import com.tenfen.entity.operation.open.TOpenSellerApps;
import com.tenfen.util.LogUtil;
import com.tenfen.util.Utils;
import com.tenfen.www.dao.operation.open.OpenSellerAppDao;
import com.tenfen.www.dao.operation.open.OpenSellerDao;

@Component
@Transactional
public class OpenSellerManager {
	
	@Autowired
	private OpenSellerDao openSellerDao;
	@Autowired
	private OpenSellerAppDao openSellerAppDao;
	@Autowired
	private CacheFactory cacheFactory;
	
	public TOpenSeller getOpenSellerByProperty(String propertyName, Object value) {
		TOpenSeller tOpenSeller = null;
		try {
			List<TOpenSeller> tOpenSellers = openSellerDao.findBy(propertyName, value);
			if (tOpenSellers.size() > 0) {
				tOpenSeller = tOpenSellers.get(0);
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return tOpenSeller;
	}
	
	public List<TOpenSeller> findAllOpenSellerList(Integer operatorType) {
//		return openSellerDao.getAll();
		return openSellerDao.getAllSellersByOperatorType(operatorType);
	}
	
	public Page<TOpenSeller> findSellerPage(final Page<TOpenSeller> page, Integer userType) {
//		Page<TOpenSeller> sellerPage = openSellerDao.getAll(page);
		Page<TOpenSeller> sellerPage = openSellerDao.getSellersByOperatorType(page, userType);
		return sellerPage;
	}
	
	public Page<TOpenSeller> findSellerPage(String name, final Page<TOpenSeller> page, Integer userType) {
		Page<TOpenSeller> sellerPage = openSellerDao.findSellerByProperties(name, page, userType);
		return sellerPage;
	}
	
	/**
	 * 保存信息
	 * @param entity
	 */
	public void save(TOpenSeller entity) {
		openSellerDao.save(entity);
		
		//删除缓存
		String key = "OpenSellerEntity_"+entity.getId();
		ICacheClient mc = cacheFactory.getCommonCacheClient();
		LogUtil.log("删除渠道缓存："+mc.deleteCache(key));
	}
	
	/**
	 * 获取渠道信息
	 * @param id
	 * @return
	 */
	public TOpenSeller get(Integer id) {
		String key = "OpenSellerEntity_"+id;
		ICacheClient mc = cacheFactory.getCommonCacheClient();
		TOpenSeller tOpenSeller = (TOpenSeller)mc.getCache(key);
		if (Utils.isEmpty(tOpenSeller)) {
			tOpenSeller = openSellerDao.get(id);
			TOpenSeller newOpenSeller = new TOpenSeller();
			newOpenSeller.setId(tOpenSeller.getId());
			newOpenSeller.setName(tOpenSeller.getName());
			newOpenSeller.setEmail(tOpenSeller.getEmail());
			newOpenSeller.setContact(tOpenSeller.getContact());
			newOpenSeller.setTelephone(tOpenSeller.getTelephone());
			newOpenSeller.setSellerKey(tOpenSeller.getSellerKey());
			newOpenSeller.setSellerSecret(tOpenSeller.getSellerSecret());
			newOpenSeller.setCallbackUrl(tOpenSeller.getCallbackUrl());
			newOpenSeller.setCompanyShow(tOpenSeller.getCompanyShow());
			newOpenSeller.setStatus(tOpenSeller.getStatus());
			
			List<TOpenSellerApps> newList = new ArrayList<TOpenSellerApps>();
			List<TOpenSellerApps> list = tOpenSeller.getSellerApps();
			for (TOpenSellerApps tOpenSellerApps : list) {
				TOpenApp tOpenApp = tOpenSellerApps.getOpenApp();
				TOpenApp newTOpenApp = new TOpenApp();
				newTOpenApp.setId(tOpenApp.getId());
				newTOpenApp.setName(tOpenApp.getName());
				newTOpenApp.setMerchantId(tOpenApp.getMerchantId());
				newTOpenApp.setAppKey(tOpenApp.getAppKey());
				newTOpenApp.setAppSecret(tOpenApp.getAppSecret());
				newTOpenApp.setCallbackUrl(tOpenApp.getCallbackUrl());
				
				TOpenSellerApps newTOpenSellerApps = new TOpenSellerApps();
				newTOpenSellerApps.setOpenApp(newTOpenApp);
				newList.add(newTOpenSellerApps);
			}
			newOpenSeller.setSellerApps(newList);
			tOpenSeller = newOpenSeller;
			mc.setCache(key, tOpenSeller, CacheFactory.HOUR * 6);
		}
		return tOpenSeller;
	}
	
	public TOpenSeller getEntity(Integer id) {
		return openSellerDao.get(id);
	}
	
	/**
	 * 删除信息
	 * @param id
	 */
	public void delete(Integer id) {
		openSellerDao.delete(id);
	}
	
	public void delete(TOpenSeller entity) {
		openSellerDao.delete(entity);
	}
	
	public int deleteByProperty(TOpenSeller entity) {
		return openSellerAppDao.deleteByProperty("openSeller", entity);
	}
	
//	public void deleteOpenSellerApps(Integer sellerId, Integer appId) {
//		openSellerAppDao.deleteOpenSellerApps(sellerId, appId);
//	}
	
	public void resetAppToday() {
		openSellerAppDao.resetAppToday();
	}
	
	public void saveOpenSellerApps(Integer sellerId, Integer appId, Integer fee){
		openSellerAppDao.saveEntity(sellerId, appId, fee);
	}
	
	/**
	 * 根据appId查找所属seller
	 * @param pid
	 * @return
	 */
	public List<TOpenSeller> findSellerByAppId(Integer appId) {
		List<TOpenSeller> list = null;
		try {
			List<Integer> sellerIds = openSellerAppDao.findSellerByAppId(appId);
			if (sellerIds.size() > 0) {
				list = new ArrayList<TOpenSeller>();
			}
			for (Integer sellerId : sellerIds) {
				TOpenSeller tOpenSeller = get(sellerId);
				list.add(tOpenSeller);
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return list;
	}
	
}
