/*
 * Copyright 2000-2011 Enonic AS
 * http://www.enonic.com/license
 */
package com.enonic.cms.portal.rendering.viewtransformer;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.transform.JDOMSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enonic.cms.framework.util.JDOMUtil;
import com.enonic.cms.framework.xml.XMLDocument;

import com.enonic.cms.core.resource.ResourceFile;
import com.enonic.cms.core.resource.ResourceService;
import com.enonic.cms.core.structure.TemplateParameterType;
import com.enonic.cms.core.xslt.XsltProcessor;
import com.enonic.cms.core.xslt.XsltProcessorException;
import com.enonic.cms.portal.PortletXsltViewTransformationException;

/**
 * Apr 30, 2009
 */
public class PortletXsltViewTransformer
        extends AbstractXsltViewTransformer
{
    private static final Logger LOG = LoggerFactory.getLogger( PortletXsltViewTransformer.class );

    public ViewTransformationResult transform( ResourceFile viewFile, TransformationParams transformationParams,
                                               XMLDocument xml )
    {
        try
        {
            XMLDocument viewAsXMLDocument = viewFile.getDataAsXml();

            XsltProcessor processor = createProcessor( viewFile.getResourceKey(), viewAsXMLDocument, true );
            processor.clearParameters();

            Document viewAsDocument = viewAsXMLDocument.getAsJDOMDocument();
            for ( Element parameterEl : findXsltParamElements( viewAsDocument ) )
            {
                TemplateParameterType parameterType = resolveTemplateParameterType( parameterEl );
                String parameterName = parameterEl.getAttributeValue( "name" );
                TransformationParameter parameter = transformationParams.get( parameterName );

                if ( parameter == null || parameter.getValue() == null )
                {
                    if ( TemplateParameterType.OBJECT.equals( parameterType ) || TemplateParameterType.PAGE.equals( parameterType ) ||
                            TemplateParameterType.CATEGORY.equals( parameterType ) || TemplateParameterType.CONTENT.equals( parameterType ) )
                    {
                        processor.setParameter( parameterName, "" );
                    }
                    continue;
                }

                processor.setParameter( parameter.getName(), parameter.getValue() );
            }

            String stringResult;

            stringResult = processor.process( new JDOMSource( xml.getAsJDOMDocument() ) );
            ViewTransformationResult viewTransformationResult = new ViewTransformationResult();
            viewTransformationResult.setContent( stringResult );
            viewTransformationResult.setHttpContentType( processor.getContentType() );
            viewTransformationResult.setOutputMethod( processor.getOutputMethod() );
            viewTransformationResult.setOutputEncoding( processor.getOutputEncoding() );
            viewTransformationResult.setOutputMediaType( processor.getOutputMediaType() );
            return viewTransformationResult;
        }
        catch ( XsltProcessorException e )
        {
            logXsltProcessorErrors( e.getErrors(), LOG );
            throw new PortletXsltViewTransformationException( "Failed to transform portlet template view", e );
        }

    }

    private TemplateParameterType resolveTemplateParameterType( Element paramEl )
    {
        Element typeEl = JDOMUtil.getFirstElement( paramEl );
        if ( typeEl == null )
        {
            return null;
        }
        return TemplateParameterType.parse( typeEl.getText() );
    }

    @SuppressWarnings("unchecked")
    private Element[] findXsltParamElements( Document doc )
    {
        List list = doc.getRootElement().getChildren( "param", Namespace.getNamespace( XSLT_NS ) );
        return (Element[]) list.toArray( new Element[list.size()] );
    }

    @PostConstruct
    public void afterPropertiesSet()
            throws Exception
    {
        setup();
    }


    @Inject
    public void setResourceService( ResourceService resourceService )
    {
        this.resourceService = resourceService;
    }
}
