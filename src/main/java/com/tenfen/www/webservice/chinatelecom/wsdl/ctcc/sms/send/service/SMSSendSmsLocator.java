/**
 * 
 */
package com.tenfen.www.webservice.chinatelecom.wsdl.ctcc.sms.send.service;

/**
 * @author wangqijun
 * 
 */
public class SMSSendSmsLocator extends SendSmsServiceLocator {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5983501880132578439L;

	/**
	 * 
	 */
	public SMSSendSmsLocator() {
		// TODO �Զ���ɹ��캯����
		super();
	}

	/* ���� Javadoc��
	 * @see cn.com.chinatelecom.www.wsdl.ctcc.sms.send.v2_1.service.SendSmsServiceLocator#getSendSms(java.net.URL)
	 */
	public SMSSendSmsStub getSendSms(
			java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
		try {
			SMSSendSmsStub _stub = new SMSSendSmsStub(
					portAddress, this);
			_stub.setPortName(getSendSmsWSDDServiceName());
			return _stub;
		} catch (org.apache.axis.AxisFault e) {
			return null;
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO �Զ���ɷ������

	}

}
