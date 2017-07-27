package com.tenfen.entity.operation.pack;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.tenfen.entity.IdEntity;

@Entity
@Table(name = "T_PUSH_PACKAGE_LIMIT")
public class PushPackageLimit extends IdEntity {

	private static final long serialVersionUID = 6932210734466832689L;
	// Fields    
	private Integer packageId;
	private String province;
	private Integer dayLimit;
	private Integer monthLimit;
	
	// Constructors

	/** default constructor */
	public PushPackageLimit() {
	}

	/** minimal constructor */
	public PushPackageLimit(Integer id) {
		this.id = id;
	}

	@Column(name = "PACKAGE_ID")
	public Integer getPackageId() {
		return packageId;
	}

	public void setPackageId(Integer packageId) {
		this.packageId = packageId;
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