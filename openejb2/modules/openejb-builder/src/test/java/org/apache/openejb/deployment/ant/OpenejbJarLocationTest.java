/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.deployment.ant;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.tools.ant.Project;
import org.apache.xmlbeans.XmlException;

import junit.framework.TestCase;


/**
 *
 * @version $Revision$ $Date$
 */
public class OpenejbJarLocationTest extends TestCase {
    private static final File basedir = new File(System.getProperty("basedir", System.getProperty("user.dir")));

    private Project project;

    public void testJar() throws Exception {
        OpenejbJarLocation location = new OpenejbJarLocation("jar:target/test-ejb-jar.jar");
        executeAssert(location);
    }

    public void testJarJar() throws Exception {
        OpenejbJarLocation location = new OpenejbJarLocation("jar:jar:target/test-ear.ear!/test-ejb-jar.jar");
        executeAssert(location);
    }

    public void testNested() throws Exception {
        OpenejbJarLocation location = new OpenejbJarLocation("nested:src/test-ant/META-INF/geronimo-application.xml!/0");
        executeAssert(location);
    }

    public void testNestedJar() throws Exception {
        OpenejbJarLocation location = new OpenejbJarLocation("nested:jar:target/test-ant.ear!/0");
        executeAssert(location);
    }

    public void testBare() throws Exception {
        OpenejbJarLocation location = new OpenejbJarLocation("src/test-ejb-jar/META-INF/openejb-jar.xml");
        executeAssert(location);
    }

    private void executeAssert(OpenejbJarLocation location) throws IOException, XmlException {
        InputStream in = location.getInputStream(project);
        try {
            assertNotNull(in);
        } finally {
            in.close();
        }
    }

    protected void setUp() throws Exception {
        project = new Project();
        project.setBaseDir(basedir);
    }

}