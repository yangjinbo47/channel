package com.tenfen.www.action.external.pack;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;
import com.tenfen.cache.CacheFactory;
import com.tenfen.cache.services.ICacheClient;
import com.tenfen.entity.operation.pack.PushPackage;
import com.tenfen.entity.operation.pack.PushPackageLimit;
import com.tenfen.entity.operation.pack.TOrder;
import com.tenfen.entity.operation.pack.TPushSeller;
import com.tenfen.entity.operation.pack.TPushSellerPackages;
import com.tenfen.entity.system.ImsiMdnRelation;
import com.tenfen.util.CTUtil;
import com.tenfen.util.HttpClientUtils;
import com.tenfen.util.LogUtil;
import com.tenfen.util.StringUtil;
import com.tenfen.util.Utils;
import com.tenfen.util.encrypt.MD5;
import com.tenfen.util.servlet.ServletRequestUtils;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.common.Constants;
import com.tenfen.www.service.operation.BlackListManager;
import com.tenfen.www.service.operation.MobileAreaManager;
import com.tenfen.www.service.operation.pack.OrderManager;
import com.tenfen.www.service.operation.pack.PackageManager;
import com.tenfen.www.service.operation.pack.PushSellerManager;
import com.tenfen.www.service.system.ImsiMdnRelationManager;
import com.tenfen.www.util.tyyd.TyreadUtil;
import com.tenfen.www.util.tyydclient.HttpSendClient;
import com.tenfen.www.util.tyydclient.HttpSendRequest;
import com.tenfen.www.util.tyydclient.HttpSendResponse;

public class PackAction extends SimpleActionSupport {

	private static final long serialVersionUID = 3205177939227033736L;

	private static Log visitLog = LogFactory.getLog("visitLog");
	@Autowired
	private BlackListManager blackListManager;
	@Autowired
	private OrderManager orderManager;
	@Autowired
	private PackageManager packageManager;
	@Autowired
	private MobileAreaManager mobileAreaManager;
	@Autowired
	private ImsiMdnRelationManager imsiMdnRelationManager;
	@Autowired
	private CacheFactory cacheFactory;
	@Autowired
	private PushSellerManager pushSellerManager;

