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
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id$
 */


package org.openejb;

import java.net.URL;
import java.security.Permission;
import java.util.*;

import javax.transaction.TransactionManager;

import org.openejb.spi.*;
import org.openejb.util.JarUtils;
import org.openejb.util.Logger;
import org.openejb.util.Messages;
import org.openejb.util.SafeToolkit;


/**
 * OpenEJB is the main factory for bootstrapping and obtaining a references to
 * the ContainerSystem.  The properties used in the static init( )
 * method this class determines the characteristics of the ContainerSystem
 * assembled at run time.
 * <p>
 * An OpenEJB container system is assembled at runtime by the application server
 * according to the properties passed into the static init( ) method.
 * Properties determine the Assembler used to assemble the ContainerSystem as
 * well as the source configuration data used by the Assembler.  A set of
 * standard environment property names are defined in the org.openejb.EnvProps
 * class.
 * <p>
 * Below is an example of how an application server would initialize and use this
 * class to assemble and obtain an ContainerSystem.
 * <p><blockquote><tt>
 * Properties initProps = new Properites();
 * initProps.setProperty(EnvProps.ASSEMBLER,"org.openejb.core.conf.Assembler");
 * initProps.setProperty(EnvProps.CONFIGURATION, "/openejb/bin/config/openejb.xml");
 * OpenEJB myEJB = OpenEJB.init(initProps);
 * </tt></blockquote>
 * <p>
 * When assembling a ContainerSystem OpenEJB will use the Assembler implementation
 * specified in the EnvProps.ASSEMBLER property.  By default the
 * org.openejb.core.conf.Assembler is used.  In addition to specifying the Assembler,
 * developers can also specify the location configuration of the configuration file
 * that the Assembler will use.  In the case of the default Assembler the configuration
 * property must a URI to an XML document that adheres to the OpenEJB DTD.
 * <p>
 * Custom Assembler can be created that assemble containers from different a
 * different configuration source using a different algorithm and implementation
 * classes.  See the org.openejb.spi.Assembler interfce for more details.
 * <p>
 * Initialization properties can also be declared in the System properties.
 * The System properties are combined with properties explicitly passed in the
 * OpenEJB.init( )method.  Properties passed into the init( ) method override
 * System properties.
 * <p>
 * OpenEJB provides a singleton interface for the OpenEJB container system. Only one OpenEJB
 * instance can be constructed in the lifetime of a VM process.
 * <p>
 * @author Richard Monson-Haefel
 * @author David Blevins
 * @version 0.1, 3/21/2000
 * @since JDK 1.2
 * @see org.openejb.EnvProps
 * @see org.openejb.spi.Assembler
 */

public final class OpenEJB {

    private static Map                containerSystems;
    private static ApplicationServer  applicationServer;
    private static TransactionManager transactionManager;
    private static Deployer           deployer;
    private static Properties         props;
    private static boolean            initialized;
    private static Logger             logger;
    private static Messages           messages = new Messages( "org.openejb.util.resources" );
    private static String             defaultContainerSystem;

    public static void init(Properties props)
    throws OpenEJBException{
        init(props,null);
    }

