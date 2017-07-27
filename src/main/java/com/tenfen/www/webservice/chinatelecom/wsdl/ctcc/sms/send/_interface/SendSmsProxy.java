package com.tenfen.www.webservice.chinatelecom.wsdl.ctcc.sms.send._interface;

import com.tenfen.www.webservice.chinatelecom.schema.ctcc.common.ChargingInformation;
import com.tenfen.www.webservice.chinatelecom.schema.ctcc.common.PolicyException;
import com.tenfen.www.webservice.chinatelecom.schema.ctcc.common.ServiceException;
import com.tenfen.www.webservice.chinatelecom.schema.ctcc.common.SimpleReference;
import com.tenfen.www.webservice.chinatelecom.schema.ctcc.sms.DeliveryInformation;
import com.tenfen.www.webservice.chinatelecom.schema.ctcc.sms.SmsFormat;
import com.tenfen.www.webservice.chinatelecom.wsdl.ctcc.sms.send.service.SendSmsServiceLocator;

public class SendSmsProxy implements SendSms {
  private String _endpoint = null;
  private SendSms sendSms = null;
  
  public SendSmsProxy() {
    _initSendSmsProxy();
  }
  
  public SendSmsProxy(String endpoint) {
    _endpoint = endpoint;
    _initSendSmsProxy();
  }
  
  private void _initSendSmsProxy() {
    try {
      sendSms = (new SendSmsServiceLocator()).getSendSms();
      if (sendSms != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)sendSms)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)sendSms)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (sendSms != null)
      ((javax.xml.rpc.Stub)sendSms)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public SendSms getSendSms() {
    if (sendSms == null)
      _initSendSmsProxy();
    return sendSms;
  }
  
  public java.lang.String sendSms(org.apache.axis.types.URI[] addresses, java.lang.String senderName, ChargingInformation charging, java.lang.String message, SimpleReference receiptRequest) throws java.rmi.RemoteException, PolicyException, ServiceException{
    if (sendSms == null)
      _initSendSmsProxy();
    return sendSms.sendSms(addresses, senderName, charging, message, receiptRequest);
  }
  
  public java.lang.String sendSmsLogo(org.apache.axis.types.URI[] addresses, java.lang.String senderName, ChargingInformation charging, byte[] image, SmsFormat smsFormat, SimpleReference receiptRequest) throws java.rmi.RemoteException, PolicyException, ServiceException{
    if (sendSms == null)
      _initSendSmsProxy();
    return sendSms.sendSmsLogo(addresses, senderName, charging, image, smsFormat, receiptRequest);
  }
  
  public java.lang.String sendSmsRingtone(org.apache.axis.types.URI[] addresses, java.lang.String senderName, ChargingInformation charging, java.lang.String ringtone, SmsFormat smsFormat, SimpleReference receiptRequest) throws java.rmi.RemoteException, PolicyException, ServiceException{
    if (sendSms == null)
      _initSendSmsProxy();
    return sendSms.sendSmsRingtone(addresses, senderName, charging, ringtone, smsFormat, receiptRequest);
  }
  
  public DeliveryInformation[] getSmsDeliveryStatus(java.lang.String requestIdentifier) throws java.rmi.RemoteException, PolicyException, ServiceException{
    if (sendSms == null)
      _initSendSmsProxy();
    return sendSms.getSmsDeliveryStatus(requestIdentifier);
  }
  
  
}