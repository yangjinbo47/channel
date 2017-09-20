/**
 * 开放平台app管理store
 */
Ext.define('CMS.store.operation.open.OpenMailgroupStore',{
	extend : 'Ext.data.Store',
	model : 'CMS.model.operation.open.OpenMailgroupModel',
	autoLoad : true,
	pageSize : pageSize,
    proxy : {
		type : 'ajax',
		url : '../operation/openMail_groups.action',
		reader : {
			type : 'json',
			total : 'total',
			root : 'groups'
		},
		actionMethods:{
			read:'POST'
		}
	}
});