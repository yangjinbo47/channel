package com.tenfen.www.dao.operation.sms;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;

import com.tenfen.entity.operation.sms.TSmsAppLimit;
import com.tenfen.util.LogUtil;
import com.tenfen.www.dao.CustomHibernateDao;

@SuppressWarnings("unchecked")
@Component
public class SmsAppLimitDao extends CustomHibernateDao<TSmsAppLimit, Long>{
	
	public List<TSmsAppLimit> findSmsAppByProperty(Integer appId) {
		List<TSmsAppLimit> tSmsAppLimits = null;
		try {
			Criteria criteria = createCriteria();
			criteria.add(Restrictions.eq("appId", appId));
			
			tSmsAppLimits = criteria.list();
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return tSmsAppLimits;
	}
	
	public TSmsAppLimit findSmsAppByProperty(Integer appId, String province) {
		TSmsAppLimit tSmsAppLimit = null;
		try {
			Criteria criteria = createCriteria();
			criteria.add(Restrictions.and(Restrictions.eq("appId", appId), Restrictions.eq("province", province)));
			
			tSmsAppLimit = (TSmsAppLimit)criteria.uniqueResult();
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return tSmsAppLimit;
	}
	
}