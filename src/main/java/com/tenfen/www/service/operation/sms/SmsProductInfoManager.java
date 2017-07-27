package com.tenfen.www.service.operation.sms;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springside.modules.orm.Page;

import com.tenfen.entity.operation.sms.TSmsProductInfo;
import com.tenfen.util.LogUtil;
import com.tenfen.www.dao.operation.sms.SmsProductInfoDao;

@Component
@Transactional
public class SmsProductInfoManager {
	
	@Autowired
	private SmsProductInfoDao smsProductInfoDao;
	
	public TSmsProductInfo getSmsProductInfoByProperty(String propertyName, Object value) {
		TSmsProductInfo tSmsProductInfo = null;
		try {
			List<TSmsProductInfo> tSmsProductInfos = smsProductInfoDao.findBy(propertyName, value);
			if (tSmsProductInfos.size() > 0) {
				tSmsProductInfo = tSmsProductInfos.get(0);
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return tSmsProductInfo;
	}
	
	public TSmsProductInfo getSmsProductInfoByProperty(Integer merchantId, String instruction) {
		TSmsProductInfo tSmsProductInfo = null;
		try {
			List<TSmsProductInfo> tSmsProductInfos = smsProductInfoDao.getSmsProductInfoByProperty(merchantId, instruction);
			if (tSmsProductInfos.size() > 0) {
				tSmsProductInfo = tSmsProductInfos.get(0);
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return tSmsProductInfo;
	}
	
	public Page<TSmsProductInfo> findProductPage(final Page<TSmsProductInfo> page) {
		Page<TSmsProductInfo> productPage = smsProductInfoDao.getAll(page);
		return productPage;
	}
	
	public Page<TSmsProductInfo> findProductPage(String name, final Page<TSmsProductInfo> page) {
		Page<TSmsProductInfo> productPage = smsProductInfoDao.findProductByProperties(name, page);
		return productPage;
	}
	
	public List<TSmsProductInfo> findSmsProductInfoByMerchantId(Integer merchantId) {
		return smsProductInfoDao.findBy("merchantId", merchantId);
	}
	
	/**
	 * 保存信息
	 * @param entity
	 */
	public void save(TSmsProductInfo entity) {
		smsProductInfoDao.save(entity);
	}
	
	/**
	 * 获取信息
	 * @param id
	 * @return
	 */
	public TSmsProductInfo get(Integer id) {
		return smsProductInfoDao.get(id);
	}
	
	/**
	 * 删除信息
	 * @param id
	 */
	public void delete(Integer id) {
		smsProductInfoDao.delete(id);
	}
	
}
