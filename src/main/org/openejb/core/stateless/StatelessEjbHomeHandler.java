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
package org.openejb.core.stateless;

import java.lang.reflect.Method;
import java.rmi.RemoteException;

import org.openejb.RpcContainer;
import org.openejb.core.ThreadContext;
import org.openejb.core.ivm.EjbHomeProxyHandler;
import org.openejb.core.ivm.EjbObjectProxyHandler;
import org.openejb.core.ivm.IntraVmHandle;
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
public class StatelessEjbHomeHandler extends EjbHomeProxyHandler {
    
    public StatelessEjbHomeHandler(RpcContainer container, Object pk, Object depID){
        super(container, pk, depID);
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
     * ------------------------------------
     * 5.3.2 Removing a session object
     * A client may remove a session object using the remove() method on the javax.ejb.EJBObject
     * interface, or the remove(Handle handle) method of the javax.ejb.EJBHome interface.
     * 
     * Because session objects do not have primary keys that are accessible to clients, invoking the
     * javax.ejb.EJBHome.remove(Object primaryKey) method on a session results in the
     * javax.ejb.RemoveException.
     * 
     * ------------------------------------
     * 5.5 Session object identity
     * 
     * Session objects are intended to be private resources used only by the
     * client that created them. For this reason, session objects, from the
     * client’s perspective, appear anonymous. In contrast to entity objects,
     * which expose their identity as a primary key, session objects hide their
     * identity. As a result, the EJBObject.getPrimaryKey() and
     * EJBHome.remove(Object primaryKey) methods result in a java.rmi.RemoteException
     * if called on a session bean. If the EJBMetaData.getPrimaryKeyClass()
     * method is invoked on a EJBMetaData object for a Session bean, the method throws
     * the java.lang.RuntimeException.
     * ------------------------------------
     * 
     * Sections 5.3.2 and 5.5 conflict.  5.3.2 says to throw javax.ejb.RemoveException, 5.5 says to
     * throw java.rmi.RemoteException.
     * 
     * For now, we are going with java.rmi.RemoteException.
     */
    protected Object removeByPrimaryKey(Method method, Object[] args, Object proxy) throws Throwable {
        throw new RemoteException("Session objects are private resources and do not have primary keys");        
    }
    /*
    * This method is different from the stateful and entity behavior because we only want the 
    * stateless session bean that created the proxy to be invalidated, not all the proxies.
    *
    * TODO: this method relies on the fact that the handle implementation is a subclass
    * of IntraVM handle, which isn't neccessarily the case for arbitrary remote protocols.
    */
    protected Object removeWithHandle(Method method, Object[] args, Object proxy) throws Throwable{

        // Extract the primary key from the handle
        IntraVmHandle handle = (IntraVmHandle)args[0];
        Object primKey = handle.getPrimaryKey();
        EjbObjectProxyHandler stub;
        try{
            stub = (EjbObjectProxyHandler)ProxyManager.getInvocationHandler(handle.getEJBObject());
        }catch(IllegalArgumentException e) {
            // a remote proxy
            stub=null;
        }
        // invoke the remove on the container
        container.invoke(deploymentID, method, args, primKey, ThreadContext.getThreadContext().getSecurityIdentity());
        if(stub!=null) {
        stub.invalidateReference();
        }
        return null;
    }
    
    protected EjbObjectProxyHandler newEjbObjectHandler(RpcContainer container, Object pk, Object depID) {
        return new StatelessEjbObjectHandler(container, pk, depID);
    }
    
}
