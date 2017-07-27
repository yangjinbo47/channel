package com.tenfen.entity.account;

import java.io.Serializable;
import javax.persistence.*;
import java.sql.Timestamp;


/**
 * The persistent class for the t_frame_button database table.
 * 
 */
@Entity
@Table(name="t_frame_button")
public class TFrameButton implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int buttonid;

	private int createoperid;

	private Timestamp createtime;

	private int menuid;

	private String name;

	private int status;

	public TFrameButton() {
	}

	public int getButtonid() {
		return this.buttonid;
	}

	public void setButtonid(int buttonid) {
		this.buttonid = buttonid;
	}

	public int getCreateoperid() {
		return this.createoperid;
	}

	public void setCreateoperid(int createoperid) {
		this.createoperid = createoperid;
	}

	public Timestamp getCreatetime() {
		return this.createtime;
	}

	public void setCreatetime(Timestamp createtime) {
		this.createtime = createtime;
	}

	public int getMenuid() {
		return this.menuid;
	}

	public void setMenuid(int menuid) {
		this.menuid = menuid;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getStatus() {
		return this.status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

}