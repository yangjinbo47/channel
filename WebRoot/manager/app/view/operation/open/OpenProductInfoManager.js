Ext.define('CMS.view.operation.open.OpenProductInfoManager', {
	extend : 'Ext.grid.Panel',
	xtype : 'openProductInfoManagerView',
	store : 'CMS.store.operation.open.OpenProductInfoStore',
	layout : 'fit',
	columnLines : true,
	border : false,
	plugins : [{
		ptype : 'rowediting',
		pluginId : 'openProductInfoManager-rowediting',
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
		header : '产品名称',
		dataIndex : 'name',
		flex : 2,
		editor : {
			xtype : 'textfield',
			allowBlank : false,
			selectOnFocus : true
		}
	},{
		header : '价格',
		dataIndex : 'price',
		flex : 1,
		editor : {
			xtype : 'textfield',
			allowBlank : false,
			selectOnFocus : true
		}
	},{
		header : '接入号',
		dataIndex : 'code',
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
		header : '指令',
		dataIndex : 'instruction',
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
		header : '计费点ID',
		dataIndex : 'productId',
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
		header : '类型',
		dataIndex : 'type',
		flex : 1,
		editor : {
			xtype : 'combobox',
	    	store : Ext.create('Ext.data.Store', {
			    fields: ['id', 'name'],
			    data : [
			        {"id":1, "name":"点播"},
			        {"id":2, "name":"包月"}
			    ]
			}),
	    	displayField : 'name',
	    	valueField : 'id',
	    	name: 'type',
	    	queryMode: 'local',
	    	editable : false,
	    	allowBlank : false
		},
		renderer : function(value) {
			if(value == '1'){
				return '点播';
			} else if(value == '2'){
				return '包月';
			}
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
	    	store : 'CMS.store.operation.open.OpenMerchantStore',
	    	displayField : 'merchantShowName',
	    	valueField : 'id',
	    	name: 'merchantId',
	    	queryMode: 'remote',
	    	editable : false,
	    	allowBlank : false
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
		value : '产品名称：'
	},{
		xtype : 'textfield',
		name : 'productName',
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
		store : 'CMS.store.operation.open.OpenProductInfoStore',
		displayInfo: true
	}]
});