Ext.define('CMS.view.operation.sms.SmsAppProductModFormPanel', {
	extend : 'Ext.form.Panel',
	xtype : 'smsAppProductModForm',
	requires : ['CMS.model.operation.sms.SmsProductInfoModel'],
	id : 'smsAppProductModForm',
	plain : true,
	border : 0,
	bodyPadding : 5,
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
		var pstore = this.productstore = Ext.create('Ext.data.Store', {
			model : 'CMS.model.operation.sms.SmsProductInfoModel',
			//autoLoad:true,
			proxy : {
				type : 'ajax',
				url : '../operation/smsApp_productsOfApp.action',
				reader : {
					type : 'json',
					total : 'total',
					root : 'productInfos'
				}
			}
		});
		
		this.items = [ {
			xtype : 'textfield',
			fieldLabel : '序号',
			name : 'appId',
			hidden : true
		}, {
			xtype : 'displayfield',
			fieldLabel : '应用名',
			readOnly : true,
			name : 'name',
			allowBlank : false
		}, {
			xtype : 'gridpanel',
			columnLines : true,
			loadMask : true,
			multiSelect : true,
			selModel : new Ext.selection.CheckboxModel,
			height: 290,
			store : pstore,
			columns : {
				items : [{
					text : '序号',
					dataIndex : 'id',
					hidden : true
				}, {
					text : '产品名',
					dataIndex : 'name',
					flex : 2
				}, {
					text : '价格',
					dataIndex : 'price',
					flex : 1
				}]
			},
			tbar : [ {
				text : '添加产品',
				iconCls : 'icon-add',
				name : 'add'
			}, {
				text : '删除产品',
				iconCls : 'icon-delete',
				name : 'delete'
			} ]
		}]
		
		this.callParent(arguments);
	}
});