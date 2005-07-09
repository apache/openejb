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
 *    please contact dev@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://www.openejb.org/).
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
 * Copyright 2002 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.loader;

import java.util.Hashtable;
import java.io.File;

import org.openejb.util.ClasspathUtils;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public class EmbeddingLoader implements Loader {
    
    static boolean loaded = false;
    
    /**
     * Checks to see if OpenEJB is available through classpath.  
     * If it isn't, then the required libraries are
     * added and OpenEJB is pulled in and instantiated.
     * 
     * @param env
     * @exception Exception
     */
    public void load( Hashtable env ) throws Exception{
        if (loaded) return;

        ClassLoader cl = getContextClassLoader();
        try{
            cl.loadClass("org.openejb.OpenEJB");
        } catch (Exception e){
            importOpenEJBLibraries( env );
        } 
        try{
            Loader embedded = null;
            
            Class loaderClass = cl.loadClass( "org.openejb.loader.EmbeddedLoader" );
            
            embedded = (Loader)loaderClass.newInstance();
            embedded.load( env );
            
        } catch (Exception e){
            throw new Exception( "Cannot embed OpenEJB. Exception: "+
                                 e.getClass().getName()+" "+ e.getMessage());
        }
        loaded = true;
    }

    /**
     * Loads all the libraries in the lib and dist directories depending on the
     * environment it pulls it from openejb.home or openejb.base directory
     * 
     * @param env
     * @throws Exception
     */
    private void importOpenEJBLibraries(  Hashtable env ) throws Exception{
        try{
            String openejbLoader = (String) env.get("openejb.loader");

            File directory = SystemInstance.get().getHome().getDirectory("lib");
            SystemInstance.get().getLoader().addJarsToPath(directory);
            File directory1 = SystemInstance.get().getHome().getDirectory("dist");
            SystemInstance.get().getLoader().addJarsToPath(directory1);
        } catch (Exception e){
            throw new Exception( "Could not load OpenEJB libraries. Exception: "+
                                 e.getClass().getName()+" "+ e.getMessage());
        }
    }

    private ClassLoader getContextClassLoader() {
        return (ClassLoader) java.security.AccessController.doPrivileged(new java.security.PrivilegedAction() {
            public Object run() {
                return Thread.currentThread().getContextClassLoader();
            }
        });
    }
}
