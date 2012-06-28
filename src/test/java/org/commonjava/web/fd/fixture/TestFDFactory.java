package org.commonjava.web.fd.fixture;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.commonjava.auth.couch.conf.DefaultUserManagerConfig;
import org.commonjava.auth.couch.conf.UserManagerConfiguration;
import org.commonjava.auth.couch.inject.UserData;
import org.commonjava.couch.conf.CouchDBConfiguration;
import org.commonjava.web.fd.config.DefaultFileDepotConfiguration;
import org.commonjava.web.fd.config.FileDepotConfiguration;
import org.commonjava.web.fd.inject.FileDepotData;

@ApplicationScoped
public class TestFDFactory
{

    public static final String DB_URL = "http://localhost:5984/test-fd";

    @Inject
    private InjectableTemporaryFolder temp;

    private FileDepotConfiguration config;

    @PostConstruct
    public void postConstruct()
    {
        getFDConfig();
    }

    public void delete()
    {
        if ( temp != null )
        {
            temp.delete();
        }
    }

    @Produces
    // @TestData
    @Default
    public synchronized FileDepotConfiguration getFDConfig()
    {
        if ( config == null )
        {
            final DefaultFileDepotConfiguration c =
                new DefaultFileDepotConfiguration( DB_URL, temp.newFolder( "storage" ) );
            c.setUploadDir( temp.newFolder( "upload" ) );
            config = c;
        }

        return config;
    }

    @Produces
    // @TestData
    @Default
    @FileDepotData
    public CouchDBConfiguration getFDCouchConfig()
    {
        return config.getDatabaseConfig();
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
