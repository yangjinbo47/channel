Ext.define('CMS.view.account.UserAddUnCheckedRolesGrid', {
	extend : 'Ext.grid.Panel',
	xtype : 'userAddUnCheckedRolesGrid',
	id : 'userAddUnCheckedRolesGrid',
	columnLines : true,
	layout : 'fit',
	border : false,
	loadMask : true,
	multiSelect : true,
	initComponent : function() {
		this.store = Ext.create('Ext.data.Store', {
			model : 'CMS.model.account.RoleModel',
//			autoLoad : true,
			proxy : {
				type : 'ajax',
				url : '../account/user_getUnallocateRole.action',
				reader : {
					type : 'json',
					total : 'total',
					root : 'roles'
				}
			}
		});
		Ext.apply(this, {
			selModel : new Ext.selection.CheckboxModel,
			columns : [ {
				text : '序号',
				dataIndex : 'roleid',
				hidden : false,
				editor : {
					xtype : 'textfield',
					readOnly : true,
					selectOnFocus : true
				}
			}, {
				text : '角色名',
				dataIndex : 'name',
				width : 200,
				editor : {
					xtype : 'textfield',
					readOnly : true,
					selectOnFocus : true
				}
			},
			// {text: '类型',dataIndex: 'type',flex: 1},
			{
				text : '角色说明',
				dataIndex : 'remark',
				editor : {
					xtype : 'textfield',
					readOnly : true,
					selectOnFocus : true
				},
				flex : 1
			} ]
		});
		this.callParent(arguments);
	}
});
