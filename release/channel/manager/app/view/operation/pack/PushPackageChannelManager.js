Ext.define('CMS.view.operation.pack.PushPackageChannelManager', {
	extend : 'Ext.grid.Panel',
	xtype : 'pushPackageChannelManagerView',
	store : 'CMS.store.operation.pack.PushPackageChannelStore',
	layout : 'fit',
	columnLines : true,
	border : false,
	plugins : [{
		ptype : 'rowediting',
		pluginId : 'pushPackageChannelManager-rowediting',
        clicksToMoveEditor: 1,
        saveBtnText : '保存',
        cancelBtnText : '取消'
    }],
    selModel : new Ext.selection.CheckboxModel({
		mode : 'single',
		showHeaderCheckbox : false
	}),
	columns : [ {
		header : 'id',
		dataIndex : 'id',
		width : 50
	},{
		header : '渠道名称',
		dataIndex : 'channelName',
		width : 300,
		editor : {
			xtype : 'textfield',
			allowBlank : false,
			selectOnFocus : true
		}
	},{
		header : '版本名称',
		dataIndex : 'clientVersion',
		width : 300,
		editor : {
			xtype : 'textfield',
			allowBlank : false,
			selectOnFocus : true
		}
	},{
		header : '创建时间',
		dataIndex : 'createTime',
		width : 200
	},{
		header : '可见类型',
		dataIndex : 'companyShow',
		flex : 1,
		editor : {
			xtype : 'combobox',
	    	store : Ext.create('Ext.data.Store', {
			    fields: ['id', 'name'],
			    data : [
			        {"id":0, "name":"所有可见"},
			        {"id":1, "name":"十分可见"},
			        {"id":2, "name":"乾坤可见"},
			        {"id":3, "name":"安晴可见"}
			    ]
			}),
	    	displayField : 'name',
	    	valueField : 'id',
	    	name: 'companyShow',
	    	queryMode: 'local',
	    	editable : false,
	    	allowBlank : false
		},
		renderer : function(value) {
			if(value == 0){
				return '所有可见';
			} else if(value == 1){
				return '十分可见';
			} else if(value == 2){
				return '乾坤可见';
			} else if(value == 3){
				return '安晴可见';
			} else {
				return '其他';
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
		value : '渠道名：'
	},{
		xtype : 'textfield',
		name : 'packageName',
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
		store : 'CMS.store.operation.pack.PushPackageChannelStore',
		displayInfo: true
	}]
});