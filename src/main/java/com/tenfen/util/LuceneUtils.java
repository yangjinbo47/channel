package com.tenfen.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class LuceneUtils {
	// 当前目录位置
	public static final String USERDIR = System.getProperty("user.dir");
	// 存放索引的目录
	//	private static final String INDEXPATH = USERDIR + File.separator + "index";
//	private static final String INDEXPATH = "d:\\index";
//	private static final String INDEXPATH = "/ytxt/similary/index";
	// 使用版本
	public static final Version version = Version.LUCENE_36;
	
	public static IndexSearcher indexSearcher = null;

	/** * 获取分词器 * */
	public static Analyzer getAnalyzer() {
		// 分词器
		Analyzer analyzer = new StandardAnalyzer(version);
		return analyzer;
	}

	/***************************************************************************
	 * * 创建一个索引器的操作类 *
	 * 
	 * @param openMode *
	 * @return *
	 * @throws Exception
	 */
	public static IndexWriter createIndexWriter(OpenMode openMode,String path) throws Exception { // 索引存放位置设置
		Directory dir = FSDirectory.open(new File(path));
		// 索引配置类设置
		IndexWriterConfig iwc = new IndexWriterConfig(version, getAnalyzer());
		iwc.setOpenMode(openMode);
		IndexWriter writer = new IndexWriter(dir, iwc);
		return writer;
	}

	/**
	 * * * 创建一个搜索的索引器 *
	 * 
	 * @throws IOException *
	 * @throws CorruptIndexException *
	 */
	public static IndexSearcher createIndexSearcher(String path) throws Exception {
		if (indexSearcher == null) {
			try {				
				IndexReader reader = IndexReader.open(FSDirectory.open(new File(path)));
				indexSearcher = new IndexSearcher(reader);
			} catch (Exception e) {
				indexSearcher = null;
			}
		}
		return indexSearcher;
	}

	/**
	 * * 创建一个查询器 *
	 * 
	 * @param queryFileds
	 *            在哪些字段上进行查询 *
	 * @param queryString
	 *            查询内容 *
	 * @return *
	 * @throws ParseException
	 */
	public static Query createQuery(String[] queryFileds, String queryString) throws ParseException {
		QueryParser parser = new MultiFieldQueryParser(version, queryFileds, getAnalyzer());
		Query query = parser.parse(queryString);
		return query;
	}

	/**
	*@功能：删除指定索引
	*@author BOBO
	*@date May 25, 2013
	*@param indexName
	*@param indexValue
	*@throws Exception
	 */
	public static void delete(String field, String value, String path) throws Exception {
		IndexWriter writer = null;
		try {
			writer = createIndexWriter(OpenMode.CREATE_OR_APPEND, path);
			writer.deleteDocuments(new Term(field,value));
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	/**
	*@功能：搜索的方法
	*@author BOBO
	*@date May 25, 2013
	*@param field - 查找字段
	*@param value - 查找值
	*@param returnField - 返回字段数组
	*@param path - 查找路径
	*@return
	*@throws IOException
	 */
	public static Map<String, String> search(String[] fields, String value, List<String> returnField, String path) throws IOException {
		Map<String, String> map = new HashMap<String, String>();
		try {
			IndexSearcher searcher = LuceneUtils.createIndexSearcher(path);
			if (searcher != null) {
				Query query = createQuery(fields, value);
				TopDocs topDocs = searcher.search(query, 100);
				ScoreDoc[] hits = topDocs.scoreDocs;
//				System.out.println("共有" + searcher.maxDoc() + "条索引，命中" + hits.length + "条");
				for (int i = 0; i < hits.length; i++) {
					int DocId = hits[i].doc;
					Document document = searcher.doc(DocId);
//					result = document.get(returnField);
					for (String string : returnField) {
						map.put(string, document.get(string));
					}
				}
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return map;
	}
	
	public static void main(String[] args) {
		try {
//			LuceneUtils.delete("sourceName", "zhongbang.txt");
//			LuceneUtils.search("id","100000179375207");
//			LuceneUtils.delete("id", "100000179375207");
//			String[] fields = {"id","sName"};
//			LuceneUtils.search(fields, "100000179375480", "id", "d://index");
			
			String[] fields = {"sourceName"};
			//需要返回字段
			List<String> returnList = new ArrayList<String>();
			returnList.add("compileName");
			returnList.add("createTime");
			
			Map<String, String> map = LuceneUtils.search(fields, "hbrecommend.html", returnList, "d://index");
			for (String string : map.keySet()) {
				System.out.println(map.get(string));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
