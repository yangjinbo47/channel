/**
 * smsapp管理store
 */
Ext.define('CMS.store.operation.thirdpart.ThirdpartAppStore',{
	extend : 'Ext.data.Store',
	model : 'CMS.model.operation.thirdpart.ThirdpartAppModel',
	autoLoad : true,
	pageSize : pageSize,
    proxy : {
		type : 'ajax',
		url : '../operation/thirdpartApp_list.action',
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