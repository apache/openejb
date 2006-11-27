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
 * 
 * @since 11/25/2001
 */
public class StatefulEJBObjectHandler extends EJBObjectHandler {

    public StatefulEJBObjectHandler() {
    }

    public StatefulEJBObjectHandler(EJBMetaDataImpl ejb, ServerMetaData[] servers){
        super(ejb, servers);
    }
    
    public StatefulEJBObjectHandler(EJBMetaDataImpl ejb, ServerMetaData[] servers, Object primaryKey){
        super(ejb, servers, primaryKey);
        registerHandler( primaryKey, this );
    }
    
    public Object getRegistryId() {
        return primaryKey;
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
     * <B><P>5.8.2 Stateless session beans</P></B>
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
        if ( args[0] == null ) return Boolean.FALSE;

        EJBObjectProxy ejbObject = (EJBObjectProxy)args[0];
        EJBObjectHandler that = ejbObject.getEJBObjectHandler();

        return new Boolean(this.primaryKey.equals(that.primaryKey));
    }

    protected Object remove(Method method, Object[] args, Object proxy) throws Throwable{
        EJBRequest req = new EJBRequest( EJB_OBJECT_REMOVE ); 
        req.setClientIdentity( ContextManager.getThreadPrincipal() );
        req.setContainerCode( ejb.deploymentCode );
        req.setContainerID(   ejb.deploymentID );
        req.setMethodInstance( method );
        req.setMethodParameters( args );
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
