package com.tenfen.cache.services.impl;

import java.util.Date;

import net.rubyeye.xmemcached.MemcachedClient;

import com.tenfen.cache.CacheFactory;
import com.tenfen.cache.services.ICacheClient;
import com.tenfen.util.LogUtil;

public class XMemcacheClient implements ICacheClient {

	private MemcachedClient memcachedClient;
	private static XMemcacheClient xMemcacheClient;
	
	public XMemcacheClient(MemcachedClient memcachedClient) {
		this.memcachedClient = memcachedClient;
	}
	
	public static XMemcacheClient getInstance(MemcachedClient memcachedClient) {
		if (xMemcacheClient == null) {
			xMemcacheClient = new XMemcacheClient(memcachedClient);
		}
		return xMemcacheClient;
	}

	@Override
	public boolean setCache(String key, Object object) {
		boolean b = false;
		try {
			if (object != null) {				
				b = memcachedClient.set(key, CacheFactory.HOUR * 2, object);
			}
		} catch (Exception e) {
			LogUtil.error("#### MemcacheClient.setCache(" + key + ")", e);
		}
		return b;
	}
	
	@Override
	public boolean setCache(String key, Object object, Integer expiryTime) {
		boolean b = false;
		try {
			if (object != null) {				
				b = memcachedClient.set(key, expiryTime, object);
			}
		} catch (Exception e) {
			LogUtil.error("#### MemcacheClient.setCache(" + key + ")", e);
		}
		return b;
	}
	
	@Override
	public boolean addCache(String key, Object object) {
		boolean b = false;
		try {
			if (object != null) {				
				b = memcachedClient.add(key, CacheFactory.HOUR * 2, object);
			}
		} catch (Exception e) {
			LogUtil.error("#### MemcacheClient.addCache(" + key + ")", e);
		}
		return b;
	}

	@Override
	public boolean addCache(String key, Object object, Integer expiryTime) {
		boolean b = false;
		try {
			if (object != null) {				
				b = memcachedClient.add(key, expiryTime, object);
			}
		} catch (Exception e) {
			LogUtil.error("#### MemcacheClient.addCache(" + key + ")", e);
		}
		return b;
	}

	@Override
	public boolean addCache(String key, Object object, Date expiryDate) {
		int second = (int) (expiryDate.getTime() - new Date().getTime()) / 1000;
		boolean b = false;
		try {
			if (object != null) {				
				b = memcachedClient.add(key, second, object);
			}
		} catch (Exception e) {
			LogUtil.error("#### MemcacheClient.addCache(" + key + ")", e);
		}
		return b;
	}

	@Override
	public boolean deleteCache(String key) {
		boolean b = false;
		try {
			b = memcachedClient.delete(key);
//			if (b) {				
//				LogUtil.log("#### MemcacheClient.deleteCache(" + key + "),result="+ b +";");
//			}
		} catch (Exception e) {
			LogUtil.error("#### MemcacheClient.deleteCache(" + key + ")", e);
		}
		return b;
	}

	@Override
	public boolean existsCache(String key) {
		return getCache(key) != null;
	}

	@Override
	public Object getCache(String key) {
		try {
//			LogUtil.log("#### MemcacheClient.getCache(" + key + ");");
			return memcachedClient.get(key);
		} catch (Exception e) {
			LogUtil.error("#### MemcacheClient.getCache(" + key + ")", e);
		}
		return null;
	}

	@Override
	public boolean flushAll() {
		try {
			memcachedClient.flushAll();
			return true;
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return false;
	}
	
}
