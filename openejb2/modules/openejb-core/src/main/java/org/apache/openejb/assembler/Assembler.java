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
package org.apache.openejb.assembler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import javax.naming.NamingException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;
import javax.transaction.TransactionManager;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.naming.ConnectionFactoryReference;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.OpenEJBErrorHandler;
import org.apache.openejb.util.SafeToolkit;
/**
 * <pre>
 * This method performs the following actions(in order):
 * 
 * 1  Assembles Containers and Deployments
 * 1.1  Assembles StatefulContainer(s)
 * 1.1.1  Assembles Stateful SessionBean Deployments
 * 1.2  Assembles StatelessContainer(s)
 * 1.2.1  Assembles Stateless SessionBean Deployments
 * 1.3  Assembles EntityContainer(s)
 * 1.3.1  Assembles EntityBean Deployments
 * 2  Assembles SecurityService
 * 3  Assembles TransactionService
 * 4  Assembles ConnectionManager(s)
 * 5  Assembles Connector(s)
 * 7  Applies MethodPermission(s)
 * 8  Applies SecurityRole(s)
 * 8  Applies TransactionAttribute(s)
 * 10  Assembles ProxyFactory
 * 11  Assembles bean JNDI namespaces
 * </pre>
 * 
 * @see org.apache.openejb.spi.Assembler
 * @see OpenEjbConfigurationFactory
 */
public class Assembler extends AssemblerTool implements org.apache.openejb.spi.Assembler {
    private org.apache.openejb.assembler.ContainerSystem containerSystem;
    private TransactionManager transactionManager;
    private org.apache.openejb.spi.SecurityService securityService;
    private HashMap remoteJndiContexts = null;

    public org.apache.openejb.spi.ContainerSystem getContainerSystem() {
        return containerSystem;
    }

