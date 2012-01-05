package org.commonjava.web.fd.fixture;

import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import org.commonjava.auth.couch.conf.DefaultUserManagerConfig;
import org.commonjava.auth.couch.conf.UserManagerConfiguration;
import org.commonjava.auth.couch.inject.UserData;
import org.commonjava.couch.conf.CouchDBConfiguration;
import org.commonjava.web.fd.config.DefaultFileDepotConfiguration;
import org.commonjava.web.fd.config.FileDepotConfiguration;
import org.commonjava.web.fd.inject.FileDepotData;

@Singleton
public class TestFDFactory
{

    private static final String DB_URL = "http://localhost:5984/test-fd";

    private FileDepotConfiguration config;

    @Produces
    // @TestData
    @Default
    public synchronized FileDepotConfiguration getFDConfig()
    {
        if ( config == null )
        {
            config = new DefaultFileDepotConfiguration( DB_URL );
        }

        return config;
    }

    @Produces
    // @TestData
    @Default
    @FileDepotData
    public CouchDBConfiguration getFDCouchConfig()
    {
        return getFDConfig().getDatabaseConfig();
    }

    private UserManagerConfiguration umConfig;

    @Produces
    // @TestData
    @UserData
    @Default
    public synchronized CouchDBConfiguration getCouchDBConfiguration()
    {
        return getUserManagerConfiguration().getDatabaseConfig();
    }

    @Produces
    // @TestData
    @Default
    public synchronized UserManagerConfiguration getUserManagerConfiguration()
    {
        if ( umConfig == null )
        {
            umConfig =
                new DefaultUserManagerConfig( "admin@nowhere.com", "password", "Admin", "User",
                                              "http://localhost:5984/test-user-manager" );
        }

        return umConfig;
    }

}
