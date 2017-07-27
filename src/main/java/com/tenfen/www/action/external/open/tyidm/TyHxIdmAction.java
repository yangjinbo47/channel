package com.tenfen.www.action.external.open.tyidm;

import java.io.PrintWriter;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;
import com.tenfen.entity.operation.TMobileArea;
import com.tenfen.entity.operation.open.TOpenApp;
import com.tenfen.entity.operation.open.TOpenAppLimit;
import com.tenfen.entity.operation.open.TOpenMerchant;
import com.tenfen.entity.operation.open.TOpenOrder;
import com.tenfen.entity.operation.open.TOpenProductInfo;
import com.tenfen.entity.operation.open.TOpenSeller;
import com.tenfen.entity.operation.open.TOpenSellerApps;
import com.tenfen.entity.system.ImsiMdnRelation;
import com.tenfen.util.CTUtil;
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
import com.tenfen.www.util.TokenService;
import com.tenfen.www.util.TokenService.TokenParam;

public class TyHxIdmAction extends SimpleActionSupport{
	
	private static final long serialVersionUID = 8850486384317240505L;

	@Autowired
	private OpenOrderManager openOrderManager;
	@Autowired
	private OpenMerchantManager openMerchantManager;
	@Autowired
	private OpenSellerManager openSellerManager;
	@Autowired
	private OpenAppManager openAppManager;
	@Autowired
	private ImsiMdnRelationManager imsiMdnRelationManager;
	@Autowired
	private MobileAreaManager mobileAreaManager;
	
