/*
 * Copyright 2000-2011 Enonic AS
 * http://www.enonic.com/license
 */
package com.enonic.cms.core.service;

public interface KeyService
{
    public int generateNextKeySafe( String tableName );
}
