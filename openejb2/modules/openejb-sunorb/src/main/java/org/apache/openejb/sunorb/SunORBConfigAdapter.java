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

import org.apache.openejb.corba.CORBABean;
import org.apache.openejb.corba.CSSBean;
import org.apache.openejb.corba.NameService;
import org.apache.openejb.corba.security.config.ConfigAdapter;
import org.apache.openejb.corba.security.config.ConfigException;
import org.apache.openejb.corba.security.config.css.CSSCompoundSecMechConfig;
import org.apache.openejb.corba.security.config.css.CSSCompoundSecMechListConfig;
import org.apache.openejb.corba.security.config.css.CSSConfig;
import org.apache.openejb.corba.security.config.tss.TSSConfig;
import org.apache.openejb.corba.security.config.ssl.SSLConfig;
import org.apache.openejb.corba.security.config.tss.TSSSSLTransportConfig;
import org.apache.openejb.corba.security.config.tss.TSSTransportMechConfig;


/**
 * @version $Revision$ $Date$
 */
public class SunORBConfigAdapter implements ConfigAdapter {

    private final Log log = LogFactory.getLog(SunORBConfigAdapter.class);

    // location for creating any ORB required files (such as the activation database)
    private String dbDir;

    public SunORBConfigAdapter(String dbDir) {
        this.dbDir = dbDir;
    }


    /**
     * Start the config adapter GBean.  This is basically
     * an opportunity to set any system properties
     * required to make the ORB hook ups.  In particular,
     * this makes the ORB hookups for the RMI over IIOP
     * support.
     *
     * @exception Exception
     */
    public void doStart() throws Exception {
        // define the default ORB for ORB.init();
        System.setProperty("org.omg.CORBA.ORBSingletonClass", "com.sun.corba.se.internal.corba.ORBSingleton");
        System.setProperty("org.omg.CORBA.ORBClass", "org.apache.openejb.sunorb.OpenEJBORB");

        // redirect the RMI implementation to use the Sun ORB.
        System.setProperty("javax.rmi.CORBA.PortableRemoteObjectClass", "com.sun.corba.se.internal.javax.rmi.PortableRemoteObject ");
        System.setProperty("javax.rmi.CORBA.StubClass", "com.sun.corba.se.internal.javax.rmi.CORBA.StubDelegateImpl");

        // this hooks the util class and allows us to override certain functions
        System.setProperty("javax.rmi.CORBA.UtilClass", "org.apache.openejb.corba.util.UtilDelegateImpl");
        // this tells the openejb UtilDelegateImpl which implementation to delegate non-overridden
        // operations to.
        System.setProperty("org.apache.openejb.corba.UtilDelegateClass", "com.sun.corba.se.internal.POA.ShutdownUtilDelegate");

        // ok, now we have a potential classloading problem because of where our util delegates are located.
        // by forcing these classes to load now using our class loader, we can ensure things are properly initialized
        this.getClass().getClassLoader().loadClass("javax.rmi.PortableRemoteObject");
        this.getClass().getClassLoader().loadClass("javax.rmi.PortableRemoteObject");

        log.debug("Started  SunORBConfigAdapter");
    }

    public void doStop() throws Exception {
        log.debug("Stopped SunORBConfigAdapter");
    }

    public void doFail() {
        log.warn("Failed SunORBConfigAdapter");
    }


    /**
     * Create an ORB for a CORBABean server context.
     *
     * @param server The CORBABean that owns this ORB's configuration.
     *
     * @return An ORB instance configured for the CORBABean.
     * @exception ConfigException
     */
    public ORB createServerORB(CORBABean server)  throws ConfigException {
        ORB orb = ORB.init(translateToArgs(server), translateToProps(server));
        postProcess(server, orb);
        return orb;
    }


    /**
     * Create an ORB for a CSSBean client context.
     *
     * @param client The configured CSSBean used for access.
     *
     * @return An ORB instance configured for this client access.
     * @exception ConfigException
     */
    public ORB createClientORB(CSSBean client)  throws ConfigException {
        ORB orb = ORB.init(new String[0], translateToProps(client));
        postProcess(client, orb);
        return orb;
    }


