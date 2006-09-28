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
 * $Id: Stop.java 444631 2004-03-10 09:20:24Z dblevins $
 */
package org.apache.openejb.server.admin;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.Properties;
import org.apache.openejb.util.JarUtils;

/**
 */
public class Stop implements org.apache.openejb.client.RequestMethods {
    
    /**
     * 
     * @param host The ip address the Remote Server is running on
     * @param port The port the Remote Server is running on      
     */
    public static void stop(String host, int port) {
        try{
            
        Socket socket = new Socket(host, port);
        OutputStream out = socket.getOutputStream();
        
        out.write( STOP_REQUEST_Stop );
                
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String [] args) {
        try {

            // The ip address the Remote Server is running on
            String host = "localhost";

            // The port the Remote Server is running on      
            int port = 4200;

            for (int i=0; i < args.length; i++){
                if (args[i].equals("-h")){
                    if (args.length > i+1 ) {
                        host = args[++i];
                    }
                } else if (args[i].equals("-p")){
                    if (args.length > i+1 ) {
                        port = Integer.parseInt( args[++i] );
                    }
                } else if (args[i].equals("-help")){
                    printHelp();
                    return;
                } else if (args[i].equals("-examples")){
                    printExamples();
                    return;
                }
            }

            stop( host, port );           
        } catch ( Exception re ) {
            System.err.println("[EJB Server] FATAL ERROR: "+ re.getMessage());
            re.printStackTrace();
        }
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
            InputStream in = new URL( "resource:/openejb/stop.txt" ).openConnection().getInputStream();

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
            InputStream in = new URL( "resource:/openejb/stop-examples.txt" ).openConnection().getInputStream();

            int b = in.read();
            while (b != -1) {
                System.out.write( b );
                b = in.read();
            }
        } catch (java.io.IOException e) {
        }
    }
}
