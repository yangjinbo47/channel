Ext.define('CMS.view.operation.open.OpenAppManager', {
	extend : 'Ext.grid.Panel',
	xtype : 'openAppManagerView',
	store : 'CMS.store.operation.open.OpenAppStore',
	layout : 'fit',
	columnLines : true,
	border : false,
	plugins : [{
		ptype : 'rowediting',
		pluginId : 'openAppManager-rowediting',
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
	},{
		header : 'app_key',
		dataIndex : 'appKey',
		flex : 1,
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
		header : 'app_secret',
		dataIndex : 'appSecret',
		flex : 1,
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
		header : 'client_id',
		dataIndex : 'clientId',
		flex : 1,
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
	},
//	{
//		header : '排除区域',
//		dataIndex : 'excludeArea',
//		flex : 1,
//		renderer : function(value, metadata) {
//            metadata.tdAttr = 'data-qtip="' + value + '"';
//            return value;
//        },
//        editor : {
//			xtype : 'combobox',
//	    	store : Ext.create('Ext.data.Store', {
//			    fields: ['name', 'value'],
//			    data : [
//			        {name:'河北', value:'河北'},
//			        {name:'山西', value:'山西'},
//			        {name:'辽宁', value:'辽宁'},
//			        {name:'吉林', value:'吉林'},
//			        {name:'黑龙江', value:'黑龙江'},
//			        {name:'江苏', value:'江苏'},
//			        {name:'浙江', value:'浙江'},
//			        {name:'安徽', value:'安徽'},
//			        {name:'福建', value:'福建'},
//			        {name:'江西', value:'江西'},
//			        {name:'山东', value:'山东'},
//			        {name:'河南', value:'河南'},
//			        {name:'湖北', value:'湖北'},
//			        {name:'湖南', value:'湖南'},
//			        {name:'广东', value:'广东'},
//			        {name:'海南', value:'海南'},
//			        {name:'四川', value:'四川'},
//			        {name:'贵州', value:'贵州'},
//			        {name:'云南', value:'云南'},
//			        {name:'陕西', value:'陕西'},
//			        {name:'甘肃', value:'甘肃'},
//			        {name:'青海', value:'青海'},
//			        {name:'内蒙古', value:'内蒙古'},
//			        {name:'广西', value:'广西'},
//			        {name:'西藏', value:'西藏'},
//			        {name:'宁夏', value:'宁夏'},
//			        {name:'新疆', value:'新疆'},
//			        {name:'北京', value:'北京'},
//			        {name:'天津', value:'天津'},
//			        {name:'上海', value:'上海'},
//			        {name:'重庆', value:'重庆'}
//			    ]
//			}),
//	    	displayField : 'name',
//	    	valueField : 'value',
//	    	name: 'excludeAreaArray',
//	    	queryMode: 'local',
//	    	editable : true,
//	    	multiSelect: true
//		}
//	},
	{
		xtype : "actioncolumn",
		text : "统计",
		width : 50,
        iconCls : 'icon-report'
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
		text : '产品关联',
		name : 'relation',
		iconCls : 'icon-switch'
	},'-',{
		text : '省份限量',
		name : 'limit',
		iconCls : 'icon-errorinfo'
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
		store : 'CMS.store.operation.open.OpenAppStore',
		displayInfo: true
	}]
});