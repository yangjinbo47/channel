Ext.define('CMS.controller.operation.open.OpenOrderReportController', {
	extend : 'Ext.app.Controller',
	views : ['CMS.view.operation.open.OpenOrderReportManager'],
	stores : ['CMS.store.operation.open.OpenOrderReportStore'],
	refs : [{
		ref : 'openOrderReportView',
		selector : 'openOrderReportView'
	}],
	init : function() {
		this.control({
			'openOrderReportView > toolbar > button[name=report]' : {//开始统计按钮
				click : this.onReportClick
			},
			'openOrderReportView > toolbar > button[name=total]' : {//合计按钮
				click : this.onTotalClick
			},
			'openOrderReportView actioncolumn' : {//详细
				render : function(actioncolumn){
					actioncolumn.items[0].handler = Ext.bind(this.detail,this);
				}
			}
		});
	},
	
	onReportClick : function() {
		var me = this,grid = me.getOpenOrderReportView();
		var startTimeField = grid.down('toolbar > datetimefield[name=startTime]');
		var endTimeField = grid.down('toolbar > datetimefield[name=endTime]');
//		var startTimeHiddenField = grid.down('toolbar > hidden[name=startTime]');
//		var endTimeHiddenField = grid.down('toolbar > hidden[name=endTime]');
		
		if(startTimeField.getValue() == null) {
			Ext.tipbox.msg('提示', '请选择查询开始时间');
			return;
		}
		if(endTimeField.getValue() == null) {
			Ext.tipbox.msg('提示', '请选择查询结束时间');
			return;
		}
		if(startTimeField.lastActiveError.length != 0){
			return;
		}
		if(endTimeField.lastActiveError.length != 0){
			return;
		}
		
//		startTimeHiddenField.setValue(startTimeField.getValue());
//		endTimeHiddenField.setValue(endTimeField.getValue());
		
		var queryParams = {};
		queryParams.startTime = startTimeField.getValue();
		queryParams.endTime = endTimeField.getValue();
		
		grid.getStore().proxy.extraParams = queryParams;
//		grid.getStore().loadPage(1);
		grid.getStore().load({
			callback: function(records, options, success){
				//调用合并单元格方法
				var myArray = new Array();
				myArray[0] = 1;
				me.merge(grid, myArray, true);
			}
		});
	},
	
	merge : function(grid, colIndexArray, isAllSome) {
		isAllSome = isAllSome == undefined ? true : isAllSome; // 默认为true

		// 1.是否含有数据
		var gridView = document
				.getElementById(grid.getView().getId() + '-body');
		if (gridView == null) {
			return;
		}

		// 2.获取Grid的所有tr
		var trArray = [];
		if (grid.layout.type == 'table') { // 若是table部署方式，获取的tr方式如下
			trArray = gridView.childNodes;
		} else {
			trArray = gridView.getElementsByTagName('tr');
		}

		// 3.进行合并操作
		if (isAllSome) { // 3.1 全部列合并：只有相邻tr所指定的td都相同才会进行合并
			var lastTr = trArray[0]; // 指向第一行
			// 1)遍历grid的tr，从第二个数据行开始
			for (var i = 1, trLength = trArray.length; i < trLength; i++) {
				var thisTr = trArray[i];
				var isPass = true; // 是否验证通过
				// 2)遍历需要合并的列
				for (var j = 0, colArrayLength = colIndexArray.length; j < colArrayLength; j++) {
					var colIndex = colIndexArray[j];
					// 3)比较2个td的列是否匹配，若不匹配，就把last指向当前列
					if (lastTr.childNodes[colIndex].innerText != thisTr.childNodes[colIndex].innerText) {
						lastTr = thisTr;
						isPass = false;
						break;
					}
				}

				// 4)若colIndexArray验证通过，就把当前行合并到'合并行'
				if (isPass) {
					for (var j = 0, colArrayLength = colIndexArray.length; j < colArrayLength; j++) {
						var colIndex = colIndexArray[j];
						// 5)设置合并行的td rowspan属性
						if (lastTr.childNodes[colIndex].hasAttribute('rowspan')) {
							var rowspan = lastTr.childNodes[colIndex]
									.getAttribute('rowspan')
									- 0;
							rowspan++;
							lastTr.childNodes[colIndex].setAttribute('rowspan',
									rowspan);
						} else {
							lastTr.childNodes[colIndex].setAttribute('rowspan',
									'2');
						}
						// lastTr.childNodes[colIndex].style['text-align'] = 'center';; // 水平居中
						lastTr.childNodes[colIndex].style['vertical-align'] = 'middle';; // 纵向居中
						thisTr.childNodes[colIndex].style.display = 'none';
					}
				}
			}
		} else { // 3.2 逐个列合并：每个列在前面列合并的前提下可分别合并
			// 1)遍历列的序号数组
			for (var i = 0, colArrayLength = colIndexArray.length; i < colArrayLength; i++) {
				var colIndex = colIndexArray[i];
				var lastTr = trArray[0]; // 合并tr，默认为第一行数据
				// 2)遍历grid的tr，从第二个数据行开始
				for (var j = 1, trLength = trArray.length; j < trLength; j++) {
					var thisTr = trArray[j];
					// 3)2个tr的td内容一样
					if (lastTr.childNodes[colIndex].innerText == thisTr.childNodes[colIndex].innerText) {
						// 4)若前面的td未合并，后面的td都不进行合并操作
						if (i > 0
								&& thisTr.childNodes[colIndexArray[i - 1]].style.display != 'none') {
							lastTr = thisTr;
							continue;
						} else {
							// 5)符合条件合并td
							if (lastTr.childNodes[colIndex]
									.hasAttribute('rowspan')) {
								var rowspan = lastTr.childNodes[colIndex]
										.getAttribute('rowspan')
										- 0;
								rowspan++;
								lastTr.childNodes[colIndex].setAttribute(
										'rowspan', rowspan);
							} else {
								lastTr.childNodes[colIndex].setAttribute(
										'rowspan', '2');
							}
							// lastTr.childNodes[colIndex].style['text-align'] = 'center';; // 水平居中
							lastTr.childNodes[colIndex].style['vertical-align'] = 'middle';; // 纵向居中
							thisTr.childNodes[colIndex].style.display = 'none'; // 当前行隐藏
						}
					} else {
						// 5)2个tr的td内容不一样
						lastTr = thisTr;
					}
				}
			}
		}
	},
	
	detail : function(grid, rowIndex, colIndex) {
		var rec = grid.getStore().getAt(rowIndex);
		var sellerName = rec.get('sellerName');
		var sellerId = rec.get('sellerId');
		var appId = rec.get('appId');
		
		var me = this,gd = me.getOpenOrderReportView();
		var startTimeField = gd.down('toolbar > datetimefield[name=startTime]');
		var endTimeField = gd.down('toolbar > datetimefield[name=endTime]');
		
		var win = Ext.create('CMS.view.operation.open.OpenOrderReportDetailWindow');
		var panel = Ext.create('CMS.view.operation.open.OpenOrderReportDetailPanel');
		with(panel.form){
			panel.rstore.proxy.extraParams.sellerId = sellerId;
			panel.rstore.proxy.extraParams.appId = appId;
			panel.rstore.proxy.extraParams.startTime = startTimeField.getValue();
			panel.rstore.proxy.extraParams.endTime = endTimeField.getValue();
			panel.rstore.loadPage(1);
			
			findField('sellerName').setValue(sellerName);
		}
		win.add(panel);
		win.show();
	},
	
	onTotalClick : function() {
		var grid = this.getOpenOrderReportView();
		var sm = grid.getSelectionModel();
		var rs = sm.getSelection();
		if (!rs.length) {
			Ext.Msg.alert('提示', '请选择需要合计的渠道!');
			return;
		} else {
			var req = 0,succ = 0,fail = 0,noPay = 0,fee = 0,feeReduce = 0,users_num = 0,users_succ_num = 0;
			Ext.Array.each(rs, function(rec) {
				req += rec.get("req");
				succ += rec.get("succ");
				fail += rec.get("fail");
				noPay += rec.get("noPay");
				fee += rec.get("fee");
				feeReduce += rec.get("feeReduce");
				users_num += rec.get("users_num");
				users_succ_num += rec.get("users_succ_num");
			});
			
			var store = Ext.create('Ext.data.Store',{
				model : 'CMS.model.operation.open.OpenOrderReportModel',
				data : [{"sellerName":"合计","req":req,"succ":succ,"fail":fail,"noPay":noPay,"fee":fee,"feeReduce":feeReduce,"users_num":users_num,"users_succ_num":users_succ_num}]
			});
			var win = Ext.create('Ext.window.Window',{
				id:'openOrderReportTotalWin',
				title:'合计',
				height:200,
	      		width:900
			});
			var panel = Ext.create('Ext.grid.Panel',{
				id : 'openOrderReportTotalPanel',
				layout : 'fit',
				store : store,
				columns : [{
					header : '名称',
					dataIndex : 'sellerName',
					flex : 1
				},{
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
				}]
			});
			win.add(panel);
			win.show();
		}
	}
	
});
