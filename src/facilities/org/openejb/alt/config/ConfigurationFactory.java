package org.openejb.alt.config;

import java.io.*;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import org.openejb.OpenEJB;
import org.openejb.OpenEJBException;
import org.openejb.alt.assembler.classic.*;
import org.openejb.alt.config.ejb11.*;
import org.openejb.alt.config.sys.*;

/**
 * An implementation of the Classic Assembler's OpenEjbConfigurationFactory
 * interface. This implementation translates the user's config file and
 * deployed jars into the required InfoObject structure.
 *
 * This class doesn't do any configuring per se.  It just
 * reads in the config information for the assebler.
 */
public class ConfigurationFactory implements OpenEjbConfigurationFactory, ProviderDefaults{
    
    public static final String DEFAULT_SECURITY_ROLE = "openejb.default.security.role";

    Openejb openejb;
    DeployedJar[] jars;
    ServicesJar openejbDefaults = null;

    

    String configLocation = "";

    Vector deploymentIds = new Vector();
    Vector securityRoles = new Vector();
    Vector containerIds  = new Vector();

    Vector mthdPermInfos = new Vector();
    Vector mthdTranInfos = new Vector();
    Vector sRoleInfos    = new Vector();

    //------------------------------------------------//
    //
    //   I n f o   O b j e c t s
    //
    //------------------------------------------------//
    OpenEjbConfiguration sys;
    
    ContainerInfo[] cntrs;
    EntityContainerInfo[] entyCntrs;
    StatefulSessionContainerInfo[] stflCntrs;
    StatelessSessionContainerInfo[] stlsCntrs;
    
    /** Hash of container info objects for quick reference */
    HashMap containerTable = new HashMap();

    public void init(Properties props) throws OpenEJBException {
        if ( props == null ) props = new Properties();

        configLocation = props.getProperty("openejb.conf.file");
        if ( configLocation == null ) {
            configLocation = props.getProperty("openejb.configuration","conf/default.openejb.conf");
        }

        if ( configLocation == null ) {
            configLocation = ConfigUtils.searchForConfiguration();
        }
        
    }

