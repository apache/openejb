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
package org.openejb.alt.assembler.modern;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;
import javax.naming.spi.ObjectFactory;
import javax.resource.spi.ManagedConnectionFactory;

import org.openejb.OpenEJBException;
import org.openejb.alt.assembler.modern.jar.JarDeployer;
import org.openejb.alt.assembler.modern.rar.RarDeployer;
import org.openejb.core.ConnectorReference;
import org.openejb.core.ContainerSystem;
import org.openejb.spi.ConnectionManagerConfig;
import org.openejb.spi.ConnectionManagerFactory;
import org.openejb.spi.OpenEJBConnectionManager;

/**
 * Holds some extra properties over the standard ContainerSystem.  This includes
 * the deployed JARs, RARs, connection manager factories, and the list of
 * connectors that the server *wants* to be available (which will only be
 * fulfilled as RARs are actually deployed).
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 * @version $Revision$
 */
public class GlobalContainerSystem extends ContainerSystem {
    private static GlobalContainerSystem instance;
    public static GlobalContainerSystem instance() {
        return instance;
    }

    private Map deployedJars;
    private Map deployedRars;
    private Map connectionMgrs;
    private Map desiredConnectors;
    private JarDeployer jarDeployer;
    private RarDeployer rarDeployer;

    public GlobalContainerSystem() {
        if(instance != null) {
            throw new IllegalStateException("Only one GlobalContainerSystem allowed!");
        } else {
            instance = this;
        }
        deployedJars = new HashMap();
        deployedRars = new HashMap();
        connectionMgrs = new HashMap();
        desiredConnectors = new HashMap();
        try {
            createDeployers();
        } catch(NamingException e) {
            throw new IllegalStateException("Unable to register deployers in JNDI.");
        }
    }

    private void createDeployers() throws NamingException {
        jarDeployer = new JarDeployer(this);
        rarDeployer = new RarDeployer(this);
        getJNDIContext().bind("openejb/assembler/modern/JarDeployer", jarDeployer);
        getJNDIContext().bind("openejb/assembler/modern/RarDeployer", rarDeployer);
    }

    private static class JarReferenceFactory implements Referenceable, ObjectFactory {
        public Reference getReference() throws NamingException {
            return new Reference(getClass().getName(), new StringRefAddr("GCS:", "jar"), getClass().getName(), null);
        }
        public Object getObjectInstance(Object obj, Name name, Context nameCtx,
                                        Hashtable environment) throws Exception {
            System.out.println("Getting object for name: "+name+" for object "+obj);
            return "Here's an object";
        }
    }

    /**
     * Support for hot-deploying EJB JARs.
     */
    public DeployerService getJarDeployer() {
        return jarDeployer;
    }

    /**
     * Support for hot-deploying J2EE Connector RARs.
     */
    public DeployerService getRarDeployer() {
        return rarDeployer;
    }

    public void setConnectionManagers(Map mgrs) {
        connectionMgrs = mgrs;
    }
    public ConnectionManagerFactory getConnectionManager(String id) {
        return (ConnectionManagerFactory)connectionMgrs.get(id);
    }
    public Map getConnectionManagers() {
        return connectionMgrs;
    }

    public Object getDeployedJar(String id) {
        return deployedJars.get(id);
    }
    public void deployJar(String id, Object jarReference) {
        deployedJars.put(id, new JarBeans(jarReference));
    }
    public void undeployJar(String id) {
        deployedJars.remove(id);
    }
    public String[] getDeployedJarNames() {
        Set set = deployedJars.keySet();
        return (String[])set.toArray(new String[set.size()]);
    }
    public boolean isJarDeployed(String name) {
        return deployedJars.containsKey(name);
    }
    public void setJarBeans(String jarName, String[] beanNames) {
        JarBeans beans = (JarBeans)deployedJars.get(jarName);
        if(beans == null) {
            throw new IllegalArgumentException("No such JAR '"+jarName+"' deployed here!");
        }
        beans.beanNames = beanNames;
    }
    public String[] getJarBeans(String jarName) {
        JarBeans beans = (JarBeans)deployedJars.get(jarName);
        if(beans == null) {
            throw new IllegalArgumentException("No such JAR '"+jarName+"' deployed here!");
        }
        return beans.beanNames;
    }

    public Object getDeployedRar(String id) {
        return deployedRars.get(id);
    }
    public void deployRar(String id, Object rarReference) {
        deployedRars.put(id, new RarDeployments(rarReference));
    }
    public void undeployRar(String id) {
        deployedRars.remove(id);
    }
    public String[] getDeployedRarNames() {
        Set set = deployedRars.keySet();
        return (String[])set.toArray(new String[set.size()]);
    }
    public boolean isRarDeployed(String name) {
        return deployedRars.containsKey(name);
    }
    public void setRarDeployments(String rarName, String[] deployments) {
        RarDeployments record = (RarDeployments)deployedRars.get(rarName);
        if(record == null) {
            throw new IllegalArgumentException("No such RAR '"+rarName+"' deployed here!");
        }
        record.deployments = deployments;
    }
    public String[] getRarDeployments(String rarName) {
        RarDeployments record = (RarDeployments)deployedRars.get(rarName);
        if(record == null) {
            throw new IllegalArgumentException("No such RAR '"+rarName+"' deployed here!");
        }
        return record.deployments;
    }

    public void addDesiredConnector(String connectorID, String connectionManagerID, Properties props) {
        desiredConnectors.put(connectorID,
                              new ConnectorValue(connectionManagerID, props));
    }

    public void deployConnector(String connectorID, ManagedConnectionFactory factory) throws OpenEJBException {
        ConnectorValue value = (ConnectorValue)desiredConnectors.get(connectorID);
        if(value == null) {
            throw new OpenEJBException("No matching connector configuration for deployment "+connectorID);
        }
        ConnectionManagerFactory cmf = (ConnectionManagerFactory)connectionMgrs.get(value.connectionManagerID);
        OpenEJBConnectionManager cmsiManager = cmf.createConnectionManager(connectorID, new ConnectionManagerConfig(value.props, true), factory);
        OpenEJBConnectionManager bmsiManager = cmf.createConnectionManager(connectorID, new ConnectionManagerConfig(value.props, false), factory);
        ConnectorReference cmsiReference = new ConnectorReference(cmsiManager, factory);
        ConnectorReference bmsiReference = new ConnectorReference(bmsiManager, factory);
        try {
            String location = "java:openejb/connector/containermanaged/"+connectorID;
            getJNDIContext().bind(location, cmsiReference);
            location = "java:openejb/connector/beanmanaged/"+connectorID;
            getJNDIContext().bind(location, bmsiReference);
        } catch(NamingException e) {
            throw new OpenEJBException("Unable to bind connector in JNDI");
        }
    }

    private static class ConnectorValue {
        String connectionManagerID;
        Properties props;

        public ConnectorValue(String connectionManagerID, Properties props) {
            this.connectionManagerID = connectionManagerID;
            this.props = props;
        }
    }

    private static class JarBeans {
        Object jarReference;
        String[] beanNames;

        public JarBeans(Object jarReference) {
            this.jarReference = jarReference;
        }
    }

    private static class RarDeployments {
        Object rarReference;
        String[] deployments;

        public RarDeployments(Object rarReference) {
            this.rarReference = rarReference;
        }
    }
}
