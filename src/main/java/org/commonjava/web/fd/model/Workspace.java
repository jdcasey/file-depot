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
package org.commonjava.web.fd.model;

import static org.commonjava.couch.util.IdUtils.namespaceId;

import java.util.HashMap;
import java.util.Map;

import org.commonjava.couch.model.AbstractCouchDocument;
import org.commonjava.couch.model.DenormalizedCouchDoc;

import com.google.gson.annotations.Expose;

public class Workspace
    extends AbstractCouchDocument
    implements DenormalizedCouchDoc
{

    public static final String NAMESPACE = "workspace";

    public static final String METADATA_LAST_MODIFIED = "last_modified";

    private String name;

    private Map<String, Map<String, String>> fileMetadata;

    @Expose( deserialize = false )
    private final String doctype = NAMESPACE;

    Workspace()
    {
    }

    public Workspace( final String name )
    {
        this.name = name;
        calculateDenormalizedFields();
    }

    public String getName()
    {
        return name;
    }

    void setName( final String name )
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return String.format( "Workspace [name='%s']", name );
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( name == null ) ? 0 : name.hashCode() );
        return result;
    }

    @Override
    public boolean equals( final Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( obj == null )
        {
            return false;
        }
        if ( getClass() != obj.getClass() )
        {
            return false;
        }
        final Workspace other = (Workspace) obj;
        if ( name == null )
        {
            if ( other.name != null )
            {
                return false;
            }
        }
        else if ( !name.equals( other.name ) )
        {
            return false;
        }
        return true;
    }

    public String getDoctype()
    {
        return doctype;
    }

    @Override
    public void calculateDenormalizedFields()
    {
        setCouchDocId( namespaceId( NAMESPACE, this.name ) );
    }

    public static String adminRole( final String wsName )
    {
        return namespaceId( NAMESPACE, wsName, "admin" );
    }

    public static String userRole( final String wsName )
    {
        return namespaceId( NAMESPACE, wsName, "user" );
    }

    public static String getWorkspaceForRole( final String role )
    {
        final String[] parts = role.split( ":" );
        if ( parts.length < 2 || !NAMESPACE.equals( parts[0] ) )
        {
            return null;
        }

        return parts[1];
    }

    public Map<String, Map<String, String>> getFileMetadata()
    {
        return fileMetadata;
    }

    void setFileMetadata( final Map<String, Map<String, String>> fileMetadata )
    {
        this.fileMetadata = fileMetadata;
    }

    public synchronized void setFileMetadata( final String file, final String key, final String value )
    {
        if ( fileMetadata == null )
        {
            fileMetadata = new HashMap<String, Map<String, String>>();
        }

        Map<String, String> md = fileMetadata.get( file );
        if ( md == null )
        {
            md = new HashMap<String, String>();
            fileMetadata.put( file, md );
        }

        md.put( key, value );
    }

    public synchronized void setFileMetadata( final String file, final Map<String, String> map )
    {
        if ( fileMetadata == null )
        {
            fileMetadata = new HashMap<String, Map<String, String>>();
        }

        fileMetadata.put( file, map );
    }

    public Map<String, String> getFileMetadataMap( final String file )
    {
        if ( fileMetadata != null )
        {
            return fileMetadata.get( file );
        }

        return null;
    }

    public String getFileMetadata( final String file, final String key )
    {
        if ( fileMetadata != null )
        {
            final Map<String, String> md = fileMetadata.get( file );
            if ( md != null )
            {
                return md.get( key );
            }
        }

        return null;
    }
}
