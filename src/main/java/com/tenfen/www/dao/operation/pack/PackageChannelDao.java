package com.tenfen.www.dao.operation.pack;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;
import org.springside.modules.orm.Page;

import com.tenfen.entity.operation.pack.PushPackageChannel;
import com.tenfen.www.common.Constants;
import com.tenfen.www.dao.CustomHibernateDao;

@Component
public class PackageChannelDao extends CustomHibernateDao<PushPackageChannel, Long>{
	
	@SuppressWarnings("unchecked")
	public List<PushPackageChannel> findAllList() {
		List<PushPackageChannel> packageChannelList = new ArrayList<PushPackageChannel>();
		try {
			Criteria criteria = createCriteria();
			criteria.addOrder(Order.desc("id"));
			
			packageChannelList = criteria.list();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return packageChannelList;
	}
	
	/**
	 * 获取所有渠道list
	 * @param channel
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<PushPackageChannel> findAllList(Integer operatorType) {
		List<PushPackageChannel> packageChannelList = new ArrayList<PushPackageChannel>();
		try {
			Criteria criteria = createCriteria();
			if (Constants.USER_TYPE.ALL.getValue().equals(operatorType)) {//所有可见
			} else {
				criteria.add(Restrictions.eq("companyShow", operatorType));
			}
			criteria.addOrder(Order.desc("id"));
			
			packageChannelList = criteria.list();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return packageChannelList;
	}
	
	public Page<PushPackageChannel> getPackageChannelPage(final Page<PushPackageChannel> page, Integer operatorType) {
		Page<PushPackageChannel> packageChannelPage = null;
		try {
			if (Constants.USER_TYPE.ALL.getValue().equals(operatorType)) {//所有可见
				packageChannelPage = getAll(page);
			} else {
				packageChannelPage = findPage(page, Restrictions.eq("companyShow", operatorType));
			}
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return packageChannelPage;
	}
	
	/**
	 * 根据名称获取渠道list
	 * @param name
	 * @param page
	 * @return
	 */
	public Page<PushPackageChannel> getPackageChannelsByName(String name,final Page<PushPackageChannel> page, Integer operatorType) {
		Page<PushPackageChannel> packageChannelPage = null;
		try {
			if (Constants.USER_TYPE.ALL.getValue().equals(operatorType)) {//所有可见
				packageChannelPage = findPage(page, Restrictions.ilike("channelName", "%"+name+"%"));
			} else {
				packageChannelPage = findPage(page, Restrictions.and(Restrictions.ilike("channelName", "%"+name+"%"),Restrictions.eq("companyShow", operatorType)));
			}
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return packageChannelPage;
	}
}
