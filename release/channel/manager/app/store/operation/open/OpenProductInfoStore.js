/**
 * 开放平台产品管理store
 */
Ext.define('CMS.store.operation.open.OpenProductInfoStore',{
	extend : 'Ext.data.Store',
	model : 'CMS.model.operation.open.OpenProductInfoModel',
	autoLoad : true,
	pageSize : pageSize,
    proxy : {
		type : 'ajax',
		url : '../operation/openProductInfo_list.action',
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