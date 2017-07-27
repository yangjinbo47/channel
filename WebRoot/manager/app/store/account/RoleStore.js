Ext.define('CMS.store.account.RoleStore', {
	extend : 'Ext.data.Store',
	requires : [ 'Ext.data.reader.Json' ],
	model : 'CMS.model.account.RoleModel',
	autoLoad : true,
	proxy : {
		type : 'ajax',
		url : '../account/role_list.action',
		reader : {
			type : 'json',
			total : 'total',
			root : 'roles'
		}
	}
});
