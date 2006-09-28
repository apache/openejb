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
package org.apache.openejb.util;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 */
public class ClasspathUtils{
    
    private static Loader tomcatLoader = new ClasspathUtils().new TomcatLoader();
    private static Loader sysLoader = new ClasspathUtils().new SystemLoader();
    private static Loader ctxLoader = new ClasspathUtils().new ContextLoader();
    
    public static void addJarToPath(String jar) throws Exception {
        addJarToPath( FileUtils.getHome().getFile(jar) );
    }       

    public static void addJarToPath(final File jar) throws Exception {
        addJarToPath( jar.toURL() );
    }       

    public static void addJarToPath(final URL jar) throws Exception {
        getLoader().addJarToPath( jar );
    }       
    
    public static void addJarToPath(String jar, String loaderName) throws Exception {
        addJarToPath( FileUtils.getHome().getFile(jar), loaderName );
    }       

    public static void addJarToPath(final File jar, String loaderName) throws Exception {
        addJarToPath( jar.toURL() , loaderName);
    }       

    public static void addJarToPath(final URL jar, String loaderName) throws Exception {
        getLoader(loaderName).addJarToPath( jar );
    }       
    
    public static void addJarsToPath(String dir) throws Exception {
        addJarsToPath( FileUtils.getHome().getDirectory(dir) );
    }       

    public static void addJarsToPath(final File dir) throws Exception {
        if ( dir == null ) return;
        getLoader().addJarsToPath( dir );
    }       
    
    public static void addJarsToPath(String dir, String loaderName) throws Exception {
        addJarsToPath( FileUtils.getHome().getDirectory(dir), loaderName );
    }       

    public static void addJarsToPath(final File dir, String loaderName) throws Exception {
        getLoader(loaderName).addJarsToPath( dir );
    }       

    
    /**
     * Appends the jar to the classpath of the classloader passed in.
     *
     * @param url the URL to be added to the search path of URLs
     */
    public static void addJarToSystemPath(String jar) throws Exception {
        addJarToSystemPath( FileUtils.getHome().getFile(jar) );
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
    }       
    
    protected static Loader getLoader(){
        String name = getContextClassLoader().getClass().getName();

        if (name.startsWith("org.apache.catalina.loader")) {
            return tomcatLoader;
        } else if (name.startsWith("org.apache.jasper.servlet")) {
            return tomcatLoader;
        } else if (name.startsWith( "sun.misc.Launcher" )) {
            return sysLoader;
        } else {
            return ctxLoader;
        }
    }
    
