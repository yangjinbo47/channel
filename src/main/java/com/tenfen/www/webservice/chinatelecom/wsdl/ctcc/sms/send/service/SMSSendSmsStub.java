/**
 *
 */
package com.tenfen.www.webservice.chinatelecom.wsdl.ctcc.sms.send.service;

import java.net.URL;

import javax.xml.rpc.Service;

import org.apache.axis.AxisFault;

import com.tenfen.www.webservice.chinatelecom.schema.ctcc.common.ChargingInformation;
import com.tenfen.www.webservice.chinatelecom.schema.ctcc.common.PolicyException;
import com.tenfen.www.webservice.chinatelecom.schema.ctcc.common.RequestSOAPHeader;
import com.tenfen.www.webservice.chinatelecom.schema.ctcc.common.RouteSoapHeader;
import com.tenfen.www.webservice.chinatelecom.schema.ctcc.common.ServiceException;
import com.tenfen.www.webservice.chinatelecom.schema.ctcc.common.SimpleReference;

/**
 * @author wangqijun
 * 
 */
public class SMSSendSmsStub extends SendSmsBindingStub
{
	/**
	 * @throws AxisFault
	 */
	public SMSSendSmsStub() throws AxisFault
	{
		// TODO 自动生成构造函数存根
		super();
	}
	
	
	
	/**
	 * @param endpointURL
	 * @param service
	 * @throws AxisFault
	 */
	public SMSSendSmsStub(URL endpointURL, Service service) throws AxisFault
	{
		super(endpointURL, service);
		
		
		// TODO 自动生成构造函数存根
	}
	
	
	
	/**
	 * @param service
	 * @throws AxisFault
	 */
	public SMSSendSmsStub(Service service) throws AxisFault
	{
		super(service);
		
		
		// TODO 自动生成构造函数存根
	}
	
	
	
	/**
	 * 短信发送
	 * 
	 * @param addresses
	 *            目的地址
	 * @param senderName
	 *            发送者信息
	 * @param charging
	 *            计费方案
	 * @param message
	 *            短信内容
	 * @param receiptRequest
	 *            是否状态报告
	 * @param soapHeader
	 *            消息头对象
	 * @return 返回码
	 * @throws java.rmi.RemoteException
	 * @throws PolicyException
	 * @throws ServiceException
	 */
	public java.lang.String sendSms
	(
			org.apache.axis.types.URI[] addresses,
			java.lang.String senderName,
			ChargingInformation charging,
			java.lang.String message,
			SimpleReference receiptRequest,
			RequestSOAPHeader soapHeader) throws java.rmi.RemoteException,
			PolicyException,
			ServiceException
	{
		if (super.cachedEndpoint == null)
		{
			throw new org.apache.axis.NoEndPointException();
		}
		
		org.apache.axis.client.Call _call = createCall();
		
		
		// 设置soap头
		// RouteSoapHeader soap = new RouteSoapHeader(".\\downsoap.properties");
		RouteSoapHeader soap = new RouteSoapHeader(soapHeader);
		
		soap.setSoapHeader(_call);
		
		_call.setOperation(get_operations()[0]);
		_call.setUseSOAPAction(true);
		_call.setSOAPActionURI("");
		_call.setEncodingStyle(null);
		_call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
				Boolean.FALSE);
		_call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
				Boolean.FALSE);
		_call
				.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
		_call
				.setOperationName(new javax.xml.namespace.QName(
						"http://www.chinatelecom.com.cn/schema/ctcc/sms/send/v2_1/local",
						"sendSms"));
		
		setRequestHeaders(_call);
		setAttachments(_call);
		
		java.lang.Object _resp = _call.invoke(new java.lang.Object[] {
				addresses, senderName, charging, message, receiptRequest });
		
		if (_resp instanceof java.rmi.RemoteException)
		{
			throw (java.rmi.RemoteException) _resp;
		}
		else
		{
			extractAttachments(_call);
			
			try
			{
				return (java.lang.String) _resp;
			}
			catch (java.lang.Exception _exception)
			{
				return (java.lang.String) org.apache.axis.utils.JavaUtils
						.convert(_resp, java.lang.String.class);
			}
		}
	}
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		// TODO 自动生成方法存根
	}
}
