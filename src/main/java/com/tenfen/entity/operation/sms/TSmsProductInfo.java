package com.tenfen.entity.operation.sms;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.tenfen.entity.IdEntity;

@Entity
@Table(name = "T_SMS_PRODUCT_INFO")
public class TSmsProductInfo extends IdEntity implements Comparable<TSmsProductInfo>{

	private static final long serialVersionUID = 6932210734466832689L;
	// Fields
	private String name;
	private Integer price;
	private String sendNumber;
	private String instruction;
	private String productId;
	private Integer type;
	private Integer merchantId;
	
	private String merchantShowName;
	// Constructors
	public TSmsProductInfo() {
		type = 1;
	}
	
	/** minimal constructor */
	public TSmsProductInfo(Integer id) {
		this.id = id;
	}

	@Column(name = "NAME", length = 50)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "PRICE")
	public Integer getPrice() {
		return price;
	}

	public void setPrice(Integer price) {
		this.price = price;
	}

	@Column(name = "SEND_NUMBER", length = 20)
	public String getSendNumber() {
		return sendNumber;
	}

	public void setSendNumber(String sendNumber) {
		this.sendNumber = sendNumber;
	}

	@Column(name = "INSTRUCTION", length = 20)
	public String getInstruction() {
		return instruction;
	}

	public void setInstruction(String instruction) {
		this.instruction = instruction;
	}

	@Column(name = "PRODUCT_ID", length = 45)
	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	@Column(name = "TYPE")
	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	@Column(name = "MERCHANT_ID", length = 10)
	public Integer getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(Integer merchantId) {
		this.merchantId = merchantId;
	}
	
	@Transient
	public String getMerchantShowName() {
		return merchantShowName;
	}

	public void setMerchantShowName(String merchantShowName) {
		this.merchantShowName = merchantShowName;
	}

	@Override
	public int compareTo(TSmsProductInfo entity) {
		if (this.getPrice() < entity.getPrice()) {
			return 1;
		} else if (this.getPrice() > entity.getPrice()) {
			return -1;
		} else {			
			return 0;
		}
	}

}