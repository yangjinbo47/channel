package com.tenfen.www.action.services;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import com.tenfen.entity.system.TVisitLogTmp;
import com.tenfen.util.LogUtil;
import com.tenfen.util.StringUtil;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.common.MSG;
import com.tenfen.www.service.operation.BlackListManager;
import com.tenfen.www.service.system.SystemConfigManager;
import com.tenfen.www.service.system.VisitLogTmpManager;

public class SystemAction extends SimpleActionSupport {

	private static final long serialVersionUID = 9173885858938550927L;
//	private static Log visitLog = LogFactory.getLog("visitLog");

	@Autowired
	private SystemConfigManager systemConfigManager;
	@Autowired
	private BlackListManager blackListManager;
	@Autowired
	private VisitLogTmpManager visitLogTmpManager;
	
	public void isjump() {
		try {
			String jumpYZMJson = systemConfigManager.jumpYZMJson();
			
			StringUtil.printJson(getResponse(), jumpYZMJson);
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
	public void visitlog() {
		String imsi = getStringParam("imsi","");
		String phoneNum = getStringParam("phoneNum", "");
		String channel = getStringParam("channel","");
		String province = getStringParam("province", "");
		String ua = getStringParam("ua","");
		
		try {
//			StringBuilder sb = new StringBuilder();
//			sb.append(imsi);
//			sb.append("||").append(phoneNum);
//			sb.append("||").append(channel);
//			sb.append("||").append(province);
//			sb.append("||").append(ua);
//			sb.append("||").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
//			visitLog.info(sb.toString());
			
			TVisitLogTmp tVisitLogTmp = new TVisitLogTmp();
			tVisitLogTmp.setImsi(imsi);
			tVisitLogTmp.setPhoneNum(phoneNum);
			tVisitLogTmp.setClientVersion(channel);
			tVisitLogTmp.setProvince(province);
			tVisitLogTmp.setUserAgent(ua);
			tVisitLogTmp.setVisitTime(new Date());
			visitLogTmpManager.save(tVisitLogTmp);
			StringUtil.printJson(response, MSG.success("保存日志成功"));
		} catch (Exception e) {
			StringUtil.printJson(response, MSG.exception("保存日志异常"));
		}
	}
	
	public void checkPhoneIsBlack() {
		String phoneNum = getStringParam("phoneNum", "");
		boolean isExist = blackListManager.isBlackList(phoneNum);
		if (isExist) {
			StringUtil.printJson(response, MSG.success("黑名单用户"));
		} else {
			StringUtil.printJson(response, MSG.failure("非黑名单用户"));
		}
	}

}
