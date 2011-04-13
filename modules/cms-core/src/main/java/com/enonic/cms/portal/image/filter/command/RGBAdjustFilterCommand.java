/*
 * Copyright 2000-2011 Enonic AS
 * http://www.enonic.com/license
 */
package com.enonic.cms.portal.image.filter.command;

import com.enonic.cms.portal.image.filter.BuilderContext;
import com.jhlabs.image.RGBAdjustFilter;

public final class RGBAdjustFilterCommand
    extends FilterCommand
{
    public RGBAdjustFilterCommand()
    {
        super( "rgbadjust" );
    }

    protected Object doBuild( BuilderContext context, Object[] args )
    {
        double r = getDoubleArg( args, 0, 0.0 );
        double g = getDoubleArg( args, 1, 0.0 );
        double b = getDoubleArg( args, 2, 0.0 );

        return new RGBAdjustFilter( r, g, b );
    }
}