package org.commonjava.web.fd.live;

import java.io.File;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.commonjava.couch.db.CouchManager;
import org.commonjava.couch.user.web.test.AbstractUserRESTCouchTest;
import org.commonjava.web.fd.config.FileDepotConfiguration;
import org.commonjava.web.fd.data.WorkspaceDataManager;
import org.commonjava.web.fd.fixture.TestFDFactory;
import org.commonjava.web.fd.inject.FileDepotData;
import org.commonjava.web.fd.webctl.ShiroBasicAuthenticationFilter;
import org.commonjava.web.fd.webctl.ShiroSetupListener;
import org.commonjava.web.test.fixture.TestWarArchiveBuilder;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;

public abstract class AbstractFDLiveTest
    extends AbstractUserRESTCouchTest
{

    @Inject
    @FileDepotData
    private CouchManager couch;

    @Inject
    protected WorkspaceDataManager dataManager;

    @Inject
    private FileDepotConfiguration config;

    protected static WebArchive createDeployment( final Class<?> testClass )
    {
        final TestWarArchiveBuilder builder =
            new TestWarArchiveBuilder( testClass ).withExtraClasses( AbstractFDLiveTest.class, TestFDFactory.class )
                                                  .withLog4jProperties()
                                                  .withoutBuildClasses( ShiroBasicAuthenticationFilter.class,
                                                                        ShiroSetupListener.class )
                                                  // .withJarKnockoutClasses( "couch-user-test-harness.*",
                                                  // TestAuthenticationFilter.class,
                                                  // TestAuthenticationControls.class )
                                                  .withLibrariesIn( new File( "target/dependency" ) );

        return builder.build();
    }

    @Before
    public final void setup()
        throws Exception
    {
        dataManager.install();
    }

    public final void teardown()
        throws Exception
    {
        FileUtils.forceDelete( config.getUploadDirectory() );
    }

    @Override
    protected CouchManager getCouchManager()
    {
        return couch;
    }

}
