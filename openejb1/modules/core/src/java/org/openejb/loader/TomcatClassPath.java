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
/*-------------------------------------------------------*/
/* Tomcat ClassLoader Support */
/*-------------------------------------------------------*/
public class TomcatClassPath extends BasicURLClassPath {

    /**
     * The Tomcat Common ClassLoader
     */
    private ClassLoader tomcatLoader;

    /**
     * The addRepository(String jar) method of the Tomcat Common ClassLoader
     */
    private java.lang.reflect.Method addRepositoryMethod;

    public void addJarsToPath(File dir) throws Exception {
        String[] jarNames = dir.list(new java.io.FilenameFilter() {
            public boolean accept(File dir, String name) {
                //System.out.println("FILE "+name);
                return (name.endsWith(".jar") || name.endsWith(".zip"));
            }
        });

        if (jarNames == null) {
            return;
        }

        for (int j = 0; j < jarNames.length; j++) {
            this.addJarToPath(new File(dir, jarNames[j]).toURL());
        }
        rebuild();
    }

    public void addJarToPath(URL jar) throws Exception {
        //System.out.println("[|] TOMCAT "+jar.toExternalForm());
        this._addJarToPath(jar);
        rebuild();
    }

    public void _addJarToPath(URL jar) throws Exception {
        String path = jar.toExternalForm();
        //System.out.println("[] PATH "+path);
        //if (path.startsWith("file:/C")) {
        //    path = path.substring("file:/C".length());
        //    path = "file:C"+path;
        //}
        this.addRepository(path);
        //ClassLoader cl = ClasspathUtils.getContextClassLoader();
        //cl = getCommonLoader(cl);
        //System.out.println("[] "+cl.getClass().getName());
        //System.out.println("[] "+cl);
        //
        ////Reloader loader = (Reloader)cl.getParent();
        //cl = cl.getParent();
        //java.lang.reflect.Method m = getAddRepositoryMethod(
        // cl.getClass());
        //m.invoke( cl, new Object[]{jar.toExternalForm()});
        ////loader.addRepository( jar.toExternalForm() );
    }

    public void addRepository(String path) throws Exception {

        // Add this repository to our underlying class loader
        this.getAddRepositoryMethod().invoke(getCommonLoader(), new Object[] { new URL(path) });
    }

    private void rebuild() {

        try {
            sun.misc.URLClassPath cp = getURLClassPath((URLClassLoader) getCommonLoader());
            URL[] urls = cp.getURLs();
            //for (int i=0; i < urls.length; i++){
            //    System.out.println(urls[i].toExternalForm());
            //}
            if (urls.length < 1)
                return;

            StringBuffer path = new StringBuffer(urls.length * 32);

            File s = new File(urls[0].getFile());
            path.append(s.getPath());
            //System.out.println(s.getPath());

            for (int i = 1; i < urls.length; i++) {
                path.append(File.pathSeparator);

                s = new File(urls[i].getFile());
                //System.out.println(s.getPath());
                path.append(s.getPath());
            }
            System.setProperty("java.class.path", path.toString());
        } catch (Exception e) {
        }

    }

    protected ClassLoader getCommonLoader() {
        if (tomcatLoader == null) {
            tomcatLoader = this.getCommonLoader(getContextClassLoader()).getParent();
        }
        return tomcatLoader;
    }

    private ClassLoader getCommonLoader(ClassLoader loader) {
        if (loader.getClass().getName().equals("org.apache.catalina.loader.StandardClassLoader")) {
            return loader;
        } else {
            return this.getCommonLoader(loader.getParent());
        }
    }

    /**
     * This method gets the Tomcat StandardClassLoader.addRepository method
     * via reflection. This allows us to call the addRepository method for
     * Tomcat integration, but doesn't require us to include or ship any
     * Tomcat libraries.
     *
     * @return URLClassLoader.addURL method instance
     */
    private java.lang.reflect.Method getAddRepositoryMethod() throws Exception {
        if (addRepositoryMethod == null) {

            final Class clazz = URLClassLoader.class;

            this.addRepositoryMethod = (java.lang.reflect.Method) AccessController
                    .doPrivileged(new PrivilegedAction() {
                        public Object run() {
                            java.lang.reflect.Method method = null;
                            try {
                                method = clazz.getDeclaredMethod("addURL", new Class[] { URL.class });
                                method.setAccessible(true);
                            } catch (Exception e2) {
                                e2.printStackTrace();
                            }
                            return method;
                        }
                    });
        }

        return addRepositoryMethod;
    }
}
