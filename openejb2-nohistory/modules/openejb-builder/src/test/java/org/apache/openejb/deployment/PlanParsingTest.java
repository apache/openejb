package org.apache.openejb.deployment;

import java.io.File;
import java.util.Collections;

import junit.framework.TestCase;
import org.apache.geronimo.j2ee.deployment.NamingBuilderCollection;
import org.apache.openejb.xbeans.ejbjar.OpenejbOpenejbJarType;

/**
 */
public class PlanParsingTest extends TestCase {
    private OpenEjbModuleBuilder builder;
    File basedir = new File(System.getProperty("basedir", "."));

    protected void setUp() throws Exception {
        super.setUp();
        builder = new OpenEjbModuleBuilder(null, 
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                Collections.EMPTY_LIST,
                Collections.EMPTY_LIST,
                new NamingBuilderCollection(null, null),
                new MockResourceEnvironmentSetter(),
                null,
                null);
    }

    public void testResourceRef() throws Exception {
        File resourcePlan = new File(basedir, "src/test-resources/plans/plan1.xml");
        assertTrue(resourcePlan.exists());
        OpenejbOpenejbJarType openejbJar = builder.getOpenejbJar(resourcePlan, null, true, null, null);
        assertEquals(1, openejbJar.getEnterpriseBeans().getSessionArray()[0].getResourceRefArray().length);
        System.out.println(openejbJar.toString());
    }

}
