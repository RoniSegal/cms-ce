/*
 * Copyright 2000-2011 Enonic AS
 * http://www.enonic.com/license
 */
package com.enonic.cms.business.portal.cache;

import com.enonic.cms.core.SiteKey;

public interface BaseCacheService
{

    void clearCache();

    SiteKey getSiteKey();

    boolean isEnabled();

    public int getDefaultTimeToLive();
}
