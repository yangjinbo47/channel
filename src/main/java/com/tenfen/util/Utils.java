package com.tenfen.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * 工具类
 * @author BOBO
 *
 */
@SuppressWarnings("unchecked")
public class Utils {

	private static final String inj_str = "'@and@exec@insert@select@delete@update@count@*@%@chr@mid@master@truncate@char@declare@;@lock table@grant@drop@ascii@-@+";  
	
	private Utils() {

	}

	/**
	 * 为地址加密方便在连接中传输
	 * @return
	 */
	public static String encodeArea(String content) {
		StringBuffer sb = new StringBuffer();
		for (int index = 0; index < content.length(); index++) {
			//将test参数转为数字码传递到页面
			sb.append((int) content.charAt(index) + ",");
		}
		return sb.toString();
	}

	public static String decodeArea(String content) {
		if (!Utils.isEmpty(content)) {
			//将URL里面所传过来的数字码转码
			char b = 0;
			String[] chars = content.split(",");
			StringBuffer c = new StringBuffer();
			for (int i = 0; i < chars.length; i++) {
				b = (char) Integer.parseInt(chars[i]);
				c.append(b);
			}
			return c.toString();
		}
		return null;
	}

	/**
	 * 获得REQUEST的参数
	 * 
	 * @param request
	 * @param parameterName
	 * @return
	 */
	public static String getRequestParameter(javax.servlet.http.HttpServletRequest request, String parameterName) {
		String value = "";
		try {
			value = (String) request.getParameter(parameterName);

		} catch (Exception e) {
			value = "";
		}
		if (Utils.isEmpty(value)) {
			value = "";
		}
		return value;
	}

	public static String getRequestAttribute(javax.servlet.http.HttpServletRequest request, String parameterName) {
		String value = "";
		try {
			value = (String) request.getAttribute(parameterName);

		} catch (Exception e) {
			value = "";
		}
		if (Utils.isEmpty(value)) {
			value = "";
		}
		return value;
	}

	// 获得根目录
	public static String getWebRoot(javax.servlet.http.HttpServletRequest request) {

		StringBuffer webRoot = new StringBuffer(200);
		webRoot.append(request.getScheme());
		webRoot.append("://");
		webRoot.append(request.getServerName());
		webRoot.append(":");
		webRoot.append(request.getServerPort());
		webRoot.append(request.getContextPath());
		return webRoot.toString();
	}

	/**
	 * 获得SESSION参数
	 * 
	 * @param request
	 * @param parameterName
	 * @return
	 */
	public static String getSessionParameter(javax.servlet.http.HttpServletRequest request, String parameterName) {
		String value = "";
		try {
			value = (request.getSession().getAttribute(parameterName)).toString();

		} catch (Exception e) {
			value = "";
		}
		if (Utils.isEmpty(value)) {
			value = "";
		}
		return value;
	}

	/**
	 * 判断值和对象是否为空
	 * 
	 * @param obj
	 * @return
	 */
	public static boolean isEmpty(Object obj) {
		if (obj == null)
			return true;

		if (obj instanceof String) {
			boolean back = obj.equals("");
			if (!back) {//判断是否等于NULL
				back = obj.equals("null");
			}
			return back;
		} else if (obj instanceof Collection) {

			Collection coll = (Collection) obj;
			return coll.size() == 0;
		} else if (obj instanceof Map) {

			Map map = (Map) obj;
			return map.size() == 0;
		} else if (obj.getClass().isArray()) {

			return Array.getLength(obj) == 0;
		} else {

			return false;
		}
	}

	/**
	 * 截取字符串方法
	 * 
	 * @param inStr
	 * @param length
	 * @return
	 */
	public static String subStr(String inStr, int length) {
		try {
			if (!Utils.isEmpty(inStr)) {
				if (inStr.length() > length) {
					inStr = inStr.substring(0, length);
				}
			}
		} catch (Exception e) {

		}
		return inStr;
	}

	/**
	 * 判断是否是数字
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNumeric(String str) {
		boolean ok = true;
		try {
			if (str == null || "".equals(str)) {
				ok = false;
			} else {
				for (int i = str.length(); --i >= 0;) {
					if (!Character.isDigit(str.charAt(i))) {
						ok = false;
					}
				}
			}
		} catch (Exception ex) {
			ok = false;
		}
		return ok;
	}

	/**
	 * 是否数字
	 * @author BOBO
	 * @param str
	 * @return
	 */
	public static boolean isNumer(String str) {
		if (str != null && !"".equals(str)) {
			Pattern pattern = Pattern.compile("^\\+?\\-?\\d+$");
//			Pattern pattern = Pattern.compile("^\\d+$");
			Matcher isNum = pattern.matcher(str);
			if (isNum.matches()) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public static List<List> getRequestList(javax.servlet.http.HttpServletRequest request, String name) {
		List<List> tmp = null;
		try {
			tmp = (List<List>) request.getAttribute(name);
		} catch (Exception ex) {
			LogUtil.log(" 获得request数据失败  ....", Level.INFO, ex);
		}
		if (Utils.isEmpty(tmp)) {
			tmp = new ArrayList<List>();
		}
		return tmp;
	}

	/**
	 * 获取cookie里设置的字数值
	 * @author BOBO
	 * @param str
	 * @return
	 */
	public static String getCookieWords(javax.servlet.http.HttpServletRequest request) {
		String words = "1500";
		try {
			Cookie[] cookies = request.getCookies();
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if (cookie.getName().equals("words") && !cookie.getValue().equals("")) {
						words = cookie.getValue();
						break;
					}
				}
			} else {
				words = "1500";
			}
		} catch (Exception e) {
			LogUtil.log(" 获取内容字数失败  ....", Level.INFO, e);
		}
		return words;
	}
	
