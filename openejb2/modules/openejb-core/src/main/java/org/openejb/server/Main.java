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
package org.openejb.server;

import java.util.Properties;
import java.io.InputStream;
import java.net.URL;
import org.openejb.util.JarUtils;

/**
 * This class will parse all the command line arguments then
 * create a properties object and pass that into the server.
 * 
 * If you'd like to start a server from code, you can just
 * call the Server class directly with your own set of properties.
 * 
 */
public class Main {

    public static void main (String args[]) {

        try{
            Properties props = parseArguments(args);

            initServer(props);
        } catch (DontStartServerException e){
            // OK, we won't start the server then
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    private static Properties parseArguments(String args[]) throws DontStartServerException {
        Properties props = new Properties(System.getProperties());

        try {

            // -- Set Defaults -- //
            props.put("openejb.server.ip",         "127.0.0.1");
            props.put("openejb.server.port",       "4201");
            props.put("openejb.server.threads",    "20");

            // Setting the handler system property should be the first thing
            // OpenEJB does.
            JarUtils.setHandlerSystemProperty();

            for (int i=0; i < args.length; i++) {
                if (args[i].equals("-h")) {
                    if (args.length > i+1 ) {
                        System.setProperty("openejb.server.ip", args[++i]);
                    }
                } else if (args[i].equals("-p")) {
                    if (args.length > i+1 ) {
                        System.setProperty("openejb.server.port", args[++i]);
                    }
                } else if (args[i].equals("-t")) {
                    if (args.length > i+1 ) {
                        System.setProperty("openejb.server.threads", args[++i]);
                    }
                } else if (args[i].equals("-conf")) {
                    if (args.length > i+1 ) {
                        System.setProperty("openejb.configuration", args[++i]);
                    }
                } else if (args[i].equals("-l")) {
                    if (args.length > i+1 ) {
                        System.setProperty("log4j.configuration", args[++i]);
                    }
                } else if (args[i].equals("-d")) {
                    if (args.length > i+1 ) {
                        System.setProperty("openejb.home", args[++i]);
                    }
                } else if (args[i].equals("--admin-ip")) {
                    if (args.length > i+1 ) {
                        System.setProperty("openejb.server.admin-ip", args[++i]);
                    }
                } else if (args[i].startsWith("--local-copy")) {
                    if (args[i].endsWith("false") || 
                        args[i].endsWith("FALSE") || 
                        args[i].endsWith("no") || 
                        args[i].endsWith("NO") ) {
                        System.setProperty("openejb.localcopy", "false");
                    } else {
                        System.setProperty("openejb.localcopy", "true");
                    }
                } else if (args[i].equals("-help")) {
                    printHelp();
                    throw new DontStartServerException();
                } else if (args[i].equals("-version")) {
                    printVersion();
                    throw new DontStartServerException();
                } else if (args[i].equals("-examples")) {
                    printExamples();
                    throw new DontStartServerException();
                }
            }

            props.setProperty("org/openejb/configuration_factory", "org.openejb.config.ConfigurationFactory");

        } catch ( Exception re ) {
            System.err.println("FATAL ERROR: "+ re.getMessage());
            System.err.println("");
            System.err.println("Check logs for more details.");
            System.err.println("");
            //re.printStackTrace();
            throw new DontStartServerException();
        }
        
        return props;
    }

    private static void printVersion() {
        /*
         * Output startup message
         */
        Properties versionInfo = new Properties();

        try {
            JarUtils.setHandlerSystemProperty();
            versionInfo.load( new URL( "resource:/openejb-version.properties" ).openConnection().getInputStream() );
        } catch (java.io.IOException e) {
        }
        System.out.println( "OpenEJB Remote Server " + versionInfo.get( "version" ) +"    build: "+versionInfo.get( "date" )+"-"+versionInfo.get( "time" ));
        System.out.println( "" + versionInfo.get( "url" ) );
    }

    private static void printHelp() {
        String header = "OpenEJB Remote Server ";
        try {
            JarUtils.setHandlerSystemProperty();
            Properties versionInfo = new Properties();
            versionInfo.load( new URL( "resource:/openejb-version.properties" ).openConnection().getInputStream() );
            header += versionInfo.get( "version" );
        } catch (java.io.IOException e) {
        }

        System.out.println( header );

        // Internationalize this
        try {
            InputStream in = new URL( "resource:/openejb/ejbserver.txt" ).openConnection().getInputStream();

            int b = in.read();
            while (b != -1) {
                System.out.write( b );
                b = in.read();
            }
        } catch (java.io.IOException e) {
        }
    }

    private static void printExamples() {
        String header = "OpenEJB Remote Server ";
        try {
            JarUtils.setHandlerSystemProperty();
            Properties versionInfo = new Properties();
            versionInfo.load( new URL( "resource:/openejb-version.properties" ).openConnection().getInputStream() );
            header += versionInfo.get( "version" );
        } catch (java.io.IOException e) {
        }

        System.out.println( header );

        // Internationalize this
        try {
            InputStream in = new URL( "resource:/openejb/ejbserver-examples.txt" ).openConnection().getInputStream();

            int b = in.read();
            while (b != -1) {
                System.out.write( b );
                b = in.read();
            }
        } catch (java.io.IOException e) {
        }
    }



    private static void initServer(Properties props) throws Exception{
        Server server = new Server();

        server.init(props);
    }
}

class DontStartServerException extends Exception{
}

