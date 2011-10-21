package org.commonjava.web.fd.data;

import static org.commonjava.web.fd.fixture.CouchShiroTestFixture.clearSubject;
import static org.commonjava.web.fd.fixture.CouchShiroTestFixture.setupSecurityManager;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.List;
import java.util.Properties;

import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Singleton;

import org.apache.log4j.Level;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.commonjava.auth.couch.data.PasswordManager;
import org.commonjava.auth.couch.data.UserDataException;
import org.commonjava.auth.couch.data.UserDataManager;
import org.commonjava.auth.couch.inject.UserData;
import org.commonjava.auth.couch.model.User;
import org.commonjava.auth.shiro.couch.CouchPermissionResolver;
import org.commonjava.auth.shiro.couch.CouchRealm;
import org.commonjava.auth.shiro.couch.model.ShiroUserUtils;
import org.commonjava.couch.conf.CouchDBConfiguration;
import org.commonjava.couch.db.CouchManager;
import org.commonjava.couch.test.fixture.LoggingFixture;
import org.commonjava.web.fd.config.DefaultFileDepotConfiguration;
import org.commonjava.web.fd.config.FileDepotConfiguration;
import org.commonjava.web.fd.fixture.TestFDPropertiesProducer;
import org.commonjava.web.fd.inject.FileDepotData;
import org.commonjava.web.fd.model.Workspace;
import org.commonjava.web.test.fixture.TestData;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class WorkspaceDataManagerWeldTest
{

    private CouchRealm realm;

    private WorkspaceDataManager dataManager;

    private PasswordManager passwordManager;

    private CouchManager fdCouch;

    private CouchManager userCouch;

    private UserDataManager userMgr;

    @BeforeClass
    public static void setupStatic()
    {
        LoggingFixture.setupLogging( Level.INFO );
    }

    @Before
    @SuppressWarnings( "serial" )
    public void setup()
        throws Exception
    {
        WeldContainer weld = new Weld().initialize();

        fdCouch =
            weld.instance().select( CouchManager.class, new AnnotationLiteral<FileDepotData>()
            {} ).get();

        userCouch = weld.instance().select( CouchManager.class, new AnnotationLiteral<UserData>()
        {} ).get();

        dataManager = weld.instance().select( WorkspaceDataManager.class ).get();
        passwordManager = weld.instance().select( PasswordManager.class ).get();

        userMgr = weld.instance().select( UserDataManager.class ).get();
        realm = new CouchRealm( userMgr, new CouchPermissionResolver( userMgr ) );
        setupSecurityManager( realm );

        fdCouch.dropDatabase();
        userCouch.dropDatabase();
        dataManager.install();
    }

    @After
    public void teardown()
        throws Exception
    {
        fdCouch.dropDatabase();
        userCouch.dropDatabase();
        clearSubject();
    }

    @Test
    public void storeAndRetrieveWorkspace()
        throws WorkspaceDataException
    {
        Workspace ws = new Workspace( "test" );

        dataManager.storeWorkspace( ws );
        Workspace result = dataManager.getWorkspace( ws.getName() );

        assertThat( result.getName(), equalTo( ws.getName() ) );
    }

    @Test
    public void storeTwoAndRetrieveAll()
        throws WorkspaceDataException
    {
        Workspace ws = new Workspace( "test" );
        Workspace ws2 = new Workspace( "test2" );

        dataManager.storeWorkspace( ws );
        dataManager.storeWorkspace( ws2 );

        List<Workspace> workspaces = dataManager.getWorkspaces();

        assertThat( workspaces, notNullValue() );
        assertThat( workspaces.size(), equalTo( 2 ) );

        assertThat( workspaces.contains( ws ), equalTo( true ) );
        assertThat( workspaces.contains( ws2 ), equalTo( true ) );
    }

    @Test
    public void storeThreeAndRetrieveTwoAssociatedWithUser()
        throws WorkspaceDataException, UserDataException
    {
        Workspace ws = new Workspace( "test" );
        Workspace ws2 = new Workspace( "test2" );
        Workspace ws3 = new Workspace( "test3" );

        dataManager.storeWorkspace( ws );
        dataManager.storeWorkspace( ws2 );
        dataManager.storeWorkspace( ws3 );

        User user =
            new User( "user", "user@nowherer.com", "User", "One",
                      passwordManager.digestPassword( "password" ) );
        user.addRole( Workspace.userRole( ws.getName() ) );
        user.addRole( Workspace.userRole( ws2.getName() ) );

        userMgr.storeUser( user );

        Subject subject = SecurityUtils.getSubject();
        subject.login( ShiroUserUtils.getAuthenticationToken( user ) );

        List<Workspace> workspaces = dataManager.getWorkspacesForUser( subject );

        assertThat( workspaces, notNullValue() );
        assertThat( workspaces.size(), equalTo( 2 ) );

        assertThat( workspaces.contains( ws ), equalTo( true ) );
        assertThat( workspaces.contains( ws2 ), equalTo( true ) );
        assertThat( workspaces.contains( ws3 ), equalTo( false ) );
    }

    @Test
    public void storeAndDeleteWorkspace()
        throws WorkspaceDataException
    {
        Workspace ws = new Workspace( "test" );

        dataManager.storeWorkspace( ws );
        Workspace result = dataManager.getWorkspace( ws.getName() );

        assertThat( result, notNullValue() );
        assertThat( result.getName(), equalTo( ws.getName() ) );

        dataManager.deleteWorkspace( ws.getName() );

        result = dataManager.getWorkspace( ws.getName() );

        assertThat( result, nullValue() );
    }

    @Singleton
    public static final class TestConfigProvider
    {

        private final Properties props = new TestFDPropertiesProducer().getTestProperties();

        @Produces
        @TestData
        @Default
        public FileDepotConfiguration getFileDepotConfig()
        {
            return new DefaultFileDepotConfiguration(
                                                      new File(
                                                                props.getProperty( TestFDPropertiesProducer.FD_STORAGE_ROOT_DIR ) ),
                                                      props.getProperty( TestFDPropertiesProducer.FD_DATABASE_URL ) );
        }

        @Produces
        @TestData
        @FileDepotData
        public CouchDBConfiguration getFileDepotCouchConfig()
        {
            return getFileDepotConfig().getDatabaseConfig();
        }

    }

}
