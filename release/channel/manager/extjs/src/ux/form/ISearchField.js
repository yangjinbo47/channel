Ext.define('Ext.ux.form.ISearchField', {
    extend: 'Ext.form.field.Trigger',
    alias: 'widget.isearchfield',
    trigger1Cls: Ext.baseCSSPrefix + 'form-clear-trigger',
    trigger2Cls: Ext.baseCSSPrefix + 'form-search-trigger',
    
    hideTrigger1 : false,//查询trigger
    hideTrigger2 : true,//清除查询trigger
    initComponent: function() {
        var me = this;

        me.callParent(arguments);
        me.on('specialkey', function(f, e){
            if (e.getKey() == e.ENTER) {
                me.onTrigger2Click();
            }
        });

    },

    afterRender: function(){
        this.callParent();
        this.triggerCell.item(0).setDisplayed(this.hideTrigger1);
        this.triggerCell.item(1).setDisplayed(this.hideTrigger2);
    },

    onTrigger1Click : Ext.emptyFn,

    onTrigger2Click : Ext.emptyFn
});