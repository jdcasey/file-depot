package org.commonjava.web.fd.fixture;

import java.io.File;
import java.util.Properties;

import javax.enterprise.inject.Produces;
import javax.inject.Named;

import org.commonjava.couch.test.fixture.TestPropertyDefinitions;
import org.commonjava.couch.user.fixture.UserTestPropertyDefinitions;

public class TestFDPropertiesProducer
{

    public static final String FD_DATABASE_URL = "fd.database.url";

    public static final String FD_STORAGE_ROOT_DIR = "fd.storage.dir";

    private static final String USER_DB = "http://localhost:5984/test-users";

    private static final String FD_DB = "http://localhost:5984/test-fd";

    @Produces
    @Named( TestPropertyDefinitions.NAMED )
    public Properties getTestProperties()
    {
        Properties p = new Properties();

        p.setProperty( UserTestPropertyDefinitions.USER_DATABASE_URL, USER_DB );

        p.setProperty( FD_DATABASE_URL, FD_DB );

        p.setProperty( FD_STORAGE_ROOT_DIR,
                       new File( System.getProperty( FD_STORAGE_ROOT_DIR, "target/storage-root" ) ).getAbsolutePath() );

        return p;
    }

}