    /**
     * Create a transient name service instance using the
     * specified host name and port.
     *
     * @param host   The String host name.
     * @param port   The port number of the listener.
     *
     * @return An opaque object that represents the name service.
     * @exception ConfigException
     */
    public Object createNameService(String host, int port) throws ConfigException {
        Properties properties = new Properties();

        // This must be a system property, the Sun BootStrapActivation class only looks at the
        // system properties for this value
        System.setProperty("com.sun.CORBA.activation.DbDir", "C:\\temp");

        // the transient name service is automatically started by the Sun NSORB
        properties.put("org.omg.CORBA.ORBClass", "com.sun.corba.se.internal.CosNaming.NSORB");
        // useful for debugging.
//      properties.put("com.sun.CORBA.ORBDebug", "transport,subcontract,poa,orbd,naming,serviceContext,giop,transientObjectManager");

        String portString = Integer.toString(port);

        // causes the Sun orb to immedately activate and start the activation services
        properties.put("com.sun.CORBA.POA.ORBPersistentServerPort", portString);

        // this port must match the above entry so the orb can find its own name server
        properties.put("org.omg.CORBA.ORBInitialPort", portString);

        // create the orb
        return ORB.init(new String[0], properties);
    }


    /**
     * Destroy a name service instance created by a
     * prior call to createNameService().
     *
     * @param ns     The opaque name service object returned from a
     *               prior call to createNameService().
     */
    public void destroyNameService(Object ns) {
        ((ORB)ns).destroy();
    }

    /**
     * Translate the ORB configuration values to options that need to be
     * specfied as invocation arguments.
     *
     * @param server The CORBABean used to configure this server instance.
     *
     * @return A list containing the reconfigured set of string args.
     * @exception ConfigException
     */
    private String[] translateToArgs(CORBABean server) throws ConfigException {
        ArrayList list = new ArrayList();

        // the TSS config holds the security information.
        TSSConfig config = server.getTssConfig();

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

        NameService nameService = server.getNameService();

        // if we have a name service to enable as an initial ref, add it to the init processing.
        if (nameService != null) {
            list.add("-ORBInitRef");
            list.add("NameService=" + nameService.getURI());
        }

        // set the initial listener port
        list.add("-ORBPort");
        list.add(Integer.toString(server.getPort()));

        if (log.isDebugEnabled()) {
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                log.debug(iter.next());
            }
        }

