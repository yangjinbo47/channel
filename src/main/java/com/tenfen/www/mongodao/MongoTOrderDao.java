package com.tenfen.www.mongodao;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.core.mapreduce.MapReduceResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.DBObject;
import com.tenfen.mongoEntity.MongoTOrder;

@Component
public class MongoTOrderDao extends MongoGenDao<MongoTOrder>{
	
	@Override
    protected Class<MongoTOrder> getEntityClass() {
        return MongoTOrder.class;
    }
	
	/**
	 * 根据order_id进行保存操作
	 * 不存在保存，存在则更新
	 * @param entity
	 */
	public void saveAndUpdate(MongoTOrder entity) {
		Criteria criteria = Criteria.where("trade_id").is(entity.getTradeId());
		Query query = new Query(criteria);
		Update update = new Update();
		update.set("trade_id", entity.getTradeId());
		update.set("out_trade_no", entity.getOutTradeNo());
		update.set("imsi", entity.getImsi());
		update.set("phone_num", entity.getPhoneNum());
		update.set("seller_id", entity.getSellerId());
		update.set("push_id", entity.getPushId());
		update.set("fee", entity.getFee());
		update.set("status", entity.getStatus());
		update.set("create_time", entity.getCreateTime());
		update.set("name", entity.getName());
//		update.set("channel", entity.getChannel());
		update.set("province", entity.getProvince());
		update.set("reduce", entity.getReduce());
		super.updateInsert(query, update);
	}
	
	public Map<Integer, String> packageCount(Date startTime, Date endTime, Integer status) {
		Map<Integer, String> returnMap = new HashMap<Integer, String>();
		Criteria criteria = Criteria.where("create_time").gt(startTime).lt(endTime).and("status").is(status);
		Query query = new Query(criteria);
		String map = "function() {emit(this.push_id, {count:1,fee:this.fee});}";
		String reduce = "function(key, values) {"
				+ "var total = 0,sumfee = 0;"
				+ "for(var i=0;i<values.length;i++){"
				+ "total += values[i].count;"
				+ "sumfee += values[i].fee;"
				+ "}"
				+ "return {count:total,fee:sumfee};"
				+ "}";
		MapReduceResults<MongoTOrder> r = mongoTemplate.mapReduce(query, "t_order", map, reduce, MongoTOrder.class);
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
	
	public List<MongoTOrder> getOrderListByPhone(int page, int pageSize, String phone) {
		int start = (page - 1) * pageSize;
		Criteria criteria = Criteria.where("phone_num").is(phone);
		Query query = new Query(criteria);
		return getPage(query, start, pageSize);
	}
	
	public Long getOrderListByPhoneCount(String phone) {
		Criteria criteria = Criteria.where("phone_num").is(phone);
		Query query = new Query(criteria);
		return getPageCount(query);
	}
	
	/**
	 * mapreduce出渠道数据（mo、mo去重量、mr、信息费）
	 * @param startTime
	 * @param endTime
	 * @return
	 */
//	public Map<Integer, String> mapReducePushIds(Integer sellerId, Date startTime, Date endTime, Integer status) {
//		Map<Integer, String> returnMap = new HashMap<Integer, String>();
//		Criteria criteria = Criteria.where("seller_id").is(sellerId).and("create_time").gt(startTime).lt(endTime);
//		Query query = new Query(criteria);
//		//count:总数    user：用户数    succ：成功数
//		String map = "function() {emit(this.push_id, {count:1,user:1,succ:1,fee:this.fee,imsis:this.imsi,status:this.status});}";
//		String reduce = "function(key, values) {"
//				+ "var total = 0,succfee = 0,mr = 0;"
//				+ "var temp = new Array();"
//				+ "var imsis = new Array;"
//				+ "for(var i=0;i<values.length;i++){"
//				+ "total += values[i].count;"
//				+ "imsis=imsis.concat(values[i].imsis);"
//				+ "if("+status+" == values[i].status){"
//				+ "mr += values[i].succ;"
//				+ "succfee += values[i].fee;"
//				+ "}"
//				+ "}"
//				//imsis去重
//				+ "imsis.sort();"
//				+ "for(i = 0; i < imsis.length; i++) {"
//				+ "if(imsis[i] == imsis[i+1]) {continue;}"
//				+ "temp[temp.length]=imsis[i];"
//				+ "}"
//				+ "return {count:total, user:temp.length, succ:mr, fee:succfee, imsis:imsis, status:"+status+"};"
//				+ "}";
//		MapReduceResults<MongoTOrder> r = mongoTemplate.mapReduce(query, "t_order", map, reduce, MongoTOrder.class);
//		DBObject dbObject = r.getRawResults();
//		JSONArray jsonArray = JSONArray.parseArray(String.valueOf(dbObject.get("results")));
//		for (int i = 0; i < jsonArray.size(); i++) {
//			JSONObject o = (JSONObject)jsonArray.get(i);
//			JSONObject countObj = (JSONObject)o.get("value");
//			JSONObject returnJson = new JSONObject();
//			returnJson.put("count", countObj.getInteger("count"));
//			returnJson.put("user", countObj.getInteger("user"));
//			returnJson.put("succ", countObj.getInteger("succ"));
//			returnJson.put("fee", countObj.getInteger("fee"));
//			returnMap.put(o.getInteger("_id"), returnJson.toString());
//		}
//		return returnMap;
//	}
	public Map<Integer, String> mapReducePushIds(Integer sellerId, Date startTime, Date endTime, Integer status) {
		Map<Integer, String> returnMap = new HashMap<Integer, String>();
		Criteria criteria = Criteria.where("seller_id").is(sellerId).and("status").is(status).and("create_time").gt(startTime).lt(endTime);
		Query query = new Query(criteria);
		//count:总数    user：用户数    succ：成功数
		String map = "function() {emit(this.push_id, {count:1,user:1,fee:this.fee,imsi:this.imsi});}";
		String reduce = "function(key, values) {"
				+ "var total = 0,amount = 0;"
				+ "var temp = new Array();"
				+ "var imsis = new Array;"
				+ "for(var i=0;i<values.length;i++){"
				+ "total += values[i].count;"
				+ "imsis=imsis.concat(values[i].imsi);"
				+ "amount += values[i].fee;"
				+ "}"
				//imsis去重
				+ "imsis.sort();"
				+ "for(i = 0; i < imsis.length; i++) {"
				+ "if(imsis[i] == imsis[i+1]) {continue;}"
				+ "temp[temp.length]=imsis[i];"
				+ "}"
				+ "return {count:total, user:temp.length, fee:amount, imsi:imsis};"
				+ "}";
		MapReduceResults<MongoTOrder> r = mongoTemplate.mapReduce(query, "t_order", map, reduce, MongoTOrder.class);
		DBObject dbObject = r.getRawResults();
		JSONArray jsonArray = JSONArray.parseArray(String.valueOf(dbObject.get("results")));
		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject o = (JSONObject)jsonArray.get(i);
			JSONObject countObj = (JSONObject)o.get("value");
			JSONObject returnJson = new JSONObject();
			returnJson.put("count", countObj.getInteger("count"));
			returnJson.put("user", countObj.getInteger("user"));
			returnJson.put("fee", countObj.getInteger("fee"));
			returnMap.put(o.getInteger("_id"), returnJson.toString());
		}
		return returnMap;
	}
	
