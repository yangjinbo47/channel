package com.tenfen.entity.operation.open;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.tenfen.entity.IdEntity;

@Entity
@Table(name = "T_OPEN_ORDER_YIXIN")
public class TOpenOrderYiXin extends IdEntity {

	private static final long serialVersionUID = 6932210734466832689L;
	// Fields    
	private Integer logId;
	private Integer seqId;
	private String msisdn;
	private Integer userType;
	private String prodCode;
	private Date logTime;
	private String operation;
	private Integer chargeType;
	private Integer price;
	
	@Column(name = "LOG_ID")
	public Integer getLogId() {
		return logId;
	}
	
	public void setLogId(Integer logId) {
		this.logId = logId;
	}
	
	@Column(name = "SEQ_ID")
	public Integer getSeqId() {
		return seqId;
	}
	
	public void setSeqId(Integer seqId) {
		this.seqId = seqId;
	}
	
	@Column(name = "MSISDN", length = 11)
	public String getMsisdn() {
		return msisdn;
	}
	
	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}
	
	@Column(name = "USER_TYPE")
	public Integer getUserType() {
		return userType;
	}
	
	public void setUserType(Integer userType) {
		this.userType = userType;
	}
	
	@Column(name = "PROD_CODE", length = 10)
	public String getProdCode() {
		return prodCode;
	}
	
	public void setProdCode(String prodCode) {
		this.prodCode = prodCode;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "LOG_TIME", nullable = false, length = 19)
	public Date getLogTime() {
		return logTime;
	}
	
	public void setLogTime(Date logTime) {
		this.logTime = logTime;
	}
	
	@Column(name = "OPERATION", length = 10)
	public String getOperation() {
		return operation;
	}
	
	public void setOperation(String operation) {
		this.operation = operation;
	}

	@Column(name = "CHARGE_TYPE")
	public Integer getChargeType() {
		return chargeType;
	}

	public void setChargeType(Integer chargeType) {
		this.chargeType = chargeType;
	}

	@Column(name = "PRICE")
	public Integer getPrice() {
		return price;
	}

	public void setPrice(Integer price) {
		this.price = price;
	}
	
}