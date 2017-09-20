Ext.define('CMS.controller.operation.pack.OrderController', {
	extend : 'Ext.app.Controller',
	views : ['CMS.view.operation.pack.OrderManager'],
	stores : ['CMS.store.operation.pack.PushPackageChannelTreeStore','CMS.store.operation.pack.OrderStore'],
	refs : [{
		ref : 'orderManagerView',
		selector : 'orderManagerView'
	}],
	
	init : function() {
		this.control({
			'orderManagerView > gridpanel > toolbar > button[name=search]' : {
				click : this.search
			},
			'orderManagerView > gridpanel > toolbar > button[name=export]' : {
				click : this.exportFile
			}
		});
	},
	
	search : function() {
		var me = this,orderManagerPanel = me.getOrderManagerView(),treePanel = orderManagerPanel.down('treepanel'),
			selectionModel = treePanel.getSelectionModel(), records = selectionModel.getSelection();
		var startTimeField = orderManagerPanel.down('gridpanel > toolbar > datetimefield[name=startTime]');
		var endTimeField = orderManagerPanel.down('gridpanel > toolbar > datetimefield[name=endTime]');
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
			var grid = orderManagerPanel.down('gridpanel');
			var queryParams = {};
//			queryParams.channel = records[0].data.clientVersion;
			queryParams.sellerId = records[0].data.id;
			queryParams.startTime = startTimeField.getValue();
			queryParams.endTime = endTimeField.getValue();
			
			grid.getStore().proxy.extraParams = queryParams;
			grid.getStore().loadPage(1);
		}
	},
	
	exportFile : function() {
		var me = this,orderManagerPanel = me.getOrderManagerView(),treePanel = orderManagerPanel.down('treepanel'),
			selectionModel = treePanel.getSelectionModel(), records = selectionModel.getSelection();
		var startTimeField = orderManagerPanel.down('gridpanel > toolbar > datetimefield[name=startTime]');
		var endTimeField = orderManagerPanel.down('gridpanel > toolbar > datetimefield[name=endTime]');
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
			var startTime = me.formatDate("yyyy-MM-dd HH:mm:ss",startTimeField.getValue());
			var endTime = me.formatDate("yyyy-MM-dd HH:mm:ss",endTimeField.getValue());
			window.open('../operation/order_export.action?sellerId='+records[0].data.id+'&startTime='+startTime+'&endTime='+endTime);
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
	}
	
});