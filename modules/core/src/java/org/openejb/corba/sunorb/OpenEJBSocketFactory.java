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
 * Copyright 2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.corba.sunorb;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import com.sun.corba.se.connection.EndPointInfo;
import com.sun.corba.se.connection.GetEndPointInfoAgainException;
import com.sun.corba.se.connection.ORBSocketFactory;
import com.sun.corba.se.internal.core.IOR;
import com.sun.corba.se.internal.iiop.EndPointImpl;
import com.sun.corba.se.internal.ior.IIOPAddress;
import com.sun.corba.se.internal.ior.IIOPProfileTemplate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.ORB;
import org.omg.CSIIOP.Confidentiality;
import org.omg.CSIIOP.EstablishTrustInTarget;
import org.omg.CSIIOP.NoProtection;
import org.omg.CSIIOP.TAG_CSI_SEC_MECH_LIST;
import org.omg.IOP.TaggedComponent;

import org.openejb.corba.MinorCodes;
import org.openejb.corba.security.config.ConfigUtil;
import org.openejb.corba.security.config.tss.TSSCompoundSecMechListConfig;
import org.openejb.corba.security.config.tss.TSSSSLTransportConfig;
import org.openejb.corba.security.config.tss.TSSTransportMechConfig;
import org.openejb.corba.util.Util;


/**
 * @version $Revision$ $Date$
 */
public class OpenEJBSocketFactory implements ORBSocketFactory {

    private final static Log log = LogFactory.getLog(OpenEJBSocketFactory.class);

    public final static String IIOP_SSL = "IIOP_SSL";
    public final static String SOCKET_SUPPORTS = "org.openejb.corba.ssl.SocketProperties.supports";
    public final static String SOCKET_REQUIRES = "org.openejb.corba.ssl.SocketProperties.requires";

    private final SSLSocketFactory socketFactory;
    private final SSLServerSocketFactory serverSocketFactory;
    private final String[] cipherSuites;
    private final boolean clientAuthSupported;
    private final boolean clientAuthRequired;

    public OpenEJBSocketFactory() {

        if (log.isDebugEnabled()) log.debug("<init>");

        socketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        serverSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();

        String[] props = getProperty(SOCKET_SUPPORTS, "").split(",");
        int supports = 0;
        boolean caSupported = false;
        for (int i = 0; i < props.length; i++) {
            if ("EstablishTrustInTarget".equals(props[i])) {
                supports |= EstablishTrustInTarget.value;
                caSupported = true;
            } else if ("EstablishTrustInClient".equals(props[i])) {
                supports |= EstablishTrustInTarget.value;
                caSupported = true;
            } else if ("Confidentiality".equals(props[i])) {
                supports |= Confidentiality.value;
            } else if ("Integrity".equals(props[i])) {
            } else if ("NoProtection".equals(props[i])) {
                supports |= NoProtection.value;
            } else if (props[i].trim().length() == 0) {
                supports |= NoProtection.value;
            } else {
                log.error("Unsupported socket property: " + props[i]);
            }
        }

        props = getProperty(SOCKET_REQUIRES, "").split(",");
        int requires = 0;
        boolean caRequired = false;
        for (int i = 0; i < props.length; i++) {
            if ("EstablishTrustInTarget".equals(props[i])) {
                requires |= EstablishTrustInTarget.value;
                caRequired = true;
            } else if ("EstablishTrustInClient".equals(props[i])) {
                requires |= EstablishTrustInTarget.value;
                caRequired = true;
            } else if ("Confidentiality".equals(props[i])) {
                requires |= Confidentiality.value;
            } else if ("Integrity".equals(props[i])) {
            } else if ("NoProtection".equals(props[i])) {
                requires |= NoProtection.value;
            } else {
                log.error("Unsupported socket property: " + props[i]);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("   SUPPORTS: " + ConfigUtil.flags(supports));
            log.debug("   REQUIRES: " + ConfigUtil.flags(requires));
        }

        clientAuthSupported = caSupported;
        clientAuthRequired = caRequired;
        cipherSuites = SSLCipherSuiteDatabase.getCipherSuites(requires, supports, socketFactory.getSupportedCipherSuites());
    }

    public ServerSocket createServerSocket(String type, int port) throws IOException {

        if (type.equals(ORBSocketFactory.IIOP_CLEAR_TEXT)) {
            if (log.isDebugEnabled()) log.debug("Created plain server socket on port " + port);

            return new ServerSocket(port);
        } else if (type.equals(IIOP_SSL)) {
            SSLServerSocket serverSocket = (SSLServerSocket) serverSocketFactory.createServerSocket(port);

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

    public Socket createSocket(EndPointInfo endPointInfo) throws IOException, GetEndPointInfoAgainException {

        String type = endPointInfo.getType();

        if (type.equals(ORBSocketFactory.IIOP_CLEAR_TEXT)) {
            if (log.isDebugEnabled()) log.debug("Created plain socket to " + endPointInfo.getHost() + ":" + endPointInfo.getPort());

            return new Socket(endPointInfo.getHost(), endPointInfo.getPort());
        } else if (type.equals(IIOP_SSL)) {
            SSLSocket socket = null;
            try {
                socket = (SSLSocket) socketFactory.createSocket(endPointInfo.getHost(), endPointInfo.getPort());
            } catch (IOException e) {
                log.debug("could not create socket:", e);
                throw e;
            }

            socket.setEnabledCipherSuites(cipherSuites);
            socket.setWantClientAuth(clientAuthSupported);
            socket.setNeedClientAuth(clientAuthRequired);
            socket.setSoTimeout(60 * 1000);

            if (log.isDebugEnabled()) {
                log.debug("Created SSL socket to " + endPointInfo.getHost() + ":" + endPointInfo.getPort());
                log.debug("    client authentication " + (clientAuthSupported ? "SUPPORTED" : "UNSUPPORTED"));
                log.debug("    client authentication " + (clientAuthRequired ? "REQUIRED" : "OPTIONAL"));
                log.debug("    cipher suites:");

                for (int i = 0; i < cipherSuites.length; i++) {
                    log.debug("    " + cipherSuites[i]);
                }
            }

            return socket;
        }
        throw new COMM_FAILURE("SocketFactory cannot handle: " + type, MinorCodes.UNSUPPORTED_ENDPOINT_TYPE, CompletionStatus.COMPLETED_NO);
    }

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
                        TSSSSLTransportConfig sslConfig = (TSSSSLTransportConfig) transport_mech;

                        if (log.isDebugEnabled()) {
                            int supports = sslConfig.getSupports();
                            int requires = sslConfig.getRequires();

                            log.debug("IOR from target " + sslConfig.getHostname().toLowerCase() + ":" + sslConfig.getPort());
                            log.debug("   SUPPORTS: " + ConfigUtil.flags(supports));
                            log.debug("   REQUIRES: " + ConfigUtil.flags(requires));
                        }

                        if ((NoProtection.value & sslConfig.getRequires()) == NoProtection.value) break;

                        return new EndPointImpl(IIOP_SSL,
                                                sslConfig.getPort(),
                                                sslConfig.getHostname().toLowerCase());

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

    private String getProperty(final String key, final String def) {
        return (String) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return System.getProperty(key, def);
            }
        });
    }
}
