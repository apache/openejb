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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnectionFactory;
import javax.transaction.TransactionManager;

import org.openejb.Container;
import org.openejb.OpenEJBException;
import org.openejb.alt.assembler.classic.AssemblerTool;
import org.openejb.alt.assembler.classic.MethodInfo;
import org.openejb.alt.assembler.modern.global.CMMetaData;
import org.openejb.alt.assembler.modern.global.SecurityMetaData;
import org.openejb.alt.assembler.modern.global.ServerMetaData;
import org.openejb.alt.assembler.modern.global.TransactionMetaData;
import org.openejb.alt.assembler.modern.jar.EntityBeanMetaData;
import org.openejb.alt.assembler.modern.jar.ResourceRefMetaData;
import org.openejb.alt.assembler.modern.jar.SecurityRoleMetaData;
import org.openejb.alt.assembler.modern.jar.SessionBeanMetaData;
import org.openejb.alt.assembler.modern.jar.ejb11.BeanMetaData;
import org.openejb.alt.assembler.modern.jar.ejb11.ContainerTransactionMetaData;
import org.openejb.alt.assembler.modern.jar.ejb11.EnvVariableMetaData;
import org.openejb.alt.assembler.modern.jar.ejb11.MethodMetaData;
import org.openejb.alt.assembler.modern.jar.ejb11.MethodPermissionMetaData;
import org.openejb.alt.assembler.modern.jar.ejb11.RoleRefMetaData;
import org.openejb.alt.assembler.modern.rar.JcaMetaData;
import org.openejb.alt.assembler.modern.rar.jca10.AuthMechanismMetaData;
import org.openejb.alt.assembler.modern.rar.jca10.ConfigPropertyMetaData;
import org.openejb.core.ContainerSystem;
import org.openejb.core.DeploymentInfo;
import org.openejb.core.TransactionManagerWrapper;
import org.openejb.core.entity.EntityContainer;
import org.openejb.core.ivm.naming.IvmContext;
import org.openejb.core.ivm.naming.NameNode;
import org.openejb.core.ivm.naming.ParsedName;
import org.openejb.core.stateful.StatefulContainer;
import org.openejb.core.stateless.StatelessContainer;
import org.openejb.spi.ConnectionManagerFactory;
import org.openejb.spi.SecurityService;
import org.openejb.spi.TransactionService;
import org.openejb.util.MemoryClassLoader;
import org.openejb.util.proxy.ProxyFactory;
import org.openejb.util.proxy.ProxyManager;

/**
 * Verious helper classes for creating and configuring OpenEJB.
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 * @version $Revision 1.0 $
 */
public abstract class AssemblerUtilities extends AssemblerTool {
    private final static String JNDI_BEAN_PREFIX = "java:openejb/ejb/";
    private final static String JNDI_CONNECTOR_PREFIX = "java:openejb/connector/";

    private Set jarContainers = new HashSet();

    /**
     * Generates a ClassLoader for a RAR file.  This is complicated because the
     * RAR file embeds other JARs that need to be available.
     */
    public static ClassLoader getRarClassLoader(File rarFile) throws IOException {
        JarFile jar = new JarFile(rarFile);
    // FIXME: don't use the URLClassLoader as it locks & caches files
    //        which disallows redeploy
        ClassLoader cl = new URLClassLoader(new URL[]{rarFile.toURL()}, Thread.currentThread().getContextClassLoader());
        List embedded = new LinkedList();
        for(Enumeration en = jar.entries(); en.hasMoreElements();) {
            JarEntry entry = (JarEntry)en.nextElement();
            if(entry.getName().toLowerCase().endsWith(".jar")) {
                JarInputStream stream = new JarInputStream(cl.getResourceAsStream(entry.getName()));
                System.out.println("Loading embedded JAR file "+entry.getName());
                embedded.add(stream);
            }
        }
        ClassLoader result = new MemoryClassLoader(cl, (JarInputStream[])embedded.toArray(new JarInputStream[embedded.size()]));
        for(int i=0; i<embedded.size(); i++) {
            ((InputStream)embedded.get(i)).close();
        }
        return result;
    }

