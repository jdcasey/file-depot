package org.commonjava.web.fd.webctl;

import javax.inject.Inject;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.commonjava.auth.couch.data.PasswordManager;
import org.commonjava.util.logging.Logger;

@WebFilter( urlPatterns = "/*", filterName = "shiro-basic-authc" )
public class ShiroBasicAuthenticationFilter
    extends BasicHttpAuthenticationFilter
{

    public static final String APPLICATION_NAME_KEY = "aprox-application-name";

    public static final String DEFAULT_APPLICATION_NAME = "file-depot";

    private final Logger logger = new Logger( getClass() );

    @Inject
    private PasswordManager passwordManager;

    @Override
    protected AuthenticationToken createToken( final String username, final String password,
                                               final boolean rememberMe, final String host )
    {
        return new UsernamePasswordToken( username, passwordManager.digestPassword( password ),
                                          rememberMe, host );
    }

    @Override
    protected void onFilterConfigSet()
        throws Exception
    {
        logger.info( "Initializing authentication filter..." );
        Object appName = getFilterConfig().getServletContext().getAttribute( APPLICATION_NAME_KEY );
        if ( appName == null )
        {
            appName = DEFAULT_APPLICATION_NAME;
        }

        logger.info( "  setting application name: '%s'", appName );

        setApplicationName( String.valueOf( appName ) );

        processPathConfig( "/**", Boolean.TRUE.toString() );

        logger.info( "...done." );
    }

    @Override
    protected boolean onAccessDenied( final ServletRequest request, final ServletResponse response )
        throws Exception
    {
        // TODO Auto-generated method stub
        return super.onAccessDenied( request, response );
    }

}
