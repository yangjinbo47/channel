Ext.define('CMS.controller.operation.BlackListController', {
	extend : 'Ext.app.Controller',
	views : ['CMS.view.operation.BlackListManager'],
	stores : ['CMS.store.operation.BlackListStore'],
	refs : [{
		ref : 'blackListManagerView',
		selector : 'blackListManagerView'
	}],
	init : function() {
		this.control({
			'blackListManagerView' : {
				render : function(grid) {
					var roweditplugin = grid.getPlugin('blackListManager-rowediting');
					roweditplugin.on({
						canceledit : this.roweditcancel,
					    edit: this.roweditupdate,
					    scope: this
					});
				}
			},
			'blackListManagerView > toolbar > button[name=add]' : {
				click : this.onAddClick
			},
			'blackListManagerView > toolbar > button[name=delete]' : {
				click : this.onDelClick
			},
			'blackListManagerView > toolbar > button[name=search]' : {
				click : this.search
			},
			'blackListManagerView > toolbar > button[name=reset]' : {
				click : this.reset
			}
		});
	},
	
	onAddClick : function() {
		var grid = this.getBlackListManagerView();
		var roweditplugin = grid.getPlugin('blackListManager-rowediting');
		roweditplugin.cancelEdit();
		var r = Ext.create('CMS.model.operation.BlackListModel', {
            phoneNum: '手机号码'
        });
        
        grid.store.insert(0, r);
        roweditplugin.startEdit(0,0);
	},
	
	onDelClick : function() {
		var grid = this.getBlackListManagerView();
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
						url : '../operation/blackList_delete.action',
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
		
		var grid = this.getBlackListManagerView();
		var sm = grid.getSelectionModel();
		var rs = sm.getSelection();
		
		Ext.each(grid.getSelectionModel().getSelection(), function(row, index, value) {
			var id;
			if(row.data.id){
				id = row.data.id;
			}
			
			Ext.Ajax.request({
				url : '../operation/blackList_save.action',
				params : {
					'id' : id,
					'phoneNum' : row.data.phoneNum
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
		var grid = this.getBlackListManagerView();
		grid.store.reload();
	},
	
	search : function() {
		var grid = this.getBlackListManagerView();
		var queryParams = {};
		queryParams.phoneNum = this.getBlackListManagerView().down('toolbar > textfield[name=phoneNum]').getValue();
		grid.getStore().proxy.extraParams = queryParams;
		grid.getStore().reload();
		grid.getView().refresh();
	},
	
	reset : function() {
		var grid = this.getBlackListManagerView();
		var phoneNumField = grid.down('toolbar > textfield[name=phoneNum]');
		
		grid.getStore().proxy.extraParams = {};
		grid.getStore().load();
		grid.getView().refresh();
		
		phoneNumField.reset();
	}
});
