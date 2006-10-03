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
package org.apache.openejb.sunorb;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.ORB;
import org.omg.CSIIOP.EstablishTrustInClient;
import org.omg.CSIIOP.Confidentiality;
import org.omg.CSIIOP.EstablishTrustInTarget;
import org.omg.CSIIOP.NoProtection;
import org.omg.CSIIOP.TAG_CSI_SEC_MECH_LIST;
import org.omg.IOP.TaggedComponent;
import org.apache.openejb.corba.ORBConfiguration;
import org.apache.openejb.corba.security.config.ConfigUtil;
import org.apache.openejb.corba.security.config.tss.TSSCompoundSecMechListConfig;
import org.apache.openejb.corba.security.config.css.CSSCompoundSecMechConfig;
import org.apache.openejb.corba.security.config.css.CSSCompoundSecMechListConfig;
import org.apache.openejb.corba.security.config.css.CSSConfig;
import org.apache.openejb.corba.security.config.tss.TSSConfig;
import org.apache.openejb.corba.security.config.ssl.SSLConfig;
import org.apache.openejb.corba.security.config.ssl.SSLCipherSuiteDatabase;
import org.apache.openejb.corba.security.config.tss.TSSSSLTransportConfig;
import org.apache.openejb.corba.security.config.tss.TSSTransportMechConfig;
import org.apache.openejb.corba.util.Util;

import com.sun.corba.se.connection.EndPointInfo;
import com.sun.corba.se.connection.GetEndPointInfoAgainException;
import com.sun.corba.se.connection.ORBSocketFactory;
import com.sun.corba.se.internal.core.IOR;
import com.sun.corba.se.internal.iiop.EndPointImpl;
import com.sun.corba.se.internal.ior.IIOPAddress;
import com.sun.corba.se.internal.ior.IIOPProfileTemplate;


/**
 * @version $Revision$ $Date$
 */
public class OpenEJBSocketFactory implements ORBSocketFactory {

    private final static Log log = LogFactory.getLog(OpenEJBSocketFactory.class);

    public final static String IIOP_SSL = "IIOP_SSL";

    // The initialized SSLSocketFactory obtained from the Geronimo KeystoreManager.
    private SSLSocketFactory socketFactory = null;
    // The initialized SSLServerSocketFactory obtained from the Geronimo KeystoreManager.
    private SSLServerSocketFactory serverSocketFactory = null;
    // the CorabBean or CSSBean instance this ORB is attached to.
    private ORBConfiguration config = null;
    // The initialized SSLConfig we use to retrieve the SSL socket factories.
    private SSLConfig sslConfig = null;
    // The set of cypher suites we use with the SSL connection.
    private String[] cipherSuites;
    // indicates whether client authentication is supported by this transport.
    private boolean clientAuthSupported;
    // indicates whether client authentication is required by this transport.
    private boolean clientAuthRequired;
    // supports and requires values used to retrieve the cipher suites.
    int supports = NoProtection.value;
    int requires = NoProtection.value;

    public OpenEJBSocketFactory() {

        if (log.isDebugEnabled()) log.debug("<init>");
    }

    /**
     * Create a server socket of the indicated type and port.
     *
     * @param type   The string type name for the socket.  This will be either IIOP_CLEAR_TEXT
     *               or IIOP_SSL.
     * @param port   The required port number for the server socket.
     *
     * @return A created and configured socket of the desired type.
     * @exception IOException
     */
    public ServerSocket createServerSocket(String type, int port) throws IOException {

        if (type.equals(ORBSocketFactory.IIOP_CLEAR_TEXT)) {
            if (log.isDebugEnabled()) log.debug("Created plain server socket on port " + port);

            return new ServerSocket(port);
        } else if (type.equals(IIOP_SSL)) {
            SSLServerSocket serverSocket = (SSLServerSocket)getServerSocketFactory().createServerSocket(port);

            serverSocket.setEnabledCipherSuites(cipherSuites);
            serverSocket.setWantClientAuth(clientAuthSupported);
            serverSocket.setNeedClientAuth(clientAuthRequired);
            serverSocket.setSoTimeout(60 * 1000);

            if (log.isDebugEnabled()) {
                log.debug("Created SSL server socket on port " + port);
                log.debug("    client authentication " + (clientAuthSupported ? "SUPPORTED" : "UNSUPPORTED"));
                log.debug("    client authentication " + (clientAuthRequired ? "REQUIRED" : "OPTIONAL"));
                log.debug("    cipher suites:");

                for (int i = 0; i < cipherSuites.length; i++) {
                    log.debug("    " + cipherSuites[i]);
                }
            }

            return serverSocket;
        }
        throw new COMM_FAILURE("SocketFactory cannot handle: " + type, MinorCodes.UNSUPPORTED_ENDPOINT_TYPE, CompletionStatus.COMPLETED_NO);
    }

