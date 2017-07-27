package com.tenfen.www.action.external.open.tyspace;

import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tenfen.entity.operation.TMobileArea;
import com.tenfen.entity.operation.open.TOpenApp;
import com.tenfen.entity.operation.open.TOpenOrder;
import com.tenfen.entity.operation.open.TOpenProductInfo;
import com.tenfen.entity.operation.open.TOpenSeller;
import com.tenfen.entity.operation.open.TOpenSellerApps;
import com.tenfen.util.DateUtil;
import com.tenfen.util.HttpClientUtils;
import com.tenfen.util.LogUtil;
import com.tenfen.util.StringUtil;
import com.tenfen.util.Utils;
import com.tenfen.util.encrypt.MD5;
import com.tenfen.util.servlet.ServletRequestUtils;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.service.operation.MobileAreaManager;
import com.tenfen.www.service.operation.open.OpenAppManager;
import com.tenfen.www.service.operation.open.OpenOrderManager;
import com.tenfen.www.service.operation.open.OpenSellerManager;
import com.tenfen.www.service.system.ImsiMdnRelationManager;
import com.tenfen.www.util.TokenService;
import com.tenfen.www.util.TokenService.TokenParam;

/**
 * 天翼空间（朗天接入）
 * @author BOBO
 *
 */
public class TySpaceLTAction extends SimpleActionSupport {
	
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
		
