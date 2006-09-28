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