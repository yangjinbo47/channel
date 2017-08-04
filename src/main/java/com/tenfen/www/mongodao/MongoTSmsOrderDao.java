package com.tenfen.www.mongodao;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.core.mapreduce.GroupBy;
import org.springframework.data.mongodb.core.mapreduce.GroupByResults;
import org.springframework.data.mongodb.core.mapreduce.MapReduceCounts;
import org.springframework.data.mongodb.core.mapreduce.MapReduceResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.tenfen.mongoEntity.MongoTSmsOrder;

@Component
public class MongoTSmsOrderDao extends MongoGenDao<MongoTSmsOrder>{
	
	@Override
    protected Class<MongoTSmsOrder> getEntityClass() {
        return MongoTSmsOrder.class;
    }
	
	/**
	 * 根据order_id进行保存操作
	 * 不存在保存，存在则更新
	 * @param entity
	 */
	public void saveAndUpdate(MongoTSmsOrder entity) {
		Criteria criteria = Criteria.where("order_id").is(entity.getOrderId());
		Query query = new Query(criteria);
		Update update = new Update();
		update.set("imsi", entity.getImsi());
		update.set("order_id", entity.getOrderId());
		update.set("out_trade_no", entity.getOutTradeNo());
		update.set("link_id", entity.getLinkId());
		update.set("seller_id", entity.getSellerId());
		update.set("app_id", entity.getAppId());
		update.set("merchant_id", entity.getMerchantId());
		update.set("subject", entity.getSubject());
		update.set("sender_number", entity.getSenderNumber());
		update.set("msg_content", entity.getMsgContent());
		update.set("mo_number", entity.getMoNumber());
		update.set("mo_msg", entity.getMoMsg());
		update.set("create_time", entity.getCreateTime());
		update.set("fee", entity.getFee());
		update.set("product_type", entity.getProductType());
		update.set("status", entity.getStatus());
		update.set("pay_time", entity.getPayTime());
		update.set("pay_phone", entity.getPayPhone());
		update.set("province", entity.getProvince());
		update.set("unsubscribe_time", entity.getUnsubscribeTime());
		update.set("reduce", entity.getReduce());
		super.updateInsert(query, update);
	}
	
	public List<MongoTSmsOrder> getOrderList(Integer sellerId, String payPhone, Date startTime, Date endTime) {
		Criteria criteria = Criteria.where("seller_id").is(sellerId).and("pay_phone").is(payPhone).and("create_time").gt(startTime).lt(endTime);
		Query query = new Query(criteria);
		return findList(query);
	}
	
	public List<MongoTSmsOrder> getOrderList(Integer sellerId, Date startTime, Date endTime) {
		Criteria criteria = Criteria.where("seller_id").is(sellerId).and("create_time").gt(startTime).lt(endTime);
		Query query = new Query(criteria);
		return findList(query);
	}
	
	public List<MongoTSmsOrder> getOrderListByPhone(int page, int pageSize, String phone) {
		int start = (page - 1) * pageSize;
		Criteria criteria = Criteria.where("pay_phone").is(phone);
		Query query = new Query(criteria);
		return getPage(query, start, pageSize);
	}
	
	public Long getOrderListByPhoneCount(String phone) {
		Criteria criteria = Criteria.where("pay_phone").is(phone);
		Query query = new Query(criteria);
		return getPageCount(query);
	}
	
