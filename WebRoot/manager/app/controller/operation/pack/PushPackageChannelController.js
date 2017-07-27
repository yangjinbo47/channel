Ext.define('CMS.controller.operation.pack.PushPackageChannelController', {
	extend : 'Ext.app.Controller',
	views : ['CMS.view.operation.pack.PushPackageChannelManager'],
	stores : ['CMS.store.operation.pack.PushPackageChannelStore'],
	refs : [{
		ref : 'pushPackageChannelManagerView',
		selector : 'pushPackageChannelManagerView'
	}],
	init : function() {
		this.control({
			'pushPackageChannelManagerView' : {
				render : function(grid) {
					var roweditplugin = grid.getPlugin('pushPackageChannelManager-rowediting');
					roweditplugin.on({
						canceledit : this.roweditcancel,
					    edit: this.roweditupdate,
					    scope: this
					});
				},
				afterrender : this.hidColumn
			},
			'pushPackageChannelManagerView > toolbar > button[name=add]' : {
				click : this.onAddClick
			},
			'pushPackageChannelManagerView > toolbar > button[name=delete]' : {
				click : this.onDelClick
			},
			'pushPackageChannelManagerView > toolbar > button[name=search]' : {
				click : this.search
			},
			'pushPackageChannelManagerView > toolbar > button[name=reset]' : {
				click : this.reset
			}
		});
	},
	
	onAddClick : function() {
		var grid = this.getPushPackageChannelManagerView();
		var roweditplugin = grid.getPlugin('pushPackageChannelManager-rowediting');
		roweditplugin.cancelEdit();
		var r = Ext.create('CMS.model.operation.pack.PackageChannelModel', {
            channelName: '渠道名',
            clientVersion: '客户端版本号',
            companyShow:0
        });
        
        grid.store.insert(0, r);
        roweditplugin.startEdit(0,0);
	},
	
	onDelClick : function() {
		var grid = this.getPushPackageChannelManagerView();
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
						url : '../operation/pushPackageChannel_delete.action',
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
		
		var grid = this.getPushPackageChannelManagerView();
		var sm = grid.getSelectionModel();
		var rs = sm.getSelection();
		
		Ext.each(grid.getSelectionModel().getSelection(), function(row, index, value) {
			var id;
			if(row.data.id){
				id = row.data.id;
			}
			
			Ext.Ajax.request({
				url : '../operation/pushPackageChannel_save.action',
				params : {
					'id' : id,
					'channelName' : row.data.channelName,
					'clientVersion' : row.data.clientVersion,
					'companyShow' : row.data.companyShow
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
		var grid = this.getPushPackageChannelManagerView();
		grid.store.reload();
	},
	
	search : function() {
		var grid = this.getPushPackageChannelManagerView();
		var queryParams = {};
		queryParams.name = this.getPushPackageChannelManagerView().down('toolbar > textfield[name=packageName]').getValue();
		grid.getStore().proxy.extraParams = queryParams;
		grid.getStore().reload();
		grid.getView().refresh();
	},
	
	reset : function() {
		var grid = this.getPushPackageChannelManagerView();
		var packageNameField = grid.down('toolbar > textfield[name=packageName]');
		
		grid.getStore().proxy.extraParams = {};
		grid.getStore().load();
		grid.getView().refresh();
		
		packageNameField.reset();
	},
	
	hidColumn : function() {
		var grid = this.getPushPackageChannelManagerView();
		Ext.Ajax.request({
			url : '../www/getStatus.action',
			disableCaching : true, // 禁止缓存
			timeout : 60000, // 最大等待时间,超出则会触发超时
			method : "GET",
			success : function(response, opts){
				var ret = Ext.JSON.decode(response.responseText); // JSON对象化
                if (ret.success){
					if(ret.operatorType != 0) {//非超级管理员隐藏
						grid.columns[4].hide();
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
	}
});
