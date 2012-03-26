package org.commonjava.web.fd.webctl;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.commonjava.auth.couch.inject.UserData;
import org.commonjava.couch.change.CouchChangeListener;
import org.commonjava.couch.db.CouchDBException;
import org.commonjava.util.logging.Logger;
import org.commonjava.web.fd.data.WorkspaceDataException;
import org.commonjava.web.fd.data.WorkspaceDataManager;
import org.commonjava.web.fd.inject.FileDepotData;

@WebListener
public class InstallerListener
    implements ServletContextListener
{

    private final Logger logger = new Logger( getClass() );

    @Inject
    private WorkspaceDataManager dataManager;

    @Inject
    @UserData
    private CouchChangeListener userChangeListener;

    @Inject
    @FileDepotData
    private CouchChangeListener fdChangeListener;

    @Override
    public void contextInitialized( final ServletContextEvent sce )
    {
        logger.info( "Verfiying that CouchDB + applications + basic data is installed..." );

        try
        {
            userChangeListener.startup();
            fdChangeListener.startup();
        }
        catch ( final CouchDBException e )
        {
            throw new RuntimeException( "Failed to start change listener(s): " + e.getMessage(), e );
        }

        try
        {
            dataManager.install();
        }
        catch ( final WorkspaceDataException e )
        {
            throw new RuntimeException( "Failed to install workspace database: " + e.getMessage(), e );
        }
        logger.info( "...done." );
    }

    @Override
    public void contextDestroyed( final ServletContextEvent sce )
    {
        try
        {
            userChangeListener.shutdown();
            fdChangeListener.shutdown();
        }
        catch ( final CouchDBException e )
        {
            throw new RuntimeException( "Failed to shutdown change listener(s): " + e.getMessage(), e );
        }

    }

}
