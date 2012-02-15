package org.commonjava.web.fd.live;

import java.io.File;

import javax.inject.Inject;

import org.cjtest.fixture.TestAuthenticationControls;
import org.commonjava.couch.db.CouchManager;
import org.commonjava.couch.user.web.test.AbstractUserRESTCouchTest;
import org.commonjava.web.fd.data.WorkspaceDataManager;
import org.commonjava.web.fd.fixture.TestFDFactory;
import org.commonjava.web.fd.inject.FileDepotData;
import org.commonjava.web.fd.webctl.ShiroBasicAuthenticationFilter;
import org.commonjava.web.fd.webctl.ShiroSetupListener;
import org.commonjava.web.json.test.WebFixture;
import org.commonjava.web.test.fixture.TestWarArchiveBuilder;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

public abstract class AbstractFDLiveTest
    extends AbstractUserRESTCouchTest
{

    @Inject
    @FileDepotData
    private CouchManager couch;

    @Inject
    protected WorkspaceDataManager dataManager;

    @Inject
    protected TestAuthenticationControls authControls;

    @Rule
    public WebFixture fixture = new WebFixture();

    @Rule
    public TestName testName = new TestName();

    protected static WebArchive createDeployment( final Class<?> testClass, final Class<?>... extras )
    {
        final TestWarArchiveBuilder builder =
            new TestWarArchiveBuilder( testClass ).withExtraClasses( AbstractFDLiveTest.class, TestFDFactory.class )
                                                  .withExtraClasses( extras )
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
        authControls.resetUser();
        System.out.println( "\n\n\nRunning: " + testName.getMethodName() + "\n\n\n\n" );
    }

    @After
    public final void teardown()
    {
        System.out.println( "\n\n\nFinished: " + testName.getMethodName() + "\n\n\n\n" );
    }

    @Override
    protected CouchManager getCouchManager()
    {
        return couch;
    }

}
