Ext.define('CMS.view.operation.MobileAreaManager', {
	extend : 'Ext.grid.Panel',
	xtype : 'mobileAreaManagerView',
	store : 'CMS.store.operation.MobileAreaStore',
	layout : 'fit',
	columnLines : true,
	border : false,
	plugins : [{
		ptype : 'rowediting',
		pluginId : 'mobileAreaManager-rowediting',
        clicksToMoveEditor: 1,
        saveBtnText : '保存',
        cancelBtnText : '取消'
    }],
    selModel : new Ext.selection.CheckboxModel({
		mode : 'single',
		showHeaderCheckbox : false
	}),
	columns : [{
		header : 'first_num',
		dataIndex : 'firstNum',
		width : 100,
		editor : {
			xtype : 'textfield',
			allowBlank : false,
			selectOnFocus : true
		}
	},{
		header : 'middle_num',
		dataIndex : 'middleNum',
		width : 100,
		editor : {
			xtype : 'textfield',
			allowBlank : false,
			selectOnFocus : true
		}
	},{
		header : '地址',
		dataIndex : 'address',
		width : 150,
		editor : {
			xtype : 'textfield',
			allowBlank : false,
			selectOnFocus : true
		}
	},{
		header : '省份',
		dataIndex : 'province',
		width : 150,
		editor : {
			xtype : 'textfield',
			allowBlank : false,
			selectOnFocus : true
		}
	},{
		header : '城市',
		dataIndex : 'city',
		width : 150,
		editor : {
			xtype : 'textfield',
			allowBlank : false,
			selectOnFocus : true
		}
	},{
		header : '运营商',
		dataIndex : 'brand',
		width : 150,
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
		value : 'first_num：'
	},{
		xtype : 'textfield',
		name : 'firstNum',
		width : 130,
		emptyText : '请输入'
	},{
		xtype : 'displayfield',
		value : 'middle_num：'
	},{
		xtype : 'textfield',
		name : 'middleNum',
		width : 130,
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
		store : 'CMS.store.operation.MobileAreaStore',
		displayInfo: true
	}]
});