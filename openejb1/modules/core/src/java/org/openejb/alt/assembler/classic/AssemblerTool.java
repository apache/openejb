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
package org.openejb.alt.assembler.classic;

import org.openejb.Container;
import org.openejb.OpenEJBException;
import org.openejb.core.ContainerSystem;
import org.openejb.core.DeploymentInfo;
import org.openejb.spi.SecurityService;
import org.openejb.spi.TransactionService;
import org.openejb.util.Messages;
import org.openejb.util.SafeToolkit;
import org.openejb.util.proxy.ProxyFactory;
import org.openejb.util.proxy.ProxyManager;

import javax.naming.InitialContext;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

/**
 * This class provides a set of utility methods for constructing various artifacts
 * in the container system from org.openejb.alt.assembler.classic configuration classes.
 * <p/>
 * This class is used as an independent tool or is extended to create specialized
 * assemblers as is the case with the org.openejb.alt.assembler.classic.Assembler which bootstraps
 * the core container system extracting the configuration from a single XML file and
 * building the container system from a complete graph of conf objects.
 * <p/>
 * The methods in this class are not interdependent and other then a SafeToolKit
 * variable they are stateless (the class has no instance variables).
 *
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 * @see org.openejb.alt.assembler.classic.Assembler
 * @see org.openejb.spi.Assembler
 * @see OpenEjbConfigurationFactory
 */
public class AssemblerTool {

    public static final Class PROXY_FACTORY = org.openejb.util.proxy.ProxyFactory.class;
    public static final Class SECURITY_SERVICE = org.openejb.spi.SecurityService.class;
    public static final Class TRANSACTION_SERVICE = org.openejb.spi.TransactionService.class;
    public static final Class CONNECTION_MANAGER = javax.resource.spi.ConnectionManager.class;
    public static final Class CONNECTOR = javax.resource.spi.ManagedConnectionFactory.class;

    /**
     * A mutable static field could be changed by malicious code or by accident from another package. The field could be made final to avoid this vulnerability.
     */
    protected static final Messages messages = new Messages("org.openejb.util.resources");
    protected static final SafeToolkit toolkit = SafeToolkit.getToolkit("AssemblerTool");
    protected static final HashMap codebases = new HashMap();

    protected Properties props;

    static {
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        codebases.put("CLASSPATH", cl);

        System.setProperty("noBanner", "true");
    }

    /**
     * When given a complete ContainerSystemInfo object, this method,
     * will construct all the containers (entity, stateful, stateless)
     * and add those containers to the ContainerSystem.  The containers
     * are constructed using the assembleContainer() method. Once constructed
     * the container and its deployments are added to the container system.
     *
     * @param containerSystem     the system to which the container should be added.
     * @param containerSystemInfo defines the contain system,its containers, and deployments.
     * @throws Exception if there was a problem constructing the ContainerManager.
     * @throws Exception
     * @see org.openejb.core.ContainerSystem
     * @see ContainerManagerInfo
     */
    public void assembleContainers(ContainerSystem containerSystem, ContainerSystemInfo containerSystemInfo) throws Exception {

        ContainerBuilder containerBuilder = new ContainerBuilder(containerSystemInfo, this.props);
        List containers = (List) containerBuilder.build();
        for (int i = 0; i < containers.size(); i++) {
            Container container = (Container) containers.get(i);
            containerSystem.addContainer(container.getContainerID(), container);
            org.openejb.DeploymentInfo[] deployments = container.deployments();
            for (int j = 0; j < deployments.length; j++) {
                containerSystem.addDeployment((org.openejb.core.DeploymentInfo) deployments[j]);
            }
        }
    }

    /*
    TODO: The Exception Handling here isn't up-to-date and doesn't
    use a message number. Message numbers allow the message text to
    be internationalized.
    */
    public InitialContext assembleRemoteJndiContext(JndiContextInfo context)
            throws org.openejb.OpenEJBException {
        try {
            InitialContext ic = new InitialContext(context.properties);
            return ic;
        } catch (javax.naming.NamingException ne) {
            //FIXME Log this
            // not a super serious error but assemble should be stoped.
            throw new org.openejb.OpenEJBException("The remote JNDI EJB references for remote-jndi-contexts = " + context.jndiContextId + "+ could not be resolved.", ne);
        }
    }

