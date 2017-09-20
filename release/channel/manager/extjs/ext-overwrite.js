/**
 * 此文件专门用于对extjs进行扩展或重写
 */
 
Ext.override(Ext.window.Window,{
	buttonAlign : 'center'
});

Ext.override(Ext.form.field.Base,{
	labelAlign : 'right'
});
Ext.override(Ext.form.CheckboxGroup,{
	labelAlign : 'right'
});


Ext.override(Ext.tree.Panel,{
	viewConfig : {
		loadMask : true
	}
});

Ext.override(Ext.grid.Panel,{
	viewConfig : {
		loadMask : true
	}
});



Ext.apply(Ext,{
	isIE11 : (navigator.userAgent.toLowerCase().toLowerCase().indexOf("trident") > -1 && navigator.userAgent.toLowerCase().indexOf("rv") > -1)
});
/**
 * 添加组件的vtype验证功能 
 */
Ext.apply(Ext.form.field.VTypes,{
    daterange: function(val, field) {
        var date = field.parseDate(val);

        if (!date) {
            return false;
        }
        if (field.startDateField && (!this.dateRangeMax || (date.getTime() != this.dateRangeMax.getTime()))) {
            var start = Ext.getCmp(field.startDateField);
            start.setMaxValue(date);
            start.validate();
            this.dateRangeMax = date;
        }
        else if (field.endDateField && (!this.dateRangeMin || (date.getTime() != this.dateRangeMin.getTime()))) {
            var end = Ext.getCmp(field.endDateField);
            end.setMinValue(date);
            end.validate();
            this.dateRangeMin = date;
        }

        return true;
    },
    daterangeText: '开始日期必须小于结束日期',
    
    mobile:function (value, field) {
        return /^(((1(3|5|7|8){1}))+\d{9})$/.test(value);
    },
    mobileText:'手机号码必须以13、15、17、18开头，且长度为11位',

    noallnumber : function(value,field){
    	return /[a-zA-Z]+[0-9]+$/.test(value);
    },
    noallnumberText : '不能为纯数字或纯字母，只能为字母和数字组合',
    
    nochinesechar : function(value,field){
    	return !(/[\u4e00-\u9fa5]/.test(value));
    },
    nochinesecharText : '输入字符中不能包含中文字符'
});

/**
 * console调试功能
 */
Ext.apply(Ext,{
	console : {
		level : 'info',//日志级级
		switcher : true,//是否打开日志开关
		log : function(value){
			if(this.switcher && this.level === 'log'){
				try{
					if(window.console && window.console.log){
						console.log(value);
					}
				}catch(e){ }
			}
		},
		
		info : function(value){
			if(this.switcher && this.level === 'info'){
				try{
					if(window.console && window.console.log){
						console.info(value);
					}
				}catch(e){ }
			}
		},
		
		debug : function(value){
			if(this.switcher && this.level === 'info'){
				try{
					if(window.console && window.console.log){
						console.debug(value);
					}
				}catch(e){ }
			}
		},
		
		warn : function(value){
			if(this.switcher && this.level === 'info'){
				try{
					if(window.console && window.console.log){
						console.warn(value);
					}
				}catch(e){ }
			}
		},
		
		error : function(value){
			if(this.switcher && this.level === 'info'){
				try{
					if(window.console && window.console.log){
						console.error(value);
					}
				}catch(e){ }
			}
		}
	}
});



