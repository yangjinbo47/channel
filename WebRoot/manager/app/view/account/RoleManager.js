Ext.define('CMS.view.account.RoleManager', {
	extend : 'Ext.grid.Panel',
	xtype : 'roleManager',
	columnLines : true,
	layout : 'fit',
	border : false,
	loadMask : true,
	selModel : new Ext.selection.CheckboxModel({
		mode : 'MULTI',
		showHeaderCheckbox : true
	}),
	store : 'CMS.store.account.RoleStore',
	
	initComponent : function() {
		this.columns = [ {
			text : '序号',
			dataIndex : 'roleid',
			hidden : false
		}, {
			text : '角色名',
			dataIndex : 'name',
			width : 200
		},{
			text : '角色说明',
			dataIndex : 'remark',
			width : 200
		}, {
			text : '创建人',
			dataIndex : 'createoperid',
			width : 200
		}, {
			text : '创建时间',
			dataIndex : 'createtime',
			width : 200
		} ]
		
		this.tbar = [ {
			text : '新增',
			iconCls : 'icon-add',
			name : 'add',
			scope : this
		}, '-', {
			text : '修改',
			iconCls : 'icon-edit',
			name : 'mod',
			scope : this
		}, '-', {
			text : '删除',
			iconCls : 'icon-delete',
			name : 'delete',
			scope : this
		},'->', '角色名称：',{
			xtype : 'textfield',
			name : 'r_name',
			id : 'r_name',
			width:130,
			emptyText : '请输入需要查询的角色名称',
			allowBlank : true
		}, {
			iconCls : 'icon-search',
			text : '查询',
			name : 'search'
		}, {
			iconCls : 'icon-reset',
			text : '重置',
			name : 'reset'
		} ]

		this.bbar = [{
			xtype : 'pagingtoolbar',
			store : 'CMS.store.account.RoleStore',
			displayInfo : true,
			inputItemWidth : 40,
			pageSize : pageSize
		}]
		
		this.callParent(arguments);
	}
	
});
