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
 *    please contact openejb-group@openejb.sf.net.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.sf.net/).
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
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.util;

import java.net.*;
import java.net.URL;
import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public class ClasspathUtils{
    private static java.lang.reflect.Field ucpField;

    /**
     * Appends the jars and zips in the dir to the classpath of the 
     * classloader passed in.
     *
     * @param url the URL to be added to the search path of URLs
     */
    public static void addJarsToSystemPath(String dir) throws Exception {
        addJarsToSystemPath( FileUtils.getDirectory( dir ) );
    }
    
    /**
     * Appends the jars and zips in the dir to the classpath of the 
     * classloader passed in.
     *
     * @param url the URL to be added to the search path of URLs
     */
    public static void addJarsToSystemPath(File dir) throws Exception {
        java.net.URLClassLoader systemLoader = (java.net.URLClassLoader)ClassLoader.getSystemClassLoader();
        addJarsToPath( dir , systemLoader );
    }
    
    /**
     * Appends the jars and zips in the dir to the classpath of the 
     * classloader passed in.
     *
     * @param url the URL to be added to the search path of URLs
     */
    public static void addJarsToPath(String dir, final URLClassLoader loader) throws Exception {
        addJarsToPath( FileUtils.getDirectory( dir ), loader );
    }
    
    /**
     * Appends the jars and zips in the dir to the classpath of the 
     * classloader passed in.
     *
     * @param url the URL to be added to the search path of URLs
     */
    public static void addJarsToPath(final File dir, final URLClassLoader loader) throws Exception {
        //System.out.println("DIR "+dir);

        // Get the list of jars and zips
        String[] jarNames = dir.list(new java.io.FilenameFilter(){
            public boolean accept(File dir, String name) {
                //System.out.println("FILE "+name);
                return (name.endsWith(".jar") ||name.endsWith(".zip"));
            }
        });

        // Create URLs from them
        final URL[] jars = new URL[jarNames.length];
        //System.out.println("URL "+jars.length);
        for (int j=0; j < jarNames.length; j++){
            jars[j] = new File( dir, jarNames[j]).toURL();
            //System.out.println("URL "+jars[j]);
        }

        sun.misc.URLClassPath path = getURLClassPath(loader);
        for (int i=0; i < jars.length; i++){
            //System.out.println("URL "+jars[i]);
            path.addURL( jars[i] );
        }
    }

    /**
     * Appends the jar to the classpath of the classloader passed in.
     *
     * @param url the URL to be added to the search path of URLs
     */
    public static void addJarToSystemPath(String jar) throws Exception {
        addJarToSystemPath( FileUtils.getFile(jar) );
    }       

    /**
     * Appends the jar to the classpath of the classloader passed in.
     *
     * @param url the URL to be added to the search path of URLs
     */
    public static void addJarToSystemPath(final File jar) throws Exception {
        addJarToSystemPath( jar.toURL() );
    }       

    /**
     * Appends the jar to the classpath of the classloader passed in.
     *
     * @param url the URL to be added to the search path of URLs
     */
    public static void addJarToSystemPath(final URL jar) throws Exception {
        java.net.URLClassLoader systemLoader = (java.net.URLClassLoader)ClassLoader.getSystemClassLoader();
        getURLClassPath(systemLoader).addURL( jar );
    }       
    
    /**
     * Appends the jar to the classpath of the classloader passed in.
     *
     * @param url the URL to be added to the search path of URLs
     */
    public static void addJarToPath(String jar, final URLClassLoader loader) throws Exception {
        addJarToPath( FileUtils.getFile(jar), loader );
    }       

    /**
     * Appends the jar to the classpath of the classloader passed in.
     *
     * @param url the URL to be added to the search path of URLs
     */
    public static void addJarToPath(final File jar, final URLClassLoader loader) throws Exception {
        addJarToPath( jar.toURL(), loader );
    }       

    /**
     * Appends the jar to the classpath of the classloader passed in.
     *
     * @param url the URL to be added to the search path of URLs
     */
    public static void addJarToPath(final URL jar, final URLClassLoader loader) throws Exception {
        getURLClassPath(loader).addURL( jar );
    }       
    
    private static sun.misc.URLClassPath getURLClassPath(URLClassLoader loader) throws Exception{
        return (sun.misc.URLClassPath)getUcpField().get(loader);
    }

    private static java.lang.reflect.Field getUcpField() throws Exception{
        if (ucpField == null) {
            // Add them to the URLClassLoader's classpath
            ucpField = (java.lang.reflect.Field)AccessController.doPrivileged(
                new PrivilegedAction(){
                    public Object run() { 
                        java.lang.reflect.Field ucp = null;
                        try{
                        ucp = URLClassLoader.class.getDeclaredField("ucp");
                        ucp.setAccessible(true);
                        } catch (Exception e2){
                            e2.printStackTrace();
                        }
                        return ucp;
                    }
                }
            );
        }
        
        return ucpField;
    }

}


