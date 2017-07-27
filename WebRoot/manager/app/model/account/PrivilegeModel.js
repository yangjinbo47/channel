/**
 * 权限菜单管理model
 */
Ext.define('CMS.model.account.PrivilegeModel',{
	extend : 'Ext.data.Model',
	fields : ['privilegeid','text','parentid','status','iconCls','xtype','leaf','menulevel','expanded','controller']
});