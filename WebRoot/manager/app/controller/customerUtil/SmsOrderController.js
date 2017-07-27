Ext.define('CMS.controller.customerUtil.SmsOrderController', {
	extend : 'Ext.app.Controller',
	views : ['CMS.view.customerUtil.SmsOrderManager'],
	stores : ['CMS.store.customerUtil.SmsOrderStore'],
	refs : [{
		ref : 'customerUtilSmsOrderManagerView',
		selector : 'customerUtilSmsOrderManagerView'
	}],
	
	init : function() {
		this.control({
			'customerUtilSmsOrderManagerView > toolbar > button[name=search]' : {
				click : this.search
			},
			'customerUtilSmsOrderManagerView > toolbar > button[name=reset]' : {
				click : this.reset
			}
		});
	},
	
	search : function() {
		var me = this,grid = me.getCustomerUtilSmsOrderManagerView();
		var payPhoneFidld = grid.down('toolbar > textfield[name=payPhone]');
		
		var queryParams = {};
		queryParams.phone = payPhoneFidld.getValue();
		
		grid.getStore().proxy.extraParams = queryParams;
		grid.getStore().loadPage(1);
	},
	
	reset : function() {
		var me = this,grid = me.getCustomerUtilSmsOrderManagerView();
		var payPhoneFidld = grid.down('toolbar > textfield[name=payPhone]');
		
		grid.getStore().proxy.extraParams = {};
		grid.getStore().load();
		grid.getView().refresh();
		
		payPhoneFidld.reset();
	}
	
});