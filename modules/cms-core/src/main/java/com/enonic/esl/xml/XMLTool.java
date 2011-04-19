/*
 * Copyright 2000-2011 Enonic AS
 * http://www.enonic.com/license
 */
package com.enonic.esl.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.fileupload.FileItem;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.tidy.Configuration;
import org.w3c.tidy.Tidy;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.io.Closeables;

import com.enonic.esl.containers.ExtendedMap;
import com.enonic.esl.util.StringUtil;

/**
 * This class implements xml utility functions.
 */
public final class XMLTool
{
    private final static Tidy TIDY = new Tidy();

    private final static DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY =
            DocumentBuilderFactory.newInstance();

    private final static TransformerFactory TRANSFORMER_FACTORY =
            TransformerFactory.newInstance();

    private final static XPathFactory XPATH_FACTORY =
            XPathFactory.newInstance();

    static {
        TIDY.setBreakBeforeBR( false );
        TIDY.setCharEncoding( Configuration.UTF8 );
        TIDY.setMakeClean( false );
        TIDY.setDropEmptyParas( true );
        TIDY.setDropFontTags( false );
        TIDY.setEncloseBlockText( false );
        TIDY.setEncloseText( false );
        TIDY.setFixBackslash( true );
        TIDY.setFixComments( true );
        TIDY.setEmacs( false );
        TIDY.setHideEndTags( false );
        TIDY.setIndentContent( false );
        TIDY.setIndentAttributes( false );
        TIDY.setSpaces( 2 );
        TIDY.setXmlTags( false );
        TIDY.setLiteralAttribs( true );
        TIDY.setLogicalEmphasis( false );
        TIDY.setOnlyErrors( false );
        TIDY.setNumEntities( false );
        TIDY.setXHTML( true );
        TIDY.setXmlOut( true );
        TIDY.setQuiet( true );
        TIDY.setQuoteAmpersand( true );
        TIDY.setQuoteMarks( false );
        TIDY.setQuoteNbsp( true );
        TIDY.setRawOut( true );
        TIDY.setShowWarnings( false );
        TIDY.setBurstSlides( false );
        TIDY.setTidyMark( false );
        TIDY.setUpperCaseAttrs( false  );
        TIDY.setUpperCaseTags( false  );
        TIDY.setWord2000( false );
        TIDY.setWraplen( 0 );
        TIDY.setWrapAttVals( false );
        TIDY.setWrapScriptlets( false );
        TIDY.setWrapSection( false );
    }

    private static class ElementComparator
        implements Comparator
    {
        private final String orderByAttribute;

        private final boolean descending;

        private final boolean intSort;

        public ElementComparator( String orderByAttribute, boolean intSort, boolean descending )
        {
            this.orderByAttribute = orderByAttribute;
            this.intSort = intSort;
            this.descending = descending;
        }

        public int compare( Object a, Object b )
        {

            final String valueA = ( (Element) a ).getAttribute( orderByAttribute );
            final String valueB = ( (Element) b ).getAttribute( orderByAttribute );

            int result;
            if ( intSort )
            {
                result = Integer.parseInt( valueA ) - Integer.parseInt( valueB );
            }
            else
            {
                result = valueA.compareTo( valueB );
            }

            if ( descending )
            {
                return -result;
            }
            else
            {
                return result;
            }
        }

    }

    private XMLTool()
    {
    }

    private static DocumentBuilder getDocumentBuilder()
    {
        try {
            return DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
        } catch (Exception e) {
            throw new IllegalArgumentException( e );
        }
    }

    /**
     * Create a CDATA section in a XML DOM.
     *
     * @param doc   The owner document.
     * @param root  The element the CDATA should be created below.
     * @param cdata The CDATA content.
     * @return CDATASection
     */
    public static CDATASection createCDATASection( Document doc, Element root, String cdata )
    {

        CDATASection cdataSection;

        if ( cdata != null )
        {
            cdataSection = doc.createCDATASection( StringUtil.replaceECC( cdata ) );
            root.appendChild( cdataSection );
        }
        else
        {
            cdataSection = doc.createCDATASection( "" );
            root.appendChild( cdataSection );
        }

        return cdataSection;
    }


