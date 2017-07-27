package com.tenfen.util.encrypt;

import java.io.IOException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import sun.misc.BASE64Encoder;
import sun.misc.BASE64Decoder;

public class AES128Cryptor {

	public static byte[] encrypt(String input, String key) {
		
		byte[] pwd = new byte[16];
		for (int j = 0; j < 16; j++) {
			pwd[j] = 0;
		}
		for (int i = 0; i < key.length(); i++) {
			pwd[i % 16] = (byte) (pwd[i % 16] ^ key.charAt(i));
		}
		
		byte[] crypted = null;
		try {
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(pwd, "AES"));
			crypted = cipher.doFinal(input.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return crypted;
	}
	
	public static String decrypt(byte[] input, String key) {
		
		byte[] pwd = new byte[16];
		for (int j = 0; j < 16; j++) {
			pwd[j] = 0;
		}
		for (int i = 0; i < key.length(); i++) {
			pwd[i % 16] = (byte) ((pwd[i % 16] ^ key.charAt(i)));
		}
		
		byte[] output = null;
		try {
			SecretKeySpec skey = new SecretKeySpec(pwd, "AES");

			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			
			cipher.init(Cipher.DECRYPT_MODE, skey);
			output = cipher.doFinal(input);
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		return new String(output);
	}

	public static void test() throws IOException{
		BASE64Decoder base64Decoder = new sun.misc.BASE64Decoder();
		BASE64Encoder base64Encoder = new sun.misc.BASE64Encoder();
		String k = "UCADS!@qkl";
		
		String input = "460036720415240";
		System.out.println(base64Encoder.encode(AES128Cryptor.encrypt(input, k)));
		System.out.println(AES128Cryptor.decrypt(base64Decoder.decodeBuffer("a0w5AgF6g0vMbwr7ukXOmA=="), k));
		
		String input2 = "460030906175533";
		System.out.println(base64Encoder.encode(AES128Cryptor.encrypt(input2, k)));
		System.out.println(AES128Cryptor.decrypt(base64Decoder.decodeBuffer("sZqp8zcasJ/LTYE5nclBgQ=="), k));
		
		String input3 = "460008051482696";
		System.out.println(base64Encoder.encode(AES128Cryptor.encrypt(input3, k)));
		System.out.println(AES128Cryptor.decrypt(base64Decoder.decodeBuffer("EfeMQdjq6P5s/qBXJcFMyg=="), k));
		
	}
	
	public static void main(String[] args) throws IOException {
		test();
	}
}
