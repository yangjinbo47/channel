package com.tenfen.entity.operation.open;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.tenfen.entity.IdEntity;

@Entity
@Table(name = "T_OPEN_ORDER")
public class TOpenOrder extends IdEntity {

	private static final long serialVersionUID = 6932210734466832689L;
	// Fields    
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
	private Date unsubscribeTime;
	private Integer reduce;
	
	//extra fields
	private String sellerName;
	
	// Constructors
	public TOpenOrder() {
		createTime = new Date();
		status = "1";//未支付
		reduce = 0;
	}

	/** minimal constructor */
	public TOpenOrder(Integer id) {
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
	
	@Transient
	public String getCreateTimeString() {
		String returnStr = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if (createTime != null) {
			returnStr = sdf.format(createTime);
		}
		return returnStr;
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

	public String getStatus() {
		return status;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "PAY_TIME", length = 19)
	public Date getPayTime() {
		return payTime;
	}

	public void setPayTime(Date payTime) {
		this.payTime = payTime;
	}
	
	@Transient
	public String getPayTimeString() {
		String returnStr = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if (payTime != null) {
			returnStr = sdf.format(payTime);
		}
		return returnStr;
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
	
	@Column(name = "REDUCE")
	public Integer getReduce() {
		return reduce;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "UNSUBSCRIBE_TIME", length = 19)
	public Date getUnsubscribeTime() {
		return unsubscribeTime;
	}

	public void setUnsubscribeTime(Date unsubscribeTime) {
		this.unsubscribeTime = unsubscribeTime;
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