package com.tenfen.entity.operation.sms;

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
@Table(name = "T_SMS_APP")
public class TSmsApp extends IdEntity {

	private static final long serialVersionUID = 6932210734466832689L;
	// Fields
	private String name;
	private Integer merchantId;
	private String appKey;
	private String appSecret;
	private String tips;
//	private String excludeArea;
	private Integer companyShow;
	
	//extra Fields
	private Integer appLimit;
	private Integer appToday;

	//extra Fields
	private String appShowName;
	private List<TSmsProductInfo> productList = Lists.newArrayList();//有序的关联对象集合
	private List<TSmsSellerApps> sellerApps = Lists.newArrayList();
//	private String[] excludeAreaArray;
	
	// Constructors
	public TSmsApp() {
		companyShow = 0;
	}
	
	/** minimal constructor */
	public TSmsApp(Integer id) {
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
	
	@Column(name = "tips", length = 200)
	public String getTips() {
		return tips;
	}

	public void setTips(String tips) {
		this.tips = tips;
	}
	
	@Column(name = "COMPANY_SHOW")
	public Integer getCompanyShow() {
		return companyShow;
	}

	public void setCompanyShow(Integer companyShow) {
		this.companyShow = companyShow;
	}
	
//	@Column(name = "EXCLUDE_AREA", length = 200)
//	public String getExcludeArea() {
//		return excludeArea;
//	}
//
//	public void setExcludeArea(String excludeArea) {
//		this.excludeArea = excludeArea;
//	}

	@Transient
	public String getAppShowName() {
		return appShowName;
	}

	public void setAppShowName(String appShowName) {
		this.appShowName = appShowName;
	}

	//多对多定义
	@ManyToMany
	//中间表定义,表名采用默认命名规则
	@JoinTable(name = "t_sms_app_product", joinColumns = { @JoinColumn(name = "app_id") }, inverseJoinColumns = { @JoinColumn(name = "p_id") })
	//Fecth策略定义
	@Fetch(FetchMode.SUBSELECT)
	//集合按id排序.
	@OrderBy("id")
	public List<TSmsProductInfo> getProductList() {
		return productList;
	}

	public void setProductList(List<TSmsProductInfo> productList) {
		this.productList = productList;
	}

	@OneToMany(mappedBy = "smsApp",cascade=CascadeType.ALL, fetch = FetchType.LAZY)
	public List<TSmsSellerApps> getSellerApps() {
		return sellerApps;
	}

	public void setSellerApps(List<TSmsSellerApps> sellerApps) {
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