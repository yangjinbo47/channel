package com.tenfen.entity.operation.thirdpart;

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
@Table(name = "T_THIRDPART_ORDER")
public class TThirdOrder extends IdEntity {

	private static final long serialVersionUID = 6932210734466832689L;
	// Fields    
	private String imsi;
	private String orderId;
	private String outTradeNo;
	private Integer appId;
	private Integer merchantId;
	private Integer sellerId;
	private String subject;
	private Date createTime;
	private Integer fee;
	private String status;
	private Date payTime;
	private String payUser;
	private Integer type;
	
	//extra fields
	private String sellerName;
	
	// Constructors
	public TThirdOrder() {
		createTime = new Date();
		status = "1";//未支付
	}

	/** minimal constructor */
	public TThirdOrder(Integer id) {
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

	@Column(name = "PAY_USER", length = 20)
	public String getPayUser() {
		return payUser;
	}

	public void setPayUser(String payUser) {
		this.payUser = payUser;
	}
	
	@Column(name = "TYPE")
	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	@Transient
	public String getSellerName() {
		return sellerName;
	}

	public void setSellerName(String sellerName) {
		this.sellerName = sellerName;
	}
	
}