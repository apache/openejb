
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

import junit.framework.*;
import java.io.File;
import java.io.*;
import java.util.*;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class TestServerManager {
    
    private static TestServer defaultServer;
    private static HashMap servers = new HashMap();
    private static String defaultServerName;

    public static synchronized TestServer installServer(String propertiesFileName){
        TestServer server = getServer(propertiesFileName);
        if (server != null) return server;
        try{
            Properties props = getProperties(propertiesFileName);
            Class testServerClass = Class.forName((String)props.get("test.server.class"));
            server = (TestServer)testServerClass.newInstance();
            server.create( props );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (TestServer)servers.put( propertiesFileName, server );
    }
    
    public static synchronized TestServer uninstallServer(String propertiesFileName){
        TestServer server = (TestServer)servers.remove( propertiesFileName );
        if (server != null) {
            server.destroy();
        }
        return server;
    }
            
    protected static Properties getProperties(String fileName) throws Exception{
        File file = new File(fileName);
        file = file.getAbsoluteFile();
        Properties props = (Properties)System.getProperties().clone();
        props.load(new FileInputStream(file));
        return props;
    }
    
    public static void checkDefaultServer(){
        if (defaultServer == null) throw new IllegalStateException("[Proxy Manager] No default test server specified.");
    }

    public static TestServer getServer(String serverName){
        return (TestServer)servers.get(serverName);
    }

    /**
     * Sets the default Server.
     * 
     * The Server must already be registered.
     * 
     * @param ServerName
     */
    public static synchronized TestServer setDefaultServer(String ServerName){
        TestServer newServer = getServer(ServerName);
        if (newServer == null) return defaultServer;

        TestServer oldServer = defaultServer;
        defaultServer = newServer;
        defaultServerName = ServerName;
        
        return oldServer;
    }

    public static TestServer getDefaultServer(){
        return defaultServer;
    }

    public static String getDefaultServerName(){
        return defaultServerName;
    }
    
    public static void startServer(){
        checkDefaultServer();
        defaultServer.start();
    }

    public static void stopServer(){
        checkDefaultServer();
        defaultServer.stop();
    }

    public static Properties getContextEnvironment(){
        checkDefaultServer();
        return defaultServer.getContextEnvironment();
    }
}
