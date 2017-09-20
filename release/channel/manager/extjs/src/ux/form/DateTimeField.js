Ext.define('Ext.ux.form.DateTimeField', {
    extend: 'Ext.form.field.Date',
    alias: 'widget.datetimefield',
    showTimer : true,
    format : 'Y-m-d H:i:s',
    timeFormat : 'H:i:s',
    requires : ['Ext.ux.form.SpinnerTimeField'],
    createPicker: function() {
        var me = this, format = Ext.String.format;

        return new Ext.picker.Date({
            pickerField: me,
            ownerCt: me.ownerCt,
            renderTo: document.body,
            floating: true,
            hidden: true,
            focusOnShow: true,
            minDate: me.minValue,
            maxDate: me.maxValue,
            disabledDatesRE: me.disabledDatesRE,
            disabledDatesText: me.disabledDatesText,
            disabledDays: me.disabledDays,
            disabledDaysText: me.disabledDaysText,
            format: me.format,
            timeFormat : me.timeFormat,
            showToday: me.showToday,
            showTimer : me.showTimer,
            startDay: me.startDay,
            minText: format(me.minText, me.formatDate(me.minValue)),
            maxText: format(me.maxText, me.formatDate(me.maxValue)),
            listeners: {
                scope: me,
                select: me.onSelect
            },
            keyNavConfig: {
                esc: function() {
                    me.collapse();
                }
            }
        });
    }
});