package com.tenfen.www.dao.operation.sms;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;

import com.tenfen.entity.operation.sms.TSmsOrderXwPackage;
import com.tenfen.util.LogUtil;
import com.tenfen.www.dao.CustomHibernateDao;

@Component
public class SmsOrderXwPackageDao extends CustomHibernateDao<TSmsOrderXwPackage, Long>{
	
	public List<TSmsOrderXwPackage> getOrderListByPhoneAndContent(String mobile, String content) {
		List<TSmsOrderXwPackage> list = null;
		try {
			Criteria criteria = createCriteria();
			criteria.add(Restrictions.and(Restrictions.eq("mobile", mobile), Restrictions.eq("content", content)));
			criteria.addOrder(Order.desc("id"));
			
			list = criteria.list();
		} catch (Exception e) {
			LogUtil.error(e.getMessage(),e);
		}
		return list;
	}
	
}