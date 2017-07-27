package com.tenfen.entity.operation.pack;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.tenfen.entity.IdEntity;

@Entity
@Table(name = "T_PUSH_SELLER_PACKAGES")
public class TPushSellerPackages extends IdEntity {

	private static final long serialVersionUID = 6932210734466832689L;
	// Fields    
	private TPushSeller pushSeller;
	private PushPackage pushPackage;
	private Integer packageLimit;
	private Integer packageToday;

	// Constructors
	public TPushSellerPackages() {
		packageLimit = -1;
		packageToday = 0;
	}
	
	/** minimal constructor */
	public TPushSellerPackages(Integer id) {
		this.id = id;
	}

	@Column(name = "package_limit")
	public Integer getPackageLimit() {
		return packageLimit;
	}

	public void setPackageLimit(Integer packageLimit) {
		this.packageLimit = packageLimit;
	}

	@Column(name = "package_today")
	public Integer getPackageToday() {
		return packageToday;
	}

	public void setPackageToday(Integer packageToday) {
		this.packageToday = packageToday;
	}

	@ManyToOne(cascade=CascadeType.ALL)
	@JoinColumn(name = "seller_id", unique = true)
	public TPushSeller getPushSeller() {
		return pushSeller;
	}

	public void setPushSeller(TPushSeller pushSeller) {
		this.pushSeller = pushSeller;
	}

	@ManyToOne(cascade=CascadeType.ALL)
	@JoinColumn(name = "package_id", unique = true)
	public PushPackage getPushPackage() {
		return pushPackage;
	}

	public void setPushPackage(PushPackage pushPackage) {
		this.pushPackage = pushPackage;
	}
	
}