/**
 * 包月推送store
 */
Ext.define('CMS.store.operation.pack.PushPackageStore',{
	extend : 'Ext.data.Store',
	model : 'CMS.model.operation.pack.PackageModel',
	autoLoad : true,
	pageSize : pageSize,
    proxy : {
		type : 'ajax',
		url : '../operation/pushPackage_list.action',
		reader : {
			type : 'json',
			total : 'total',
			root : 'packages'
		},
		actionMethods:{
			read:'POST'
		}
	}
});