		//返回响应obj
		JSONObject returnJson = new JSONObject();
		try {
			LogUtil.log("langtian三方天翼空间参数: seller_key:"+sellerKey+" imsi:"+imsi+" appName:"+appName+" fee:"+fee+" outTradeNo:"+outTradeNo+" sign:"+sign);
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
				returnJson.put("code", "1010");
				returnJson.put("msg", "out_trade_no参数不能为空");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			
			//通过sellerKey查询渠道信息
			TOpenSeller tOpenSeller = openSellerManager.getOpenSellerByProperty("sellerKey", sellerKey);
			if (Utils.isEmpty(tOpenSeller)) {
				returnJson.put("code", "1006");
				returnJson.put("msg", "没有找到渠道相关信息");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			if (tOpenSeller.getStatus() == 0) {
				returnJson.put("code", "1011");
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
				returnJson.put("code", "1007");
				returnJson.put("msg", "消息签名不正确");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			
			//查询关联app
			String appNameDecode = URLDecoder.decode(appName, "UTF-8");
			List<TOpenSellerApps> openSellerAppList = tOpenSeller.getSellerApps();
			TOpenApp tOpenApp = null;
			for (TOpenSellerApps tOpenSellerApps : openSellerAppList) {
				TOpenApp tOpenAppTmp = tOpenSellerApps.getOpenApp();
				if(appNameDecode.equals(tOpenAppTmp.getName()))
					tOpenApp = tOpenAppTmp;
			}
			if (Utils.isEmpty(tOpenApp)) {
				returnJson.put("code", "1008");
				returnJson.put("msg", "没有找到相关app信息");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			//查询app对应的channelId和key,及创建订单所需参数
			String channelId = tOpenApp.getAppKey();
			String key = tOpenApp.getAppSecret();
			Integer appId = tOpenApp.getId();
			Integer merchantId = tOpenApp.getMerchantId();
			Integer sellerId = tOpenSeller.getId();
			
			//查询fee所对应的产品，找出extraData
			String extraData = null;
			List<TOpenProductInfo> proList = tOpenApp.getProductList();
			for (TOpenProductInfo tOpenProductInfo : proList) {
				if(fee == tOpenProductInfo.getPrice())
					extraData = tOpenProductInfo.getInstruction();
			}
			if (Utils.isEmpty(extraData)) {
				returnJson.put("code", "1009");
				returnJson.put("msg", "没有找到相关计费点");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			
			//调用朗天接口
			String orderSeq = DateUtil.getCurrentTimestamp("yyyyMMddHHmmssSSS") + Math.round(Math.random() * 1000);
			String res = generateOrder(orderSeq, fee, channelId, imsi, extraData, key);
			JSONObject jsonObject = JSONObject.parseObject(res);
			String code = jsonObject.getString("resultCode");
			if ("0000".equals(code)) {
				String senderNumber = jsonObject.getString("longCode");
				String msgContent = jsonObject.getString("content");
				
				TOpenOrder tOpenOrder = new TOpenOrder();
				tOpenOrder.setImsi(imsi);
				tOpenOrder.setOrderId(orderSeq);
				tOpenOrder.setOutTradeNo(outTradeNo);
				tOpenOrder.setAppId(appId);
				tOpenOrder.setMerchantId(merchantId);
				tOpenOrder.setSubject(appNameDecode);
				tOpenOrder.setSenderNumber(senderNumber);
				tOpenOrder.setMsgContent(msgContent);
				tOpenOrder.setFee(fee);
				tOpenOrder.setSellerId(sellerId);
				openOrderManager.save(tOpenOrder);
				
				returnJson.put("code", "1");
				returnJson.put("msg", "成功");
				returnJson.put("out_trade_no", outTradeNo);
				returnJson.put("fee", fee);
				returnJson.put("sender_number", senderNumber);
				returnJson.put("message_content", msgContent);
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
			returnJson.put("code", "9999");
			returnJson.put("msg", "系统未知异常");
			StringUtil.printJson(response, returnJson.toString());
			return;
		}
	}
	
	/**
	 * 拼资费生成订单
	 */
	public void generateMultiOrder() {
		String sellerKey = ServletRequestUtils.getStringParameter(request, "seller_key", null);
		String imsi = ServletRequestUtils.getStringParameter(request, "imsi", null);
		String appName = ServletRequestUtils.getStringParameter(request, "app_name", null);
		int fee = ServletRequestUtils.getIntParameter(request, "fee", 0);
		String outTradeNo = ServletRequestUtils.getStringParameter(request, "out_trade_no", null);
		String sign = ServletRequestUtils.getStringParameter(request, "sign", null);
		
		//返回响应obj
		JSONObject returnJson = new JSONObject();
		try {
			LogUtil.log("lt params: seller_key:"+sellerKey+" imsi:"+imsi+" appName:"+appName+" fee:"+fee+" outTradeNo:"+outTradeNo+" sign:"+sign);
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
				returnJson.put("code", "1010");
				returnJson.put("msg", "out_trade_no参数不能为空");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			
			//通过sellerKey查询渠道信息
			TOpenSeller tOpenSeller = openSellerManager.getOpenSellerByProperty("sellerKey", sellerKey);
			if (Utils.isEmpty(tOpenSeller)) {
				returnJson.put("code", "1006");
				returnJson.put("msg", "没有找到渠道相关信息");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			if (tOpenSeller.getStatus() == 0) {
				returnJson.put("code", "1011");
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
			if (!sign.equals(geneSign)) {
				returnJson.put("code", "1007");
				returnJson.put("msg", "消息签名不正确");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			
			//查询关联app
			String appNameDecode = URLDecoder.decode(appName, "UTF-8");
			List<TOpenSellerApps> openSellerAppList = tOpenSeller.getSellerApps();
			TOpenApp tOpenApp = null;
			for (TOpenSellerApps tOpenSellerApps : openSellerAppList) {
				TOpenApp tOpenAppTmp = tOpenSellerApps.getOpenApp();
				if(appNameDecode.equals(tOpenAppTmp.getName()))
					tOpenApp = tOpenAppTmp;
			}
			if (Utils.isEmpty(tOpenApp)) {
				returnJson.put("code", "1008");
				returnJson.put("msg", "没有找到相关app信息");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			//查询app对应的channelId和key,及创建订单所需参数
			String channelId = tOpenApp.getAppKey();
			String key = tOpenApp.getAppSecret();
			Integer appId = tOpenApp.getId();
			Integer merchantId = tOpenApp.getMerchantId();
			Integer sellerId = tOpenSeller.getId();
			
			//查询fee所对应的产品，找出extraData
			int chargeFee = 0;
			String extraData = null;
			List<TOpenProductInfo> proList = tOpenApp.getProductList();
			Collections.sort(proList);
			JSONArray jsonArray = new JSONArray();
			while (fee >= 100) {
				for (int i = 0; i < proList.size(); i++) {
					TOpenProductInfo tOpenProductInfo = proList.get(i);
					chargeFee = tOpenProductInfo.getPrice();
					if (fee - chargeFee >= 0) {
						fee = fee - chargeFee;
						
						extraData = tOpenProductInfo.getInstruction();
						if (Utils.isEmpty(extraData)) {
							returnJson.put("code", "1009");
							returnJson.put("msg", "没有找到相关计费点");
							StringUtil.printJson(response, returnJson.toString());
							return;
						}
						
						//调用朗天接口
						String orderSeq = DateUtil.getCurrentTimestamp("yyyyMMddHHmmssSSS") + Math.round(Math.random() * 1000);
						String res = generateOrder(orderSeq, chargeFee, channelId, imsi, extraData, key);
						JSONObject jsonObject = JSONObject.parseObject(res);
						String code = jsonObject.getString("resultCode");
						if ("0000".equals(code)) {
							String senderNumber = jsonObject.getString("longCode");
							String msgContent = jsonObject.getString("content");
							
							TOpenOrder tOpenOrder = new TOpenOrder();
							tOpenOrder.setImsi(imsi);
							tOpenOrder.setOrderId(orderSeq);
							tOpenOrder.setOutTradeNo(outTradeNo);
							tOpenOrder.setAppId(appId);
							tOpenOrder.setMerchantId(merchantId);
							tOpenOrder.setSubject(appNameDecode);
							tOpenOrder.setSenderNumber(senderNumber);
							tOpenOrder.setMsgContent(msgContent);
							tOpenOrder.setFee(chargeFee);
							tOpenOrder.setSellerId(sellerId);
							openOrderManager.save(tOpenOrder);
							
							JSONObject msgObj = new JSONObject();
							msgObj.put("order_id", orderSeq);
							msgObj.put("out_trade_no", outTradeNo);
							msgObj.put("fee", chargeFee);
							msgObj.put("sender_number", senderNumber);
							msgObj.put("message_content", msgContent);
							jsonArray.add(msgObj);
						}
                        break;
					}
				}
			}
			
			returnJson.put("code", "1");
			returnJson.put("msg", jsonArray);
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
	
	public static String generateOrder(String orderSeq, Integer price, String channelId, String imsi, String extraData, String key) {
		String responseString = null;

		try {
			String mac = MD5.getMD5(orderSeq+price+channelId+imsi+extraData+key).toUpperCase();
			
			Map<String, String> map = new HashMap<String, String>();
			map.put("orderSeq",orderSeq);
			map.put("price",price+"");
			map.put("channelId",channelId);
			map.put("IMSI",imsi);
			map.put("extraData",extraData);
			map.put("mac", mac);
			
			responseString = HttpClientUtils.simplePostInvoke("http://121.41.58.237:8981/center/getSmsCode.sys", map);
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return responseString;
	}

	/**
	 * 天翼空间朗天三方-回调地址
	 */
	public void callBack() {
		String mobile = ServletRequestUtils.getStringParameter(request, "mobile", null);
		String linkId = ServletRequestUtils.getStringParameter(request, "linkId", null);
		String longCode = ServletRequestUtils.getStringParameter(request, "longCode", null);
		String msg = ServletRequestUtils.getStringParameter(request, "msg", null);
		String status = ServletRequestUtils.getStringParameter(request, "status", null);
		String fee = ServletRequestUtils.getStringParameter(request, "fee", null);
		String mac = ServletRequestUtils.getStringParameter(request, "mac", null);
		String key = "1614Nm44";
//		LogUtil.log("ltcallback mobile:"+mobile+" LinkId:"+linkId+" longCode:"+longCode+" msg:"+msg+" status:"+status+" fee:"+fee+" mac:"+mac);
		try {
			String geneMac = MD5.getMD5(mobile+linkId+longCode+msg+status+fee+key).toUpperCase();
			if (mac.equals(geneMac)) {
				TOpenOrder tOpenOrder = openOrderManager.getOpenOrderByProperty("orderId", linkId);
				if (!Utils.isEmpty(tOpenOrder)) {
					//status转换
					String statusChange = "9999";
					if ("00".equals(status)) {
						statusChange = "3";
					} else if ("01".equals(status)) {
						statusChange = "4";
					} else if ("02".equals(status)) {
						statusChange = "9999";
					}
					
					tOpenOrder.setStatus(statusChange);
					tOpenOrder.setPayTime(new Date());
					String payPhone = imsiMdnRelationManager.getPhone(mobile);
					if (!Utils.isEmpty(payPhone)) {
						TMobileArea mobileArea = mobileAreaManager.getMobileArea(payPhone);
						if (!Utils.isEmpty(mobileArea)) {
							tOpenOrder.setProvince(mobileArea.getProvince());
						}
						tOpenOrder.setPayPhone(payPhone);
					} else {
						tOpenOrder.setPayPhone(mobile);
					}
					openOrderManager.save(tOpenOrder);
					
					//增加今日量
					if ("00".equals(status)) {
						openSellerManager.saveOpenSellerApps(tOpenOrder.getSellerId(), tOpenOrder.getAppId(), tOpenOrder.getFee());
					}
					
					//回调渠道
					TOpenSeller tOpenSeller = openSellerManager.get(tOpenOrder.getSellerId());
					String callbackUrl = tOpenSeller.getCallbackUrl();
					if (!Utils.isEmpty(callbackUrl)) {						
						String orderId = tOpenOrder.getOrderId();
						String outTradeNo = tOpenOrder.getOutTradeNo();
						String price = tOpenOrder.getFee()+"";
						new Thread(new SendPartner(status,orderId,outTradeNo,price,callbackUrl)).start();
					}
				}
				PrintWriter out = response.getWriter();
		        response.setContentType("text/html");
		        out.println("success");
		        out.flush();
				out.close();
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
	private class SendPartner implements Runnable {
		private String fee;
		private String status;
		private String orderId;
		private String outTradeNo;
		private String callbackUrl;
		
		public SendPartner(String status, String orderId, String outTradeNo, String fee, String callbackUrl) {
			this.fee = fee;
			this.status = status;
			this.orderId = orderId;
			this.outTradeNo = outTradeNo;
			this.callbackUrl = callbackUrl;
		}
		
		@Override
		public void run() {
			try {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("order_id", orderId);
				jsonObject.put("out_trade_no", outTradeNo);
				jsonObject.put("fee", fee);
				jsonObject.put("status", status);
				
		        LogUtil.log("sendChannelTySpaceLTMsg:"+jsonObject.toString());
		        HttpClientUtils.postJson(callbackUrl, jsonObject.toString());
			} catch (Exception e) {
				LogUtil.error(e.getMessage(), e);
			}
			
		}
	}
}
