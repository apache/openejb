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
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;


/**
 *
 * @version $Revision$ $Date$
 */
public class EjbJarLocation {
    private static final String EJB_JAR = "META-INF/ejb-jar.xml";
    
    private final String location;

    public EjbJarLocation(String location) {
        this.location = location;
    }

    public InputStream getInputStream(Project project) throws BuildException, IOException {
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
            ZipEntry entry = jarFile.getEntry(EJB_JAR);
            if (null == entry) {
                throw new BuildException(EJB_JAR + " not found");
            }
            return jarFile.getInputStream(entry);
        }
        
        return new FileInputStream(project.resolveFile(newLoc));
    }
}