    /**
     * This class will assemble a ConnectionManager instace from a ConnectionManagerInfo
     * configuration object.
     *
     * @param cmInfo describes the ConnectionManager to be assembled.
     * @return the ConnectionManager instance assembled.
     * @see org.openejb.alt.assembler.classic.ConnectionManagerInfo
     */
    public ConnectionManager assembleConnectionManager(ConnectionManagerInfo cmInfo)
            throws OpenEJBException, java.lang.Exception {
        /*TODO: Add better exception handling, this method throws java.lang.Exception,
         which is not very specific. Only a very specific OpenEJBException should be
         thrown.
         */
        Class managerClass = SafeToolkit.loadClass(cmInfo.className, cmInfo.codebase);

        checkImplementation(CONNECTION_MANAGER, managerClass, "ConnectionManager", cmInfo.connectionManagerId);

        ConnectionManager connectionManager = (ConnectionManager) toolkit.newInstance(managerClass);

        // a container manager has either properties or configuration information or nothing at all
        if (cmInfo.properties != null) {
            Properties clonedProps = (Properties) (this.props.clone());
            clonedProps.putAll(cmInfo.properties);
            applyProperties(connectionManager, clonedProps);
        }

        return connectionManager;
    }

    /**
     * This method will assemble a ManagedConnectionFactory instance from a
     * ManagedConnecitonFactoryInfo configuration object.
     *
     * @param mngedConFactInfo describes the the ManagedConnectionFactory to be created.
     * @return the ManagedConnecitonFactory assembled.
     * @see org.openejb.alt.assembler.classic.ManagedConnectionFactoryInfo
     */
    public ManagedConnectionFactory assembleManagedConnectionFactory(ManagedConnectionFactoryInfo mngedConFactInfo)
            throws org.openejb.OpenEJBException, java.lang.Exception {

        ManagedConnectionFactory managedConnectionFactory = null;
        try {
            Class factoryClass = SafeToolkit.loadClass(mngedConFactInfo.className, mngedConFactInfo.codebase);
            checkImplementation(CONNECTOR, factoryClass, "Connector", mngedConFactInfo.id);

            managedConnectionFactory = (ManagedConnectionFactory) toolkit.newInstance(factoryClass);
        } catch (Exception e) {
            throw new OpenEJBException("Could not instantiate Connector '" + mngedConFactInfo.id + "'.", e);
        }


        try {
            // a ManagedConnectionFactory has either properties or configuration information or nothing at all
            if (mngedConFactInfo.properties != null) {
                Properties clonedProps = (Properties) (this.props.clone());
                clonedProps.putAll(mngedConFactInfo.properties);
                applyProperties(managedConnectionFactory, clonedProps);
            }
        } catch (java.lang.reflect.InvocationTargetException ite) {
            throw new OpenEJBException("Could not initialize Connector '" + mngedConFactInfo.id + "'.", ite.getTargetException());
        } catch (Exception e) {
            //e.printStackTrace();
            throw new OpenEJBException("Could not initialize Connector '" + mngedConFactInfo.id + "'.", e);
        }

        return managedConnectionFactory;
    }

    /**
     * This method assembles the SecurityService from the SecuirtyServiceInfo
     * configuration object.
     *
     * @param securityInfo describes the SecurityService to be assembled.
     * @return the SecurityService object that was assembled.
     * @see org.openejb.alt.assembler.classic.SecurityServiceInfo
     */
    public SecurityService assembleSecurityService(SecurityServiceInfo securityInfo)
            throws org.openejb.OpenEJBException, java.lang.Exception {
        /*TODO: Add better exception handling, this method throws java.lang.Exception,
         which is not very specific. Only a very specific OpenEJBException should be
         thrown.
         */
        Class serviceClass = SafeToolkit.loadClass(securityInfo.factoryClassName, securityInfo.codebase);

        checkImplementation(SECURITY_SERVICE, serviceClass, "SecurityService", securityInfo.serviceName);

        SecurityService securityService = (SecurityService) toolkit.newInstance(serviceClass);

        // a SecurityService has either properties or configuration information or nothing at all
        if (securityInfo.properties != null)
            applyProperties(securityService, securityInfo.properties);

        return securityService;
    }