	/**
	 * 用户在一定时间内的信息费
	 * @param sellerId
	 * @param phone
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public Integer getSumFeeByPhone(Integer sellerId, String phone, Date startTime, Date endTime) {
		Criteria criteria = Criteria.where("seller_id").is(sellerId).and("pay_phone").is(phone).and("create_time").gt(startTime).lt(endTime).and("status").is("3");
		Query query = new Query(criteria);
		String map = "function() {emit(this.pay_phone, {count:1,fee:this.fee});}";
		String reduce = "function(key, values) {"
				+ "var total = 0,sumfee = 0;"
				+ "for(var i=0;i<values.length;i++){"
				+ "total += values[i].count;"
				+ "sumfee += values[i].fee;"
				+ "}"
				+ "return {count:total,fee:sumfee};"
				+ "}";
		MapReduceResults<MongoTSmsOrder> r = mongoTemplate.mapReduce(query, "t_sms_order", map, reduce, MongoTSmsOrder.class);
		DBObject dbObject = r.getRawResults();
		JSONArray jsonArray = JSONArray.parseArray(String.valueOf(dbObject.get("results")));
		Integer sumfee = 0;
		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject o = (JSONObject)jsonArray.get(i);
			JSONObject countObj = (JSONObject)o.get("value");
			sumfee = countObj.getInteger("fee");
		}
		return sumfee;
	}
	
	/**
	 * group by app_id 各状态请求订单数
	 * @param sellerId
	 * @param startTime
	 * @param endTime
	 */
	public Map<Integer, String> reqStateCount(Integer sellerId, Date startTime, Date endTime, String status) {
		GroupBy groupBy = GroupBy.key("app_id").initialDocument("{count:0, fee:0}").reduceFunction("function(doc, aggr){aggr.count+=1;aggr.fee+=doc.fee}");
		Criteria criteria = Criteria.where("seller_id").is(sellerId).and("create_time").gt(startTime).lt(endTime).and("status").is(status);
		GroupByResults<MongoTSmsOrder> r = mongoTemplate.group(criteria, "t_sms_order", groupBy, MongoTSmsOrder.class);
		BasicDBList list = (BasicDBList)r.getRawResults().get("retval");
		Map<Integer, String> map = new HashMap<Integer, String>();
		for (int i = 0; i < list.size(); i ++) {
			BasicDBObject obj = (BasicDBObject)list.get(i);
			Integer appId = obj.getInt("app_id");
			JSONObject json = new JSONObject();
			json.put("count", obj.getInt("count"));
			json.put("fee", obj.getInt("fee"));
			map.put(appId, json.toString());
		}
		return map;
	}
	
	/**
	 * group by app_id 成功用户数
	 * @param sellerId
	 * @param startTime
	 * @param endTime
	 */
	public Map<Integer, Integer> succUserCount(Integer sellerId, Date startTime, Date endTime) {
		GroupBy groupBy = GroupBy.key("app_id","imsi").initialDocument("{user:0}").reduceFunction("function(doc, aggr){aggr.user += 1;}");
		Criteria criteria = Criteria.where("seller_id").is(sellerId).and("create_time").gt(startTime).lt(endTime).and("status").is("3");
		GroupByResults<MongoTSmsOrder> r = mongoTemplate.group(criteria, "t_sms_order", groupBy, MongoTSmsOrder.class);
		BasicDBList list = (BasicDBList)r.getRawResults().get("retval");
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		for (int i = 0; i < list.size(); i ++) {
            BasicDBObject obj = (BasicDBObject)list.get(i);
            Integer appId = obj.getInt("app_id");
            Integer value = map.get(appId);
            if (value == null) {
				map.put(appId, 1);
			} else {
				map.put(appId, value+1);
			}
        }
		return map;
	}
	
	/**
	 * group by app_id 请求用户数
	 * @param sellerId
	 * @param startTime
	 * @param endTime
	 */
	public Map<Integer, Integer> reqUserCount(Integer sellerId, Date startTime, Date endTime) {
		GroupBy groupBy = GroupBy.key("app_id","imsi").initialDocument("{user:0}").reduceFunction("function(doc, aggr){aggr.user += 1;}");
		Criteria criteria = Criteria.where("seller_id").is(sellerId).and("create_time").gt(startTime).lt(endTime);
		GroupByResults<MongoTSmsOrder> r = mongoTemplate.group(criteria, "t_sms_order", groupBy, MongoTSmsOrder.class);
		BasicDBList list = (BasicDBList)r.getRawResults().get("retval");
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		for (int i = 0; i < list.size(); i ++) {
            BasicDBObject obj = (BasicDBObject)list.get(i);
            Integer appId = obj.getInt("app_id");
            Integer value = map.get(appId);
            if (value == null) {
				map.put(appId, 1);
			} else {
				map.put(appId, value+1);
			}
        }
		return map;
	}
	
