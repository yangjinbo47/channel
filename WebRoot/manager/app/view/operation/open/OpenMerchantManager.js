Ext.define('CMS.view.operation.open.OpenMerchantManager', {
	extend : 'Ext.grid.Panel',
	xtype : 'openMerchantManagerView',
	store : 'CMS.store.operation.open.OpenMerchantStore',
	layout : 'fit',
	columnLines : true,
	border : false,
	plugins : [{
		ptype : 'rowediting',
		pluginId : 'openMerchantManager-rowediting',
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
		header : '商户名',
		dataIndex : 'merchantName',
		flex : 2,
		editor : {
			xtype : 'textfield',
			allowBlank : false,
			selectOnFocus : true
		}
	},{
		header : '邮箱',
		dataIndex : 'email',
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
		header : '联系人',
		dataIndex : 'contact',
		flex : 1,
		editor : {
			xtype : 'textfield',
//			allowBlank : false,
			selectOnFocus : true
		}
	},{
		header : '联系电话',
		dataIndex : 'telephone',
		flex : 1,
		editor : {
			xtype : 'textfield',
//			allowBlank : false,
			selectOnFocus : true
		}
	},{
		header : '接入类型',
		dataIndex : 'joinType',
		flex : 2,
		editor : {
			xtype : 'combobox',
	    	store : Ext.create('Ext.data.Store', {
			    fields: ['id', 'name'],
			    data : [
			        {"id":1, "name":"天翼阅读"},
			        {"id":2, "name":"爱动漫"},
			        {"id":3, "name":"爱音乐"},
			        {"id":4, "name":"wo阅读"},
			        {"id":5, "name":"天翼阅读-离线"},
			        {"id":6, "name":"wo+"},
			        {"id":7, "name":"易信"},
			        {"id":11, "name":"天翼空间-朗天"},
			        {"id":12, "name":"天翼空间-通用"},
			        {"id":13, "name":"天翼空间-旭游"},
			        {"id":14, "name":"咪咕动漫wap"},
			        {"id":15, "name":"MM网页支付"},
			        {"id":20, "name":"联通小额支付"}
			    ]
			}),
	    	displayField : 'name',
	    	valueField : 'id',
	    	name: 'joinType',
	    	queryMode: 'local',
	    	editable : false,
	    	allowBlank : false
		},
		renderer : function(value) {
			if(value == '1'){
				return '天翼阅读';
			} else if (value == '2'){
				return '爱动漫';
			} else if (value == '3'){
				return '爱音乐';
			} else if (value == '4'){
				return 'wo阅读';
			} else if (value == '5'){
				return '天翼阅读-离线';
			} else if (value == '6'){
				return 'wo+';
			} else if (value == '7'){
				return '易信';
			} else if (value == '11'){
				return '天翼空间-朗天';
			} else if (value == '12'){
				return '天翼空间-通用';
			} else if (value == '13'){
				return '天翼空间-旭游';
			} else if (value == '14'){
				return '咪咕动漫wap';
			} else if (value == '15'){
				return 'MM网页支付';
			} else if (value == '20'){
				return '联通小额支付';
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
	},'->',{
		xtype : 'displayfield',
		value : '商户名：'
	},{
		xtype : 'textfield',
		name : 'merchantName',
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
		store : 'CMS.store.operation.open.OpenMerchantStore',
		displayInfo: true
	}]
});