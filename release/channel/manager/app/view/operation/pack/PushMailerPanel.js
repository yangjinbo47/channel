Ext.define('CMS.view.operation.pack.PushMailerPanel', {
	extend : 'Ext.form.Panel',
	xtype : 'pushMailerPanel',
	id : 'pushMailerPanel',
	requires : ['CMS.model.operation.pack.PushMailerModel'],
	layout : 'fit',
	fieldDefaults : {
		labelWidth : 50,
		anchor : '100%'
	},
	layout : {
		type : 'vbox',
		align : 'stretch'
	},
	initComponent : function() {
		var me = this;
		
		var mailerstore = this.mstore = Ext.create('Ext.data.Store', {
			model : 'CMS.model.operation.pack.PushMailerModel',
//			autoLoad : true,
			proxy : {
				type : 'ajax',
				url : '../operation/pushMail_mailerList.action',
				reader : {
					type : 'json',
					total : 'total',
					root : 'mailers'
				},
				timeout : 180000
			}
		});
		
		this.items = [ {
			xtype : 'textfield',
			fieldLabel : '邮件组ID号',
			name : 'groupId',
			hidden : true
		}, {
			xtype : 'displayfield',
			fieldLabel : '邮件组名称',
			labelWidth : 80,
			readOnly : true,
			name : 'name',
			allowBlank : false
		}, {
			xtype : 'gridpanel',
			columnLines : true,
			loadMask : true,
			multiSelect : true,
			height: 300,
			store : mailerstore,
			plugins : [{
				ptype : 'rowediting',
				pluginId : 'pushMailerPanel-rowediting',
		        clicksToMoveEditor: 1,
		        saveBtnText : '保存',
		        cancelBtnText : '取消'
		    }],
			selModel : new Ext.selection.CheckboxModel({
				mode : 'MULTI',
				showHeaderCheckbox : true
			}),
			columns : [ {
				text : 'ID',
				dataIndex : 'id',
				width : 50
			}, {
				text : '姓名',
				dataIndex : 'name',
				flex : 1,
				editor : {
					xtype : 'textfield',
					allowBlank : false,
					selectOnFocus : true
				}
			}, {
				text : '邮箱',
				dataIndex : 'email',
				flex : 2,
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
			}]
		}]
		
		this.callParent(arguments);
	}
});
