package com.tenfen.cache.services.impl;

import java.util.Date;

import com.danga.MemCached.MemCachedClient;
import com.tenfen.cache.CacheFactory;
import com.tenfen.cache.services.ICacheClient;
import com.tenfen.util.LogUtil;

public class JavaMemcacheProvider implements ICacheClient {

	private MemCachedClient memCachedClient;
	private static JavaMemcacheProvider javaMemcacheProvider;
	// 缓存服务
//	MemcacheClientService service = MemcacheClientServiceFactory.getInstance();

	public JavaMemcacheProvider(MemCachedClient memcachedClient) {
		this.memCachedClient = memcachedClient;
	}
	
	public static JavaMemcacheProvider getInstance(MemCachedClient memcachedClient) {
		if (javaMemcacheProvider == null) {
			javaMemcacheProvider = new JavaMemcacheProvider(memcachedClient);
		}
		return javaMemcacheProvider;
	}
	
	@Override
	public boolean setCache(String key, Object object) {
		return false;
	}
	
	@Override
	public boolean setCache(String key, Object object, Integer expiryTime) {
		return false;
	}
	
	@Override
	public boolean addCache(String key, Object object, Date expiryDate) {
		int second = (int) (expiryDate.getTime() - new Date().getTime()) / 1000;
		return addCache(key, object, second);
	}

	@Override
	public boolean addCache(String key, Object object, Integer expiryTime) {
		boolean b = memCachedClient.set(key, object, expiryTime);
		LogUtil.log("#### MemcacheClient.addCache(" + key + "),result="+b+";");
		return b;
	}

	@Override
	public boolean addCache(String key, Object object) {
		return addCache(key, object, CacheFactory.MINUTE * 30);
	}

	@Override
	public boolean deleteCache(String key) {
		boolean b = memCachedClient.delete(key);
		LogUtil.log("#### MemcacheClient.deleteCache(" + key + "),result="+b+";");
		return b;
	}

	@Override
	public boolean existsCache(String key) {
		return memCachedClient.keyExists(key);
	}

	@Override
	public Object getCache(String key) {
		LogUtil.log("#### MemcacheClient.getCache(" + key + ");");
		return memCachedClient.get(key);
	}
	
	@Override
	public boolean flushAll() {
		boolean b = memCachedClient.flushAll();
		LogUtil.log("#### MemcacheClient.flushAll,result="+b+";");
		return b;
	}

}
