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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.SecurityException;
import org.openejb.OpenEJBException;
import java.net.URL;

/**
 * @author <a href="mailto:adc@toolazydogs.com">Alan Cabrera</a>
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public class JarUtils{
    
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
            try {
                String oldPkgs = System.getProperty( "java.protocol.handler.pkgs" );

                if ( oldPkgs == null )
                    System.setProperty( "java.protocol.handler.pkgs", "org.openejb.util.urlhandler" );
                else if ( oldPkgs.indexOf( "org.openejb.util.urlhandler" ) < 0 )
                    System.setProperty( "java.protocol.handler.pkgs", oldPkgs + "|" + "org.openejb.util.urlhandler" );

            } catch ( SecurityException ex ) {
            }
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
                url = ClassLoader.getSystemResource( resource );
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
}
