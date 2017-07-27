package com.tenfen.www.dao.operation.thirdpart;

import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;
import org.springside.modules.orm.Page;

import com.tenfen.entity.operation.thirdpart.TThirdApp;
import com.tenfen.util.LogUtil;
import com.tenfen.www.dao.CustomHibernateDao;

@Component
public class ThirdpartAppDao extends CustomHibernateDao<TThirdApp, Long>{
	
	public Page<TThirdApp> findAppByProperties(String appName, final Page<TThirdApp> page) {
		Page<TThirdApp> appPage = null;
		try {
			appPage = findPage(page, Restrictions.ilike("name", "%"+appName+"%"));
		} catch (Exception e) {
			LogUtil.error(e.getMessage(),e);
		}
		return appPage;
	}
}