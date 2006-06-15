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
package org.openejb.proxy;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;

import org.openejb.ContainerIndex;
import org.openejb.EJBContainer;
import org.openejb.EJBInterfaceType;
import org.openejb.ContainerNotFoundException;
import org.openejb.dispatch.InterfaceMethodSignature;

public class EJBProxyFactory implements Serializable, org.tranql.ejb.EJBProxyFactory {
    private static final long serialVersionUID = -3095014837800918826L;

    private static final Class[] sessionBaseClasses =
            new Class[]{SessionEJBObject.class, SessionEJBHome.class, SessionEJBLocalObject.class, SessionEJBLocalHome.class};

    private static final Class[] entityBaseClasses =
            new Class[]{EntityEJBObject.class, EntityEJBHome.class, EntityEJBLocalObject.class, EntityEJBLocalHome.class};

    private final String containerId;
    private final boolean isSessionBean;
    private final Class remoteInterface;
    private final Class homeInterface;
    private final Class localInterface;
    private final Class localHomeInterface;

    private transient final CglibEJBProxyFactory remoteFactory;
    private transient final CglibEJBProxyFactory homeFactory;
    private transient final CglibEJBProxyFactory localFactory;
    private transient final CglibEJBProxyFactory localHomeFactory;

    private transient InterfaceMaps interfaceMaps;

    public EJBProxyFactory(EJBContainer container) {
        this(container.getProxyInfo());
        interfaceMaps = new InterfaceMaps(container);
    }

    public EJBProxyFactory(ProxyInfo proxyInfo) {
        this(
                proxyInfo.getContainerID(),
                proxyInfo.isSessionBean(),
                proxyInfo.getRemoteInterface(),
                proxyInfo.getHomeInterface(),
                proxyInfo.getLocalInterface(),
                proxyInfo.getLocalHomeInterface());
    }

    public EJBProxyFactory(
            String containerId,
            boolean sessionBean,
            Class remoteInterface,
            Class homeInterface,
            Class localInterface,
            Class localHomeInterface) {
        this.containerId = containerId;
        isSessionBean = sessionBean;

// JNB: these are temporarily disabled due to classloader issues during deployment
//        assert remoteInterface == null || (remoteInterface.isInterface() && EJBObject.class.isAssignableFrom(remoteInterface));
        this.remoteInterface = remoteInterface;

//        assert homeInterface == null || (homeInterface.isInterface() && EJBHome.class.isAssignableFrom(homeInterface));
        this.homeInterface = homeInterface;

//        assert localInterface == null || (localInterface.isInterface() && EJBLocalObject.class.isAssignableFrom(localInterface));
        this.localInterface = localInterface;

//        assert localHomeInterface == null || (localHomeInterface.isInterface() && EJBLocalHome.class.isAssignableFrom(localHomeInterface));
        this.localHomeInterface = localHomeInterface;

        this.remoteFactory = getFactory(EJBInterfaceType.REMOTE.getOrdinal(), remoteInterface);
        this.homeFactory = getFactory(EJBInterfaceType.HOME.getOrdinal(), homeInterface);
        this.localFactory = getFactory(EJBInterfaceType.LOCAL.getOrdinal(), localInterface);
        this.localHomeFactory = getFactory(EJBInterfaceType.LOCALHOME.getOrdinal(), localHomeInterface);
    }

    public String getEJBName() {
        InterfaceMaps interfaceMaps = getInterfaceMaps();
        return interfaceMaps.getContainer().getEjbName();
    }

    EJBContainer getContainer() throws ContainerNotFoundException {
        InterfaceMaps interfaceMaps = getInterfaceMaps();
        return interfaceMaps.getContainer();
    }

    private synchronized InterfaceMaps getInterfaceMaps() {
        InterfaceMaps interfaceMaps = this.interfaceMaps;
        if (interfaceMaps == null) {
            ContainerIndex containerIndex = ContainerIndex.getInstance();

            EJBContainer container = null;
            try {
                container = containerIndex.getContainer(containerId);
            } catch (ContainerNotFoundException e) {
            }

            if (container == null) {
                throw new IllegalStateException("Contianer not found: " + containerId);
            }

            interfaceMaps = new InterfaceMaps(container);
            this.interfaceMaps = interfaceMaps;
        }
        return interfaceMaps;
    }

