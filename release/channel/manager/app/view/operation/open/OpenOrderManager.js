Ext.define('CMS.view.operation.open.OpenOrderManager', {
	extend : 'Ext.panel.Panel',
	xtype : 'openOrderManagerView',
	layout : 'border',
	requires : ['Ext.ux.form.DateTimeField'],
//	requires : ['Ext.ux.form.DateTimeField','Ext.grid.plugin.BufferedRenderer'],
//	viewConfig: {
//        trackOver: false
//    },
//    features:[{
//        ftype: 'grouping',
//        hideGroupedHeader: false
//    }],
	initComponent : function(){
		this.callParent(arguments);
		
		this.westPanel = Ext.create('CMS.extensions.FilterTreePanel',{
			region : 'west',	//表示boder布局的位置 west：西边 center：中间 east：东边 north：北边 south：南边
	 		collapsible : false,//表示可以收缩 默认为false
	 		width : 250,
	 		title : '渠道',
	 		rootVisible : false,
			hideHeaders : true,
			useArrows : true,
	 		store : 'CMS.store.operation.open.OpenSellerTreeStore',
	 		columns : [{
                xtype: 'treecolumn',
                header : '渠道名称',
                hideable : false,
                dataIndex: 'name',
                flex : 1,
                renderer : function(value, metadata) {
		            metadata.tdAttr = 'data-qtip="' + value + '"';
		            return value;
		        }
            }],
            tbar : [{
            	xtype: 'trigger',  
		        triggerCls: 'x-form-clear-trigger',
		        onTriggerClick: function () {  
		            this.setValue('');  
		            var panel=this.ownerCt.ownerCt;  
		            panel.clearFilter();  
		        },
		        width:'100%',  
		        emptyText:'请输入渠道名',
		        enableKeyEvents: true,
		        listeners: {
		            keyup: {
		            	fn: function (field, e) {
			                var panel=this.ownerCt.ownerCt;
		                    if (Ext.EventObject.ESC == e.getKey()) {
		                    	field.onTriggerClick();
		                    } else {
		                    	panel.filterBy(this.getRawValue(),'name');
		                    }
		                }
		            }
	        	}
            }]
		});
		
		this.centerPanel = Ext.create('Ext.grid.Panel',{
	 		region : 'center',
	 		padding : '0 5 0 5',
	 		store : 'CMS.store.operation.open.OpenOrderStore',
		 	columns : [{
		 		header : '订单号',
				dataIndex : 'orderId',
				flex : 2
		 	},{
				header : '渠道订单号',
				dataIndex : 'outTradeNo',
				flex : 3,
				renderer : function(value, metadata) {
		            metadata.tdAttr = 'data-qtip="' + value + '"';
		            return value;
		        }
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
					} else if(value == '5'){
						return '退订';
					} else if(value == '1001'){
						return '屏蔽';
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
				header : '是否扣量',
				dataIndex : 'reduce',
				flex : 1,
				renderer : function(value) {
					if(value == '0'){
						return '未扣量';
					} else if(value == '1'){
						return '<span style="color:#FF0000">已扣量</span>';
					}
				}
			}],
			tbar : ['起始时间',{
				xtype:'datetimefield',
				name : 'startTime',
				id : 'openorder_starttime',
				allowBlank : false,
				format : 'Y-m-d H:i:s',
				enableKeyEvents : true,
				validateOnBlur : true,
				validateOnChange : false,
				validator : function(){
					var rawValue = this.getRawValue(), endField = Ext.getCmp('openorder_endtime'),endRawValue = endField.getRawValue();
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
				id : 'openorder_endtime',
				allowBlank : false,
				format : 'Y-m-d H:i:s',
				validateOnBlur : true,
				validateOnChange : false,
				validator : function(){
					var rawValue = this.getRawValue(), startField = Ext.getCmp('openorder_starttime'),startRawValue = startField.getRawValue();
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
			},
			'->',{
				iconCls : 'icon-search',
				name : 'search',
				text : '查询'
			},'-',{
				iconCls : 'icon-appgo',
				name : 'export',
				text : '导出'
			}
//			,'-',{
//				text : '省份统计',
//				name : 'province',
//				iconCls : 'icon-paste'
//			}
			],
			bbar : [{
				xtype : 'pagingtoolbar',
				store : 'CMS.store.operation.open.OpenOrderStore',
				displayInfo: true
			}]
		});
		
		this.add(this.westPanel,this.centerPanel);
	 	
//	 	me.callParent(arguments);
	},
	renderTo: Ext.getBody()
});