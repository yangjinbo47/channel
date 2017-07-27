package com.tenfen.www.service.operation.sms;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springside.modules.orm.Page;

import com.tenfen.cache.CacheFactory;
import com.tenfen.cache.services.ICacheClient;
import com.tenfen.entity.operation.sms.TSmsApp;
import com.tenfen.entity.operation.sms.TSmsSeller;
import com.tenfen.entity.operation.sms.TSmsSellerApps;
import com.tenfen.entity.operation.sms.TSmsSellerLimit;
import com.tenfen.util.LogUtil;
import com.tenfen.util.Utils;
import com.tenfen.www.dao.operation.sms.SmsSellerAppDao;
import com.tenfen.www.dao.operation.sms.SmsSellerDao;
import com.tenfen.www.dao.operation.sms.SmsSellerLimitDao;
import com.tenfen.www.service.operation.MobileAreaManager;

@Component
@Transactional
public class SmsSellerManager {
	
	@Autowired
	private SmsSellerDao smsSellerDao;
	@Autowired
	private SmsSellerAppDao smsSellerAppDao;
	@Autowired
	private SmsSellerLimitDao smsSellerLimitDao;
	@Autowired
	private CacheFactory cacheFactory;
	@Autowired
	private MobileAreaManager mobileAreaManager;
	
