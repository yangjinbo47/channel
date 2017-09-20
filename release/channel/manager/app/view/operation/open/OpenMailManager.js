Ext.define('CMS.view.operation.open.OpenMailManager', {
	extend : 'Ext.grid.Panel',
	xtype : 'openMailManagerView',
	store : 'CMS.store.operation.open.OpenMailgroupStore',
	layout : 'fit',
	columnLines : true,
	border : false,
	plugins : [{
		ptype : 'rowediting',
		pluginId : 'openMailgroupManager-rowediting',
        clicksToMoveEditor: 1,
        saveBtnText : '保存',
        cancelBtnText : '取消'
    }],
    selModel : new Ext.selection.CheckboxModel({
		mode : 'MULTI',
		showHeaderCheckbox : false
	}),
	initComponent : function(){
		var me = this;
		
		Ext.apply(this, {
			columns : [ {
				header : 'id',
				dataIndex : 'id',
				width : 50
			},{
				header : '邮件组名称',
				dataIndex : 'name',
				width : 300,
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
			},'-',{
				text : '组联系人',
				name : 'mailer',
				iconCls : 'icon-info'
			},'-',{
				text : '渠道配置',
				name : 'seller',
				iconCls : 'icon-info'
			},'->',{
				xtype : 'displayfield',
				value : '邮件组名称：'
			},{
				xtype : 'textfield',
				name : 'groupName',
				width : 150,
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
				store : 'CMS.store.operation.open.OpenMailgroupStore',
				displayInfo: true
			}]
		});
		this.callParent(arguments);
	}
});