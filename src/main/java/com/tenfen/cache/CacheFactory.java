package com.tenfen.cache;

import net.rubyeye.xmemcached.MemcachedClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tenfen.cache.services.ICacheClient;
import com.tenfen.cache.services.impl.XMemcacheClient;

@Component
public class CacheFactory {

	public static final Integer MINUTE = 60;// 一分钟
	public static final Integer HOUR = 3600;// 一小时
	public static final Integer DAY = 86400;// 一天
	public static final Integer UNEXPIRY = 0;// 不过期
	
//	@Autowired
//	private MemCachedClient memcachedClient;
	@Autowired
	private MemcachedClient memcachedClient;
	
	private CacheFactory() {
	}

	/**
	*@功能：WAP通用缓存
	*@author BOBO
	*@date Jan 31, 2012
	*@return
	 */
	public ICacheClient getCommonCacheClient() {
		ICacheClient cacheClient = null;
		if (memcachedClient != null) {
			cacheClient = XMemcacheClient.getInstance(memcachedClient);
//			cacheClient = JavaMemcacheProvider.getInstance(memcachedClient);
		}
		return cacheClient;
	}

}
