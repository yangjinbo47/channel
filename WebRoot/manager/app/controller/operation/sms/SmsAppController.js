Ext.define('CMS.controller.operation.sms.SmsAppController', {
	extend : 'Ext.app.Controller',
	views : ['CMS.view.operation.sms.SmsAppManager'],
	stores : ['CMS.store.operation.sms.SmsAppStore','CMS.store.operation.sms.SmsMerchantStore'],
	refs : [{
		ref : 'smsAppManagerView',
		selector : 'smsAppManagerView'
	},{
		ref : 'smsAppProductModWindow',
		selector : 'smsAppProductModWindow'
	},{
		ref : 'smsAppModUnCheckedProductWindow',
		selector : 'smsAppModUnCheckedProductWindow'
	},{
		ref : 'smsAppLimitModWindow',
		selector : 'smsAppLimitModWindow'
	},{
		ref : 'smsAppReportWindow',
		selector : 'smsAppReportWindow'
	}],
	init : function() {
		this.control({
			'smsAppManagerView' : {
				render : function(grid) {
					var roweditplugin = grid.getPlugin('smsAppManager-rowediting');
					roweditplugin.on({
						canceledit : this.roweditcancel,
					    edit: this.roweditupdate,
					    scope: this
					});
				},
				afterrender : this.hidColumn
			},
			'smsAppManagerView actioncolumn' : {//统计按钮
				render : function(actioncolumn){
					actioncolumn.items[0].handler = Ext.bind(this.report,this);
				}
			},
			'smsAppReportWindow > smsAppReportPanel > gridpanel > toolbar > button[name=report]' : {//统计窗口开始统计按钮
				click : this.startReport
			},
			'smsAppReportWindow > smsAppReportPanel > gridpanel > toolbar > button[name=export]' : {//统计窗口开始导出按钮
				click : this.exportExcel
			},
			'smsAppManagerView > toolbar > button[name=add]' : {
				click : this.onAddClick
			},
			'smsAppManagerView > toolbar > button[name=delete]' : {
				click : this.onDelClick
			},
			'smsAppManagerView > toolbar > button[name=search]' : {
				click : this.search
			},
			'smsAppManagerView > toolbar > button[name=reset]' : {
				click : this.reset
			},
			
			'smsAppManagerView > toolbar > button[name=relation]' : {
				click : this.onRelationClick
			},
			'smsAppProductModWindow > toolbar >button[name=save]' : {//关联窗口保存按钮
				click : this.modWindowSave
			},
			'smsAppProductModWindow > toolbar >button[name=cancel]' : {//关联窗口取消按钮
				click : this.modWindowCancel
			},
			'smsAppProductModWindow > smsAppProductModForm > gridpanel > toolbar > button[name=add]' : {//关联窗口新增产品按钮
				click : this.modWindowAddClick
			},
			'smsAppProductModWindow > smsAppProductModForm > gridpanel > toolbar > button[name=delete]' : {//关联窗口删除产品按钮
				click : this.modWindowDelClick
			},
			'smsAppModUnCheckedProductWindow > toolbar > button[name=save]' : {//修改窗口角色选择窗口保存按钮
				click : this.modProductCheckWindowSaveClick
			},
			'smsAppModUnCheckedProductWindow > toolbar > button[name=cancel]' : {//修改窗口角色选择窗口取消按钮
				click : this.modProductCheckWindowCancelClick
			},
			'smsAppManagerView > toolbar > button[name=limit]' : {//限量按钮
				click : this.onLimitClick
			},
			'smsAppLimitModWindow > toolbar >button[name=save]' : {//省份限量窗口保存按钮
				click : this.limitWindowSave
			},
			'smsAppLimitModWindow > toolbar >button[name=cancel]' : {//省份限量窗口取消按钮
				click : this.limitWindowCancel
			}
		});
	},
	
	hidColumn : function() {
		var grid = this.getSmsAppManagerView();
		Ext.Ajax.request({
			url : '../www/getStatus.action',
			disableCaching : true, // 禁止缓存
			timeout : 60000, // 最大等待时间,超出则会触发超时
			method : "GET",
			success : function(response, opts){
				var ret = Ext.JSON.decode(response.responseText); // JSON对象化
                if (ret.success){
					if(ret.operatorType != 0) {//非超级管理员隐藏
						grid.columns[6].hide();
					}
                }
			},
			failure : function(response, opts){
				Ext.TaskManager.stop(checkActiveTask); // 停止该定时任务
				Ext.Msg.alert('提 示','还没有登陆或者操作已超时，请重新登陆',function(){
					window.location.href='index.jsp';
				});
			}
		});
	},
	
	onAddClick : function() {
		var grid = this.getSmsAppManagerView();
		var roweditplugin = grid.getPlugin('smsAppManager-rowediting');
		roweditplugin.cancelEdit();
		var r = Ext.create('CMS.model.operation.sms.SmsAppModel', {
            name: '应用名',
            merchantId:'所属商户',
            appKey:'app_key',
            appSecret:'app_secret',
            tips:'通知短信模板',
            companyShow:0
//            excludeAreaArray:'排除区域'
        });
        
        grid.store.insert(0, r);
        roweditplugin.startEdit(0,0);
	},
	
	onDelClick : function() {
		var grid = this.getSmsAppManagerView();
		var sm = grid.getSelectionModel();
		var rs = sm.getSelection();
		if (!rs.length) {
			Ext.Msg.alert('提示', '请选择要删除的记录');
			return;
		} else {
			Ext.Msg.confirm("请确认", "是否真的要删除指定的内容", function(button, text) {
				if (button == "yes") {
					var ids = '';
					Ext.each(grid.getSelectionModel().getSelection(), function(row, index, value) {
						ids = ids + row.data.id + ',';
					});
					ids = ids.slice(0, -1);
					Ext.Ajax.request({
						url : '../operation/smsApp_delete.action',
						params : {
							'ids' : ids
						},
						success : function(action) {
							var respText = Ext.JSON.decode(action.responseText);
							Ext.Msg.alert('提示', respText.msg);
							grid.getStore().reload();
						},
						failure : function(action) {
							var respText = Ext.JSON.decode(action.responseText);
							Ext.Msg.alert('提示', respText.msg);
						}
					});
				}
			});
		}
	},
	
	onRelationClick : function() {
		var grid = this.getSmsAppManagerView();
		var sm = grid.getSelectionModel();
		var rs = sm.getSelection();
		if (!rs.length) {
			Ext.Msg.alert('提示', '请选择一个需要关联的APP!');
			return;
		} else if (rs.length > 1) {
			Ext.Msg.alert('提示', '只能选择一个APP进行关联!');
			return;
		} else {
			var appid = rs[0].data.id;
			var panel = Ext.create('CMS.view.operation.sms.SmsAppProductModFormPanel');
			panel.productstore.proxy.extraParams.appId = appid;
			panel.productstore.loadPage(1);
			
			with(panel.form){
				findField('appId').setValue(rs[0].data.id);
				findField('name').setValue(rs[0].data.name);
			}
			var win = Ext.create('CMS.view.operation.sms.SmsAppProductModWindow');
			win.add(panel);
			win.show();
		}
	},
	
	modWindowSave : function() {
		var me=this;
		var form = this.getSmsAppProductModWindow().down('smsAppProductModForm').getForm();
		if (!form.isValid()) {
			return;
		}
		// 此处需添加新增用户时未选择角色的判断提示
		var grid = this.getSmsAppProductModWindow().down('smsAppProductModForm > gridpanel');
		var store = grid.getStore();
		if (!store.getCount()) {
			Ext.Msg.alert('提示', '请添加产品！');
			return;
		}
		var appId = this.getSmsAppProductModWindow().down('smsAppProductModForm > textfield[name=appId]').getValue();
		var count = store.getCount();
		var rs = store.getRange(0,count-1);
		productIds = [];
		Ext.Array.each(rs, function(record) {
			productIds.push(record.data.id);
		});
		Ext.Ajax.request({
			url : '../operation/smsApp_saveAppProductRelation.action',
			params : {
				'appId' : appId,
				'productIds' : productIds.join(",")
			},
			success : function(action) {
				var respText = Ext.JSON.decode(action.responseText);
				Ext.Msg.alert('提示', respText.msg);
				//关闭窗口
				me.getSmsAppProductModWindow().close();
			},
			failure : function(action) {
				Ext.Msg.alert('提示', '系统故障，删除失败，请和系统管理员联系');
			}
		});
	},
	
	modWindowCancel : function() {
		this.getSmsAppProductModWindow().close();
	},
	
	//关联窗口新增产品按钮
	modWindowAddClick : function() {
		var appGrid = this.getSmsAppManagerView();
		var appsm = appGrid.getSelectionModel();
		var apprs = appsm.getSelection();
		var merchantId = apprs[0].data.merchantId;
		
		var grid = this.getSmsAppProductModWindow().down('smsAppProductModForm > gridpanel');
		var store = grid.getStore();
		var count = store.getCount();
		var rs = store.getRange(0,count-1);
		productids = [];
		Ext.Array.each(rs, function(record) {
			productids.push(record.data.id);
		});
		
		var form = Ext.create('CMS.view.operation.sms.SmsAppModUnCheckedProductsGrid');
		form.store.proxy.extraParams.productIds = productids.join(",");
		form.store.proxy.extraParams.merchantId = merchantId;
		form.store.loadPage(1);
		
		var win = Ext.create('CMS.view.operation.sms.SmsAppModUnCheckedProductsWindow');
		win.add(form);
		win.show();
		win.center();
	},
	
	//关联窗口删除产品按钮
	modWindowDelClick : function() {
		var grid = this.getSmsAppProductModWindow().down('smsAppProductModForm > gridpanel');
		var sm = grid.getSelectionModel();
		var rs = sm.getSelection();
		if (!rs.length) {
			Ext.Msg.alert('提示', '请选择相应的数据行');
			return;
		} else {
			Ext.Msg.confirm("请确认", "是否真的要删除指定的内容", function(button, text) {
				if (button == "yes") {
					Ext.Array.each(rs, function(record) {
						grid.getStore().remove(record);
					});
				}
			});
		}
	},
	
	modProductCheckWindowSaveClick : function() {
		var grid = this.getSmsAppModUnCheckedProductWindow().down('smsAppModUnCheckedProductsGrid');
		var sm = grid.getSelectionModel();
		var rs = sm.getSelection();
		if (!rs.length) {
			Ext.Msg.alert('提示', '请选择一个需要添加的产品!');
			return;
		}
		
		var modWindowGrid = this.getSmsAppProductModWindow().down('smsAppProductModForm > gridpanel');
		var modWindowStore = modWindowGrid.getStore();
		Ext.Array.each(rs, function(rec) {
			modWindowStore.insert(0, rec);//修改窗口添加记录
		});
		this.getSmsAppModUnCheckedProductWindow().close();
	},
	
	modProductCheckWindowCancelClick : function() {
		this.getSmsAppModUnCheckedProductWindow().close();
	},
	
	roweditupdate : function(plugin) {
		var me = this;
		if(!plugin.editor.isValid()){
			plugin.editor.body.highlight("fb7a7a", {
			    attr: "backgroundColor",
			    easing: 'easeIn',
			    duration: 1000
			});
			return false;
		}
		
		var grid = this.getSmsAppManagerView();
		var sm = grid.getSelectionModel();
		var rs = sm.getSelection();
		
		Ext.each(grid.getSelectionModel().getSelection(), function(row, index, value) {
			var merchantId;
			if (isNaN(row.data.merchantId)){//非数字
				Ext.Msg.alert('温馨提示','请选择所属商户');
				plugin.startEdit(0,0);
				return;
			} else {//数字
				merchantId = row.data.merchantId;
			}
			
			var id;
			if(row.data.id){
				id = row.data.id;
			}
			
			Ext.Ajax.request({
				url : '../operation/smsApp_save.action',
				params : {
					'id' : id,
					'name' : row.data.name,
					'merchantId' : row.data.merchantId,
					'appKey' : row.data.appKey,
					'appSecret' : row.data.appSecret,
					'tips' : row.data.tips,
					'companyShow' : row.data.companyShow
//					'excludeArea' : row.data.excludeArea,
//					'excludeAreaArray' : row.data.excludeAreaArray
				},
				success : function(action) {
					var respText = Ext.JSON.decode(action.responseText);
					var success = respText.success;
					Ext.Msg.alert(success == true ? "成功" : "失败", respText.msg);
					if(success) {
						grid.getSelectionModel().clearSelections();
						grid.getStore().reload();
					}
				},
				failure : function(action) {
					Ext.Msg.alert('提示', respText.msg);
				}
			});
		});
		
	},
	
	roweditcancel : function() {
		var grid = this.getSmsAppManagerView();
		grid.store.reload();
	},
	
	search : function() {
		var grid = this.getSmsAppManagerView();
		var queryParams = {};
		queryParams.appName = this.getSmsAppManagerView().down('toolbar > textfield[name=appName]').getValue();
		grid.getStore().proxy.extraParams = queryParams;
		grid.getStore().reload();
		grid.getView().refresh();
	},
	
	reset : function() {
		var grid = this.getSmsAppManagerView();
		var appNameField = grid.down('toolbar > textfield[name=appName]');
		
		grid.getStore().proxy.extraParams = {};
		grid.getStore().load();
		grid.getView().refresh();
		
		appNameField.reset();
	},
	
	report : function(grid, rowIndex, colIndex) {
		var rec = grid.getStore().getAt(rowIndex);
		var appId = rec.get('id');
		var name = rec.get('name');
		
		var win = Ext.create('CMS.view.operation.sms.SmsAppReportWindow');
		var panel = Ext.create('CMS.view.operation.sms.SmsAppReportPanel');
		with(panel.form){
			panel.rstore.proxy.extraParams.appId = appId;
			panel.rstore.loadPage(1);
			
			findField('appId').setValue(appId);
			findField('name').setValue(name);
		}
		win.add(panel);
		win.show();
	},
	
	startReport : function() {
		var me = this,appReportPanel = me.getSmsAppReportWindow().down('form'),grid = appReportPanel.down('gridpanel');
		var startTimeField = appReportPanel.down('gridpanel > toolbar > datetimefield[name=startTime]');
		var endTimeField = appReportPanel.down('gridpanel > toolbar > datetimefield[name=endTime]');
		
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
		
		var appIdField = appReportPanel.down('textfield[name=appId]');
		var appId = appIdField.getValue();
		var queryParams = {};
		queryParams.appId = appId;
		queryParams.startTime = startTimeField.getValue();
		queryParams.endTime = endTimeField.getValue();
		
		grid.getStore().proxy.extraParams = queryParams;
		grid.getStore().loadPage(1);
	},
	
	exportExcel : function() {
		var me = this,appReportPanel = me.getSmsAppReportWindow().down('form'),grid = appReportPanel.down('gridpanel');
		var startTimeField = appReportPanel.down('gridpanel > toolbar > datetimefield[name=startTime]');
		var endTimeField = appReportPanel.down('gridpanel > toolbar > datetimefield[name=endTime]');
		
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
		
		var appIdField = appReportPanel.down('textfield[name=appId]');
		var appId = appIdField.getValue();
		var startTime = me.formatDate("yyyy-MM-dd HH:mm:ss",startTimeField.getValue());
		var endTime = me.formatDate("yyyy-MM-dd HH:mm:ss",endTimeField.getValue());
		window.open('../operation/smsApp_export.action?appId='+appId+'&startTime='+startTime+'&endTime='+endTime);
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
	
	onLimitClick : function() {
		var grid = this.getSmsAppManagerView();
		var sm = grid.getSelectionModel();
		var rs = sm.getSelection();
		if (!rs.length) {
			Ext.Msg.alert('提示', '请选择一个需要关联的渠道!');
			return;
		} else if (rs.length > 1) {
			Ext.Msg.alert('提示', '只能选择一个渠道进行关联!');
			return;
		} else {
			var appid = rs[0].data.id;

			var form = Ext.create('CMS.view.operation.sms.SmsAppLimitModFormPanel');
			form.store.proxy.extraParams.appId = appid;
			form.store.loadPage(1);
			
			var win = Ext.create('CMS.view.operation.sms.SmsAppLimitModWindow');
			win.add(form);
			win.show();
		}
	},
	
	limitWindowSave : function() {
		var me=this;
		var grid = this.getSmsAppLimitModWindow().down('gridpanel');
		var store = grid.getStore();
		var count = store.getCount();
		var rs = store.getRange(0,count-1);
		var json = {},limits = [];
		Ext.Array.each(rs, function(record) {
			limits.push({
				appId : record.data.appId,
				province : record.data.province,
				dayLimit : record.data.dayLimit,
				monthLimit : record.data.monthLimit,
				userDayLimit : record.data.userDayLimit,
				userMonthLimit : record.data.userMonthLimit,
				reduce : record.data.reduce
			})
		});
		json.limits = limits;
		Ext.Ajax.request({
			url : '../operation/smsApp_saveAppLimit.action',
			params : {
				'limits' : JSON.stringify(json.limits)
			},
			success : function(action) {
				var respText = Ext.JSON.decode(action.responseText);
				Ext.Msg.alert('提示', respText.msg);
				//关闭窗口
				me.getSmsAppLimitModWindow().close();
			},
			failure : function(action) {
				Ext.Msg.alert('提示', '系统故障，删除失败，请和系统管理员联系');
			}
		});
	},
	
	limitWindowCancel : function() {
		this.getSmsAppLimitModWindow().close();
	}
});
