package com.tenfen.www.common;

/**
 * 
 * Title: Description:常量类
 * 
 * @author
 * @version 1.0
 */

public class Constants {
	/** session会话中存放的用户名 */
	public static final String OPERATOR_NAME = "operator_name";

	/** session会话中存放的用户ID */
	public static final String OPERATOR_ID = "operator_id";

	/** session会话中存放的用户密码 */
	public static final String OPERATOR_PWD = "operator_pwd";
	
	/** session会话中存放的用户密码 */
	public static final String OPERATOR_TYPE = "operator_type";

	/** 角色类型　 */
	public static final String OPERATOR_ROLETYPE = "operator_roletype";

	/** 角色名称　 */
	public static final String OPERATOR_ROLENAME = "operator_rolename";
	
	public static final String COOKIE_OPERATOR_NAME = "cookie_operator_name";
	
	public static final String COOKIE_OPERATOR_ID = "cookie_operator_id";

	/** 超级管理员 **/
	public static final String SUPER_ADMIN = "administrator";

	/** 超级管理员ID **/
	public static final String SUPER_ADMIN_ID = "00000";
	
	/** memcache Key 相关 **/
	public static final String PRE_FIRST_AUTHORITY = "first_authority_";
	public static final String PRE_SECOND_AUTHORITY = "second_authority_";
	public static final String PRE_WOPLUS_TOKEN = "woplus_token_";
	
	/**
	 * 批量处理名称
	 */
	public static final String TORDER_BATCHSAVEENTITY = "TOrder_BatchSaveEntity";

	/**
	 * 管理员状态
	 * @author BOBO
	 */
	public static enum OPERATOR_STATUS {

		NORMAL(1),FREEZE(2);

		private Integer value;

		OPERATOR_STATUS(Integer value) {
			this.value = value;
		}

		public Integer getValue() {
			return value;
		}
	}
	
	/**
	 * 管理员类型
	 * @author BOBO
	 */
	public static enum USER_TYPE {

		ALL(0),TENFEN(1),QIANKUN(2),ANQING(3);

		private Integer value;

		USER_TYPE(Integer value) {
			this.value = value;
		}

		public Integer getValue() {
			return value;
		}
	}
	
	/**
	 * 权限状态
	 * @author BOBO
	 */
	public static enum PRIVILEGE_STATUS {

		NORMAL(1),FORBID(0);

		private Integer value;

		PRIVILEGE_STATUS(Integer value) {
			this.value = value;
		}

		public Integer getValue() {
			return value;
		}
	}
	
	/**
	 * 包月包状态
	 * @author BOBO
	 */
	public static enum PACKAGE_STATUS {

		NORMAL(1),FREEZE(0);

		private Integer value;

		PACKAGE_STATUS(Integer value) {
			this.value = value;
		}

		public Integer getValue() {
			return value;
		}
	}
	
	/**
	 * 包月订单状态
	 */
	public static enum T_ORDER_STATUS {

		NOPAY(1),SUCCESS(3),FAIL(4);

		private Integer value;

		T_ORDER_STATUS(Integer value) {
			this.value = value;
		}

		public Integer getValue() {
			return value;
		}
	}
	
	/**
	 * 是否公司运营可见
	 */
//	public static enum COMPANY_SHOW {
//
//		SHOW(1),HIDDEN(0);
//
//		private Integer value;
//
//		COMPANY_SHOW(Integer value) {
//			this.value = value;
//		}
//
//		public Integer getValue() {
//			return value;
//		}
//	}
	
	public static enum OPEN_MERCHANT_TYPE {
		TYYD(1),IDM(2),IMUSIC(3),WOREAD(4),TYYD_LX(5),WOPLUS(6),YIXIN(7),LTSPACE(11),GNSPACE(12),ZYSPACE(13);

		private Integer value;

		OPEN_MERCHANT_TYPE(Integer value) {
			this.value = value;
		}

		public Integer getValue() {
			return value;
		}
	}
	
	/**
	 * 产品计费点类型
	 */
	public static enum PRODUCT_CHARGETYPE {

		DIANBO(1),PACKAGE(2);

		private Integer value;

		PRODUCT_CHARGETYPE(Integer value) {
			this.value = value;
		}

		public Integer getValue() {
			return value;
		}
	}
	
	/**
	 * sms订单产品类型
	 */
	public static enum T_SMS_ORDER_PRODUCT_TYPE {

		DIANBO(1),PACKAGE(2);

		private Integer value;

		T_SMS_ORDER_PRODUCT_TYPE(Integer value) {
			this.value = value;
		}

		public Integer getValue() {
			return value;
		}
	}
}
