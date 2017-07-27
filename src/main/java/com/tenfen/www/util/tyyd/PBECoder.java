/*
 * 
 */
// Created on 2013-5-5

package com.tenfen.www.util.tyyd;

import java.security.Key;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

/**
 * @author joe.chen
 */
public abstract class PBECoder extends Coder {

    /**
     * 支持以下任意一种算法
     * 
     * <pre>
     * PBEWithMD5AndDES  
     * PBEWithMD5AndTripleDES  
     * PBEWithSHA1AndDESede 
     * PBEWithSHA1AndRC2_40
     * </pre>
     */
    public static final String ALGORITHM = "PBEWITHMD5andDES";

    /**
     * 盐初始化
     * 
     * @return
     * @throws Exception
     */
    public static byte[] initSalt() throws Exception {
        byte[] salt = new byte[8];
        Random random = new Random();
        random.nextBytes(salt);
        return salt;
    }

    /**
     * 转换密钥<br>
     * 
     * @param password
     * @return
     * @throws Exception
     */
    private static Key toKey(String password) throws Exception {
        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray());
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
        SecretKey secretKey = keyFactory.generateSecret(keySpec);

        return secretKey;
    }
    
    public static byte[] encrypt(byte[] data) throws Exception{
    	byte[] salt = new byte[]{126, 27, 33, 64, 57, 76, 87, 98};
    	String password = "fb887dcf145644058c3739e977f5a854";
    	return encrypt(data,password,salt);
    }

    /**
     * 加密
     * 
     * @param data 数据
     * @param password 密码
     * @param salt 盐
     * @return
     * @throws Exception
     */
    public static byte[] encrypt(byte[] data, String password, byte[] salt) throws Exception {

        Key key = toKey(password);

        PBEParameterSpec paramSpec = new PBEParameterSpec(salt, 100);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);

        return cipher.doFinal(data);

    }

    public static byte[] decrypt(byte[] data) throws Exception{
    	byte[] salt = new byte[]{126, 27, 33, 64, 57, 76, 87, 98};
    	String password = "fb887dcf145644058c3739e977f5a854";
    	return decrypt(data,password,salt);
    }
    
    /**
     * 解密
     * 
     * @param data 数据
     * @param password 密码
     * @param salt 盐
     * @return
     * @throws Exception
     */
    public static byte[] decrypt(byte[] data, String password, byte[] salt) throws Exception {

        Key key = toKey(password);

        PBEParameterSpec paramSpec = new PBEParameterSpec(salt, 100);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, paramSpec);

        return cipher.doFinal(data);

    }
    
    public static void main(String[] args ) throws Exception {
    	byte[] salt = new byte[]{126, 27, 33, 64, 57, 76, 87, 98};
    	String s = "test2#0";
    	String password = "12345";
    	System.out.println(s.length());
        byte[] b = PBECoder.encrypt(s.getBytes("UTF-8"), password, salt);
        String str = PBECoder.encryptBASE64(b);
        System.out.println(str.length());
        System.out.println(str);
        b = PBECoder.decrypt(PBECoder.decryptBASE64("nENdV0RfbcaBURSyV0mHSFLIYo6chafJaQCT6I/Rp51Har9Scn34JN3gBKd5hU/Tg6wwCkEGtN5tg6IRD3xSjdXC9f1AdSPdRoQefArB8UWQy+wPg2WMswRWvt/2Q1LGpuTtw6f/lkzM7bH8mxdPB+iJ00jmtGUiMMlphMPAFPo4tQJQFegxc5xXHM4L2U85NhOMLXKPhCZ+EsjPTwJdMkaNVpS3dwyJ7f25DGy1YMaKIwzSfpxaJiZJ2lp4bRRFN6bZgDwY1KwHXsDDy3qALCaAK6aI9R2QbS6L4UlXI/fDkRzEwEMM/70B3A2NF2RkFD1D8Tklj1gCpKX9fMc7O/asP7g/PSncOrsSsuhv4hmWsQK7GlbJnYHIVaBDnPKTo8u0ahM91gZzj19//B/5+UUyHjslj8dksNM1O6cah80XqHxVwyuuG3tI77p/zFF9zqWce9xMBgGxrPNQv7uS0gBYcd6P8ypoYVIbpzQoskkAbvtK9/LtNUroW/9dhkQE/mU23pdJMXNyccuzlFgjApJGlQZyzJKthsQkyTWyvtQMIarNl3kbllPBkS21YcJFIjUuitAOFT44gR265Ou42WjR0wCHa1kgpnwuJHMTVvG4+lVcPW6sYCBp9RUYPLs8SZAiFnVvOEuxG0vRDF5ynX6JBI7z+KN4uQ3x+y+1b5oX4QrCuD3sePjJaTETDbtIrLVdqWKdWFszb4+2PtQzKMiyqjVb1vszSN1uK9ZlJBbQO4tb/30uoKxp5RfBnnjBo65iEw9XzNEcYb4tBb0ASL4EiOOwHdN7qTmh7Jl8MOUqZMIsxRKtB228egYOHsrlpnzgwrwcFEkE4rWx9D/ScwjOVERLg8EKKavYXUAQMtcd0EYf9kNtuhZ8wRPqKWmO580X5owP/PmW25gUXV2w04n0sFJD/YUGfeLeXeoGusdxgjfOiSag00tCUpWReEKx3WHNMRSaZNgvd2kQHWZsIQJwDmwOb6bHbqxnT6EVCV+F2tiqjSeTVz8bCvCEz4IdAgDVcd0cZlZkfFiOuIX0TFBV53uZzR+UiVLiRShMXZZGkVDE2gXlBR5lTUls6HKhIb8Jx806wzG/2qsS8x4Vj9aQGAtucJZzy07jQAjVSYQC8GAbpTJasw5E88E0JhkAL+TohZp0hRfG5kVZ9piUnEr4788n7WKSMX3x31YR0fi1pCDfU9BzGopueC3TirPnhuSVCh5+AVegYxo8dAWggxZLANOuatdamkSEl5hbaRFXqfuIWmw2inHiIyukEWtuubP+Mge72i5rDuvstxkILphv3DqZ8+lKrKw6oma9394ZYdxPye61H4tGYiOFN2j3AQ7xMqbuXHrC8ozZFerJ88ndC3GXHRuiEN7X6YDR7XMZn9uzlme1M3nJYl88gjaW0ais937onYW1Z+7E/6fSvaCtLpm8ubElSv7r2Mqj/iZcsZuxHpLbvN+5AzNBV5jdkcnoW2GBI/rbi+B/Ou/Hf354ye7n09ahDE+bNZ+55Q8xmV7/qx2TgzK8fyC/LPvVuLqUacUMOshT5i4LoQooE/jR/tFuoNVikBvbSfQOjPo="), password, salt);
        System.out.println(new String(b, "UTF-8"));
    }
    
}
