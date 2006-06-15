package org.openejb.deployment;

import java.io.File;

import junit.framework.TestCase;
import org.openejb.xbeans.ejbjar.OpenejbOpenejbJarType;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.j2ee.deployment.WebServiceBuilder;

/**
 */
public class PlanParsingTest extends TestCase {
    private static final File basedir = new File(System.getProperty("basedir", "."));
    private OpenEJBModuleBuilder builder;

    protected void setUp() throws Exception {
        builder = new OpenEJBModuleBuilder((Environment)null, (AbstractNameQuery)null, (GBeanData) null, (WebServiceBuilder)null, (Kernel)null);
    }

    public void testResourceRef() throws Exception {
        File resourcePlan = new File(basedir, "src/test-resources/plans/plan1.xml");
        assertTrue(resourcePlan.exists());
        OpenejbOpenejbJarType openejbJar = builder.getOpenejbJar(resourcePlan, null, true, null, null);
        assertEquals(1, openejbJar.getEnterpriseBeans().getSessionArray()[0].getResourceRefArray().length);
        System.out.println(openejbJar.toString());
    }

}