    public ClassLoader getClassLoader() {
        if (remoteFactory != null) {
            return remoteFactory.getType().getClassLoader();
        } else {
            return localFactory.getType().getClassLoader();
        }
    }

    int[] getOperationMap(EJBInterfaceType type) throws ContainerNotFoundException {
        EJBProxyFactory.InterfaceMaps interfaceMaps = getInterfaceMaps();

        if (type == EJBInterfaceType.REMOTE) {
            return interfaceMaps.remoteMap;
        } else if (type == EJBInterfaceType.HOME) {
            return interfaceMaps.homeMap;
        } else if (type == EJBInterfaceType.LOCAL) {
            return interfaceMaps.localMap;
        } else if (type == EJBInterfaceType.LOCALHOME) {
            return interfaceMaps.localHomeMap;
        } else {
            throw new IllegalArgumentException("Unsupported interface type " + type);
        }
    }

    public int getMethodIndex(Method method) {
        EJBProxyFactory.InterfaceMaps interfaceMaps = getInterfaceMaps();
        Integer index = (Integer) interfaceMaps.legacyMethodMap.get(method);
        if (index == null) {
            index = new Integer(-1);
        }
        return index.intValue();
    }

    public Class getLocalInterfaceClass() {
        return localInterface;
    }

    public Class getRemoteInterfaceClass() {
        return remoteInterface;
    }

    /**
     * Return a proxy for the EJB's remote interface. This can be passed back
     * to any client that wishes to access the EJB (e.g. in response to a
     * call to SessionContext.getEJBObject() )
     * @return the proxy for this EJB's home interface
     */
    public EJBObject getEJBObject(Object primaryKey) {
        if (remoteFactory == null) {
            throw new IllegalStateException("getEJBObject is not allowed if no remote interface is defined");
        }
        EJBProxyFactory.InterfaceMaps interfaceMaps = getInterfaceMaps();
        EJBMethodInterceptor handler = new EJBMethodInterceptor(
                this,
                EJBInterfaceType.REMOTE,
                interfaceMaps.container,
                interfaceMaps.remoteMap,
                primaryKey);
        return (EJBObject) remoteFactory.create(handler);
    }

    /**
     * Return a proxy for the EJB's home interface. This can be passed back
     * to any client that wishes to access the EJB (e.g. in response to a
     * call to EJBContext.getEJBHome() )
     * @return the proxy for this EJB's home interface
     */
    public EJBHome getEJBHome() {
        if (homeFactory == null) {
            throw new IllegalStateException("getEJBHome is not allowed if no remote interface is defined");
        }
        EJBProxyFactory.InterfaceMaps interfaceMaps = getInterfaceMaps();
        EJBMethodInterceptor handler = new EJBMethodInterceptor(
                this,
                EJBInterfaceType.HOME,
                interfaceMaps.container,
                interfaceMaps.homeMap);
        return (EJBHome) homeFactory.create(handler);
    }

    /**
     * Return a proxy for the EJB's local interface. This can be passed back
     * to any client that wishes to access the EJB (e.g. in response to a
     * call to SessionContext.getEJBLocalObject() )
     * @return the proxy for this EJB's local interface
     */

    public EJBLocalObject getEJBLocalObject(Object primaryKey) {
        if (localFactory == null) {
            throw new IllegalStateException("getEJBLocalObject is not allowed if no local interface is defined");
        }
        EJBProxyFactory.InterfaceMaps interfaceMaps = getInterfaceMaps();
        EJBMethodInterceptor handler = new EJBMethodInterceptor(
                this,
                EJBInterfaceType.LOCAL,
                interfaceMaps.container,
                interfaceMaps.localMap,
                primaryKey);
        return (EJBLocalObject) localFactory.create(handler);
    }

    /**
     * Return a proxy for the EJB's local home interface. This can be
     * passed back to any client that wishes to access the EJB
     * (e.g. in response to a call to EJBContext.getEJBLocalHome() )
     * @return the proxy for this EJB's local home interface
     */

