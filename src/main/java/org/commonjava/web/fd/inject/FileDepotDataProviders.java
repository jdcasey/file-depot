package org.commonjava.web.fd.inject;

import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.commonjava.couch.change.CouchChangeListener;
import org.commonjava.couch.conf.CouchDBConfiguration;
import org.commonjava.couch.db.CouchFactory;
import org.commonjava.couch.db.CouchManager;
import org.commonjava.couch.io.CouchHttpClient;

@Singleton
public class FileDepotDataProviders
{

    @Inject
    private CouchFactory factory;

    @Inject
    @FileDepotData
    private CouchDBConfiguration config;

    private CouchManager couchManager;

    private CouchHttpClient httpClient;

    private CouchChangeListener changeListener;

    @Produces
    @FileDepotData
    @Default
    public synchronized CouchChangeListener getChangeListener()
    {
        if ( changeListener == null )
        {
            changeListener = factory.getChangeListener( config );
        }

        return changeListener;
    }

    @Produces
    @FileDepotData
    @Default
    public synchronized CouchManager getCouchManager()
    {
        if ( couchManager == null )
        {
            couchManager = factory.getCouchManager( config );
        }

        return couchManager;
    }

    @Produces
    @FileDepotData
    @Default
    public synchronized CouchHttpClient getHttpClient()
    {
        if ( httpClient == null )
        {
            httpClient = factory.getHttpClient( config );
        }

        return httpClient;
    }

}
