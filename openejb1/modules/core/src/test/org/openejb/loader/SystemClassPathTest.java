package org.openejb.loader;
/**
 * @version $Revision$ $Date$
 */

import junit.framework.*;
import org.openejb.loader.SystemClassPath;

import java.net.URL;

public class SystemClassPathTest extends TestCase {
    SystemClassPath systemClassPath;

    public void testAddJarToPath() throws Exception {
        SystemClassPath systemClassPath = new SystemClassPath();

        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();

        try {
            systemClassLoader.loadClass("org.apache.commons.io.HexDump");
            fail("Class already exists");
        } catch (ClassNotFoundException e) {
            // this should fail
        }

        URL commonsIoJar = new URL("http://www.ibiblio.org/maven/commons-io/jars/commons-io-1.0.jar");
        systemClassPath.addJarToPath(commonsIoJar);

        try {
            systemClassLoader.loadClass("org.apache.commons.io.HexDump");
        } catch (ClassNotFoundException e) {
            // this should fail pass
            fail("Class should exist");
        }
    }
}