    public static void main(String[] args){
        try{
        ConfigurationFactory conf = new ConfigurationFactory();
        conf.configLocation = args[0];        
        conf.init(null);
        OpenEjbConfiguration openejb = conf.getOpenEjbConfiguration();

        conf.printConf(openejb);
        } catch (Exception e){
            System.out.println("[OpenEJB] "+e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Read in the configuration information into the 
     * Openejb object
     * Validate it
     *
     * Read in each deployment object
     * Validate it
     *
     * Translate the whole thing into Info objects.
     * Return them.
     */
    public OpenEjbConfiguration getOpenEjbConfiguration() throws OpenEJBException {
        
        // Load configuration
        // Validate Configuration
        openejb = ConfigUtils.readConfig(configLocation);
        
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
        
        sys.containerSystem.containers          = cntrs;
        sys.containerSystem.entityContainers    = entyCntrs;
        sys.containerSystem.statefulContainers  = stflCntrs;
        sys.containerSystem.statelessContainers = stlsCntrs;

        for (int i=0; i < jars.length; i++){
            EnterpriseBeanInfo[] beans = initEnterpriseBeanInfos(jars[i]);
        }
        

        // Add the defaults
        SecurityRoleInfo defaultRole = new SecurityRoleInfo();
        defaultRole.description = "The role applied to recurity references that are not linked.";
        defaultRole.roleName    = DEFAULT_SECURITY_ROLE;
        sRoleInfos.add(defaultRole);


        // Collect Arrays
        sys.containerSystem.securityRoles      = new SecurityRoleInfo[sRoleInfos.size()];
        sys.containerSystem.methodPermissions  = new MethodPermissionInfo[mthdPermInfos.size()];
        sys.containerSystem.methodTransactions = new MethodTransactionInfo[mthdTranInfos.size()];

        sRoleInfos.copyInto( sys.containerSystem.securityRoles );
        mthdPermInfos.copyInto( sys.containerSystem.methodPermissions );
        mthdTranInfos.copyInto( sys.containerSystem.methodTransactions );
        
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
     * @exception OpenEJBException
     */
    private void initJndiProviders(Openejb openejb, FacilitiesInfo facilities) throws OpenEJBException{
        JndiProvider[] provider =  openejb.getJndiProvider();

        if (provider == null || provider.length < 1) return;

        JndiContextInfo[] ctxInfo = new JndiContextInfo[provider.length];
        facilities.remoteJndiContexts = ctxInfo;

        // Init each provider one by one
        for (int i=0; i < provider.length; i++){
            ServiceProvider service = ConfigUtils.getService(provider[i].getJar(), provider[i].getId());

			if (!service.getProviderType().equals("JndiProvider")) {
				handleException("conf.4902", provider[i].getId(), provider[i].getJar(), "JndiProvider");
			}
	
            ctxInfo[i] = new JndiContextInfo();

            ctxInfo[i].jndiContextId = provider[i].getId();

            // Verify the uniqueness of the ID
            if (jndiProviderIds.contains(provider[i].getId())) {
                handleException("conf.0103",configLocation,provider[i].getId());
            }
            
            jndiProviderIds.add(provider[i].getId());

            // Load the propterties file 
            ctxInfo[i].properties = ConfigUtils.assemblePropertiesFor("JndiProvider",
                                                                      provider[i].getId(),
                                                                      provider[i].getContent(),
                                                                      configLocation,
                                                                      provider[i].getJar(),
                                                                      service);
        }
    }
    
    private void initSecutityService(Openejb openejb, FacilitiesInfo facilities) throws OpenEJBException{
        SecurityService ss      = openejb.getSecurityService();
        SecurityServiceInfo ssi = new SecurityServiceInfo();
        ServiceProvider ssp     = ConfigUtils.getService(ss.getJar(), ss.getId());
        
		if (!ssp.getProviderType().equals("Security")) {
			handleException("conf.4902", ss.getId(), ss.getJar(), "Security");
		}

        ssi.codebase         = ss.getJar();
        ssi.description      = ssp.getDescription();
        ssi.displayName      = ssp.getDisplayName();
        ssi.factoryClassName = ssp.getClassName();
        ssi.serviceName      = ss.getId();
        ssi.properties       = ConfigUtils.assemblePropertiesFor("Security", ss.getId(),
                                                                 ss.getContent(),
                                                                 configLocation,ss.getJar(),
                                                                 ssp);
        SecurityRoleInfo[] roles = sys.containerSystem.securityRoles;
        RoleMappingInfo[] r = new RoleMappingInfo[roles.length];
        ssi.roleMappings    = r;

        // This is really a workaround, I'm simply giving
        // the physical role the same name as the logical 
        // role.  No security services have been integrated
        // with OpenEJB yet. The conecpt of having OpenEJB 
        // do the role linking for the security service has
        // never been put to the test, therefore, we are not 
        // going to worry about role mapping until a valid
        // security service is integrated.  At that time, we
        // can take the approach that makes the most sense.
        for (int i=0; i < r.length; i++){
            r[i] = new RoleMappingInfo();
            r[i].logicalRoleNames  = new String[]{ roles[i].roleName };
            r[i].physicalRoleNames = new String[]{ roles[i].roleName };
        }

        facilities.securityService = ssi;
    }
    
    private void initTransactionService(Openejb openejb, FacilitiesInfo facilities) throws OpenEJBException {
        TransactionService ts      = openejb.getTransactionService();
        TransactionServiceInfo tsi = new TransactionServiceInfo();
        ServiceProvider service    = ConfigUtils.getService(ts.getJar(), ts.getId());

		if (!service.getProviderType().equals("Transaction")) {
			handleException("conf.4902", ts.getId(), ts.getJar(), "Transaction");
		}

        tsi.codebase         = ts.getJar();
        tsi.description      = service.getDescription();
        tsi.displayName      = service.getDisplayName();
        tsi.factoryClassName = service.getClassName();
        tsi.serviceName      = ts.getId();
        tsi.properties       = ConfigUtils.assemblePropertiesFor("Transaction",
                                                                 ts.getId(),ts.getContent(),
                                                                 configLocation, ts.getJar(),
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
     * @exception OpenEJBException
     */
    private void initConnectors(Openejb openejb, FacilitiesInfo facilities) throws OpenEJBException{
        
        Connector[] conn =  openejb.getConnector();
        
        if (conn == null || conn.length < 1)  return;

        ConnectorInfo[] info = new ConnectorInfo[conn.length];
        facilities.connectors = info;

        // Init each conn one by one
        for (int i=0; i < conn.length; i++){
            ServiceProvider service = ConfigUtils.getService(conn[i].getJar(), conn[i].getId());

			if (!service.getProviderType().equals("Connector")) {
				handleException("conf.4902", conn[i].getId(), conn[i].getJar(), "Connector");
			}
				
			ManagedConnectionFactoryInfo factory = new ManagedConnectionFactoryInfo();

            info[i]                     = new ConnectorInfo();
            info[i].connectorId         = conn[i].getId();
            info[i].connectionManagerId = DEFAULT_LOCAL_TX_CON_MANAGER;
            info[i].managedConnectionFactory = factory;
                                               
            factory.id         = conn[i].getId();
            factory.className  = service.getClassName();
            factory.codebase   = conn[i].getJar();
            factory.properties = ConfigUtils.assemblePropertiesFor(
                                                    "Connector", conn[i].getId(),     
                                                    conn[i].getContent(),
                                                    configLocation, 
                                                    conn[i].getJar(), service);            

            // Verify the uniqueness of the ID
            if (connectorIds.contains(conn[i].getId())) {
                handleException("conf.0103",configLocation,conn[i].getId());
            }
            
            connectorIds.add(conn[i].getId());
        }
    }
    
    private void initConnectionManagers(Openejb openejb, FacilitiesInfo facilities) throws OpenEJBException{
            
        String defaultJar = ConfigUtils.OPENEJB_JAR_FILE.getPath();

        ConnectionManagerInfo manager = new ConnectionManagerInfo();
        ServiceProvider service = ConfigUtils.getService(defaultJar, DEFAULT_LOCAL_TX_CON_MANAGER);

        if (!service.getProviderType().equals("ConnectionManager")) {
                handleException("conf.4902", DEFAULT_LOCAL_TX_CON_MANAGER, defaultJar, "ConnectionManager");
        }

        manager.connectionManagerId = DEFAULT_LOCAL_TX_CON_MANAGER;
        manager.className           = service.getClassName();
        manager.codebase            = defaultJar;
        manager.properties          = ConfigUtils.assemblePropertiesFor(
                                                        "ConnectionManager", 
                                                        DEFAULT_LOCAL_TX_CON_MANAGER,
                                                        null, configLocation,
                                                        defaultJar, service);


        facilities.connectionManagers = new ConnectionManagerInfo[]{manager};
    }
    
    
    private void initProxyFactory(Openejb openejb, FacilitiesInfo facilities) throws OpenEJBException{
        
        ProxyFactory pf       = openejb.getProxyFactory();
        ServiceProvider pfp   = ConfigUtils.getService(pf.getJar(), pf.getId());
        

		if (!pfp.getProviderType().equals("Proxy")) {
			handleException("conf.4902", pf.getId(), pf.getJar(), "Proxy");
		}
		
		IntraVmServerInfo pfi = new IntraVmServerInfo();
        
        facilities.intraVmServer  = pfi; 
        pfi.proxyFactoryClassName = pfp.getClassName();

        pfi.factoryName      = pf.getId();
        pfi.codebase         = pf.getJar();
        pfi.properties       = ConfigUtils.assemblePropertiesFor("Proxy", pf.getId(),
                                                                 pf.getContent(),
                                                                 configLocation,pf.getJar(),
                                                                 pfp);
    }


    /**
     * Initializes the four container info arrays.
     *     
     *     ContainerInfo[] cntrs;
     *     EntityContainerInfo[] entyCntrs;
     *     StatefulSessionContainerInfo stflCntrs;
     *     StatelessSessionContainerInfo stlsCntrs;
     * 
     * @param conf
     */
    private void initContainerInfos(Openejb conf) throws OpenEJBException{
        Vector e  = new Vector();
        Vector sf = new Vector();
        Vector sl = new Vector();

        Enumeration enum = conf.enumerateContainer();
        while (enum.hasMoreElements()) {
            Container c             = (Container)enum.nextElement();
            ServiceProvider service = ConfigUtils.getService(c.getJar(), c.getId());
				
			if (!service.getProviderType().equals("Container")) {
				handleException("conf.4902", c.getId(), c.getJar(), "Container");
			}
			
			ContainerInfo ci        = null;

            if (c.getCtype().equals("STATELESS") ) {
                ci = new StatelessSessionContainerInfo();
                sl.add(ci);
            } else if (c.getCtype().equals("STATEFUL")) {
                ci = new StatefulSessionContainerInfo();
                sf.add(ci);
            } else {
                ci = new EntityContainerInfo();
                e.add(ci);
            }
            
            ci.ejbeans        = new EnterpriseBeanInfo[0];
            ci.containerName  = c.getId();
            ci.className      = service.getClassName();
            ci.codebase       = c.getJar();
            ci.properties     = ConfigUtils.assemblePropertiesFor(
                                        "Container", c.getId(), c.getContent(),
                                        configLocation, c.getJar(), service);
            
            //// Check if ID is a Duplicate /////
            if ( containerIds.contains(c.getId()) ){
                handleException("conf.0101",configLocation,c.getId());
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

        for (int i=0; i < cntrs.length; i++){
            containerTable.put(cntrs[i].containerName, cntrs[i]);
        }
    
    }
    

    private Map getDeployments(OpenejbJar j) throws OpenEJBException{
        HashMap map = new HashMap(j.getEjbDeploymentCount());
        Enumeration enum = j.enumerateEjbDeployment();
        
        while (enum.hasMoreElements()) {
            EjbDeployment d = (EjbDeployment)enum.nextElement();
            map.put( d.getEjbName(), d);
        }

        return map;
    }

    private EnterpriseBeanInfo[] initEnterpriseBeanInfos(DeployedJar jar)  throws OpenEJBException{
    
        int beansDeployed = jar.openejbJar.getEjbDeploymentCount();
        int beansInEjbJar = jar.ejbJar.getEnterpriseBeans().getEnterpriseBeansItemCount();

        if (beansInEjbJar != beansDeployed) {
            ConfigUtils.logWarning("conf.0008",jar.jarURI,""+beansInEjbJar, ""+beansDeployed);
            return new EnterpriseBeanInfo[0];
        }


        Map ejbds = getDeployments(jar.openejbJar);
        Map infos = new HashMap();
        Map items = new HashMap();
        EnterpriseBeanInfo[] beans = new EnterpriseBeanInfo[ejbds.size()];
        int i = -1;

        Enumeration bl = jar.ejbJar.getEnterpriseBeans().enumerateEnterpriseBeansItem();
        while ( bl.hasMoreElements() ) {
            EnterpriseBeansItem item = (EnterpriseBeansItem)bl.nextElement();
            i++;

            if ( item.getEntity() == null ) {
                beans[i] = initSessionBean(item, ejbds);
            } else {
                beans[i] = initEntityBean(item, ejbds);
            }
            
            // Check For Duplicate Deployment IDs
            if (deploymentIds.contains(beans[i].ejbDeploymentId)){
                ConfigUtils.logWarning("conf.0100",beans[i].ejbDeploymentId,jar.jarURI);
            } else {
                deploymentIds.add(beans[i].ejbDeploymentId);
            }
            
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
            for (int x=0; x < beans.length; x++){
                resolveRoleLinks( jar, beans[x], (EnterpriseBeansItem)items.get(beans[x].ejbName));
            }
        }
        
        assignBeansToContainers(beans, ejbds);
        
        return beans;
    }


    private void initJndiReferences(Map ejbds, Map infos, Map items) throws OpenEJBException{
        
        Iterator i = infos.values().iterator();
        while ( i.hasNext() ) {
            EnterpriseBeanInfo bean = (EnterpriseBeanInfo)i.next();
            EnterpriseBeansItem item = (EnterpriseBeansItem)items.get(bean.ejbName);
            Enumeration ee = null;
            Enumeration er = null;
            Enumeration rr = null;
            
            if ( item.getEntity() != null ) {
                ee = item.getEntity().enumerateEnvEntry();
                er = item.getEntity().enumerateEjbRef();
                rr = item.getEntity().enumerateResourceRef();
            } else {
                ee = item.getSession().enumerateEnvEntry();
                er = item.getSession().enumerateEjbRef();
                rr = item.getSession().enumerateResourceRef();
            }

            Vector envRef = new Vector();
            Vector ejbRef = new Vector();
            Vector resRef = new Vector();
            
            /* Build Environment entries *****************/
            while (ee.hasMoreElements()) {
                EnvEntry env = (EnvEntry)ee.nextElement();
                EnvEntryInfo info = new EnvEntryInfo();
                
                info.name  = env.getEnvEntryName();
                info.type  = env.getEnvEntryType();
                info.value = env.getEnvEntryValue();
                
                envRef.add(info);
            }
            
            /* Build EJB References **********************/
            while (er.hasMoreElements()) {
                EjbRef ejb = (EjbRef)er.nextElement();
                EjbReferenceInfo info = new EjbReferenceInfo();

                info.homeType      = ejb.getHome();
                info.referenceName = ejb.getEjbRefName();
                info.location = new EjbReferenceLocationInfo();

                EnterpriseBeanInfo otherBean  = (EnterpriseBeanInfo)infos.get(ejb.getEjbLink());
                info.location.ejbDeploymentId = otherBean.ejbDeploymentId;

                ejbRef.add(info);
            }

            /* Build Resource References *****************/
            EjbDeployment dep  = (EjbDeployment)ejbds.get(bean.ejbName);
            Enumeration rl = dep.enumerateResourceLink();
            Map resLinks = new HashMap();
            while (rl.hasMoreElements()) {
                ResourceLink link = (ResourceLink)rl.nextElement();
                resLinks.put(link.getResRefName(), link);
            }

            while (rr.hasMoreElements()) {
                ResourceRef res = (ResourceRef)rr.nextElement();
                ResourceReferenceInfo info = new ResourceReferenceInfo();

                info.referenceAuth = res.getResAuth();
                info.referenceName = res.getResRefName();
                info.referenceType = res.getResType();
                
                ResourceLink link = (ResourceLink)resLinks.get(res.getResRefName());
                info.resourceID    = link.getResId();
                
                resRef.add(info);
            }

            /*  Assign everything to the EnterpriseBeanInfo *****/
            JndiEncInfo jndi = new JndiEncInfo();
            jndi.envEntries     = new EnvEntryInfo[envRef.size()];
            jndi.ejbReferences  = new EjbReferenceInfo[ejbRef.size()];
            jndi.resourceRefs   = new ResourceReferenceInfo[resRef.size()];
            
            envRef.copyInto(jndi.envEntries);
            ejbRef.copyInto(jndi.ejbReferences);
            resRef.copyInto(jndi.resourceRefs);

            bean.jndiEnc = jndi;

        }

    }

    private void initMethodTransactions(DeployedJar jar, Map ejbds, Map infos, Map items) throws OpenEJBException{

        ContainerTransaction[] cTx = jar.ejbJar.getAssemblyDescriptor().getContainerTransaction();
        
        if (cTx == null || cTx.length < 1 ) return;

        MethodTransactionInfo[] mTxs = new MethodTransactionInfo[cTx.length];

        for (int i=0; i < mTxs.length; i++){ 
            mTxs[i] = new MethodTransactionInfo();

            mTxs[i].description    = cTx[i].getDescription();
            mTxs[i].transAttribute = cTx[i].getTransAttribute();
            mTxs[i].methods        = getMethodInfos(cTx[i].getMethod(), ejbds);
        }
        
        this.mthdTranInfos.addAll(Arrays.asList(mTxs));
    }

    private void initSecurityRoles(DeployedJar jar, Map ejbds, Map infos, Map items) throws OpenEJBException{


        SecurityRole[] sr = jar.ejbJar.getAssemblyDescriptor().getSecurityRole();
        
        if (sr == null || sr.length < 1 ) return;

        SecurityRoleInfo[] roles = new SecurityRoleInfo[sr.length];
        for (int i=0; i < roles.length; i++){
            roles[i] = new SecurityRoleInfo();

            roles[i].description = sr[i].getDescription();
            roles[i].roleName    = sr[i].getRoleName();

                // Check For Duplicate Container IDs
            if ( securityRoles.contains(sr[i].getRoleName()) ){
                ConfigUtils.logWarning("conf.0102",jar.jarURI,sr[i].getRoleName());
            } else {
                securityRoles.add(sr[i].getRoleName());
            }
        }

        this.sRoleInfos.addAll(Arrays.asList(roles));
    }
    
    private void initMethodPermissions(DeployedJar jar, Map ejbds, Map infos, Map items) throws OpenEJBException{

        MethodPermission[] mp = jar.ejbJar.getAssemblyDescriptor().getMethodPermission();
        if (mp == null || mp.length < 1) return;
        
        MethodPermissionInfo[] perms = new MethodPermissionInfo[mp.length];
        
        for (int i=0; i < perms.length; i++){
            perms[i] = new MethodPermissionInfo();

            perms[i].description = mp[i].getDescription();
            perms[i].roleNames   = mp[i].getRoleName();
            perms[i].methods     = getMethodInfos(mp[i].getMethod(), ejbds);
        }
        
        this.mthdPermInfos.addAll(Arrays.asList(perms));
    }

    /**
     * Verify that everything has been linked and 
     * that those links refer to actual declared roles.
     * 
     * @param bean
     * @exception OpenEJBException
     */
    private void resolveRoleLinks(DeployedJar jar, EnterpriseBeanInfo bean, EnterpriseBeansItem item) throws OpenEJBException{
        SecurityRoleRef[] refs = null;
        if (item.getEntity() != null ) {
            refs = item.getEntity().getSecurityRoleRef();
        } else {
            refs = item.getSession().getSecurityRoleRef();
        }

        if (refs == null || refs.length < 1 ) return;
        
        SecurityRoleReferenceInfo[] sr = new  SecurityRoleReferenceInfo[refs.length];
        bean.securityRoleReferences = sr;

        for (int i=0; i < sr.length; i++){
            sr[i] = new SecurityRoleReferenceInfo();

            sr[i].description = refs[i].getDescription();
            sr[i].roleLink    = refs[i].getRoleLink();
            sr[i].roleName    = refs[i].getRoleName();

            if ( sr[i].roleLink == null ) {
                ConfigUtils.logWarning("conf.0009", sr[i].roleName, bean.ejbName, jar.jarURI);
                sr[i].roleLink = DEFAULT_SECURITY_ROLE;
            }
        }
        
    }

    private MethodInfo[] getMethodInfos(Method[] ms, Map ejbds){
        if (ms == null) return null;

        MethodInfo[] mi = new MethodInfo[ms.length];
        for (int i=0; i < mi.length; i++){

            mi[i] = new MethodInfo();

            EjbDeployment d = (EjbDeployment)ejbds.get(ms[i].getEjbName());

            mi[i].description     = ms[i].getDescription();
            mi[i].ejbDeploymentId = d.getDeploymentId();
            mi[i].methodIntf      = ms[i].getMethodIntf();
            mi[i].methodName      = ms[i].getMethodName();

            // get the method parameters
            MethodParams mp = ms[i].getMethodParams();
            if (mp != null ) {
                mi[i].methodParams = mp.getMethodParam();
            }
        }

        return mi;
    }
    
    private EnterpriseBeanInfo initSessionBean(EnterpriseBeansItem item, Map m) throws OpenEJBException{
        Session s = item.getSession();
        EnterpriseBeanInfo bean = null;
        
        if (s.getSessionType().equals("Stateful"))
             bean = new StatefulBeanInfo();
        else bean = new StatelessBeanInfo();

        EjbDeployment d = (EjbDeployment)m.get(s.getEjbName());
        bean.ejbDeploymentId = d.getDeploymentId();
        

        bean.description     = s.getDescription();
        bean.largeIcon       = s.getLargeIcon();
        bean.smallIcon       = s.getSmallIcon();
        bean.displayName     = s.getDisplayName();
        bean.ejbClass        = s.getEjbClass();
        bean.ejbName         = s.getEjbName();
        bean.home            = s.getHome();
        bean.remote          = s.getRemote();
        bean.transactionType = s.getTransactionType();


        return bean;
    }
    
    private EnterpriseBeanInfo initEntityBean(EnterpriseBeansItem item, Map m) throws OpenEJBException{
        Entity e = item.getEntity();
        EntityBeanInfo bean = new EntityBeanInfo();
        
        EjbDeployment d = (EjbDeployment)m.get(e.getEjbName());
        bean.ejbDeploymentId = d.getDeploymentId();
        

        bean.description     = e.getDescription();
        bean.largeIcon       = e.getLargeIcon();
        bean.smallIcon       = e.getSmallIcon();
        bean.displayName     = e.getDisplayName();
        bean.ejbClass        = e.getEjbClass();
        bean.ejbName         = e.getEjbName();
        bean.home            = e.getHome();
        bean.remote          = e.getRemote();
        bean.transactionType = "Container";
        
        bean.primKeyClass    = e.getPrimKeyClass();
        bean.primKeyField    = e.getPrimkeyField();
        bean.persistenceType = e.getPersistenceType();
        bean.reentrant       = e.getReentrant()+"";
        
        bean.cmpFieldNames = new String[e.getCmpFieldCount()];
        
        for (int i=0; i < bean.cmpFieldNames.length; i++){ 
            bean.cmpFieldNames[i] = e.getCmpField(i).getFieldName();
        }
        
        return bean;
    }
    
    private void assignBeansToContainers(EnterpriseBeanInfo[] beans, Map ejbds){
        
        for (int i=0; i < beans.length; i++){
            // Get the bean deployment object
            EjbDeployment d = (EjbDeployment)ejbds.get(beans[i].ejbName);

            // Get the container it was assigned to
            ContainerInfo cInfo = (ContainerInfo)containerTable.get(d.getContainerId());
            
            // Add the bean info object to the cotnainer's bean array
            EnterpriseBeanInfo[] oldList = cInfo.ejbeans;
            EnterpriseBeanInfo[] newList = new EnterpriseBeanInfo[oldList.length + 1];
            System.arraycopy(oldList,0,newList,1,oldList.length);
            newList[0] = beans[i];
            cInfo.ejbeans = newList;
        }

        // Now create the bean arrays of the specific container
        // type.
        for (int i=0; i < entyCntrs.length; i++){
            EnterpriseBeanInfo[] b = entyCntrs[i].ejbeans;
            EntityBeanInfo[] eb = new EntityBeanInfo[b.length];
            System.arraycopy(b,0,eb,0,b.length);
        }

    }

    /**
     * Resolve file locations
     * Resolve classes
     * TODO: Not integral now, implement later.
     */
    private void resolveDependencies(Openejb openejb){

    }
    /**
     * Resolve file locations
     * Resolve classes
     * TODO: Not integral now, implement later.
     */
    private void resolveDependencies(EjbJar[] jars){

    }

    /** Loads a list of jar names that we will attempt
     * to deploy.
     * If the Deployments element is a directory
     * then it will load all the jars from that directory.
     * 
     * If a jar was listed twice in the config file for some
     * reason, it only occur once in the list returned
     * 
     */
    private String[] getJarLocations(Deployments[] deploy) {
            
        Vector jarList = new Vector(deploy.length);
        
        try {
        
        for (int i=0; i < deploy.length; i++){
                
            Deployments d = deploy[i];
            
            ///// Add Jar file  /////
            if ( d.getDir() == null && d.getJar() != null ) {
                File jar = new File(d.getJar());
                if ( !jarList.contains(jar.getAbsolutePath()) ) {
                    jarList.add( jar.getAbsolutePath() );
                }
            
                continue;                    
            }
                    
            ///// A directory /////
                    
            File dir = new File( d.getDir() );
                    
            if ( !dir.isDirectory() ) continue; // Opps! Not a directory
    
            String[] files = dir.list();
                    
            for (int x=0; x < files.length; x++){
                        
                String f = files[x];
                        
                if (  !f.endsWith(".jar")  ) continue;
                            
                //// Found a jar in the dir ////
                            
                File jar = new File( dir, f );
                            
                if ( jarList.contains( jar.getAbsolutePath() ) ) continue;
                jarList.add( jar.getAbsolutePath() );
                            
            }

        }
        } catch (SecurityException se){
            //Worthless security exception
            // log it and move on
            // TODO:  Log this
        }
        
        String[] locations = new String[jarList.size()];
        jarList.copyInto( locations );

        return locations;

    }

    /**
     * Resolve file locations
     * Resolve classes
     * TODO: Not integral now, implement later.
     */
    private DeployedJar[] loadDeployments(Openejb openejb) throws OpenEJBException{
        
        Vector jarsVect = new Vector();

        String[] jarsToLoad = getJarLocations(openejb.getDeployments());

        /*[1]  Put all EjbJar & OpenejbJar objects in a vector ***************/
        for (int i=0; i < jarsToLoad.length; i++){    
            
            String jarLocation = jarsToLoad[i];
            
            try{
            EjbJar ejbJar = ConfigUtils.readEjbJar(jarLocation);
            /* If there is no openejb-jar.xml an exception
             * will be thrown.
             * TODO: This shouldn't cause such a problem.  If
             * a jar in the path has not yet been deployed we could
             * attempt to auto deploy it. 
             */
            OpenejbJar openejbJar = ConfigUtils.readOpenejbJar(jarLocation);
            
            /* Add it to the Vector ***************/
            jarsVect.add(new DeployedJar(jarLocation, ejbJar, openejbJar));
            } catch (OpenEJBException e){
                ConfigUtils.logWarning("conf.0004",jarLocation, e.getMessage());
            }
        }
        
        /*[2]  Get a DeployedJar array from the vector ***************/
        DeployedJar[] jars = new DeployedJar[jarsVect.size()];
        jarsVect.copyInto(jars);
        return jars;
    }

    String[] tabs = {""," ","    ","      ","        ","          "};
    private void printConf(OpenEjbConfiguration conf){
        out(0,"CONFIGURATION");
        
        out(1,conf.containerSystem.containers.length);
        for (int i=0; i < conf.containerSystem.containers.length; i++){
            out(1,"className    ",conf.containerSystem.containers[i].className);
            out(1,"codebase     ",conf.containerSystem.containers[i].codebase);
            out(1,"containerName",conf.containerSystem.containers[i].containerName);
            out(1,"containerType",conf.containerSystem.containers[i].containerType);
            out(1,"description  ",conf.containerSystem.containers[i].description);
            out(1,"displayName  ",conf.containerSystem.containers[i].displayName);
            out(1,"properties   ");
            conf.containerSystem.containers[i].properties.list(System.out);
            out(1,"ejbeans      ",conf.containerSystem.containers[i].ejbeans.length);
            for (int j=0; j < conf.containerSystem.containers[i].ejbeans.length; j++){
                EnterpriseBeanInfo bean = conf.containerSystem.containers[i].ejbeans[j];
                out(2,"codebase       " ,bean.codebase );
                out(2,"description    " ,bean.description );
                out(2,"displayName    " ,bean.displayName );
                out(2,"ejbClass       " ,bean.ejbClass );
                out(2,"ejbDeploymentId" ,bean.ejbDeploymentId );
                out(2,"ejbName        " ,bean.ejbName );
                out(2,"home           " ,bean.home );
                out(2,"largeIcon      " ,bean.largeIcon );
                out(2,"remote         " ,bean.remote );
                out(2,"smallIcon      " ,bean.smallIcon );
                out(2,"transactionType" ,bean.transactionType );
                out(2,"type           " ,bean.type );
                out(2,"jndiEnc        " ,bean.jndiEnc );
                out(2,"envEntries     " ,bean.jndiEnc.envEntries.length );
                for (int n=0; n < bean.jndiEnc.envEntries.length; n++){                                
                    out(3,"--["+n+"]----------------------");
                    out(3,"name  " ,bean.jndiEnc.envEntries[n].name );
                    out(3,"type  " ,bean.jndiEnc.envEntries[n].type );
                    out(3,"value " ,bean.jndiEnc.envEntries[n].value );
                }
                out(2,"ejbReferences  " ,bean.jndiEnc.ejbReferences.length );
                for (int n=0; n < bean.jndiEnc.ejbReferences.length; n++){                                
                    out(3,"--["+n+"]----------------------");
                    out(3,"homeType        " ,bean.jndiEnc.ejbReferences[n].homeType );
                    out(3,"referenceName   " ,bean.jndiEnc.ejbReferences[n].referenceName );
                    out(3,"location        " ,bean.jndiEnc.ejbReferences[n].location );
                    out(3,"ejbDeploymentId " ,bean.jndiEnc.ejbReferences[n].location.ejbDeploymentId );
                    out(3,"jndiContextId   " ,bean.jndiEnc.ejbReferences[n].location.jndiContextId );
                    out(3,"remote          " ,bean.jndiEnc.ejbReferences[n].location.remote );
                    out(3,"remoteRefName   " ,bean.jndiEnc.ejbReferences[n].location.remoteRefName );
                }
                out(2,"resourceRefs   " ,bean.jndiEnc.resourceRefs.length );
                for (int n=0; n < bean.jndiEnc.resourceRefs.length; n++){                                
                    out(3,"--["+n+"]----------------------");
                    out(3,"referenceAuth   " ,bean.jndiEnc.resourceRefs[n].referenceAuth );
                    out(3,"referenceName   " ,bean.jndiEnc.resourceRefs[n].referenceName );
                    out(3,"referenceType   " ,bean.jndiEnc.resourceRefs[n].referenceType );
                    if (bean.jndiEnc.resourceRefs[n].location != null) {
                    out(3,"location        " ,bean.jndiEnc.resourceRefs[n].location );
                    out(3,"jndiContextId   " ,bean.jndiEnc.resourceRefs[n].location.jndiContextId );
                    out(3,"remote          " ,bean.jndiEnc.resourceRefs[n].location.remote );
                    out(3,"remoteRefName   " ,bean.jndiEnc.resourceRefs[n].location.remoteRefName );
                    }
                }
            }
        }

        if ( conf.containerSystem.securityRoles != null) {
            out(0,"--Security Roles------------");
            for (int i=0; i < sys.containerSystem.securityRoles.length; i++){
                out(1,"--["+i+"]----------------------");
                out(1,"            ", sys.containerSystem.securityRoles[i]);
                out(1,"description ", sys.containerSystem.securityRoles[i].description);
                out(1,"roleName    ", sys.containerSystem.securityRoles[i].roleName);
            }
        }

        if ( conf.containerSystem.methodPermissions != null) {
            out(0,"--Method Permissions--------");
            for (int i=0; i < sys.containerSystem.methodPermissions.length; i++){
                out(1,"--["+i+"]----------------------");
                out(1,"            ", sys.containerSystem.methodPermissions[i]);
                out(1,"description ", sys.containerSystem.methodPermissions[i].description);
                out(1,"roleNames   ", sys.containerSystem.methodPermissions[i].roleNames);
                if (sys.containerSystem.methodPermissions[i].roleNames != null){
                    for (int r=0; r < sys.containerSystem.methodPermissions[i].roleNames.length; r++){
                        out(1,"roleName["+r+"]   ",sys.containerSystem.methodPermissions[i].roleNames[r]);
                    }
                }
                out(1,"methods     ", conf.containerSystem.methodPermissions[i].methods);
                if (conf.containerSystem.methodPermissions[i].methods != null) {
                    MethodInfo[] mthds = conf.containerSystem.methodPermissions[i].methods;
                    for (int j=0; j < mthds.length; j++){
                        out(2,"description    ",mthds[j].description     );
                        out(2,"ejbDeploymentId",mthds[j].ejbDeploymentId );
                        out(2,"methodIntf     ",mthds[j].methodIntf      );
                        out(2,"methodName     ",mthds[j].methodName      );
                        out(2,"methodParams   ",mthds[j].methodParams    );
                        if (mthds[j].methodParams != null) {
                            for (int n=0; n < mthds[j].methodParams.length; n++){
                                out(3,"param["+n+"]", mthds[j].methodParams[n]);
                            }
                        }

                    }
                }
            }
        }

        if ( conf.containerSystem.methodTransactions != null) {
            out(0,"--Method Transactions-------");
            for (int i=0; i < conf.containerSystem.methodTransactions.length; i++){
                
                out(1,"--["+i+"]----------------------");
                out(1,"               ", conf.containerSystem.methodTransactions[i]);
                out(1,"description    ", conf.containerSystem.methodTransactions[i].description);
                out(1,"transAttribute ", conf.containerSystem.methodTransactions[i].transAttribute);
                out(1,"methods        ", conf.containerSystem.methodTransactions[i].methods);
                if (conf.containerSystem.methodTransactions[i].methods != null) {
                    MethodInfo[] mthds = conf.containerSystem.methodTransactions[i].methods;
                    for (int j=0; j < mthds.length; j++){
                        out(2,"description    ",mthds[j].description     );
                        out(2,"ejbDeploymentId",mthds[j].ejbDeploymentId );
                        out(2,"methodIntf     ",mthds[j].methodIntf      );
                        out(2,"methodName     ",mthds[j].methodName      );
                        out(2,"methodParams   ",mthds[j].methodParams    );
                        if (mthds[j].methodParams != null) {
                            for (int n=0; n < mthds[j].methodParams.length; n++){
                                out(3,"param["+n+"]", mthds[j].methodParams[n]);
                            }
                        }

                    }
                }
            }
        }
    }
    private void out(int t, String m){
        System.out.println(tabs[t]+m);
    }
    
    private void out(int t, String m, String n){
        System.out.println(tabs[t]+m+" = "+n);
    }
    private void out(int t, String m, boolean n){
        System.out.println(tabs[t]+m+" = "+n);
    }
    private void out(int t, String m, int n){
        System.out.println(tabs[t]+m+" = "+n);
    }
    private void out(int t, String m, Object n){
        System.out.println(tabs[t]+m+" = "+n);
    }
    private void out(int t, int m){
        System.out.println(tabs[t]+m);
    }

    /*------------------------------------------------------*/
    /*    Methods for easy exception handling               */
    /*------------------------------------------------------*/
	public static void handleException(String errorCode, Object arg0, Object arg1, Object arg2, Object arg3) throws OpenEJBException {
		Object[] args = { arg0, arg1, arg2, arg3};
		throw new OpenEJBException(errorCode, args);
	}
	
	public static void handleException(String errorCode, Object arg0, Object arg1, Object arg2) throws OpenEJBException {
		Object[] args = { arg0, arg1, arg2};
		throw new OpenEJBException(errorCode, args);
	}
	
	public static void handleException(String errorCode, Object arg0, Object arg1) throws OpenEJBException {
		Object[] args = { arg0, arg1};
		throw new OpenEJBException(errorCode, args);
	}
	
	public static void handleException(String errorCode, Object arg0) throws OpenEJBException {
		Object[] args = { arg0};
		throw new OpenEJBException(errorCode, args);
	}
	
	public static void handleException(String errorCode) throws OpenEJBException {
		throw new OpenEJBException(errorCode);
	}
}

class DeployedJar {

    EjbJar ejbJar;
    OpenejbJar openejbJar;
    String jarURI;


    public DeployedJar(String jar, EjbJar ejbJar, OpenejbJar openejbJar){
        this.ejbJar = ejbJar;
        this.openejbJar = openejbJar;
        this.jarURI = jar;
    }
}
