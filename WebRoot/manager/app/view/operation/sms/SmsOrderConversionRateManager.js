Ext.define('CMS.view.operation.sms.SmsOrderConversionRateManager', {
	extend : 'Ext.panel.Panel',
	xtype : 'smsOrderConversionRateView',
	layout : 'fit',
	
	initComponent : function() {
		this.callParent(arguments);
		
		this.chart = Ext.create('Ext.chart.Chart', {
			name : 'chart',
			animate : true,
			shadow: true,
			store : 'CMS.store.operation.sms.SmsOrderConversionRateStore',
			axes : [{
						type : 'Numeric',
						position : 'left',
						fields : ['rate','rateReq'],
						title : '转化率',
						minimum: 0,
						grid : true
					}, {
						type : 'Category',
						position : 'bottom',
						fields : ['showHour'],
						title : '时间段'
					}],
			series : [{
						type : 'line',
						axis : 'left',
						smooth : true,
						fill : true,
						fillOpacity : 0.5,
						gutter: 80,
						xField : 'showHour',
						yField : 'rate',
						tips : {
							trackMouse : true,
							width : 140,
							height : 50,
							renderer: function(storeItem, item) {
								this.setTitle('MO:' + storeItem.get('mo') + '<br/>'+'MR:' + storeItem.get('mr') + '<br/>' + '上行转化率:' + storeItem.get('rate') + '%'+'<br/>');
							}
						}
					},{
						type : 'line',
						axis : 'left',
						smooth : true,
						fill : true,
						fillOpacity : 0.5,
						gutter: 80,
						xField : 'showHour',
						yField : 'rateReq',
						tips : {
							trackMouse : true,
							width : 140,
							height : 50,
							renderer: function(storeItem, item) {
								this.setTitle('请求数:' + storeItem.get('orderReq') + '<br/>' + 'MR:' + storeItem.get('mr') + '<br/>' + '请求转化率:' + storeItem.get('rateReq') + '%');
							}
						}
					}]
		});
		
		this.add(this.chart);
	},
	
	tbar: [{
		xtype : 'combobox',
		name : 'sellerId',
		labelWidth : 80,
		width : 400,
		fieldLabel : '渠道选择',
		store : 'CMS.store.operation.sms.SmsSellerStore',
		mode: 'local',
		displayField :'name',
		valueField : 'id',
		emptyText : '请选择...',
		editable:false
	}
//	,{
//        text : 'Reload Data',
//        name : 'reload',
//        iconCls : 'icon-tablerefresh'
//    }
    ]
	
});