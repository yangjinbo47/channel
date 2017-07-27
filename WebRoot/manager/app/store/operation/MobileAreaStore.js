/**
 * 包月推送渠道store
 */
Ext.define('CMS.store.operation.MobileAreaStore',{
	extend : 'Ext.data.Store',
	model : 'CMS.model.operation.MobileAreaModel',
	autoLoad : true,
	pageSize : pageSize,
    proxy : {
		type : 'ajax',
		url : '../operation/mobileArea_list.action',
		reader : {
			type : 'json',
			total : 'total',
			root : 'mobileAreas'
		},
		actionMethods:{
			read:'POST'
		}
	}
});