package com.tenfen.www.util;

import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.tenfen.util.LogUtil;
import com.tenfen.util.encrypt.SHA1;


public class TokenService {
	
	/**
     * 请求参数封装类
     * @author 
     *
     */
    public static class TokenParam {

        private String name;

        private String value;

        public TokenParam(String name, String value){
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

    }
    
    /**
     * TokenParam Comparator
     */
    private final static Comparator<TokenService.TokenParam> TOKEN_COMPARATOR = new Comparator<TokenService.TokenParam>() {
        @Override
        public int compare(TokenParam o1,
                           TokenParam o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };
    
    /**
     * 构建通用Token(天翼阅读)
     * @param tokenParams
     * @param clientSecret
     * @return
     */
    public static String buildToken(List<TokenParam> tokenParams, String clientSecret) {
    	//排序
    	Collections.sort(tokenParams, TOKEN_COMPARATOR);

        StringBuilder tokenBuf = new StringBuilder();

        for (TokenParam param : tokenParams) {
            tokenBuf.append(param.getName()).append("=").append(param.getValue()).append("&");
        }
        //加密前的字符串
        String myMd5 = tokenBuf.deleteCharAt(tokenBuf.length() - 1).append(clientSecret).toString();

        try {
        	myMd5 = myMD5(myMd5);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }

        return myMd5;
    }
    
    /**
     * 构建通用Token(天翼空间-通用)
     * @param tokenParams
     * @param clientSecret
     * @return
     */
    public static String buildTySpaceToken(List<TokenParam> tokenParams, String clientSecret) {
    	//排序
    	Collections.sort(tokenParams, TOKEN_COMPARATOR);
    	
    	StringBuilder tokenBuf = new StringBuilder();

        for (TokenParam param : tokenParams) {
            tokenBuf.append(param.getName()).append(param.getValue());
        }
        String returnString = null;
        try {
        	//url encode
        	String encodeString = URLEncoder.encode(tokenBuf.toString(), "UTF-8");
        	//加密前的字符串
        	String myData = encodeString+clientSecret;
        	
        	returnString = SHA1.getDigestOfString(myData.getBytes()).toLowerCase();
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
    	return returnString;
    }
    
    /**
     * 构建支付宝签名体
     * @param tokenParams
     * @param clientSecret
     * @return
     */
    public static String buildAlipayToken(List<TokenParam> tokenParams) {
    	//排序
    	Collections.sort(tokenParams, TOKEN_COMPARATOR);
    	
    	StringBuilder tokenBuf = new StringBuilder();

        for (TokenParam param : tokenParams) {
            tokenBuf.append(param.getName()).append("=").append(param.getValue()).append("&");
        }

        String returnString = null;
        try {
        	//加密前的字符串
        	String mySign = tokenBuf.deleteCharAt(tokenBuf.length() - 1).toString();
        	
        	returnString = mySign;
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
    	return returnString;
    }
    
    /**
     * 构建wo+融合支付签名体
     * @param tokenParams
     * @param clientSecret
     * @return
     */
    public static String buildWoPlusMixToken(List<TokenParam> tokenParams, String clientSecret) {
    	//排序
    	Collections.sort(tokenParams, TOKEN_COMPARATOR);

        StringBuilder tokenBuf = new StringBuilder();

        for (TokenParam param : tokenParams) {
            tokenBuf.append(param.getName()).append("=").append(param.getValue()).append("&");
        }
        //加密前的字符串
        String myMd5 = tokenBuf.append("key="+clientSecret).toString();

        try {
        	myMd5 = myMD5(myMd5).toUpperCase();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return myMd5;
    }
    
    /**
     * MD5简单实现
     * @param s
     * @return
     */
    private final static String myMD5(String s) {
        char hexDigits[]={'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};       

        try {
            byte[] btInput = s.getBytes("UTF-8");
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            char[] str = encodeHex(md, hexDigits);
            
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 将消息摘要转换成十六进制表示
     * @param data
     * @param toDigits
     * @return
     */
    protected static char[] encodeHex(byte[] data, char[] toDigits) {
        int l = data.length;
        char[] out = new char[l << 1];
        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
            out[j++] = toDigits[0x0F & data[i]];
        }
        return out;
    }


	/**
	 * 测试
	 * @param args
	 */
//	public static void main(String[] args) {
//		try {
//			String method = "getSmsDetail";
//			String channel = "5004";
//			String imsi = "460036970087302";
//			String timestamp = String.valueOf(System.currentTimeMillis());
//			String ver = "1.0";
//			String id_fee = "111_100,222_200";
//			
//			List<TokenParam> queryParamList = new ArrayList<TokenParam>(4);
//			queryParamList.add(new TokenParam("method",method));
//			queryParamList.add(new TokenParam("channel",channel));
//			queryParamList.add(new TokenParam("imsi",imsi));
//			queryParamList.add(new TokenParam("timestamp", timestamp));
//			queryParamList.add(new TokenParam("ver", ver));
//			queryParamList.add(new TokenParam("id_fee", id_fee));
//			String sign = TokenService.buildTySpaceToken(queryParamList, "n2KSZHJyu38K096SvmvQZbO5HZ8");
//			
//			System.out.println(sign);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//	}
    
    public static void main(String[] args) {
		try {
			String sellerKey = "SMS_PARTNER_TEST";
			String imsi = "460036970087302";
			String appName = URLEncoder.encode("有缘网", "UTF-8");
			String fee = "500";
			String outTradeNo = "123456";
			String secret = "877eb16a61d1430884ddcae163fcc064";
			
			List<TokenParam> queryParamList = new ArrayList<TokenParam>(4);
			queryParamList.add(new TokenParam("seller_key",sellerKey));
			queryParamList.add(new TokenParam("imsi",imsi));
			queryParamList.add(new TokenParam("app_name",appName));
			queryParamList.add(new TokenParam("fee",fee+""));
			queryParamList.add(new TokenParam("out_trade_no", outTradeNo));
			String sign = TokenService.buildToken(queryParamList, secret);
			
			System.out.println(sign);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
