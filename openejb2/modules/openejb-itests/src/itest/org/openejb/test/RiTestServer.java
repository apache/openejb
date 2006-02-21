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
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.test;

import java.io.DataInputStream;
import java.io.File;
import java.net.URL;
import java.util.Properties;
import javax.naming.Context;

/**
 * The Client test suite needs the following environment variables
 * to be set before it can be run.
 * <p/>
 * <code>test.home</code>
 * <code>server.classpath</code>
 */
public class RiTestServer implements TestServer {

    protected Process server;
    protected boolean startServerProcess;
    protected String configFile;
    protected String serverClassName = " org.openejb.ri.server.Server ";
    protected String classPath;
    protected DataInputStream in;
    protected DataInputStream err;
    protected String testHomePath;
    protected File testHome;

    /**
     * The environment variable <code>test.home</code> sould be set
     * to the base directory where the test suite is located.
     */
    public static final String TEST_HOME = "test.home";
    public static final String SERVER_CLASSPATH = "server.classpath";
    public static final String SERVER_CONFIG = "test.server.config";
    public static final String START_SERVER_PROCESS = "test.start.server.process";
    public static final String BAD_ENVIRONMENT_ERROR = "The following environment variables must be set before running the test suite:\n";


    static {
        System.setProperty("noBanner", "true");
    }

    public RiTestServer() {
    }

    public void init(Properties props) {
        try {
            /* [DMB] Temporary fix  */
            try {
                System.setSecurityManager(new TestSecurityManager());
            } catch (Exception e) {
                e.printStackTrace();
            }
            /* [DMB] Temporary fix  */
            
            String tmp = props.getProperty(START_SERVER_PROCESS, "true").trim();
            startServerProcess = "true".equalsIgnoreCase(tmp);
                        
            /* If we will not be starting process for the 
             * server than we don't need to read in the other
             * properties 
             */
            if (!startServerProcess) return;

            testHomePath = props.getProperty(TEST_HOME);
            classPath = props.getProperty(SERVER_CLASSPATH);
            configFile = props.getProperty(SERVER_CONFIG);

            checkEnvironment();

            testHome = new File(testHomePath);
            testHome = testHome.getAbsoluteFile();
            testHomePath = testHome.getAbsolutePath();

            prepareServerClasspath();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void destroy() {

    }

    /**
     * Starts and Ri Server with the configuration file from
     * the properties used to create this RiTestServer.
     */
    public void start() {

        if (!startServerProcess) return;

        String command = "java -classpath " + classPath + " " + serverClassName + " " + configFile;
        try {
            server = Runtime.getRuntime().exec(command);
            in = new DataInputStream(server.getInputStream());
            err = new DataInputStream(server.getErrorStream());
            while (true) {
                try {
                    String line = in.readLine();
                    System.out.println(line);
                    if (line == null || "[RI Server] Ready!".equals(line)) break;

                } catch (Exception e) {
                    break;
                }
            }

            Thread t = new Thread(new Runnable() {
                public void run() {
                    while (true) {
                        try {
                            String line = in.readLine();
                            if (line == null) break;
                            System.out.println(line);
                        } catch (Exception e) {
                            break;
                        }
                    }

                }
            });
            t.start();
            Thread t2 = new Thread(new Runnable() {
                public void run() {
                    while (true) {
                        try {
                            String line = err.readLine();
                            if (line == null) break;
//                                System.out.println(line);
                        } catch (Exception e) {
                            break;
                        }
                    }

                }
            });
            t2.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if (!startServerProcess) return;

        if (server != null) server.destroy();
        server = null;
        try {
            in.close();
            err.close();
        } catch (Exception e) {
        }
    }

    public Properties getContextEnvironment() {
        Properties properties = new Properties();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, "org.openejb.ri.server.RiInitCtxFactory");

        try {
            properties.put(Context.PROVIDER_URL, new URL("http", "127.0.0.1", 1098, ""));
        } catch (Exception e) {
        }

        properties.put(Context.SECURITY_PRINCIPAL, "STATEFUL_TEST_CLIENT");
        properties.put(Context.SECURITY_CREDENTIALS, "STATEFUL_TEST_CLIENT");

        return properties;
    }

    //==========================================
    //  Methods supporting this implementation
    //  of the TestServer interface
    // 
    private String getConfFilePath(String confFileName) {
        String str = getConfFile(confFileName).getAbsolutePath();
        return str;
    }

    private File getConfFile(String confFileName) {
        return new File(testHome, confFileName);
    }

    private void checkEnvironment() {

        if (testHomePath == null || classPath == null || configFile == null) {
            String error = BAD_ENVIRONMENT_ERROR;
            error += (testHomePath == null) ? TEST_HOME + "\n" : "";
            error += (classPath == null) ? SERVER_CLASSPATH + "\n" : "";
            error += (configFile == null) ? SERVER_CONFIG + "\n" : "";
            throw new RuntimeException(error);
        }
    }

    private void prepareServerClasspath() {
        char PS = File.pathSeparatorChar;
        char FS = File.separatorChar;

        String javaTools = System.getProperty("java.home") + FS + "lib" + FS + "tools.jar";
        classPath = classPath.replace('/', FS);
        classPath = classPath.replace(':', PS);
        classPath += PS + javaTools;
    }
    // 
    //  Methods supporting this implementation
    //  of the TestServer interface
    //==========================================

}
