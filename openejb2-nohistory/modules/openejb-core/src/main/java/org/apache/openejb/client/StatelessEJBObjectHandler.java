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
 *    please contact openejb-group@openejb.sf.net.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.sf.net/).
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
 * $Id: StatelessEJBObjectHandler.java 445853 2005-12-21 14:21:56Z gdamour $
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
