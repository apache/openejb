package org.openejb.deployment;

/**
 * @version $Revision$ $Date$
 */

import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.jar.JarFile;
import java.net.URLClassLoader;
import java.net.URL;

import javax.management.ObjectName;

import junit.framework.*;
import org.openejb.deployment.AxisWebServiceContainerBuilder;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.gbean.GBeanData;

public class AxisWebServiceContainerBuilderTest extends TestCase {
    AxisWebServiceContainerBuilder axisWebServiceContainerBuilder;

    public void testBuildGBeanData() throws Exception {
        AxisWebServiceContainerBuilder axisWebServiceContainerBuilder = new AxisWebServiceContainerBuilder();

        URLClassLoader classLoader = new URLClassLoader(new URL[]{new File("target/test-ejb-jar.jar").toURL()},this.getClass().getClassLoader());
        JarFile jarFile = new JarFile("target/test-ejb-jar.jar");

        String serviceEndpointName = "org.openejb.test.simple.slsb.SimpleStatelessSessionEndpoint";
        ObjectName sessionObjectName = JMXUtil.getObjectName("openejb:type=StatelessSessionBean,name=SimpleStatelessSession");
        GBeanData gBeanData = axisWebServiceContainerBuilder.buildGBeanData(sessionObjectName, null, "SimpleStatelessSession", serviceEndpointName, jarFile, classLoader );
        Object attribute = gBeanData.getAttribute("serviceDesc");
        // serialize
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);

        out.writeObject(attribute);

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        Object object = in.readObject();

    }
}