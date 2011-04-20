/*
 * Copyright 2000-2011 Enonic AS
 * http://www.enonic.com/license
 */
package com.enonic.cms.store.dao;

import com.enonic.cms.core.security.group.GroupKey;
import com.enonic.cms.core.structure.DefaultSiteAccessEntity;
import org.springframework.stereotype.Repository;

/**
 * Jul 8, 2009
 */
@Repository
public class DefaultSiteAccessEntityDao
    extends AbstractBaseEntityDao<DefaultSiteAccessEntity>
    implements DefaultSiteAccessDao
{
    public void deleteByGroupKey( GroupKey groupKey )
    {
        deleteByNamedQuery( "DefaultSiteAccessEntity.deleteByGroupKey", "groupKey", groupKey );
    }
}
