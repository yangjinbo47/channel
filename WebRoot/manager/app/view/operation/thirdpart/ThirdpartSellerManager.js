Ext.define('CMS.view.operation.thirdpart.ThirdpartSellerManager', {
	extend : 'Ext.grid.Panel',
	xtype : 'thirdpartSellerManagerView',
	store : 'CMS.store.operation.thirdpart.ThirdpartSellerStore',
	layout : 'fit',
	columnLines : true,
	border : false,
	plugins : [{
		ptype : 'rowediting',
		pluginId : 'thirdpartSellerManager-rowediting',
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
		header : '渠道名',
		dataIndex : 'name',
		flex : 2,
		editor : {
			xtype : 'textfield',
			allowBlank : false,
			selectOnFocus : true
		}
	},
//	{
//		header : '邮箱',
//		dataIndex : 'email',
//		flex : 1,
//		renderer : function(value, metadata) {
//            metadata.tdAttr = 'data-qtip="' + value + '"';
//            return value;
//        },
//		editor : {
//			xtype : 'textfield',
//			selectOnFocus : true
//		}
//	},{
//		header : '联系人',
//		dataIndex : 'contact',
//		flex : 1,
//		editor : {
//			xtype : 'textfield',
//			selectOnFocus : true
//		}
//	},{
//		header : '联系电话',
//		dataIndex : 'telephone',
//		flex : 1,
//		editor : {
//			xtype : 'textfield',
//			selectOnFocus : true
//		}
//	},
	{
		header : '渠道KEY',
		dataIndex : 'sellerKey',
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
	},{
		header : '渠道SECRET',
		dataIndex : 'sellerSecret',
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
	},{
		header : '通知渠道方地址',
		dataIndex : 'callbackUrl',
		flex : 2,
		renderer : function(value, metadata) {
            metadata.tdAttr = 'data-qtip="' + value + '"';
            return value;
        },
		editor : {
			xtype : 'textfield',
//			allowBlank : false,
			selectOnFocus : true
		}
	},{
		header : '状态',
		dataIndex : 'status',
		width : 100,
		editor : {
			xtype : 'combobox',
	    	store : Ext.create('Ext.data.Store', {
			    fields: ['id', 'name'],
			    data : [
			        {"id":1, "name":"正常"},
			        {"id":0, "name":"冻结"}
			    ]
			}),
	    	displayField : 'name',
	    	valueField : 'id',
	    	name: 'status',
	    	queryMode: 'local',
	    	editable : false,
	    	allowBlank : false
		},
		renderer : function(value) {
			if(value == '1'){
				return '正常';
			} else {
				return '冻结';
			}
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
	},'-',{
		text : 'App关联',
		name : 'relation',
		iconCls : 'icon-switch'
	},'->',{
		xtype : 'displayfield',
		value : '渠道名：'
	},{
		xtype : 'textfield',
		name : 'sellerName',
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
		store : 'CMS.store.operation.thirdpart.ThirdpartSellerStore',
		displayInfo: true
	}]
});