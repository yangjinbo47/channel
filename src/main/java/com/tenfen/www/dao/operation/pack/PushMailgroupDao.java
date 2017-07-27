package com.tenfen.www.dao.operation.pack;

import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;
import org.springside.modules.orm.Page;

import com.tenfen.entity.operation.pack.TPushMailgroup;
import com.tenfen.www.dao.CustomHibernateDao;

@Component
public class PushMailgroupDao extends CustomHibernateDao<TPushMailgroup, Long>{
	
	public Page<TPushMailgroup> findGroupByProperties(String name, final Page<TPushMailgroup> page) {
		Page<TPushMailgroup> groupPage = null;
		try {
			groupPage = findPage(page, Restrictions.ilike("name", "%"+name+"%"));
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return groupPage;
	}
}