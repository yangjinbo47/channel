/**
 * 开放平台渠道管理store
 */
Ext.define('CMS.store.operation.pack.PushSellerStore',{
	extend : 'Ext.data.Store',
	model : 'CMS.model.operation.pack.PushSellerModel',
	autoLoad : true,
	pageSize : pageSize,
    proxy : {
		type : 'ajax',
		url : '../operation/pushSeller_list.action',
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