/*
 * Copyright 2000-2011 Enonic AS
 * http://www.enonic.com/license
 */
package com.enonic.cms.core.security.userstore.connector.synchronize;

import javax.inject.Inject;

import com.enonic.cms.core.security.userstore.UserStoreConnectorManager;
import com.enonic.cms.core.security.userstore.UserStoreKey;
import com.enonic.cms.core.security.userstore.UserStoreService;
import com.enonic.cms.core.security.userstore.connector.remote.RemoteUserStoreConnector;

public class SynchronizeUserStoreJobFactory
{
    @Inject
    private UserStoreService userStoreService;

    @Inject
    private UserStoreConnectorManager userStoreConnectorManager;

    public SynchronizeUserStoreJob createSynchronizeUserStoreJob( final UserStoreKey userStoreKey, final SynchronizeUserStoreType type,
                                                                  final int batchSize )
    {
        final RemoteUserStoreConnector connector = userStoreConnectorManager.getRemoteUserStoreConnector( userStoreKey );

        if ( connector == null )
        {
            throw new IllegalStateException( "Can't perform synchronize with this userstore connector" );
        }

        if ( ( type == SynchronizeUserStoreType.GROUPS_ONLY || type == SynchronizeUserStoreType.USERS_AND_GROUPS ) &&
            !connector.canReadGroup() )
        {
            throw new IllegalStateException( "Can't synchronize groups with this userstore connector" );
        }

        final SynchronizeUserStoreJobImpl job = new SynchronizeUserStoreJobImpl( userStoreKey, type, batchSize );
        job.setUserStoreService( userStoreService );
        job.setUserStoreConnector( connector );
        return job;
    }
}
