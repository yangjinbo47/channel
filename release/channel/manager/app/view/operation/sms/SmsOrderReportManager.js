Ext.define('CMS.view.operation.sms.SmsOrderReportManager', {
	extend : 'Ext.grid.Panel',
	xtype : 'smsOrderReportView',
	store : 'CMS.store.operation.sms.SmsOrderReportStore',
	requires : ['Ext.ux.form.DateTimeField'],
	layout : 'fit',
	columnLines : true,
	border : false,
    selModel : new Ext.selection.CheckboxModel({
		mode : 'MULTI',
		showHeaderCheckbox : false
	}),
	initComponent : function(){
		
		var me = this;
		
		Ext.apply(this, {
			columns : [ {
				text : '渠道名',
				dataIndex : 'sellerName',
				flex : 2,
				renderer : function(value, metadata) {
		            metadata.tdAttr = 'data-qtip="' + value + '"';
		            return value;
		        }
			}, {
				text : '应用名',
				dataIndex : 'appName',
				flex : 2,
				renderer : function(value, metadata) {
		            metadata.tdAttr = 'data-qtip="' + value + '"';
		            return value;
		        }
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
			},{
				xtype : "actioncolumn",
				text : "详细",
				width : 50,
		        iconCls : 'icon-report'
			}],
			tbar : ['起始时间',{
				xtype:'datetimefield',
				name : 'startTime',
				id : 'smsorderreport_starttime',
				allowBlank : false,
				format : 'Y-m-d',
				enableKeyEvents : true,
				validateOnBlur : true,
				validateOnChange : false,
				validator : function(){
					var rawValue = this.getRawValue(), endField = Ext.getCmp('smsorderreport_endtime'),endRawValue = endField.getRawValue();
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
				id : 'smsorderreport_endtime',
				allowBlank : false,
				format : 'Y-m-d',
				validateOnBlur : true,
				validateOnChange : false,
				validator : function(){
					var rawValue = this.getRawValue(), startField = Ext.getCmp('smsorderreport_starttime'),startRawValue = startField.getRawValue();
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
				iconCls : 'icon-showall',
				name : 'total',
				text : '合计'
			}]
//			,bbar : [{
//				xtype : 'pagingtoolbar',
//				store : 'CMS.store.operation.open.OpenSellerReportStore',
//				displayInfo: true
//			}]
		});
		this.callParent(arguments);
	}
});