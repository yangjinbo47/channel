Ext.define('CMS.model.account.RolePrivilegeModel', {
	extend : 'Ext.data.Model',
	defaults : {
		checked : false
	},
	fields : ['privilegeid','text','parentid','leaf','checked']
});