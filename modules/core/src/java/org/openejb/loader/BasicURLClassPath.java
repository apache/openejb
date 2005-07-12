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

package org.openejb.loader;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * @version $Revision$ $Date$
 */
public abstract class BasicURLClassPath implements ClassPath {
    public static ClassLoader getContextClassLoader() {
        return (ClassLoader) java.security.AccessController.doPrivileged(new java.security.PrivilegedAction() {
            public Object run() {
                return Thread.currentThread().getContextClassLoader();
            }
        });
    }

    private java.lang.reflect.Field ucpField;

    protected void addJarToPath(final URL jar, final URLClassLoader loader) throws Exception {
        this.getURLClassPath(loader).addURL(jar);
    }

    protected void addJarsToPath(final File dir, final URLClassLoader loader) throws Exception {
        if (dir == null || !dir.exists()) return;
        //System.out.println("DIR "+dir);
        // Get the list of jars and zips
        String[] jarNames = dir.list(new java.io.FilenameFilter() {
            public boolean accept(File dir, String name) {
                //System.out.println("FILE "+name);
                return (name.endsWith(".jar") || name.endsWith(".zip"));
            }
        });

        // Create URLs from them
        final URL[] jars = new URL[jarNames.length];
        for (int j = 0; j < jarNames.length; j++) {
            jars[j] = new File(dir, jarNames[j]).toURL();
        }

        sun.misc.URLClassPath path = getURLClassPath(loader);
        for (int i = 0; i < jars.length; i++) {
            //System.out.println("URL "+jars[i]);
            path.addURL(jars[i]);
        }
    }

    protected sun.misc.URLClassPath getURLClassPath(URLClassLoader loader) throws Exception {
        return (sun.misc.URLClassPath) getUcpField().get(loader);
    }

    private java.lang.reflect.Field getUcpField() throws Exception {
        if (ucpField == null) {
            // Add them to the URLClassLoader's classpath
            ucpField = (java.lang.reflect.Field) AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    java.lang.reflect.Field ucp = null;
                    try {
                        ucp = URLClassLoader.class.getDeclaredField("ucp");
                        ucp.setAccessible(true);
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                    return ucp;
                }
            });
        }

        return ucpField;
    }

}
