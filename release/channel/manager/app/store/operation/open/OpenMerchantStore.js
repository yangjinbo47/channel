/**
 * 开放平台商户管理store
 */
Ext.define('CMS.store.operation.open.OpenMerchantStore',{
	extend : 'Ext.data.Store',
	model : 'CMS.model.operation.open.OpenMerchantModel',
	autoLoad : true,
	pageSize : pageSize,
    proxy : {
		type : 'ajax',
		url : '../operation/openMerchant_list.action',
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