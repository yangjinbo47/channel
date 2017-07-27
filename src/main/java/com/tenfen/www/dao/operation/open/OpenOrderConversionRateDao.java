package com.tenfen.www.dao.operation.open;

import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;
import org.springside.modules.orm.Page;

import com.tenfen.entity.operation.open.TOpenOrderConversionrate;
import com.tenfen.util.LogUtil;
import com.tenfen.www.dao.CustomHibernateDao;

@Component
public class OpenOrderConversionRateDao extends CustomHibernateDao<TOpenOrderConversionrate, Long>{
	
	public Page<TOpenOrderConversionrate> findListByProperties(Integer sellerId) {
		Page<TOpenOrderConversionrate> page = new Page<TOpenOrderConversionrate>();
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
	
	public TOpenOrderConversionrate getEntity(Integer year, Integer month, Integer day, Integer hour, Integer sellerId){
		TOpenOrderConversionrate tOpenOrderConversionrate = null;
		List<TOpenOrderConversionrate> list = find(Restrictions.eq("sellerId", sellerId), Restrictions.eq("year", year), Restrictions.eq("month", month), Restrictions.eq("day", day), Restrictions.eq("hour", hour));
		if (list.size() > 0) {
			tOpenOrderConversionrate = list.get(0);
		}
		return tOpenOrderConversionrate;
	}
}