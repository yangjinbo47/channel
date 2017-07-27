package com.tenfen.cache.modules;

import java.util.List;

import net.rubyeye.xmemcached.MemcachedSessionLocator;
import net.rubyeye.xmemcached.impl.LibmemcachedMemcachedSessionLocator;

public class MemcachedInstanceBean {

	private String cachedNode;
	private List<String> serverList;
	private MemcachedSessionLocator sessionLocator;
	private boolean isCompress;
	private int compressBytes;
	private int connectPool;
	private boolean isStandy;

	public MemcachedInstanceBean() {
		sessionLocator = new LibmemcachedMemcachedSessionLocator();
		isCompress = false;
		compressBytes = 2 * 1024 * 1024 * 1024;
		connectPool = 1;
		isStandy = false;
	}

	public boolean isStandy() {
		return isStandy;
	}

	public void setStandy(boolean isStandy) {
		this.isStandy = isStandy;
	}

	public String getCachedNode() {
		return cachedNode;
	}

	public void setCachedNode(String cachedNode) {
		this.cachedNode = cachedNode;
	}

	public List<String> getServerList() {
		return serverList;
	}

	public void setServerList(List<String> serverList) {
		this.serverList = serverList;
	}

	public MemcachedSessionLocator getSessionLocator() {
		return sessionLocator;
	}

	public void setSessionLocator(MemcachedSessionLocator sessionLocator) {
		this.sessionLocator = sessionLocator;
	}

	public boolean isCompress() {
		return isCompress;
	}

	public void setCompress(boolean isCompress) {
		this.isCompress = isCompress;
	}

	public int getCompressBytes() {
		return compressBytes;
	}

	public void setCompressBytes(int compressBytes) {
		this.compressBytes = compressBytes;
	}

	public int getConnectPool() {
		return connectPool;
	}

	public void setConnectPool(int connectPool) {
		this.connectPool = connectPool;
	}

}
