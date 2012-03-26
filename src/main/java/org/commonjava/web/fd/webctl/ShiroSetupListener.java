package org.commonjava.web.fd.webctl;

import javax.servlet.ServletContextEvent;

import org.commonjava.auth.shiro.couch.web.CouchShiroSetupListener;
import org.commonjava.util.logging.Logger;

//@WebListener
public class ShiroSetupListener
    extends CouchShiroSetupListener
{

    private final Logger logger = new Logger( getClass() );

    @Override
    public void contextInitialized( final ServletContextEvent sce )
    {
        logger.info( "Initializing CouchDB Shiro authentication/authorization realm..." );
        super.setAutoCreateAuthorizationInfo( true );
        super.contextInitialized( sce );
        logger.info( "...done." );
    }

}
