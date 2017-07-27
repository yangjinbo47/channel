package com.tenfen.entity.operation.thirdpart;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.tenfen.entity.IdEntity;

@Entity
@Table(name = "T_THIRDPART_SELLER_APPS")
public class TThirdSellerApps extends IdEntity {

	private static final long serialVersionUID = 6932210734466832689L;
	// Fields    
	private TThirdSeller thirdSeller;
	private TThirdApp thirdApp;
	private Integer appLimit;
	private Integer appToday;

	// Constructors
	public TThirdSellerApps() {
		appLimit = -1;
		appToday = 0;
	}
	
	/** minimal constructor */
	public TThirdSellerApps(Integer id) {
		this.id = id;
	}

	@Column(name = "app_limit")
	public Integer getAppLimit() {
		return appLimit;
	}

	public void setAppLimit(Integer appLimit) {
		this.appLimit = appLimit;
	}

	@Column(name = "app_today")
	public Integer getAppToday() {
		return appToday;
	}

	public void setAppToday(Integer appToday) {
		this.appToday = appToday;
	}

	@ManyToOne(cascade=CascadeType.ALL)
	@JoinColumn(name = "seller_id", unique = true)
	public TThirdSeller getThirdSeller() {
		return thirdSeller;
	}

	public void setThirdSeller(TThirdSeller thirdSeller) {
		this.thirdSeller = thirdSeller;
	}

	@ManyToOne(cascade=CascadeType.ALL)
	@JoinColumn(name = "app_id", unique = true)
	public TThirdApp getThirdApp() {
		return thirdApp;
	}

	public void setThirdApp(TThirdApp thirdApp) {
		this.thirdApp = thirdApp;
	}

}