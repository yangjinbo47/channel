package com.tenfen.entity.operation.open;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "T_OPEN_ORDER_HISTORY")
public class TOpenOrderHistory implements Serializable{

	private static final long serialVersionUID = 6932210734466832689L;
	// Fields    
	private Integer id;
	private String imsi;
	private String orderId;
	private String outTradeNo;
	private Integer appId;
	private Integer merchantId;
	private Integer sellerId;
	private String subject;
	private String senderNumber;
	private String msgContent;
	private Date createTime;
	private Integer fee;
	private String status;
	private Date payTime;
	private String payPhone;
	private String province;
	
	// Constructors
	public TOpenOrderHistory() {
	}

	/** minimal constructor */
	public TOpenOrderHistory(Integer id) {
		this.id = id;
	}
	
	@Id
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Column(name = "IMSI", length = 20)
	public String getImsi() {
		return imsi;
	}

	public void setImsi(String imsi) {
		this.imsi = imsi;
	}

	@Column(name = "ORDER_ID", length = 45)
	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	@Column(name = "out_trade_no", length = 45)
	public String getOutTradeNo() {
		return outTradeNo;
	}

	public void setOutTradeNo(String outTradeNo) {
		this.outTradeNo = outTradeNo;
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

}