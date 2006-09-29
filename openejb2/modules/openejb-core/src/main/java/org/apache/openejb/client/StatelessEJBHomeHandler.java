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
import javax.ejb.RemoveException;

/**
 * This InvocationHandler and its proxy are serializable and can be used by
 * HomeHandle, Handle, and MetaData to persist and revive handles. It maintains
 * its original client identity which allows the container to be more discerning about
 * allowing the revieed proxy to be used. See StatefulContaer manager for more details.
 * 
 */
public class StatelessEJBHomeHandler extends EJBHomeHandler {
    
    public StatelessEJBHomeHandler(){
    }
    
    public StatelessEJBHomeHandler(EJBMetaDataImpl ejb, ServerMetaData[] servers){
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
    
    
    /*
    * TODO:3: Get a related quote from the specification to add here
    *
    * This method is differnt the the stateful and entity behavior because we only want the 
    * stateless session bean that created the proxy to be invalidated, not all the proxies. Special case
    * for the stateless session beans.
    */
    protected Object removeWithHandle(Method method, Object[] args, Object proxy) throws Throwable{
        // Extract the primary key from the handle
        EJBObjectHandle handle = (EJBObjectHandle)args[0];
        
        // TODO:1: Check that this is exactly spec compliant
        if ( handle == null ) throw new NullPointerException("The handle is null");

        EJBObjectHandler handler = (EJBObjectHandler)handle.ejbObjectProxy.getEJBObjectHandler();
        
        // TODO:1: Check that this is exactly spec compliant
        if ( !handler.ejb.deploymentID.equals(this.ejb.deploymentID) ){
            throw new IllegalArgumentException("The handle is not from the same deployment");
        }
        handler.invalidateReference();

        return null;
    }

    protected EJBObjectHandler newEJBObjectHandler() {
        return new StatelessEJBObjectHandler();
    }
    
}