    /**
     * Creates and configures the selected ProxyFactory instance.
     */
    protected void createProxyFactory(ServerMetaData data) throws OpenEJBException {
        ProxyFactory factory = (ProxyFactory)toolkit.newInstance(data.getProxyFactoryClass());
        factory.init(data.asProperties(data.getProxyMap()));
        ProxyManager.registerFactory("ivm_server", factory);
        ProxyManager.setDefaultFactory("ivm_server");
    }

    /**
     * Creates containers from the container meta data, and adds them to the
     * ContainerSystem.
     */
    protected void createContainers(ContainerSystem system, ContainerMetaData[] data) throws OpenEJBException {
        Container[] containers = createContainers(data);
        for(int i=0; i<containers.length; i++) {
            system.addContainer(containers[i].getContainerID(), containers[i]);
        }
    }

    /**
     * Creates containers from the container meta data.
     */
    protected Container[] createContainers(ContainerMetaData[] data) throws OpenEJBException {
        List list = new LinkedList();
        for(int i=0; i<data.length; i++) {
            Container container = null;
            switch(data[i].getType()) {
                case ContainerMetaData.STATEFUL_CONTAINER_TYPE:
                    container = new StatefulContainer();
                    container.init(data[i].getName(), new HashMap(), data[i].asProperties(data[i].getMap()));
                    break;
                case ContainerMetaData.ENTITY_CONTAINER_TYPE:
                    container = new EntityContainer();
                    container.init(data[i].getName(), new HashMap(), data[i].asProperties(data[i].getMap()));
                    break;
                case ContainerMetaData.STATELESS_CONTAINER_TYPE:
                    container = new StatelessContainer();
                    container.init(data[i].getName(), new HashMap(), data[i].asProperties(data[i].getMap()));
                    break;
                default:
                    throw new OpenEJBException("Unknown container type in GlobalAssembler.createContainers");
            }
            list.add(container);
        }
        return (Container[])list.toArray(new Container[list.size()]);
    }

    /**
     * Creates a security service from the metadata.
     */
    protected SecurityService createSecurityService(SecurityMetaData data) throws OpenEJBException {
        SecurityService service = (SecurityService)toolkit.newInstance(data.getClassName());
        try {
            applyProperties(service, data.asProperties(data.getMap()));
        } catch(Exception e) {
            throw new OpenEJBException("Unable to apply security service properties", e);
        }
        return service;
    }

    /**
     * Creates a transaction service from the metadata.
     */
    protected TransactionManager createTransactionService(TransactionMetaData data) throws OpenEJBException {
        TransactionService txService = (TransactionService)toolkit.newInstance(data.getClassName());
        try {
            applyProperties(txService, data.asProperties(data.getMap()));
        } catch(Exception e) {
            throw new OpenEJBException("Unable to apply transaction service properties", e);
        }

        // TransactionManagerWrapper must be used to allow proper synchronization by ConnectionManager and persistence manager.
        // See org.openejb.core.TransactionManagerWrapper for details.
        return (TransactionManager)(new TransactionManagerWrapper(txService.getTransactionManager()));
    }

    /**
     * Creates connection managers from the metadata.
     */
    protected Map createConnectionManagers(CMMetaData[] data) throws OpenEJBException {
        Map map = new HashMap();
        for(int i=0; i<data.length; i++) {
            ConnectionManagerFactory mgr = (ConnectionManagerFactory)toolkit.newInstance(data[i].getClassName());
            try {
                applyProperties(mgr, data[i].asProperties(data[i].getMap()));
            } catch(Exception e) {
                throw new OpenEJBException("Unable to apply connection manager properties", e);
            }
            map.put(data[i].getName(), mgr);
        }
        return map;
    }

