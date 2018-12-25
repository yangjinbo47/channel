package com.tenfen.www.action.external.sms.cmccsms;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;
import com.tenfen.entity.operation.TMobileArea;
import com.tenfen.entity.operation.sms.TSmsApp;
import com.tenfen.entity.operation.sms.TSmsOrder;
import com.tenfen.entity.operation.sms.TSmsProductInfo;
import com.tenfen.entity.operation.sms.TSmsSeller;
import com.tenfen.entity.operation.sms.TSmsSellerApps;
import com.tenfen.util.HttpClientUtils;
import com.tenfen.util.LogUtil;
import com.tenfen.util.Utils;
import com.tenfen.util.servlet.ServletRequestUtils;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.service.operation.MobileAreaManager;
import com.tenfen.www.service.operation.sms.SmsOrderManager;
import com.tenfen.www.service.operation.sms.SmsProductInfoManager;
import com.tenfen.www.service.operation.sms.SmsSellerManager;

/**
 * 移动全网短信
 * @author BOBO
 *
 */
public class CmccSmsAction extends SimpleActionSupport {
	
	private static final long serialVersionUID = 8112913793580559503L;
	
	@Autowired
	private SmsProductInfoManager smsProductInfoManager;
	@Autowired
	private SmsSellerManager smsSellerManager;
	@Autowired
	private SmsOrderManager smsOrderManager;
	@Autowired
	private MobileAreaManager mobileAreaManager;
	
	public static void main(String[] args) {
		String content = "2328";
		String[] str = content.split("#");
		System.out.println(str.length);
	}
	
