package org.commonjava.web.fd;

import org.junit.Test;

public interface WSFileDataTestPlan
{

    @Test
    void storeFileWithWorkspaceAndRetrieveInfo()
        throws Exception;

    @Test
    void storeFileWithWorkspaceAndRetrieveData()
        throws Exception;

    @Test
    void storeFileAndDeleteFileWithWorkspace()
        throws Exception;

}
