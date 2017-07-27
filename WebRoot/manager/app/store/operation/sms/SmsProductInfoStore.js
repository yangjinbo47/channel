/**
 * 开放平台产品管理store
 */
Ext.define('CMS.store.operation.sms.SmsProductInfoStore',{
	extend : 'Ext.data.Store',
	model : 'CMS.model.operation.sms.SmsProductInfoModel',
	autoLoad : true,
	pageSize : pageSize,
    proxy : {
		type : 'ajax',
		url : '../operation/smsProductInfo_list.action',
		reader : {
			type : 'json',
			total : 'total',
			root : 'productInfos'
		},
		actionMethods:{
			read:'POST'
		}
	}
});