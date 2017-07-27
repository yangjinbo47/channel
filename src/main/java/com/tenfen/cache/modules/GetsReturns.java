package com.tenfen.cache.modules;

public class GetsReturns {

	private final long cas;
	private final Object value;

	public GetsReturns(long cas, Object value) {
		this.cas = cas;
		this.value = value;
	}

	public long getCas() {
		return cas;
	}

	public Object getValue() {
		return value;
	}

	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + (int) (cas ^ cas >>> 32);
		result = prime * result + (value != null ? value.hashCode() : 0);
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GetsReturns other = (GetsReturns) obj;
		if (cas != other.cas)
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	public String toString() {
		return (new StringBuilder("GetsReturns[cas=")).append(cas).append(",value=").append(value).append("]").toString();
	}

}
