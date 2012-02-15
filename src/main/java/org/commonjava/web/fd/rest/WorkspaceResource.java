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

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
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
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;
import org.commonjava.couch.rbac.Permission;
import org.commonjava.util.logging.Logger;
import org.commonjava.web.fd.data.WorkspaceDataException;
import org.commonjava.web.fd.data.WorkspaceDataManager;
import org.commonjava.web.fd.model.Workspace;
import org.commonjava.web.json.model.Listing;
import org.commonjava.web.json.ser.JsonSerializer;

import com.google.gson.reflect.TypeToken;

@Path( "/workspace" )
@RequestScoped
@RequiresAuthentication
public class WorkspaceResource
{

    private final Logger logger = new Logger( getClass() );

    @Inject
    private WorkspaceDataManager dataManager;

    @Inject
    private JsonSerializer jsonSerializer;

    @Context
    private UriInfo uriInfo;

    // @Context
    // private HttpServletRequest request;

    @PUT
    @Path( "{name}" )
    public Response create( @PathParam( "name" ) final String name )
    {
        SecurityUtils.getSubject()
                     .isPermitted( Permission.name( Workspace.NAMESPACE, Permission.ADMIN ) );

        final Workspace ws = new Workspace( name );
        ResponseBuilder builder;
        try
        {
            if ( dataManager.storeWorkspace( ws ) )
            {
                builder = Response.created( uriInfo.getAbsolutePathBuilder()
                                                   .build() );
            }
            else
            {
                builder = Response.status( Status.CONFLICT )
                                  .entity( "Repository already exists." );
            }
        }
        catch ( final WorkspaceDataException e )
        {
            logger.error( "Failed to create workspace: %s. Reason: %s", e, ws.getName(), e.getMessage() );
            builder = Response.serverError();
        }

        return builder.build();
    }

    @DELETE
    @Path( "{name}" )
    public Response delete( @PathParam( "name" ) final String name )
    {
        SecurityUtils.getSubject()
                     .isPermitted( Permission.name( Workspace.NAMESPACE, Permission.ADMIN ) );

        try
        {
            dataManager.deleteWorkspace( name );
            return Response.ok()
                           .build();
        }
        catch ( final WorkspaceDataException e )
        {
            logger.error( "Failed to delete workspace: %s. Reason: %s", e, name, e.getMessage() );
            throw new WebApplicationException( Status.INTERNAL_SERVER_ERROR );
        }
    }

    @GET
    @Path( "{name}" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response getWorkspace( @PathParam( "name" ) final String name )
    {
        SecurityUtils.getSubject()
                     .isPermitted( Permission.name( Workspace.NAMESPACE, name, Permission.ADMIN ) );

        try
        {
            final Workspace ws = dataManager.getWorkspace( name );
            if ( ws == null )
            {
                return Response.status( Status.NOT_FOUND )
                               .build();
            }
            else
            {
                final String json = jsonSerializer.toString( ws );
                return Response.ok( json )
                               .build();
            }
        }
        catch ( final WorkspaceDataException e )
        {
            logger.error( "Failed to retrieve workspace: %s. Reason: %s", e, name, e.getMessage() );
            return Response.serverError()
                           .build();
        }
    }

    @GET
    @Path( "all" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getAllWorkspaces()
    {
        SecurityUtils.getSubject()
                     .isPermitted( Permission.name( Workspace.NAMESPACE, Permission.ADMIN ) );

        Listing<Workspace> listing;
        try
        {
            listing = new Listing<Workspace>( dataManager.getWorkspaces() );
        }
        catch ( final WorkspaceDataException e )
        {
            logger.error( "Failed to get workspace listing. Reason: %s", e, e.getMessage() );
            throw new WebApplicationException( Status.INTERNAL_SERVER_ERROR );
        }

        final String json = jsonSerializer.toString( listing, new TypeToken<Listing<Workspace>>()
        {
        }.getType() );

        return Response.ok( json )
                       .build();
    }

    @GET
    @Path( "my" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getUserWorkspaces()
    {
        final Subject subject = SecurityUtils.getSubject();

        try
        {
            subject.checkPermission( Permission.name( Workspace.NAMESPACE, Permission.ADMIN ) );

            logger.info( "retrieving ALL workspaces (workspace admin found)" );
            return getAllWorkspaces();
        }
        catch ( final AuthorizationException e )
        {
            // Used to check whether the user can see all workspaces.
        }

        Listing<Workspace> listing;
        try
        {
            logger.info( "retrieving workspaces for user: %s", subject );
            listing = new Listing<Workspace>( dataManager.getWorkspacesForUser( subject ) );
        }
        catch ( final WorkspaceDataException e )
        {
            logger.error( "Failed to get workspace listing for user: %s. Reason: %s", e, subject.getPrincipal(),
                          e.getMessage() );
            throw new WebApplicationException( Status.INTERNAL_SERVER_ERROR );
        }

        final String json = jsonSerializer.toString( listing, new TypeToken<Listing<Workspace>>()
        {
        }.getType() );

        return Response.ok( json )
                       .build();
    }

}
