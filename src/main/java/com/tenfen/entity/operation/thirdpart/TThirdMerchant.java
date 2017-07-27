package com.tenfen.entity.operation.thirdpart;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.tenfen.entity.IdEntity;

@Entity
@Table(name = "T_THIRDPART_MERCHANT")
public class TThirdMerchant extends IdEntity {

	private static final long serialVersionUID = 6932210734466832689L;
	// Fields    
	private String merchantName;
	private String email;
	private String contact;
	private String telephone;
	private Integer joinType;
	
	//extra
	private String merchantShowName;
	
	// Constructors
	public TThirdMerchant() {
	}

	/** minimal constructor */
	public TThirdMerchant(Integer id) {
		this.id = id;
	}

	@Column(name = "MERCHANT_NAME", length = 100)
	public String getMerchantName() {
		return merchantName;
	}

	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}

	@Column(name = "EMAIL", length = 45)
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	@Column(name = "CONTACT", length = 20)
	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	@Column(name = "TELEPHONE", length = 45)
	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	@Column(name = "join_type")
	public Integer getJoinType() {
		return joinType;
	}

	public void setJoinType(Integer joinType) {
		this.joinType = joinType;
	}

	@Transient
	public String getMerchantShowName() {
		String joinTypeString = null;
		switch (joinType) {
		case 1:{
			joinTypeString = "支付宝";
			break;
		}
		case 2:{
			joinTypeString = "微信支付";
			break;
		}
		default:
			joinTypeString = "未找到接入类型";
			break;
		}
		merchantShowName = merchantName+"("+joinTypeString+")";
		return merchantShowName;
	}

}