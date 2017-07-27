package com.tenfen.util;

import com.tenfen.util.encrypt.MD5;

public class UrlRewriteUtil {
	
	/**
	 * 长地址转化为短地址
	 * @param url
	 * @return
	 */
	public String[] ShortUrl(String url){
		//可以自定义生成MD5加密字符传前的混合KEY
		String key = "wapread";
		//要使用生成URL的字符
        String[] chars = new String[]{
        		"a","b","c","d","e","f","g","h",
        		"i","j","k","l","m","n","o","p",
        		"q","r","s","t","u","v","w","x",
        		"y","z","0","1","2","3","4","5",
        		"6","7","8","9","A","B","C","D",
        		"E","F","G","H","I","J","K","L",
        		"M","N","O","P","Q","R","S","T",
        		"U","V","W","X","Y","Z"
        };
        String[] resUrl = new String[4];
        try {
        	//对传入网址进行MD5加密
//        String hex = System.Web.Security.FormsAuthentication.HashPasswordForStoringInConfigFile(key + url, "md5");
        	String hex = MD5.getMD5((key+url).getBytes("utf-8"));
        	
        	for (int i = 0; i < 4; i++)
        	{
        		//把加密字符按照8位一组16进制与0x3FFFFFFF进行位与运算
//        		String a = toHexString(hex.substring(i * 8, 8));
//        		System.out.println(hex.substring(i * 8, i * 8 + 7));
        		int hexint = 0x3FFFFFFF & Integer.parseInt(hex.substring(i * 8, i * 8 + 7), 16);
        		String outChars = "";
        		for (int j = 0; j < 6; j++)
        		{
        			//把得到的值与0x0000003D进行位与运算，取得字符数组chars索引
        			int index = 0x0000003D & hexint;
        			//把取得的字符相加
        			outChars += chars[index];
        			//每次循环按位右移5位
        			hexint = hexint >> 5;
        		}
        		//把字符串存入对应索引的输出数组
        		resUrl[i] = outChars;
        	}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return resUrl;
    }
	
	//转化字符串为十六进制编码
	@SuppressWarnings("unused")
	private String toHexString(String s){
		String str="";
		for (int i=0;i<s.length();i++)
		{
			int ch = (int)s.charAt(i);
			String s4 = Integer.toHexString(ch);
			str = str + s4;
		}
		return "0x" + str;//0x表示十六进制
	}

	
	public static void main(String[] args){
		String[] b = null;
    	String url="http://wap.tyread.com/goChapterContent.action?bookId=1000003001275&chapterId=5&curpage=2";
    	try{
    		UrlRewriteUtil u = new UrlRewriteUtil();
    		b = u.ShortUrl(url);
    		
    		for (int i = 0; i < b.length; i++) {
				System.out.println(b[i]);
			}
    	} catch (Exception e) {
			e.printStackTrace();
		}
    }
	
}