    /**
     * Create XML/XHTML nodes in a XML DOM from a HTML string. All
     * nodes inside the body tag are then inserted in the document passed as parameter.
     *
     * @param doc       The owner document.
     * @param root      The element the nodes should be created below.
     * @param html      The html code that should be inserted as nodes.
     * @param tidyFirst Tidy up the document before creating the nodes.
     */
    public static void createXHTMLNodes( Document doc, Element root, String html, boolean tidyFirst )
    {
        if ( html == null )
        {
            return;
        }

        if ( tidyFirst )
        {
            html = tidy( html );
        }

        html = html.trim();
        if ( html.length() == 0 )
        {
            return;
        }

        Document xhtml = domparse( html );

        NodeList xhtmlTags = xhtml.getElementsByTagName( "body" ).item( 0 ).getChildNodes();

        for ( int i = 0; i < xhtmlTags.getLength(); i++ )
        {
            root.appendChild( doc.importNode( xhtmlTags.item( i ), true ) );
        }
    }

    private static String tidy( String html )
    {
        try
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = new ByteArrayInputStream( html.getBytes( "UTF-8" ) );

            // Run Tidy on the input
            Document tidied = HTMLtoXML( in, out );
            html = XMLTool.documentToString( tidied, 4 );

            in.close();
            out.close();

            return html;
        }
        catch ( UnsupportedEncodingException e )
        {
            throw new IllegalArgumentException( e );
        }
        catch ( IOException e )
        {
            throw new IllegalArgumentException( "Could not close temporary streams used by Tidy", e );
        }
    }

    /**
     * Convert a HTML stream to a XML stream
     *
     * @param in  The HTML input stream
     * @param out The XML output stream
     * @return Document A Document containing the XML
     */
    public static org.w3c.dom.Document HTMLtoXML( InputStream in, OutputStream out )
    {
        return TIDY.parseDOM( in, out );
    }

    /**
     * Create an empty document.
     *
     * @return Document
     */
    public static Document createDocument()
    {
        return getDocumentBuilder().newDocument();
    }

    /**
     * Create a document with a top element.
     *
     * @param qualifiedName The name of the top element.
     * @return Document
     */
    public static Document createDocument( String qualifiedName )
    {

        return createDocument( null, qualifiedName, null );
    }

    /**
     * Create a new document with a top element in a specific namespace.
     *
     * @param namespaceURI  The namespace URI.
     * @param qualifiedName The name of the top element.
     * @param docType       DocumentType
     * @return Document
     */
    private static Document createDocument( String namespaceURI, String qualifiedName, DocumentType docType )
    {
        Document doc = createDocument();

        if ( docType != null )
        {
            doc.appendChild( docType );
        }
        if ( namespaceURI != null )
        {
            doc.appendChild( doc.createElementNS( namespaceURI, qualifiedName ) );
        }
        else
        {
            doc.appendChild( doc.createElement( qualifiedName ) );
        }

        return doc;
    }

    /**
     * Create a DOM element, containing the specified text.
     *
     * @param doc  Document
     * @param name The name of the element.
     * @param text The text.
     * @return Element
     */
    public static Element createElement( Document doc, String name, String text )
    {
        if ( name == null )
        {
            throw new IllegalArgumentException( "Element name cannot be null!" );
        }
        else if ( name.trim().length() == 0 )
        {
            throw new IllegalArgumentException( "Element name has to contain at least one character!" );
        }

        Element elem = doc.createElement( name );
        if ( text != null )
        {
            Text textNode = doc.createTextNode( text );
            elem.appendChild( textNode );
        }

        return elem;
    }

    public static Element createElement( Element root, String name )
    {

        return createElement( root.getOwnerDocument(), root, name, null );
    }

    /**
     * Create a DOM element below the specified element.
     *
     * @param doc  Document
     * @param root The parent element.
     * @param name The name of the new element.
     * @return Element
     */
    public static Element createElement( Document doc, Element root, String name )
    {

        return createElement( doc, root, name, null );
    }

    public static Element createElement( Document doc, Element root, String name, String text, String sortAttribute, String sortValue )
    {

        if ( name == null )
        {
            throw new IllegalArgumentException( "Element name cannot be null!" );
        }
        else if ( name.trim().length() == 0 )
        {
            throw new IllegalArgumentException( "Element name has to contain at least one character!" );
        }

        Element elem = doc.createElement( name );
        if ( text != null )
        {
            Text textNode = doc.createTextNode( StringUtil.getXMLSafeString( text ) );
            elem.appendChild( textNode );
        }

        if ( sortAttribute == null || sortValue == null )
        {
            root.appendChild( elem );
        }
        else
        {
            Element[] childElems = getElements( root );
            if ( childElems.length == 0 )
            {
                root.appendChild( elem );
            }
            else
            {
                int i = 0;
                for (; i < childElems.length; i++ )
                {
                    String childValue = childElems[i].getAttribute( sortAttribute );
                    if ( childValue != null && childValue.compareToIgnoreCase( sortValue ) >= 0 )
                    {
                        break;
                    }
                }

                if ( i < childElems.length )
                {
                    root.insertBefore( elem, childElems[i] );
                }
                else
                {
                    root.appendChild( elem );
                }
            }
        }

        return elem;
    }

    public static Element createElement( Document doc, Element root, String name, String text )
    {

        return createElement( doc, root, name, text, null, null );
    }

    public static Text createTextNode( Document doc, Element root, String text )
    {

        Text textNode;

        if ( text != null )
        {
            textNode = doc.createTextNode( text );
            root.appendChild( textNode );
        }
        else
        {
            textNode = doc.createTextNode( "" );
            root.appendChild( textNode );
        }

        return textNode;
    }

    private static byte[] documentToBytes( Document doc, int indent, String enc )
    {
        return documentToBytes( doc, indent, enc, false );
    }

    private static byte[] documentToBytes( Document doc, int indent, String enc, boolean preserveSpace )
    {
        try
        {
            String xml = documentToString( doc, indent, preserveSpace );
            if ( xml != null )
            {
                return xml.getBytes( enc );
            }
            else
            {
                return null;
            }
        }
        catch ( UnsupportedEncodingException uee )
        {
            return null;
        }
    }

    static public String documentToString( Document doc )
    {
        java.io.StringWriter swriter = new java.io.StringWriter();
        printDocument( swriter, doc );
        return swriter.toString();
    }

    static public String documentToString( Document doc, int indent )
    {
        return documentToString( doc, indent, false );
    }

    private static String documentToString( Document doc, int indent, boolean preserveSpace )
    {
        java.io.StringWriter swriter = new java.io.StringWriter();
        printDocument( swriter, doc, indent );
        String str = swriter.toString();

        if ( preserveSpace )
        {
            str = str.replaceAll( "&apos;", "'" );
        }

        return str;
    }

    public static Document domparse( InputStream in )
    {

        Document doc;
        InputSource inputSource = new InputSource( in );
        doc = domparse( inputSource, null );

        try
        {
            in.close();
        }
        catch ( IOException e )
        {
            throw new IllegalArgumentException( "Failed to close input stream", e );
        }

        return doc;
    }

    /**
     * Get an element's sub-elements as an Element array. Returns an empty array if root element is null or the element does not have any
     * sub-elements.
     *
     * @param root Element
     * @return Element[]
     */
    public static Element[] getElements( Element root )
    {
        ArrayList<Element> elements = getElementsAsList( root );
        if ( elements.size() > 0 )
        {
            return elements.toArray( new Element[elements.size()] );
        }
        else
        {
            return new Element[0];
        }
    }

    /**
     * Get an element's sub-elements as a list of Element objects. Returns an empty array if root element is null or the element does not
     * have any sub-elements.
     *
     * @param root Element
     * @return ArrayList
     */
    public static ArrayList<Element> getElementsAsList( Element root )
    {

        if ( root == null )
        {
            return new ArrayList<Element>();
        }

        ArrayList<Element> elements = new ArrayList<Element>();
        NodeList nodeList = root.getChildNodes();
        for ( int i = 0; i < nodeList.getLength(); i++ )
        {
            Node n = nodeList.item( i );
            if ( n.getNodeType() == Node.ELEMENT_NODE )
            {
                elements.add( (Element) n );
            }
        }

        return elements;
    }

    public static Element selectElement( Element elem, String xpath )
    {
        return (Element) selectNode( elem, xpath );
    }

    private static NodeList selectNodeList( Node node, String xpath )
    {
        try {
            final XPath xp = XPATH_FACTORY.newXPath();
            return (NodeList)xp.evaluate( xpath, node, XPathConstants.NODESET );
        } catch (Exception e) {
            throw new IllegalArgumentException( e );
        }
    }

    private static Node selectSingleNode( Node node, String xpath )
    {
        try {
            final XPath xp = XPATH_FACTORY.newXPath();
            return (Node)xp.evaluate( xpath, node, XPathConstants.NODE );
        } catch (Exception e) {
            throw new IllegalArgumentException( e );
        }
    }
    

    public static Element[] selectElements( Element elem, String xpath )
    {
        NodeList nl = selectNodeList(elem, xpath);
        Node[] elementNodes = filterNodes( nl, Node.ELEMENT_NODE );
        Element[] elements = new Element[elementNodes.length];
        for ( int i = 0; i < elements.length; i++ )
        {
            elements[i] = (Element) elementNodes[i];
        }

        return elements;
    }

    /**
     * Get an element's named sub-elements as an Element array. Returns an empty array if root element is null or the element does not have
     * any sub-elements or sub-elements with the specified name.
     *
     * @param root        Element
     * @param elementName String
     * @return Element[]
     */
    public static Element[] getElements( Element root, String elementName )
    {

        ArrayList<Element> elements = getElementsAsList( root, elementName );
        if ( elements.size() > 0 )
        {
            return elements.toArray( new Element[elements.size()] );
        }
        else
        {
            return new Element[0];
        }
    }

    /**
     * Get an element's named sub-elements as a list of Element objects. Returns an empty array if root element is null or the element does
     * not have any sub-elements or sub-elements with the specified name.
     *
     * @param root        Element
     * @param elementName The name of the element to look for inside the root.
     * @return All found elements.
     */
    private static ArrayList<Element> getElementsAsList( Element root, String elementName )
    {

        if ( root == null )
        {
            return new ArrayList<Element>();
        }

        ArrayList<Element> elements = new ArrayList<Element>();
        NodeList nodeList = root.getChildNodes();
        for ( int i = 0; i < nodeList.getLength(); i++ )
        {
            Node n = nodeList.item( i );
            if ( n != null && n.getNodeType() == Node.ELEMENT_NODE && elementName.equals( n.getNodeName() ) )
            {
                elements.add( (Element) n );
            }
        }

        return elements;
    }

    /**
     * Parse the string into a DOM document.
     *
     * @param xmlData A string containing an XML document.
     * @return Document
     */
    public static Document domparse( String xmlData )
    {
        InputSource inputSource = new InputSource( new StringReader( xmlData ) );
        return domparse( inputSource, null );
    }

    public static Document domparse( String xmlData, String rootName )
    {
        InputSource inputSource = new InputSource( new StringReader( xmlData ) );
        return domparse( inputSource, new String[]{rootName} );
    }

    private static Document domparse( InputSource inputSource, String[] rootNames )
    {
        Document doc;

        try
        {
            doc = getDocumentBuilder().parse( inputSource );
        }
        catch ( IOException e )
        {
            throw new IllegalArgumentException( "Failed to retrieve XML source document on the given URL", e );
        }
        catch ( SAXException e )
        {
            throw new IllegalArgumentException( "Failed to parse xml document", e );
        }

        if ( rootNames != null )
        {
            Element root = doc.getDocumentElement();
            if ( root == null )
            {
                throw new IllegalArgumentException( "No root element in XML document" );
            }

            Arrays.sort( rootNames );
            if ( Arrays.binarySearch( rootNames, root.getTagName() ) < 0 )
            {
                throw new IllegalArgumentException( "Wrong root element name: " + root.getTagName() );
            }
        }

        return doc;
    }

    /**
     * Each element in the list is entered into a map, with a given attribute as key.
     *
     * @param nodeList  The nodes that should be filtered.
     * @param attribute The name of the attribute that should be used as a key.
     * @return The resulting map.
     */
    public static Map<String, Element> filterElementsWithAttributeAsKey( NodeList nodeList, String attribute )
    {

        Map<String, Element> nodes = new HashMap<String, Element>();

        if ( nodeList == null )
        {
            return nodes;
        }

        for ( int i = 0; i < nodeList.getLength(); i++ )
        {
            Node n = nodeList.item( i );
            if ( n.getNodeType() == Node.ELEMENT_NODE )
            {
                Element e = (Element) n;
                nodes.put( e.getAttribute( attribute ), e );
            }
        }

        return nodes;
    }

    /**
     * Filter a nodelist on a specific node type.
     *
     * @param nodeList NodeList
     * @param nodeType short
     * @return Node[]
     */
    public static Node[] filterNodes( NodeList nodeList, short nodeType )
    {

        List<Node> nodes = new ArrayList<Node>();

        if ( nodeList == null )
        {
            return nodes.toArray( new Node[nodes.size()] );
        }

        for ( int i = 0; i < nodeList.getLength(); i++ )
        {
            Node n = nodeList.item( i );
            if ( n != null && n.getNodeType() == nodeType )
            {
                nodes.add( n );
            }
        }

        return nodes.toArray( new Node[nodes.size()] );
    }

    /**
     * Get an element's sub-element by name. Will return null if root is null, sub-element's name is null or empty and if the element is not
     * found. If more than one sub-element with the same name, the first match is returned.
     *
     * @param root        Element the root element to search in
     * @param elementName String name of the sub-element to find
     * @return Element the sub-element
     */
    public static Element getElement( Element root, String elementName )
    {

        if ( root == null || elementName == null || elementName.trim().length() == 0 )
        {
            return null;
        }

        Node[] element = filterNodes( root.getChildNodes(), Node.ELEMENT_NODE );
        for ( Node anElement : element )
        {
            String tagName = ( (Element) anElement ).getTagName();
            if ( elementName.equals( tagName ) )
            {
                return (Element) anElement;
            }
        }

        return null;
    }

    /**
     * Get an element's first sub-element. Will return null if root is null, root does not have any sub-elements.
     *
     * @param root Element the root element to search in
     * @return Element the first sub-element, or null if none found
     */
    public static Element getFirstElement( Element root )
    {

        if ( root == null )
        {
            return null;
        }

        Node n = root.getFirstChild();
        while ( n != null && n.getNodeType() != Node.ELEMENT_NODE )
        {
            n = n.getNextSibling();
        }

        if ( n != null )
        {
            return (Element) n;
        }

        return null;
    }

    public static void removeChildNodes( Element root, boolean keepAttributeNodes )
    {

        if ( root == null )
        {
            return;
        }

        NodeList nodeList = root.getChildNodes();
        for ( int i = 0; i < nodeList.getLength(); )
        {
            Node n = nodeList.item( i );
            if ( ( !keepAttributeNodes && n.getNodeType() == Node.ATTRIBUTE_NODE ) || n.getNodeType() != Node.ATTRIBUTE_NODE )
            {
                root.removeChild( n );
            }
            else
            {
                i++;
            }
        }
    }

    public static void removeChildNodes( Node root )
    {
        if ( root == null )
        {
            return;
        }

        NodeList nodeList = root.getChildNodes();
        for ( int i = 0; i < nodeList.getLength(); )
        {
            root.removeChild( nodeList.item( i ) );
        }
    }

    /**
     * Retrieve a text from an element or attribute using an xpath expression.
     *
     * @param doc   The source document that the text is to be retrieved from.
     * @param xpath The xpath that selects the element or attribute containing the text.
     * @return The element text.
     */
    public static String getElementText( Document doc, String xpath )
    {

        Node node = selectNode( doc.getDocumentElement(), xpath );

        return getNodeText( node );
    }

    public static String getNodeText( Node node )
    {
        if ( node == null )
        {
            return null;
        }
        else if ( node.getNodeType() == Node.TEXT_NODE )
        {
            return ( (Text) node ).getData();
        }
        else if ( node.getNodeType() == Node.ATTRIBUTE_NODE )
        {
            return ( (Attr) node ).getValue();
        }
        else
        {
            return getElementText( (Element) node );
        }
    }

    /**
     * Retrieve a text from an element or attribute using an xpath expression.
     *
     * @param contextElement The root element that the xpath expression is to be applied on.
     * @param xpath          The xpath that selects the element or attribute containing the text.
     * @return The element text.
     */

    public static String getElementText( Element contextElement, String xpath )
    {

        if ( contextElement == null )
        {
            return null;
        }

        Node node = selectNode( contextElement, xpath );

        if ( node != null )
        {
            if ( node.getNodeType() == Node.TEXT_NODE )
            {
                return ( (Text) node ).getData();
            }
            else if ( node.getNodeType() == Node.ATTRIBUTE_NODE )
            {
                return ( (Attr) node ).getValue();
            }
            else
            {
                return getElementText( (Element) node );
            }
        }

        return null;
    }

    public static String getElementText( Element element )
    {
        if ( element == null )
        {
            return null;
        }
        else
        {
            String value = DomUtils.getTextValue( element );
            if ( value == null )
            {
                return null;
            }
            else if ( value.trim().length() == 0 )
            {
                return null;
            }
            else
            {
                return value;
            }

        }
    }

    /**
     * Print a document to a specific stream.
     *
     * @param out      The output stream.
     * @param doc      The document.
     * @param indent   The specified indentation level.
     * @param encoding The character encoding.
     */
    private static void printDocument( OutputStream out, Document doc, String encoding, int indent )
    {
        printDocument( out, doc, encoding, indent, false );
    }

    /**
     * Print a document to a specific stream.
     *
     * @param out           The output stream.
     * @param doc           The document.
     * @param encoding      The character encoding.
     * @param indent        The specified indentation level.
     * @param preserveSpace Whether to preserve the original spacing or strip all whitespace from the result.
     */
    private static void printDocument( OutputStream out, Document doc, String encoding, int indent, boolean preserveSpace )
    {
        // check: is document present?
        if ( doc == null )
        {
            throw new IllegalArgumentException( "The supplied document is null (doc==" + doc + ")" );
        }

        serialize( out, doc, indent );
    }

    private static String serialize( Node node, int indent )
    {
        final StringWriter writer = new StringWriter();

        try {
            serialize( writer, node, indent );
        } finally {
            Closeables.closeQuietly( writer );
        }

        return writer.toString();
    }

    private static void serialize( Writer out, Node node, int indent )
    {
        final StreamResult result = new StreamResult(out);
        serialize(result, node, indent, true);
    }

    private static void serialize( OutputStream out, Node node, int indent )
    {
        final StreamResult result = new StreamResult(out);
        serialize(result, node, indent, true);
    }

    private static void serialize( Result result, Node node, int indent, boolean omitDecl )
    {
        try {
            final Transformer transformer = TRANSFORMER_FACTORY.newTransformer();
            transformer.setOutputProperty( OutputKeys.INDENT, indent > 0 ? "yes" : "no" );
            transformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, omitDecl ? "yes" : "no" );
            transformer.transform( new DOMSource(node), result );
        } catch (Exception e) {
            throw new IllegalArgumentException( e );
        }
    }

    private static String elementToString( Element elem, int indent )
    {
        // check: is element present?
        if ( elem == null )
        {
            return null;
        }

        return serialize(elem, indent);
    }

    public static void printDocument( Writer out, Document doc )
    {
        printDocument( out, doc, 0 );
    }

    public static void printDocument( Writer out, Document doc, int indent )
    {
        // check: is document present?
        if ( doc == null )
        {
            throw new IllegalArgumentException( "The supplied document is null (doc==" + doc + ")" );
        }

        serialize(out, doc, indent);
    }

    /**
     * Use an XPath string to select a single node.
     *
     * @param contextNode The node to start searching from.
     * @param xpath       A valid XPath string.
     * @return The first node matching the xpath, or null.
     */
    public static Node selectNode( Node contextNode, String xpath )
    {
        return selectSingleNode( contextNode, xpath );
    }

    /**
     * Use an XPath string to select all nodes matching the xpath. A nodelist is returned.
     *
     * @param contextNode The node to start searching from.
     * @param xpath       A valid XPath string.
     * @return A nodelist containing the matching nodes.
     */
    public static NodeList selectNodes( Node contextNode, String xpath )
    {
        return selectNodeList( contextNode, xpath );
    }

    /**
     * Removes a child element from a parent element, and returns the next element to the removed one.
     *
     * @param parent The parent element to the child to be removed.
     * @param child  The child element to remove.
     * @return The next element to the removed child.
     */
    public static Element removeChildFromParent( Element parent, Element child )
    {

        Element previousChild = (Element) child.getPreviousSibling();

        // If the child to remove is the first child
        if ( previousChild == null )
        {
            parent.removeChild( child );
            // Return the first child as the next child
            return (Element) parent.getFirstChild();
        }
        // If the child is not the first child
        else
        {
            parent.removeChild( child );
            return (Element) previousChild.getNextSibling();
        }
    }

    private static Node moveNode( Node node, Node toParent )
    {
        Node fromParent = node.getParentNode();
        fromParent.removeChild( node );
        return toParent.appendChild( node );
    }

    public static String serialize( Node n, boolean includeSelf, String encoding )
    {
        DocumentFragment df = XMLTool.createDocument().createDocumentFragment();
        NodeList children = n.getChildNodes();

        // Check whether the child is a CDATA node
        Node firstChild = n.getFirstChild();
        if ( firstChild != null && firstChild.getNodeType() == Node.CDATA_SECTION_NODE )
        {
            return null;
        }

        if ( includeSelf )
        {
            df.appendChild( df.getOwnerDocument().importNode( n, true ) );
        }
        else
        {
            if ( children == null || children.getLength() == 0 )
            {
                return null;
            }

            // If only one node is found and it is a CDATA section, there is no need for serialization
            if ( children.getLength() == 1 && children.item( 0 ).getNodeType() == Node.CDATA_SECTION_NODE )
            {
                return null;
            }

            for ( int i = 0; i < children.getLength(); i++ )
            {
                df.appendChild( df.getOwnerDocument().importNode( children.item( i ), true ) );
            }
        }

        return serialize( df, 4 );
    }

    public static void mergeDocuments( Document destDoc, String xmlDoc, boolean copyRoot )
    {
        Document srcDoc = XMLTool.domparse( xmlDoc );
        mergeDocuments( destDoc, srcDoc, copyRoot );
    }

    public static void mergeDocuments( Document destDoc, Document srcDoc, boolean copyRoot )
    {
        Element destDocElem = destDoc.getDocumentElement();

        if ( copyRoot )
        {
            Element element = srcDoc.getDocumentElement();
            destDocElem.appendChild( destDoc.importNode( element, true ) );
        }
        else
        {
            NodeList nodes = srcDoc.getDocumentElement().getChildNodes();

            for ( int i = 0; i < nodes.getLength(); i++ )
            {
                destDocElem.appendChild( destDoc.importNode( nodes.item( i ), true ) );
            }
        }

    }

    public static int getElementIndex( Element elem )
    {
        Element[] elems = XMLTool.getElements( (Element) elem.getParentNode(), elem.getNodeName() );
        for ( int i = 0; i < elems.length; i++ )
        {
            if ( elems[i] == elem )
            {
                return i;
            }
        }
        return 0;
    }

    public static void sortChildElements( Element elem, String orderByAttribute, boolean descending, boolean recursive )
    {
        Element[] childElems = XMLTool.getElements( elem );
        if ( childElems == null )
        {
            return;
        }

        Arrays.sort( childElems, new ElementComparator( orderByAttribute, true, descending ) );
        for ( Element childElem : childElems )
        {
            elem.appendChild( childElem );
            if ( recursive )
            {
                sortChildElements( childElem, orderByAttribute, descending, recursive );
            }
        }
    }

}
