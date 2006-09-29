/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.client;

import java.lang.reflect.Method;
import java.rmi.RemoteException;

import org.apache.geronimo.security.ContextManager;

/**
 * This InvocationHandler and its proxy are serializable and can be used by
 * HomeHandle, Handle, and MetaData to persist and revive handles. It maintains
 * its original client identity which allows the container to be more discerning about
 * allowing the revieed proxy to be used. See StatefulContaer manager for more details.
 * 
 */
public class EntityEJBObjectHandler extends EJBObjectHandler {
    
                    
    public EntityEJBObjectHandler(){
    }
    
    public EntityEJBObjectHandler(EJBMetaDataImpl ejb, ServerMetaData[] servers){
        super(ejb, servers);
    }
    
    public EntityEJBObjectHandler(EJBMetaDataImpl ejb, ServerMetaData[] servers, Object primaryKey){
        super(ejb, servers, primaryKey);
        registryId = ejb.deploymentID+":"+primaryKey;
        registerHandler( registryId, this );
    }
    
    
    /**
    * The Registry id is a logical identifier that is used as a key when placing EjbObjectProxyHanlders into
    * the BaseEjbProxyHanlder's liveHandleRegistry.  EjbObjectProxyHanlders that represent the same
    * bean identity (keyed by the registry id) will be stored together so that they can be removed together
    * when the EJBInvocationHandler.invalidateAllHandlers is invoked.
    *
    * This method is implemented by the subclasses to return an id that logically identifies
    * bean identity for a specific deployment id and container.  The EntityEJBObjectHandler
    * overrides this method to return a compound key composed of the bean's primary key, deployment id, and
    * container id.  This uniquely identifies the bean identity that is proxied by this handler.
    */
    public Object getRegistryId(){
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
     * @return 
     * @exception Throwable
     */
    protected Object isIdentical(Method method, Object[] args, Object proxy) throws Throwable{
        if ( args[0] == null ) return Boolean.FALSE;

        EJBObjectProxy ejbObject = (EJBObjectProxy)args[0];
        EJBObjectHandler that = ejbObject.getEJBObjectHandler();

        return new Boolean(this.registryId.equals(that.registryId));
    
    }
    
    protected Object remove(Method method, Object[] args, Object proxy) throws Throwable{
        
        EJBRequest req = new EJBRequest( EJB_OBJECT_REMOVE ); 
        
        req.setMethodParameters( args );
        req.setMethodInstance( method );
        req.setClientIdentity( ContextManager.getThreadPrincipal() );
        req.setContainerCode( ejb.deploymentCode );
        req.setContainerID( ejb.deploymentID );
        req.setPrimaryKey( primaryKey );
        
        EJBResponse res = request( req );
  
        switch (res.getResponseCode()) {
        case EJB_ERROR:
            throw (Throwable)res.getResult();
        case EJB_SYS_EXCEPTION:
            throw (Throwable)res.getResult();
        case EJB_APP_EXCEPTION:
            throw (Throwable)res.getResult();
        case EJB_OK:
            invalidateAllHandlers(getRegistryId());
            invalidateReference();
            return null;
        default:
            throw new RemoteException("Received invalid response code from server: "+res.getResponseCode());
        }
    }
    
}
