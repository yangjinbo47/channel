package com.tenfen.entity.operation.pack;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.tenfen.entity.IdEntity;

@Entity
@Table(name = "T_ORDER")
public class TOrder extends IdEntity {

	private static final long serialVersionUID = 4015910258615211042L;
	// Fields
	private String tradeId;
	private String outTradeNo;
	private String imsi;
	private String phoneNum;
	private Integer sellerId;
	private Integer pushId;
	private Integer fee;
	private Integer status;
	private Date createTime;
	private String name;
//	private String channel;
	private String province;
	private Integer reduce;
//	private Integer pushPackageId;

	// Constructors

	/** default constructor */
	public TOrder() {
		createTime = new Date();
		status=1;
		reduce = 0;
	}

	/** minimal constructor */
	public TOrder(Integer id) {
		this.id = id;
	}

	/** full constructor */
	public TOrder(Integer id, String phoneNum, Integer status, Date createTime) {
		this.id = id;
		this.phoneNum = phoneNum;
		this.status = status;
		this.createTime = createTime;
	}

	@Column(name = "TRADE_ID", length = 20)
	public String getTradeId() {
		return tradeId;
	}

	public void setTradeId(String tradeId) {
		this.tradeId = tradeId;
	}
	
	@Column(name = "OUT_TRADE_NO", length = 50)
	public String getOutTradeNo() {
		return outTradeNo;
	}

	public void setOutTradeNo(String outTradeNo) {
		this.outTradeNo = outTradeNo;
	}

	@Column(name = "IMSI", length = 20)
	public String getImsi() {
		return imsi;
	}

	public void setImsi(String imsi) {
		this.imsi = imsi;
	}

	@Column(name = "PHONE_NUM", length = 11)
	public String getPhoneNum() {
		return this.phoneNum;
	}

	public void setPhoneNum(String phoneNum) {
		this.phoneNum = phoneNum;
	}

	@Column(name = "STATUS", precision = 2, scale = 0)
	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATE_TIME", length = 11)
	public Date getCreateTime() {
		return this.createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	@Column(name = "NAME", length = 50)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

//	@Column(name = "CHANNEL", length = 20)
//	public String getChannel() {
//		return channel;
//	}
//
//	public void setChannel(String channel) {
//		this.channel = channel;
//	}

	@Column(name = "PROVINCE", length = 20)
	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}
	
	@Column(name = "SELLER_ID")
	public Integer getSellerId() {
		return sellerId;
	}

	public void setSellerId(Integer sellerId) {
		this.sellerId = sellerId;
	}
	
	@Column(name = "PUSH_ID")
	public Integer getPushId() {
		return pushId;
	}

	public void setPushId(Integer pushId) {
		this.pushId = pushId;
	}

	@Column(name = "FEE")
	public Integer getFee() {
		return fee;
	}

	public void setFee(Integer fee) {
		this.fee = fee;
	}

	@Column(name = "REDUCE")
	public Integer getReduce() {
		return reduce;
	}

	public void setReduce(Integer reduce) {
		this.reduce = reduce;
	}
	
//	@Transient
//	public Integer getPushPackageId() {
//		return pushPackageId;
//	}
//
//	public void setPushPackageId(Integer pushPackageId) {
//		this.pushPackageId = pushPackageId;
//	}

}