    /**
     * This method assembles the TransactionManager from the TransactionServiceInfo
     * configuration object.
     *
     * @param txInfo describes the TransactionService to be assembled. The Transaction
     *               manager is obtained from this service.
     * @return the TranactionManager instance that was obtained from the assembled TransactionService
     * @see org.openejb.alt.assembler.classic.TransactionServiceInfo
     */
    public javax.transaction.TransactionManager assembleTransactionManager(TransactionServiceInfo txInfo)
            throws org.openejb.OpenEJBException, java.lang.Exception {
        /*TODO: Add better exception handling, this method throw java.lang.Exception,
         which is not very specific.  If something is wrong, we should at least say
         "Cannot initialize the TransactionManager, because X happened."
         */

        Class serviceClass = SafeToolkit.loadClass(txInfo.factoryClassName, txInfo.codebase);

        checkImplementation(TRANSACTION_SERVICE, serviceClass, "TransactionService", txInfo.serviceName);

        TransactionService txService = (TransactionService) toolkit.newInstance(serviceClass);

        // a TransactionService has either properties or configuration information or nothing at all
        if (txInfo.properties != null)
            applyProperties(txService, txInfo.properties);


        // TransactionManagerWrapper must be used to allow proper synchronization by ConnectionManager and persistence manager.
        // See org.openejb.core.TransactionManagerWrapper for details.
        return (javax.transaction.TransactionManager) (new org.openejb.core.TransactionManagerWrapper(txService.getTransactionManager()));
    }

    /**
     * This method constructs a ProxyFactory from teh IntraVmServerInfo conf class and automatically
     * registers that ProxyFactory with the ProxyManager as the default proxy.
     * Because of interedependices that require a proxy to be in place (specifically the creation of
     * the OpenEJB JNDI global name space in the org.openejb.core.ContainerSystem class, this method
     * should be processed before anything else is done in the deployment process.
     *
     * @param ivmInfo the IntraVmServerInfo configuration object that describes the ProxyFactory
     * @see org.openejb.alt.assembler.classic.IntraVmServerInfo
     */
    public void applyProxyFactory(IntraVmServerInfo ivmInfo) throws OpenEJBException {
        Class factoryClass = SafeToolkit.loadClass(ivmInfo.proxyFactoryClassName, ivmInfo.codebase);

        checkImplementation(PROXY_FACTORY, factoryClass, "ProxyFactory", ivmInfo.factoryName);

        ProxyFactory factory = (ProxyFactory) toolkit.newInstance(factoryClass);

        factory.init(ivmInfo.properties);
        ProxyManager.registerFactory("ivm_server", factory);
        ProxyManager.setDefaultFactory("ivm_server");

    }

    /**
     * This method will automatically attempt to invoke an init(Properties )
     * method on the target object, passing in the properties and an argument.
     *
     * @param target the object that will have its init(Properties) method invoked
     * @param props
     * @throws java.lang.reflect.InvocationTargetException
     *
     * @throws java.lang.IllegalAccessException
     *
     * @throws java.lang.NoSuchMethodException
     *
     */
    public void applyProperties(Object target, Properties props)
            throws java.lang.reflect.InvocationTargetException,
            java.lang.IllegalAccessException, java.lang.NoSuchMethodException {
        if (props != null /*&& props.size()>0*/) {
            Method method = target.getClass().getMethod("init", new Class[]{Properties.class});
            method.invoke(target, new Object[]{props});
        }
    }

