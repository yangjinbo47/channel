Ext.define('CMS.store.account.RolePrivilegeTreeStore', {
	extend : 'Ext.data.TreeStore',
	model : 'CMS.model.account.RolePrivilegeModel',
	root : {
		expanded : true,
		leaf : false,
		expandable : false,
		text : '根节点',
		id : 'root'
	},
	proxy : {
		type : 'ajax',
		url : '../account/role_privilegeList.action'
	}
});