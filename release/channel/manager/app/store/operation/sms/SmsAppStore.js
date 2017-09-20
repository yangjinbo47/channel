/**
 * smsapp管理store
 */
Ext.define('CMS.store.operation.sms.SmsAppStore',{
	extend : 'Ext.data.Store',
	model : 'CMS.model.operation.sms.SmsAppModel',
	autoLoad : true,
	pageSize : pageSize,
    proxy : {
		type : 'ajax',
		url : '../operation/smsApp_list.action',
		reader : {
			type : 'json',
			total : 'total',
			root : 'apps'
		},
		actionMethods:{
			read:'POST'
		}
	}
});