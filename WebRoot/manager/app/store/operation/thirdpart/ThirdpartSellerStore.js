/**
 * 短信平台渠道管理store
 */
Ext.define('CMS.store.operation.thirdpart.ThirdpartSellerStore',{
	extend : 'Ext.data.Store',
	model : 'CMS.model.operation.thirdpart.ThirdpartSellerModel',
	autoLoad : true,
	pageSize : pageSize,
    proxy : {
		type : 'ajax',
		url : '../operation/thirdpartSeller_list.action',
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