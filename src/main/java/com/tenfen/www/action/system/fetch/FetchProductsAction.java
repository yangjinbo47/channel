package com.tenfen.www.action.system.fetch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;

import com.alibaba.fastjson.JSONObject;
import com.tenfen.util.LogUtil;
import com.tenfen.util.RegExp;
import com.tenfen.util.StringUtil;
import com.tenfen.util.Utils;
import com.tenfen.util.servlet.ServletRequestUtils;
import com.tenfen.www.action.SimpleActionSupport;

public class FetchProductsAction extends SimpleActionSupport{
	
	private static final long serialVersionUID = 5790953702078366451L;

	private static final String ctoken = "7r3nno1_5jhv";
	
	private static FileWriter fileWriter = null;
	
	private Map<String,String> suppliersMap = new HashMap<String, String>();
	
//	public static void main(String[] args) {
//		try {
//			File toFile = new File("D://result.txt");//写文件地址
//			fileWriter = new FileWriter(toFile, true);
//			
//			FetchProducts fetch = new FetchProducts();
//			String url = "https://www.alibaba.com/products/SBS.html?IndexArea=product_en&Country=CN&prov=Zhejiang&page=1&assessment_company=ASS&ta=y&viewtype=L";
//			fetch.fetch(url);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
	public String alidata() {
		String fileName = ServletRequestUtils.getStringParameter(request, "fileName", "");
		try {
			if (Utils.isEmpty(fileName)) {
				String rspStr = "{msg:'请输入文件名!'}";
            	StringUtil.printJson(response, rspStr);
			} else {
//				File toFile = new File("D://"+fileName+"_result.txt");//写文件地址
				File toFile = new File("/home/channel/alidata/"+fileName+"_result.txt");//写文件地址linux
				fileWriter = new FileWriter(toFile, true);
				
				String urlTemplate = "https://www.alibaba.com/products/{0}.html?IndexArea=product_en&Country=CN&prov=Zhejiang&page=1&n=62&assessment_company=ASS&ta=y&viewtype=L";
//				File readFile = new File("D://"+fileName+".txt");//读文件地址
				File readFile = new File("/home/channel/alidata/"+fileName+".txt");//读文件地址linux
				List<String> list = FileUtils.readLines(readFile, "UTF-8");
				for (String string : list) {
					String s = string.replace(" ", "_");
					String url = MessageFormat.format(urlTemplate, s);
					
					fetch(url, s);
				}
				String rspStr = "{msg:'success'}";
            	StringUtil.printJson(response, rspStr);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
//	public static void main(String[] args) {
//		try {
//			File toFile = new File("D://product1_result.txt");//写文件地址
//			fileWriter = new FileWriter(toFile, true);
//			
//			String urlTemplate = "https://www.alibaba.com/products/{0}.html?IndexArea=product_en&Country=CN&prov=Zhejiang&page=1&n=62&assessment_company=ASS&ta=y&viewtype=L";
//			File readFile = new File("D://products1.txt");
//			List<String> list = FileUtils.readLines(readFile, "UTF-8");
//			for (String string : list) {
//				String s = string.replace(" ", "_");
//				String url = MessageFormat.format(urlTemplate, s);
//				
//				FetchProducts fetch = new FetchProducts();
//				fetch.fetch(url, s);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
	public void fetch(String url, String product) {
		try {
			System.out.println("visitUrl:"+url);
			
			//解析当前页列表
			boolean hasNext = parseList(url, product);
			if (hasNext) {
				String nextUrl = parseNextUrl(url);
				fetch(nextUrl, product);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String parseNextUrl(String url) {
		String nextUrl = null;
		try {
			String pageNumStr = RegExp.getString(url, "(?<=page=)(\\d{1,})");
			int pageNum = Integer.parseInt(pageNumStr);
			pageNum += 1;
			nextUrl = url.replaceAll("(?<=page=)(\\d{1,})", String.valueOf(pageNum));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return nextUrl;
	}
	
	public boolean parseList(String url, String product) {
		boolean hasNext = false;
		try {
			String res = AliHttpClientUtils.simpleGetInvoke(url, null);
			Parser parser = new Parser(res);
			NodeFilter filter = new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class","stitle util-ellipsis"));
			NodeList nodes = parser.extractAllNodesThatMatch(filter);
			if (nodes.size() > 0) {
				hasNext = true;
			}
			for (int i=0; i<nodes.size();i++) {
				Node node = (Node)nodes.elementAt(i);
				String source = node.toHtml();
				Parser supplierParser = new Parser(source);
				NodeFilter supplierFilter = new AndFilter(new TagNameFilter("a"),new HasAttributeFilter("data-domdot"));
				NodeList supplierNameNodes = supplierParser.extractAllNodesThatMatch(supplierFilter);
				Node supplierNameNode = (Node)supplierNameNodes.elementAt(0);
				String supplierNodeSource = supplierNameNode.toHtml();
				String supplierName = RegExp.getString(supplierNodeSource, "(?<=>)([\\s\\S]*)(?=<)");
				if (supplierName != null) {
					supplierName = supplierName.trim();
				}
				String suppUrl = suppliersMap.get(supplierName);
				if (suppUrl == null) {//map中不存在则解析
					String companyProfile = RegExp.getString(supplierNodeSource, "(?<=href=\")(.*)(?=\"([\\s\\S]*)target=\"_blank\")");
					int jinidx = companyProfile.indexOf("#");
					String companyProfileUrl = companyProfile.substring(0, jinidx);
					String contactUrl = companyProfileUrl.replace("company_profile", "contactinfo");
					if (!contactUrl.startsWith("http")) {
						contactUrl = "https:"+contactUrl;
					}
					new Thread(new Task(product, contactUrl, fileWriter)).start();
					suppliersMap.put(supplierName, contactUrl);
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return hasNext;
	}
	
//	public boolean parseList(String url) {
//		boolean hasNext = false;
//		try {
//			//获取联系信息url
//			Parser parser = new Parser((HttpURLConnection)(new URL(url)).openConnection());
//			NodeFilter filter = new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class","stitle util-ellipsis"));
//			NodeList nodes = parser.extractAllNodesThatMatch(filter);
//			for (int i=0; i<nodes.size();i++) {
//				Node node = (Node)nodes.elementAt(i);
//				String source = node.toHtml();
//				String companyProfile = RegExp.getString(source, "(?<=href=\")(.*)(?=\"([\\s\\S]*)target=\"_blank\")");
//				int jinidx = companyProfile.indexOf("#");
//				String companyProfileUrl = companyProfile.substring(0, jinidx);
//				String contactUrl = companyProfileUrl.replace("company_profile", "contactinfo");
//				if (!contactUrl.startsWith("http")) {
//					contactUrl = "https:"+contactUrl;
//				}
//				new Thread(new Task(contactUrl, fileWriter)).start();
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return hasNext;
//	}
	
	public static void main(String[] args) {
		FetchProductsAction fpa = new FetchProductsAction();
		fpa.test("test", "https://ningshing.en.alibaba.com/contactinfo.html");
	}
	
	public void test(String product, String contactUrl) {
		try {
			String res = AliHttpClientUtils.simpleGetInvoke(contactUrl, null);
			Parser rootparser = new Parser(res);
			NodeFilter typeFilter = new AndFilter(new TagNameFilter("html"),new HasAttributeFilter("lang","en"));
			NodeList rootNodes = rootparser.extractAllNodesThatMatch(typeFilter);
			if (rootNodes.size() == 1) {//<html lang="en">版本
				List<String> list = RegExp.getList(res, "<td\\sclass=\"item-value\">([^<>]+)</td>");
				String company = StringUtils.substringBetween(list.get(0), ">", "<");
				String address = StringUtils.substringBetween(list.get(1), ">", "<");
				
				String country = null;
				String province = null;
				String city = null;
				Parser parser = new Parser(res);
				NodeFilter filter = new AndFilter(new TagNameFilter("table"),new HasAttributeFilter("class","info-table"));
				NodeList nodes = parser.extractAllNodesThatMatch(filter);
				Node node = (Node)nodes.elementAt(0);
				NodeList nodeList = node.getChildren();
				for (int i=0; i<nodeList.size();i++) {
					Node item = (Node)nodeList.elementAt(i);
					String infoItemSource = item.toHtml();
					String name = StringUtils.substringBetween(infoItemSource, "<th>", "</th>");
					if (name.indexOf("Country") != -1) {
						String value = StringUtils.substringBetween(infoItemSource, "<td>", "</td>");
						country = value;
					}
					if (name.indexOf("Province") != -1) {
						String value = StringUtils.substringBetween(infoItemSource, "<td>", "</td>");
						province = value;
					}
					if (name.indexOf("City") != -1) {
						String value = StringUtils.substringBetween(infoItemSource, "<td>", "</td>");
						city = value;
					}
				}
				
				String contactName = RegExp.getString(res, "<div\\sclass=\"contact-name\">([^<>]+)</div>");
				contactName = StringUtils.substringBetween(contactName, ">", "<");
				String contactDepartment = RegExp.getString(res, "<div\\sclass=\"contact-department\">([^<>]+)</div>");
				contactDepartment = StringUtils.substringBetween(contactDepartment, ">", "<");
				String contactJob = RegExp.getString(res, "<div\\sclass=\"contact-job\">([^<>]+)</div>");
				contactJob = StringUtils.substringBetween(contactJob, ">", "<");
				String encryptAccountId = RegExp.getString(res, "(?<=encryptAccountId%22%3A%22)(.*)(?=%22%2C%22aliMemberId)");
				
				String contactInfoUrl = contactUrl.replace("contactinfo.html", "event/app/contactPerson/showContactInfo.htm");
				Map<String, String> map = new HashMap<String, String>();
				map.put("encryptAccountId", encryptAccountId);
				map.put("ctoken", ctoken);
				String contactInfoRes = AliHttpClientUtils.simpleGetInvoke(contactInfoUrl, map);
				JSONObject json = JSONObject.parseObject(contactInfoRes);
				String mobile = null;
				String tel = null;
				String fax = null;
				if (json != null) {
					JSONObject contactInfoJson = json.getJSONObject("contactInfo");
					mobile = contactInfoJson.getString("accountMobileNo");
					tel = contactInfoJson.getString("accountPhone");
					fax = contactInfoJson.getString("accountFax");
				}
				
				fileWriter.write(product+"|||"+company+"|||"+address+"|||"+country+"|||"+province+"|||"+city+"|||"+contactName+"|||"+contactDepartment+"|||"+contactJob+"|||"+mobile+"|||"+tel+"|||"+fax+System.getProperty("line.separator"));
			} else {//<html xmlns="http://www.w3.org/1999/xhtml">版本
				Parser parser = new Parser(res);
				NodeFilter filter = new AndFilter(new TagNameFilter("table"),new HasAttributeFilter("class","company-info-data table"));
				NodeList nodes = parser.extractAllNodesThatMatch(filter);
				Node node = (Node)nodes.elementAt(0);
				NodeList nodeList = node.getChildren();
				String company = null;
				String address = null;
				for (int i=0; i<nodeList.size();i++) {
					Node item = (Node)nodeList.elementAt(i);
					String companyInfoSource = item.toHtml();
					String name = StringUtils.substringBetween(companyInfoSource, "<th>", "</th>");
					if (!Utils.isEmpty(name)) {
						if (name.indexOf("Company Name") != -1) {
							String value = StringUtils.substringBetween(companyInfoSource, "<td>", "</td>");
							company = value;
						}
						if (name.indexOf("Address") != -1) {
							String value = StringUtils.substringBetween(companyInfoSource, "<td>", "</td>");
							address = value;
						}
					}
				}
				
				String country = null;
				String province = null;
				String city = null; 
				Parser regionParser = new Parser(res);
				NodeFilter regionFilter = new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class","public-info"));
				NodeList publicInfoNodes = regionParser.extractAllNodesThatMatch(regionFilter);
				Node publicInfoNode = publicInfoNodes.elementAt(0);
				Node dlHorizontalNode = publicInfoNode.getChildren().elementAt(1);
				NodeList regionNodeList = dlHorizontalNode.getChildren();
				for (int i=0; i<regionNodeList.size();i++) {
					Node item = (Node)regionNodeList.elementAt(i);
					String itemResource = item.toHtml();
					String name = StringUtils.substringBetween(itemResource, "<dt>", "</dt>");
					if (!Utils.isEmpty(name)) {
						if (name.indexOf("Country") != -1) {
							Node itemVal = (Node)regionNodeList.elementAt(i+2);
							String value = StringUtils.substringBetween(itemVal.toHtml(), "<dd>", "</dd>");
							country = value;
						}
						if (name.indexOf("Province") != -1) {
							Node itemVal = (Node)regionNodeList.elementAt(i+2);
							String value = StringUtils.substringBetween(itemVal.toHtml(), "<dd>", "</dd>");
							province = value;
						}
						if (name.indexOf("City") != -1) {
							Node itemVal = (Node)regionNodeList.elementAt(i+2);
							String value = StringUtils.substringBetween(itemVal.toHtml(), "<dd>", "</dd>");
							city = value;
						}
					}
				}
				
				Parser contactNameParser = new Parser(res);
				NodeFilter contactFilter = new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class","contact-info"));
				NodeList contactInfoNodes = contactNameParser.extractAllNodesThatMatch(contactFilter);
				Node contactInfoNode = contactInfoNodes.elementAt(0);
				String contactInfoResource = contactInfoNode.toHtml();
				String contactName = RegExp.getString(contactInfoResource, "(?<=<h1 class=\"name\">)([^<>]+)(?=</h1>)");
				if (!Utils.isEmpty(contactName)) {
					contactName = contactName.trim();
				}
				String contactJob = RegExp.getString(contactInfoResource, "(?<=<dd>)([^<>]+)(?=</dd>)");
				if (!Utils.isEmpty(contactJob)) {
					contactJob = contactJob.trim();
				}
				
				String encryptAccountId = RegExp.getString(res, "(?<=data-account-id=\")([^<>]+)(?=\"\\s{1,}data-domdot)");
				String contactInfoUrl = contactUrl.replace("contactinfo.html", "event/app/contactPerson/showContactInfo.htm");
				Map<String, String> map = new HashMap<String, String>();
				map.put("encryptAccountId", encryptAccountId);
				String contactInfoRes = AliHttpClientUtils.simpleGetInvoke(contactInfoUrl, map);
				JSONObject json = JSONObject.parseObject(contactInfoRes);
				String mobile = null;
				String tel = null;
				String fax = null;
				if (json != null) {
					JSONObject contactInfoJson = json.getJSONObject("contactInfo");
					mobile = contactInfoJson.getString("accountMobileNo");
					tel = contactInfoJson.getString("accountPhone");
					fax = contactInfoJson.getString("accountFax");
				}
				
				fileWriter.write(product+"|||"+company+"|||"+address+"|||"+country+"|||"+province+"|||"+city+"|||"+contactName+"|||"+null+"|||"+contactJob+"|||"+mobile+"|||"+tel+"|||"+fax+System.getProperty("line.separator"));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private class Task implements Runnable {

		private String product;
		private String contactUrl;
		private FileWriter fileWriter;
		
		public Task(String product, String contactUrl,FileWriter fileWriter){
			this.product = product;
			this.contactUrl = contactUrl;
			this.fileWriter = fileWriter;
		}
		
		@Override
		public void run() {
			try {
				String res = AliHttpClientUtils.simpleGetInvoke(contactUrl, null);
				Parser rootparser = new Parser(res);
				NodeFilter typeFilter = new AndFilter(new TagNameFilter("html"),new HasAttributeFilter("lang","en"));
				NodeList rootNodes = rootparser.extractAllNodesThatMatch(typeFilter);
				if (rootNodes.size() == 1) {//<html lang="en">版本
					List<String> list = RegExp.getList(res, "<td\\sclass=\"item-value\">([^<>]+)</td>");
					String company = StringUtils.substringBetween(list.get(0), ">", "<");
					String address = StringUtils.substringBetween(list.get(1), ">", "<");
					
					String country = null;
					String province = null;
					String city = null;
					Parser parser = new Parser(res);
					NodeFilter filter = new AndFilter(new TagNameFilter("table"),new HasAttributeFilter("class","info-table"));
					NodeList nodes = parser.extractAllNodesThatMatch(filter);
					Node node = (Node)nodes.elementAt(0);
					NodeList nodeList = node.getChildren();
					for (int i=0; i<nodeList.size();i++) {
						Node item = (Node)nodeList.elementAt(i);
						String infoItemSource = item.toHtml();
						String name = StringUtils.substringBetween(infoItemSource, "<th>", "</th>");
						if (name.indexOf("Country") != -1) {
							String value = StringUtils.substringBetween(infoItemSource, "<td>", "</td>");
							country = value;
						}
						if (name.indexOf("Province") != -1) {
							String value = StringUtils.substringBetween(infoItemSource, "<td>", "</td>");
							province = value;
						}
						if (name.indexOf("City") != -1) {
							String value = StringUtils.substringBetween(infoItemSource, "<td>", "</td>");
							city = value;
						}
					}
					
					String contactName = RegExp.getString(res, "<div\\sclass=\"contact-name\">([^<>]+)</div>");
					contactName = StringUtils.substringBetween(contactName, ">", "<");
					String contactDepartment = RegExp.getString(res, "<div\\sclass=\"contact-department\">([^<>]+)</div>");
					contactDepartment = StringUtils.substringBetween(contactDepartment, ">", "<");
					String contactJob = RegExp.getString(res, "<div\\sclass=\"contact-job\">([^<>]+)</div>");
					contactJob = StringUtils.substringBetween(contactJob, ">", "<");
					String encryptAccountId = RegExp.getString(res, "(?<=encryptAccountId%22%3A%22)(.*)(?=%22%2C%22aliMemberId)");
					
					String contactInfoUrl = contactUrl.replace("contactinfo.html", "event/app/contactPerson/showContactInfo.htm");
					Map<String, String> map = new HashMap<String, String>();
					map.put("encryptAccountId", encryptAccountId);
					map.put("ctoken", ctoken);
					String contactInfoRes = AliHttpClientUtils.simpleGetInvoke(contactInfoUrl, map);
					JSONObject json = JSONObject.parseObject(contactInfoRes);
					String mobile = null;
					String tel = null;
					String fax = null;
					if (json != null) {
						JSONObject contactInfoJson = json.getJSONObject("contactInfo");
						mobile = contactInfoJson.getString("accountMobileNo");
						tel = contactInfoJson.getString("accountPhone");
						fax = contactInfoJson.getString("accountFax");
					}
					
					fileWriter.write(product+"|||"+company+"|||"+address+"|||"+country+"|||"+province+"|||"+city+"|||"+contactName+"|||"+contactDepartment+"|||"+contactJob+"|||"+mobile+"|||"+tel+"|||"+fax+System.getProperty("line.separator"));
				} else {//<html xmlns="http://www.w3.org/1999/xhtml">版本
					Parser parser = new Parser(res);
					NodeFilter filter = new AndFilter(new TagNameFilter("table"),new HasAttributeFilter("class","company-info-data table"));
					NodeList nodes = parser.extractAllNodesThatMatch(filter);
					Node node = (Node)nodes.elementAt(0);
					NodeList nodeList = node.getChildren();
					String company = null;
					String address = null;
					for (int i=0; i<nodeList.size();i++) {
						Node item = (Node)nodeList.elementAt(i);
						String companyInfoSource = item.toHtml();
						String name = StringUtils.substringBetween(companyInfoSource, "<th>", "</th>");
						if (!Utils.isEmpty(name)) {
							if (name.indexOf("Company Name") != -1) {
								String value = StringUtils.substringBetween(companyInfoSource, "<td>", "</td>");
								company = value;
							}
							if (name.indexOf("Address") != -1) {
								String value = StringUtils.substringBetween(companyInfoSource, "<td>", "</td>");
								address = value;
							}
						}
					}
					
					String country = null;
					String province = null;
					String city = null; 
					Parser regionParser = new Parser(res);
					NodeFilter regionFilter = new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class","public-info"));
					NodeList publicInfoNodes = regionParser.extractAllNodesThatMatch(regionFilter);
					Node publicInfoNode = publicInfoNodes.elementAt(0);
					Node dlHorizontalNode = publicInfoNode.getChildren().elementAt(1);
					NodeList regionNodeList = dlHorizontalNode.getChildren();
					for (int i=0; i<regionNodeList.size();i++) {
						Node item = (Node)regionNodeList.elementAt(i);
						String itemResource = item.toHtml();
						String name = StringUtils.substringBetween(itemResource, "<dt>", "</dt>");
						if (!Utils.isEmpty(name)) {
							if (name.indexOf("Country") != -1) {
								Node itemVal = (Node)regionNodeList.elementAt(i+2);
								String value = StringUtils.substringBetween(itemVal.toHtml(), "<dd>", "</dd>");
								country = value;
							}
							if (name.indexOf("Province") != -1) {
								Node itemVal = (Node)regionNodeList.elementAt(i+2);
								String value = StringUtils.substringBetween(itemVal.toHtml(), "<dd>", "</dd>");
								province = value;
							}
							if (name.indexOf("City") != -1) {
								Node itemVal = (Node)regionNodeList.elementAt(i+2);
								String value = StringUtils.substringBetween(itemVal.toHtml(), "<dd>", "</dd>");
								city = value;
							}
						}
					}
					
					Parser contactNameParser = new Parser(res);
					NodeFilter contactFilter = new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class","contact-info"));
					NodeList contactInfoNodes = contactNameParser.extractAllNodesThatMatch(contactFilter);
					Node contactInfoNode = contactInfoNodes.elementAt(0);
					String contactInfoResource = contactInfoNode.toHtml();
					String contactName = RegExp.getString(contactInfoResource, "(?<=<h1 class=\"name\">)([^<>]+)(?=</h1>)");
					if (!Utils.isEmpty(contactName)) {
						contactName = contactName.trim();
					}
					String contactJob = RegExp.getString(contactInfoResource, "(?<=<dd>)([^<>]+)(?=</dd>)");
					if (!Utils.isEmpty(contactJob)) {
						contactJob = contactJob.trim();
					}
					
					String encryptAccountId = RegExp.getString(res, "(?<=data-account-id=\")([^<>]+)(?=\"\\s{1,}data-domdot)");
					String contactInfoUrl = contactUrl.replace("contactinfo.html", "event/app/contactPerson/showContactInfo.htm");
					Map<String, String> map = new HashMap<String, String>();
					map.put("encryptAccountId", encryptAccountId);
					String contactInfoRes = AliHttpClientUtils.simpleGetInvoke(contactInfoUrl, map);
					JSONObject json = JSONObject.parseObject(contactInfoRes);
					String mobile = null;
					String tel = null;
					String fax = null;
					if (json != null) {
						JSONObject contactInfoJson = json.getJSONObject("contactInfo");
						mobile = contactInfoJson.getString("accountMobileNo");
						tel = contactInfoJson.getString("accountPhone");
						fax = contactInfoJson.getString("accountFax");
					}
					
					fileWriter.write(product+"|||"+company+"|||"+address+"|||"+country+"|||"+province+"|||"+city+"|||"+contactName+"|||"+null+"|||"+contactJob+"|||"+mobile+"|||"+tel+"|||"+fax+System.getProperty("line.separator"));
				}
				
			} catch (Exception e) {
				LogUtil.error(contactUrl, e);
				fileWriter = null;
			} finally {
				if (fileWriter != null) {
					try {
						fileWriter.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
//			try {
//				String res = AliHttpClientUtils.simpleGetInvoke(contactUrl, null);
//				Parser rootparser = new Parser(res);
//				NodeFilter typeFilter = new AndFilter(new TagNameFilter("html"),new HasAttributeFilter("lang","en"));
//				NodeList rootNodes = rootparser.extractAllNodesThatMatch(typeFilter);
//				if (rootNodes.size() == 1) {//<html lang="en">版本
//					List<String> list = RegExp.getList(res, "<td\\sclass=\"item-value\">([^<>]+)</td>");
//					String company = StringUtils.substringBetween(list.get(0), ">", "<");
//					String address = StringUtils.substringBetween(list.get(1), ">", "<");
//					
//					Parser parser = new Parser(res);
//					NodeFilter filter = new AndFilter(new TagNameFilter("table"),new HasAttributeFilter("class","info-table"));
//					NodeList nodes = parser.extractAllNodesThatMatch(filter);
//					Node node = (Node)nodes.elementAt(0);
//					NodeList nodeList = node.getChildren();
//					String country = null;
//					String province = null;
//					String city = null;
//					for (int i=0; i<nodeList.size();i++) {
//						Node item = (Node)nodeList.elementAt(i);
//						String infoItemSource = item.toHtml();
//						String name = StringUtils.substringBetween(infoItemSource, "<th>", "</th>");
//						if (name.indexOf("Country") != -1) {
//							String value = StringUtils.substringBetween(infoItemSource, "<td>", "</td>");
//							country = value;
//						}
//						if (name.indexOf("Province") != -1) {
//							String value = StringUtils.substringBetween(infoItemSource, "<td>", "</td>");
//							province = value;
//						}
//						if (name.indexOf("City") != -1) {
//							String value = StringUtils.substringBetween(infoItemSource, "<td>", "</td>");
//							city = value;
//						}
//					}
//					
//					String contactName = RegExp.getString(res, "<div\\sclass=\"contact-name\">([^<>]+)</div>");
//					contactName = StringUtils.substringBetween(contactName, ">", "<");
//					String contactDepartment = RegExp.getString(res, "<div\\sclass=\"contact-department\">([^<>]+)</div>");
//					contactDepartment = StringUtils.substringBetween(contactDepartment, ">", "<");
//					String contactJob = RegExp.getString(res, "<div\\sclass=\"contact-job\">([^<>]+)</div>");
//					contactJob = StringUtils.substringBetween(contactJob, ">", "<");
//					String encryptAccountId = RegExp.getString(res, "(?<=encryptAccountId%22%3A%22)(.*)(?=%22%2C%22aliMemberId)");
//					
//					String contactInfoUrl = contactUrl.replace("contactinfo.html", "event/app/contactPerson/showContactInfo.htm");
//					Map<String, String> map = new HashMap<String, String>();
//					map.put("encryptAccountId", encryptAccountId);
//					map.put("ctoken", ctoken);
//					String contactInfoRes = AliHttpClientUtils.simpleGetInvoke(contactInfoUrl, map);
//					JSONObject json = JSONObject.parseObject(contactInfoRes);
//					String mobile = null;
//					String tel = null;
//					String fax = null;
//					if (json != null) {
//						JSONObject contactInfoJson = json.getJSONObject("contactInfo");
//						mobile = contactInfoJson.getString("accountMobileNo");
//						tel = contactInfoJson.getString("accountPhone");
//						fax = contactInfoJson.getString("accountFax");
//					}
//					
//					fileWriter.write(product+"|||"+company+"|||"+address+"|||"+country+"|||"+province+"|||"+city+"|||"+contactName+"|||"+contactDepartment+"|||"+contactJob+"|||"+mobile+"|||"+tel+"|||"+fax+System.getProperty("line.separator"));
//				} else {//<html xmlns="http://www.w3.org/1999/xhtml">版本
//					Parser parser = new Parser(res);
//					NodeFilter filter = new AndFilter(new TagNameFilter("table"),new HasAttributeFilter("class","info-table"));
//					NodeList nodes = parser.extractAllNodesThatMatch(filter);
//					Node node = (Node)nodes.elementAt(0);
//					System.out.println(node.toHtml());
//				}
//				
//			} catch (Exception e) {
//				LogUtil.error(contactUrl, e);
//				fileWriter = null;
//			} finally {
//				if (fileWriter != null) {
//					try {
//						fileWriter.flush();
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//				}
//			}
			
		}
		
	}
	
	
}
