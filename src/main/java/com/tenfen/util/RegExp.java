package com.tenfen.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Lordz
 * @Date Jun 23, 2009
 */
public class RegExp {

	/**
	 * @功能：执行正则表达式，返回符合规则的第一条数据
	 */
	public static String getString(String text, String regEx) {
		return getString(text, regEx, false);
	}

	public static String getString(String text, String regEx, boolean caseInsensitive) {
		List<String> list = getList(text, regEx, caseInsensitive);
		String result = "";
		if (!list.isEmpty()) {
			result = list.get(0);
		}
		return result;
	}

	/**
	 * @功能：执行正则表达式，返回符合规则的数据
	 */
	public static List<String> getList(String text, String regEx) {
		return getList(text, regEx, false);
	}

	public static List<String> getList(String text, String regEx, boolean caseInsensitive) {

		List<String> list = new ArrayList<String>();

		if (isEmptyOrNull(text) || isEmptyOrNull(regEx))
			return list;

		Pattern pattern = getPattern(regEx, caseInsensitive);
		Matcher matcher = pattern.matcher(text);

		while (matcher.find()) {
			list.add(matcher.group());
		}

		return list;
	}

	/**
	 * @功能：执行正则表达式，返回符合规则的数据 找不到则返回空字符串
	 */
	public static String getStringBetween(String text, String regEx1, String regEx2) {
		return getStringBetween(text, regEx1, regEx2, false);
	}

	public static String getStringBetween(String text, String regEx1, String regEx2, boolean caseInsensitive) {

		if (isEmptyOrNull(text) || isEmptyOrNull(regEx1) || isEmptyOrNull(regEx2))
			return "";

		Pattern pattern = getPattern(regEx1, caseInsensitive);
		Matcher matcher = pattern.matcher(text);
		String parseBegin = null, parseEnd = null;

		Pattern pattern2 = getPattern(regEx2, caseInsensitive);
		Matcher matcher2 = pattern2.matcher(text);

		if (matcher.find() && matcher2.find()) {
			parseBegin = matcher.group();
			parseEnd = matcher2.group();
			return text.substring((text.indexOf(parseBegin)+parseBegin.length()), text.indexOf(parseEnd));
		} else {
			return "";
		}
	}

	/**
	 * @功能：执行正则表达式，判断是否有符合规则的数据
	 */
	public static Boolean find(String text, String regEx) {
		return find(text, regEx, false);
	}

	public static Boolean find(String text, String regEx, boolean caseInsensitive) {
		if (isEmptyOrNull(text) || isEmptyOrNull(regEx))
			return false;
		Pattern pattern = getPattern(regEx, caseInsensitive);
		Matcher matcher = pattern.matcher(text);
		return matcher.find();
	}

	/**
	 * @功能：执行正则表达式，替换符合规则的数据，大小写一致
	 */
	public static String replace(String text, String word, String regEx) {
		return replace(text, word, regEx, false);
	}

	public static String replace(String text, String word, String regEx, boolean caseInsensitive) {
		Pattern pattern = getPattern(regEx, caseInsensitive);
		Matcher matcher = pattern.matcher(text);

		if (matcher.find())
			return matcher.replaceAll(word);
		else {
			return text;
		}
	}

	private static Pattern getPattern(String regEx, boolean caseInsensitive) {
		Pattern pattern;
		if (caseInsensitive) {
			pattern = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
		} else {
			pattern = Pattern.compile(regEx);
		}
		return pattern;
	}

	public static Boolean isEmptyOrNull(Object o) {
		if (o == null)
			return true;
		if (o instanceof String) {
			return String.valueOf(o).trim().equals("");
		}
		return false;
	}

	public static boolean isNumeric(String str) {
		if (str != null && !str.equals("")) {
			Pattern pattern = Pattern.compile("[0-9]*");
			Matcher isNum = pattern.matcher(str);
			if (isNum.matches()) {
				return true;
			}
		}
		return false;
	}

	/**  
     * 删除input字符串中的html格式 
     */
    public String splitAndFilterString(String input){
        if (input == null || input.trim().equals("")) {   
            return "";   
        }
        // 去掉所有html元素,   
        String str = input.replaceAll("\\&[a-zA-Z]{1,10};", "").replaceAll("<[^>]*>", ""); 
        str = str.replaceAll("[(/>)<]","");  
        return str;
    }

    
	public static void main(String[] args) {
		String string = "<A href=\"/news2/090702/201/575345-1.shtml\" target=\"_blank\"><FONT size=\"+0\">汪林朋揭秘居然之家扩张“秘诀”</FONT></A>";
		String aLinkregEx = "<a[\\w\\W]*?href=[\"|']?[\\w\\W]*?[\"|']?[\\w\\W]*?>[\\w\\W]*?</a>";
		System.out.println(find(string, aLinkregEx, true));
	}

}
