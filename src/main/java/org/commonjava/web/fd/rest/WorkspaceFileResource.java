/*******************************************************************************
 * Copyright (C) 2011  John Casey
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public
 * License along with this program.  If not, see 
 * <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.commonjava.web.fd.rest;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.commonjava.couch.model.Attachment;
import org.commonjava.couch.model.StreamAttachment;
import org.commonjava.couch.rbac.Permission;
import org.commonjava.shelflife.expire.ExpirationManager;
import org.commonjava.shelflife.expire.ExpirationManagerException;
import org.commonjava.shelflife.model.Expiration;
import org.commonjava.util.logging.Logger;
import org.commonjava.web.fd.config.FileDepotConfiguration;
import org.commonjava.web.fd.data.WorkspaceDataException;
import org.commonjava.web.fd.data.WorkspaceDataManager;
import org.commonjava.web.fd.model.Workspace;
import org.commonjava.web.fd.model.WorkspaceFile;
import org.commonjava.web.json.model.Listing;
import org.commonjava.web.json.ser.JsonSerializer;

@Path( "/workspace/{workspaceName}/file/" )
@RequestScoped
@RequiresAuthentication
public class WorkspaceFileResource
{

    private static final String EXPIRES_FORMAT = "EEE, dd MMM yyyyy HH:mm:ss z";

    private static final TimeZone UTC = TimeZone.getTimeZone( "UTC" );

    private static final String DEFAULT_EXPIRE_FLAG = "DEFAULT";

    private static final int ONE_MINUTE_MS = 60 * 1000;;

    private final Logger logger = new Logger( getClass() );

    @Inject
    private WorkspaceDataManager wsDataManager;

    @Inject
    private JsonSerializer serializer;

    @Inject
    private FileDepotConfiguration config;

    @Inject
    private ExpirationManager expirationManager;

    @PUT
    @Path( "{name}" )
    public Response save( @PathParam( "workspaceName" ) final String workspaceName,
                          @PathParam( "name" ) final String filename,
                          @HeaderParam( "Content-Type" ) final MediaType contentType,
                          @HeaderParam( "Content-Length" ) final int contentLength,
                          @HeaderParam( "Expires" ) @DefaultValue( DEFAULT_EXPIRE_FLAG ) final String expires,
                          @QueryParam( "lastModified" ) final Long lastModified,
                          @Context final HttpServletRequest request, @Context final UriInfo uriInfo )
    {
        long timeout;
        if ( DEFAULT_EXPIRE_FLAG.equals( expires ) )
        {
            timeout = config.getFileExpirationMins() * ONE_MINUTE_MS;
        }
        else
        {
            final SimpleDateFormat fmt = new SimpleDateFormat( EXPIRES_FORMAT );
            fmt.setTimeZone( UTC );

            try
            {
                final Date d = fmt.parse( expires );
                timeout = d.getTime() - System.currentTimeMillis();
                if ( timeout < 0 )
                {
                    timeout = config.getFileExpirationMins() * ONE_MINUTE_MS;
                }
            }
            catch ( final ParseException e )
            {
                logger.error( "Invalid Expires HTTP header: '%s'. Parse error was: %s", e, expires, e.getMessage() );
                return Response.status( Status.BAD_REQUEST )
                               .build();
            }
        }

        SecurityUtils.getSubject()
                     .isPermitted( Permission.name( Workspace.NAMESPACE, workspaceName, Permission.CREATE ) );

        final long lm = lastModified == null ? System.currentTimeMillis() : lastModified.longValue();

        StreamAttachment attach;
        try
        {
            logger.info( "Creating file with type: '%s'", contentType );
            attach = new StreamAttachment( filename, request.getInputStream(), contentType.toString(), contentLength );
        }
        catch ( final IOException e )
        {
            logger.error( "Failed to get input stream from request: %s", e, e.getMessage() );
            return Response.serverError()
                           .build();
        }

        final WorkspaceFile wf = new WorkspaceFile( workspaceName, filename, attach, lm );

        try
        {
            wsDataManager.storeWorkspaceFile( wf );
            expirationManager.schedule( wsDataManager.createExpiration( wf, timeout ) );
        }
        catch ( final WorkspaceDataException e )
        {
            logger.error( e.getMessage(), e );
            return Response.serverError()
                           .build();
        }
        catch ( final ExpirationManagerException e )
        {
            logger.error( "Failed to schedule expiration of: %s in workspace: %s. Reason: %s", e, filename,
                          workspaceName, e.getMessage() );
        }

        return Response.created( uriInfo.getAbsolutePathBuilder()
                                        .build() )
                       .build();
    }

    @DELETE
    @Path( "{name}" )
    public Response delete( @PathParam( "workspaceName" ) final String workspaceName,
                            @PathParam( "name" ) final String filename )
    {
        SecurityUtils.getSubject()
                     .isPermitted( Permission.name( Workspace.NAMESPACE, workspaceName, Permission.CREATE ) );

        try
        {
            wsDataManager.deleteWorkspaceFile( workspaceName, filename );
            expirationManager.cancel( new Expiration( wsDataManager.createExpirationKey( workspaceName, filename ) ) );
        }
        catch ( final WorkspaceDataException e )
        {
            logger.error( e.getMessage(), e );
            return Response.serverError()
                           .build();
        }
        catch ( final ExpirationManagerException e )
        {
            logger.error( "Failed to cancel expiration of: %s in workspace: %s. Reason: %s", e, filename,
                          workspaceName, e.getMessage() );
        }

        return Response.ok()
                       .build();
    }

    @GET
    @Path( "list" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response list( @PathParam( "workspaceName" ) final String workspaceName )
    {
        SecurityUtils.getSubject()
                     .isPermitted( Permission.name( Workspace.NAMESPACE, workspaceName, Permission.READ ) );

        try
        {
            final Listing<WorkspaceFile> items = new Listing<WorkspaceFile>( getFiles( workspaceName ) );

            final String json = serializer.toString( items );
            return Response.ok( json )
                           .build();
        }
        catch ( final WorkspaceDataException e )
        {
            throw new WebApplicationException( Response.status( Status.INTERNAL_SERVER_ERROR )
                                                       .build() );
        }
    }

    @GET
    @Path( "list" )
    @Produces( MediaType.TEXT_PLAIN )
    public Response listText( @PathParam( "workspaceName" ) final String workspaceName )
    {
        SecurityUtils.getSubject()
                     .isPermitted( Permission.name( Workspace.NAMESPACE, workspaceName, Permission.READ ) );

        final StringBuilder sb = new StringBuilder();
        try
        {
            for ( final WorkspaceFile f : getFiles( workspaceName ) )
            {
                if ( sb.length() > 0 )
                {
                    sb.append( "\n" );
                }

                sb.append( f.getContentLength() )
                  .append( "  " )
                  .append( new Date( f.getLastModified() ) )
                  .append( "  " )
                  .append( f.getFileName() );
            }

            return Response.ok( sb )
                           .build();
        }
        catch ( final WorkspaceDataException e )
        {
            throw new WebApplicationException( Response.status( Status.INTERNAL_SERVER_ERROR )
                                                       .build() );
        }
    }

    @GET
    @Path( "info/{name}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getFileInfo( @PathParam( "workspaceName" ) final String workspaceName,
                                 @PathParam( "name" ) final String filename )
    {
        SecurityUtils.getSubject()
                     .isPermitted( Permission.name( Workspace.NAMESPACE, workspaceName, Permission.READ ) );

        ResponseBuilder builder = null;
        try
        {
            final WorkspaceFile wsFile = _getFileInfo( workspaceName, filename );
            if ( wsFile == null )
            {
                builder = Response.status( Status.NOT_FOUND );
            }
            else
            {
                final String json = serializer.toString( wsFile );
                logger.info( "Result:\n\n%s\n\n", json );

                builder = Response.ok( json );
            }
        }
        catch ( final WorkspaceDataException e )
        {
            builder = Response.status( Status.INTERNAL_SERVER_ERROR );
        }

        return builder == null ? Response.serverError()
                                         .build() : builder.build();
    }

    @GET
    @Path( "info/{name}" )
    @Produces( MediaType.TEXT_PLAIN )
    public Response getFileInfoText( @PathParam( "workspaceName" ) final String workspaceName,
                                     @PathParam( "name" ) final String filename )
    {
        SecurityUtils.getSubject()
                     .isPermitted( Permission.name( Workspace.NAMESPACE, workspaceName, Permission.READ ) );

        ResponseBuilder builder = null;
        try
        {
            final WorkspaceFile wsFile = _getFileInfo( workspaceName, filename );
            if ( wsFile == null )
            {
                builder = Response.status( Status.NOT_FOUND );
            }
            else
            {
                final StringBuilder sb = new StringBuilder();
                sb.append( wsFile.getContentLength() )
                  .append( "  " )
                  .append( new Date( wsFile.getLastModified() ) )
                  .append( "  " )
                  .append( wsFile.getFileName() );

                logger.info( "Result:\n\n%s\n\n", sb );

                builder = Response.ok( sb.toString() );
            }
        }
        catch ( final WorkspaceDataException e )
        {
            builder = Response.status( Status.INTERNAL_SERVER_ERROR );
        }

        return builder == null ? Response.serverError()
                                         .build() : builder.build();
    }

    @GET
    @Path( "{name}" )
    @Produces( MediaType.APPLICATION_OCTET_STREAM )
    public Response getFile( @PathParam( "workspaceName" ) final String workspaceName,
                             @PathParam( "name" ) final String filename )
    {
        SecurityUtils.getSubject()
                     .isPermitted( Permission.name( Workspace.NAMESPACE, workspaceName, Permission.READ ) );

        try
        {
            final WorkspaceFile wsFile = wsDataManager.getWorkspaceFile( workspaceName, filename );
            if ( wsFile == null )
            {
                return Response.status( Status.NOT_FOUND )
                               .build();
            }

            final Attachment attachment = wsDataManager.getWorkspaceFileData( workspaceName, filename );

            final ResponseBuilder builder = Response.ok( attachment.getData() );
            builder.header( "Content-Type", wsFile.getContentType() );
            builder.header( "Content-Length", wsFile.getContentLength() );

            final long lastMod = wsFile.getLastModified();
            if ( lastMod > 0 )
            {
                final SimpleDateFormat format = new SimpleDateFormat( "EEE, d MM yyyy HH:mm:ss z" );
                final Calendar cal = Calendar.getInstance( TimeZone.getTimeZone( "GMT" ) );
                cal.setTimeInMillis( lastMod );

                final String time = format.format( cal.getTime() );
                builder.header( "Last-Modified", time );
            }

            return builder.build();
        }
        catch ( final WorkspaceDataException e )
        {
            logger.error( "Failed to retrieve file-data for: %s in workspace: %s. Reason: %s", e, filename,
                          workspaceName, e.getMessage() );
            return Response.serverError()
                           .build();
        }
        catch ( final IOException e )
        {
            logger.error( "Failed to retrieve file-data for: %s in workspace: %s. Reason: %s", e, filename,
                          workspaceName, e.getMessage() );
            return Response.serverError()
                           .build();
        }
    }

    public List<WorkspaceFile> getFiles( final String workspaceName )
        throws WorkspaceDataException
    {
        SecurityUtils.getSubject()
                     .isPermitted( Permission.name( Workspace.NAMESPACE, workspaceName, Permission.READ ) );

        try
        {
            return wsDataManager.getWorkspaceFiles( workspaceName );
        }
        catch ( final WorkspaceDataException e )
        {
            logger.error( "Failed to retrieve file listing for workspace: %s. Reason: %s", e, workspaceName,
                          e.getMessage() );
            throw e;
        }
    }

    private WorkspaceFile _getFileInfo( final String workspaceName, final String filename )
        throws WorkspaceDataException
    {
        SecurityUtils.getSubject()
                     .isPermitted( Permission.name( Workspace.NAMESPACE, workspaceName, Permission.READ ) );

        try
        {
            return wsDataManager.getWorkspaceFile( workspaceName, filename );
        }
        catch ( final WorkspaceDataException e )
        {
            logger.error( "Failed to retrieve info for file: %s in workspace: %s. Reason: %s", e, filename,
                          workspaceName, e.getMessage() );
            throw e;
        }
    }

}
