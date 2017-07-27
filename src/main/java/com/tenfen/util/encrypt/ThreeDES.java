package com.tenfen.util.encrypt;

import java.security.Key;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;


public class ThreeDES{
	private static String private_key = "";
	
	public Key getKey(String keystr){
			Key key=null;
			try {
				KeyGenerator kg=KeyGenerator.getInstance("DESede");
				SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG","SUN");  
	            secureRandom.setSeed(keystr.getBytes("UTF-8")); 
				kg.init(secureRandom);
				key=kg.generateKey();
			} catch (Exception e) {
				System.out.println("获取DES KEY异常:"+e);
			}
		return key;
	}

	
	public String encoder(String str,Key key){
		String result="";
		try {
			Cipher cipher=Cipher.getInstance("DESede/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			byte[] ebytes=cipher.doFinal(str.getBytes("UTF-8"));
			BASE64Encoder encoder=new BASE64Encoder();
			result=encoder.encode(ebytes);
		} catch (Exception e) {
			System.out.println("用DES加密异常:"+ e);
		} 
		return result;
	}
	
	
	public String encoder(String str){
		return encoder(str, private_key);//默认密钥
	}

	public String encoder(String str, String strkey){
		Key key=this.getKey(strkey);
		return encoder(str, key);
	}
	
	public String decoder(String str,Key key){
		String result="";
			Cipher cipher;
			try {
				cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
				cipher.init(Cipher.DECRYPT_MODE, key);
				BASE64Decoder decoder=new BASE64Decoder();
				byte[] bytes=decoder.decodeBuffer(str);
				byte[] dbytes=cipher.doFinal(bytes);
				result=new String(dbytes,"UTF-8");
			} catch (Exception e) {
				System.out.println("desede解密异常"+e);
			}
			
		return result;
	}
	
	public String decoder(String str){
		Key key_=this.getKey(private_key);
		return decoder(str, key_);
	}
	
	public String decoder(String str, String key) {
		Key key_=this.getKey(key);
		return decoder(str, key_);
	}
	   
	public static void main(String[] args) {
		System.out.println(new ThreeDES().encoder("杨锦波yjb"));
		System.out.println(new ThreeDES().decoder(new ThreeDES().encoder("杨锦波yjb")));
	}
}
