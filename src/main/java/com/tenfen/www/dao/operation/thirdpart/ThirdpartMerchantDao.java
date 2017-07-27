package com.tenfen.www.dao.operation.thirdpart;

import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;
import org.springside.modules.orm.Page;

import com.tenfen.entity.operation.thirdpart.TThirdMerchant;
import com.tenfen.www.dao.CustomHibernateDao;

@Component
public class ThirdpartMerchantDao extends CustomHibernateDao<TThirdMerchant, Long>{
	
	public Page<TThirdMerchant> findMerchantByProperties(String merchantName, final Page<TThirdMerchant> page) {
		Page<TThirdMerchant> merchantPage = null;
		try {
			merchantPage = findPage(page, Restrictions.ilike("merchantName", "%"+merchantName+"%"));
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return merchantPage;
	}
}