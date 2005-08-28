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
import java.lang.reflect.Method;
import java.rmi.RemoteException;

import javax.ejb.EJBHome;
import javax.ejb.EJBException;

import org.openejb.ProxyInfo;
import org.openejb.RpcContainer;
import org.openejb.core.ThreadContext;
import org.openejb.util.proxy.ProxyManager;

/**
 * This is an InvocationHandler that is used only for handling requests from an
 * EJBHome stub.  The EjbHomeProxyHandler handles all in-VM requests from the EJBHome stub.
 * The EjbHomeProxyHandler is different from the EjbObjectProxyHandler in that it does not need to be synchronized.
 * One instance of the EjbHomeProxyHandler can be used by all instances of the EJBObject stub in the
 * same VM as the bean deployment they represent.
 * This InvocationHandler and its proxy are serializable and can be used by
 * HomeHandle, Handle, and MetaData to persist and revive handles to the EJBHome. It maintains
 * its original client identity which allows the container to be more discerning about
 * allowing the revieed proxy to be used. See StatefulContainer manager for more details.
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 * @see org.openejb.core.ivm.EjbObjectProxyHandler
 * @see org.openejb.core.stateful.StatefulContainer
 */
public abstract class EjbHomeProxyHandler extends BaseEjbProxyHandler {
    protected final static org.apache.log4j.Category logger = org.apache.log4j.Category.getInstance("OpenEJB");

    static final java.util.HashMap dispatchTable;

    // this table helps dispatching in constant time, instead of many expensive equals() calls
    static {
        dispatchTable = new java.util.HashMap();
        dispatchTable.put("create", new Integer(1));
        dispatchTable.put("getEJBMetaData", new Integer(2));
        dispatchTable.put("getHomeHandle", new Integer(3));
        dispatchTable.put("remove", new Integer(4));
    }
    
    /**
     * Constructs an EjbHomeProxyHandler to handle invocations from an EJBHome stub/proxy.  
     * 
     * @param container The Container that the bean deployment this stub hanlder represents is deployed in.
     * @param pk        The primary key of the bean deployment or null if the deployment is a bean type that doesn't require a primary key.
     * @param depID     The unique id of the bean deployment that this stub handler will represent.
     */
    public EjbHomeProxyHandler(RpcContainer container, Object pk, Object depID) {
        super(container, pk, depID);
    }

    public void invalidateReference(){
        throw new IllegalStateException("A home reference must never be invalidated!");
    }
    
    protected Object createProxy(ProxyInfo proxyInfo){
        
        if (proxyInfo instanceof SpecialProxyInfo) {
            Object proxy = ((SpecialProxyInfo)proxyInfo).getProxy();
            if (proxy == null) throw new RuntimeException("Could not create IVM proxy for "+proxyInfo.getInterface()+" interface");
            return proxy;
        }
        
        Object newProxy = null;
        try {
            EjbObjectProxyHandler handler = newEjbObjectHandler(proxyInfo.getBeanContainer(), proxyInfo.getPrimaryKey(), proxyInfo.getDeploymentInfo().getDeploymentID());
            handler.setLocal(isLocal());
            handler.doIntraVmCopy = this.doIntraVmCopy;
            Class[] interfaces = new Class[]{ proxyInfo.getInterface(), IntraVmProxy.class };
            newProxy = ProxyManager.newProxyInstance( interfaces , handler );
        } catch (IllegalAccessException iae) {
            throw new RuntimeException("Could not create IVM proxy for "+proxyInfo.getInterface()+" interface");
        }
        if (newProxy == null) throw new RuntimeException("Could not create IVM proxy for "+proxyInfo.getInterface()+" interface");
    
        return newProxy;
    }

    protected abstract EjbObjectProxyHandler newEjbObjectHandler(RpcContainer container, Object pk, Object depID);
    
