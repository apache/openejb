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
package org.openejb.server.ejbd;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Collection;
import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.HomeHandle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openejb.DeploymentIndex;
import org.openejb.client.RequestMethods;
import org.openejb.client.ResponseCodes;
import org.openejb.client.ProtocolMetaData;
import org.openejb.proxy.ProxyInfo;
import org.openejb.spi.ApplicationServer;

/**
 *
 * @since 11/25/2001
 */
public class EjbDaemon implements ApplicationServer, ResponseCodes, RequestMethods {
    private static final ProtocolMetaData PROTOCOL_VERSION = new ProtocolMetaData("2.0");
    private static final Log log = LogFactory.getLog(EjbDaemon.class);

    private static EjbDaemon ejbDaemon;

    public static EjbDaemon getEjbDaemon() throws Exception {
        if (ejbDaemon == null) {
            ejbDaemon = new EjbDaemon();
        }
        return ejbDaemon;
    }

    private final ClientObjectFactory clientObjectFactory;
    private final EjbRequestHandler ejbHandler;
    private final JndiRequestHandler jndiHandler;
    private final AuthRequestHandler authHandler;

    private EjbDaemon() throws Exception {
        this(DeploymentIndex.getInstance(), null);
    }

    public EjbDaemon(DeploymentIndex deploymentIndex, Collection orbRefs) throws Exception {
        clientObjectFactory = new ClientObjectFactory(deploymentIndex);

        // Request Handlers
        ejbHandler = new EjbRequestHandler(deploymentIndex, orbRefs);
        jndiHandler = new JndiRequestHandler(deploymentIndex);
        authHandler = new AuthRequestHandler();
    }

    public void service(Socket socket) throws IOException {
        ProtocolMetaData protocolMetaData = new ProtocolMetaData();
        String requestTypeName = null;
        InputStream in = null;
        OutputStream out = null;

        /**
         * The ObjectInputStream used to receive incoming messages from the client.
         */
        ObjectInputStream ois = null;
        /**
         * The ObjectOutputStream used to send outgoing response messages to the client.
         */
        ObjectOutputStream oos = null;

        try {
            in = socket.getInputStream();
            out = socket.getOutputStream();

            //TODO: Implement multiple request processing
            //while ( !stop ) {

            protocolMetaData.readExternal(in);

            PROTOCOL_VERSION.writeExternal(out);

            // Read the request
            byte requestType = (byte) in.read();

            //if (requestType == -1) {continue;}
            if (requestType == -1) {
                return;
            }

            ois = new EJBObjectInputStream(in);
            oos = new ObjectOutputStream(out);

            // Process the request

            // Exceptions should not be thrown from these methods
            // They should handle their own exceptions and clean
            // things up with the client accordingly.
            switch (requestType) {
            case EJB_REQUEST:
                requestTypeName = "EJB_REQUEST";
                ejbHandler.processRequest(ois, oos);
                break;
            case JNDI_REQUEST:
                requestTypeName = "JNDI_REQUEST";
                jndiHandler.processRequest(ois, oos);
                break;
            case AUTH_REQUEST:
                requestTypeName = "AUTH_REQUEST";
                authHandler.processRequest(ois, oos);
                break;
            default:
                requestTypeName = requestType+" (UNKNOWN)";
                log.error(socket.getInetAddress().getHostAddress()+" \""+requestTypeName +" "+ protocolMetaData.getSpec() + "\" FAIL \"Unknown request type "+requestType);
            }

//            log.debug(socket.getInetAddress().getHostAddress()+" \""+requestTypeName +" "+ protocolMetaData.getSpec() + "\" OK");
        } catch (SecurityException e) {
            log.error(socket.getInetAddress().getHostAddress()+" \""+requestTypeName +" "+ protocolMetaData.getSpec() + "\" FAIL \"Security error - "+e.getMessage()+"\"",e);
        } catch (Throwable e) {
            log.error(socket.getInetAddress().getHostAddress()+" \""+requestTypeName +" "+ protocolMetaData.getSpec() + "\" FAIL \"Unexpected error - "+e.getMessage()+"\"",e);
        } finally {
            try {
                if (oos != null) {
                    oos.flush();
                    oos.close();
                } else if (out != null) {
                    out.flush();
                    out.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (Throwable t) {
                log.error(socket.getInetAddress().getHostAddress()+" \""+requestTypeName +" "+ protocolMetaData.getSpec() + "\" FAIL \""+t.getMessage()+"\"");
            }
        }
    }

    //=============================================================
    //  ApplicationServer interface methods
    //=============================================================
    public EJBMetaData getEJBMetaData(ProxyInfo info) {
        return clientObjectFactory.getEJBMetaData(info);
    }

    public Handle getHandle(ProxyInfo info) {
        return clientObjectFactory.getHandle(info);
    }

    public HomeHandle getHomeHandle(ProxyInfo info) {
        return clientObjectFactory.getHomeHandle(info);
    }

    public EJBObject getEJBObject(ProxyInfo info) {
        return clientObjectFactory.getEJBObject(info);
    }

    public EJBHome getEJBHome(ProxyInfo info) {
        return clientObjectFactory.getEJBHome(info);
    }

    //=============================================================
    //  IO helper methods
    //=============================================================
    private static void close(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                log.error("Error closing socket", e);
            }
        }
    }

    private static void close(InputStream in) {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                log.error("Error closing input stream", e);
            }
        }
    }

    private static void close(OutputStream out) {
        if (out != null) {
            try {
                out.flush();
            } catch (IOException e) {
                log.error("Encountered problem while communicating with client", e);
            }
            try {
                out.close();
            } catch (IOException e) {
                log.warn("Error closing output stream", e);
            }
        }
    }
}

