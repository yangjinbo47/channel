package com.tenfen.cache.modules;

import java.util.Collection;
import java.util.HashMap;

import com.tenfen.cache.services.ICacheClient;

@SuppressWarnings("unchecked")
public class CacheClientPool extends HashMap {

	private static final long serialVersionUID = 1L;
	static CacheClientPool cacheClientPool = new CacheClientPool();

	private CacheClientPool() {
	}

	public static CacheClientPool getInstance() {
		return cacheClientPool;
	}

	public ICacheClient getCacheClient(String nodeName) {
		return (ICacheClient) super.get(nodeName);
	}

	protected Collection getAllCacheClient() {
		return super.values();
	}

}