	public static String read_package_pre = "http://wap.tyread.com";
	public static String sound_read_package_pre = "http://wap.tyread.com:8080";

//	public void createOrder() {
//		ICacheClient mc = cacheFactory.getCommonCacheClient();
//		JSONObject json = new JSONObject();
//		String code = null;
//		String msg = null;
//		String comingKey = null;
//		try {
//			String imsi = ServletRequestUtils.getStringParameter(request, "imsi", null);
//			String channel = ServletRequestUtils.getStringParameter(request, "channel");
//			int price = ServletRequestUtils.getIntParameter(request, "price");
//			String outTradeNo = ServletRequestUtils.getStringParameter(request, "out_trade_no", "");
//			String phone = ServletRequestUtils.getStringParameter(request, "phone", null);
//			
//			//代码屏蔽
////			boolean cut = true;
////			if (cut) {
////				json.put("code", "2000");
////				json.put("msg", "基地返回码错误");
////				StringUtil.printJson(response, json.toString());
////				return;
////			}
//			
//			comingKey = "coming_"+imsi;
//			Boolean coming = (Boolean)mc.getCache(comingKey);
//			if (!Utils.isEmpty(coming)) {
//				json.put("code", "1007");
//				json.put("msg", imsi+"支付中,不可重复提交");
//				StringUtil.printJson(response, json.toString());
//				return;
//			} else {
//				mc.setCache(comingKey, true, 5*CacheFactory.MINUTE);
//			}
//			
//			TyreadUtil tyu = new TyreadUtil(cacheFactory, blackListManager, orderManager, packageManager, mobileAreaManager);
//			String province = null;
//			
//			//从本地库中获取
//			ImsiMdnRelation imsiMdnRelation = imsiMdnRelationManager.getEntityByProperty("imsi", imsi);
//			if (imsiMdnRelation != null) {
//				phone = imsiMdnRelation.getPhoneNum();
//			}
//			//从接口获取号码
//			if (phone == null || phone.length() == 0) {
//				phone = CTUtil.queryPhoneByIMSI(imsi);
//				if (!Utils.isEmpty(phone)) {
//					imsiMdnRelation = new ImsiMdnRelation();
//					imsiMdnRelation.setImsi(imsi);
//					imsiMdnRelation.setPhoneNum(phone);
//					imsiMdnRelationManager.save(imsiMdnRelation);
//				}
//			}
//			if (!Utils.isEmpty(phone)) {
//				province = tyu.searchAreaByPhone(phone);
//			}
//			
//			LogUtil.log("packageInfo="+ imsi + "查询电话号码为：" + phone +",所在区域："+province);
//			if (Utils.isEmpty(phone)) {
//				code = "1001";
//				msg = "没有获取到您的imsi号对应的手机号码";
//			}
//
//			// 检查是否是黑名单用户
//			boolean success = tyu.checkPhoneIsBlack(phone);
//			if (success) {
//				code = "1002";
//				msg = "您属于黑名单用户";
//			}
//
//			// 检查该号码是否有过包月，如果包月数量不为0，则不能重复包月
//			Boolean isBaoyue = tyu.checkBaoyue(phone);
//			if (isBaoyue) {
//				code = "1003";
//				msg = "三个月内已有包月包订购记录";
//			}
//			
//			TPushSeller pushSeller = pushSellerManager.getPushSellerByProperty("sellerKey", channel);
//			if (Utils.isEmpty(pushSeller)) {
//				code = "1004";
//				msg = "该渠道信息不存在";
//			}
//			if (Constants.PACKAGE_SELLER_STATUS.FORBID.getValue().equals(pushSeller.getStatus())) {
//				code = "1005";
//				msg = "该渠道信息不存在";
//			}
//			
//			if (!Utils.isEmpty(code) && !Utils.isEmpty(msg)) {
//				json.put("code", code);
//				json.put("msg", msg);
//				StringUtil.printJson(response, json.toString());
//				return;
//			}
//			
//			Integer sellerId = pushSeller.getId();
//			if (Utils.isCTPhone(phone)) {
//				// 查询推送包月列表
//				List<TPushSellerPackages> sellerPackages = pushSeller.getSellerPackages();
//				if (sellerPackages.size() == 0) {
//					code = "1006";
//					msg = "未找到推送包月包信息";
//				}
//				
//				//加入自有包
////				TPushSellerPackages self = new TPushSellerPackages();
////				PushPackage selfPackage = packageManager.get(481);
////				self.setPushSeller(pushSeller);
////				self.setPushPackage(selfPackage);
////				sellerPackages.add(0,self);
//				
//				for (TPushSellerPackages tPushSellerPackages : sellerPackages) {
//					PushPackage pushPackage = tPushSellerPackages.getPushPackage();
//					if (price == pushPackage.getPrice() || 481 == pushPackage.getId()) {
//						boolean flag = packageValidate(tyu, tPushSellerPackages, province);//验证包月包状态
//						if (flag) {
//							LogUtil.log("packageInfo===="+phone + "进入" + pushPackage.getPackageName());
//							String amount = String.valueOf(pushPackage.getPrice());
//							String productId = pushPackage.getPackageUrl();
//							Integer fee = pushPackage.getPrice();
//							if (pushPackage.getType() == 1) {//天翼阅读
//								if (pushPackage.getPackageName().indexOf("图文") != -1) {//天翼图文
//									//创建tradeId
//									String tradeId = empOrderCreate(amount, productId, phone);
//									if (Utils.isEmpty(tradeId)) {
//										code = "1009";
//										msg = "黑名单用户";
//										break;
//									}
//									//发送验证码
//									boolean isSucc = empSecurityCodeFetch(tradeId, phone);
//									if (isSucc) {
//										createOrder(tradeId, outTradeNo, imsi, phone, pushPackage.getPackageName(), province, sellerId, pushPackage.getId(), fee);
//										
//										code = "1";
//										if (481 == pushPackage.getId()) {
//											json.put("price", 800);
//											json.put("package_name", "aaa");
//											json.put("monthProductId", "24283263");
//										} else {
//											json.put("price", pushPackage.getPrice());
//											json.put("package_name", pushPackage.getPackageName());
//											json.put("monthProductId", pushPackage.getPackageUrl());
//										}
//										json.put("tradeId", tradeId);
//										msg = "发送成功";
//										break;
//									} else {
//										code = "1010";
//										msg = "发送失败";
//										break;
//									}
//								} else {//天翼有声
//									//创建tradeId
//									String tradeId = empSoundOrderCreate(amount, productId, phone);
//									if (Utils.isEmpty(tradeId)) {
//										code = "1009";
//										msg = "黑名单用户";
//										break;
//									}
//									//发送验证码
//									boolean isSucc = empSecurityCodeFetch(tradeId, phone);
//									if (isSucc) {
//										createOrder(tradeId, outTradeNo, imsi, phone, pushPackage.getPackageName(), province, sellerId, pushPackage.getId(), fee);
//										
//										code = "1";
//										json.put("price", pushPackage.getPrice());
//										json.put("package_name", pushPackage.getPackageName());
//										json.put("tradeId", tradeId);
//										json.put("monthProductId", pushPackage.getPackageUrl());
//										msg = "发送成功";
//										break;
//									} else {
//										code = "1010";
//										msg = "发送失败";
//										break;
//									}
//								}
//							} else if (pushPackage.getType() == 2) {//爱游戏
//								//创建correlator
//								String tradeId = null;
//								String createJsonStr = iGameEmpOrderCreate(productId, phone);
//								JSONObject createJson = JSONObject.parseObject(createJsonStr);
//								Integer resultCode = createJson.getInteger("code");
//								if (resultCode == 0) {
//									String ext = createJson.getString("ext");
//									JSONObject extJson = JSONObject.parseObject(ext);
//									tradeId = extJson.getString("correlator");
//									
//									createOrder(tradeId, outTradeNo, imsi, phone, pushPackage.getPackageName(), province, sellerId, pushPackage.getId(), fee);
//									code = "1";
//									json.put("price", pushPackage.getPrice());
//									json.put("package_name", pushPackage.getPackageName());
//									json.put("tradeId", tradeId);
//									json.put("monthProductId", pushPackage.getPackageUrl());
//									msg = "发送成功";
//									break;
//								}
//							}
//						} else {
//							LogUtil.log(phone + "没进入:"+pushPackage.getPackageName());
//							code = "1007";
//							msg = "包月校验未通过";
//							continue;
//						}
//					}
//				}
//			}
//			
//			json.put("code", code);
//			json.put("msg", msg);
//			LogUtil.log("packageInfo===="+json.toString());
//			StringUtil.printJson(response, json.toString());
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//		} finally {
////			mc.deleteCache(comingKey);
//		}
//	}
	
