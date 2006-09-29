/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.server;

import java.util.Properties;
import java.io.InputStream;
import java.net.URL;
import org.apache.openejb.util.JarUtils;

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

            props.setProperty("org/openejb/configuration_factory", "org.apache.openejb.config.ConfigurationFactory");

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

