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

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.commonjava.auth.couch.model.Permission;
import org.commonjava.util.logging.Logger;
import org.commonjava.web.common.model.Listing;
import org.commonjava.web.fd.config.FileDepotConfiguration;
import org.commonjava.web.fd.data.WorkspaceDataException;
import org.commonjava.web.fd.data.WorkspaceDataManager;
import org.commonjava.web.fd.model.FileInfo;
import org.commonjava.web.fd.model.Workspace;

@Path( "/files/{workspaceName}" )
@RequestScoped
@RequiresAuthentication
public class FileResource
{

    private final Logger logger = new Logger( getClass() );

    @Inject
    private FileDepotConfiguration config;

    @Inject
    private WorkspaceDataManager wsDataManager;

    @PUT
    @Path( "{name}" )
    public Response save( @PathParam( "workspaceName" ) final String workspaceName,
                          @PathParam( "name" ) final String filename,
                          @Context final HttpServletRequest request )
    {
        SecurityUtils.getSubject().isPermitted( Permission.name( Workspace.NAMESPACE,
                                                                 Permission.CREATE ) );

        InputStream in = null;
        try
        {
            in = request.getInputStream();
        }
        catch ( final IOException e )
        {
            logger.error( "Failed to get input stream from request: %s", e, e.getMessage() );
            throw new WebApplicationException( Status.BAD_REQUEST );
        }

        File f;
        try
        {
            f = getFilesystemFile( workspaceName, filename );
        }
        catch ( WorkspaceDataException e )
        {
            return Response.status( Status.INTERNAL_SERVER_ERROR ).build();
        }

        if ( f.exists() )
        {
            logger.error( "File already exists: %s", f.getName() );
            throw new WebApplicationException( Status.CONFLICT );
        }

        f.getParentFile().mkdirs();

        FileOutputStream out = null;
        try
        {
            out = new FileOutputStream( f );
        }
        catch ( final IOException e )
        {
            logger.error( "Cannot write file: %s. Reason: %s", e, f.getName(), e.getMessage() );
            throw new WebApplicationException( Status.INTERNAL_SERVER_ERROR );
        }
        finally
        {
            closeQuietly( in );
            closeQuietly( out );
        }

        return Response.created( UriBuilder.fromResource( getClass() ).path( filename ).build() ).build();
    }

    @DELETE
    @Path( "{name}" )
    public Response delete( @PathParam( "workspaceName" ) final String workspaceName,
                            @PathParam( "name" ) final String filename )
    {
        SecurityUtils.getSubject().isPermitted( Permission.name( Workspace.NAMESPACE,
                                                                 Permission.READ ) );

        File f;
        try
        {
            f = getFilesystemFile( workspaceName, filename );
        }
        catch ( WorkspaceDataException e )
        {
            return Response.status( Status.INTERNAL_SERVER_ERROR ).build();
        }

        if ( f.exists() )
        {
            if ( f.delete() )
            {
                return Response.ok().build();
            }
            else
            {
                throw new WebApplicationException( Status.NOT_MODIFIED );
            }
        }
        else
        {
            logger.info( "File not found: %s", f );
            throw new WebApplicationException( Status.NOT_FOUND );
        }
    }

    public Listing<FileInfo> getFiles( final String workspaceName )
        throws WorkspaceDataException
    {
        final List<FileInfo> result = new ArrayList<FileInfo>();
        final Workspace ws;
        try
        {
            ws = wsDataManager.getWorkspace( workspaceName );
        }
        catch ( WorkspaceDataException e )
        {
            logger.error( "Failed to retrieve workspace info: %s. Reason: %s", e, workspaceName,
                          e.getMessage() );
            throw e;
        }

        SecurityUtils.getSubject().checkPermission( Permission.name( Workspace.NAMESPACE,
                                                                     Permission.READ ) );

        final File dir = new File( config.getUploadDirectory(), ws.getPathName() );

        if ( dir.exists() )
        {
            for ( final String name : dir.list() )
            {
                final File f = new File( dir, name );
                result.add( new FileInfo( f ) );
            }
        }

        return new Listing<FileInfo>( result );
    }

