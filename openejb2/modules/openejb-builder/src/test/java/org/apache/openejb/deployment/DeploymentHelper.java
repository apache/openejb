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
package org.apache.openejb.deployment;


import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.transaction.TransactionManager;

import junit.framework.TestCase;
import org.apache.geronimo.connector.ActivationSpecWrapperGBean;
import org.apache.geronimo.connector.ResourceAdapterModuleImplGBean;
import org.apache.geronimo.connector.ResourceAdapterWrapperGBean;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTrackingCoordinatorGBean;
import org.apache.geronimo.connector.work.GeronimoWorkManagerGBean;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.management.impl.J2EEServerImpl;
import org.apache.geronimo.kernel.Jsr77Naming;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.EditableKernelConfigurationManager;
import org.apache.geronimo.kernel.config.IOUtil;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.config.NullConfigurationStore;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.DefaultArtifactManager;
import org.apache.geronimo.kernel.repository.DefaultArtifactResolver;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.ImportType;
import org.apache.geronimo.pool.ThreadPool;
import org.apache.geronimo.system.serverinfo.BasicServerInfo;
import org.apache.geronimo.timer.vm.VMStoreThreadPooledNonTransactionalTimer;
import org.apache.geronimo.timer.vm.VMStoreThreadPooledTransactionalTimer;
import org.apache.geronimo.transaction.manager.GeronimoTransactionManagerGBean;
import org.apache.geronimo.transaction.manager.XidFactoryImplGBean;
import org.apache.geronimo.webservices.SoapHandler;
import org.apache.geronimo.webservices.WebServiceContainer;
import org.apache.openejb.DeploymentIndexGBean;
import org.apache.openejb.deployment.mdb.mockra.MockActivationSpec;
import org.apache.openejb.deployment.mdb.mockra.MockResourceAdapter;
import org.apache.openejb.entity.bmp.DefaultBmpEjbContainerGBean;
import org.apache.openejb.entity.cmp.DefaultCmpEjbContainerGBean;
import org.apache.openejb.mdb.DefaultMdbContainerGBean;
import org.apache.openejb.sfsb.DefaultStatefulEjbContainerGBean;
import org.apache.openejb.slsb.DefaultStatelessEjbContainerGBean;

/**
 * @version $Revision$ $Date$
 */
public class DeploymentHelper extends TestCase {
    public static final Artifact BOOTSTRAP_ID = new Artifact("test", "base", "1", "car");
    public static final Artifact TEST_CONFIGURATION_ID = new Artifact("foo", "bar", "1", "car");
    public static final Environment TEST_ENVIRONMENT = new Environment(TEST_CONFIGURATION_ID);

    static {
        TEST_ENVIRONMENT.addDependency(BOOTSTRAP_ID, ImportType.ALL);
    }

    public static final GBeanData ACTIVATION_SPEC_INFO = new GBeanData(ActivationSpecWrapperGBean.getGBeanInfo());

    public Naming naming = new Jsr77Naming();
    public final AbstractName serverName = naming.createRootName(BOOTSTRAP_ID, "Server", "J2EEServer");

    protected Kernel kernel;
    public String statelessEjbContainerName = "stateless";
    public String statefulEjbContainerName = "stateful";
    public String bmpEjbContainerName = "bmp";
    public String cmpEjbContainerName = "cmp";
    public String mdbEjbContainerName = "mdb";
    public AbstractName ctcName;
    public AbstractName tmName;
    public TransactionManager transactionManager;
    public AbstractName txTimerName;
    public AbstractName nonTxTimerName;
    public AbstractName activationSpecName;
    public ClassLoader cl;
    private File basedir = new File(System.getProperty("basedir", "."));
    private Environment defaultEnvironment = new Environment();
    public ConfigurationManager configurationManager;
    public ConfigurationStore configStore;
    public AbstractName dataSourceName;
    public AbstractName listenerName;
    protected final AbstractName CONTAINER_NAME = new AbstractName(TEST_CONFIGURATION_ID, Collections.singletonMap("ejb", "Mock"));

