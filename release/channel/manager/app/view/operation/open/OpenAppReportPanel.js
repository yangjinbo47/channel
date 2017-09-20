Ext.define('CMS.view.operation.open.OpenAppReportPanel', {
	extend : 'Ext.form.Panel',
	xtype : 'openAppReportPanel',
	id : 'openAppReportPanel',
	layout : 'fit',
	requires : ['Ext.ux.form.DateTimeField'],
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
			fields: ['sellerName','req','succ','succReduce','fail','noPay','fee','feeReduce','users_num','users_succ_num','rate','reqRate'],
//			autoLoad : true,
			proxy : {
				type : 'ajax',
				url : '../operation/openApp_report.action',
				reader : {
					type : 'json',
					total : 'total',
					root : 'report'
				},
				timeout : 180000
			}
		});
		
		this.items = [ {
			xtype : 'textfield',
			fieldLabel : '序号',
			name : 'appId',
			hidden : true
		}, {
			xtype : 'displayfield',
			fieldLabel : '渠道名',
			readOnly : true,
			name : 'name',
			allowBlank : false
		}, {
			xtype : 'gridpanel',
			columnLines : true,
			loadMask : true,
			multiSelect : true,
			height: 430,
			store : reportstore,
			columns : [ {
				text : '渠道名',
				dataIndex : 'sellerName',
				flex : 2
			}, {
				text : '下单数',
				dataIndex : 'req',
				flex : 1
			}, {
				text : '成功数',
				dataIndex : 'succ',
				flex : 1
			}, {
				text : '成功数(扣后)',
				dataIndex : 'succReduce',
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
				text : '金额(扣后)',
				dataIndex : 'feeReduce',
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
			}],
			
			tbar : ['起始时间',{
				xtype:'datetimefield',
				name : 'startTime',
				id : 'openapp_starttime',
				allowBlank : false,
				format : 'Y-m-d',
				enableKeyEvents : true,
				validateOnBlur : true,
				validateOnChange : false,
				validator : function(){
					var rawValue = this.getRawValue(), endField = Ext.getCmp('openapp_endtime'),endRawValue = endField.getRawValue();
					if(Ext.isEmpty(rawValue) || Ext.isEmpty(endRawValue)){
						return true;
					}else{
						if(rawValue > endRawValue){
							return '开始日期必须小于结束日期';
						}else{
							endField.clearInvalid();
							return true;
						}
					}
				}
			},'结束时间',{
				xtype:'datetimefield',
				name : 'endTime',
				id : 'openapp_endtime',
				allowBlank : false,
				format : 'Y-m-d',
				validateOnBlur : true,
				validateOnChange : false,
				validator : function(){
					var rawValue = this.getRawValue(), startField = Ext.getCmp('openapp_starttime'),startRawValue = startField.getRawValue();
					if(Ext.isEmpty(rawValue) || Ext.isEmpty(startRawValue)){
						return true;
					}else{
						if(rawValue < startRawValue){
							return '结束日期必须大于开始日期';
						}else{
							startField.clearInvalid();
							return true;
						}
					}
				}
			},{
				xtype : 'button',
				text : '开始统计',
				name : 'report'
			},{
				xtype : 'button',
				text : '按省导出',
				name : 'export'
			}]
		}]
		
		this.callParent(arguments);
	}
});
