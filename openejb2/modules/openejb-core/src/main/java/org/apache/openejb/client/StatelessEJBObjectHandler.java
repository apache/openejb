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


/**
 * This InvocationHandler and its proxy are serializable and can be used by
 * HomeHandle, Handle, and MetaData to persist and revive handles. It maintains
 * its original client identity which allows the container to be more discerning about
 * allowing the revieed proxy to be used. See StatefulContaer manager for more details.
 * 
 */
public class StatelessEJBObjectHandler extends EJBObjectHandler {
    
    // TODO:2: Registry hashtable should be moved here.

    public Object registryId;      
                     
    public StatelessEJBObjectHandler(){
    }
    
    public StatelessEJBObjectHandler(EJBMetaDataImpl ejb, ServerMetaData[] servers){
        super(ejb, servers);
    }
    
    public StatelessEJBObjectHandler(EJBMetaDataImpl ejb, ServerMetaData[] servers, Object primaryKey){
        super(ejb, servers, primaryKey);
    }
    
    // This should only be created at the server side and should not reference Container
    public static Object createRegistryId(Object primKey, Object deployId, String containerID){
        return "" + deployId + containerID;
    }
    
    public Object getRegistryId(){
//      if(registryId== null)
//          registryId= createRegistryId(primaryKey, deploymentID, container);
//      return registryId;
        return null;
    }
    
    
    /**
     * <B>5.8.3 getPrimaryKey()</B>
     * <P>
     * The object identifier of a session object is, in general, opaque
     * to the client. The result of getPrimaryKey() on a session EJBObject
     * reference results in java.rmi.RemoteException.
     * </P>
     * 
     * @param method
     * @param args
     * @param proxy
     * @return 
     * @exception Throwable
     */
    protected Object getPrimaryKey(Method method, Object[] args, Object proxy) throws Throwable{
        throw new RemoteException("Session objects are private resources and do not have primary keys");        
    }
    
    /**
     * <B>5.8.2 Stateless session beans</B>
     * <P>
     * All session objects of the same stateless session bean within
     * the same home have the same object identity, which is assigned
     * by the container. If a stateless session bean is deployed 
     * multiple times (each deployment results in the creation of a 
     * distinct home), session objects from different homes will have a
     * different identity.
     * </P>
     * <P>
     * The isIdentical(EJBObject otherEJBObject) method always returns
     * true when used to compare object references of two session 
     * objects of the same stateless session bean. The following example
     * illustrates the use of the isIdentical method for a stateless 
     * session object.
     * </P>
     * <PRE>
     * FooHome fooHome = ...; // obtain home of a stateless session bean
     * Foo foo1 = fooHome.create();
     * Foo foo2 = fooHome.create();
     * if (foo1.isIdentical(foo1)) {// this test returns true
     * ...
     * }
     * if (foo1.isIdentical(foo2)) {// this test returns true
     * ...
     * }
     * </PRE>
     * 
     * @param method
     * @param args
     * @param proxy
     * @return 
     * @exception Throwable
     */
    protected Object isIdentical(Method method, Object[] args, Object proxy) throws Throwable{
        // TODO:3: Check Authorization to Invoke method.
        Object arg = ( args.length == 1 )? args[0]: null;
        
        if ( arg == null || !(arg instanceof EJBObjectProxy) ) return Boolean.FALSE;
        EJBObjectProxy proxy2 = (EJBObjectProxy)arg;
        EJBObjectHandler that = proxy2.getEJBObjectHandler();
        return new Boolean( this.ejb.deploymentID.equals(that.ejb.deploymentID) );
    }

    protected Object remove(Method method, Object[] args, Object proxy) throws Throwable{
//      checkAuthorization(method);
        invalidateReference();
        return null;
    }
    
}