Ext.define('Ext.picker.Date', {
    extend: 'Ext.Component',
    requires: [
        'Ext.XTemplate',
        'Ext.button.Button',
        'Ext.button.Split',
        'Ext.util.ClickRepeater',
        'Ext.util.KeyNav',
        'Ext.EventObject',
        'Ext.fx.Manager',
        'Ext.picker.Month'
    ],
    alias: 'widget.datepicker',
    alternateClassName: 'Ext.DatePicker',

    childEls: [
        'innerEl', 'eventEl', 'prevEl', 'nextEl', 'middleBtnEl','timerEl','footerEl'
    ],
    
    // border: true,

    renderTpl: [
        '<div id="{id}-innerEl" role="grid">',
            '<div role="presentation" class="{baseCls}-header">',
                 // the href attribute is required for the :hover selector to work in IE6/7/quirks
                '<a id="{id}-prevEl" class="{baseCls}-prev {baseCls}-arrow" href="#" role="button" title="{prevText}" hidefocus="on" ></a>',
                '<div class="{baseCls}-month" id="{id}-middleBtnEl">{%this.renderMonthBtn(values, out)%}</div>',
                 // the href attribute is required for the :hover selector to work in IE6/7/quirks
                '<a id="{id}-nextEl" class="{baseCls}-next {baseCls}-arrow" href="#" role="button" title="{nextText}" hidefocus="on" ></a>',
            '</div>',
            '<table id="{id}-eventEl" class="{baseCls}-inner" cellspacing="0" role="grid">',
                '<thead role="presentation"><tr role="row">',
                    '<tpl for="dayNames">',
                        '<th role="columnheader" class="{parent.baseCls}-column-header" title="{.}">',
                            '<div class="{parent.baseCls}-column-header-inner">{.:this.firstInitial}</div>',
                        '</th>',
                    '</tpl>',
                '</tr></thead>',
                '<tbody role="presentation"><tr role="row">',
                    '<tpl for="days">',
                        '{#:this.isEndOfWeek}',
                        '<td role="gridcell" id="{[Ext.id()]}">',
                            // the href attribute is required for the :hover selector to work in IE6/7/quirks
                            '<a role="presentation" hidefocus="on" class="{parent.baseCls}-date" href="#"></a>',
                        '</td>',
                    '</tpl>',
                '</tr></tbody>',
            '</table>',
            '<tpl if="showTimer">',
            	'<div id="{id}-timerEl" style="border:none;" role="presentation" class="{baseCls}-footer">{%this.renderTimer(values, out)%}</div>',
            '</tpl>',
            '<tpl if="showToday">',
                '<div id="{id}-footerEl" role="presentation" class="{baseCls}-footer">',

				'<tpl if="showTimer">',
            		'{%this.renderOKBtn(values, out)%}',
            	'</tpl>',

                '{%this.renderTodayBtn(values, out)%}</div>',
            '</tpl>',
        '</div>',{
            firstInitial: function(value) {
                return Ext.picker.Date.prototype.getDayInitial(value);
            },
            isEndOfWeek: function(value) {
                // convert from 1 based index to 0 based
                // by decrementing value once.
                value--;
                var end = value % 7 === 0 && value !== 0;
                return end ? '</tr><tr role="row">' : '';
            },
            renderTodayBtn: function(values, out) {
                Ext.DomHelper.generateMarkup(values.$comp.todayBtn.getRenderTree(), out);
            },
            renderMonthBtn: function(values, out) {
                Ext.DomHelper.generateMarkup(values.$comp.monthBtn.getRenderTree(), out);
            },
            renderTimer : function(values, out){
            	Ext.DomHelper.generateMarkup(values.$comp.timer.getRenderTree(), out);
            },
            renderOKBtn : function(values, out){
            	Ext.DomHelper.generateMarkup(values.$comp.okBtn.getRenderTree(), out);
            }
        }
    ],

    todayText : '今天',
    ariaTitle: '日期选择: {0}',
    ariaTitleDateFormat: 'F d, Y',
    todayTip : '{0} (Spacebar)',
    minText : '日期不能小于最小日期',
    maxText : '日期不能大于最大日期',
    disabledDaysText : 'Disabled',
    disabledDatesText : 'Disabled',
    nextText : '下月 (Control+Right)',
    prevText : '上月 (Control+Left)',
    monthYearText : '选择月份 (Control+Up/Down)',
    monthYearFormat: 'F Y',
    startDay : 0,
    showToday : true,

    showTimer : false,
    defaultTimeFormat : 'H:i:s',

    disableAnim: false,
    baseCls: Ext.baseCSSPrefix + 'datepicker',

    longDayFormat: 'F d, Y',
    focusOnShow: false,
    focusOnSelect: true,
    initHour: 12, // 24-hour format

    numDays: 42,

    initComponent : function() {
        var me = this, clearTime = Ext.Date.clearTime;

        me.selectedCls = me.baseCls + '-selected';
        me.disabledCellCls = me.baseCls + '-disabled';
        me.prevCls = me.baseCls + '-prevday';
        me.activeCls = me.baseCls + '-active';
        me.cellCls = me.baseCls + '-cell';
        me.nextCls = me.baseCls + '-prevday';
        me.todayCls = me.baseCls + '-today';
        
        
        if (!me.format) {
            me.format = Ext.Date.defaultFormat;
        }

        if(me.showTimer){
        	if(!me.timeFormat){
        		me.timeFormat = me.defaultTimeFormat;
        	}
        }

        if (!me.dayNames) {
            me.dayNames = Ext.Date.dayNames;
        }
        me.dayNames = me.dayNames.slice(me.startDay).concat(me.dayNames.slice(0, me.startDay));

        me.callParent();

        me.value = me.value ? clearTime(me.value, true) : clearTime(new Date());

        me.addEvents(
            'select'
        );

        me.initDisabledDays();
    },

    beforeRender: function () {
        var me = this, days = new Array(me.numDays),
            today = Ext.Date.format(new Date(), me.format);

        // If there's a Menu among our ancestors, then add the menu class.
        // This is so that the MenuManager does not see a mousedown in this Component as a document mousedown, outside the Menu
        if (me.up('menu')) {
            me.addCls(Ext.baseCSSPrefix + 'menu');
        }

        me.monthBtn = new Ext.button.Split({
            ownerCt: me,
            ownerLayout: me.getComponentLayout(),
            text: '',
            tooltip: me.monthYearText,
            listeners: {
                click: me.showMonthPicker,
                arrowclick: me.showMonthPicker,
                scope: me
            }
        });

        if (me.showToday) {
            me.todayBtn = new Ext.button.Button({
                ownerCt: me,
                ownerLayout: me.getComponentLayout(),
                text: Ext.String.format(me.todayText, today),
                tooltip: Ext.String.format(me.todayTip, today),
                tooltipType: 'title',
                handler: me.selectToday,
                scope: me
            });
        }

        if (me.showTimer){

        	me.timer = new Ext.ux.form.SpinnerTimeField({
        		ownerCt: me,
                ownerLayout: me.getComponentLayout(),
                fieldLabel : '时间',
                labelStyle : 'margin-left:5px;',
                labelWidth : 35,
                fieldStyle : 'width:100%',
                style : 'width:100%',
                timeFormat : me.timeFormat,
                value : Ext.Date.format(new Date(), me.timeFormat),
                scope: me
        	});

        	me.okBtn = new Ext.button.Button({
                ownerCt: me,
                ownerLayout: me.getComponentLayout(),
                text: '确定',
                tooltip: '确定选择日期',
                tooltipType: 'title',
                handler: me.selectDateTime,
                scope: me
            });
        }

        me.callParent();

        Ext.applyIf(me, {
            renderData: {}
        });

        Ext.apply(me.renderData, {
            dayNames: me.dayNames,
            showToday: me.showToday,
            showTimer: me.showTimer,
            prevText: me.prevText,
            nextText: me.nextText,
            days: days
        });

        me.protoEl.unselectable();
    },

    // Do the job of a container layout at this point even though we are not a Container.
    // TODO: Refactor as a Container.
    finishRenderChildren: function () {
        var me = this;
        
        me.callParent();
        me.monthBtn.finishRender();
        if (me.showToday) {
            me.todayBtn.finishRender();
        }
        if(me.showTimer){
        	me.timer.finishRender();
        	me.okBtn.finishRender();
        }
    },

    // @private
    // @inheritdoc
    onRender : function(container, position){
        var me = this;

        me.callParent(arguments);

        me.cells = me.eventEl.select('tbody td');
        me.textNodes = me.eventEl.query('tbody td a');
        
        me.mon(me.eventEl, {
            scope: me,
            mousewheel: me.handleMouseWheel,
            click: {
                fn: me.handleDateClick,
                delegate: 'a.' + me.baseCls + '-date'
            }
        });
        
    },

    // @private
    // @inheritdoc
    initEvents: function(){
        var me = this, eDate = Ext.Date, day = eDate.DAY;

        me.callParent();

        me.prevRepeater = new Ext.util.ClickRepeater(me.prevEl, {
            handler: me.showPrevMonth,
            scope: me,
            preventDefault: true,
            stopDefault: true
        });

        me.nextRepeater = new Ext.util.ClickRepeater(me.nextEl, {
            handler: me.showNextMonth,
            scope: me,
            preventDefault:true,
            stopDefault:true
        });

        me.keyNav = new Ext.util.KeyNav(me.eventEl, Ext.apply({
            scope: me,
            left : function(e){
                if(e.ctrlKey){
                    me.showPrevMonth();
                }else{
                    me.update(eDate.add(me.activeDate, day, -1));
                }
            },

            right : function(e){
                if(e.ctrlKey){
                    me.showNextMonth();
                }else{
                    me.update(eDate.add(me.activeDate, day, 1));
                }
            },

            up : function(e){
                if(e.ctrlKey){
                    me.showNextYear();
                }else{
                    me.update(eDate.add(me.activeDate, day, -7));
                }
            },

            down : function(e){
                if(e.ctrlKey){
                    me.showPrevYear();
                }else{
                    me.update(eDate.add(me.activeDate, day, 7));
                }
            },

            pageUp:function (e) {
                if (e.altKey) {
                    me.showPrevYear();
                } else {
                    me.showPrevMonth();
                }
            },

            pageDown:function (e) {
                if (e.altKey) {
                    me.showNextYear();
                } else {
                    me.showNextMonth();
                }
            },

            tab:function (e) {
                me.doCancelFieldFocus = true;
                me.handleTabClick(e);
                delete me.doCancelFieldFocus;
                return true;
            },
            
            enter : function(e){
                e.stopPropagation();
                return true;
            },

            //space: ???

            home:function (e) {
                me.update(eDate.getFirstDateOfMonth(me.activeDate));
            },

            end:function (e) {
                me.update(eDate.getLastDateOfMonth(me.activeDate));
            }
        }, me.keyNavConfig));

        if (me.showToday) {
            me.todayKeyListener = me.eventEl.addKeyListener(Ext.EventObject.SPACE, me.selectToday,  me);
        }
        me.update(me.value);
    },

    handleTabClick:function (e) {
        var me = this, t = me.getSelectedDate(me.activeDate), handler = me.handler;

        // The following code is like handleDateClick without the e.stopEvent()
        if (!me.disabled && t.dateValue && !Ext.fly(t.parentNode).hasCls(me.disabledCellCls)) {
            me.doCancelFocus = me.focusOnSelect === false;
            me.setValue(new Date(t.dateValue));
            delete me.doCancelFocus;
            me.fireEvent('select', me, me.value);
            if (handler) {
                handler.call(me.scope || me, me, me.value);
            }
            me.onSelect();
        }
    },

    getSelectedDate:function (date) {
        var me = this,
            t = date.getTime(),
            cells = me.cells,
            cls = me.selectedCls,
            cellItems = cells.elements,
            c,
            cLen = cellItems.length,
            cell;

        cells.removeCls(cls);

        for (c = 0; c < cLen; c++) {
            cell = Ext.fly(cellItems[c]);

            if (cell.dom.firstChild.dateValue == t) {
                return cell.dom.firstChild;
            }
        }
        return null;
    },

    /**
     * Setup the disabled dates regex based on config options
     * @private
     */
    initDisabledDays : function(){
        var me = this,
            dd = me.disabledDates,
            re = '(?:',
            len,
            d, dLen, dI;

        if(!me.disabledDatesRE && dd){
                len = dd.length - 1;

            dLen = dd.length;

            for (d = 0; d < dLen; d++) {
                dI = dd[d];

                re += Ext.isDate(dI) ? '^' + Ext.String.escapeRegex(Ext.Date.dateFormat(dI, me.format)) + '$' : dI;
                if (d != len) {
                    re += '|';
                }
            }

            me.disabledDatesRE = new RegExp(re + ')');
        }
    },

    /**
     * Replaces any existing disabled dates with new values and refreshes the DatePicker.
     * @param {String[]/RegExp} disabledDates An array of date strings (see the {@link #disabledDates} config for
     * details on supported values), or a JavaScript regular expression used to disable a pattern of dates.
     * @return {Ext.picker.Date} this
     */
    setDisabledDates : function(dd){
        var me = this;

        if(Ext.isArray(dd)){
            me.disabledDates = dd;
            me.disabledDatesRE = null;
        }else{
            me.disabledDatesRE = dd;
        }
        me.initDisabledDays();
        me.update(me.value, true);
        return me;
    },

    /**
     * Replaces any existing disabled days (by index, 0-6) with new values and refreshes the DatePicker.
     * @param {Number[]} disabledDays An array of disabled day indexes. See the {@link #disabledDays} config for details
     * on supported values.
     * @return {Ext.picker.Date} this
     */
    setDisabledDays : function(dd){
        this.disabledDays = dd;
        return this.update(this.value, true);
    },

    /**
     * Replaces any existing {@link #minDate} with the new value and refreshes the DatePicker.
     * @param {Date} value The minimum date that can be selected
     * @return {Ext.picker.Date} this
     */
    setMinDate : function(dt){
        this.minDate = dt;
        return this.update(this.value, true);
    },

    /**
     * Replaces any existing {@link #maxDate} with the new value and refreshes the DatePicker.
     * @param {Date} value The maximum date that can be selected
     * @return {Ext.picker.Date} this
     */
    setMaxDate : function(dt){
        this.maxDate = dt;
        return this.update(this.value, true);
    },

    /**
     * Sets the value of the date field
     * @param {Date} value The date to set
     * @return {Ext.picker.Date} this
     */
    setValue : function(value){
        // this.value = Ext.Date.clearTime(value, true);
        // return this.update(this.value);
        return this.setDateTimeValue(value);
    },

    setDateTimeValue : function(value){
    	if(this.showTimer && this.timer){
    		value = this.getDateTimeValue(value);
    	}
    	this.value = Ext.Date.clearTime(value, true);
        return this.update(this.value);
    },

    /**
     * Gets the current selected value of the date field
     * @return {Date} The selected date
     */
    getValue : function(){
        return this.getDateTimeValue();
    },

    //<locale type="function">
    /**
     * Gets a single character to represent the day of the week
     * @return {String} The character
     */
    getDayInitial: function(value){
        return value.substr(0,1);
    },
    //</locale>

    // @private
    focus : function(){
        this.update(this.activeDate);
    },

    // @private
    // @inheritdoc
    onEnable: function(){
        this.callParent();
        this.setDisabledStatus(false);
        this.update(this.activeDate);

    },

    // @private
    // @inheritdoc
    onDisable : function(){
        this.callParent();
        this.setDisabledStatus(true);
    },

    /**
     * Set the disabled state of various internal components
     * @private
     * @param {Boolean} disabled
     */
    setDisabledStatus : function(disabled){
        var me = this;

        me.keyNav.setDisabled(disabled);
        me.prevRepeater.setDisabled(disabled);
        me.nextRepeater.setDisabled(disabled);
        if (me.showToday) {
            me.todayKeyListener.setDisabled(disabled);
            me.todayBtn.setDisabled(disabled);
        }
    },

    /**
     * Get the current active date.
     * @private
     * @return {Date} The active date
     */
    getActive: function(){
        return this.activeDate || this.value;
    },

    /**
     * Run any animation required to hide/show the month picker.
     * @private
     * @param {Boolean} isHide True if it's a hide operation
     */
    runAnimation: function(isHide){
        var picker = this.monthPicker,
            options = {
                duration: 200,
                callback: function(){
                    if (isHide) {
                        picker.hide();
                    } else {
                        picker.show();
                    }
                }
            };

        if (isHide) {
            picker.el.slideOut('t', options);
        } else {
            picker.el.slideIn('t', options);
        }
    },

    /**
     * Hides the month picker, if it's visible.
     * @param {Boolean} [animate] Indicates whether to animate this action. If the animate
     * parameter is not specified, the behavior will use {@link #disableAnim} to determine
     * whether to animate or not.
     * @return {Ext.picker.Date} this
     */
    hideMonthPicker : function(animate){
        var me = this,
            picker = me.monthPicker;

        if (picker) {
            if (me.shouldAnimate(animate)) {
                me.runAnimation(true);
            } else {
                picker.hide();
            }
        }
        return me;
    },

    /**
     * Show the month picker
     * @param {Boolean} [animate] Indicates whether to animate this action. If the animate
     * parameter is not specified, the behavior will use {@link #disableAnim} to determine
     * whether to animate or not.
     * @return {Ext.picker.Date} this
     */
    showMonthPicker : function(animate){
        var me = this,
            picker;
        
        if (me.rendered && !me.disabled) {
            picker = me.createMonthPicker();
            picker.setValue(me.getActive());
            picker.setSize(me.getSize());
            picker.setPosition(-1, -1);
            if (me.shouldAnimate(animate)) {
                me.runAnimation(false);
            } else {
                picker.show();
            }
        }

        return me;
    },
    
    /**
     * Checks whether a hide/show action should animate
     * @private
     * @param {Boolean} [animate] A possible animation value
     * @return {Boolean} Whether to animate the action
     */
    shouldAnimate: function(animate){
        return Ext.isDefined(animate) ? animate : !this.disableAnim;
    },

    /**
     * Create the month picker instance
     * @private
     * @return {Ext.picker.Month} picker
     */
    createMonthPicker: function(){
        var me = this,
            picker = me.monthPicker;

        if (!picker) {
            me.monthPicker = picker = new Ext.picker.Month({
                renderTo: me.el,
                floating: true,
                shadow: false,
                small: me.showToday === false,
                listeners: {
                    scope: me,
                    cancelclick: me.onCancelClick,
                    okclick: me.onOkClick,
                    yeardblclick: me.onOkClick,
                    monthdblclick: me.onOkClick
                }
            });
            if (!me.disableAnim) {
                // hide the element if we're animating to prevent an initial flicker
                picker.el.setStyle('display', 'none');
            }
            me.on('beforehide', Ext.Function.bind(me.hideMonthPicker, me, [false]));
        }
        return picker;
    },

    /**
     * Respond to an ok click on the month picker
     * @private
     */
    onOkClick: function(picker, value){
        var me = this,
            month = value[0],
            year = value[1],
            date = new Date(year, month, me.getActive().getDate());

        if (date.getMonth() !== month) {
            // 'fix' the JS rolling date conversion if needed
            date = Ext.Date.getLastDateOfMonth(new Date(year, month, 1));
        }
        me.setValue(date);
        me.hideMonthPicker();
    },

    /**
     * Respond to a cancel click on the month picker
     * @private
     */
    onCancelClick: function(){
        // update the selected value, also triggers a focus
        this.selectedUpdate(this.activeDate);
        this.hideMonthPicker();
    },

    /**
     * Show the previous month.
     * @param {Object} e
     * @return {Ext.picker.Date} this
     */
    showPrevMonth : function(e){
        return this.setValue(Ext.Date.add(this.activeDate, Ext.Date.MONTH, -1));
    },

    /**
     * Show the next month.
     * @param {Object} e
     * @return {Ext.picker.Date} this
     */
    showNextMonth : function(e){
        return this.setValue(Ext.Date.add(this.activeDate, Ext.Date.MONTH, 1));
    },

    /**
     * Show the previous year.
     * @return {Ext.picker.Date} this
     */
    showPrevYear : function(){
        return this.setValue(Ext.Date.add(this.activeDate, Ext.Date.YEAR, -1));
    },

    /**
     * Show the next year.
     * @return {Ext.picker.Date} this
     */
    showNextYear : function(){
        return this.setValue(Ext.Date.add(this.activeDate, Ext.Date.YEAR, 1));
    },

    /**
     * Respond to the mouse wheel event
     * @private
     * @param {Ext.EventObject} e
     */
    handleMouseWheel : function(e){
        e.stopEvent();
        if(!this.disabled){
            var delta = e.getWheelDelta();
            if(delta > 0){
                this.showPrevMonth();
            } else if(delta < 0){
                this.showNextMonth();
            }
        }
    },

    /**
     * Respond to a date being clicked in the picker
     * @private
     * @param {Ext.EventObject} e
     * @param {HTMLElement} t
     */
    handleDateClick : function(e, t){
        var me = this, handler = me.handler;
        e.stopEvent();
        if(!me.disabled && t.dateValue && !Ext.fly(t.parentNode).hasCls(me.disabledCellCls)){
            me.doCancelFocus = me.focusOnSelect === false;
            me.setValue(this.getDateTimeValue(new Date(t.dateValue)));
            me.value = me.getValue();
            delete me.doCancelFocus;
            me.fireEvent('select', me, me.value);
            if (handler) {
                handler.call(me.scope || me, me, me.value);
            }
            // event handling is turned off on hide
            // when we are using the picker in a field
            // therefore onSelect comes AFTER the select
            // event.
            me.onSelect();
        }
    },

    getDateTimeValue : function(value){
    	var date = value || this.value;
    	if(this.showTimer && this.timer){
    		var time = this.timer.getValue() ,arr = time.split(':');
    		if(arr[0]){
    			date.setHours(arr[0]);
    		}
    		if(arr[1]){
    			date.setMinutes(arr[1]);
    		}
			if(arr[2]){
				date.setSeconds(arr[2]);
			}
    	}
        return date;
    },

    /**
     * Perform any post-select actions
     * @private
     */
    onSelect: function() {
        if (this.hideOnSelect) {
             this.hide();
         }
    },

    /**
     * Sets the current value to today.
     * @return {Ext.picker.Date} this
     */
    selectToday : function(){
        var me = this, btn = me.todayBtn, handler = me.handler;

        if(btn && !btn.disabled){
        	var date = Ext.Date.clearTime(new Date());

            me.setValue(this.getDateTimeValue(date));
            me.value = me.getValue();
            me.fireEvent('select', me, me.value);
            if (handler) {
                handler.call(me.scope || me, me, me.value);
            }
            me.onSelect();
        }
        return me;
    },

    selectDateTime : function(){
    	var me = this, date = this.getValue(),handler = me.handler;
    	var value = Ext.Date.clearTime(date);
        me.setValue(this.getDateTimeValue(value));
        me.value = me.getValue();
        me.fireEvent('select', me, me.value);
        if (handler) {
            handler.call(me.scope || me, me, me.value);
        }
        me.onSelect();
        return me;
    },

    /**
     * Update the selected cell
     * @private
     * @param {Date} date The new date
     */
    selectedUpdate: function(date){
        var me        = this,
            t         = date.getTime(),
            cells     = me.cells,
            cls       = me.selectedCls,
            cellItems = cells.elements,
            c,
            cLen      = cellItems.length,
            cell;

        cells.removeCls(cls);

        for (c = 0; c < cLen; c++) {
            cell = Ext.fly(cellItems[c]);

            if (cell.dom.firstChild.dateValue == t) {
                me.fireEvent('highlightitem', me, cell);
                cell.addCls(cls);

                if(me.isVisible() && !me.doCancelFocus){
                    Ext.fly(cell.dom.firstChild).focus(50);
                }

                break;
            }
        }
    },

    /**
     * Update the contents of the picker for a new month
     * @private
     * @param {Date} date The new date
     */
    fullUpdate: function(date){
        var me = this,
            cells = me.cells.elements,
            textNodes = me.textNodes,
            disabledCls = me.disabledCellCls,
            eDate = Ext.Date,
            i = 0,
            extraDays = 0,
            visible = me.isVisible(),
            newDate = +eDate.clearTime(date, true),
            today = +eDate.clearTime(new Date()),
            min = me.minDate ? eDate.clearTime(me.minDate, true) : Number.NEGATIVE_INFINITY,
            max = me.maxDate ? eDate.clearTime(me.maxDate, true) : Number.POSITIVE_INFINITY,
            ddMatch = me.disabledDatesRE,
            ddText = me.disabledDatesText,
            ddays = me.disabledDays ? me.disabledDays.join('') : false,
            ddaysText = me.disabledDaysText,
            format = me.format,
            days = eDate.getDaysInMonth(date),
            firstOfMonth = eDate.getFirstDateOfMonth(date),
            startingPos = firstOfMonth.getDay() - me.startDay,
            previousMonth = eDate.add(date, eDate.MONTH, -1),
            longDayFormat = me.longDayFormat,
            prevStart,
            current,
            disableToday,
            tempDate,
            setCellClass,
            html,
            cls,
            formatValue,
            value;

        if (startingPos < 0) {
            startingPos += 7;
        }

        days += startingPos;
        prevStart = eDate.getDaysInMonth(previousMonth) - startingPos;
        current = new Date(previousMonth.getFullYear(), previousMonth.getMonth(), prevStart, me.initHour);

        if (me.showToday) {
            tempDate = eDate.clearTime(new Date());
            disableToday = (tempDate < min || tempDate > max ||
                (ddMatch && format && ddMatch.test(eDate.dateFormat(tempDate, format))) ||
                (ddays && ddays.indexOf(tempDate.getDay()) != -1));

            if (!me.disabled) {
                me.todayBtn.setDisabled(disableToday);
                me.todayKeyListener.setDisabled(disableToday);
            }
        }

        setCellClass = function(cell, cls){
            value = +eDate.clearTime(current, true);
            cell.title = eDate.format(current, longDayFormat);
            // store dateValue number as an expando
            cell.firstChild.dateValue = value;
            if(value == today){
                cls += ' ' + me.todayCls;
                cell.title = me.todayText;
                
                // Extra element for ARIA purposes
                me.todayElSpan = Ext.DomHelper.append(cell.firstChild, {
                    tag:'span',
                    cls: Ext.baseCSSPrefix + 'hide-clip',
                    html:me.todayText
                }, true);
            }
            if(value == newDate) {
                cls += ' ' + me.selectedCls;
                me.fireEvent('highlightitem', me, cell);
                if (visible && me.floating) {
                    Ext.fly(cell.firstChild).focus(50);
                }
            }

            if (value < min) {
                cls += ' ' + disabledCls;
                cell.title = me.minText;
            }
            else if (value > max) {
                cls += ' ' + disabledCls;
                cell.title = me.maxText;
            }
            else if (ddays && ddays.indexOf(current.getDay()) !== -1){
                cell.title = ddaysText;
                cls += ' ' + disabledCls;
            }
            else if (ddMatch && format){
                formatValue = eDate.dateFormat(current, format);
                if(ddMatch.test(formatValue)){
                    cell.title = ddText.replace('%0', formatValue);
                    cls += ' ' + disabledCls;
                }
            }
            cell.className = cls + ' ' + me.cellCls;
        };

        for(; i < me.numDays; ++i) {
            if (i < startingPos) {
                html = (++prevStart);
                cls = me.prevCls;
            } else if (i >= days) {
                html = (++extraDays);
                cls = me.nextCls;
            } else {
                html = i - startingPos + 1;
                cls = me.activeCls;
            }
            textNodes[i].innerHTML = html;
            current.setDate(current.getDate() + 1);
            setCellClass(cells[i], cls);
        }

        me.monthBtn.setText(Ext.Date.format(date, me.monthYearFormat));
    },

    /**
     * Update the contents of the picker
     * @private
     * @param {Date} date The new date
     * @param {Boolean} forceRefresh True to force a full refresh
     */
    update : function(date, forceRefresh){
        var me = this,
            active = me.activeDate;

        if (me.rendered) {
            me.activeDate = date;
            if(!forceRefresh && active && me.el && active.getMonth() == date.getMonth() && active.getFullYear() == date.getFullYear()){
                me.selectedUpdate(date, active);
            } else {
                me.fullUpdate(date, active);
            }
        }
        return me;
    },

    // @private
    // @inheritdoc
    beforeDestroy : function() {
        var me = this;

        if (me.rendered) {
            Ext.destroy(
                me.todayKeyListener,
                me.keyNav,
                me.monthPicker,
                me.monthBtn,
                me.nextRepeater,
                me.prevRepeater,
                me.todayBtn,
                me.timer,
                me.okBtn
            );
            delete me.textNodes;
            delete me.cells.elements;
        }
        me.callParent();
    },

    // @private
    // @inheritdoc
    onShow: function() {
        this.callParent(arguments);
        if (this.focusOnShow) {
            this.focus();
        }

        if(this.showTimer && this.timer){
        	this.timer.triggerWrap.setStyle('width','95%');
//        	this.timer.updateTime();
        }
    }
});











