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
package org.openejb.core.entity;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.ejb.EJBHome;
import org.openejb.DeploymentInfo;
import org.openejb.InvalidateReferenceException;
import org.openejb.OpenEJB;
import org.openejb.OpenEJBException;
import org.openejb.ProxyInfo;
import org.openejb.RpcContainer;
import org.openejb.core.ThreadContext;
import org.openejb.core.ivm.EjbHomeProxyHandler;
import org.openejb.core.ivm.EjbObjectProxyHandler;
import org.openejb.core.ivm.IntraVmHandle;
import org.openejb.core.ivm.IntraVmMetaData;
import org.openejb.util.proxy.InvalidatedReferenceHandler;
import org.openejb.util.proxy.InvocationHandler;
import org.openejb.util.proxy.InvocationHandler;
import org.openejb.util.proxy.ProxyManager;


/**
 * This InvocationHandler and its proxy are serializable and can be used by
 * HomeHandle, Handle, and MetaData to persist and revive handles. It maintains
 * its original client identity which allows the container to be more discerning about
 * allowing the revieed proxy to be used. See StatefulContaer manager for more details.
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class EntityEjbHomeHandler extends EjbHomeProxyHandler {
    
    public EntityEjbHomeHandler(RpcContainer container, Object pk, Object depID){
        super(container, pk, depID);
    }
    
    protected Object createProxy(ProxyInfo proxyInfo){
        Object proxy = super.createProxy(proxyInfo);
        EjbObjectProxyHandler handler = (EjbObjectProxyHandler)ProxyManager.getInvocationHandler(proxy);
        
        /* 
        * Register the handle with the BaseEjbProxyHandler.liveHandleRegistry
        * If the bean is removed by its home or by an identical proxy, then the 
        * this proxy will be automatically invalidated because its properly registered
        * with the liveHandleRegistry.
        */
        registerHandler(handler.getRegistryId(),handler);
        
        return proxy;
    
    }
    /**
     * <P>
     * Locates and returns a new EJBObject or a collection
     * of EJBObjects.  The EJBObject(s) is a new proxy with
     * a new handler. This implementation should not be
     * sent outside the virtual machine.
     * </P>
     * <P>
     * This method propogates to the container
     * system.
     * </P>
     * <P>
     * The find method is required to be defined
     * by the bean's home interface of Entity beans.
     * </P>
     *
     * @param method
     * @param args
     * @param proxy
     * @return Returns an new EJBObject proxy and handler
     * @exception Throwable
     */
    /**
     * <P>
     * Locates and returns a new EJBObject or a collection
     * of EJBObjects.  The EJBObject(s) is a new proxy with
     * a new handler. This implementation should not be
     * sent outside the virtual machine.
     * </P>
     * <P>
     * This method propogates to the container
     * system.
     * </P>
     * <P>
     * The find method is required to be defined
     * by the bean's home interface of Entity beans.
     * </P>
     * 
     * @param method
     * @param args
     * @param proxy
     * @return Returns an new EJBObject proxy and handler
     * @exception Throwable
     */
    protected Object findX(Method method, Object[] args, Object proxy) throws Throwable {
        Object retValue = container.invoke(deploymentID,method,args,null, getThreadSpecificSecurityIdentity());

        if ( retValue instanceof java.util.Collection ) {

            Object [] proxyInfos = ((java.util.Collection)retValue).toArray();
            Vector proxies = new Vector();
            for ( int i = 0; i < proxyInfos.length; i++ ) {
                proxies.addElement( createProxy((ProxyInfo)proxyInfos[i]) );
            }
            if(method.getReturnType() == Enumeration.class){
                /* 
                   FIXME: This needs to change for two reasons:
                   a) Although we know the core containers return a Vector we shouldn't assume that
                   b) The Vector's impl of Enumeration is not serializable.
                */
                return ((java.util.Vector)proxies).elements();
            }else
                return proxies;// vector is a type of Collection.

        } else {
            org.openejb.ProxyInfo proxyInfo = (org.openejb.ProxyInfo)
              container.invoke(deploymentID,method,args,null, getThreadSpecificSecurityIdentity());

            return createProxy(proxyInfo); 
        }

    }
    /**
     * <P>
     * Attempts to remove an EJBObject from the
     * container system.  The EJBObject to be removed
     * is represented by the primaryKey passed
     * into the remove method of the EJBHome.
     * </P>
     * <P>
     * This method propogates to the container system.
     * </P>
     * <P>
     * remove(Object primary) is a method of javax.ejb.EJBHome
     * </P>
     * <P>
     * Checks if the caller is authorized to invoke the
     * javax.ejb.EJBHome.remove on the EJBHome of the
     * deployment.
     * </P>
     *
     * @param method
     * @param args
     * @return Returns null
     * @exception Throwable
     * @see javax.ejb.EJBHome
     * @see javax.ejb.EJBHome#remove
     */
    protected Object removeByPrimaryKey(Method method, Object[] args, Object proxy) throws Throwable{
        checkAuthorization(method);
        Object primKey = args[0];
        container.invoke(deploymentID, method, args, primKey, getThreadSpecificSecurityIdentity());
            
        /* 
        * This operation takes care of invalidating all the EjbObjectProxyHanders associated with 
        * the same RegistryId. See this.createProxy().
        */
        invalidateAllHandlers(EntityEjbObjectHandler.getRegistryId(primKey,deploymentID,container));
        return null;
    }
    /**
     * <P>
     * Attempts to remove an EJBObject from the
     * container system.  The EJBObject to be removed
     * is represented by the javax.ejb.Handle object passed
     * into the remove method in the EJBHome.
     * </P>
     * <P>
     * This method propogates to the container system.
     * </P>
     * <P>
     * remove(Handle handle) is a method of javax.ejb.EJBHome
     * </P>
     * <P>
     * Checks if the caller is authorized to invoke the
     * javax.ejb.EJBHome.remove on the EJBHome of the
     * deployment.
     * </P>
     * 
     * @param method
     * @param args
     * @return Returns null
     * @exception Throwable
     * @see javax.ejb.EJBHome
     * @see javax.ejb.EJBHome#remove
     */
    protected Object removeWithHandle(Method method, Object[] args, Object proxy) throws Throwable{
        checkAuthorization(method);

        // Extract the primary key from the handle
        IntraVmHandle handle = (IntraVmHandle)args[0];
        EjbObjectProxyHandler stub = (EjbObjectProxyHandler)ProxyManager.getInvocationHandler(handle.theProxy);
        Object primKey = stub.primaryKey;
        // invoke the remove on the container
        container.invoke(deploymentID, method, args, primKey, ThreadContext.getThreadContext().getSecurityIdentity());
        
        /* 
        * This operation takes care of invalidating all the EjbObjectProxyHanders associated with 
        * the same RegistryId. See this.createProxy().
        */
        invalidateAllHandlers(stub.getRegistryId());
        return null;
    }
    
    /*-------------------------------------------------*/
    /*  EJBHome methods                                */  
    /*-------------------------------------------------*/

    /**
     * <P>
     * Returns an EJBMetaData implementation that is
     * valid inside this virtual machine.  This
     * implementation should not be sent outside the
     * virtual machine.
     * </P>
     * <P>
     * This method does not propogate to the container
     * system.
     * </P>
     * <P>
     * getMetaData is a method of javax.ejb.EJBHome
     * </P>
     * <P>
     * Checks if the caller is authorized to invoke the
     * javax.ejb.EJBHome.getMetaData on the EJBHome of the
     * deployment.
     * </P>
     * 
     * @return Returns an IntraVmMetaData
     * @exception Throwable
     * @see IntraVmMetaData
     * @see javax.ejb.EJBHome
     * @see javax.ejb.EJBHome#getEJBMetaData
     */
    protected Object getEJBMetaData(Method method, Object[] args, Object proxy) throws Throwable {
        checkAuthorization(method);

        byte compType = IntraVmMetaData.ENTITY;

        // component type is identified outside the IntraVmMetaData so that IntraVmMetaData doesn't reference DeploymentInfo avoiding the need to load the DeploymentInfo class into the client VM.
        IntraVmMetaData metaData = new IntraVmMetaData(deploymentInfo.getHomeInterface(), deploymentInfo.getRemoteInterface(),deploymentInfo.getPrimaryKeyClass(),compType);
        metaData.setEJBHome((EJBHome)proxy);
        return metaData;

    }
    
    protected EjbObjectProxyHandler newEjbObjectHandler(RpcContainer container, Object pk, Object depID) {
        return new EntityEjbObjectHandler(container, pk, depID);
    }
    
}
