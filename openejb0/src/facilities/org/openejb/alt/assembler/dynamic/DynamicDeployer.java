package org.openejb.alt.assembler.dynamic;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.lang.reflect.*;
import java.lang.reflect.Method;
import javax.naming.NamingException;
import javax.ejb.*;
import org.openejb.spi.*;
import org.openejb.*;
import org.openejb.core.ivm.naming.*;
import org.openejb.core.ContainerSystem;
import org.openejb.core.stateless.StatelessContainer;
import org.openejb.core.entity.EntityContainer;
import org.openejb.core.stateful.StatefulContainer;
import org.openejb.util.*;
import org.openejb.alt.config.ejb11.*;
import org.openejb.alt.config.ejb11.EnterpriseBean;
import org.openejb.alt.assembler.classic.ContainerInfo;

/**
 * Knows how to deploy, undeploy, and redeploy container systems at runtime.
 *
 * Note: In order for this to work right, each EJB JAR must have both a
 * META-INF/ejb-jar.xml file and a META-INF/openejb-jar.xml file, and both
 * files must list exactly the same beans (by EJB Name).  Also, additional
 * JARs used by EJB JARs should not have EJB DDs.  Otherwise, deployment
 * exceptions will likely ensue.
 *
 * Note: Nothing is deployed during the server startup process.  At the
 * very end, startDeploying should be called on this, at which point any
 * hardwired JARs will be deployed in a single application.
 *
 * @version $Revision$
 */
public class DynamicDeployer implements Deployer {
    // Class Vars
    private final static String DEPLOY_DIRS = "ejb.deploy.dirs";
    private final static String DEFAULT_STATELESS = "ejb.deploy.default.stateless";
    private final static String DEFAULT_STATEFUL = "ejb.deploy.default.stateful";
    private final static String DEFAULT_BMP = "ejb.deploy.default.bmp";
    private final static String DEFAULT_CMP = "ejb.deploy.default.cmp";
    private final static String EXTRACT_INTERFACES = "ejb.deploy.extract.interfaces";
    private final static String ARCHIVE_TEMP_DIR = "tmp/deploy/cache";
    private final static String J2CA_TEMP_DIR = "tmp/deploy/rar";
    private final static SimpleDateFormat sdf = new SimpleDateFormat("MM/dd hh:mm aa");
    private final static Logger log = Logger.getInstance("OpenEJB.dynamic.deploy", "OpenEJB.dynamic.deploy"); //todo: resource bundle
    private final static Method OBJECT_REMOVE_METHOD;
    private final static Method HOME_REMOVE_METHOD_1;
    private final static Method HOME_REMOVE_METHOD_2;
    static {
        try {
            OBJECT_REMOVE_METHOD = EJBObject.class.getMethod("remove", new Class[0]);
            HOME_REMOVE_METHOD_1 = EJBHome.class.getMethod("remove", new Class[]{Object.class});
            HOME_REMOVE_METHOD_2 = EJBHome.class.getMethod("remove", new Class[]{Handle.class});
        } catch(Exception e) {
            log.error("Unable to look up remove method", e);
            throw new RuntimeException("Unable to look up remove method! "+e.getMessage());
        }
    }

    // Instance Vars
    private List pendingDeployments = new ArrayList();
    private String[] deployDirs;
    private Map defaultContainers = new HashMap();
    private SecurityService defaultSecurityService;
    private boolean extractInterfaces = false;
    private String defaultStateless = "Default Stateless Container";
    private String defaultStateful = "Default Stateful Container";
    private String defaultCMP = "Default BMP Container";
    private String defaultBMP = "Default CMP Container";
    private Object lock = new Object();
    private Map deployments = new HashMap();
    private Map defaultRemoteContexts;
    private Map defaultConnectors;

    public DynamicDeployer() {
    }

    /**
     * Loads config properties for this deployer.  In this case, checks whether
     * this deployer should pull out interface classes and load them into the
     * current ClassLoader, looks up default container IDs for each bean type,
     * and checks for directories to watch for future deployments:
     * <ul>
     *   <li>ejb.deploy.dirs</li>
     *   <li>ejb.deploy.default.stateless</li>
     *   <li>ejb.deploy.default.stateful</li>
     *   <li>ejb.deploy.default.bmp</li>
     *   <li>ejb.deploy.default.cmp</li>
     *   <li>ejb.deploy.extract.interfaces</li>
     * </ul>
     */
    public void init(Properties props) {
        String s = props.getProperty(DEPLOY_DIRS);
        if(s != null && !s.equals("")) {
            StringTokenizer tok = new StringTokenizer(s, ",", false);
            deployDirs = new String[tok.countTokens()];
            for(int i=0; tok.hasMoreElements(); deployDirs[i++] = (String)tok.nextElement());
        }
        s = props.getProperty(DEFAULT_BMP);
        if(s != null && !s.equals("")) {
            defaultBMP = s;
        }
        s = props.getProperty(DEFAULT_CMP);
        if(s != null && !s.equals("")) {
            defaultCMP = s;
        }
        s = props.getProperty(DEFAULT_STATEFUL);
        if(s != null && !s.equals("")) {
            defaultStateful = s;
        }
        s = props.getProperty(DEFAULT_STATELESS);
        if(s != null && !s.equals("")) {
            defaultStateless = s;
        }
        s = props.getProperty(EXTRACT_INTERFACES);
        if(s != null && !s.equals("")) {
            extractInterfaces = new Boolean(props.getProperty(EXTRACT_INTERFACES)).booleanValue();
        }
    }

    // ---------- Deployer interface methods ---------------------------------------

    public String[] getDeployedApplications() {
        return OpenEJB.getContainerSystemIDs();
    }

    public void startDeploying() {
        try {
        URL[] u = new URL[pendingDeployments.size()];
        log.info("Deploying "+u.length+" JARs in the default application");
        int i=0;
        for(Iterator it = pendingDeployments.iterator(); it.hasNext();) {
            u[i++] = (URL)it.next();
        }
        try {
            deployApplication(DynamicAssembler.DEFAULT_CONTAINER_SYSTEM, u);
        } catch(DeploymentException e) {
            log.error("Unable to deploy application at startup", e);
        }
        log.info("Finished deploying default application");
        } catch(Exception e) {e.printStackTrace();}
    }

