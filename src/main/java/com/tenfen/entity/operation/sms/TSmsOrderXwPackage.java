package com.tenfen.entity.operation.sms;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.tenfen.entity.IdEntity;

@Entity
@Table(name = "T_SMS_ORDER_XWPACKAGE")
public class TSmsOrderXwPackage extends IdEntity {

	private static final long serialVersionUID = 6932210734466832689L;
	// Fields    
	private String orderId;
	private String content;
	private String channelNo;
	private String time;
	private String mobile;
	private Integer status;
	private Date createTime;
	
	// Constructors
	public TSmsOrderXwPackage() {
		createTime = new Date();
	}

	/** minimal constructor */
	public TSmsOrderXwPackage(Integer id) {
		this.id = id;
	}

	@Column(name = "ORDER_ID", length = 30)
	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	@Column(name = "CONTENT	", length = 10)
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Column(name = "CHANNEL_NO", length = 10)
	public String getChannelNo() {
		return channelNo;
	}

	public void setChannelNo(String channelNo) {
		this.channelNo = channelNo;
	}

	@Column(name = "TIME", length = 20)
	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	@Column(name = "MOBILE", length = 11)
	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	@Column(name = "STATUS", length = 45)
	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATE_TIME", nullable = false, length = 19)
	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

}