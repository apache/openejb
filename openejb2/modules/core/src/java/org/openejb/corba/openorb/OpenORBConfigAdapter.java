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
package org.openejb.corba.openorb;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.omg.Security.Confidentiality;
import org.omg.Security.EstablishTrustInClient;
import org.omg.Security.EstablishTrustInTarget;
import org.omg.Security.NoProtection;

import org.apache.geronimo.security.deploy.DefaultPrincipal;

import org.openejb.corba.security.config.ConfigAdapter;
import org.openejb.corba.security.config.ConfigException;
import org.openejb.corba.security.config.css.CSSCompoundSecMechConfig;
import org.openejb.corba.security.config.css.CSSCompoundSecMechListConfig;
import org.openejb.corba.security.config.css.CSSConfig;
import org.openejb.corba.security.config.tss.TSSConfig;
import org.openejb.corba.security.config.tss.TSSSSLTransportConfig;
import org.openejb.corba.security.config.tss.TSSTransportMechConfig;


/**
 * @version $Revision$ $Date$
 */
public class OpenORBConfigAdapter implements ConfigAdapter {

    public String[] translateToArgs(TSSConfig config, List args) throws ConfigException {
        ArrayList list = new ArrayList();

        list.addAll(args);

        DefaultPrincipal principal = config.getDefaultPrincipal();
        if (principal != null) {
            list.add("default-principal::" + principal.getRealmName() + ":" + principal.getPrincipal().getClassName() + ":" + principal.getPrincipal().getPrincipalName());
        }

        return (String[]) list.toArray(new String[list.size()]);
    }

    public Properties translateToProps(TSSConfig config) throws ConfigException {
        Properties props = new Properties();

        if (config != null) {
            TSSTransportMechConfig transportMech = config.getTransport_mech();
            if (transportMech != null) {
                if (transportMech instanceof TSSSSLTransportConfig) {
                    TSSSSLTransportConfig sslConfig = (TSSSSLTransportConfig) transportMech;
                    short supports = sslConfig.getSupports();
                    short requires = sslConfig.getRequires();
                    String supProp = "Integrity";
                    String reqProp = "Integrity";

                    props.put("ssliop.port", Short.toString(sslConfig.getPort()));

                    if (sslConfig.getHandshakeTimeout() > 0) {
                        props.put("ssliop.server.handshake.timeout", Short.toString(sslConfig.getHandshakeTimeout()));
                    }

                    if ((supports & Confidentiality.value) != 0) {
                        props.put("ssliop.server.encrypt.support", "true");
                        supProp += ",Confidentiality";

                        if ((requires & Confidentiality.value) != 0) {
                            props.put("ssliop.server.encrypt.requires", "true");
                            reqProp += ",Confidentiality";
                        }
                    }
                    if ((supports & EstablishTrustInTarget.value) != 0) {
                        props.put("ssliop.server.auth.support", "true");
                        supProp += ",EstablishTrustInTarget";

                        if ((requires & EstablishTrustInTarget.value) != 0) {
                            props.put("ssliop.server.auth", "true");
                            reqProp += ",EstablishTrustInTarget";
                        }
                    }
                    if ((supports & EstablishTrustInClient.value) != 0) {
                        props.put("ssliop.server.auth.support", "true");
                        supProp += ",EstablishTrustInClient";

                        if ((requires & EstablishTrustInClient.value) != 0) {
                            props.put("ssliop.server.authClient", "true");
                            reqProp += ",EstablishTrustInClient";
                        }
                    }

                    props.put("csiv2.tss.trans.tls.supports", supProp);
                    props.put("csiv2.tss.trans.tls.requires", reqProp);

                }
            }
        }

        props.put("org.omg.CORBA.ORBClass", "org.openorb.orb.core.ORB");
        props.put("org.omg.CORBA.ORBSingletonClass", "org.openorb.orb.core.ORBSingleton");
        props.put("ssliop.server.AllowBiDir", "true");
        props.put("ssliop.iiopport.disable", "true");
        props.put("ssliop.SSLContextFinderClass", "org.openorb.orb.ssl.JSSEContextFinder");
        props.put("org.omg.PortableInterceptor.ORBInitializerClass.org.openejb.corba.transaction.TransactionInitializer", "");
        props.put("org.omg.PortableInterceptor.ORBInitializerClass.org.openejb.corba.security.SecurityInitializer", "");
        props.put("org.omg.PortableInterceptor.ORBInitializerClass.org.openejb.corba.openorb.OpenORBInitializer", "");
        props.put("iiop.TransportServerInitializerClass", "org.openorb.orb.ssl.SSLTransportServerInitializer");

        return props;
    }

    public String[] translateToArgs(CSSConfig config, List args) throws ConfigException {
        return (String[]) args.toArray(new String[args.size()]);
    }

    public Properties translateToProps(CSSConfig config) throws ConfigException {
        Properties props = new Properties();

        if (config != null) {
            short supports = 0;
            short requires = 0;
            CSSCompoundSecMechListConfig mechList = config.getMechList();
            for (int i = 0; i < mechList.size(); i++) {
                CSSCompoundSecMechConfig mech = mechList.mechAt(i);

                supports |= mech.getTransport_mech().getSupports();
                requires |= mech.getTransport_mech().getRequires();
            }

            String supProp = "Integrity";
            String reqProp = "Integrity";
            if ((supports & NoProtection.value) != 0) {
                props.put("secure.client.allowUnsecure", "true");
                supProp += ",NoProtection";
            } else {
                props.put("secure.client.allowUnsecure", "false");
            }
            if ((supports & Confidentiality.value) != 0) {
                props.put("ssliop.client.encrypt.support", "true");
                supProp += ",Confidentiality";

                if ((requires & Confidentiality.value) != 0) {
                    props.put("ssliop.client.encrypt.requires", "true");
                    reqProp += ",Confidentiality";
                }
            }
            if ((supports & EstablishTrustInTarget.value) != 0) {
                props.put("ssliop.client.auth.support", "true");
                supProp += ",EstablishTrustInTarget";

                if ((requires & EstablishTrustInTarget.value) != 0) {
                    props.put("ssliop.client.auth.requires", "true");
                    reqProp += ",EstablishTrustInTarget";
                }
            }
            if ((supports & EstablishTrustInClient.value) != 0) {
                props.put("ssliop.client.auth.support", "true");
                supProp += ",EstablishTrustInClient";
            }

            props.put("csiv2.css.trans.tls.supports", supProp);
            props.put("csiv2.css.trans.tls.requires", reqProp);

        }

        props.put("org.omg.CORBA.ORBClass", "org.openorb.orb.core.ORB");
        props.put("org.omg.CORBA.ORBSingletonClass", "org.openorb.orb.core.ORBSingleton");
        props.put("ssliop.SSLContextFinderClass", "org.openorb.orb.ssl.JSSEContextFinder");
        props.put("org.omg.PortableInterceptor.ORBInitializerClass.org.openejb.corba.transaction.TransactionInitializer", "");
        props.put("org.omg.PortableInterceptor.ORBInitializerClass.org.openejb.corba.security.SecurityInitializer", "");
        props.put("org.omg.PortableInterceptor.ORBInitializerClass.org.openejb.corba.openorb.OpenORBInitializer", "");
        props.put("iiop.TransportClientInitializerClass", "org.openorb.orb.ssl.SSLTransportClientInitializer");

        return props;
    }
}
