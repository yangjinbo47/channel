package com.tenfen.util.servlet;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import com.tenfen.util.LogUtil;

public class HttpRequestUtils {

	/**
	 * 
	 * @author Lordz
	 * @date Jan 10, 2011
	 * @param uri
	 * @return
	 */
	public static String doGET(String uri) {
		StringBuffer sb = new StringBuffer("");
		try {
			java.net.URL url = new java.net.URL(uri);
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), Charset.forName("UTF-8")));
			String line;
			while ((line = in.readLine()) != null) {
				sb.append(line);
			}
			in.close();
			LogUtil.log("GET " + uri + " SUCCESS!");
		} catch (Exception e) {
			LogUtil.log("GET " + uri + " ERROR!");
		}
		String[] errorFilter = { "HTTP Status 404", "HTTP Status 500" };
		for (String string : errorFilter) {
			if (sb.toString().indexOf(string) != -1) {
				return "";
			}
		}
		return sb.toString();
	}

}
