package com.tenfen.www.webservice.chinatelecom.schema.ctcc.common;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
import java.io.FileInputStream;
import java.util.Properties;

import javax.xml.soap.SOAPElement;

import org.apache.axis.message.PrefixedQName;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.message.SOAPHeaderElement;
import org.apache.axis.types.URI;

import com.tenfen.util.encrypt.MD5;

public class RouteSoapHeader {
	public RequestSOAPHeader soapHeader = null;

	public NotifySOAPHeader UpsoapHeader = null;

	public RouteSoapHeader(String fileHeader) {
		if (fileHeader.equals("")) {
			initUpSoapFromFile();
		} else
			initSoapFromFile(fileHeader);
	}

	public RouteSoapHeader(RequestSOAPHeader soapHeader) {
		this.soapHeader = soapHeader;
	}

	public void initUpSoapFromFile() {
		String fileName = "F:\\myfile\\平台\\gateway\\src\\main\\resources\\test.properties";
		UpsoapHeader = new NotifySOAPHeader();
		try {
			Properties prop = new Properties();
			prop.load(new FileInputStream(fileName));

			String SpId = prop.getProperty("SpId", "");
			String SpPassword = prop.getProperty("SpPassword", "");
			String TimeStamp = prop.getProperty("TimeStamp", ""); // MMDDHHMMSS
			String temp = SpId + SpPassword + TimeStamp;

			UpsoapHeader.setSpId(SpId);
//			UpsoapHeader.setSpRevpassword(MD5.MD5Crypt(temp));
			UpsoapHeader.setSpRevpassword(MD5.getMD5(temp));
			UpsoapHeader
					.setTransactionId(prop.getProperty("TransactionID", ""));
			UpsoapHeader.setLinkId(prop.getProperty("LinkID", ""));
			UpsoapHeader.setSAN(prop.getProperty("SAN", ""));
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public void initSoapFromFile(String fileHeader) {
		String fileName = fileHeader;
		soapHeader = new RequestSOAPHeader();
		try {
			Properties prop = new Properties();
			prop.load(new FileInputStream(fileName));

			String SpId = prop.getProperty("SpId", "");
			String SpPassword = prop.getProperty("SpPassword", "");
			String TimeStamp = prop.getProperty("TimeStamp", ""); // MMDDHHMMSS
			String temp = SpId + SpPassword + TimeStamp;
			soapHeader.setSpId(SpId);
			soapHeader.setSpPassword(MD5.getMD5(temp));
			soapHeader.setTimeStamp(TimeStamp);
			soapHeader.setProductId(prop.getProperty("ProductId", ""));

			soapHeader.setTransactionId(prop.getProperty("TransactionID", ""));
			String transEnd = prop.getProperty("TransEnd", "").trim();
			if (!transEnd.equals("") && transEnd.length() > 0)
				soapHeader.setTransEnd(EndReason.fromString(transEnd));

			if (prop.getProperty("LinkID", "").length() > 0)
				soapHeader.setLinkId(prop.getProperty("LinkID", ""));

			if (prop.getProperty("OA", "").length() > 3) {
				soapHeader.setOA(new URI(prop.getProperty("OA", "")));
			}

			if (prop.getProperty("FA", "").length() > 1)
				soapHeader.setFA(new URI(prop.getProperty("FA", "")));
			soapHeader.setMulticastMessaging(Boolean.valueOf(prop.getProperty(
					"MulticastMessaging", "")));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void setSoapHeader(org.apache.axis.client.Call _call) {
		try {
			PrefixedQName qName = new PrefixedQName(
					"http://www.chinatelecom.com.cn/schema/ctcc/common/v2_1",
					"RequestSOAPHeader", "");
			SOAPHeaderElement header = new SOAPHeaderElement(qName);
			SOAPElement spID = header.addChildElement("spId");
			spID.addTextNode(soapHeader.getSpId());
			SOAPElement spPassword = header.addChildElement("spPassword");
			spPassword.addTextNode(soapHeader.getSpPassword());
			SOAPElement timeStamp = header.addChildElement("timeStamp");
			timeStamp.addTextNode(soapHeader.getTimeStamp()); // change by xm
			SOAPElement serviceId = header.addChildElement("productId");
			serviceId.addTextNode(soapHeader.getProductId());
			SOAPElement san = header.addChildElement("SAN");
			san.addTextNode(soapHeader.getSAN());
			if (soapHeader.getTransactionId() != null) {
				SOAPElement transactionID = header
						.addChildElement("transactionId");
				transactionID.addTextNode(soapHeader.getTransactionId());
			}
			SOAPElement transEnd = header.addChildElement("transEnd");
			transEnd.addTextNode(soapHeader.getTransEnd().getValue());
			if (soapHeader.getLinkId() != null) {
				SOAPElement linkID = header.addChildElement("linkId");
				linkID.addTextNode(soapHeader.getLinkId());
			}
			if (soapHeader.getOA() != null
					&& soapHeader.getOA().toString().length() > 3) {
				SOAPElement oa = header.addChildElement("OA");
				oa.addTextNode(soapHeader.getOA().toString());
			}

			if(soapHeader.getFA()!=null)
			{
				SOAPElement fa = header.addChildElement("FA");
				fa.addTextNode(soapHeader.getFA().toString());
			}
			SOAPElement multicastMessaging = header
					.addChildElement("multicastMessaging");
			multicastMessaging.addTextNode(soapHeader.getMulticastMessaging()
					.toString());
			_call.addHeader(header);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void setSoapHeader(SOAPEnvelope responseEnvelope) {
		try {
			PrefixedQName qName = new PrefixedQName(
					"http://www.chinatelecom.com.cn/schema/ctcc/common/v2_1",
					"RequestSOAPHeader", "");
			SOAPHeaderElement header = new SOAPHeaderElement(qName);
			SOAPElement spID = header.addChildElement("spId");
			spID.addTextNode(soapHeader.getSpId());
			SOAPElement spPassword = header.addChildElement("spPassword");
			spPassword.addTextNode(soapHeader.getSpPassword());
			// SOAPElement timeStamp = header.addChildElement("timeStamp");
			// timeStamp.addTextNode(soapHeader.getTimeStamp());
			SOAPElement serviceId = header.addChildElement("serviceId");
			serviceId.addTextNode(soapHeader.getProductId());
			// SOAPElement san = header.addChildElement("SAN");
			// san.addTextNode(soapHeader.getSAN());
			SOAPElement transactionID = header.addChildElement("transactionId");
			transactionID.addTextNode(soapHeader.getTransactionId());
			// SOAPElement transEnd = header.addChildElement("transEnd");
			// transEnd.addTextNode(soapHeader.getTransEnd().getValue());
			// SOAPElement linkID = header.addChildElement("linkId");
			// linkID.addTextNode(soapHeader.getLinkId());
			SOAPElement oa = header.addChildElement("OA");
			oa.addTextNode(soapHeader.getOA().toString());
			SOAPElement fa = header.addChildElement("FA");
			fa.addTextNode(soapHeader.getFA().toString());
			// SOAPElement multicastMessaging =
			// header.addChildElement("multicastMessaging");
			// multicastMessaging.addTextNode(soapHeader.getMulticastMessaging().toString());
			// _call.addHeader(header);
			responseEnvelope.addHeader(header);
		} catch (Exception ex) {
		}
	}

	public void setUpSoapHeader(org.apache.axis.client.Call _call) {
		try {
			PrefixedQName qName = new PrefixedQName(
					"http://www.chinatelecom.com.cn/schema/ctcc/common/v2_1",
					"NotifySOAPHeader", "");
			SOAPHeaderElement Upheader = new SOAPHeaderElement(qName);
			SOAPElement spID = Upheader.addChildElement("spId");
			spID.addTextNode(UpsoapHeader.getSpId());
			SOAPElement spPassword = Upheader.addChildElement("spRevPassword");
			spPassword.addTextNode(UpsoapHeader.getSpRevpassword());

			// SOAPElement serviceId = Upheader.addChildElement("serviceId");
			// serviceId.addTextNode(UpsoapHeader.getProductId());

			SOAPElement transactionID = Upheader
					.addChildElement("transactionId");
			transactionID.addTextNode(UpsoapHeader.getTransactionId());
			SOAPElement transEnd = Upheader.addChildElement("transEnd");

			SOAPElement linkID = Upheader.addChildElement("linkId");
			linkID.addTextNode(UpsoapHeader.getLinkId());

			_call.addHeader(Upheader);
		} catch (Exception ex) {
		}
	}

	
	public static SOAPHeaderElement getHearder(RouteSoapHeader routeSoapHeader){
		

		// SOAPElement serviceId = Upheader.addChildElement("serviceId");
		// serviceId.addTextNode(UpsoapHeader.getProductId());

		try {
			PrefixedQName qName = new PrefixedQName(
					"http://www.chinatelecom.com.cn/schema/ctcc/common/v2_1",
					"NotifySOAPHeader", "");
			SOAPHeaderElement Upheader = new SOAPHeaderElement(qName);
			SOAPElement spID = Upheader.addChildElement("spId");
			spID.addTextNode(routeSoapHeader.UpsoapHeader.getSpId());
			SOAPElement spPassword = Upheader.addChildElement("spRevPassword");
			spPassword.addTextNode(routeSoapHeader.UpsoapHeader
					.getSpRevpassword());
			SOAPElement transactionID = Upheader
					.addChildElement("transactionId");
			transactionID.addTextNode(routeSoapHeader.UpsoapHeader
					.getTransactionId());
			SOAPElement transEnd = Upheader.addChildElement("transEnd");
			SOAPElement linkID = Upheader.addChildElement("linkId");
			linkID.addTextNode(routeSoapHeader.UpsoapHeader.getLinkId());
			return Upheader;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
