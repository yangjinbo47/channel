Ext.define('CMS.view.operation.pack.OrderReportDetailPanel', {
	extend : 'Ext.form.Panel',
	xtype : 'orderReportDetailPanel',
	id : 'orderReportDetailPanel',
	plain : true,
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
		
		var reportstore = this.rstore = Ext.create('Ext.data.Store', {
			fields: ['province','mo','moQc','mr','fee','zhl'],
//			autoLoad : true,
			proxy : {
				type : 'ajax',
				url : '../operation/order_provinceDetail.action',
				reader : {
					type : 'json',
					total : 'total',
					root : 'reports'
				}
			}
		});
		
		this.items = [{
			xtype : 'displayfield',
			fieldLabel : '渠道名',
			readOnly : true,
			name : 'sellerName',
			allowBlank : false
		}, {
			xtype : 'gridpanel',
			columnLines : true,
			loadMask : true,
			multiSelect : true,
			height: 430,
			store : reportstore,
			columns : [ {
				text : '省份',
				dataIndex : 'province',
				flex : 1
			}, {
				text : '请求MO',
				dataIndex : 'mo',
				flex : 1
			}, {
				text : 'MO去重',
				dataIndex : 'moQc',
				flex : 1
			}, {
				text : '成功MR',
				dataIndex : 'mr',
				flex : 1
			}, {
				text : '金额(元)',
				dataIndex : 'fee',
				flex : 1
			}, {
				text : '转化率',
				dataIndex : 'zhl',
				flex : 1
			}]
		}]
		
		this.callParent(arguments);
	}
});
