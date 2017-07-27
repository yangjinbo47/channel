Ext.define('CMS.view.Viewport', {
	extend : 'Ext.container.Viewport',
	xtype : 'cmsviewport',
	layout : 'border',
	initComponent : function(){
		this.callParent(arguments);
		//头部显示logo,退出系统的面板
		this.northPanel = Ext.create('Ext.panel.Panel',{
			name : 'north',
			region : 'north',
			height : 62,
		    layout : {
		        type : 'hbox',
		        align : 'middle'
		    },
		    bodyStyle : 'background:transparent;',
		    items : [{
	            xtype: 'component',
	            style : 'top:0px !important;line-height: 62px;height:62px;',
	            html: '<img src="extjs/resources/themes/images/bannerlogo.png" style=" margin-top:0;"/>',
	            flex: 1
	        },{
				xtype: 'component',
		        html: '<span style="color:#FFFFFF;font-size: 14px; margin:0 0 30px 100px;">您好,'+ (Ext.isEmpty(__operatorname__)?'请登陆':__operatorname__) +'</span>',
		        margin : '0 10 0 0'
			},{
				xtype : 'container',
				layout : 'vbox',
				items : [{
					xtype : 'button',
					text : '修改密码',
					margin : '0 20 0 0',
					iconCls : 'icon-edit',
					scope: this,
					handler : this.onchangePwd		
				},{
					xtype : 'button',
					margin : '5 20 0 0',
					text : '退出系统',
					iconCls : 'icon-logout',
					scope: this,
					handler : this.onQuit		
				}]
			}],
			listeners : {
				scope : this,
				'render' : this.onHeaderRender
			}
		});
		
		//中间的内容区域
		this.centerPanel = Ext.create('Ext.tab.Panel',{
			name : 'center',
			region : 'center',
			layout : 'fit',
			plugins : Ext.create('Ext.tab.plugin.TabCloseMenu', {
				extraItemsTail : [ '-', {
					text : '可关闭',
					checked : true,
					hideOnClick : true,
					handler : function(item) {
						currentItem.tab.setClosable(item.checked);
					}
				} ],
				listeners : {
					beforemenu : function(menu, item) {
						menu.child('[text="可关闭"]').setChecked(item.closable);
						currentItem = item;
					}
				}
			})
		});
		
		//左侧的导航菜单
		this.westPanel = Ext.create('Ext.panel.Panel',{
			title : '功能菜单',
			iconCls : 'icon-home',
			name : 'west',
			region : 'west',
			width : 220,
			minWidth : 130,
			maxWidth : 400,
			split : true,
			collapsible: true,
			layout: {
		        type: 'accordion',
		        titleCollapse: true,
		        animate: false,
		        activeOnTop: false
		    },
			listeners : {
				scope : this,
				'render' : this.onWestRender
			}
		});
		
		this.add(this.northPanel,this.centerPanel,this.westPanel);
	},
	
	onchangePwd	: function(){
    	var form = Ext.create('CMS.view.account.ModPasswordFormPanel');
//		form.load({
//			url : '../account/user_getCurrentUser.action'
//		});
		var win = Ext.create('CMS.view.account.ModPasswordWindow');
		win.add(form);
		win.show();
    },
    
    onQuit : function() {
		Ext.Msg.confirm('操作提示', '您确定要退出本系统?', function(btn) {
			if ('yes' == btn) {
				Ext.Ajax.request({
					url : '../www/logout.action',
					success : function() {
						location = (Ext.isIE10 || Ext.isIE10m || Ext.isIE10p)? './' : '../manager/index.jsp';
					},
					failure : function() {
						Ext.Msg.show({
							title : '错误提示',
							msg : '退出系统失败!',
							icon : Ext.Msg.ERROR,
							buttons : Ext.Msg.OK
						});
					}
				});
			}
		});
	},
	
	onHeaderRender : function(){
		var checkActiveTask = {
			run: function(){
				Ext.Ajax.request({
					url : '../www/getStatus.action',
					disableCaching : true, // 禁止缓存
					timeout : 60000, // 最大等待时间,超出则会触发超时
					method : "GET",
					success : function(response, opts){
						var ret = Ext.JSON.decode(response.responseText); // JSON对象化
                        if (ret.success){

                        }else{
                            Ext.TaskManager.stop(checkActiveTask); // 停止该定时任务
                            Ext.Msg.alert('提 示','还没有登陆或者操作已超时，请重新登陆',function(){
                            	window.location.href='index.jsp';
                            });
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
    　　　		interval : 300000 // 5分钟
        };
    	Ext.TaskManager.start(checkActiveTask);
	},
	
	onWestRender : function(west){
		var tipel = west.body.createChild('<div style="width: 100%; margin-left: 29%;"><span style="top: 35%; position: absolute;" class="loading16"></span><span style="margin-left: 22px; top: 35%; position: absolute;">获取菜单中</span></div>');
		Ext.Ajax.request({
			url : '../account/user_getMenu.action',
			success : function(response) {
				tipel.remove();
				var json = Ext.decode(response.responseText);
				if (json.success) {
					Ext.each(json.children, function(obj, index, jsonself) {
						var args = index == 0 ? {
							id : obj.privilegeid,
							title : obj.text,
							layout : 'fit',
							uid : 'firstTree',
							collapsed : true
						} : {
							id : obj.privilegeid,
							title : obj.text,
							layout : 'fit',
							collapsed : true,
							style : 'margin-top:-4px;'
						};
						var childpanel = Ext.create('Ext.panel.Panel', Ext.apply(args, Ext.isEmpty(obj.iconCls)?{}:{iconCls : obj.iconCls.replace('tree-','')}));
						west.add(childpanel);
					});
				}
			},
			failure : function(response) {
				tipel.update('<span style="top: 35%; position: absolute;width:16px;height:16px;" class="icon-errorinfo"></span><span style="margin-left: 20px; top: 35%; position: absolute;">初始化菜单失败 </span>');
			}
		});
		west.doLayout();
		
		//隐藏中间区域加载中的提示
		try{
			Ext. get ('loadingres').setOpacity (0.0, {//加载完成后隐藏提示
		    	duration : 700,
		       	easing : 'easeOut',
		       	callback : function(){
		       		Ext. get ('loadingres').setStyle({
		       			opacity: 1,
		       			display:'none'
		       		});
		       	}
		    });
		}catch(e){ }
	}
});