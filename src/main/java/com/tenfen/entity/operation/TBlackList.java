package com.tenfen.entity.operation;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.tenfen.entity.IdEntity;

@Entity
@Table(name = "T_BLACK_LIST")
public class TBlackList extends IdEntity {

	private static final long serialVersionUID = 4015910258615211042L;
	// Fields
	private String phoneNum;

	// Constructors

	/** default constructor */
	public TBlackList() {
	}

	/** minimal constructor */
	public TBlackList(Integer id) {
		this.id = id;
	}

	@Column(name = "PHONE_NUM", length = 11)
	public String getPhoneNum() {
		return this.phoneNum;
	}

	public void setPhoneNum(String phoneNum) {
		this.phoneNum = phoneNum;
	}

}