    public void deployApplication(String name, ClassLoader ejbJars) throws DeploymentException {
        log.debug("Requested to deploy application "+name+" from a CL");
        if(OpenEJB.getContainerSystem(name) != null) {
            throw new DeploymentException("Application " + name + " is already deployed!");
        }
        DynamicDeploymentData data = readDeploymentDescriptors(ejbJars);
        data.application = name;
        String[] interfaces = getJ2eeInterfaces(data);
        if(extractInterfaces) {
            try {
                File dir = FileUtils.getBase().getDirectory(ARCHIVE_TEMP_DIR);
                File client = File.createTempFile(name+"-client", ".jar", dir);
                client.deleteOnExit();
                ClassInspector.createClientJar(ejbJars, client, interfaces, true);
                ClasspathUtils.addJarToPath(client);
                data.interfaceFile = client;
            } catch(IOException e) {
                throw new DeploymentException("Unable to extract interfaces for application: "+e.getMessage());
            } catch(Exception e) {
                throw new DeploymentException("Unable to add interface JAR to CLASSPATH: " + e.getMessage());
            }
        }
        ContainerSystem cs = constructContainerSystem(data);
        synchronized(lock) {
            if(OpenEJB.getContainerSystem(name) != null) {
                throw new DeploymentException("Application "+name+" is already deployed!");
            }
            OpenEJB.setContainerSystem(name, cs);
            deployments.put(name, data);
        }
    }

    public void deployApplication(String name, URL[] ejbJars) throws DeploymentException {
        log.debug("Requested to deploy application " + name + " from "+ejbJars.length+" JARs");
        if(OpenEJB.getContainerSystem(name) != null) {
            throw new DeploymentException("Application " + name + " is already deployed!");
        }
        File dir = null;
        try {
            dir = FileUtils.getBase().getDirectory(ARCHIVE_TEMP_DIR);
        } catch(IOException e) {
            throw new DeploymentException("Unable to create temporary deployment directory "+ ARCHIVE_TEMP_DIR);
        }
        File[] files = download(dir, ejbJars);
        DynamicDeploymentData data = readDeploymentDescriptors(files);
        data.application = name;
        String[] interfaces = getJ2eeInterfaces(data);
        if(extractInterfaces) {
            try {
                File client = File.createTempFile(name + "-client", ".jar", dir);
                client.deleteOnExit();
                ClassInspector.createClientJar(files, client, interfaces, true);
                ClasspathUtils.addJarToPath(client);
                data.interfaceFile = client;
            } catch(IOException e) {
                throw new DeploymentException("Unable to extract interfaces for application: " + e.getMessage());
            } catch(Exception e) {
                throw new DeploymentException("Unable to add interface JAR to CLASSPATH: " + e.getMessage());
            }
        }
        ContainerSystem cs = constructContainerSystem(data);
        synchronized(lock) {
            if(OpenEJB.getContainerSystem(name) != null) {
                throw new DeploymentException("Application " + name + " is already deployed!");
            }
            OpenEJB.setContainerSystem(name, cs);
            deployments.put(name, data);
        }
    }

    public void redeployApplication(String name, ClassLoader ejbJars) throws DeploymentException {
        log.debug("Requested to redeploy application " + name + " from a CL");
        if(OpenEJB.getContainerSystem(name) == null) {
            throw new DeploymentException("Application " + name + " is not deployed!");
        }
        DynamicDeploymentData data = readDeploymentDescriptors(ejbJars);
        data.application = name;
        if(extractInterfaces) {
            synchronized(lock) {
                if(OpenEJB.getContainerSystem(name) == null) {
                    throw new DeploymentException("Application " + name + " is not deployed!");
                }
                data.interfaceFile = ((DynamicDeploymentData)deployments.get(name)).interfaceFile;
                log.info("Using interface JAR originally extracted at "+sdf.format(new Date(data.interfaceFile.lastModified())));
            }
        }
        ContainerSystem cs = constructContainerSystem(data);
        synchronized(lock) {
            if(OpenEJB.getContainerSystem(name) == null) {
                throw new DeploymentException("Application " + name + " is not deployed!");
            }
            OpenEJB.setContainerSystem(name, cs);
            deployments.put(name, data);
        }
    }

    public void redeployApplication(String name, URL[] ejbJars) throws DeploymentException {
        log.debug("Requested to redeploy application " + name + " from " + ejbJars.length + " JARs");
        if(OpenEJB.getContainerSystem(name) == null) {
            throw new DeploymentException("Application " + name + " is not deployed!");
        }
        File dir = null;
        try {
            dir = FileUtils.getBase().getDirectory(ARCHIVE_TEMP_DIR);
        } catch(IOException e) {
            throw new DeploymentException("Unable to create temporary deployment directory " + ARCHIVE_TEMP_DIR);
        }
        File[] files = download(dir, ejbJars);
        DynamicDeploymentData data = readDeploymentDescriptors(files);
        data.application = name;
        if(extractInterfaces) {
            synchronized(lock) {
                if(OpenEJB.getContainerSystem(name) == null) {
                    throw new DeploymentException("Application " + name + " is not deployed!");
                }
                data.interfaceFile = ((DynamicDeploymentData)deployments.get(name)).interfaceFile;
                log.info("Using interface JAR originally extracted at " + sdf.format(new Date(data.interfaceFile.lastModified())));
            }
        }
        ContainerSystem cs = constructContainerSystem(data);
        synchronized(lock) {
            if(OpenEJB.getContainerSystem(name) == null) {
                throw new DeploymentException("Application " + name + " is not deployed!");
            }
            OpenEJB.setContainerSystem(name, cs);
            deployments.put(name, data);
        }
    }

    public void undeployApplication(String name) throws DeploymentException {
        System.out.println("Requested to undeploy application " + name);
        synchronized(lock) {
            if(OpenEJB.getContainerSystem(name) == null) {
                throw new DeploymentException("Application " + name + " is not deployed!");
            }
            OpenEJB.removeContainerSystem(name);
            DynamicDeploymentData data = (DynamicDeploymentData)deployments.remove(name);
            if(data.interfaceFile != null) {
                if(!data.interfaceFile.delete()) {
                    log.warning("Unable to delete unused interface JAR "+data.interfaceFile.getAbsolutePath());
                }
            }
        }
    }