    /**
     * This method applies the transaction attributed described by the collection of MethodTransactionInfo
     * object to the org.openejb.core.DeploymentInfo objects.  This method maps every method of the bean's
     * remote and home interfaces (including remove() methods) to their assigned transaction attributes and
     * then applies these mappings to the bean through the DeploymentInfo.setMethodTransationAttribute().
     * At run time the container will get the transaction attribute associated with a client call from the
     * DeploymentInfo object by invoking its getTransactionAttribute(Method) method.
     * See page 251 EJB 1.1 for an explanation of the method attribute.
     *
     * @param deploymentInfo the deployment to which the transaction attributes are applied
     * @param mtis           describes the transaction attributes for the enterprise bean(s)
     * @see org.openejb.alt.assembler.classic.MethodTransactionInfo
     */
    public void applyTransactionAttributes(DeploymentInfo deploymentInfo, MethodTransactionInfo[] mtis) {
        /*TODO: Add better exception handling.  This method doesn't throws any exceptions!!
         there is a lot of complex code here, I'm sure something could go wrong the user
         might want to know about.
         */
        for (int i = 0; i < mtis.length; i++) {
            MethodTransactionInfo transInfo = mtis[i];
            MethodInfo[] mis = transInfo.methods;

            for (int z = 0; z < mis.length; z++) {
                MethodInfo methodInfo = mis[z];
                // IF no deployment was specified OR this deployment was specified
                if (mis[z].ejbDeploymentId == null || mis[z].ejbDeploymentId.equals(deploymentInfo.getDeploymentID())) {
                    if (!deploymentInfo.isBeanManagedTransaction()) {
                        // if its not Bean Managed transaction type
                        Vector methodVect = new Vector();

                        if (methodInfo.methodIntf == null) {
                            // => attribute applies to both home and remote interface methods
                            resolveMethods(methodVect, deploymentInfo.getRemoteInterface(), methodInfo);
                            resolveMethods(methodVect, deploymentInfo.getHomeInterface(), methodInfo);
                        } else if (methodInfo.methodIntf.equals("Home")) {
                            resolveMethods(methodVect, deploymentInfo.getHomeInterface(), methodInfo);
                        } else if (methodInfo.methodIntf.equals("Remote")) {
                            resolveMethods(methodVect, deploymentInfo.getRemoteInterface(), methodInfo);
                        } else {
                            // wrong string constant
                        }

                        for (int x = 0; x < methodVect.size(); x++) {
                            Method method = (Method) methodVect.elementAt(x);

                            // filter out all EJBObject and EJBHome methods that are not remove() methods
                            if ((method.getDeclaringClass() == javax.ejb.EJBObject.class ||
                                    method.getDeclaringClass() == javax.ejb.EJBHome.class) &&
                                    method.getName().equals("remove") == false) {
                                continue;   //skip it
                            }
                            deploymentInfo.setMethodTransactionAttribute(method, transInfo.transAttribute);
                        }
                    }
                }
            }
        }

    }

    /**
     * Maps the security role references used by enterprise beans to their associated physical
     * in the target environment.  Each security role reference is mapped to a logical role. The
     * logical roles are themselves mapped to their respective physical role equivalents in the
     * AssemblerTool.RoleMapping object.
     *
     * @param deployment  the DeploymentInfo object to which the mapping should be applied.
     * @param beanInfo    the EnterpiseBeanInfo object which contains the securityRoleReferences
     * @param roleMapping the RoleMapping object which contains the logical to physical security roles.
     * @see org.openejb.alt.assembler.classic.EnterpriseBeanInfo
     * @see org.openejb.alt.assembler.classic.AssemblerTool.RoleMapping
     * @see org.openejb.core.DeploymentInfo#addSecurityRoleReference(String, String[])
     */
    public void applySecurityRoleReference(DeploymentInfo deployment, EnterpriseBeanInfo beanInfo, AssemblerTool.RoleMapping roleMapping) {
        if (beanInfo.securityRoleReferences != null) {
            for (int l = 0; l < beanInfo.securityRoleReferences.length; l++) {
                SecurityRoleReferenceInfo roleRef = beanInfo.securityRoleReferences[l];
                String[] physicalRoles = roleMapping.getPhysicalRoles(roleRef.roleLink);
                deployment.addSecurityRoleReference(roleRef.roleName, physicalRoles);
            }
        }
    }

