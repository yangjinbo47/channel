/**
 * NotifySOAPHeader.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package com.tenfen.www.webservice.chinatelecom.schema.ctcc.common;

public class NotifySOAPHeader  implements java.io.Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = -1740357176619988009L;
	private java.lang.String spRevId;
    private java.lang.String spRevpassword;
    private java.lang.String spId;
    private java.lang.String SAN;
    private java.lang.String productId;
    private java.lang.String transactionId;
    private java.lang.String linkId;

    public NotifySOAPHeader() {
    }

    public java.lang.String getSpRevId() {
        return spRevId;
    }

    public void setSpRevId(java.lang.String spRevId) {
        this.spRevId = spRevId;
    }

    public java.lang.String getSpRevpassword() {
        return spRevpassword;
    }

    public void setSpRevpassword(java.lang.String spRevpassword) {
        this.spRevpassword = spRevpassword;
    }

    public java.lang.String getSpId() {
        return spId;
    }

    public void setSpId(java.lang.String spId) {
        this.spId = spId;
    }

    public java.lang.String getSAN() {
        return SAN;
    }

    public void setSAN(java.lang.String SAN) {
        this.SAN = SAN;
    }

    public java.lang.String getProductId() {
        return productId;
    }

    public void setProductId(java.lang.String productId) {
        this.productId = productId;
    }

    public java.lang.String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(java.lang.String transactionId) {
        this.transactionId = transactionId;
    }

    public java.lang.String getLinkId() {
        return linkId;
    }

    public void setLinkId(java.lang.String linkId) {
        this.linkId = linkId;
    }

    private java.lang.Object __equalsCalc = null;
    @Override
	public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof NotifySOAPHeader)) return false;
        NotifySOAPHeader other = (NotifySOAPHeader) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.spRevId==null && other.getSpRevId()==null) || 
             (this.spRevId!=null &&
              this.spRevId.equals(other.getSpRevId()))) &&
            ((this.spRevpassword==null && other.getSpRevpassword()==null) || 
             (this.spRevpassword!=null &&
              this.spRevpassword.equals(other.getSpRevpassword()))) &&
            ((this.spId==null && other.getSpId()==null) || 
             (this.spId!=null &&
              this.spId.equals(other.getSpId()))) &&
            ((this.SAN==null && other.getSAN()==null) || 
             (this.SAN!=null &&
              this.SAN.equals(other.getSAN()))) &&
            ((this.productId==null && other.getProductId()==null) || 
             (this.productId!=null &&
              this.productId.equals(other.getProductId()))) &&
            ((this.transactionId==null && other.getTransactionId()==null) || 
             (this.transactionId!=null &&
              this.transactionId.equals(other.getTransactionId()))) &&
            ((this.linkId==null && other.getLinkId()==null) || 
             (this.linkId!=null &&
              this.linkId.equals(other.getLinkId())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    @Override
	public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getSpRevId() != null) {
            _hashCode += getSpRevId().hashCode();
        }
        if (getSpRevpassword() != null) {
            _hashCode += getSpRevpassword().hashCode();
        }
        if (getSpId() != null) {
            _hashCode += getSpId().hashCode();
        }
        if (getSAN() != null) {
            _hashCode += getSAN().hashCode();
        }
        if (getProductId() != null) {
            _hashCode += getProductId().hashCode();
        }
        if (getTransactionId() != null) {
            _hashCode += getTransactionId().hashCode();
        }
        if (getLinkId() != null) {
            _hashCode += getLinkId().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

}
