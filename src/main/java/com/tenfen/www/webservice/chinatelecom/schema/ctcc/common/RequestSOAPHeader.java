/**
 * RequestSOAPHeader.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package com.tenfen.www.webservice.chinatelecom.schema.ctcc.common;

import org.apache.axis.types.URI;

public class RequestSOAPHeader  implements java.io.Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = -4341164726670681586L;
	private java.lang.String spId;
    private java.lang.String spPassword;
    private java.lang.String timeStamp;
    private java.lang.String productId;
    private java.lang.String SAN;
    private java.lang.String transactionId;
    private EndReason transEnd;
    private java.lang.String linkId;
    private URI OA;
    private URI FA;
    private java.lang.Boolean multicastMessaging;

    public RequestSOAPHeader() {
    }

    public java.lang.String getSpId() {
        return spId;
    }

    public void setSpId(java.lang.String spId) {
        this.spId = spId;
    }

    public java.lang.String getSpPassword() {
        return spPassword;
    }

    public void setSpPassword(java.lang.String spPassword) {
        this.spPassword = spPassword;
    }

    public java.lang.String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(java.lang.String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public java.lang.String getProductId() {
        return productId;
    }

    public void setProductId(java.lang.String productId) {
        this.productId = productId;
    }

    public java.lang.String getSAN() {
        return SAN;
    }

    public void setSAN(java.lang.String SAN) {
        this.SAN = SAN;
    }

    public java.lang.String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(java.lang.String transactionId) {
        this.transactionId = transactionId;
    }

    public EndReason getTransEnd() {
        return transEnd;
    }

    public void setTransEnd(EndReason transEnd) {
        this.transEnd = transEnd;
    }

    public java.lang.String getLinkId() {
        return linkId;
    }

    public void setLinkId(java.lang.String linkId) {
        this.linkId = linkId;
    }

    public URI getOA() {
        return OA;
    }

    public void setOA(URI OA) {
        this.OA = OA;
    }

    public URI getFA() {
        return FA;
    }

    public void setFA(URI FA) {
        this.FA = FA;
    }

    public java.lang.Boolean getMulticastMessaging() {
        return multicastMessaging;
    }

    public void setMulticastMessaging(java.lang.Boolean multicastMessaging) {
        this.multicastMessaging = multicastMessaging;
    }

    private java.lang.Object __equalsCalc = null;
    @Override
	public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof RequestSOAPHeader)) return false;
        RequestSOAPHeader other = (RequestSOAPHeader) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.spId==null && other.getSpId()==null) || 
             (this.spId!=null &&
              this.spId.equals(other.getSpId()))) &&
            ((this.spPassword==null && other.getSpPassword()==null) || 
             (this.spPassword!=null &&
              this.spPassword.equals(other.getSpPassword()))) &&
            ((this.timeStamp==null && other.getTimeStamp()==null) || 
             (this.timeStamp!=null &&
              this.timeStamp.equals(other.getTimeStamp()))) &&
            ((this.productId==null && other.getProductId()==null) || 
             (this.productId!=null &&
              this.productId.equals(other.getProductId()))) &&
            ((this.SAN==null && other.getSAN()==null) || 
             (this.SAN!=null &&
              this.SAN.equals(other.getSAN()))) &&
            ((this.transactionId==null && other.getTransactionId()==null) || 
             (this.transactionId!=null &&
              this.transactionId.equals(other.getTransactionId()))) &&
            ((this.transEnd==null && other.getTransEnd()==null) || 
             (this.transEnd!=null &&
              this.transEnd.equals(other.getTransEnd()))) &&
            ((this.linkId==null && other.getLinkId()==null) || 
             (this.linkId!=null &&
              this.linkId.equals(other.getLinkId()))) &&
            ((this.OA==null && other.getOA()==null) || 
             (this.OA!=null &&
              this.OA.equals(other.getOA()))) &&
            ((this.FA==null && other.getFA()==null) || 
             (this.FA!=null &&
              this.FA.equals(other.getFA()))) &&
            ((this.multicastMessaging==null && other.getMulticastMessaging()==null) || 
             (this.multicastMessaging!=null &&
              this.multicastMessaging.equals(other.getMulticastMessaging())));
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
        if (getSpId() != null) {
            _hashCode += getSpId().hashCode();
        }
        if (getSpPassword() != null) {
            _hashCode += getSpPassword().hashCode();
        }
        if (getTimeStamp() != null) {
            _hashCode += getTimeStamp().hashCode();
        }
        if (getProductId() != null) {
            _hashCode += getProductId().hashCode();
        }
        if (getSAN() != null) {
            _hashCode += getSAN().hashCode();
        }
        if (getTransactionId() != null) {
            _hashCode += getTransactionId().hashCode();
        }
        if (getTransEnd() != null) {
            _hashCode += getTransEnd().hashCode();
        }
        if (getLinkId() != null) {
            _hashCode += getLinkId().hashCode();
        }
        if (getOA() != null) {
            _hashCode += getOA().hashCode();
        }
        if (getFA() != null) {
            _hashCode += getFA().hashCode();
        }
        if (getMulticastMessaging() != null) {
            _hashCode += getMulticastMessaging().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

}