        return (String[]) list.toArray(new String[list.size()]);
    }

    /**
     * Translate a CORBABean configuration into a set of ORB-specific properties
     * required to launch the ORB configured for this property set.
     *
     * @param server The CORBABean object this ORB instance will be
     *               created for.
     *
     * @return The new property bundled configured to launch the ORB.
     * @exception ConfigException
     */
    private Properties translateToProps(CORBABean server) throws ConfigException {
        Properties result = new Properties();

        result.put("org.omg.CORBA.ORBClass", "org.apache.openejb.sunorb.OpenEJBORB");
        result.put("com.sun.CORBA.connection.ORBSocketFactoryClass", "org.apache.openejb.sunorb.OpenEJBSocketFactory");
        result.put("org.omg.PortableInterceptor.ORBInitializerClass.org.apache.openejb.corba.transaction.TransactionInitializer", "");
        result.put("org.omg.PortableInterceptor.ORBInitializerClass.org.apache.openejb.corba.security.SecurityInitializer", "");
        result.put("org.omg.PortableInterceptor.ORBInitializerClass.org.apache.openejb.sunorb.SunORBInitializer", "");
        result.put("com.sun.CORBA.ORBAllowLocalOptimization", "");

        // add the host name
        result.put("com.sun.CORBA.ORBServerHost", server.getHost());

        if (log.isDebugEnabled()) {
            log.debug("translateToProps(TSSConfig)");
            for (Enumeration iter = result.keys(); iter.hasMoreElements();) {
                String key = (String) iter.nextElement();
                log.debug(key + " = " + result.getProperty(key));
            }
        }
        return result;
    }

    /**
     * Perform ORB-specific postProcessing on an ORB created using
     * a CORBABean.
     *
     * This post processing is performed to override the default listening
     * end points so that only SSL ports are opened if the TSS config is
     * configured to use SSL
     *
     * @param config The TSSConfig used to launch the ORB initially.
     * @param ssl    The SSL config required for creating of SSL sockets.
     * @param orb    The ORB instance the following configurations apply to.
     *
     * @exception ConfigException
     */
    private void postProcess(CORBABean server, ORB orb) throws ConfigException {
        OpenEJBORB o = (OpenEJBORB) orb;

        InetAddress host = null;

        try {
            host = InetAddress.getByName(server.getHost());
        } catch (UnknownHostException e) {
            throw new ConfigException("Unable to resolve host name " + server.getHost());
        }

        // make sure the we stuff the socket factory implementation with the config info before creating
        // the first socket.
        OpenEJBSocketFactory factory = (OpenEJBSocketFactory)o.getSocketFactory();
        factory.setConfig(server);

        TSSConfig config = server.getTssConfig();

        if (config != null) {
            TSSTransportMechConfig transportMech = config.getTransport_mech();
            if (transportMech != null) {
                if (transportMech instanceof TSSSSLTransportConfig) {
                    TSSSSLTransportConfig sslConfig = (TSSSSLTransportConfig) transportMech;
                    try {
                        // force the initial listener end point to be an SSL connection.
                        o.getServerGIOP().getEndpoint(OpenEJBSocketFactory.IIOP_SSL, server.getPort(), host);
                        return;
                    } catch (Throwable e) {
                        log.error(e);
                        throw new ConfigException(e);
                    }
                }
            }
        }
        try {
            // force the initial listener to be a plain socket if transport-level security is not indicated.
            o.getServerGIOP().getEndpoint(EndPoint.IIOP_CLEAR_TEXT, server.getPort(), host);
        } catch (Throwable e) {
            log.error(e);
            throw new ConfigException(e);
        }
    }


    /**
     * Translate a CSSConfig into a ORB-specific property bundle used
     * to initialize the ORB.
     *
     * @param config The source CSSConfig.
     * @param props  The initial property bundle set.
     *
     * @return A Property bundle with all of the required ORB parameters.
     * @exception ConfigException
     */
    private Properties translateToProps(CSSBean client) throws ConfigException {
        Properties result = new Properties();

        result.put("org.omg.CORBA.ORBClass", "org.apache.openejb.sunorb.OpenEJBORB");
        result.put("com.sun.CORBA.connection.ORBSocketFactoryClass", "org.apache.openejb.sunorb.OpenEJBSocketFactory");
        result.put("org.omg.PortableInterceptor.ORBInitializerClass.org.apache.openejb.corba.transaction.TransactionInitializer", "");
        result.put("org.omg.PortableInterceptor.ORBInitializerClass.org.apache.openejb.corba.security.SecurityInitializer", "");
        result.put("org.omg.PortableInterceptor.ORBInitializerClass.org.apache.openejb.sunorb.SunORBInitializer", "");

        if (log.isDebugEnabled()) {
            log.debug("translateToProps(CSSConfig)");
            for (Enumeration iter = result.keys(); iter.hasMoreElements();) {
                String key = (String) iter.nextElement();
                log.debug(key + " = " + result.getProperty(key));
            }
        }
        return result;
    }

    /**
     * Perform ORB-specific postProcessing on an ORB created using
     * A CSSConfig.
     *
     * @param config The CSSConfig used to launch the ORB initially.
     * @param ssl    The SSL config required for creating of SSL sockets.
     * @param orb    The ORB instance the following configurations apply to.
     *
     * @exception ConfigException
     */
    private void postProcess(CSSBean client, ORB orb) throws ConfigException {
        // all we need here is stuffing the configuration information into the socket factory before it
        // gets used for anything.
        OpenEJBSocketFactory factory = (OpenEJBSocketFactory)((OpenEJBORB)orb).getSocketFactory();
        factory.setConfig(client);
    }

    /**
     * Retrieve the directory used for ORB configuration files.
     *
     * @return The configured directory.
     */
    public String getDbDir() {
        return dbDir;
    }
}