    protected Object _invoke(Object proxy, Method method, Object[] args) throws Throwable{

        if (logger.isInfoEnabled()) {
            logger.info("invoking method "+method.getName()+" on "+deploymentID);
        }
        
        String methodName = method.getName();
        
        try{
            java.lang.Object retValue;
            Integer operation = (Integer)dispatchTable.get(methodName);

            if(operation==null) {
                if ( methodName.startsWith("find") ){
                    retValue = findX(method, args, proxy);
                } else {
                    // Cannot return null.  Must throw and exception instead.
                    throw new UnsupportedOperationException("Unkown method: "+method);
                }
            }else {
                switch(operation.intValue()) {
        /*-- CREATE ------------- <HomeInterface>.create(<x>) ---*/
                    case 1: retValue = create(method, args, proxy); break;
        /*-- GET EJB METADATA ------ EJBHome.getEJBMetaData() ---*/
                    case 2: retValue = getEJBMetaData(method, args, proxy); break;
        /*-- GET HOME HANDLE -------- EJBHome.getHomeHandle() ---*/
                    case 3: retValue = getHomeHandle(method, args, proxy); break;
        /*-- REMOVE ------------------------ EJBHome.remove() ---*/
                    case 4: {
                        Class type = method.getParameterTypes()[0];

            /*-- HANDLE ------- EJBHome.remove(Handle handle) ---*/
                        if (javax.ejb.Handle.class.isAssignableFrom(type)) {
                            retValue = removeWithHandle(method, args, proxy);
            } else {
                        /*-- PRIMARY KEY ----- EJBHome.remove(Object key) ---*/
                            retValue = removeByPrimaryKey(method, args, proxy);
                        }
                        break;
                    }
                    default:
                        throw new RuntimeException("Inconsistent internal state: value "+operation.intValue()+" for operation "+methodName);
                }
            }

            if(logger.isDebugEnabled()) {
                logger.debug("finished invoking method "+method.getName()+". Return value:"+retValue);
            } else if (logger.isInfoEnabled()) {
                logger.info("finished invoking method "+method.getName());
            }
            
            return retValue;

        /*
         * The ire is thrown by the container system and propagated by
         * the server to the stub.
         */
        } catch (RemoteException re) {
            if (isLocal()){
                throw new EJBException(re.getMessage(),(Exception)re.detail);
            } else {
                throw re;
            }

        } catch ( org.openejb.InvalidateReferenceException ire ) {
            Throwable cause = ire.getRootCause();
            if (cause instanceof RemoteException && isLocal()){
                RemoteException re = (RemoteException)cause;
                Throwable detail = (re.detail !=  null)? re.detail: re;
                cause = new EJBException(re.getMessage(), (Exception) detail);
            }
            throw cause;
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
            if (isLocal()){
                throw new EJBException("Container has suffered a SystemException", (Exception)se.getRootCause());
            } else {
                throw new RemoteException("Container has suffered a SystemException",se.getRootCause());
            }
        } catch ( org.openejb.OpenEJBException oe ) {
            if (isLocal()){
                throw new EJBException("Unknown Container Exception", (Exception)oe.getRootCause());
            } else {
                throw new RemoteException("Unknown Container Exception",oe.getRootCause());
            }
        } catch(Throwable t) {
            logger.info("finished invoking method "+method.getName()+" with exception:"+t, t);
            throw t;
        }
    }

    /*-------------------------------------------------*/
    /*  Home interface methods                         */  
    /*-------------------------------------------------*/
    
    /**
     * <P>
     * Creates a new EJBObject and returns it to the 
     * caller.  The EJBObject is a new proxy with a 
     * new handler. This implementation should not be
     * sent outside the virtual machine.
     * </P>
     * <P>
     * This method propogates to the container
     * system.
     * </P>
     * <P>
     * The create method is required to be defined
     * by the bean's home interface.
     * </P>
     * 
     * @param method
     * @param args
     * @param proxy
     * @return Returns an new EJBObject proxy and handler
     * @exception Throwable
     */
    protected Object create(Method method, Object[] args, Object proxy) throws Throwable{
        ProxyInfo proxyInfo = (ProxyInfo) container.invoke(deploymentID,method,args,null, getThreadSpecificSecurityIdentity());
        return createProxy(proxyInfo);
    }

    /**
     * <P>
     * Locates and returns a new EJBObject or a collection
     * of EJBObjects.  The EJBObject(s) is a new proxy with
     * a new handler. This implementation should not be
     * sent outside the virtual machine.
     * </P>
     * <P>
     * This method propogates to the container
     * system.
     * </P>
     * <P>
     * The find method is required to be defined
     * by the bean's home interface of Entity beans.
     * </P>
     * 
     * @param method
     * @param args
     * @param proxy
     * @return Returns an new EJBObject proxy and handler
     * @exception Throwable
     */
    protected abstract Object findX(Method method, Object[] args, Object proxy) throws Throwable;

    /*-------------------------------------------------*/
    /*  EJBHome methods                                */  
    /*-------------------------------------------------*/

