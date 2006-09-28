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
 * $Id: ConfigurationFactory.java 444993 2004-10-25 09:55:08Z dblevins $
 */
package org.apache.openejb.config;

import java.io.File;
import java.util.*;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.config.ejb11.*;
import org.apache.openejb.config.sys.*;
import org.apache.openejb.config.sys.Container;
import org.apache.openejb.assembler.*;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Messages;

/**
 * An implementation of the Classic Assembler's OpenEjbConfigurationFactory
 * interface. This implementation translates the user's config file and
 * deployed jars into the required InfoObject structure.
 * <p/>
 * This class doesn't do any configuring per se.  It just
 * reads in the config information for the assebler.
 *
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public class ConfigurationFactory implements OpenEjbConfigurationFactory, ProviderDefaults {

    public static final String DEFAULT_SECURITY_ROLE = "openejb.default.security.role";
    protected static Logger logger = Logger.getInstance("OpenEJB", "org.openejb.util.resources");
    protected static Messages messages = new Messages("org.openejb.util.resources");

    private AutoDeployer deployer;
    private Openejb openejb;
    private DeployedJar[] jars;
    private ServicesJar openejbDefaults = null;

    private String configLocation = "";

    private Vector deploymentIds = new Vector();
    private Vector securityRoles = new Vector();
    private Vector containerIds = new Vector();

    private Vector mthdPermInfos = new Vector();
    private Vector mthdTranInfos = new Vector();
    private Vector sRoleInfos = new Vector();

    //------------------------------------------------//
    //
    //   I n f o   O b j e c t s
    //
    //------------------------------------------------//
    public static OpenEjbConfiguration sys;

    private ContainerInfo[] cntrs;
    private EntityContainerInfo[] entyCntrs;
    private StatefulSessionContainerInfo[] stflCntrs;
    private StatelessSessionContainerInfo[] stlsCntrs;

    /**
     * Hash of container info objects for quick reference
     */
    private HashMap containerTable = new HashMap();

    private Properties props;

    public void init(Properties props) throws OpenEJBException {
        this.props = props;

        configLocation = props.getProperty("openejb.conf.file");

        if (configLocation == null) {
            configLocation = props.getProperty("openejb.configuration");
        }

        configLocation = ConfigUtils.searchForConfiguration(configLocation, props);
        this.props.setProperty("openejb.configuration", configLocation);

    }

    public static void main(String[] args) {
        try {
            ConfigurationFactory conf = new ConfigurationFactory();
            conf.configLocation = args[0];
            conf.init(null);
            OpenEjbConfiguration openejb = conf.getOpenEjbConfiguration();

            conf.printConf(openejb);
        } catch (Exception e) {
            System.out.println("[OpenEJB] " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Read in the configuration information into the
     * Openejb object
     * Validate it
     * <p/>
     * Read in each deployment object
     * Validate it
     * <p/>
     * Translate the whole thing into Info objects.
     * Return them.
     */
    public OpenEjbConfiguration getOpenEjbConfiguration() throws OpenEJBException {

        // Load configuration
        // Validate Configuration
        openejb = ConfigUtils.readConfig(configLocation);

        deployer = new AutoDeployer(openejb);
        
        // Resolve File Locations
        // Resolve Classes
        resolveDependencies(openejb);

        // Load deployments
        jars = loadDeployments(openejb);

        // Build the base OpenEjbConfiguration
        sys = new OpenEjbConfiguration();
        sys.containerSystem = new ContainerSystemInfo();
        sys.facilities = new FacilitiesInfo();

        initJndiProviders(openejb, sys.facilities);
        initTransactionService(openejb, sys.facilities);
        initConnectors(openejb, sys.facilities);
        initConnectionManagers(openejb, sys.facilities);
        initProxyFactory(openejb, sys.facilities);

        // Fills the four container info arrays
        // in this class
        initContainerInfos(openejb);

        sys.containerSystem.containers = cntrs;
        sys.containerSystem.entityContainers = entyCntrs;
        sys.containerSystem.statefulContainers = stflCntrs;
        sys.containerSystem.statelessContainers = stlsCntrs;

        for (int i = 0; i < jars.length; i++) {
            try {
                initEnterpriseBeanInfos(jars[i]);
            } catch (Exception e) {
                e.printStackTrace();
                ConfigUtils.logWarning("conf.0004", jars[i].jarURI, e.getMessage());
            }
        }

        // Add the defaults
        SecurityRoleInfo defaultRole = new SecurityRoleInfo();
        defaultRole.description = "The role applied to recurity references that are not linked.";
        defaultRole.roleName = DEFAULT_SECURITY_ROLE;
        sRoleInfos.add(defaultRole);

        // Collect Arrays
        sys.containerSystem.securityRoles = new SecurityRoleInfo[sRoleInfos.size()];
        sys.containerSystem.methodPermissions = new MethodPermissionInfo[mthdPermInfos.size()];
        sys.containerSystem.methodTransactions = new MethodTransactionInfo[mthdTranInfos.size()];

        sRoleInfos.copyInto(sys.containerSystem.securityRoles);
        mthdPermInfos.copyInto(sys.containerSystem.methodPermissions);
        mthdTranInfos.copyInto(sys.containerSystem.methodTransactions);

        initSecutityService(openejb, sys.facilities);

        return sys;
    }

    Vector jndiProviderIds = new Vector();

    /**
     * Create the JndiContextInfo section of the OpenEJBConfiguration
     * factory.
     *
     * @param openejb
     * @param facilities
     * @throws OpenEJBException
     */
    private void initJndiProviders(Openejb openejb, FacilitiesInfo facilities)
            throws OpenEJBException {
        JndiProvider[] provider = openejb.getJndiProvider();

        if (provider == null || provider.length < 1) return;

        JndiContextInfo[] ctxInfo = new JndiContextInfo[provider.length];
        facilities.remoteJndiContexts = ctxInfo;

        // Init each provider one by one
        for (int i = 0; i < provider.length; i++) {
            provider[i] = (JndiProvider) initService(provider[i], null);
            ServiceProvider service = ServiceUtils.getServiceProvider(provider[i]);
            checkType(service, provider[i], "JndiProvider");

            ctxInfo[i] = new JndiContextInfo();

            ctxInfo[i].jndiContextId = provider[i].getId();

            // Verify the uniqueness of the ID
            if (jndiProviderIds.contains(provider[i].getId())) {
                handleException("conf.0103", configLocation, provider[i].getId());
            }

            jndiProviderIds.add(provider[i].getId());

            // Load the propterties file 
            ctxInfo[i].properties =
                    ServiceUtils.assemblePropertiesFor("JndiProvider",
                            provider[i].getId(),
                            provider[i].getContent(),
                            configLocation,
                            service);
        }
    }

    private void initSecutityService(Openejb openejb, FacilitiesInfo facilities)
            throws OpenEJBException {
        SecurityService ss = openejb.getSecurityService();

        ss = (SecurityService) initService(ss, DEFAULT_SECURITY_SERVICE, SecurityService.class);
        ServiceProvider ssp = ServiceUtils.getServiceProvider(ss);
        checkType(ssp, ss, "Security");

        SecurityServiceInfo ssi = new SecurityServiceInfo();

        ssi.codebase = ss.getJar();
        ssi.description = ssp.getDescription();
        ssi.displayName = ssp.getDisplayName();
        ssi.factoryClassName = ssp.getClassName();
        ssi.serviceName = ss.getId();
        ssi.properties =
                ServiceUtils.assemblePropertiesFor("Security",
                        ss.getId(),
                        ss.getContent(),
                        configLocation,
                        ssp);
        SecurityRoleInfo[] roles = sys.containerSystem.securityRoles;
        RoleMappingInfo[] r = new RoleMappingInfo[roles.length];
        ssi.roleMappings = r;

        // This is really a workaround, I'm simply giving
        // the physical role the same name as the logical 
        // role.  No security services have been integrated
        // with OpenEJB yet. The conecpt of having OpenEJB 
        // do the role linking for the security service has
        // never been put to the test, therefore, we are not 
        // going to worry about role mapping until a valid
        // security service is integrated.  At that time, we
        // can take the approach that makes the most sense.
        for (int i = 0; i < r.length; i++) {
            r[i] = new RoleMappingInfo();
            r[i].logicalRoleNames = new String[]{roles[i].roleName};
            r[i].physicalRoleNames = new String[]{roles[i].roleName};
        }

        facilities.securityService = ssi;
    }

    private void initTransactionService(Openejb openejb, FacilitiesInfo facilities)
            throws OpenEJBException {
        TransactionService ts = openejb.getTransactionService();

        ts =
                (TransactionService) initService(ts,
                        DEFAULT_TRANSACTION_MANAGER,
                        TransactionService.class);
        ServiceProvider service = ServiceUtils.getServiceProvider(ts);
        checkType(service, ts, "Transaction");

        TransactionServiceInfo tsi = new TransactionServiceInfo();

        tsi.codebase = ts.getJar();
        tsi.description = service.getDescription();
        tsi.displayName = service.getDisplayName();
        tsi.factoryClassName = service.getClassName();
        tsi.serviceName = ts.getId();
        tsi.properties =
                ServiceUtils.assemblePropertiesFor("Transaction",
                        ts.getId(),
                        ts.getContent(),
                        configLocation,
                        service);
        facilities.transactionService = tsi;
    }

    Vector connectorIds = new Vector();

    /**
     * Create the ConnectorInfo section of the OpenEJBConfiguration
     * factory.
     *
     * @param openejb
     * @param facilities
     * @throws OpenEJBException
     */
    private void initConnectors(Openejb openejb, FacilitiesInfo facilities)
            throws OpenEJBException {

        Connector[] conn = openejb.getConnector();

        if (conn == null || conn.length < 1) return;

        ConnectorInfo[] info = new ConnectorInfo[conn.length];
        facilities.connectors = info;

        // Init each conn one by one
        for (int i = 0; i < conn.length; i++) {

            conn[i] = (Connector) initService(conn[i], DEFAULT_JDBC_DATABASE, Connector.class);
            ServiceProvider service = ServiceUtils.getServiceProvider(conn[i]);
            checkType(service, conn[i], "Connector");

            ManagedConnectionFactoryInfo factory = new ManagedConnectionFactoryInfo();

            info[i] = new ConnectorInfo();
            info[i].connectorId = conn[i].getId();
            info[i].connectionManagerId = DEFAULT_LOCAL_TX_CON_MANAGER;
            info[i].managedConnectionFactory = factory;

            factory.id = conn[i].getId();
            factory.className = service.getClassName();
            factory.codebase = conn[i].getJar();
            factory.properties =
                    ServiceUtils.assemblePropertiesFor("Connector",
                            conn[i].getId(),
                            conn[i].getContent(),
                            configLocation,
                            service);

            // Verify the uniqueness of the ID
            if (connectorIds.contains(conn[i].getId())) {
                handleException("conf.0103", configLocation, conn[i].getId());
            }

            connectorIds.add(conn[i].getId());
        }
    }

    private void initConnectionManagers(Openejb openejb, FacilitiesInfo facilities)
            throws OpenEJBException {

        ConnectionManagerInfo manager = new ConnectionManagerInfo();
        ConnectionManager cm = openejb.getConnectionManager();

        cm =
                (ConnectionManager) initService(cm,
                        DEFAULT_LOCAL_TX_CON_MANAGER,
                        ConnectionManager.class);

        ServiceProvider service = ServiceUtils.getServiceProvider(cm);

        checkType(service, cm, "ConnectionManager");

        manager.connectionManagerId = cm.getId();
        manager.className = service.getClassName();
        manager.codebase = cm.getJar();
        manager.properties =
                ServiceUtils.assemblePropertiesFor("ConnectionManager",
                        cm.getId(),
                        cm.getContent(),
                        configLocation,
                        service);

        facilities.connectionManagers = new ConnectionManagerInfo[]{manager};
    }

    private void initProxyFactory(Openejb openejb, FacilitiesInfo facilities)
            throws OpenEJBException {
        String defaultFactory = null;
        try {
            String version = System.getProperty("java.vm.version");
            if (version.startsWith("1.1") || version.startsWith("1.2")) {
                defaultFactory = "Default JDK 1.2 ProxyFactory";
            } else {
                defaultFactory = "Default JDK 1.3 ProxyFactory";
            }
        } catch (Exception e) {
            //TODO: Better exception handling
            throw new RuntimeException("Unable to determine the version of your VM.  No ProxyFactory Can be installed");
        }

        ProxyFactory pf = openejb.getProxyFactory();

        pf = (ProxyFactory) initService(pf, defaultFactory, ProxyFactory.class);
        ServiceProvider pfp = ServiceUtils.getServiceProvider(pf);
        checkType(pfp, pf, "Proxy");

        IntraVmServerInfo pfi = new IntraVmServerInfo();

        facilities.intraVmServer = pfi;
        pfi.proxyFactoryClassName = pfp.getClassName();

        pfi.factoryName = pf.getId();
        pfi.codebase = pf.getJar();
        pfi.properties =
                ServiceUtils.assemblePropertiesFor("Proxy",
                        pf.getId(),
                        pf.getContent(),
                        configLocation,
                        pfp);
    }

    /**
     * Initializes the four container info arrays.
     * <p/>
     * ContainerInfo[] cntrs;
     * EntityContainerInfo[] entyCntrs;
     * StatefulSessionContainerInfo stflCntrs;
     * StatelessSessionContainerInfo stlsCntrs;
     *
     * @param conf
     */
    private void initContainerInfos(Openejb conf) throws OpenEJBException {
        Vector e = new Vector();
        Vector sf = new Vector();
        Vector sl = new Vector();

        Container[] containers = conf.getContainer();

        for (int i = 0; i < containers.length; i++) {

            Container c = containers[i];
            ContainerInfo ci = null;

            if (c.getCtype().equals("STATELESS")) {
                c = (Container) initService(c, DEFAULT_STATELESS_CONTAINER);
                ci = new StatelessSessionContainerInfo();
                sl.add(ci);
            } else if (c.getCtype().equals("STATEFUL")) {
                c = (Container) initService(c, DEFAULT_STATEFUL_CONTAINER);
                ci = new StatefulSessionContainerInfo();
                sf.add(ci);
            } else if (c.getCtype().equals("BMP_ENTITY")) {
                c = (Container) initService(c, DEFAULT_BMP_CONTAINER);
                ci = new EntityContainerInfo();
                e.add(ci);
            } else if (c.getCtype().equals("CMP_ENTITY")) {
                c = (Container) initService(c, DEFAULT_CMP_CONTAINER);
                ci = new EntityContainerInfo();
                e.add(ci);
            } else {
                throw new OpenEJBException("Unrecognized contianer type " + c.getCtype());
            }

            ServiceProvider service = ServiceUtils.getServiceProvider(c);
            checkType(service, c, "Container");

            ci.ejbeans = new EnterpriseBeanInfo[0];
            ci.containerName = c.getId();
            ci.className = service.getClassName();
            ci.codebase = c.getJar();
            ci.properties =
                    ServiceUtils.assemblePropertiesFor("Container",
                            c.getId(),
                            c.getContent(),
                            configLocation,
                            service);

            //// Check if ID is a Duplicate /////
            if (containerIds.contains(c.getId())) {
                handleException("conf.0101", configLocation, c.getId());
            }

            containerIds.add(c.getId());

        }

        entyCntrs = new EntityContainerInfo[e.size()];
        e.copyInto(entyCntrs);

        stflCntrs = new StatefulSessionContainerInfo[sf.size()];
        sf.copyInto(stflCntrs);

        stlsCntrs = new StatelessSessionContainerInfo[sl.size()];
        sl.copyInto(stlsCntrs);

        e.addAll(sf);
        e.addAll(sl);
        cntrs = new ContainerInfo[e.size()];
        e.copyInto(cntrs);

        for (int i = 0; i < cntrs.length; i++) {
            containerTable.put(cntrs[i].containerName, cntrs[i]);
        }

    }

    private Map getDeployments(OpenejbJar j) throws OpenEJBException {
        HashMap map = new HashMap(j.getEjbDeploymentCount());

        org.apache.openejb.config.ejb11.EjbDeployment[] ejbDeployment = j.getEjbDeployment();
        for (int i = 0; i < ejbDeployment.length; i++) {
            EjbDeployment deployment = ejbDeployment[i];
            map.put(deployment.getEjbName(), deployment);
        }

        return map;
    }

    /**
     * Creates and EnterpriseBeanInfo for each bean in the deployed jar
     * then calls assignBeansToContainers.  If there is a problem with
     * the jar, such as a duplicate deployment id, the jar will be skipped.
     *
     * @param jar
     * @throws OpenEJBException
     */
    private void initEnterpriseBeanInfos(DeployedJar jar) throws OpenEJBException {

        int beansDeployed = jar.openejbJar.getEjbDeploymentCount();
        int beansInEjbJar = jar.ejbJar.getEnterpriseBeans().getEnterpriseBeansItemCount();

        if (beansInEjbJar != beansDeployed) {
            ConfigUtils.logWarning("conf.0008", jar.jarURI, "" + beansInEjbJar, "" + beansDeployed);
            // Not all ejb in this jar have been deployed.
            // This jar cannot be loaded into the system and must 
            // be skipped.
            return;
        }

        Map ejbds = getDeployments(jar.openejbJar);
        Map infos = new HashMap();
        Map items = new HashMap();
        EnterpriseBeanInfo[] beans = new EnterpriseBeanInfo[ejbds.size()];
        int i = -1;

        Enumeration bl = jar.ejbJar.getEnterpriseBeans().enumerateEnterpriseBeansItem();
        while (bl.hasMoreElements()) {
            EnterpriseBeansItem item = (EnterpriseBeansItem) bl.nextElement();
            i++;

            if (item.getEntity() == null) {
                beans[i] = initSessionBean(item, ejbds);
            } else {
                beans[i] = initEntityBean(item, ejbds);
            }

            // Check For Duplicate Deployment IDs
            if (deploymentIds.contains(beans[i].ejbDeploymentId)) {
                ConfigUtils.logWarning("conf.0100",
                        beans[i].ejbDeploymentId,
                        jar.jarURI,
                        beans[i].ejbName);
                // No two deployments can have the same deployment ID
                // the entire ejb jar is invalid and must be redeployed.
                // This jar cannot be loaded into the system and must 
                // be skipped.
                return;
            }

            deploymentIds.add(beans[i].ejbDeploymentId);

            beans[i].codebase = jar.jarURI;
            infos.put(beans[i].ejbName, beans[i]);
            items.put(beans[i].ejbName, item);

        }

        initJndiReferences(ejbds, infos, items);

        if (jar.ejbJar.getAssemblyDescriptor() != null) {
            initSecurityRoles(jar, ejbds, infos, items);
            initMethodPermissions(jar, ejbds, infos, items);
            initMethodTransactions(jar, ejbds, infos, items);

            // Resolve security role links
            for (int x = 0; x < beans.length; x++) {
                resolveRoleLinks(jar, beans[x], (EnterpriseBeansItem) items.get(beans[x].ejbName));
            }
        }

        assignBeansToContainers(beans, ejbds);

        try {
            //TODO:2: This is really temporary, jars should have their
            // own classpaths.  We have code for this, but it has a couple
            // issues in the CMP container that prevent us from relying on it.
            org.apache.openejb.util.ClasspathUtils.addJarToPath(jar.jarURI);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initJndiReferences(Map ejbds, Map infos, Map items) throws OpenEJBException {

        Iterator i = infos.values().iterator();
        while (i.hasNext()) {
            EnterpriseBeanInfo bean = (EnterpriseBeanInfo) i.next();
            EnterpriseBeansItem item = (EnterpriseBeansItem) items.get(bean.ejbName);
            Enumeration envEntries = null;
            Enumeration ejbRefs = null;
            Enumeration ejbLocalRefs = null;
            Enumeration resourceRefs = null;

            if (item.getEntity() != null) {
                envEntries = item.getEntity().enumerateEnvEntry();
                ejbRefs = item.getEntity().enumerateEjbRef();
                ejbLocalRefs = item.getEntity().enumerateEjbLocalRef();
                resourceRefs = item.getEntity().enumerateResourceRef();
            } else {
                envEntries = item.getSession().enumerateEnvEntry();
                ejbRefs = item.getSession().enumerateEjbRef();
                ejbLocalRefs = item.getSession().enumerateEjbLocalRef();
                resourceRefs = item.getSession().enumerateResourceRef();
            }

            Vector envRef = new Vector();
            Vector ejbRef = new Vector();
            Vector ejbLocalRef = new Vector();
            Vector resRef = new Vector();

            /* Build Environment entries *****************/
            while (envEntries.hasMoreElements()) {
                EnvEntry env = (EnvEntry) envEntries.nextElement();
                EnvEntryInfo info = new EnvEntryInfo();

                info.name = env.getEnvEntryName();
                info.type = env.getEnvEntryType();
                info.value = env.getEnvEntryValue();

                envRef.add(info);
            }

            /* Build Resource References *****************/
            EjbDeployment dep = (EjbDeployment) ejbds.get(bean.ejbName);
            Enumeration rl = dep.enumerateResourceLink();
            Map resLinks = new HashMap();
            while (rl.hasMoreElements()) {
                ResourceLink link = (ResourceLink) rl.nextElement();
                resLinks.put(link.getResRefName(), link);
            }

            /* Build EJB References **********************/
            while (ejbRefs.hasMoreElements()) {
                EjbRef ejb = (EjbRef) ejbRefs.nextElement();
                EjbReferenceInfo info = new EjbReferenceInfo();

                info.homeType = ejb.getHome();
                info.referenceName = ejb.getEjbRefName();
                info.location = new EjbReferenceLocationInfo();

                //if the ejb-link is null on the ejb-jar, get it from the openejb-jar
                String ejbLink;
                if (ejb.getEjbLink() == null) {
                    ejbLink = ((ResourceLink) resLinks.get(ejb.getEjbRefName())).getResId();
                } else {
                    ejbLink = ejb.getEjbLink();
                }

                EnterpriseBeanInfo otherBean = (EnterpriseBeanInfo) infos.get(ejbLink);
                if (otherBean == null) {
                    String msg =
                            messages.format("config.noBeanFound", ejb.getEjbRefName(), bean.ejbName);

                    logger.fatal(msg);
                    throw new OpenEJBException(msg);
                }
                info.location.ejbDeploymentId = otherBean.ejbDeploymentId;

                ejbRef.add(info);
            }


            /* Build EJB References **********************/
            while (ejbLocalRefs.hasMoreElements()) {
                EjbLocalRef ejb = (EjbLocalRef) ejbLocalRefs.nextElement();
                EjbLocalReferenceInfo info = new EjbLocalReferenceInfo();

                info.homeType = ejb.getLocalHome();
                info.referenceName = ejb.getEjbRefName();
                info.location = new EjbReferenceLocationInfo();

                //the ejb-link must be local to the ejb-jar
                String ejbLink;
                if (ejb.getEjbLink() == null) {
                    ejbLink = null;
                    //ejbLink = ((ResourceLink) resLinks.get(ejb.getEjbRefName())).getResId();
                } else {
                    ejbLink = ejb.getEjbLink();
                }

                EnterpriseBeanInfo otherBean = (EnterpriseBeanInfo) infos.get(ejbLink);
                if (otherBean == null) {
                    String msg =
                            messages.format("config.noBeanFound", ejb.getEjbRefName(), bean.ejbName);

                    logger.fatal(msg);
                    throw new OpenEJBException(msg);
                }
                info.location.ejbDeploymentId = otherBean.ejbDeploymentId;

                ejbLocalRef.add(info);
            }

            while (resourceRefs.hasMoreElements()) {
                ResourceRef res = (ResourceRef) resourceRefs.nextElement();
                ResourceReferenceInfo info = new ResourceReferenceInfo();

                info.referenceAuth = res.getResAuth();
                info.referenceName = res.getResRefName();
                info.referenceType = res.getResType();

                ResourceLink link = (ResourceLink) resLinks.get(res.getResRefName());
                info.resourceID = link.getResId();

                resRef.add(info);
            }

            /*  Assign everything to the EnterpriseBeanInfo *****/
            JndiEncInfo jndi = new JndiEncInfo();
            jndi.envEntries = new EnvEntryInfo[envRef.size()];
            jndi.ejbReferences = new EjbReferenceInfo[ejbRef.size()];
//            jndi.ejbLocalReferences = new EjbLocalReferenceInfo[ejbLocalRef.size()];
            jndi.resourceRefs = new ResourceReferenceInfo[resRef.size()];

            envRef.copyInto(jndi.envEntries);
            ejbRef.copyInto(jndi.ejbReferences);
            resRef.copyInto(jndi.resourceRefs);
//            ejbLocalRef.copyInto(jndi.ejbLocalReferences);

            bean.jndiEnc = jndi;

        }

    }

    private void initMethodTransactions(DeployedJar jar, Map ejbds, Map infos, Map items)
            throws OpenEJBException {

        ContainerTransaction[] cTx = jar.ejbJar.getAssemblyDescriptor().getContainerTransaction();

        if (cTx == null || cTx.length < 1) return;

        MethodTransactionInfo[] mTxs = new MethodTransactionInfo[cTx.length];

        for (int i = 0; i < mTxs.length; i++) {
            mTxs[i] = new MethodTransactionInfo();

            mTxs[i].description = cTx[i].getDescription();
            mTxs[i].transAttribute = cTx[i].getTransAttribute();
            mTxs[i].methods = getMethodInfos(cTx[i].getMethod(), ejbds);
        }

        this.mthdTranInfos.addAll(Arrays.asList(mTxs));
    }

    private void initSecurityRoles(DeployedJar jar, Map ejbds, Map infos, Map items)
            throws OpenEJBException {

        SecurityRole[] sr = jar.ejbJar.getAssemblyDescriptor().getSecurityRole();

        if (sr == null || sr.length < 1) return;

        SecurityRoleInfo[] roles = new SecurityRoleInfo[sr.length];
        for (int i = 0; i < roles.length; i++) {
            roles[i] = new SecurityRoleInfo();

            roles[i].description = sr[i].getDescription();
            roles[i].roleName = sr[i].getRoleName();

            // Check For Duplicate Container IDs
            if (securityRoles.contains(sr[i].getRoleName())) {
                ConfigUtils.logWarning("conf.0102", jar.jarURI, sr[i].getRoleName());
            } else {
                securityRoles.add(sr[i].getRoleName());
            }
        }

        this.sRoleInfos.addAll(Arrays.asList(roles));
    }

    private void initMethodPermissions(DeployedJar jar, Map ejbds, Map infos, Map items)
            throws OpenEJBException {

        MethodPermission[] mp = jar.ejbJar.getAssemblyDescriptor().getMethodPermission();
        if (mp == null || mp.length < 1) return;

        MethodPermissionInfo[] perms = new MethodPermissionInfo[mp.length];

        for (int i = 0; i < perms.length; i++) {
            perms[i] = new MethodPermissionInfo();

            perms[i].description = mp[i].getDescription();
            perms[i].roleNames = mp[i].getRoleName();
            perms[i].methods = getMethodInfos(mp[i].getMethod(), ejbds);
        }

        this.mthdPermInfos.addAll(Arrays.asList(perms));
    }

    /**
     * Verify that everything has been linked and
     * that those links refer to actual declared roles.
     *
     * @param bean
     * @throws OpenEJBException
     */
    private void resolveRoleLinks(DeployedJar jar,
                                  EnterpriseBeanInfo bean,
                                  EnterpriseBeansItem item)
            throws OpenEJBException {
        SecurityRoleRef[] refs = null;
        if (item.getEntity() != null) {
            refs = item.getEntity().getSecurityRoleRef();
        } else {
            refs = item.getSession().getSecurityRoleRef();
        }

        if (refs == null || refs.length < 1) return;

        SecurityRoleReferenceInfo[] sr = new SecurityRoleReferenceInfo[refs.length];
        bean.securityRoleReferences = sr;

        for (int i = 0; i < sr.length; i++) {
            sr[i] = new SecurityRoleReferenceInfo();

            sr[i].description = refs[i].getDescription();
            sr[i].roleLink = refs[i].getRoleLink();
            sr[i].roleName = refs[i].getRoleName();

            if (sr[i].roleLink == null) {
                ConfigUtils.logWarning("conf.0009", sr[i].roleName, bean.ejbName, jar.jarURI);
                sr[i].roleLink = DEFAULT_SECURITY_ROLE;
            }
        }

    }

    private MethodInfo[] getMethodInfos(Method[] ms, Map ejbds) {
        if (ms == null) return null;

        MethodInfo[] mi = new MethodInfo[ms.length];
        for (int i = 0; i < mi.length; i++) {

            mi[i] = new MethodInfo();

            EjbDeployment d = (EjbDeployment) ejbds.get(ms[i].getEjbName());

            mi[i].description = ms[i].getDescription();
            mi[i].ejbDeploymentId = d.getDeploymentId();
            mi[i].methodIntf = ms[i].getMethodIntf();
            mi[i].methodName = ms[i].getMethodName();

            // get the method parameters
            MethodParams mp = ms[i].getMethodParams();
            if (mp != null) {
                mi[i].methodParams = mp.getMethodParam();
            }
        }

        return mi;
    }

    private EnterpriseBeanInfo initSessionBean(EnterpriseBeansItem item, Map m)
            throws OpenEJBException {
        Session s = item.getSession();
        EnterpriseBeanInfo bean = null;

        if (s.getSessionType().equals("Stateful"))
            bean = new StatefulBeanInfo();
        else
            bean = new StatelessBeanInfo();

        EjbDeployment d = (EjbDeployment) m.get(s.getEjbName());
        if (d == null) {
            throw new OpenEJBException("No deployment information in openejb-jar.xml for bean "
                    + s.getEjbName()
                    + ". Please redeploy the jar");
        }
        bean.ejbDeploymentId = d.getDeploymentId();

        bean.description = s.getDescription();
        bean.largeIcon = s.getLargeIcon();
        bean.smallIcon = s.getSmallIcon();
        bean.displayName = s.getDisplayName();
        bean.ejbClass = s.getEjbClass();
        bean.ejbName = s.getEjbName();
        bean.home = s.getHome();
        bean.remote = s.getRemote();
//        bean.localHome = s.getLocalHome();
//        bean.local = s.getLocal();
        bean.transactionType = s.getTransactionType();

        return bean;
    }

    private EnterpriseBeanInfo initEntityBean(EnterpriseBeansItem item, Map m)
            throws OpenEJBException {
        Entity e = item.getEntity();
        EntityBeanInfo bean = new EntityBeanInfo();

        EjbDeployment d = (EjbDeployment) m.get(e.getEjbName());
        if (d == null) {
            throw new OpenEJBException("No deployment information in openejb-jar.xml for bean "
                    + e.getEjbName()
                    + ". Please redeploy the jar");
        }
        bean.ejbDeploymentId = d.getDeploymentId();

        bean.description = e.getDescription();
        bean.largeIcon = e.getLargeIcon();
        bean.smallIcon = e.getSmallIcon();
        bean.displayName = e.getDisplayName();
        bean.ejbClass = e.getEjbClass();
        bean.ejbName = e.getEjbName();
        bean.home = e.getHome();
        bean.remote = e.getRemote();
//        bean.localHome = e.getLocalHome();
//        bean.local = e.getLocal();
        bean.transactionType = "Container";

        bean.primKeyClass = e.getPrimKeyClass();
        bean.primKeyField = e.getPrimkeyField();
        bean.persistenceType = e.getPersistenceType();
        bean.reentrant = e.getReentrant() + "";

        bean.cmpFieldNames = new String[e.getCmpFieldCount()];

        for (int i = 0; i < bean.cmpFieldNames.length; i++) {
            bean.cmpFieldNames[i] = e.getCmpField(i).getFieldName();
        }

        if (bean.persistenceType.equals("Container")) {

            Query[] q = d.getQuery();
            QueryInfo[] qi = new QueryInfo[q.length];

            for (int i = 0; i < q.length; i++) {
                QueryInfo query = new QueryInfo();
                query.description = q[i].getDescription();
                query.queryStatement = q[i].getObjectQl().trim();

                MethodInfo method = new MethodInfo();
                QueryMethod qm = q[i].getQueryMethod();
                method.methodName = qm.getMethodName();
                method.methodParams = qm.getMethodParams().getMethodParam();

                query.method = method;
                qi[i] = query;
            }
            bean.queries = qi;
        }
        return bean;
    }

    private void assignBeansToContainers(EnterpriseBeanInfo[] beans, Map ejbds)
            throws OpenEJBException {

        for (int i = 0; i < beans.length; i++) {
            // Get the bean deployment object
            EjbDeployment d = (EjbDeployment) ejbds.get(beans[i].ejbName);

            // Get the container it was assigned to
            ContainerInfo cInfo = (ContainerInfo) containerTable.get(d.getContainerId());
            if (cInfo == null) {
                //TODO Create container if one is not provided
                String msg =
                        messages.format("config.noContainerFound", d.getContainerId(), d.getEjbName());

                logger.fatal(msg);
                throw new OpenEJBException(msg);
            }

            // Add the bean info object to the cotnainer's bean array
            EnterpriseBeanInfo[] oldList = cInfo.ejbeans;
            EnterpriseBeanInfo[] newList = new EnterpriseBeanInfo[oldList.length + 1];
            System.arraycopy(oldList, 0, newList, 1, oldList.length);
            newList[0] = beans[i];
            cInfo.ejbeans = newList;
        }

        // Now create the bean arrays of the specific container
        // type.
        for (int i = 0; i < entyCntrs.length; i++) {
            EnterpriseBeanInfo[] b = entyCntrs[i].ejbeans;
            EntityBeanInfo[] eb = new EntityBeanInfo[b.length];
            System.arraycopy(b, 0, eb, 0, b.length);
        }

    }

    /**
     * Resolve file locations
     * Resolve classes
     * TODO: Not integral now, implement later.
     */
    private void resolveDependencies(Openejb openejb) {
    }

    /**
     * Resolve file locations
     * Resolve classes
     * TODO: Not integral now, implement later.
     */
    private void resolveDependencies(EjbJar[] jars) {
    }

    /**
     * Loads a list of jar names that we will attempt
     * to deploy.
     * If the Deployments element is a directory
     * then it will load all the jars from that directory.
     * <p/>
     * If a jar was listed twice in the config file for some
     * reason, it only occur once in the list returned
     */
    private String[] getJarLocations(Deployments[] deploy) {

        Vector jarList = new Vector(deploy.length);

        try {

            for (int i = 0; i < deploy.length; i++) {

                Deployments d = deploy[i];

                ///// Add Jar file  /////
                if (d.getDir() == null && d.getJar() != null) {
                    File jar = null;
                    try {
//                        jar = FileUtils.getBase(this.props).getFile(d.getJar(), false);
                    } catch (Exception ignored) {
                        try {
//                            jar = FileUtils.getHome(this.props).getFile(d.getJar(), false);
                        } catch (Exception ignoredAgain) {
                        }
                    }
                    if (!jarList.contains(jar.getAbsolutePath())) {
                        jarList.add(jar.getAbsolutePath());
                    }

                    continue;
                }

                ///// A directory /////

                File dir = null;
                try {
//                    dir = FileUtils.getBase(this.props).getFile(d.getDir(), false);
                } catch (Exception ignored) {
                }
                if (dir == null || !dir.exists()) {
                    try {
//                        dir = FileUtils.getHome(this.props).getFile(d.getDir(), false);
                    } catch (Exception ignoredAgain) {
                    }
                }

                // Opps! Not a directory
                if (dir == null || !dir.isDirectory()) continue;

                String[] files = dir.list();

                if (files == null) {
                    continue;
                }

                for (int x = 0; x < files.length; x++) {

                    String f = files[x];

                    if (!f.endsWith(".jar")) continue;

                    //// Found a jar in the dir ////

                    File jar = new File(dir, f);

                    if (jarList.contains(jar.getAbsolutePath())) continue;
                    jarList.add(jar.getAbsolutePath());

                }

            }
        } catch (SecurityException se) {
            //Worthless security exception
            // log it and move on
            // TODO:  Log this
        }

        String[] locations = new String[jarList.size()];
        jarList.copyInto(locations);

        return locations;

    }

    /**
     * Resolve file locations
     * Resolve classes
     * TODO: Not integral now, implement later.
     */
    private DeployedJar[] loadDeployments(Openejb openejb) throws OpenEJBException {

        EjbValidator validator = new EjbValidator();

        Vector jarsVect = new Vector();

        String[] jarsToLoad = getJarLocations(openejb.getDeployments());

        /*[1]  Put all EjbJar & OpenejbJar objects in a vector ***************/
        for (int i = 0; i < jarsToLoad.length; i++) {

            String jarLocation = jarsToLoad[i];
            try {
                EjbJar ejbJar = EjbJarUtils.readEjbJar(jarLocation);

                /* If there is no openejb-jar.xml attempt to auto deploy it. 
                 */
                OpenejbJar openejbJar = ConfigUtils.readOpenejbJar(jarLocation);
                if (openejbJar == null) {
                    openejbJar = deployer.deploy(ejbJar, jarLocation);
                }
                validateJar(ejbJar, jarLocation);

                /* Add it to the Vector ***************/
                jarsVect.add(new DeployedJar(jarLocation, ejbJar, openejbJar));
            } catch (OpenEJBException e) {
                ConfigUtils.logWarning("conf.0004", jarLocation, e.getMessage());
            }
        }

        /*[2]  Get a DeployedJar array from the vector ***************/
        DeployedJar[] jars = new DeployedJar[jarsVect.size()];
        jarsVect.copyInto(jars);
        return jars;
    }

    private void validateJar(EjbJar ejbJar, String jarLocation) throws OpenEJBException {

        EjbValidator validator = new EjbValidator();
        EjbSet set = validator.validateJar(ejbJar, jarLocation);
        if (set.hasErrors() || set.hasFailures()) {
            //System.out.println("[] INVALID "+ jarLocation);
            throw new OpenEJBException("Jar failed validation.  Use the validation tool for more details");
        }

    }

    public Service initService(Service service, String defaultName) throws OpenEJBException {
        return initService(service, defaultName, null);
    }

    /**
     * Service loading...
     * <p/>
     * 1. Try and load by provider id
     * 2. Try and load by id of the service
     * 3. Load the default provider
     *
     * @param service
     * @param defaultName
     * @return
     * @throws OpenEJBException
     */
    public Service initService(Service service, String defaultName, Class type)
            throws OpenEJBException {

        if (service == null) {
            try {
                service = (Service) type.newInstance();
                service.setProvider(defaultName);
                service.setId(defaultName);
            } catch (Exception e) {
                throw new OpenEJBException("Cannot instantiate class " + type);
            }
        } else if (service.getProvider() == null) {
            // If the service.getId() points to a valid
            // ServiceProvider, then let's use that...
            try {
                ServiceUtils.getServiceProvider(service.getId());
                service.setProvider(service.getId());
            } catch (Exception e) {
                // Guess that didn't work, let's use the default...
                service.setProvider(defaultName);
            }
        }

        return service;
    }

    private void checkType(ServiceProvider provider, Service service, String type)
            throws OpenEJBException {
        if (!provider.getProviderType().equals(type)) {
            handleException("conf.4902", service, type);
        }
    }

    String[] tabs = {"", " ", "    ", "      ", "        ", "          "};

    private void printConf(OpenEjbConfiguration conf) {
        out(0, "CONFIGURATION");

        out(1, conf.containerSystem.containers.length);
        for (int i = 0; i < conf.containerSystem.containers.length; i++) {
            out(1, "className    ", conf.containerSystem.containers[i].className);
            out(1, "codebase     ", conf.containerSystem.containers[i].codebase);
            out(1, "containerName", conf.containerSystem.containers[i].containerName);
            out(1, "containerType", conf.containerSystem.containers[i].containerType);
            out(1, "description  ", conf.containerSystem.containers[i].description);
            out(1, "displayName  ", conf.containerSystem.containers[i].displayName);
            out(1, "properties   ");
            conf.containerSystem.containers[i].properties.list(System.out);
            out(1, "ejbeans      ", conf.containerSystem.containers[i].ejbeans.length);
            for (int j = 0; j < conf.containerSystem.containers[i].ejbeans.length; j++) {
                EnterpriseBeanInfo bean = conf.containerSystem.containers[i].ejbeans[j];
                out(2, "codebase       ", bean.codebase);
                out(2, "description    ", bean.description);
                out(2, "displayName    ", bean.displayName);
                out(2, "ejbClass       ", bean.ejbClass);
                out(2, "ejbDeploymentId", bean.ejbDeploymentId);
                out(2, "ejbName        ", bean.ejbName);
                out(2, "home           ", bean.home);
                out(2, "largeIcon      ", bean.largeIcon);
                out(2, "remote         ", bean.remote);
                out(2, "smallIcon      ", bean.smallIcon);
                out(2, "transactionType", bean.transactionType);
                out(2, "type           ", bean.type);
                out(2, "jndiEnc        ", bean.jndiEnc);
                out(2, "envEntries     ", bean.jndiEnc.envEntries.length);
                for (int n = 0; n < bean.jndiEnc.envEntries.length; n++) {
                    out(3, "--[" + n + "]----------------------");
                    out(3, "name  ", bean.jndiEnc.envEntries[n].name);
                    out(3, "type  ", bean.jndiEnc.envEntries[n].type);
                    out(3, "value ", bean.jndiEnc.envEntries[n].value);
                }
                out(2, "ejbReferences  ", bean.jndiEnc.ejbReferences.length);
                for (int n = 0; n < bean.jndiEnc.ejbReferences.length; n++) {
                    out(3, "--[" + n + "]----------------------");
                    out(3, "homeType        ", bean.jndiEnc.ejbReferences[n].homeType);
                    out(3, "referenceName   ", bean.jndiEnc.ejbReferences[n].referenceName);
                    out(3, "location        ", bean.jndiEnc.ejbReferences[n].location);
                    out(3, "ejbDeploymentId ", bean.jndiEnc.ejbReferences[n].location.ejbDeploymentId);
                    out(3, "jndiContextId   ", bean.jndiEnc.ejbReferences[n].location.jndiContextId);
                    out(3, "remote          ", bean.jndiEnc.ejbReferences[n].location.remote);
                    out(3, "remoteRefName   ", bean.jndiEnc.ejbReferences[n].location.remoteRefName);
                }
                out(2, "resourceRefs   ", bean.jndiEnc.resourceRefs.length);
                for (int n = 0; n < bean.jndiEnc.resourceRefs.length; n++) {
                    out(3, "--[" + n + "]----------------------");
                    out(3, "referenceAuth   ", bean.jndiEnc.resourceRefs[n].referenceAuth);
                    out(3, "referenceName   ", bean.jndiEnc.resourceRefs[n].referenceName);
                    out(3, "referenceType   ", bean.jndiEnc.resourceRefs[n].referenceType);
                    if (bean.jndiEnc.resourceRefs[n].location != null) {
                        out(3, "location        ", bean.jndiEnc.resourceRefs[n].location);
                        out(3, "jndiContextId   ", bean.jndiEnc.resourceRefs[n].location.jndiContextId);
                        out(3, "remote          ", bean.jndiEnc.resourceRefs[n].location.remote);
                        out(3, "remoteRefName   ", bean.jndiEnc.resourceRefs[n].location.remoteRefName);
                    }
                }
            }
        }

        if (conf.containerSystem.securityRoles != null) {
            out(0, "--Security Roles------------");
            for (int i = 0; i < sys.containerSystem.securityRoles.length; i++) {
                out(1, "--[" + i + "]----------------------");
                out(1, "            ", sys.containerSystem.securityRoles[i]);
                out(1, "description ", sys.containerSystem.securityRoles[i].description);
                out(1, "roleName    ", sys.containerSystem.securityRoles[i].roleName);
            }
        }

        if (conf.containerSystem.methodPermissions != null) {
            out(0, "--Method Permissions--------");
            for (int i = 0; i < sys.containerSystem.methodPermissions.length; i++) {
                out(1, "--[" + i + "]----------------------");
                out(1, "            ", sys.containerSystem.methodPermissions[i]);
                out(1, "description ", sys.containerSystem.methodPermissions[i].description);
                out(1, "roleNames   ", sys.containerSystem.methodPermissions[i].roleNames);
                if (sys.containerSystem.methodPermissions[i].roleNames != null) {
                    String[] roleNames = sys.containerSystem.methodPermissions[i].roleNames;
                    for (int r = 0; r < roleNames.length; r++) {
                        out(1, "roleName[" + r + "]   ", roleNames[r]);
                    }
                }
                out(1, "methods     ", conf.containerSystem.methodPermissions[i].methods);
                if (conf.containerSystem.methodPermissions[i].methods != null) {
                    MethodInfo[] mthds = conf.containerSystem.methodPermissions[i].methods;
                    for (int j = 0; j < mthds.length; j++) {
                        out(2, "description    ", mthds[j].description);
                        out(2, "ejbDeploymentId", mthds[j].ejbDeploymentId);
                        out(2, "methodIntf     ", mthds[j].methodIntf);
                        out(2, "methodName     ", mthds[j].methodName);
                        out(2, "methodParams   ", mthds[j].methodParams);
                        if (mthds[j].methodParams != null) {
                            for (int n = 0; n < mthds[j].methodParams.length; n++) {
                                out(3, "param[" + n + "]", mthds[j].methodParams[n]);
                            }
                        }

                    }
                }
            }
        }

        if (conf.containerSystem.methodTransactions != null) {
            out(0, "--Method Transactions-------");
            for (int i = 0; i < conf.containerSystem.methodTransactions.length; i++) {

                out(1, "--[" + i + "]----------------------");
                out(1, "               ", conf.containerSystem.methodTransactions[i]);
                out(1, "description    ", conf.containerSystem.methodTransactions[i].description);
                out(1, "transAttribute ", conf.containerSystem.methodTransactions[i].transAttribute);
                out(1, "methods        ", conf.containerSystem.methodTransactions[i].methods);
                if (conf.containerSystem.methodTransactions[i].methods != null) {
                    MethodInfo[] mthds = conf.containerSystem.methodTransactions[i].methods;
                    for (int j = 0; j < mthds.length; j++) {
                        out(2, "description    ", mthds[j].description);
                        out(2, "ejbDeploymentId", mthds[j].ejbDeploymentId);
                        out(2, "methodIntf     ", mthds[j].methodIntf);
                        out(2, "methodName     ", mthds[j].methodName);
                        out(2, "methodParams   ", mthds[j].methodParams);
                        if (mthds[j].methodParams != null) {
                            for (int n = 0; n < mthds[j].methodParams.length; n++) {
                                out(3, "param[" + n + "]", mthds[j].methodParams[n]);
                            }
                        }

                    }
                }
            }
        }
    }

    private void out(int t, String m) {
        System.out.println(tabs[t] + m);
    }

    private void out(int t, String m, String n) {
        System.out.println(tabs[t] + m + " = " + n);
    }

    private void out(int t, String m, boolean n) {
        System.out.println(tabs[t] + m + " = " + n);
    }

    private void out(int t, String m, int n) {
        System.out.println(tabs[t] + m + " = " + n);
    }

    private void out(int t, String m, Object n) {
        System.out.println(tabs[t] + m + " = " + n);
    }

    private void out(int t, int m) {
        System.out.println(tabs[t] + m);
    }

    /*------------------------------------------------------*/
    /*    Methods for easy exception handling               */
    /*------------------------------------------------------*/
    public static void handleException(String errorCode,
                                       Object arg0,
                                       Object arg1,
                                       Object arg2,
                                       Object arg3)
            throws OpenEJBException {
        throw new OpenEJBException(messages.format(errorCode, arg0, arg1, arg2, arg3));
    }

    public static void handleException(String errorCode, Object arg0, Object arg1, Object arg2)
            throws OpenEJBException {
        throw new OpenEJBException(messages.format(errorCode, arg0, arg1, arg2));
    }

    public static void handleException(String errorCode, Object arg0, Object arg1)
            throws OpenEJBException {
        throw new OpenEJBException(messages.format(errorCode, arg0, arg1));
    }

    public static void handleException(String errorCode, Object arg0) throws OpenEJBException {
        throw new OpenEJBException(messages.format(errorCode, arg0));
    }

    public static void handleException(String errorCode) throws OpenEJBException {
        throw new OpenEJBException(messages.message(errorCode));
    }
}

class DeployedJar {

    EjbJar ejbJar;
    OpenejbJar openejbJar;
    String jarURI;

    public DeployedJar(String jar, EjbJar ejbJar, OpenejbJar openejbJar) {
        this.ejbJar = ejbJar;
        this.openejbJar = openejbJar;
        this.jarURI = jar;
    }
}
