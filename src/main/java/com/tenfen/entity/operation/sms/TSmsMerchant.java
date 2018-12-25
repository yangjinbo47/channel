package com.tenfen.entity.operation.sms;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.tenfen.entity.IdEntity;

@Entity
@Table(name = "T_SMS_MERCHANT")
public class TSmsMerchant extends IdEntity {

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
	public TSmsMerchant() {
	}

	/** minimal constructor */
	public TSmsMerchant(Integer id) {
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

	@Column(name = "JOIN_TYPE")
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
			joinTypeString = "信元短代";
			break;
		}
		case 2:{
			joinTypeString = "爱音乐短代";
			break;
		}
		case 3:{
			joinTypeString = "联通在信";
			break;
		}
		case 4:{
			joinTypeString = "联通全网短信";
			break;
		}
		case 5:{
			joinTypeString = "天翼爱动漫";
			break;
		}
		case 6:{
			joinTypeString = "移动全网短信";
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