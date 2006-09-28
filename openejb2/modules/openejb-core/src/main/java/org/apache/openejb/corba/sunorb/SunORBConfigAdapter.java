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
 * $Id: SunORBConfigAdapter.java 445986 2006-02-10 22:23:25Z ammulder $
 */
package org.apache.openejb.corba.sunorb;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.sun.corba.se.internal.core.EndPoint;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.omg.CORBA.ORB;
import org.omg.CSIIOP.EstablishTrustInClient;
import org.omg.Security.Confidentiality;
import org.omg.Security.EstablishTrustInTarget;
import org.omg.Security.NoProtection;

import org.apache.geronimo.security.deploy.DefaultDomainPrincipal;
import org.apache.geronimo.security.deploy.DefaultPrincipal;
import org.apache.geronimo.security.deploy.DefaultRealmPrincipal;

import org.apache.openejb.corba.security.config.ConfigAdapter;
import org.apache.openejb.corba.security.config.ConfigException;
import org.apache.openejb.corba.security.config.css.CSSCompoundSecMechConfig;
import org.apache.openejb.corba.security.config.css.CSSCompoundSecMechListConfig;
import org.apache.openejb.corba.security.config.css.CSSConfig;
import org.apache.openejb.corba.security.config.tss.TSSConfig;
import org.apache.openejb.corba.security.config.tss.TSSSSLTransportConfig;
import org.apache.openejb.corba.security.config.tss.TSSTransportMechConfig;


/**
 * @version $Revision$ $Date$
 */
public class SunORBConfigAdapter implements ConfigAdapter {

    private final Log log = LogFactory.getLog(SunORBConfigAdapter.class);

    public String[] translateToArgs(TSSConfig config, List args) throws ConfigException {
        ArrayList list = new ArrayList();

        list.addAll(args);

        DefaultPrincipal principal = config.getDefaultPrincipal();
        if (principal != null) {
            if (principal instanceof DefaultRealmPrincipal) {
                DefaultRealmPrincipal realmPrincipal = (DefaultRealmPrincipal) principal;
                list.add("default-realm-principal::" + realmPrincipal.getRealm() + ":" + realmPrincipal.getDomain() + ":"
                         + realmPrincipal.getPrincipal().getClassName() + ":" + realmPrincipal.getPrincipal().getPrincipalName());
            } else if (principal instanceof DefaultDomainPrincipal) {
                DefaultDomainPrincipal domainPrincipal = (DefaultDomainPrincipal) principal;
                list.add("default-domain-principal::" + domainPrincipal.getDomain() + ":"
                         + domainPrincipal.getPrincipal().getClassName() + ":" + domainPrincipal.getPrincipal().getPrincipalName());
            } else {
                list.add("default-principal::" + principal.getPrincipal().getClassName() + ":" + principal.getPrincipal().getPrincipalName());
            }
        }

        if (log.isDebugEnabled()) {
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                log.debug(iter.next());
            }
        }