    /**
     * Deploys beans from the metadata.
     * @param system     The ContainerSystem for all deployments
     * @param containers The available containers deployed at the global level
     *                   in OpenEJB
     * @param beans      The metadata for the beans to deploy.
     * @param roles      The security roles defined in the EJB JAR, including
     *                   the OpenEJB mapping to physical roles.
     * @param loader     The ClassLoader to use to load the bean classes.
     */
    protected synchronized DeploymentInfo[] createDeployments(GlobalContainerSystem system,
                                                              Container[] containers,
                                                              BeanMetaData[] beans,
                                                              org.openejb.alt.assembler.modern.jar.ejb11.SecurityRoleMetaData[] roles,
                                                              ClassLoader loader)
                                   throws OpenEJBException {
        List list = new LinkedList();
        for(int i=0; i<beans.length; i++) {
            byte type;
            String pkClass = null;
            boolean isBMT = false;
            String containerName;
            if(beans[i] instanceof EntityBeanMetaData) {
                EntityBeanMetaData entity = (EntityBeanMetaData)beans[i];
                if(entity.isCMP()) {
                    type = DeploymentInfo.CMP_ENTITY;
                } else {
                    type = DeploymentInfo.BMP_ENTITY;
                }
                pkClass = entity.getPrimaryKeyClassName();
                containerName = entity.getContainerName();
            } else if(beans[i] instanceof SessionBeanMetaData) {
                SessionBeanMetaData session = (SessionBeanMetaData)beans[i];
                if(session.isStatefulSession()) {
                    type = DeploymentInfo.STATEFUL;
                } else {
                    type = DeploymentInfo.STATELESS;
                }
                isBMT = !session.isContainerTransaction();
                containerName = session.getContainerName();
            } else {
                throw new OpenEJBException("Unknown bean type "+beans[i].getClass().getName());
            }
            DeploymentInfo info = new DeploymentInfo(beans[i].getEJBName(), beans[i].getHomeInterfaceName(),
                                                     beans[i].getRemoteInterfaceName(), beans[i].getBeanClassName(),
                                                     pkClass, type, loader);
            info.setBeanManagedTransaction(isBMT);

            IvmContext root = new IvmContext(new NameNode(null, new ParsedName("comp"),null));
            info.setJndiEnc(root);

            createEjbRefJNDIBindings(root, beans[i].getEjbRefs(), type);
            createResourceRefJNDIBindings(root, beans[i].getResourceRefs(), type);
            createEnvVariableJNDIBindings(root, beans[i].getEnvironmentVariables(), type, loader);

            RoleRefMetaData refs[] = beans[i].getSecurityRoleRefs();
            for(int j=0; j<refs.length; j++) {
                info.addSecurityRoleReference(refs[j].getRoleName(), translateToPhysicalRoles(new String[]{refs[j].getRoleLink()}, roles));
            }

            // Identify the container for this bean.
            // If there's already a container with the requested name that
            // was defined at a global level, use it.
            // Otherwise, check whether a container with the requested name is
            // defined in the JAR, and use that.
            boolean containerFound = false;
            Container cont = system.getContainer(containerName);
            if(cont != null && !jarContainers.contains(cont)) {
                cont.deploy(info.getDeploymentID(), info);
                info.setContainer(cont);
                containerFound = true;
            } else {
                for(int j=0; j<containers.length; j++) {
                    if(containers[j].getContainerID().equals(containerName)) {
                        if(cont != null) {
                            throw new OpenEJBException("The container named '"+containerName+"' conflicts with a container deployed in another JAR.  Please use a different container name.");
                        }
                        containers[j].deploy(info.getDeploymentID(), info);
                        info.setContainer(containers[j]);
                        containerFound = true;
                        system.addContainer(containers[j].getContainerID(), containers[j]);
                        jarContainers.add(containers[j]);
                        break;
                    }
                }
            }
            if(!containerFound) {
                if(cont == null)
                    throw new OpenEJBException("Unable to locate container '"+containerName+"' for bean '"+beans[i].getEJBName()+"'");
                else
                    throw new OpenEJBException("Bean '"+beans[i].getEJBName()+"' is trying to use container '"+containerName+"' which was defined in a different JAR.  Either define the container at the global level or use a different name and define the container in the same JAR.");
            }
            system.addDeployment(info);
            System.out.println("Deployed bean "+info.getDeploymentID());

            list.add(info);
        }

        return (DeploymentInfo[])list.toArray(new DeploymentInfo[list.size()]);
    }