    /**
     *
     * @param props Specifies the Assembler and other properties used to build the ContainerSystem
     * @exception org.openejb.OpenEJBException Thrown if a problem occurs building the ContainerSystem
     * @since JDK 1.2
     */
    public static void init(Properties initProps, ApplicationServer appServer) throws OpenEJBException {

        if ( initialized ) {
	    String msg = messages.message( "startup.alreadyInitialzied" );
            logger.i18n.error( msg );
            throw new OpenEJBException( msg );
        } else {
            // Setting the handler system property should be the first thing
            // OpenEJB does.
            JarUtils.setHandlerSystemProperty();

            logger = Logger.getInstance( "OpenEJB.startup", "org.openejb.util.resources" );

            initialized = true;
        }

	/*
	 * Output startup message
	 */
	Properties versionInfo = new Properties();

	try {
	    versionInfo.load( new URL( "resource:/openejb-version.properties" ).openConnection().getInputStream() );
	} catch (java.io.IOException e) {
	}
        if( System.getProperty( "openejb.nobanner" ) == null ) {
            System.out.println( "OpenEJB " + versionInfo.get( "version" ) +"    build: "+versionInfo.get( "date" )+"-"+versionInfo.get( "time" ));
            System.out.println( "" + versionInfo.get( "url" ) );
        }

	logger.i18n.info( "startup.banner", versionInfo.get( "url" ), new Date(), versionInfo.get( "copyright" ),
		     versionInfo.get( "version" ), versionInfo.get( "date" ), versionInfo.get( "time" ) );

        /* DMB: This is causing bug 725781.
         * I can't even remember why we decided to add a default security manager.
         * the testsuite runs fine without it, so out it goes for now.
        SecurityManager sm = System.getSecurityManager();
        if (sm == null) {
            try{
                logger.i18n.debug( "startup.noSecurityManagerInstalled" );
                System.setSecurityManager(new SecurityManager(){
                    public void checkPermission(Permission perm) {}
                    public void checkPermission(Permission perm, Object context) {}

                });
            } catch (Exception e){
                logger.i18n.warning( "startup.couldNotInstalllDefaultSecurityManager", e.getClass().getName(), e.getMessage() );
            }
        }
        */

        props = new Properties(System.getProperties());

        if ( initProps == null ) {
            logger.i18n.debug( "startup.noInitializationProperties" );
        } else {
            props.putAll( initProps );
        }

        if ( appServer == null ) logger.i18n.warning( "startup.noApplicationServerSpecified" );
        applicationServer = appServer;


        SafeToolkit toolkit = SafeToolkit.getToolkit("OpenEJB");

        /* Uses the EnvProps.ASSEMBLER property to obtain the Assembler impl.
           Default is org.openejb.core.conf.Assembler*/
        String className = props.getProperty( EnvProps.ASSEMBLER );
        if ( className == null ) {
            className = props.getProperty( "openejb.assembler", "org.openejb.alt.assembler.dynamic.DynamicAssembler" );
        } else {
            logger.i18n.warning( "startup.deprecatedPropertyName", EnvProps.ASSEMBLER );
        }

        logger.i18n.debug( "startup.instantiatingAssemberClass", className );
        Assembler assembler = null;

        try {
            assembler = (Assembler)toolkit.newInstance(className);
        } catch ( OpenEJBException oe ){
            logger.i18n.fatal( "startup.assemblerCannotBeInstanitated", oe );
            throw oe;
        } catch ( Throwable t ){
	    String msg = messages.message(  "startup.openEjbEncounterUnexpectedError" );
            logger.i18n.fatal( msg, t );
            throw new OpenEJBException( msg, t );
        }

        try {
            assembler.init(props);
        } catch ( OpenEJBException oe ){
            logger.i18n.fatal( "startup.assemblerFailedToInitialize", oe );
            throw oe;
        } catch ( Throwable t ){
	    String msg = messages.message( "startup.assemblerEncounterUnexpectedError" );
            logger.i18n.fatal( msg, t );
            throw new OpenEJBException( msg, t );
        }

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

        ContainerSystem[] containerSystems = assembler.getContainerSystems();
        if (containerSystems == null) {
            String msg = messages.message( "startup.assemblerReturnedNullContainer" );
            logger.i18n.fatal( msg );
            throw new OpenEJBException( msg );
        }
        defaultContainerSystem = assembler.getDefaultContainerSystemID();
        if(defaultContainerSystem == null && containerSystems.length > 0) {
            defaultContainerSystem = containerSystems[0].getId();
        }
        OpenEJB.containerSystems = new HashMap();
        for(int j=0; j<containerSystems.length; j++) {
            OpenEJB.containerSystems.put(containerSystems[j].getId(), containerSystems[j]);
            if (containerSystems[j].getSecurityService() == null) {
                String msg = messages.format( "startup.assemblerReturnedNullSecurityService", containerSystems[j].getId());
                logger.i18n.fatal( msg );
                throw new OpenEJBException( msg );
            } else {
                logger.i18n.debug( "startup.securityService", containerSystems[j].getId(), containerSystems[j].getSecurityService().getClass().getName() );
            }
        }

        if (logger.isDebugEnabled()){
            int containers = 0;
            int deployments = 0;
            for(int j=0; j<containerSystems.length; j++) {
                if (containerSystems[j].containers().length > 0) {
                    containers += containerSystems[j].containers().length;
                    Container[] c = containerSystems[j].containers();
                    logger.i18n.debug( "startup.debugContainersType" );
                    for (int i=0; i < c.length; i++){
                        String entry = "   ";
                        switch ( c[i].getContainerType() ) {
                        case Container.ENTITY:    entry += "ENTITY      "; break;
                        case Container.STATEFUL:  entry += "STATEFUL    "; break;
                        case Container.STATELESS: entry += "STATELESS   "; break;
                        }
                        entry += c[i].getContainerID();
                        logger.i18n.debug( "startup.debugEntry", entry) ;
                    }
                }

                if (containerSystems[j].deployments().length > 0) {
                    deployments += containerSystems[j].deployments().length;
                    logger.i18n.debug( "startup.debugDeploymentsType" );
                    DeploymentInfo[] d = containerSystems[j].deployments();
                    for (int i=0; i < d.length; i++){
                        String entry = "   ";
                        switch ( d[i].getComponentType() ) {
                        case DeploymentInfo.BMP_ENTITY: entry += "BMP_ENTITY  "; break;
                        case DeploymentInfo.CMP_ENTITY: entry += "CMP_ENTITY  "; break;
                        case DeploymentInfo.STATEFUL:   entry += "STATEFUL    "; break;
                        case DeploymentInfo.STATELESS:  entry += "STATELESS   "; break;
                        }
                        entry += d[i].getDeploymentID();
                        logger.i18n.debug( "startup.debugEntry", entry );
                    }
                }
            }
            logger.i18n.debug( "startup.debugDeployments", new Integer(deployments) );
            logger.i18n.debug( "startup.debugContainers", new Integer(containers) );
        }

      //logger.i18n.debug("There are "+containerSystem.containers().length+" containers.");
      //logger.i18n.debug("There are "+containerSystem.deployments().length+" ejb deployments.");

        transactionManager = assembler.getTransactionManager();
        if (transactionManager == null) {
	    String msg = messages.message( "startup.assemblerReturnedNullTransactionManager" );
            logger.i18n.fatal( msg );
            throw new OpenEJBException( msg );
        } else {
            logger.i18n.debug( "startup.transactionManager", transactionManager.getClass().getName() );
        }

        deployer = assembler.getDeployer();
        if(deployer != null) {
            deployer.startDeploying();
        }

        logger.i18n.info( "startup.ready" );
    }

