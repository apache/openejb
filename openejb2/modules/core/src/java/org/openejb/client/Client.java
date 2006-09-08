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
package org.openejb.client;

import java.io.*;
import java.rmi.RemoteException;

import org.openejb.server.ejbd.EJBObjectInputStream;

/**
 *
 * @since 11/25/2001
 */
public class Client {

    private static final ProtocolMetaData PROTOCOL_VERSION = new ProtocolMetaData("2.0");

    public static Response request(Request req, Response res, ServerMetaData server) throws RemoteException {
        if ( server == null ) throw new IllegalArgumentException("Server instance cannot be null");

        OutputStream out       = null;
        ObjectOutput objectOut = null;
        ObjectInput  objectIn  = null;
        Connection   conn      = null;

        try {
            /*----------------------------*/
            /* Get a connection to server */
            /*----------------------------*/
            try{
                conn = ConnectionManager.getConnection( server );
            } catch (IOException e){
                throw new RemoteException("Cannot access server: "+server.address+":"+server.port+" Exception: ", e );
            } catch (Throwable e){
                throw new RemoteException("Cannot access server: "+server.address+":"+server.port+" due to an unknown exception in the OpenEJB client: ", e );
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
        return res;
    }

}