	/**
	 * 接受联通全网短信mo
	 */
	public void mo() {
		String success = "fail";
		try {
			String destnumber = ServletRequestUtils.getStringParameter(request, "destnumber", null);
			String srcnumber = ServletRequestUtils.getStringParameter(request, "srcnumber", null);
			String unionid = ServletRequestUtils.getStringParameter(request, "unionid", null);
			String content = ServletRequestUtils.getStringParameter(request, "content", null);
			String linkid = ServletRequestUtils.getStringParameter(request, "linkid", null);
			LogUtil.log("cmccsms_mo destnumber="+destnumber+",srcnumber="+srcnumber+",unionid="+unionid+",content="+content+",linkid="+linkid);
			
			String instruction = null;
			String sellerId = null;
			String[] str = content.split("a");
			if(str.length == 1) {
				instruction = str[0];
			} else {
				instruction = str[0];
				sellerId = str[1];
			}
			
			if (sellerId != null) {
				TSmsSeller tSmsSeller = smsSellerManager.get(Integer.parseInt(sellerId));
				if (!Utils.isEmpty(tSmsSeller)) {
					List<TSmsSellerApps> smsSellerAppList = tSmsSeller.getSellerApps();
					TSmsApp tSmsApp = null;
					if (smsSellerAppList.size() > 0) {
						for (TSmsSellerApps tSmsSellerApps : smsSellerAppList) {
							tSmsApp = tSmsSellerApps.getSmsApp();
						}
					}
					
					if (!Utils.isEmpty(tSmsApp)) {
						Integer merchantId = tSmsApp.getMerchantId();
						Integer appId = tSmsApp.getId();
						//根据指令查询计费点
						TSmsProductInfo tSmsProductInfo = smsProductInfoManager.getSmsProductInfoByProperty(merchantId, instruction);
						if (!Utils.isEmpty(tSmsProductInfo)) {
							TSmsOrder tSmsOrder = new TSmsOrder();
							tSmsOrder.setOrderId(linkid);
							tSmsOrder.setOutTradeNo(null);
							tSmsOrder.setLinkId(linkid);
							tSmsOrder.setImsi(srcnumber);
							tSmsOrder.setAppId(appId);
							tSmsOrder.setMerchantId(merchantId);
							tSmsOrder.setSellerId(tSmsSeller.getId());
							tSmsOrder.setSubject("移动全网短信");
							tSmsOrder.setSenderNumber(destnumber);
							tSmsOrder.setMsgContent(tSmsProductInfo.getInstruction());
							tSmsOrder.setMoNumber(srcnumber);
							tSmsOrder.setMoMsg(content);
							tSmsOrder.setFee(tSmsProductInfo.getPrice());
							tSmsOrder.setProductType(tSmsProductInfo.getType());
							tSmsOrder.setPayPhone(srcnumber);
							TMobileArea mobileArea = mobileAreaManager.getMobileArea(srcnumber);
							if (!Utils.isEmpty(mobileArea)) {
								tSmsOrder.setProvince(mobileArea.getProvince());
							}
							tSmsOrder.setStatus("1");
							smsOrderManager.save(tSmsOrder);
							
							success = "ok";
						}
					}
				}
			}
		} catch (Exception e) {
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
	
	/**
	 * 接受联通全网短信mr
	 */
	public void mr() {
		String success = "fail";
		try {
//			String success = "fail";
//			String time = ServletRequestUtils.getStringParameter(request, "t", null);
//			String linkId = ServletRequestUtils.getStringParameter(request, "link_id", null);
//			String rp = ServletRequestUtils.getStringParameter(request, "rp", null);
//			String rpmsg = ServletRequestUtils.getStringParameter(request, "rpmsg", null);
//			String mobile = ServletRequestUtils.getStringParameter(request, "mo", null);
//			
//			String status = "";
//			if ("1".equals(rp) && "DELIVRD".equals(rpmsg)) {
//				status = "3";
//			} else {
//				status = "4";
//			}
//			
//			TSmsOrder tSmsOrder = smsOrderManager.getSmsOrderByProperty("linkId", linkId);
//			if (!Utils.isEmpty(tSmsOrder)) {
//				if ("1".equals(tSmsOrder.getStatus())) {
//					SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
//					
//					tSmsOrder.setStatus(status);
//					tSmsOrder.setPayTime(sdf.parse(time));
//					
//					smsOrderManager.save(tSmsOrder);
//					
//					//增加今日量
//					if ("3".equals(status)) {
//						smsSellerManager.saveSmsSellerApps(tSmsOrder.getSellerId(), tSmsOrder.getAppId(), tSmsOrder.getFee());
//					}
//					
//					//调用回调渠道方法
//					TSmsSeller tSmsSeller = smsSellerManager.get(tSmsOrder.getSellerId());
//					String callbackUrl = tSmsSeller.getCallbackUrl();
//					if (!Utils.isEmpty(callbackUrl)) {
//						new Thread(new SendPartner(mobile, status, tSmsOrder.getOrderId(), tSmsOrder.getFee()+"", tSmsOrder.getMoNumber(), tSmsOrder.getMoMsg(), callbackUrl)).start();
//					}
//				}
//			}
			
			String phone = ServletRequestUtils.getStringParameter(request, "phone", null);
			String linkid = ServletRequestUtils.getStringParameter(request, "linkid", null);
			String report = ServletRequestUtils.getStringParameter(request, "report", null);
			String unionid = ServletRequestUtils.getStringParameter(request, "unionid", null);
			String donetime = ServletRequestUtils.getStringParameter(request, "donetime", null);
			String destnumber = ServletRequestUtils.getStringParameter(request, "destnumber", null);
			String content = ServletRequestUtils.getStringParameter(request, "content", null);
			LogUtil.log("cmccsms_mo phone="+phone+",linkid="+linkid+",report="+report+",unionid="+unionid+",donetime="+donetime+",destnumber="+destnumber+",content="+content);
			
			String status = "";
			if ("DELIVRD".equals(report)) {
				status = "3";
			} else {
				status = "4";
			}
			
			TSmsOrder tSmsOrder = smsOrderManager.getSmsOrderByProperty("linkId", linkid);
			if (!Utils.isEmpty(tSmsOrder)) {
				if ("1".equals(tSmsOrder.getStatus())) {
					SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmm");
					
					tSmsOrder.setStatus(status);
					tSmsOrder.setPayTime(sdf.parse(donetime));
					
					smsOrderManager.save(tSmsOrder);
					
					//增加今日量
					if ("3".equals(status)) {
						smsSellerManager.saveSmsSellerApps(tSmsOrder.getSellerId(), tSmsOrder.getAppId(), tSmsOrder.getFee());
					}
					
					//调用回调渠道方法
					TSmsSeller tSmsSeller = smsSellerManager.get(tSmsOrder.getSellerId());
					String callbackUrl = tSmsSeller.getCallbackUrl();
					if (!Utils.isEmpty(callbackUrl)) {
						new Thread(new SendPartner(phone, status, tSmsOrder.getOrderId(), tSmsOrder.getFee()+"", tSmsOrder.getMoNumber(), tSmsOrder.getMoMsg(), callbackUrl)).start();
					}
				}
				success = "ok";
			}
		} catch (Exception e) {
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
		private String callbackUrl;
		private String phone;
		private String content;
		private String channelNo;
		
		public SendPartner(String phone, String status, String orderNo, String fee, String channelNo, String content, String callbackUrl) {
			this.phone = phone;
			this.fee = fee;
			this.status = status;
			this.orderNo = orderNo;
			this.callbackUrl = callbackUrl;
			this.channelNo = channelNo;
			this.content = content;
		}
		
		@Override
		public void run() {
			try {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("order_no", orderNo);
				jsonObject.put("phone", phone);
				jsonObject.put("fee", fee);
				jsonObject.put("status", status);
				jsonObject.put("channel_no", channelNo);
				jsonObject.put("content", content);
				
		        LogUtil.log("unicomsms call:"+callbackUrl+" sendMsg:"+jsonObject.toString());
		        HttpClientUtils.postJson(callbackUrl, jsonObject.toString());
			} catch (Exception e) {
				LogUtil.error(e.getMessage(), e);
			}
		}
	}
}
