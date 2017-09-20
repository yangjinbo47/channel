Ext.define('CMS.view.operation.sms.SmsAppLimitModFormPanel', {
	extend : 'Ext.grid.Panel',
	xtype : 'smsAppLimitModForm',
	id : 'smsAppLimitModForm',
	requires : ['CMS.model.operation.sms.SmsAppLimitModel'],
	columnLines : true,
	layout : 'fit',
	border : false,
	loadMask : true,
	multiSelect : true,
	plugins : [{
		ptype : 'cellediting',
		pluginId : 'smsAppLimitsManager-cellediting',
        clicksToEdit: 1
    }],
	initComponent : function() {
		this.store = Ext.create('Ext.data.Store', {
			model : 'CMS.model.operation.sms.SmsAppLimitModel',
			proxy : {
				type : 'ajax',
				url : '../operation/smsApp_limitlist.action',
				reader : {
					type : 'json',
					total : 'total',
					root : 'appLimits'
				}
			}
		});
		Ext.apply(this, {
			selModel : new Ext.selection.CheckboxModel,
			columns : [ {
				text : '省份',
				dataIndex : 'province',
				flex : 1
			}, {
				text : '日限',
				dataIndex : 'dayLimit',
				flex : 1,
				editor : {
					xtype : 'numberfield',
					selectOnFocus : true
				}
			}
			,{
				text : '月限',
				dataIndex : 'monthLimit',
				flex : 1,
				editor : {
					xtype : 'numberfield',
					selectOnFocus : true
				}
			},{
				text : '扣量(%)',
				dataIndex : 'reduce',
				flex : 1,
				editor : {
					xtype : 'numberfield',
					selectOnFocus : true
				}
			}
			]
		});
		this.callParent(arguments);
	}
});
