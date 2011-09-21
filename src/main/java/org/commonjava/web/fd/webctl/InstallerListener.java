package org.commonjava.web.fd.webctl;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.commonjava.couch.change.CouchChangeListener;
import org.commonjava.couch.db.CouchDBException;
import org.commonjava.util.logging.Logger;
import org.commonjava.web.fd.data.WorkspaceDataException;
import org.commonjava.web.fd.data.WorkspaceDataManager;

@WebListener
public class InstallerListener
    implements ServletContextListener
{

    private final Logger logger = new Logger( getClass() );

    @Inject
    private WorkspaceDataManager dataManager;

    @Inject
    private CouchChangeListener changeListener;

    @Override
    public void contextInitialized( final ServletContextEvent sce )
    {
        logger.info( "Verfiying that CouchDB + applications + basic data is installed..." );

        try
        {
            changeListener.startup();
        }
        catch ( CouchDBException e )
        {
            throw new RuntimeException( "Failed to start change listener: " + e.getMessage(), e );
        }

        try
        {
            dataManager.install();
        }
        catch ( WorkspaceDataException e )
        {
            throw new RuntimeException( "Failed to install workspace database: " + e.getMessage(),
                                        e );
        }
        logger.info( "...done." );
    }

    @Override
    public void contextDestroyed( final ServletContextEvent sce )
    {
        try
        {
            changeListener.shutdown();
        }
        catch ( CouchDBException e )
        {
            throw new RuntimeException( "Failed to shutdown change listener: " + e.getMessage(), e );
        }

    }

}
