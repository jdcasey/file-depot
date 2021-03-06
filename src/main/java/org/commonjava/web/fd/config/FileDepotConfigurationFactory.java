/*******************************************************************************
 * Copyright (C) 2011  John Casey
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public
 * License along with this program.  If not, see 
 * <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.commonjava.web.fd.config;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import org.commonjava.auth.couch.conf.DefaultUserManagerConfig;
import org.commonjava.auth.couch.conf.UserManagerConfiguration;
import org.commonjava.auth.couch.inject.UserData;
import org.commonjava.couch.conf.CouchDBConfiguration;
import org.commonjava.couch.inject.Production;
import org.commonjava.couch.util.UrlUtils;
import org.commonjava.web.config.ConfigurationException;
import org.commonjava.web.config.DefaultConfigurationListener;
import org.commonjava.web.config.dotconf.DotConfConfigurationReader;
import org.commonjava.web.fd.inject.FileDepotData;

@Singleton
public class FileDepotConfigurationFactory
    extends DefaultConfigurationListener
{

    private static final String CONFIG_PATH = "/etc/file-depot/main.conf";

    private DefaultFileDepotConfiguration fileDepotConfig;

    private DefaultUserManagerConfig userManagerConfig;

    public FileDepotConfigurationFactory()
        throws ConfigurationException
    {
        super( DefaultFileDepotConfiguration.class, DefaultUserManagerConfig.class );
    }

    @PostConstruct
    protected void load()
        throws ConfigurationException
    {
        InputStream stream = null;
        try
        {
            stream = new FileInputStream( CONFIG_PATH );
            new DotConfConfigurationReader( this ).loadConfiguration( stream );
        }
        catch ( final IOException e )
        {
            throw new ConfigurationException( "Cannot open configuration file: %s. Reason: %s", e, CONFIG_PATH,
                                              e.getMessage() );
        }
        finally
        {
            closeQuietly( stream );
        }
    }

    @Produces
    @Production
    @Default
    public FileDepotConfiguration getFileDepotConfiguration()
    {
        return fileDepotConfig;
    }

    @Produces
    @Production
    @Default
    public UserManagerConfiguration getUserManagerConfiguration()
    {
        return userManagerConfig;
    }

    @Produces
    @Production
    @UserData
    @Default
    public CouchDBConfiguration getUserCouchConfig()
    {
        if ( fileDepotConfig.getDbBaseUrl() != null && userManagerConfig.getDatabaseUrl() == null )
        {
            try
            {
                userManagerConfig.setDatabaseUrl( UrlUtils.buildUrl( fileDepotConfig.getDbBaseUrl(),
                                                                     UserManagerConfiguration.DEFAULT_DB_NAME ) );
            }
            catch ( final MalformedURLException e )
            {
                throw new IllegalArgumentException( "Invalid base-url: " + fileDepotConfig.getDbBaseUrl(), e );
            }
        }

        return userManagerConfig.getDatabaseConfig();
    }

    @Produces
    @Production
    @FileDepotData
    @Default
    public CouchDBConfiguration getFileDepotCouchConfig()
    {
        return fileDepotConfig.getDatabaseConfig();
    }

    @Override
    public void configurationComplete()
        throws ConfigurationException
    {
        fileDepotConfig = getConfiguration( DefaultFileDepotConfiguration.class );
        userManagerConfig = getConfiguration( DefaultUserManagerConfig.class );
    }

}
