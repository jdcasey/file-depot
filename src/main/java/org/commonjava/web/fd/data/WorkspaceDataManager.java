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
package org.commonjava.web.fd.data;

import static org.commonjava.couch.rbac.Permission.ADMIN;
import static org.commonjava.couch.rbac.Permission.CREATE;
import static org.commonjava.couch.rbac.Permission.READ;
import static org.commonjava.couch.util.IdUtils.namespaceId;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.shiro.subject.Subject;
import org.commonjava.auth.couch.data.UserDataException;
import org.commonjava.auth.couch.data.UserDataManager;
import org.commonjava.couch.conf.CouchDBConfiguration;
import org.commonjava.couch.db.CouchDBException;
import org.commonjava.couch.db.CouchManager;
import org.commonjava.couch.model.CouchDocRef;
import org.commonjava.couch.rbac.Permission;
import org.commonjava.couch.rbac.Role;
import org.commonjava.couch.rbac.User;
import org.commonjava.couch.util.JoinString;
import org.commonjava.util.logging.Logger;
import org.commonjava.web.fd.config.FileDepotConfiguration;
import org.commonjava.web.fd.data.WorkspaceAppDescription.View;
import org.commonjava.web.fd.inject.FileDepotData;
import org.commonjava.web.fd.model.Workspace;

@Singleton
public class WorkspaceDataManager
{
    private final Logger logger = new Logger( getClass() );

    @Inject
    private UserDataManager userMgr;

    @Inject
    @FileDepotData
    private CouchManager couch;

    @Inject
    private FileDepotConfiguration config;

    @Inject
    @FileDepotData
    private CouchDBConfiguration couchConfig;

    public WorkspaceDataManager()
    {
    }

    public WorkspaceDataManager( final FileDepotConfiguration config, final CouchManager couch )
    {
        this.config = config;
        this.couch = couch;
    }

    public void install()
        throws WorkspaceDataException
    {
        try
        {
            couch.initialize( new WorkspaceAppDescription() );

            userMgr.install();
            userMgr.setupAdminInformation();

            final Permission wsAdmin = new Permission( Workspace.NAMESPACE, Permission.WILDCARD );
            userMgr.storePermission( wsAdmin );

            final Role wsAdminRole = new Role( "workspace-admin", wsAdmin );
            userMgr.storeRole( wsAdminRole );
        }
        catch ( final CouchDBException e )
        {
            throw new WorkspaceDataException(
                                              "Failed to initialize workspace-management database: %s (application: %s). Reason: %s",
                                              e, couchConfig.getDatabaseUrl(),
                                              WorkspaceAppDescription.APPLICATION_RESOURCE, e.getMessage() );
        }
        catch ( final UserDataException e )
        {
            throw new WorkspaceDataException(
                                              "Failed to initialize admin user/privilege information in workspace-management database: %s. Reason: %s",
                                              e, couchConfig.getDatabaseUrl(), e.getMessage() );
        }
    }

    public boolean storeWorkspace( final Workspace workspace )
        throws WorkspaceDataException
    {
        boolean stored = false;
        try
        {
            workspace.calculateDenormalizedFields();
            stored = couch.store( workspace, false );
        }
        catch ( final CouchDBException e )
        {
            throw new WorkspaceDataException( "Failed to store workspace: %s. Reason: %s", e, workspace, e.getMessage() );
        }

        if ( stored )
        {
            final String name = workspace.getName();

            try
            {
                final Map<String, Permission> perms =
                    userMgr.createPermissions( Workspace.NAMESPACE, name, ADMIN, CREATE, READ );

                userMgr.createRole( Workspace.adminRole( name ), perms.values() );
                userMgr.createRole( Workspace.userRole( name ), perms.get( READ ), perms.get( CREATE ) );
            }
            catch ( final UserDataException e )
            {
                throw new WorkspaceDataException(
                                                  "Failed to add permissions and roles for controlling workspace: %s. Reason: %s",
                                                  e, workspace.getName(), e.getMessage() );
            }
        }

        return stored;
    }

    public Workspace getWorkspace( final String name )
        throws WorkspaceDataException
    {
        try
        {
            return couch.getDocument( new CouchDocRef( namespaceId( Workspace.NAMESPACE, name ) ), Workspace.class );
        }
        catch ( final CouchDBException e )
        {
            throw new WorkspaceDataException( "Failed to read workspace: %s. Reason: %s", e, name, e.getMessage() );
        }
    }

    public void deleteWorkspace( final String name )
        throws WorkspaceDataException
    {
        try
        {
            couch.delete( new CouchDocRef( namespaceId( Workspace.NAMESPACE, name ) ) );
        }
        catch ( final CouchDBException e )
        {
            throw new WorkspaceDataException( "Failed to delete workspace: %s. Reason: %s", e, name, e.getMessage() );
        }
    }

    public List<Workspace> getWorkspaces()
        throws WorkspaceDataException
    {
        try
        {
            return couch.getViewListing( new WorkspaceViewRequest( config, View.WORKSPACES ), Workspace.class );
        }
        catch ( final CouchDBException e )
        {
            throw new WorkspaceDataException( "Failed to read workspace listing: %s", e, e.getMessage() );
        }
    }

    public List<Workspace> getWorkspacesForUser( final Subject subject )
        throws WorkspaceDataException
    {
        User user;
        try
        {
            user = userMgr.getUser( subject.getPrincipal()
                                           .toString() );
        }
        catch ( final UserDataException e )
        {
            throw new WorkspaceDataException( "Failed to read user data for workspace retrieval: %s. Reason: %s", e,
                                              subject.getPrincipal(), e.getMessage() );
        }

        final Set<String> roles = user.getRoles();
        final Set<CouchDocRef> workspaceRefs = new HashSet<CouchDocRef>();
        for ( final String role : roles )
        {
            final String wsId = Workspace.getWorkspaceForRole( role );
            if ( wsId != null )
            {
                workspaceRefs.add( new CouchDocRef( namespaceId( Workspace.NAMESPACE, wsId ) ) );
            }
        }

        logger.debug( "Retrieving workspaces for:\n\t%s", new JoinString( "\n\t", workspaceRefs ) );

        try
        {
            return couch.getDocuments( Workspace.class, workspaceRefs );
        }
        catch ( final CouchDBException e )
        {
            throw new WorkspaceDataException( "Failed to retrieve workspaces for user: %s. Reason: %s", e,
                                              subject.getPrincipal(), e.getMessage() );
        }
    }

}
