package com.tenfen.util.encrypt;
import java.security.Security;  
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

/** 
 * 3DES的编解码工具类
 * 
 * @author JiangXiaofeng
 * @date 2010-9-2
 */  
public class DESede {  

	final static String Algorithm = "DESede/CBC/PKCS7Padding";  //加密方法／运算模式／填充模式  
    private byte[] key;  								//秘密密钥
    private byte[] iv;  									//3DES加解密向量
      
    static {  
        //添加JCE支持(sun有其默认实现)  
        Security.addProvider(new BouncyCastleProvider());  
    }  
    
    public void setKey(byte[] key) {  
        this.key = key;  
    }  
      
    public void setIv(byte[] iv) {  
        this.iv = iv;  
    }  
  
    /** 
     * 3DES加密 
     * @param data 明文数据
     * @return 3DES加密后的密文(字节数组)
     */  
    public byte[] encrypt(byte[] data) {  
        byte[] b = null;
        try {  
            Cipher cipher = Cipher.getInstance(Algorithm,"BC");  
            SecretKeyFactory skf = SecretKeyFactory.getInstance("DESede");  
            SecretKey sk = skf.generateSecret(new DESedeKeySpec(key));  
            IvParameterSpec ips = new IvParameterSpec(iv);  
            cipher.init(Cipher.ENCRYPT_MODE, sk, ips);  
            b = cipher.doFinal(data);  
        } catch (Exception ex) {  
            ex.printStackTrace();  
        }  
        return b;  
    }
    
    /** 
     * 3DES解密 
     * @param data 3DES加密后的密文(字节数组)
     * @return 3DES解密后的明文数据(字节数组)
     */  
    public byte[] decrypt(byte[] data) {  
        byte[] b = null;
        try {  
            Cipher cipher = Cipher.getInstance(Algorithm);  
            SecretKeyFactory skf = SecretKeyFactory.getInstance("DESede");  
            SecretKey sk = skf.generateSecret(new DESedeKeySpec(key));  
            IvParameterSpec ips = new IvParameterSpec(iv);  
            cipher.init(Cipher.DECRYPT_MODE, sk, ips);
            b = cipher.doFinal(data);
        } catch (Exception ex) {  
            ex.printStackTrace();  
        }  
        return b;  
    }  
      
    public static void main(String[] args) throws Exception { 
    	
//    	DESede deSede = new DESede();
//    	
//    	//秘密密钥
//    	deSede.setKey("B97FED4E9994E33353F2A65A063DFAA8A31428E11BD7AE59".getBytes("UTF-8"));  
//        
//        /**
//         * 
//         * 3DES加解密向量定义为8 字节数组IV =“12345678”
//         */
//    	
//    	deSede.setIv("12345678".getBytes());
//    	System.out.println("12345678".getBytes().length);
//    	
//        //用于加密的明文
//    	byte[] data="百姓人家".getBytes("UTF-8");  
//    	System.out.println(deSede.encrypt(data));
        
//        //3DES加密后进行base64编码
//        String e=BASE64.base64Encode(deSede.encrypt(data));
//        
//        //对密文进行base64解码后再进行3DES解码
//        byte[] d=deSede.decrypt(BASE64.base64Decode(e));  
//        
//        System.out.println("加密前:"+data+"\n加密后:"+e+"\n解密后:"+new String(d));  
//        
//        
//        DESede deSede2 = new DESede();
//    	
//    	//秘密密钥
//        deSede2.setKey("B97FED4E9994E33353F2A65A063DFAA8A31428E11BD7AE59".getBytes("UTF-8")); 
//        
//        byte[] iv2 = { 1, 2, 3, 4, 5, 6, 7, 8 };
//        
//        String vistring = new String(iv2);
//        System.out.println(vistring);
//        byte [] bytetemp = vistring.getBytes("GBK");
//        
//        //3DES加解密向量定义为8 字节数组IV =“12345678”
//        deSede2.setIv(iv2);
//    	System.out.println(iv2.length);
//    	
//        //用于加密的明文
//    	byte[] data2="百姓人家".getBytes("UTF-8");  
//        
//        //3DES加密后进行base64编码
//        String e2=BASE64.base64Encode(deSede2.encrypt(data2));
//        
//        //对密文进行base64解码后再进行3DES解码
//        byte[] d2=deSede2.decrypt(BASE64.base64Decode(e2));  
//        
//        System.out.println("加密前:"+data2+"\n加密后:"+e2+"\n解密后:"+new String(d2)); 
//        
//        System.out.println("123");
//        byte[] byte1231 = "123".getBytes();
//        System.out.println(byte1231);
//        byte[] byte1232 = "123".getBytes();
//        System.out.println("123".getBytes());
        
//        DESede deSede = new DESede();
//        deSede.
        
    	
    	Cipher cipher = Cipher.getInstance("DESede");
        SecretKey sk = new SecretKeySpec("6A898E3EFD775A809E86A3C1".getBytes("UTF-8"),"DESede");
        cipher.init(Cipher.ENCRYPT_MODE, sk);
        System.out.println(cipher.doFinal("杨锦波".getBytes("UTF-8")));
    }  
}  
