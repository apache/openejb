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
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact info@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.assembler;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Properties;
import javax.ejb.EJBHome;
import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.transaction.UserTransaction;

import org.apache.geronimo.naming.java.ReadOnlyContext;

import org.openejb.ContainerIndex;
import org.openejb.EJBComponentType;
import org.openejb.EJBContainer;
import org.openejb.GenericEJBContainer;
import org.openejb.OpenEJB;
import org.openejb.OpenEJBException;
import org.openejb.entity.bmp.BMPContainerBuilder;
import org.openejb.sfsb.StatefulContainerBuilder;
import org.openejb.slsb.StatelessContainerBuilder;

import org.apache.geronimo.transaction.UserTransactionImpl;

public class ContainerBuilder implements RpcContainer {

    private Object containerId = null;
    private HashMap deployments = new HashMap();

    public void init(Object containerId, HashMap deploymentsMap, Properties properties)
            throws OpenEJBException {

        setupJndi();


        this.containerId = containerId;

        Object[] deploys = deploymentsMap.values().toArray();

        for (int i = 0; i < deploys.length; i++) {
            CoreDeploymentInfo info = (CoreDeploymentInfo) deploys[i];
            deploy(info.getDeploymentID(), info);
        }
    }

    private void setupJndi() {
        /* Add Geronimo JNDI service ///////////////////// */
        String str = System.getProperty(javax.naming.Context.URL_PKG_PREFIXES);
        if (str == null)
            str = ":org.apache.geronimo.naming";
        else
            str = str + ":org.apache.geronimo.naming";
        System.setProperty(javax.naming.Context.URL_PKG_PREFIXES, str);
    }

    public Object invoke(
            Object deployID,
            Method callMethod,
            Object[] args,
            Object primKey,
            Object securityIdentity)
            throws OpenEJBException {

        DeploymentInfoWrapper dep =
                (DeploymentInfoWrapper) deployments.get(deployID);
        Object obj = dep.invoke(callMethod, args, primKey);
        //System.out.println("[obj]"+obj);
        return obj;
    }

    public int getContainerType() {
        return EJBComponentType.STATELESS;
    }

    public org.openejb.assembler.DeploymentInfo getDeploymentInfo(Object deploymentID) {
        return (DeploymentInfoWrapper) deployments.get(deploymentID);
    }

    public org.openejb.assembler.DeploymentInfo[] deployments() {
        return (CoreDeploymentInfo[]) deployments.values().toArray(new DeploymentInfoWrapper[0]);

    }

    public void deploy(Object deploymentID, org.openejb.assembler.DeploymentInfo info)
            throws OpenEJBException {
        ((org.openejb.assembler.CoreDeploymentInfo) info).setContainer(this);
        deployments.put(info.getDeploymentID(), new DeploymentInfoWrapper(info));
    }

    public Object getContainerID() {
        return containerId;
    }

    static class DeploymentInfoWrapper extends CoreDeploymentInfo {
        CoreDeploymentInfo deploymentInfo;
        GenericEJBContainer container;

        public DeploymentInfoWrapper(org.openejb.assembler.DeploymentInfo deploymentInfo) {
            this((CoreDeploymentInfo) deploymentInfo);
        }

        public DeploymentInfoWrapper(CoreDeploymentInfo di) {
            this.deploymentInfo = di;


            try {
                switch (di.getComponentType()) {
                case EJBComponentType.BMP_ENTITY:
                    container = createBMPEntityEJBContainerWapper(deploymentInfo);
                    break;
                case EJBComponentType.CMP_ENTITY:
                    container = createBMPEntityEJBContainerWapper(deploymentInfo);
                    break; //TODO: only a hack.  Course, this hole file is a hack.
                case EJBComponentType.STATEFUL:
                    container = createStatefulEJBContainerWapper(deploymentInfo);
                    break;
                case EJBComponentType.STATELESS:
                    container = createStatelessEJBContainerWapper(deploymentInfo);
                    break;
                }
                ContainerIndex index = ContainerIndex.getInstance();

                if (container != null) {
                    index.addContainer(container);
                }

            } catch (Exception e) {
                // TODO Auto-generated catch block
                System.out.println("FAILED " + e.getMessage());
                throw new RuntimeException("Cannot create container.", e);
            }

        }

        public Object invoke(Method callMethod, Object[] args, Object primKey) throws OpenEJBException {
            try {
                return getEjbContainer().invoke(callMethod, args, primKey);
            } catch (OpenEJBException e) {
                throw e;
            } catch (Throwable e) {
                throw new OpenEJBException(e);
            }
        }

        /**
         * @return Returns the deploymentInfo.
         */
        public CoreDeploymentInfo getDeploymentInfo() {
            return deploymentInfo;
        }

        /**
         * @return Returns the container.
         */
        public EJBContainer getEjbContainer() {
            return container;
        }

//============== Delegated methods ===========================/

        public void addSecurityRoleReference(String securityRoleReference, String[] physicalRoles) {
        }

        public void appendMethodPermissions(Method m, String[] roleNames) {
        }

        public boolean equals(Object obj) {
            return deploymentInfo.equals(obj);
        }

        public Class getBeanClass() {
            return deploymentInfo.getBeanClass();
        }

        public int getComponentType() {
            return deploymentInfo.getComponentType();
        }

        public Container getContainer() {
            return deploymentInfo.getContainer();
        }

        public Object getDeploymentID() {
            return deploymentInfo.getDeploymentID();
        }

        public EJBHome getEJBHome() {
            return container.getEJBHome();
        }

        public Class getHomeInterface() {
            return deploymentInfo.getHomeInterface();
        }

        public Context getJndiEnc() {
            return deploymentInfo.getJndiEnc();
        }