/**
 * 图片查看实际尽寸
 */
Ext.define('CMS.extensions.ImageViewer',{
	extend : 'Ext.tip.Tip',
	closable : true,
	autoScroll : true,
	maxWidth : 800,
	maxHeight : 560,
	minWidth : 120,
	minHeight : 120,
	modal : true,
	header : {
		cls : 'imageviewer-header'
	},
	cls : 'imageviewer',
	bodyCls : 'imageviewer-body',
	messageCls : '',
	docBody : Ext.getBody(),
	initComponent : function(){
		this.callParent(arguments);
		delete this.title;
		
		this.on({
			scope : this,
			show : this.handlerShowEvent,
			hide : this.handlerHideEvent
		});
	},
	
	handlerShowEvent : function(){
		Ext.getBody().on('click',this.onViewHide,this);
	},
	
	handlerHideEvent : function(){
		Ext.getBody().un('click',this.onViewHide,this);
	},
	
	onViewHide : function(e){
		if(e && e.target){
			if(e.target.src != this.src){
				this.hide();
			}
		}
		
		if(this.modal){
        	this.docBody.unmask();
        }
	},
	
	afterRender : function(){
		this.callParent(arguments);

		if(this.message){
			this.messageEl = Ext.DomHelper.insertAfter(this.body,this.getMessageHtml(),true);
		}
	},
	
	onDocClick : function(e){
		if(this.isVisible()){
			this.hide();
		}
	},
	
	setSrc : function(src){
		this.src = src;
		
		var img = new Image();
		img.src = src;
		
		this.update(Ext.String.format('<img style="margin-top:30px;" src="resources/img/loading.gif" />',src));
		Ext.defer(function(){
			var cWidth = Math.min(img.width,this.maxWidth);
			var cHeight = Math.min(img.height,this.maxHeight);
			if(Ext.isChrome){
				this.setWidth(cWidth + 5);
				this.setHeight(cHeight + 25);
			}
			this.update(Ext.String.format('<img src="{0}" />',src));
			this.el.center(document.body);
		},150,this);
		return this;
	},
	
	setMessage : function(message){
		this.message = message;
		if(this.messageEl){
			this.messageEl.update(this.getMessageHtml(message));
		}
		return this;
	},
	
	setTitle : Ext.emptyFn,
	
	getMessageHtml : function(message){
		var msg = message || this.message;
		return Ext.String.format('<div class="imageviewer-title {0}"><span class="imageviewer-title-inner">{1}</span></div>',this.messageCls,msg);
	},
	
	// private
	doClose: function() {
        this.callParent(arguments);
        if(this.modal){
        	this.docBody.unmask();
        }
    },
	
	// private
	onShow : function(){
		this.callParent(arguments);
		if(this.modal){
			this.docBody.mask();
		}
	}
	
});