    protected void setUp() throws Exception {
        super.setUp();
        cl = this.getClass().getClassLoader();
        kernel = KernelFactory.newInstance().createKernel("test");
        kernel.boot();

        ConfigurationData bootstrap = new ConfigurationData(BOOTSTRAP_ID, naming);

        bootstrap.addGBean("ServerInfo", BasicServerInfo.GBEAN_INFO).setAttribute("baseDirectory", ".");

        GBeanData configStoreData = bootstrap.addGBean("MockConfigurationStore", MockConfigStore.GBEAN_INFO);
        AbstractName configStoreName = configStoreData.getAbstractName();
        configStoreData.setAttribute("baseURL", basedir.toURL());

        GBeanData artifactManagerData = bootstrap.addGBean("ArtifactManager", DefaultArtifactManager.GBEAN_INFO);

        GBeanData artifactResolverData = bootstrap.addGBean("ArtifactResolver", DefaultArtifactResolver.GBEAN_INFO);
        artifactResolverData.setReferencePattern("ArtifactManager", artifactManagerData.getAbstractName());

        GBeanData configurationManagerData = bootstrap.addGBean("ConfigurationManager", EditableKernelConfigurationManager.GBEAN_INFO);
        configurationManagerData.setReferencePattern("ArtifactManager", artifactManagerData.getAbstractName());
        configurationManagerData.setReferencePattern("ArtifactResolver", artifactResolverData.getAbstractName());
        configurationManagerData.setReferencePattern("Stores", configStoreName);
        bootstrap.addGBean(configurationManagerData);

        GBeanData serverData = new GBeanData(serverName, J2EEServerImpl.GBEAN_INFO);
        bootstrap.addGBean(serverData);

        GBeanData xidFactory = bootstrap.addGBean("XidFactory", XidFactoryImplGBean.GBEAN_INFO);
        xidFactory.setAttribute("tmId", "tmId".getBytes());

        GBeanData tm = bootstrap.addGBean("TransactionManager", GeronimoTransactionManagerGBean.GBEAN_INFO);
        tmName = tm.getAbstractName();
        tm.setReferencePattern("XidFactory", xidFactory.getAbstractName());
        tm.setAttribute("defaultTransactionTimeoutSeconds", new Integer(10));

        GBeanData ctc = bootstrap.addGBean("ConnectionTrackingCoordinator", ConnectionTrackingCoordinatorGBean.GBEAN_INFO);
        ctcName = ctc.getAbstractName();
        ctc.setReferencePattern("TransactionManager", tmName);

        //timer
        GBeanData threadPoolGBean = bootstrap.addGBean("Threadpool", ThreadPool.GBEAN_INFO);
        threadPoolGBean.setAttribute("keepAliveTime", new Long(5000));
        threadPoolGBean.setAttribute("poolSize", new Integer(5));
        threadPoolGBean.setAttribute("poolName", "DefaultThreadPool");

        GBeanData transactionalTimerGBean = bootstrap.addGBean("TransactionalTimer", VMStoreThreadPooledTransactionalTimer.GBEAN_INFO);
        txTimerName = transactionalTimerGBean.getAbstractName();
        transactionalTimerGBean.setAttribute("repeatCount", new Integer(5));
        transactionalTimerGBean.setReferencePattern("TransactionManager", tmName);
        transactionalTimerGBean.setReferencePattern("ThreadPool", threadPoolGBean.getAbstractName());

        GBeanData nonTransactionalTimerGBean = bootstrap.addGBean("NonTransactionTimer", VMStoreThreadPooledNonTransactionalTimer.GBEAN_INFO);
        nonTxTimerName = nonTransactionalTimerGBean.getAbstractName();
        nonTransactionalTimerGBean.setReferencePattern("ThreadPool", threadPoolGBean.getAbstractName());

        //resourceadapter
        GBeanData geronimoWorkManagerGBean = bootstrap.addGBean("WorkManager", GeronimoWorkManagerGBean.getGBeanInfo());
        geronimoWorkManagerGBean.setReferencePattern("SyncPool", threadPoolGBean.getAbstractName());
        geronimoWorkManagerGBean.setReferencePattern("StartPool", threadPoolGBean.getAbstractName());
        geronimoWorkManagerGBean.setReferencePattern("ScheduledPool", threadPoolGBean.getAbstractName());
        geronimoWorkManagerGBean.setReferencePattern("TransactionManager", tmName);

        Map activationSpecInfoMap = new HashMap();
        ACTIVATION_SPEC_INFO.setAttribute("activationSpecClass", MockActivationSpec.class.getName());
        activationSpecInfoMap.put(javax.jms.MessageListener.class.getName(), ACTIVATION_SPEC_INFO);

        GBeanData moduleData = new GBeanData(createResourceAdapterModuleName(BOOTSTRAP_ID), ResourceAdapterModuleImplGBean.GBEAN_INFO);
        bootstrap.addGBean(moduleData);
        moduleData.setAttribute("activationSpecInfoMap", activationSpecInfoMap);

        GBeanData resourceAdapterGBean = new GBeanData(createJCAResourceAdapterName(BOOTSTRAP_ID), ResourceAdapterWrapperGBean.getGBeanInfo());
        bootstrap.addGBean(resourceAdapterGBean);
        resourceAdapterGBean.setAttribute("resourceAdapterClass", MockResourceAdapter.class.getName());
        resourceAdapterGBean.setReferencePattern("WorkManager", geronimoWorkManagerGBean.getAbstractName());

        GBeanData activationSpecGBean = bootstrap.addGBean("ActivationSpec", ActivationSpecWrapperGBean.getGBeanInfo());
        activationSpecName = activationSpecGBean.getAbstractName();
        activationSpecGBean.setAttribute("activationSpecClass", MockActivationSpec.class.getName());
        //TODO fix this configid
//        activationSpecGBean.setAttribute("containerId", CONTAINER_NAME.toURI().toString());
        activationSpecGBean.setReferencePattern("ResourceAdapterWrapper", resourceAdapterGBean.getAbstractName());

        //containerIndex
        GBeanData containerIndexGBean = bootstrap.addGBean("DeploymentIndex", DeploymentIndexGBean.GBEAN_INFO);
        Set ejbContainerNames = new HashSet();
        ejbContainerNames.add(new AbstractNameQuery(CONTAINER_NAME));
        ejbContainerNames.add(new AbstractNameQuery(null, Collections.singletonMap("j2eeType", "StatelessSessionBean")));
        ejbContainerNames.add(new AbstractNameQuery(null, Collections.singletonMap("j2eeType", "StatefulSessionBean")));
        ejbContainerNames.add(new AbstractNameQuery(null, Collections.singletonMap("j2eeType", "EntityBean")));
        containerIndexGBean.setReferencePatterns("EjbDeployments", ejbContainerNames);

        //datasource
        dataSourceName = bootstrap.addGBean("DefaultDatasource", MockConnectionProxyFactory.GBEAN_INFO).getAbstractName();

        // soap listener
        listenerName = bootstrap.addGBean("SoapListener", MockListener.GBEAN_INFO).getAbstractName();

        // Containers
        GBeanData statelessEjbContainer = bootstrap.addGBean(statelessEjbContainerName, DefaultStatelessEjbContainerGBean.GBEAN_INFO);
        statelessEjbContainer.setReferencePattern("TransactionManager", tmName);
        statelessEjbContainer.setReferencePattern("TrackedConnectionAssociator", ctcName);
        statelessEjbContainer.setReferencePattern("TransactedTimer", txTimerName);
        statelessEjbContainer.setReferencePattern("NontransactedTimer", nonTxTimerName);

        GBeanData statefulEjbContainer = bootstrap.addGBean(statefulEjbContainerName, DefaultStatefulEjbContainerGBean.GBEAN_INFO);
        statefulEjbContainer.setReferencePattern("TransactionManager", tmName);
        statefulEjbContainer.setReferencePattern("TrackedConnectionAssociator", ctcName);

        GBeanData bmpEjbContainer = bootstrap.addGBean(bmpEjbContainerName, DefaultBmpEjbContainerGBean.GBEAN_INFO);
        bmpEjbContainer.setReferencePattern("TransactionManager", tmName);
        bmpEjbContainer.setReferencePattern("TrackedConnectionAssociator", ctcName);
        bmpEjbContainer.setReferencePattern("TransactedTimer", txTimerName);
        bmpEjbContainer.setReferencePattern("NontransactedTimer", nonTxTimerName);

        GBeanData cmpEjbContainer = bootstrap.addGBean(cmpEjbContainerName, DefaultCmpEjbContainerGBean.GBEAN_INFO);
        cmpEjbContainer.setReferencePattern("TransactionManager", tmName);
        cmpEjbContainer.setReferencePattern("TrackedConnectionAssociator", ctcName);
        cmpEjbContainer.setReferencePattern("TransactedTimer", txTimerName);
        cmpEjbContainer.setReferencePattern("NontransactedTimer", nonTxTimerName);

        GBeanData mdbEjbContainer = bootstrap.addGBean("mdb", DefaultMdbContainerGBean.GBEAN_INFO);
        mdbEjbContainer.setReferencePattern("TransactionManager", tmName);
        mdbEjbContainer.setReferencePattern("TrackedConnectionAssociator", ctcName);
        mdbEjbContainer.setReferencePattern("TransactedTimer", txTimerName);
        mdbEjbContainer.setReferencePattern("NontransactedTimer", nonTxTimerName);

        // load and start the configuration
        ConfigurationUtil.loadBootstrapConfiguration(kernel, bootstrap, getClass().getClassLoader());

        configurationManager = ConfigurationUtil.getEditableConfigurationManager(kernel);
        configStore = (ConfigurationStore) kernel.getGBean(configStoreName);
        configStore.install(bootstrap);

        transactionManager = (TransactionManager) kernel.getGBean(tmName);
        defaultEnvironment.addDependency(BOOTSTRAP_ID, ImportType.ALL);
        defaultEnvironment.setConfigId(TEST_CONFIGURATION_ID);
    }


