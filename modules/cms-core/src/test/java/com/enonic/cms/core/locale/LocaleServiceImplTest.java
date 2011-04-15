/*
 * Copyright 2000-2011 Enonic AS
 * http://www.enonic.com/license
 */
package com.enonic.cms.core.locale;

import java.util.Locale;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class LocaleServiceImplTest
{

    @Inject
    private LocaleService localeService;

    @Test
    public void getLocales()
    {
        Locale[] locales = localeService.getLocales();
        assertNotNull( locales );
    }

}
