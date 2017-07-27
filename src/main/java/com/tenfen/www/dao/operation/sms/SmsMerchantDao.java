package com.tenfen.www.dao.operation.sms;

import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;
import org.springside.modules.orm.Page;

import com.tenfen.entity.operation.sms.TSmsMerchant;
import com.tenfen.www.dao.CustomHibernateDao;

@Component
public class SmsMerchantDao extends CustomHibernateDao<TSmsMerchant, Long>{
	
	public Page<TSmsMerchant> findMerchantByProperties(String merchantName, final Page<TSmsMerchant> page) {
		Page<TSmsMerchant> merchantPage = null;
		try {
			merchantPage = findPage(page, Restrictions.ilike("merchantName", "%"+merchantName+"%"));
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return merchantPage;
	}
}