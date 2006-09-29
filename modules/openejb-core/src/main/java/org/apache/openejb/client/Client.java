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
package org.apache.openejb.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.openejb.server.ejbd.EJBObjectInputStream;

/**
 *
 * @since 11/25/2001
 */
public class Client {
    private static final Log log = LogFactory.getLog(Client.class);
    
    private static final ProtocolMetaData PROTOCOL_VERSION = new ProtocolMetaData("2.0");

    public static void request(RequestInfo reqInfo, ResponseInfo resInfo) throws RemoteException {
        Request req = reqInfo.getRequest();
        ServerMetaData[] servers = reqInfo.getServers();
        if ( servers == null || servers.length == 0) throw new IllegalArgumentException("Server instance cannot be null");

        OutputStream out       = null;
        ObjectOutput objectOut = null;
        ObjectInput  objectIn  = null;
        Connection   conn      = null;

        try {
            /*----------------------------*/
            /* Get a connection to server */
            /*----------------------------*/
            for (int i = 0; i < servers.length && null == conn; i++) {
                ServerMetaData server = servers[i];
                try{
                    conn = ConnectionManager.getConnection( server );
                } catch (IOException e){
                    log.error("Cannot access server: "+server.address+":"+server.port+" Exception: ", e);
                } catch (Throwable e){
                    log.error("Cannot access server: "+server.address+":"+server.port+" due to an unknown exception in the OpenEJB client: ", e );
                }
            }
            if (null == conn) {
                StringBuffer buffer = new StringBuffer();
                for (int i = 0; i < servers.length; i++) {
                    ServerMetaData server = servers[i];
                    buffer.append("Server #" + i + ": " + server);
                }
                throw new RemoteException("Cannot access servers: " + buffer);
            }

            /*----------------------------------*/
            /* Get output streams               */
            /*----------------------------------*/
            try{

                out = conn.getOuputStream();

            } catch (Throwable e){
                throw new RemoteException("Cannot open output stream to server: " , e );
            }

            /*----------------------------------*/
            /* Write the protocol magic         */
            /*----------------------------------*/
            try{

                PROTOCOL_VERSION.writeExternal(out);

            } catch (Throwable e){
                throw new RemoteException("Cannot write the protocol metadata to the server: " , e );
            }

            /*----------------------------------*/
            /* Write request type               */
            /*----------------------------------*/
            try{
                out.write( req.getRequestType() );
            } catch (IOException e){
                throw new RemoteException("Cannot write the request type to the server: " , e );

            } catch (Throwable e){
                throw new RemoteException("Cannot write the request type to the server: " , e );
            }

            /*----------------------------------*/
            /* Get output streams               */
            /*----------------------------------*/
            try{

                objectOut = new ObjectOutputStream( out );

            } catch (IOException e){
                throw new RemoteException("Cannot open object output stream to server: " , e );

            } catch (Throwable e){
                throw new RemoteException("Cannot open object output stream to server: " , e );
            }


            /*----------------------------------*/
            /* Write request                    */
            /*----------------------------------*/
            try{

                // Write the request data.
                req.writeExternal( objectOut );
                objectOut.flush();

            } catch (java.io.NotSerializableException e){
                //TODO:3: This doesn't seem to work in the OpenEJB test suite
                // run some other test to see if the exception reaches the client.
                throw new IllegalArgumentException("Object is not serializable: "+ e.getMessage());

            } catch (IOException e){
                throw new RemoteException("Cannot write the request to the server: " , e );

            } catch (Throwable e){
                throw new RemoteException("Cannot write the request to the server: " , e );
            }

            /*----------------------------------*/
            /* Get input streams               */
            /*----------------------------------*/
            InputStream in = null;
            try {

                in = conn.getInputStream();

            } catch (IOException e) {
                throw new RemoteException("Cannot open input stream to server: " , e );
            }

            ProtocolMetaData protocolMetaData = null;
            try {

                protocolMetaData = new ProtocolMetaData();
                protocolMetaData.readExternal(in);

            } catch (IOException e) {
                throw new RemoteException("Cannot deternmine server protocol version: Received "+protocolMetaData.getSpec() , e );
            }

            try{

                objectIn = new EJBObjectInputStream(in);

            } catch (Throwable e){
                throw new RemoteException("Cannot open object input stream to server ("+protocolMetaData.getSpec() +") : "+e.getMessage() , e );
            }

            Response res = resInfo.getResponse();
            
            /*----------------------------------*/
            /* Read response                    */
            /*----------------------------------*/
            try{
                // Read the response from the server
                res.readExternal( objectIn );
            } catch (ClassNotFoundException e){
                throw new RemoteException("Cannot read the response from the server.  The class for an object being returned is not located in this system:" , e );

            } catch (IOException e){
                throw new RemoteException("Cannot read the response from the server ("+protocolMetaData.getSpec() +") : "+e.getMessage() , e );

            } catch (Throwable e){
                throw new RemoteException("Error reading response from server ("+protocolMetaData.getSpec() +") : "+e.getMessage() , e );
            }
            
            ServerMetaData[] newServers = servers;
            if (res instanceof ClusteredResponse) {
                ClusteredResponse clusteredResponse = (ClusteredResponse) res;
                ServerMetaData[] tmpNewServers = clusteredResponse.getServers();
                if (null != tmpNewServers && 0 != tmpNewServers.length) {
                    newServers = tmpNewServers;
                }
            }
            resInfo.setServers(newServers);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Throwable t){
                //TODO:2: Log this
                System.out.println("Error closing connection with server: "+t.getMessage() );
            }
        }
    }

}
