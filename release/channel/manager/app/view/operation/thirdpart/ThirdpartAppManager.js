Ext.define('CMS.view.operation.thirdpart.ThirdpartAppManager', {
	extend : 'Ext.grid.Panel',
	xtype : 'thirdpartAppManagerView',
	store : 'CMS.store.operation.thirdpart.ThirdpartAppStore',
	layout : 'fit',
	columnLines : true,
	border : false,
	plugins : [{
		ptype : 'rowediting',
		pluginId : 'thirdpartAppManager-rowediting',
        clicksToMoveEditor: 1,
        saveBtnText : '保存',
        cancelBtnText : '取消'
    }],
    selModel : new Ext.selection.CheckboxModel({
		mode : 'single',
		showHeaderCheckbox : false
	}),
	columns : [{
		header : 'ID',
		dataIndex : 'id',
		width : 50
	},{
		header : '应用名',
		dataIndex : 'name',
		flex : 1,
		editor : {
			xtype : 'textfield',
			allowBlank : false,
			selectOnFocus : true
		}
	},{
		header : '所属商户',
		dataIndex : 'merchantShowName',
		flex : 2,
		renderer : function(value, metadata) {
            metadata.tdAttr = 'data-qtip="' + value + '"';
            return value;
        },
		editor : {
			xtype : 'combobox',
	    	store : 'CMS.store.operation.thirdpart.ThirdpartMerchantStore',
	    	displayField : 'merchantShowName',
	    	valueField : 'id',
	    	name: 'merchantId',
	    	queryMode: 'remote',
	    	editable : false,
	    	allowBlank : false
		}
	},{
		header : 'third_app_id',
		dataIndex : 'thirdAppId',
		flex : 1,
		renderer : function(value, metadata) {
            metadata.tdAttr = 'data-qtip="' + value + '"';
            return value;
        },
		editor : {
			xtype : 'textfield',
			allowBlank : true,
			selectOnFocus : true
		}
	},{
		header : 'third_app_mch',
		dataIndex : 'thirdAppMch',
		flex : 1,
		renderer : function(value, metadata) {
            metadata.tdAttr = 'data-qtip="' + value + '"';
            return value;
        },
		editor : {
			xtype : 'textfield',
			allowBlank : true,
			selectOnFocus : true
		}
	},{
		header : 'third_app_secret',
		dataIndex : 'thirdAppSecret',
		flex : 1,
		renderer : function(value, metadata) {
            metadata.tdAttr = 'data-qtip="' + value + '"';
            return value;
        },
		editor : {
			xtype : 'textfield',
			allowBlank : true,
			selectOnFocus : true
		}
	},{
		header : '回调地址',
		dataIndex : 'callbackUrl',
		flex : 2,
		renderer : function(value, metadata) {
            metadata.tdAttr = 'data-qtip="' + value + '"';
            return value;
        },
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
		value : '应用名：'
	},{
		xtype : 'textfield',
		name : 'appName',
		width : 200,
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
		store : 'CMS.store.operation.thirdpart.ThirdpartAppStore',
		displayInfo: true
	}]
});