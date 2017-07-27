package com.tenfen.entity.operation.sms;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.tenfen.entity.IdEntity;

@Entity
@Table(name = "T_SMS_SELLER_APPS")
public class TSmsSellerApps extends IdEntity {

	private static final long serialVersionUID = 6932210734466832689L;
	// Fields    
	private TSmsSeller smsSeller;
	private TSmsApp smsApp;
	private Integer appLimit;
	private Integer appToday;

	// Constructors
	public TSmsSellerApps() {
		appLimit = -1;
		appToday = 0;
	}
	
	/** minimal constructor */
	public TSmsSellerApps(Integer id) {
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
	public TSmsSeller getSmsSeller() {
		return smsSeller;
	}

	public void setSmsSeller(TSmsSeller smsSeller) {
		this.smsSeller = smsSeller;
	}

	@ManyToOne(cascade=CascadeType.ALL)
	@JoinColumn(name = "app_id", unique = true)
	public TSmsApp getSmsApp() {
		return smsApp;
	}

	public void setSmsApp(TSmsApp smsApp) {
		this.smsApp = smsApp;
	}

}