    @GET
    @Path( "list" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Listing<FileInfo> list( @PathParam( "workspaceName" ) final String workspaceName )
    {
        SecurityUtils.getSubject().isPermitted( Permission.name( Workspace.NAMESPACE,
                                                                 Permission.READ ) );

        try
        {
            return getFiles( workspaceName );
        }
        catch ( WorkspaceDataException e )
        {
            throw new WebApplicationException(
                                               Response.status( Status.INTERNAL_SERVER_ERROR ).build() );
        }
    }

    @GET
    @Path( "list" )
    @Produces( MediaType.TEXT_PLAIN )
    public String listText( @PathParam( "workspaceName" ) final String workspaceName )
    {
        SecurityUtils.getSubject().isPermitted( Permission.name( Workspace.NAMESPACE,
                                                                 Permission.READ ) );

        final StringBuilder sb = new StringBuilder();
        try
        {
            for ( final FileInfo f : getFiles( workspaceName ) )
            {
                if ( sb.length() > 0 )
                {
                    sb.append( "\n" );
                }

                sb.append( f.length() ).append( "  " ).append( new Date( f.lastModified() ) ).append( "  " ).append( f.getName() );
            }
        }
        catch ( WorkspaceDataException e )
        {
            throw new WebApplicationException(
                                               Response.status( Status.INTERNAL_SERVER_ERROR ).build() );
        }

        return sb.toString();
    }

    @GET
    @Path( "{name}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public FileInfo getFileInfo( @PathParam( "workspaceName" ) final String workspaceName,
                                 @PathParam( "name" ) final String filename )
    {
        SecurityUtils.getSubject().isPermitted( Permission.name( Workspace.NAMESPACE,
                                                                 Permission.READ ) );

        try
        {
            return _getFileInfo( workspaceName, filename );
        }
        catch ( WorkspaceDataException e )
        {
            throw new WebApplicationException(
                                               Response.status( Status.INTERNAL_SERVER_ERROR ).build() );
        }
    }

    @GET
    @Path( "{name}" )
    @Produces( MediaType.TEXT_PLAIN )
    public Response getFileInfoText( @PathParam( "workspaceName" ) final String workspaceName,
                                     @PathParam( "name" ) final String filename )
    {
        SecurityUtils.getSubject().isPermitted( Permission.name( Workspace.NAMESPACE,
                                                                 Permission.READ ) );

        String result;
        try
        {
            result = String.valueOf( _getFileInfo( workspaceName, filename ) );
        }
        catch ( WorkspaceDataException e )
        {
            return Response.status( Status.INTERNAL_SERVER_ERROR ).build();
        }

        logger.info( "Result:\n\n%s\n\n", result );

        return Response.ok( result, MediaType.TEXT_PLAIN ).build();
    }

    private FileInfo _getFileInfo( final String workspaceName, final String filename )
        throws WorkspaceDataException
    {
        SecurityUtils.getSubject().checkPermission( "view:file-info" );

        File f = getFilesystemFile( workspaceName, filename );

        if ( f.exists() )
        {
            return new FileInfo( f );
        }
        else
        {
            logger.error( "File not found: %s", f );
            throw new WebApplicationException( Status.NOT_FOUND );
        }
    }

    private File getFilesystemFile( final String workspaceName, final String filename )
        throws WorkspaceDataException
    {
        File f;
        try
        {
            final Workspace ws = wsDataManager.getWorkspace( workspaceName );

            final File dir = new File( config.getUploadDirectory(), ws.getPathName() );
            f = new File( dir, URLDecoder.decode( filename, "UTF-8" ) );
        }
        catch ( final UnsupportedEncodingException e )
        {
            logger.error( "Failed to decode filename: %s", e, e.getMessage() );
            throw new WebApplicationException( Status.BAD_REQUEST );
        }
        catch ( WorkspaceDataException e )
        {
            logger.error( "Failed to retrieve workspace info: %s. Reason: %s", e, workspaceName,
                          e.getMessage() );
            throw e;
        }

        return f;
    }

    @GET
    @Path( "{name}/data" )
    @Produces( MediaType.APPLICATION_OCTET_STREAM )
    public Response getFile( @PathParam( "workspaceName" ) final String workspaceName,
                             @PathParam( "name" ) final String filename )
    {
        SecurityUtils.getSubject().isPermitted( Permission.name( Workspace.NAMESPACE,
                                                                 Permission.READ ) );

        File f;
        try
        {
            f = getFilesystemFile( workspaceName, filename );
        }
        catch ( WebApplicationException e )
        {
            return Response.status( Status.BAD_REQUEST ).build();
        }
        catch ( WorkspaceDataException e )
        {
            return Response.status( Status.INTERNAL_SERVER_ERROR ).build();
        }

        if ( f.exists() )
        {
            return Response.ok( f ).build();
            // return Response.ok( f ).header( "Content-Disposition",
            // "attachment; filename=\"" + filename + "\"" ).build();

            // .header( "Content-Disposition", "inline; filename=\"" + filename + "\"" )
        }
        else
        {
            return Response.status( Status.NOT_FOUND ).build();
        }
    }

}
