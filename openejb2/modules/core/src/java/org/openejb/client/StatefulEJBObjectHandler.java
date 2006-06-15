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
 * $Id$
 */
package org.openejb.client;

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

    public StatefulEJBObjectHandler(EJBMetaDataImpl ejb, ServerMetaData server){
        super(ejb, server);
    }
    
    public StatefulEJBObjectHandler(EJBMetaDataImpl ejb, ServerMetaData server, Object primaryKey){
        super(ejb, server, primaryKey);
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