    /**
     * Binds EJB references in the private JNDI namespace for a bean.
     */
    protected void createEjbRefJNDIBindings(IvmContext root, org.openejb.alt.assembler.modern.jar.ejb11.EjbRefMetaData[] refs,
                                            byte type)
                       throws OpenEJBException {
        for(int i=0; i<refs.length; i++) {
            if(refs[i].getEjbLink() != null) {
                Object ref = null;
                String jndiName = JNDI_BEAN_PREFIX+refs[i].getEjbLink();
                org.openejb.core.ivm.naming.Reference ref2 = new org.openejb.core.ivm.naming.IntraVmJndiReference( jndiName );
                switch(type) {
                    case DeploymentInfo.BMP_ENTITY:
                case DeploymentInfo.CMP_ENTITY:
                        ref = new org.openejb.core.entity.EncReference( ref2 );
                        break;
                    case DeploymentInfo.STATEFUL:
                        ref = new org.openejb.core.stateful.EncReference( ref2 );
                        break;
                    case DeploymentInfo.STATELESS:
                        ref = new org.openejb.core.stateless.EncReference( ref2 );
                        break;
                    default:
                        throw new OpenEJBException("Illegal bean type for EJB ref assembly.");
                }
                try {
                    root.bind(prefixForBinding(refs[i].getRefName()), ref);
                } catch(Exception e) {
                    throw new OpenEJBException("Unable to bind EJB ref in JNDI: ", e);
                }
            } else {
                throw new OpenEJBException("Binding for remote EJB references not yet implemented!");
            }
        }
    }

    /**
     * Binds resource references in the private JNDI namespace for a bean.
     */
    protected void createResourceRefJNDIBindings(IvmContext root, org.openejb.alt.assembler.modern.jar.ejb11.ResourceRefMetaData[] refs,
                                                 byte type)
                       throws OpenEJBException {
        for(int i=0; i<refs.length; i++) {
            ResourceRefMetaData resource = (ResourceRefMetaData)refs[i];
            String jndiName = JNDI_CONNECTOR_PREFIX+(resource.isAuthContainer() ? "containermanaged/" : "beanManaged/")+resource.getConnectorName();
            org.openejb.core.ivm.naming.Reference ref2 = new org.openejb.core.ivm.naming.IntraVmJndiReference( jndiName );
            Object ref = null;
            switch(type) {
                case DeploymentInfo.BMP_ENTITY:
                case DeploymentInfo.CMP_ENTITY:
                    ref = new org.openejb.core.entity.EncReference( ref2 );
                    break;
                case DeploymentInfo.STATEFUL:
                    ref = new org.openejb.core.stateful.EncReference( ref2 );
                    break;
                case DeploymentInfo.STATELESS:
                    ref = new org.openejb.core.stateless.EncReference( ref2 );
                    break;
                default:
                    throw new OpenEJBException("Illegal bean type for Resource ref assembly.");
            }
            try {
                root.bind(prefixForBinding(refs[i].getRefName()), ref);
            } catch(Exception e) {
                throw new OpenEJBException("Unable to bind Resource ref in JNDI: ", e);
            }
        }
    }

    /**
     * Binds environment variables in the private JNDI namespace for a bean.
     */
    protected void createEnvVariableJNDIBindings(IvmContext root, EnvVariableMetaData[] vars,
                                                 byte type, ClassLoader loader)
                       throws OpenEJBException {
        for(int i=0; i<vars.length; i++) {
            try {
                Class cls = loader.loadClass(vars[i].getType());
                Constructor con = cls.getConstructor(new Class[]{String.class});
                Object o = con.newInstance(new Object[]{vars[i].getValue()});
                root.bind(prefixForBinding(vars[i].getName()), o);
            } catch(Exception e) {
                throw new OpenEJBException("Unable to bind Environment Variable in JNDI", e);
            }
        }
    }

    /**
     * Applies container transaction settings to the beans/containers.
     */
    protected void createContainerTransactions(ContainerTransactionMetaData data[], DeploymentInfo[] info,
                                               ClassLoader loader)
                   throws OpenEJBException {
        for(int i=0; i<data.length; i++) {
            MethodMetaData methods[] = data[i].getMethods();
            for(int j=0; j<methods.length; j++) {
                DeploymentInfo deployment = getDeploymentInfo(info, methods[j].getEJBName());
                createContainerTransaction(getMethodInfo(methods[j], loader), deployment, data[i].getTransactionAttribute());
            }
        }
    }

