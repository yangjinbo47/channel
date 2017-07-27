package com.tenfen.www.service.operation.pack;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springside.modules.orm.Page;

import com.tenfen.cache.CacheFactory;
import com.tenfen.entity.operation.pack.PushPackage;
import com.tenfen.entity.operation.pack.PushPackageLimit;
import com.tenfen.util.Utils;
import com.tenfen.www.dao.operation.pack.PackageDao;
import com.tenfen.www.dao.operation.pack.PackageLimitDao;

@Component
@Transactional
public class PackageManager {
	
	@Autowired
	private PackageDao packageDao;
	@Autowired
	private CacheFactory cacheFactory;
	@Autowired
	private PackageLimitDao packageLimitDao;
	
//	private String keyPre = "push_package_list_";
	
//	public String findPackageList(String channel, Integer price) {
//		String arrayString = null;
//		try {
//			ICacheClient mc = cacheFactory.getCommonCacheClient();
//			if (Utils.isEmpty(price)) {
//				arrayString = (String) mc.getCache(keyPre+channel);
//			} else {
//				arrayString = (String) mc.getCache(keyPre+channel+price);
//			}
//			if (arrayString == null) {
//				List<PushPackage> list = null;
//				if (Utils.isEmpty(price)) {
//					list = packageDao.findPackageList(channel, Constants.PACKAGE_STATUS.NORMAL.getValue());
//				} else {
//					list = packageDao.findPackageList(channel, price, Constants.PACKAGE_STATUS.NORMAL.getValue());
//				}
//				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
//				for (PushPackage pushPackage : list) {//将该包的剩余推送量计算出保存入memcached
//					Integer limit = (Integer)mc.getCache("limit_"+pushPackage.getRecChannel()+"_"+pushPackage.getId()+"_"+sdf.format(new Date()));
//					int package_limit = pushPackage.getPackageLimit();
//					int package_today = pushPackage.getPackageToday();
//					limit = package_limit-package_today < 0 ? 0 : package_limit-package_today;
//					mc.setCache("limit_"+pushPackage.getRecChannel()+"_"+pushPackage.getId()+"_"+sdf.format(new Date()), limit, CacheFactory.DAY);//包月剩余量
//				}
//				arrayString = JSON.toJSONString(list);
//				if (Utils.isEmpty(price)) {
//					mc.setCache(keyPre+channel, arrayString, CacheFactory.HOUR);
//				} else {
//					mc.setCache(keyPre+channel+price, arrayString, CacheFactory.HOUR);
//				}
//			}
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//		}
//		return arrayString;
//	}
	
//	public List<PushPackage> getAll(Integer userType) {
//		return packageDao.getAll();
//	}
	
//	@SuppressWarnings("unchecked")
//	public List<PushPackage> findPackageList(String channel, Integer price) {
//		List<PushPackage> list = null;
//		try {
//			ICacheClient mc = cacheFactory.getCommonCacheClient();
//			list = (List<PushPackage>) mc.getCache(keyPre+channel+price);
//			if (list == null) {
//				list = packageDao.findPackageList(channel, price, Constants.PACKAGE_STATUS.NORMAL.getValue());
//				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
//				for (PushPackage pushPackage : list) {//将该包的剩余推送量计算出保存入memcached
//					Integer limit = (Integer)mc.getCache("limit_"+pushPackage.getRecChannel()+"_"+pushPackage.getId()+"_"+sdf.format(new Date()));
//					int package_limit = pushPackage.getPackageLimit();
//					int package_today = pushPackage.getPackageToday();
//					limit = package_limit-package_today < 0 ? 0 : package_limit-package_today;
//					mc.setCache("limit_"+pushPackage.getRecChannel()+"_"+pushPackage.getId()+"_"+sdf.format(new Date()), limit, CacheFactory.DAY);//包月剩余量
//				}
//				mc.setCache(keyPre+channel+price, list, CacheFactory.HOUR);
//			}
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//		}
//		return list;
//	}
	
	/**
	 * 获取正常状态的包月list
	 * @param channel
	 * @return
	 */
//	public List<PushPackage> findPackageListByChannel(String channel) {
//		return packageDao.findPackageList(channel, Constants.PACKAGE_STATUS.NORMAL.getValue());
//	}
	
	/**
	 * 获取某渠道下所有的包月列表
	 * @param channel
	 * @return
	 */
//	public List<PushPackage> findPackageListByChannelAll(String channel) {
//		return packageDao.findPackageListAll(channel);
//	}
//	public List<PushPackage> findPackageList(Integer userType) {
//		return packageDao.findPackageListAll(userType);
//	}
	
	/**
	 * 查询所有推送列表
	 * @param page
	 * @return
	 * @author BOBO
	 */
	public List<PushPackage> findAllPackageList(Integer userType) {
//		List<PushPackage> list = packageDao.findAll();
		List<PushPackage> list = packageDao.findPackageListAll(userType);
		return list;
	}
	
	/**
	 * 查询正常状态的推送列表
	 * @param page
	 * @return
	 * @author BOBO
	 */
	public Page<PushPackage> getPackageList(final Page<PushPackage> page, Integer operatorType) {
		Page<PushPackage> packagePage = packageDao.getPackagesByOperatorType(operatorType, page);
		return packagePage;
	}
	
	/**
	 * 根据名字查询包月推送列表
	 * @param name
	 * @return
	 */
	public Page<PushPackage> getPackagesByName(String packageName, String channelName, Integer operatorType, final Page<PushPackage> page) {
		Page<PushPackage> packagePage = packageDao.getPackagesByName(packageName, channelName, operatorType, page);
		return packagePage;
	}
	
	/**
	 * 保存包月信息
	 * @param entity
	 */
	public void save(PushPackage entity) {
		packageDao.save(entity);
	}
	
	/**
	 * 增加今日量
	 * @param pushPackageId
	 */
	public void addTodayLimit(Integer pushPackageId) {
		packageDao.addTodayLimit(pushPackageId);
	}
	
	/**
	 * 获取包月信息
	 * @param id
	 * @return
	 */
	public PushPackage get(Integer id) {
		return packageDao.get(id);
	}
	
	/**
	 * 删除包月信息
	 * @param id
	 */
	public void delete(PushPackage entity) {
		packageDao.delete(entity);
	}
	
	/**
	 * 根据packageId查询渠道限量规则
	 * @param packageId
	 * @return
	 */
	public List<PushPackageLimit> findPackageLimits(Integer packageId) {
		return packageLimitDao.findPackageLimitByProperty(packageId);
	}
	
	public PushPackageLimit findPackageLimitByProperty(Integer packageId, String province) {
		PushPackageLimit pushPackageLimit = packageLimitDao.findPackageLimitByProperty(packageId, province);
		if (Utils.isEmpty(pushPackageLimit)) {
			pushPackageLimit = new PushPackageLimit();
			pushPackageLimit.setPackageId(packageId);
			pushPackageLimit.setProvince(province);
			pushPackageLimit.setDayLimit(-1);
			pushPackageLimit.setMonthLimit(-1);
		}
		return pushPackageLimit;
	}
	
	public void savePackageLimit(PushPackageLimit entity) {
		packageLimitDao.save(entity);
	}
	
}
