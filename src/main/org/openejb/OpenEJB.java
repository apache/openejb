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

import java.security.Permission;
import java.net.URL;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;
import javax.transaction.TransactionManager;
import org.openejb.spi.ApplicationServer;
import org.openejb.spi.Assembler;
import org.openejb.spi.AssemblerFactory;
import org.openejb.spi.ContainerSystem;
import org.openejb.spi.SecurityService;
import org.openejb.util.SafeToolkit;
import org.apache.log4j.Category;

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
 * @see org.openejb.core.conf.Assembler
 * @see org.openejb.spi.Assembler
 */

public final class OpenEJB {

    private static ContainerSystem    containerSystem;
    private static SecurityService    securityService;
    private static ApplicationServer  applicationServer;
    private static TransactionManager transactionManager;
    private static Properties         props;
    private static boolean            initialized;
    private static Category           logger;

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
            logger.error( "Cannot initialize OpenEJB a second time in the same VM." );
            throw new OpenEJBException( "OpenEJB has already been initialized." );
        }

	/*
	 * Setup the java protocol handler path to include org.openejb.util.urlhandler
	 * so that org.openejb.util.urlhandler.resource.Handler will be used for URLs
	 * of the form "resource:/path".
	 */
	try {
	    String oldPkgs = System.getProperty( "java.protocol.handler.pkgs" );

	    if ( oldPkgs == null )
		System.setProperty( "java.protocol.handler.pkgs", "org.openejb.util.urlhandler" );
	    else if ( oldPkgs.indexOf( "org.openejb.util.urlhandler" ) < 0 )
		System.setProperty( "java.protocol.handler.pkgs", oldPkgs + "|" + "org.openejb.util.urlhandler" );
	} catch ( SecurityException ex ) {
	}
	
	// Set log4j's configuration (Note the URL's form)
	if( System.getProperty( "log4j.configuration" ) == null ) {
	    System.setProperty( "log4j.configuration", "resource:/default.logging.conf" );
	}

	logger = Category.getInstance( "OpenEJB" );
        

	/*
	 * Output startup message
	 */
	Properties versionInfo = new Properties();

	try {
	    versionInfo.load( new URL( "resource:/openejb-version.properties" ).openConnection().getInputStream() );
	} catch (java.io.IOException e) {
	}
	logger.info( "" );
        logger.info( "********************************************************************************" );
        logger.info( "OpenEJB " + versionInfo.get( "url" ) );
        logger.info( "Startup: " + new Date() );
	logger.info( versionInfo.get( "copyright" ) );
	logger.info( "Version: " + versionInfo.get( "version" ) );
	logger.info( "Build date: " + versionInfo.get( "date" ) );
	logger.info( "Build time: " + versionInfo.get( "time" ) );



        SecurityManager sm = System.getSecurityManager();
        if (sm == null) {
            try{
                logger.debug("No SecurityManager installed. Installing default.");
                System.setSecurityManager(new SecurityManager(){
                    public void checkPermission(Permission perm) {}
                    public void checkPermission(Permission perm, Object context) {}
                
                });
            } catch (Exception e){
                logger.warn("Could not install default SecurityManager: "+e.getClass().getName()+": "+e.getMessage());
            }
        }
        
        props = new Properties(System.getProperties());

        if ( initProps == null ) {
            logger.info("No initialization properties were passed in, using system properties instead.");
        } else {
            props.putAll( initProps );            
        }
        
        if ( appServer == null ) logger.warn("No ApplicationServer was specified!  The container system will only be accessible by same-vm clients via the IntraVm Server.");
        applicationServer = appServer;
            

        SafeToolkit toolkit = SafeToolkit.getToolkit("OpenEJB");

        /* Uses the EnvProps.ASSEMBLER property to obtain the Assembler impl.
           Default is org.openejb.core.conf.Assembler*/
        String className = props.getProperty(EnvProps.ASSEMBLER);
        if (className == null) {
            className = props.getProperty("openejb.assembler","org.openejb.alt.assembler.classic.Assembler");
        } else {
            logger.warn("Deprecated: The propery name '"+EnvProps.ASSEMBLER+"' is deprecated and will be removed in a future version of OpenEJB, please use the property name 'openejb.assembler' instead.");
        }
        
        logger.debug("Instantiating assembler class "+className);
        Assembler assembler = null;

        try {
            assembler = (Assembler)toolkit.newInstance(className);
        } catch ( OpenEJBException oe ){
            logger.fatal("OpenEJB has encountered a fatal error and cannot be started: Assembler cannot be instantiated.", oe);
            throw oe;
        } catch ( Throwable t ){
            logger.fatal("OpenEJB has encountered a fatal error and cannot be started: OpenEJB encountered an unexpected error while attempting to instantiate the assembler.", t);
            throw new OpenEJBException("OpenEJB encountered an unexpected error while attempting to instantiate the assembler.  Received exception " + t.getClass().getName() + ".", t);
        }
        
        try {
            assembler.init(props);
        } catch ( OpenEJBException oe ){
            logger.fatal("OpenEJB has encountered a fatal error and cannot be started: Assembler failed to initialize.", oe);
            throw oe;
        } catch ( Throwable t ){
            logger.fatal("OpenEJB has encountered a fatal error and cannot be started: The Assembler encountered an unexpected error while attempting to initialize.", t);
            throw new OpenEJBException("The Assembler encountered an unexpected error while attempting to initialize.  Received exception " + t.getClass().getName() + ".", t);
        }
        
        try {
            assembler.build();
        } catch ( OpenEJBException oe ){
            logger.fatal("OpenEJB has encountered a fatal error and cannot be started: Assembler failed to build the container system.", oe);
            throw oe;
        } catch ( Throwable t ){
            logger.fatal("OpenEJB has encountered a fatal error and cannot be started: The Assembler encountered an unexpected error while attempting to build the container system.", t);
            throw new OpenEJBException("The Assembler encountered an unexpected error while attempting to build the container system.  Received exception " + t.getClass().getName() + ".", t);
        }
        
        containerSystem    = assembler.getContainerSystem();
        if (containerSystem == null) {
            logger.fatal("OpenEJB has encountered a fatal error and cannot be started: The Assembler returned a null container system.");
            throw new OpenEJBException("The Assembler returned a null container system.");
        }

        if (logger.isDebugEnabled()){
            logger.debug("Containers        : "+containerSystem.containers().length);
            
            if (containerSystem.containers().length > 0) {
                Container[] c = containerSystem.containers();
                logger.debug("   Type        Container ID");
                for (int i=0; i < c.length; i++){
                    String entry = "   ";
                    switch ( c[i].getContainerType() ) {
                    case Container.ENTITY:    entry += "ENTITY      "; break;
                    case Container.STATEFUL:  entry += "STATEFUL    "; break;
                    case Container.STATELESS: entry += "STATELESS   "; break;
                    }
                    entry += c[i].getContainerID();
                    logger.debug(entry);
                }
            }
    
            logger.debug("Deployments       : "+containerSystem.deployments().length);
            if (containerSystem.deployments().length > 0) {
                logger.debug("   Type        Deployment ID");
                DeploymentInfo[] d = containerSystem.deployments();
                for (int i=0; i < d.length; i++){
                    String entry = "   ";
                    switch ( d[i].getComponentType() ) {
                    case DeploymentInfo.BMP_ENTITY: entry += "BMP_ENTITY  "; break;
                    case DeploymentInfo.CMP_ENTITY: entry += "CMP_ENTITY  "; break;
                    case DeploymentInfo.STATEFUL:   entry += "STATEFUL    "; break;
                    case DeploymentInfo.STATELESS:  entry += "STATELESS   "; break;
                    }
                    entry += d[i].getDeploymentID();
                    logger.debug(entry);
                }
            }
        }
        
      //logger.debug("There are "+containerSystem.containers().length+" containers.");
      //logger.debug("There are "+containerSystem.deployments().length+" ejb deployments.");

        securityService    = assembler.getSecurityService();
        if (securityService == null) {
            logger.fatal("OpenEJB has encountered a fatal error and cannot be started: The Assembler returned a null security service.");
            throw new OpenEJBException("The Assembler returned a null security service.");
        } else {
            logger.debug("SecurityService   : "+securityService.getClass().getName());
        }
        
        transactionManager = assembler.getTransactionManager();
        if (transactionManager == null) {
            logger.fatal("OpenEJB has encountered a fatal error and cannot be started: The Assembler returned a null transaction manager.");
            throw new OpenEJBException("The Assembler returned a null transaction service.");
        } else {
            logger.debug("TransactionManager: "+transactionManager.getClass().getName());
        }
        
	initialized = true;

        logger.info("OpenEJB ready.");
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

    /**
     * Gets the <code>SecurityService</code> that this container manager exposes to the <code>Container</code>s it manages.
     *
     * @return the SecurityService to be used by this container manager's containers when servicing beans
     * @see org.openejb.spi.SecurityService
     */
    public static SecurityService getSecurityService( ){
        return securityService;
    }
    
    public static ApplicationServer getApplicationServer(){
        return applicationServer;
    }

    /**
     * Gets the <code>DeploymentInfo</code> object for the bean with the specified deployment id.
     *
     * @param id the deployment id of the deployed bean.
     * @return the DeploymentInfo object associated with the bean.
     * @see DeploymentInfo
     * @see Container#getDeploymentInfo(Object) Container.getDeploymentInfo
     * @see DeploymentInfo#getDeploymentID()
     */
    public static DeploymentInfo getDeploymentInfo(Object id){
        return containerSystem.getDeploymentInfo(id);
    }

    /**
     * Gets the <code>DeploymentInfo</code> objects for all the beans deployed in all the containers in this container system.
     *
     * @return an array of DeploymentInfo objects
     * @see DeploymentInfo
     * @see Container#deployments() Container.deployments()
     */
    public static DeploymentInfo [] deployments( ){
        return containerSystem.deployments();
    }

    /**
     * Returns the <code>Container</code> in this container system with the specified id.
     *
     * @param id the id of the Container
     * @return the Container associated with the id
     * @see Container
     * @see ContainerManager#getContainer(Object) ContainerManager.getContainer
     * @see Container#getContainerID() Container.getContainerID()
     * @see DeploymentInfo#getContainerID() DeploymentInfo.getContainerID()
     */
    public static Container getContainer(Object id){
        return containerSystem.getContainer(id);
    }

    /**
     * Gets all the <code>Container</code>s in this container system.
     *
     * @return an array of all the Containers
     * @see Container
     * @see ContainerManager#containers() ContainerManager.containers()
     */
    public static Container [] containers( ){
        //TODO:2: In the event there are no containers, we should log a warning or error.
        if(containerSystem==null) // Something went wrong in the configuration.
            return null;   
        else
            return containerSystem.containers();
    }

    /**
    * Returns the global JNDI name space for the OpenEJB container system.
    * The global JNDI name space contains bindings for all enterprise bean
    * EJBHome object deployed in the entire container system.  EJBHome objects
    * are bound using their deployment-id under the java:openejb/ejb/ namespace.
    * For example, an enterprise bean with the deployment id = 55555 would be
    * have its EJBHome bound to the name "java:openejb/ejb/55555"
    *
    * @return the global JNDI context
    */
    public static javax.naming.Context getJNDIContext(){
        return containerSystem.getJNDIContext();
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
