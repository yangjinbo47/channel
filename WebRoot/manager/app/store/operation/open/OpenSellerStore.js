/**
 * 开放平台渠道管理store
 */
Ext.define('CMS.store.operation.open.OpenSellerStore',{
	extend : 'Ext.data.Store',
	model : 'CMS.model.operation.open.OpenSellerModel',
	autoLoad : true,
	pageSize : pageSize,
    proxy : {
		type : 'ajax',
		url : '../operation/openSeller_list.action',
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