        public Class getPrimaryKeyClass() {
            return deploymentInfo.getPrimaryKeyClass();
        }

        public Class getRemoteInterface() {
            return deploymentInfo.getRemoteInterface();
        }

        public byte getTransactionAttribute(Method method) {
            return deploymentInfo.getTransactionAttribute(method);
        }

        public int hashCode() {
            return deploymentInfo.hashCode();
        }

        public boolean isBeanManagedTransaction() {
            return deploymentInfo.isBeanManagedTransaction();
        }

        public boolean isReentrant() {
            return deploymentInfo.isReentrant();
        }

        public void setBeanManagedTransaction(boolean value) {
            deploymentInfo.setBeanManagedTransaction(value);
        }

        public void setContainer(Container cont) {
            deploymentInfo.setContainer(cont);
        }

        public void setIsReentrant(boolean reentrant) {
            deploymentInfo.setIsReentrant(reentrant);
        }

        public void setJndiEnc(Context cntx) {
            deploymentInfo.setJndiEnc(cntx);
        }

        public void setMethodTransactionAttribute(Method method, String transAttribute) {
            deploymentInfo.setMethodTransactionAttribute(method, transAttribute);
        }

        public String toString() {
            return deploymentInfo.toString();
        }

    }

    private static GenericEJBContainer createStatelessEJBContainerWapper(CoreDeploymentInfo deploymentInfo) throws Exception {
        StatelessContainerBuilder builder = new StatelessContainerBuilder();
        builder.setClassLoader(Thread.currentThread().getContextClassLoader());
        builder.setContainerId(deploymentInfo.getDeploymentID());
        builder.setEJBName(deploymentInfo.getDeploymentID().toString());
        builder.setBeanClassName(deploymentInfo.getBeanClass().getName());
        builder.setHomeInterfaceName(deploymentInfo.getHomeInterface().getName());
        builder.setRemoteInterfaceName(deploymentInfo.getRemoteInterface().getName());
        builder.setTransactionPolicySource(new DeploymentInfoTxPolicySource(deploymentInfo));
        builder.setTransactionManager(OpenEJB.getTransactionManager());

        UserTransactionImpl userTransaction = new UserTransactionImpl();
        if (deploymentInfo.isBeanManagedTransaction()) {
            builder.setUserTransaction(userTransaction);
        }
        builder.setComponentContext(new ReadOnlyContextWrapper(deploymentInfo.getJndiEnc(), userTransaction));
        builder.setJndiNames(new String[]{deploymentInfo.getDeploymentID().toString()});

        return (GenericEJBContainer) builder.createContainer();
    }

    private static GenericEJBContainer createStatefulEJBContainerWapper(CoreDeploymentInfo deploymentInfo) throws Exception {
        StatefulContainerBuilder builder = new StatefulContainerBuilder();
        builder.setClassLoader(Thread.currentThread().getContextClassLoader());
        builder.setContainerId(deploymentInfo.getDeploymentID());
        builder.setEJBName(deploymentInfo.getDeploymentID().toString());
        builder.setBeanClassName(deploymentInfo.getBeanClass().getName());
        builder.setHomeInterfaceName(deploymentInfo.getHomeInterface().getName());
        builder.setRemoteInterfaceName(deploymentInfo.getRemoteInterface().getName());
        builder.setTransactionPolicySource(new DeploymentInfoTxPolicySource(deploymentInfo));
        builder.setTransactionManager(OpenEJB.getTransactionManager());

        UserTransactionImpl userTransaction = new UserTransactionImpl();
        if (deploymentInfo.isBeanManagedTransaction()) {
            builder.setUserTransaction(userTransaction);
        }
        builder.setComponentContext(new ReadOnlyContextWrapper(deploymentInfo.getJndiEnc(), userTransaction));
        builder.setJndiNames(new String[]{deploymentInfo.getDeploymentID().toString()});

        return (GenericEJBContainer) builder.createContainer();
    }

    private static GenericEJBContainer createBMPEntityEJBContainerWapper(CoreDeploymentInfo deploymentInfo) throws Exception {
        BMPContainerBuilder builder = new BMPContainerBuilder();
        builder.setClassLoader(Thread.currentThread().getContextClassLoader());
        builder.setContainerId(deploymentInfo.getDeploymentID());
        builder.setEJBName(deploymentInfo.getDeploymentID().toString());
        builder.setBeanClassName(deploymentInfo.getBeanClass().getName());
        builder.setHomeInterfaceName(deploymentInfo.getHomeInterface().getName());
        builder.setRemoteInterfaceName(deploymentInfo.getRemoteInterface().getName());
        builder.setPrimaryKeyClassName(deploymentInfo.getPrimaryKeyClass().getName());
        builder.setComponentContext(new ReadOnlyContextWrapper(deploymentInfo.getJndiEnc(), null));
        builder.setTransactionPolicySource(new DeploymentInfoTxPolicySource(deploymentInfo));
        builder.setTransactionManager(OpenEJB.getTransactionManager());
        builder.setJndiNames(new String[]{deploymentInfo.getDeploymentID().toString()});
        return (GenericEJBContainer) builder.createContainer();
    }

    static class ReadOnlyContextWrapper extends ReadOnlyContext {
        public ReadOnlyContextWrapper(Context ctx, UserTransaction userTransaction) throws NamingException {
            super();
            NamingEnumeration enum = ctx.list("");

            while (enum.hasMoreElements()) {
                NameClassPair pair = (NameClassPair) enum.next();

                String name = pair.getName();
                Object value = ctx.lookup(name);

                internalBind(name, value);
            }

            if (userTransaction != null) {
                internalBind("UserTransaction", userTransaction);
            }
        }
    }
}


