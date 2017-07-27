package com.tenfen.mongoEntity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Transient;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "t_sms_order")
public class MongoTSmsOrder implements Serializable{
	
	private static final long serialVersionUID = -4051818671872639854L;
	@Id
	private String id;
	@Field("imsi")
	private String imsi;
	@Field("order_id")
	private String orderId;
	@Field("out_trade_no")
	private String outTradeNo;
	@Field("link_id")
	private String linkId;
	@Field("seller_id")
	private Integer sellerId;
	@Field("app_id")
	private Integer appId;
	@Field("merchant_id")
	private Integer merchantId;
	@Field("subject")
	private String subject;
	@Field("sender_number")
	private String senderNumber;
	@Field("msg_content")
	private String msgContent;
	@Field("mo_number")
	private String moNumber;
	@Field("mo_msg")
	private String moMsg;
	@Field("create_time")
	private Date createTime;
	@Field("fee")
	private Integer fee;
	@Field("product_type")
	private Integer productType;
	@Field("status")
	private String status;
	@Field("pay_time")
	private Date payTime;
	@Field("pay_phone")
	private String payPhone;
	@Field("province")
	private String province;
	@Field("unsubscribe_time")
	private Date unsubscribeTime;
	@Field("reduce")
	private Integer reduce;
	
	//extra fields
	private String sellerName;
	
	public MongoTSmsOrder() {
		reduce = 0;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getImsi() {
		return imsi;
	}
	
	public void setImsi(String imsi) {
		this.imsi = imsi;
	}
	
	public String getOrderId() {
		return orderId;
	}
	
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	
	public String getOutTradeNo() {
		return outTradeNo;
	}
	
	public void setOutTradeNo(String outTradeNo) {
		this.outTradeNo = outTradeNo;
	}
	
	public String getLinkId() {
		return linkId;
	}
	
	public void setLinkId(String linkId) {
		this.linkId = linkId;
	}
	
	public Integer getSellerId() {
		return sellerId;
	}
	
	public void setSellerId(Integer sellerId) {
		this.sellerId = sellerId;
	}
	
	public Integer getAppId() {
		return appId;
	}
	
	public void setAppId(Integer appId) {
		this.appId = appId;
	}
	
	public Integer getMerchantId() {
		return merchantId;
	}
	
	public void setMerchantId(Integer merchantId) {
		this.merchantId = merchantId;
	}
	
	public String getSubject() {
		return subject;
	}
	
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public String getSenderNumber() {
		return senderNumber;
	}
	
	public void setSenderNumber(String senderNumber) {
		this.senderNumber = senderNumber;
	}
	
	public String getMsgContent() {
		return msgContent;
	}
	
	public void setMsgContent(String msgContent) {
		this.msgContent = msgContent;
	}
	
	public String getMoNumber() {
		return moNumber;
	}
	
	public void setMoNumber(String moNumber) {
		this.moNumber = moNumber;
	}
	
	public String getMoMsg() {
		return moMsg;
	}
	
	public void setMoMsg(String moMsg) {
		this.moMsg = moMsg;
	}
	
	public Date getCreateTime() {
		return createTime;
	}
	
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	public Integer getFee() {
		return fee;
	}
	
	public void setFee(Integer fee) {
		this.fee = fee;
	}
	
	public Integer getProductType() {
		return productType;
	}
	
	public void setProductType(Integer productType) {
		this.productType = productType;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public Date getPayTime() {
		return payTime;
	}
	
	public void setPayTime(Date payTime) {
		this.payTime = payTime;
	}
	
	public String getPayPhone() {
		return payPhone;
	}
	
	public void setPayPhone(String payPhone) {
		this.payPhone = payPhone;
	}
	
	public String getProvince() {
		return province;
	}
	
	public void setProvince(String province) {
		this.province = province;
	}
	
	public Date getUnsubscribeTime() {
		return unsubscribeTime;
	}

	public void setUnsubscribeTime(Date unsubscribeTime) {
		this.unsubscribeTime = unsubscribeTime;
	}

	public Integer getReduce() {
		return reduce;
	}

	public void setReduce(Integer reduce) {
		this.reduce = reduce;
	}

	@Transient
	public String getSellerName() {
		return sellerName;
	}

	public void setSellerName(String sellerName) {
		this.sellerName = sellerName;
	}
	
}