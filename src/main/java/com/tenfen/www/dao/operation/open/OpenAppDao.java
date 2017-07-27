package com.tenfen.www.dao.operation.open;

import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;
import org.springside.modules.orm.Page;

import com.tenfen.entity.operation.open.TOpenApp;
import com.tenfen.util.LogUtil;
import com.tenfen.www.common.Constants;
import com.tenfen.www.dao.CustomHibernateDao;

@Component
public class OpenAppDao extends CustomHibernateDao<TOpenApp, Long>{
	
	public Page<TOpenApp> findAppByProperties(String appName, final Page<TOpenApp> page, Integer operatorType) {
		Page<TOpenApp> appPage = null;
		try {
			if (Constants.USER_TYPE.ALL.getValue().equals(operatorType)) {//所有可见				
				appPage = findPage(page, Restrictions.ilike("name", "%"+appName+"%"));
			} else {
				appPage = findPage(page, Restrictions.and(Restrictions.ilike("name", "%"+appName+"%"), Restrictions.eq("companyShow", operatorType)));
			}
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return appPage;
	}
	
	/**
	 * 根据用户类型查所有
	 * @param page
	 * @return
	 */
	public Page<TOpenApp> getAppsByOperatorType(final Page<TOpenApp> page, Integer operatorType) {
		Page<TOpenApp> appPage = null;
		try {
			if (Constants.USER_TYPE.ALL.getValue().equals(operatorType)) {//所有可见
				appPage = getAll(page);
			} else {
				appPage = findPage(page, Restrictions.eq("companyShow", operatorType));
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(),e);
		}
		return appPage;
	}
}