	/**
	 * @功能手机号码验证,11位，不知道详细的手机号码段，只是验证开头必须是1和位数
	 * @param cellPhoneNr
	 * @return
	 */
	public static boolean checkCellPhone(String cellPhoneNr) {
		if (cellPhoneNr != null) {
			String reg = "^(13[0-9]|15[0-9]|18[0-9]|14[5|7]|17[0|3|6|7|8]|19[9])[\\d]{8}$";
			return startCheck(reg, cellPhoneNr);
		}
		return false;
	}
	
	/**
	 * @功能 执行正则表达式
	 * @param reg
	 * @param string
	 * @return
	 */
	public static boolean startCheck(String reg, String string) {
		boolean tem = false;
		if (string != null) {			
			Pattern pattern = Pattern.compile(reg);
			Matcher matcher = pattern.matcher(string);
			tem = matcher.matches();
		}
		return tem;
	}
	
	/**
	 * 
	*@author BOBO
	*@功能：判断是否是正整数或者正小数
	*@param str
	 */
	public static boolean checkNumber(String str) {
		String reg = "^\\d+$|^\\d+\\.\\d{1,2}$";
		return startCheck(reg, str);
	}
	
	/**
	 * sql注入过滤处理
	 * @param value
	 * @return
	 */
	public static String fiterSQL(String value){
		String inj_stra[] = inj_str.split("@");  
		for (int i = 0; i < inj_stra.length; i++) {
			if(value.indexOf(inj_stra[i])>=0){
				value = value.replace(inj_stra[i], "");
				break;
			}
		}
		return value;
	}

	
	/**
	 * 脚本注入过滤处理
	 * @param value
	 * @return
	 */
	public static String fiterScript(String value){
		value = value.replace("script","");
		value = value.replace("<", "");
		value = value.replace(">", "");
		value = value.replace("%", "");
		value = value.replace("/", "");
		return value;
	}
	/**
	 * 由字符索引值转换成字符
	 * @author slc
	 * @param str
	 * @return String
	 */
	public static String ConverCharAtToStr(String str) {
		if (str != null) {
			char b = 0;
			String[] chars = str.split(",");
			StringBuffer c = new StringBuffer();
			for (int i = 0; i < chars.length; i++) {
				b = (char) Integer.parseInt(chars[i]);
				c.append(b);
			}
			str = c.toString();
		}
		return str;
	}
	/**
	 * 由字符转换成字符索引值
	 * @author slc
	 * @param str
	 * @return String
	 */
	public static String ConverStrToCharAt(String str) {
		if (!Utils.isEmpty(str)) {
			StringBuilder sb=new StringBuilder();
			for (int index = 0; index < str.length(); index++) {
				// str转为数字码传递到页面
				sb.append((int) str.charAt(index) + ",");
			}
			str=sb.toString();
		}
		return str;
	}

	/**
	*@功能：验证是否邮箱
	*@author BOBO
	*@date Mar 19, 2012
	*@param email
	*@return
	 */
	public static boolean checkEmail(String email) {
		String reg = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
		return startCheck(reg, email);
	}
	
	/**
	*@功能：格式化手机号码
	*@author BOBO
	*@date Mar 31, 2014
	*@param phone
	*@return
	 */
	public static String mobilePhoneFormat(String phone) {
		if (phone != null) {
			phone = phone.trim();
			if (phone.length() > 11) {
				return phone.substring(phone.length() - 11);
			}
		}
		return phone;
	}
	
	/**
	*@功能：从cookie取值
	*@author BOBO
	*@date Jun 25, 2012
	*@param request
	*@param name
	*@return
	 */
	public static String getValueFromCookie(HttpServletRequest request, String name) {
		String returnString = null;
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(name) && !cookie.getValue().equals("")) {
					returnString = cookie.getValue();
				}
			}
		}
		return returnString;
	}
	
	/**
	 * 是否电信号段
	 * @param phoneNum
	 * @return
	 */
	public static boolean isCTPhone(String phoneNum) {
		if (phoneNum != null) {
			if (phoneNum.length() > 11) {
				phoneNum = phoneNum.substring(phoneNum.length() - 11, phoneNum.length());
			}
			if (checkCellPhone(phoneNum)) {
				List<String> chinaTel = Arrays.asList(new String[] { "133", "153", "189", "180", "181", "170", "173", "177", "199"});
				return chinaTel.contains(phoneNum.substring(0, 3));
			}
		}
		return false;
	}
	
}
