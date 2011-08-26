Ext.define( 'CMS.controller.ShowPanelController', {
    extend: 'Ext.app.Controller',

    stores: [
        'UserstoreConfigStore',
        'UserstoreConnectorStore'
    ],
    models: [
        'UserstoreConfigModel',
        'UserstoreConnectorModel'
    ],
    views: [
        'ShowPanel',
        'UserstoreFormPanel'
    ],

    refs: [
        {ref: 'cmsTabPanel', selector: 'cmsTabPanel'},
        {ref: 'userstoreShow', selector: 'userstoreShow'},
        {ref: 'userstoreForm', selector: 'userstoreForm'},
        {ref: 'userstoreContextMenu', selector: 'userstoreContextMenu', autoCreate: true, xtype: 'userstoreContextMenu'}
    ],

    init: function()
    {
        this.application.on({
            createUserstoreTab: this.createUserstoreTab,
            scope: this
        });

        this.control( {
            'viewport': {
                afterrender: this.createBrowseTab
            }
            /*
            '*[action=newUserstore]': {
                click: function() {
                    this.createUserstoreTab( true );
                }
            },
            '*[action=editUserstore]': {
                click: function() {
                    this.createUserstoreTab( false );
                }
            },
            '*[action=deleteUserstore]': {
                click: this.notImplementedAction
            },
            '*[action=syncUserstore]': {
                click: this.notImplementedAction
            },
            '*[action=saveUserstore]': {
                click: this.saveUserstore
            },
            '*[action=cancelUserstore]': {
                click: this.closeUserstoreTab
            },
            'userstoreForm #defaultCheckbox': {
                change: this.handleDefaultChange
            },
            'userstoreForm textfield[name=name]': {
                keyup: this.handleUserstoreChange
            },
            'userstoreForm combobox[name=connectorName]': {
                change: this.handleConnectorChange
            },
            'cmsTabPanel': {
                tabchange: this.checkUserstoreDirty
            }
            */
        } );
    },

    createBrowseTab: function( component, options )
    {
        this.getCmsTabPanel().addTab( {
           id: 'tab-browse',
           title: 'Browse',
           closable: false,
           xtype: 'panel',
           layout: 'border',
           items: [
               {
                   region: 'center',
                   xtype: 'userstoreShow'
               }
           ]
        } );
    },

    handleDefaultChange: function( field, newValue, oldValue, options )
    {
        if ( newValue )
        {
            Ext.Msg.confirm( "Important", "Do you really want to set this userstore default ?", function( button )
            {
                if ( "no" == button )
                {
                    field.setValue( oldValue );
                }
            } );
        }
    },

    handleConnectorChange: function( field, newValue, oldValue, options )
    {
        var form = field.up( 'userstoreForm' );
        var newVals = form.userstore || { data: {} };
        var record = field.store.findRecord( field.valueField, newValue );
        if ( record )
        {
            newVals.data[ field.name ] = record.data[ field.displayField ];
            form.updateUserstoreHeader( newVals );
        }
    },

    handleUserstoreChange: function( field, evt, opts )
    {
        var form = field.up( 'userstoreForm' );
        var newVals = form.userstore || { data: {} };
        newVals.data[field.name] = field.getValue();
        form.updateUserstoreHeader( newVals );
        var tab = field.up( 'userstoreFormPanel' );
        tab.setTitle( newVals.data.name );
    },

    createUserstoreTab: function( forceNew )
    {
        var tabs = this.getTabs();
        if ( tabs )
        {
            var selection = this.getUserstoreGrid().getSelectionModel().getSelection();
            var userstore;

            if ( !forceNew && selection && selection.length > 0 ) {
                userstore = selection[0];
                var showPanel = this.getUserstoreShow();

                showPanel.el.mask( "Loading..." );
                Ext.Ajax.request( {
                    url: '/admin/data/userstore/config',
                    method: 'GET',
                    params: {
                        name: userstore.data.name
                    },
                    success: function( response )
                    {
                        var obj = Ext.decode( response.responseText, true );
                        // add missing fields for now
                        Ext.apply( obj, {
                            userCount: 231,
                            userPolicy: 'User Policy',
                            groupCount: 12,
                            groupPolicy: 'Group Policy',
                            lastModified: '2001-07-04 12:08:56',
                            plugin: 'Plugin Name'
                        });
                        showPanel.el.unmask();
                        tabs.addTab( {
                            xtype: 'userstoreFormPanel',
                            id: 'tab-userstore-' + userstore.data.key,
                            title: userstore.data.name,
                            userstore: {
                                data: obj
                            }
                        } );
                    }
                } );
            } else {
                tabs.addTab( {
                    xtype: 'userstoreFormPanel'
                } );
            }
        }
    },

    closeUserstoreTab: function( btn, evt, opts )
    {
        var tabs = this.getTabs();
        if ( tabs )
        {
            var tab = btn.up( 'userstoreFormPanel' );
            tabs.remove( tab, true );
        }
    },

    saveUserstore: function()
    {
        var form = this.getUserstoreForm().getForm();
        var tabs = this.getTabs();
        if ( form.isValid() )
        {
            form.submit( {
                 success: function( form, action )
                 {
                     Ext.Msg.alert( 'Success', action.result.msg  || 'Userstore has been saved.' );
                     tabs.userstoreDirty = true;
                 },
                 failure: function( form, action )
                 {
                     Ext.Msg.alert( 'Error',
                                    action.result.msg  || 'Userstore has not been saved.' );
                 }
             } );
        }
    },

    checkUserstoreDirty: function( tabPanel, newCard, oldCard, options )
    {
        /*
        if( newCard.id == 'tab-browse' && tabPanel.userstoreDirty ) {
            var iframe = this.getIframe();
            if ( iframe ) {
                var grid = iframe.Ext.getCmp('userstoreGridID');
                if ( grid ) {
                    grid.getStore().load({
                        callback: function(records, operation, success) {
                            tabPanel.userstoreDirty = false;
                        }
                    });
                }
            }
        }
        */
    },

    notImplementedAction: function ( btn, evt, opts )
    {
        Ext.Msg.alert( "Not implemented", btn.action + " is not implemented yet." );
    },

    /*
    updateDetailsPanel: function( selModel, selected )
    {
        var userstore = selected[0];
        var userstoreDetail = this.getUserstoreDetail();

        if ( userstore )
        {
            userstoreDetail.update( userstore.data );
            userstoreDetail.setCurrentUserstore( userstore.data );
        }

        userstoreDetail.setTitle( selected.length + " userstore selected" );
    },
    */

    getTabs: function() {
        // returns tabs if executed in the system scope
        var tabs = this.getCmsTabPanel();
        // returns tabs if executed inside the iframe of the system app
        if ( tabs == null && window.parent )
        {
            tabs = window.parent.Ext.getCmp( 'systemTabPanelID' );
        }
        return tabs;
    },

    getIframe: function() {
        var el = Ext.get('system-iframe');
        return el ? el.dom.contentWindow : null;
    }

} );
