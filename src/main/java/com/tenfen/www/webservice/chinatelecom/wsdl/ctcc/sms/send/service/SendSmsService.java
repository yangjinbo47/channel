/**
 * SendSmsService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.tenfen.www.webservice.chinatelecom.wsdl.ctcc.sms.send.service;

import com.tenfen.www.webservice.chinatelecom.wsdl.ctcc.sms.send._interface.SendSms;

public interface SendSmsService extends javax.xml.rpc.Service {
    public java.lang.String getSendSmsAddress();

    public SendSms getSendSms() throws javax.xml.rpc.ServiceException;

    public SendSms getSendSms(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
