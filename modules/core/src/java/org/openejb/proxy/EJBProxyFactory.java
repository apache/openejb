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

import org.openejb.EJBContainer;
import org.openejb.EJBInterfaceType;
import org.openejb.dispatch.InterfaceMethodSignature;

public class EJBProxyFactory implements Serializable {

    private static final Class[][] baseClasses = new Class[][]{
       {StatefulEJBObject.class, SessionEJBHome.class, StatefulEJBLocalObject.class, SessionEJBLocalHome.class}, // STATEFUL  
       {StatelessEJBObject.class, SessionEJBHome.class, StatelessEJBLocalObject.class, SessionEJBLocalHome.class}, // STATELESS 
       {EntityEJBObject.class, EntityEJBHome.class, EntityEJBLocalObject.class, EntityEJBLocalHome.class}, // BMP_ENTITY
       {EntityEJBObject.class, EntityEJBHome.class, EntityEJBLocalObject.class, EntityEJBLocalHome.class}, // CMP_ENTITY
    };
    
    private transient final CglibEJBProxyFactory ejbLocalObjectFactory;
    private transient final CglibEJBProxyFactory ejbLocalHomeFactory;
    private transient final CglibEJBProxyFactory ejbObjectFactory;
    private transient final CglibEJBProxyFactory ejbHomeFactory;

    private transient final int[] ejbLocalObjectMap;
    private transient final int[] ejbLocalHomeMap;
    private transient final int[] ejbObjectMap;
    private transient final int[] ejbHomeMap;
    
    private transient final Map legacyMethodMap;

    private transient EJBContainer container;
    
    private final ProxyInfo proxyInfo;

    private final InterfaceMethodSignature[] signatures;

    public EJBProxyFactory(ProxyInfo proxyInfo, InterfaceMethodSignature[] signatures) {
        this.signatures = signatures;
        this.proxyInfo = proxyInfo;
        
        this.ejbLocalObjectFactory = getFactory(EJBInterfaceType.LOCAL.getOrdinal(), proxyInfo.getLocalInterface());
        this.ejbLocalHomeFactory = getFactory(EJBInterfaceType.LOCALHOME.getOrdinal(), proxyInfo.getLocalHomeInterface());
        this.ejbObjectFactory = getFactory(EJBInterfaceType.REMOTE.getOrdinal(), proxyInfo.getRemoteInterface());
        this.ejbHomeFactory = getFactory(EJBInterfaceType.HOME.getOrdinal(), proxyInfo.getHomeInterface());
    
        // build the legacy map
        Map map = new HashMap();
        addLegacyMethods(map, proxyInfo.getHomeInterface(), signatures);
        addLegacyMethods(map, proxyInfo.getRemoteInterface(), signatures);
        addLegacyMethods(map, proxyInfo.getLocalHomeInterface(), signatures);
        addLegacyMethods(map, proxyInfo.getLocalInterface(), signatures);
        legacyMethodMap = Collections.unmodifiableMap(map);
        
        ejbLocalObjectMap = getOperationsMap(ejbLocalObjectFactory);
        ejbLocalHomeMap = getOperationsMap(ejbLocalHomeFactory);
        ejbObjectMap = getOperationsMap(ejbObjectFactory);
        ejbHomeMap = getOperationsMap(ejbHomeFactory);
    }
    
    public int getMethodIndex(Method method) {
        Integer index = (Integer) legacyMethodMap.get(method);
        if (index == null) {
            index = new Integer(-1);
        }
        return index.intValue();
    }
    
    public void setContainer(EJBContainer container) {
        this.container = container;
    }

    public String getEJBName() {
        return container.getEJBName();
    }

    private int[] getOperationsMap(CglibEJBProxyFactory factory){
        if (factory == null) return new int[0];
        return EJBProxyHelper.getOperationMap(factory.getType(), signatures);
    }
    
    private CglibEJBProxyFactory getFactory(int interfaceType, Class interfaceClass){
        if (interfaceClass == null) return null;
        Class baseClass = baseClasses[proxyInfo.getComponentType()][interfaceType];
        return new CglibEJBProxyFactory(baseClass, interfaceClass);
    }

    public ProxyInfo getProxyInfo() {
        return proxyInfo;
    }

    /**
     * Return a proxy for the EJB's home interface. This can be passed back
     * to any client that wishes to access the EJB (e.g. in response to a
     * call to EJBContext.getEJBHome() )
     * @return the proxy for this EJB's home interface
     */
    public EJBHome getEJBHome(){
        EJBInterfaceType type = EJBInterfaceType.HOME;
        int[] map = ejbHomeMap;
        EJBMethodInterceptor handler = new EJBMethodInterceptor(container, this, type, map);
        return (EJBHome) ejbHomeFactory.create(handler); 
    }

    /**
     * Return a proxy for the EJB's remote interface. This can be passed back
     * to any client that wishes to access the EJB (e.g. in response to a
     * call to SessionContext.getEJBObject() )
     * @return the proxy for this EJB's home interface
     */
    public EJBObject getEJBObject(Object primaryKey){
        // TODO: Refactor EJBMethodHandler so it can take a primary key as a parameter 
        // TODO: Refactor ProxyInfo so it doesn't have a primary key
        EJBInterfaceType type = EJBInterfaceType.REMOTE;
        int[] map = ejbObjectMap;
        EJBMethodInterceptor handler = new EJBMethodInterceptor(container, this, type, map, primaryKey);
        return (EJBObject) ejbObjectFactory.create(handler); 
    }

    /**
     * Return a proxy for the EJB's local home interface. This can be
     * passed back to any client that wishes to access the EJB
     * (e.g. in response to a call to EJBContext.getEJBLocalHome() )
     * @return the proxy for this EJB's local home interface
     */
    public EJBLocalHome getEJBLocalHome(){
        EJBInterfaceType type = EJBInterfaceType.LOCALHOME;
        int[] map = ejbLocalHomeMap;
        EJBMethodInterceptor handler = new EJBMethodInterceptor(container, this, type, map);
        return (EJBLocalHome) ejbLocalHomeFactory.create(handler); 
    }

    /**
     * Return a proxy for the EJB's local interface. This can be passed back
     * to any client that wishes to access the EJB (e.g. in response to a
     * call to SessionContext.getEJBLocalObject() )
     * @return the proxy for this EJB's local interface
     */
    public EJBLocalObject getEJBLocalObject(Object primaryKey){
        EJBInterfaceType type = EJBInterfaceType.LOCAL;
        int[] map = ejbLocalObjectMap;
        EJBMethodInterceptor handler = new EJBMethodInterceptor(container, this, type, map, primaryKey);
        return (EJBLocalObject) ejbLocalObjectFactory.create(handler); 
    }

    private Object readResolve() {
        return new EJBProxyFactory(proxyInfo, signatures);
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
    
}