    public EJBLocalHome getEJBLocalHome() {
        if (localFactory == null) {
            throw new IllegalStateException("getEJBLocalHome is not allowed if no local interface is defined");
        }
        EJBProxyFactory.InterfaceMaps interfaceMaps = getInterfaceMaps();
        EJBMethodInterceptor handler = new EJBMethodInterceptor(
                this,
                EJBInterfaceType.LOCALHOME,
                interfaceMaps.container,
                interfaceMaps.localHomeMap);
        return (EJBLocalHome) localHomeFactory.create(handler);
    }

    private CglibEJBProxyFactory getFactory(int interfaceType, Class interfaceClass) {
        if (interfaceClass == null) {
            return null;
        }

        Class baseClass;
        if (isSessionBean) {
            baseClass = sessionBaseClasses[interfaceType];
        } else {
            baseClass = entityBaseClasses[interfaceType];
        }

        ClassLoader classLoader = findClassLoader(baseClass, interfaceClass);

        return new CglibEJBProxyFactory(baseClass, interfaceClass, classLoader);
    }

    private ClassLoader findClassLoader(Class baseClass, Class interfaceClass) {
        ClassLoader cl = interfaceClass.getClassLoader();
        try {
            cl.loadClass(baseClass.getName());
            return cl;
        } catch (ClassNotFoundException e) {

        }
        cl = baseClass.getClassLoader();
        try {
            cl.loadClass(interfaceClass.getName());
            return cl;
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Openejb base class: " + baseClass.getName() + " and interface class: " + interfaceClass.getName() + " do not have a common classloader that will load both!");
        }
    }

    private static void addLegacyMethods(Map legacyMethodMap, Class clazz, InterfaceMethodSignature[] signatures) {
        if (clazz == null) {
            return;
        }

        for (int i = 0; i < signatures.length; i++) {
            InterfaceMethodSignature signature = signatures[i];
            Method method = signature.getMethod(clazz);
            if (method != null) {
                legacyMethodMap.put(method, new Integer(i));
            }
        }
    }

    protected Object readResolve() {
        return new EJBProxyFactory(
                containerId,
                isSessionBean,
                remoteInterface,
                homeInterface,
                localInterface,
                localHomeInterface);
    }

    private final class InterfaceMaps {
        private final EJBContainer container;
        private final int[] remoteMap;
        private final int[] homeMap;
        private final int[] localMap;
        private final int[] localHomeMap;
        private final Map legacyMethodMap;

        public InterfaceMaps(EJBContainer container) {
            if (container == null) throw new NullPointerException("container is null");
            this.container = container;

            ProxyInfo proxyInfo = container.getProxyInfo();
            InterfaceMethodSignature[] signatures = container.getSignatures();

            // build the legacy map
            Map map = new HashMap();
            addLegacyMethods(map, proxyInfo.getRemoteInterface(), signatures);
            addLegacyMethods(map, proxyInfo.getHomeInterface(), signatures);
            addLegacyMethods(map, proxyInfo.getLocalInterface(), signatures);
            addLegacyMethods(map, proxyInfo.getLocalHomeInterface(), signatures);
            addLegacyMethods(map, proxyInfo.getServiceEndpointInterface(), signatures);
            legacyMethodMap = Collections.unmodifiableMap(map);

            remoteMap = createOperationsMap(remoteFactory, signatures);
            homeMap = createOperationsMap(homeFactory, signatures);

            localMap = createOperationsMap(localFactory, signatures);
            localHomeMap = createOperationsMap(localHomeFactory, signatures);
        }

        public EJBContainer getContainer() {
            return container;
        }

        public int[] getRemoteMap() {
            return remoteMap;
        }

        public int[] getHomeMap() {
            return homeMap;
        }

        public int[] getLocalMap() {
            return localMap;
        }

        public int[] getLocalHomeMap() {
            return localHomeMap;
        }

        public Map getLegacyMethodMap() {
            return legacyMethodMap;
        }

        private int[] createOperationsMap(CglibEJBProxyFactory factory, InterfaceMethodSignature[] signatures) {
            if (factory == null) return new int[0];
            return EJBProxyHelper.getOperationMap(factory.getType(), signatures, false);
        }
    }
}