    /**
     * Create a client socket to connect to a given end point.
     *
     * @param endPointInfo
     *               The EndPointInfo object that defines the characteristics of
     *               the target (type of socket, host, and port).
     *
     * @return A created and configured socket of the desired type.
     * @exception IOException
     * @exception GetEndPointInfoAgainException
     */
    public Socket createSocket(EndPointInfo endPointInfo) throws IOException, GetEndPointInfoAgainException {

        String type = endPointInfo.getType();

        if (type.equals(ORBSocketFactory.IIOP_CLEAR_TEXT)) {
            if (log.isDebugEnabled()) log.debug("Created plain socket to " + endPointInfo.getHost() + ":" + endPointInfo.getPort());

            return new Socket(endPointInfo.getHost(), endPointInfo.getPort());
        } else if (type.equals(IIOP_SSL)) {
            SSLSocket socket = null;
            SSLSocketFactory factory = getSocketFactory();
            try {
                socket = (SSLSocket) factory.createSocket(endPointInfo.getHost(), endPointInfo.getPort());
            } catch (IOException e) {
                log.debug("could not create socket:", e);
                throw e;
            }

            socket.setSoTimeout(60 * 1000);

            OpenEJBEndPointImpl e = (OpenEJBEndPointImpl)endPointInfo;

            String[] iorSuites = SSLCipherSuiteDatabase.getCipherSuites(e.getRequires(), e.getSupports(), serverSocketFactory.getSupportedCipherSuites());
            socket.setEnabledCipherSuites(iorSuites);
            socket.setWantClientAuth(e.clientAuthSupported());
            socket.setNeedClientAuth(e.clientAuthRequired());

            if (log.isDebugEnabled()) {
                log.debug("Created SSL socket to " + endPointInfo.getHost() + ":" + endPointInfo.getPort());
                log.debug("    client authentication " + (e.clientAuthSupported() ? "SUPPORTED" : "UNSUPPORTED"));
                log.debug("    client authentication " + (e.clientAuthRequired() ? "REQUIRED" : "OPTIONAL"));
                log.debug("    cipher suites:");

                for (int i = 0; i < iorSuites.length; i++) {
                    log.debug("    " + iorSuites[i]);
                }
            }
            return socket;
        }
        throw new COMM_FAILURE("SocketFactory cannot handle: " + type, MinorCodes.UNSUPPORTED_ENDPOINT_TYPE, CompletionStatus.COMPLETED_NO);
    }

    /**
     * Determine the end point information for a particular socket
     * connection.  This examines the IOR profile information to
     * see if the IOR profile is configured for TSS security mechanisms.
     *
     * @param orb    The orb this request is running on.
     * @param ior    The ior of the target connection.
     * @param endPointInfo
     *               Existing endpoint information for this connection.
     *
     * @return An endpoint Info object configured with the appropriate socket
     *         type and target information.
     */
    public EndPointInfo getEndPointInfo(ORB orb, IOR ior, EndPointInfo endPointInfo) {

        IIOPProfileTemplate temp = ior.getProfile().getTemplate();
        IIOPAddress primary = temp.getPrimaryAddress();

        TaggedComponent[] iopComponents = ior.getProfile().getIOPComponents(orb, TAG_CSI_SEC_MECH_LIST.value);
        for (int i = 0; i < iopComponents.length; i++) {
            try {
                TSSCompoundSecMechListConfig config = TSSCompoundSecMechListConfig.decodeIOR(Util.getCodec(), iopComponents[i]);
                for (int j = 0; j < config.size(); j++) {
                    TSSTransportMechConfig transport_mech = config.mechAt(j).getTransport_mech();
                    if (transport_mech instanceof TSSSSLTransportConfig) {
                        TSSSSLTransportConfig transportConfig = (TSSSSLTransportConfig) transport_mech;
                        int supports = transportConfig.getSupports();
                        int requires = transportConfig.getRequires();

                        if (log.isDebugEnabled()) {

                            log.debug("IOR from target " + transportConfig.getHostname().toLowerCase() + ":" + transportConfig.getPort());
                            log.debug("   SUPPORTS: " + ConfigUtil.flags(supports));
                            log.debug("   REQUIRES: " + ConfigUtil.flags(requires));
                        }

                        // if ne protection is the order of the day, break out and create a plain text version.
                        if ((NoProtection.value & transportConfig.getRequires()) == NoProtection.value) {
                            break;
                        }
                        return new OpenEJBEndPointImpl(IIOP_SSL,
                                                transportConfig.getPort(),
                                                transportConfig.getHostname().toLowerCase(), supports, requires);

                    }
                }
            } catch (Exception e) {
                // do nothing
            }
        }

        if (log.isDebugEnabled()) log.debug("Created plain endpoint to " + primary.getHost() + ":" + primary.getPort());

        return new EndPointImpl(ORBSocketFactory.IIOP_CLEAR_TEXT,
                                primary.getPort(),
                                primary.getHost().toLowerCase());
    }

