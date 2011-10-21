package org.commonjava.web.fd;

import javax.inject.Inject;

import org.commonjava.couch.db.CouchManager;
import org.commonjava.couch.user.fixture.TestUserWarArchiveBuilder;
import org.commonjava.couch.user.web.test.AbstractUserRESTCouchTest;
import org.commonjava.web.fd.fixture.TestFDPropertiesProducer;
import org.commonjava.web.fd.inject.FileDepotData;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;

@RunWith( Arquillian.class )
public class AbstractFDLiveTest
    extends AbstractUserRESTCouchTest
{

    @Inject
    @FileDepotData
    private CouchManager couch;

    @Deployment
    public WebArchive getDeployment()
    {
        TestUserWarArchiveBuilder builder =
            new TestUserWarArchiveBuilder( TestFDPropertiesProducer.class );

        WebArchive archive = builder.build();

        return archive;
    }

    @Override
    protected CouchManager getCouchManager()
    {
        return couch;
    }

}
