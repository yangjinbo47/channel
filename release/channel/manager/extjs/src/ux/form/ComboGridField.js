/**
 * 
 **/
Ext.define('Ext.ux.form.ComboGridField',{
	extend: 'Ext.form.field.Picker',
    xtype: 'combogridfield',
    
    uses: [
        'Ext.grid.Panel','Ext.ux.form.ISearchField'
    ],

    queryParamName : 'query',
	matchFieldWidth : false,
	multiSelect : false,
	
    triggerCls: Ext.baseCSSPrefix + 'form-arrow-trigger',
    config: {
        /**
         * @cfg {Ext.data.Store} store
         * A grid store that the grid picker will be bound to
         */
        store: null,

        /**
         * @cfg {String} displayField
         * The field inside the model that will be used as the combo display text.
         */
        displayField: null,

        /**
         * @cfg {String} valueField
         * The field inside the model that will be used as the combo value. It will submit to server
         */
        valueField : null,

        /**
         * @cfg {Array} columns
         * An optional array of columns for grid
         */
        columns: null,

        /**
         * @cfg {Boolean} selectOnTab
         * Whether the Tab key should select the currently highlighted item. Defaults to `true`.
         */
        selectOnTab: true,

        /**
         * @cfg {Number} maxPickerHeight
         * The maximum height of the grid dropdown. Defaults to 320.
         */
        maxPickerHeight: 320,

        /**
         * @cfg {Number} minPickerHeight
         * The minimum height of the grid dropdown. Defaults to 100.
         */
        minPickerHeight: 100
    },
   
    editable: false,

    initComponent: function() {
        var me = this;
        me.callParent(arguments);

        me.addEvents(
            /**
             * @event select
             * Fires when a grid record is selected
             * @param {Ext.ux.form.ComboGridField} picker        This grid picker
             * @param {Ext.data.Model} record           The selected record
             */
            'select',
            'beforeselect',
            'load',
            'expandpicker'
        );

        me.mon(me.store, {
            scope: me,
            load: me.onLoad
        });
    },

    /**
     * Creates and returns the grid panel to be used as this field's picker.
     */
    createPicker: function() {
        var me = this, topbar = Ext.create('Ext.toolbar.Toolbar',{
        	items : [{
            	xtype : 'isearchfield',
            	flex : 1,
            	emptyText : '输入内容查询',
            	onTrigger1Click : Ext.bind(me.onClearSearch,me),
            	onTrigger2Click : Ext.bind(me.onSearch,me)
            }]
        });

        Ext.each(me.columns,function(item,index,columns){
        	item.sortable = false;
        },this);

        var picker = Ext.create('Ext.grid.Panel',{
                shrinkWrapDock: 2,
                store: me.store,
                floating: true,
                autoScroll:true,
                displayField: me.displayField,
                columns: me.columns,
                height : me.maxPickerHeight,
                minHeight: me.minPickerHeight,
                maxHeight: me.maxPickerHeight,
                manageHeight: true,
                shadow: true,
                enableColumnHide : false,
                tbar : topbar,
                bbar : Ext.create('Ext.toolbar.Paging',{
                	store : me.store,
                	displayInfo : false
                }),
                listeners: {
                    scope: me,
                    itemclick: me.onItemClick,
                    columnmove : me.syncToolbarWidth,
                    columnresize : me.syncToolbarWidth
                },
                viewConfig: {
                	loadMask : true,
                    listeners: {
                        scope: me,
                        itemkeydown : me.onPickerKeypress
                    }
                }
            });
            view = picker.getView();

        if (Ext.isIE9 && Ext.isStrict) {
            // In IE9 strict mode, the tree view grows by the height of the horizontal scroll bar when the items are highlighted or unhighlighted.
            // Also when items are collapsed or expanded the height of the view is off. Forcing a repaint fixes the problem.
            view.on({
                scope: me,
                highlightitem: me.repaintPickerView,
                unhighlightitem: me.repaintPickerView,
                afteritemexpand: me.repaintPickerView,
                afteritemcollapse: me.repaintPickerView
            });
        }

        return picker;
    },


    onSearch : function(){
    	var searchfield = this.picker.down('toolbar > isearchfield');
    	var params = {};
    	params[this.queryParamName] = searchfield.getValue();
    	this.store.loadPage(1,{
    		scope : this,
    		params : params,
    		callback : function(){
    			searchfield.triggerCell.item(0).setDisplayed(true);
    		}
    	});
    },

    onClearSearch : function(){
    	var searchfield = this.picker.down('toolbar > isearchfield');
    	this.store.loadPage(1,{
    		scope : this,
    		callback : function(){
    			searchfield.triggerCell.item(0).setDisplayed(false);
    		}
    	});
    },
    

    /**
     * repaints the grid view
     */
    repaintPickerView: function() {
        var style = this.picker.getView().getEl().dom.style;

        // can't use Element.repaint because it contains a setTimeout, which results in a flicker effect
        style.display = style.display;
    },

    /**
     * Aligns the picker to the input element
     */
    alignPicker: function() {
        var me = this, picker, topbar;

        if (me.isExpanded) {
            picker = me.getPicker();
            if (me.matchFieldWidth) {
                // Auto the height (it will be constrained by max height)
                picker.setWidth(me.bodyEl.getWidth());
            }
            if (picker.isFloating()) {
                me.doAlign();
            }
        }
    },

    syncToolbarWidth : function(){
    	var picker = this.getPicker();
    	var width = picker.body.getWidth();
    	var toolbars = picker.getDockedItems('toolbar');

    	Ext.each(toolbars,function(item,index){
    		item.setWidth(width);
    	},this);
    },

    /**
     * Handles a click even on a grid record
     * @private
     * @param {Ext.grid.Panel} grid
     * @param {Ext.data.Model} record
     * @param {HTMLElement} item
     * @param {Number} The item's index
     * @param {Ext.EventObject} e
     */
    onItemClick: function(grid, record, item, index, e) {
        this.selectItem(record);
    },

    /**
     * Handles a keypress event on the picker element
     * @private
     * @param {Ext.EventObject} e
     * @param {HTMLElement} el
     */
    onPickerKeypress: function(view,record,item,index,e, el) {
        var key = e.getKey();
        if(key === e.ENTER || (key === e.TAB && this.selectOnTab)) {
            this.selectItem(record);
        }
    },

    /**
     * Changes the selection to a given record and closes the picker
     * @private
     * @param {Ext.data.Model} record
     */
    selectItem: function(record) {
        var me = this;
        if(me.fireEvent('beforeselect',me,record) === false){
        	return;
        }

        me.setValue(record.get(me.valueField));
//        me.picker.hide();
        me.collapse();
        me.inputEl.focus();
        me.fireEvent('select', me, record)

    },

    /**
     * Runs when the picker is expanded.  Selects the appropriate tree node based on the value of the input element,
     * and focuses the picker so that keyboard navigation will work.
     * @private
     */
    onExpand: function() {
        var me = this,
            picker = me.picker,
            store = picker.store,
            value = me.value,
            record;

        if(store.autoLoad === true || me.store.loaded === true){
        	me.fireEvent('expandpicker',me,picker,store);
        	if (value) {
	            record = store.find(me.valueField,value) || store.getAt(0);
	        }
	        
	        picker.getSelectionModel().select(record);
	        Ext.defer(function() {
	            picker.getView().focus();
	        }, 1);
        }else{
        	store.loadPage(1,{
        		scope : me,
        		callback : function(){
        			me.fireEvent('expandpicker',me,picker,store);
        		}
        	});
        }

        me.syncToolbarWidth();
    },

    /**
     * Sets the specified value into the field
     * @param {Mixed} value
     * @return {Ext.ux.form.ComboGridField} this
     */
    setValue: function(value) {
        var me = this, record;
        me.value = value;

        
        
        if (me.store.loading) {
            // Called while the Store is loading. Ensure it is processed by the onLoad method.
            return me;
        }
            
        // try to find a record in the store that matches the value
        if (value === undefined) {
//            record = me.store.getAt(0);
//            me.value = record.get(me.valueField);
        } else {
        	var index = me.store.find(me.valueField,value);
        	if(index != -1){
        		record = me.store.getAt(index);
        	}
        }

        // set the raw value to the record's display field if a record was found
        me.setRawValue(record ? record.get(me.displayField) : '');

        return me;
    },
    
    getSubmitValue: function(){
        return this.value;    
    },

    /**
     * Returns the current data value of the field (the idProperty of the record)
     * @return {Number}
     */
    getValue: function() {
        return this.value;
    },

    /**
     * Handles the store's load event.
     * @private
     */
    onLoad: function() {
        var value = this.value;
		this.store.loaded = true;
        if (value) {
            this.setValue(value);
        }
        this.fireEvent('load',this,this.store);
    }
});