package com.tenfen.entity.operation.sms;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.tenfen.entity.IdEntity;

@Entity
@Table(name = "T_SMS_APP_LIMIT")
public class TSmsAppLimit extends IdEntity {

	private static final long serialVersionUID = 6932210734466832689L;
	// Fields    
	private Integer appId;
	private String province;
	private Integer dayLimit;
	private Integer monthLimit;
	private Integer userDayLimit;
	private Integer userMonthLimit;
	private Integer reduce;
	
	// Constructors
	public TSmsAppLimit() {
	}

	/** minimal constructor */
	public TSmsAppLimit(Integer id) {
		this.id = id;
	}

	@Column(name = "APP_ID")
	public Integer getAppId() {
		return appId;
	}

	public void setAppId(Integer appId) {
		this.appId = appId;
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

	@Column(name = "USER_DAY_LIMIT")
	public Integer getUserDayLimit() {
		return userDayLimit;
	}

	public void setUserDayLimit(Integer userDayLimit) {
		this.userDayLimit = userDayLimit;
	}

	@Column(name = "USER_MONTH_LIMIT")
	public Integer getUserMonthLimit() {
		return userMonthLimit;
	}

	public void setUserMonthLimit(Integer userMonthLimit) {
		this.userMonthLimit = userMonthLimit;
	}

	@Column(name = "REDUCE")
	public Integer getReduce() {
		return reduce;
	}

	public void setReduce(Integer reduce) {
		this.reduce = reduce;
	}
	
}