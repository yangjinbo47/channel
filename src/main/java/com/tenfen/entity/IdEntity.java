package com.tenfen.entity;

import java.io.Serializable;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * @author BOBO
 */
@MappedSuperclass
public abstract class IdEntity implements Serializable {

	private static final long serialVersionUID = 8381881591332937943L;
	
	protected Integer id;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
}
