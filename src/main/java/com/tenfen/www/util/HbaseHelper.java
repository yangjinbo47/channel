//package com.tenfen.www.util;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.apache.hadoop.conf.Configuration;
//import org.apache.hadoop.hbase.HBaseConfiguration;
//import org.apache.hadoop.hbase.HColumnDescriptor;
//import org.apache.hadoop.hbase.HTableDescriptor;
//import org.apache.hadoop.hbase.client.Delete;
//import org.apache.hadoop.hbase.client.Get;
//import org.apache.hadoop.hbase.client.HBaseAdmin;
//import org.apache.hadoop.hbase.client.HTable;
//import org.apache.hadoop.hbase.client.Put;
//import org.apache.hadoop.hbase.client.Result;
//import org.apache.hadoop.hbase.client.ResultScanner;
//import org.apache.hadoop.hbase.client.Scan;
//import org.apache.hadoop.hbase.filter.CompareFilter;
//import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
//import org.apache.hadoop.hbase.filter.Filter;
//import org.apache.hadoop.hbase.filter.FilterList;
//import org.apache.hadoop.hbase.filter.FilterList.Operator;
//import org.apache.hadoop.hbase.filter.PrefixFilter;
//import org.apache.hadoop.hbase.filter.RegexStringComparator;
//import org.apache.hadoop.hbase.filter.RowFilter;
//import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
//import org.apache.hadoop.hbase.util.Bytes;
//
//import com.tenfen.util.LogUtil;
//
//public class HbaseHelper {
//
//	private String hbaseMaster;
//	private String hbaseZookeeperQuorum;
//	private String hbaseZookeeperPropertyClientPort;
//	
//	public String getHbaseMaster() {
//		return hbaseMaster;
//	}
//
//	public void setHbaseMaster(String hbaseMaster) {
//		this.hbaseMaster = hbaseMaster;
//	}
//
//	public String getHbaseZookeeperQuorum() {
//		return hbaseZookeeperQuorum;
//	}
//
//	public void setHbaseZookeeperQuorum(String hbaseZookeeperQuorum) {
//		this.hbaseZookeeperQuorum = hbaseZookeeperQuorum;
//	}
//
//	public String getHbaseZookeeperPropertyClientPort() {
//		return hbaseZookeeperPropertyClientPort;
//	}
//
//	public void setHbaseZookeeperPropertyClientPort(
//			String hbaseZookeeperPropertyClientPort) {
//		this.hbaseZookeeperPropertyClientPort = hbaseZookeeperPropertyClientPort;
//	}
//
//	public static Configuration configuration;
//	
//	public void init() {
//		configuration = HBaseConfiguration.create();
//		configuration.set("hbase.master", hbaseMaster);
//		configuration.set("hbase.zookeeper.quorum", hbaseZookeeperQuorum);
//		configuration.set("hbase.zookeeper.property.clientPort", hbaseZookeeperPropertyClientPort);
//	}
//	
//	/**
//	 * 创建一张表
//	 * 
//	 * @param tableName
//	 * @param families
//	 */
//	public void createTable(String tableName, String[] families) {
//		HBaseAdmin admin = null;
//		try {
//			admin = new HBaseAdmin(configuration);
//			if (admin.tableExists(tableName)) {
//				LogUtil.log("HBase表：" + tableName + " 已经存在！");
//			} else {
//				HTableDescriptor tableDesc = new HTableDescriptor(tableName);
//				for (String column : families) {
//					tableDesc.addFamily(new HColumnDescriptor(column));
//				}
//				admin.createTable(tableDesc);
//			}
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//		} finally {
//			try {
//				admin.close();
//			} catch (Exception e) {
//				LogUtil.error(e.getMessage(), e);
//			}
//		}
//	}
//
//	/**
//	 * 删除一张表
//	 * 
//	 * @param tableName
//	 */
//	public void dropTable(String tableName) {
//		HBaseAdmin admin = null;
//		try {
//			admin = new HBaseAdmin(configuration);
//			admin.disableTable(tableName);
//			admin.deleteTable(tableName);
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//		} finally {
//			try {
//				admin.close();
//			} catch (Exception e) {
//				LogUtil.error(e.getMessage(), e);
//			}
//		}
//
//	}
//
//	/**
//	 * 获得一组Put
//	 * 
//	 * @param line
//	 * @return
//	 */
////	public Put getPut(String rowKey, String familyKey,
////			String[] qualifierKey, String[] value) {
////		Put put = new Put(Bytes.toBytes(rowKey));
////
////		if (StringUtils.isNotBlank(familyKey)) {
////			for (int i = 0; i < qualifierKey.length; i++) {
////				put.add(familyKey.getBytes(), qualifierKey[i].getBytes(),
////						value[i].getBytes());
////			}
////		}
////		return put;
////	}
//
//	/**
//	 * 存储一列
//	 * 
//	 * @param tableName
//	 * @param rowKey
//	 * @param family
//	 * @param qualifier
//	 * @param value
//	 */
//	public void writeRecord(String tableName, String rowKey,
//			String family, String qualifier, String value) {
//		HTable htable = null;
//		try {
//			htable = new HTable(configuration, tableName);
//			Put put = new Put(Bytes.toBytes(rowKey));
//			put.add(Bytes.toBytes(family), Bytes.toBytes(qualifier),
//					Bytes.toBytes(value));
//			htable.put(put);
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//		} finally {
//			try {
//				htable.flushCommits();
//				htable.close();
//				htable = null;
//			} catch (Exception e) {
//				LogUtil.error(e.getMessage(), e);
//			}
//		}
//	}
//
//	/**
//	 * 删除一行
//	 * 
//	 * @param tableName
//	 * @param rowKey
//	 */
//	public void deleteRecord(String tableName, String rowKey) {
//		HTable htable = null;
//		try {
//			htable = new HTable(configuration, tableName);
//			Delete del = new Delete(Bytes.toBytes(rowKey));
//			htable.delete(del);
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//		} finally {
//			try {
//				htable.flushCommits();
//				htable.close();
//				htable = null;
//			} catch (Exception e) {
//				LogUtil.error(e.getMessage(), e);
//			}
//		}
//	}
//
//	/**
//	 * 查询出一条数据
//	 * @param tableName
//	 * @param rowKey
//	 */
//	public Result getRecord(String tableName, String rowKey) {
//		HTable htable = null;
//		Result rs = null;
//		try {
//			htable = new HTable(configuration, tableName);
//			Get get = new Get(Bytes.toBytes(rowKey));
//			rs = htable.get(get);
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//		} finally {
////			try {
////				htable.flushCommits();
////				htable.close();
////			} catch (Exception e) {
////				LogUtil.error(e.getMessage(), e);
////			}
//		}
//		return rs;
//	}
//
//	
//	
//	
//	//返回ResultScanner
//	public ResultScanner getALLRecords(String tableName) {
////		List<Result> list = new ArrayList<Result>();
//		ResultScanner rs = null;
//		HTable htable = null;
//		try {
//			htable = new HTable(configuration, tableName);
//			Scan s = new Scan();
//			rs = htable.getScanner(s);
////			for (Result r : ss) {
////				list.add(r);
////			}
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//		} finally {
////			try {
////				htable.flushCommits();
////				htable.close();
////			} catch (Exception e) {
////				LogUtil.error(e.getMessage(), e);
////			}
//		}
//		return rs;
//	}
//
//	/**
//	 * 根据family匹配
//	 * @param tableName
//	 * @param familyName
//	 * @param value
//	 * @return
//	 */
//	public ResultScanner getRecordsByFamily(String tableName, String familyName, String value) {
////		List<Result> list = new ArrayList<Result>();
//		ResultScanner rs = null;
//		HTable htable = null;
//		try {
//			htable = new HTable(configuration, tableName);
//			Filter filter = new SingleColumnValueFilter(
//					Bytes.toBytes(familyName), null, CompareOp.EQUAL,
//					Bytes.toBytes(value)); // 当列columnName的值为value时进行查询
//			Scan s = new Scan();
//			s.setFilter(filter);
//			rs = htable.getScanner(s);
//			
////			for (Result r : rs) {
////				list.add(r);
////			}
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//		} finally {
////			try {
////				htable.flushCommits();
////				htable.close();
////			} catch (Exception e) {
////				LogUtil.error(e.getMessage(), e);
////			}
//		}
//		return rs;
//	}
//	
//	/**
//	 * 根据family和qulifier匹配
//	 * @param tableName
//	 * @param familyName
//	 * @param qualifierName
//	 * @param value
//	 * @return
//	 */
//	public ResultScanner getRecordsByQualifier(String tableName, String familyName, String[] qualifierName, String[] value) {
////		List<Result> list = new ArrayList<Result>();
//		ResultScanner rs = null;
//		HTable htable = null;
//		try {
//			htable = new HTable(configuration, tableName);
//			
//			FilterList filterList = new FilterList();
//			for (int i = 0; i < qualifierName.length; i++) {
//				String q = qualifierName[i];
//				String v = value[i];
//				filterList.addFilter(new SingleColumnValueFilter(Bytes.toBytes(familyName), Bytes.toBytes(q), CompareOp.EQUAL, Bytes.toBytes(v)));
//			}
//			Scan s = new Scan();
//			s.setFilter(filterList);
//			rs = htable.getScanner(s);
////			for (Result r : rs) {
////				list.add(r);
////			}
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//		} finally {
////			try {
////				htable.flushCommits();
////				htable.close();
////			} catch (Exception e) {
////				LogUtil.error(e.getMessage(), e);
////			}
//		}
//		return rs;
//	}
//	
//	public Scan getScanByQualifier(String familyName, String[] qualifierName, String[] value) {
//		Scan scan = null;
//		try {
//			List<Filter> filters = new ArrayList<Filter>();
//			for (int i = 0; i < qualifierName.length; i++) {
//				String q = qualifierName[i];
//				String v = value[i];
//				
//				filters.add(new SingleColumnValueFilter(Bytes.toBytes(familyName), Bytes.toBytes(q), CompareOp.EQUAL, Bytes.toBytes(v)));
//			}
//			FilterList filterList = new FilterList(Operator.MUST_PASS_ALL,filters);
//			scan = new Scan();
//			scan.setFilter(filterList);
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//		}
//		return scan;
//	}
//	
//	/**
//	 * 根据前缀匹配
//	 * @param tableName
//	 * @param prefix
//	 * @return
//	 */
//	public ResultScanner getRecordsByPrefixFilter(String tableName, String prefix) {
////		List<Result> list = new ArrayList<Result>();
//		ResultScanner rs = null;
//		HTable htable = null;
//		try {
//			htable = new HTable(configuration, tableName);
//			Scan s = new Scan();
//			s.setFilter(new PrefixFilter(Bytes.toBytes(prefix)));
//			rs = htable.getScanner(s);
////			for (Result r : rs) {
////				list.add(r);
////			}
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//		} finally {
////			try {
////				htable.flushCommits();
////				htable.close();
////			} catch (Exception e) {
////				LogUtil.error(e.getMessage(), e);
////			}
//		}
//		return rs;
//	}
//	
//	/**
//	 * 根据后缀匹配
//	 * @param tableName
//	 * @param suffix
//	 * @return
//	 */
//	public ResultScanner getRecordsBySuffixFilter(String tableName, String suffix) {
////		List<Result> list = new ArrayList<Result>();
//		ResultScanner rs = null;
//		HTable htable = null;
//		try {
//			htable = new HTable(configuration, tableName);
//			Scan s = new Scan();
//			s.setFilter(new RowFilter(CompareFilter.CompareOp.EQUAL, new RegexStringComparator(".*"+suffix)));
//			rs = htable.getScanner(s);
////			for (Result r : rs) {
////				list.add(r);
////			}
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//		} finally {
////			try {
////				htable.flushCommits();
////				htable.close();
////			} catch (Exception e) {
////				LogUtil.error(e.getMessage(), e);
////			}
//		}
//		return rs;
//	}
//	
//	public ResultScanner getRecordsByZone(String tableName, String rowStart, String rowEnd) {
////		List<Result> list = new ArrayList<Result>();
//		ResultScanner rs = null;
//		HTable htable = null;
//		try {
//			htable = new HTable(configuration, tableName);
//			Scan s = new Scan();
//			s.setStartRow(rowStart.getBytes());
//			s.setStopRow(rowEnd.getBytes());
//			rs = htable.getScanner(s);
////			for (Result r : rs) {
////				list.add(r);
////			}
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//		} finally {
////			try {
////				htable.flushCommits();
////				htable.close();
////			} catch (Exception e) {
////				LogUtil.error(e.getMessage(), e);
////			}
//		}
//		return rs;
//	}
//
//}
