package org.openejb.deployment;

import java.io.File;

import junit.framework.TestCase;
import org.openejb.xbeans.ejbjar.OpenejbOpenejbJarType;
import org.apache.geronimo.kernel.Kernel;

/**
 */
public class PlanParsingTest extends TestCase {
    private Kernel kernel = null;

    private OpenEJBModuleBuilder builder = new OpenEJBModuleBuilder(null, null, kernel);
    File basedir = new File(System.getProperty("basedir", "."));

    public void testResourceRef() throws Exception {
        File resourcePlan = new File(basedir, "src/test-resources/plans/plan1.xml");
        assertTrue(resourcePlan.exists());
        OpenejbOpenejbJarType openejbJar = builder.getOpenejbJar(resourcePlan, null, true, null, null);
        assertEquals(1, openejbJar.getEnterpriseBeans().getSessionArray()[0].getResourceRefArray().length);
        System.out.println(openejbJar.toString());
    }

}
