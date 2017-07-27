package com.tenfen.cache.provider;

import com.tenfen.cache.services.ICacheClient;

public interface IMemcacheProvider {

	public ICacheClient getContentCacheClient();

	public ICacheClient getProductCacheClient();

	public ICacheClient getCommonCacheClient();
	
	public ICacheClient getCacheClient(String nodeName);

}