    private AbstractName createJCAResourceAdapterName(Artifact artifact) {
        AbstractName jcaResourceName = createJCAResourceName(artifact);
        return naming.createChildName(jcaResourceName, "MockRA", NameFactory.JCA_RESOURCE_ADAPTER);
    }

    protected AbstractName createJCAResourceName(Artifact artifact) {
        AbstractName moduleName = createResourceAdapterModuleName(artifact);
        AbstractName resourceAdapterName = naming.createChildName(moduleName, artifact.toString(), NameFactory.RESOURCE_ADAPTER);
        return naming.createChildName(resourceAdapterName, artifact.toString(), NameFactory.JCA_RESOURCE);
    }

    private AbstractName createResourceAdapterModuleName(Artifact artifact) {
        AbstractName appName = naming.createRootName(artifact, NameFactory.NULL, NameFactory.J2EE_APPLICATION);
        return naming.createChildName(appName, artifact.toString(), NameFactory.RESOURCE_ADAPTER_MODULE);
    }

    protected void tearDown() throws Exception {
        kernel.shutdown();
        super.tearDown();
    }

    public static class MockConfigStore extends NullConfigurationStore {
        private static final Map locations = new HashMap();
        private Map configs = new HashMap();

        URL baseURL;

        public MockConfigStore(URL baseURL) {
            this.baseURL = baseURL;
        }

