package com.tenfen.cache.services;

import java.util.Date;

public interface ICacheClient {

	/**
	*@author BOBO
	*@功能：强制放入缓存不管原先是否存在（默认过期时间2小时）
	*@param key
	*@param object
	*@return
	 */
	public boolean setCache(String key, Object object);
	
	/**
	*@author BOBO
	*@功能：强制放入缓存不管原先是否存在
	*@param key
	*@param object
	*@return
	 */
	public boolean setCache(String key, Object object, Integer expiryTime);
	
	/**
	*@author BOBO
	*@功能：放入缓存（默认过期时间30分钟）
	*@param key
	*@param object
	*@return
	 */
	public boolean addCache(String key, Object object);

	/**
	*@author BOBO
	*@功能：放入缓存（单位：秒）
	*@param key
	*@param object
	*@param expiryTime
	*@return
	 */
	public boolean addCache(String key, Object object, Integer expiryTime);

	/**
	*@author BOBO
	*@功能：放入缓存（参数：过期时间）
	*@param key
	*@param object
	*@param expiryDate
	*@return
	 */
	public boolean addCache(String key, Object object, Date expiryDate);

	/**
	*@author BOBO
	*@功能：从缓存中移除对象
	*@param key
	*@return
	 */
	public boolean deleteCache(String key);

	/**
	*@author BOBO
	*@功能：从缓存获取对象
	*@param key
	*@return
	 */
	public Object getCache(String key);

	/**
	*@author BOBO
	*@功能：判断缓存中是否存在对象
	*@param key
	*@return
	 */
	public boolean existsCache(String key);
	
	/**
	*@功能：清除所有缓存
	*@author BOBO
	*@date Apr 11, 2012
	*@return
	 */
	public boolean flushAll();

//	public abstract GetsReturns gets(String s);
//
//	public abstract boolean casOperate(String s, CasObjectOperate casobjectoperate, int i, int j) throws TimeoutException, InterruptedException, Exception;
//
//	public abstract boolean casOperate(String s, CasObjectOperate casobjectoperate, int i) throws TimeoutException, InterruptedException, Exception;
//
//	public abstract void shutDown() throws IOException;
}