    public TransactionManager getTransactionManager() {
        try {
            /*[5] Assemble TransactionManager /////////////////////////////////*/
            transactionManager = assembleTransactionManager(config.facilities.transactionService);

            /*[5]\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*/
        } catch (OpenEJBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return transactionManager;
    }

    public SecurityService getSecurityService() {
        if (securityService == null) {
            try {
                /*[3] Assemble SecurityServices ////////////////////////////////////*/
                securityService = assembleSecurityService(config.facilities.securityService);
            } catch (OpenEJBException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NamingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            /*[3]\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*/
        }
        return securityService;
    }

    protected SafeToolkit toolkit = SafeToolkit.getToolkit("Assembler");
    protected Properties props;
    protected OpenEjbConfiguration config;


    //==================================
    // Error messages

    private String INVALID_CONNECTION_MANAGER_ERROR = "Invalid connection manager specified for connector identity = ";

    // Error messages
    //==================================


    public Assembler() {
    }

    public void init(Properties props) throws OpenEJBException{
        this.props = props;

        /* Get Configuration ////////////////////////////*/
        String className = props.getProperty("openejb.configurator","org.apache.openejb.config.ConfigurationFactory");

        OpenEjbConfigurationFactory configFactory = (OpenEjbConfigurationFactory)toolkit.newInstance(className);
        configFactory.init(props);
        config = configFactory.getOpenEjbConfiguration();
        /*\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*/


        /* Add IntraVM JNDI service /////////////////////*/
        String str = System.getProperty(javax.naming.Context.URL_PKG_PREFIXES);
        if (str==null)
            str = ":org.apache.openejb.naming";
        else
            str = str+":org.apache.openejb.naming";
        System.setProperty(javax.naming.Context.URL_PKG_PREFIXES, str);
        /*\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*/
    }

    public void build() throws OpenEJBException{
        try {
            containerSystem = buildContainerSystem(config);
            containerSystem.getJNDIContext().bind("java:openejb/TransactionManager",getTransactionManager());
            containerSystem.getJNDIContext().bind("java:openejb/SecurityService",getSecurityService());
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
        }
    }

    /////////////////////////////////////////////////////////////////////
    ////
    ////    Public Methods Used for Assembly
    ////
    /////////////////////////////////////////////////////////////////////

    /**
     * When given a complete OpenEjbConfiguration graph this method,
     * will construct an entire container system and return a reference to that
     * container system, as ContainerSystem instance.
     * 
     * This method leverage the other assemble and apply methods which
     * can be used independently.
     * 
     * Assembles and returns the {@link ContainerSystem} using the
     * information from the {@link OpenEjbConfiguration} object passed in.
     * <pre>
     * This method performs the following actions(in order):
     * 
     * 1  Assembles ProxyFactory
     * 2  Assembles Containers and Deployments
     * 3  Assembles SecurityService
     * 4  Apply method permissions, role refs, and tx attributes
     * 5  Assembles TransactionService
     * 6  Assembles ConnectionManager(s)
     * 7  Assembles Connector(s)
     * </pre>
     * 
     * @param configInfo
     * @return 
     * @exception throws    Exception if there was a problem constructing the ContainerSystem.
     * @exception Exception
     * @see OpenEjbConfiguration
     * @see org.apache.openejb.assembler.ContainerSystem
     */
    public org.apache.openejb.assembler.ContainerSystem buildContainerSystem(OpenEjbConfiguration configInfo)throws Exception{

        /*[1] Assemble ProxyFactory //////////////////////////////////////////
            
            This operation must take place first because of interdependencies.
            As DeploymentInfo objects are registered with the ContainerSystem using the
            ContainerSystem.addDeploymentInfo() method, they are also added to the JNDI
            Naming Service for OpenEJB.  This requires that a proxy for the deployed bean's 
            EJBHome be created. The proxy requires that the default proxy factory is set.
        */

        this.applyProxyFactory(configInfo.facilities.intraVmServer);
        /*[1]\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*/


        ContainerSystemInfo containerSystemInfo = configInfo.containerSystem;



        org.apache.openejb.assembler.ContainerSystem containerSystem = new org.apache.openejb.assembler.ContainerSystem();

        /*[2] Assemble Containers and Deployments ///////////////////////////////////*/

        assembleContainers(containerSystem,containerSystemInfo);
        /*[2]\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*/



        /*[4] Apply method permissions, role refs, and tx attributes ////////////////////////////////////*/

        // roleMapping used later in buildMethodPermissions
        AssemblerTool.RoleMapping roleMapping = new AssemblerTool.RoleMapping(configInfo.facilities.securityService.roleMappings);
        org.apache.openejb.assembler.DeploymentInfo [] deployments = containerSystem.deployments();
        for (int i = 0; i < deployments.length; i++) {
            applyMethodPermissions((org.apache.openejb.assembler.CoreDeploymentInfo)deployments[i], containerSystemInfo.methodPermissions, roleMapping);
            applyTransactionAttributes((org.apache.openejb.assembler.CoreDeploymentInfo)deployments[i],containerSystemInfo.methodTransactions);
        }

        ArrayList list = new ArrayList();
        if (containerSystemInfo.entityContainers!=null)list.addAll(Arrays.asList(containerSystemInfo.entityContainers));
        if (containerSystemInfo.statefulContainers!=null)list.addAll(Arrays.asList(containerSystemInfo.statefulContainers));
        if (containerSystemInfo.statelessContainers!=null)list.addAll(Arrays.asList(containerSystemInfo.statelessContainers));
        Iterator iterator = list.iterator();
        while (iterator.hasNext()) {
            ContainerInfo container = (ContainerInfo)iterator.next();
            for (int z = 0; z < container.ejbeans.length; z++) {
                CoreDeploymentInfo deployment= (org.apache.openejb.assembler.CoreDeploymentInfo)containerSystem.getDeploymentInfo(container.ejbeans[z].ejbDeploymentId);
                applySecurityRoleReference(deployment, container.ejbeans[z], roleMapping);
            }
        }
        /*[4]\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*/
        if (configInfo.facilities.remoteJndiContexts!=null) {
            for (int i = 0; i < configInfo.facilities.remoteJndiContexts.length; i++) {
                javax.naming.InitialContext cntx = assembleRemoteJndiContext(configInfo.facilities.remoteJndiContexts[i]);   
                containerSystem.getJNDIContext().bind("java:openejb/remote_jndi_contexts/"+configInfo.facilities.remoteJndiContexts[i].jndiContextId, cntx);
            }

        }



        /*[6] Assemble Connector(s) //////////////////////////////////////////*/
        HashMap connectionManagerMap = new HashMap();
        // connectors are optional in the openejb_config.dtd
        if (configInfo.facilities.connectionManagers != null) {
            for (int i = 0; i < configInfo.facilities.connectionManagers.length;i++) {
                ConnectionManagerInfo cmInfo = configInfo.facilities.connectionManagers[i];
                ConnectionManager connectionManager = assembleConnectionManager(cmInfo);
                connectionManagerMap.put(cmInfo.connectionManagerId,connectionManager);
            }
        }
        // connectors are optional in the openejb_config.dtd
        if (configInfo.facilities.connectors != null) {
            for (int i = 0; i < configInfo.facilities.connectors.length; i++) {
                ConnectorInfo conInfo = configInfo.facilities.connectors[i];

                ConnectionManager connectionManager = (ConnectionManager)connectionManagerMap.get(conInfo.connectionManagerId);
                if (connectionManager == null)
                    throw new RuntimeException(INVALID_CONNECTION_MANAGER_ERROR + conInfo.connectorId);

                ManagedConnectionFactory managedConnectionFactory = assembleManagedConnectionFactory(conInfo.managedConnectionFactory);

                ConnectionFactoryReference reference = new ConnectionFactoryReference(connectionManager, managedConnectionFactory);

                containerSystem.getJNDIContext().bind("java:openejb/connector/"+conInfo.connectorId, reference);
            }
        }
        /*[6]\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*/
        return containerSystem;
    }
}
