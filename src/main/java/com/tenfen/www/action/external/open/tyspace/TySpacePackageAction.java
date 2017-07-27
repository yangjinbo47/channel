package com.tenfen.www.action.external.open.tyspace;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;
import com.tenfen.entity.operation.TMobileArea;
import com.tenfen.entity.operation.open.TOpenApp;
import com.tenfen.entity.operation.open.TOpenMerchant;
import com.tenfen.entity.operation.open.TOpenOrder;
import com.tenfen.entity.operation.open.TOpenProductInfo;
import com.tenfen.entity.operation.open.TOpenSeller;
import com.tenfen.entity.operation.open.TOpenSellerApps;
import com.tenfen.util.DateUtil;
import com.tenfen.util.HttpClientUtils;
import com.tenfen.util.LogUtil;
import com.tenfen.util.StringUtil;
import com.tenfen.util.Utils;
import com.tenfen.util.servlet.ServletRequestUtils;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.service.operation.MobileAreaManager;
import com.tenfen.www.service.operation.open.OpenAppManager;
import com.tenfen.www.service.operation.open.OpenMerchantManager;
import com.tenfen.www.service.operation.open.OpenOrderManager;
import com.tenfen.www.service.operation.open.OpenSellerManager;
import com.tenfen.www.service.system.ImsiMdnRelationManager;
import com.tenfen.www.util.SendToBJ;
import com.tenfen.www.util.TokenService;
import com.tenfen.www.util.TokenService.TokenParam;

/**
 * 天翼空间包月
 * @author BOBO
 *
 */
public class TySpacePackageAction extends SimpleActionSupport {
	
	private static final long serialVersionUID = 8112913793580559503L;
	
	@Autowired
	private OpenSellerManager openSellerManager;
	@Autowired
	private OpenOrderManager openOrderManager;
	@Autowired
	private OpenAppManager openAppManager;
	@Autowired
	private ImsiMdnRelationManager imsiMdnRelationManager;
	@Autowired
	private MobileAreaManager mobileAreaManager;
	@Autowired
	private OpenMerchantManager openMerchantManager;
	
