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
 * $Id: JarUtils.java 444624 2004-03-01 07:17:26Z dblevins $
 */
package org.apache.openejb.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Hashtable;
import java.util.jar.JarFile;

import org.apache.openejb.OpenEJBException;

/**
 */
public class JarUtils{
    
    private static Messages messages = new Messages( "org.openejb" );
    
    static {
        setHandlerSystemProperty();
    }
    
    private static boolean alreadySet = false;

    public static void setHandlerSystemProperty(){
        if (!alreadySet) {
            /*
             * Setup the java protocol handler path to include org.openejb.util.urlhandler
             * so that org.openejb.util.urlhandler.resource.Handler will be used for URLs
             * of the form "resource:/path".
             */
            /*try {
                String oldPkgs = System.getProperty( "java.protocol.handler.pkgs" );

                if ( oldPkgs == null )
                    System.setProperty( "java.protocol.handler.pkgs", "org.openejb.util.urlhandler" );
                else if ( oldPkgs.indexOf( "org.openejb.util.urlhandler" ) < 0 )
                    System.setProperty( "java.protocol.handler.pkgs", oldPkgs + "|" + "org.openejb.util.urlhandler" );

            } catch ( SecurityException ex ) {
            }*/
            Hashtable urlHandlers = (Hashtable)AccessController.doPrivileged(
                new PrivilegedAction(){
                    public Object run() { 
                        java.lang.reflect.Field handlers = null;
                        try{
                        handlers = URL.class.getDeclaredField("handlers");
                        handlers.setAccessible(true);
                        return handlers.get(null);
                        } catch (Exception e2){
                            e2.printStackTrace();
                        }
                        return null;
                    }
                }
            );
            urlHandlers.put("resource", new org.apache.openejb.util.urlhandler.resource.Handler());
            alreadySet = true;
        }
    }

    public static File getJarContaining(String path) throws OpenEJBException{
        File jarFile = null;        
        try {
            URL url = new URL("resource:/"+path);
        
            /*
             * If we loaded the configuration from a jar, either from a jar:
             * URL or a resource: URL, we must strip off the config file location
             * from the URL.
             */
            String jarPath = null;
            if ( url.getProtocol().compareTo("resource") == 0 ) {
                String resource = url.getFile().substring( 1 );
                //url = ClassLoader.getSystemResource( resource );
                url = getContextClassLoader().getResource( resource );
                if (url == null) {
                    throw new OpenEJBException("Could not locate a jar containing the path "+path);
                }
            }
            
            if ( url != null  ) {
                jarPath = url.getFile();
                jarPath = jarPath.substring( 0, jarPath.indexOf('!') );
                jarPath = jarPath.substring( "file:".length() );
            }

            jarFile = new File(jarPath);
            jarFile = jarFile.getAbsoluteFile();
        } catch (Exception e){
            throw new OpenEJBException("Could not locate a jar containing the path "+path, e);
        }
        return jarFile;
    }	

    public static void addFileToJar(String jarFile, String file ) throws OpenEJBException{
        ByteArrayOutputStream errorBytes = new ByteArrayOutputStream();
    
        /* NOTE: Sadly, we have to play this little game 
         * with temporarily switching the standard error
         * stream to capture the errors.
         * Although you can pass in an error stream in 
         * the constructor of the jar tool, they are not
         * used when an error occurs.
         */
        PrintStream newErr = new PrintStream(errorBytes);
        PrintStream oldErr = System.err;
        System.setErr(newErr);
    
        sun.tools.jar.Main jarTool = new sun.tools.jar.Main(newErr, newErr, "config_utils");
    
        String[] args = new String[]{"uf",jarFile,file};
        jarTool.run(args);
    
        System.setErr(oldErr);
    
        try{
        errorBytes.close();
        newErr.close();
        } catch (Exception e){
            throw new OpenEJBException( messages.format("file.0020",jarFile, e.getLocalizedMessage()));
        }
    
        String error = new String(errorBytes.toByteArray());
        if (error.indexOf("java.io.IOException") != -1) {
            // an IOException was thrown!
            // clean the error message
            int begin = error.indexOf(':')+1;
            int end = error.indexOf('\n');
            String message = error.substring(begin, end);
            throw new OpenEJBException( messages.format("file.0003", file, jarFile, message) );
        }
    
    }

    public static JarFile getJarFile(String jarFile) throws OpenEJBException{
        /*[1.1]  Get the jar ***************/
        JarFile jar = null;
        try {
            File file = new File(jarFile);
            jar = new JarFile(file);
        } catch ( FileNotFoundException e ) {
            throw new OpenEJBException( messages.format("file.0001", jarFile, e.getLocalizedMessage()));
        } catch ( IOException e ) {
            throw new OpenEJBException( messages.format("file.0002", jarFile, e.getLocalizedMessage()));
        }
        return jar;
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

}