	/**
	 * 生成单笔订单action
	 * app_name需要申报
	 * 计费点需要申报，事先录入产品列表
	 */
	public void generateOrder() {
		String sellerKey = ServletRequestUtils.getStringParameter(request, "seller_key", null);
		String imsi = ServletRequestUtils.getStringParameter(request, "imsi", null);
		String appName = ServletRequestUtils.getStringParameter(request, "app_name", null);
		int fee = ServletRequestUtils.getIntParameter(request, "fee", 0);
		String outTradeNo = ServletRequestUtils.getStringParameter(request, "out_trade_no", null);
		String sign = ServletRequestUtils.getStringParameter(request, "sign", null);
		int chargeFee = 0;
		
		//返回响应obj
		JSONObject returnJson = new JSONObject();
		try {
			LogUtil.log("general params: seller_key:"+sellerKey+" imsi:"+imsi+" appName:"+appName+" fee:"+fee+" outTradeNo:"+outTradeNo+" sign:"+sign);
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
			
			//从本地库中获取
			String phone = null;
			if (imsi.length() == 11) {
				phone = imsi;
			} else {
				ImsiMdnRelation imsiMdnRelation = imsiMdnRelationManager.getEntityByProperty("imsi", imsi);
				if (imsiMdnRelation != null) {
					phone = imsiMdnRelation.getPhoneNum();
				}
				//从接口获取号码
				if (phone == null || phone.length() == 0) {
					phone = CTUtil.queryPhoneByIMSI(imsi);
					if (!Utils.isEmpty(phone)) {
						imsiMdnRelation = new ImsiMdnRelation();
						imsiMdnRelation.setImsi(imsi);
						imsiMdnRelation.setPhoneNum(phone);
						imsiMdnRelationManager.save(imsiMdnRelation);
					}
				}
				if (Utils.isEmpty(phone)) {
					returnJson.put("code", "1013");
					returnJson.put("msg", "未获取到手机号");
					StringUtil.printJson(response, returnJson.toString());
					return;
				} else {//检查省份屏蔽状况
					String province = null;
					TMobileArea mobileArea = mobileAreaManager.getMobileArea(phone);
					if (mobileArea != null) {
						province = mobileArea.getProvince();
					}
					boolean flag = false;//false-排除 true-不排除
					if (province != null) {
						//判断省是否到量
						TOpenAppLimit tOpenAppLimit = openAppManager.findAppLimitByProperty(tOpenApp.getId(), province);
						Integer packagedaylimit_conf = tOpenAppLimit.getDayLimit();
						if (packagedaylimit_conf == -1) {//不屏蔽
							flag = true;
						}
					} else {//未取到号码所在地
						flag = true;
					}
					
					if (!flag) {
						returnJson.put("code", "1014");
						returnJson.put("msg", "号码在排除地市内");
						StringUtil.printJson(response, returnJson.toString());
						return;
					}
				}
			}
			
			//查询app对应的channelId和key,及创建订单所需参数
			String channelId = tOpenApp.getAppKey();
			Integer appId = tOpenApp.getId();
			Integer merchantId = tOpenApp.getMerchantId();
			Integer sellerId = tOpenSeller.getId();
			
			TOpenMerchant tOpenMerchant = openMerchantManager.get(merchantId);
			if (Utils.isEmpty(tOpenMerchant)) {
				returnJson.put("code", "1011");
				returnJson.put("msg", "没有找到相关商户信息");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			
			//通过app查询关联的计费信息
			List<TOpenProductInfo> proList = tOpenApp.getProductList();
			Collections.sort(proList);
			JSONObject json = null;
			while (fee >= 1) {
				for (int i = 0; i < proList.size(); i++) {
					TOpenProductInfo tOpenProductInfo = proList.get(i);
					chargeFee = tOpenProductInfo.getPrice();
					if (fee - chargeFee >= 0) {
						fee = fee - chargeFee;
						
						json = generateIDMOrder(outTradeNo, appNameDecode, appId, merchantId, sellerId, chargeFee, imsi, phone, channelId, tOpenProductInfo.getProductId());
						if (Utils.isEmpty(json)) {
							returnJson.put("code", "1015");
							returnJson.put("msg", "爱动漫平台订单错误");
							StringUtil.printJson(response, returnJson.toString());
							return;
						}
						
                        break;
					}
				}
			}
			
			returnJson.put("code", "1");
			returnJson.put("msg", json);
			StringUtil.printJson(response, returnJson.toString());
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
			returnJson.put("code", "9999");
			returnJson.put("msg", "系统未知异常");
			StringUtil.printJson(response, returnJson.toString());
			return;
		}
	}
	
	/**
	 * 生成爱动漫订单
	 * @return
	 */
	private JSONObject generateIDMOrder(String outTradeNo, String appName, Integer appId, Integer merchantId, Integer sellerId, Integer price, String imsi, String phone, String channelId, String productKey) {
		JSONObject returnJsonMsgObj = null;
		try {
			String result = generateIDMOrder(price, phone, channelId, productKey);
			
			JSONObject res = JSONObject.parseObject(result);
			Integer re = res.getInteger("re");//订单创建结果 0-成功 1-失败
			if (re == 0) {
				String orderid = res.getString("orderid");
				
				//创建订单
				TOpenOrder tOpenOrder = new TOpenOrder();
				tOpenOrder.setImsi(imsi);
				tOpenOrder.setOrderId(orderid);
				tOpenOrder.setOutTradeNo(outTradeNo);
				tOpenOrder.setSellerId(sellerId);
				tOpenOrder.setAppId(appId);
				tOpenOrder.setMerchantId(merchantId);
				tOpenOrder.setSubject(appName);
				tOpenOrder.setSenderNumber("");
				tOpenOrder.setMsgContent(productKey);
				tOpenOrder.setFee(price);
				tOpenOrder.setPayPhone(phone);
				TMobileArea mobileArea = mobileAreaManager.getMobileArea(phone);
				String province = null;
				if (!Utils.isEmpty(mobileArea)) {
					province = mobileArea.getProvince();
					tOpenOrder.setProvince(province);
				}
				openOrderManager.save(tOpenOrder);
				
				returnJsonMsgObj = new JSONObject();
				returnJsonMsgObj.put("order_id", orderid);
				returnJsonMsgObj.put("out_trade_no", outTradeNo);
				returnJsonMsgObj.put("fee", price);
				returnJsonMsgObj.put("phone", phone);
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		
		return returnJsonMsgObj;
	}
	
	private String generateIDMOrder(Integer price, String phone, String channelId, String productKey) {
		String res = null;
		try {
			Map<String, String> map = new HashMap<String, String>();
			map.put("productKey",productKey);
			map.put("fee",price+"");
			map.put("channelid",channelId);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			map.put("timeStamp",sdf.format(new Date()));
			map.put("phone", phone);
			
			res = HttpClientUtils.simpleGetInvoke("http://114.55.111.145:5500/order", map);
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return res;
	}
	
	/**
	 * 验证码提交
	 */
	public void verify() {
		String smsCode = ServletRequestUtils.getStringParameter(request, "smsCode", null);
		String orderId = ServletRequestUtils.getStringParameter(request, "orderId", null);
		
		JSONObject returnJson = new JSONObject();
		try {
			if (Utils.isEmpty(smsCode)) {
				returnJson.put("code", "1001");
				returnJson.put("msg", "smsCode参数不能为空");
				StringUtil.printJson(response, returnJson.toString());
				return;
			} else if (Utils.isEmpty(orderId)) {
				returnJson.put("code", "1002");
				returnJson.put("msg", "orderId参数不能为空");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			
			TOpenOrder tOpenOrder = openOrderManager.getOpenOrderByProperty("orderId", orderId);
			if (!Utils.isEmpty(tOpenOrder)) {
				String productKey = tOpenOrder.getMsgContent();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String productname = tOpenOrder.getSubject();
				String contentname = tOpenOrder.getSubject();
				
				Map<String, String> map = new HashMap<String, String>();
				map.put("productKey",productKey);
				map.put("smsCode", smsCode);
				map.put("orderId",orderId);
				map.put("timeStamp",sdf.format(new Date()));
				map.put("productname", productname);
				map.put("contentname", contentname);
				
				String res = HttpClientUtils.simpleGetInvoke("http://114.55.111.145:5500/verify_code", map);
				StringUtil.printJson(response, res);
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
	public void callBack() {
		String behavior = ServletRequestUtils.getStringParameter(request, "behavior", null);
		String trade_status = ServletRequestUtils.getStringParameter(request, "trade_status", null);
		String orderid = ServletRequestUtils.getStringParameter(request, "orderid", null);
		String order_time = ServletRequestUtils.getStringParameter(request, "order_time", null);
		String pay_phone = ServletRequestUtils.getStringParameter(request, "phone", null);
		String price = ServletRequestUtils.getStringParameter(request, "price", null);
		String trade_no = ServletRequestUtils.getStringParameter(request, "trade_no", null);
		LogUtil.log("tyidm callback params: behavior:"+behavior+" trade_status:"+trade_status+" orderid:"+orderid+" order_time:"+order_time+" pay_phone:"+pay_phone+" price:"+price+" trade_no:"+trade_no);
		String success = "success";
		int reduce = 0;
		try {
			TOpenOrder tOpenOrder = openOrderManager.getOpenOrderByProperty("orderId", orderid);
			if (!Utils.isEmpty(tOpenOrder)) {
				String status = "4";
				if ("0".equals(trade_status)) {
					status = "3";
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Date payTime = sdf.parse(order_time);
					tOpenOrder.setStatus(status);
					tOpenOrder.setPayPhone(pay_phone);
					tOpenOrder.setPayTime(payTime);
					TMobileArea mobileArea = mobileAreaManager.getMobileArea(pay_phone);
					String province = null;
					if (!Utils.isEmpty(mobileArea)) {
						province = mobileArea.getProvince();
						tOpenOrder.setProvince(province);
					}
					
					//是否扣量
					Integer appId = tOpenOrder.getAppId();
					TOpenAppLimit tOpenAppLimit = openAppManager.findAppLimitByProperty(appId, province);
					double reduce_conf = tOpenAppLimit.getReduce()/(double)100;
					double rate = new Random().nextDouble();
					if (rate < reduce_conf) {
						reduce = 1;
						tOpenOrder.setReduce(reduce);
					}
					openOrderManager.save(tOpenOrder);
				} else {
					status = "4";
					tOpenOrder.setStatus(status);
					openOrderManager.save(tOpenOrder);
				}
				//增加今日量
				if ("0".equals(trade_status)) {
					openSellerManager.saveOpenSellerApps(tOpenOrder.getSellerId(), tOpenOrder.getAppId(), tOpenOrder.getFee());
				}
				//回调渠道
				TOpenSeller tOpenSeller = openSellerManager.get(tOpenOrder.getSellerId());
				String callbackUrl = tOpenSeller.getCallbackUrl();
				if (!Utils.isEmpty(callbackUrl)) {
					String outTradeNo = tOpenOrder.getOutTradeNo();
					if (reduce != 1) {//不扣量
						new Thread(new SendPartner(status,orderid,outTradeNo,tOpenOrder.getFee()+"",callbackUrl)).start();
					} else {
						new Thread(new SendPartner("4",orderid,outTradeNo,tOpenOrder.getFee()+"",callbackUrl)).start();
					}
				}
				success = "success";
			}
			
		} catch (Exception e) {
			success = "fail";
			LogUtil.error(e.getMessage(), e);
		}
		
		try {
			PrintWriter out = response.getWriter();
			response.setContentType("text/html");
			out.println(success);
			out.flush();
			out.close();
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
	private class SendPartner implements Runnable {
		private String fee;
		private String status;
		private String orderNo;
		private String outTradeNo;
		private String callbackUrl;
		
		public SendPartner(String status,String orderNo,String outTradeNo,String fee,String callbackUrl) {
			this.fee = fee;
			this.status = status;
			this.orderNo = orderNo;
			this.outTradeNo = outTradeNo;
			this.callbackUrl = callbackUrl;
		}
		
		@Override
		public void run() {
			try {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("order_no", orderNo);
				jsonObject.put("out_trade_no", outTradeNo);
				jsonObject.put("fee", fee);
				jsonObject.put("status", status);
				
				LogUtil.log("sendChannelIdmMsg:"+jsonObject.toString());
				HttpClientUtils.postJson(callbackUrl, jsonObject.toString());
			} catch (Exception e) {
				LogUtil.error(e.getMessage(), e);
			}
			
		}
	}

}