    /**
     * Applies a container transaction setting to all the relevant methods
     * in the specified bean.
     */
    protected void createContainerTransaction(MethodInfo method, DeploymentInfo info,
                                               String transactionSetting)
                   throws OpenEJBException {
        if(info.getComponentType() == DeploymentInfo.STATEFUL || info.getComponentType() == DeploymentInfo.STATELESS) {
            if(!info.isBeanManagedTransaction()){// if its not Bean Managed transaction type
                Vector methodVect = new Vector();
                Class remote = info.getRemoteInterface();
                if(method.methodIntf==null || !method.methodIntf.equals("Home"))//If remote methods are specified
                    resolveMethods(methodVect,remote,method);

                for(int x = 0; x < methodVect.size(); x++) {
                    Method meth = (Method)methodVect.elementAt(x);
                    // filter out all EJBObject and EJBHome methods that are not remove() methods
                    if(meth.getDeclaringClass()==javax.ejb.EJBObject.class)
                        continue;// its an EJBObject method skip it

                    info.setMethodTransactionAttribute(meth,transactionSetting);
                }
            }
        } else if(info.getComponentType() == DeploymentInfo.BMP_ENTITY || info.getComponentType() == DeploymentInfo.CMP_ENTITY) {
            Method [] methods = resolveMethodInfo(method,info);
            for(int x = 0; x < methods.length; x++){
                Method meth = methods[x];

                // filter out all EJBObject and EJBHome methods that are not remove() methods
                if((meth.getDeclaringClass()==javax.ejb.EJBHome.class || meth.getDeclaringClass()==javax.ejb.EJBObject.class) && !meth.getName().equals("remove"))
                    continue;// its an EJBObject or EJBHome method, and its not remove( ) skip it

                info.setMethodTransactionAttribute(meth,transactionSetting);
            }
        }
    }

    /**
     * Applies method permission settings to the beans/containers.
     */
    protected void createMethodPermissions(MethodPermissionMetaData[] data, DeploymentInfo[] info,
                                           org.openejb.alt.assembler.modern.jar.ejb11.SecurityRoleMetaData[] roles,
                                           ClassLoader loader)
                   throws OpenEJBException {
        for(int i=0; i<data.length; i++) {
            MethodMetaData methods[] = data[i].getMethods();
            for(int j=0; j<methods.length; j++) {
                DeploymentInfo deployment = getDeploymentInfo(info, methods[j].getEJBName());
                createMethodPermission(getMethodInfo(methods[j], loader), deployment,
                                        translateToPhysicalRoles(data[i].getRoles(), roles));
            }
        }
    }

    /**
     * Applies a method permission setting to all the relevant methods
     * in the specified bean.
     */
    protected void createMethodPermission(MethodInfo method, DeploymentInfo info,
                                          String[] physicalRoles)
                   throws OpenEJBException {
        // get the actual methods that match for this deployment (EJBHome, EJBObject, remote and home interface methods)
        Method[] methods = resolveMethodInfo(method, info);
        // add the method permission to the set of permissions held by the deployment  info object
        for(int i = 0; i < methods.length; i++){
            info.appendMethodPermissions(methods[i],physicalRoles);
        }
    }

