package org.openejb.deployment;

/**
 * @version $Revision$ $Date$
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;

import javax.management.ObjectName;

import junit.framework.TestCase;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.ObjectInputStreamExt;
import org.apache.geronimo.kernel.jmx.JMXUtil;

public class AxisWebServiceContainerBuilderTest extends TestCase {
    AxisWebServiceContainerBuilder axisWebServiceContainerBuilder;

    public void testNothing() {}

    public void XtestBuildGBeanData() throws Exception {
        AxisWebServiceContainerBuilder axisWebServiceContainerBuilder = new AxisWebServiceContainerBuilder();

        URLClassLoader classLoader = new URLClassLoader(new URL[]{new File("target/test-ejb-jar.jar").toURL()},this.getClass().getClassLoader());
        JarFile jarFile = new JarFile("target/test-ejb-jar.jar");

        String serviceEndpointName = "org.openejb.test.simple.slsb.SimpleStatelessSessionEndpoint";
        ObjectName sessionObjectName = JMXUtil.getObjectName("openejb:type=StatelessSessionBean,name=SimpleStatelessSession");
        GBeanData gBeanData = axisWebServiceContainerBuilder.buildGBeanData(sessionObjectName, null, "SimpleStatelessSession", serviceEndpointName, jarFile, classLoader, null);
        Object attribute = gBeanData.getAttribute("serviceDesc");
        // serialize
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);

        out.writeObject(attribute);

        ObjectInputStream in = new ObjectInputStreamExt(new ByteArrayInputStream(baos.toByteArray()), classLoader);
        Object object = in.readObject();

    }
}