        return (String[]) list.toArray(new String[list.size()]);
    }

    public Properties translateToProps(TSSConfig config, Properties props) throws ConfigException {
        Properties result = new Properties();

        String supProp = "";
        String reqProp = "";
        if (config != null) {
            TSSTransportMechConfig transportMech = config.getTransport_mech();
            if (transportMech != null) {
                if (transportMech instanceof TSSSSLTransportConfig) {
                    TSSSSLTransportConfig sslConfig = (TSSSSLTransportConfig) transportMech;
                    short supports = sslConfig.getSupports();
                    short requires = sslConfig.getRequires();
                    supProp = "Integrity";
                    reqProp = "Integrity";

                    if ((supports & NoProtection.value) != 0) {
                        supProp += ",NoProtection";
                    }
                    if ((supports & Confidentiality.value) != 0) {
                        supProp += ",Confidentiality";

                        if ((requires & Confidentiality.value) != 0) {
                            reqProp += ",Confidentiality";
                        }
                    }
                    if ((supports & EstablishTrustInClient.value) != 0) {
                        supProp += ",EstablishTrustInClient";

                        if ((requires & EstablishTrustInClient.value) != 0) {
                            reqProp += ",EstablishTrustInClient";
                        }
                    }
                    if ((supports & EstablishTrustInTarget.value) != 0) {
                        supProp += ",EstablishTrustInTarget";

                        if ((requires & EstablishTrustInTarget.value) != 0) {
                            reqProp += ",EstablishTrustInTarget";
                        }
                    }

                } else {
                    //no protection
                    supProp = "NoProtection";
                    reqProp = "NoProtection";
                }
            }
        }
        System.setProperty("org.apache.openejb.corba.ssl.SocketProperties.supports", supProp);
        System.setProperty("org.apache.openejb.corba.ssl.SocketProperties.requires", reqProp);

        result.put("org.omg.CORBA.ORBClass", "org.apache.openejb.corba.sunorb.OpenEJBORB");
        result.put("com.sun.CORBA.connection.ORBSocketFactoryClass", "org.apache.openejb.corba.sunorb.OpenEJBSocketFactory");
        result.put("org.omg.PortableInterceptor.ORBInitializerClass.org.apache.openejb.corba.transaction.TransactionInitializer", "");
        result.put("org.omg.PortableInterceptor.ORBInitializerClass.org.apache.openejb.corba.security.SecurityInitializer", "");
        if ("NoProtection".equals(reqProp)) {
            result.put("org.omg.PortableInterceptor.ORBInitializerClass.org.apache.openejb.corba.sunorb.SunORBInitializerNoSSL", "");
        } else {
            result.put("org.omg.PortableInterceptor.ORBInitializerClass.org.apache.openejb.corba.sunorb.SunORBInitializer", "");
        }
        result.put("com.sun.CORBA.ORBAllowLocalOptimization", "");

        if (log.isDebugEnabled()) {
            log.debug("translateToProps(TSSConfig)");
            for (Enumeration iter = result.keys(); iter.hasMoreElements();) {
                String key = (String) iter.nextElement();
                log.debug(key + " = " + result.getProperty(key));
            }
        }

        result.putAll(props);

        return result;
    }

    /**
     * This post processing is performed to override the default listening
     * end points so that only SSL ports are opened if the TSS config is
     * configured to use SSL
     *
     * @param config Target Security Server configuration
     * @param orb    the ORB to be post processed
     * @throws ConfigException thrown if any error occurs
     */
    public void postProcess(TSSConfig config, ORB orb) throws ConfigException {
        OpenEJBORB o = (OpenEJBORB) orb;
        if (config != null) {
            TSSTransportMechConfig transportMech = config.getTransport_mech();
            if (transportMech != null) {
                if (transportMech instanceof TSSSSLTransportConfig) {
                    TSSSSLTransportConfig sslConfig = (TSSSSLTransportConfig) transportMech;


                    try {
                        o.getServerGIOP().getEndpoint(OpenEJBSocketFactory.IIOP_SSL, sslConfig.getPort(), null);
                    } catch (Throwable e) {
                        log.error(e);
                        throw new ConfigException(e);
                    }
                }
            }
        } else {
            try {
                o.getServerGIOP().getEndpoint(EndPoint.IIOP_CLEAR_TEXT, 6882, null);
            } catch (Throwable e) {
                log.error(e);
                throw new ConfigException(e);
            }
        }

    }

    public InetSocketAddress getDefaultListenAddress(TSSConfig config, ORB orb) throws ConfigException {
        OpenEJBORB o = (OpenEJBORB) orb;
        try {
            if (config != null) {
                TSSTransportMechConfig transportMech = config.getTransport_mech();
                if (transportMech != null) {
                    if (transportMech instanceof TSSSSLTransportConfig) {
                        TSSSSLTransportConfig sslConfig = (TSSSSLTransportConfig) transportMech;
                        return new InetSocketAddress(InetAddress.getLocalHost(), sslConfig.getPort());
                    }
                }
            } else {
                return new InetSocketAddress(InetAddress.getLocalHost(), 6882);
            }
        } catch (UnknownHostException e) {
            throw new ConfigException("Unable to calculate default listen host", e);
        }
        return null; // If they configured a protocol other than SSL that we don't currently support
    }

    public String[] translateToArgs(CSSConfig config, List args) throws ConfigException {
        return (String[]) args.toArray(new String[args.size()]);
    }

    public Properties translateToProps(CSSConfig config, Properties props) throws ConfigException {
        Properties result = new Properties();

        String supProp = "";
        String reqProp = "";
        if (config != null) {
            short supports = 0;
            short requires = 0;
            CSSCompoundSecMechListConfig mechList = config.getMechList();
            for (int i = 0; i < mechList.size(); i++) {
                CSSCompoundSecMechConfig mech = mechList.mechAt(i);

                supports |= mech.getTransport_mech().getSupports();
                requires |= mech.getTransport_mech().getRequires();
            }

            supProp = "Integrity";
            reqProp = "Integrity";
            if ((supports & NoProtection.value) != 0) {
                supProp += ",NoProtection";
            }
            if ((supports & Confidentiality.value) != 0) {
                supProp += ",Confidentiality";

                if ((requires & Confidentiality.value) != 0) {
                    reqProp += ",Confidentiality";
                }
            }
            if ((supports & EstablishTrustInClient.value) != 0) {
                supProp += ",EstablishTrustInClient";

                if ((requires & EstablishTrustInClient.value) != 0) {
                    reqProp += ",EstablishTrustInClient";
                }
            }
            if ((supports & EstablishTrustInTarget.value) != 0) {
                supProp += ",EstablishTrustInTarget";

                if ((requires & EstablishTrustInTarget.value) != 0) {
                    reqProp += ",EstablishTrustInTarget";
                }
            }

        } else {
            supProp = "NoProtection";
            reqProp = "NoProtection";
        }

        System.setProperty("org.apache.openejb.corba.ssl.SocketProperties.supports", supProp);
        System.setProperty("org.apache.openejb.corba.ssl.SocketProperties.requires", reqProp);

        result.put("org.omg.CORBA.ORBClass", "org.apache.openejb.corba.sunorb.OpenEJBORB");
        result.put("com.sun.CORBA.connection.ORBSocketFactoryClass", "org.apache.openejb.corba.sunorb.OpenEJBSocketFactory");
        result.put("org.omg.PortableInterceptor.ORBInitializerClass.org.apache.openejb.corba.transaction.TransactionInitializer", "");
        result.put("org.omg.PortableInterceptor.ORBInitializerClass.org.apache.openejb.corba.security.SecurityInitializer", "");
        result.put("org.omg.PortableInterceptor.ORBInitializerClass.org.apache.openejb.corba.sunorb.SunORBInitializer", "");

        if (log.isDebugEnabled()) {
            log.debug("translateToProps(CSSConfig)");
            for (Enumeration iter = result.keys(); iter.hasMoreElements();) {
                String key = (String) iter.nextElement();
                log.debug(key + " = " + result.getProperty(key));
            }
        }

        result.putAll(props);

        return result;
    }

    public void postProcess(CSSConfig config, ORB orb) throws ConfigException {
    }
}
