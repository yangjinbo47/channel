package com.tenfen.www.dao.operation.sms;

import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;
import org.springside.modules.orm.Page;

import com.tenfen.entity.operation.sms.TSmsOrderConversionrate;
import com.tenfen.util.LogUtil;
import com.tenfen.www.dao.CustomHibernateDao;

@Component
public class SmsOrderConversionRateDao extends CustomHibernateDao<TSmsOrderConversionrate, Long>{
	
	public Page<TSmsOrderConversionrate> findListByProperties(Integer sellerId) {
		Page<TSmsOrderConversionrate> page = new Page<TSmsOrderConversionrate>();
		page.setPageSize(24);
		page.setPageNo(1);
		if (!page.isOrderBySetted()) {
			page.setOrderBy("id");
			page.setOrder(Page.DESC);
		}
		try {
			page = findPage(page, Restrictions.eq("sellerId", sellerId));
		} catch (Exception e) {
			LogUtil.error(e.getMessage(),e);
		}
		return page;
	}
	
	public TSmsOrderConversionrate getEntity(Integer year, Integer month, Integer day, Integer hour, Integer sellerId){
		TSmsOrderConversionrate tSmsOrderConversionrate = null;
		List<TSmsOrderConversionrate> list = find(Restrictions.eq("sellerId", sellerId), Restrictions.eq("year", year), Restrictions.eq("month", month), Restrictions.eq("day", day), Restrictions.eq("hour", hour));
		if (list.size() > 0) {
			tSmsOrderConversionrate = list.get(0);
		}
		return tSmsOrderConversionrate;
	}
}