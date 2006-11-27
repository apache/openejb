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

package org.apache.openejb;

import java.net.URL;
import java.util.Date;
import java.util.Properties;
import javax.transaction.TransactionManager;

import org.apache.openejb.assembler.Container;
import org.apache.openejb.assembler.DeploymentInfo;
import org.apache.openejb.spi.ApplicationServer;
import org.apache.openejb.spi.Assembler;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.JarUtils;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Messages;
import org.apache.openejb.util.SafeToolkit;

/**
 * OpenEJB is the main factory for bootstrapping and obtaining a references to
 * the ContainerSystem. The properties used in the static init( ) method this
 * class determines the characteristics of the ContainerSystem assembled at run
 * time.
 * <p>
 * An OpenEJB container system is assembled at runtime by the application
 * server according to the properties passed into the static init( ) method.
 * Properties determine the Assembler used to assemble the ContainerSystem as
 * well as the source configuration data used by the Assembler. A set of
 * standard environment property names are defined in the org.apache.openejb.EnvProps
 * class.
 * <p>
 * Below is an example of how an application server would initialize and use
 * this class to assemble and obtain an ContainerSystem.
 * <p>
 * <blockquote><tt>
 * Properties initProps = new Properites();
 * initProps.setProperty(EnvProps.ASSEMBLER,"org.apache.openejb.core.conf.Assembler");
 * initProps.setProperty(EnvProps.CONFIGURATION, "/openejb/bin/config/openejb.xml");
 * OpenEJB myEJB = OpenEJB.init(initProps);
 * </tt>
 * </blockquote>
 * <p>
 * When assembling a ContainerSystem OpenEJB will use the Assembler
 * implementation specified in the EnvProps.ASSEMBLER property. By default the
 * org.apache.openejb.core.conf.Assembler is used. In addition to specifying the
 * Assembler, developers can also specify the location configuration of the
 * configuration file that the Assembler will use. In the case of the default
 * Assembler the configuration property must a URI to an XML document that
 * adheres to the OpenEJB DTD.
 * <p>
 * Custom Assembler can be created that assemble containers from different a
 * different configuration source using a different algorithm and
 * implementation classes. See the org.apache.openejb.spi.Assembler interfce for more
 * details.
 * <p>
 * Initialization properties can also be declared in the System properties. The
 * System properties are combined with properties explicitly passed in the
 * OpenEJB.init( )method. Properties passed into the init( ) method override
 * System properties.
 * <p>
 * OpenEJB provides a singleton interface for the OpenEJB container system.
 * Only one OpenEJB instance can be constructed in the lifetime of a VM
 * process.
 * <p>
 *
 * @version 0.1, 3/21/2000
 * @since JDK 1.2
 * @see org.apache.openejb.alt.config.NovaAssembler
 * @see org.apache.openejb.spi.Assembler
 */

public final class OpenEJB {

    private static ContainerSystem containerSystem;
    private static SecurityService securityService;
    private static ApplicationServer applicationServer;
    private static TransactionManager transactionManager;
    private static Properties props;
    private static boolean initialized;
    private static Logger logger;
    private static Messages messages =
        new Messages("org.apache.openejb");

    public static void init(Properties props) throws OpenEJBException {
        init(props, null);
    }

