package org.openejb.deployment;

import java.io.File;

import junit.framework.TestCase;

/**
 */
public class PlanParsingTest extends TestCase {

    private OpenEJBModuleBuilder builder = new OpenEJBModuleBuilder(null);
    File basedir = new File(System.getProperty("basedir", "."));

    public void testResourceRef() throws Exception {
        // David you didn't check in this file
//        File resourcePlan = new File(basedir, "src/test-resources/plans/plan1.xml");
//        assertTrue(resourcePlan.exists());
//        OpenejbOpenejbJarType openejbJar = builder.getOpenejbJar(resourcePlan, null, true, null, null);
//        assertEquals(1, openejbJar.getEnterpriseBeans().getSessionArray()[0].getResourceRefArray().length);
//        System.out.println(openejbJar.toString());
    }

}
