/* ====================================================================
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce this list of
 *    conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
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
 *    (http://openejb.org/).
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
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the OpenEJB Project.  For more information
 * please see <http://openejb.org/>.
 *
 * ====================================================================
 */
package org.openejb.deployment;

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
import org.apache.geronimo.transaction.context.TransactionContextManagerGBean;
import org.apache.geronimo.transaction.manager.TransactionManagerImplGBean;
import org.apache.geronimo.transaction.manager.XidFactoryImplGBean;
import org.apache.geronimo.webservices.SoapHandler;
import org.apache.geronimo.webservices.WebServiceContainer;
import org.openejb.ContainerIndex;
import org.openejb.deployment.mdb.mockra.MockActivationSpec;
import org.openejb.deployment.mdb.mockra.MockResourceAdapter;

/**
 * @version $Revision$ $Date$
 */
public class DeploymentHelper extends TestCase {
    public static final Environment DEFAULT_ENVIRONMENT = new Environment();
    public static final Environment ENVIRONMENT = new Environment();

    static {
        Artifact defaultConfigId = Artifact.create("geronimo/server/1/car");
        DEFAULT_ENVIRONMENT.setConfigId(defaultConfigId);
        Artifact configId = Artifact.create("test/test/1/car");
        ENVIRONMENT.setConfigId(configId);
    }

    public static final GBeanData ACTIVATION_SPEC_INFO = new GBeanData(ActivationSpecWrapperGBean.getGBeanInfo());

    public Naming naming = new Jsr77Naming();
    public Artifact baseId = new Artifact("test", "base", "1", "car");
    public final AbstractName serverName = naming.createRootName(baseId, "Server", "J2EEServer");

    protected Kernel kernel;
    public AbstractName ctcName;
    public AbstractName tmName;
    public AbstractName tcmName;
    public AbstractName txTimerName;
    public AbstractName nonTxTimerName;
    public AbstractName activationSpecName;
    public ClassLoader cl;
    private File basedir = new File(System.getProperty("basedir", "."));
    public Artifact testConfigurationArtifact = new Artifact("foo", "bar", "1", "car");
    private Environment defaultEnvironment = new Environment();
    public ConfigurationManager configurationManager;
    public ConfigurationStore configStore;
    public AbstractName dataSourceName;
    public AbstractName listenerName;
    protected final AbstractName CONTAINER_NAME = new AbstractName(testConfigurationArtifact, Collections.singletonMap("ejb", "Mock"));

