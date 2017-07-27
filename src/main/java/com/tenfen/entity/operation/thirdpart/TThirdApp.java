package com.tenfen.entity.operation.thirdpart;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.google.common.collect.Lists;
import com.tenfen.entity.IdEntity;

@Entity
@Table(name = "T_THIRDPART_APP")
public class TThirdApp extends IdEntity {

	private static final long serialVersionUID = 6932210734466832689L;
	// Fields    
	private String name;
	private Integer merchantId;
	private String thirdAppId;
	private String thirdAppMch;
	private String thirdAppSecret;
	private String callbackUrl;
	
	//extra Fields
	private Integer appLimit;
	private Integer appToday;

	//extra Fields
	private String appShowName;
	private List<TThirdSellerApps> sellerApps = Lists.newArrayList();
	
	// Constructors
	public TThirdApp() {
	}
	
	/** minimal constructor */
	public TThirdApp(Integer id) {
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

	@Column(name = "THIRD_APP_ID", length = 45)
	public String getThirdAppId() {
		return thirdAppId;
	}

	public void setThirdAppId(String thirdAppId) {
		this.thirdAppId = thirdAppId;
	}

	@Column(name = "THIRD_APP_MCH", length = 45)
	public String getThirdAppMch() {
		return thirdAppMch;
	}

	public void setThirdAppMch(String thirdAppMch) {
		this.thirdAppMch = thirdAppMch;
	}

	@Column(name = "THIRD_APP_SECRET", length = 1000)
	public String getThirdAppSecret() {
		return thirdAppSecret;
	}

	public void setThirdAppSecret(String thirdAppSecret) {
		this.thirdAppSecret = thirdAppSecret;
	}

	@Column(name = "CALLBACK_URL", length = 45)
	public String getCallbackUrl() {
		return callbackUrl;
	}

	public void setCallbackUrl(String callbackUrl) {
		this.callbackUrl = callbackUrl;
	}

	@Transient
	public String getAppShowName() {
		return appShowName;
	}

	public void setAppShowName(String appShowName) {
		this.appShowName = appShowName;
	}

	@OneToMany(mappedBy = "thirdApp",cascade=CascadeType.ALL, fetch = FetchType.LAZY)
	public List<TThirdSellerApps> getSellerApps() {
		return sellerApps;
	}

	public void setSellerApps(List<TThirdSellerApps> sellerApps) {
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
	
}