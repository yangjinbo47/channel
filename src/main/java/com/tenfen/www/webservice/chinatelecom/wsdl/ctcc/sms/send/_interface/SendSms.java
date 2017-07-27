/**
 * SendSms.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.tenfen.www.webservice.chinatelecom.wsdl.ctcc.sms.send._interface;

import com.tenfen.www.webservice.chinatelecom.schema.ctcc.common.ChargingInformation;
import com.tenfen.www.webservice.chinatelecom.schema.ctcc.common.PolicyException;
import com.tenfen.www.webservice.chinatelecom.schema.ctcc.common.ServiceException;
import com.tenfen.www.webservice.chinatelecom.schema.ctcc.common.SimpleReference;
import com.tenfen.www.webservice.chinatelecom.schema.ctcc.sms.DeliveryInformation;
import com.tenfen.www.webservice.chinatelecom.schema.ctcc.sms.SmsFormat;

public interface SendSms extends java.rmi.Remote {
    public java.lang.String sendSms(org.apache.axis.types.URI[] addresses, java.lang.String senderName, ChargingInformation charging, java.lang.String message, SimpleReference receiptRequest) throws java.rmi.RemoteException, PolicyException, ServiceException;
    public java.lang.String sendSmsLogo(org.apache.axis.types.URI[] addresses, java.lang.String senderName, ChargingInformation charging, byte[] image, SmsFormat smsFormat, SimpleReference receiptRequest) throws java.rmi.RemoteException, PolicyException, ServiceException;
    public java.lang.String sendSmsRingtone(org.apache.axis.types.URI[] addresses, java.lang.String senderName, ChargingInformation charging, java.lang.String ringtone, SmsFormat smsFormat, SimpleReference receiptRequest) throws java.rmi.RemoteException, PolicyException, ServiceException;
    public DeliveryInformation[] getSmsDeliveryStatus(java.lang.String requestIdentifier) throws java.rmi.RemoteException, PolicyException, ServiceException;
}
