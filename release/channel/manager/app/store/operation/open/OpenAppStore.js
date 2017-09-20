/**
 * 开放平台app管理store
 */
Ext.define('CMS.store.operation.open.OpenAppStore',{
	extend : 'Ext.data.Store',
	model : 'CMS.model.operation.open.OpenAppModel',
	autoLoad : true,
	pageSize : pageSize,
    proxy : {
		type : 'ajax',
		url : '../operation/openApp_list.action',
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