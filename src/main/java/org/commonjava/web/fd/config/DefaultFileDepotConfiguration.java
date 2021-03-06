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

import java.io.File;
import java.net.MalformedURLException;

import javax.enterprise.inject.Alternative;
import javax.inject.Named;

import org.commonjava.couch.conf.CouchDBConfiguration;
import org.commonjava.couch.conf.DefaultCouchDBConfiguration;
import org.commonjava.couch.util.UrlUtils;
import org.commonjava.web.config.annotation.ConfigName;
import org.commonjava.web.config.annotation.ConfigNames;
import org.commonjava.web.config.annotation.SectionName;
import org.commonjava.web.config.section.ConfigurationSectionListener;

@SectionName( ConfigurationSectionListener.DEFAULT_SECTION )
@Named( "do-not-use-directly" )
@Alternative
public class DefaultFileDepotConfiguration
    extends DefaultCouchDBConfiguration
    implements FileDepotConfiguration
{

    private static final File DEFAULT_UPLOAD_DIR = new File( System.getProperty( "java.io.tmpdir" ),
                                                             "file-depot-uploads" );

    private String dbBaseUrl;

    private CouchDBConfiguration dbConfig;

    private Integer fileExpirationMinutes;

    private File storageDir;

    private File uploadDir;

    public DefaultFileDepotConfiguration()
    {
    }

    @ConfigNames( { "db.base.url", "storage.dir" } )
    public DefaultFileDepotConfiguration( final String dbUrl, final File storageDir )
    {
        this.storageDir = storageDir;
        setDatabaseUrl( dbUrl );
    }

    @Override
    public synchronized CouchDBConfiguration getDatabaseConfig()
    {
        if ( dbConfig == null )
        {
            dbConfig = new DefaultCouchDBConfiguration( getDatabaseUrl(), getMaxConnections() );
        }

        return dbConfig;
    }

    public String getDbBaseUrl()
    {
        return dbBaseUrl;
    }

    @ConfigName( "db.base.url" )
    public void setDbBaseUrl( final String dbBaseUrl )
    {
        this.dbBaseUrl = dbBaseUrl;
        try
        {
            setDatabaseUrl( UrlUtils.buildUrl( dbBaseUrl, DEFAULT_DB_NAME ) );
        }
        catch ( final MalformedURLException e )
        {
            throw new IllegalArgumentException( "Invalid base-url: " + dbBaseUrl, e );
        }
    }

    @ConfigName( "file.expiration.mins" )
    public void setFileExpirationMins( final int fileExpirationMins )
    {
        this.fileExpirationMinutes = fileExpirationMins;
    }

    @Override
    public int getFileExpirationMins()
    {
        return fileExpirationMinutes == null ? DEFAULT_FILE_EXPIRATION_MINS : fileExpirationMinutes;
    }

    @Override
    public final File getStorageDir()
    {
        return storageDir;
    }

    @ConfigName( "storage.dir" )
    public final void setStorageDir( final File storageDir )
    {
        this.storageDir = storageDir;
    }

    @Override
    public final File getUploadDir()
    {
        return uploadDir == null ? DEFAULT_UPLOAD_DIR : uploadDir;
    }

    @ConfigName( "upload.dir" )
    public final void setUploadDir( final File uploadDir )
    {
        this.uploadDir = uploadDir;
    }

}
