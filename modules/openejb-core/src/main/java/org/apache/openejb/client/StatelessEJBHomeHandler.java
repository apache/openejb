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
