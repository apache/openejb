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
                jarPath = url.getPath();
                jarPath = jarPath.substring( 0, jarPath.indexOf('!') );
                jarPath = jarPath.substring( "file:/".length() );
            }

            jarFile = new File(jarPath);
        } catch (Exception e){
            throw new OpenEJBException("Could not locate a jar containing the path "+path, e);
        }
        return jarFile;
    }	
}
