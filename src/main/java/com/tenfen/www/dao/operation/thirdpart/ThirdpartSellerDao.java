package com.tenfen.www.dao.operation.thirdpart;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;
import org.springside.modules.orm.Page;

import com.tenfen.entity.operation.thirdpart.TThirdSeller;
import com.tenfen.util.LogUtil;
import com.tenfen.www.common.Constants;
import com.tenfen.www.dao.CustomHibernateDao;

@Component
public class ThirdpartSellerDao extends CustomHibernateDao<TThirdSeller, Long>{
	
	public Page<TThirdSeller> findSellerByProperties(String name, final Page<TThirdSeller> page) {
		Page<TThirdSeller> sellerPage = null;
		try {
			sellerPage = findPage(page, Restrictions.ilike("name", "%"+name+"%"));
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return sellerPage;
	}
	
	public List<TThirdSeller> getAllSellersByOperatorType(Integer operatorType) {
		List<TThirdSeller> sellerList = new ArrayList<TThirdSeller>();
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
	public Page<TThirdSeller> getSellersByOperatorType(final Page<TThirdSeller> page, Integer operatorType) {
		Page<TThirdSeller> sellerPage = null;
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
	
	public Page<TThirdSeller> findSellerByProperties(String name, final Page<TThirdSeller> page, Integer operatorType) {
		Page<TThirdSeller> sellerPage = null;
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