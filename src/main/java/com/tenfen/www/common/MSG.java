package com.tenfen.www.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;


/**
 * 返回成功或失败的JSON格式信息到前台
 */
public class MSG {
	
	public final static String ADDSUCCESS = "添加成功";		//添加成功字符串常量
	public final static String ADDFAILURE = "添加失败";		//添加失败字符串常量
	public final static String SAVESUCCCESS = "保存成功";	//保存成功字符串常量
	public final static String SAVEFAILURE = "保存失败";		//保存失败字符串常量
	public final static String DELETESUCCESS = "删除成功";	//删除成功字符串常量
	public final static String DELETEFAILURE = "删除失败";	//删除失败字符串常量
	public final static String UPDATESUCCESS = "更新成功";	//更新成功字符串常量
	public final static String UPDATEFAILURE = "更新失败";	//更新失败字符串常量
	public final static String EXCEPTION = "出现异常";		//出现异常情况
	public final static String MISSINGPARAM = "缺少能数";		//缺少参数

	private boolean exception;	//异常否
	private boolean success;	//是否成功
	private Object msg;			//与成功或失败相关的消息说明
	private Object data;		//额外需要输出的信息
	
	/**
	 * 带有成功与否标志与相关信息说明的构造函数
	 * @param success true/false表示成功/失败
	 * @param msg 成功/失败消息说明
	 */
	public MSG(boolean success,Object msg){
		this.success = success;
		this.msg = msg;
	}
	
	/**
	 * 带有成功与否标志,相关信息说明,额外数据的构造函数
	 * @param success true/false表示成功/失败
	 * @param msg 成功/失败消息说明
	 * @param data 额外信息
	 */
	public MSG(boolean success,Object msg,Object data){
		this.success = success;
		this.msg = msg;
		this.data = data;
	}
	
	public MSG(boolean success,boolean exception,Object msg,Object data){
		this.success = success;
		this.exception = exception;
		this.msg = msg;
		this.data = data;
	}
	
	public MSG(boolean success,boolean exception,Object msg){
		this.success = success;
		this.exception = exception;
		this.msg = msg;
	}
	
	/**
	 * 将一个消息转化为带有成功标志的JSON格式的字符串l,格式类似为:{success:true,msg:'XXX'}
	 * @param msg 参与序列化的消息
	 * @return 经JSON-Lib序列化后的JSON格式字符串,不包含data属性
	 */
	public static String success(Object msg){
		MSG msgBean = new MSG(true,msg);
		SimplePropertyPreFilter filter = new SimplePropertyPreFilter();
		filter.getExcludes().add("data");
		filter.getExcludes().add("exception");
		return JSON.toJSONString(msgBean, filter);
	}
	
	/**
	 * 将消息说明,额外数据序列化为带有成功标记的JSON格式的字符串,格式类似为:{success:true,msg:'XX',data:XXX}
	 * @param msg 要参与序列化的消息说明
	 * @param data 要参与序列化的额外数据
	 * @return 经JSON-Lib序列化的带有成功标记,消息说明,额外数据的JSON格式字符串
	 */
	public static String success(Object msg,Object data){
		MSG msgBean = new MSG(true,msg,data);
		SimplePropertyPreFilter filter = new SimplePropertyPreFilter();
		filter.getExcludes().add("exception");
		return JSON.toJSONString(msgBean, filter);
	}
	
	public static String failure(Object msg,Object data){
		MSG msgBean = new MSG(false,msg,data);
		SimplePropertyPreFilter filter = new SimplePropertyPreFilter();
		filter.getExcludes().add("exception");
		return JSON.toJSONString(msgBean, filter);
	}
	
	/**
	 * 将消息说明序列化为一个带有失败标记的JSON格式字符串,格式类似为:{success:false,msg:'XXX'}
	 * @param msg 参与序列化的消息说明
	 * @return 经JSON-Lib序列化后带有失败标记和信息说明的JSON格式字符串
	 */
	public static String failure(Object msg){
		MSG msgBean = new MSG(false,msg);
		SimplePropertyPreFilter filter = new SimplePropertyPreFilter();
		filter.getExcludes().add("exception");
		return JSON.toJSONString(msgBean,filter);
	}
	
	public static String exception(Object msg){
		MSG msgBean = new MSG(false,true,msg);
		return JSON.toJSONString(msgBean);
	}
	
	/**
	 * 将消息说明序列化为一个JSON格式的字符串,格式类似为:{msg:'XXX'}
	 * @param msg 参与序列化的消息说明
	 * @return 经JSON-Lib序列化后只带有消息说明的JSON字符串,不包含成功与否标志
	 */
	public static String string(Object msg){
		MSG msgBean = new MSG(true,msg);
		SimplePropertyPreFilter filter = new SimplePropertyPreFilter();
		filter.getExcludes().add("success");
		return JSON.toJSONString(msgBean, filter);
	}
	
	/**
	 * 将传入的参数转化为一个JSON格式的字符串反回,格式类型为:{success:XXX,msg:XXX}
	 * @param success
	 * @param msg
	 * @return
	 */
	public static String message(boolean success,String msg){
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("success", success);
		jsonObject.put("msg", msg);
		return JSON.toJSONString(jsonObject); //JSONSerializer.toJSON(jsonObject).toString();
	}
	
	/**
	 * 取得成功与否的标志
	 * @return true/false成功/失败
	 */
	public boolean isSuccess() {
		return success;
	}

	/**
	 * 设置成功/失败标志
	 * @param success true/false成功/失败
	 */
	public void setSuccess(boolean success) {
		this.success = success;
	}

	/**
	 * 取得消息对象
	 * @return 消息对象
	 */
	public Object getMsg() {
		return msg;
	}

	/**
	 * 设置消息
	 * @param msg 要设置的消息对象
	 */
	public void setMsg(Object msg) {
		this.msg = msg;
	}
	
	/**
	 * 获取额外数据对象
	 * @return 额外数据对象
	 */
	public Object getData() {
		return data;
	}

	/**
	 * 设置额外数据
	 * @param data 额外数据对象
	 */
	public void setData(Object data) {
		this.data = data;
	}
	
	public boolean isException() {
		return exception;
	}

	public void setException(boolean exception) {
		this.exception = exception;
	}
}
