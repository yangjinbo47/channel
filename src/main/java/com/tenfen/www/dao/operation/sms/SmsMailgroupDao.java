package com.tenfen.www.dao.operation.sms;

import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;
import org.springside.modules.orm.Page;

import com.tenfen.entity.operation.sms.TSmsMailgroup;
import com.tenfen.www.dao.CustomHibernateDao;

@Component
public class SmsMailgroupDao extends CustomHibernateDao<TSmsMailgroup, Long>{
	
	public Page<TSmsMailgroup> findGroupByProperties(String name, final Page<TSmsMailgroup> page) {
		Page<TSmsMailgroup> groupPage = null;
		try {
			groupPage = findPage(page, Restrictions.ilike("name", "%"+name+"%"));
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return groupPage;
	}
}