Ext.define('CMS.view.account.RolesChoosenTreePanel', {
	extend : 'Ext.tree.Panel',
	xtype : 'rolesChoosenTreePanel',
	id : 'rolesChoosenTreePanel',
	requires : [ 'CMS.store.account.RolePrivilegeTreeStore' ],
	layout : 'fit',
	// expanded: true,
	// title : '选择权限',
	useArrows : true,
	border : false,
	rootVisible : false,
	checked : false,
	
	initComponent : function() {
		Ext.apply(this, {
			store : Ext.create('CMS.store.account.RolePrivilegeTreeStore')
		});
		this.callParent(arguments);
	}
});