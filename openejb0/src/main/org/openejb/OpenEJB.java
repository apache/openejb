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
import org.apache.log4j.BasicConfigurator;

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

public class OpenEJB {


    protected static ContainerSystem containerSystem;
    protected static SecurityService securityService;
    protected static ApplicationServer applicationServer;
    protected static TransactionManager transactionManager;
    protected static Properties initProps;

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
    public static void init(Properties props, ApplicationServer appServer) throws OpenEJBException{
        SecurityManager sm = System.getSecurityManager();
        if (sm == null) {
            try{
                System.setSecurityManager(new SecurityManager(){
                    public void checkPermission(Permission perm) {}
                    public void checkPermission(Permission perm, Object context) {}
                
                });
            } catch (Exception e){}
        }
        initProps = props;
        applicationServer = appServer;
        if(containerSystem != null)
            throw new OpenEJBException("OpenEJB already initiated");

        SafeToolkit.getToolkit("OpenEJB").getSafeProperties(props);
        Enumeration enum = System.getProperties().propertyNames();
        while(enum.hasMoreElements()){
            String name = (String)enum.nextElement();
            if(!props.containsKey(name)){
                props.setProperty(name, System.getProperties().getProperty(name));
            }
        }

        /* Uses the EnvProps.ASSEMBLER property to obtain the Assembler impl.
           Default is org.openejb.core.conf.Assembler*/
        Assembler assembler = AssemblerFactory.getAssembler(props);
        assembler.build();
        containerSystem = assembler.getContainerSystem();
        securityService = assembler.getSecurityService();
        transactionManager = assembler.getTransactionManager();

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
        return (Properties)initProps.clone();
    }
}