/**
 * 提示框
 */
Ext.tipbox = function(){
    var msgCt;

    function createBox(t, s){
       // return ['<div class="msg">',
       //         '<div class="x-box-tl"><div class="x-box-tr"><div class="x-box-tc"></div></div></div>',
       //         '<div class="x-box-ml"><div class="x-box-mr"><div class="x-box-mc"><h3>', t, '</h3>', s, '</div></div></div>',
       //         '<div class="x-box-bl"><div class="x-box-br"><div class="x-box-bc"></div></div></div>',
       //         '</div>'].join('');
       return '<div class="msg"><h3>' + t + '</h3><p>' + s + '</p></div>';
    }
    return {
        msg : function(title, format,delay){
            if(!msgCt){
                msgCt = Ext.DomHelper.insertFirst(document.body, {id:'msg-div'}, true);
            }
            var s = Ext.String.format.apply(String, Array.prototype.slice.call(arguments, 1));
            var m = Ext.DomHelper.append(msgCt, createBox(title, s), true);
            m.hide();
            m.slideIn('t').ghost("t", { delay: delay || 2000, remove: true});
        },

        init : function(){
            if(!msgCt){
                // It's better to create the msg-div here in order to avoid re-layouts 
                // later that could interfere with the HtmlEditor and reset its iFrame.
                msgCt = Ext.DomHelper.insertFirst(document.body, {id:'msg-div'}, true);
            }
//            var t = Ext.get('exttheme');
//            if(!t){ // run locally?
//                return;
//            }
//            var theme = Cookies.get('exttheme') || 'aero';
//            if(theme){
//                t.dom.value = theme;
//                Ext.getBody().addClass('x-'+theme);
//            }
//            t.on('change', function(){
//                Cookies.set('exttheme', t.getValue());
//                setTimeout(function(){
//                    window.location.reload();
//                }, 250);
//            });
//
//            var lb = Ext.get('lib-bar');
//            if(lb){
//                lb.show();
//            }
        }
    };
}();
Ext.onReady(Ext.tipbox.init, Ext.tipbox);
// old school cookie functions
var Cookies = {};
Cookies.set = function(name, value){
     var argv = arguments;
     var argc = arguments.length;
     var expires = (argc > 2) ? argv[2] : null;
     var path = (argc > 3) ? argv[3] : '/';
     var domain = (argc > 4) ? argv[4] : null;
     var secure = (argc > 5) ? argv[5] : false;
     document.cookie = name + "=" + escape (value) +
       ((expires == null) ? "" : ("; expires=" + expires.toGMTString())) +
       ((path == null) ? "" : ("; path=" + path)) +
       ((domain == null) ? "" : ("; domain=" + domain)) +
       ((secure == true) ? "; secure" : "");
};
Cookies.get = function(name){
	var arg = name + "=";
	var alen = arg.length;
	var clen = document.cookie.length;
	var i = 0;
	var j = 0;
	while(i < clen){
		j = i + alen;
		if (document.cookie.substring(i, j) == arg)
			return Cookies.getCookieVal(j);
		i = document.cookie.indexOf(" ", i) + 1;
		if(i == 0)
			break;
	}
	return null;
};
Cookies.clear = function(name) {
  if(Cookies.get(name)){
    document.cookie = name + "=" +
    "; expires=Thu, 01-Jan-70 00:00:01 GMT";
  }
};
Cookies.getCookieVal = function(offset){
   var endstr = document.cookie.indexOf(";", offset);
   if(endstr == -1){
       endstr = document.cookie.length;
   }
   return unescape(document.cookie.substring(offset, endstr));
};



