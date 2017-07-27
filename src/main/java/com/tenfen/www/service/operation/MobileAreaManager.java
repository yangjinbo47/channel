package com.tenfen.www.service.operation;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springside.modules.orm.Page;

import com.tenfen.cache.CacheFactory;
import com.tenfen.cache.services.ICacheClient;
import com.tenfen.entity.operation.TMobileArea;
import com.tenfen.www.dao.operation.MobileAreaDao;

@Component
@Transactional
public class MobileAreaManager {
	
	@Autowired
	private CacheFactory cacheFactory;
	@Autowired
	private MobileAreaDao mobileAreaDao;
	
	public TMobileArea getMobileArea(Integer id) {
		TMobileArea mobileArea = mobileAreaDao.get(id);
		return mobileArea;
	}
	
	public TMobileArea getMobileArea(String phoneNum) {
		ICacheClient iCacheClient = cacheFactory.getCommonCacheClient();
		TMobileArea mobileArea = (TMobileArea) iCacheClient.getCache(phoneNum.substring(0,8));
		if (mobileArea == null) {
			String firstNum = phoneNum.substring(0, 3);
			String middleNum = phoneNum.substring(3, 7);
			List<TMobileArea> list = null;
			list = mobileAreaDao.getMobileAreaByPhone(firstNum, middleNum);
			if (list != null && !list.isEmpty()) {
				mobileArea = list.get(0);
				if (mobileArea != null) {
					iCacheClient.setCache(phoneNum.substring(0,8), mobileArea, CacheFactory.HOUR*2);
				}
			}
		}
		return mobileArea;
	}
	
	public Page<TMobileArea> getMobileAreaPage(final Page<TMobileArea> page) {
		Page<TMobileArea> mobileAreaPage = mobileAreaDao.getAll(page);
		return mobileAreaPage;
	}
	
	/**
	 * 根据firstNum,middleNum查询H码信息
	 * @param name
	 * @return
	 */
	public Page<TMobileArea> getMobileAreaByProperties(String firstNum, String middleNum, final Page<TMobileArea> page) {
		Page<TMobileArea> mobileAreaPage = mobileAreaDao.getMobileAreaByProperties(firstNum, middleNum, page);
		return mobileAreaPage;
	}
	
	/**
	 * 根据firstNum,middleNum查询H码信息列表
	 * @param name
	 * @return
	 */
	public List<TMobileArea> getMobileAreaList(String firstNum, String middleNum) {
		return mobileAreaDao.getMobileAreaByPhone(firstNum, middleNum);
	}
	
	/**
	 * 保存H码信息
	 * @param entity
	 */
	public void save(TMobileArea entity) {
		mobileAreaDao.save(entity);
	}
	
	/**
	 * 删除H码表信息
	 * @param id
	 */
	public void delete(Integer id) {
		mobileAreaDao.delete(id);
	}
	
}
