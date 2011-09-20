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
package org.commonjava.web.fd.config;

import java.io.File;

import javax.enterprise.inject.Alternative;
import javax.inject.Named;

import org.commonjava.web.config.annotation.ConfigName;
import org.commonjava.web.config.annotation.SectionName;
import org.commonjava.web.config.section.ConfigurationSectionListener;

@SectionName( ConfigurationSectionListener.DEFAULT_SECTION )
@Named( "standalone" )
@Alternative
public class DefaultFileDepotConfiguration
    implements FileDepotConfiguration
{

    protected static final File DEFAULT_UPLOAD_DIR =
        new File( System.getProperty( "java.io.tmpdir", "/tmp" ), "uploads" );

    private File uploadDirectory = DEFAULT_UPLOAD_DIR;

    @Override
    public File getUploadDirectory()
    {
        return uploadDirectory;
    }

    @ConfigName( "upload.dir" )
    public void setUploadDirectory( final File uploadDir )
    {
        this.uploadDirectory = uploadDir;
    }

}