	public Map<String, Map<Integer, String>> mapReduceProvince(Integer sellerId, Date startTime, Date endTime) {
		Map<String, Map<Integer, String>> returnMap = new HashMap<String, Map<Integer, String>>();
		Map<Integer, String> statusMap = new HashMap<Integer, String>();
		Criteria criteria = Criteria.where("seller_id").is(sellerId).and("create_time").gt(startTime).lt(endTime);;
		Query query = new Query(criteria);
		String map = "function() {emit({province:this.province,status:this.status}, {count:1,user:1,fee:this.fee,imsi:this.imsi});}";
		String reduce = "function(key, values) {"
				+ "var total = 0,amount = 0;"
				+ "var temp = new Array();"
				+ "var imsis = new Array;"
				+ "for(var i=0;i<values.length;i++){"
				+ "total += values[i].count;"
				+ "amount += values[i].fee;"
				+ "imsis=imsis.concat(values[i].imsi);"
				+ "}"
				//imsis去重
				+ "imsis.sort();"
				+ "for(i = 0; i < imsis.length; i++) {"
				+ "if(imsis[i] == imsis[i+1]) {continue;}"
				+ "temp[temp.length]=imsis[i];"
				+ "}"
				+ "return {count:total, user:temp.length, fee:amount, imsi:imsis};"
				+ "}";
		MapReduceResults<MongoTOrder> r = mongoTemplate.mapReduce(query, "t_order", map, reduce, MongoTOrder.class);
		DBObject dbObject = r.getRawResults();
		JSONArray jsonArray = JSONArray.parseArray(String.valueOf(dbObject.get("results")));
		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject o = (JSONObject)jsonArray.get(i);
			JSONObject mapJson = (JSONObject)o.get("_id");
			String province = mapJson.getString("province");
			
			Integer status = mapJson.getInteger("status");
			JSONObject countObj = (JSONObject)o.get("value");
			JSONObject returnJson = new JSONObject();
			returnJson.put("count", countObj.getInteger("count"));
			returnJson.put("user", countObj.getInteger("user"));
			returnJson.put("fee", countObj.getInteger("fee"));
			
			statusMap = returnMap.get(province) == null ? new HashMap<Integer, String>() : returnMap.get(province);
			statusMap.put(status, returnJson.toString());
			returnMap.put(province, statusMap);
		}
		return returnMap;
	}
	
	public Map<String, Map<Integer, String>> mapReduceProvince(Integer sellerId, Integer pushId, Date startTime, Date endTime) {
		Map<String, Map<Integer, String>> returnMap = new HashMap<String, Map<Integer, String>>();
		Map<Integer, String> statusMap = new HashMap<Integer, String>();
		Criteria criteria = Criteria.where("seller_id").is(sellerId).and("push_id").is(pushId).and("create_time").gt(startTime).lt(endTime);;
		Query query = new Query(criteria);
		String map = "function() {emit({province:this.province,status:this.status}, {count:1,user:1,fee:this.fee,imsi:this.imsi});}";
		String reduce = "function(key, values) {"
				+ "var total = 0,amount = 0;"
				+ "var temp = new Array();"
				+ "var imsis = new Array;"
				+ "for(var i=0;i<values.length;i++){"
				+ "total += values[i].count;"
				+ "amount += values[i].fee;"
				+ "imsis=imsis.concat(values[i].imsi);"
				+ "}"
				//imsis去重
				+ "imsis.sort();"
				+ "for(i = 0; i < imsis.length; i++) {"
				+ "if(imsis[i] == imsis[i+1]) {continue;}"
				+ "temp[temp.length]=imsis[i];"
				+ "}"
				+ "return {count:total, user:temp.length, fee:amount, imsi:imsis};"
				+ "}";
		MapReduceResults<MongoTOrder> r = mongoTemplate.mapReduce(query, "t_order", map, reduce, MongoTOrder.class);
		DBObject dbObject = r.getRawResults();
		JSONArray jsonArray = JSONArray.parseArray(String.valueOf(dbObject.get("results")));
		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject o = (JSONObject)jsonArray.get(i);
			JSONObject mapJson = (JSONObject)o.get("_id");
			String province = mapJson.getString("province");
			
			Integer status = mapJson.getInteger("status");
			JSONObject countObj = (JSONObject)o.get("value");
			JSONObject returnJson = new JSONObject();
			returnJson.put("count", countObj.getInteger("count"));
			returnJson.put("user", countObj.getInteger("user"));
			returnJson.put("fee", countObj.getInteger("fee"));
			
			statusMap = returnMap.get(province) == null ? new HashMap<Integer, String>() : returnMap.get(province);
			statusMap.put(status, returnJson.toString());
			returnMap.put(province, statusMap);
		}
		return returnMap;
	}
	
	/**
	 * mapreduce出渠道数据（mo、mo去重量、mr、信息费）
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public Map<Integer, Map<Integer, String>> mapReduceSeller(Date startTime, Date endTime) {
		Map<Integer, Map<Integer, String>> returnMap = new HashMap<Integer, Map<Integer, String>>();
		Map<Integer, String> statusMap = new HashMap<Integer, String>();
		Criteria criteria = Criteria.where("create_time").gt(startTime).lt(endTime);
		Query query = new Query(criteria);
		//count:总数    user：用户数  
		String map = "function() {emit({seller_id:this.seller_id,status:this.status}, {count:1,user:1,fee:this.fee,imsi:this.imsi});}";
		String reduce = "function(key, values) {"
				+ "var total = 0,amount = 0;"
				+ "var temp = new Array();"
				+ "var imsis = new Array;"
				+ "for(var i=0;i<values.length;i++){"
				+ "total += values[i].count;"
				+ "amount += values[i].fee;"
				+ "imsis=imsis.concat(values[i].imsi);"
				+ "}"
				//imsis去重
				+ "imsis.sort();"
				+ "for(i = 0; i < imsis.length; i++) {"
				+ "if(imsis[i] == imsis[i+1]) {continue;}"
				+ "temp[temp.length]=imsis[i];"
				+ "}"
				+ "return {count:total, user:temp.length, fee:amount, imsi:imsis};"
				+ "}";
		MapReduceResults<MongoTOrder> r = mongoTemplate.mapReduce(query, "t_order", map, reduce, MongoTOrder.class);
		DBObject dbObject = r.getRawResults();
		JSONArray jsonArray = JSONArray.parseArray(String.valueOf(dbObject.get("results")));
		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject o = (JSONObject)jsonArray.get(i);
			JSONObject mapJson = (JSONObject)o.get("_id");
			Integer sellerId = mapJson.getInteger("seller_id");
			
			Integer status = mapJson.getInteger("status");
			JSONObject countObj = (JSONObject)o.get("value");
			JSONObject returnJson = new JSONObject();
			returnJson.put("count", countObj.getInteger("count"));
			returnJson.put("user", countObj.getInteger("user"));
			returnJson.put("fee", countObj.getInteger("fee"));
			
			statusMap = returnMap.get(sellerId) == null ? new HashMap<Integer, String>() : returnMap.get(sellerId);
			statusMap.put(status, returnJson.toString());
			returnMap.put(sellerId, statusMap);
		}
		return returnMap;
	}
	
}
