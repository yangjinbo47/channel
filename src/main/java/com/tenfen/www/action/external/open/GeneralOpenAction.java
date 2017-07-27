package com.tenfen.www.action.external.open;

import java.io.PrintWriter;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONArray;
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
import com.tenfen.util.DateUtil;
import com.tenfen.util.HttpClientUtils;
import com.tenfen.util.LogUtil;
import com.tenfen.util.StringUtil;
import com.tenfen.util.Utils;
import com.tenfen.util.servlet.ServletRequestUtils;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.common.Constants;
import com.tenfen.www.service.operation.MobileAreaManager;
import com.tenfen.www.service.operation.open.OpenAppManager;
import com.tenfen.www.service.operation.open.OpenMerchantManager;
import com.tenfen.www.service.operation.open.OpenOrderManager;
import com.tenfen.www.service.operation.open.OpenSellerManager;
import com.tenfen.www.service.system.ImsiMdnRelationManager;
import com.tenfen.www.util.TokenService;
import com.tenfen.www.util.TokenService.TokenParam;
import com.tenfen.www.util.tyyd.PBECoder;
import com.tenfen.www.util.tyyd.SMSOrderIDGenerator;

/**
 * 天翼空间阅读兼容action
 * @author BOBO
 *
 */
public class GeneralOpenAction extends SimpleActionSupport {
	
	private static final long serialVersionUID = 8112913793580559503L;
	
