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
package org.apache.openejb.server.admin;

import java.io.*;
import java.net.*;
import java.util.*;
import org.activeio.xnet.ServerService;
import org.activeio.xnet.ServiceException;
import org.apache.openejb.server.ServiceManager;
import org.apache.openejb.client.RequestMethods;
import org.apache.openejb.DeploymentIndex;

/**
 * This is the base class for orcistrating the other daemons 
 * which actually accept and react to calls coming in from 
 * different protocols or channels.
 * 
 * To perform this task, this class will
 *    newInstance()
 *    init( port, properties)
 *    start()
 *    stop()
 * 
 * 
 */
public class AdminDaemon implements ServerService {

    public void init(Properties props) throws Exception {
    }

    public AdminDaemon(DeploymentIndex index) {
    }

    public void service(Socket socket) throws ServiceException,IOException {
        InputStream in = null;
        InetAddress clientIP = null;
        
        try {
            in = socket.getInputStream();
            clientIP = socket.getInetAddress();


            byte requestType = (byte)in.read();
            
            if (requestType == -1) {return;}
            
            switch (requestType) {
                case RequestMethods.STOP_REQUEST_Quit:
                case RequestMethods.STOP_REQUEST_quit:
                case RequestMethods.STOP_REQUEST_Stop:
                case RequestMethods.STOP_REQUEST_stop:
                    ServiceManager.getManager().stop();
                    //stop(clientIP, serverSocket.getInetAddress());
                    
            }

            // Exceptions should not be thrown from these methods
            // They should handle their own exceptions and clean
            // things up with the client accordingly.
        } catch ( SecurityException e ) {
            //logger.error( "Security error: "+ e.getMessage() );
        } catch ( Throwable e ) {
            //logger.error( "Unexpected error", e );
            //System.out.println("ERROR: "+clienntIP.getHostAddress()+": " +e.getMessage());
        } finally {
            try {
                if ( in     != null ) in.close();
                if ( socket != null ) socket.close();
            } catch ( Throwable t ){
                //logger.error("Encountered problem while closing connection with client: "+t.getMessage());
            }
        }
    }
    
    public void start() throws ServiceException {
    }

    public void stop() throws ServiceException {
    }

    /**
     * Gets the port number that the
     * daemon is listening on.
     */
    public int getPort() {
        return 0;
    }
    
    public String getIP() {
        return "";
    }
    
    public String getName() {
        return "admin thread";
    }

}
