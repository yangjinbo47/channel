Ext.define('CMS.view.operation.pack.PushPackageManager', {
	extend : 'Ext.grid.Panel',
	xtype : 'pushPackageManagerView',
	store : 'CMS.store.operation.pack.PushPackageStore',
	layout : 'fit',
	columnLines : true,
	border : false,
	plugins : [{
		ptype : 'rowediting',
		pluginId : 'pushPackageManager-rowediting',
        clicksToMoveEditor: 1,
        saveBtnText : '保存',
        cancelBtnText : '取消'
    }],
    selModel : new Ext.selection.CheckboxModel({
		mode : 'single',
		showHeaderCheckbox : false
	}),
	initComponent : function(){
		Ext.apply(this, {
			columns : [ {
				header : 'id',
				dataIndex : 'id',
				width : 50
			},{
				header : '包月名称',
				dataIndex : 'packageName',
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
				header : '价格（分）',
				dataIndex : 'price',
				flex : 1,
				editor : {
					xtype : 'textfield',
					allowBlank : false,
					selectOnFocus : true
				}
			},{
				header : '每日限制',
				dataIndex : 'packageLimit',
				flex : 1,
				editor : {
					xtype : 'textfield',
					allowBlank : false,
					selectOnFocus : true
				}
			},{
				header : '今日包月',
				dataIndex : 'packageToday',
				flex : 1
			},{
				header : '入口地址',
				dataIndex : 'packageUrl',
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
				header : '通道类型',
				dataIndex : 'type',
				flex : 1,
				editor : {
					xtype : 'combobox',
			    	store : Ext.create('Ext.data.Store', {
					    fields: ['id', 'name'],
					    data : [
					        {"id":1, "name":"天翼阅读"},
					        {"id":2, "name":"爱游戏"}
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
						return '天翼阅读';
					} else if(value == '2') {
						return '爱游戏';
					}
				}
			},
//			{
//				header : '渠道名',
//				dataIndex : 'channelName',
//				flex : 1,
//				renderer : function(value, metadata) {
//		            metadata.tdAttr = 'data-qtip="' + value + '"';
//		            return value;
//		        },
//				editor : {
//					xtype : 'combobox',
//			    	store : 'CMS.store.operation.pack.PushPackageChannelStore',
//			    	displayField : 'channelName',
//			    	valueField : 'clientVersion',
//			    	name: 'recChannel',
//			    	queryMode: 'remote',
//			    	editable : false,
//			    	allowBlank : false
//				}
//			},
			{
				header : '状态',
				dataIndex : 'status',
				flex : 1,
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
			},{
				header : '创建时间',
				dataIndex : 'createtime',
				flex : 2
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
			},'-',{
				text : '省份限量',
				name : 'limit',
				iconCls : 'icon-errorinfo'
			},'->',{
				xtype : 'displayfield',
				value : '包月名：'
			},{
				xtype : 'textfield',
				name : 'packageName',
				width : 130,
				emptyText : '请输入'
			},{
				xtype : 'displayfield',
				value : '渠道名：'
			},{
				xtype : 'textfield',
				name : 'channelName',
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
				store : 'CMS.store.operation.pack.PushPackageStore',
				displayInfo: true
			}]
		});
		this.callParent(arguments);
	}
	
});