	@Autowired
	private OpenSellerManager openSellerManager;
	@Autowired
	private OpenOrderManager openOrderManager;
	@Autowired
	private OpenMerchantManager openMerchantManager;
	@Autowired
	private ImsiMdnRelationManager imsiMdnRelationManager;
	@Autowired
	private MobileAreaManager mobileAreaManager;
	@Autowired
	private OpenAppManager openAppManager;
	
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
			String appNameDecode = URLDecoder.decode(appName, "UTF-8");
			LogUtil.log("general params: seller_key:"+sellerKey+" imsi:"+imsi+" appName:"+appNameDecode+" fee:"+fee+" outTradeNo:"+outTradeNo+" sign:"+sign);
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
			if (!sign.toLowerCase().equals(geneSign) && !"test".equals(sign)) {
				returnJson.put("code", "1009");
				returnJson.put("msg", "消息签名不正确");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			
			//查询关联app
			boolean b = false;//检测应用是否全部达到限量值
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
			String timestamp = String.valueOf(System.currentTimeMillis());
			String key = tOpenApp.getAppKey();
			String secret = tOpenApp.getAppSecret();
			String callBackUrl = tOpenApp.getCallbackUrl();
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
			String merchantName = tOpenMerchant.getMerchantName();
			
			JSONArray returnJsonArray = new JSONArray();
			//通过app查询关联的计费信息
			List<TOpenProductInfo> proList = tOpenApp.getProductList();
			Collections.sort(proList);
			while (fee >= 1) {
				for (int i = 0; i < proList.size(); i++) {
					TOpenProductInfo tOpenProductInfo = proList.get(i);
					chargeFee = tOpenProductInfo.getPrice();
					if (fee - chargeFee >= 0) {
						fee = fee - chargeFee;
						
						JSONObject json = null;
						if (Constants.OPEN_MERCHANT_TYPE.TYYD.getValue().equals(tOpenMerchant.getJoinType())) {//天翼阅读
							json = generateTyydOrder(outTradeNo, appNameDecode, appId, merchantId, sellerId, key, secret, callBackUrl, proList.get(i).getProductId(), chargeFee, timestamp, imsi);
						} else if (Constants.OPEN_MERCHANT_TYPE.TYYD_LX.getValue().equals(tOpenMerchant.getJoinType())) {//天翼阅读-离线
							json = generateTyydLxOrder(outTradeNo, appNameDecode, appId, merchantId, sellerId, tOpenProductInfo.getInstruction(), tOpenProductInfo.getCode(), chargeFee, imsi);
						} else if (Constants.OPEN_MERCHANT_TYPE.GNSPACE.getValue().equals(tOpenMerchant.getJoinType())) {//天翼空间-通用（旭游空间有验证码）
							json = generateTySpaceGnOrder(outTradeNo, appNameDecode, merchantName, tOpenProductInfo.getName(), appId, merchantId, sellerId, key, secret, chargeFee, timestamp, imsi, tOpenProductInfo.getType()+"");
						} else if (Constants.OPEN_MERCHANT_TYPE.ZYSPACE.getValue().equals(tOpenMerchant.getJoinType())) {//天翼空间-旭游（旭游空间无验证码）
							json = generateTySpaceZyOrder(outTradeNo, appNameDecode, merchantName, tOpenProductInfo.getName(), appId, merchantId, sellerId, key, secret, chargeFee, timestamp, imsi, tOpenProductInfo.getType()+"");
						}
//						else if (Constants.OPEN_MERCHANT_TYPE.IMUSIC.getValue().equals(tOpenMerchant.getJoinType())) {//爱音乐
//							json = generateIMusicOrder(outTradeNo, appNameDecode, merchantName, tOpenProductInfo.getName(), appId, merchantId, sellerId, key, secret, chargeFee, timestamp, imsi, tOpenProductInfo.getType()+"");
//						}
						else if (Constants.OPEN_MERCHANT_TYPE.YIXIN.getValue().equals(tOpenMerchant.getJoinType())) {//易信
							json = generateYiXinOrder(outTradeNo, appNameDecode, appId, merchantId, sellerId, chargeFee, imsi, phone, key, tOpenProductInfo.getType());
						}
						if (Utils.isEmpty(json)) {
							returnJson.put("code", "1015");
							returnJson.put("msg", "基地未正确返回代码");
							StringUtil.printJson(response, returnJson.toString());
							return;
						}
						returnJsonArray.add(json);
						
                        break;
					}
				}
			}
			
			returnJson.put("code", "1");
			returnJson.put("msg", returnJsonArray);
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
	 * 生成tyyd离线订单
	 * @return
	 */
	public JSONObject generateTyydLxOrder(String outTradeNo, String appName, Integer appId, Integer merchantId, Integer sellerId, String instruction, String code, Integer price, String imsi) {
		JSONObject returnJsonMsgObj = null;
		try {
			String orderNo = SMSOrderIDGenerator.getOrderID(15);
			
			//生成短信内容
			String sms = generateTyydLxSms(instruction, orderNo);
			
			//创建订单
			TOpenOrder tOpenOrder = new TOpenOrder();
			tOpenOrder.setImsi(imsi);
			tOpenOrder.setOrderId(orderNo);
			tOpenOrder.setOutTradeNo(outTradeNo);
			tOpenOrder.setSellerId(sellerId);
			tOpenOrder.setAppId(appId);
			tOpenOrder.setMerchantId(merchantId);
			tOpenOrder.setSubject(appName);
			tOpenOrder.setSenderNumber(code);
			tOpenOrder.setMsgContent(sms);
			tOpenOrder.setFee(price);
			openOrderManager.save(tOpenOrder);
			
			returnJsonMsgObj = new JSONObject();
			returnJsonMsgObj.put("order_id", orderNo);
			returnJsonMsgObj.put("out_trade_no", outTradeNo);
			returnJsonMsgObj.put("fee", price);
			returnJsonMsgObj.put("sender_number", code);
			returnJsonMsgObj.put("message_content", sms);
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return returnJsonMsgObj;
	}
	
	/**
	 * 生成TYYD离线短信
	 * @param instruction
	 * @return
	 */
	public String generateTyydLxSms(String instruction, String smsExtInfo) {
		String parm =  SMSOrderIDGenerator.getOrderID(15)+","+smsExtInfo;
		String message = instruction+"#"+parm;
		
		int index = message.indexOf("#");
		String message1 = message.substring(0, index+1);
		String message2 = message.substring(index+1, message.length());
		try{
			byte[] b = PBECoder.encrypt(message2.getBytes("UTF-8"));
			message2 = PBECoder.encryptBASE64(b);
		} catch (Exception e){
			e.printStackTrace();
		}
		
		message = message1+message2;
		return message;
	}
	
	/**
	 * 生成tyyd订单
	 * @return
	 */
	public JSONObject generateTyydOrder(String outTradeNo, String appName, Integer appId, Integer merchantId, Integer sellerId, String key, String secret, String callbackUrl, String productId, Integer price, String timestamp, String imsi) {
		JSONObject returnJsonMsgObj = null;
		try {
			//调用天翼阅读开放平台接口
			List<TokenParam> paramList = new ArrayList<TokenParam>(4);
			paramList.add(new TokenParam("client_app_key",key));
			paramList.add(new TokenParam("product_id",productId));
			paramList.add(new TokenParam("product_name",appName));
			paramList.add(new TokenParam("timestamp",timestamp));
			paramList.add(new TokenParam("call_back_url",callbackUrl));
			String token = TokenService.buildToken(paramList, secret);
			String result = generateTyydOrder(key, productId, appName, timestamp, callbackUrl, token);
			
			JSONObject json = JSONObject.parseObject(result);
			String response = json.getString("response");
			JSONObject res = JSONObject.parseObject(response);
			
			Integer code = res.getInteger("code");//订单创建结果 0-成功 1-失败
			String orderNo = res.getString("order_no");
			if (code == 0) {
				String senderNumber = res.getString("sender_number");
				String msgContent = res.getString("message_content");
				//创建订单
				TOpenOrder tOpenOrder = new TOpenOrder();
				tOpenOrder.setImsi(imsi);
				tOpenOrder.setOrderId(orderNo);
				tOpenOrder.setOutTradeNo(outTradeNo);
				tOpenOrder.setAppId(appId);
				tOpenOrder.setMerchantId(merchantId);
				tOpenOrder.setSubject(appName);
				tOpenOrder.setSenderNumber(senderNumber);
				tOpenOrder.setMsgContent(msgContent);
				tOpenOrder.setFee(price);
				tOpenOrder.setSellerId(sellerId);
				openOrderManager.save(tOpenOrder);
				
				returnJsonMsgObj = new JSONObject();
				returnJsonMsgObj.put("order_id", orderNo);
				returnJsonMsgObj.put("out_trade_no", outTradeNo);
				returnJsonMsgObj.put("fee", price);
				returnJsonMsgObj.put("sender_number", senderNumber);
				returnJsonMsgObj.put("message_content", msgContent);
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return returnJsonMsgObj;
	}
	
	/**
	 * 调用tyyd订单生成接口
	 * @return
	 */
	private String generateTyydOrder(String appKey, String productId, String appName, String timestamp, String callBackUrl, String token) {
		String responseString = null;
		try {
			Map<String, String> map = new HashMap<String, String>();
			map.put("client_app_key",appKey);
			map.put("product_id",productId);
			map.put("product_name",appName);
			map.put("timestamp",timestamp);
			map.put("call_back_url", callBackUrl);
			map.put("token",token);
			responseString = HttpClientUtils.simplePostInvoke("http://pay.tyread.com/v2/generate_order.json", map);
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return responseString;
	}
	
	private JSONObject generateTySpaceGnOrder(String outTradeNo, String appName, String merchantName, String productName, Integer appId, Integer merchantId, Integer sellerId, String key, String secret, Integer price, String timestamp, String imsi, String chargeType) {
		JSONObject returnJsonMsgObj = null;
		try {
			String orderNo = DateUtil.getCurrentTimestamp("yyyyMMddHHmmssSSS") + Math.round(Math.random() * 1000);
			String result = generateTySpaceGnOrder(appName, merchantName, productName, key, secret, imsi, timestamp, orderNo, price, chargeType);
			
			if (result != null) {
				JSONObject res = JSONObject.parseObject(result);
				Integer code = res.getInteger("resultCode");//订单创建结果 0-成功 1-失败
				String desc = res.getString("resultDesc");//订单创建结果 0-成功 1-失败
				if (code == 0) {
					String senderNumber = res.getString("smsNum");
					String msgContent = res.getString("smsContent");
					
					//创建订单
					TOpenOrder tOpenOrder = new TOpenOrder();
					tOpenOrder.setImsi(imsi);
					tOpenOrder.setOrderId(orderNo);
					tOpenOrder.setOutTradeNo(outTradeNo);
					tOpenOrder.setAppId(appId);
					tOpenOrder.setMerchantId(merchantId);
					tOpenOrder.setSubject(appName);
					tOpenOrder.setSenderNumber(senderNumber);
					tOpenOrder.setMsgContent(msgContent);
					tOpenOrder.setFee(price);
					tOpenOrder.setSellerId(sellerId);
					openOrderManager.save(tOpenOrder);
					
					returnJsonMsgObj = new JSONObject();
					returnJsonMsgObj.put("order_id", orderNo);
					returnJsonMsgObj.put("out_trade_no", outTradeNo);
					returnJsonMsgObj.put("fee", price);
					returnJsonMsgObj.put("sender_number", senderNumber);
					returnJsonMsgObj.put("message_content", msgContent);
				} else {
					LogUtil.error(imsi + "请求空间代码错误，resultCode："+code+",resultDesc:"+desc);
				}
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return returnJsonMsgObj;
	}
	
	private String generateTySpaceGnOrder(String appName, String merchantName, String productName, String channel, String secret, String imsi, String timestamp, String orderId, Integer price, String chargeType) {
		String responseString = null;
		try {
			List<TokenParam> queryParamList = new ArrayList<TokenParam>();
			queryParamList.add(new TokenParam("ver", "1.0"));
			queryParamList.add(new TokenParam("channel",channel));
			queryParamList.add(new TokenParam("imsi",imsi));
			queryParamList.add(new TokenParam("apName",merchantName));
			queryParamList.add(new TokenParam("appName",appName));
			queryParamList.add(new TokenParam("chargeName",productName));
			queryParamList.add(new TokenParam("price",price+""));
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
			map.put("price",price+"");
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
	
	//掌游
	private JSONObject generateTySpaceZyOrder(String outTradeNo, String appName, String merchantName, String productName, Integer appId, Integer merchantId, Integer sellerId, String key, String secret, Integer price, String timestamp, String imsi, String chargeType) {
		JSONObject returnJsonMsgObj = null;
		try {
			String orderNo = DateUtil.getCurrentTimestamp("yyyyMMddHHmmssSSS") + Math.round(Math.random() * 1000);
			String result = generateTySpaceZyOrder(appName, merchantName, productName, key, secret, imsi, timestamp, orderNo, price, chargeType);
			
			JSONObject res = JSONObject.parseObject(result);
			Integer code = res.getInteger("resultCode");//订单创建结果 0-成功 1-失败
			String resultDesc = res.getString("resultDesc");//描述
			if (code == 0) {
//				String senderNumber = res.getString("smsNum");
//				String msgContent = res.getString("smsContent");
				
				//创建订单
				TOpenOrder tOpenOrder = new TOpenOrder();
				tOpenOrder.setImsi(imsi);
				tOpenOrder.setOrderId(orderNo);
				tOpenOrder.setOutTradeNo(outTradeNo);
				tOpenOrder.setAppId(appId);
				tOpenOrder.setMerchantId(merchantId);
				tOpenOrder.setSubject(appName);
				tOpenOrder.setSenderNumber("118030");
				tOpenOrder.setMsgContent(resultDesc);
				tOpenOrder.setFee(price);
				tOpenOrder.setSellerId(sellerId);
				openOrderManager.save(tOpenOrder);
				
				returnJsonMsgObj = new JSONObject();
				returnJsonMsgObj.put("order_id", orderNo);
				returnJsonMsgObj.put("out_trade_no", outTradeNo);
				returnJsonMsgObj.put("fee", price);
//				returnJsonMsgObj.put("sender_number", senderNumber);
//				returnJsonMsgObj.put("message_content", msgContent);
				returnJsonMsgObj.put("resultCode", code);
				returnJsonMsgObj.put("resultDesc", resultDesc);
			} else {
				returnJsonMsgObj = new JSONObject();
				returnJsonMsgObj.put("order_id", orderNo);
				returnJsonMsgObj.put("out_trade_no", outTradeNo);
				returnJsonMsgObj.put("fee", price);
				returnJsonMsgObj.put("resultCode", code);
				returnJsonMsgObj.put("resultDesc", resultDesc);
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return returnJsonMsgObj;
	}
	
	//掌游新
	private String generateTySpaceZyOrder(String appName, String merchantName, String productName, String channel, String secret, String imsi, String timestamp, String orderId, Integer price, String chargeType) {
		String responseString = null;
		try {
			List<TokenParam> queryParamList = new ArrayList<TokenParam>();
			queryParamList.add(new TokenParam("ver", "1.0"));
			queryParamList.add(new TokenParam("channel",channel));
			queryParamList.add(new TokenParam("imsi",imsi));
			queryParamList.add(new TokenParam("apName",merchantName));
			queryParamList.add(new TokenParam("appName",appName));
			queryParamList.add(new TokenParam("chargeName",productName));
			queryParamList.add(new TokenParam("price",price+""));
			queryParamList.add(new TokenParam("chargeType",chargeType));
			queryParamList.add(new TokenParam("timestamp", timestamp));
			queryParamList.add(new TokenParam("orderId", orderId));
//			String sig = TokenService.buildTySpaceToken(queryParamList, secret);
			
			Map<String, String> map = new HashMap<String, String>();
			map.put("ver","1.0");
			map.put("channel",channel);
			map.put("imsi",imsi);
			map.put("apName",merchantName);
			map.put("appName",appName);
			map.put("chargeName",productName);
			map.put("price",price+"");
			map.put("chargeType",chargeType);
			map.put("timestamp",timestamp);
			map.put("orderId",orderId);
			map.put("sig","test");
			responseString = HttpClientUtils.simplePostInvoke("http://139.196.252.159:8178/HttpAPI", map);
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return responseString;
	}
	
	//生成爱音乐订单
//	private JSONObject generateIMusicOrder(String outTradeNo, String appName, String merchantName, String productName, Integer appId, Integer merchantId, Integer sellerId, String key, String secret, Integer price, String timestamp, String imsi, String chargeType) {
//		JSONObject returnJsonMsgObj = null;
//		try {
//			String orderNo = DateUtil.getCurrentTimestamp("yyyyMMddHHmmssSSS") + Math.round(Math.random() * 1000);
//			String result = generateIMusicOrder(appName, merchantName, productName, key, secret, imsi, timestamp, orderNo, price, chargeType);
//			
//			JSONObject res = JSONObject.parseObject(result);
//			Integer code = res.getInteger("resultCode");//订单创建结果 0-成功 1-失败
//			if (code == 0) {
//				String senderNumber = res.getString("smsNum");
//				String msgContent = res.getString("smsContent");
//				
//				//创建订单
//				TOpenOrder tOpenOrder = new TOpenOrder();
//				tOpenOrder.setImsi(imsi);
//				tOpenOrder.setOrderId(orderNo);
//				tOpenOrder.setOutTradeNo(outTradeNo);
//				tOpenOrder.setAppId(appId);
//				tOpenOrder.setMerchantId(merchantId);
//				tOpenOrder.setSubject(appName);
//				tOpenOrder.setSenderNumber(senderNumber);
//				tOpenOrder.setMsgContent(msgContent);
//				tOpenOrder.setFee(price);
//				tOpenOrder.setSellerId(sellerId);
//				openOrderManager.save(tOpenOrder);
//				
//				returnJsonMsgObj = new JSONObject();
//				returnJsonMsgObj.put("order_id", orderNo);
//				returnJsonMsgObj.put("out_trade_no", outTradeNo);
//				returnJsonMsgObj.put("fee", price);
//				returnJsonMsgObj.put("sender_number", senderNumber);
//				returnJsonMsgObj.put("message_content", msgContent);
//			}
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//		}
//		return returnJsonMsgObj;
//	}
//	
//	private String generateIMusicOrder(String appName, String merchantName, String productName, String channel, String secret, String imsi, String timestamp, String orderId, Integer price, String chargeType) {
//		String responseString = null;
//		try {
//			List<TokenParam> queryParamList = new ArrayList<TokenParam>();
//			queryParamList.add(new TokenParam("ver", "1.0"));
//			queryParamList.add(new TokenParam("channel",channel));
//			queryParamList.add(new TokenParam("imsi",imsi));
//			queryParamList.add(new TokenParam("apName",merchantName));
//			queryParamList.add(new TokenParam("appName",appName));
//			queryParamList.add(new TokenParam("chargeName",productName));
//			queryParamList.add(new TokenParam("price",price+""));
//			queryParamList.add(new TokenParam("chargeType",chargeType));
//			queryParamList.add(new TokenParam("timestamp", timestamp));
//			queryParamList.add(new TokenParam("orderId", orderId));
//			String sig = TokenService.buildTySpaceToken(queryParamList, secret);
//			
//			Map<String, String> map = new HashMap<String, String>();
//			map.put("ver","1.0");
//			map.put("channel",channel);
//			map.put("imsi",imsi);
//			map.put("apName",merchantName);
//			map.put("appName",appName);
//			map.put("chargeName",productName);
//			map.put("price",price+"");
//			map.put("chargeType",chargeType);
//			map.put("timestamp",timestamp);
//			map.put("orderId",orderId);
//			map.put("sig",sig);
//			responseString = HttpClientUtils.simplePostInvoke("http://gwpay.52yole.com:9168/API.RequestPay", map);
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//		}
//		return responseString;
//	}
	
	/**
	 * 生成易信订单
	 * @return
	 */
	private JSONObject generateYiXinOrder(String outTradeNo, String appName, Integer appId, Integer merchantId, Integer sellerId, Integer price, String imsi, String phone, String channelId, Integer chargeType) {
		JSONObject returnJsonMsgObj = null;
		try {
			String orderNo = DateUtil.getCurrentTimestamp("yyyyMMddHHmmssSSS") + Math.round(Math.random() * 1000);
			String result = generateYiXinOrder(orderNo, appName, price, phone, channelId);
			
			JSONObject res = JSONObject.parseObject(result);
			Integer code = res.getInteger("returnCode");//订单创建结果 0-成功 1-失败
			if (code == 0) {
				String senderNumber = res.getString("smsNo");
				String msgContent = res.getString("smsCnt");
				
				//创建订单
				TOpenOrder tOpenOrder = new TOpenOrder();
				tOpenOrder.setImsi(imsi);
				tOpenOrder.setOrderId(orderNo);
				tOpenOrder.setOutTradeNo(outTradeNo);
				tOpenOrder.setSellerId(sellerId);
				tOpenOrder.setAppId(appId);
				tOpenOrder.setMerchantId(merchantId);
				tOpenOrder.setSubject(appName);
				tOpenOrder.setSenderNumber(senderNumber);
				tOpenOrder.setMsgContent(msgContent);
				tOpenOrder.setFee(price);
				tOpenOrder.setPayPhone(phone);
				TMobileArea mobileArea = mobileAreaManager.getMobileArea(phone);
				if (!Utils.isEmpty(mobileArea)) {
					tOpenOrder.setProvince(mobileArea.getProvince());
				}
				openOrderManager.save(tOpenOrder);
				
				returnJsonMsgObj = new JSONObject();
				returnJsonMsgObj.put("order_id", orderNo);
				returnJsonMsgObj.put("out_trade_no", outTradeNo);
				returnJsonMsgObj.put("fee", price);
				returnJsonMsgObj.put("sender_number", senderNumber);
				returnJsonMsgObj.put("message_content", msgContent);
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		
		return returnJsonMsgObj;
	}
	
	private String generateYiXinOrder(String orderId, String appName, Integer price, String phone, String channelId) {
		String res = null;
		try {
			Map<String, String> map = new HashMap<String, String>();
			map.put("phoneNo",phone);
			map.put("price",price+"");
			map.put("channelId",channelId);
			map.put("custom",orderId);
			map.put("spId", "10001");
			map.put("appName", appName);
			
			res = HttpClientUtils.simplePostInvoke("http://115.29.228.133:8082/lereader/pay/unifiedOrder/25", map);
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return res;
	}
	
	//回调地址
	public void callBack() {
		String result = ServletRequestUtils.getStringParameter(request, "result", null);
		String imsi = ServletRequestUtils.getStringParameter(request, "imsi", null);
		String orderId = ServletRequestUtils.getStringParameter(request, "orderId", null);
		String mobile = ServletRequestUtils.getStringParameter(request, "mobile", null);
		String payTime = ServletRequestUtils.getStringParameter(request, "payTime", null);
		try {
			LogUtil.log("general回调参数: result:"+result+" imsi:"+imsi+" orderId:"+orderId+" mobile:"+mobile+" payTime:"+payTime);
			String success = "fail";
			TOpenOrder tOpenOrder = openOrderManager.getOpenOrderByProperty("orderId", orderId);
			if (!Utils.isEmpty(tOpenOrder) && "1".equals(tOpenOrder.getStatus())) {
				//status转换
				String statusChange = "9999";
				if ("0".equals(result)) {
					statusChange = "3";
				} else {
					statusChange = "4";
				}
				
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				tOpenOrder.setStatus(statusChange);
				tOpenOrder.setPayTime(sdf.parse(payTime));
				String payPhone = imsiMdnRelationManager.getPhone(imsi);
				payPhone = payPhone == null ? mobile : payPhone;
				if (!Utils.isEmpty(payPhone)) {
					TMobileArea mobileArea = mobileAreaManager.getMobileArea(payPhone);
					if (!Utils.isEmpty(mobileArea)) {
						tOpenOrder.setProvince(mobileArea.getProvince());
					}
					tOpenOrder.setPayPhone(payPhone);
				} else {
					tOpenOrder.setPayPhone(imsi);
				}
				openOrderManager.save(tOpenOrder);
				
				//增加今日量
				if ("0".equals(result)) {
					openSellerManager.saveOpenSellerApps(tOpenOrder.getSellerId(), tOpenOrder.getAppId(), tOpenOrder.getFee());
				}
				
				success = "success";
				//回调渠道
				TOpenSeller tOpenSeller = openSellerManager.get(tOpenOrder.getSellerId());
				String callbackUrl = tOpenSeller.getCallbackUrl();
				if (!Utils.isEmpty(callbackUrl)) {
					String orderid = tOpenOrder.getOrderId();
					String outTradeNo = tOpenOrder.getOutTradeNo();
					String price = tOpenOrder.getFee()+"";
					new Thread(new SendPartner(statusChange,orderid,outTradeNo,price,callbackUrl)).start();
				}
				
			} else {
				success = "fail";
			}
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
				jsonObject.put("order_no", orderId);
				jsonObject.put("out_trade_no", outTradeNo);
				jsonObject.put("fee", fee);
				jsonObject.put("status", status);
				
				if (!Utils.isEmpty(callbackUrl)) {
			        LogUtil.log("sendChannelTySpaceGNMsg:"+jsonObject.toString());
		        	HttpClientUtils.postJson(callbackUrl, jsonObject.toString());
				}
			} catch (Exception e) {
				LogUtil.error(e.getMessage(), e);
			}
			
		}
	}
	
}
