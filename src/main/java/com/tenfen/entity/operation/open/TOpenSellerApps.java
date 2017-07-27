package com.tenfen.entity.operation.open;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.tenfen.entity.IdEntity;

@Entity
@Table(name = "T_OPEN_SELLER_APPS")
public class TOpenSellerApps extends IdEntity {

	private static final long serialVersionUID = 6932210734466832689L;
	// Fields    
	private TOpenSeller openSeller;
	private TOpenApp openApp;
	private Integer appLimit;
	private Integer appToday;

	// Constructors
	public TOpenSellerApps() {
		appLimit = -1;
		appToday = 0;
	}
	
	/** minimal constructor */
	public TOpenSellerApps(Integer id) {
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
	public TOpenSeller getOpenSeller() {
		return openSeller;
	}

	public void setOpenSeller(TOpenSeller openSeller) {
		this.openSeller = openSeller;
	}

	@ManyToOne(cascade=CascadeType.ALL)
	@JoinColumn(name = "app_id", unique = true)
	public TOpenApp getOpenApp() {
		return openApp;
	}

	public void setOpenApp(TOpenApp openApp) {
		this.openApp = openApp;
	}
	
}