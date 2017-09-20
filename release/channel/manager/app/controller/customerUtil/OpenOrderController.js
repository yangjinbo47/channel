Ext.define('CMS.controller.customerUtil.OpenOrderController', {
	extend : 'Ext.app.Controller',
	views : ['CMS.view.customerUtil.OpenOrderManager'],
	stores : ['CMS.store.customerUtil.OpenOrderStore'],
	refs : [{
		ref : 'customerUtilOpenOrderManagerView',
		selector : 'customerUtilOpenOrderManagerView'
	}],
	
	init : function() {
		this.control({
			'customerUtilOpenOrderManagerView > toolbar > button[name=search]' : {
				click : this.search
			},
			'customerUtilOpenOrderManagerView > toolbar > button[name=reset]' : {
				click : this.reset
			}
		});
	},
	
	search : function() {
		var me = this,grid = me.getCustomerUtilOpenOrderManagerView();
		var payPhoneFidld = grid.down('toolbar > textfield[name=payPhone]');
		
		var queryParams = {};
		queryParams.phone = payPhoneFidld.getValue();
		
		grid.getStore().proxy.extraParams = queryParams;
		grid.getStore().loadPage(1);
	},
	
	reset : function() {
		var me = this,grid = me.getCustomerUtilOpenOrderManagerView();
		var payPhoneFidld = grid.down('toolbar > textfield[name=payPhone]');
		
		grid.getStore().proxy.extraParams = {};
		grid.getStore().load();
		grid.getView().refresh();
		
		payPhoneFidld.reset();
	}
	
});