    /**
     * Gets the <code>TransactionManager</code> that this container manager exposes to the <code>Container</code>s it manages.
     *
     * @return the TransactionManager to be used by this container manager's containers when servicing beans
     * @see "javax.transaction.TransactionManager"
     * @see org.openejb.spi.TransactionService#getTransactionManager() TransactionService.getTransactionManager()
     */
    public static TransactionManager getTransactionManager( ){
        return transactionManager;
    }

    public static ApplicationServer getApplicationServer(){
        return applicationServer;
    }

    /**
     * Gets the current deployer implementation.  This may be null of the
     * current configuration does not support deployment at runtime.
     */
    public static Deployer getDeployer() {
        return deployer;
    }

    /**
     * Gets the <code>DeploymentInfo</code> object for the bean with the specified deployment id in the specified container system.
     *
     * @param id the deployment id of the deployed bean.
     * @return the DeploymentInfo object associated with the bean.
     * @see DeploymentInfo
     * @see Container#getDeploymentInfo(Object) Container.getDeploymentInfo
     * @see DeploymentInfo#getDeploymentID()
     */
    public static DeploymentInfo getDeploymentInfo(String containerSystem, Object id){
        return ((ContainerSystem)containerSystems.get(containerSystem)).getDeploymentInfo(id);
    }