    /**
     * This method applies a set of method permissions to a deploymentInfo object, so that the container
     * can verify that a specific physical security role has access to a specific method.
     * The method itself maps each of the physical security roles to a method and add this binding
     * to the org.openejb.core.DeploymentInfo object by invoking its DeploymentInfo.appendMethodPermission()
     * method.  The roleNames of the MethodPermissionInfo object are assumed to be the physical names,
     * not the logical names. If this is not the case then the MethodPermissionInfo object should be preprocessed
     * by the applyRoleMapping( ) method, or the overloaded version of this method which takes a RoleMapping
     * object should be used (both these strategies will map logical to physical roles).
     *
     * @param deployment  the DeploymentInfo object to which the Method Permissions should be applied.
     * @param permissions the Method Permission to be applied to the deployment.
     * @see org.openejb.alt.assembler.classic.MethodPermissionInfo
     * @see org.openejb.core.DeploymentInfo#appendMethodPermissions(java.lang.reflect.Method, String[])
     */
    public void applyMethodPermissions(DeploymentInfo deployment, MethodPermissionInfo[] permissions) {
        /*TODO: Add better exception handling.  This method doesn't throws any exceptions!!
         there is a lot of complex code here, I'm sure something could go wrong the user
         might want to know about.
         At the very least, log a warning or two.
         */
        for (int a = 0; a < permissions.length; a++) {
            MethodPermissionInfo methodPermission = permissions[a];
            for (int b = 0; b < methodPermission.methods.length; b++) {
                MethodInfo methodInfo = methodPermission.methods[b];

                // IF no deployment id was specified OR this deployment's id is specified.
                if (methodInfo.ejbDeploymentId == null || methodInfo.ejbDeploymentId.equals(deployment.getDeploymentID())) {
                    // get the actual methods that match for this deployment (EJBHome, EJBObject, remote and home interface methods)
                    java.lang.reflect.Method[] methods = resolveMethodInfo(methodInfo, deployment);
                    // add the method permission to the set of permissions held by the deployment  info object
                    for (int c = 0; c < methods.length; c++) {
                        deployment.appendMethodPermissions(methods[c], methodPermission.roleNames);
                    }
                }

            }
        }
    }

    /**
     * This method applies a set of method permissions and RoleMapping to a deploymentInfo object, so that the container
     * can verify that a specific physical security role has access to a specific method.
     * The method itself maps each of the physical security roles to a method and adds this binding
     * to the org.openejb.core.DeploymentInfo object by invoking its DeploymentInfo.appendMethodPermission()
     * method.  The roleNames of the MethodPermissionInfo object are assumed to be the logical names that corrspond
     * to logical mappings in the RoleMappig object. If the MethodPermissionInfo object's roleMappings are actually
     * physical role names then the overloaded version of this method which doesn't require a RoleMapping parameter should
     * be used.
     *
     * @param deployment  the DeploymentInfo object to which the Method Permissions should be applied.
     * @param permissions the Method Permission to be applied to the deployment.
     * @param roleMapping the encapsulation of logical roles and their corresponding physical role mappings.
     * @see org.openejb.alt.assembler.classic.MethodPermissionInfo
     * @see org.openejb.alt.assembler.classic.AssemblerTool.RoleMapping
     * @see org.openejb.core.DeploymentInfo#appendMethodPermissions(java.lang.reflect.Method, String[])
     */
    public void applyMethodPermissions(DeploymentInfo deployment, MethodPermissionInfo[] permissions, AssemblerTool.RoleMapping roleMapping) {
        /*TODO: Add better exception handling.  This method doesn't throws any exceptions!!
         there is a lot of complex code here, I'm sure something could go wrong the user
         might want to know about.
         At the very least, log a warning or two.
         */
        for (int i = 0; i < permissions.length; i++) {
            permissions[i] = applyRoleMappings(permissions[i], roleMapping);
        }
        applyMethodPermissions(deployment, permissions);
    }

    /*
    * Makes a copy of the MethodPermissionObject and then replaces the logical roles of the MethodPermissionInfo copy
    * with the physical roles in the roleMapping object.
    * If the RoleMapping object doesn't have a set of physical roles for a particular logical role in the
    * MethodPermissionInfo, then the logical role is used.
    *
    * @param methodPermission the permission object to be copies and updated.
    * @param roleMapping encapsulates the mapping of many logical roles to their equivalent physical roles.
    * @see org.openejb.alt.assembler.classic.MethodPermissionInfo
    * @see org.openejb.alt.assembler.classic.AssemblerTool.RoleMapping
    */
    public MethodPermissionInfo applyRoleMappings(MethodPermissionInfo methodPermission,
                                                  AssemblerTool.RoleMapping roleMapping) {
        /*TODO: Add better exception handling.  This method doesn't throws any exceptions!!
         there is a lot of complex code here, I'm sure something could go wrong the user
         might want to know about.
         At the very least, log a warning or two.
         */

        HashSet physicalRoles = new HashSet();

        for (int z = 0; z < methodPermission.roleNames.length; z++) {
            String[] physicals = roleMapping.getPhysicalRoles(methodPermission.roleNames[z]);
            if (physicals != null) {
                for (int x = 0; x < physicals.length; x++) {
                    physicalRoles.add(physicals[x]);
                }
            } else {// if no physical roles are mapped use logical role

                physicalRoles.add(methodPermission.roleNames[z]);
            }
        }
        methodPermission.roleNames = new String[physicalRoles.size()];
        physicalRoles.toArray(methodPermission.roleNames);
        return methodPermission;
    }

