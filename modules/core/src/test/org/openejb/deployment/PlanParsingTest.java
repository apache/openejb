package org.openejb.deployment;

import java.io.File;

import junit.framework.TestCase;
import org.openejb.xbeans.ejbjar.OpenejbOpenejbJarType;

/**
 */
public class PlanParsingTest extends TestCase {

    private OpenEJBModuleBuilder builder = new OpenEJBModuleBuilder(null);
    File basedir = new File(System.getProperty("basedir", "."));

    public void testResourceRef() throws Exception {
        File resourcePlan = new File(basedir, "src/test-resources/plans/plan1.xml");
        assertTrue(resourcePlan.exists());
        OpenejbOpenejbJarType openejbJar = builder.getOpenejbJar(resourcePlan, null, null, null);
        assertEquals(1, openejbJar.getEnterpriseBeans().getSessionArray()[0].getResourceRefArray().length);
        System.out.println(openejbJar.toString());
    }

}
