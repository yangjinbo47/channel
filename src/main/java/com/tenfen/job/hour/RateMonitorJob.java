package com.tenfen.job.hour;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;
import com.tenfen.entity.operation.open.TOpenOrder;
import com.tenfen.entity.operation.open.TOpenSeller;
import com.tenfen.util.LogUtil;
import com.tenfen.www.common.Constants;
import com.tenfen.www.service.operation.open.OpenOrderManager;
import com.tenfen.www.service.operation.open.OpenSellerManager;
import com.tenfen.www.util.SendWeixinUtil;

public class RateMonitorJob {

	@Autowired
	private OpenOrderManager openOrderManager;
	@Autowired
	private OpenSellerManager openSellerManager;
	
	public void execute() {
		try {
			//获取微信access_token
			String accessToken = SendWeixinUtil.getAccessToken("wxfa15850f2f43a2ea", "KE1P4QXYzKfVWBPeiTBkP-cNt7vALoxvFld7-XqL3MBCV0A352Y7p0xIbjsxpiV3");
			
			List<Integer> openSellerIdList = Arrays.asList(new Integer[]{20,21});//开放给北京的渠道号
//			List<Integer> openSellerIdList = new ArrayList<Integer>();
			
			Calendar calendar = Calendar.getInstance();
			//格式化时间
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			SimpleDateFormat sdfSql = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
			//获取结束时间
			String endString = sdf.format(calendar.getTime());
			Date endDate = sdfSql.parse(endString);
			java.sql.Date end = new java.sql.Date(endDate.getTime());
			
			calendar.add(Calendar.MINUTE, -30);
			//获取当日时间区间
			String startString = sdf.format(calendar.getTime());
			Date startDate = sdfSql.parse(startString);
			java.sql.Date start = new java.sql.Date(startDate.getTime());
			
			List<TOpenSeller> sellerList = openSellerManager.findAllOpenSellerList(Constants.USER_TYPE.ALL.getValue());
			for (TOpenSeller tOpenSeller : sellerList) {
				List<TOpenOrder> orders = openOrderManager.getOrderList(tOpenSeller.getId(), null, start, end);
				
				int orderReq = orders.size();
				int mr = 0;
				int fail = 0;
				for (TOpenOrder tOpenOrder : orders) {
					if ("3".equals(tOpenOrder.getStatus())) {
						mr++;
					} else if ("4".equals(tOpenOrder.getStatus())) {
						fail++;
					}
				}
				int mo = mr+fail;
				
				DecimalFormat df = new DecimalFormat("0.0");//格式化小数，不足的补0
				//转化率
				float f = 0;
				if (mr == 0) {
					f = 0;
				} else {
					f = (float)mr/mo;
				}
				if (f != 0) {
					f = (float)(Math.round(f*1000))/1000;
				}
				//请求转化率
				float reqf = 0;
				if (mr == 0) {
					reqf = 0;
				} else {
					reqf = (float)mr/orderReq;
				}
				if (reqf != 0) {
					reqf = (float)(Math.round(reqf*1000))/1000;
				}
				String reqfString = "0%";
				if (reqf == 0) {
					reqfString = "0%";
				} else {
					reqfString = df.format(reqf*100) + "%";//返回的是String类型的
				}
				
				if (orderReq > 10) {//表明有请求的渠道才需要告警
					//阀值5%
					if (reqf < 0.05) {
						if (openSellerIdList.contains(tOpenSeller.getId())) {
							JSONObject postJson = new JSONObject();
							postJson.put("toparty", "2|3");//北京|杭州
							postJson.put("msgtype", "text");
							postJson.put("agentid", 1);
							JSONObject postTextJson = new JSONObject();
							postTextJson.put("content", tOpenSeller.getName()+"转化率低,目前转化率为："+reqfString);
							postJson.put("text", postTextJson);
							postJson.put("safe", "0");
							
							SendWeixinUtil.sendTextMessage(accessToken, postJson.toString());
//							SendMailUtil.sendHtmlMail(mailLoginName, mailLoginPwd, mailSmtp, mailToListALL, mailTitle, content);
						} else {
							JSONObject postJson = new JSONObject();
							postJson.put("toparty", "2");//杭州
							postJson.put("msgtype", "text");
							postJson.put("agentid", 1);
							JSONObject postTextJson = new JSONObject();
							postTextJson.put("content", tOpenSeller.getName()+"转化率低,目前转化率为："+reqfString);
							postJson.put("text", postTextJson);
							postJson.put("safe", "0");
							
							SendWeixinUtil.sendTextMessage(accessToken, postJson.toString());
						}
					}
				}
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
}