    // ---------- Additional public methods ---------------------------------------

    public SecurityService getDefaultSecurityService() {
        return defaultSecurityService;
    }

    public void setDefaultSecurityService(SecurityService defaultSecurityService) {
        this.defaultSecurityService = defaultSecurityService;
    }

    public Map getDefaultRemoteContexts() {
        return defaultRemoteContexts;
    }

    public void setDefaultRemoteContexts(Map defaultRemoteContexts) {
        this.defaultRemoteContexts = defaultRemoteContexts;
    }

    public Map getDefaultConnectors() {
        return defaultConnectors;
    }

    public void setDefaultConnectors(Map defaultConnectors) {
        this.defaultConnectors = defaultConnectors;
    }

    /**
     * Queues a JAR file to be deployed when the deployer starts deploying.
     */
    public void addPendingDeployment(File ejbJar) {
        try {
            pendingDeployments.add(ejbJar.toURL());
        } catch(MalformedURLException e) {
            throw new IllegalArgumentException("Unable to deploy file: " + e.getMessage());
        }
    }

    /**
     * Queues a JAR file to be deployed when the deployer starts deploying.
     */
    public void addPendingDeployment(URL ejbJar) {
        pendingDeployments.add(ejbJar);
    }

    /**
     * The number of EJB JARs that will be deployed once deployment starts.
     */
    public int getPendingDeploymentCount() {
        return pendingDeployments.size();
    }

    /**
     * Gives this deployer a list of directories it should watch for
     * deployments.
     */
    public void setDeployDirs(String[] deployDirs) {
        this.deployDirs = deployDirs;
    }

    /**
     * Gives this deployer a list of the containers configured at the OpenEJB
     * level.  EJB JARs may use those or may define their own.
     */
    public void setDefaultContainers(Map defaultContainers) {
        this.defaultContainers = defaultContainers;
    }

