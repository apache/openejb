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
package org.openejb.core.ivm;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.security.Principal;
import java.util.Hashtable;

import javax.ejb.EJBHome;

import org.openejb.DeploymentInfo;
import org.openejb.InvalidateReferenceException;
import org.openejb.OpenEJB;
import org.openejb.OpenEJBException;
import org.openejb.ProxyInfo;
import org.openejb.RpcContainer;
import org.openejb.core.ThreadContext;
import org.openejb.util.proxy.InvalidatedReferenceHandler;
import org.openejb.util.proxy.InvocationHandler;
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
public abstract class EjbObjectProxyHandler extends BaseEjbProxyHandler {
                     
    public EjbObjectProxyHandler(RpcContainer container, Object pk, Object depID){
        super(container, pk, depID);
    }
    
    /**
    * The Registry id is a logical identifier that is used as a key when placing EjbObjectProxyHanlders into
    * the BaseEjbProxyHanlder's liveHandleRegistry.  EjbObjectProxyHanlders that represent the same
    * bean identity (keyed by the registry id) will be stored together so that they can be removed together
    * when the BaseEjbProxyHandler.invalidateAllHandlers is invoked.
    *
    * This method is implemented by the subclasses to return an id that logically identifies
    * bean identity for a specific deployment id and container.  For example, the EntityEjbObjectHandler
    * overrides this method to return a compound key composed of the bean's primary key, deployment id, and
    * container id.  This uniquely identifies the bean identity that is proxied by this handler.  Another example
    * is the StatefulEjbObjectHanlder which overrides this method to return the stateful bean's hidden primary key,
    * which is a java.rmi.dgc.VMID. 
    */
    public abstract Object getRegistryId();

    // The EJBObject stub is synchronized to prevent multiple client threads from accessing the
    // stub concurrently.  This is required by session beans (6.5.6 Serializing session bean methods) but
    // not required by entity beans. Implementing synchronization on the stub prohibits multiple threads from currently
    // invoking methods on the same stub, but doesn't prohibit a client from having multiple references to the same
    // entity identity. Synchronizing the stub is a simple and elegant solution to a difficult problem.
    //
    public synchronized Object _invoke(Object p, Method m, Object[] a) throws Throwable{

        Object retValue = null;
        /*
         * This section is to be replaced by a more appropriate solution.
         * This code is very temporary.
         */
        
        try{

            String method = m.getName();
            
            if(method.equals("getHandle"))          retValue = getHandle(m,a,p);
            else if(method.equals("getPrimaryKey")) retValue = getPrimaryKey(m,a,p);
            else if(method.equals("isIdentical"))   retValue = isIdentical(m,a,p);
            else if(method.equals("getEJBHome"))    retValue = getEJBHome(m,a,p);
            else if(method.equals("remove"))        retValue = remove(m,a,p);
            else                                    retValue = businessMethod(m,a,p);
            
            /*
            * Business methods that return EJBHome or EJBObject references to local
            * beans (beans in the same container system) must have the return value
            * converted to a ProxyInfo object, so that the server can provide the client
            * with a proper remote reference.  Local remote references are implemented using
            * the org.openejb.core.ivm.BaseEjbProxyHandler types, which should not be returned
            * to the client.  Non-local remote references are assumed to be serializable and valid
            * return types for the clients.
            *
            * If the reference is a local remote reference a subtype of ProxyInfo is returned. The subtype
            * is a org.openejb.core.ivm.SpecialProxyInfo. This class type is useful when the calling server
            * is the IntraVM server.  Instead of creating a new remote ref from the proxy the IntraVM takes
            * a short cut and reuses the original local remote reference -- they are thread safe with no synchronization.
            *
            * See Section 2.2.1.2.5 Remote References of the OpenEJB specification.
            */
                
            if(retValue instanceof SpecialProxyInfo)
                retValue = ((SpecialProxyInfo)retValue).getProxy();
        
        /*
         * The ire is thrown by the container system and propagated by
         * the server to the stub.
         */
        }catch ( org.openejb.InvalidateReferenceException ire ) {
            invalidateAllHandlers(getRegistryId());
            throw ire.getRootCause();
        /*
         * Application exceptions must be reported dirctly to the client. They
         * do not impact the viability of the proxy.
         */
        } catch ( org.openejb.ApplicationException ae ) {
            throw ae.getRootCause();
        /*
         * A system exception would be highly unusual and would indicate a sever
         * problem with the container system.
         */
        } catch ( org.openejb.SystemException se ) {
            invalidateReference();
            throw new RemoteException("Container has suffered a SystemException",se.getRootCause());
        } catch ( org.openejb.OpenEJBException oe ) {
            throw new RemoteException("Unknown Container Exception",oe.getRootCause());
        }  
        return retValue;
    }
    

    protected Object getEJBHome(Method method, Object[] args, Object proxy) throws Throwable{
        checkAuthorization(method);
        return deploymentInfo.getEJBHome();
    }
    
    protected Object getHandle(Method method, Object[] args, Object proxy) throws Throwable{
        checkAuthorization(method);
        return new IntraVmHandle(proxy);
    }
    public org.openejb.ProxyInfo getProxyInfo(){
        return new org.openejb.ProxyInfo(deploymentInfo, primaryKey, deploymentInfo.getRemoteInterface(), container);
    }
    
    /**
     * The writeReplace method is invoked on the proxy when it enters the 
     * serialization process.  The call is passed to the handler, then delegated 
     * to this method.
     * 
     * If the proxy is being copied between bean instances in a RPC
     * call we use the IntraVmArtifact.  This object is immutable and does not 
     * need to be dereferenced; therefore, we have no need to actually serialize
     * this object.
     * 
     * If the proxy is referenced by a stateful bean that is being
     * passivated by the container we allow this object to be serialized.
     * 
     * If the proxy is being serialized in any other context, we know that its
     * destination is outside the core container system.  This is the 
     * responsibility of the Application Server and one of its proxies is 
     * serialized to the stream in place of the IntraVmProxy.
     * 
     * @param proxy
     * @return 
     * @exception ObjectStreamException
     */
    protected Object _writeReplace(Object proxy) throws ObjectStreamException{
        /*
         * If the proxy is being  copied between bean instances in a RPC
         * call we use the IntraVmArtifact
         */
        if(IntraVmCopyMonitor.isIntraVmCopyOperation()){
            return new IntraVmArtifact( proxy );
        /* 
         * If the proxy is referenced by a stateful bean that is  being
         * passivated by the container we allow this object to be serialized.
         */
        }else if(IntraVmCopyMonitor.isStatefulPassivationOperation()){
            return proxy;
        /*
         * If the proxy is serialized outside the core container system,
         * we allow the application server to handle it.
         */
        } else{
            return org.openejb.OpenEJB.getApplicationServer().getEJBObject(this.getProxyInfo());
        }
    }
    
    protected abstract Object getPrimaryKey(Method method, Object[] args, Object proxy) throws Throwable;
    
    protected abstract Object isIdentical(Method method, Object[] args, Object proxy) throws Throwable;

    protected abstract Object remove(Method method, Object[] args, Object proxy) throws Throwable;
    
    protected Object businessMethod(Method method, Object[] args, Object proxy) throws Throwable{
        checkAuthorization(method);
        return container.invoke(deploymentID, method, args, primaryKey, getThreadSpecificSecurityIdentity());
    }
}
