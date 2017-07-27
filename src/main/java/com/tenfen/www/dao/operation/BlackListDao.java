package com.tenfen.www.dao.operation;

import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;
import org.springside.modules.orm.Page;

import com.tenfen.entity.operation.TBlackList;
import com.tenfen.www.dao.CustomHibernateDao;

@Component
public class BlackListDao extends CustomHibernateDao<TBlackList, Long>{
	
	public Page<TBlackList> findBlackListByPhone(String phoneNum, final Page<TBlackList> page) {
		Page<TBlackList> blackListPage = null;
		try {
			blackListPage = findPage(page, Restrictions.ilike("phoneNum", "%"+phoneNum+"%"));
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return blackListPage;
	}
}