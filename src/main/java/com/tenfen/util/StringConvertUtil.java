package com.tenfen.util;

import java.io.UnsupportedEncodingException;

import com.tenfen.util.encrypt.Base64Codec;
import com.tenfen.util.encrypt.DESede;

/**
 * 字符串转换工具类
 * @file StringConvertUtil.java
 * @author chenjinjie
 * @created Mar 28, 2011
 * @version v1.0
 */
public class StringConvertUtil {
	/**
	* 将初始化向量的字符串表现形式转换为字节数组
	* 
	* @param ivString
	*            　初始化向量的字符串
	* @return　初始化向量的字节数组
	*/
	public static byte[] ivGeneration(String ivString) {
		byte[] ivBytes = new byte[ivString.length()];
		for (int i = 0; i < ivString.length(); i++) {
			ivBytes[i] = Byte.parseByte(ivString.substring(i, i + 1));
		}
		return ivBytes;
	}

	/**
	 * 3DES加密后进行base64编码
	 * @author chenjinjie
	 * @created Mar 28, 2011
	 * @version v1.0
	 * @param msg
	 * @return
	 */
	public static String encodeString(String msg) {
		String result = null;
		if (Utils.isEmpty(msg)) {
			return result;
		}
		try {
			DESede deSede = new DESede();
			deSede.setKey("B97FED4E9994E33353F2A65A063DFAA8A31428E11BD7AE59".getBytes("UTF-8"));

			byte[] iv2 = { 1, 2, 3, 4, 5, 6, 7, 8 };
			//3DES加解密向量定义为8 字节数组IV =“12345678”
			deSede.setIv(iv2);
			byte[] data = msg.getBytes("UTF-8");
			//3DES加密后进行base64编码
			//	        result=BASE64.base64Encode(deSede.encrypt(data));
			result = Base64Codec.encode(deSede.encrypt(data));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 对密文进行base64解码后再进行3DES解码
	 * @author chenjinjie
	 * @created Mar 28, 2011
	 * @version v1.0
	 * @param msg
	 * @return
	 */
	public static String decodeString(String msg) {
		String result = null;
		if (Utils.isEmpty(msg)) {
			return result;
		}
		try {
			DESede deSede = new DESede();
			deSede.setKey("B97FED4E9994E33353F2A65A063DFAA8A31428E11BD7AE59".getBytes("UTF-8"));

			byte[] iv2 = { 1, 2, 3, 4, 5, 6, 7, 8 };
			//3DES加解密向量定义为8 字节数组IV =“12345678”
			deSede.setIv(iv2);
			//对密文进行base64解码后再进行3DES解码
			//          byte[] resultData=deSede.decrypt(BASE64.base64Decode(msg)); 
			byte[] resultData = deSede.decrypt(Base64Codec.encode(msg).getBytes("UTF-8"));
			result = new String(resultData, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}


    /**
	 * 十六进制字符串到字节数组的转换
	 * 
	 * @param s
	 *            十六进制字符串
	 * @return 字节数组
	 */
	public static byte[] hexStringToByteArray(String s) {
		byte[] buf = new byte[s.length() / 2];
		for (int i = 0; i < buf.length; i++) {
			buf[i] = (byte) (chr2hex(s.substring(i * 2, i * 2 + 1)) * 0x10 + chr2hex(s.substring(i * 2 + 1, i * 2 + 2)));
		}
		return buf;
	}

	/**
	 * 十六进制字符到字节类型数据的转换
	 * 
	 * @param chr
	 *            十六进制字符
	 * @return 字节类型数据
	 */
	private static byte chr2hex(String chr) {
		if ("0".equals(chr)) {
			return 0x00;
		} else if ("1".equals(chr)) {
			return 0x01;
		} else if ("2".equals(chr)) {
			return 0x02;
		} else if ("3".equals(chr)) {
			return 0x03;
		} else if ("4".equals(chr)) {
			return 0x04;
		} else if ("5".equals(chr)) {
			return 0x05;
		} else if ("6".equals(chr)) {
			return 0x06;
		} else if ("7".equals(chr)) {
			return 0x07;
		} else if ("8".equals(chr)) {
			return 0x08;
		} else if ("9".equals(chr)) {
			return 0x09;
		} else if ("A".equals(chr)) {
			return 0x0a;
		} else if ("B".equals(chr)) {
			return 0x0b;
		} else if ("C".equals(chr)) {
			return 0x0c;
		} else if ("D".equals(chr)) {
			return 0x0d;
		} else if ("E".equals(chr)) {
			return 0x0e;
		} else if ("F".equals(chr)) {
			return 0x0f;
		} else {
			return 0x00;
		}
	}
	
}
