/*
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
package org.apache.openejb.assembler.classic;

import org.apache.geronimo.connector.GeronimoBootstrapContext;
import org.apache.geronimo.connector.outbound.AbstractConnectionManager;
import org.apache.geronimo.connector.work.GeronimoWorkManager;
import org.apache.geronimo.connector.work.HintsContextHandler;
import org.apache.geronimo.connector.work.TransactionContextHandler;
import org.apache.geronimo.connector.work.WorkContextHandler;
import org.apache.geronimo.transaction.manager.GeronimoTransactionManager;
import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;
import org.apache.openejb.BeanType;
import org.apache.openejb.ClassLoaderUtil;
import org.apache.openejb.Container;
import org.apache.openejb.DuplicateDeploymentIdException;
import org.apache.openejb.Injection;
import org.apache.openejb.JndiConstants;
import org.apache.openejb.MethodContext;
import org.apache.openejb.NoSuchApplicationException;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.UndeployException;
import org.apache.openejb.assembler.classic.event.AssemblerAfterApplicationCreated;
import org.apache.openejb.assembler.classic.event.AssemblerBeforeApplicationDestroyed;
import org.apache.openejb.assembler.classic.event.AssemblerCreated;
import org.apache.openejb.assembler.classic.event.AssemblerDestroyed;
import org.apache.openejb.assembler.classic.event.ContainerSystemPostCreate;
import org.apache.openejb.assembler.classic.event.ContainerSystemPreDestroy;
import org.apache.openejb.assembler.monitoring.JMXContainer;
import org.apache.openejb.async.AsynchronousPool;
import org.apache.openejb.cdi.CdiAppContextsService;
import org.apache.openejb.cdi.CdiBuilder;
import org.apache.openejb.cdi.CdiResourceInjectionService;
import org.apache.openejb.cdi.CdiScanner;
import org.apache.openejb.cdi.CustomELAdapter;
import org.apache.openejb.cdi.ManagedSecurityService;
import org.apache.openejb.cdi.OpenEJBTransactionService;
import org.apache.openejb.cdi.OptimizedLoaderService;
import org.apache.openejb.cdi.ThreadSingletonServiceImpl;
import org.apache.openejb.classloader.ClassLoaderConfigurer;
import org.apache.openejb.component.ClassLoaderEnricher;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.core.ConnectorReference;
import org.apache.openejb.core.CoreContainerSystem;
import org.apache.openejb.core.CoreUserTransaction;
import org.apache.openejb.core.JndiFactory;
import org.apache.openejb.core.ParentClassLoaderFinder;
import org.apache.openejb.core.SimpleTransactionSynchronizationRegistry;
import org.apache.openejb.core.TransactionSynchronizationRegistryWrapper;
import org.apache.openejb.core.WebContext;
import org.apache.openejb.core.ivm.naming.IvmContext;
import org.apache.openejb.core.ivm.naming.IvmJndiFactory;
import org.apache.openejb.core.security.SecurityContextHandler;
import org.apache.openejb.core.timer.EjbTimerServiceImpl;
import org.apache.openejb.core.timer.NullEjbTimerServiceImpl;
import org.apache.openejb.core.timer.ScheduleData;
import org.apache.openejb.core.timer.TimerStore;
import org.apache.openejb.core.transaction.JtaTransactionPolicyFactory;
import org.apache.openejb.core.transaction.SimpleBootstrapContext;
import org.apache.openejb.core.transaction.SimpleWorkManager;
import org.apache.openejb.core.transaction.TransactionPolicyFactory;
import org.apache.openejb.core.transaction.TransactionType;
import org.apache.openejb.javaagent.Agent;
import org.apache.openejb.jpa.integration.MakeTxLookup;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.JarLocation;
import org.apache.openejb.loader.Options;
import org.apache.openejb.loader.ProvisioningUtil;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.monitoring.DynamicMBeanWrapper;
import org.apache.openejb.monitoring.LocalMBeanServer;
import org.apache.openejb.monitoring.ObjectNameBuilder;
import org.apache.openejb.monitoring.remote.RemoteResourceMonitor;
import org.apache.openejb.observer.Observes;
import org.apache.openejb.persistence.JtaEntityManagerRegistry;
import org.apache.openejb.persistence.PersistenceClassLoaderHandler;
import org.apache.openejb.resource.GeronimoConnectionManagerFactory;
import org.apache.openejb.resource.jdbc.DataSourceFactory;
import org.apache.openejb.resource.jdbc.managed.local.ManagedDataSource;
import org.apache.openejb.spi.ApplicationServer;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.AsmParameterNameLoader;
import org.apache.openejb.util.Contexts;
import org.apache.openejb.util.EventHelper;
import org.apache.openejb.util.JndiTreeBrowser;
import org.apache.openejb.util.Join;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Messages;
import org.apache.openejb.util.OpenEJBErrorHandler;
import org.apache.openejb.util.PropertiesHelper;
import org.apache.openejb.util.PropertyPlaceHolderHelper;
import org.apache.openejb.util.References;
import org.apache.openejb.util.SafeToolkit;
import org.apache.openejb.util.proxy.ProxyFactory;
import org.apache.openejb.util.proxy.ProxyManager;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.logger.JULLoggerFactory;
import org.apache.webbeans.spi.ContainerLifecycle;
import org.apache.webbeans.spi.ContextsService;
import org.apache.webbeans.spi.LoaderService;
import org.apache.webbeans.spi.ResourceInjectionService;
import org.apache.webbeans.spi.ScannerService;
import org.apache.webbeans.spi.TransactionService;
import org.apache.webbeans.spi.adaptor.ELAdaptor;
import org.apache.xbean.finder.ResourceFinder;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;
import org.apache.xbean.recipe.UnsetPropertiesRecipe;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.XATerminator;
import javax.resource.spi.work.WorkManager;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings({"UnusedDeclaration", "UnqualifiedFieldAccess", "UnqualifiedMethodAccess"})
public class Assembler extends AssemblerTool implements org.apache.openejb.spi.Assembler, JndiConstants {

    static {
        AsmParameterNameLoader.install();
        // avoid linkage error on mac
        // adding just in case others run into in their tests
        JULLoggerFactory.class.getName();
    }

    public static final String OPENEJB_URL_PKG_PREFIX = IvmContext.class.getPackage().getName();
    public static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, Assembler.class);
    public static final String OPENEJB_JPA_DEPLOY_TIME_ENHANCEMENT_PROP = "openejb.jpa.deploy-time-enhancement";
    private static final String GLOBAL_UNIQUE_ID = "global";

    Messages messages = new Messages(Assembler.class.getPackage().getName());
    private final CoreContainerSystem containerSystem;
    private final PersistenceClassLoaderHandler persistenceClassLoaderHandler;
    private final JndiBuilder jndiBuilder;
    private TransactionManager transactionManager;
    private SecurityService securityService;
    protected OpenEjbConfigurationFactory configFactory;
    private final Map<String, AppInfo> deployedApplications = new HashMap<String, AppInfo>();
    private final Set<String> moduleIds = new HashSet<String>();
    private final Set<ObjectName> containerObjectNames = new HashSet<ObjectName>();
    private final RemoteResourceMonitor remoteResourceMonitor = new RemoteResourceMonitor();

    @Override
    public org.apache.openejb.spi.ContainerSystem getContainerSystem() {
        return containerSystem;
    }

    @Override
    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    @Override
    public SecurityService getSecurityService() {
        return securityService;
    }

    public synchronized void addDeploymentListener(final DeploymentListener deploymentListener) {
        logger.warning("DeploymentListener API is replaced by @Observes event");
        SystemInstance.get().addObserver(new DeploymentListenerObserver(deploymentListener));
    }

    public synchronized void removeDeploymentListener(final DeploymentListener deploymentListener) {
        // the wrapping is done here to get the correct equals/hashcode methods
        SystemInstance.get().removeObserver(new DeploymentListenerObserver(deploymentListener));
    }

    protected SafeToolkit toolkit = SafeToolkit.getToolkit("Assembler");
    protected OpenEjbConfiguration config;

    public Assembler() {
        this(new IvmJndiFactory());
    }

    public Assembler(final JndiFactory jndiFactory) {
        persistenceClassLoaderHandler = new PersistenceClassLoaderHandlerImpl();

        installNaming();

        final SystemInstance system = SystemInstance.get();

        system.setComponent(org.apache.openejb.spi.Assembler.class, this);
        system.setComponent(Assembler.class, this);

        containerSystem = new CoreContainerSystem(jndiFactory);
        system.setComponent(ContainerSystem.class, containerSystem);

        jndiBuilder = new JndiBuilder(containerSystem.getJNDIContext());

        setConfiguration(new OpenEjbConfiguration());

        final ApplicationServer appServer = system.getComponent(ApplicationServer.class);
        if (appServer == null) {
            system.setComponent(ApplicationServer.class, new org.apache.openejb.core.ServerFederation());
        }

        system.setComponent(EjbResolver.class, new EjbResolver(null, EjbResolver.Scope.GLOBAL));

        installExtensions();

        system.fireEvent(new AssemblerCreated());
    }

    private void installExtensions() {
        EventHelper.installExtensions(new ResourceFinder("META-INF"));
    }

    private void setConfiguration(final OpenEjbConfiguration config) {
        this.config = config;
        if (config.containerSystem == null) {
            config.containerSystem = new ContainerSystemInfo();
        }

        if (config.facilities == null) {
            config.facilities = new FacilitiesInfo();
        }

        SystemInstance.get().setComponent(OpenEjbConfiguration.class, this.config);
    }

    @Override
    public void init(final Properties props) throws OpenEJBException {
        this.props = new Properties(props);
        final Options options = new Options(props, SystemInstance.get().getOptions());
        final String className = options.get("openejb.configurator", "org.apache.openejb.config.ConfigurationFactory");

        if ("org.apache.openejb.config.ConfigurationFactory".equals(className)) {
            configFactory = new ConfigurationFactory(); // no need to use reflection
        } else {
            configFactory = (OpenEjbConfigurationFactory) toolkit.newInstance(className);
        }
        configFactory.init(props);
        SystemInstance.get().setComponent(OpenEjbConfigurationFactory.class, configFactory);
    }

    public static void installNaming() {
        if (SystemInstance.get().hasProperty("openejb.geronimo"))
            return;

        /* Add IntraVM JNDI service /////////////////////*/
        installNaming(OPENEJB_URL_PKG_PREFIX);
        /*\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*/
    }

    public static void installNaming(final String prefix) {
        installNaming(prefix, false);
    }

    public static synchronized void installNaming(final String prefix, final boolean clean) {
        final Properties systemProperties = System.getProperties();

        String str = systemProperties.getProperty(Context.URL_PKG_PREFIXES);
        if (str == null || clean) {
            str = prefix;
        } else if (!str.contains(prefix)) {
            str = str + ":" + prefix;
        }
        systemProperties.setProperty(Context.URL_PKG_PREFIXES, str);
    }

    private static final ThreadLocal<Map<String, Object>> context = new ThreadLocal<Map<String, Object>>();

    public static void setContext(final Map<String, Object> map) {
        context.set(map);
    }

    public static Map<String, Object> getContext() {
        Map<String, Object> map = context.get();
        if (map == null) {
            map = new HashMap<String, Object>();
            context.set(map);
        }
        return map;
    }

    @Override
    public void build() throws OpenEJBException {
        setContext(new HashMap<String, Object>());
        try {
            final OpenEjbConfiguration config = getOpenEjbConfiguration();
            buildContainerSystem(config);
        } catch (OpenEJBException ae) {
            /* OpenEJBExceptions contain useful information and are debbugable.
             * Let the exception pass through to the top and be logged.
             */
            throw ae;
        } catch (Exception e) {
            /* General Exceptions at this level are too generic and difficult to debug.
             * These exceptions are considered unknown bugs and are fatal.
             * If you get an error at this level, please trap and handle the error
             * where it is most relevant.
             */
            OpenEJBErrorHandler.handleUnknownError(e, "Assembler");
            throw new OpenEJBException(e);
        } finally {
            context.set(null);
        }
    }

    protected OpenEjbConfiguration getOpenEjbConfiguration() throws OpenEJBException {
        return configFactory.getOpenEjbConfiguration();
    }

    /////////////////////////////////////////////////////////////////////
    ////
    ////    Public Methods Used for Assembly
    ////
    /////////////////////////////////////////////////////////////////////

    /**
     * When given a complete OpenEjbConfiguration graph this method
     * will construct an entire container system and return a reference to that
     * container system, as ContainerSystem instance.
     * <p/>
     * This method leverage the other assemble and apply methods which
     * can be used independently.
     * <p/>
     * Assembles and returns the {@link org.apache.openejb.core.CoreContainerSystem} using the
     * information from the {@link OpenEjbConfiguration} object passed in.
     * <pre>
     * This method performs the following actions(in order):
     *
     * 1  Assembles ProxyFactory
     * 2  Assembles External JNDI Contexts
     * 3  Assembles TransactionService
     * 4  Assembles SecurityService
     * 5  Assembles ConnectionManagers
     * 6  Assembles Connectors
     * 7  Assembles Containers
     * 8  Assembles Applications
     * </pre>
     *
     * @param configInfo OpenEjbConfiguration
     * @throws Exception if there was a problem constructing the ContainerSystem.
     * @see OpenEjbConfiguration
     */
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public void buildContainerSystem(final OpenEjbConfiguration configInfo) throws Exception {
        if (SystemInstance.get().getOptions().get(OPENEJB_JPA_DEPLOY_TIME_ENHANCEMENT_PROP, false)) {
            SystemInstance.get().addObserver(new DeployTimeEnhancer());
        }

        for (final ServiceInfo serviceInfo : configInfo.facilities.services) {
            createService(serviceInfo);
        }

        final ContainerSystemInfo containerSystemInfo = configInfo.containerSystem;

        if (configInfo.facilities.intraVmServer != null) {
            createProxyFactory(configInfo.facilities.intraVmServer);
        }

        for (final JndiContextInfo contextInfo : configInfo.facilities.remoteJndiContexts) {
            createExternalContext(contextInfo);
        }

        createTransactionManager(configInfo.facilities.transactionService);

        createSecurityService(configInfo.facilities.securityService);

        for (final ResourceInfo resourceInfo : configInfo.facilities.resources) {
            createResource(resourceInfo);
        }

        // Containers
        for (final ContainerInfo serviceInfo : containerSystemInfo.containers) {
            createContainer(serviceInfo);
        }

        for (final AppInfo appInfo : containerSystemInfo.applications) {

            try {
                createApplication(appInfo, createAppClassLoader(appInfo));
            } catch (DuplicateDeploymentIdException e) {
                // already logged.
            } catch (Throwable e) {
                logger.error("appNotDeployed", e, appInfo.path);

                final DeploymentExceptionManager exceptionManager = SystemInstance.get().getComponent(DeploymentExceptionManager.class);
                if (exceptionManager != null && e instanceof Exception) {
                    exceptionManager.saveDeploymentException(appInfo, (Exception) e);
                }
            }
        }

        SystemInstance.get().fireEvent(new ContainerSystemPostCreate());
    }

    public boolean isDeployed(final String path) {
        return deployedApplications.containsKey(ProvisioningUtil.realLocation(path));
    }

    public Collection<AppInfo> getDeployedApplications() {
        return new ArrayList<AppInfo>(deployedApplications.values());
    }

    public AppContext createApplication(final EjbJarInfo ejbJar) throws NamingException, IOException, OpenEJBException {
        return createEjbJar(ejbJar);
    }

    public AppContext createEjbJar(final EjbJarInfo ejbJar) throws NamingException, IOException, OpenEJBException {
        final AppInfo appInfo = new AppInfo();
        appInfo.path = ejbJar.path;
        appInfo.appId = ejbJar.moduleName;
        appInfo.ejbJars.add(ejbJar);
        return createApplication(appInfo);
    }

    public AppContext createApplication(final EjbJarInfo ejbJar, final ClassLoader classLoader) throws NamingException, IOException, OpenEJBException {
        return createEjbJar(ejbJar, classLoader);
    }

    public AppContext createEjbJar(final EjbJarInfo ejbJar, final ClassLoader classLoader) throws NamingException, IOException, OpenEJBException {
        final AppInfo appInfo = new AppInfo();
        appInfo.path = ejbJar.path;
        appInfo.appId = ejbJar.moduleName;
        appInfo.ejbJars.add(ejbJar);
        return createApplication(appInfo, classLoader);
    }

    public AppContext createClient(final ClientInfo clientInfo) throws NamingException, IOException, OpenEJBException {
        final AppInfo appInfo = new AppInfo();
        appInfo.path = clientInfo.path;
        appInfo.appId = clientInfo.moduleId;
        appInfo.clients.add(clientInfo);
        return createApplication(appInfo);
    }

    public AppContext createClient(final ClientInfo clientInfo, final ClassLoader classLoader) throws NamingException, IOException, OpenEJBException {
        final AppInfo appInfo = new AppInfo();
        appInfo.path = clientInfo.path;
        appInfo.appId = clientInfo.moduleId;
        appInfo.clients.add(clientInfo);
        return createApplication(appInfo, classLoader);
    }

    public AppContext createConnector(final ConnectorInfo connectorInfo) throws NamingException, IOException, OpenEJBException {
        final AppInfo appInfo = new AppInfo();
        appInfo.path = connectorInfo.path;
        appInfo.appId = connectorInfo.moduleId;
        appInfo.connectors.add(connectorInfo);
        return createApplication(appInfo);
    }

    public AppContext createConnector(final ConnectorInfo connectorInfo, final ClassLoader classLoader) throws NamingException, IOException, OpenEJBException {
        final AppInfo appInfo = new AppInfo();
        appInfo.path = connectorInfo.path;
        appInfo.appId = connectorInfo.moduleId;
        appInfo.connectors.add(connectorInfo);
        return createApplication(appInfo, classLoader);
    }

    public AppContext createWebApp(final WebAppInfo webAppInfo) throws NamingException, IOException, OpenEJBException {
        final AppInfo appInfo = new AppInfo();
        appInfo.path = webAppInfo.path;
        appInfo.appId = webAppInfo.moduleId;
        appInfo.webApps.add(webAppInfo);
        return createApplication(appInfo);
    }

    public AppContext createWebApp(final WebAppInfo webAppInfo, final ClassLoader classLoader) throws NamingException, IOException, OpenEJBException {
        final AppInfo appInfo = new AppInfo();
        appInfo.path = webAppInfo.path;
        appInfo.appId = webAppInfo.moduleId;
        appInfo.webApps.add(webAppInfo);
        return createApplication(appInfo, classLoader);
    }

    public AppContext createApplication(final AppInfo appInfo) throws OpenEJBException, IOException, NamingException {
        return createApplication(appInfo, createAppClassLoader(appInfo));
    }

    public AppContext createApplication(final AppInfo appInfo, final ClassLoader classLoader) throws OpenEJBException, IOException, NamingException {
        return createApplication(appInfo, classLoader, true);
    }

    public AppContext createApplication(final AppInfo appInfo, ClassLoader classLoader, final boolean start) throws OpenEJBException, IOException, NamingException {
        // The path is used in the UrlCache, command line deployer, JNDI name templates, tomcat integration and a few other places
        if (appInfo.appId == null) {
            throw new IllegalArgumentException("AppInfo.appId cannot be null");
        }
        if (appInfo.path == null) {
            appInfo.path = appInfo.appId;
        }

        logger.info("createApplication.start", appInfo.path);

        //        try {
        //            Thread.sleep(5000);
        //        } catch (InterruptedException e) {
        //            e.printStackTrace();
        //            Thread.interrupted();
        //        }

        // To start out, ensure we don't already have any beans deployed with duplicate IDs.  This
        // is a conflict we can't handle.
        final List<String> used = new ArrayList<String>();
        for (final EjbJarInfo ejbJarInfo : appInfo.ejbJars) {
            for (final EnterpriseBeanInfo beanInfo : ejbJarInfo.enterpriseBeans) {
                if (containerSystem.getBeanContext(beanInfo.ejbDeploymentId) != null) {
                    used.add(beanInfo.ejbDeploymentId);
                }
            }
        }

        if (used.size() > 0) {
            String message = logger.error("createApplication.appFailedDuplicateIds", appInfo.path);
            for (final String id : used) {
                logger.debug("createApplication.deploymentIdInUse", id);
                message += "\n    " + id;
            }
            throw new DuplicateDeploymentIdException(message);
        }

        //Construct the global and app jndi contexts for this app
        final InjectionBuilder injectionBuilder = new InjectionBuilder(classLoader);

        final Set<Injection> injections = new HashSet<Injection>();
        injections.addAll(injectionBuilder.buildInjections(appInfo.globalJndiEnc));
        injections.addAll(injectionBuilder.buildInjections(appInfo.appJndiEnc));

        final JndiEncBuilder globalBuilder = new JndiEncBuilder(appInfo.globalJndiEnc, injections, appInfo.appId, null, GLOBAL_UNIQUE_ID, classLoader);
        final Map<String, Object> globalBindings = globalBuilder.buildBindings(JndiEncBuilder.JndiScope.global);
        final Context globalJndiContext = globalBuilder.build(globalBindings);

        final JndiEncBuilder appBuilder = new JndiEncBuilder(appInfo.appJndiEnc, injections, appInfo.appId, null, appInfo.appId, classLoader);
        final Map<String, Object> appBindings = appBuilder.buildBindings(JndiEncBuilder.JndiScope.app);
        final Context appJndiContext = appBuilder.build(appBindings);

        try {
            // Generate the cmp2/cmp1 concrete subclasses
            final CmpJarBuilder cmpJarBuilder = new CmpJarBuilder(appInfo, classLoader);
            final File generatedJar = cmpJarBuilder.getJarFile();
            if (generatedJar != null) {
                classLoader = ClassLoaderUtil.createClassLoader(appInfo.path, new URL[]{generatedJar.toURI().toURL()}, classLoader);
            }

            final AppContext appContext = new AppContext(appInfo.appId, SystemInstance.get(), classLoader, globalJndiContext, appJndiContext, appInfo.standaloneModule);
            appContext.getProperties().putAll(appInfo.properties);
            appContext.getInjections().addAll(injections);
            appContext.getBindings().putAll(globalBindings);
            appContext.getBindings().putAll(appBindings);

            containerSystem.addAppContext(appContext);

            appContext.set(AsynchronousPool.class, AsynchronousPool.create(appContext));

            final Context containerSystemContext = containerSystem.getJNDIContext();

            if (!SystemInstance.get().hasProperty("openejb.geronimo")) {
                // Bean Validation
                // ValidatorFactory needs to be put in the map sent to the entity manager factory
                // so it has to be constructed before
                final List<CommonInfoObject> vfs = new ArrayList<CommonInfoObject>();
                for (final ClientInfo clientInfo : appInfo.clients) {
                    vfs.add(clientInfo);
                }
                for (final ConnectorInfo connectorInfo : appInfo.connectors) {
                    vfs.add(connectorInfo);
                }
                for (final EjbJarInfo ejbJarInfo : appInfo.ejbJars) {
                    vfs.add(ejbJarInfo);
                }
                for (final WebAppInfo webAppInfo : appInfo.webApps) {
                    vfs.add(webAppInfo);
                }

                final Map<String, ValidatorFactory> validatorFactories = new HashMap<String, ValidatorFactory>();
                for (final CommonInfoObject info : vfs) {
                    ValidatorFactory factory = null;
                    try {
                        factory = ValidatorBuilder.buildFactory(classLoader, info.validationInfo);
                    } catch (ValidationException ve) {
                        logger.warning("can't build the validation factory for module " + info.uniqueId, ve);
                    }
                    if (factory != null) {
                        validatorFactories.put(info.uniqueId, factory);
                    }
                }
                moduleIds.addAll(validatorFactories.keySet());

                // validators bindings
                for (final Entry<String, ValidatorFactory> validatorFactory : validatorFactories.entrySet()) {
                    final String id = validatorFactory.getKey();
                    final ValidatorFactory factory = validatorFactory.getValue();
                    try {
                        containerSystemContext.bind(VALIDATOR_FACTORY_NAMING_CONTEXT + id, factory);

                        Validator validator;
                        try {
                            validator = factory.usingContext().getValidator();
                        } catch (Exception e) {
                            validator = (Validator) Proxy.newProxyInstance(appContext.getClassLoader(), new Class<?>[]{Validator.class}, new LazyValidator(factory));
                        }

                        containerSystemContext.bind(VALIDATOR_NAMING_CONTEXT + id, validator);
                    } catch (NameAlreadyBoundException e) {
                        throw new OpenEJBException("ValidatorFactory already exists for module " + id, e);
                    } catch (Exception e) {
                        throw new OpenEJBException(e);
                    }
                }
            }

            // JPA - Persistence Units MUST be processed first since they will add ClassFileTransformers
            // to the class loader which must be added before any classes are loaded
            final Map<String, String> units = new HashMap<String, String>();
            final PersistenceBuilder persistenceBuilder = new PersistenceBuilder(persistenceClassLoaderHandler);
            for (final PersistenceUnitInfo info : appInfo.persistenceUnits) {
                final ReloadableEntityManagerFactory factory;
                try {
                    factory = persistenceBuilder.createEntityManagerFactory(info, classLoader);
                    containerSystem.getJNDIContext().bind(PERSISTENCE_UNIT_NAMING_CONTEXT + info.id, factory);
                    units.put(info.name, PERSISTENCE_UNIT_NAMING_CONTEXT + info.id);
                } catch (NameAlreadyBoundException e) {
                    throw new OpenEJBException("PersistenceUnit already deployed: " + info.persistenceUnitRootUrl);
                } catch (Exception e) {
                    throw new OpenEJBException(e);
                }

                factory.register();
            }

            logger.debug("Loaded peristence units: " + units);

            // Connectors
            for (final ConnectorInfo connector : appInfo.connectors) {
                final ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(classLoader);
                try {
                    // todo add undeployment code for these
                    if (connector.resourceAdapter != null) {
                        createResource(connector.resourceAdapter);
                    }
                    for (final ResourceInfo outbound : connector.outbound) {
                        createResource(outbound);
                    }
                    for (final MdbContainerInfo inbound : connector.inbound) {
                        createContainer(inbound);
                    }
                    for (final ResourceInfo adminObject : connector.adminObject) {
                        createResource(adminObject);
                    }
                } finally {
                    Thread.currentThread().setContextClassLoader(oldClassLoader);
                }
            }

            final List<BeanContext> allDeployments = initEjbs(classLoader, appInfo, appContext, injections, new ArrayList<BeanContext>(), null);

            new CdiBuilder().build(appInfo, appContext, allDeployments);

            ensureWebBeansContext(appContext);

            appJndiContext.bind("app/BeanManager", appContext.getBeanManager());
            appContext.getBindings().put("app/BeanManager", appContext.getBeanManager());

            startEjbs(start, allDeployments);

            // App Client
            for (final ClientInfo clientInfo : appInfo.clients) {
                // determine the injections
                final List<Injection> clientInjections = injectionBuilder.buildInjections(clientInfo.jndiEnc);

                // build the enc
                final JndiEncBuilder jndiEncBuilder = new JndiEncBuilder(clientInfo.jndiEnc, clientInjections, "Bean", clientInfo.moduleId, null, clientInfo.uniqueId, classLoader);
                // if there is at least a remote client classes
                // or if there is no local client classes
                // then, we can set the client flag
                if ((clientInfo.remoteClients.size() > 0) || (clientInfo.localClients.size() == 0)) {
                    jndiEncBuilder.setClient(true);

                }
                jndiEncBuilder.setUseCrossClassLoaderRef(false);
                final Context context = jndiEncBuilder.build(JndiEncBuilder.JndiScope.comp);

                //                Debug.printContext(context);

                containerSystemContext.bind("openejb/client/" + clientInfo.moduleId, context);

                if (clientInfo.path != null) {
                    context.bind("info/path", clientInfo.path);
                }
                if (clientInfo.mainClass != null) {
                    context.bind("info/mainClass", clientInfo.mainClass);
                }
                if (clientInfo.callbackHandler != null) {
                    context.bind("info/callbackHandler", clientInfo.callbackHandler);
                }
                context.bind("info/injections", clientInjections);

                for (final String clientClassName : clientInfo.remoteClients) {
                    containerSystemContext.bind("openejb/client/" + clientClassName, clientInfo.moduleId);
                }

                for (final String clientClassName : clientInfo.localClients) {
                    containerSystemContext.bind("openejb/client/" + clientClassName, clientInfo.moduleId);
                    logger.getChildLogger("client").info("createApplication.createLocalClient", clientClassName, clientInfo.moduleId);
                }
            }

            final SystemInstance systemInstance = SystemInstance.get();

            // WebApp

            final WebAppBuilder webAppBuilder = systemInstance.getComponent(WebAppBuilder.class);
            if (webAppBuilder != null) {
                webAppBuilder.deployWebApps(appInfo, classLoader);
            }

            if (start) {
                final EjbResolver globalEjbResolver = systemInstance.getComponent(EjbResolver.class);
                globalEjbResolver.addAll(appInfo.ejbJars);
            }

            // bind all global values on global context
            for (final Map.Entry<String, Object> value : appContext.getBindings().entrySet()) {
                final String path = value.getKey();
                // keep only global bindings
                if (path.startsWith("module/") || path.startsWith("app/") || path.startsWith("comp/") || path.equalsIgnoreCase("global/dummy")) {
                    continue;
                }

                // a bit weird but just to be consistent if user doesn't lookup directly the resource
                final Context lastContext = Contexts.createSubcontexts(containerSystemContext, path);
                try {
                    lastContext.rebind(path.substring(path.lastIndexOf("/") + 1, path.length()), value.getValue());
                } catch (NameAlreadyBoundException nabe) {
                    nabe.printStackTrace();
                }
                containerSystemContext.rebind(path, value.getValue());
            }

            // deploy MBeans
            for (final String mbean : appInfo.mbeans) {
                deployMBean(appContext.getWebBeansContext(), classLoader, mbean, appInfo.jmx, appInfo.appId);
            }
            for (final EjbJarInfo ejbJarInfo : appInfo.ejbJars) {
                for (final String mbean : ejbJarInfo.mbeans) {
                    deployMBean(appContext.getWebBeansContext(), classLoader, mbean, appInfo.jmx, ejbJarInfo.moduleName);
                }
            }
            for (final ConnectorInfo connectorInfo : appInfo.connectors) {
                for (final String mbean : connectorInfo.mbeans) {
                    deployMBean(appContext.getWebBeansContext(), classLoader, mbean, appInfo.jmx, appInfo.appId + ".add-lib");
                }
            }


            deployedApplications.put(appInfo.path, appInfo);
            systemInstance.fireEvent(new AssemblerAfterApplicationCreated(appInfo));

            logger.info("createApplication.success", appInfo.path);

            return appContext;
        } catch (ValidationException ve) {
            throw ve;
        } catch (Throwable t) {
            try {
                destroyApplication(appInfo);
            } catch (Exception e1) {
                logger.debug("createApplication.undeployFailed", e1, appInfo.path);
            }
            throw new OpenEJBException(messages.format("createApplication.failed", appInfo.path), t);
        }
    }

    public List<BeanContext> initEjbs(final ClassLoader classLoader, final AppInfo appInfo, final AppContext appContext,
                         final Set<Injection> injections, final List<BeanContext> allDeployments, final String webappId) throws OpenEJBException {
        final EjbJarBuilder ejbJarBuilder = new EjbJarBuilder(props, appContext);
        for (final EjbJarInfo ejbJar : appInfo.ejbJars) {
            boolean skip = false;
            if (!appInfo.webAppAlone) {
                for (final WebAppInfo webapp : appInfo.webApps) {
                    if ((webappId == null && ejbJar.moduleId.equals(webapp.moduleId))
                            || (webappId != null && !ejbJar.moduleId.equals(webappId))) {
                        skip = true;
                    }
                }
            }
            if (skip) {
                continue;
            }

            final HashMap<String, BeanContext> deployments = ejbJarBuilder.build(ejbJar, injections, classLoader);

            final JaccPermissionsBuilder jaccPermissionsBuilder = new JaccPermissionsBuilder();
            final PolicyContext policyContext = jaccPermissionsBuilder.build(ejbJar, deployments);
            jaccPermissionsBuilder.install(policyContext);

            final TransactionPolicyFactory transactionPolicyFactory = createTransactionPolicyFactory(ejbJar, classLoader);
            for (final BeanContext beanContext : deployments.values()) {
                beanContext.setTransactionPolicyFactory(transactionPolicyFactory);
            }

            final MethodTransactionBuilder methodTransactionBuilder = new MethodTransactionBuilder();
            methodTransactionBuilder.build(deployments, ejbJar.methodTransactions);

            final MethodConcurrencyBuilder methodConcurrencyBuilder = new MethodConcurrencyBuilder();
            methodConcurrencyBuilder.build(deployments, ejbJar.methodConcurrency);

            for (final BeanContext beanContext : deployments.values()) {
                containerSystem.addDeployment(beanContext);
            }

            //bind ejbs into global jndi
            jndiBuilder.build(ejbJar, deployments);

            // setup timers/asynchronous methods - must be after transaction attributes are set
            for (final BeanContext beanContext : deployments.values()) {
                if (beanContext.getComponentType() != BeanType.STATEFUL) {
                    final Method ejbTimeout = beanContext.getEjbTimeout();
                    boolean timerServiceRequired = false;
                    if (ejbTimeout != null) {
                        // If user set the tx attribute to RequiresNew change it to Required so a new transaction is not started
                        if (beanContext.getTransactionType(ejbTimeout) == TransactionType.RequiresNew) {
                            beanContext.setMethodTransactionAttribute(ejbTimeout, TransactionType.Required);
                        }
                        timerServiceRequired = true;
                    }
                    for (Iterator<Map.Entry<Method, MethodContext>> it = beanContext.iteratorMethodContext(); it.hasNext(); ) {
                        final Map.Entry<Method, MethodContext> entry = it.next();
                        final MethodContext methodContext = entry.getValue();
                        if (methodContext.getSchedules().size() > 0) {
                            timerServiceRequired = true;
                            final Method method = entry.getKey();
                            //TODO Need ?
                            if (beanContext.getTransactionType(method) == TransactionType.RequiresNew) {
                                beanContext.setMethodTransactionAttribute(method, TransactionType.Required);
                            }
                        }
                    }
                    if (timerServiceRequired) {
                        // Create the timer
                        final EjbTimerServiceImpl timerService = new EjbTimerServiceImpl(beanContext);
                        //Load auto-start timers
                        final TimerStore timerStore = timerService.getTimerStore();
                        for (Iterator<Map.Entry<Method, MethodContext>> it = beanContext.iteratorMethodContext(); it.hasNext(); ) {
                            final Map.Entry<Method, MethodContext> entry = it.next();
                            final MethodContext methodContext = entry.getValue();
                            for (final ScheduleData scheduleData : methodContext.getSchedules()) {
                                timerStore.createCalendarTimer(timerService, (String) beanContext.getDeploymentID(), null, entry.getKey(), scheduleData.getExpression(), scheduleData.getConfig());
                            }
                        }
                        beanContext.setEjbTimerService(timerService);
                    } else {
                        beanContext.setEjbTimerService(new NullEjbTimerServiceImpl());
                    }
                }
                //set asynchronous methods transaction
                //TODO ???
                for (Iterator<Entry<Method, MethodContext>> it = beanContext.iteratorMethodContext(); it.hasNext(); ) {
                    final Entry<Method, MethodContext> entry = it.next();
                    if (entry.getValue().isAsynchronous() && beanContext.getTransactionType(entry.getKey()) == TransactionType.RequiresNew) {
                        beanContext.setMethodTransactionAttribute(entry.getKey(), TransactionType.Required);
                    }
                }
            }
            // process application exceptions
            for (final ApplicationExceptionInfo exceptionInfo : ejbJar.applicationException) {
                try {
                    final Class exceptionClass = classLoader.loadClass(exceptionInfo.exceptionClass);
                    for (final BeanContext beanContext : deployments.values()) {
                        beanContext.addApplicationException(exceptionClass, exceptionInfo.rollback, exceptionInfo.inherited);
                    }
                } catch (ClassNotFoundException e) {
                    logger.error("createApplication.invalidClass", e, exceptionInfo.exceptionClass, e.getMessage());
                }
            }

            allDeployments.addAll(deployments.values());
        }

        final List<BeanContext> ejbs = sort(allDeployments);
        appContext.getBeanContexts().addAll(ejbs);
        return ejbs;
    }

    public void startEjbs(final boolean start, final List<BeanContext> allDeployments) throws OpenEJBException {
        // now that everything is configured, deploy to the container
        if (start) {
            final Collection<BeanContext> toStart = new ArrayList<BeanContext>();

            // deploy
            for (final BeanContext deployment : allDeployments) {
                try {
                    final Container container = deployment.getContainer();
                    if (container.getBeanContext(deployment.getDeploymentID()) == null) {
                        container.deploy(deployment);
                        if (!((String) deployment.getDeploymentID()).endsWith(".Comp")
                                && !deployment.isHidden()) {
                            logger.info("createApplication.createdEjb", deployment.getDeploymentID(), deployment.getEjbName(), container.getContainerID());
                        }
                        if (logger.isDebugEnabled()) {
                            for (final Map.Entry<Object, Object> entry : deployment.getProperties().entrySet()) {
                                logger.info("createApplication.createdEjb.property", deployment.getEjbName(), entry.getKey(), entry.getValue());
                            }
                        }
                        toStart.add(deployment);
                    }
                } catch (Throwable t) {
                    throw new OpenEJBException("Error deploying '" + deployment.getEjbName() + "'.  Exception: " + t.getClass() + ": " + t.getMessage(), t);
                }
            }

            // start
            for (final BeanContext deployment : toStart) {
                try {
                    final Container container = deployment.getContainer();
                    container.start(deployment);
                    if (!((String) deployment.getDeploymentID()).endsWith(".Comp")
                            && !deployment.isHidden()) {
                        logger.info("createApplication.startedEjb", deployment.getDeploymentID(), deployment.getEjbName(), container.getContainerID());
                    }
                } catch (Throwable t) {
                    throw new OpenEJBException("Error starting '" + deployment.getEjbName() + "'.  Exception: " + t.getClass() + ": " + t.getMessage(), t);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void deployMBean(final WebBeansContext wc, final ClassLoader cl, final String mbeanClass, final Properties appMbeans, final String id) {
        final Class<?> clazz;
        try {
            clazz = cl.loadClass(mbeanClass);
        } catch (ClassNotFoundException e) {
            throw new OpenEJBRuntimeException(e);
        }
        final BeanManager bm = wc.getBeanManagerImpl();
        final Set<Bean<?>> beans = bm.getBeans(clazz);
        final Bean bean = bm.resolve(beans);
        final Object instance;
        if (bean == null) {
            try {
                instance = clazz.newInstance();
            } catch (InstantiationException e) {
                logger.error("the mbean " + mbeanClass + " can't be registered because it can't be instantiated", e);
                return;
            } catch (IllegalAccessException e) {
                logger.error("the mbean " + mbeanClass + " can't be registered because it can't be accessed", e);
                return;
            }
        } else {
            final CreationalContext creationalContext = bm.createCreationalContext(bean);
            instance = bm.getReference(bean, clazz, creationalContext);
            if (Dependent.class.equals(bean.getScope())) {
                creationalContext.release();
            }
        }

        if (LocalMBeanServer.isJMXActive()) {
            final MBeanServer server = LocalMBeanServer.get();
            try {
                final ObjectName leaf = new ObjectNameBuilder("openejb.user.mbeans")
                        .set("application", id)
                        .set("group", clazz.getPackage().getName())
                        .set("name", clazz.getSimpleName())
                        .build();

                server.registerMBean(new DynamicMBeanWrapper(wc, instance), leaf);
                appMbeans.put(mbeanClass, leaf.getCanonicalName());
                logger.info("Deployed MBean(" + leaf.getCanonicalName() + ")");
            } catch (Exception e) {
                logger.error("the mbean " + mbeanClass + " can't be registered", e);
            }
        }
    }

    private void ensureWebBeansContext(final AppContext appContext) {
        WebBeansContext webBeansContext = appContext.get(WebBeansContext.class);
        if (webBeansContext == null)
            webBeansContext = appContext.getWebBeansContext();
        if (webBeansContext == null) {

            final Map<Class<?>, Object> services = new HashMap<Class<?>, Object>();

            services.put(AppContext.class, appContext);
            services.put(TransactionService.class, new OpenEJBTransactionService());
            services.put(ContextsService.class, new CdiAppContextsService(webBeansContext, true));
            services.put(ResourceInjectionService.class, new CdiResourceInjectionService());
            services.put(ScannerService.class, new CdiScanner());
            services.put(ELAdaptor.class, new CustomELAdapter(appContext));
            services.put(LoaderService.class, new OptimizedLoaderService());
            services.put(org.apache.webbeans.proxy.ProxyFactory.class, new org.apache.webbeans.proxy.ProxyFactory(ThreadSingletonServiceImpl.owbProxyFactory()));
            final Properties properties = new Properties();
            properties.setProperty(org.apache.webbeans.spi.SecurityService.class.getName(), ManagedSecurityService.class.getName());
            webBeansContext = new WebBeansContext(services, properties);
            appContext.setCdiEnabled(false);
        }

        appContext.set(WebBeansContext.class, webBeansContext);
        appContext.setWebBeansContext(webBeansContext);
    }

    private TransactionPolicyFactory createTransactionPolicyFactory(final EjbJarInfo ejbJar, final ClassLoader classLoader) {
        TransactionPolicyFactory factory = null;

        final Object value = ejbJar.properties.get(TransactionPolicyFactory.class.getName());
        if (value instanceof TransactionPolicyFactory) {
            factory = (TransactionPolicyFactory) value;
        } else if (value instanceof String) {
            try {
                final String[] parts = ((String) value).split(":", 2);

                final ResourceFinder finder = new ResourceFinder("META-INF", classLoader);
                final Map<String, Class<? extends TransactionPolicyFactory>> plugins = finder.mapAvailableImplementations(TransactionPolicyFactory.class);
                final Class<? extends TransactionPolicyFactory> clazz = plugins.get(parts[0]);
                if (clazz != null) {
                    if (parts.length == 1) {
                        factory = clazz.getConstructor(String.class).newInstance(parts[1]);
                    } else {
                        factory = clazz.newInstance();
                    }
                }
            } catch (Exception ignored) {
                // couldn't determine the plugins, which isn't fatal
            }
        }

        if (factory == null) {
            factory = new JtaTransactionPolicyFactory(transactionManager);
        }
        return factory;
    }

    private static List<BeanContext> sort(List<BeanContext> deployments) {
        // Sort all the singletons to the back of the list.  We want to make sure
        // all non-singletons are created first so that if a singleton refers to them
        // they are available.
        Collections.sort(deployments, new Comparator<BeanContext>() {
            @Override
            public int compare(final BeanContext a, final BeanContext b) {
                final int aa = (a.getComponentType() == BeanType.SINGLETON) ? 1 : 0;
                final int bb = (b.getComponentType() == BeanType.SINGLETON) ? 1 : 0;
                return aa - bb;
            }
        });

        // Sort all the beans with references to the back of the list.  Beans
        // without references to ther beans will be deployed first.
        deployments = References.sort(deployments, new References.Visitor<BeanContext>() {
            @Override
            public String getName(final BeanContext t) {
                return (String) t.getDeploymentID();
            }

            @Override
            public Set<String> getReferences(final BeanContext t) {
                return t.getDependsOn();
            }
        });

        // Now Sort all the MDBs to the back of the list.  The Resource Adapter
        // may attempt to use the MDB on endpointActivation and the MDB may have
        // references to other ejbs that would need to be available first.
        Collections.sort(deployments, new Comparator<BeanContext>() {
            @Override
            public int compare(final BeanContext a, final BeanContext b) {
                final int aa = (a.getComponentType() == BeanType.MESSAGE_DRIVEN) ? 1 : 0;
                final int bb = (b.getComponentType() == BeanType.MESSAGE_DRIVEN) ? 1 : 0;
                return aa - bb;
            }
        });

        return deployments;
    }

    @Override
    public synchronized void destroy() {

        SystemInstance.get().fireEvent(new ContainerSystemPreDestroy());

        try {
            EjbTimerServiceImpl.shutdown();
        } catch (Exception e) {
            logger.warning("Unable to shutdown scheduler", e);
        }

        logger.debug("Undeploying Applications");
        final Assembler assembler = this;
        for (final AppInfo appInfo : assembler.getDeployedApplications()) {
            try {
                assembler.destroyApplication(appInfo.path);
            } catch (UndeployException e) {
                logger.error("Undeployment failed: " + appInfo.path, e);
            } catch (NoSuchApplicationException e) {
                //Ignore
            }
        }

        final Iterator<ObjectName> it = containerObjectNames.iterator();
        final MBeanServer server = LocalMBeanServer.get();
        while (it.hasNext()) {
            try {
                server.unregisterMBean(it.next());
            } catch (Exception ignored) {
                // no-op
            }
            it.remove();
        }
        try {
            remoteResourceMonitor.unregister();
        } catch (Exception ignored) {
            // no-op
        }

        NamingEnumeration<Binding> namingEnumeration = null;
        try {
            namingEnumeration = containerSystem.getJNDIContext().listBindings("openejb/Resource");
        } catch (NamingException ignored) {
            // no resource adapters were created
        }
        while (namingEnumeration != null && namingEnumeration.hasMoreElements()) {
            final Binding binding = namingEnumeration.nextElement();
            final Object object = binding.getObject();
            destroyResource(binding.getName(), binding.getClassName(), object);
        }

        try {
            containerSystem.getJNDIContext().unbind("java:global");
        } catch (NamingException ignored) {
            // no-op
        }

        SystemInstance.get().removeComponent(OpenEjbConfiguration.class);
        SystemInstance.get().removeComponent(JtaEntityManagerRegistry.class);
        SystemInstance.get().removeComponent(TransactionSynchronizationRegistry.class);
        SystemInstance.get().removeComponent(EjbResolver.class);
        SystemInstance.get().fireEvent(new AssemblerDestroyed());
        SystemInstance.reset();
    }

    private void destroyResource(final String name, final String className, final Object object) {
        if (object instanceof ResourceAdapter) {
            final ResourceAdapter resourceAdapter = (ResourceAdapter) object;
            try {
                logger.info("Stopping ResourceAdapter: " + name);

                if (logger.isDebugEnabled()) {
                    logger.debug("Stopping ResourceAdapter: " + className);
                }

                resourceAdapter.stop();
            } catch (Throwable t) {
                logger.fatal("ResourceAdapter Shutdown Failed: " + name, t);
            }
        } else if (DataSourceFactory.knows(object)) {
            logger.info("Closing DataSource: " + name);

            try {
                DataSourceFactory.destroy(object);
            } catch (Throwable t) {
                //Ignore
            }

            if (object instanceof ManagedDataSource) {
                ((ManagedDataSource) object).clean();
            }

        } else if (object instanceof ConnectorReference) {
            final ConnectorReference cr = (ConnectorReference) object;
            try {
                final ConnectionManager cm = cr.getConnectionManager();
                if (cm != null && cm instanceof AbstractConnectionManager) {
                    ((AbstractConnectionManager) cm).doStop();
                }
            } catch (Exception e) {
                logger.debug("Not processing resource on destroy: " + className, e);
            }
        } else if (logger.isDebugEnabled()) {
            logger.debug("Not processing resource on destroy: " + className);
        }
    }

    public synchronized void destroyApplication(final String filePath) throws UndeployException, NoSuchApplicationException {
        final AppInfo appInfo = deployedApplications.remove(filePath);
        if (appInfo == null) {
            throw new NoSuchApplicationException(filePath);
        }
        destroyApplication(appInfo);
    }

    public synchronized void destroyApplication(final AppContext appContext) throws UndeployException {
        final AppInfo appInfo = deployedApplications.remove(appContext.getId());
        if (appInfo == null) {
            throw new IllegalStateException(String.format("Cannot find AppInfo for app: %s", appContext.getId()));
        }
        destroyApplication(appInfo);
    }

    public synchronized void destroyApplication(final AppInfo appInfo) throws UndeployException {
        deployedApplications.remove(appInfo.path);
        logger.info("destroyApplication.start", appInfo.path);

        SystemInstance.get().fireEvent(new AssemblerBeforeApplicationDestroyed(appInfo));

        final Context globalContext = containerSystem.getJNDIContext();
        final AppContext appContext = containerSystem.getAppContext(appInfo.appId);

        if (null == appContext) {
            logger.warning("Application id '" + appInfo.appId + "' not found in: " + Arrays.toString(containerSystem.getAppContextKeys()));
            return;
        } else {

            final WebBeansContext webBeansContext = appContext.getWebBeansContext();
            if (webBeansContext != null) {
                final ClassLoader old = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(appContext.getClassLoader());
                try {
                    webBeansContext.getService(ContainerLifecycle.class).stopApplication(null);
                } finally {
                    Thread.currentThread().setContextClassLoader(old);
                }
            }

            final Map<String, Object> cb = appContext.getBindings();

            // dumpJndiTree(globalContext, "\n\nJndi Tree Before unbinds:\n===================\n\n");

            for (final Map.Entry<String, Object> value : cb.entrySet()) {
                String path = value.getKey();
                if (path.startsWith("global")) {
                    path = "java:" + path;
                }
                if (!path.startsWith("java:global")) {
                    continue;
                }

                unbind(globalContext, path);
                unbind(globalContext, "openejb/global/" + path.substring("java:".length()));
                unbind(globalContext, path.substring("java:global".length()));
            }

            if (appInfo.appId != null && !appInfo.appId.isEmpty() && !"openejb".equals(appInfo.appId)) {
                unbind(globalContext, "global/" + appInfo.appId);
                unbind(globalContext, appInfo.appId);
            }

            // dumpJndiTree(globalContext, "\n\nJndi Tree After unbinds:\n======================\n\n");
        }

        final EjbResolver globalResolver = new EjbResolver(null, EjbResolver.Scope.GLOBAL);
        for (final AppInfo info : deployedApplications.values()) {
            globalResolver.addAll(info.ejbJars);
        }
        SystemInstance.get().setComponent(EjbResolver.class, globalResolver);

        final UndeployException undeployException = new UndeployException(messages.format("destroyApplication.failed", appInfo.path));

        final WebAppBuilder webAppBuilder = SystemInstance.get().getComponent(WebAppBuilder.class);
        if (webAppBuilder != null) {
            try {
                webAppBuilder.undeployWebApps(appInfo);
            } catch (Exception e) {
                undeployException.getCauses().add(new Exception("App: " + appInfo.path + ": " + e.getMessage(), e));
            }
        }

        // get all of the ejb deployments
        List<BeanContext> deployments = new ArrayList<BeanContext>();
        for (final EjbJarInfo ejbJarInfo : appInfo.ejbJars) {
            for (final EnterpriseBeanInfo beanInfo : ejbJarInfo.enterpriseBeans) {
                final String deploymentId = beanInfo.ejbDeploymentId;
                final BeanContext beanContext = containerSystem.getBeanContext(deploymentId);
                if (beanContext == null) {
                    undeployException.getCauses().add(new Exception("deployment not found: " + deploymentId));
                } else {
                    deployments.add(beanContext);
                }
            }
        }

        // Just as with startup we need to get things in an
        // order that respects the singleton @DependsOn information
        // Theoreticlly if a Singleton depends on something in its
        // @PostConstruct, it can depend on it in its @PreDestroy.
        // Therefore we want to make sure that if A dependsOn B,
        // that we destroy A first then B so that B will still be
        // usable in the @PreDestroy method of A.

        // Sort them into the original starting order
        deployments = sort(deployments);
        // reverse that to get the stopping order
        Collections.reverse(deployments);

        // stop
        for (final BeanContext deployment : deployments) {
            final String deploymentID = deployment.getDeploymentID() + "";
            try {
                final Container container = deployment.getContainer();
                container.stop(deployment);
            } catch (Throwable t) {
                undeployException.getCauses().add(new Exception("bean: " + deploymentID + ": " + t.getMessage(), t));
            }
        }

        // undeploy
        for (final BeanContext bean : deployments) {
            final String deploymentID = bean.getDeploymentID() + "";
            try {
                final Container container = bean.getContainer();
                container.undeploy(bean);
                bean.setContainer(null);
            } catch (Throwable t) {
                undeployException.getCauses().add(new Exception("bean: " + deploymentID + ": " + t.getMessage(), t));
            } finally {
                bean.setDestroyed(true);
            }
        }

        // get the client ids
        final List<String> clientIds = new ArrayList<String>();
        for (final ClientInfo clientInfo : appInfo.clients) {
            clientIds.add(clientInfo.moduleId);
            for (final String className : clientInfo.localClients) {
                clientIds.add(className);
            }
            for (final String className : clientInfo.remoteClients) {
                clientIds.add(className);
            }
        }

        if (appContext != null)
            for (final WebContext webContext : appContext.getWebContexts()) {
                containerSystem.removeWebContext(webContext);
            }

        // Clear out naming for all components first
        for (final BeanContext deployment : deployments) {
            final String deploymentID = deployment.getDeploymentID() + "";
            try {
                containerSystem.removeBeanContext(deployment);
            } catch (Throwable t) {
                undeployException.getCauses().add(new Exception(deploymentID, t));
            }

            final JndiBuilder.Bindings bindings = deployment.get(JndiBuilder.Bindings.class);
            if (bindings != null)
                for (final String name : bindings.getBindings()) {
                    try {
                        globalContext.unbind(name);
                    } catch (Throwable t) {
                        undeployException.getCauses().add(new Exception("bean: " + deploymentID + ": " + t.getMessage(), t));
                    }
                }
        }

        for (final String sId : moduleIds) {
            try {
                globalContext.unbind(VALIDATOR_FACTORY_NAMING_CONTEXT + sId);
                globalContext.unbind(VALIDATOR_NAMING_CONTEXT + sId);
            } catch (NamingException e) {
                undeployException.getCauses().add(new Exception("validator: " + sId + ": " + e.getMessage(), e));
            }
        }
        moduleIds.clear();

        // dumpJndiTree(globalContext, "-->");

        try {
            if (globalContext instanceof IvmContext) {
                final IvmContext ivmContext = (IvmContext) globalContext;
                ivmContext.prune("openejb/Deployment");
                ivmContext.prune("openejb/local");
                ivmContext.prune("openejb/remote");
                ivmContext.prune("openejb/global");
            }
        } catch (NamingException e) {
            undeployException.getCauses().add(new Exception("Unable to prune openejb/Deployments and openejb/local namespaces, this could cause future deployments to fail.", e));
        }

        deployments.clear();

        for (final String clientId : clientIds) {
            try {
                globalContext.unbind("/openejb/client/" + clientId);
            } catch (Throwable t) {
                undeployException.getCauses().add(new Exception("client: " + clientId + ": " + t.getMessage(), t));
            }
        }

        // mbeans
        final MBeanServer server = LocalMBeanServer.get();
        for (final Object objectName : appInfo.jmx.values()) {
            try {
                final ObjectName on = new ObjectName((String) objectName);
                if (server.isRegistered(on)) {
                    server.unregisterMBean(on);
                }
            } catch (InstanceNotFoundException e) {
                logger.warning("can't unregister " + objectName + " because the mbean was not found", e);
            } catch (MBeanRegistrationException e) {
                logger.warning("can't unregister " + objectName, e);
            } catch (MalformedObjectNameException mone) {
                logger.warning("can't unregister because the ObjectName is malformed: " + objectName, mone);
            }
        }

        // destroy PUs before resources since the JPA provider can use datasources
        for (final PersistenceUnitInfo unitInfo : appInfo.persistenceUnits) {
            try {
                final Object object = globalContext.lookup(PERSISTENCE_UNIT_NAMING_CONTEXT + unitInfo.id);
                globalContext.unbind(PERSISTENCE_UNIT_NAMING_CONTEXT + unitInfo.id);

                // close EMF so all resources are released
                final ReloadableEntityManagerFactory remf = ((ReloadableEntityManagerFactory) object);
                remf.close();
                persistenceClassLoaderHandler.destroy(unitInfo.id);
                remf.unregister();
            } catch (Throwable t) {
                undeployException.getCauses().add(new Exception("persistence-unit: " + unitInfo.id + ": " + t.getMessage(), t));
            }
        }

        for (final String id : appInfo.resourceIds) {
            final String name = OPENEJB_RESOURCE_JNDI_PREFIX + id;
            try {
                final Object object = globalContext.lookup(name);
                final String clazz;
                if (object == null) { // should it be possible?
                    clazz = "?";
                } else {
                    clazz = object.getClass().getName();
                }
                destroyResource(id, clazz, object);
                globalContext.unbind(name);
            } catch (NamingException e) {
                logger.warning("can't unbind resource '{0}'", id);
            }
        }
        for (final String id : appInfo.resourceAliases) {
            final String name = OPENEJB_RESOURCE_JNDI_PREFIX + id;
            try {
                globalContext.unbind(name);
            } catch (NamingException e) {
                logger.warning("can't unbind resource '{0}'", id);
            }
        }

        containerSystem.removeAppContext(appInfo.appId);

        ClassLoaderUtil.destroyClassLoader(appInfo.path);

        if (undeployException.getCauses().size() > 0) {
            throw undeployException;
        }

        logger.debug("destroyApplication.success", appInfo.path);
    }

    private void unbind(final Context context, final String name) {
        try {
            context.unbind(name);
        } catch (NamingException e) {
            // no-op
        }
    }

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    private void dumpJndiTree(final Context containerSystemContext, final String message) {
        System.out.println(message);
        try {
            JndiTreeBrowser.log(containerSystemContext);
        } catch (NamingException e) {
            // no-op
        }
    }

    public ClassLoader createAppClassLoader(final AppInfo appInfo) throws OpenEJBException, IOException {
        final Set<URL> jars = new HashSet<URL>();
        for (final EjbJarInfo info : appInfo.ejbJars) {
            if (info.path != null)
                jars.add(toUrl(info.path));
        }
        for (final ClientInfo info : appInfo.clients) {
            if (info.path != null)
                jars.add(toUrl(info.path));
        }
        for (final ConnectorInfo info : appInfo.connectors) {
            for (final String jarPath : info.libs) {
                jars.add(toUrl(jarPath));
            }
        }
        for (final String jarPath : appInfo.libs) {
            jars.add(toUrl(jarPath));
        }

        // add openejb-jpa-integration if the jpa provider is in lib/
        if (appInfo.libs.size() > 0) { // the test could be enhanced
            try {
                final File jpaIntegrationFile = JarLocation.jarLocation(MakeTxLookup.class);
                final URL url = jpaIntegrationFile.toURI().toURL();
                if (!jars.contains(url)) { // could have been done before (webapp enrichment or manually for instance)
                    jars.add(url);
                }
            } catch (RuntimeException re) {
                logger.warning("can't find open-jpa-integration jar");
            }
        }
        jars.addAll(Arrays.asList(SystemInstance.get().getComponent(ClassLoaderEnricher.class).applicationEnrichment()));

        // Create the class loader
        final ParentClassLoaderFinder parentFinder = SystemInstance.get().getComponent(ParentClassLoaderFinder.class);
        ClassLoader parent = OpenEJB.class.getClassLoader();
        if (parentFinder != null) {
            parent = parentFinder.getParentClassLoader(parent);
        }

        final ClassLoaderConfigurer configurer = ClassLoaderUtil.configurer(appInfo.appId);
        if (configurer != null) {
            final Iterator<URL> it = jars.iterator();
            while (it.hasNext()) {
                if (!configurer.accept(it.next())) {
                    it.remove();
                }
            }
            jars.addAll(Arrays.asList(configurer.additionalURLs()));
        }

        final URL[] filtered = jars.toArray(new URL[jars.size()]);

        if (appInfo.delegateFirst) {
            return ClassLoaderUtil.createClassLoader(appInfo.path, filtered, parent);
        }
        return ClassLoaderUtil.createClassLoaderFirst(appInfo.path, filtered, parent);
    }

    public void createExternalContext(final JndiContextInfo contextInfo) throws OpenEJBException {
        logger.getChildLogger("service").info("createService", contextInfo.service, contextInfo.id, contextInfo.className);

        final InitialContext initialContext;
        try {
            initialContext = new InitialContext(contextInfo.properties);
        } catch (NamingException ne) {
            throw new OpenEJBException(String.format("JndiProvider(id=\"%s\") could not be created.  Failed to create the InitialContext using the supplied properties", contextInfo.id), ne);
        }

        try {
            containerSystem.getJNDIContext().bind("openejb/remote_jndi_contexts/" + contextInfo.id, initialContext);
        } catch (NamingException e) {
            throw new OpenEJBException("Cannot bind " + contextInfo.service + " with id " + contextInfo.id, e);
        }

        // Update the config tree
        config.facilities.remoteJndiContexts.add(contextInfo);

        logger.getChildLogger("service").debug("createService.success", contextInfo.service, contextInfo.id, contextInfo.className);
    }

    public void createContainer(final ContainerInfo serviceInfo) throws OpenEJBException {

        final ObjectRecipe serviceRecipe = createRecipe(serviceInfo);

        serviceRecipe.setProperty("id", serviceInfo.id);
        serviceRecipe.setProperty("transactionManager", props.get(TransactionManager.class.getName()));
        serviceRecipe.setProperty("securityService", props.get(SecurityService.class.getName()));
        serviceRecipe.setProperty("properties", new UnsetPropertiesRecipe());

        // MDB container has a resource adapter string name that
        // must be replaced with the real resource adapter instance
        replaceResourceAdapterProperty(serviceRecipe);

        final Object service = serviceRecipe.create();

        logUnusedProperties(serviceRecipe, serviceInfo);

        final Class interfce = serviceInterfaces.get(serviceInfo.service);
        checkImplementation(interfce, service.getClass(), serviceInfo.service, serviceInfo.id);

        bindService(serviceInfo, service);

        setSystemInstanceComponent(interfce, service);

        props.put(interfce.getName(), service);
        props.put(serviceInfo.service, service);
        props.put(serviceInfo.id, service);

        containerSystem.addContainer(serviceInfo.id, (Container) service);

        // Update the config tree
        config.containerSystem.containers.add(serviceInfo);

        logger.getChildLogger("service").debug("createService.success", serviceInfo.service, serviceInfo.id, serviceInfo.className);

        if (Container.class.isInstance(service) && LocalMBeanServer.isJMXActive()) {
            final ObjectName objectName = ObjectNameBuilder.uniqueName("containers", serviceInfo.id, service);
            try {
                LocalMBeanServer.get().registerMBean(new DynamicMBeanWrapper(new JMXContainer(serviceInfo, (Container) service)), objectName);
                containerObjectNames.add(objectName);
            } catch (Exception e) {
                // no-op
            } catch (NoClassDefFoundError ncdfe) { // OSGi
                // no-op
            }
        }
    }

    private void bindService(final ServiceInfo serviceInfo, final Object service) throws OpenEJBException {
        try {
            this.containerSystem.getJNDIContext().bind(JAVA_OPENEJB_NAMING_CONTEXT + serviceInfo.service + "/" + serviceInfo.id, service);
        } catch (NamingException e) {
            throw new OpenEJBException(messages.format("assembler.cannotBindServiceWithId", serviceInfo.service, serviceInfo.id), e);
        }
    }

    public void removeContainer(final String containerId) {
        containerSystem.removeContainer(containerId);

        // Update the config tree
        for (Iterator<ContainerInfo> iterator = config.containerSystem.containers.iterator(); iterator.hasNext(); ) {
            final ContainerInfo containerInfo = iterator.next();
            if (containerInfo.id.equals(containerId)) {
                iterator.remove();
                try {
                    this.containerSystem.getJNDIContext().unbind(JAVA_OPENEJB_NAMING_CONTEXT + containerInfo.service + "/" + containerInfo.id);
                } catch (Exception e) {
                    logger.error("removeContainer.unbindFailed", containerId);
                }
            }
        }
    }

    public void createService(final ServiceInfo serviceInfo) throws OpenEJBException {
        final ObjectRecipe serviceRecipe = createRecipe(serviceInfo);

        final Object service = serviceRecipe.create();
        SystemInstance.get().addObserver(service);

        logUnusedProperties(serviceRecipe, serviceInfo);

        final Class<?> serviceClass = service.getClass();

        getContext().put(serviceClass.getName(), service);

        props.put(serviceClass.getName(), service);
        props.put(serviceInfo.service, service);
        props.put(serviceInfo.id, service);

        config.facilities.services.add(serviceInfo);

        logger.getChildLogger("service").debug("createService.success", serviceInfo.service, serviceInfo.id, serviceInfo.className);
    }

    public void createProxyFactory(final ProxyFactoryInfo serviceInfo) throws OpenEJBException {

        final ObjectRecipe serviceRecipe = createRecipe(serviceInfo);

        final Object service = serviceRecipe.create();

        logUnusedProperties(serviceRecipe, serviceInfo);

        final Class interfce = serviceInterfaces.get(serviceInfo.service);
        checkImplementation(interfce, service.getClass(), serviceInfo.service, serviceInfo.id);

        ProxyManager.registerFactory(serviceInfo.id, (ProxyFactory) service);
        ProxyManager.setDefaultFactory(serviceInfo.id);

        bindService(serviceInfo, service);

        setSystemInstanceComponent(interfce, service);

        getContext().put(interfce.getName(), service);

        props.put(interfce.getName(), service);
        props.put(serviceInfo.service, service);
        props.put(serviceInfo.id, service);

        // Update the config tree
        config.facilities.intraVmServer = serviceInfo;

        logger.getChildLogger("service").debug("createService.success", serviceInfo.service, serviceInfo.id, serviceInfo.className);
    }

    private void replaceResourceAdapterProperty(final ObjectRecipe serviceRecipe) throws OpenEJBException {
        final Object resourceAdapterId = serviceRecipe.getProperty("ResourceAdapter");
        if (resourceAdapterId instanceof String) {
            String id = (String) resourceAdapterId;
            id = id.trim();

            Object resourceAdapter = null;
            try {
                resourceAdapter = containerSystem.getJNDIContext().lookup("openejb/Resource/" + id);
            } catch (NamingException e) {
                // handled below
            }

            if (resourceAdapter == null) {
                throw new OpenEJBException("No existing resource adapter defined with id '" + id + "'.");
            }
            if (!(resourceAdapter instanceof ResourceAdapter)) {
                throw new OpenEJBException(messages.format("assembler.resourceAdapterNotResourceAdapter", id, resourceAdapter.getClass()));
            }
            serviceRecipe.setProperty("ResourceAdapter", resourceAdapter);
        }
    }

    public void createResource(final ResourceInfo serviceInfo) throws OpenEJBException {
        final ObjectRecipe serviceRecipe = createRecipe(serviceInfo);
        serviceRecipe.setProperty("transactionManager", transactionManager);
        serviceRecipe.setProperty("ServiceId", serviceInfo.id);
        serviceRecipe.setProperty("properties", new UnsetPropertiesRecipe());

        final Properties props = PropertyPlaceHolderHelper.holds(serviceInfo.properties);
        if (serviceInfo.properties.containsKey("Definition")) {
            try { // we catch classcast etc..., if it fails it is not important
                final InputStream is = new ByteArrayInputStream(serviceInfo.properties.getProperty("Definition").getBytes());
                final Properties p = new Properties();
                IO.readProperties(is, p);
                for (final Map.Entry<Object, Object> entry : p.entrySet()) {
                    final String key = entry.getKey().toString();
                    if (!props.containsKey(key) // never override from Definition, just use it to complete the properties set
                        && !(key.equalsIgnoreCase("url") && props.containsKey("JdbcUrl"))) { // with @DataSource we can get both, see org.apache.openejb.config.ConvertDataSourceDefinitions.rawDefinition()
                        props.put(key, entry.getValue());
                    }
                }
            } catch (Exception e) {
                // ignored
            }
        }
        serviceRecipe.setProperty("Definition", PropertiesHelper.propertiesToString(props));

        replaceResourceAdapterProperty(serviceRecipe);

        Object service = serviceRecipe.create();

        // Java Connector spec ResourceAdapters and ManagedConnectionFactories need special activation
        if (service instanceof ResourceAdapter) {
            final ResourceAdapter resourceAdapter = (ResourceAdapter) service;

            // Create a thead pool for work manager
            final int threadPoolSize = getIntProperty(serviceInfo.properties, "threadPoolSize", 30);
            final Executor threadPool;
            if (threadPoolSize <= 0) {
                threadPool = Executors.newCachedThreadPool(new ResourceAdapterThreadFactory(serviceInfo.id));
            } else {
                threadPool = Executors.newFixedThreadPool(threadPoolSize, new ResourceAdapterThreadFactory(serviceInfo.id));
            }

            // WorkManager: the resource adapter can use this to dispatch messages or perform tasks
            final WorkManager workManager;
            if (GeronimoTransactionManager.class.isInstance(transactionManager)) {
                final GeronimoTransactionManager geronimoTransactionManager = (GeronimoTransactionManager) transactionManager;
                final TransactionContextHandler txWorkContextHandler = new TransactionContextHandler(geronimoTransactionManager);

                // use id as default realm name if realm is not specified in service properties
                final String securityRealmName = getStringProperty(serviceInfo.properties, "realm", serviceInfo.id);

                final SecurityContextHandler securityContextHandler = new SecurityContextHandler(securityRealmName);
                final HintsContextHandler hintsContextHandler = new HintsContextHandler();

                final Collection<WorkContextHandler> workContextHandlers = new ArrayList<WorkContextHandler>();
                workContextHandlers.add(txWorkContextHandler);
                workContextHandlers.add(securityContextHandler);
                workContextHandlers.add(hintsContextHandler);

                workManager = new GeronimoWorkManager(threadPool, threadPool, threadPool, workContextHandlers);
            } else {
                workManager = new SimpleWorkManager(threadPool);
            }

            // BootstrapContext: wraps the WorkMananger and XATerminator
            final BootstrapContext bootstrapContext;
            if (transactionManager instanceof GeronimoTransactionManager) {
                bootstrapContext = new GeronimoBootstrapContext((GeronimoWorkManager) workManager, (GeronimoTransactionManager) transactionManager, (GeronimoTransactionManager) transactionManager);
            } else if (transactionManager instanceof XATerminator) {
                bootstrapContext = new SimpleBootstrapContext(workManager, (XATerminator) transactionManager);
            } else {
                bootstrapContext = new SimpleBootstrapContext(workManager);
            }

            // start the resource adapter
            try {
                logger.debug("createResource.startingResourceAdapter", serviceInfo.id, service.getClass().getName());
                resourceAdapter.start(bootstrapContext);
            } catch (ResourceAdapterInternalException e) {
                throw new OpenEJBException(e);
            }

            final Map<String, Object> unset = serviceRecipe.getUnsetProperties();
            unset.remove("threadPoolSize");
            logUnusedProperties(unset, serviceInfo);
        } else if (service instanceof ManagedConnectionFactory) {
            final ManagedConnectionFactory managedConnectionFactory = (ManagedConnectionFactory) service;

            // connection manager is constructed via a recipe so we automatically expose all cmf properties
            final ObjectRecipe connectionManagerRecipe = new ObjectRecipe(GeronimoConnectionManagerFactory.class, "create");
            connectionManagerRecipe.allow(Option.CASE_INSENSITIVE_PROPERTIES);
            connectionManagerRecipe.allow(Option.IGNORE_MISSING_PROPERTIES);
            connectionManagerRecipe.setAllProperties(serviceInfo.properties);
            connectionManagerRecipe.setProperty("name", serviceInfo.id);
            connectionManagerRecipe.setProperty("mcf", managedConnectionFactory);

            // standard properties
            connectionManagerRecipe.setProperty("transactionManager", transactionManager);
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader == null)
                classLoader = getClass().getClassLoader();
            if (classLoader == null)
                classLoader = ClassLoader.getSystemClassLoader();
            connectionManagerRecipe.setProperty("classLoader", classLoader);

            logger.getChildLogger("service").info("createResource.createConnectionManager", serviceInfo.id, service.getClass().getName());

            // create the connection manager
            final ConnectionManager connectionManager = (ConnectionManager) connectionManagerRecipe.create();
            if (connectionManager == null) {
                throw new OpenEJBRuntimeException(messages.format("assembler.invalidConnectionManager", serviceInfo.id));
            }

            final Map<String, Object> unsetA = serviceRecipe.getUnsetProperties();
            final Map<String, Object> unsetB = connectionManagerRecipe.getUnsetProperties();
            final Map<String, Object> unset = new HashMap<String, Object>();
            for (final Map.Entry<String, Object> entry : unsetA.entrySet()) {
                if (unsetB.containsKey(entry.getKey()))
                    unset.put(entry.getKey(), entry.getValue());
            }

            // service becomes a ConnectorReference which merges connection manager and mcf
            service = new ConnectorReference(connectionManager, managedConnectionFactory);

            // init cm if needed
            final Object eagerInit = unset.remove("eagerInit");
            if (eagerInit != null && eagerInit instanceof String && "true".equalsIgnoreCase((String) eagerInit)
                            && connectionManager instanceof AbstractConnectionManager) {
                try {
                    ((AbstractConnectionManager) connectionManager).doStart();
                    try {
                        final Object cf = managedConnectionFactory.createConnectionFactory(connectionManager);
                        if (cf instanceof ConnectionFactory) {
                            final Connection connection = ((ConnectionFactory) cf).getConnection();
                            connection.getMetaData();
                            connection.close();
                        }
                    } catch (Exception e) {
                        // no-op: just to force eager init of pool
                    }
                } catch (Exception e) {
                    logger.warning("Can't start connection manager", e);
                }
            }

            logUnusedProperties(unset, serviceInfo);
        } else if (service instanceof DataSource) {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader == null) {
                classLoader = getClass().getClassLoader();
            }

            final ImportSql importer = new ImportSql(classLoader, serviceInfo.id, (DataSource) service);
            if (importer.hasSomethingToImport()) {
                importer.doImport();
            }

            logUnusedProperties(DataSourceFactory.forgetRecipe(service, serviceRecipe), serviceInfo);

            final Properties prop = serviceInfo.properties;
            String url = prop.getProperty("JdbcUrl", prop.getProperty("url"));
            if (url == null) {
                url = prop.getProperty("jdbcUrl");
            }
            if (url == null) {
                logger.info("can't find url for " + serviceInfo.id + " will not monitor it");
            } else {
                final String host = extractHost(url);
                if (host != null) {
                    remoteResourceMonitor.addHost(host);
                    remoteResourceMonitor.registerIfNot();
                }
            }
        } else {
            logUnusedProperties(serviceRecipe, serviceInfo);
        }

        bindResource(serviceInfo.id, service);
        for (final String alias : serviceInfo.aliases) {
            bindResource(alias, service);
        }

        // Update the config tree
        config.facilities.resources.add(serviceInfo);

        if (logger.isDebugEnabled()) { // weird to check parent logger but save time and it is almost never activated
            logger.getChildLogger("service").debug("createService.success", serviceInfo.service, serviceInfo.id, serviceInfo.className);
        }
    }

    private void bindResource(final String id, final Object service) throws OpenEJBException {
        final String name = OPENEJB_RESOURCE_JNDI_PREFIX + id;
        try {
            containerSystem.getJNDIContext().bind(name, service);
        } catch (NameAlreadyBoundException nabe) {
            logger.warning("unbounding resource " + name + " can happen because of a redeployment or because of a duplicated id");
            try {
                containerSystem.getJNDIContext().unbind(name);
                containerSystem.getJNDIContext().bind(name, service);
            } catch (NamingException e) {
                throw new OpenEJBException("Cannot bind resource adapter with id " + id, e);
            }
        } catch (NamingException e) {
            throw new OpenEJBException("Cannot bind resource adapter with id " + id, e);
        }
    }

    private static String extractHost(final String url) { // can be enhanced
        if (url == null || !url.contains("://")) {
            return null;
        }

        final int idx = url.indexOf("://");
        final String subUrl = url.substring(idx + 3);
        final int port = subUrl.indexOf(':');
        final int slash = subUrl.indexOf('/');

        int end = port;
        if (end < 0 || (slash > 0 && slash < end)) {
            end = slash;
        }
        if (end > 0) {
            return subUrl.substring(0, end);
        }

        return subUrl;
    }

    private int getIntProperty(final Properties properties, final String propertyName, final int defaultValue) {
        final String propertyValue = getStringProperty(properties, propertyName, Integer.toString(defaultValue));
        if (propertyValue == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(propertyValue);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(propertyName + " is not an integer " + propertyValue, e);
        }
    }

    private String getStringProperty(final Properties properties, final String propertyName, final String defaultValue) {
        final String propertyValue = properties.getProperty(propertyName);
        if (propertyValue == null) {
            return defaultValue;
        }

        return propertyValue;
    }

    public void createConnectionManager(final ConnectionManagerInfo serviceInfo) throws OpenEJBException {

        final ObjectRecipe serviceRecipe = createRecipe(serviceInfo);

        final Object object = props.get("TransactionManager");
        serviceRecipe.setProperty("transactionManager", object);

        final Object service = serviceRecipe.create();

        logUnusedProperties(serviceRecipe, serviceInfo);

        final Class interfce = serviceInterfaces.get(serviceInfo.service);
        checkImplementation(interfce, service.getClass(), serviceInfo.service, serviceInfo.id);

        bindService(serviceInfo, service);

        setSystemInstanceComponent(interfce, service);

        getContext().put(interfce.getName(), service);

        props.put(interfce.getName(), service);
        props.put(serviceInfo.service, service);
        props.put(serviceInfo.id, service);

        // Update the config tree
        config.facilities.connectionManagers.add(serviceInfo);

        logger.getChildLogger("service").debug("createService.success", serviceInfo.service, serviceInfo.id, serviceInfo.className);
    }

    public void createSecurityService(final SecurityServiceInfo serviceInfo) throws OpenEJBException {

        Object service = SystemInstance.get().getComponent(SecurityService.class);
        if (service == null) {
            final ObjectRecipe serviceRecipe = createRecipe(serviceInfo);
            service = serviceRecipe.create();
            logUnusedProperties(serviceRecipe, serviceInfo);
        }

        final Class interfce = serviceInterfaces.get(serviceInfo.service);
        checkImplementation(interfce, service.getClass(), serviceInfo.service, serviceInfo.id);

        try {
            this.containerSystem.getJNDIContext().bind(JAVA_OPENEJB_NAMING_CONTEXT + serviceInfo.service, service);
        } catch (NamingException e) {
            throw new OpenEJBException("Cannot bind " + serviceInfo.service + " with id " + serviceInfo.id, e);
        }

        setSystemInstanceComponent(interfce, service);

        getContext().put(interfce.getName(), service);

        props.put(interfce.getName(), service);
        props.put(serviceInfo.service, service);
        props.put(serviceInfo.id, service);

        this.securityService = (SecurityService) service;

        // Update the config tree
        config.facilities.securityService = serviceInfo;

        logger.getChildLogger("service").debug("createService.success", serviceInfo.service, serviceInfo.id, serviceInfo.className);
    }

    public void createTransactionManager(final TransactionServiceInfo serviceInfo) throws OpenEJBException {

        Object service = SystemInstance.get().getComponent(TransactionManager.class);
        if (service == null) {
            final ObjectRecipe serviceRecipe = createRecipe(serviceInfo);
            service = serviceRecipe.create();
            logUnusedProperties(serviceRecipe, serviceInfo);
        } else {
            logger.info("Reusing provided TransactionManager " + service);
        }

        final Class interfce = serviceInterfaces.get(serviceInfo.service);
        checkImplementation(interfce, service.getClass(), serviceInfo.service, serviceInfo.id);

        try {
            this.containerSystem.getJNDIContext().bind(JAVA_OPENEJB_NAMING_CONTEXT + serviceInfo.service, service);
            this.containerSystem.getJNDIContext().bind("comp/UserTransaction", new CoreUserTransaction((TransactionManager) service));
            this.containerSystem.getJNDIContext().bind("comp/TransactionManager", service);
        } catch (NamingException e) {
            throw new OpenEJBException("Cannot bind " + serviceInfo.service + " with id " + serviceInfo.id, e);
        }

        setSystemInstanceComponent(interfce, service);

        getContext().put(interfce.getName(), service);

        props.put(interfce.getName(), service);
        props.put(serviceInfo.service, service);
        props.put(serviceInfo.id, service);

        this.transactionManager = (TransactionManager) service;

        // Update the config tree
        config.facilities.transactionService = serviceInfo;

        // todo find a better place for this

        // TransactionSynchronizationRegistry
        final TransactionSynchronizationRegistry synchronizationRegistry;
        if (transactionManager instanceof TransactionSynchronizationRegistry) {
            synchronizationRegistry = (TransactionSynchronizationRegistry) transactionManager;
        } else {
            // todo this should be built
            synchronizationRegistry = new SimpleTransactionSynchronizationRegistry(transactionManager);
        }

        Assembler.getContext().put(TransactionSynchronizationRegistry.class.getName(), synchronizationRegistry);
        SystemInstance.get().setComponent(TransactionSynchronizationRegistry.class, synchronizationRegistry);

        try {
            this.containerSystem.getJNDIContext().bind("comp/TransactionSynchronizationRegistry", new TransactionSynchronizationRegistryWrapper());
        } catch (NamingException e) {
            throw new OpenEJBException("Cannot bind java:comp/TransactionSynchronizationRegistry", e);
        }

        // JtaEntityManagerRegistry
        // todo this should be built
        final JtaEntityManagerRegistry jtaEntityManagerRegistry = new JtaEntityManagerRegistry(synchronizationRegistry);
        Assembler.getContext().put(JtaEntityManagerRegistry.class.getName(), jtaEntityManagerRegistry);
        SystemInstance.get().setComponent(JtaEntityManagerRegistry.class, jtaEntityManagerRegistry);

        logger.getChildLogger("service").debug("createService.success", serviceInfo.service, serviceInfo.id, serviceInfo.className);
    }

    public static void logUnusedProperties(final ObjectRecipe serviceRecipe, final ServiceInfo info) {
        final Map<String, Object> unsetProperties = serviceRecipe.getUnsetProperties();
        logUnusedProperties(unsetProperties, info);
    }

    private static void logUnusedProperties(final Map<String, Object> unsetProperties, final ServiceInfo info) {
        for (final String property : unsetProperties.keySet()) {
            //TODO: DMB: Make more robust later
            if (property.equalsIgnoreCase("JndiName"))
                return;
            if (property.equalsIgnoreCase("Origin"))
                return;
            if (property.equalsIgnoreCase("DatabaseName"))
                return;
            if (property.equalsIgnoreCase("connectionAttributes"))
                return;

            if (property.equalsIgnoreCase("properties"))
                return;
            if (property.equalsIgnoreCase("ApplicationWide"))
                return;
            if (property.equalsIgnoreCase("transactionManager"))
                return;
            if (info.types.contains("javax.mail.Session"))
                return;
            //---

            if (info.types.isEmpty() && "class".equalsIgnoreCase(property))
                continue; // inline service (no sp)

            logger.getChildLogger("service").warning("unusedProperty", property, info.id);
        }
    }

    public static ObjectRecipe prepareRecipe(final ServiceInfo info) {
        final String[] constructorArgs = info.constructorArgs.toArray(new String[info.constructorArgs.size()]);
        final ObjectRecipe serviceRecipe = new ObjectRecipe(info.className, info.factoryMethod, constructorArgs, null);
        serviceRecipe.allow(Option.CASE_INSENSITIVE_PROPERTIES);
        serviceRecipe.allow(Option.IGNORE_MISSING_PROPERTIES);
        return serviceRecipe;
    }

    private ObjectRecipe createRecipe(final ServiceInfo info) {
        final Logger serviceLogger = logger.getChildLogger("service");

        if (info instanceof ResourceInfo) {
            final List<String> aliasesList = ((ResourceInfo) info).aliases;
            if (!aliasesList.isEmpty()) {
                final String aliases = Join.join(", ", aliasesList);
                serviceLogger.info("createServiceWithAliases", info.service, info.id, aliases);
            } else {
                serviceLogger.info("createService", info.service, info.id);
            }
        } else {
            serviceLogger.info("createService", info.service, info.id);
        }

        final ObjectRecipe serviceRecipe = prepareRecipe(info);
        serviceRecipe.setAllProperties(info.properties);

        if (serviceLogger.isDebugEnabled()) {
            for (final Map.Entry<String, Object> entry : serviceRecipe.getProperties().entrySet()) {
                serviceLogger.debug("createService.props", entry.getKey(), entry.getValue());
            }
        }
        return serviceRecipe;
    }

    @SuppressWarnings({"unchecked"})
    private void setSystemInstanceComponent(final Class interfce, final Object service) {
        SystemInstance.get().setComponent(interfce, service);
    }

    private URL toUrl(final String jarPath) throws OpenEJBException {
        try {
            return new File(jarPath).toURI().toURL();
        } catch (MalformedURLException e) {
            throw new OpenEJBException(messages.format("cl0001", jarPath, e.getMessage()), e);
        }
    }

    private static class PersistenceClassLoaderHandlerImpl implements PersistenceClassLoaderHandler {

        private static boolean logged = false;

        private final Map<String, List<ClassFileTransformer>> transformers = new TreeMap<String, List<ClassFileTransformer>>();

        @Override
        public void addTransformer(final String unitId, final ClassLoader classLoader, final ClassFileTransformer classFileTransformer) {
            final Instrumentation instrumentation = Agent.getInstrumentation();
            if (instrumentation != null) {
                instrumentation.addTransformer(classFileTransformer);

                if (unitId != null) {
                    List<ClassFileTransformer> transformers = this.transformers.get(unitId);
                    if (transformers == null) {
                        transformers = new ArrayList<ClassFileTransformer>(1);
                        this.transformers.put(unitId, transformers);
                    }
                    transformers.add(classFileTransformer);
                }
            } else if (!logged) {
                logger.warning("assembler.noAgent");
                logged = true;
            }
        }

        @Override
        public void destroy(final String unitId) {
            final List<ClassFileTransformer> transformers = this.transformers.remove(unitId);
            if (transformers != null) {
                final Instrumentation instrumentation = Agent.getInstrumentation();
                if (instrumentation != null) {
                    for (final ClassFileTransformer transformer : transformers) {
                        instrumentation.removeTransformer(transformer);
                    }
                } else {
                    logger.error("assembler.noAgent");
                }
            }
        }

        @Override
        public ClassLoader getNewTempClassLoader(final ClassLoader classLoader) {
            return ClassLoaderUtil.createTempClassLoader(classLoader);
        }
    }

    // Based on edu.emory.mathcs.backport.java.util.concurrent.Executors.DefaultThreadFactory
    // Which is freely licensed as follows.
    // "Use, modify, and redistribute this code in any way without acknowledgement"
    private static class ResourceAdapterThreadFactory implements ThreadFactory {

        private final ThreadGroup group;
        private final String namePrefix;
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        ResourceAdapterThreadFactory(final String resourceAdapterName) {
            final SecurityManager securityManager = System.getSecurityManager();
            if (securityManager != null) {
                group = securityManager.getThreadGroup();
            } else {
                group = Thread.currentThread().getThreadGroup();
            }

            namePrefix = resourceAdapterName + "-worker-";
        }

        @Override
        public Thread newThread(final Runnable runnable) {
            final Thread thread = new Thread(group, runnable, namePrefix + threadNumber.getAndIncrement(), 0);
            if (!thread.isDaemon())
                thread.setDaemon(true);
            if (thread.getPriority() != Thread.NORM_PRIORITY)
                thread.setPriority(Thread.NORM_PRIORITY);
            return thread;
        }
    }

    public static class DeploymentListenerObserver {

        private final DeploymentListener delegate;

        public DeploymentListenerObserver(final DeploymentListener deploymentListener) {
            delegate = deploymentListener;
        }

        public void afterApplicationCreated(
                @Observes
                final AssemblerAfterApplicationCreated event) {
            delegate.afterApplicationCreated(event.getApp());
        }

        public void beforeApplicationDestroyed(
                @Observes
                final AssemblerBeforeApplicationDestroyed event) {
            delegate.beforeApplicationDestroyed(event.getApp());
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof DeploymentListenerObserver)) {
                return false;
            }

            final DeploymentListenerObserver that = (DeploymentListenerObserver) o;

            return !(delegate != null ? !delegate.equals(that.delegate) : that.delegate != null);
        }

        @Override
        public int hashCode() {
            return delegate != null ? delegate.hashCode() : 0;
        }
    }
}