    /**
     * Given resource adapter metadata, create the appropriate
     * ManagedConnectionFactories.
     */
    protected ManagedConnectionFactory[] createRAFactories(JcaMetaData data, ClassLoader loader) throws OpenEJBException {
        ManagedConnectionFactory[] factories = new ManagedConnectionFactory[data.getDeployments().length];
        AuthMechanismMetaData auth[] = data.getAuthMechanisms();
        boolean nonpw = false, pw = false;
        for(int i=0; i<auth.length; i++) {
            if(auth[i].getAuthMechType().equals("basic-password")) {
                pw = true;
            } else {
                nonpw = true;
            }
        }
        if(nonpw && !pw) {
            throw new OpenEJBException("Can't use kerberos-only resource adapters yet!");
        }

        Class factoryClass;
        try {
            factoryClass = loader.loadClass(data.getManagedConnectionFactoryClass());
        } catch(ClassNotFoundException e) {
            throw new OpenEJBException("Unable to load class '"+data.getManagedConnectionFactoryClass()+"'", e);
        }
        for(int i=0; i<factories.length; i++) {
        // Create the factory instance
            System.out.println("Initializing resource adapter deployment "+data.getDeployments()[i].getName()+"...");
            try {
                factories[i] = (ManagedConnectionFactory)factoryClass.newInstance();
            } catch(Exception e) {
                throw new OpenEJBException("Unable to instantiate class '"+factoryClass.getName()+"'", e);
            }
        // Set the Log Writer
            try {
                factories[i].setLogWriter(new PrintWriter(new OutputStreamWriter(System.out)));
            } catch(ResourceException e) {}
        // Set the properties
            Map values = data.getDeployments()[i].getProperties();
            ConfigPropertyMetaData[] props = data.getConfigProperties();
            for(int j=0; j<props.length; j++) {
                String stringValue = (String)values.get(props[j].getName());
                if(stringValue != null) {
                    try {
                        Class type = loader.loadClass(props[j].getType());
                        Method setter = factoryClass.getMethod("set"+props[j].getName(), new Class[]{type});
                        Object value = type.equals(String.class) ? stringValue : type.getConstructor(new Class[]{String.class}).newInstance(new Object[]{stringValue});
                        setter.invoke(factories[i], new Object[]{value});
                    } catch(Exception e) {
                        throw new OpenEJBException("Unable to set property '"+props[j].getName()+"' for resource adapter deployment "+data.getDeployments()[i].getName(), e);
                    }
                } else System.out.println("Skipping property "+props[j].getName()+" for resource adapter deployment "+data.getDeployments()[i].getName());
            }
        }
        return factories;
    }

    /**
     * Gets a specific DeploymentInfo object from a list, by name.
     */
    private DeploymentInfo getDeploymentInfo(DeploymentInfo[] info, String name) throws OpenEJBException {
        for(int i=0; i<info.length; i++) {
            if(info[i].getDeploymentID().equals(name)) {
                return info[i];
            }
        }
        throw new OpenEJBException("Unable to find a deployed bean named '"+name+"'");
    }

    /**
     * Translates a MethodMetaData object to a MethodInfo object so we can reuse
     * some of the methods in the "classic" assembler.
     */
    private MethodInfo getMethodInfo(MethodMetaData data, ClassLoader loader) throws OpenEJBException {
        MethodInfo info = new MethodInfo();
        info.description = data.getDescription();
        info.ejbDeploymentId = data.getEJBName();
        Boolean b = data.isRemoteInterface();
        info.methodIntf = b == null ? null : b.booleanValue() ? "Remote" : "Home";
        info.methodName = data.getMethodName();
        try {
            /*
              This was commented out because the data type of the 
              MethodInfo.methodParams was changed from Class[] to String[]
              
              info.methodParams = getClasses(data.getArguments(), loader);
            */
            getClasses(data.getArguments(), loader);
        } catch(ClassNotFoundException e) {
            throw new OpenEJBException("Unable to load method parameter class", e);
        }
        return info;
    }

    /**
     * Given a list of class names, loads them, and returns the corresponding
     * list of classes.
     */
    private Class[] getClasses(String[] names, ClassLoader loader) throws ClassNotFoundException {
        if(names == null) {
            return null;
        }
        Class[] list = new Class[names.length];
        for(int i=0; i<list.length; i++) {
            list[i] = loader.loadClass(names[i]);
        }
        return list;
    }

    /**
     * Given a list of logical roles and role mappings, returns a list of
     * physical roles that the logical roles translate to.  This is not
     * necessarily a one-to-one mapping, there may be zero or many physical
     * roles for each logical role.
     */
    private String[] translateToPhysicalRoles(String[] logicalRoles,
                                              org.openejb.alt.assembler.modern.jar.ejb11.SecurityRoleMetaData[] roles) {
        Set translated = new HashSet();
        for(int i=0; i<logicalRoles.length; i++) {
            for(int j=0; j<roles.length; j++) {
                if(logicalRoles[i].equals(roles[j].getRoleName())) {
                    translated.addAll(((SecurityRoleMetaData)roles[j]).getPhysicalRoleNames());
                }
            }
        }
        return (String[])translated.toArray(new String[translated.size()]);
    }
}