    /**
     * This class encapsulates a mapping between a collection of
     * logical roles and each of those roles equivalent physical security roles
     * in the target environment.
     * <p/>
     * Instance of this class are constructed from a RoleMappingInfo configuration
     * class.  This class is used in the applySecurityRoleReferences( ) and
     * applyMethodPermissions( ) Assembler methods.
     */
    public static class RoleMapping {
        private HashMap map = new HashMap();

        /**
         * Constructs an instance from a RoleMappingInfo configuration object.
         *
         * @param roleMappingInfos configuration object holds collections of logical and physical roles
         * @see org.openejb.alt.assembler.classic.RoleMappingInfo
         */
        public RoleMapping(RoleMappingInfo[] roleMappingInfos) {
            for (int i = 0; i < roleMappingInfos.length; i++) {
                RoleMappingInfo mapping = roleMappingInfos[i];
                for (int z = 0; z < mapping.logicalRoleNames.length; z++) {
                    map.put(mapping.logicalRoleNames[z], mapping.physicalRoleNames);
                }
            }
        }

        /**
         * Returns all the logical roles in this mapping. The logical roles
         * act as keys to collections of equivalent physical roles
         *
         * @return a collection of logical roles
         */
        public String[] logicalRoles() {
            return (String[]) map.keySet().toArray();
        }

        /**
         * Returns a collection of physical roles that are mapped to the
         * logical role.
         *
         * @param logicalRole a logical role that is mapped to physical roles
         * @return a collection of physical roles; null if no roles are mapped.
         */
        public String[] getPhysicalRoles(String logicalRole) {
            String[] roles = (String[]) map.get(logicalRole);
            return roles != null ? (String[]) roles.clone() : null;
        }

    }


    ////////////////////////////////////////////////////////////////
    /////
    /////       Protected Helper methods. Not part of public static interface
    /////
    ///////////////////////////////////////////////////////////////






    /**
     * Returns all the Method objects specified by a MethodInfo object for a specific bean deployment.
     *
     * @see org.openejb.core.DeploymentInfo
     * @see MethodInfo
     */
    protected java.lang.reflect.Method[] resolveMethodInfo(MethodInfo methodInfo, org.openejb.core.DeploymentInfo di) {
        /*TODO: Add better exception handling.  This method doesn't throws any exceptions!!
         there is a lot of complex code here, I'm sure something could go wrong the user
         might want to know about.
         At the very least, log a warning or two.
         */

        Vector methodVect = new Vector();

        Class remote = di.getRemoteInterface();
        Class home = di.getHomeInterface();
        if (methodInfo.methodIntf == null) {
            resolveMethods(methodVect, remote, methodInfo);
            resolveMethods(methodVect, home, methodInfo);
        } else if (methodInfo.methodIntf.equals("Remote")) {
            resolveMethods(methodVect, remote, methodInfo);
        } else {
            resolveMethods(methodVect, home, methodInfo);
        }
        return (java.lang.reflect.Method[]) methodVect.toArray(new java.lang.reflect.Method[methodVect.size()]);
    }

