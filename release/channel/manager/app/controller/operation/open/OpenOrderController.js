Ext.define('CMS.controller.operation.open.OpenOrderController', {
	extend : 'Ext.app.Controller',
	views : ['CMS.view.operation.open.OpenOrderManager'],
	stores : ['CMS.store.operation.open.OpenSellerTreeStore','CMS.store.operation.open.OpenOrderStore'],
	requires : ['CMS.model.operation.open.OpenOrderProvinceCountModel'],
	refs : [{
		ref : 'openOrderManagerView',
		selector : 'openOrderManagerView'
	},{
		ref : 'openOrderProvinceCountWindow',
		selector : 'openOrderProvinceCountWindow'
	}],
	
	init : function() {
		this.control({
			'openOrderManagerView > gridpanel > toolbar > button[name=search]' : {
				click : this.search
			},
			'openOrderManagerView > gridpanel > toolbar > button[name=export]' : {
				click : this.exportFile
			},
			
			//省份统计
			'openOrderManagerView > gridpanel > toolbar > button[name=province]' : {
				click : this.onCountClick
			}
		});
	},
	
	search : function() {
		var me = this,openOrderManagerPanel = me.getOpenOrderManagerView(),treePanel = openOrderManagerPanel.down('treepanel'),
			selectionModel = treePanel.getSelectionModel(), records = selectionModel.getSelection();
		var startTimeField = openOrderManagerPanel.down('gridpanel > toolbar > datetimefield[name=startTime]');
		var endTimeField = openOrderManagerPanel.down('gridpanel > toolbar > datetimefield[name=endTime]');
//		var payPhoneFidld = openOrderManagerPanel.down('gridpanel > toolbar > textfield[name=payPhone]');
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
		if(Ext.isEmpty(records) || records.length < 1){
			Ext.tipbox.msg('提示', '请选择查询渠道');
			return;
		} else if(records.length > 1){
			Ext.tipbox.msg('提示', '一次只能选择一条记录');
			return;
		} else {
			var grid = openOrderManagerPanel.down('gridpanel');
			var queryParams = {};
			queryParams.sellerId = records[0].data.id;
			queryParams.startTime = startTimeField.getValue();
			queryParams.endTime = endTimeField.getValue();
//			queryParams.payPhone = payPhoneFidld.getValue();
			
			grid.getStore().proxy.extraParams = queryParams;
			grid.getStore().loadPage(1);
		}
	},
	
	exportFile : function() {
		var me = this,openOrderManagerPanel = me.getOpenOrderManagerView(),treePanel = openOrderManagerPanel.down('treepanel'),
			selectionModel = treePanel.getSelectionModel(), records = selectionModel.getSelection();
		var startTimeField = openOrderManagerPanel.down('gridpanel > toolbar > datetimefield[name=startTime]');
		var endTimeField = openOrderManagerPanel.down('gridpanel > toolbar > datetimefield[name=endTime]');
//		var payPhoneFidld = openOrderManagerPanel.down('gridpanel > toolbar > textfield[name=payPhone]');
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
		if(Ext.isEmpty(records) || records.length < 1){
			Ext.tipbox.msg('提示', '请选择查询版本号');
			return;
		} else if(records.length > 1){
			Ext.tipbox.msg('提示', '一次只能选择一条记录');
			return;
		} else {
			var startTime = me.formatDate("yyyy-MM-dd HH:mm:ss",startTimeField.getValue());
			var endTime = me.formatDate("yyyy-MM-dd HH:mm:ss",endTimeField.getValue());
//			var payPhone = payPhoneFidld.getValue();
			window.open('../operation/openOrder_export.action?sellerId='+records[0].data.id+'&startTime='+startTime+'&endTime='+endTime);
		}
	},
	
	formatDate : function (fmt,date) {
	    var o = {
	        "M+": date.getMonth() + 1, //月份 
	        "d+": date.getDate(), //日 
	        "H+": date.getHours(), //小时 
	        "m+": date.getMinutes(), //分 
	        "s+": date.getSeconds(), //秒 
	        "q+": Math.floor((date.getMonth() + 3) / 3), //季度 
	        "S": date.getMilliseconds() //毫秒 
	    };
	    if (/(y+)/.test(fmt)) fmt = fmt.replace(RegExp.$1, (date.getFullYear() + "").substr(4 - RegExp.$1.length));
	    for (var k in o)
	    if (new RegExp("(" + k + ")").test(fmt)) fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
	    return fmt;
	},
	
	
	//省份统计相关
	onCountClick : function() {
		var me = this,openOrderManagerPanel = me.getOpenOrderManagerView(),treePanel = openOrderManagerPanel.down('treepanel'),
			selectionModel = treePanel.getSelectionModel(), records = selectionModel.getSelection();
		var startTimeField = openOrderManagerPanel.down('gridpanel > toolbar > datetimefield[name=startTime]');
		var endTimeField = openOrderManagerPanel.down('gridpanel > toolbar > datetimefield[name=endTime]');
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
		if(Ext.isEmpty(records) || records.length < 1){
			Ext.tipbox.msg('提示', '请选择查询渠道');
			return;
		} else if(records.length > 1){
			Ext.tipbox.msg('提示', '只能选择一个渠道进行查看');
			return;
		} else {
			var grid = openOrderManagerPanel.down('gridpanel');
			var queryParams = {};
			queryParams.sellerId = records[0].data.id;
			queryParams.startTime = startTimeField.getValue();
			queryParams.endTime = endTimeField.getValue();
			
			grid.getStore().proxy.extraParams = queryParams;
			grid.getStore().loadPage(1);
			
			var form = Ext.create('CMS.view.operation.open.OpenOrderProvinceCountGrid');
			form.store.proxy.extraParams.sellerId = records[0].data.id;
			form.store.proxy.extraParams.startTime = startTimeField.getValue();
			form.store.proxy.extraParams.endTime = endTimeField.getValue();
			form.store.loadPage(1);
			
			var win = Ext.create('CMS.view.operation.open.OpenOrderProvinceCountWindow');
			win.add(form);
			win.show();
			win.center();
		}
	}
});