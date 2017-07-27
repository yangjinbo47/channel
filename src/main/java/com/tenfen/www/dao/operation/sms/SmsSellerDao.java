package com.tenfen.www.dao.operation.sms;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;
import org.springside.modules.orm.Page;

import com.tenfen.entity.operation.sms.TSmsSeller;
import com.tenfen.util.LogUtil;
import com.tenfen.www.common.Constants;
import com.tenfen.www.dao.CustomHibernateDao;

@Component
public class SmsSellerDao extends CustomHibernateDao<TSmsSeller, Long>{
	
//	public Page<TSmsSeller> findSellerByProperties(String name, final Page<TSmsSeller> page) {
//		Page<TSmsSeller> sellerPage = null;
//		try {
//			sellerPage = findPage(page, Restrictions.ilike("name", "%"+name+"%"));
//		} catch (Exception e) {
//			logger.error(e.getMessage(),e);
//		}
//		return sellerPage;
//	}
	
	public List<TSmsSeller> getAllSellersByOperatorType(Integer operatorType) {
		List<TSmsSeller> sellerList = new ArrayList<TSmsSeller>();
		try {
			if (Constants.USER_TYPE.ALL.getValue().equals(operatorType)) {//所有可见
				sellerList = getAll();
			} else {
				sellerList = findBy("companyShow", operatorType);
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(),e);
		}
		return sellerList;
	}
	
	/**
	 * 根据用户类型查所有
	 * @param page
	 * @return
	 */
	public Page<TSmsSeller> getSellersByOperatorType(final Page<TSmsSeller> page, Integer operatorType) {
		Page<TSmsSeller> sellerPage = null;
		try {
			if (Constants.USER_TYPE.ALL.getValue().equals(operatorType)) {//所有可见
				sellerPage = getAll(page);
			} else {
				sellerPage = findPage(page, Restrictions.eq("companyShow", operatorType));
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(),e);
		}
		return sellerPage;
	}
	
	public Page<TSmsSeller> findSellerByProperties(String name, final Page<TSmsSeller> page, Integer operatorType) {
		Page<TSmsSeller> sellerPage = null;
		try {
			if (Constants.USER_TYPE.ALL.getValue().equals(operatorType)) {//所有可见
				sellerPage = findPage(page, Restrictions.ilike("name", "%"+name+"%"));
			} else {
				sellerPage = findPage(page, Restrictions.and(Restrictions.ilike("name", "%"+name+"%"), Restrictions.eq("companyShow", operatorType)));
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(),e);
		}
		return sellerPage;
	}
}