package org.openejb.alt.assembler.dynamic;

import java.util.*;
import java.io.File;
import javax.transaction.TransactionManager;
import javax.naming.InitialContext;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;
import org.openejb.spi.*;
import org.openejb.spi.Assembler;
import org.openejb.OpenEJBException;
import org.openejb.core.ConnectorReference;
import org.openejb.alt.assembler.classic.*;

/**
 * An assembler that is able to manage multiple container systems and handle
 * deployment at runtime.
 *
 * @version $Revision$
 */
public class DynamicAssembler implements Assembler {
    final static String DEFAULT_CONTAINER_SYSTEM = "default";
    private DynamicDeployer deployer;
    private TransactionManager tm;
    private OpenEjbConfiguration config;

    /**
     * Initializes the assembler and the deployer.
     *
     * @see DynamicDeployer#init
     */
    public void init(Properties props) throws OpenEJBException {
        try {
        deployer = new DynamicDeployer();
        deployer.init(props);

        /* Get Configuration ////////////////////////////*/
        DynamicConfigurationFactory configFactory = new DynamicConfigurationFactory();
        configFactory.init(props);
        config = configFactory.getOpenEjbConfiguration();
        File[] deps = configFactory.getDeployments();
        for(int i=0; i<deps.length; i++) {
            deployer.addPendingDeployment(deps[i]);
        }
        deployer.setDefaultContainers(configFactory.getContainers());
        /*\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*/


        /* Add IntraVM JNDI service /////////////////////*/
        String str = System.getProperty(javax.naming.Context.URL_PKG_PREFIXES);
        if(str == null)
            str = "org.openejb.core.ivm.naming";
        else
            str = str + ":org.openejb.core.ivm.naming";
        System.setProperty(javax.naming.Context.URL_PKG_PREFIXES, str);
        /*\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*/
        } catch(Exception e) {e.printStackTrace();}
    }

    public ContainerSystem[] getContainerSystems() {
        return new ContainerSystem[0]; // Nothing is deployed initially
    }

    public String getDefaultContainerSystemID() {
        //todo: add a config file setting somewhere
        if(deployer.getPendingDeploymentCount() > 0) {
            return DEFAULT_CONTAINER_SYSTEM;
        } else {
            return null;
        }
    }

    public TransactionManager getTransactionManager() {
        return tm;
    }

    public Deployer getDeployer() {
        return deployer;
    }

    /**
     * Reads the configuration files and constructs any defined ContainerSystems.
     *
     * @throws OpenEJBException
     */
    public void build() throws OpenEJBException {
        AssemblerTool.applyProxyFactory(config.facilities.intraVmServer);
        deployer.setDefaultRemoteContexts(assembleRemoteJndi(config.facilities.remoteJndiContexts));
        tm = AssemblerTool.assembleTransactionManager(config.facilities.transactionService);
        deployer.setDefaultSecurityService(AssemblerTool.assembleSecurityService(config.facilities.securityService));
        deployer.setDefaultConnectors(assembleConnectors(config.facilities.connectionManagers, config.facilities.connectors));
    }

    private Map assembleRemoteJndi(JndiContextInfo[] contexts) throws OpenEJBException {
        Map map = new HashMap();
        if(contexts != null) {
            for(int i = 0; i < contexts.length; i++) {
                InitialContext cntx = AssemblerTool.assembleRemoteJndiContext(contexts[i]);
                map.put(contexts[i].jndiContextId, cntx);
            }
        }
        return map;
    }

    private Map assembleConnectors(ConnectionManagerInfo[] managers, ConnectorInfo[] connectors) throws OpenEJBException {
        HashMap connectionManagerMap = new HashMap();
        Map map = new HashMap();
        if(managers != null) {
            for(int i = 0; i < managers.length; i++) {
                ConnectionManagerInfo cmInfo = managers[i];
                ConnectionManager connectionManager = AssemblerTool.assembleConnectionManager(cmInfo);
                connectionManagerMap.put(cmInfo.connectionManagerId, connectionManager);
            }
        }
        if(connectors != null) {
            for(int i = 0; i < connectors.length; i++) {
                ConnectorInfo conInfo = connectors[i];

                ConnectionManager connectionManager = (ConnectionManager)connectionManagerMap.get(conInfo.connectionManagerId);
                if(connectionManager == null)
                    throw new OpenEJBException(org.openejb.alt.assembler.classic.Assembler.INVALID_CONNECTION_MANAGER_ERROR + conInfo.connectorId);
                ManagedConnectionFactory managedConnectionFactory = AssemblerTool.assembleManagedConnectionFactory(conInfo.managedConnectionFactory);
                ConnectorReference reference = new ConnectorReference(connectionManager, managedConnectionFactory);
                map.put(conInfo.connectorId, reference);
            }
        }
        return map;
    }
}
