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


package org.openejb.core.stateful;


import java.lang.reflect.Method;
import java.rmi.RemoteException;

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
public class StatefulEjbObjectHandler extends EjbObjectProxyHandler {
                     
    public StatefulEjbObjectHandler(RpcContainer container, Object pk, Object depID){
        super(container, pk, depID);
    }
    
    public Object getRegistryId(){
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
        checkAuthorization(method);
        EjbObjectProxyHandler handler = (EjbObjectProxyHandler)ProxyManager.getInvocationHandler(proxy);
        return new Boolean( primaryKey.equals(handler.primaryKey) );
    }
    
    protected Object remove(Method method, Object[] args, Object proxy) throws Throwable{
        checkAuthorization(method);
        Object value = container.invoke(deploymentID, method, args, primaryKey, getThreadSpecificSecurityIdentity());
        // invalidates all the handler's associated with the same bean identity
        invalidateAllHandlers(getRegistryId());
        return value;
    }
    
}
