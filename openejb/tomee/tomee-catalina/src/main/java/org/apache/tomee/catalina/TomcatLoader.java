/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tomee.catalina;

import org.apache.catalina.Container;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.startup.Bootstrap;
import org.apache.catalina.startup.Catalina;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.assembler.WebAppDeployer;
import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.assembler.classic.WebAppBuilder;
import org.apache.openejb.classloader.WebAppEnricher;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.NewLoaderLogic;
import org.apache.openejb.config.sys.Tomee;
import org.apache.openejb.core.ParentClassLoaderFinder;
import org.apache.openejb.core.ServerFederation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.Loader;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.ServerService;
import org.apache.openejb.server.ServiceException;
import org.apache.openejb.server.ServiceManager;
import org.apache.openejb.server.ejbd.EjbServer;
import org.apache.openejb.spi.Service;
import org.apache.openejb.util.Join;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.OptionsLog;
import org.apache.tomcat.util.scan.Constants;
import org.apache.tomee.catalina.deployment.TomcatWebappDeployer;
import org.apache.tomee.installer.Installer;
import org.apache.tomee.installer.Paths;
import org.apache.tomee.loader.TomcatHelper;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * <h1>Prerequisites</h1>
 * <p/>
 * System properties that must be set:
 * <ul>
 * <li/>openejb.home -> catalina.home
 * <li/>openejb.base -> catalina.base
 * <li/>tomee.war -> $tomee.war
 * <li/>tomcat.version if not set
 * <li/>tomcat.built if not set
 * </ul>
 * <p/>
 * <h1>Integration Actions</h1>
 * <p/>
 * <ul>
 * <li/>Setup ServiceJar: set openejb.provider.default -> org.apache.tomee
 * We therefore will load this file: META-INF/org.apache.openejb.tomcat/service-jar.xml
 * <li/>Init SystemInstance and OptionsLog
 * <li/>
 * <li/>
 * </ul>
 * <p/>
 * See {@link org.apache.openejb.config.ServiceUtils#DEFAULT_PROVIDER_URL}
 *
 * @version $Revision: 617255 $ $Date: 2008-01-31 13:58:36 -0800 (Thu, 31 Jan 2008) $
 */
public class TomcatLoader implements Loader {

    static {
        Warmup.warmup();
    }

    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, TomcatLoader.class);
    public static final String TOMEE_NOSHUTDOWNHOOK_PROP = "tomee.noshutdownhook";

    /**
     * OpenEJB Server Daemon
     */
    private static EjbServer ejbServer;

    /**
     * OpenEJB Service Manager that manage services
     */
    private static ServiceManager manager;

    /** other services */
    private static final List<ServerService> services = new ArrayList<ServerService> ();

    /**
     * Creates a new instance.
     */
    public TomcatLoader() {
    }

    /**
     *  {@inheritDoc}
     */
    public void init(Properties properties) throws Exception {

        // Enable System EJBs like the MEJB and DeployerEJB
        initDefaults(properties);

        // Loader maybe the first thing executed in a new classloader
        // so we must attempt to initialize the system instance.
        SystemInstance.init(properties);
        initialize(properties);
    }

    public void initDefaults(Properties properties) {
        setIfNull(properties, "openejb.system.apps", "true");
        setIfNull(properties, "openejb.deployments.classpath", "false");
        setIfNull(properties, "openejb.deployments.classpath.filter.systemapps", "false");

        //Sets default service provider
        setIfNull(properties, "openejb.provider.default", "org.apache.tomee");
    }

    public void initialize(Properties properties) throws Exception {
        //Install Log
        OptionsLog.install();

        // install conf/openejb.xml and conf/logging.properties files
        String openejbWarDir = properties.getProperty("tomee.war");
        if (openejbWarDir != null) {

            Paths paths = new Paths(new File(openejbWarDir));
            if (paths.verify()) {
                Installer installer = new Installer(paths);
                if (installer.getStatus() != Installer.Status.INSTALLED) {
                    installer.installConfigFiles();
                }
            }
        }

        // Not thread safe
        if (OpenEJB.isInitialized()) {
            ejbServer = SystemInstance.get().getComponent(EjbServer.class);
            return;
        }

        final File conf = new File(SystemInstance.get().getBase().getDirectory(), "conf");
        final File tomeeXml = new File(conf, "tomee.xml");
        if (tomeeXml.exists()) { // use tomee.xml instead of openejb.xml
            SystemInstance.get().setProperty("openejb.configuration", tomeeXml.getAbsolutePath());
            SystemInstance.get().setProperty("openejb.configuration.class", Tomee.class.getName());
        }

        // set tomcat pool
        try {// in embedded mode we can easily remove it so check we can use it before setting it
            final Class<?> creatorClass = TomcatLoader.class.getClassLoader().loadClass("org.apache.tomee.jdbc.TomEEDataSourceCreator");
            SystemInstance.get().setProperty(ConfigurationFactory.OPENEJB_JDBC_DATASOURCE_CREATOR, creatorClass.getName());
        } catch (Throwable ignored) {
            // will use the defaul tone
        }

        // tomcat default behavior is webapp, simply keep it, it is overridable by system property too
        SystemInstance.get().setProperty("openejb.default.deployment-module", System.getProperty("openejb.default.deployment-module", "org.apache.openejb.config.WebModule"));

        //Those are set by TomcatHook, why re-set here???
        System.setProperty("openejb.home", SystemInstance.get().getHome().getDirectory().getAbsolutePath());
        System.setProperty("openejb.base", SystemInstance.get().getBase().getDirectory().getAbsolutePath());

        // Install tomcat thread context listener
        ThreadContext.addThreadContextListener(new TomcatThreadContextListener());

        // set ignorable libraries from a tomee property instead of using the standard openejb one
        // don't ignore standard openejb exclusions file
        final Set<String> exclusions = new HashSet<String>(Arrays.asList(NewLoaderLogic.getExclusions()));
        final File catalinaProperties = new File(conf, "catalina.properties");
        if (catalinaProperties.exists()) {
            final Properties catalinaProps = IO.readProperties(catalinaProperties);
            final String jarToSkipProp = catalinaProps.getProperty("tomcat.util.scan.DefaultJarScanner.jarsToSkip");
            if (jarToSkipProp != null) {
                for (String s : jarToSkipProp.split(",")) {
                    exclusions.add(NewLoaderLogic.sanitize(s.trim()));
                }
            }
        }
        NewLoaderLogic.setExclusions(exclusions.toArray(new String[exclusions.size()]));
        System.setProperty(Constants.SKIP_JARS_PROPERTY, Join.join(",", exclusions));

        // Install tomcat war builder
        TomcatWebAppBuilder tomcatWebAppBuilder = (TomcatWebAppBuilder) SystemInstance.get().getComponent(WebAppBuilder.class);
        if (tomcatWebAppBuilder == null) {
            tomcatWebAppBuilder = new TomcatWebAppBuilder();
            tomcatWebAppBuilder.start();
            SystemInstance.get().setComponent(WebAppBuilder.class, tomcatWebAppBuilder);
        }
        SystemInstance.get().setComponent(ParentClassLoaderFinder.class, tomcatWebAppBuilder);

        // set webapp deployer reusing tomcat deployer instead of our custom deployer for war
        SystemInstance.get().setComponent(WebAppDeployer.class, new TomcatWebappDeployer());

        // for compatibility purpose, no more used normally by our trunk
        SystemInstance.get().setComponent(WebDeploymentListeners.class, new WebDeploymentListeners());

        // tomee webapp enricher
        SystemInstance.get().setComponent(WebAppEnricher.class, new TomEEClassLoaderEnricher());

        if (optionalService(properties, "org.apache.tomee.webservices.TomeeJaxRsService")) {
            // in embedded mode we use regex, in tomcat we use tomcat servlet mapping
            SystemInstance.get().setProperty("openejb.rest.wildcard", "*");
        }
        optionalService(properties, "org.apache.tomee.webservices.TomeeJaxWsService");

        // Start OpenEJB
        ejbServer = new EjbServer();
        SystemInstance.get().setComponent(EjbServer.class, ejbServer);
        OpenEJB.init(properties, new ServerFederation());
        TomcatJndiBuilder.importOpenEJBResourcesInTomcat(SystemInstance.get().getComponent(OpenEjbConfiguration.class).facilities.resources, TomcatHelper.getServer());

        Properties ejbServerProps = new Properties();
        ejbServerProps.putAll(properties);
        ejbServerProps.setProperty("openejb.ejbd.uri", "http://127.0.0.1:8080/tomee/ejb");
        ejbServer.init(ejbServerProps);

        // Add our naming context listener to the server which registers all Tomcat resources with OpenEJB
        StandardServer standardServer = TomcatHelper.getServer();
        OpenEJBNamingContextListener namingContextListener = new OpenEJBNamingContextListener(standardServer);
        // Standard server has no state property, so we check global naming context to determine if server is started yet
        if (standardServer.getGlobalNamingContext() != null) {
            namingContextListener.start();
        }
        standardServer.addLifecycleListener(namingContextListener);

        // Process all applications already started.  This deploys EJBs, PersistenceUnits
        // and modifies JNDI ENC references to OpenEJB managed objects such as EJBs.
        processRunningApplications(tomcatWebAppBuilder, standardServer);

        final ClassLoader cl = TomcatLoader.class.getClassLoader();
        if (SystemInstance.get().getOptions().get("openejb.servicemanager.enabled", true)) {
            final String clazz = SystemInstance.get().getOptions().get("openejb.service.manager.class", "org.apache.tomee.catalina.TomEEServiceManager");
            try {
                manager = (ServiceManager) cl.loadClass(clazz).newInstance();
            } catch (ClassNotFoundException cnfe) {
                logger.error("can't find the service manager " + clazz + ", the TomEE one will be used");
                manager = new TomEEServiceManager();
            }
            manager.init();
            manager.start(false);
        } else {
            // WS
            try {
                ServerService cxfService = (ServerService) cl.loadClass("org.apache.openejb.server.cxf.CxfService").newInstance();
                cxfService.start();
                services.add(cxfService);
            } catch (ClassNotFoundException ignored) {
            } catch (Exception e) {
                Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, getClass());
                logger.error("Webservices failed to start", e);
            }

            // REST
            try {
                ServerService restService = (ServerService) cl.loadClass("org.apache.openejb.server.cxf.rs.CxfRSService").newInstance();
                restService.start();
                services.add(restService);
            } catch (ClassNotFoundException ignored) {
            } catch (Exception e) {
                logger.error("REST failed to start", e);
            }
        }

        if (SystemInstance.get().getOptions().get(TOMEE_NOSHUTDOWNHOOK_PROP, (String) null) != null) {
            final Field daemonField = Bootstrap.class.getDeclaredField("daemon");
            final boolean acc = daemonField.isAccessible();
            try {
                daemonField.setAccessible(true);
                final Bootstrap daemon = (Bootstrap) daemonField.get(null);
                if (daemon != null) {
                    final Field catalinaField = Bootstrap.class.getDeclaredField("catalinaDaemon");
                    final boolean catalinaAcc = catalinaField.isAccessible();
                    catalinaField.setAccessible(true);
                    try {
                        Catalina.class.getMethod("setUseShutdownHook", boolean.class).invoke(catalinaField.get(daemon), false);
                    } finally {
                        catalinaField.setAccessible(catalinaAcc);
                    }
                }
            } finally {
                daemonField.setAccessible(acc);
            }
        }
    }

    private boolean optionalService(Properties properties, String className) {
        try {
            Service service = (Service) getClass().getClassLoader().loadClass(className).newInstance();
            service.init(properties);
            return true;
        } catch (ClassNotFoundException e) {
            logger.info("Optional service not installed: " + className);
        } catch (Exception e) {
            logger.error("Failed to start: " + className, e);
        }
        return false;
    }

    private void setIfNull(Properties properties, String key, String value) {
        if (!properties.containsKey(key) && !System.getProperties().containsKey(key)) properties.setProperty(key, value);
    }

    /**
     * Destroy system.
     */
    public static void destroy() {
        for (ServerService s : services) {
            try {
                s.stop();
            } catch (ServiceException ignored) {
                // no-op
            }
        }

        //Stop ServiceManager
        if (manager != null) {
            try {
                manager.stop();
            } catch (ServiceException e) {
                // no-op
            }
            manager = null;
        }

        //Stop Ejb server
        if (ejbServer != null) {
            try {
                ejbServer.stop();
            } catch (ServiceException e) {
                // no-op
            }
            ejbServer = null;
        }

        TomcatWebAppBuilder tomcatWebAppBuilder = (TomcatWebAppBuilder) SystemInstance.get().getComponent(WebAppBuilder.class);
        if (tomcatWebAppBuilder != null) {
            try {
                tomcatWebAppBuilder.stop();
            } catch (Exception ignored) {
                // no-op
            }
        }

        //Destroy OpenEJB system
        OpenEJB.destroy();
    }

    /**
     * Process running web applications for ejb deployments.
     *
     * @param tomcatWebAppBuilder tomcat web app builder instance
     * @param standardServer      tomcat server instance
     */
    private void processRunningApplications(TomcatWebAppBuilder tomcatWebAppBuilder, StandardServer standardServer) {
        for (org.apache.catalina.Service service : standardServer.findServices()) {
            if (service.getContainer() instanceof Engine) {
                Engine engine = (Engine) service.getContainer();
                for (Container engineChild : engine.findChildren()) {
                    if (engineChild instanceof Host) {
                        Host host = (Host) engineChild;
                        for (Container hostChild : host.findChildren()) {
                            if (hostChild instanceof StandardContext) {
                                StandardContext standardContext = (StandardContext) hostChild;
                                int state = TomcatHelper.getContextState(standardContext);
                                if (state == 0) {
                                    // context only initialized
                                    tomcatWebAppBuilder.init(standardContext);
                                } else if (state == 1) {
                                    // context already started
                                    standardContext.addParameter("openejb.start.late", "true");
                                    ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
                                    Thread.currentThread().setContextClassLoader(standardContext.getLoader().getClassLoader());
                                    try {
                                        tomcatWebAppBuilder.init(standardContext);
                                        tomcatWebAppBuilder.beforeStart(standardContext);
                                        tomcatWebAppBuilder.start(standardContext);
                                        tomcatWebAppBuilder.afterStart(standardContext);
                                    } finally {
                                        Thread.currentThread().setContextClassLoader(oldCL);
                                    }
                                    standardContext.removeParameter("openejb.start.late");
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
