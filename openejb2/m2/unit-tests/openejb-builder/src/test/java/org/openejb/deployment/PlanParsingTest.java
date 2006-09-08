package org.openejb.deployment;

import java.io.File;

import junit.framework.TestCase;
import org.openejb.xbeans.ejbjar.OpenejbOpenejbJarType;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.repository.Repository;

/**
 */
public class PlanParsingTest extends TestCase {
    private Repository repository = null;

    private OpenEJBModuleBuilder builder;
    File basedir = new File(System.getProperty("basedir", "."));

    protected void setUp() throws Exception {
        builder = new OpenEJBModuleBuilder(null, null, null, null, repository, null);
    }

    public void testResourceRef() throws Exception {
        File resourcePlan = new File(basedir, "src/test/resources/plans/plan1.xml");
        assertTrue(resourcePlan.exists());
        OpenejbOpenejbJarType openejbJar = builder.getOpenejbJar(resourcePlan, null, true, null, null);
        assertEquals(1, openejbJar.getEnterpriseBeans().getSessionArray()[0].getResourceRefArray().length);
        System.out.println(openejbJar.toString());
    }

}
