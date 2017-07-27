package com.tenfen.entity.operation.open;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.google.common.collect.Lists;
import com.tenfen.entity.IdEntity;

@Entity
@Table(name = "T_OPEN_APP")
public class TOpenApp extends IdEntity {

	private static final long serialVersionUID = 6932210734466832689L;
	// Fields
	private String name;
	private Integer merchantId;
	private String appKey;
	private String appSecret;
	private String callbackUrl;
	private String clientId;
//	private String excludeArea;
	private Integer companyShow;

	//extra Fields
	private Integer appLimit;
	private Integer appToday;
	
	private List<TOpenProductInfo> productList = Lists.newArrayList();//有序的关联对象集合
	private List<TOpenSellerApps> sellerApps = Lists.newArrayList();
//	private String[] excludeAreaArray;
	
	// Constructors
	public TOpenApp() {
		companyShow = 0;
	}
	
	/** minimal constructor */
	public TOpenApp(Integer id) {
		this.id = id;
	}

	@Column(name = "NAME", length = 45)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "MERCHANT_ID")
	public Integer getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(Integer merchantId) {
		this.merchantId = merchantId;
	}

	@Column(name = "APP_KEY", length = 45)
	public String getAppKey() {
		return appKey;
	}

	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}

	@Column(name = "APP_SECRET", length = 45)
	public String getAppSecret() {
		return appSecret;
	}

	public void setAppSecret(String appSecret) {
		this.appSecret = appSecret;
	}

	@Column(name = "CALLBACK_URL", length = 200)
	public String getCallbackUrl() {
		return callbackUrl;
	}

	public void setCallbackUrl(String callbackUrl) {
		this.callbackUrl = callbackUrl;
	}

//	@Column(name = "EXCLUDE_AREA", length = 200)
//	public String getExcludeArea() {
//		return excludeArea;
//	}
//
//	public void setExcludeArea(String excludeArea) {
//		this.excludeArea = excludeArea;
//	}
	
	@Column(name = "CLIENT_ID", length = 45)
	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	
	@Column(name = "COMPANY_SHOW")
	public Integer getCompanyShow() {
		return companyShow;
	}

	public void setCompanyShow(Integer companyShow) {
		this.companyShow = companyShow;
	}

	//多对多定义
	@ManyToMany
	//中间表定义,表名采用默认命名规则
	@JoinTable(name = "t_open_app_product", joinColumns = { @JoinColumn(name = "app_id") }, inverseJoinColumns = { @JoinColumn(name = "p_id") })
	//Fecth策略定义
	@Fetch(FetchMode.SUBSELECT)
	//集合按id排序.
	@OrderBy("id")
	public List<TOpenProductInfo> getProductList() {
		return productList;
	}

	public void setProductList(List<TOpenProductInfo> productList) {
		this.productList = productList;
	}

	@OneToMany(mappedBy = "openApp",cascade=CascadeType.ALL, fetch = FetchType.LAZY)
	public List<TOpenSellerApps> getSellerApps() {
		return sellerApps;
	}

	public void setSellerApps(List<TOpenSellerApps> sellerApps) {
		this.sellerApps = sellerApps;
	}

	@Transient
	public Integer getAppLimit() {
		return appLimit;
	}

	public void setAppLimit(Integer appLimit) {
		this.appLimit = appLimit;
	}

	@Transient
	public Integer getAppToday() {
		return appToday;
	}

	public void setAppToday(Integer appToday) {
		this.appToday = appToday;
	}
	
//	@Transient
//	public String[] getExcludeAreaArray() {
//		if (!Utils.isEmpty(excludeArea)) {			
//			excludeAreaArray = excludeArea.split(",");
//		}
//		return excludeAreaArray;
//	}
//
//	public void setExcludeAreaArray(String[] excludeAreaArray) {
//		this.excludeAreaArray = excludeAreaArray;
//	}

}