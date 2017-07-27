package com.tenfen.www.dao.operation.open;

import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;
import org.springside.modules.orm.Page;

import com.tenfen.entity.operation.open.TOpenProductInfo;
import com.tenfen.www.dao.CustomHibernateDao;

@Component
public class OpenProductInfoDao extends CustomHibernateDao<TOpenProductInfo, Long>{
	
	public Page<TOpenProductInfo> findProductByProperties(String name, final Page<TOpenProductInfo> page) {
		Page<TOpenProductInfo> productPage = null;
		try {
			productPage = findPage(page, Restrictions.ilike("name", "%"+name+"%"));
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return productPage;
	}
}