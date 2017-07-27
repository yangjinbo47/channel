package com.tenfen.www.dao.operation.pack;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;

import com.tenfen.entity.operation.pack.PushPackageLimit;
import com.tenfen.util.LogUtil;
import com.tenfen.www.dao.CustomHibernateDao;

@Component
@SuppressWarnings("unchecked")
public class PackageLimitDao extends CustomHibernateDao<PushPackageLimit, Long>{
	
	public List<PushPackageLimit> findPackageLimitByProperty(Integer packageId) {
		List<PushPackageLimit> pushPackageLimits = null;
		try {
			Criteria criteria = createCriteria();
			criteria.add(Restrictions.eq("packageId", packageId));
			
			pushPackageLimits = criteria.list();
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return pushPackageLimits;
	}
	
	public PushPackageLimit findPackageLimitByProperty(Integer packageId, String province) {
		PushPackageLimit pushPackageLimit = null;
		try {
			Criteria criteria = createCriteria();
			criteria.add(Restrictions.and(Restrictions.eq("packageId", packageId), Restrictions.eq("province", province)));
			
			pushPackageLimit = (PushPackageLimit)criteria.uniqueResult();
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return pushPackageLimit;
	}
}
