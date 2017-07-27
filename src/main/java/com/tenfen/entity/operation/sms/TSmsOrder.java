package com.tenfen.entity.operation.sms;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.tenfen.entity.IdEntity;

@Entity
@Table(name = "T_SMS_ORDER")
public class TSmsOrder extends IdEntity {

	private static final long serialVersionUID = 6932210734466832689L;
	// Fields    
	private String orderId;
	private String outTradeNo;
	private String linkId;
//	private String createImsi;
	private String imsi;
	private Integer appId;
	private Integer merchantId;
	private Integer sellerId;
	private String subject;
//	private String productId;
	private String senderNumber;
	private String msgContent;
	private String moNumber;
	private String moMsg;
	private Date createTime;
	private Integer fee;
	private String status;
	private Integer productType;
	private Date payTime;
	private String payPhone;
	private String province;
	private Date unsubscribeTime;
	private Integer reduce;
	
	// Constructors
	public TSmsOrder() {
		createTime = new Date();
		status = "1";//未支付
		reduce = 0;
	}

	/** minimal constructor */
	public TSmsOrder(Integer id) {
		this.id = id;
	}

	@Column(name = "ORDER_ID", length = 45)
	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	@Column(name = "OUT_TRADE_NO", length = 45)
	public String getOutTradeNo() {
		return outTradeNo;
	}

	public void setOutTradeNo(String outTradeNo) {
		this.outTradeNo = outTradeNo;
	}

	@Column(name = "LINK_ID", length = 45)
	public String getLinkId() {
		return linkId;
	}

	public void setLinkId(String linkId) {
		this.linkId = linkId;
	}

	@Column(name = "APP_ID")
	public Integer getAppId() {
		return appId;
	}

	public void setAppId(Integer appId) {
		this.appId = appId;
	}

	@Column(name = "MERCHANT_ID")
	public Integer getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(Integer merchantId) {
		this.merchantId = merchantId;
	}

	@Column(name = "SELLER_ID")
	public Integer getSellerId() {
		return sellerId;
	}

	public void setSellerId(Integer sellerId) {
		this.sellerId = sellerId;
	}

	@Column(name = "SUBJECT", length = 45)
	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	@Column(name = "SENDER_NUMBER", length = 20)
	public String getSenderNumber() {
		return senderNumber;
	}

	public void setSenderNumber(String senderNumber) {
		this.senderNumber = senderNumber;
	}

	@Column(name = "MSG_CONTENT", length = 70)
	public String getMsgContent() {
		return msgContent;
	}

	public void setMsgContent(String msgContent) {
		this.msgContent = msgContent;
	}

	@Column(name = "MO_NUMBER", length = 20)
	public String getMoNumber() {
		return moNumber;
	}

	public void setMoNumber(String moNumber) {
		this.moNumber = moNumber;
	}

	@Column(name = "MO_MSG", length = 70)
	public String getMoMsg() {
		return moMsg;
	}

	public void setMoMsg(String moMsg) {
		this.moMsg = moMsg;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATE_TIME", nullable = false, length = 19)
	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	@Column(name = "FEE")
	public Integer getFee() {
		return fee;
	}

	public void setFee(Integer fee) {
		this.fee = fee;
	}

	@Column(name = "STATUS")
	public void setStatus(String status) {
		this.status = status;
	}

	public void setPayTime(Date payTime) {
		this.payTime = payTime;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "PAY_TIME", length = 19)
	public Date getPayTime() {
		return payTime;
	}

	public String getStatus() {
		return status;
	}

	@Column(name = "PAY_PHONE", length = 20)
	public String getPayPhone() {
		return payPhone;
	}

	public void setPayPhone(String payPhone) {
		this.payPhone = payPhone;
	}

	@Column(name = "PROVINCE", length = 20)
	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	@Column(name = "IMSI", length = 20)
	public String getImsi() {
		return imsi;
	}

	public void setImsi(String imsi) {
		this.imsi = imsi;
	}

	@Column(name = "PRODUCT_TYPE")
	public Integer getProductType() {
		return productType;
	}

	public void setProductType(Integer productType) {
		this.productType = productType;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "UNSUBSCRIBE_TIME", length = 19)
	public Date getUnsubscribeTime() {
		return unsubscribeTime;
	}

	public void setUnsubscribeTime(Date unsubscribeTime) {
		this.unsubscribeTime = unsubscribeTime;
	}

	@Column(name = "REDUCE")
	public Integer getReduce() {
		return reduce;
	}

	public void setReduce(Integer reduce) {
		this.reduce = reduce;
	}
//	@Column(name = "PRODUCT_ID", length = 11)
//	public String getProductId() {
//		return productId;
//	}
//
//	public void setProductId(String productId) {
//		this.productId = productId;
//	}

}