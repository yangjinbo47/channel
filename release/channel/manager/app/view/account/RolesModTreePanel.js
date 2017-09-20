Ext.define('CMS.view.account.RolesModTreePanel', {
	extend : 'Ext.tree.Panel',
	xtype : 'rolesModTreePanel',
	id : 'rolesModTreePanel',
	// requires:['CMS.store.Privilege'],
	layout : 'fit',
	// expanded: true,
	// title : '选择权限',
	useArrows : true,
	border : false,
	rootVisible : false,
	checked : false
});