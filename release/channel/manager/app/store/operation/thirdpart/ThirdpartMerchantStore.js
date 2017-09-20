/**
 * 开放平台商户管理store
 */
Ext.define('CMS.store.operation.thirdpart.ThirdpartMerchantStore',{
	extend : 'Ext.data.Store',
	model : 'CMS.model.operation.thirdpart.ThirdpartMerchantModel',
	autoLoad : true,
	pageSize : pageSize,
    proxy : {
		type : 'ajax',
		url : '../operation/thirdpartMerchant_list.action',
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