    /**
     * <P>
     * Returns an EJBMetaData implementation that is
     * valid inside this virtual machine.  This
     * implementation should not be sent outside the
     * virtual machine.
     * </P>
     * <P>
     * This method does not propogate to the container
     * system.
     * </P>
     * <P>
     * getMetaData is a method of javax.ejb.EJBHome
     * </P>
     * <P>
     * Checks if the caller is authorized to invoke the
     * javax.ejb.EJBHome.getMetaData on the EJBHome of the
     * deployment.
     * </P>
     * 
     * @return Returns an IntraVmMetaData
     * @exception Throwable
     * @see IntraVmMetaData
     * @see javax.ejb.EJBHome
     * @see javax.ejb.EJBHome#getEJBMetaData
     */
    protected Object getEJBMetaData(Method method, Object[] args, Object proxy) throws Throwable {
        checkAuthorization(method);
        IntraVmMetaData metaData = new IntraVmMetaData(deploymentInfo.getHomeInterface(), deploymentInfo.getRemoteInterface(),deploymentInfo.getPrimaryKeyClass(), deploymentInfo.getComponentType());
        metaData.setEJBHome((EJBHome)proxy);
        return metaData;
    }
    

    /**
     * <P>
     * Returns a HomeHandle implementation that is
     * valid inside this virtual machine.  This
     * implementation should not be sent outside the
     * virtual machine.
     * </P>
     * <P>
     * This method does not propogate to the container
     * system.
     * </P>
     * <P>
     * getHomeHandle is a method of javax.ejb.EJBHome
     * </P>
     * <P>
     * Checks if the caller is authorized to invoke the
     * javax.ejb.EJBHome.getHomeHandle on the EJBHome of the
     * deployment.
     * </P>
     * 
     * @param proxy
     * @return Returns an IntraVmHandle
     * @exception Throwable
     * @see IntraVmHandle
     * @see javax.ejb.EJBHome
     * @see javax.ejb.EJBHome#getHomeHandle
     */
    protected Object getHomeHandle(Method method, Object[] args, Object proxy) throws Throwable{
        checkAuthorization(method);
        return new IntraVmHandle(proxy);
    }
    public org.openejb.ProxyInfo getProxyInfo(){
        return new org.openejb.ProxyInfo(deploymentInfo, null, deploymentInfo.getHomeInterface(), container);
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
     * @return Object 
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
            return org.openejb.OpenEJB.getApplicationServer().getEJBHome(this.getProxyInfo());
        }
    }
    
    /**
     * <P>
     * Attempts to remove an EJBObject from the
     * container system.  The EJBObject to be removed
     * is represented by the javax.ejb.Handle object passed
     * into the remove method in the EJBHome.
     * </P>
     * <P>
     * This method propogates to the container system.
     * </P>
     * <P>
     * remove(Handle handle) is a method of javax.ejb.EJBHome
     * </P>
     * <P>
     * Checks if the caller is authorized to invoke the
     * javax.ejb.EJBHome.remove on the EJBHome of the
     * deployment.
     * </P>
     * 
     * TODO: this method relies on the fact that the handle implementation is a subclass
     * of IntraVM handle, which isn't neccessarily the case for arbitrary remote protocols.
     * Also, for all other but IntraVM handles, the stub invalidation doesn't currently work.
     *
     * @param method
     * @param args
     * @return Returns null
     * @exception Throwable
     * @see javax.ejb.EJBHome
     * @see javax.ejb.EJBHome#remove(javax.ejb.Handle)
     */
    protected Object removeWithHandle(Method method, Object[] args, Object proxy) throws Throwable{

        // Extract the primary key from the handle
        IntraVmHandle handle = (IntraVmHandle)args[0];
        Object primKey = handle.getPrimaryKey();
        EjbObjectProxyHandler stub;
        try{
            stub = (EjbObjectProxyHandler)ProxyManager.getInvocationHandler(handle.getEJBObject());
        }catch(IllegalArgumentException e) {
            // a remote proxy, see comment above
            stub=null;
        }
        // invoke remove() on the container
        container.invoke(deploymentID, method, args, primKey, ThreadContext.getThreadContext().getSecurityIdentity());

        /*
         * This operation takes care of invalidating all the EjbObjectProxyHanders associated with
         * the same RegistryId. See this.createProxy().
         */
        if(stub!=null) {
            invalidateAllHandlers(stub.getRegistryId());
        }
        return null;
    }
    
    /**
     * <P>
     * Attempts to remove an EJBObject from the
     * container system.  The EJBObject to be removed
     * is represented by the primaryKey passed
     * into the remove method of the EJBHome.
     * </P>
     * <P>
     * This method propogates to the container system.
     * </P>
     * <P>
     * remove(Object primary) is a method of javax.ejb.EJBHome
     * </P>
     * <P>
     * Checks if the caller is authorized to invoke the
     * javax.ejb.EJBHome.remove on the EJBHome of the
     * deployment.
     * </P>
     * 
     * @param method
     * @param args
     * @return Returns null
     * @exception Throwable
     * @see javax.ejb.EJBHome
     * @see javax.ejb.EJBHome#remove(javax.ejb.Handle)
     */
    protected abstract Object removeByPrimaryKey(Method method, Object[] args, Object proxy) throws Throwable;
}
