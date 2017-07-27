package com.tenfen.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.tenfen.util.encrypt.BASE64;

public class CTUtil {

	public static String queryPhoneByIMSI(String imsi){
		String phone = null;
		try {
			String deviceNo = "3500000000404101";
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String timeStamp = sdf.format(new Date());
			String ency = deviceNo + imsi + timeStamp + "tyyd";
			String authenticator = getAuthenicator(ency);
			//获取手机号码
			String result = QueryMDNByIMSI(deviceNo, imsi, timeStamp, authenticator, "tyyd");
			phone = RegExp.getString(result, "(?<=<MDN>).*(?=</MDN>)");
			if (phone.length() > 0) {
				phone = phone.trim();
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		
		return phone;
	}
	
	public static String QueryMDNByIMSI(String deviceNo, String IMSI, String timeStamp, String authenticator,
			String extension) {
		// TODO 自动生成方法存根
		try {
			URL urls = new URL("http://zx.passport.189.cn/UDBAPPInterface/UDBAPPSYS/MDNIMSIRelation.asmx");
			StringBuffer message = new StringBuffer();
//			message.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
//			.append("<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">")
//			.append("<soap:Body>")
//			.append("<QueryMDNByIMSI  xmlns=\"http://udb.chinatelecom.com\">")
//			.append("<DeviceNo><![CDATA[" + deviceNo + "]]></DeviceNo>")
//			.append("<IMSI><![CDATA[" + IMSI + "]]></IMSI>")
//			.append("<StampTime><![CDATA[" + timeStamp + "]]></StampTime>")
//			.append("<Extension><![CDATA[" + extension + "]]></Extension>")
//			.append("<Authencitator><![CDATA[" + authenticator + "]]></Authencitator>")
//			.append("</QueryMDNByIMSI>")
//			.append("</soap:Body>")
//			.append("</soap:Envelope>");
			
			message.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
			.append("<soap12:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap12=\"http://www.w3.org/2003/05/soap-envelope\">")
			.append("<soap12:Body>")
			.append("<QueryMDNByIMSI xmlns=\"http://udb.chinatelecom.com\">")
			.append("<DeviceNo><![CDATA[" + deviceNo + "]]></DeviceNo>")
			.append("<IMSI><![CDATA[" + IMSI + "]]></IMSI>")
			.append("<StampTime><![CDATA[" + timeStamp + "]]></StampTime>")
			.append("<Extension><![CDATA[" + extension + "]]></Extension>")
			.append("<Authencitator><![CDATA[" + authenticator + "]]></Authencitator>")
			.append("</QueryMDNByIMSI>")
			.append("</soap12:Body>")
			.append("</soap12:Envelope>");
			String msg = message.toString();
			
			byte[] bytes = msg.getBytes("UTF-8");
			HttpURLConnection con = (HttpURLConnection) urls.openConnection();
			con.setConnectTimeout(10000);
			con.setReadTimeout(10000);
			con.setRequestProperty("Content-length", String.valueOf(bytes.length));
			con.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
//			con.setRequestProperty("SOAPAction", "http://udb.chinatelecom.com/QueryMDNByIMSI");
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.setDoInput(true);
			OutputStream out = con.getOutputStream();
			
			out.write(bytes);
			out.flush();
			out.close();
			
			InputStream incoming = con.getInputStream();
			byte[] b = new byte[incoming.available()];
			incoming.read(b);
			return new String(b);
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
			return null;
		}
	}
	
	public static String getAuthenicator(String sourceStr) {
		String strResult = "";
		try {

			Cipher c3des = Cipher.getInstance("DESede/CBC/PKCS5Padding");
			SecretKeySpec myKey = new SecretKeySpec(StringConvertUtil.hexStringToByteArray("6A898E3EFD775A809E86A3C168F36B8C02596740DCCC1FAC"), "DESede");
			IvParameterSpec ivspec = new IvParameterSpec(StringConvertUtil.ivGeneration("12345678"));
			c3des.init(Cipher.ENCRYPT_MODE, myKey, ivspec);

			byte[] testSrc = sourceStr.getBytes();
			MessageDigest alga = MessageDigest.getInstance("SHA-1");
			alga.update(testSrc);
			byte[] digesta = alga.digest();

			byte[] encoded = c3des.doFinal(digesta);
			strResult = BASE64.encode(encoded);
		} catch (Exception e) {
			strResult = "";
			System.out.println("Decrypt failure!!!" + e.getMessage());
		}

		return strResult;
	}	
	
	
	
	public static void main(String[] args) {
		CTUtil.queryPhoneByIMSI("15311424842");
	}
}