    /**
     * On-demand creation of an SSL socket factory, using the provided
     * Geronimo SSLConfig information.
     *
     * @return The SSLSocketFactory this connection should be using to create
     *         secure connections.
     */
    private SSLSocketFactory getSocketFactory() throws IOException {
        // first use?
        if (socketFactory == null) {
            if (sslConfig == null) {
                socketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            }
            else {
                try {
                    socketFactory = (SSLSocketFactory)sslConfig.createSSLFactory(Thread.currentThread().getContextClassLoader());
                } catch (Exception e) {
                    throw new IOException("Unable to create client SSL socket factory: " + e.getMessage());
                }
            }
            if (cipherSuites == null) {
                cipherSuites = SSLCipherSuiteDatabase.getCipherSuites(requires, supports, socketFactory.getSupportedCipherSuites());
            }
        }
        return socketFactory;
    }

    /**
     * On-demand creation of an SSL server socket factory, using the provided
     * Geronimo SSLConfig information.
     *
     * @return The SSLServerSocketFactory this connection should be using to create
     *         secure connections.
     */
    private SSLServerSocketFactory getServerSocketFactory() throws IOException {
        // first use?
        if (serverSocketFactory == null) {
            if (sslConfig == null) {
                serverSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            }
            else {
                try {
                    serverSocketFactory = (SSLServerSocketFactory)sslConfig.createSSLServerFactory(Thread.currentThread().getContextClassLoader());
                } catch (Exception e) {
                    throw new IOException("Unable to create server SSL socket factory: " + e.getMessage());
                }
            }
            if (cipherSuites == null) {
                cipherSuites = SSLCipherSuiteDatabase.getCipherSuites(requires, supports, serverSocketFactory.getSupportedCipherSuites());
            }
        }
        return serverSocketFactory;
    }


    /**
     * Set the CSSConfig item that should be used by this connection.
     *
     * @param config The Bean configured CSSConfig.
     */
    public void setConfig(ORBConfiguration config) {
        this.config = config;

        clientAuthSupported = false;
        clientAuthRequired = false;

        // retrieve the SSL factory manager
        sslConfig = config.getSslConfig();

        TSSConfig tssConfig = config.getTssConfig();
        TSSTransportMechConfig transportMech = tssConfig.getTransport_mech();
        if (transportMech != null) {
            if (transportMech instanceof TSSSSLTransportConfig) {
                TSSSSLTransportConfig transportConfig = (TSSSSLTransportConfig) transportMech;
                supports = transportConfig.getSupports();
                requires = transportConfig.getRequires();
            }
        }

        // now set the server parameters based on the profile
        if ((supports & EstablishTrustInClient.value) != 0) {
            clientAuthSupported = true;

            if ((requires & EstablishTrustInClient.value) != 0) {
                clientAuthRequired = true;
            }
        }

        if ((supports & EstablishTrustInTarget.value) != 0) {
            clientAuthSupported = true;

            if ((requires & EstablishTrustInTarget.value) != 0) {
                clientAuthRequired = true;
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("   SUPPORTS: " + ConfigUtil.flags(supports));
            log.debug("   REQUIRES: " + ConfigUtil.flags(requires));
        }
    }
}
