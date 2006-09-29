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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.geronimo.deployment.util.NestedJarFile;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;


/**
 *
 * @version $Revision$ $Date$
 */
public class OpenejbJarLocation {
    private static final String OPENEJB_JAR = "META-INF/openejb-jar.xml";
    private static final String GERONIMO_APP = "META-INF/geronimo-application.xml";

    private final String location;

    public OpenejbJarLocation(String location) {
        this.location = location;
    }

    public InputStream getInputStream(Project project) throws BuildException, IOException, XmlException {
        String newLoc = location;
        if (newLoc.startsWith("jar:")) {
            JarFile jarFile;
            newLoc = newLoc.substring("jar:".length());
            if (newLoc.startsWith("jar:")) {
                String name = newLoc.substring("jar:".length(), newLoc.indexOf("!/"));
                jarFile = new JarFile(project.resolveFile(name));
                name = newLoc.substring(newLoc.indexOf("!/") + 2);
                jarFile = new NestedJarFile(jarFile, name);
            } else {
                jarFile = new JarFile(project.resolveFile(newLoc));
            }
            ZipEntry ejbJarEntry = jarFile.getEntry(OPENEJB_JAR);
            if (null == ejbJarEntry) {
                throw new BuildException(OPENEJB_JAR + " not found");
            }
            return jarFile.getInputStream(ejbJarEntry);
        } else if (newLoc.startsWith("nested:")) {
            JarFile jarFile = null;
            newLoc = newLoc.substring("nested:".length());
            InputStream in = null;
            if (newLoc.startsWith("jar:")) {
                String name = newLoc.substring("jar:".length(), newLoc.indexOf("!/"));
                jarFile = new JarFile(project.resolveFile(name));
                ZipEntry entry = jarFile.getEntry(GERONIMO_APP);
                if (null == entry) {
                    throw new BuildException(GERONIMO_APP + " not found");
                }
                in = jarFile.getInputStream(entry);
            } else {
                String name = newLoc.substring(0, newLoc.indexOf("!/"));
                in = new FileInputStream(project.resolveFile(name));
            }
            XmlObject xmlObject = XmlBeansUtil.parse(in);
            int index = Integer.parseInt(newLoc.substring(newLoc.indexOf("!/") + 2));

            int found = 0;
            XmlCursor cursor = xmlObject.newCursor();
            try {
                while (cursor.hasNextToken()) {
                    if (cursor.isStart()) {
                        String localName = cursor.getName().getLocalPart();
                        if (localName.equals("openejb-jar")) {
                            if (found == index) {
                                if (false == cursor.toParent()) {
                                    throw new AssertionError("No parent found.");
                                }
                                return cursor.getObject().newInputStream();
                            }
                            found++;
                        }
                    }
                    cursor.toNextToken();
                }
            } finally {
                cursor.dispose();
            }

            throw new BuildException("Only " + found + " openejb-jar DD are nested.");
        }

        return new FileInputStream(project.resolveFile(newLoc));
    }
}