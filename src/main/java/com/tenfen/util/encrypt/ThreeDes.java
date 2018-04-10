package com.tenfen.util.encrypt;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;


/*字符串 DESede(3DES) 加密
 * ECB模式/使用PKCS7方式填充不足位,目前给的密钥是192位
 * 3DES（即Triple DES）是DES向AES过渡的加密算法（1999年，NIST将3-DES指定为过渡的
 * 加密标准），是DES的一个更安全的变形。它以DES为基本模块，通过组合分组方法设计出分组加
 * 密算法，其具体实现如下：设Ek()和Dk()代表DES算法的加密和解密过程，K代表DES算法使用的
 * 密钥，P代表明文，C代表密表，这样，
 * 3DES加密过程为：C=Ek3(Dk2(Ek1(P)))
 * 3DES解密过程为：P=Dk1((EK2(Dk3(C)))
 * */
public class ThreeDes {

	/**
	 * @param args在java中调用sun公司提供的3DES加密解密算法时，需要使
	 * 用到$JAVA_HOME/jre/lib/目录下如下的4个jar包：
	 *jce.jar
	 *security/US_export_policy.jar
	 *security/local_policy.jar
	 *ext/sunjce_provider.jar 
	 */
	
	private static final String Algorithm = "DESede"; //定义加密算法,可用 DES,DESede,Blowfish
    //keybyte为加密密钥，长度为24字节    
	//src为被加密的数据缓冲区（源）
	public static byte[] encryptMode(byte[] keybyte,byte[] src){
		Key deskey = null;
		
		try {
			DESedeKeySpec spec = new DESedeKeySpec(keybyte);
			SecretKeyFactory keyfactory = SecretKeyFactory.getInstance(Algorithm);
			deskey = keyfactory.generateSecret(spec);
			Cipher cipher = Cipher.getInstance(Algorithm + "/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, deskey);
			byte[] bOut = cipher.doFinal(src);
			return bOut;
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	
	}
	
	//keybyte为加密密钥，长度为24字节    
	//src为加密后的缓冲区
	public static byte[] decryptMode(byte[] keybyte,byte[] src){
		try {
			//生成密钥
			SecretKey deskey = new SecretKeySpec(keybyte, Algorithm);
			//解密
			Cipher c1 = Cipher.getInstance(Algorithm);
			c1.init(Cipher.DECRYPT_MODE, deskey);
			return c1.doFinal(src);
		} catch (java.security.NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		}catch(javax.crypto.NoSuchPaddingException e2){
			e2.printStackTrace();
		}catch(java.lang.Exception e3){
			e3.printStackTrace();
		}
		return null;		
	}
	
    //转换成十六进制字符串
	public static String byte2Hex(byte[] b){
		String hs="";
		String stmp="";
		for(int n=0; n<b.length; n++){
			stmp = (java.lang.Integer.toHexString(b[n]& 0XFF));
			if(stmp.length()==1){
				hs = hs + "0" + stmp;				
			}else{
				hs = hs + stmp;
			}
			if(n<b.length-1)hs=hs+":";
		}
		return hs.toUpperCase();		
	}
	public static void main(String[] args) {
		//添加新安全算法,如果用JCE就要把它添加进去
		byte[] key="channel3des_012345678910".getBytes();
		
		//Security.addProvider(new com.sun.crypto.provider.SunJCE());
		String password = "{\"sign\":\"xxxxxxxxxxxxxxxxxxxx\",\"amount\":\"1000\",\"payer\":\"11111111111\",\"attach\":\"备注\",\"buyer\":\"93237040\",\"clientIp\":\"0.0.0.0\",\"productName\":\"产品名称\",\"productId\":\"123456\",\"appKey\":\"012345678910\"}";//密码
		System.out.println("加密前的字符串:" + password);
		byte[] encoded = encryptMode(key,password.getBytes());
		
		String pword = Base64.encodeBase64String(encoded);
		System.out.println("加密后的字符串:" + pword);
		
		byte[] srcBytes = decryptMode(key,Base64.decodeBase64(pword));
		System.out.println("解密后的字符串:" + (new String(srcBytes)));
		
		
	}
}