/**
 * Plugin for adding a close context menu to tabs. Note that the menu respects
 * the closable configuration on the tab. As such, commands like remove others
 * and remove all will not remove items that are not closable.
 */
Ext.define('Ext.tab.plugin.TabCloseMenu', {
    alias: 'plugin.tabclosemenu',

    mixins: {
        observable: 'Ext.util.Observable'
    },

    /**
     * @cfg {String} closeTabText
     * The text for closing the current tab.
     */
    closeTabText: 'Close Tab',

    /**
     * @cfg {Boolean} showCloseOthers
     * Indicates whether to show the 'Close Others' option.
     */
    showCloseOthers: true,

    /**
     * @cfg {String} closeOthersTabsText
     * The text for closing all tabs except the current one.
     */
    closeOthersTabsText: 'Close Other Tabs',

    /**
     * @cfg {Boolean} showCloseAll
     * Indicates whether to show the 'Close All' option.
     */
    showCloseAll: true,

    /**
     * @cfg {String} closeAllTabsText
     * The text for closing all tabs.
     */
    closeAllTabsText: 'Close All Tabs',

    /**
     * @cfg {Array} extraItemsHead
     * An array of additional context menu items to add to the front of the context menu.
     */
    extraItemsHead: null,

    /**
     * @cfg {Array} extraItemsTail
     * An array of additional context menu items to add to the end of the context menu.
     */
    extraItemsTail: null,

    //public
    constructor: function (config) {
        this.addEvents(
            'aftermenu',
            'beforemenu');

        this.mixins.observable.constructor.call(this, config);
    },

    init : function(tabpanel){
        this.tabPanel = tabpanel;
        this.tabBar = tabpanel.down("tabbar");

        this.mon(this.tabPanel, {
            scope: this,
            afterlayout: this.onAfterLayout,
            single: true
        });
    },

    onAfterLayout: function() {
        this.mon(this.tabBar.el, {
            scope: this,
            contextmenu: this.onContextMenu,
            delegate: '.x-tab'
        });
    },

    onBeforeDestroy : function(){
        Ext.destroy(this.menu);
        this.callParent(arguments);
    },

    // private
    onContextMenu : function(event, target){
        var me = this,
            menu = me.createMenu(),
            disableAll = true,
            disableOthers = true,
            tab = me.tabBar.getChildByElement(target),
            index = me.tabBar.items.indexOf(tab);

        me.item = me.tabPanel.getComponent(index);
        menu.child('*[text="' + me.closeTabText + '"]').setDisabled(!me.item.closable);

        if (me.showCloseAll || me.showCloseOthers) {
            me.tabPanel.items.each(function(item) {
                if (item.closable) {
                    disableAll = false;
                    if (item != me.item) {
                        disableOthers = false;
                        return false;
                    }
                }
                return true;
            });

            if (me.showCloseAll) {
                menu.child('*[text="' + me.closeAllTabsText + '"]').setDisabled(disableAll);
            }

            if (me.showCloseOthers) {
                menu.child('*[text="' + me.closeOthersTabsText + '"]').setDisabled(disableOthers);
            }
        }

        event.preventDefault();
        me.fireEvent('beforemenu', menu, me.item, me);

        menu.showAt(event.getXY());
    },

    createMenu : function() {
        var me = this;

        if (!me.menu) {
            var items = [{
                text: me.closeTabText,
                scope: me,
                handler: me.onClose
            }];

            if (me.showCloseAll || me.showCloseOthers) {
                items.push('-');
            }

            if (me.showCloseOthers) {
                items.push({
                    text: me.closeOthersTabsText,
                    scope: me,
                    handler: me.onCloseOthers
                });
            }

            if (me.showCloseAll) {
                items.push({
                    text: me.closeAllTabsText,
                    scope: me,
                    handler: me.onCloseAll
                });
            }

            if (me.extraItemsHead) {
                items = me.extraItemsHead.concat(items);
            }

            if (me.extraItemsTail) {
                items = items.concat(me.extraItemsTail);
            }

            me.menu = Ext.create('Ext.menu.Menu', {
                items: items,
                listeners: {
                    hide: me.onHideMenu,
                    scope: me
                }
            });
        }

        return me.menu;
    },

    onHideMenu: function () {
        var me = this;

        me.item = null;
        me.fireEvent('aftermenu', me.menu, me);
    },

    onClose : function(){
        this.tabPanel.remove(currentItem);
    },

    onCloseOthers : function(){
        this.doClose(true);
    },

    onCloseAll : function(){
        this.doClose(false);
    },

    doClose : function(excludeActive){
        var items = [];

        this.tabPanel.items.each(function(item){
            if(item.closable){
                if(!excludeActive || item != currentItem){
                    items.push(item);
                }
            }
        }, this);

        Ext.each(items, function(item){
            this.tabPanel.remove(item);
        }, this);
    }
});




