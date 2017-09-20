Ext.define('CMS.view.operation.sms.SmsOrderReportDetailPanel', {
	extend : 'Ext.form.Panel',
	xtype : 'smsOrderReportDetailPanel',
	id : 'smsOrderReportDetailPanel',
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
			fields: ['province','req','succ','succReduce','fail','noPay','fee','feeReduce','users_num','users_succ_num','rate','reqRate'],
//			autoLoad : true,
			proxy : {
				type : 'ajax',
				url : '../operation/smsOrder_provinceDetail.action',
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
				text : '下单数',
				dataIndex : 'req',
				flex : 1
			}, {
				text : '成功数',
				dataIndex : 'succ',
				flex : 1
			}, {
				text : '失败数',
				dataIndex : 'fail',
				flex : 1
			}, {
				text : '未支付',
				dataIndex : 'noPay',
				flex : 1
			}, {
				text : '金额',
				dataIndex : 'fee',
				flex : 1
			}, {
				text : '用户数',
				dataIndex : 'users_num',
				flex : 1
			}, {
				text : '成功用户数',
				dataIndex : 'users_succ_num',
				flex : 1
			}, {
				text : '上行转化率',
				dataIndex : 'rate',
				flex : 1
			}, {
				text : '请求转化率',
				dataIndex : 'reqRate',
				flex : 1
			}]
		}]
		
		this.callParent(arguments);
	}
});
