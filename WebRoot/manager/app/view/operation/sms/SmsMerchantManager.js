Ext.define('CMS.view.operation.sms.SmsMerchantManager', {
	extend : 'Ext.grid.Panel',
	xtype : 'smsMerchantManagerView',
	store : 'CMS.store.operation.sms.SmsMerchantStore',
	layout : 'fit',
	columnLines : true,
	border : false,
	plugins : [{
		ptype : 'rowediting',
		pluginId : 'smsMerchantManager-rowediting',
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
			        {"id":1, "name":"信元短代"},
			        {"id":2, "name":"爱音乐短代"},
			        {"id":3, "name":"联通在信"},
			        {"id":4, "name":"联通全网短信"},
			        {"id":5, "name":"天翼爱动漫"},
			        {"id":6, "name":"移动全网短信"}
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
				return '信元短代';
			} else if(value == '2'){
				return '爱音乐短代';
			} else if(value == '3'){
				return '联通在信';
			} else if(value == '4'){
				return '联通全网短信';
			} else if(value == '5'){
				return '天翼爱动漫';
			} else if(value == '6'){
				return '移动全网短信';
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
		store : 'CMS.store.operation.sms.SmsMerchantStore',
		displayInfo: true
	}]
});