    /**
     * If a server wants total control over the deployment process, it can
     * manually assemble the DynamicDeploymentData for a deployment and
     * use this helper method to create an OpenEJB ContainerSystem.  Then it
     * can call OpenEJB.setContainerSystem() to enable the ContainerSystem.
     *
     * @see org.openejb.OpenEJB#setContainerSystem
     */
    public ContainerSystem constructContainerSystem(DynamicDeploymentData data) throws DeploymentException {
        org.openejb.core.ContainerSystem system = new org.openejb.core.ContainerSystem(data.application);
        if(data.openejbDD.length != data.standardDD.length) {
            throw new DeploymentException("There is not exactly one OpenEJB openejb-jar.xml ("+data.openejbDD.length+") for every EJB ejb-jar.xml ("+data.standardDD.length+")");
        }
        //todo: validate deployments (current validator doesn't handle class loaders properly)
        // Pre-visit each EJB to build a list of EJB IDs so we can resolve EJB refs
        Map allDeployments = new HashMap();
        Map allEJBs = new HashMap();
        for(int i = 0; i < data.openejbDD.length; i++) {
            EjbDeployment[] deps = data.openejbDD[i].getEjbDeployment();
            for(int j = 0; j < deps.length; j++) {
                allEJBs.put(deps[j].getEjbName(), deps[j].getDeploymentId());
            }
        }
        Map containers = new HashMap();
        Map containerInfos = new HashMap();
        Map containerDeployments = new HashMap();
        // March through each EJB JAR
        for(int i=0; i<data.openejbDD.length; i++) {
            // Step 1: Create any custom Containers
            if(false) {
                //todo: configure custom Container definitions declared in openejb-jar.xml (not yet in DD Schema)
            }
            // Step 2: March through each EJB
            EnterpriseBeansItem[] beans = data.standardDD[i].getEnterpriseBeans().getEnterpriseBeansItem();
            EjbDeployment[] deps = data.openejbDD[i].getEjbDeployment();
            for(int j=0; j<beans.length; j++) {
                EnterpriseBean bean = beans[j].getEntity() == null ? (EnterpriseBean)beans[j].getSession() : (EnterpriseBean)beans[j].getEntity();
                // Step 2.1: Find matching OpenEJB info
                EjbDeployment deployment = getDeployment(bean.getEjbName(), deps);
                if(deployment == null) {
                    throw new DeploymentException("There is no openejb-jar.xml EJB entry matching ejb-jar.xml entry for EJB "+bean.getEjbName());
                }

                // Step 2.2: Check for an existing container
                Container container = null;
                String contName = deployment.getContainerId();
                if(contName != null) {
                    container = (Container)containers.get(contName);
                    if(container == null) {
                        ContainerInfo ci = (ContainerInfo)defaultContainers.get(contName);
                        if(ci == null) {
                            log.error("EJB "+bean.getEjbName()+" is configured to use container '"+contName+"' but no such container exists; using default container.");
                        } else {
                            container = createContainer(ci);
                            containers.put(contName, container);
                            containerInfos.put(contName, ci);
                        }
                    }
                }

                // Step 2.3: Create a custom container if tags are present and not using a standard one
                //todo: currently required tags aren't present in DD Schema

                // Step 2.4: Assign the bean to a default container if it still doesn't have one
                if(container == null) {
                    contName = null;
                    if(bean instanceof Session) {
                        if(((Session)bean).getSessionType().equalsIgnoreCase("Stateless")) {
                            contName = defaultStateless;
                        } else if(((Session)bean).getSessionType().equalsIgnoreCase("Stateful")) {
                            contName = defaultStateful;
                        } else {
                            throw new DeploymentException("Unrecognized session bean type for bean "+bean.getEjbName());
                        }
                    } else if(bean instanceof Entity) {
                        if(((Entity)bean).getPersistenceType().equalsIgnoreCase("Bean")) {
                            contName = defaultBMP;
                        } else if(((Entity)bean).getPersistenceType().equalsIgnoreCase("Container")) {
                            contName = defaultCMP;
                        } else {
                            throw new DeploymentException("Unrecognized entity bean type for bean " + bean.getEjbName());
                        }
                    } else {
                        throw new DeploymentException("Unrecognized EJB type for bean "+bean.getEjbName());
                    }
                    if(contName != null) {
                        container = (Container)containers.get(contName);
                        if(container == null) {
                            ContainerInfo ci = (ContainerInfo)defaultContainers.get(contName);
                            if(ci == null) {
                                throw new DeploymentException("EJB " + bean.getEjbName() + " is configured to use container '" + contName + "' but no such container exists!");
                            } else {
                                container = createContainer(ci);
                                containers.put(contName, container);
                                containerInfos.put(contName, ci);
                            }
                        }
                    }
                }
                if(container == null) {
                    throw new DeploymentException("Unable to identify a container for bean "+bean.getEjbName());
                }

                // Step 2.5: Create the DeploymentInfo for this bean, and add it to the container
                DeploymentInfo di = createDeploymentInfo(system.getId(), bean, deployment, data.loader, allEJBs);
                List list = (List)containerDeployments.get(contName);
                if(list == null) {
                    list = new ArrayList();
                    containerDeployments.put(contName, list);
                }
                list.add(di);
                allDeployments.put(bean.getEjbName(), di);
            }
            AssemblyDescriptor ad = data.standardDD[i].getAssemblyDescriptor();
            if(ad != null) {
                // Step 3: Define security roles
                //todo: figure out what to do about security

                // Step 4: Apply method permissions
                for(int j=0; j<ad.getMethodPermissionCount(); j++) {
                    MethodPermission perm = ad.getMethodPermission(j);
                    for(int k=0; k<perm.getMethodCount(); k++) {
                        org.openejb.alt.config.ejb11.Method m = perm.getMethod(k);
                        org.openejb.core.DeploymentInfo di = (org.openejb.core.DeploymentInfo)allDeployments.get(m.getEjbName());
                        if(di == null) {
                            throw new DeploymentException("Unable to locate EJB "+m.getEjbName()+" for method permission");
                        }
                        Method[] methods = resolveMethods(m.getEjbName(), di.getHomeInterface(), di.getRemoteInterface(), m, data.loader);
                        for(int l=0; l<methods.length; l++) {
                            di.appendMethodPermissions(methods[l], perm.getRoleName());
                        }
                    }
                }

                // Step 5: Apply container transactions
                try {
                    for(int j = 0; j < ad.getContainerTransactionCount(); j++) {
                        ContainerTransaction tx = ad.getContainerTransaction(j);
                        for(int k = 0; k < tx.getMethodCount(); k++) {
                            org.openejb.alt.config.ejb11.Method m = tx.getMethod(k);
                            org.openejb.core.DeploymentInfo di = (org.openejb.core.DeploymentInfo)allDeployments.get(m.getEjbName());
                            if(di == null) {
                                throw new DeploymentException("Unable to locate EJB " + m.getEjbName() + " for container transaction");
                            }
                            Method[] methods = resolveMethods(m.getEjbName(), di.getHomeInterface(), di.getRemoteInterface(), m, data.loader);
                            for(int l = 0; l < methods.length; l++) {
                                di.setMethodTransactionAttribute(methods[l], tx.getTransAttribute());
                            }
                        }
                    }
                } catch(IllegalArgumentException e) { // thrown by DeploymentInfo for an invalid TX attribute
                    throw new DeploymentException(e.getMessage());
                }
            }
        }

        // After handling all EJB JARs, configure the ContainerSystem itself
        //todo: allow custom security services for the ContainerSystem
        try {
            system.getJNDIContext().bind("java:openejb/SecurityService", defaultSecurityService);
            system.setSecurityService(defaultSecurityService);
        } catch(NamingException e) {
            throw new DeploymentException("Unable to bind the security service for the application: "+e.getMessage());
        }
        try {
            system.getJNDIContext().bind("java:openejb/TransactionManager", OpenEJB.getTransactionManager());
        } catch(NamingException e) {
            throw new DeploymentException("Unable to bind the transaction manager for the application: " + e.getMessage());
        }
        if(defaultRemoteContexts != null) {
            try {
                for(Iterator it = defaultRemoteContexts.keySet().iterator(); it.hasNext();) {
                    String s = (String)it.next();
                    system.getJNDIContext().bind("java:openejb/remote_jndi_contexts/" + s, defaultRemoteContexts.get(s));
                }
            } catch(NamingException e) {
                throw new DeploymentException("Unable to bind remote JNDI contexts for the application: " + e.getMessage());
            }
        }
        if(defaultConnectors != null) {
            try {
                for(Iterator it = defaultConnectors.keySet().iterator(); it.hasNext();) {
                    String s = (String)it.next();
                    system.getJNDIContext().bind("java:openejb/connector/" + s, defaultConnectors.get(s));
                }
            } catch(NamingException e) {
                throw new DeploymentException("Unable to bind connectors for the application: " + e.getMessage());
            }
        }
        // Initialize the containers and deployments
        for(Iterator it = containers.keySet().iterator(); it.hasNext();) {
            String id = (String)it.next();
            Container c = (Container)containers.get(id);
            ContainerInfo ci = (ContainerInfo)containerInfos.get(id);
            HashMap map = new HashMap();
            List infos = (List)containerDeployments.get(id);
            for(int i = 0; i < infos.size(); i++) {
                DeploymentInfo info = (DeploymentInfo)infos.get(i);
                map.put(info.getDeploymentID(), info);
            }
            try {
                c.init(system, ci.containerName, map, ci.properties);
            } catch(OpenEJBException e) {
                throw new DeploymentException("Unable to initialize container: " + e.getMessage());
            }
            for(int i = 0; i < infos.size(); i++) {
                DeploymentInfo info = (DeploymentInfo)infos.get(i);
                system.addDeployment((org.openejb.core.DeploymentInfo)info);
            }
        }

        return system;
    }

    // ---------- Implementation methods ---------------------------------------

