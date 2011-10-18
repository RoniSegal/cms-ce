/*
 * Copyright 2000-2011 Enonic AS
 * http://www.enonic.com/license
 */
package com.enonic.cms.server.service.mbean.configuration;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jmx.export.annotation.ManagedAttribute;

import com.enonic.cms.api.Version;
import com.enonic.cms.upgrade.UpgradeService;

public class System
    implements SystemMBean
{
    @Value("#{config.properties}")
    private Properties properties;

    @Autowired
    @Qualifier("upgradeService")
    private UpgradeService upgradeService;

    @ManagedAttribute
    public String getCmsVersion()
    {
        return Version.getVersion();
    }

    @ManagedAttribute
    public int getDatabaseModelVersion()
    {
        return upgradeService.getCurrentModelNumber();
    }

    @ManagedAttribute
    public Properties getCmsProperties()
    {
        return properties;
    }

    @ManagedAttribute
    public String getSaxonVersion()
    {
        return net.sf.saxon.Version.getProductVersion();
    }
}