/**
 * UX used to provide a spotlight around a specified component/element.
 */
Ext.define('Ext.ux.Spotlight', {
    /**
     * @private
     * The baseCls for the spotlight elements
     */
    baseCls: 'x-spotlight',

    /**
     * @cfg animate {Boolean} True to animate the spotlight change
     * (defaults to true)
     */
    animate: true,

    /**
     * @cfg duration {Integer} The duration of the animation, in milliseconds
     * (defaults to 250)
     */
    duration: 250,

    /**
     * @cfg easing {String} The type of easing for the spotlight animatation
     * (defaults to null)
     */
    easing: null,

    /**
     * @private
     * True if the spotlight is active on the element
     */
    active: false,
    
    constructor: function(config){
        Ext.apply(this, config);
    },

    /**
     * Create all the elements for the spotlight
     */
    createElements: function() {
        var me = this,
            baseCls = me.baseCls,
            body = Ext.getBody();

        me.right = body.createChild({
            cls: baseCls
        });
        me.left = body.createChild({
            cls: baseCls
        });
        me.top = body.createChild({
            cls: baseCls
        });
        me.bottom = body.createChild({
            cls: baseCls
        });

        me.all = Ext.create('Ext.CompositeElement', [me.right, me.left, me.top, me.bottom]);
    },

    /**
     * Show the spotlight
     */
    show: function(el, callback, scope) {
        var me = this;
        
        //get the target element
        me.el = Ext.get(el);

        //create the elements if they don't already exist
        if (!me.right) {
            me.createElements();
        }

        if (!me.active) {
            //if the spotlight is not active, show it
            me.all.setDisplayed('');
            me.active = true;
            Ext.EventManager.onWindowResize(me.syncSize, me);
            me.applyBounds(me.animate, false);
        } else {
            //if the spotlight is currently active, just move it
            me.applyBounds(false, false);
        }
    },

    /**
     * Hide the spotlight
     */
    hide: function(callback, scope) {
        var me = this;
        
        Ext.EventManager.removeResizeListener(me.syncSize, me);

        me.applyBounds(me.animate, true);
    },

    /**
     * Resizes the spotlight with the window size.
     */
    syncSize: function() {
        this.applyBounds(false, false);
    },

    /**
     * Resizes the spotlight depending on the arguments
     * @param {Boolean} animate True to animate the changing of the bounds
     * @param {Boolean} reverse True to reverse the animation
     */
    applyBounds: function(animate, reverse) {
        var me = this,
            box = me.el.getBox(),
            //get the current view width and height
            viewWidth = Ext.Element.getViewWidth(true),
            viewHeight = Ext.Element.getViewHeight(true),
            i = 0,
            config = false,
            from, to, clone;
            
        //where the element should start (if animation)
        from = {
            right: {
                x: box.right,
                y: viewHeight,
                width: (viewWidth - box.right),
                height: 0
            },
            left: {
                x: 0,
                y: 0,
                width: box.x,
                height: 0
            },
            top: {
                x: viewWidth,
                y: 0,
                width: 0,
                height: box.y
            },
            bottom: {
                x: 0,
                y: (box.y + box.height),
                width: 0,
                height: (viewHeight - (box.y + box.height)) + 'px'
            }
        };

        //where the element needs to finish
        to = {
            right: {
                x: box.right,
                y: box.y,
                width: (viewWidth - box.right) + 'px',
                height: (viewHeight - box.y) + 'px'
            },
            left: {
                x: 0,
                y: 0,
                width: box.x + 'px',
                height: (box.y + box.height) + 'px'
            },
            top: {
                x: box.x,
                y: 0,
                width: (viewWidth - box.x) + 'px',
                height: box.y + 'px'
            },
            bottom: {
                x: 0,
                y: (box.y + box.height),
                width: (box.x + box.width) + 'px',
                height: (viewHeight - (box.y + box.height)) + 'px'
            }
        };

        //reverse the objects
        if (reverse) {
            clone = Ext.clone(from);
            from = to;
            to = clone;
        }

        if (animate) {
            Ext.Array.forEach(['right', 'left', 'top', 'bottom'], function(side) {
                me[side].setBox(from[side]);
                me[side].animate({
                    duration: me.duration,
                    easing: me.easing,
                    to: to[side]
                });
            },
            this);
        } else {
            Ext.Array.forEach(['right', 'left', 'top', 'bottom'], function(side) {
                me[side].setBox(Ext.apply(from[side], to[side]));
                me[side].repaint();
            },
            this);
        }
    },

    /**
     * Removes all the elements for the spotlight
     */
    destroy: function() {
        var me = this;
        
        Ext.destroy(me.right, me.left, me.top, me.bottom);
        delete me.el;
        delete me.all;
    }
});





