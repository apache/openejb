/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact info@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
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