    /**
	 * @param initProps
	 *            Specifies the Assembler and other properties used to build
	 *            the ContainerSystem
	 * @exception org.apache.openejb.OpenEJBException
	 *                Thrown if a problem occurs building the ContainerSystem
	 * @since JDK 1.2
	 */
    public static void init(Properties initProps, ApplicationServer appServer)
        throws OpenEJBException {

        checkInitailizationState();
        checkAppServer(appServer);

        printStartupBanner();

        loadInitProps(initProps);



        SafeToolkit toolkit = SafeToolkit.getToolkit("OpenEJB");

        Assembler assembler = initializeAssembler(toolkit);

//        securityService    = assembler.getSecurityService();
//        if (securityService == null) {
//            String msg = messages.message( "startup.assemblerReturnedNullSecurityService" );
//            logger.i18n.fatal( msg );
//            throw new OpenEJBException( msg );
//        } else {
//            logger.i18n.debug( "startup.securityService", securityService.getClass().getName() );
//        }
//
//        transactionManager = assembler.getTransactionManager();
//        if (transactionManager == null) {
//            String msg = messages.message( "startup.assemblerReturnedNullTransactionManager" );
//            logger.i18n.fatal( msg );
//            throw new OpenEJBException( msg );
//        } else {
//            logger.i18n.debug( "startup.transactionManager", transactionManager.getClass().getName() );
//        }

        try {
            assembler.build();
        } catch ( OpenEJBException oe ){
            logger.i18n.fatal( "startup.assemblerFailedToBuild", oe );
            throw oe;
        } catch ( Throwable t ){
            String msg = messages.message( "startup.assemblerEncounterUnexpectedBuildError" );
            logger.i18n.fatal( msg, t );
            throw new OpenEJBException( msg, t );
        }

        containerSystem    = assembler.getContainerSystem();
        if (containerSystem == null) {
            String msg = messages.message( "startup.assemblerReturnedNullContainer" );
            logger.i18n.fatal( msg );
            throw new OpenEJBException( msg );
        }

        if (logger.isDebugEnabled()) {
            logger.i18n.debug(
                "startup.debugContainers",
                new Integer(containerSystem.containers().length));

            if (containerSystem.containers().length > 0) {
                Container[] c = containerSystem.containers();
                logger.i18n.debug("startup.debugContainersType");
                for (int i = 0; i < c.length; i++) {
                    String entry = "   ";
                    switch (c[i].getContainerType()) {
                        case Container.ENTITY :
                            entry += "ENTITY      ";
                            break;
                        case Container.STATEFUL :
                            entry += "STATEFUL    ";
                            break;
                        case Container.STATELESS :
                            entry += "STATELESS   ";
                            break;
                    }
                    entry += c[i].getContainerID();
                    logger.i18n.debug("startup.debugEntry", entry);
                }
            }

            logger.i18n.debug(
                "startup.debugDeployments",
                new Integer(containerSystem.deployments().length));
            if (containerSystem.deployments().length > 0) {
                logger.i18n.debug("startup.debugDeploymentsType");
                DeploymentInfo[] d = containerSystem.deployments();
                for (int i = 0; i < d.length; i++) {
                    String entry = "   ";
                    switch (d[i].getComponentType()) {
                        case EJBComponentType.BMP_ENTITY :
                            entry += "BMP_ENTITY  ";
                            break;
                        case EJBComponentType.CMP_ENTITY :
                            entry += "CMP_ENTITY  ";
                            break;
                        case EJBComponentType.STATEFUL :
                            entry += "STATEFUL    ";
                            break;
                        case EJBComponentType.STATELESS :
                            entry += "STATELESS   ";
                            break;
                    }
                    entry += d[i].getDeploymentID();
                    logger.i18n.debug("startup.debugEntry", entry);
                }
            }
        }

        //logger.i18n.debug("There are "+containerSystem.containers().length+"
		// containers.");
        //logger.i18n.debug("There are
		// "+containerSystem.deployments().length+" ejb deployments.");


        logger.i18n.info("startup.ready");
    }

    private static Assembler initializeAssembler(SafeToolkit toolkit) throws OpenEJBException {
        Assembler assembler = null;
        /*
         * Uses the EnvProps.ASSEMBLER property to obtain the Assembler impl.
         */
        String className = props.getProperty("openejb.assembler", "org.apache.openejb.assembler.Assembler");
        //String className = org.apache.openejb.config.NovaAssembler.class.getName();


        logger.i18n.debug("startup.instantiatingAssemberClass", className);

        try {
            assembler = (Assembler) toolkit.newInstance(className);
        } catch (OpenEJBException oe) {
            logger.i18n.fatal("startup.assemblerCannotBeInstanitated", oe);
            throw oe;
        } catch (Throwable t) {
            String msg =
                messages.message("startup.openEjbEncounterUnexpectedError");
            logger.i18n.fatal(msg, t);
            throw new OpenEJBException(msg, t);
        }

        try {
            assembler.init(props);
        } catch (OpenEJBException oe) {
            logger.i18n.fatal("startup.assemblerFailedToInitialize", oe);
            throw oe;
        } catch (Throwable t) {
            String msg =
                messages.message("startup.assemblerEncounterUnexpectedError");
            logger.i18n.fatal(msg, t);
            throw new OpenEJBException(msg, t);
        }
        return assembler;
    }

