package com.tenfen.www.dao.operation.sms;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;
import org.springside.modules.orm.Page;

import com.tenfen.entity.operation.sms.TSmsApp;
import com.tenfen.util.LogUtil;
import com.tenfen.www.common.Constants;
import com.tenfen.www.dao.CustomHibernateDao;

@Component
public class SmsAppDao extends CustomHibernateDao<TSmsApp, Long>{
	
	/**
	 * 根据用户类型查所有
	 * @param page
	 * @return
	 */
	public Page<TSmsApp> getAppsByOperatorType(final Page<TSmsApp> page, Integer operatorType) {
		Page<TSmsApp> appPage = null;
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
	
	public Page<TSmsApp> findAppByProperties(String appName, final Page<TSmsApp> page, Integer operatorType) {
		Page<TSmsApp> appPage = null;
		try {
			if (Constants.USER_TYPE.ALL.getValue().equals(operatorType)) {//所有可见				
				appPage = findPage(page, Restrictions.ilike("name", "%"+appName+"%"));
			} else {
				appPage = findPage(page, Restrictions.and(Restrictions.ilike("name", "%"+appName+"%"), Restrictions.eq("companyShow", operatorType)));
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(),e);
		}
		return appPage;
	}
	
	/**
	 * 根据pid查询所属appid
	 * @param pid
	 * @return
	 */
	public List<Integer> findAppByProductId(Integer pid) {
		Session session = null;
		List<Integer> list = null;
		try {
			session = getSession();
			String sql = "select app_id from t_sms_app_product t where t.p_id=:pid";
			Query query = session.createSQLQuery(sql);
			query.setParameter("pid", pid);
			list = query.list();//appid
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return list;
	}
}