Ext.define('CMS.view.operation.pack.OrderReportManager', {
	extend : 'Ext.grid.Panel',
	xtype : 'orderReportView',
	store : 'CMS.store.operation.pack.OrderReportStore',
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
				text : '包名',
				dataIndex : 'packageName',
				flex : 2,
				renderer : function(value, metadata) {
		            metadata.tdAttr = 'data-qtip="' + value + '"';
		            return value;
		        }
			}, {
				text : '请求MO',
				dataIndex : 'mo',
				flex : 1
			}, {
				text : 'MO去重',
				dataIndex : 'moQc',
				flex : 1
			},  {
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
			},{
				xtype : "actioncolumn",
				text : "详细",
				width : 50,
		        iconCls : 'icon-report'
			}],
			tbar : ['起始时间',{
				xtype:'datetimefield',
				name : 'startTime',
				id : 'orderreport_starttime',
				allowBlank : false,
				format : 'Y-m-d',
				enableKeyEvents : true,
				validateOnBlur : true,
				validateOnChange : false,
				validator : function(){
					var rawValue = this.getRawValue(), endField = Ext.getCmp('orderreport_endtime'),endRawValue = endField.getRawValue();
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
				id : 'orderreport_endtime',
				allowBlank : false,
				format : 'Y-m-d',
				validateOnBlur : true,
				validateOnChange : false,
				validator : function(){
					var rawValue = this.getRawValue(), startField = Ext.getCmp('orderreport_starttime'),startRawValue = startField.getRawValue();
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
				xtype : 'hidden',
				name : 'startTime'
			},{
				xtype : 'hidden',
				name : 'endTime'
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