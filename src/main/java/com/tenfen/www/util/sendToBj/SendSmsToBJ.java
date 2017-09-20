package com.tenfen.www.util.sendToBj;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tenfen.entity.operation.sms.TSmsOrder;
import com.tenfen.util.HttpClientUtils;
import com.tenfen.util.LogUtil;
import com.tenfen.util.Utils;
import com.tenfen.www.util.TokenService;
import com.tenfen.www.util.TokenService.TokenParam;

public class SendSmsToBJ implements Runnable{

	private TSmsOrder tSmsOrder;
	
	public SendSmsToBJ(TSmsOrder tSmsOrder) {
		this.tSmsOrder = tSmsOrder;
	}
	
	@Override
	public void run() {
		try {
			String orderId = tSmsOrder.getOrderId();
			Integer sellerId = tSmsOrder.getSellerId();
			Integer appId = tSmsOrder.getAppId();
			
			Integer price = tSmsOrder.getFee();
			String gatewayCode = "sms-"+appId+"-"+price;
			String productCode = "sms-"+sellerId+"-"+appId+"-"+price;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			Date payTime = tSmsOrder.getPayTime();
			if (Utils.isEmpty(payTime)) {
				payTime = new Date();
			}
			String paymentTime = sdf.format(payTime);
			String payPhone = tSmsOrder.getPayPhone();
			String province = tSmsOrder.getProvince();
			
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
			LogUtil.log("发送短代数据orderId:"+orderId+" beijing res:"+res);
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
}
