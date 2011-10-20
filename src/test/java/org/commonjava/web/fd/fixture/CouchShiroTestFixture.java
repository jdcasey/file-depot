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
package org.commonjava.web.fd.fixture;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.UnavailableSecurityManagerException;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.LifecycleUtils;
import org.apache.shiro.util.ThreadState;
import org.commonjava.auth.couch.conf.UserManagerConfiguration;
import org.commonjava.auth.couch.data.PasswordManager;
import org.commonjava.auth.couch.data.UserDataManager;
import org.commonjava.auth.shiro.couch.CouchPermissionResolver;
import org.commonjava.auth.shiro.couch.CouchRealm;
import org.commonjava.couch.conf.CouchDBConfiguration;
import org.commonjava.couch.db.CouchManager;
import org.commonjava.couch.io.CouchAppReader;
import org.commonjava.couch.io.CouchHttpClient;
import org.commonjava.couch.io.Serializer;

public final class CouchShiroTestFixture
{

    private static ThreadState subjectThreadState;

    private CouchShiroTestFixture()
    {}

    public static void setupSecurityManager( final CouchDBConfiguration couchConfig,
                                             final UserManagerConfiguration userConfig,
                                             final Realm... fallbackRealms )
    {
        Serializer serializer = new Serializer();
        CouchManager couch =
            new CouchManager( couchConfig, new CouchHttpClient( couchConfig, serializer ),
                              serializer, new CouchAppReader() );

        UserDataManager mgr = new UserDataManager( userConfig, new PasswordManager(), couch );

        CouchRealm realm = new CouchRealm( mgr, new CouchPermissionResolver( mgr ) );
        realm.setupSecurityManager( fallbackRealms );
    }

    public static void setupSecurityManager( final CouchRealm realm, final Realm... fallbackRealms )
    {
        realm.setupSecurityManager( fallbackRealms );
    }

    public static void teardownSecurityManager()
    {
        clearSubject();

        try
        {
            org.apache.shiro.mgt.SecurityManager securityManager =
                SecurityUtils.getSecurityManager();

            LifecycleUtils.destroy( securityManager );
        }
        catch ( UnavailableSecurityManagerException e )
        {
            // we don't care about this when cleaning up the test environment
        }

        SecurityUtils.setSecurityManager( null );
    }

    public static void setSubject( final Subject subject )
    {
        clearSubject();
        subjectThreadState = new SubjectThreadState( subject );
        subjectThreadState.bind();
    }

    public static void clearSubject()
    {
        if ( subjectThreadState != null )
        {
            subjectThreadState.clear();
            subjectThreadState = null;
        }
    }

}
