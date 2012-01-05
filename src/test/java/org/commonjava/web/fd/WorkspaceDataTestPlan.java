package org.commonjava.web.fd;

import org.junit.Test;

public interface WorkspaceDataTestPlan
{

    @Test
    void storeAndRetrieveWorkspace()
        throws Exception;

    @Test
    void storeTwoAndRetrieveAll()
        throws Exception;

    @Test
    void storeThreeAndRetrieveTwoAssociatedWithUser()
        throws Exception;

    @Test
    void storeAndDeleteWorkspace()
        throws Exception;

}