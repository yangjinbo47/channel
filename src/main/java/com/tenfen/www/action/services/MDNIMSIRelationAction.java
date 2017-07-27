package com.tenfen.www.action.services;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import com.tenfen.entity.system.ImsiMdnRelation;
import com.tenfen.util.CTUtil;
import com.tenfen.util.LogUtil;
import com.tenfen.util.RegExp;
import com.tenfen.util.StringUtil;
import com.tenfen.util.Utils;
import com.tenfen.util.json.JSONObject;
import com.tenfen.util.servlet.ServletRequestUtils;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.service.system.ImsiMdnRelationManager;

public class MDNIMSIRelationAction extends SimpleActionSupport{

	private static final long serialVersionUID = -7838742375073569223L;
	
	@Autowired
	private ImsiMdnRelationManager imsiMdnRelationManager;
	
	public void queryPhoneByIMSI() {
		try {
			String imsi = ServletRequestUtils.getStringParameter(request, "imsi", null);
			JSONObject json = new JSONObject();
			if (Utils.isEmpty(imsi)) {
				json.put("phoneNum", "");
				StringUtil.printJson(getResponse(), json.toString());
				return;
			}
			//从本地库中获取
			ImsiMdnRelation imsiMdnRelation = imsiMdnRelationManager.getEntityByProperty("imsi", imsi);
			if (imsiMdnRelation != null) {
				json.put("phoneNum", imsiMdnRelation.getPhoneNum());
				StringUtil.printJson(getResponse(), json.toString());
				return;
			} else {//库里没匹配上
				new Thread(new SearchThread(imsiMdnRelationManager, imsi)).start();
				
				json.put("phoneNum", "");
				StringUtil.printJson(getResponse(), json.toString());
				return;
			}
			
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
	
	private class SearchThread implements Runnable {

		private ImsiMdnRelationManager imsiMdnRelationManager;
		private String imsi;
		
		public SearchThread(ImsiMdnRelationManager imsiMdnRelationManager,String imsi){
			this.imsiMdnRelationManager = imsiMdnRelationManager;
			this.imsi = imsi;
		}
		
		@Override
		public void run() {
			try {				
				String deviceNo = "3500000000404101";
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String timeStamp = sdf.format(new Date());
				String ency = deviceNo + imsi + timeStamp + "tyyd";
				String authenticator = CTUtil.getAuthenicator(ency);
				//获取手机号码
				String result = CTUtil.QueryMDNByIMSI(deviceNo, imsi, timeStamp, authenticator, "tyyd");
				LogUtil.log("结果："+result);
				String phone = RegExp.getString(result, "(?<=<MDN>).*(?=</MDN>)");
				if (phone.length() > 0) {
					phone = phone.trim();
				}
				
				if (!Utils.isEmpty(phone)) {
					//保存入本地库
					ImsiMdnRelation imsiMdnRelation = new ImsiMdnRelation();
					imsiMdnRelation.setImsi(imsi);
					imsiMdnRelation.setPhoneNum(phone);
					imsiMdnRelationManager.save(imsiMdnRelation);
				}
			} catch (Exception e) {
				LogUtil.error(e.getMessage(), e);
			}
		}
		
	}
	
}
