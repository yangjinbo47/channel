package com.tenfen.www.action.external.sms;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;
import com.tenfen.entity.operation.sms.TSmsApp;
import com.tenfen.entity.operation.sms.TSmsOrder;
import com.tenfen.entity.operation.sms.TSmsProductInfo;
import com.tenfen.entity.operation.sms.TSmsSeller;
import com.tenfen.entity.operation.sms.TSmsSellerApps;
import com.tenfen.util.DateUtil;
import com.tenfen.util.LogUtil;
import com.tenfen.util.StringUtil;
import com.tenfen.util.Utils;
import com.tenfen.util.servlet.ServletRequestUtils;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.common.Constants;
import com.tenfen.www.service.operation.MobileAreaManager;
import com.tenfen.www.service.operation.sms.SmsAppManager;
import com.tenfen.www.service.operation.sms.SmsOrderManager;
import com.tenfen.www.service.operation.sms.SmsSellerManager;
import com.tenfen.www.util.TokenService;
import com.tenfen.www.util.TokenService.TokenParam;

/**
 * 电信全网短信
 * @author BOBO
 *
 */
public class SmsPayAction extends SimpleActionSupport {
	
	private static final long serialVersionUID = 8112913793580559503L;
	
	@Autowired
	private SmsSellerManager smsSellerManager;
	@Autowired
	private SmsOrderManager smsOrderManager;
	@Autowired
	private SmsAppManager smsAppManager;
	@Autowired
	private MobileAreaManager mobileAreaManager;
	
