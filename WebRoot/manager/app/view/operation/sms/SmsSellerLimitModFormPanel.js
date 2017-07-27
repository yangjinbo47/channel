Ext.define('CMS.view.operation.sms.SmsSellerLimitModFormPanel', {
	extend : 'Ext.grid.Panel',
	xtype : 'smsSellerLimitModForm',
	id : 'smsSellerLimitModForm',
	requires : ['CMS.model.operation.sms.SmsSellerLimitModel'],
	columnLines : true,
	layout : 'fit',
	border : false,
	loadMask : true,
	multiSelect : true,
	plugins : [{
		ptype : 'cellediting',
		pluginId : 'smsSellerLimitsManager-cellediting',
        clicksToEdit: 1
    }],
	initComponent : function() {
		this.store = Ext.create('Ext.data.Store', {
			model : 'CMS.model.operation.sms.SmsSellerLimitModel',
			proxy : {
				type : 'ajax',
				url : '../operation/smsSeller_limitlist.action',
				reader : {
					type : 'json',
					total : 'total',
					root : 'sellerLimits'
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
//			,{
//				text : '月限',
//				dataIndex : 'monthLimit',
//				flex : 1,
//				editor : {
//					xtype : 'numberfield',
//					selectOnFocus : true
//				}
//			}
			]
		});
		this.callParent(arguments);
	}
});
