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