	public TSmsSeller getSmsSellerByProperty(String propertyName, Object value) {
		TSmsSeller tSmsSeller = null;
		try {
			List<TSmsSeller> tSmsSellers = smsSellerDao.findBy(propertyName, value);
			if (tSmsSellers.size() > 0) {
				tSmsSeller = tSmsSellers.get(0);
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return tSmsSeller;
	}
	
//	public Page<TSmsSeller> findSellerPage(final Page<TSmsSeller> page) {
//		Page<TSmsSeller> sellerPage = smsSellerDao.getAll(page);
//		return sellerPage;
//	}
//	
//	public Page<TSmsSeller> findSellerPage(String name, final Page<TSmsSeller> page) {
//		Page<TSmsSeller> sellerPage = smsSellerDao.findSellerByProperties(name, page);
//		return sellerPage;
//	}
	
	public List<TSmsSeller> findAllSmsSellerList(Integer operatorType) {
		return smsSellerDao.getAllSellersByOperatorType(operatorType);
	}
	
	public Page<TSmsSeller> findSellerPage(final Page<TSmsSeller> page, Integer userType) {
		Page<TSmsSeller> sellerPage = smsSellerDao.getSellersByOperatorType(page, userType);
		return sellerPage;
	}
	
	public Page<TSmsSeller> findSellerPage(String name, final Page<TSmsSeller> page, Integer userType) {
		Page<TSmsSeller> sellerPage = smsSellerDao.findSellerByProperties(name, page, userType);
		return sellerPage;
	}
	
	/**
	 * 保存信息
	 * @param entity
	 */
	public void save(TSmsSeller entity) {
		smsSellerDao.save(entity);
		
		//删除缓存
		String key = "SmsSellerEntity_"+entity.getId();
		ICacheClient mc = cacheFactory.getCommonCacheClient();
		LogUtil.log("删除渠道缓存："+mc.deleteCache(key));
	}
	
	/**
	 * 获取信息
	 * @param id
	 * @return
	 */
	public TSmsSeller get(Integer id) {
		String key = "SmsSellerEntity_"+id;
		ICacheClient mc = cacheFactory.getCommonCacheClient();
		TSmsSeller tSmsSeller = (TSmsSeller)mc.getCache(key);
		if (Utils.isEmpty(tSmsSeller)) {
			tSmsSeller = smsSellerDao.get(id);
			TSmsSeller newSmsSeller = new TSmsSeller();
			newSmsSeller.setId(tSmsSeller.getId());
			newSmsSeller.setName(tSmsSeller.getName());
			newSmsSeller.setEmail(tSmsSeller.getEmail());
			newSmsSeller.setContact(tSmsSeller.getContact());
			newSmsSeller.setTelephone(tSmsSeller.getTelephone());
			newSmsSeller.setSellerKey(tSmsSeller.getSellerKey());
			newSmsSeller.setSellerSecret(tSmsSeller.getSellerSecret());
			newSmsSeller.setCallbackUrl(tSmsSeller.getCallbackUrl());
			newSmsSeller.setCompanyShow(tSmsSeller.getCompanyShow());
			newSmsSeller.setStatus(tSmsSeller.getStatus());
			
			List<TSmsSellerApps> newList = new ArrayList<TSmsSellerApps>();
			List<TSmsSellerApps> list = tSmsSeller.getSellerApps();
			for (TSmsSellerApps tSmsSellerApps : list) {
				TSmsApp tSmsApp = tSmsSellerApps.getSmsApp();
				TSmsApp newTSmsApp = new TSmsApp();
				newTSmsApp.setId(tSmsApp.getId());
				newTSmsApp.setName(tSmsApp.getName());
				newTSmsApp.setMerchantId(tSmsApp.getMerchantId());
				newTSmsApp.setTips(tSmsApp.getTips());
				
				TSmsSellerApps newTSmsSellerApps = new TSmsSellerApps();
				newTSmsSellerApps.setSmsApp(newTSmsApp);
				newList.add(newTSmsSellerApps);
			}
			newSmsSeller.setSellerApps(newList);
			tSmsSeller = newSmsSeller;
			mc.setCache(key, tSmsSeller, CacheFactory.HOUR * 6);
		}
		return tSmsSeller;
	}
	
	public TSmsSeller getEntity(Integer id) {
		return smsSellerDao.get(id);
	}
	
	/**
	 * 删除信息
	 * @param id
	 */
	public void delete(Integer id) {
		smsSellerDao.delete(id);
	}
	
	public void delete(TSmsSeller entity) {
		smsSellerDao.delete(entity);
	}
	
	public int deleteByProperty(TSmsSeller entity) {
		return smsSellerAppDao.deleteByProperty("smsSeller", entity);
	}
	
	
	public void resetAppToday() {
		smsSellerAppDao.resetAppToday();
	}
	
	public void saveSmsSellerApps(Integer sellerId, Integer appId, Integer fee){
		smsSellerAppDao.saveEntity(sellerId, appId, fee);
	}
	
	/**
	 * 根据appId查找所属seller
	 * @param pid
	 * @return
	 */
	public List<TSmsSeller> findSellerByAppId(Integer appId) {
		List<TSmsSeller> list = null;
		try {
			List<Integer> sellerIds = smsSellerAppDao.findSellerByAppId(appId);
			if (sellerIds.size() > 0) {
				list = new ArrayList<TSmsSeller>();
			}
			for (Integer sellerId : sellerIds) {
				TSmsSeller tSmsSeller = get(sellerId);
				list.add(tSmsSeller);
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
	public List<TSmsSellerLimit> findSellerLimits(Integer sellerId) {
		return smsSellerLimitDao.findSmsSellerByProperty(sellerId);
	}
	
	public TSmsSellerLimit findSellerLimitByProperty(Integer sellerId, String province) {
		TSmsSellerLimit tSmsSellerLimit = smsSellerLimitDao.findSmsSellerByProperty(sellerId, province);
		if (Utils.isEmpty(tSmsSellerLimit)) {
			tSmsSellerLimit = new TSmsSellerLimit();
			tSmsSellerLimit.setSellerId(sellerId);
			tSmsSellerLimit.setProvince(province);
			tSmsSellerLimit.setDayLimit(-1);
			tSmsSellerLimit.setMonthLimit(-1);
		}
		return tSmsSellerLimit;
	}
	
	public void saveSellerLimit(TSmsSellerLimit entity) {
		smsSellerLimitDao.save(entity);
	}
	
}
