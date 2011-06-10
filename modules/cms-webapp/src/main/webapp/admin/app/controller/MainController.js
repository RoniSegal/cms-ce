Ext.define('CMS.controller.MainController', {
    extend: 'Ext.app.Controller',

    views: ['main.Toolbar'],

    refs: [
        {ref: 'selectedAppLabel', selector: 'mainToolbar label[id=main-selected-application-label]'},
        {ref: 'startMenuButton', selector: 'mainToolbar button[id=main-start-button]'},
    ],

    init: function() {
        this.control({
            'viewport': {
                afterrender: this.loadDefaultApp
            },
            '*[id=main-start-button] menu > menuitem': {
                click: this.loadApp
            }
        });
    },

    loadDefaultApp: function(component, options) {
        var defaultApplication = this.getStartMenuButton().menu.items.items[0];
        this.loadApp(defaultApplication, null, null);
    },

    loadApp: function(item, e, options ) {
        this.updateSelectedAppLabel(item);

        if (item.appUrl === '') {
            Ext.Msg.alert(item.text + ' App', 'TODO');
            return;
        } // For now.

        this.getIframe().src = item.appUrl;
        this.setUrlFragment(item.text);
    },

    updateSelectedAppLabel: function(item) {
        var label = this.getSelectedAppLabel();
        var existingAppIconCls = label.el.dom.className.match(/icon-[a-z-_]+/g);
        if (existingAppIconCls) {
            label.removeCls(existingAppIconCls[0]);
        }

        label.addCls(item.iconCls);
        label.setText(item.text);
    },

    setUrlFragment: function(fragmentId) {
        window.location.hash = fragmentId;
    },

    getIframe: function() {
        return Ext.getDom('main-iframe');
    }

});