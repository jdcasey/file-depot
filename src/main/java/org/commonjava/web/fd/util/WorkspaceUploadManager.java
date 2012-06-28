package org.commonjava.web.fd.util;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.commonjava.web.fd.config.FileDepotConfiguration;

@Singleton
public class WorkspaceUploadManager
{

    @Inject
    private FileDepotConfiguration config;

    public File saveUpload( final InputStream stream )
        throws IOException
    {
        final File dir = config.getUploadDir();
        final File f = new File( dir, UUID.randomUUID()
                                          .toString() );

        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream( f );
            copy( stream, fos );
        }
        finally
        {
            closeQuietly( fos );
        }

        return f;
    }

}
