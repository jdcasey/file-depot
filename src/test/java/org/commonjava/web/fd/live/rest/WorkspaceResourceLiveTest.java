package org.commonjava.web.fd.live.rest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
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
{

    @Deployment
    public static WebArchive getDeployment()
    {
        return createDeployment( WorkspaceResourceLiveTest.class );
    }

    @Test
    public void addTwoWorkspacesAndRetrieveListingWithBoth()
        throws Exception
    {
        // final BasicCredentialsProvider creds = new BasicCredentialsProvider();
        // final Credentials cred = new UsernamePasswordCredentials( User.ADMIN, "admin123" );
        //
        // creds.setCredentials( new AuthScope( "localhost", 8080 ), cred );
        // http.setCredentialsProvider( creds );

        final Workspace ws = new Workspace( "test1" );
        assertThat( dataManager.storeWorkspace( ws ), equalTo( true ) );

        final Workspace ws2 = new Workspace( "test2" );
        assertThat( dataManager.storeWorkspace( ws2 ), equalTo( true ) );

        final HttpResponse response = getWithResponse( resourceUrl( "workspace/all" ), 200 );
        final String json = IOUtils.toString( response.getEntity()
                                                      .getContent() );

        System.out.println( json );
    }

}
