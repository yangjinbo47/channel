/**
 * 权限菜单管理controller
 */
Ext.define('CMS.controller.account.PrivilegeController',{
	extend : 'Ext.app.Controller',
	stores : ['CMS.store.account.PrivilegeStore'],
	views : ['CMS.view.account.PrivilegeManager'],
	refs : [{
		ref : 'privilegeManager',
		selector : 'privilegeManager'
	}],
	nodesAndParents : [],//用于树前台过滤的临时变量
	nodePathMap : new Ext.util.MixedCollection(),//记录展开的path，用于刷新树后保持原来展开的状态
	scrollTopPos : 0,//记录刷新前滚动条的位置，用于刷新后保持原来滚动条的位置
	init : function() {
		this.control({
			'privilegeManager > toolbar > button[name=add]' : {
				click : this.onAdd
			},
			'privilegeManager > toolbar > button[name=edit]' : {
				click : this.onEdit
			},
			'privilegeManager > toolbar > button[name=delete]' : {
				click : this.onDelete
			},
			'privilegeManager > treepanel' : {
				itemcontextmenu : this.onContextMenu,
				containercontextmenu : this.onContainerContextMenu,
				itemexpand : this.onTreeNodeExpand,
				beforeitemexpand : this.onTreeNodeBeforeExpand,
				itemcollapse : this.onTreeNodeCollapse,
				deselect : this.onTreeNodeDeselect,
				select : this.onTreeNodeSelect,
				beforeload : this.onTreePanelBeforeLoad,
				afterrender : function(tree){
					tree.getView().on({
						scope : this,
						nodedragover : function(targetNode, position, dragData){
							var currentNode = dragData.records[0];
							tree.canDrop = true;
							if(position == 'append' || targetNode.hasChildNodes() || targetNode.parentNode.data.privilegeid != currentNode.parentNode.data.privilegeid){
								tree.canDrop = false;
								return tree.canDrop;
							}
							tree.canDrop = true;
							return tree.canDrop;
						},
						beforedrop : function(node, data, overModel, position, dropHandlers){
							var currentMode = data.records[0];
							tree.canDrop = true;
							if(position == 'append' || overModel.hasChildNodes() || overModel.parentNode.data.privilegeid != currentMode.parentNode.data.privilegeid){
								tree.canDrop = false;
								dropHandlers.cancelDrop();
							}
						},
						drop : function(node, data, overModel, dropPosition, eOpts){
							var currentMode = data.records[0];
							if(tree.canDrop === true){
								this.onMoveUpOrDown(currentMode);
							}
						}
					});
				}
			},
			'privilegeManager > treepanel > toolbar > isearchfield[name=privilegetext]' : {
				keyup : this.onFilterTree
			},
			'privilegeManager' : {
				render : function(panel){
					panel.contextMenu.down('menuitem[name=add]').on('click',this.onAdd,this);
					panel.contextMenu.down('menuitem[name=edit]').on('click',this.onEdit,this);
					panel.contextMenu.down('menuitem[name=delete]').on('click',this.onDelete,this);
					panel.contextMenu.down('menuitem[name=refresh]').on('click',this.onRefresh,this);
					panel.contextMenu.down('menuitem[name=expand]').on('click',this.onExpand,this);
					panel.contextMenu.down('menuitem[name=expandall]').on('click',this.onExpandAll,this);
					panel.contextMenu.down('menuitem[name=collapse]').on('click',this.onCollapse,this);
					panel.contextMenu.down('menuitem[name=collapseall]').on('click',this.onCollapseAll,this);
					panel.contextMenu.down('menuitem[name=up]').on('click',this.onMoveUp,this);
					panel.contextMenu.down('menuitem[name=down]').on('click',this.onMoveDown,this);
				}
			},
			'privilegeManager > panel[name=eastpanel] > form[name=editpanel] > toolbar > button[name=save]' : {
				click : this.onSave
			},
			'privilegeManager > panel[name=eastpanel] > form[name=editpanel] ' : {
				'render' : function(panel){
					panel.el.on('keyup',function(e,t,opt){
						if(e.keyCode == 13){
							this.onSave();
						}
					},this);
				}
			},
			'privilegeManager > panel[name=eastpanel] > panel[name=viewpanel] ' : {
				'render' : this.onDropView
			},
			'privilegeManager > panel[name=eastpanel] > form[name=editpanel] > toolbar > button[name=cancel]' : {
				click : this.onCancel
			}
		});
	},
	
	onAdd : function(){
		var me = this,privilegeManagerPanel = me.getPrivilegeManager(), editPanel = privilegeManagerPanel.editPanel;
		if(editPanel.rendered){
			editPanel.form.reset();
		}
		privilegeManagerPanel.eastPanel.getLayout().setActiveItem(editPanel);
		privilegeManagerPanel.spot.show(editPanel.el);
		editPanel.down('textfield[name=text]').focus();
	},
	
	onEdit : function(){
		var me = this,privilegeManagerPanel = this.getPrivilegeManager(), formPanel = privilegeManagerPanel.editPanel,treePanel = privilegeManagerPanel.treePanel, 
			selectionModel = treePanel.getSelectionModel(), records = selectionModel.getSelection();
		if(Ext.isEmpty(records) || records.length < 1){
			Ext.tipbox.msg('提示', '请选择要修改的记录');
			return;
		}else if(records.length > 1){
			Ext.tipbox.msg('提示', '一次只能修改一条记录');
			return;
		}else{
			me.onAdd();
			formPanel.loadRecord(records[0]);
		}
	},
	
	onDelete : function(){
		var me = this,privilegeManagerPanel = me.getPrivilegeManager(), treePanel = privilegeManagerPanel.treePanel, 
			selectionModel = treePanel.getSelectionModel(), records = selectionModel.getSelection();
		if(Ext.isEmpty(records) || records.length < 1){
			Ext.tipbox.msg('提示', '请选择要删除的记录');
			return;
		}else if(records.length > 1){
			Ext.tipbox.msg('提示', '一次只能删除一条记录');
			return;
		}else{
			Ext.Msg.confirm('提示','您确定要删除选定的记录吗？<span style="color:red;">（如果选择的记录包含叶子节点，删除时此节点下的叶子节点将一并删除）</span>',function(btn){
				if(btn == 'yes'){
					var loadMask = new Ext.LoadMask(privilegeManagerPanel.el,{msg : '删除中'});
					try{
						loadMask.show();
						Ext.Ajax.request({
							url : '../account/privilege_delete.action',
							method : 'POST',
							params : {
								privilegeid : records[0].data.privilegeid
							},
							success : function(response){
								loadMask.hide();
								var json = Ext.decode(response.responseText);
								if(json.success){
									Ext.tipbox.msg('提示',json.msg || '删除成功');
									me.onCancel();
									me.onRefresh();
								}else{
									Ext.tipbox.msg('提示',json.msg ? "删除失败：" + json.msg : '删除失败');
								}
							},
							failure : function(){
								loadMask.hide();
								Ext.tipbox.msg('提示','删除失败：Ajax请求异常');
							}
						});
					}catch(e){
						loadMask.hide();
					}
				}
			},this);
		}
	},
	
	onSave : function(){
		var me = this,privilegeManagerPanel = this.getPrivilegeManager(), formPanel = privilegeManagerPanel.editPanel,url = '../account/privilege_add.action',params = {},
			treePanel = privilegeManagerPanel.treePanel, selectionModel = treePanel.getSelectionModel(), records = selectionModel.getSelection();
		if(formPanel.form.isValid()){
			var privilegeid = formPanel.down('hidden[name=privilegeid]').getValue();
			if(!Ext.isEmpty(privilegeid)){
				url = '../account/privilege_update.action';
			}else{
				params = {
					parentid : (!Ext.isEmpty(records) && records.length > 0)?records[0].data.privilegeid:'',
					menulevel : (!Ext.isEmpty(records) && records.length > 0)?records[0].getDepth() + 1:1
				};
			}
			
			var loadMask = new Ext.LoadMask(privilegeManagerPanel.editPanel.el,{msg : '保存中'});
			try{
				loadMask.show();
				formPanel.form.submit({
					url : url,
					method : 'POST',
					params : params,
					submitEmptyText : false,
					success : function(form, action){
						loadMask.hide();
						Ext.tipbox.msg('提示', action.result.msg || '保存成功');
						me.onCancel();
						me.onRefresh();
					},
					failure : function(form, action){
						loadMask.hide();
						switch (action.failureType) {
				            case Ext.form.action.Action.CLIENT_INVALID:
				                Ext.tipbox.msg('提示', '表单中存在未验证通过的数据');
				                break;
				            case Ext.form.action.Action.CONNECT_FAILURE:
				                Ext.tipbox.msg('提示', 'Ajax请求失败');
				                break;
				            case Ext.form.action.Action.SERVER_INVALID:
				               Ext.tipbox.msg('提示', action.result.msg);
				               break;
				            default : 
				            	Ext.tipbox.msg('提示', action.result.msg);
				       	}
					}
				});
			}catch(e){
				throw e;
				loadMask.hide();
			}
		}
	},
	
	onCancel : function(){
		var privilegeManagerPanel = this.getPrivilegeManager(),viewPanel = privilegeManagerPanel.viewPanel, editPanel = privilegeManagerPanel.editPanel,spot = privilegeManagerPanel.spot;
		if(spot && spot.el){
			spot.hide();
		}
		if(editPanel && editPanel.rendered){
			editPanel.form.reset();
		}
		privilegeManagerPanel.eastPanel.getLayout().setActiveItem(viewPanel);
		viewPanel.tplInfo.overwrite(viewPanel.body,{});
	},
	
	onContextMenu : function(tree, record, item, index, e){
		e.preventDefault();
		var privilegeManagerPanel = this.getPrivilegeManager(),treePanel = privilegeManagerPanel.treePanel, 
			selectionModel = treePanel.getSelectionModel(), records = selectionModel.getSelection();
		if(!Ext.isEmpty(records) && records.length > 0){
			var status = records[0].data.status;
			if(records[0].hasChildNodes()){
				if(records[0].isExpanded()){//选中节点有子节点，并且该节点当前为展开状态
					privilegeManagerPanel.contextMenu.down('menuitem[name=expand]').setDisabled(true);
					privilegeManagerPanel.contextMenu.down('menuitem[name=collapse]').setDisabled(false);
				}else{//选中节点有子节点，并且该节点当前为折叠状态
					privilegeManagerPanel.contextMenu.down('menuitem[name=expand]').setDisabled(false);
					privilegeManagerPanel.contextMenu.down('menuitem[name=collapse]').setDisabled(true);
				}
			}else{//选中的节点无子节点，将展开、折叠按钮置灰
				privilegeManagerPanel.contextMenu.down('menuitem[name=expand]').setDisabled(true);
				privilegeManagerPanel.contextMenu.down('menuitem[name=collapse]').setDisabled(true);
			}
			privilegeManagerPanel.contextMenu.down('menuitem[name=up]').setDisabled(Ext.isEmpty(records[0].previousSibling));
			privilegeManagerPanel.contextMenu.down('menuitem[name=down]').setDisabled(Ext.isEmpty(records[0].nextSibling));
		}else{//未选中节点，将上移、下移按钮置灰
			privilegeManagerPanel.contextMenu.down('menuitem[name=expand]').setDisabled(false);
			privilegeManagerPanel.contextMenu.down('menuitem[name=collapse]').setDisabled(false);
			
			privilegeManagerPanel.contextMenu.down('menuitem[name=up]').setDisabled(true);
			privilegeManagerPanel.contextMenu.down('menuitem[name=down]').setDisabled(true);
		}
		privilegeManagerPanel.contextMenu.showAt(e.getXY());
		return false;
	},
	
	onContainerContextMenu : function(tree, e){
		this.onContextMenu(tree,null,null,null,e);
	},
	
	onRefresh : function(){
		var privilegeManagerPanel = this.getPrivilegeManager(),treePanel = privilegeManagerPanel.treePanel,selectionModel = treePanel.getSelectionModel();
		selectionModel.clearSelections();
		
		//刷新数据之前，取得滚动条位置
		this.scrollTopPos = treePanel.getView().el.getScrollTop();
		treePanel.store.load({
			scope : this,
			callback : function(){
				var viewPanel = privilegeManagerPanel.viewPanel;
				viewPanel.tplInfo.overwrite(viewPanel.body,{});
				
				//刷新数据后保持树之前展开的状态
				this.nodePathMap.eachKey(function(key,item,index){
					treePanel.expandPath(item); 
				},this);
			}
		});
	},
	
	onExpand : function(){
		var me = this,privilegeManagerPanel = this.getPrivilegeManager(),treePanel = privilegeManagerPanel.treePanel, 
			selectionModel = treePanel.getSelectionModel(), records = selectionModel.getSelection();
		if(!Ext.isEmpty(records) && records.length > 0){
			records[0].expand(true);
		}
	},
	
	onExpandAll : function(){
		var privilegeManagerPanel = this.getPrivilegeManager(),treePanel = privilegeManagerPanel.treePanel;
		treePanel.getRootNode().expand(true);
	},
	
	onCollapse : function(){
		var me = this,privilegeManagerPanel = this.getPrivilegeManager(),treePanel = privilegeManagerPanel.treePanel, 
			selectionModel = treePanel.getSelectionModel(), records = selectionModel.getSelection();
		if(!Ext.isEmpty(records) && records.length > 0){
			records[0].collapse(true);
		}
	},
	
	onCollapseAll : function(){
		var privilegeManagerPanel = this.getPrivilegeManager(),treePanel = privilegeManagerPanel.treePanel;
		treePanel.getRootNode().collapseChildren(true);
	},
	
	onMoveUpOrDown : function(node){
		var me = this, data = [], index = 1;
		node.parentNode.eachChild(function(child){
			data.push({
				privilegeid : child.data.privilegeid,
				menuorder : index
			});
			index++;
		},this);
		
		try{
			Ext.Ajax.request({
				url : '../account/privilege_changeOrder.action',
				method : 'POST',
				params : {
					orderdata : Ext.encode(data)
				},
				success : function(response){
					var json = Ext.decode(response.responseText);
					if(json.success){
						Ext.tipbox.msg('提示',json.msg || '操作成功');
					}else{
						me.onRefresh();
						Ext.tipbox.msg('提示',json.msg || '操作失败');
					}
				},
				failure : function(){
					me.onRefresh();
					Ext.tipbox.msg('提示','请求异常');
				}
			});
		}catch(e){
			throw e;
			me.onRefresh();
		}
	},
	
	//树型数据表中，调整顺序上移操作
	onMoveUp : function(){
		var privilegeManagerPanel = this.getPrivilegeManager(),treePanel = privilegeManagerPanel.treePanel, 
			selectionModel = treePanel.getSelectionModel(), records = selectionModel.getSelection();
		if(records && records.length > 0){
			var node = records[0];
			if(node.previousSibling){
				node.parentNode.insertBefore(node,node.previousSibling);
				treePanel.view.refresh();
				this.onMoveUpOrDown(node);
			}else{
				Ext.tipbox.msg('提示','当前节点不可再上移操作');
			}
		}else{
			Ext.tipbox.msg('提示','请选择要移动的节点');
		}
	},
	
	//树型数据表中，调整顺序下移操作
	onMoveDown : function(){
		var privilegeManagerPanel = this.getPrivilegeManager(),treePanel = privilegeManagerPanel.treePanel, 
			selectionModel = treePanel.getSelectionModel(), records = selectionModel.getSelection();
		if(records && records.length > 0){
			var node = records[0];
			var index = node.parentNode.indexOf(node);
			node.parentNode.insertChild(index + 2,node);
			treePanel.view.refresh();
			this.onMoveUpOrDown(node);
		}else{
			Ext.tipbox.msg('提示','请选择要移动的节点');
		}
	},
	
	onTreeNodeDeselect : function(){
		var privilegeManagerPanel = this.getPrivilegeManager(),viewPanel = privilegeManagerPanel.viewPanel;
		privilegeManagerPanel.eastPanel.getLayout().setActiveItem(viewPanel);
		viewPanel.tplInfo.overwrite(viewPanel.body,{});
	},
	
	onTreeNodeSelect : function(treePanel,record,index){
		var privilegeManagerPanel = this.getPrivilegeManager(), viewPanel = privilegeManagerPanel.viewPanel;
		privilegeManagerPanel.eastPanel.getLayout().setActiveItem(viewPanel);
		
		viewPanel.tpl.overwrite(viewPanel.body,{
			parentid : record.data.parentid,
			parentname : record.parentNode.data.text,
			privilegeid : record.data.privilegeid,
			menutext : record.data.text,
			menulevel : record.data.menulevel,
			status : record.data.status,
			iconCls : record.data.iconCls,
			xtype : record.data.xtype,
			controller : record.data.controller
		});
	},
	
	//展开之前，记录滚动条的位置，展开之后再滚动到相应的位置
	onTreeNodeBeforeExpand : function(){
		var privilegeManagerPanel = this.getPrivilegeManager(), treePanel = privilegeManagerPanel.treePanel;
		this.scrollTopPos = treePanel.getView().el.getScrollTop();
	},
	
	onTreeNodeExpand : function(node){
		var privilegeManagerPanel = this.getPrivilegeManager(), treePanel = privilegeManagerPanel.treePanel;
		this.nodePathMap.add(node.data.privilegeid,node.getPath());
		//展开后保持滚动条在原来的位置
		Ext.defer(function(){
			treePanel.getView().el.scrollTo('top',this.scrollTopPos);
		},300,this);
		
		this.nodePathMap.eachKey(function(key,item,index){
			treePanel.expandPath(item); 
		},this);
		
	},
	
	onTreeNodeCollapse : function(node){
		var currentPath = node.getPath(), removed = [];
		this.nodePathMap.eachKey(function(key,item,index){
			if(Ext.String.startsWith(item,currentPath)){
				removed.push(key);
			}
		},this);
		for(var i=0;i<removed.length;i++){
			this.nodePathMap.removeAtKey(removed[i]);
		}
	},
	
	onDropView : function(panel){
		var me = this, privilegeManagerPanel = this.getPrivilegeManager(), treePanel = privilegeManagerPanel.treePanel;
		new Ext.dd.DropTarget(panel.body, {
            ddGroup: 'functionMenuDDGroup',
            notifyEnter: function(ddSource, e, data) {
                panel.body.stopAnimation();
                panel.body.highlight('C7EDCC');
            },
            notifyDrop: function(ddSource, e, data) {
                var selectedRecord = ddSource.dragData.records[0];
                me.onTreeNodeSelect(treePanel,selectedRecord);
                return true;
            }
        });
	},
	
	onFilterTree : function(field){
		var v = field.getValue();
		if(!Ext.isEmpty(v)){
			field.triggerCell.item(0).setDisplayed(true);
			this.onFilterBy(v,'text');
		}else{
			field.triggerCell.item(0).setDisplayed(false);
			this.onClearFilter();
		}
	},
	
	onFilterBy : function(text,by){
		var me = this,privilegeManagerPanel = this.getPrivilegeManager(), treePanel = privilegeManagerPanel.treePanel, view = treePanel.getView();
		this.onClearFilter();
 
        // 找到匹配的节点并展开.添加匹配的节点和他们的父节点到nodesAndParents数组.
		treePanel.getRootNode().expand(true,function(){
			treePanel.getRootNode().cascadeBy(function(tree, view) {
	            var currNode = this;
	            if (currNode && currNode.data[by] && currNode.data[by].toString().toLowerCase().indexOf(text.toLowerCase()) > -1) {
	                treePanel.expandPath(currNode.getPath());
	 
	                while (currNode.parentNode) {
	                    me.nodesAndParents.push(currNode.id);
	                    currNode = currNode.parentNode;
	                }
	            }
	        }, null, [treePanel, view]);
	 
	        // 将不在nodesAndParents数组中的节点隐藏
	        treePanel.getRootNode().cascadeBy(function(tree, view) {
	            var uiNode = view.getNodeByRecord(this);
	 
	            if (uiNode && !Ext.Array.contains(me.nodesAndParents, this.id)) {
	                Ext.get(uiNode).setDisplayed('none');
	            }
	        }, null, [treePanel, view]);

		},me);
	},
	
	onClearFilter : function(){
		var privilegeManagerPanel = this.getPrivilegeManager(), treePanel = privilegeManagerPanel.treePanel, view = treePanel.getView();
        treePanel.getRootNode().cascadeBy(function(tree, view) {
            var uiNode = view.getNodeByRecord(this);
            if (uiNode) {
                Ext.get(uiNode).setDisplayed('table-row');
            }
        }, null, [treePanel, view]);
        this.nodesAndParents = [];
	},
	
	onClearFilterTree : function(){
		var me = this, privilegeManagerPanel = this.getPrivilegeManager(), field = privilegeManagerPanel.treePanel.down('toolbar > isearchfield[name=pointname]');
		field.reset();
		field.triggerCell.item(0).setDisplayed(false);
		me.onClearFilter();
	},
	
	onTreePanelBeforeLoad : function(){
		var privilegeManagerPanel = this.getPrivilegeManager(), treePanel = privilegeManagerPanel.treePanel,
			field = treePanel.down('toolbar > isearchfield[name=privilegetext]');
		field.reset();
	}
	
});