    private File[] download(File dir, URL[] jars) throws DeploymentException {
        if(!dir.exists()) {
            throw new IllegalArgumentException("Temp directory doesn't exist!");
        }
        File[] results = new File[jars.length];
        for(int i=0; i<jars.length; i++) {
            if(jars[i].getProtocol().equals("file")) {
                results[i] = new File(jars[i].getPath());
            } else {
                String file = jars[i].getFile();
                int pos = file.lastIndexOf('/');
                if(pos > -1) {
                    file = file.substring(pos+1);
                }
                pos = file.lastIndexOf('.');
                if(pos > -1) {
                    file = file.substring(0, pos);
                }
                try {
                    File temp = File.createTempFile(file, ".jar", dir);
                    temp.deleteOnExit(); //todo: also delete when app is re-/un-deployed
                    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(temp));
                    byte[] buf = new byte[512];
                    BufferedInputStream in = new BufferedInputStream(jars[i].openStream());
                    int count;
                    while((count = in.read(buf)) > -1) {
                        out.write(buf, 0, count);
                    }
                    in.close();
                    out.flush();
                    out.close();
                    results[i] = temp;
                } catch(IOException e) {
                    throw new DeploymentException("Unable to locally cache JAR to deploy from "+jars[i]+": "+e.getMessage());
                }
            }
        }
        return results;
    }

    private String[] getJ2eeInterfaces(DynamicDeploymentData data) {
        List list = new ArrayList();
        for(int jars = 0; jars < data.standardDD.length; jars++) {
            EnterpriseBeansItem[] beans = data.standardDD[jars].getEnterpriseBeans().getEnterpriseBeansItem();
            for(int i=0; i<beans.length; i++) {
                if(beans[i].getEntity() != null) {
                    list.add(beans[i].getEntity().getHome());
                    list.add(beans[i].getEntity().getRemote());
                } else if(beans[i].getSession() != null) {
                    list.add(beans[i].getSession().getHome());
                    list.add(beans[i].getSession().getRemote());
                }
            }
        }
        return (String[])list.toArray(new String[list.size()]);
    }

    private DynamicDeploymentData readDeploymentDescriptors(File[] files) throws DeploymentException {
        //todo: perhaps take OpenEJB classes out of the CL path between EJB API and apps
        return readDeploymentDescriptors(new JarClassLoader(files, getClass().getClassLoader()));
    }

    private DynamicDeploymentData readDeploymentDescriptors(ClassLoader loader) throws DeploymentException {
        DynamicDeploymentData data = new DynamicDeploymentData();
        data.loader = loader;
        try {
            data.standardDD = DDLoader.readEjbJars(loader);
            data.openejbDD = DDLoader.readOpenEjbJars(loader);
            reorderDDs(data);
        } catch(IOException e) {
            throw new DeploymentException("Unable to read a Deployment Descriptor: "+e.getMessage());
        }
        return data;
    }

    private void reorderDDs(DynamicDeploymentData data) throws DeploymentException {
        List standard = new ArrayList();
        List custom = new ArrayList();
        BeanList[] ejb = new BeanList[data.standardDD.length];
        Set dupes = new HashSet();
        Map openejb = new HashMap();
        for(int i=0; i<data.standardDD.length; i++) {
            BeanList list = new BeanList();
            EnterpriseBeansItem[] beans = data.standardDD[i].getEnterpriseBeans().getEnterpriseBeansItem();
            for(int j = 0; j < beans.length; j++) {
                if(beans[j].getEntity() != null) {
                    list.addBean(beans[j].getEntity().getEjbName());
                } else if(beans[j].getSession() != null) {
                    list.addBean(beans[j].getSession().getEjbName());
                }
            }
            if(dupes.contains(list)) {
                throw new DeploymentException("Two ejb-jar.xml DDs in the same application declare exactly the same list of EJBs! ("+list+")");
            }
            dupes.add(list);
            ejb[i] = list;
        }
        for(int i = 0; i < data.openejbDD.length; i++) {
            BeanList list = new BeanList();
            EjbDeployment[] deps = data.openejbDD[i].getEjbDeployment();
            for(int j = 0; j < deps.length; j++) {
                list.addBean(deps[j].getEjbName());
            }
            if(openejb.put(list, data.openejbDD[i]) != null) {
                throw new DeploymentException("Two openejb-jar.xml DDs in the same application declare exactly the same list of EJBs! (" + list + ")");
            }
        }
        for(int i=0; i<data.standardDD.length; i++) {
            OpenejbJar jar = (OpenejbJar)openejb.get(ejb[i]);
            if(jar == null) {
                log.warning("Ignoring an ejb-jar.xml because there's no matching openejb-jar.xml");
            } else {
                standard.add(data.standardDD[i]);
                custom.add(jar);
            }
        }
        data.standardDD = (EjbJar[])standard.toArray(new EjbJar[standard.size()]);
        data.openejbDD = (OpenejbJar[])custom.toArray(new OpenejbJar[custom.size()]);
    }

    private EjbDeployment getDeployment(String ejbName, EjbDeployment[] deps) {
        for(int i = 0; i < deps.length; i++) {
            if(deps[i].getEjbName().equals(ejbName)) {
                return deps[i];
            }
        }
        return null;
    }

    private Class loadClass(String cls, ClassLoader loader) throws DeploymentException {
        try {
            return loader.loadClass(cls);
        } catch(ClassNotFoundException e) {
            throw new DeploymentException("Unable to load class '"+cls+"'");
        }
    }

    private Container createContainer(ContainerInfo containerInfo) throws DeploymentException {
        Container container = null;
        if(containerInfo.className != null) {
            try {
                container = (Container)Class.forName(containerInfo.className).newInstance();
            } catch(Exception e) {
                throw new DeploymentException("Unable to create container of type " + containerInfo.className + ": " + e);
            }
        } else {
            // create a standard container
            switch(containerInfo.containerType) {
                case ContainerInfo.STATEFUL_SESSION_CONTAINER:
                    container = new StatefulContainer();
                    break;
                case ContainerInfo.ENTITY_CONTAINER:
                    container = new EntityContainer();
                    break;
                case ContainerInfo.STATELESS_SESSION_CONTAINER:
                    container = new StatelessContainer();
            }
        }
        return container;
    }

    private DeploymentInfo createDeploymentInfo(String containerSystemId, EnterpriseBean bean, EjbDeployment dep, ClassLoader loader, Map ejbs) throws DeploymentException {
        boolean isEntity = false;

        /*[1] Check the bean's type */
        byte componentType;
        Entity entity = null;
        Session session = null;
        if(bean instanceof Entity) {
            isEntity = true;
            entity = (Entity)bean;
            if(entity.getPersistenceType().equalsIgnoreCase("Container")) {
                componentType = org.openejb.core.DeploymentInfo.CMP_ENTITY;
            } else if(entity.getPersistenceType().equalsIgnoreCase("Bean")) {
                componentType = org.openejb.core.DeploymentInfo.BMP_ENTITY;
            } else {
                throw new DeploymentException("Unknown entity bean type '" + entity.getPersistenceType() + "' for EJB " + bean.getEjbName());
            }
        } else if(bean instanceof Session) {
            session = (Session)bean;
            if(session.getSessionType().equalsIgnoreCase("Stateful")) {
                componentType = org.openejb.core.DeploymentInfo.STATEFUL;
            } else if(session.getSessionType().equalsIgnoreCase("Stateless")) {
                componentType = org.openejb.core.DeploymentInfo.STATELESS;
            } else {
                throw new DeploymentException("Unknown session bean type '" + session.getSessionType() + "' for EJB " + bean.getEjbName());
            }
        } else {
            throw new DeploymentException("Unknown EJB type for " + bean.getEjbName());
        }

        /*[2] Load the bean's classes */
        Class ejbClass = null;
        Class home = null;
        Class remote = null;
        Class ejbPk = null;

        /*[2.1] Load the bean class */
        ejbClass = loadClass(bean.getEjbClass(), loader);
        /*[2.2] Load the remote interface */
        home = loadClass(bean.getHome(), loader);

        /*[2.3] Load the home interface */
        remote = loadClass(bean.getRemote(), loader);

        /*[2.4] Load the primary-key class */
        if(isEntity && entity.getPrimKeyClass() != null) {
            ejbPk = loadClass(entity.getPrimKeyClass(), loader);
        }

        /*[3] Populate a new DeploymentInfo object  */
        IvmContext root = new IvmContext(new NameNode(null, new ParsedName("comp"), null));
        org.openejb.core.DeploymentInfo deployment = createDeploymentInfoObject(root, dep.getDeploymentId(), home, remote, ejbClass, ejbPk, componentType, bean.getEjbName());

        /*[3.1] Add Entity bean specific values */
        if(isEntity) {
            /*[3.1.1] Set reenterant property */
            deployment.setIsReentrant(entity.getReentrant());

            /*[3.1.2] Set persistenceType property */
            if(entity.getPersistenceType().equals("Container")) {
                deployment.setCmpFields(getCmpFieldNames(entity.getCmpField()));
                try {
                    /*[3.1.2.1] Set primKeyField property */
                    if(entity.getPrimkeyField() != null)
                        deployment.setPrimKeyField(entity.getPrimkeyField());
                } catch(java.lang.NoSuchFieldException ne) {
                    throw new DeploymentException("Can not locate prim-key-field '" + entity.getPrimkeyField() + "' for EJB " + bean.getEjbName() + ": " + ne.getMessage());
                }

                /*[3.1.2.2] map the finder methods to the query statements. */
                if(dep.getQuery() != null & dep.getQuery().length > 0) {
                    for(int i = 0; i < dep.getQuery().length; i++) {
                        Method[] methods = resolveMethods(bean.getEjbName(), deployment.getHomeInterface(), dep.getQuery()[i].getQueryMethod(), loader);
                        for(int j = 0; j < methods.length; j++) {
                            deployment.addQuery(methods[j], dep.getQuery()[i].getObjectQl());
                        }
                    }
                }

            }
        }


        /*[3.2] Set transactionType property */
        if(session == null || session.getTransactionType() == null) {
            deployment.setBeanManagedTransaction(false);
        } else {
            if(session.getTransactionType().equals("Bean")) {
                deployment.setBeanManagedTransaction(true);
            } else if(session.getTransactionType().equals("Container")) {
                deployment.setBeanManagedTransaction(false);
            } else {
                throw new DeploymentException("Unknown transaction type '" + session.getTransactionType() + "' for EJB " + bean.getEjbName());
            }
        }

        /*[4] Fill bean's JNDI namespace */

        /**
         * Enterprise beans deployed with transaction-type = "Bean" must have access to a javax.transaction.UserTransaction
         * through their JNDI ENC. This bit of code addes a reference to a CoreUserTransaciton for
         * Bean-Managed Transaction beans that are session beans. Entity beans are not allowed to manager their own transactions.
         */
        try {
            /*[4.1] Add UserTransaction to namespace */
            if(deployment.isBeanManagedTransaction()) {
                if(componentType == org.openejb.core.DeploymentInfo.STATEFUL) {
                    root.bind("java:comp/UserTransaction", new org.openejb.core.stateful.EncUserTransaction(new org.openejb.core.CoreUserTransaction()));
                } else if(componentType == org.openejb.core.DeploymentInfo.STATELESS) {
                    root.bind("java:comp/UserTransaction", new org.openejb.core.stateless.EncUserTransaction(new org.openejb.core.CoreUserTransaction()));
                }
            }
        } catch(javax.naming.NamingException ne) {
            throw new DeploymentException("Can't bind UserTransaction to bean deployment JNDI ENC: " + ne.getMessage());
        }

        /*[4.2] Add BeanRefs to namespace */
        bindJndiBeanRefs(containerSystemId, componentType, bean.getEjbName(), bean.getEjbRef(), ejbs, root);

        /*[4.3] Add EnvEntries to namespace */
        bindJndiEnvEntries(bean.getEjbName(), bean.getEnvEntry(), loader, root);

        /*[4.4] Add ResourceRefs to namespace */
        bindJndiResourceRefs(containerSystemId, componentType, bean.getEjbName(), bean.getResourceRef(), dep.getResourceLink(), root);

        return deployment;
    }

    private String[] getCmpFieldNames(CmpField[] cmpField) {
        String[] result = new String[cmpField.length];
        for(int i = 0; i < result.length; result[i] = cmpField[i++].getFieldName()) ;
        return result;
    }

    private Method[] resolveMethods(String ejbName, Class home, QueryMethod method, ClassLoader loader) throws DeploymentException {
        if(method.getMethodName() == null) {
            throw new DeploymentException("A method tag in openejb-jar.xml does not have a method name!");
        } else if(method.getMethodName().equals("*") || method.getMethodParams() == null) {
            List results = new LinkedList();
            results.addAll(getInterfaceMethods(home, EJBHome.class, new ArrayList()));
            if(!method.getMethodName().equals("*")) { // Filter out all methods that don't match the name
                for(ListIterator it = results.listIterator(); it.hasNext();) {
                    Method mth = (Method)it.next();
                    if(!mth.getName().equals(method.getMethodName())) {
                        it.remove();
                    }
                }
            }
            return (Method[])results.toArray(new Method[results.size()]);
        } else if(method.getMethodParams() != null) { // A single method
            Class[] params = new Class[method.getMethodParams().getMethodParamCount()];
            for(int i = 0; i < params.length; i++) {
                params[i] = loadClass(method.getMethodParams().getMethodParam(i), loader);
            }
            try {
                return new Method[]{home.getMethod(method.getMethodName(), params)};
            } catch(NoSuchMethodException e) {
                throw new DeploymentException("Unable to locate method " + method.getMethodName() + " with params " + Arrays.asList(method.getMethodParams().getMethodParam()) + " in EJB " + ejbName);
            } catch(SecurityException e) {
                log.error("Unable to look up a method", e);
                throw new DeploymentException("Unable to look up a method: " + e.getMessage());
            }
        } else {
            throw new DeploymentException("Logic error in looking up methods");
        }
    }

    private Method[] resolveMethods(String ejbName, Class home, Class component, org.openejb.alt.config.ejb11.Method method, ClassLoader loader) throws DeploymentException {
        List results = new LinkedList();
        if(method.getMethodName() == null) {
            throw new DeploymentException("A method tag in ejb-jar.xml does not have a method name!");
        } else if(method.getMethodName().equals("*") || method.getMethodParams() == null) {
            if(method.getMethodIntf() == null || method.getMethodIntf().equalsIgnoreCase("Remote")) {
                results.addAll(getInterfaceMethods(component, EJBObject.class, new ArrayList()));
                results.add(OBJECT_REMOVE_METHOD);
            }
            if(method.getMethodIntf() == null || method.getMethodIntf().equalsIgnoreCase("Home")) {
                results.addAll(getInterfaceMethods(home, EJBHome.class, new ArrayList()));
                results.add(HOME_REMOVE_METHOD_1);
                results.add(HOME_REMOVE_METHOD_2);
            }
            if(!method.getMethodName().equals("*")) { // Filter out all methods that don't match the name
                for(ListIterator it = results.listIterator(); it.hasNext();) {
                    Method mth = (Method)it.next();
                    if(!mth.getName().equals(method.getMethodName())) {
                        it.remove();
                    }
                }
            }
        } else if(method.getMethodParams() != null) { // A single method
            Class[] params = new Class[method.getMethodParams().getMethodParamCount()];
            for(int i = 0; i < params.length; i++) {
                params[i] = loadClass(method.getMethodParams().getMethodParam(i), loader);
            }
            boolean found = false;
            if(method.getMethodIntf() == null || method.getMethodIntf().equalsIgnoreCase("Remote")) {
                try {
                    results.add(new Method[]{component.getMethod(method.getMethodName(), params)});
                    found = true;
                } catch(NoSuchMethodException e) {}
            }
            if(method.getMethodIntf() == null || method.getMethodIntf().equalsIgnoreCase("Home")) {
                try {
                    results.add(new Method[]{home.getMethod(method.getMethodName(), params)});
                    found = true;
                } catch(NoSuchMethodException e) {}
            }
            if(!found) {
                throw new DeploymentException("Unable to locate method " + method.getMethodName() + " with params " + Arrays.asList(method.getMethodParams().getMethodParam()) + " in EJB " + ejbName);
            }
        }
        return (Method[])results.toArray(new Method[results.size()]);
    }

    private List getInterfaceMethods(Class component, Class exclude, List list) {
        if(component.getName().equals("java.lang.Object") || component.getName().equals(exclude.getName())) {
            return list;
        }
        list.addAll(Arrays.asList(component.getMethods()));
        Class[] ifaces = component.getInterfaces();
        for(int i = 0; i < ifaces.length; i++) {
            getInterfaceMethods(ifaces[i], exclude, list);
        }
        return list;
    }

    /**
     * This method creates the DeploymentInfo class and sets the JNDI context
     * at the same time. This is done to enable the TyrexAssembler to override
     * this method to hook in its own DeploymentInfo subclass without duplicating
     * code.
     */
    protected org.openejb.core.DeploymentInfo createDeploymentInfoObject(javax.naming.Context root, Object did, Class homeClass, Class remoteClass, Class beanClass, Class pkClass, byte componentType, String ejbName) throws DeploymentException {
        try {
            org.openejb.core.DeploymentInfo info = new org.openejb.core.DeploymentInfo(did, homeClass, remoteClass, beanClass, pkClass, componentType);
            info.setJndiEnc(root);
            return info;
        } catch(org.openejb.SystemException e) {
            throw new DeploymentException("Unable to deploy EJB "+ejbName+": "+e.getMessage());
        }
    }

    protected void bindJndiResourceRefs(String containerSystemId, byte ejbType, String ejbName, ResourceRef[] refs, ResourceLink[] links, IvmContext root) throws DeploymentException {
        links = reorderLinks(ejbName, refs, links);
        for(int i = 0; i < refs.length; i++) {
            Object ref = null;

            if(links[i].getResId() != null) {
                String jndiName = "java:openejb/connector/" + links[i].getResId();
                Reference ref2 = new IntraVmJndiReference(containerSystemId, jndiName);
                //todo: doesn't handle arbitrary container types?
                if(ejbType == org.openejb.core.DeploymentInfo.CMP_ENTITY || ejbType == org.openejb.core.DeploymentInfo.BMP_ENTITY) {
                    ref = new org.openejb.core.entity.EncReference(ref2);
                } else if(ejbType == org.openejb.core.DeploymentInfo.STATEFUL) {
                    ref = new org.openejb.core.stateful.EncReference(ref2);
                } else if(ejbType == org.openejb.core.DeploymentInfo.STATELESS) {
                    ref = new org.openejb.core.stateless.EncReference(ref2);
                } else {
                    throw new DeploymentException("Unknown EJB type "+ejbType);
                }
            } else {
                throw new DeploymentException("Remote resources are not supported; Resource "+refs[i].getResRefName()+" in EJB "+ejbName+" should have a resource ID in openejb-jar.xml");
            }

            if(ref != null) {
                try {
                    root.bind(prefixForBinding(refs[i].getResRefName()), ref);
                } catch(NamingException e) {
                    throw new DeploymentException("Unable to bind resource reference "+refs[i].getResRefName()+" for EJB "+ejbName+" in JNDI");
                }
            }
        }
    }

    protected void bindJndiBeanRefs(String containerSystemId, byte ejbType, String ejbName, EjbRef[] refs, Map ejbs, IvmContext root) throws DeploymentException {
        for(int i = 0; i < refs.length; i++) {
            Object ref = null;
            if(refs[i].getEjbLink() != null) {
                String id = (String)ejbs.get(refs[i].getEjbLink());
                if(id == null) {
                    throw new DeploymentException("Unable to bind EJB ref "+refs[i].getEjbRefName()+" for EJB "+ejbName+": Linked EJB "+refs[i].getEjbLink()+" not found.");
                }
                String jndiName = "java:openejb/ejb/" + id;
                Reference ref2 = new IntraVmJndiReference(containerSystemId, jndiName);
                //todo: doesn't handle arbitrary container types?
                if(ejbType == org.openejb.core.DeploymentInfo.CMP_ENTITY || ejbType == org.openejb.core.DeploymentInfo.BMP_ENTITY) {
                    ref = new org.openejb.core.entity.EncReference(ref2);
                } else if(ejbType == org.openejb.core.DeploymentInfo.STATEFUL) {
                    ref = new org.openejb.core.stateful.EncReference(ref2);
                } else if(ejbType == org.openejb.core.DeploymentInfo.STATELESS) {
                    ref = new org.openejb.core.stateless.EncReference(ref2);
                } else {
                    throw new DeploymentException("Unknown EJB type " + ejbType);
                }
            } else {
                throw new DeploymentException("Remote EJBs are not supported; EJB Link " + refs[i].getEjbRefName() + " in EJB " + ejbName + " should have an ejb-link");
            }
            if(ref != null) {
                try {
                    root.bind(prefixForBinding(refs[i].getEjbRefName()), ref);
                } catch(NamingException e) {
                    throw new DeploymentException("Unable to bind EJB reference " + refs[i].getEjbRefName() + " for EJB " + ejbName + " in JNDI");
                }
            }

        }
    }

    protected void bindJndiEnvEntries(String ejbName, EnvEntry[] envs, ClassLoader loader, IvmContext root) throws DeploymentException {
        for(int i = 0; i < envs.length; i++) {
            Class type = null;
            type = loadClass(envs[i].getEnvEntryType(), loader);
            Object obj = null;
            try {
                if(type == java.lang.String.class) {
                    obj = new String(envs[i].getEnvEntryValue());
                } else if(type == java.lang.Double.class) {
                    obj = new Double(envs[i].getEnvEntryValue());
                } else if(type == java.lang.Integer.class) {
                    obj = new Integer(envs[i].getEnvEntryValue());
                } else if(type == java.lang.Long.class) {
                    obj = new Long(envs[i].getEnvEntryValue());
                } else if(type == java.lang.Float.class) {
                    obj = new Float(envs[i].getEnvEntryValue());
                } else if(type == java.lang.Short.class) {
                    obj = new Short(envs[i].getEnvEntryValue());
                } else if(type == java.lang.Boolean.class) {
                    obj = new Boolean(envs[i].getEnvEntryValue());
                } else if(type == java.lang.Byte.class) {
                    obj = new Byte(envs[i].getEnvEntryValue());
                } else {
                    try {
                        Constructor con = type.getConstructor(new Class[]{String.class});
                        obj = con.newInstance(new Object[]{envs[i].getEnvEntryValue()});
                    } catch(Exception e) {
                        throw new DeploymentException("Unable to instantiate env entry "+envs[i].getEnvEntryName()+" for EJB "+ejbName+": "+e.getMessage());
                    }
                }
            } catch(NumberFormatException e) {
                throw new DeploymentException("Unable to instantiate env entry " + envs[i].getEnvEntryName() + " for EJB " + ejbName + ": " + e.getMessage());
            }
            try {
                root.bind(prefixForBinding(envs[i].getEnvEntryName()), obj);
            } catch(NamingException e) {
                throw new DeploymentException("Unable to bind env entry " + envs[i].getEnvEntryName() + " for EJB " + ejbName + " in JNDI");
            }
        }
    }

    protected String prefixForBinding(String name) {
        if(name.charAt(0) == '/')
            name = name.substring(1);
        if(!(name.startsWith("java:comp/env") || name.startsWith("comp/env"))) {
            if(name.startsWith("env/"))
                name = "comp/" + name;
            else
                name = "comp/env/" + name;
        }
        return name;
    }

    private ResourceLink[] reorderLinks(String ejbName, ResourceRef[] refs, ResourceLink[] links) throws DeploymentException {
        if(refs.length != links.length) {
            throw new DeploymentException("Not all resource references in ejb-jar.xml (" + refs.length + ") have matching resource links in openejb-jar.xml (" + links.length + ")");
        }
        ResourceLink[] results = new ResourceLink[links.length];
        for(int i = 0; i < links.length; i++) {
            for(int j = 0; j < links.length; j++) {
                if(links[j].getResRefName().equals(refs[i].getResRefName())) {
                    results[i] = links[j];
                    break;
                }
            }
            if(results[i] == null) {
                throw new DeploymentException("Resource reference " + refs[i].getResRefName() + " in EJB " + ejbName + " doesn't have a matching resource link in openejb-jar.xml");
            }
        }
        return results;
    }

    // ---------- Inner Classes ---------------------------------------

    private static class BeanList {
        private String names;

        public void addBean(String name) {
            names = names == null ? name : names + "\t" + name;
        }

        public boolean equals(Object o) {
            if(this == o) return true;
            if(!(o instanceof BeanList)) return false;

            final BeanList beanList = (BeanList)o;

            if(names != null ? !names.equals(beanList.names) : beanList.names != null) return false;

            return true;
        }

        public int hashCode() {
            return (names != null ? names.hashCode() : 0);
        }

        public String toString() {
            return names;
        }
    }
}
