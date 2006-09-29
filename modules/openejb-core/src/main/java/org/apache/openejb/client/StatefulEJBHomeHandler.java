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
import java.rmi.NoSuchObjectException;
import javax.ejb.RemoveException;

import org.apache.geronimo.security.ContextManager;

/**
 * This InvocationHandler and its proxy are serializable and can be used by
 * HomeHandle, Handle, and MetaData to persist and revive handles. It maintains
 * its original client identity which allows the container to be more discerning about
 * allowing the revieed proxy to be used. See StatefulContaer manager for more details.
 * 
 */
public class StatefulEJBHomeHandler extends EJBHomeHandler {
    
    public StatefulEJBHomeHandler(){
    }
    
    public StatefulEJBHomeHandler(EJBMetaDataImpl ejb, ServerMetaData[] servers){
        super(ejb, servers);
    }
    
    /**
     *
     * EJB 1.1 Specification, Section 5.5 Session object identity
     * Since all session objects hide their identity, there is no need to provide a finder for them. The home
     * interface of a session bean must not define any finder methods.
     *
     * @param method
     * @param args
     * @param proxy
     * @return Returns an new EJBObject proxy and handler
     * @exception Throwable
     */
    protected Object findX(Method method, Object[] args, Object proxy) throws Throwable {
        throw new UnsupportedOperationException("Stateful beans may not have find methods");
    }
    
    /**
     * EJB 2.1 Specification, Section 6.6 Session Object Identity
     * Session objects are intended to be private resources used only by the client that created them. For this
     * reason, session objects, from the client’s perspective, appear anonymous. In contrast to entity objects,
     * which expose their identity as a primary key, session objects hide their identity. As a result, the EJBObject.
     * getPrimaryKey() method results in a java.rmi.RemoteException and the EJBLocalObject.
     * getPrimaryKey() method results in a javax.ejb.EJBException, and the
     * EJBHome.remove(Object primaryKey) and the EJBLocalHome.remove(Object primaryKey)
     * methods result in a javax.ejb.RemoveException if called on a session bean. If the
     * EJBMetaData.getPrimaryKeyClass()method is invoked on a EJBMetaData object for a
     * session bean, the method throws the java.lang.RuntimeException.
     */
    protected Object removeByPrimaryKey(Method method, Object[] args, Object proxy) throws RemoveException {
        throw new RemoveException("Session objects are private resources and do not have primary keys");
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
        // Extract the primary key from the handle
        EJBObjectHandle handle = (EJBObjectHandle)args[0];
        
        // TODO:1: Check that this is exactly spec compliant
        if ( handle == null ) throw new NullPointerException("The handle is null");
        
        EJBObjectHandler handler = handle.handler;
        Object primKey = handler.primaryKey;

        if (handler.isInvalidReference) {
            throw new NoSuchObjectException("Handle has been invalidated due to removal or system exception");
        }

        // TODO:1: Check that this is exactly spec compliant
        if ( !handler.ejb.deploymentID.equals(this.ejb.deploymentID) ){
            throw new IllegalArgumentException("The handle is not from the same deployment");
        }

        EJBRequest req = new EJBRequest( EJB_HOME_REMOVE_BY_HANDLE ); 
        req.setClientIdentity( ContextManager.getThreadPrincipal() );
        req.setContainerCode( handler.ejb.deploymentCode );
        req.setContainerID(   handler.ejb.deploymentID );
        req.setMethodInstance( method );
        req.setMethodParameters( args );
        req.setPrimaryKey( primKey );
        
        EJBResponse res = request( req );
  
        if ( res.getResponseCode() == res.EJB_ERROR ) {
            throw (Throwable)res.getResult();
        }
        
        invalidateAllHandlers(handler.getRegistryId());
        handler.invalidateReference();
        return null;
    }
}
