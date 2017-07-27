package com.tenfen.entity.operation.sms;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.tenfen.entity.IdEntity;

@Entity
@Table(name = "T_SMS_SELLER_LIMIT")
public class TSmsSellerLimit extends IdEntity {

	private static final long serialVersionUID = 6932210734466832689L;
	// Fields    
	private Integer sellerId;
	private String province;
	private Integer dayLimit;
	private Integer monthLimit;
	
	// Constructors
	public TSmsSellerLimit() {
	}

	/** minimal constructor */
	public TSmsSellerLimit(Integer id) {
		this.id = id;
	}

	@Column(name = "SELLER_ID")
	public Integer getSellerId() {
		return sellerId;
	}

	public void setSellerId(Integer sellerId) {
		this.sellerId = sellerId;
	}

	@Column(name = "PROVINCE", length = 10)
	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	@Column(name = "DAY_LIMIT")
	public Integer getDayLimit() {
		return dayLimit;
	}

	public void setDayLimit(Integer dayLimit) {
		this.dayLimit = dayLimit;
	}

	@Column(name = "MONTH_LIMIT")
	public Integer getMonthLimit() {
		return monthLimit;
	}

	public void setMonthLimit(Integer monthLimit) {
		this.monthLimit = monthLimit;
	}

}