        public void install(ConfigurationData configurationData) throws IOException, InvalidConfigException {
            configs.put(configurationData.getId(), configurationData);
        }

        public void uninstall(Artifact configID) throws NoSuchConfigException, IOException {
            configs.remove(configID);
        }

        public ConfigurationData loadConfiguration(Artifact configId) throws NoSuchConfigException, IOException, InvalidConfigException {
            if (configs.containsKey(configId)) {
                ConfigurationData configurationData = (ConfigurationData) configs.get(configId);
                configurationData.setConfigurationStore(this);
                return configurationData;
            } else {
                ConfigurationData configurationData = new ConfigurationData(configId, new Jsr77Naming());
                configurationData.setConfigurationStore(this);
                return configurationData;
            }
        }

        public boolean containsConfiguration(Artifact configID) {
            return true;
        }

        public File createNewConfigurationDir(Artifact configId) {
            try {
                File file = DeploymentUtil.createTempDir();
                locations.put(configId, file);
                return file;
            } catch (IOException e) {
                return null;
            }
        }

        public Set resolve(Artifact configId, String moduleName, String pattern) throws NoSuchConfigException, MalformedURLException {
            File file = (File) locations.get(configId);
            if (file == null) {
                throw new NoSuchConfigException(configId);
            }
            return IOUtil.search(file, pattern);
        }

        public final static GBeanInfo GBEAN_INFO;

        static {
            GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(MockConfigStore.class, NameFactory.CONFIGURATION_STORE);
            infoBuilder.addAttribute("baseURL", URL.class, true, true);
            infoBuilder.setConstructor(new String[] {"baseURL"});
            infoBuilder.addInterface(ConfigurationStore.class);
            GBEAN_INFO = infoBuilder.getBeanInfo();
        }
    }


    public static class MockListener implements SoapHandler {

        public void addWebService(String contextPath, String[] virtualHosts, WebServiceContainer webServiceContainer, String securityRealmName, String realmName, String transportGuarantee, String authMethod, ClassLoader classLoader) throws Exception {
        }

        public void removeWebService(String contextPath) {
        }

        public static final  GBeanInfo GBEAN_INFO;

        static {
            GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(MockListener.class, NameFactory.GERONIMO_SERVICE);
                infoBuilder.addInterface(SoapHandler.class);
                GBEAN_INFO = infoBuilder.getBeanInfo();
        }
    }

    public static AbstractNameQuery createEjbNameQuery(String name, String j2eeType, String ejbModule) {
        Map properties = new LinkedHashMap();
        properties.put("name", name);
        properties.put("j2eeType", j2eeType);
        properties.put("EJBModule", ejbModule);
        return new AbstractNameQuery(null, properties);
    }
}
