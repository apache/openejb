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

import sun.misc.URLClassPath;

import java.io.File;
import java.lang.reflect.Method;
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
    private final ClassLoader classLoader;

    /**
     * The addRepository(String jar) method of the Tomcat Common ClassLoader
     */
    private Method addRepositoryMethod;
    private Method addURLMethod;
    private Method getLoaderMethod;


    public TomcatClassPath() {
        this(getCommonLoader(getContextClassLoader()).getParent());
    }

    public TomcatClassPath(ClassLoader classLoader){
        this.classLoader = classLoader;
        try {
            addRepositoryMethod = getAddRepositoryMethod();
        } catch (Exception tomcat4Exception) {
            // Must be tomcat 5
            try {
                addURLMethod = getAddURLMethod();
            } catch (Exception tomcat5Exception) {
                throw new RuntimeException("Failed accessing classloader for Tomcat 4 or 5", tomcat5Exception);
            }
            try {
                getLoaderMethod = getLoaderMethod();
            } catch (Exception e) {
            }
        }
    }

    private static ClassLoader getCommonLoader(ClassLoader loader) {
        if (loader.getClass().getName().equals("org.apache.catalina.loader.StandardClassLoader")) {
            return loader;
        } else {
            return getCommonLoader(loader.getParent());
        }
    }
    
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void addJarsToPath(File dir) throws Exception {
        String[] jarNames = dir.list(new java.io.FilenameFilter() {
            public boolean accept(File dir, String name) {
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
        this._addJarToPath(jar);
        rebuild();
    }

    public void _addJarToPath(URL jar) throws Exception {
        if (addRepositoryMethod != null){
            String path = jar.toExternalForm();
            addRepositoryMethod.invoke(getClassLoader(), new Object[]{path});
        } else {
            addURLMethod.invoke(getClassLoader(), new Object[]{jar});
            int index = 0;
            while (getLoader(index++) != null);
        }
    }

    private Object getLoader(int i) {
        if (getLoaderMethod == null){
            return null;
        }

        try {
            sun.misc.URLClassPath cp = getURLClassPath((URLClassLoader) getClassLoader());
            Object object = getLoaderMethod.invoke(cp, new Object[]{new Integer(i)});
            return object;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    protected void rebuild() {
        try {
            sun.misc.URLClassPath cp = getURLClassPath((URLClassLoader) getClassLoader());
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

    /**
     * This method gets the Tomcat StandardClassLoader.addRepository method
     * via reflection. This allows us to call the addRepository method for
     * Tomcat integration, but doesn't require us to include or ship any
     * Tomcat libraries.
     *
     * @return URLClassLoader.addURL method instance
     */
    private java.lang.reflect.Method getAddURLMethod() throws Exception {
        return (java.lang.reflect.Method) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                java.lang.reflect.Method method = null;
                try {
                    Class clazz = URLClassLoader.class;
                    method = clazz.getDeclaredMethod("addURL", new Class[]{URL.class});
                    method.setAccessible(true);
                    return method;
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                return method;
            }
        });
    }

    private java.lang.reflect.Method getLoaderMethod() throws Exception {
        return (java.lang.reflect.Method) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                java.lang.reflect.Method method = null;
                try {
                    Class clazz = URLClassPath.class;
                    method = clazz.getDeclaredMethod("getLoader", new Class[]{Integer.TYPE});
                    method.setAccessible(true);
                    return method;
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                return method;
            }
        });
    }

    private Method getAddRepositoryMethod() throws Exception {
        return (Method) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                Method method = null;
                try {
                    Class clazz = getClassLoader().getClass();
                    method = clazz.getDeclaredMethod("addRepository", new Class[]{String.class});
                    method.setAccessible(true);
                    return method;
                } catch (Exception e2) {
                    throw (IllegalStateException) new IllegalStateException("Unable to find or access the addRepository method in StandardClassLoader").initCause(e2);
                }
            }
        });
    }

}