	/**
	 * 根据sellerId reduce出该sellerId下存在多少appId,并统计请求数和金额
	 * @param sellerId
	 * @param startTime
	 * @param endTime
	 * @return
	 */
//	public Map<Integer, String> mapReduceAppIds(Integer sellerId, Date startTime, Date endTime, String status, Integer reduce) {
//		Map<Integer, String> returnMap = new HashMap<Integer, String>();
//		Criteria criteria = null;
//		if (reduce == null) {
//			criteria = Criteria.where("seller_id").is(sellerId).and("create_time").gt(startTime).lt(endTime).and("status").is(status);
//		} else {
//			criteria = Criteria.where("seller_id").is(sellerId).and("create_time").gt(startTime).lt(endTime).and("status").is(status).and("reduce").is(reduce);
//		}
//		Query query = new Query(criteria);
//		String map = "function() {emit(this.app_id, {count:1,fee:this.fee});}";
//		String reduceStr = "function(key, values) {"
//				+ "var total = 0,sumfee = 0;"
//				+ "for(var i=0;i<values.length;i++){"
//				+ "total += values[i].count;"
//				+ "sumfee += values[i].fee;"
//				+ "}"
//				+ "return {count:total,fee:sumfee};"
//				+ "}";
//		MapReduceResults<MongoTSmsOrder> r = mongoTemplate.mapReduce(query, "t_sms_order", map, reduceStr, MongoTSmsOrder.class);
//		DBObject dbObject = r.getRawResults();
//		JSONArray jsonArray = JSONArray.parseArray(String.valueOf(dbObject.get("results")));
//		for (int i = 0; i < jsonArray.size(); i++) {
//			JSONObject o = (JSONObject)jsonArray.get(i);
//			JSONObject countObj = (JSONObject)o.get("value");
//			JSONObject returnJson = new JSONObject();
//			returnJson.put("count", countObj.getInteger("count"));
//			returnJson.put("fee", countObj.getInteger("fee"));
//			returnMap.put(o.getInteger("_id"), returnJson.toString());
//		}
//		return returnMap;
//	}
	public Map<Integer, String> mapReduceAppIds(Integer sellerId, Date startTime, Date endTime, String status, Integer reduce) {
		Map<Integer, String> returnMap = new HashMap<Integer, String>();
		if (reduce == null) {
			reduce = 0;
		}
		Criteria criteria = Criteria.where("seller_id").is(sellerId).and("create_time").gt(startTime).lt(endTime).and("status").is(status);
		Query query = new Query(criteria);
		String map = "function() {emit(this.app_id, {count:1,user:1,fee:this.fee,feeReduce:0,imsis:this.imsi,reduce:this.reduce});}";
		String reduceStr = "function(key, values) {"
				+ "var total = 0, sumfee = 0, sumfeeReduce = 0;"
				+ "var temp = new Array();"
				+ "var imsis = new Array;"
				+ "for(var i=0;i<values.length;i++){"
				+ "total += values[i].count;"
				+ "imsis=imsis.concat(values[i].imsis);"
				+ "sumfee += values[i].fee;"
				+ "if("+reduce+" == values[i].reduce){"
				+ "sumfeeReduce += values[i].fee;"
				+ "}"
				+ "}"
				//imsis去重
				+ "imsis.sort();"
				+ "for(i = 0; i < imsis.length; i++) {"
				+ "if(imsis[i] == imsis[i+1]) {continue;}"
				+ "temp[temp.length]=imsis[i];"
				+ "}"
				+ "return {count:total,user:temp.length,fee:sumfee,feeReduce:sumfeeReduce,imsis:imsis,reduce:"+reduce+"};"
				+ "}";
		MapReduceResults<MongoTSmsOrder> r = mongoTemplate.mapReduce(query, "t_sms_order", map, reduceStr, MongoTSmsOrder.class);
		DBObject dbObject = r.getRawResults();
		JSONArray jsonArray = JSONArray.parseArray(String.valueOf(dbObject.get("results")));
		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject o = (JSONObject)jsonArray.get(i);
			JSONObject countObj = (JSONObject)o.get("value");
			JSONObject returnJson = new JSONObject();
			returnJson.put("count", countObj.getInteger("count"));
			returnJson.put("user", countObj.getInteger("user"));
			returnJson.put("fee", countObj.getInteger("fee"));
			returnJson.put("feeReduce", countObj.getInteger("feeReduce"));
			returnMap.put(o.getInteger("_id"), returnJson.toString());
		}
		return returnMap;
	}
	
	/**
	 * 根据appId reduce出该appId下存在多少sellerId,并统计请求数和金额
	 * @param appId
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public Map<Integer, String> mapReduceSellerIds(Integer appId, Date startTime, Date endTime, String status, Integer reduce) {
		Map<Integer, String> returnMap = new HashMap<Integer, String>();
		Criteria criteria = null;
		if (reduce == null) {
			criteria = Criteria.where("app_id").is(appId).and("create_time").gt(startTime).lt(endTime).and("status").is(status);
		} else {
			criteria = Criteria.where("app_id").is(appId).and("create_time").gt(startTime).lt(endTime).and("status").is(status).and("reduce").is(reduce);
		}
		Query query = new Query(criteria);
		String map = "function() {emit(this.seller_id, {count:1,fee:this.fee});}";
		String reduceStr = "function(key, values) {"
				+ "var total = 0,sumfee = 0;"
				+ "for(var i=0;i<values.length;i++){"
				+ "total += values[i].count;"
				+ "sumfee += values[i].fee;"
				+ "}"
				+ "return {count:total,fee:sumfee};"
				+ "}";
		MapReduceResults<MongoTSmsOrder> r = mongoTemplate.mapReduce(query, "t_sms_order", map, reduceStr, MongoTSmsOrder.class);
		DBObject dbObject = r.getRawResults();
		JSONArray jsonArray = JSONArray.parseArray(String.valueOf(dbObject.get("results")));
		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject o = (JSONObject)jsonArray.get(i);
			JSONObject countObj = (JSONObject)o.get("value");
			JSONObject returnJson = new JSONObject();
			returnJson.put("count", countObj.getInteger("count"));
			returnJson.put("fee", countObj.getInteger("fee"));
			returnMap.put(o.getInteger("_id"), returnJson.toString());
		}
		return returnMap;
	}
	
	/**
	 * 根据sellerId reduce出该sellerId下存在多少省份,并统计请求数和金额
	 * @param sellerId
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public Map<String, String> mapReduceProvinceBySellerId(Integer sellerId, Date startTime, Date endTime, String status) {
		Map<String, String> returnMap = new HashMap<String, String>();
		Criteria criteria = Criteria.where("seller_id").is(sellerId).and("create_time").gt(startTime).lt(endTime).and("status").is(status);
		Query query = new Query(criteria);
		String map = "function() {emit(this.province, {count:1,fee:this.fee});}";
		String reduce = "function(key, values) {"
				+ "var total = 0,sumfee = 0;"
				+ "for(var i=0;i<values.length;i++){"
				+ "total += values[i].count;"
				+ "sumfee += values[i].fee;"
				+ "}"
				+ "return {count:total,fee:sumfee};"
				+ "}";
		MapReduceResults<MongoTSmsOrder> r = mongoTemplate.mapReduce(query, "t_sms_order", map, reduce, MongoTSmsOrder.class);
		DBObject dbObject = r.getRawResults();
		JSONArray jsonArray = JSONArray.parseArray(String.valueOf(dbObject.get("results")));
		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject o = (JSONObject)jsonArray.get(i);
			JSONObject countObj = (JSONObject)o.get("value");
			JSONObject returnJson = new JSONObject();
			returnJson.put("count", countObj.getInteger("count"));
			returnJson.put("fee", countObj.getInteger("fee"));
			returnMap.put(o.getString("_id"), returnJson.toString());
		}
		return returnMap;
	}
	
	/**
	 * 根据appId reduce出该sellerId下存在多少省份,并统计请求数和金额
	 * @param sellerId
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public Map<String, String> mapReduceProvinceByAppId(Integer appId, Date startTime, Date endTime, String status) {
		Map<String, String> returnMap = new HashMap<String, String>();
		Criteria criteria = Criteria.where("app_id").is(appId).and("create_time").gt(startTime).lt(endTime).and("status").is(status);
		Query query = new Query(criteria);
		String map = "function() {emit(this.province, {count:1,fee:this.fee});}";
		String reduce = "function(key, values) {"
				+ "var total = 0,sumfee = 0;"
				+ "for(var i=0;i<values.length;i++){"
				+ "total += values[i].count;"
				+ "sumfee += values[i].fee;"
				+ "}"
				+ "return {count:total,fee:sumfee};"
				+ "}";
		MapReduceResults<MongoTSmsOrder> r = mongoTemplate.mapReduce(query, "t_sms_order", map, reduce, MongoTSmsOrder.class);
		DBObject dbObject = r.getRawResults();
		JSONArray jsonArray = JSONArray.parseArray(String.valueOf(dbObject.get("results")));
		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject o = (JSONObject)jsonArray.get(i);
			JSONObject countObj = (JSONObject)o.get("value");
			JSONObject returnJson = new JSONObject();
			returnJson.put("count", countObj.getInteger("count"));
			returnJson.put("fee", countObj.getInteger("fee"));
			returnMap.put(o.getString("_id"), returnJson.toString());
		}
		return returnMap;
	}
	
	
	/**
	 * 根据sellerId，appId reduce出用户数
	 * @param sellerId
	 * @param appId
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public Long mapReduceUserCount(Integer sellerId, Integer appId, Date startTime, Date endTime) {
		Criteria criteria = Criteria.where("seller_id").is(sellerId).and("app_id").is(appId).and("create_time").gt(startTime).lt(endTime);
		Query query = new Query(criteria);
		String map = "function() { emit(this.pay_phone, {count:1});}";
		String reduce = "function(key, values) {var total = 0;for(var i=0;i<values.length;i++){total += values[i].count;}return {count:total};}";
		MapReduceResults<MongoTSmsOrder> r = mongoTemplate.mapReduce(query, "t_sms_order", map, reduce, MongoTSmsOrder.class);
		MapReduceCounts mapReduceCounts = r.getCounts();
		return mapReduceCounts.getOutputCount();
	}
	
	/**
	 * 根据sellerId，appId reduce出成功用户数
	 * @param sellerId
	 * @param appId
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public Long mapReduceSuccUserCount(Integer sellerId, Integer appId, Date startTime, Date endTime) {
		Criteria criteria = Criteria.where("seller_id").is(sellerId).and("app_id").is(appId).and("create_time").gt(startTime).lt(endTime).and("status").is("3");
		Query query = new Query(criteria);
		String map = "function() { emit(this.pay_phone, {count:1});}";
		String reduce = "function(key, values) {var total = 0;for(var i=0;i<values.length;i++){total += values[i].count;}return {count:total};}";
		MapReduceResults<MongoTSmsOrder> r = mongoTemplate.mapReduce(query, "t_sms_order", map, reduce, MongoTSmsOrder.class);
		MapReduceCounts mapReduceCounts = r.getCounts();
		return mapReduceCounts.getOutputCount();
	}
	
	/**
	 * 根据sellerId,appId reduce出该sellerId,appId下存在多少省份,并统计请求数和金额
	 * @param sellerId
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public Map<String, String> mapReduceProvinceBySellerIdAndAppId(Integer sellerId, Integer appId, Date startTime, Date endTime, String status) {
		Map<String, String> returnMap = new HashMap<String, String>();
		Criteria criteria = Criteria.where("seller_id").is(sellerId).and("app_id").is(appId).and("create_time").gt(startTime).lt(endTime).and("status").is(status);
		Query query = new Query(criteria);
		String map = "function() {emit(this.province, {count:1,user:1,fee:this.fee,imsis:this.imsi});}";
		String reduce = "function(key, values) {"
				+ "var total = 0,sumfee = 0;"
				+ "var temp = new Array();"
				+ "var imsis = new Array;"
				+ "for(var i=0;i<values.length;i++){"
				+ "total += values[i].count;"
				+ "imsis=imsis.concat(values[i].imsis);"
				+ "sumfee += values[i].fee;"
				+ "}"
				//imsis去重
				+ "imsis.sort();"
				+ "for(i = 0; i < imsis.length; i++) {"
				+ "if(imsis[i] == imsis[i+1]) {continue;}"
				+ "temp[temp.length]=imsis[i];"
				+ "}"
				+ "return {count:total,user:temp.length,fee:sumfee,imsis:imsis};"
				+ "}";
		MapReduceResults<MongoTSmsOrder> r = mongoTemplate.mapReduce(query, "t_sms_order", map, reduce, MongoTSmsOrder.class);
		DBObject dbObject = r.getRawResults();
		JSONArray jsonArray = JSONArray.parseArray(String.valueOf(dbObject.get("results")));
		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject o = (JSONObject)jsonArray.get(i);
			JSONObject countObj = (JSONObject)o.get("value");
			JSONObject returnJson = new JSONObject();
			returnJson.put("count", countObj.getInteger("count"));
			returnJson.put("user", countObj.getInteger("user"));
			returnJson.put("fee", countObj.getInteger("fee"));
			returnMap.put(o.getString("_id"), returnJson.toString());
		}
		return returnMap;
	}
}
