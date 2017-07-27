/**
 * 权限菜单管理store
 */
Ext.define('CMS.store.account.PrivilegeStore',{
	extend : 'Ext.data.TreeStore',
	model : 'CMS.model.account.PrivilegeModel',
	root: {
		id : -1,
		text : '权限管理',
        expanded: true
    },
    nodeParam : 'privilegeid',
    proxy : {
		type : 'ajax',
		timeout : 60000 * 3,
		url : '../account/privilege_list.action',
		reader : {
			idProperty : 'privilegeid',
			type : 'json'
		},
		actionMethods:{read:'POST'}
	}
});