    private static void checkInitailizationState() throws OpenEJBException {
        if (initialized) {
            String msg = messages.message("startup.alreadyInitialzied");
            logger.i18n.error(msg);
            throw new OpenEJBException(msg);
        } else {
            // Setting the handler system property should be the first thing
            // OpenEJB does.
            JarUtils.setHandlerSystemProperty();

            logger =
                Logger.getInstance(
                    "OpenEJB.startup",
                    "org.apache.openejb");

            initialized = true;
        }
    }

    private static void checkAppServer(ApplicationServer appServer) {
        if (appServer == null)
            logger.i18n.warning("startup.noApplicationServerSpecified");
        applicationServer = appServer;
    }

    private static void loadInitProps(Properties initProps) {
        props = new Properties(System.getProperties());

        if (initProps == null) {
            logger.i18n.debug("startup.noInitializationProperties");
        } else {
            props.putAll(initProps);
        }
    }

    private static void printStartupBanner() {
        /*
		 * Output startup message
		 */
        Properties versionInfo = new Properties();

        try {
            versionInfo.load(
                new URL("resource:/openejb-version.properties")
                    .openConnection()
                    .getInputStream());
        } catch (java.io.IOException e) {
        }
        if (System.getProperty("openejb.nobanner") == null) {
            System.out.println(
                "OpenEJB "
                    + versionInfo.get("version")
                    + "    build: "
                    + versionInfo.get("date")
                    + "-"
                    + versionInfo.get("time"));
            System.out.println("" + versionInfo.get("url"));
        }

        logger.i18n.info(
            "startup.banner",
            versionInfo.get("url"),
            new Date(),
            versionInfo.get("copyright"),
            versionInfo.get("version"),
            versionInfo.get("date"),
            versionInfo.get("time"));
    }

    /**
	 * Gets the <code>TransactionManager</code> that this container manager
	 * exposes to the <code>Container</code> s it manages.
	 *
	 * @return the TransactionManager to be used by this container manager's
	 *         containers when servicing beans
	 * @see "javax.transaction.TransactionManager"
	 * @see org.apache.openejb.spi.TransactionService#getTransactionManager()
	 *      TransactionService.getTransactionManager()
	 */
    public static TransactionManager getTransactionManager() {
        return transactionManager;
    }

    /**
	 * Gets the <code>SecurityService</code> that this container manager
	 * exposes to the <code>Container</code> s it manages.
	 *
	 * @return the SecurityService to be used by this container manager's
	 *         containers when servicing beans
	 * @see org.apache.openejb.spi.SecurityService
	 */
    public static SecurityService getSecurityService() {
        return securityService;
    }

    public static ApplicationServer getApplicationServer() {
        return applicationServer;
    }

    public static void setApplicationServer(ApplicationServer appServer) {
        applicationServer = appServer;
    }

    /**
	 * Gets all the <code>Container</code> s in this container system.
	 *
	 * @return an array of all the Containers
	 * @see Container
	 */
    public static Container[] containers() {
        if (containerSystem == null) {
            // Something went wrong in the configuration.
            logger.i18n.warning("startup.noContainersConfigured");
            return null;
        } else {
            return containerSystem.containers();
        }
    }

    /**
	 * Returns the global JNDI name space for the OpenEJB container system. The
	 * global JNDI name space contains bindings for all enterprise bean EJBHome
	 * object deployed in the entire container system. EJBHome objects are
	 * bound using their deployment-id under the java:openejb/ejb/ namespace.
	 * For example, an enterprise bean with the deployment id = 55555 would be
	 * have its EJBHome bound to the name "java:openejb/ejb/55555"
	 *
	 * @return the global JNDI context
	 */
    public static javax.naming.Context getJNDIContext() {
        return containerSystem.getJNDIContext();
    }

    /**
	 * This method returns a clone of the original properties used to
	 * initialize the OpenEJB class. Modifications to the clone will not affect
	 * the operations of the OpenEJB container system.
	 */
    public static Properties getInitProps() {
        return (Properties) props.clone();
    }

    public static boolean isInitialized() {
        return initialized;
    }

}
