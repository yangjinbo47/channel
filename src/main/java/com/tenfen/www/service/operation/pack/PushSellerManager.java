package com.tenfen.www.service.operation.pack;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springside.modules.orm.Page;

import com.alibaba.fastjson.JSON;
import com.tenfen.cache.CacheFactory;
import com.tenfen.cache.services.ICacheClient;
import com.tenfen.entity.operation.pack.TPushSeller;
import com.tenfen.util.LogUtil;
import com.tenfen.util.Utils;
import com.tenfen.www.dao.operation.pack.PushSellerDao;
import com.tenfen.www.dao.operation.pack.PushSellerPackageDao;

@Component
@Transactional
public class PushSellerManager {
	
	@Autowired
	private PushSellerDao pushSellerDao;
	@Autowired
	private PushSellerPackageDao pushSellerPackageDao;
	@Autowired
	private CacheFactory cacheFactory;
	
	public TPushSeller getPushSellerByProperty(String propertyName, Object value) {
		TPushSeller tPushSeller = null;
		try {
			List<TPushSeller> tPushSellers = pushSellerDao.findBy(propertyName, value);
			if (tPushSellers.size() > 0) {
				tPushSeller = tPushSellers.get(0);
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return tPushSeller;
	}
	
	public List<TPushSeller> findAllPushSellerList(Integer operatorType) {
		return pushSellerDao.getAllSellersByOperatorType(operatorType);
	}
	
	public Page<TPushSeller> findSellerPage(final Page<TPushSeller> page, Integer userType) {
//		Page<TOpenSeller> sellerPage = openSellerDao.getAll(page);
		Page<TPushSeller> sellerPage = pushSellerDao.getSellersByOperatorType(page, userType);
		return sellerPage;
	}
	
	public Page<TPushSeller> findSellerPage(String name, final Page<TPushSeller> page, Integer userType) {
		Page<TPushSeller> sellerPage = pushSellerDao.findSellerByProperties(name, page, userType);
		return sellerPage;
	}
	
	/**
	 * 保存信息
	 * @param entity
	 */
	public void save(TPushSeller entity) {
		pushSellerDao.save(entity);
		
		//删除缓存
		String key = "PushSellerEntity_"+entity.getId();
		ICacheClient mc = cacheFactory.getCommonCacheClient();
		LogUtil.log("删除渠道缓存："+mc.deleteCache(key));
	}
	
	/**
	 * 获取渠道信息
	 * @param id
	 * @return
	 */
	public TPushSeller get(Integer id) {
		String key = "PushSellerEntity_"+id;
		ICacheClient mc = cacheFactory.getCommonCacheClient();
		TPushSeller tPushSeller = (TPushSeller)mc.getCache(key);
		if (Utils.isEmpty(tPushSeller)) {
			tPushSeller = pushSellerDao.get(id);
			String objStr = JSON.toJSONString(tPushSeller);
			TPushSeller newPushSeller = JSON.parseObject(objStr, TPushSeller.class);
			mc.setCache(key, newPushSeller, CacheFactory.HOUR * 6);
		}
		return tPushSeller;
	}
	
	public TPushSeller getEntity(Integer id) {
		return pushSellerDao.get(id);
	}
	
	/**
	 * 删除信息
	 * @param id
	 */
	public void delete(Integer id) {
		pushSellerDao.delete(id);
	}
	
	public void delete(TPushSeller entity) {
		pushSellerDao.delete(entity);
	}
	
	public int deleteByProperty(TPushSeller entity) {
		return pushSellerPackageDao.deleteByProperty("pushSeller", entity);
	}
	
	public void resetPackageToday() {
		pushSellerPackageDao.resetPackageToday();
	}
	
	public void addPushSellerPackages(Integer sellerId, Integer packageId){
		pushSellerPackageDao.saveEntity(sellerId, packageId);
	}
	
	/**
	 * 根据appId查找所属seller
	 * @param pid
	 * @return
	 */
	public List<TPushSeller> findSellerByPackageId(Integer packageId) {
		List<TPushSeller> list = null;
		try {
			List<Integer> sellerIds = pushSellerPackageDao.findSellerByPackageId(packageId);
			if (sellerIds.size() > 0) {
				list = new ArrayList<TPushSeller>();
			}
			for (Integer sellerId : sellerIds) {
				TPushSeller tPushSeller = get(sellerId);
				list.add(tPushSeller);
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return list;
	}
	
}
