package com.tenfen.mongoEntity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Transient;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "t_open_order")
public class MongoTOpenOrder implements Serializable{
	
	private static final long serialVersionUID = -4051818671872639854L;
	@Id
	private String id;
	@Field("imsi")
	private String imsi;
	@Field("order_id")
	private String orderId;
	@Field("out_trade_no")
	private String outTradeNo;
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
	@Field("create_time")
	private Date createTime;
	@Field("fee")
	private Integer fee;
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
	
	public MongoTOpenOrder() {
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