    protected static Loader getLoader(String name){

        if (name.equalsIgnoreCase("tomcat")) {
            return tomcatLoader;
        } else if (name.equalsIgnoreCase("bootstrap")) {
            return sysLoader;
        } else if (name.equalsIgnoreCase("system")) {
            return sysLoader;
        } else if (name.equalsIgnoreCase("thread")) {
            return ctxLoader;
        } else if (name.equalsIgnoreCase("context")) {
            return ctxLoader;
        } else {
            return ctxLoader;
        }
    }

    
    public static ClassLoader getContextClassLoader() {
        return (ClassLoader) java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction() {
                public Object run() {
                    return Thread.currentThread().getContextClassLoader();
                }
            }
        );
    }

    public static void rebuildJavaClassPathVariable() throws Exception{

    }


    interface Loader {
        public void addJarsToPath(File dir) throws Exception;
        public void addJarToPath(URL dir) throws Exception;
    }
    
    class BasicURLLoader implements Loader{
        public void addJarsToPath(File dir) throws Exception {
        }
        
        public void addJarToPath(URL jar) throws Exception {
        }
    
        private java.lang.reflect.Field ucpField;
        
    
        protected void addJarToPath(final URL jar, final URLClassLoader loader) throws Exception {
            this.getURLClassPath(loader).addURL( jar );
        }
    
        protected void addJarsToPath(final File dir, final URLClassLoader loader) throws Exception {
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
        for (int j=0; j < jarNames.length; j++){
            jars[j] = new File( dir, jarNames[j]).toURL();
            }
        
        sun.misc.URLClassPath path = getURLClassPath(loader);
        for (int i=0; i < jars.length; i++){
            //System.out.println("URL "+jars[i]);
            path.addURL( jars[i] );
        }
    }

        protected sun.misc.URLClassPath getURLClassPath(URLClassLoader loader) throws Exception{
        return (sun.misc.URLClassPath)getUcpField().get(loader);
    }

        private java.lang.reflect.Field getUcpField() throws Exception{
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

    /*-------------------------------------------------------*/
    /*    System ClassLoader Support                         */
    /*-------------------------------------------------------*/
    class SystemLoader extends BasicURLLoader{
        
        private URLClassLoader sysLoader;
    
        public void addJarsToPath(File dir) throws Exception {
            this.addJarsToPath( dir , getSystemLoader() );
            this.rebuildJavaClassPathVariable();
        }
        
        public void addJarToPath(URL jar) throws Exception {
            //System.out.println("[|] SYSTEM "+jar.toExternalForm());
            this.addJarToPath( jar, getSystemLoader() );
            this.rebuildJavaClassPathVariable();
        }
    
        private URLClassLoader getSystemLoader() throws Exception{
            if (sysLoader == null) {
                sysLoader = (java.net.URLClassLoader)ClassLoader.getSystemClassLoader();
            }
            return sysLoader;
        }
        
        private void rebuildJavaClassPathVariable() throws Exception{
            sun.misc.URLClassPath cp = getURLClassPath(getSystemLoader());
            URL[] urls = cp.getURLs();
            //for (int i=0; i < urls.length; i++){
            //    System.out.println(urls[i].toExternalForm());
            //}
            if (urls.length < 1) return;
    
            StringBuffer path = new StringBuffer(urls.length*32);
            
            File s = new File( urls[0].getFile() );
            path.append( s.getPath() );
            //System.out.println(s.getPath());
    
            for (int i=1; i < urls.length; i++){
                path.append( File.pathSeparator );
                
                s = new File( urls[i].getFile() );
                //System.out.println(s.getPath());
                path.append( s.getPath() );
            }
            try{
                System.setProperty("java.class.path", path.toString() );
            } catch (Exception e){}
        }
    }
    
    /*-------------------------------------------------------*/
    /*    Thread Context ClassLoader Support                 */
    /*-------------------------------------------------------*/
    class ContextLoader extends BasicURLLoader{
        
        public void addJarsToPath(File dir) throws Exception {
            URLClassLoader loader = (URLClassLoader)ClasspathUtils.getContextClassLoader();
            this.addJarsToPath( dir , loader );
        }
        
        public void addJarToPath(URL jar) throws Exception {
            URLClassLoader loader = (URLClassLoader)ClasspathUtils.getContextClassLoader();
            this.addJarToPath( jar, loader );
        }
    }
    
    /*-------------------------------------------------------*/
    /*    Tomcat ClassLoader Support                         */
    /*-------------------------------------------------------*/
    class TomcatLoader extends BasicURLLoader{
    
        /**
         * The Tomcat Common ClassLoader
         */
        private ClassLoader tomcatLoader;
    
    
        /**
         * The addRepository(String jar) method of the Tomcat Common ClassLoader
         */
        private java.lang.reflect.Method addRepositoryMethod;
        
        public void addJarsToPath(File dir) throws Exception {
            String[] jarNames = dir.list(new java.io.FilenameFilter(){
                public boolean accept(File dir, String name) {
                    //System.out.println("FILE "+name);
                    return (name.endsWith(".jar") ||name.endsWith(".zip"));
                }
            });
        
            for (int j=0; j < jarNames.length; j++){
                this.addJarToPath( new File( dir, jarNames[j]).toURL() );
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
            this.addRepository( path );
          //ClassLoader cl = ClasspathUtils.getContextClassLoader();
          //cl = getCommonLoader(cl);
          //System.out.println("[] "+cl.getClass().getName());
          //System.out.println("[] "+cl);
          //
          ////Reloader loader = (Reloader)cl.getParent();
          //cl = cl.getParent();
          //java.lang.reflect.Method m = getAddRepositoryMethod( cl.getClass());        
          //m.invoke( cl, new Object[]{jar.toExternalForm()});
          ////loader.addRepository( jar.toExternalForm() );
        }
        
        public void addRepository(String path) throws Exception{
            this.getAddRepositoryMethod().invoke(getCommonLoader(), new Object[]{path});        
        }
    
        private void rebuild(){

            try{
            sun.misc.URLClassPath cp = getURLClassPath((URLClassLoader)getCommonLoader());
            URL[] urls = cp.getURLs();
            //for (int i=0; i < urls.length; i++){
            //    System.out.println(urls[i].toExternalForm());
            //}
            if (urls.length < 1) return;
            
            StringBuffer path = new StringBuffer(urls.length*32);
            
            File s = new File( urls[0].getFile() );
            path.append( s.getPath() );
            //System.out.println(s.getPath());
            
            for (int i=1; i < urls.length; i++){
                path.append( File.pathSeparator );
            
                s = new File( urls[i].getFile() );
                //System.out.println(s.getPath());
                path.append( s.getPath() );
            }
            System.setProperty("java.class.path", path.toString() );
            } catch (Exception e){}

        }
        private ClassLoader getCommonLoader(){
            if (tomcatLoader == null) {
                tomcatLoader = this.getCommonLoader(ClasspathUtils.getContextClassLoader()).getParent();
            }
            return tomcatLoader;
        }
    

        private ClassLoader getCommonLoader(ClassLoader loader){
            if (loader.getClass().getName().equals("org.apache.catalina.loader.StandardClassLoader")) {
                return loader;                
            } else {
                return this.getCommonLoader(loader.getParent());
            }
        }
    
        /**
         * This method gets the Tomcat StandardClassLoader.addRepository method via
         * reflection.  This allows us to call the addRepository method for Tomcat
         * integration, but doesn't require us to include or ship any Tomcat 
         * libraries.
         * 
         * @param clazz
         * @return 
         * @exception Exception
         */
        private java.lang.reflect.Method getAddRepositoryMethod() 
        throws Exception{
            if (addRepositoryMethod == null) {
                final Class clazz = getCommonLoader().getClass();
                this.addRepositoryMethod = (java.lang.reflect.Method)AccessController.doPrivileged(
                    new PrivilegedAction(){
                        public Object run() { 
                            java.lang.reflect.Method method = null;
                            try{
                                method = clazz.getDeclaredMethod("addRepository", 
                                                                 new Class[]{String.class});
                                method.setAccessible(true);
                            } catch (Exception e2){
                                e2.printStackTrace();
                            }
                            return method;
                        }
                    }
                );
            }
            
            return addRepositoryMethod;
        }
    }

}


