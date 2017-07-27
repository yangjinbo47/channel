package com.tenfen.www.webservice.chinatelecom.wsdl.ctcc.sms.send._interface;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.xml.soap.SOAPElement;

import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.client.Call;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.message.SOAPHeaderElement;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.axis.types.URI;

import com.tenfen.www.webservice.chinatelecom.schema.ctcc.common.EndReason;
import com.tenfen.www.webservice.chinatelecom.schema.ctcc.common.NotifySOAPHeader;
import com.tenfen.www.webservice.chinatelecom.schema.ctcc.common.RequestSOAPHeader;

public class DecodeSoapHeader {
  private MessageContext context = null;
  private SOAPEnvelope requestEnvelope = null;
  private SOAPEnvelope responseEnvelope = null;
  private String namespace = "http://www.chinatelecom.com.cn/schema/ctcc/common/v2_1";
  public DecodeSoapHeader(MessageContext context) {
    try {
      this.context = context;
      requestEnvelope = context.getRequestMessage().getSOAPEnvelope();
      responseEnvelope = context.getResponseMessage().getSOAPEnvelope();
    }
    catch (Exception ex) {
    }
  }

  public DecodeSoapHeader(Call call) {
    try {
      Message msg = call.getResponseMessage();
      requestEnvelope = msg.getSOAPEnvelope();
    }
    catch (Exception ex) {
    }
  }

  public RequestSOAPHeader DecodeDownSoapHeader() {
    RequestSOAPHeader soapHeader = new RequestSOAPHeader();
    try {
      SOAPHeaderElement requestSequenceIdHeader = requestEnvelope.
          getHeaderByName(
          "http://www.chinatelecom.com.cn/schema/ctcc/common/v2_1",
          "RequestSOAPHeader");
      Iterator iterator = requestSequenceIdHeader.getChildElements();
      while (iterator.hasNext()) {
        SOAPElement element = (SOAPElement) iterator.next();
        String elementName = element.getElementName().getLocalName();
        if (elementName.equals("spId"))
          soapHeader.setSpId(element.getValue());
        else if (elementName.equals("spPassword"))
          soapHeader.setSpPassword(element.getValue());
        else if (elementName.equals("productId"))
          soapHeader.setProductId(element.getValue());
        else if (elementName.equals("timeStamp"))
          soapHeader.setTimeStamp(element.getValue());
        else if (elementName.equals("SAN"))
          soapHeader.setSAN(element.getValue());
        else if (elementName.equals("FA"))
          soapHeader.setFA(new URI(element.getValue()));
        else if (elementName.equals("linkId"))
          soapHeader.setLinkId(element.getValue());
        else if (elementName.equals("OA"))
          soapHeader.setOA(new URI(element.getValue()));
        else if (elementName.equals("transactionId"))
          soapHeader.setTransactionId(element.getValue());
        else if (elementName.equals("multicastMessaging"))
          soapHeader.setMulticastMessaging(Boolean.valueOf(element.getValue()));
        else if (elementName.equals("transEnd"))
          soapHeader.setTransEnd(EndReason.fromString(element.getValue()));
      }
    }
    catch (Exception ex) {
    }
    return soapHeader;
  }

  public NotifySOAPHeader DecodeUpSoapHeader() {
    NotifySOAPHeader soapHeader = new NotifySOAPHeader();
    try {
      SOAPHeaderElement requestSequenceIdHeader = requestEnvelope.
          getHeaderByName(
          "http://www.chinatelecom.com.cn/schema/ctcc/common/v2_1",
          "NotifySOAPHeader");
      Iterator iterator = requestSequenceIdHeader.getChildElements();
      while (iterator.hasNext()) {
        SOAPElement element = (SOAPElement) iterator.next();
        String elementName = element.getElementName().getLocalName();
        if (elementName.equals("spRevId"))
          soapHeader.setSpRevId(element.getValue());
        else if (elementName.equals("spRevpassword"))
          soapHeader.setSpRevpassword(element.getValue());
        else if (elementName.equals("spId"))
          soapHeader.setSpId(element.getValue());
        else if (elementName.equals("SAN"))
          soapHeader.setSAN(element.getValue());
        else if (elementName.equals("productId"))
          soapHeader.setProductId(element.getValue());
        else if (elementName.equals("linkId"))
          soapHeader.setLinkId(element.getValue());
        else if (elementName.equals("transactionId"))
          soapHeader.setTransactionId(element.getValue());
      }
    }
    catch (Exception ex) {
    }
    return soapHeader;
  }

  public String getIP() {
    HttpServletRequest request = (HttpServletRequest) context.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
    String ip = request.getRemoteAddr();
    return ip;
  }
}
