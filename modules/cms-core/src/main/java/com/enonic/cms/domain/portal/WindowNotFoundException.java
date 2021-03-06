/*
 * Copyright 2000-2011 Enonic AS
 * http://www.enonic.com/license
 */
package com.enonic.cms.domain.portal;

import com.enonic.cms.domain.NotFoundErrorType;
import com.enonic.cms.domain.Path;
import com.enonic.cms.domain.SiteKey;
import com.enonic.cms.domain.StacktraceLoggingUnrequired;
import com.enonic.cms.domain.structure.page.WindowKey;

public class WindowNotFoundException
    extends RuntimeException
    implements NotFoundErrorType, StacktraceLoggingUnrequired
{
    private SiteKey siteKey;

    private String path;

    private String message;

    public WindowNotFoundException( SiteKey siteKey, Path localPath, WindowKey windowKey )
    {
        this.siteKey = siteKey;
        this.path = localPath.toString();
        this.message = "Window [" + windowKey + "] in page '" + localPath + "' not found on site '" + siteKey + "'";
    }

    public SiteKey getSiteKey()
    {
        return siteKey;
    }

    public String getPath()
    {
        return path;
    }

    public String getMessage()
    {
        return message;
    }
}