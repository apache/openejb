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
package org.openejb.test;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Properties;

import org.apache.openejb.util.Launcher;

/**
 *
 */
public class RemoteTestServer implements org.openejb.test.TestServer {

    static {
        System.setProperty("noBanner", "true");
    }

    /**
     * Has the remote server's instance been already running ?
     */
    private boolean serverHasAlreadyBeenStarted = true;

    private Properties properties;

    public void init(Properties props) {
        properties = props;

        props.put("test.server.class", "org.openejb.test.RemoteTestServer");
        props.put("java.naming.factory.initial", "org.openejb.client.RemoteInitialContextFactory");
        props.put("java.naming.provider.url", "127.0.0.1:4201");
        props.put("java.naming.security.principal", "testuser");
        props.put("java.naming.security.credentials", "testpassword");


    }

    public void destroy() {
    }

    public void start() {
        if (!connect()) {
            try {
                System.out.println("[] START SERVER");
                serverHasAlreadyBeenStarted = false;
                Launcher.main(new String[]{"-nowait", "org.openejb.server.Main"});
                //oldStart();
            } catch (Exception e) {
                throw new RuntimeException("Cannot start the server.");
            }
            connect(10);
        } else {
            //System.out.println("[] SERVER STARTED");
        }
    }

    private void oldStart() throws IOException, FileNotFoundException {
        String s = java.io.File.separator;
        String java = System.getProperty("java.home") + s + "bin" + s + "java";
        String classpath = System.getProperty("java.class.path");
        String openejbHome = System.getProperty("openejb.home");


        String[] cmd = new String[5];
        cmd[0] = java;
        cmd[1] = "-classpath";
        cmd[2] = classpath;
        cmd[3] = "-Dopenejb.home=" + openejbHome;
        cmd[4] = "org.openejb.server.Main";
        for (int i = 0; i < cmd.length; i++) {
            //System.out.println("[] "+cmd[i]);
        }

        Process remoteServerProcess = Runtime.getRuntime().exec(cmd);

        // it seems as if OpenEJB wouldn't start up till the output stream was read
        final java.io.InputStream is = remoteServerProcess.getInputStream();
        final java.io.OutputStream out = new FileOutputStream("logs/testsuite.out");
        Thread serverOut = new Thread(new Runnable() {
            public void run() {
                try {
                    //while ( is.read() != -1 );
                    int i = is.read();
                    out.write(i);
                    while (i != -1) {
                        //System.out.write( i );
                        i = is.read();
                        out.write(i);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        serverOut.setDaemon(true);
        serverOut.start();

        final java.io.InputStream is2 = remoteServerProcess.getErrorStream();
        Thread serverErr = new Thread(new Runnable() {
            public void run() {
                try {
                    //while ( is.read() != -1 );
                    int i = is2.read();
                    out.write(i);
                    while (i != -1) {
                        //System.out.write( i );
                        i = is2.read();
                        out.write(i);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        serverErr.setDaemon(true);
        serverErr.start();
    }

    public void stop() {
        if (!serverHasAlreadyBeenStarted) {
            try {
                System.out.println("[] STOP SERVER");

                Socket socket = new Socket("localhost", 4201);
                OutputStream out = socket.getOutputStream();

                out.write("Stop".getBytes());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Properties getContextEnvironment() {
        return (Properties) properties.clone();
    }

    private boolean connect() {
        return connect(1);
    }

    private boolean connect(int tries) {
        //System.out.println("CONNECT "+ tries);
        try {
            Socket socket = new Socket("localhost", 4201);
            OutputStream out = socket.getOutputStream();
        } catch (Exception e) {
            //System.out.println(e.getMessage());
            if (tries < 2) {
                return false;
            } else {
                try {
                    Thread.sleep(3000);
                } catch (Exception e2) {
                    e.printStackTrace();
                }
                return connect(--tries);
            }
        }

        return true;
    }

}
