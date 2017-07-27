package com.tenfen.www.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.tenfen.util.HttpClientUtils;
import com.tenfen.util.LogUtil;

public class SendToBJ {
	
	public static String sendOrder(String merid, String mername, String orderId, String appName, String createTime, String fee, String status, String payTime, String imsi, String type) {
		try {
			Map<String, String> map = new HashMap<String, String>();
			map.put("merid", merid);
			map.put("mername", mername);
			map.put("orderid",orderId);
			map.put("appname",appName);
			map.put("createtime", createTime);
			map.put("fee",fee);
			map.put("status", status);
			map.put("payTime", payTime);
			map.put("imsi",imsi);
			map.put("type", type);
			HttpClientUtils.simplePostInvoke("http://api.slxz.com.cn/charge-platform/client/import_byb_data.php", map);
		} catch (Exception e) {
			LogUtil.log(e.getMessage(), e);
		}
		return null;
	}
	
	public static void main(String[] args) {
		String orderId = String.valueOf(System.currentTimeMillis());
		String appName = "测试";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String createTime = sdf.format(new Date());
		String fee = "100";
		String status = "3";
		String payTime= sdf.format(new Date());
		String imsi = "460036970087302";
		String type = "2";
//		SendToBJ.sendOrder(orderId, appName, createTime, fee, status, payTime, imsi, type);
	}

}