/**
 * 统计实时显示输入框里还剩下可以输入字符的个数
 * @class Ext.ux.NumberCount
 * @extends Ext.Component
 */
 Ext.define('Ext.ux.NumberCount',{
 	alias: 'plugin.numbercount',
    mixins: {
        observable: 'Ext.util.Observable'
    },
    maxLength : 256,
	height : 20,
	clear : false,
	init : function(field) {
		var me = this;
		this.maxLength = field.maxLength || this.maxLength;
		this.target = field;
		field.pluginInitValue = Ext.bind(this.onKeyUp,this);
		field.mon(field, 'render', function(){
			this.initExtendEl();

			Ext.override(field,{
				setValue : function(v){
			        this.callParent(arguments);
			        field.pluginInitValue.call();
			        return this;
			    }
			});
		}, this);
		
		field.reset = function(){
	        field.beforeReset();
	        field.setValue(me.originalValue);
	        field.clearInvalid();
	        delete field.wasValid;
	        me.onKeyUp();
		};
	},
	
	initExtendEl : function() {
		this.target.inputEl.setStyle('overflow-y','auto');//只有在需要时才出现纵向滚动条
		this.txtEl = null;
		
		if(Ext.isChrome){
			var node = document.createElement('div');
			node.setAttribute('class','x-form-item-label');
			node.setAttribute('style','display:none;color:#C0C0C0;width:45px;border-radius:3px;cursor:pointer;position:absolute;text-align:center;font-size:11px;border:none;z-index:10;');
			node.innerHTML = this.maxLength;
			document.getElementById(this.target.inputId).parentNode.appendChild(node);
			this.txtEl = Ext.get(node);
		}else{
			this.texthandlerEl = new Ext.Template(
				'<div class="x-form-item-label" style="display:none;color:#C0C0C0;border-radius:3px;width:45px;cursor:pointer;position:absolute;text-align:center;font-size:11px;border:none;z-index:10;">',
					this.maxLength, 
				'</div>');
			this.txtEl = this.texthandlerEl.insertAfter(this.target.inputEl, null, true);
		}
		
		
		this.txtEl.on('click', this.doClearValue, this);
		this.target.mon(this.target.inputEl, 'keyup', this.onKeyUp, this);
		this.target.mon(this.target.inputEl, 'change', this.onKeyUp, this);
		
		this.target.on({
			scope : this,
			resize : this.restrictPosition,
			blur : this.onTipboxBlur,
			focus : this.onTipboxFocus
		});
		
		this.restrictPosition();
	},
	
	onTipboxBlur : function(){
		if(this.txtEl){
			this.txtEl.setStyle({
				'color' : '#C0C0C0',
				'display' : 'none',
				'background-color' : 'transparent'
			});
		}
	},
	
	onTipboxFocus : function(){
		if(this.txtEl){
			this.txtEl.setStyle({
				'color' : '#000000',
				'display' : 'inherit',
				'background-color' : '#E0E0E0'
			});
			this.restrictPosition();
		}
	},
	
	restrictPosition : function(){
		if(this.txtEl){
			var txtElWidth = this.txtEl.getComputedWidth();
			this.txtEl.alignTo(this.target.inputEl,'br-br?',[-20,-5]);
		}
	},

	doClearValue : function() {
		if (!this.clear || this.target.disabled || this.target.readOnly) {
			return;
		}
		this.target.setValue('');
		this.target.setRawValue('');
		this.target.clearInvalid();
	},

	doUpdateTxt : function(txt) {
		if(this.txtEl){
			this.txtEl.update(txt);
		}
	},
	
	reset : function(){
		this.callParent(arguments);
		this.onKeyUp();
	},

	onKeyUp : function() {
		this.target = this.target || this;
		var v = this.target.getValue() || this.target.value;
		if (!Ext.isEmpty(v)) {
			var length = this.maxLength - v.length;
			this.doUpdateTxt(length < 0?('溢出' + Math.abs(length)):length);
		} else {
			this.doUpdateTxt(this.maxLength);
		}
		this.restrictPosition();
	}
    
 });

Ext.define('Ext.ux.MultiComboBox', {
	extend : 'Ext.form.ComboBox',
	alias : 'widget.multicombobox',
	xtype : 'multicombobox',
	initComponent : function() {
		this.multiSelect = true;
		this.listConfig = {
			itemTpl : Ext.create('Ext.XTemplate','<input type=checkbox>{value}'),
			onItemSelect : function(record) {
				var node = this.getNode(record);
				if (node) {
					Ext.fly(node).addCls(this.selectedItemCls);

					var checkboxs = node.getElementsByTagName("input");
					if (checkboxs != null) {
						var checkbox = checkboxs[0];
						checkbox.checked = true;
					}
				}
			},
			listeners : {
				itemclick : function(view, record, item, index, e, eOpts) {
					var isSelected = view.isSelected(item);
					var checkboxs = item.getElementsByTagName("input");
					if (checkboxs != null) {
						var checkbox = checkboxs[0];
						if (!isSelected) {
							checkbox.checked = true;
						} else {
							checkbox.checked = false;
						}
					}
				}
			}
		}
		
		this.callParent();
	}
});