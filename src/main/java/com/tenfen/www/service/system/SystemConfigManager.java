package com.tenfen.www.service.system;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.tenfen.cache.CacheFactory;
import com.tenfen.cache.services.ICacheClient;
import com.tenfen.entity.system.SystemConfig;
import com.tenfen.util.LogUtil;
import com.tenfen.www.dao.system.SystemConfigDao;

@Component
@Transactional
public class SystemConfigManager {
	
	@Autowired
	private SystemConfigDao systemConfigDao;
	@Autowired
	private CacheFactory cacheFactory;
	
	private String key_jumpYZM = "sysconfig_jumpYZM";
	
	/**
	 * 保存信息
	 * @param entity
	 */
	public void save(SystemConfig entity) {
		systemConfigDao.save(entity);
	}
	
	/**
	 * 获取包月信息
	 * @param id
	 * @return
	 */
	public SystemConfig get(Integer id) {
		return systemConfigDao.get(id);
	}
	
	/**
	 * 删除包月信息
	 * @param id
	 */
	public void delete(SystemConfig entity) {
		systemConfigDao.delete(entity);
	}
	
	/**
	 * 根据名称获取配置信息
	 * @param name
	 * @return
	 */
	public SystemConfig getSystemConfigByName(String name) {
		SystemConfig systemConfig = null;
		List<SystemConfig> list = systemConfigDao.findBy("name", name);
		if (list.size() > 0) {
			systemConfig = list.get(0);
		}
		return systemConfig;
	}
	
	/**
	 * 是否跳过验证码的json
	 * @return
	 */
	public String jumpYZMJson() {
		String jumpYZM = null;
		try {
			ICacheClient iCacheClient = cacheFactory.getCommonCacheClient();
			jumpYZM = (String)iCacheClient.getCache(key_jumpYZM);
			if (jumpYZM == null) {
				List<SystemConfig> list = systemConfigDao.findBy("name", "sysConfig");
				if (list.size() != 0) {
					SystemConfig systemConfig = list.get(0);
					jumpYZM = systemConfig.getJsonConfig();
					
					iCacheClient.setCache(key_jumpYZM, jumpYZM, CacheFactory.UNEXPIRY);
				}
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return jumpYZM;
	}
	
}
