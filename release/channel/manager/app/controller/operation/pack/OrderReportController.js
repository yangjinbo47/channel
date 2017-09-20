Ext.define('CMS.controller.operation.pack.OrderReportController', {
	extend : 'Ext.app.Controller',
	views : ['CMS.view.operation.pack.OrderReportManager'],
	stores : ['CMS.store.operation.pack.OrderReportStore'],
	refs : [{
		ref : 'orderReportView',
		selector : 'orderReportView'
	}],
	init : function() {
		this.control({
			'orderReportView > toolbar > button[name=report]' : {
				click : this.onReportClick
			},
			'orderReportView actioncolumn' : {//统计按钮
				render : function(actioncolumn){
					actioncolumn.items[0].handler = Ext.bind(this.detail,this);
				}
			}
		});
	},
	
	onReportClick : function() {
		var me = this,grid = me.getOrderReportView();
		var startTimeField = grid.down('toolbar > datetimefield[name=startTime]');
		var endTimeField = grid.down('toolbar > datetimefield[name=endTime]');
		
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
		
		var queryParams = {};
		queryParams.startTime = startTimeField.getValue();
		queryParams.endTime = endTimeField.getValue();
		
		grid.getStore().proxy.extraParams = queryParams;
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
		var pushId = rec.get('pushId');
		
		var me = this,gd = me.getOrderReportView();
		var startTimeField = gd.down('toolbar > datetimefield[name=startTime]');
		var endTimeField = gd.down('toolbar > datetimefield[name=endTime]');
		
		var win = Ext.create('CMS.view.operation.pack.OrderReportDetailWindow');
		var panel = Ext.create('CMS.view.operation.pack.OrderReportDetailPanel');
		with(panel.form){
			panel.rstore.proxy.extraParams.sellerId = sellerId;
			panel.rstore.proxy.extraParams.pushId = pushId;
			panel.rstore.proxy.extraParams.startTime = startTimeField.getValue();
			panel.rstore.proxy.extraParams.endTime = endTimeField.getValue();
			panel.rstore.loadPage(1);
			
			findField('sellerName').setValue(sellerName);
		}
		win.add(panel);
		win.show();
	}
	
});
