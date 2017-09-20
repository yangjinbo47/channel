Ext.define('CMS.view.operation.pack.OrderManager', {
	extend : 'Ext.panel.Panel',
	xtype : 'orderManagerView',
	layout : 'border',
	requires : ['Ext.ux.form.DateTimeField','Ext.grid.plugin.BufferedRenderer'],
	viewConfig: {
        trackOver: false
    },
    features:[{
        ftype: 'grouping',
        hideGroupedHeader: false
    }],
	initComponent : function(){
		var me = this;
		
		this.items = [{
	 		region : 'west',	//表示boder布局的位置 west：西边 center：中间 east：东边 north：北边 south：南边
	 		collapsible : false,//表示可以收缩 默认为false
	 		width : 250,
	 		title : '渠道名称',
	 		xtype : 'treepanel',
	 		rootVisible : false,
			hideHeaders : true,
			useArrows : true,
	 		store : 'CMS.store.operation.pack.PushPackageChannelTreeStore',
	 		columns : [{
                xtype: 'treecolumn',
                header : '版本号',
                hideable : false,
                dataIndex: 'name',
                flex : 1
            }]
	 	},{
	 		region : 'center',
	 		xtype : 'gridpanel',
	 		padding : '0 5 0 5',
	 		store : 'CMS.store.operation.pack.OrderStore',
		 	columns : [{
				header : '手机号',
				dataIndex : 'phoneNum',
				flex : 1
			},{
				header : '包月名',
				dataIndex : 'name',
				flex : 2
			},
//			{
//				header : '版本号',
//				dataIndex : 'channel',
//				flex : 2
//			},
			{
				header : '省份',
				dataIndex : 'province',
				flex : 1
			},{
				header : '时间',
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
					} else if(value == '5'){
						return '退订';
					}
				}
			}],
			tbar : ['起始时间',{
				xtype:'datetimefield',
				name : 'startTime',
				id : 'order_starttime',
				allowBlank : false,
				format : 'Y-m-d H:i:s',
				enableKeyEvents : true,
				validateOnBlur : true,
				validateOnChange : false,
				validator : function(){
					var rawValue = this.getRawValue(), endField = Ext.getCmp('order_endtime'),endRawValue = endField.getRawValue();
					if(Ext.isEmpty(rawValue) || Ext.isEmpty(endRawValue)){
						return true;
					}else{
						if(rawValue >= endRawValue){
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
				id : 'order_endtime',
				allowBlank : false,
				format : 'Y-m-d H:i:s',
				validateOnBlur : true,
				validateOnChange : false,
				validator : function(){
					var rawValue = this.getRawValue(), startField = Ext.getCmp('order_starttime'),startRawValue = startField.getRawValue();
					if(Ext.isEmpty(rawValue) || Ext.isEmpty(startRawValue)){
						return true;
					}else{
						if(rawValue <= startRawValue){
							return '结束日期必须大于开始日期';
						}else{
							startField.clearInvalid();
							return true;
						}
					}
				}
			},'->',{
				iconCls : 'icon-search',
				name : 'search',
				text : '查询'
			},{
				iconCls : 'icon-appgo',
				name : 'export',
				text : '导出'
			}]
	 	}];
	 	
	 	me.callParent(arguments);
	},
	renderTo: Ext.getBody()
});