package org.commonjava.web.fd.data;

import java.util.HashSet;
import java.util.Set;

import org.commonjava.couch.db.model.AppDescription;

public class WorkspaceAppDescription
    implements AppDescription
{

    public static final String APPLICATION_RESOURCE = "workspace-logic";

    public enum View
    {
        WORKSPACES( "workspaces" );

        String name;

        private View( final String name )
        {
            this.name = name;
        }

        public String viewName()
        {
            return name;
        }
    }

    private static Set<String> viewNames;

    @Override
    public String getAppName()
    {
        return APPLICATION_RESOURCE;
    }

    @Override
    public String getClasspathAppResource()
    {
        return APPLICATION_RESOURCE;
    }

    @Override
    public Set<String> getViewNames()
    {
        if ( viewNames == null )
        {
            Set<String> names = new HashSet<String>();
            for ( View view : View.values() )
            {
                names.add( view.viewName() );
            }

            viewNames = names;
        }

        return viewNames;
    }

}
