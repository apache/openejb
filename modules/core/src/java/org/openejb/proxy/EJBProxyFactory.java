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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;

import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;

import net.sf.cglib.reflect.FastClass;

import org.openejb.EJBContainer;
import org.openejb.EJBInterfaceType;
import org.openejb.dispatch.MethodHelper;
import org.openejb.dispatch.MethodSignature;

public class EJBProxyFactory {

    private static final Class[][] baseClasses = new Class[][]{
       {StatefulEJBObject.class, StatefulEJBHome.class, StatefulEJBLocalObject.class, StatefulEJBLocalHome.class}, // STATEFUL  
       {StatelessEJBObject.class, StatelessEJBHome.class, StatelessEJBLocalObject.class, StatelessEJBLocalHome.class}, // STATELESS 
       {EntityEJBObject.class, EntityEJBHome.class, EntityEJBLocalObject.class, EntityEJBLocalHome.class}, // BMP_ENTITY
       {EntityEJBObject.class, EntityEJBHome.class, EntityEJBLocalObject.class, EntityEJBLocalHome.class}, // CMP_ENTITY
    };
    
    private final CglibEJBProxyFactory ejbLocalObjectFactory;
    private final CglibEJBProxyFactory ejbLocalHomeFactory;
    private final CglibEJBProxyFactory ejbObjectFactory;
    private final CglibEJBProxyFactory ejbHomeFactory;

    private final int[] ejbLocalObjectMap;
    private final int[] ejbLocalHomeMap;
    private final int[] ejbObjectMap;
    private final int[] ejbHomeMap;
    
    private final ProxyInfo info;
    private final EJBContainer container;

    private final MethodSignature[] signatures;
////    private final int[][] operationsMaps;
    
    public EJBProxyFactory(EJBContainer container, ProxyInfo info, MethodSignature[] signatures) {        this.container = container;
        this.signatures = signatures;
        this.info = info;
        
        this.ejbLocalObjectFactory = getFactory(EJBInterfaceType.LOCAL.getOrdinal(), info.getLocalInterface());
        this.ejbLocalHomeFactory = getFactory(EJBInterfaceType.LOCALHOME.getOrdinal(), info.getLocalHomeInterface());
        this.ejbObjectFactory = getFactory(EJBInterfaceType.REMOTE.getOrdinal(), info.getRemoteInterface());
        this.ejbHomeFactory = getFactory(EJBInterfaceType.HOME.getOrdinal(), info.getHomeInterface());
    
        
        ejbLocalObjectMap = getOperationsMap(EJBInterfaceType.LOCAL, ejbLocalObjectFactory);
        ejbLocalHomeMap = getOperationsMap(EJBInterfaceType.LOCALHOME, ejbLocalHomeFactory);
        ejbObjectMap = getOperationsMap(EJBInterfaceType.REMOTE, ejbObjectFactory);
        ejbHomeMap = getOperationsMap(EJBInterfaceType.HOME, ejbHomeFactory);
    }

    private int[] getOperationsMap(EJBInterfaceType type, CglibEJBProxyFactory factory){
        if (factory == null) return new int[0];
        return EJBProxyHelper.getOperationMap(type, factory.getType(), signatures);
    }
    
    private CglibEJBProxyFactory getFactory(int interfaceType, Class interfaceClass){
        if (interfaceClass == null) return null;
        Class baseClass = baseClasses[info.getComponentType()][interfaceType];
        return new CglibEJBProxyFactory(baseClass, interfaceClass);
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
        EJBMethodInterceptor handler = new EJBMethodInterceptor(container, info, type, map);
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
        EJBMethodInterceptor handler = new EJBMethodInterceptor(container, new ProxyInfo(info,primaryKey), type, map);
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
        EJBMethodInterceptor handler = new EJBMethodInterceptor(container, info, type, map);
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
        EJBMethodInterceptor handler = new EJBMethodInterceptor(container, new ProxyInfo(info,primaryKey), type, map);
        return (EJBLocalObject) ejbLocalObjectFactory.create(handler); 
    }

}
