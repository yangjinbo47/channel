package com.tenfen.www.dao.operation.open;

import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;
import org.springside.modules.orm.Page;

import com.tenfen.entity.operation.open.TOpenMerchant;
import com.tenfen.www.dao.CustomHibernateDao;

@Component
public class OpenMerchantDao extends CustomHibernateDao<TOpenMerchant, Long>{
	
	public Page<TOpenMerchant> findMerchantByProperties(String merchantName, final Page<TOpenMerchant> page) {
		Page<TOpenMerchant> merchantPage = null;
		try {
			merchantPage = findPage(page, Restrictions.ilike("merchantName", "%"+merchantName+"%"));
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return merchantPage;
	}
}