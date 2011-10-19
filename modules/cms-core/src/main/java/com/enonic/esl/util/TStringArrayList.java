/*
 * Copyright 2000-2011 Enonic AS
 * http://www.enonic.com/license
 */
package com.enonic.esl.util;

import java.util.ArrayList;
import java.util.Arrays;

public class TStringArrayList
{
    private final ArrayList<String> strings = new ArrayList<String>();

    public void add( String string )
    {
        strings.add( string );
    }

    public void clear()
    {
        strings.clear();
    }

    public void add( String[] stringArray )
    {
        strings.addAll( Arrays.asList( stringArray ) );
    }

    public String[] toNativeArray()
    {
        return strings.toArray( new String[strings.size()] );
    }

    public boolean contains( String string )
    {
        return strings.contains( string );
    }

    public int size()
    {
        return strings.size();
    }

    public int indexOf( String string )
    {
        return strings.indexOf( string );
    }

    public void remove( String string )
    {
        strings.remove( string );
    }

    public String get( int index )
    {
        return strings.get( index );
    }

}
