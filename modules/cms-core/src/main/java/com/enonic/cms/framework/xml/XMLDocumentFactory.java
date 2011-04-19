/*
 * Copyright 2000-2011 Enonic AS
 * http://www.enonic.com/license
 */
package com.enonic.cms.framework.xml;

import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.jdom.Document;

/**
 * This class implements the xml document factory.
 */
public final class XMLDocumentFactory
{
    /**
     * Create xml document based on string.
     */
    public static XMLDocument create( String doc )
        throws XMLException
    {
        return new XMLDocumentImpl( doc );
    }

    /**
     * Create xml document based on string.
     */
    public static XMLDocument create( String doc, String systemId )
        throws XMLException
    {
        XMLDocument xml = create( doc );
        xml.setSystemId( systemId );
        return xml;
    }

    /**
     * Create xml document based on string.
     */
    public static XMLDocument create( Reader input )
        throws XMLException
    {
        return new XMLDocumentImpl( XMLDocumentHelper.copyToString( input ) );
    }

    /**
     * Create xml document based on string.
     */
    public static XMLDocument create( byte[] xml, String encoding )
    {
        return create( xml, encoding, null );
    }

    /**
     * Create xml document based on string.
     */
    private static XMLDocument create( byte[] xml, String encoding, String systemId )
    {
        try
        {
            return create( new String( xml, encoding ), systemId );
        }
        catch ( UnsupportedEncodingException e )
        {
            throw new RuntimeException( e );
        }
    }

    /**
     * Create xml document based on W3C dom document.
     */
    public static XMLDocument create( org.w3c.dom.Document doc )
    {
        return new XMLDocumentImpl( doc );
    }

    /**
     * Create xml document based on JDOM document.
     */
    public static XMLDocument create( Document doc )
    {
        return new XMLDocumentImpl( doc );
    }

    public static Document _create( Reader input )
        throws XMLException
    {
        String str = XMLDocumentHelper.copyToString( input );
        return _create( str );
    }

    public static Document _create( String str )
        throws XMLException
    {
        return XMLDocumentHelper.convertToJDOMDocument( str );
    }
}
