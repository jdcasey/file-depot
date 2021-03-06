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

import org.commonjava.couch.conf.CouchDBConfiguration;

public interface FileDepotConfiguration
{

    final String DEFAULT_DB_NAME = "file-depot";

    final int DEFAULT_FILE_EXPIRATION_MINS = 43200;

    int getFileExpirationMins();

    CouchDBConfiguration getDatabaseConfig();

    File getStorageDir();

    File getUploadDir();

}
