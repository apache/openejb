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
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.Properties;

import org.openejb.util.JarUtils;
import org.openejb.util.FileUtils;
import org.openejb.client.RemoteInitialContextFactory;
import org.openejb.loader.SystemInstance;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public class RemoteTestServer implements org.openejb.test.TestServer {

    static{
        System.setProperty("noBanner", "true");
    }

    /**
     * Has the remote server's instance been already running ?
     */
    private boolean serverHasAlreadyBeenStarted = true;

    private Properties properties;

    public void init(Properties props){
        properties = props;

//        props.put("test.server.class","org.openejb.test.RemoteTestServer");
        props.put("java.naming.factory.initial","org.openejb.client.RemoteInitialContextFactory");
        props.put("java.naming.provider.url","127.0.0.1:4201");
        props.put("java.naming.security.principal","testuser");
        props.put("java.naming.security.credentials","testpassword");
    }

    public Properties getProperties() {
        return properties;
    }

    public void destroy(){
    }

    public void start(){
        if (!connect()) {
            try{
                System.out.println("[] START SERVER");
                FileUtils home = SystemInstance.get().getHome();

                System.out.println("OPENEJB_HOME = "+home.getDirectory().getAbsolutePath());
                String systemInfo = "Java " + System.getProperty("java.version") + "; " + System.getProperty("os.name") + "/" + System.getProperty("os.version");
                System.out.println("SYSTEM_INFO  = "+systemInfo);

                serverHasAlreadyBeenStarted = false;
                String version = null;

                JarUtils.setHandlerSystemProperty();
                Properties versionInfo = new Properties();
                versionInfo.load( new URL( "resource:/openejb-version.properties" ).openConnection().getInputStream() );
                version = (String)versionInfo.get( "version" );

                File lib = home.getDirectory("lib");
                File openejbJar = new File(lib, "openejb-core-" + version + ".jar");

                //DMB: If you don't use an array, you get problems with jar paths containing spaces
                // the command won't parse correctly
                String[] args = {"java", "-jar", openejbJar.getAbsolutePath(), "start"};
                Process server = Runtime.getRuntime().exec(args);

                // Pipe the processes STDOUT to ours
                InputStream out = server.getInputStream();
                Thread serverOut = new Thread(new Pipe(out, System.out));

                serverOut.setDaemon(true);
                serverOut.start();

                // Pipe the processes STDERR to ours
                InputStream err = server.getErrorStream();
                Thread serverErr = new Thread(new Pipe(err, System.err));

                serverErr.setDaemon(true);
                serverErr.start();
            } catch (Exception e){
                throw new RuntimeException("Cannot start the server.");
            }
            connect(10);
        } else {
            //System.out.println("[] SERVER STARTED");
        }
    }

    private void oldStart() throws IOException, FileNotFoundException {
        String s = java.io.File.separator;
        String java = System.getProperty("java.home")+s+"bin"+s+"java";
        String classpath = System.getProperty("java.class.path");
        String openejbHome = System.getProperty("openejb.home");


        String[] cmd = new String[ 5 ];
        cmd[ 0 ] = java;
        cmd[ 1 ] = "-classpath";
        cmd[ 2 ] = classpath;
        cmd[ 3 ] = "-Dopenejb.home="+openejbHome;
        cmd[ 4 ] = "org.openejb.server.Main";
        for (int i=0; i < cmd.length; i++){
            //System.out.println("[] "+cmd[i]);
        }

        Process remoteServerProcess = Runtime.getRuntime().exec( cmd );

        // it seems as if OpenEJB wouldn't start up till the output stream was read
        final java.io.InputStream is = remoteServerProcess.getInputStream();
        final java.io.OutputStream out = new FileOutputStream("logs/testsuite.out");
        Thread serverOut = new Thread(new Runnable(){
                public void run() {
                    try{
                        //while ( is.read() != -1 );
                        int i = is.read();
                        out.write( i );
                        while ( i != -1 ){
                            //System.out.write( i );
                            i = is.read();
                            out.write( i );
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
        });
        serverOut.setDaemon(true);
        serverOut.start();

        final java.io.InputStream is2 = remoteServerProcess.getErrorStream();
        Thread serverErr = new Thread(new Runnable(){
                public void run() {
                    try{
                        //while ( is.read() != -1 );
                        int i = is2.read();
                        out.write( i );
                        while ( i != -1 ){
                            //System.out.write( i );
                            i = is2.read();
                            out.write( i );
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
        });
        serverErr.setDaemon(true);
        serverErr.start();
    }

    public void stop(){
        if ( !serverHasAlreadyBeenStarted ) {
            try{
                System.out.println("[] STOP SERVER");

                Socket socket = new Socket("localhost", 4200);
                OutputStream out = socket.getOutputStream();

                out.write( "Stop".getBytes() );

            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public Properties getContextEnvironment(){
        return (Properties)properties.clone();
    }

    private boolean connect() {
        return connect( 1 );
    }

    private boolean connect(int tries) {
        //System.out.println("CONNECT "+ tries);
        try{
            Socket socket = new Socket("localhost", 4200);
            OutputStream out = socket.getOutputStream();
        } catch (Exception e){
            //System.out.println(e.getMessage());
            if ( tries < 2 ) {
                return false;
            } else {
                try{
                    Thread.sleep(2000);
                } catch (Exception e2){
                    e.printStackTrace();
                }
                return connect(--tries);
            }
        }

        return true;
    }

    private static final class Pipe implements Runnable {
        private final InputStream is;
        private final OutputStream out;

        private Pipe(InputStream is, OutputStream out) {
            super();
            this.is = is;
            this.out = out;
        }

        public void run() {
            try{
                int i = is.read();
                out.write( i );

                while ( i != -1 ){
                    i = is.read();
                    out.write( i );
                }

            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
