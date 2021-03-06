/**
 * 短信平台渠道管理store
 */
Ext.define('CMS.store.operation.sms.SmsSellerStore',{
	extend : 'Ext.data.Store',
	model : 'CMS.model.operation.sms.SmsSellerModel',
	autoLoad : true,
	pageSize : pageSize,
    proxy : {
		type : 'ajax',
		url : '../operation/smsSeller_list.action',
		reader : {
			type : 'json',
			total : 'total',
			root : 'sellers'
		},
		actionMethods:{
			read:'POST'
		}
	}
});