	public void createOrder() {
		ICacheClient mc = cacheFactory.getCommonCacheClient();
		JSONObject json = new JSONObject();
		String code = null;
		String msg = null;
		String comingKey = null;
		try {
			String imsi = ServletRequestUtils.getStringParameter(request, "imsi", null);
			String channel = ServletRequestUtils.getStringParameter(request, "channel");
			int price = ServletRequestUtils.getIntParameter(request, "price");
			String outTradeNo = ServletRequestUtils.getStringParameter(request, "out_trade_no", "");
			String phone = ServletRequestUtils.getStringParameter(request, "phone", null);
			
			//用户进入打印日志
			visitLog.info("tyread package param:imsi="+imsi+",channel="+channel+",price="+price+",outTradeNo="+outTradeNo);
			
			//代码屏蔽
//			boolean cut = true;
//			if (cut) {
//				json.put("code", "2000");
//				json.put("msg", "基地返回码错误");
//				StringUtil.printJson(response, json.toString());
//				return;
//			}
			
			comingKey = "coming_"+imsi;
			Boolean coming = (Boolean)mc.getCache(comingKey);
			if (!Utils.isEmpty(coming)) {
				json.put("code", "1007");
				json.put("msg", imsi+"支付中,不可重复提交");
				StringUtil.printJson(response, json.toString());
				return;
			} else {
				mc.setCache(comingKey, true, 5*CacheFactory.MINUTE);
			}
			
			TyreadUtil tyu = new TyreadUtil(cacheFactory, blackListManager, orderManager, packageManager, mobileAreaManager);
			String province = null;
			
			//从本地库中获取
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
			if (!Utils.isEmpty(phone)) {
				province = tyu.searchAreaByPhone(phone);
			}
			
			LogUtil.log("packageInfo="+ imsi + "查询电话号码为：" + phone +",所在区域："+province);
			if (Utils.isEmpty(phone)) {
				code = "1001";
				msg = "没有获取到您的imsi="+imsi+"所对应的手机号码";
			}

			// 检查该号码是否有过包月，如果包月数量不为0，则不能重复包月
			Boolean isBaoyue = tyu.checkBaoyue(phone);
			if (isBaoyue) {
				code = "1002";
				msg = "三个月内已有包月包订购记录";
			}
			
			TPushSeller pushSeller = pushSellerManager.getPushSellerByProperty("sellerKey", channel);
			if (Utils.isEmpty(pushSeller)) {
				code = "1003";
				msg = "该渠道信息不存在";
			}
			if (Constants.PACKAGE_SELLER_STATUS.FORBID.getValue().equals(pushSeller.getStatus())) {
				code = "1004";
				msg = "该渠道被冻结";
			}
			
			if (!Utils.isEmpty(code) && !Utils.isEmpty(msg)) {
				json.put("code", code);
				json.put("msg", msg);
				StringUtil.printJson(response, json.toString());
				return;
			}
			
			Integer sellerId = pushSeller.getId();
			if (Utils.isCTPhone(phone)) {
				// 查询推送包月列表
				List<TPushSellerPackages> sellerPackages = pushSeller.getSellerPackages();
				if (sellerPackages.size() == 0) {
					code = "1005";
					msg = "未找到推送包月包信息";
				}
				
				//加入自有包
//				TPushSellerPackages self = new TPushSellerPackages();
//				PushPackage selfPackage = packageManager.get(481);
//				self.setPushSeller(pushSeller);
//				self.setPushPackage(selfPackage);
//				sellerPackages.add(0,self);
				
				for (TPushSellerPackages tPushSellerPackages : sellerPackages) {
					PushPackage pushPackage = tPushSellerPackages.getPushPackage();
					if (price == pushPackage.getPrice() || 481 == pushPackage.getId()) {
						boolean flag = packageValidate(tyu, tPushSellerPackages, province);//验证包月包状态
						if (flag) {
							LogUtil.log("packageInfo===="+phone + "进入" + pushPackage.getPackageName());
							String amount = String.valueOf(pushPackage.getPrice());
							String productId = pushPackage.getPackageUrl();
							Integer fee = pushPackage.getPrice();
							if (pushPackage.getType() == 1) {//天翼阅读
								if (pushPackage.getPackageName().indexOf("图文") != -1) {//天翼图文
									//创建tradeId
									String tradeId = empOrderCreate(amount, productId, phone);
									if (Utils.isEmpty(tradeId)) {
										code = "1009";
										msg = "黑名单用户";
										break;
									}
									//异步解析wap，发送验证码
									new Thread(new ParseThread(imsi, phone, productId, pushPackage.getPackageName(), fee, outTradeNo, sellerId, pushPackage.getId(), province, orderManager)).start();
									
									code = "1";
									json.put("imsi", imsi);
									json.put("price", pushPackage.getPrice());
									json.put("package_name", pushPackage.getPackageName());
									json.put("pushId", pushPackage.getId());
									msg = "发送成功";
									break;
								} else {//天翼有声
									//创建tradeId
									String tradeId = empSoundOrderCreate(amount, productId, phone);
									if (Utils.isEmpty(tradeId)) {
										code = "1009";
										msg = "黑名单用户";
										break;
									}
									//发送验证码
									boolean isSucc = empSecurityCodeFetch(tradeId, phone);
									if (isSucc) {
										createOrder(tradeId, outTradeNo, imsi, phone, pushPackage.getPackageName(), province, sellerId, pushPackage.getId(), fee);
										
										code = "1";
										json.put("price", pushPackage.getPrice());
										json.put("package_name", pushPackage.getPackageName());
										json.put("tradeId", tradeId);
										json.put("monthProductId", pushPackage.getPackageUrl());
										msg = "发送成功";
										break;
									} else {
										code = "1010";
										msg = "发送失败";
										break;
									}
								}
							} else if (pushPackage.getType() == 2) {//爱游戏
								//创建correlator
								String tradeId = null;
								String createJsonStr = iGameEmpOrderCreate(productId, phone);
								JSONObject createJson = JSONObject.parseObject(createJsonStr);
								Integer resultCode = createJson.getInteger("code");
								if (resultCode == 0) {
									String ext = createJson.getString("ext");
									JSONObject extJson = JSONObject.parseObject(ext);
									tradeId = extJson.getString("correlator");
									
									createOrder(tradeId, outTradeNo, imsi, phone, pushPackage.getPackageName(), province, sellerId, pushPackage.getId(), fee);
									code = "1";
									json.put("price", pushPackage.getPrice());
									json.put("package_name", pushPackage.getPackageName());
									json.put("tradeId", tradeId);
									json.put("monthProductId", pushPackage.getPackageUrl());
									msg = "发送成功";
									break;
								}
							}
						} else {
							LogUtil.log(phone + "没进入:"+pushPackage.getPackageName());
							code = "1006";
							msg = "包月校验未通过";
							continue;
						}
					}
				}
			}
			
			json.put("code", code);
			json.put("msg", msg);
			LogUtil.log("packageInfoRes===="+json.toString());
			StringUtil.printJson(response, json.toString());
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
//	public void pay() throws Exception {
//		ICacheClient mc = cacheFactory.getCommonCacheClient();
//		String monthProductId = ServletRequestUtils.getStringParameter(request, "monthProductId", null);
//		String indentifyCode = ServletRequestUtils.getStringParameter(request, "code", null);
//		String tradeId = ServletRequestUtils.getStringParameter(request, "tradeId", null);
//		
//		String comingKey = null;
//		JSONObject json = new JSONObject();
//		String code = null;
//		String msg = null;
//		String outTradeNo = null;
//		comingKey = "coming_"+tradeId;
//		Boolean coming = (Boolean)mc.getCache(comingKey);
//		if (!Utils.isEmpty(coming)) {
//			json.put("code", "1006");
//			json.put("msg", tradeId+"支付中,不可重复提交");
//			StringUtil.printJson(response, json.toString());
//			return;
//		} else {
//			mc.setCache(comingKey, true, 3*CacheFactory.MINUTE);
//		}
//		if (Utils.isEmpty(monthProductId)) {
//			code = "1001";
//			msg = "包月ID不能为空";
//		}
//		if (Utils.isEmpty(indentifyCode)) {
//			code = "1002";
//			msg = "验证码不能为空";
//		}
//		if (Utils.isEmpty(tradeId)) {
//			code = "1003";
//			msg = "交易ID不能为空";
//		}
//		
//		if (!Utils.isEmpty(code)) {
//			json.put("code", code);
//			json.put("msg", msg);
//			StringUtil.printJson(response, json.toString());
//			return;
//		}
//		
//		try {
//			//查看订单状态
//			TOrder order = orderManager.getByTradeId(tradeId);
//			if (Utils.isEmpty(order)) {
//				json.put("code", "1005");
//				json.put("msg", "没有查询到订单信息");
//				StringUtil.printJson(response, json.toString());
//				return;
//			}
//			String name = order.getName();
//			outTradeNo = order.getOutTradeNo();
//			Integer sellerId = order.getSellerId();
//			if (order.getStatus() == 1) {
//				String phone = order.getPhoneNum();
//				
//				Integer pushId = order.getPushId();//推送id
//				PushPackage pushPackage = packageManager.get(pushId);
//				Integer type = pushPackage.getType();
////				String channel = pushPackage.getRecChannel();
//				boolean isSucc = false;
//				if (type == 1) {//天翼阅读
//					isSucc = empSecurityCodeValidate(tradeId, indentifyCode, phone, name);
//				} else if (type == 2) {//爱音乐
//					isSucc = iGameEmpCodeValidate(monthProductId, phone, indentifyCode, tradeId);
//				}
//				
//				//是否扣量
//				double reduce_conf = pushPackage.getReduce()/(double)100;
//				double rate = new Random().nextDouble();
//				Integer reduce = 0;
//				if (rate < reduce_conf) {
//					reduce = 1;
//				}
//				if (isSucc) {
//					if (481 == order.getPushId()) {
//						updateOrder(sellerId, tradeId, 4, reduce);//t_order 强制为失败
//						//更新产品日限
//						packageManager.addTodayLimit(order.getPushId());
//						//保存入t_order_self
//						orderManager.saveToSelf(order);
//						code = "1004";
//						msg = "包月失败";
//					} else {
//						updateOrder(sellerId, tradeId, 3, reduce);
//						if (reduce == 1) {
//							code = "1004";
//							msg = "包月失败";
//						} else {
//							code = "1";
//							msg = "包月成功";
//						}
//					}
//					
//				} else {
//					updateOrder(sellerId, tradeId, 4, reduce);
//					
//					code = "1004";
//					msg = "包月失败";
//				}
//				json.put("code", code);
//				json.put("msg", msg);
//				json.put("out_trade_no", outTradeNo);
//				StringUtil.printJson(response, json.toString());
//			} else if (order.getStatus() == 3){
//				json.put("code", "1");
//				json.put("msg", "包月成功");
//				json.put("out_trade_no", outTradeNo);
//				StringUtil.printJson(response, json.toString());
//			} else if (order.getStatus() == 4){
//				json.put("code", "1004");
//				json.put("msg", "包月失败");
//				json.put("out_trade_no", outTradeNo);
//				StringUtil.printJson(response, json.toString());
//			}
//			LogUtil.log("packagePay result:"+json.toJSONString());
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//		}
//	}
	
	public void pay() throws Exception {
		ICacheClient mc = cacheFactory.getCommonCacheClient();
		String imsi = ServletRequestUtils.getStringParameter(request, "imsi", null);
		Integer pushId = ServletRequestUtils.getIntParameter(request, "pushId", 0);
		String indentifyCode = ServletRequestUtils.getStringParameter(request, "code", null);
		
		String comingKey = null;
		JSONObject json = new JSONObject();
		String code = null;
		String msg = null;
		String outTradeNo = null;
		comingKey = "coming_"+imsi+"_"+indentifyCode;
		Boolean coming = (Boolean)mc.getCache(comingKey);
		if (!Utils.isEmpty(coming)) {
			json.put("code", "1006");
			json.put("msg", imsi+"正在支付"+pushId+"产品,不可重复提交");
			StringUtil.printJson(response, json.toString());
			return;
		} else {
			mc.setCache(comingKey, true, 3*CacheFactory.MINUTE);
		}
		if (Utils.isEmpty(pushId)) {
			code = "1001";
			msg = "包月ID不能为空";
		}
		if (Utils.isEmpty(indentifyCode)) {
			code = "1002";
			msg = "验证码不能为空";
		}
		if (Utils.isEmpty(imsi)) {
			code = "1003";
			msg = "电话号码不能为空";
		}
		
		//从本地库中获取
		String phone = null;
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
			code = "1004";
			msg = "电话号码反查失败";
		}
		if (!Utils.isEmpty(code)) {
			json.put("code", code);
			json.put("msg", msg);
			StringUtil.printJson(response, json.toString());
			return;
		}
		
		try {
			//查看订单状态
			TOrder order = orderManager.findByPhoneAndPushId(phone, pushId);
			if (Utils.isEmpty(order)) {
				json.put("code", "1005");
				json.put("msg", "没有查询到订单信息");
				StringUtil.printJson(response, json.toString());
				return;
			}
			
			String name = order.getName();
			outTradeNo = order.getOutTradeNo();
			Integer sellerId = order.getSellerId();
			String tradeId = order.getTradeId();
			if (order.getStatus() == 1) {
				PushPackage pushPackage = packageManager.get(pushId);
				String monthProductId = pushPackage.getPackageUrl();
				Integer type = pushPackage.getType();
				
				boolean isSucc = false;
				if (type == 1) {//天翼阅读
					isSucc = empSecurityCodeValidate(tradeId, indentifyCode, phone, name);
				} else if (type == 2) {//爱音乐
					isSucc = iGameEmpCodeValidate(monthProductId, phone, indentifyCode, tradeId);
				}
				
				//是否扣量
				double reduce_conf = pushPackage.getReduce()/(double)100;
				double rate = new Random().nextDouble();
				Integer reduce = 0;
				if (rate < reduce_conf) {
					reduce = 1;
				}
				if (isSucc) {
					if (481 == order.getPushId()) {
						updateOrder(sellerId, tradeId, 4, reduce);//t_order 强制为失败
						//更新产品日限
						packageManager.addTodayLimit(order.getPushId());
						//保存入t_order_self
						orderManager.saveToSelf(order);
						code = "1006";
						msg = "包月失败";
					} else {
						updateOrder(sellerId, tradeId, 3, reduce);
						if (reduce == 1) {
							code = "1006";
							msg = "包月失败";
						} else {
							code = "1";
							msg = "包月成功";
						}
					}
					
				} else {
					updateOrder(sellerId, tradeId, 4, reduce);
					
					code = "1006";
					msg = "包月失败";
				}
				json.put("code", code);
				json.put("msg", msg);
				json.put("out_trade_no", outTradeNo);
				StringUtil.printJson(response, json.toString());
			} else if (order.getStatus() == 3){
				json.put("code", "1");
				json.put("msg", "包月成功");
				json.put("out_trade_no", outTradeNo);
				StringUtil.printJson(response, json.toString());
			} else if (order.getStatus() == 4){
				json.put("code", "1006");
				json.put("msg", "包月失败");
				json.put("out_trade_no", outTradeNo);
				StringUtil.printJson(response, json.toString());
			}
			LogUtil.log("packagePay result:"+json.toJSONString());
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
	/**
	 * @功能：解析包月页面，得到确认订购地址
	 * @author BOBO
	 * @date 2013-7-11
	 * @param url
	 * @return
	 */
//	public String analysisUrl(String text) {
//		String returnString = RegExp.getString(text,
//				"(?<=href=\")(.*?)(?=\".*>订购包月)");
//		return returnString;
//	}
	
//	public String[] analysisOrder(String sessionId, String url, String phone, String refererUrl,
//			String ua) {
//		String[] array = new String[3];
//		if (phone == null) {
//			return array;
//		}
//		HttpClient httpClient = null;
//		try {
//			httpClient = new DefaultHttpClient();
//			httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);//设置连接超时时间为10秒
//			httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 10000);//设置无响应超时时间为10秒
//			HttpGet httpGet = new HttpGet(url);
//			httpGet.setHeader("X-Up-Calling-Line-ID", phone);
//			httpGet.setHeader("User-Agent", ua);
//			httpGet.setHeader("X-Forwarded-For","10.8.70.176");
//			httpGet.setHeader("X-Source-ID","10.234.86.39");
//			httpGet.setHeader("X-Real-Ip","61.130.246.71");
//			httpGet.setHeader("Referer", refererUrl);
//			if(null != sessionId){
//				httpGet.setHeader("Cookie", "JSESSIONID=" + sessionId);
//	        }
//
//			String urlCode = null;
//			String tradeId = null;
//			HttpResponse response = httpClient.execute(httpGet);
//			if (response.getStatusLine().getStatusCode() == 200) {
//				String responseString = EntityUtils.toString(response.getEntity());
//
//				urlCode = RegExp.getString(responseString,
//						"(?<=name=\"urlCode\" value=\")(.*?)(?=\">)");
//				tradeId = RegExp.getString(responseString,
//						"(?<=name=\"tradeId\" value=\")(.*?)(?=\">)");
//				
//				array[0] = urlCode;
//				array[1] = tradeId;
//			}
//			
//			httpGet = new HttpGet("http://wap.tyread.com/user/getIdentifyCode.action?urlCode="+urlCode+"&tradeId="+tradeId);
//			response = httpClient.execute(httpGet);
//			if (response.getStatusLine().getStatusCode() == 200) {
//				String returnString = EntityUtils.toString(response.getEntity());
//				array[2] = returnString;
//			}
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//		} finally {
//			if (httpClient != null) {
//				httpClient.getConnectionManager().shutdown();
//			}
//		}
//		return array;
//	}
	
	/**
	 * @功能：包月
	 * @author BOBO
	 * @date 2013-7-11
	 * @param url
	 * @param phone
	 * @return
	 */
//	public String tyReadOrder(String url, String phone, String refererUrl,
//			String imsi, String ua) {
//		String returnString = null;
//		if (phone == null) {
//			return returnString;
//		}
//		HttpClient httpClient = null;
//		HttpClient httpClient1 = null;
//		try {
//			// 发起wap确认订购请求
//			httpClient = new DefaultHttpClient();
//			httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);//设置连接超时时间为10秒
//			httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 10000);//设置无响应超时时间为10秒
//			HttpPost httpPost = new HttpPost(url);
//			httpPost.setHeader("x-up-calling-line-id", phone);
//			httpPost.setHeader("User-Agent", ua);
//			httpPost.setHeader("Referer", refererUrl);
//			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
//			nvps.add(new BasicNameValuePair("pb", "1"));
//			httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
//			httpClient.execute(httpPost);
//
//			String monthProductId = StringUtils.substringBetween(url,
//					"monthProductId=", "&");
//			String qdid = StringUtils.substringBetween(refererUrl, "qdid=", "&");
//			// 向接口获取userid
//			String userId = TyReadXfUtil.getUserId(phone, imsi, ua);
//			// 向接口获取订购url并解析成对象
//			Product p = TyReadXfUtil.getProduct(phone, userId, monthProductId,
//					imsi, ua);
//			// 请求拼装后的订购地址
//			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
//			String time = "b" + sdf.format(new Date());
//			String ssoUrl = p.getSsoAddr() + "?SID=" + p.getSid() + "&SPID="
//					+ p.getSpid() + "&jfType=-1&colId=" + p.getMonthProductId()
//					+ "&tmpidp=" + p.getTmpidp() + "&tmpidsp=" + p.getTmpidsp()
//					+ "&LogTmp=" + time + "&CommandID=1&IDtype=0&userId="
//					+ userId + "&token=" + p.getToken() + "&channel=15&qdid="+qdid;
//			LogUtil.log("订购url:" + ssoUrl);
//			httpClient1 = new DefaultHttpClient();
//			httpClient1.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);//设置连接超时时间为10秒
//			httpClient1.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 10000);//设置无响应超时时间为10秒
//			HttpGet httpGet = new HttpGet(ssoUrl);
//			httpGet.setHeader("x-up-calling-line-id", phone);
//			httpPost.setHeader("User-Agent", ua);
//			httpGet.setHeader("Referer", url);
//			HttpResponse response1 = httpClient1.execute(httpGet);
//			if (response1.getStatusLine().getStatusCode() == 200) {
//				returnString = EntityUtils.toString(response1.getEntity());
//			}
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//		} finally {
//			if (httpClient != null) {
//				httpClient.getConnectionManager().shutdown();
//			}
//			if (httpClient1 != null) {
//				httpClient1.getConnectionManager().shutdown();
//			}
//		}
//		return returnString;
//	}
	
	/**
	 * 验证包月可用状态
	 * @param tyu
	 * @param pushPackage
	 * @param province
	 * @return true - 可用
	 * 		   false - 不可用
	 */
	public boolean packageValidate(TyreadUtil tyu, TPushSellerPackages tPushSellerPackages, String province) {
		boolean ret = true;
		try {
			ICacheClient mc = cacheFactory.getCommonCacheClient();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			
			double rate = new Random().nextDouble();
			PushPackage pushPackage = tPushSellerPackages.getPushPackage();
			if (481 == pushPackage.getId() && rate > 0.2) {
				return false;
			}
			//该渠道设置的包推广量是否到达
			if (tPushSellerPackages.getPackageLimit() != -1) {//不限量
				if (tPushSellerPackages.getPackageToday() >= tPushSellerPackages.getPackageLimit()) {
					return false;
				}
			}
			
			Integer packageId = pushPackage.getId();
			//判断产品状态是否正常
			int status = pushPackage.getStatus();
			if (status == Constants.PACKAGE_STATUS.NORMAL.getValue()) {
				ret = true;
			} else {
				ret = false;
			}
			
			if (ret) {
				//判断产品省是否到量
				PushPackageLimit pushPackageLimit = packageManager.findPackageLimitByProperty(packageId, province);
				Integer packagedaylimit_conf = pushPackageLimit.getDayLimit();
				if (packagedaylimit_conf == -1) {//不限量
					ret = true;
				} else {
					String provinceEncoder = URLEncoder.encode(province, "UTF-8");
//					Integer provNums = (Integer) mc.getCache("limit_" + sellerId + "_" + packageId + "_" + provinceEncoder + "_" + sdf.format(new Date()));//分省包月数量
					Integer provNums = (Integer) mc.getCache("limit_" + packageId + "_" + provinceEncoder + "_" + sdf.format(new Date()));//分省包月数量
					if (Utils.isEmpty(provNums)) {
						Calendar calendar = Calendar.getInstance();
						SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
						SimpleDateFormat sdfSql = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//格式化时间
						//获取当日时间区间
						String startString = format.format(calendar.getTime()) + " 00:00:00";
						Date startDate = sdfSql.parse(startString);
						java.sql.Date start = new java.sql.Date(startDate.getTime());
						
						String endString = format.format(calendar.getTime()) + " 23:59:59";
						Date endDate = sdfSql.parse(endString);
						java.sql.Date end = new java.sql.Date(endDate.getTime());
//						provNums = orderManager.getBaoyueCount(packageId, province, channel, start, end);
//						mc.setCache("limit_" + sellerId + "_" + packageId + "_" + provinceEncoder + "_" + sdf.format(new Date()), provNums, CacheFactory.DAY);//分省包月量
						provNums = orderManager.getPackageCount(packageId, province, start, end, Constants.T_ORDER_STATUS.SUCCESS.getValue());
						mc.setCache("limit_" + packageId + "_" + provinceEncoder + "_" + sdf.format(new Date()), provNums, CacheFactory.DAY);//分省包月量
					}
					if (provNums >= packagedaylimit_conf) {
						ret = false;
					}
				}
			}
			
			//判断产品全国是否到量
			if (ret) {
				Integer limit = tyu.getProductLimit(packageId);
				if (limit == null) {
					limit = 0;
				}
				if (limit < 1) {
					LogUtil.log(pushPackage.getPackageName()+"已达到推送量，剩余" + limit);
					ret = false;
				}
			}
			
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
			ret = false;
		}
		
		return ret;
	}
	
	public void createOrder(String tradeId, String outTradeNo, String imsi, String phone, String packageName, String province, Integer sellerId, Integer pushId, Integer fee){
		TOrder order = new TOrder();
		order.setTradeId(tradeId);
		order.setOutTradeNo(outTradeNo);
		order.setImsi(imsi);
		order.setPhoneNum(phone);
		order.setName(packageName);
		order.setSellerId(sellerId);
//		order.setChannel(channel);
		order.setProvince(province);
		order.setPushId(pushId);
		order.setFee(fee);
		// 保存订单(批量)并增加当日订购数
		orderManager.save(order);
	}
	
	/**
	 * 更新订单
	 * @param channel
	 * @param tradeId
	 * @param status
	 * @param reduce - 是否扣量 0-不扣 1-扣量
	 */
	public void updateOrder(Integer sellerId, String tradeId, Integer status, Integer reduce) {
		try {
			ICacheClient mc = cacheFactory.getCommonCacheClient();
			
			TOrder order = orderManager.getByTradeId(tradeId);
			if (!Utils.isEmpty(order) && order.getStatus() == 1 && status == 3) {
				order.setStatus(status);
				// 保存订单(批量)并增加当日订购数
//				orderManager.batchSave(order);
				
				if (reduce == 1) {
					order.setReduce(1);//扣量
				}
				orderManager.save(order);
				packageManager.addTodayLimit(order.getPushId());//增加产品量
				pushSellerManager.addPushSellerPackages(sellerId, order.getPushId());//增加渠道量
				
				Integer pushId = order.getPushId();
				String phoneNum = order.getPhoneNum();
				// 更新总量缓存
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
//				Integer limit = (Integer) mc.getCache("limit_" + sellerId + "_" + pushId + "_"+ sdf.format(new Date()));
				Integer limit = (Integer) mc.getCache("limit_" + pushId + "_"+ sdf.format(new Date()));
				if (Utils.isEmpty(limit)) {
					PushPackage pushPackage = packageManager.get(pushId);
					int package_limit = pushPackage.getPackageLimit();
					int package_today = pushPackage.getPackageToday();
					limit = package_limit-package_today-1 < 0 ? 0 : package_limit-package_today-1;
//					mc.setCache("limit_" + sellerId + "_" + pushId + "_"+ sdf.format(new Date()), limit, CacheFactory.DAY);// 包月剩余量
					mc.setCache("limit_" + pushId + "_"+ sdf.format(new Date()), limit, CacheFactory.DAY);// 包月剩余量
				} else {
					limit = limit - 1 < 0 ? 0 : limit - 1;
//					mc.setCache("limit_" + sellerId + "_" + pushId + "_"+ sdf.format(new Date()), limit, CacheFactory.DAY);// 包月剩余量
					mc.setCache("limit_" + pushId + "_"+ sdf.format(new Date()), limit, CacheFactory.DAY);// 包月剩余量
				}
				// 更新分省缓存
				String province = order.getProvince();
				String provinceEncoder = URLEncoder.encode(province, "UTF-8");
//				Integer provNums = (Integer) mc.getCache("limit_" + sellerId + "_" + pushId + "_" + provinceEncoder + "_" + sdf.format(new Date()));//分省包月数量
				Integer provNums = (Integer) mc.getCache("limit_" + pushId + "_" + provinceEncoder + "_" + sdf.format(new Date()));//分省包月数量
				if (Utils.isEmpty(provNums)) {
					Calendar calendar = Calendar.getInstance();
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
					SimpleDateFormat sdfSql = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//格式化时间
					//获取当日时间区间
					String startString = format.format(calendar.getTime()) + " 00:00:00";
					Date startDate = sdfSql.parse(startString);
					java.sql.Date start = new java.sql.Date(startDate.getTime());
					
					String endString = format.format(calendar.getTime()) + " 23:59:59";
					Date endDate = sdfSql.parse(endString);
					java.sql.Date end = new java.sql.Date(endDate.getTime());
//					Integer nums = orderManager.getBaoyueCount(sellerId, pushId, province, start, end);
//					mc.setCache("limit_" + sellerId + "_" + pushId + "_" + provinceEncoder + "_" + sdf.format(new Date()), nums, CacheFactory.DAY);//分省包月量
					provNums = orderManager.getPackageCount(pushId, province, start, end, Constants.T_ORDER_STATUS.SUCCESS.getValue());
					mc.setCache("limit_" + pushId + "_" + provinceEncoder + "_" + sdf.format(new Date()), provNums, CacheFactory.DAY);//分省包月量
				} else {
					provNums = provNums + 1;
					mc.setCache("limit_" + sellerId + "_" + pushId + "_" + provinceEncoder + "_" + sdf.format(new Date()), provNums, CacheFactory.DAY);//分省包月量
				}
				// 删除用户是否包月检测缓存
				String key = "check_baoyue_" + phoneNum;
				mc.deleteCache(key);
			} else if (!Utils.isEmpty(order) && order.getStatus() == 1 && status == 4) {
				order.setStatus(status);
				orderManager.save(order);
			}
			
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
	private final String key = "7513fa363b00107a";
	private static String url = "http://61.130.247.175:80/portalapi/portalapi";
	private String empOrderCreate(String price, String productId, String phone) {
		String orderId = null;
		try {
			Map<String, String> content = new HashMap<String ,String>();
			Map<String, String> header = new HashMap<String ,String>();

			HttpSendRequest request = new HttpSendRequest();
			request.setContent(content);
			request.setHeads(header);

			request.setUrl(url);
			request.setCharset("UTF-8");
			request.setTimeout(50000);

			header.put("Client-Agent", "TYYD_Android_4_0_1024_800_HW_C8812_JAVA_2_9_8/480*640/public");
			header.put("userAccount",phone);
			header.put("action", "empOrderCreate");
			
			String baoyue = "<Request>\n" +
					"<EmpOrderCreateReq>\n" +
					"<type>2</type>\n" +
					"<payFee>"+price+"</payFee>\n" +
					"<costPhone>"+phone+"</costPhone>\n" +
					"<productId>"+productId+"</productId>\n" +
					"<token>"+sort(new String[]{"2",price,productId,phone},key)+"</token>\n" +
					"</EmpOrderCreateReq>\n" +
					"</Request>\n";
			
			request.getContent().put("content", baoyue);
			HttpSendClient httpSendClient = new HttpSendClient();
			HttpSendResponse culverinResponse = httpSendClient.executeHttpPost(request);
			if (!Utils.isEmpty(culverinResponse)) {
				orderId = StringUtils.substringBetween(culverinResponse.getResponseBody(), "<orderId>", "</orderId>");
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return orderId;
	}
	//创建订单
	private String empSoundOrderCreate(String price, String productId, String phone) {
		String orderId = null;
		try {
			Map<String, String> content = new HashMap<String ,String>();
			Map<String, String> header = new HashMap<String ,String>();

			HttpSendRequest request = new HttpSendRequest();
			request.setContent(content);
			request.setHeads(header);

			request.setUrl(url);
			request.setCharset("UTF-8");
			request.setTimeout(50000);

			header.put("Client-Agent", "TYYD_Android_4_0_1024_800_HW_C8812_JAVA_2_9_8/480*640/public");
			header.put("userAccount",phone);
			header.put("action", "empOrderCreate");
			
			String baoyue = "<Request>\n" +
					"<EmpOrderCreateReq>\n" +
					"<type>2</type>\n" +
					"<payFee>"+price+"</payFee>\n" +
					"<costPhone>"+phone+"</costPhone>\n" +
					"<productId>"+productId+"</productId>\n" +
					"<rechargeChannel>38</rechargeChannel>\n" +
					"<token>"+sort(new String[]{"2",price,productId,phone,"38"},key)+"</token>\n" +
					"</EmpOrderCreateReq>\n" +
					"</Request>\n";
			
			request.getContent().put("content", baoyue);
			HttpSendClient httpSendClient = new HttpSendClient();
			HttpSendResponse culverinResponse = httpSendClient.executeHttpPost(request);
			if (!Utils.isEmpty(culverinResponse)) {
				orderId = StringUtils.substringBetween(culverinResponse.getResponseBody(), "<orderId>", "</orderId>");
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return orderId;
	}
	
	//发送验证码
	private Boolean empSecurityCodeFetch(String tradeId, String phone) {
		boolean isSucc = false;
		try {
			Map<String, String> content = new HashMap<String ,String>();
			Map<String, String> header = new HashMap<String ,String>();

			HttpSendRequest request = new HttpSendRequest();
			request.setContent(content);
			request.setHeads(header);

			request.setUrl(url);
			request.setCharset("utf-8");
			request.setTimeout(50000000);

			header.put("Client-Agent", "TYYD_Android_4_0_1024_800_HW_C8812_JAVA_2_9_8/480*640/public");
			header.put("userAccount",phone);
			header.put("action", "empSecurityCodeFetch");
			
			String baoyue = "<Request>\n" +
					"<EmpSecurityCodeFetchReq>\n" +
					"<userIdentity>"+phone+"</userIdentity>\n" +
					"<userType>mdn</userType>\n" +
					"<orderId>"+tradeId+"</orderId>\n" +
					"<token>"+sort(new String[]{phone,"mdn",tradeId},key)+"</token>\n" +
					"</EmpSecurityCodeFetchReq>\n" +
					"</Request>\n";
			
			request.getContent().put("content", baoyue);
			HttpSendClient httpSendClient = new HttpSendClient();
			HttpSendResponse culverinResponse = httpSendClient.executeHttpPost(request);
			if (!Utils.isEmpty(culverinResponse)) {
				isSucc = Boolean.parseBoolean(StringUtils.substringBetween(culverinResponse.getResponseBody(), "<isSuccess>", "</isSuccess>"));
			}
		} catch (Exception e) {
			LogUtil.log(e.getMessage(), e);
		}
		return isSucc;
	}
	
	private Boolean empSecurityCodeValidate(String tradeId, String code, String phone, String name) {
		boolean isSucc = false;
		try {
			Map<String, String> content = new HashMap<String ,String>();
			Map<String, String> header = new HashMap<String ,String>();

			HttpSendRequest request = new HttpSendRequest();
			request.setContent(content);
			request.setHeads(header);

			request.setUrl(url);
			request.setCharset("utf-8");
			request.setTimeout(50000000);

			header.put("Client-Agent", "TYYD_Android_4_0_1024_800_HW_C8812_JAVA_2_9_8/480*640/public");
			header.put("userAccount",phone);
			header.put("action", "empSecurityCodeValidate");
			
			String baoyue = "<Request>\n" +
					"<EmpSecurityCodeValidateReq>\n" +
					"<orderId>"+tradeId+"</orderId>\n" +
					"<code>"+code+"</code>\n" +
					"<token>"+sort(new String[]{tradeId,code},key)+"</token>\n" +
					"</EmpSecurityCodeValidateReq>\n" +
					"</Request>\n";
			
			request.getContent().put("content", baoyue);
			HttpSendClient httpSendClient = new HttpSendClient();
			HttpSendResponse culverinResponse = httpSendClient.executeHttpPost(request);
			if (!Utils.isEmpty(culverinResponse)) {
				isSucc = Boolean.parseBoolean(StringUtils.substringBetween(culverinResponse.getResponseBody(), "<isSuccess>", "</isSuccess>"));
				String resultCode = StringUtils.substringBetween(culverinResponse.getResponseBody(), "<resultCode>", "</resultCode>");
				LogUtil.log("packName:"+name+",indentifyCode:"+code+",isSuccess:"+isSucc+",resultCode="+resultCode);
			}
		} catch (Exception e) {
			LogUtil.log(e.getMessage(), e);
		}
		return isSucc;
	}
	
	public static String sort(String[] args, String key){
		Arrays.sort(args);
		String mySign = "";
		for (int i = 0; i < args.length; i++) {
			mySign += args[i];
		}
		mySign += key;
		return MD5.getMD5(mySign);
	}
	
	//爱游戏创建订单
	private String iGameEmpOrderCreate(String productId, String phone) {
		String returnStr = null;
		try {
			String url = "http://m.play.cn/wap/emp/orderpkg/push_auth_msg.json";
			Map<String, String> map = new HashMap<String, String>();
			map.put("mobile_phone", phone);
			map.put("package_id", productId);
			returnStr = HttpClientUtils.simpleGetInvoke(url, map, "UTF-8");
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return returnStr;
	}
	
	private Boolean iGameEmpCodeValidate(String productId, String phone, String validCode, String tradeId) {
		String returnStr = null;
		Boolean isSucc = false;
		try {
			String url = "http://m.play.cn/wap/emp/orderpkg/order.json";
			Map<String, String> map = new HashMap<String, String>();
			map.put("mobile_phone", phone);
			map.put("package_id", productId);
			map.put("validate_code", validCode);
			map.put("correlator", tradeId);
			returnStr = HttpClientUtils.simpleGetInvoke(url, map, "UTF-8");
			
			JSONObject json = JSONObject.parseObject(returnStr);
			Integer code = json.getInteger("code");
			if (code == 0) {
				isSucc = true;
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return isSucc;
	}
	
}
