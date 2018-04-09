package com.tenfen.entity.operation.open;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.tenfen.entity.IdEntity;

@Entity
@Table(name = "T_OPEN_MERCHANT")
public class TOpenMerchant extends IdEntity {

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
	public TOpenMerchant() {
	}

	/** minimal constructor */
	public TOpenMerchant(Integer id) {
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
		case 1:
			joinTypeString = "天翼阅读";
			break;
		case 2:
			joinTypeString = "爱动漫";
			break;
		case 3:
			joinTypeString = "爱音乐";
			break;
		case 4:
			joinTypeString = "wo阅读";
			break;
		case 5:
			joinTypeString = "天翼阅读-离线";
			break;
		case 6:
			joinTypeString = "wo+";
			break;
		case 7:
			joinTypeString = "易信";
			break;
		case 11:
			joinTypeString = "天翼空间-朗天";
			break;
		case 12:
			joinTypeString = "天翼空间-通用";
			break;
		case 13:
			joinTypeString = "天翼空间-旭游";
			break;
		case 14:
			joinTypeString = "咪咕动漫wap";
			break;
		case 15:
			joinTypeString = "MM网页支付";
			break;
		case 20:
			joinTypeString = "联通小额支付";
			break;
		default:
			joinTypeString = "未找到接入类型";
			break;
		}
		merchantShowName = merchantName+"("+joinTypeString+")";
		return merchantShowName;
	}

}