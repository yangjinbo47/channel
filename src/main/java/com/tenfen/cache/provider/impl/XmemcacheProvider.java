package com.tenfen.cache.provider.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.MemcachedSessionLocator;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.command.BinaryCommandFactory;
import net.rubyeye.xmemcached.utils.AddrUtil;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tenfen.cache.modules.CacheClientPool;
import com.tenfen.cache.modules.MemcachedInstanceBean;
import com.tenfen.cache.provider.IMemcacheProvider;
import com.tenfen.cache.services.ICacheClient;
import com.tenfen.cache.services.impl.XMemcacheClient;

public class XmemcacheProvider implements IMemcacheProvider {

	private static final Logger logger = LoggerFactory.getLogger(XmemcacheProvider.class);
	
	// WAP通用缓存
	private static ICacheClient commonCacheClient;
	private static ReentrantLock commonLock = new ReentrantLock();

	public XmemcacheProvider() {
		this.initCacheClientPool();
		System.setProperty("log4j.appender.net.rubyeye.xmemcached", "ERROR");
		System.setProperty("log4j.appender.com.google.code.yanf4j", "ERROR");
	}

	/**
	*@author BOBO
	*@功能：初始化连接池
	 */
	@SuppressWarnings("unchecked")
	private void initCacheClientPool(){
		try {
			List<MemcachedInstanceBean> instanceBeans = parseXml();
			CacheClientPool cacheClientPool = CacheClientPool.getInstance();
			MemcachedInstanceBean memcachedInstanceBean;
			for (Iterator iterator = instanceBeans.iterator(); iterator.hasNext(); logger.warn((new StringBuilder("Add a cacheClient in pool:")).append(memcachedInstanceBean.getCachedNode()).toString())) {
				memcachedInstanceBean = (MemcachedInstanceBean) iterator.next();
				StringBuffer servers = new StringBuffer();
				String server;
				for (Iterator iterator1 = memcachedInstanceBean.getServerList().iterator(); iterator1.hasNext(); servers.append(server).append(" "))
					server = (String) iterator1.next();
				
				servers.deleteCharAt(servers.length() - 1);
				MemcachedClientBuilder builder;
				if (memcachedInstanceBean.isStandy()) {
					builder = new XMemcachedClientBuilder(AddrUtil.getAddressMap(servers.toString()));
					builder.setFailureMode(true);
				} else {
					builder = new XMemcachedClientBuilder(AddrUtil.getAddresses(servers.toString()));
				}
				builder.setCommandFactory(new BinaryCommandFactory());
				builder.setConnectionPoolSize(memcachedInstanceBean.getConnectPool());
				MemcachedClient memcachedClient = builder.build();
				memcachedClient.getTranscoder().setCompressionThreshold(memcachedInstanceBean.getCompressBytes());
				ICacheClient cacheClient = new XMemcacheClient(memcachedClient);
				cacheClientPool.put(memcachedInstanceBean.getCachedNode(), cacheClient);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	/**
	*@author BOBO
	*@功能：解析memcached配置文件
	*@return
	 */
	@SuppressWarnings("unchecked")
	private List<MemcachedInstanceBean> parseXml(){
		List<MemcachedInstanceBean> instanceBeans = new ArrayList<MemcachedInstanceBean>();
		try {
			String separator = File.separator;
			String configFile = System.getenv("JBOSS_HOME") + separator + "server" + separator + "default" + separator + "conf" + separator + "memcached.xml";

			SAXBuilder builder = new SAXBuilder(false);
			Document doc = builder.build(new File(configFile));
			Element root = doc.getRootElement();
			
			List<Element> cachedNodes = XPath.selectNodes(root, "/memcached/cachedNodes/cachedNode");
			MemcachedInstanceBean bean;
			for (Element nodeElement : cachedNodes) {
				bean = new MemcachedInstanceBean();
				bean.setCachedNode(nodeElement.getAttributeValue("name"));
				logger.info("添加缓存："+nodeElement.getAttributeValue("name"));
				List<String> serverList = new ArrayList();
				List<Element> xmlServList = XPath.selectNodes(nodeElement, "./serverList/server");
				for (Element servElement : xmlServList) {
					serverList.add(servElement.getText());
					logger.info("添加缓存服务器："+servElement.getText());
				}
				bean.setServerList(serverList);
				
				if ("true".equals(nodeElement.getChildTextTrim("isStandy")))
					bean.setStandy(true);
				if ("true".equals(nodeElement.getChildTextTrim("isCompress"))) {
					bean.setCompress(true);
					bean.setCompressBytes(Integer.valueOf(nodeElement.getChildTextTrim("compressBytes")).intValue());
				}
				if (nodeElement.getChildText("sessionLocator") != null)
					bean.setSessionLocator((MemcachedSessionLocator) Thread.currentThread().getContextClassLoader().loadClass(nodeElement.getChildTextTrim("sessionLocator")).newInstance());
				if (nodeElement.getChildText("connectPool") != null)
					bean.setConnectPool(Integer.valueOf(nodeElement.getChildTextTrim("connectPool")).intValue());
				
				instanceBeans.add(bean);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return instanceBeans;
	}

	@Override
	public ICacheClient getCommonCacheClient() {
		if (commonCacheClient == null) {
			commonLock.lock();
			try {
				commonCacheClient = getCacheClient("common");
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
			}
			commonLock.unlock();
		}
		return commonCacheClient;
	}

	@Override
	public ICacheClient getContentCacheClient() {
		return null;
	}

	@Override
	public ICacheClient getProductCacheClient() {
		return null;
	}
	
	@Override
	public ICacheClient getCacheClient(String nodeName) {
		return CacheClientPool.getInstance().getCacheClient(nodeName);
	}

}
