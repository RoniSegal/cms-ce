Ext.define('CMS.controller.UserController', {
    extend: 'Ext.app.Controller',

    stores: ['UserStore'],
    models: ['UserModel'],
    views: [
        'user.GridPanel',
        'user.DetailPanel',
        'user.FilterPanel',
        'user.DeleteWindow',
        'user.ChangePasswordWindow',
        'user.ContextMenu'
    ],

    refs: [
        {ref: 'userStore', selector: 'UserStore'},
        {ref: 'userGrid', selector: 'userGrid'},
        {ref: 'userDetail', selector: 'userDetail'},
        {ref: 'userFilter', selector: 'userFilter'},
        {ref: 'filterTextField', selector: 'userFilter textfield[name=filter]'},
        {ref: 'userEditWindow', selector: 'userEditWindow', autoCreate: true, xtype: 'userEditWindow'},
        {ref: 'userDeleteWindow', selector: 'userDeleteWindow', autoCreate: true, xtype: 'userDeleteWindow'},
        {ref: 'userChangePasswordWindow', selector: 'userChangePassword', autoCreate: true, xtype: 'userChangePasswordWindow'},
        {ref: 'userContextMenu', selector: 'userContextMenu', autoCreate: true, xtype: 'userContextMenu'}
    ],

    init: function() {
        this.control({
            '*[action=newUser]': {
                click: this.newUser
            },
            '*[action=newGroup]': {
                click: this.newGroup
            },
            'userGrid': {
                selectionchange: this.updateInfo,
                itemcontextmenu: this.popupMenu,
                itemdblclick: this.showEditUserForm
            },
            'userFilter': {
                enterKeyPress: this.filterHandleEnterKey,
                render: this.onFilterPanelRender
            },
            'userFilter button[action=search]': {
                click: this.searchFilter
            },
            '*[action=showDeleteWindow]': {
                click: this.showDeleteUserWindow
            },
            '*[action=deleteUser]': {
                click: this.deleteUser
            },
            '*[action=edit]': {
                click: this.showEditUserForm
            },
            '*[action=changePassword]': {
                click: this.showChangePasswordWindow
            },
            'userDetail': {
                render: this.setDetailsToolbarDisabled
            }
        });
    },

    newUser: function() {
        Ext.Msg.alert('New User', 'TODO');
    },

    newGroup: function() {
        Ext.Msg.alert('New Group', 'TODO');
    },

    updateInfo: function(selModel, selected) {
        var user = selected[0];
        var userDetail = this.getUserDetail();

        if (user) {
            userDetail.update(user.data);
        }

        userDetail.setTitle(selected.length + " user selected");
        this.setDetailsToolbarDisabled();
    },

    selectUser: function(view) {
        var first = this.getUserStoreStore().getAt(0);
        if (first) {
            view.getSelectionModel().select(first);
        }
    },

    onFilterPanelRender: function() {
        Ext.getCmp('filter').focus(false, 10);
    },

    searchFilter: function() {
        var usersStore = this.getUserStoreStore();
        var textField = this.getFilterTextField();

        usersStore.clearFilter();
        usersStore.filter('displayName', textField.getValue());
        usersStore.loadPage(1);
    },

    filterHandleEnterKey: function( field, event ) {
        if ( event.getKey() == event.ENTER ) {
            this.searchFilter();
        }
    },

    showDeleteUserWindow: function() {
        this.getUserDeleteWindow().doShow(this.getUserGrid().getSelectionModel().selected.get(0));
    },

    deleteUser: function() {
        Ext.Msg.alert('Do Delete User', 'TODO');
    },

    showEditUserForm: function() {
        Ext.Msg.alert('Show Edit User Form', 'TODO');
    },

    showChangePasswordWindow: function() {
        this.getUserChangePasswordWindow().doShow(this.getUserGrid().getSelectionModel().selected.get(0));
    },

    popupMenu: function(view, rec, node, index, e) {
        e.stopEvent();
        this.getUserContextMenu().showAt(e.getXY());
        return false;
    },

    setDetailsToolbarDisabled: function() {
        var disable = !this.gridHasSelection();
        Ext.ComponentQuery.query('*[action=edit]')[0].setDisabled(disable);
        Ext.ComponentQuery.query('*[action=showDeleteWindow]')[0].setDisabled(disable);
        Ext.ComponentQuery.query('*[action=changePassword]')[0].setDisabled(disable);
    },

    gridHasSelection: function() {
        return this.getUserGrid().getSelectionModel().getSelection().length > 0;
    }

});