	/**
	 * 生成包月订单action
	 * app_name需要申报
	 * 计费点需要申报，事先录入产品列表
	 * 需要手机号，一个imsi本月内只能生成一张订单
	 */
	public void generatePackageOrder() {
		String sellerKey = ServletRequestUtils.getStringParameter(request, "seller_key", null);
		String appName = ServletRequestUtils.getStringParameter(request, "app_name", null);
		String imsi = ServletRequestUtils.getStringParameter(request, "imsi", null);
		int fee = ServletRequestUtils.getIntParameter(request, "fee", 0);
		String outTradeNo = ServletRequestUtils.getStringParameter(request, "out_trade_no", null);
		String sign = ServletRequestUtils.getStringParameter(request, "sign", null);
		
		//返回响应obj
		JSONObject returnJson = new JSONObject();
		try {
			LogUtil.log("sms全网短信参数: seller_key:"+sellerKey+" imsi:"+imsi+" appName:"+appName+" fee:"+fee+" outTradeNo:"+outTradeNo+" sign:"+sign);
			
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
			
			TSmsSeller tSmsSeller = smsSellerManager.getSmsSellerByProperty("sellerKey", sellerKey);
			if (Utils.isEmpty(tSmsSeller)) {
				returnJson.put("code", "1007");
				returnJson.put("msg", "没有找到渠道相关信息");
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
			String geneSign = TokenService.buildToken(queryParamList, tSmsSeller.getSellerSecret());
			if (!sign.equals(geneSign)) {
				returnJson.put("code", "1008");
				returnJson.put("msg", "消息签名不正确");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			//查询关联app
			boolean b = false;//检测应用是否全部达到限量值
			String appNameDecode = URLDecoder.decode(appName, "UTF-8");
			List<TSmsSellerApps> tSmsSellerAppsList = tSmsSeller.getSellerApps();
//			TSmsApp tSmsApp = null;
//			for (TSmsSellerApps tsmsSellerApps : tSmsSellerAppsList) {
//				TSmsApp tSmsAppTmp = tsmsSellerApps.getSmsApp();
//				if(appNameDecode.equals(tSmsAppTmp.getName()))
//					tSmsApp = tSmsAppTmp;
//			}
//			if (Utils.isEmpty(tSmsApp)) {
//				returnJson.put("code", "1009");
//				returnJson.put("msg", "没有找到相关app信息");
//				StringUtil.printJson(response, returnJson.toString());
//				return;
//			}
			TSmsApp tSmsApp = null;
			if (tSmsSellerAppsList.size() > 0) {
				for (TSmsSellerApps tSmsSellerApps : tSmsSellerAppsList) {
					Integer appLimit = tSmsSellerApps.getAppLimit();
					Integer appToday = tSmsSellerApps.getAppToday();
					if (appLimit != -1) {
						if (appToday >= appLimit) {
							continue;
						}
					}
					tSmsApp = tSmsSellerApps.getSmsApp();
					b = true;
				}
			} else {
				returnJson.put("code", "1009");
				returnJson.put("msg", "没有找到相关app信息");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			if (!b) {
				returnJson.put("code", "1010");
				returnJson.put("msg", "已达到今日限量值");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			//查询app对应的channelId和key,及创建订单所需参数
			Integer appId = tSmsApp.getId();
			Integer merchantId = tSmsApp.getMerchantId();
			Integer sellerId = tSmsSeller.getId();
			
			//查询fee所对应的产品
//			String productId = null;
			String sendNumber = null;
			String instruction = null;
			List<TSmsProductInfo> proList = tSmsApp.getProductList();
			for (TSmsProductInfo tSmsProductInfo : proList) {
				if(fee == tSmsProductInfo.getPrice() && tSmsProductInfo.getType() == 2) {
//					productId = tSmsProductInfo.getProductId();
					sendNumber = tSmsProductInfo.getSendNumber();
					instruction = tSmsProductInfo.getInstruction();
				}
			}
//			if (Utils.isEmpty(productId)) {
//				returnJson.put("code", "1011");
//				returnJson.put("msg", "没有找到相关计费点");
//				StringUtil.printJson(response, returnJson.toString());
//				return;
//			}
			
			//订单号
			String orderSeq = DateUtil.getCurrentTimestamp("yyyyMMddHHmmssSSS") + Math.round(Math.random() * 1000);
			
			TSmsOrder tSmsOrder = new TSmsOrder();
			tSmsOrder.setOrderId(orderSeq);
			tSmsOrder.setOutTradeNo(outTradeNo);
			tSmsOrder.setImsi(imsi);
			tSmsOrder.setAppId(appId);
			tSmsOrder.setMerchantId(merchantId);
			tSmsOrder.setSellerId(sellerId);
			tSmsOrder.setSubject(appNameDecode);
			tSmsOrder.setSenderNumber(sendNumber);
			tSmsOrder.setMsgContent(instruction);
			tSmsOrder.setProductType(Constants.T_SMS_ORDER_PRODUCT_TYPE.PACKAGE.getValue());
			tSmsOrder.setFee(fee);
//			tSmsOrder.setProductId(productId);
			smsOrderManager.save(tSmsOrder);
			
			returnJson.put("code", "1");
			returnJson.put("msg", "订单创建成功");
			returnJson.put("order_id", orderSeq);
			returnJson.put("out_trade_no", outTradeNo);
			returnJson.put("sendNumber", sendNumber);
			returnJson.put("instruction", instruction+"#"+orderSeq);
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
	
	/**
	 * 生成点播订单action
	 * app_name需要申报
	 * 计费点需要申报，事先录入产品列表
	 */
	public void generateOrder() {
		String sellerKey = ServletRequestUtils.getStringParameter(request, "seller_key", null);
		String appName = ServletRequestUtils.getStringParameter(request, "app_name", null);
		String imsi = ServletRequestUtils.getStringParameter(request, "imsi", null);
		int fee = ServletRequestUtils.getIntParameter(request, "fee", 0);
		String outTradeNo = ServletRequestUtils.getStringParameter(request, "out_trade_no", null);
		String sign = ServletRequestUtils.getStringParameter(request, "sign", null);
		
		//返回响应obj
		JSONObject returnJson = new JSONObject();
		try {
			LogUtil.log("全网短信参数: seller_key:"+sellerKey+" imsi:"+imsi+" appName:"+appName+" fee:"+fee+" outTradeNo:"+outTradeNo+" sign:"+sign);
			
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
			
			TSmsSeller tSmsSeller = smsSellerManager.getSmsSellerByProperty("sellerKey", sellerKey);
			if (Utils.isEmpty(tSmsSeller)) {
				returnJson.put("code", "1007");
				returnJson.put("msg", "没有找到渠道相关信息");
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
			String geneSign = TokenService.buildToken(queryParamList, tSmsSeller.getSellerSecret());
			if (!sign.equals(geneSign)) {
				returnJson.put("code", "1008");
				returnJson.put("msg", "消息签名不正确");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			//查询关联app
			boolean b = false;//检测应用是否全部达到限量值
			String appNameDecode = URLDecoder.decode(appName, "UTF-8");
			List<TSmsSellerApps> tSmsSellerAppsList = tSmsSeller.getSellerApps();
//			TSmsApp tSmsApp = null;
//			for (TSmsSellerApps tsmsSellerApps : tSmsSellerAppsList) {
//				TSmsApp tSmsAppTmp = tsmsSellerApps.getSmsApp();
//				if(appNameDecode.equals(tSmsAppTmp.getName()))
//					tSmsApp = tSmsAppTmp;
//			}
//			if (Utils.isEmpty(tSmsApp)) {
//				returnJson.put("code", "1009");
//				returnJson.put("msg", "没有找到相关app信息");
//				StringUtil.printJson(response, returnJson.toString());
//				return;
//			}
			TSmsApp tSmsApp = null;
			if (tSmsSellerAppsList.size() > 0) {
				for (TSmsSellerApps tSmsSellerApps : tSmsSellerAppsList) {
					Integer appLimit = tSmsSellerApps.getAppLimit();
					Integer appToday = tSmsSellerApps.getAppToday();
					if (appLimit != -1) {
						if (appToday >= appLimit) {
							continue;
						}
					}
					tSmsApp = tSmsSellerApps.getSmsApp();
					b = true;
				}
			} else {
				returnJson.put("code", "1009");
				returnJson.put("msg", "没有找到相关app信息");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			if (!b) {
				returnJson.put("code", "1010");
				returnJson.put("msg", "已达到今日限量值");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			//查询app对应的channelId和key,及创建订单所需参数
			Integer appId = tSmsApp.getId();
			Integer merchantId = tSmsApp.getMerchantId();
			Integer sellerId = tSmsSeller.getId();
			
			//查询fee所对应的产品
			String productId = null;
			String sendNumber = null;
			String instruction = null;
			List<TSmsProductInfo> proList = tSmsApp.getProductList();
			for (TSmsProductInfo tSmsProductInfo : proList) {
				if(fee == tSmsProductInfo.getPrice() && tSmsProductInfo.getType() == 1) {
					productId = tSmsProductInfo.getProductId();
					sendNumber = tSmsProductInfo.getSendNumber();
					instruction = tSmsProductInfo.getInstruction();
				}
			}
			if (Utils.isEmpty(productId)) {
				returnJson.put("code", "1011");
				returnJson.put("msg", "没有找到相关计费点");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			
			//订单号
			String orderSeq = DateUtil.getCurrentTimestamp("yyyyMMddHHmmssSSS") + Math.round(Math.random() * 1000);
			
			TSmsOrder tSmsOrder = new TSmsOrder();
			tSmsOrder.setOrderId(orderSeq);
			tSmsOrder.setOutTradeNo(outTradeNo);
			tSmsOrder.setImsi(imsi);
			tSmsOrder.setAppId(appId);
			tSmsOrder.setMerchantId(merchantId);
			tSmsOrder.setSellerId(sellerId);
			tSmsOrder.setSubject(appNameDecode);
			tSmsOrder.setSenderNumber(sendNumber);
			tSmsOrder.setMsgContent(instruction);
			tSmsOrder.setProductType(Constants.T_SMS_ORDER_PRODUCT_TYPE.DIANBO.getValue());
			tSmsOrder.setFee(fee);
			smsOrderManager.save(tSmsOrder);
			
			returnJson.put("code", "1");
			returnJson.put("msg", "订单创建成功");
			returnJson.put("order_id", orderSeq);
			returnJson.put("out_trade_no", outTradeNo);
			returnJson.put("sendNumber", sendNumber);
			returnJson.put("instruction", instruction+"#"+orderSeq);
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
}
