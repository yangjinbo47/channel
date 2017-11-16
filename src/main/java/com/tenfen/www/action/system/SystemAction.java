package com.tenfen.www.action.system;

import org.springframework.beans.factory.annotation.Autowired;

import com.tenfen.bean.system.SystemProperty;
import com.tenfen.cache.CacheFactory;
import com.tenfen.cache.services.ICacheClient;
import com.tenfen.util.Utils;
import com.tenfen.util.servlet.ServletRequestUtils;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.service.operation.open.OpenAppManager;
import com.tenfen.www.service.operation.open.OpenOrderManager;
import com.tenfen.www.service.operation.open.OpenSellerManager;
import com.tenfen.www.service.operation.pack.OrderManager;
import com.tenfen.www.service.operation.pack.PackageChannelManager;
import com.tenfen.www.service.operation.pack.PackageManager;
import com.tenfen.www.service.operation.sms.SmsAppManager;
import com.tenfen.www.service.operation.sms.SmsOrderManager;
import com.tenfen.www.service.operation.sms.SmsSellerManager;
import com.tenfen.www.service.system.VisitLogTmpManager;

public class SystemAction extends SimpleActionSupport {

	private static final long serialVersionUID = 6728866051491427081L;
	
	@Autowired
	private CacheFactory cacheFactory;
	@Autowired
	private OpenSellerManager openSellerManager;
	@Autowired
	private OpenOrderManager openOrderManager;
	@Autowired
	private OpenAppManager openAppManager;
	@Autowired
	private SmsSellerManager smsSellerManager;
	@Autowired
	private SmsOrderManager smsOrderManager;
	@Autowired
	private SmsAppManager smsAppManager;
	@Autowired
	private PackageChannelManager packageChannelManager;
	@Autowired
	private PackageManager packageManager;
	@Autowired
	private SystemProperty systemProperty;
	@Autowired
	private OrderManager orderManager;
	@Autowired
	private VisitLogTmpManager visitLogTmpManager;
	
	/**
	*@功能：清理缓存
	*@author BOBO
	*@date Apr 24, 2012
	*@return
	 */
	public String flushAll() {
		boolean b = false;
		
		try {
			ICacheClient mc = cacheFactory.getCommonCacheClient();
			b = mc.flushAll();
			
			setRequestAttribute("isSucc", b);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return "success";
	}
	
	/**
	*@功能：清理单个缓存
	*@author BOBO
	*@date Apr 24, 2012
	*@return
	 */
	public String flushSingle() {
		String key = ServletRequestUtils.getStringParameter(request, "key", "");
		try {
			if (!Utils.isEmpty(key)) {
				boolean b = false;
				ICacheClient mc = cacheFactory.getCommonCacheClient();
				b = mc.deleteCache(key);
				
				if (b) {
					addActionMessage("缓存"+key+"清理成功");
				} else {
					addActionMessage("缓存"+key+"清理成功");
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return "success";
	}
	
}
