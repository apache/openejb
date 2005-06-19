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

import java.lang.reflect.Method;

import org.openejb.Container;
import org.openejb.RpcContainer;
import org.openejb.core.ivm.EjbObjectProxyHandler;
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
public class EntityEjbObjectHandler extends EjbObjectProxyHandler {

    private final static class RegistryEntry{
        final Object primaryKey;
        final Object deploymentId;
        final Object containerId;

        RegistryEntry(Object primaryKey, Object deploymentId, Object containerId) {
            if(primaryKey==null || deploymentId==null || containerId==null) {
                throw new IllegalArgumentException();
            }
            this.primaryKey=primaryKey;
            this.deploymentId=deploymentId;
            this.containerId=containerId;
        }

        public boolean equals(Object other) {
            if(other==this) {
                return true;
            }
            if(other instanceof RegistryEntry) {
                RegistryEntry otherEntry = (RegistryEntry) other;
                return primaryKey.equals(otherEntry.primaryKey) &&
                    deploymentId.equals(otherEntry.deploymentId) &&
                    containerId.equals(otherEntry.containerId);
            }
            return false;
        }

        public int hashCode() {
            return primaryKey.hashCode();
        }
    }
    /*
    * The registryId is a logical identifier that is used as a key when placing EntityEjbObjectHandler into
    * the BaseEjbProxyHanlder's liveHandleRegistry.  EntityEjbObjectHandlers that represent the same
    * bean identity (keyed by the registry id) will be stored together so that they can be removed together
    * when the BaseEjbProxyHandler.invalidateAllHandlers is invoked. The EntityEjbObjectHandler uses a 
    * compound key composed of the entity bean's primary key, deployment id, and
    * container id.  This uniquely identifies the bean identity that is proxied by this handler allowing it
    * to be removed with other handlers bound to the same registry id.
    */
    private Object registryId;      
                    
    public EntityEjbObjectHandler(RpcContainer container, Object pk, Object depID){
        super(container, pk, depID);
    }
    
    /*
    * This method generates a logically unique entity bean identifier from the primary key,
    * deployment id, and container id. This registry key is then used as an index for the associated
    * entity bean in the BaseEjbProxyHandler.liveHandleRegistry. The liveHandleRegistry tracks 
    * handler for the same bean identity so that they can removed together when one of the remove() operations
    * is called.
    */
    public static Object getRegistryId(Object primKey, Object deployId, Container contnr){
        return new RegistryEntry(primKey, deployId, contnr.getContainerID());
    }
    
    /**
    * The Registry id is a logical identifier that is used as a key when placing EjbObjectProxyHanlders into
    * the BaseEjbProxyHanlder's liveHandleRegistry.  EjbObjectProxyHanlders that represent the same
    * bean identity (keyed by the registry id) will be stored together so that they can be removed together
    * when the BaseEjbProxyHandler.invalidateAllHandlers is invoked.
    *
    * This method is implemented by the subclasses to return an id that logically identifies
    * bean identity for a specific deployment id and container.  The EntityEjbObjectHandler
    * overrides this method to return a compound key composed of the bean's primary key, deployment id, and
    * container id.  This uniquely identifies the bean identity that is proxied by this handler.
    */
    public Object getRegistryId(){
        if(registryId== null)
            registryId= getRegistryId(primaryKey, deploymentID, container);
        return registryId;
    }
    
    
    protected Object getPrimaryKey(Method method, Object[] args, Object proxy) throws Throwable{
        return primaryKey;
    }
    
    /**
     * Entity beans are uniquely identifed by primary key, deloyment id, and the container they are
     * running in.
     *
     * @param method
     * @param args
     * @param proxy
     * @return Object 
     * @exception Throwable
     */
    protected Object isIdentical(Method method, Object[] args, Object proxy) throws Throwable{
        checkAuthorization(method);
        
        Object hndr = ProxyManager.getInvocationHandler(proxy);
        
        if(hndr instanceof EntityEjbObjectHandler){
        
            EntityEjbObjectHandler handler = (EntityEjbObjectHandler)hndr;
            
            /*
            * The registry id is a compound key composed of the bean's primary key, deployment id, and
            * container id.  It uniquely identifies the entity bean that is proxied by the EntityEjbObjectHandler
            * within the IntraVM.
            */
            if(this.getRegistryId().equals(handler.getRegistryId())){
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
        
    }
    
    protected Object remove(Method method, Object[] args, Object proxy) throws Throwable{
        checkAuthorization(method);
        Object value = container.invoke(deploymentID, method, args, primaryKey, getThreadSpecificSecurityIdentity());
        /* 
        * This operation takes care of invalidating all the EjbObjectProxyHanders associated with 
        * the same RegistryId. See this.createProxy().
        */
        invalidateAllHandlers(getRegistryId());
        return value;
    }
    
}
