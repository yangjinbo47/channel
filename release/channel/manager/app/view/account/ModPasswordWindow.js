Ext.define('CMS.view.account.ModPasswordWindow', {
	extend : 'Ext.window.Window',
	xtype : 'modPasswordWindow',
	id : 'modPasswordWindow',
	title : '修改密码',
	resizable : false,
	modal : true,
	width : 300,
	height : 180,
	layout : 'fit',
	dockedItems : [ {
		xtype : 'toolbar',
		dock : 'bottom',
		ui : 'footer',
		layout : {
			pack : 'center'
		},
		items : [ {
			minWidth : 80,
			text : '保存',
			handler : function() {
				var form = Ext.getCmp("modPasswordFormPanel").getForm();
				if (!form.isValid()) {
					return;
				}
				Ext.getCmp('modPasswordFormPanel').submit({
					success : function(form, action) {
						Ext.Msg.alert('提示', '修改密码成功!');
						Ext.getCmp("modPasswordWindow").close();
					},
					failure : function(form, action) {
						Ext.Msg.alert('提示', '修改密码失败！');
						Ext.getCmp("modPasswordWindow").close();
					}
				});
			}
		}, {
			minWidth : 80,
			text : '取消',
			handler : function() {
				this.ownerCt.ownerCt.close();
			}
		} ]
	} ]
});
