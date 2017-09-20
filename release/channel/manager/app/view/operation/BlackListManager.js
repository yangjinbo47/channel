Ext.define('CMS.view.operation.BlackListManager', {
	extend : 'Ext.grid.Panel',
	xtype : 'blackListManagerView',
	store : 'CMS.store.operation.BlackListStore',
	layout : 'fit',
	columnLines : true,
	border : false,
	plugins : [{
		ptype : 'rowediting',
		pluginId : 'blackListManager-rowediting',
        clicksToMoveEditor: 1,
        saveBtnText : '保存',
        cancelBtnText : '取消'
    }],
    selModel : new Ext.selection.CheckboxModel({
		mode : 'single',
		showHeaderCheckbox : false
	}),
	columns : [{
		header : '手机号码',
		dataIndex : 'phoneNum',
		width : 200,
		editor : {
			xtype : 'textfield',
			allowBlank : false,
			selectOnFocus : true
		}
	}],
	tbar : [{
		text : '新建',
		name : 'add',
		iconCls : 'icon-add'
	},'-',{
		text : '删除',
		name : 'delete',
		iconCls : 'icon-delete'
	},'->',{
		xtype : 'displayfield',
		value : '手机号码：'
	},{
		xtype : 'textfield',
		name : 'phoneNum',
		width : 150,
		emptyText : '请输入'
	},{
		iconCls : 'icon-search',
		name : 'search',
		text : '查询'
	},{
		iconCls : 'icon-reset',
		name : 'reset',
		text : '重置'
	}],
	bbar : [{
		xtype : 'pagingtoolbar',
		store : 'CMS.store.operation.BlackListStore',
		displayInfo: true
	}]
});