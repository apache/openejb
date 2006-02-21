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
package org.openejb.server.ejbd;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.Properties;

import org.openejb.DeploymentInfo;
import org.openejb.ProxyInfo;
import org.openejb.client.EJBRequest;
import org.openejb.client.RequestMethods;
import org.openejb.client.ResponseCodes;
import org.openejb.util.Logger;
import org.openejb.util.Messages;

/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @since 11/25/2001
 */
public class EjbDaemon implements org.openejb.spi.ApplicationServer, ResponseCodes, RequestMethods {

    Messages _messages = new Messages( "org.openejb.server.util.resources" );
    Logger logger = Logger.getInstance( "OpenEJB.server.remote", "org.openejb.server.util.resources" );

    Properties props;

    ClientObjectFactory clientObjectFactory;
    DeploymentIndex deploymentIndex;
    EjbRequestHandler ejbHandler;
    JndiRequestHandler jndiHandler;
    AuthRequestHandler authHandler;

    boolean stop = false;

    static EjbDaemon thiss;

    private EjbDaemon() {
    }

    public static EjbDaemon getEjbDaemon() {
        if ( thiss == null ) {
            thiss = new EjbDaemon();
        }
        return thiss;
    }

    public void init(Properties props) throws Exception{
        this.props = props;
        
        deploymentIndex = new DeploymentIndex();

        clientObjectFactory = new ClientObjectFactory(this);
        
        // Request Handlers
        ejbHandler  = new EjbRequestHandler(this);
        jndiHandler = new JndiRequestHandler(this);
        authHandler = new AuthRequestHandler(this);
    }

    public void service(Socket socket) throws IOException{
        InputStream  in  = socket.getInputStream();
        OutputStream out = socket.getOutputStream();

        try {
            service(in, out);
        } finally {
            try {
                if (socket != null) socket.close();
            } catch (Throwable t) {
                logger.error("Encountered problem while closing connection with client: " + t.getMessage());
            }
        }
    }

    public void service(InputStream in, OutputStream out) throws IOException {
        ObjectInputStream ois = null;
        /**
         * The ObjectOutputStream used to send outgoing response messages to the client.
         */
        ObjectOutputStream oos = null;


        try {

            //TODO: Implement multiple request processing
            //while ( !stop ) {

            // Read the request
            byte requestType = (byte)in.read();

            //if (requestType == -1) {continue;}
            if (requestType == -1) {
                return;
            }

            ois = new ObjectInputStream( in );
            oos = new ObjectOutputStream( out );

            // Process the request
            switch (requestType) {
            case EJB_REQUEST:  processEjbRequest(ois, oos); break;
            case JNDI_REQUEST: processJndiRequest(ois, oos);break;
            case AUTH_REQUEST: processAuthRequest(ois, oos);break;
            default: logger.error("Unknown request type "+requestType);
            }
            try {
                if ( oos != null ) {
                    oos.flush();
                }
            } catch ( Throwable t ) {
                logger.error("Encountered problem while communicating with client: "+t.getMessage());
            }
            //}

            // Exceptions should not be thrown from these methods
            // They should handle their own exceptions and clean
            // things up with the client accordingly.
        } catch ( SecurityException e ) {
            logger.error( "Security error: "+ e.getMessage() );
        } catch ( Throwable e ) {
            logger.error( "Unexpected error", e );
            //System.out.println("ERROR: "+clienntIP.getHostAddress()+": " +e.getMessage());
        } finally {
            try {
                if ( oos != null ) {
                    oos.flush();
                }
            } catch ( Throwable t ) {
                logger.error("Encountered problem while flushing connection with client: " + t.getMessage());
            }
        }
    }

    protected DeploymentInfo getDeployment(EJBRequest req) throws RemoteException {
        return deploymentIndex.getDeployment(req);
    }

    public void processEjbRequest (ObjectInputStream in, ObjectOutputStream out) {
        ejbHandler.processRequest(in,out);
    }

    public void processJndiRequest(ObjectInputStream in, ObjectOutputStream out) throws Exception{
        jndiHandler.processRequest(in,out);
    }

    public void processAuthRequest(ObjectInputStream in, ObjectOutputStream out) {
        authHandler.processRequest(in,out);
    }

    //=============================================================
    //  ApplicationServer interface methods
    //=============================================================
    public javax.ejb.EJBMetaData getEJBMetaData(ProxyInfo info) {
        return clientObjectFactory.getEJBMetaData(info);
    }

    public javax.ejb.Handle getHandle(ProxyInfo info) {
        return clientObjectFactory.getHandle(info);
    }

    public javax.ejb.HomeHandle getHomeHandle(ProxyInfo info) {
        return clientObjectFactory.getHomeHandle(info);
    }

    public javax.ejb.EJBObject getEJBObject(ProxyInfo info) {
        return clientObjectFactory.getEJBObject(info);
    }

    public javax.ejb.EJBHome getEJBHome(ProxyInfo info) {
        return clientObjectFactory.getEJBHome(info);
    }

}

