package com.tenfen.www.dao.operation.open;

import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;
import org.springside.modules.orm.Page;

import com.tenfen.entity.operation.open.TOpenMailgroup;
import com.tenfen.www.dao.CustomHibernateDao;

@Component
public class OpenMailgroupDao extends CustomHibernateDao<TOpenMailgroup, Long>{
	
	public Page<TOpenMailgroup> findGroupByProperties(String name, final Page<TOpenMailgroup> page) {
		Page<TOpenMailgroup> groupPage = null;
		try {
			groupPage = findPage(page, Restrictions.ilike("name", "%"+name+"%"));
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return groupPage;
	}
}