    protected void setUp() throws Exception {
        super.setUp();
        cl = this.getClass().getClassLoader();
        kernel = KernelFactory.newInstance().createKernel("test");
        kernel.boot();

        ConfigurationData bootstrap = new ConfigurationData(baseId, naming);

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

        GBeanData tm = bootstrap.addGBean("TransactionManager", TransactionManagerImplGBean.GBEAN_INFO);
        tmName = tm.getAbstractName();
        tm.setReferencePattern("XidFactory", xidFactory.getAbstractName());
        tm.setAttribute("defaultTransactionTimeoutSeconds", new Integer(10));

        GBeanData tcm = bootstrap.addGBean("TransactionContextManager", TransactionContextManagerGBean.GBEAN_INFO);
        tcm.setReferencePattern("TransactionManager", tmName);
        tcmName = tcm.getAbstractName();
        ctcName = bootstrap.addGBean("ConnectionTrackingCoordinator", ConnectionTrackingCoordinatorGBean.GBEAN_INFO).getAbstractName();

        //timer
        GBeanData threadPoolGBean = bootstrap.addGBean("Threadpool", ThreadPool.GBEAN_INFO);
        threadPoolGBean.setAttribute("keepAliveTime", new Long(5000));
        threadPoolGBean.setAttribute("poolSize", new Integer(5));
        threadPoolGBean.setAttribute("poolName", "DefaultThreadPool");

        GBeanData transactionalTimerGBean = bootstrap.addGBean("TransactionalTimer", VMStoreThreadPooledTransactionalTimer.GBEAN_INFO);
        txTimerName = transactionalTimerGBean.getAbstractName();
        transactionalTimerGBean.setAttribute("repeatCount", new Integer(5));
        transactionalTimerGBean.setReferencePattern("TransactionContextManager", tcmName);
        transactionalTimerGBean.setReferencePattern("ThreadPool", threadPoolGBean.getAbstractName());

        GBeanData nonTransactionalTimerGBean = bootstrap.addGBean("NonTransactionTimer", VMStoreThreadPooledNonTransactionalTimer.GBEAN_INFO);
        nonTxTimerName = nonTransactionalTimerGBean.getAbstractName();
        nonTransactionalTimerGBean.setReferencePattern("ThreadPool", threadPoolGBean.getAbstractName());

        //resourceadapter
        GBeanData geronimoWorkManagerGBean = bootstrap.addGBean("WorkManager", GeronimoWorkManagerGBean.getGBeanInfo());
        geronimoWorkManagerGBean.setReferencePattern("SyncPool", threadPoolGBean.getAbstractName());
        geronimoWorkManagerGBean.setReferencePattern("StartPool", threadPoolGBean.getAbstractName());
        geronimoWorkManagerGBean.setReferencePattern("ScheduledPool", threadPoolGBean.getAbstractName());
        geronimoWorkManagerGBean.setReferencePattern("TransactionContextManager", tcmName);

        Map activationSpecInfoMap = new HashMap();
        ACTIVATION_SPEC_INFO.setAttribute("activationSpecClass", MockActivationSpec.class.getName());
        activationSpecInfoMap.put(javax.jms.MessageListener.class.getName(), ACTIVATION_SPEC_INFO);

        GBeanData moduleData = new GBeanData(createResourceAdapterModuleName(baseId), ResourceAdapterModuleImplGBean.GBEAN_INFO);
        bootstrap.addGBean(moduleData);
        moduleData.setAttribute("activationSpecInfoMap", activationSpecInfoMap);

        GBeanData resourceAdapterGBean = new GBeanData(createJCAResourceAdapterName(baseId), ResourceAdapterWrapperGBean.getGBeanInfo());
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
        GBeanData containerIndexGBean = bootstrap.addGBean("ContainerIndex", ContainerIndex.GBEAN_INFO);
        Set ejbContainerNames = new HashSet();
        ejbContainerNames.add(new AbstractNameQuery(null, Collections.singletonMap("j2eeType", "StatelessSessionBean")));
        ejbContainerNames.add(new AbstractNameQuery(null, Collections.singletonMap("j2eeType", "StatefulSessionBean")));
        ejbContainerNames.add(new AbstractNameQuery(null, Collections.singletonMap("j2eeType", "EntityBean")));
        containerIndexGBean.setReferencePatterns("EJBContainers", ejbContainerNames);

        //datasource
        dataSourceName = bootstrap.addGBean("DefaultDatasource", MockConnectionProxyFactory.GBEAN_INFO).getAbstractName();

        listenerName = bootstrap.addGBean("SoapListener", MockListener.GBEAN_INFO).getAbstractName();

        ConfigurationUtil.loadBootstrapConfiguration(kernel, bootstrap, getClass().getClassLoader());

        configurationManager = ConfigurationUtil.getEditableConfigurationManager(kernel);
        configStore = (ConfigurationStore) kernel.getGBean(configStoreName);
        configStore.install(bootstrap);

        defaultEnvironment.addDependency(baseId, ImportType.ALL);
        defaultEnvironment.setConfigId(testConfigurationArtifact);
    }


    private AbstractName createJCAResourceAdapterName(Artifact artifact) {
        AbstractName jcaResourceName = createJCAResourceName(artifact);
        return naming.createChildName(jcaResourceName, "MockRA", NameFactory.JCA_RESOURCE_ADAPTER);
    }

    protected AbstractName createJCAResourceName(Artifact artifact) {
        AbstractName moduleName = createResourceAdapterModuleName(artifact);
        AbstractName resourceAdapterName = naming.createChildName(moduleName, artifact.toString(), NameFactory.RESOURCE_ADAPTER);
        AbstractName jcaResourceName = naming.createChildName(resourceAdapterName, artifact.toString(), NameFactory.JCA_RESOURCE);
        return jcaResourceName;
    }

    private AbstractName createResourceAdapterModuleName(Artifact artifact) {
        AbstractName appName = naming.createRootName(artifact, NameFactory.NULL, NameFactory.J2EE_APPLICATION);
        AbstractName moduleName = naming.createChildName(appName, artifact.toString(), NameFactory.RESOURCE_ADAPTER_MODULE);
        return moduleName;
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
            Set matches = IOUtil.search(file, pattern);
            return matches;
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