    /**
     * @throws SecurityException
     * @see org.openejb.core.DeploymentInfo
     * @see MethodInfo
     */
    protected static void resolveMethods(Vector methods, Class intrface, MethodInfo mi)
            throws SecurityException {
        /*TODO: Add better exception handling. There is a lot of complex code here, I'm sure something could go wrong the user
         might want to know about.
         At the very least, log a warning or two.
         */

        if (mi.methodName.equals("*")) {
            Method[] mthds = intrface.getMethods();
            for (int i = 0; i < mthds.length; i++)
                methods.add(mthds[i]);
        } else if (mi.methodParams != null) {// there are paramters specified
            try {
                Class[] params = new Class[mi.methodParams.length];
                ClassLoader cl = intrface.getClassLoader();
                for (int i = 0; i < params.length; i++) {
                    try {
                        params[i] = getClassForParam(mi.methodParams[i], cl);
                    } catch (ClassNotFoundException cnfe) {
                        // logSomething here.
                    }
                }
                Method m = intrface.getMethod(mi.methodName, params);
                methods.add(m);
            } catch (NoSuchMethodException nsme) {
                /*
                Do nothing.  Exceptions are not only possible they are expected to be a part of normall processing.
                Normally exception handling should not be a part of business logic, but server start up doesn't need to be
                as peformant as server runtime, so its allowed.
                */
            }
        } else {// no paramters specified so may be several methods
            Method[] ms = intrface.getMethods();
            for (int i = 0; i < ms.length; i++) {
                if (ms[i].getName().equals(mi.methodName))
                    methods.add(ms[i]);
            }
        }

    }

    protected void checkImplementation(Class intrfce, Class factory, String serviceType, String serviceName) throws OpenEJBException {
        if (!intrfce.isAssignableFrom(factory)) {
            handleException("init.0100", serviceType, serviceName, factory.getName(), intrfce.getName());
        }
    }

    /**
     * Return the correct Class object. Either use forName or
     * return a primitive TYPE Class.
     */
    private static java.lang.Class getClassForParam(java.lang.String className, ClassLoader cl) throws ClassNotFoundException {
        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }

        // Test if the name is a primitive type name
        if (className.equals("int")) {
            return java.lang.Integer.TYPE;
        } else if (className.equals("double")) {
            return java.lang.Double.TYPE;
        } else if (className.equals("long")) {
            return java.lang.Long.TYPE;
        } else if (className.equals("boolean")) {
            return java.lang.Boolean.TYPE;
        } else if (className.equals("float")) {
            return java.lang.Float.TYPE;
        } else if (className.equals("char")) {
            return java.lang.Character.TYPE;
        } else if (className.equals("short")) {
            return java.lang.Short.TYPE;
        } else if (className.equals("byte")) {
            return java.lang.Byte.TYPE;
        } else
            return cl.loadClass(className);

    }


    /*------------------------------------------------------*/
    /*    Methods for easy exception handling               */
    /*------------------------------------------------------*/
    public void handleException(String errorCode, Object arg0, Object arg1, Object arg2, Object arg3) throws OpenEJBException {
        throw new OpenEJBException(messages.format(errorCode, arg0, arg1, arg2, arg3));
    }

    public void handleException(String errorCode, Object arg0, Object arg1, Object arg2) throws OpenEJBException {
        throw new OpenEJBException(messages.format(errorCode, arg0, arg1, arg2));
    }

    public void handleException(String errorCode, Object arg0, Object arg1) throws OpenEJBException {
        throw new OpenEJBException(messages.format(errorCode, arg0, arg1));
    }

    public void handleException(String errorCode, Object arg0) throws OpenEJBException {
        throw new OpenEJBException(messages.format(errorCode, arg0));
    }

    public void handleException(String errorCode) throws OpenEJBException {
        throw new OpenEJBException(messages.format(errorCode));
    }


    /*------------------------------------------------------*/
    /*  Methods for logging exceptions that are noteworthy  */
    /*  but not bad enough to stop the container system.    */
    /*------------------------------------------------------*/
    public void logWarning(String errorCode, Object arg0, Object arg1, Object arg2, Object arg3) {
        System.out.println("Warning: " + messages.format(errorCode, arg0, arg1, arg2, arg3));
    }

    public void logWarning(String errorCode, Object arg0, Object arg1, Object arg2) {
        System.out.println("Warning: " + messages.format(errorCode, arg0, arg1, arg2));
    }

    public void logWarning(String errorCode, Object arg0, Object arg1) {
        System.out.println("Warning: " + messages.format(errorCode, arg0, arg1));
    }

    public void logWarning(String errorCode, Object arg0) {
        System.out.println("Warning: " + messages.format(errorCode, arg0));
    }

    public void logWarning(String errorCode) {
        System.out.println("Warning: " + messages.format(errorCode));
    }
}