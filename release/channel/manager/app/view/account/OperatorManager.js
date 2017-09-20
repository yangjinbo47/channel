Ext.define('CMS.view.account.OperatorManager', {
	extend : 'Ext.grid.Panel',
	xtype : 'operatorManager',
	layout : 'fit',
	columnLines : true,
	loadMask : true,
	selModel : new Ext.selection.CheckboxModel({
		mode : 'MULTI',
		showHeaderCheckbox : true
	}),
	store : 'CMS.store.account.OperatorStore',
	initComponent : function() {
		this.columns = [{
			text : '账号ID',
			dataIndex : 'operid',
			hidden : false
		},{
			text : '登陆账号',
			dataIndex : 'loginName',
			width : 200
		},{
			text : '用户名',
			dataIndex : 'name',
			width : 200
		},{
			text: '角色名',
			dataIndex: 'roleNames',
			width: 200
		},{
			text: '所属公司',
			dataIndex: 'type',
			width: 200,
			renderer : function(value) {
			if(value == '0'){
				return '所有可见';
			} else if (value == '1'){
				return '十分科技';
			} else if (value == '2'){
				return '乾坤汇智';
			} else if (value == '3'){
				return '安晴科技';
			}
		}
		},{
			text : '创建时间',
			dataIndex : 'createtime',
			flex : 1
		}]
		
		this.tbar = [ {
			text : '新增',
			iconCls : 'icon-add',
			name : 'add'
		}, '-', {
			text : '修改',
			iconCls : 'icon-edit',
			name : 'mod'
		}, '-', {
			text : '删除',
			iconCls : 'icon-delete',
			name : 'delete'
		},'->','帐号：', {
			xtype : 'textfield',
			name : 'u_name',
			id : 'u_name',
			emptyText : '请输入需要查询的账号',
			width:130,
			allowBlank : true
		}, {
			xtype : 'button',
			iconCls : 'icon-search',
			text : '查询',
			name : 'search'
		}, {
			xtype : 'button',
			text : '重置',
			iconCls : 'icon-reset',
			name : 'reset'
		} ]

		this.bbar = [{
			xtype : 'pagingtoolbar',
			store : 'CMS.store.account.OperatorStore',
			displayInfo: true,
			pageSize : pageSize
		}]
		
		this.callParent(arguments);
	}
});
