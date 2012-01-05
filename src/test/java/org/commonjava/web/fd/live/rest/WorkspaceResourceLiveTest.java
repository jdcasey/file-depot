package org.commonjava.web.fd.live.rest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.commonjava.auth.couch.data.UserDataManager;
import org.commonjava.couch.rbac.User;
import org.commonjava.web.fd.WorkspaceDataTestPlan;
import org.commonjava.web.fd.data.WorkspaceDataManager;
import org.commonjava.web.fd.live.AbstractFDLiveTest;
import org.commonjava.web.fd.model.Workspace;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith( Arquillian.class )
public class WorkspaceResourceLiveTest
    extends AbstractFDLiveTest
    implements WorkspaceDataTestPlan
{

    @Inject
    private UserDataManager userMgr;

    @Inject
    private WorkspaceDataManager wsMgr;

    @Deployment
    public static WebArchive getDeployment()
    {
        return createDeployment( WorkspaceResourceLiveTest.class, WorkspaceDataTestPlan.class );
    }

    @Test
    @Override
    public void storeTwoAndRetrieveAll()
        throws Exception
    {
        HttpResponse response = fixture.put( fixture.resourceUrl( "workspace", "test1" ), 201 );
        fixture.assertLocationHeader( response, fixture.resourceUrl( "workspace", "test1" ) );

        response = fixture.put( fixture.resourceUrl( "workspace", "test2" ), 201 );
        fixture.assertLocationHeader( response, fixture.resourceUrl( "workspace", "test2" ) );

        response = fixture.getWithResponse( resourceUrl( "workspace/all" ), 200 );
        final String json = IOUtils.toString( response.getEntity()
                                                      .getContent() );

        System.out.println( json );

        assertThat( json.contains( "test1" ), equalTo( true ) );
        assertThat( json.contains( "test2" ), equalTo( true ) );
    }

    @Test
    @Override
    public void storeAndRetrieveWorkspace()
        throws Exception
    {
        final HttpResponse response = fixture.put( fixture.resourceUrl( "workspace", "test1" ), 201 );
        fixture.assertLocationHeader( response, fixture.resourceUrl( "workspace", "test1" ) );

        final Header[] headers = response.getHeaders( "Location" );
        assertThat( headers, notNullValue() );
        assertThat( headers.length, equalTo( 1 ) );

        final String location = headers[0].getValue();

        final Workspace ws = fixture.get( location, Workspace.class );

        assertThat( ws, notNullValue() );
        assertThat( ws.getName(), equalTo( "test1" ) );
    }

    @Test
    @Override
    public void storeThreeAndRetrieveTwoAssociatedWithUser()
        throws Exception
    {
        HttpResponse response = fixture.put( fixture.resourceUrl( "workspace", "test1" ), 201 );
        fixture.assertLocationHeader( response, fixture.resourceUrl( "workspace", "test1" ) );

        response = fixture.put( fixture.resourceUrl( "workspace", "test2" ), 201 );
        fixture.assertLocationHeader( response, fixture.resourceUrl( "workspace", "test2" ) );

        response = fixture.put( fixture.resourceUrl( "workspace", "test3" ), 201 );
        fixture.assertLocationHeader( response, fixture.resourceUrl( "workspace", "test3" ) );

        final User user =
            new User( "user", "user@nowherer.com", "User", "One", passwordManager.digestPassword( "password" ) );

        user.addRole( Workspace.userRole( "test1" ) );
        user.addRole( Workspace.userRole( "test2" ) );

        userMgr.storeUser( user );

        // fixture.setCredentials( "user", "password" );
        authControls.setUser( "user" );
        response = fixture.getWithResponse( resourceUrl( "workspace/my" ), 200 );
        final String json = IOUtils.toString( response.getEntity()
                                                      .getContent() );

        System.out.println( json );

        assertThat( json.contains( "test1" ), equalTo( true ) );
        assertThat( json.contains( "test2" ), equalTo( true ) );
        assertThat( json.contains( "test3" ), equalTo( false ) );
    }

    @Test
    @Override
    public void storeAndDeleteWorkspace()
        throws Exception
    {
        final HttpResponse response = fixture.put( fixture.resourceUrl( "workspace", "test1" ), 201 );
        fixture.assertLocationHeader( response, fixture.resourceUrl( "workspace", "test1" ) );

        final Header[] headers = response.getHeaders( "Location" );
        assertThat( headers, notNullValue() );
        assertThat( headers.length, equalTo( 1 ) );

        final String location = headers[0].getValue();

        final Workspace ws = fixture.get( location, Workspace.class );

        assertThat( ws, notNullValue() );
        assertThat( ws.getName(), equalTo( "test1" ) );

        fixture.delete( fixture.resourceUrl( "workspace", "test1" ) );

        fixture.get( location, HttpStatus.SC_NOT_FOUND );
    }

}
