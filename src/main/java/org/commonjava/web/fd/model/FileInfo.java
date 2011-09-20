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

import java.io.File;
import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class FileInfo
{

    private String name;

    private Long size;

    private Date lastModified;

    public FileInfo()
    {
    }

    public FileInfo( final File file )
    {
        name = file.getName();
        size = file.length();
        lastModified = new Date( file.lastModified() );
    }

    public String getName()
    {
        return name;
    }

    public Long getSize()
    {
        return size;
    }

    public Date getLastModified()
    {
        return lastModified;
    }

    public void setName( final String name )
    {
        this.name = name;
    }

    public void setSize( final Long size )
    {
        this.size = size;
    }

    public void setLastModified( final Date lastModified )
    {
        this.lastModified = lastModified;
    }

    @Override
    public String toString()
    {
        return String.format( "name=%s\nsize=%s\nlastModified=%s", name, size, lastModified );
    }

    public Long length()
    {
        return getSize();
    }

    public Long lastModified()
    {
        return getLastModified().getTime();
    }

}
