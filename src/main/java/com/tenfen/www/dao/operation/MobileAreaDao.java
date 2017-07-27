package com.tenfen.www.dao.operation;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;
import org.springside.modules.orm.Page;

import com.tenfen.entity.operation.TMobileArea;
import com.tenfen.www.dao.CustomHibernateDao;

@Component
@SuppressWarnings("unchecked")
public class MobileAreaDao extends CustomHibernateDao<TMobileArea, Long>{
	
	public List<TMobileArea> getMobileAreaByPhone(String firstNum) {
		List<TMobileArea> tMobileAreaList = new ArrayList<TMobileArea>();
		try {
			Criteria criteria = createCriteria();
			criteria.add(Restrictions.eq("firstNum", firstNum));
			
			tMobileAreaList = criteria.list();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return tMobileAreaList;
	}

	public List<TMobileArea> getMobileAreaByPhone(String firstNum, String middleNum) {
		List<TMobileArea> tMobileAreaList = new ArrayList<TMobileArea>();
		try {
			Criteria criteria = createCriteria();
			criteria.add(Restrictions.eq("firstNum", firstNum));
			criteria.add(Restrictions.eq("middleNum", middleNum));
			
			tMobileAreaList = criteria.list();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return tMobileAreaList;
	}
	
	/**
	 * 根据名称获取渠道list
	 * @param name
	 * @param page
	 * @return
	 */
	public Page<TMobileArea> getMobileAreaByProperties(String firstNum, String middleNum, final Page<TMobileArea> page) {
		Page<TMobileArea> mobileAreaPage = null;
		try {
			mobileAreaPage = findPage(page, Restrictions.and(Restrictions.ilike("firstNum", "%"+firstNum+"%"), Restrictions.ilike("middleNum", "%"+middleNum+"%")));
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return mobileAreaPage;
	}
	
}
