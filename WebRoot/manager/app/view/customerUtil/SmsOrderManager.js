Ext.define('CMS.view.customerUtil.SmsOrderManager', {
	extend : 'Ext.grid.Panel',
	xtype : 'customerUtilSmsOrderManagerView',
	store : 'CMS.store.customerUtil.SmsOrderStore',
	layout : 'fit',
	columnLines : true,
	border : false,
    selModel : new Ext.selection.CheckboxModel({
		mode : 'single',
		showHeaderCheckbox : false
	}),
	columns : [
//	{
// 		header : '订单号',
//		dataIndex : 'orderId',
//		flex : 2
// 	},{
//		header : '渠道订单号',
//		dataIndex : 'outTradeNo',
//		flex : 3,
//		renderer : function(value, metadata) {
//            metadata.tdAttr = 'data-qtip="' + value + '"';
//            return value;
//        }
//	},
	{
		header : 'IMSI',
		dataIndex : 'imsi',
		flex : 2
	},{
		header : '渠道名',
		dataIndex : 'sellerName',
		flex : 2
	},{
		header : '应用名',
		dataIndex : 'subject',
		flex : 2
	},{
		header : '价格',
		dataIndex : 'fee',
		flex : 1
	},{
		header : '创建时间',
		dataIndex : 'createTime',
		flex : 2
	},{
		header : '状态',
		dataIndex : 'status',
		flex : 1,
		renderer : function(value) {
			if(value == '1'){
				return '未支付';
			} else if(value == '3'){
				return '成功';
			} else if(value == '4'){
				return '失败';
			}
		}
	},{
		header : '支付号码',
		dataIndex : 'payPhone',
		flex : 2
	},{
		header : '省份',
		dataIndex : 'province',
		flex : 1
	},{
		header : '支付时间',
		dataIndex : 'payTime',
		flex : 2
	}],
	tbar : ['->',{
		xtype : 'displayfield',
		value : '号码：'
	},{
		xtype : 'textfield',
		name : 'payPhone',
		width : 200,
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
		store : 'CMS.store.customerUtil.SmsOrderStore',
		displayInfo: true
	}]
});