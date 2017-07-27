package com.tenfen.www.dao.operation.sms;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;
import org.springside.modules.orm.Page;

import com.tenfen.entity.operation.sms.TSmsProductInfo;
import com.tenfen.util.LogUtil;
import com.tenfen.www.dao.CustomHibernateDao;

@Component
public class SmsProductInfoDao extends CustomHibernateDao<TSmsProductInfo, Long>{
	
	public Page<TSmsProductInfo> findProductByProperties(String name, final Page<TSmsProductInfo> page) {
		Page<TSmsProductInfo> productPage = null;
		try {
			productPage = findPage(page, Restrictions.ilike("name", "%"+name+"%"));
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return productPage;
	}
	
	public List<TSmsProductInfo> getSmsProductInfoByProperty(Integer merchantId, String instruction) {
		List<TSmsProductInfo> tSmsProductInfos = null;
		try {
			Criteria criteria = createCriteria();
			criteria.add(Restrictions.and(Restrictions.eq("merchantId", merchantId), Restrictions.eq("instruction", instruction)));
			
			tSmsProductInfos = criteria.list();
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return tSmsProductInfos;
	}
}