    /**
     * Gets the <code>DeploymentInfo</code> objects for all the beans deployed in all the containers in the specified container system.
     *
     * @return an array of DeploymentInfo objects
     * @see DeploymentInfo
     * @see Container#deployments() Container.deployments()
     */
    public static DeploymentInfo [] deployments(String containerSystem){
        return ((ContainerSystem)containerSystems.get(containerSystem)).deployments();
    }

    /**
     * Gets the ID of the default <code>ContainerSystem</code>.
     */
    public static String getDefaultContainerSystemID() {
        return defaultContainerSystem;
    }

    /**
     * Returns the <code>ContainerSystem</code> with the specified id.
     */
    public static ContainerSystem getContainerSystem(String id) {
        return (ContainerSystem)containerSystems.get(id);
    }

    /**
     * Returns all the <code>ContainerSystems</code> available.
     */
    public static ContainerSystem[] getContainerSystems() {
        return (ContainerSystem[])containerSystems.values().toArray(new ContainerSystem[containerSystems.size()]);
    }

    /**
     * Returns the IDs of all the <code>ContainerSystems</code>.
     */
    public static String[] getContainerSystemIDs() {
        ContainerSystem[] cs = (ContainerSystem[])containerSystems.values().toArray(new ContainerSystem[containerSystems.size()]);
        String[] ids = new String[cs.length];
        for(int i=0; i<ids.length; ids[i] = cs[i++].getId());
        return ids;
    }

    /**
     * Updates the <code>ContainerSystem</code> with the specified id (for
     * example, in the case of a redeployment).
     */
    public static void setContainerSystem(String id, ContainerSystem system) {
        containerSystems.put(id, system);
    }

    /**
     * Removes the <code>ContainerSystem</code> with the specified id (for
     * example, in the case of an undeployment).  If this was the default
     * ContainerSystem, you may want to set a new default.
     */
    public static void removeContainerSystem(String id) {
        containerSystems.remove(id);
    }

    /**
     * Returns the <code>Container</code> in the specified container system with the specified id.
     *
     * @param id the id of the Container
     * @return the Container associated with the id
     * @see Container
     * @see ContainerManager#getContainer(Object) ContainerManager.getContainer
     * @see Container#getContainerID() Container.getContainerID()
     * @see DeploymentInfo#getContainerID() DeploymentInfo.getContainerID()
     */
    public static Container getContainer(String containerSystem, Object id){
        return ((ContainerSystem)containerSystems.get(containerSystem)).getContainer(id);
    }

    /**
     * Gets all the <code>Container</code>s in the specified container system.
     *
     * @return an array of all the Containers
     * @see Container
     * @see ContainerManager#containers() ContainerManager.containers()
     */
    public static Container [] containers(String containerSystem) {
        if ( containerSystems == null ) {// Something went wrong in the configuration.
            logger.i18n.warning( "startup.noContainersConfigured" );
            return null;
        } else {
            return ((ContainerSystem)containerSystems.get(containerSystem)).containers();
	}
    }

    /**
    * Returns the JNDI name space for the specified container system.
    * The container system's JNDI name space contains bindings for all enterprise bean
    * EJBHome object deployed in the entire container system.  EJBHome objects
    * are bound using their deployment-id under the java:openejb/ejb/ namespace.
    * For example, an enterprise bean with the deployment id = 55555 would be
    * have its EJBHome bound to the name "java:openejb/ejb/55555"
    *
    * @return the container system's JNDI context
    */
    public static javax.naming.Context getJNDIContext(String containerSystem){
        return ((ContainerSystem)containerSystems.get(containerSystem)).getJNDIContext();
    }

    /**
    * This method returns a clone of the original properties used to initialize the OpenEJB
    * class.  Modifications to the clone will not affect the operations of the OpenEJB
    * container system.
    */
    public static Properties getInitProps( ){
        return (Properties)props.clone();
    }

    public static boolean isInitialized(){
        return initialized;
    }

}
