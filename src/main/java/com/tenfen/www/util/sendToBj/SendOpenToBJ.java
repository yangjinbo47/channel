package com.tenfen.www.util.sendToBj;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tenfen.entity.operation.open.TOpenOrder;
import com.tenfen.util.HttpClientUtils;
import com.tenfen.util.LogUtil;
import com.tenfen.util.Utils;
import com.tenfen.www.util.TokenService;
import com.tenfen.www.util.TokenService.TokenParam;

public class SendOpenToBJ implements Runnable{

	private static Log visitLog = LogFactory.getLog("visitLog");
	
	private TOpenOrder tOpenOrder;
	
	public SendOpenToBJ(TOpenOrder tOpenOrder) {
		this.tOpenOrder = tOpenOrder;
	}
	
	@Override
	public void run() {
		try {
			String orderId = tOpenOrder.getOrderId();
			Integer sellerId = tOpenOrder.getSellerId();
			Integer appId = tOpenOrder.getAppId();
			
			Integer price = tOpenOrder.getFee();
			String gatewayCode = appId+"-"+price;
			String productCode = sellerId+"-"+appId+"-"+price;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			Date payTime = tOpenOrder.getPayTime();
			if (Utils.isEmpty(payTime)) {
				payTime = new Date();
			}
			String paymentTime = sdf.format(payTime);
			String payPhone = tOpenOrder.getPayPhone();
			String province = tOpenOrder.getProvince();
			
			List<TokenParam> queryParamList = new ArrayList<TokenParam>();
			queryParamList.add(new TokenParam("gatewayCode",gatewayCode));
			queryParamList.add(new TokenParam("orderId",orderId));
			queryParamList.add(new TokenParam("paymentTime",paymentTime));
			queryParamList.add(new TokenParam("productCode",productCode));
			queryParamList.add(new TokenParam("province", province));
			queryParamList.add(new TokenParam("userId", payPhone));
			
			String sign = TokenService.buildToken(queryParamList, "10fen#$0sign!key");
			
			Map<String, String> map = new HashMap<String, String>();
			map.put("gatewayCode",gatewayCode);
			map.put("orderId",orderId);
			map.put("paymentTime",paymentTime);
			map.put("productCode",productCode);
			map.put("province", province);
			map.put("userId", payPhone);
			map.put("sign", sign);
			String res = HttpClientUtils.simpleGetInvoke("http://fin.tenfen.com/tdmpbi/prototype/1", map);
			visitLog.info("发送能力开放数据orderId:"+orderId+" beijing res:"+res);
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
//	public static String sendOrder(String merid, String mername, String orderId, String appName, String createTime, String fee, String status, String payTime, String imsi, String type) {
//		try {
//			Map<String, String> map = new HashMap<String, String>();
//			map.put("merid", merid);
//			map.put("mername", mername);
//			map.put("orderid",orderId);
//			map.put("appname",appName);
//			map.put("createtime", createTime);
//			map.put("fee",fee);
//			map.put("status", status);
//			map.put("payTime", payTime);
//			map.put("imsi",imsi);
//			map.put("type", type);
//			HttpClientUtils.simplePostInvoke("http://api.slxz.com.cn/charge-platform/client/import_byb_data.php", map);
//		} catch (Exception e) {
//			LogUtil.log(e.getMessage(), e);
//		}
//		return null;
//	}
	
}
