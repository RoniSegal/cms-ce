package com.enonic.cms.core.plugin.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;

import com.enonic.cms.api.plugin.ext.Extension;
import com.enonic.cms.api.plugin.ext.FunctionLibrary;
import com.enonic.cms.api.plugin.ext.TaskHandler;
import com.enonic.cms.api.plugin.ext.TextExtractor;
import com.enonic.cms.api.plugin.ext.http.HttpAutoLogin;
import com.enonic.cms.api.plugin.ext.http.HttpInterceptor;
import com.enonic.cms.api.plugin.ext.http.HttpProcessor;
import com.enonic.cms.api.plugin.ext.http.HttpResponseFilter;
import com.enonic.cms.core.plugin.ExtensionListener;
import com.enonic.cms.core.plugin.ExtensionManager;
import com.enonic.cms.core.plugin.ExtensionManagerAccessor;
import com.enonic.cms.core.plugin.PluginRegistry;

import com.enonic.cms.business.SiteURLResolver;

import com.enonic.cms.domain.SitePath;

public final class ExtensionManagerImpl
    implements ExtensionManager, InitializingBean
{
    private PluginRegistry pluginRegistry;

    private Map<String, ExtensionListener> listeners;

    @Autowired
    private ApplicationContext applicationContext;

    public void setPluginRegistry( final PluginRegistry pluginRegistry )
    {
        this.pluginRegistry = pluginRegistry;
    }

    public void afterPropertiesSet()
        throws Exception
    {
        ExtensionManagerAccessor.setExtensionManager(this);
    }

    private List<Extension> getAllExtensions()
    {
        if ( this.pluginRegistry == null )
        {
            return Collections.emptyList();
        }

        return this.pluginRegistry.getExtensions();
    }

    @SuppressWarnings("unchecked")
    private <E extends Extension> List<E> getExtensionsForType( final Class<E> type )
    {
        final List<Extension> all = getAllExtensions();
        final Collection<E> filtered = (Collection<E>) Collections2.filter( all, Predicates.instanceOf( type ) );
        return new ArrayList<E>( filtered );
    }

    public <T extends Extension> List<T> getPlugins( Class<T> type )
    {
        return getExtensionsForType( type );
    }

    @SuppressWarnings("unchecked")
    private <T extends HttpProcessor> Collection<T> findHttpProcessorPlugins( Class<T> type, String path )
    {
        LinkedList<T> list = new LinkedList<T>();
        for ( T plugin : getPlugins( type ) )
        {
            if ( plugin.matchesUrlPattern( path ) )
            {
                list.add( plugin );
            }
        }

        Collections.sort( list );
        return list;
    }

    public FunctionLibrary findFunctionLibrary( String namespace )
    {
        for ( FunctionLibrary plugin : getAllFunctionLibraries() )
        {
            if ( namespace.equals( plugin.getName() ) )
            {
                return plugin;
            }
        }

        return null;
    }

    public TaskHandler findTaskPlugin( String className )
    {
        for ( TaskHandler plugin : getAllTaskPlugins() )
        {
            if ( className.equals( plugin.getClass().getName() ) )
            {
                return plugin;
            }
        }
        return null;
    }

    public TextExtractor findTextExtractorPluginByMimeType( String mimeType )
    {
        for ( TextExtractor plugin : getAllTextExtractorPlugins() )
        {
            if ( plugin.canHandle( mimeType ) )
            {
                return plugin;
            }
        }
        return null;
    }

    private String getRequestPathFromSitePath( SitePath originalSitePath )
    {
        if ( originalSitePath == null )
        {
            throw new IllegalArgumentException( "SitePath is null: illegal request" );
        }

        return SiteURLResolver.DEFAULT_SITEPATH_PREFIX + "/" + originalSitePath.getSiteKey() + originalSitePath.getLocalPath();

    }

    public Collection<HttpInterceptor> findMatchingHttpInterceptors( String path )
    {
        return findHttpProcessorPlugins( HttpInterceptor.class, path );
    }

    public Collection<HttpInterceptor> getAllHttpInterceptors()
    {
        List<HttpInterceptor> plugins = getPlugins( HttpInterceptor.class );
        Collections.sort( plugins );
        return plugins;

    }

    public Collection<HttpResponseFilter> findMatchingHttpResponseFilters( SitePath originalSitePath )
    {
        String path = getRequestPathFromSitePath( originalSitePath );
        return findHttpProcessorPlugins( HttpResponseFilter.class, path );
    }

    public Collection<HttpResponseFilter> getAllHttpResponseFilters()
    {
        List<HttpResponseFilter> plugins = getPlugins( HttpResponseFilter.class );
        Collections.sort( plugins );
        return plugins;
    }

    public HttpAutoLogin findMatchingHttpAutoLoginPlugin( String path )
    {
        Collection<HttpAutoLogin> list = findHttpProcessorPlugins( HttpAutoLogin.class, path );
        return list.isEmpty() ? null : list.iterator().next();
    }

    public Collection<HttpAutoLogin> getAllHttpAutoLoginPlugins()
    {
        List<HttpAutoLogin> plugins = getPlugins( HttpAutoLogin.class );
        Collections.sort( plugins );
        return plugins;
    }

    public Collection<FunctionLibrary> getAllFunctionLibraries()
    {
        return getPlugins( FunctionLibrary.class );
    }

    public Collection<TaskHandler> getAllTaskPlugins()
    {
        return getPlugins( TaskHandler.class );
    }

    public Collection<TextExtractor> getAllTextExtractorPlugins()
    {
        return getPlugins( TextExtractor.class );
    }

    public PluginRegistry getPluginRegistry()
    {
        return this.pluginRegistry;
    }

    public Map<String, ExtensionListener> getListeners()
    {
        this.listeners = this.applicationContext.getBeansOfType( ExtensionListener.class );
        return this.listeners;
    }

    public void setListeners( Map<String, ExtensionListener> listeners )
    {
        this.listeners = listeners;
    }
}
