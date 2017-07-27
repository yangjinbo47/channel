package com.tenfen.www.dao.operation.open;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;

import com.tenfen.entity.operation.open.TOpenAppLimit;
import com.tenfen.util.LogUtil;
import com.tenfen.www.dao.CustomHibernateDao;

@SuppressWarnings("unchecked")
@Component
public class OpenAppLimitDao extends CustomHibernateDao<TOpenAppLimit, Long>{
	
	public List<TOpenAppLimit> findOpenAppByProperty(Integer appId) {
		List<TOpenAppLimit> tOpenAppLimits = null;
		try {
			Criteria criteria = createCriteria();
			criteria.add(Restrictions.eq("appId", appId));
			
			tOpenAppLimits = criteria.list();
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return tOpenAppLimits;
	}
	
	public TOpenAppLimit findOpenAppByProperty(Integer appId, String province) {
		TOpenAppLimit tOpenAppLimit = null;
		try {
			Criteria criteria = createCriteria();
			criteria.add(Restrictions.and(Restrictions.eq("appId", appId), Restrictions.eq("province", province)));
			
			tOpenAppLimit = (TOpenAppLimit)criteria.uniqueResult();
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return tOpenAppLimit;
	}
	
}