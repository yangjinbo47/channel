package com.tenfen.www.dao.operation.sms;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;

import com.tenfen.entity.operation.sms.TSmsSellerLimit;
import com.tenfen.util.LogUtil;
import com.tenfen.www.dao.CustomHibernateDao;

@SuppressWarnings("unchecked")
@Component
public class SmsSellerLimitDao extends CustomHibernateDao<TSmsSellerLimit, Long>{
	
	public List<TSmsSellerLimit> findSmsSellerByProperty(Integer sellerId) {
		List<TSmsSellerLimit> tSmsSellerLimits = null;
		try {
			Criteria criteria = createCriteria();
			criteria.add(Restrictions.eq("sellerId", sellerId));
			
			tSmsSellerLimits = criteria.list();
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return tSmsSellerLimits;
	}
	
	public TSmsSellerLimit findSmsSellerByProperty(Integer sellerId, String province) {
		TSmsSellerLimit tSmsSellerLimit = null;
		try {
			Criteria criteria = createCriteria();
			criteria.add(Restrictions.and(Restrictions.eq("sellerId", sellerId), Restrictions.eq("province", province)));
			
			tSmsSellerLimit = (TSmsSellerLimit)criteria.uniqueResult();
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return tSmsSellerLimit;
	}
	
}