	public void generateSubscribeOrder() {
		String sellerKey = ServletRequestUtils.getStringParameter(request, "seller_key", null);
		String imsi = ServletRequestUtils.getStringParameter(request, "imsi", null);
		String appName = ServletRequestUtils.getStringParameter(request, "app_name", null);
		int fee = ServletRequestUtils.getIntParameter(request, "fee", 0);
		String outTradeNo = ServletRequestUtils.getStringParameter(request, "out_trade_no", null);
		String sign = ServletRequestUtils.getStringParameter(request, "sign", null);
		
		//返回响应obj
		JSONObject returnJson = new JSONObject();
		try {
			LogUtil.log("天翼空间包月参数: seller_key:"+sellerKey+" imsi:"+imsi+" appName:"+appName+" fee:"+fee+" outTradeNo:"+outTradeNo+" sign:"+sign);
			if (Utils.isEmpty(sellerKey)) {
				returnJson.put("code", "1001");
				returnJson.put("msg", "seller_key参数不能为空");
				StringUtil.printJson(response, returnJson.toString());
				return;
			} else if (Utils.isEmpty(appName)) {
				returnJson.put("code", "1002");
				returnJson.put("msg", "app_name参数不能为空");
				StringUtil.printJson(response, returnJson.toString());
				return;
			} else if (fee == 0) {
				returnJson.put("code", "1003");
				returnJson.put("msg", "fee参数不能为空");
				StringUtil.printJson(response, returnJson.toString());
				return;
			} else if (Utils.isEmpty(imsi)) {
				returnJson.put("code", "1004");
				returnJson.put("msg", "imsi参数不能为空");
				StringUtil.printJson(response, returnJson.toString());
				return;
			} else if (Utils.isEmpty(sign)) {
				returnJson.put("code", "1005");
				returnJson.put("msg", "sign参数不能为空");
				StringUtil.printJson(response, returnJson.toString());
				return;
			} else if (Utils.isEmpty(outTradeNo)) {
				returnJson.put("code", "1006");
				returnJson.put("msg", "out_trade_no参数不能为空");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			
			//通过sellerKey查询渠道信息
			TOpenSeller tOpenSeller = openSellerManager.getOpenSellerByProperty("sellerKey", sellerKey);
			if (Utils.isEmpty(tOpenSeller)) {
				returnJson.put("code", "1007");
				returnJson.put("msg", "没有找到渠道相关信息");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			if (tOpenSeller.getStatus() == 0) {
				returnJson.put("code", "1008");
				returnJson.put("msg", "该渠道已被关闭，请联系管理员");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			//校验sign
			List<TokenParam> queryParamList = new ArrayList<TokenParam>(4);
			queryParamList.add(new TokenParam("seller_key",sellerKey));
			queryParamList.add(new TokenParam("imsi",imsi));
			queryParamList.add(new TokenParam("app_name",appName));
			queryParamList.add(new TokenParam("fee",fee+""));
			queryParamList.add(new TokenParam("out_trade_no", outTradeNo));
			String geneSign = TokenService.buildToken(queryParamList, tOpenSeller.getSellerSecret());
			if (!sign.toLowerCase().equals(geneSign)) {
				returnJson.put("code", "1009");
				returnJson.put("msg", "消息签名不正确");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			
			//查询关联app
			boolean b = false;//检测应用是否全部达到限量值
			String appNameDecode = URLDecoder.decode(appName, "UTF-8");
			List<TOpenSellerApps> openSellerAppList = tOpenSeller.getSellerApps();
			TOpenApp tOpenApp = null;
			String packageName = null;
			if (openSellerAppList.size() > 0) {
				for (TOpenSellerApps tOpenSellerApps : openSellerAppList) {
					Integer appLimit = tOpenSellerApps.getAppLimit();
					Integer appToday = tOpenSellerApps.getAppToday();
					if (appLimit != -1) {
						if (appToday >= appLimit) {
							continue;
						}
					}
					tOpenApp = tOpenSellerApps.getOpenApp();
					b = true;
					List<TOpenProductInfo> proList = tOpenApp.getProductList();
					for (TOpenProductInfo tOpenProductInfo : proList) {
						if(fee == tOpenProductInfo.getPrice()) {
							packageName = tOpenProductInfo.getName();
							break;
						}
					}
				}
			} else {
				returnJson.put("code", "1010");
				returnJson.put("msg", "没有找到相关app信息");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			if (!b) {
				returnJson.put("code", "1012");
				returnJson.put("msg", "已达到今日限量值");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			
			//查询app对应的channelId和key,及创建订单所需参数
			String timestamp = String.valueOf(System.currentTimeMillis());
			String key = tOpenApp.getAppKey();
			String secret = tOpenApp.getAppSecret();
//			String callBackUrl = tOpenApp.getCallbackUrl();
			Integer appId = tOpenApp.getId();
			Integer merchantId = tOpenApp.getMerchantId();
			Integer sellerId = tOpenSeller.getId();
			//业务参数
			String chargeType = "2";
			String orderId = DateUtil.getCurrentTimestamp("yyyyMMddHHmmssSSS") + Math.round(Math.random() * 1000);
			
			TOpenMerchant tOpenMerchant = openMerchantManager.get(merchantId);
			if (Utils.isEmpty(tOpenMerchant)) {
				returnJson.put("code", "1011");
				returnJson.put("msg", "没有找到相关商户信息");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			String merchantName = tOpenMerchant.getMerchantName();
			
			new Thread(new CallJD(orderId, key, imsi, merchantName, appNameDecode, packageName, timestamp, fee, chargeType, secret, appId, merchantId, sellerId, outTradeNo)).start();
			returnJson.put("code", "1");
			returnJson.put("msg", "提交成功");
			returnJson .put("orderId", orderId);
			StringUtil.printJson(response, returnJson.toString());
			return;
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
			returnJson.put("code", "9999");
			returnJson.put("msg", "系统未知异常");
			StringUtil.printJson(response, returnJson.toString());
			return;
		}
	}
	
	private class CallJD implements Runnable {
		private String orderId;
		private String channel;
		private String imsi;
		private String merchantName;
		private String appNameDecode;
		private String productName;
		private String timestamp;
		private Integer price;
		private String chargeType;
		private String secret;
		private Integer sellerId;
		private Integer appId;
		private Integer merchantId;
		private String outTradeNo;
		
		public CallJD(String orderId, String channel, String imsi, String merchantName, String appNameDecode,String productName,String timestamp,
				Integer price, String chargeType, String secret, Integer appId, Integer merchantId, Integer sellerId, String outTradeNo) {
			this.orderId = orderId;
			this.channel = channel;
			this.imsi = imsi;
			this.merchantName = merchantName;
			this.appNameDecode = appNameDecode;
			this.productName = productName;
			this.timestamp = timestamp;
			this.price = price;
			this.chargeType = chargeType;
			this.secret = secret;
			this.appId = appId;
			this.merchantId = merchantId;
			this.sellerId = sellerId;
			this.outTradeNo = outTradeNo;
		}
		
		@Override
		public void run() {
			try {
				JSONObject returnJson = new JSONObject();
				returnJson.put("order_no", orderId);
				returnJson.put("out_trade_no", outTradeNo);
				returnJson.put("fee", price);
				
				//调用长城sso支付接口
				String res = ssoPay(orderId, channel, imsi, merchantName, appNameDecode, productName, timestamp, String.valueOf(price), chargeType, secret);
				LogUtil.log("CallTySpaceZYPackageResp:"+res);
				String code = null;
				String message = null;
				if (!Utils.isEmpty(res)) {
					JSONObject jsonObject = JSONObject.parseObject(res);
					code = jsonObject.getString("resultCode");//订单创建结果 0-成功 1-失败
					message = jsonObject.getString("resultDesc");
				}
				
				TOpenOrder tOpenOrder = new TOpenOrder();
				tOpenOrder.setImsi(imsi);
				tOpenOrder.setOrderId(orderId);
				tOpenOrder.setOutTradeNo(outTradeNo);
				tOpenOrder.setSellerId(sellerId);
				tOpenOrder.setAppId(appId);
				tOpenOrder.setMerchantId(merchantId);
				tOpenOrder.setSubject(appNameDecode);
				tOpenOrder.setFee(price);
				tOpenOrder.setSenderNumber("");
				tOpenOrder.setMsgContent("");
				if ("0".equals(code)) {
					tOpenOrder.setStatus("1");//此处返回的成功不作为判断依据
					returnJson.put("code", "3");
					returnJson.put("msg", "成功");
					
					//增加今日量
					openSellerManager.saveOpenSellerApps(sellerId, appId, price);
				} else {
					tOpenOrder.setStatus("4");
					returnJson.put("code", "4");
					returnJson.put("msg", message);
				}
				String phone = imsiMdnRelationManager.getPhone(imsi);
				if (!Utils.isEmpty(phone)) {
					TMobileArea mobileArea = mobileAreaManager.getMobileArea(phone);
					if (!Utils.isEmpty(mobileArea)) {
						tOpenOrder.setProvince(mobileArea.getProvince());
					}
					tOpenOrder.setPayPhone(phone);
					tOpenOrder.setPayTime(new Date());
				} else {
					tOpenOrder.setPayPhone(imsi);
					tOpenOrder.setPayTime(new Date());
				}
				
				openOrderManager.save(tOpenOrder);
				
		        LogUtil.log("TySpaceJDPackageMsg:"+returnJson.toString());
//		        if (!Utils.isEmpty(url)) {
//		        	HttpClientUtils.postJson(url, returnJson.toString());
//				}
		        
		        //调用北京平台接口
		        TOpenSeller tOpenSeller = openSellerManager.get(tOpenOrder.getSellerId());
		        String merName = tOpenSeller.getName();
		        SendToBJ.sendOrder(String.valueOf(tOpenOrder.getSellerId()), merName, outTradeNo, appNameDecode, tOpenOrder.getCreateTimeString(), String.valueOf(price), tOpenOrder.getStatus(), tOpenOrder.getPayTimeString(), imsi, "2");
			} catch (Exception e) {
				LogUtil.error(e.getMessage(), e);
			}
		}
		
		/**
		 * 调用长虹创建EMP订单接口
		 * @param orderId
		 * @return
		 */
		private String ssoPay(String orderId, String channel, String imsi, String merchantName, String appName, String productName,
				String timestamp, String price, String chargeType, String secret) {
			String responseString = null;
			try {
				List<TokenParam> queryParamList = new ArrayList<TokenParam>();
				queryParamList.add(new TokenParam("ver", "1.0"));
				queryParamList.add(new TokenParam("channel",channel));
				queryParamList.add(new TokenParam("imsi",imsi));
				queryParamList.add(new TokenParam("apName",merchantName));
				queryParamList.add(new TokenParam("appName",appName));
				queryParamList.add(new TokenParam("chargeName",productName));
				queryParamList.add(new TokenParam("price",price));
				queryParamList.add(new TokenParam("chargeType",chargeType));
				queryParamList.add(new TokenParam("timestamp", timestamp));
				queryParamList.add(new TokenParam("orderId", orderId));
				String sig = TokenService.buildTySpaceToken(queryParamList, secret);
				
				Map<String, String> map = new HashMap<String, String>();
				map.put("ver","1.0");
				map.put("channel",channel);
				map.put("imsi",imsi);
				map.put("apName",merchantName);
				map.put("appName",appName);
				map.put("chargeName",productName);
				map.put("price",price);
				map.put("chargeType",chargeType);
				map.put("timestamp",timestamp);
				map.put("orderId",orderId);
				map.put("sig",sig);
				responseString = HttpClientUtils.simplePostInvoke("http://123.57.23.150:8002/HttpAPI", map);
			} catch (Exception e) {
				LogUtil.error(e.getMessage(), e);
			}
			return responseString;
		}
	}
	
}
