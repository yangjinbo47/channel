/**
 * 开放平台商户管理store
 */
Ext.define('CMS.store.operation.sms.SmsMerchantStore',{
	extend : 'Ext.data.Store',
	model : 'CMS.model.operation.sms.SmsMerchantModel',
	autoLoad : true,
	pageSize : pageSize,
    proxy : {
		type : 'ajax',
		url : '../operation/smsMerchant_list.action',
		reader : {
			type : 'json',
			total : 'total',
			root : 'merchants'
		},
		actionMethods:{
			read:'POST'
		}
	}
});