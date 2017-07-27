package com.tenfen.entity.operation;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.tenfen.entity.IdEntity;

/**
 * TMobileArea entity. @author MyEclipse Persistence Tools
 */
@Entity
@Table(name = "T_MOBILE_AREA")
public class TMobileArea extends IdEntity {

	// Fields    

	private static final long serialVersionUID = 5306198720395924349L;
	private String firstNum;
	private String middleNum;
	private String address;
	private String province;
	private String city;
	private String brand;

	// Constructors

	/** default constructor */
	public TMobileArea() {
	}

	@Column(name = "first_num", length = 11)
	public String getFirstNum() {
		return this.firstNum;
	}

	public void setFirstNum(String firstNum) {
		this.firstNum = firstNum;
	}

	@Column(name = "middle_num", length = 11)
	public String getMiddleNum() {
		return this.middleNum;
	}

	public void setMiddleNum(String middleNum) {
		this.middleNum = middleNum;
	}

	@Column(name = "address", length = 50)
	public String getAddress() {
		return this.address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@Column(name = "province", length = 50)
	public String getProvince() {
		return this.province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	@Column(name = "city", length = 50)
	public String getCity() {
		return this.city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	@Column(name = "brand", length = 50)
	public String getBrand() {
		return this.brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

}