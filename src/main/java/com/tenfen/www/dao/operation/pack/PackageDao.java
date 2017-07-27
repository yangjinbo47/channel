package com.tenfen.www.dao.operation.pack;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;
import org.springside.modules.orm.Page;

import com.tenfen.entity.operation.pack.PushPackage;
import com.tenfen.www.common.Constants;
import com.tenfen.www.dao.CustomHibernateDao;

@Component
@SuppressWarnings("unchecked")
public class PackageDao extends CustomHibernateDao<PushPackage, Long>{
	
	/**
	 * 增加今日量
	 * @param pushPackageId
	 */
	public void addTodayLimit(Integer pushPackageId) {
		PushPackage pushPackage = get(pushPackageId);
		Integer todayLimit = pushPackage.getPackageToday();
		todayLimit = todayLimit + 1;
		pushPackage.setPackageToday(todayLimit);
		save(pushPackage);
	}
	
	/**
	 * 获取所有推送list
	 * @return
	 */
	public List<PushPackage> findAll() {
		List<PushPackage> packageList = new ArrayList<PushPackage>();
		try {
			Criteria criteria = createCriteria();
			
			packageList = criteria.list();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return packageList;
	}
	
	/**
	 * 根据渠道名获取所有包月list
	 * @param channel
	 * @return
	 */
//	public List<PushPackage> findPackageListAll(String channel) {
//		List<PushPackage> packageList = new ArrayList<PushPackage>();
//		try {
//			Criteria criteria = createCriteria();
//			criteria.add(Restrictions.eq("recChannel", channel));
//			criteria.addOrder(Order.desc("id"));
//			
//			packageList = criteria.list();
//		} catch (Exception e) {
//			logger.error(e.getMessage(), e);
//		}
//		return packageList;
//	}
	public List<PushPackage> findPackageListAll(Integer operatorType) {
		List<PushPackage> packageList = new ArrayList<PushPackage>();
		try {
			if (Constants.USER_TYPE.ALL.getValue().equals(operatorType)) {//所有可见
				packageList = getAll();
			} else {
				packageList = find(Restrictions.eq("companyShow", operatorType));
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return packageList;
	}
	
	/**
	 * 根据渠道名获取某状态包月list
	 * @param channel
	 * @return
	 */
	public List<PushPackage> findPackageList(String channel, Integer status) {
		List<PushPackage> packageList = new ArrayList<PushPackage>();
		try {
			Criteria criteria = createCriteria();
			criteria.add(Restrictions.eq("recChannel", channel));
			criteria.add(Restrictions.eq("status", status));
			criteria.addOrder(Order.desc("id"));
			
			packageList = criteria.list();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return packageList;
	}
	
	/**
	 * 根据渠道名,资费获取包月list
	 * @param channel
	 * @param price
	 * @return
	 */
	public List<PushPackage> findPackageList(String channel, Integer price, Integer status) {
		List<PushPackage> packageList = new ArrayList<PushPackage>();
		try {
			Criteria criteria = createCriteria();
			criteria.add(Restrictions.eq("recChannel", channel));
			criteria.add(Restrictions.eq("price", price));
			criteria.add(Restrictions.eq("status", status));
			criteria.addOrder(Order.desc("id"));
			
			packageList = criteria.list();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return packageList;
	}
	
	/**
	 * 根据类型查所有
	 * @param name
	 * @param page
	 * @return
	 */
	public Page<PushPackage> getPackagesByOperatorType(Integer operatorType, final Page<PushPackage> page) {
		Page<PushPackage> packagePage = null;
		try {
			if (Constants.USER_TYPE.ALL.getValue().equals(operatorType)) {//所有可见
				packagePage = getAll(page);
			} else {
				packagePage = findPage(page, Restrictions.eq("companyShow", operatorType));
			}
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return packagePage;
	}
	
	/**
	 * 根据名称获取包月list
	 * @param name
	 * @param page
	 * @return
	 */
	public Page<PushPackage> getPackagesByName(String packageName, String channelName, Integer operatorType, final Page<PushPackage> page) {
		Page<PushPackage> packagePage = null;
		try {
			if (Constants.USER_TYPE.ALL.getValue().equals(operatorType)) {//所有可见
				packagePage = findPage(page, Restrictions.and(Restrictions.ilike("packageName", "%"+packageName+"%"), Restrictions.ilike("recChannel", "%"+channelName+"%")));
			} else {
				packagePage = findPage(page, Restrictions.and(Restrictions.and(Restrictions.ilike("packageName", "%"+packageName+"%"), Restrictions.ilike("recChannel", "%"+channelName+"%")), Restrictions.eq("companyShow", operatorType)));
			}
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return packagePage;
	}

}
