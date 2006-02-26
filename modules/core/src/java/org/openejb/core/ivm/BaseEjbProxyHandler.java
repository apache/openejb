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

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.rmi.MarshalledObject;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

import javax.ejb.EJBException;

import org.openejb.OpenEJB;
import org.openejb.RpcContainer;
import org.openejb.core.DeploymentInfo;
import org.openejb.core.ThreadContext;
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
public abstract class BaseEjbProxyHandler implements InvocationHandler, Serializable  {
    /**
    * keeps track of live BaseEjbProxyHanlders in this VM. So that related handlers can be removed
    * if necessary.
    *
    * Currently this registry is only used to track live EjbObjectProxyHandlers. The EjbObjectProxyHandlers
    * are tracked to allow remove() operations and invalidate exceptions to be propagated to the proper 
    * handler instances. 
    * 
    * There are several scenarios where this is useful:
    * <ul>
    * <li>
    * If an EJBHome.remove( )method is invoked, the EjbHomeProxyHandler will use 
    * this registry to notify the EjbObjectProxyHandler associated with the removed identity
    * that the EjbObjectProxyHandler should invalidate itself. 
    * <li>
    * When two EjbObjectProxy handlers are both associated with the same bean identity and one is 
    * removed, the EjbObjectProxyHandler executing the remove will notify the other EjbObjectProxyHandler 
    * objects associated with the same identity that they are to be invalidated.
    * <li>
    * When an EjbObjectProxyHanlder performs an operation that results in a an InvalidateReferenceException
    * The EjbObjectProxyHandler will use the registry to ensure that all EjbObjectProxyHandlers associated
    * the identity are invalidated.
    */
    protected static final Hashtable liveHandleRegistry = new Hashtable();

    /**
     * The unique id of the bean deployment that this stub handler represents.
     */
    public final Object deploymentID;

    /**
     * The primary key of the bean deployment or null if the deployment is a bean type that doesn't require a primary key
     */
    public final Object primaryKey;

    /**
     */
    public boolean inProxyMap = false;

    /**
     * The DeployemtnInfo object if the bean deployment that this stub handler represents.
     */
    public transient DeploymentInfo deploymentInfo;

    /**
     * The RpcContainer that the bean deployment this stub hanlder represents is deployed in.
     */
    public transient RpcContainer container;

    protected boolean isInvalidReference = false;
    
    /*
    * The EJB 1.1 specification requires that arguments and return values between beans adhere to the
    * Java RMI copy semantics which requires that the all arguments be passed by value (copied) and 
    * never passed as references.  However, it is possible for the system administrator to turn off the
    * copy operation so that arguments and return values are passed by reference as performance optimization.
    * Simply setting the org.openejb.core.EnvProps.INTRA_VM_COPY property to FALSE will cause this variable to 
    * set to false, and therefor bypass the copy operations in the invoke( ) method of this class; arguments
    * and return values will be passed by reference not value. 
    *
    * This property is, by default, always TRUE but it can be changed to FALSE by setting it as a System property
    * or a property of the Property argument when invoking OpenEJB.init(props).  This variable is set to that
    * property in the static block for this class.
    */
    protected boolean doIntraVmCopy;
    private boolean isLocal;

    /**
     * Constructs a BaseEjbProxyHandler representing the specifed bean deployment.
     *
     * @param container The Container that the bean deployment this stub hanlder represents is deployed in.
     * @param pk        The primary key of the bean deployment or null if the deployment is a bean type that doesn't require a primary key.
     * @param depID     The unique id of the bean deployment that this stub handler will represent.
     */
    public BaseEjbProxyHandler(RpcContainer container, Object pk, Object depID){
       this.container = container;
       this.primaryKey = pk;
       this.deploymentID = depID;
       this.deploymentInfo = (org.openejb.core.DeploymentInfo)container.getDeploymentInfo(depID);
       
       String value = org.openejb.OpenEJB.getInitProps().getProperty("openejb.localcopy");
       if ( value == null ) {
           value = org.openejb.OpenEJB.getInitProps().getProperty(org.openejb.core.EnvProps.INTRA_VM_COPY);
       }
       if(value == null){
           value = System.getProperty("openejb.localcopy");
       }
       if(value == null){
           value = System.getProperty(org.openejb.core.EnvProps.INTRA_VM_COPY);
       }
       doIntraVmCopy = value==null || !value.equalsIgnoreCase("FALSE");
    }
    /**
     * Invoked by the ObjectInputStream during desrialization of this stub handler.  In this method the stub handler resets the deploymentInfo and container member variables.
     *
     * @param in     The ObjectInputStream that has called this callback method and is deserializing this stub handler.
     * @exception java.io.IOException
     *                   If there is a problem reading this object from the InputStream
     * @exception ClassNotFoundException
     *                   If the class definition of an object refernced by this stub handler cannot be found.
     * @exception NoSuchMethodException
     */
    private void readObject(java.io.ObjectInputStream in)
    throws java.io.IOException,ClassNotFoundException, NoSuchMethodException{

        in.defaultReadObject();

        deploymentInfo = (org.openejb.core.DeploymentInfo)OpenEJB.getDeploymentInfo(deploymentID);
        container = (RpcContainer)deploymentInfo.getContainer();
    }
    /**
     * Checks if the caller of the specified method is authorized to access and execute it.
     *
     * Relies on the SecurityService assigned to the Container that this bean deployment is in to determine if the caller is authorized.
     *
     * @param method The method the caller is attempting to execute.
     * @exception org.openejb.OpenEJBException
     *                   If the caller does bot have adequate authorization to execute the specified method.
     */
    protected void checkAuthorization(Method method) throws org.openejb.OpenEJBException{
        Object caller = getThreadSpecificSecurityIdentity();
        boolean authorized = OpenEJB.getSecurityService().isCallerAuthorized(caller, deploymentInfo.getAuthorizedRoles(method));
        if(!authorized)
            throw new org.openejb.ApplicationException(new RemoteException("Unauthorized Access by Principal Denied"));
    }

    protected Object getThreadSpecificSecurityIdentity(){
        ThreadContext context = ThreadContext.getThreadContext();
        if(context.valid()){
            return context.getSecurityIdentity();
        }else{
            return OpenEJB.getSecurityService().getSecurityIdentity();
        }
    }

    /**
      * This method enables/disables the copy process of the arguments and return value to and from
     * a regular EJB invocation. In some cases it is desireable to skip the copy, e.g. when the
     * invocation comes from an RMI or CORBA remote layer, where the arguemnts are already copies.
     */
    public void setIntraVmCopyMode(boolean on) {
        doIntraVmCopy=on;
    }
        
    /**
     * Preserves the context of the current thread and passes the invoke on to the BaseEjbProxyHandler subclass where the Container will be asked to invoke the method on the bean.
     *
     * When entering a container the ThreadContext will change to match the context of
     * the bean being serviced. That changes the current context of the calling bean,
     * so the context must be preserved and then resourced after request is serviced.
     * The context is restored after the subclass' _invoke method returns.
     *
     * @param proxy  The Proxy object that represents this bean deployment's EJBObject or EJBHome.
     * @param method The EJBHome or EJBObject method the caller is invoking.
     * @param args   The parameters to the mehtod being invoked
     * @return The result of invoking the appropriate method on the bean instance.
     * @exception Throwable
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable{
        if (isInvalidReference) throw new NoSuchObjectException("reference is invalid");


        if (method.getDeclaringClass() == Object.class ) {
            final String methodName = method.getName();

            if ( methodName.equals( "toString" ))       return toString();
            else if (methodName.equals( "equals" ))     return equals(args[0])?Boolean.TRUE: Boolean.FALSE;
            else if (methodName.equals("hashCode"))     return new Integer(hashCode());
            else throw new UnsupportedOperationException("Unkown method: "+method);
        } else if (method.getDeclaringClass() == IntraVmProxy.class ) {
            final String methodName = method.getName();

            if (methodName.equals("writeReplace"))      return _writeReplace( proxy );
            else throw new UnsupportedOperationException("Unkown method: "+method);
        } 
        /* Preserve the context
            When entering a container the ThreadContext will change to match the context of
            the bean being serviced. That changes the current context of the calling bean,
            so the context must be preserved and then resourced after request is serviced.
            The context is restored in the finnaly clause below.
            
            We could have same some typing by obtaining a ref to the ThreadContext and then
            setting the current ThreadContext to null, but this results in more object creation
            since the container will create a new context on the invoke( ) operation if the current
            context is null. Getting the context values and resetting them reduces object creation.
            It's ugly but performant.
        */
        
        ThreadContext cntext = null;
        DeploymentInfo depInfo = null;
        Object prmryKey = null;
        byte crrntOperation = (byte)0;
        Object scrtyIdentity = null;
        boolean cntextValid = false;
        cntext = ThreadContext.getThreadContext();
        if(cntext.valid()){
             depInfo = cntext.getDeploymentInfo();
             prmryKey = cntext.getPrimaryKey();
             crrntOperation = cntext.getCurrentOperation();
             scrtyIdentity = cntext.getSecurityIdentity();
             cntextValid = true;
        }

        String jndiEnc = System.getProperty(javax.naming.Context.URL_PKG_PREFIXES);
//        System.setProperty(javax.naming.Context.URL_PKG_PREFIXES,"org.openejb.core.ivm.naming");
        // the four operations on IntraVmCopyMonitor are quite expensive, because
        // all of them require a Thread.currentThread() operation, which is native code
        try{
            if(doIntraVmCopy==true){// copy arguments as required by the specification
                // demarcate the begining of the copy operation.
                if(args!=null && args.length > 0) {
                    // methods w/o arguments pass in a null value
                    IntraVmCopyMonitor.preCopyOperation();
                    args = copyArgs(args);
                    // demarcate end of copy operation
                    IntraVmCopyMonitor.postCopyOperation();
                }
                Object returnObj = _invoke(proxy,method,args);
                
                // demarcate the begining of the copy operation.
                IntraVmCopyMonitor.preCopyOperation();
                returnObj = copyObj(returnObj);
                return returnObj;                
                // postCopyOperation() is handled in try/finally clause.
            } else {
                try {
					/*
					* The EJB 1.1 specification requires that arguments and return values between beans adhere to the
					* Java RMI copy semantics which requires that the all arguments be passed by value (copied) and 
					* never passed as references.  However, it is possible for the system administrator to turn off the
					* copy operation so that arguments and return values are passed by reference as a performance optimization.
					* Simply setting the org.openejb.core.EnvProps.INTRA_VM_COPY property to FALSE will cause  
					* IntraVM to bypass the copy operations; arguments and return values will be passed by reference not value. 
					* This property is, by default, always TRUE but it can be changed to FALSE by setting it as a System property
					* or a property of the Property argument when invoking OpenEJB.init(props).  The doIntraVmCopy variable is set to that
					* property in the static block for this class.
					*/
                	
					return _invoke(proxy,method,args);
				} catch (RemoteException e) {
                    if (this.isLocal()){
                        throw new EJBException(e.getMessage()).initCause(e.getCause());
                    } else {
                        throw e;
                    }
				} catch (Throwable t) {
					t.printStackTrace();
					Class[] etypes = method.getExceptionTypes();
					for (int i = 0; i < etypes.length; i++) {

						if (etypes[i].isAssignableFrom(t.getClass())){
							throw t;
						}						
					}
					// Exception is undeclared
					// Try and find a runtime exception in there
					while (t.getCause() != null && !(t instanceof RuntimeException)){
						t = t.getCause();
					}
					throw t;
				}
            }
        } finally {
//            System.setProperty(javax.naming.Context.URL_PKG_PREFIXES, jndiEnc);
            // restore the context
            if(cntextValid){
                cntext.set(depInfo, prmryKey, scrtyIdentity);
                cntext.setCurrentOperation(crrntOperation);
            }
            if(doIntraVmCopy==true){
                // demarcate end of copy operation
                // postCopyOperation() is executed here in case of an exception
                IntraVmCopyMonitor.postCopyOperation();
            }
        }
    }

    public String toString() {
        return "proxy="+getProxyInfo().getInterface().getName()+";deployment="+this.deploymentID+";pk="+this.primaryKey;
    }
    
    public int hashCode() {
        if(primaryKey==null) {
            //stateless bean or home object
            return deploymentID.hashCode();
        }else {
            return primaryKey.hashCode();
        }
    }
    
    public boolean equals(Object obj) {
        try{
            obj = ProxyManager.getInvocationHandler(obj);
        }catch(IllegalArgumentException e) {
            return false;
        }
        BaseEjbProxyHandler other = (BaseEjbProxyHandler) obj;
        if(primaryKey==null) {
            return other.primaryKey==null && deploymentID.equals(other.deploymentID);
        } else {
            return primaryKey.equals(other.primaryKey) && deploymentID.equals(other.deploymentID);
        }
    }

    /**
     * Overridden by subclasses and called by {@link #invoke}.  Subclasses implement the main behavior of calling invoke on the Container that the bean deployment lives in.
     *
     * @param proxy  The Proxy subclass that is the bean's EJBObject or EJBHome.
     * @param method The bean method that the caller is attempting to invoke.
     * @param args   The arguments to the method being invoked.
     * @return The result of invoking the appropriate method on the bean instance.
     * @exception Throwable
     */
    protected abstract Object _invoke(Object proxy, Method method, Object[] args) throws Throwable;


    /**
     *
     * @param objects The object array you wish to dereference.
     * @return An object array with new, equlivilent instances of the original objects contained in the array.
     */

    protected Object[] copyArgs(Object[] objects) throws IOException, ClassNotFoundException{
        /* 
            while copying the arguments is necessary. Its not necessary to copy the array itself,
            because they array is created by the Proxy implementation for the sole purpose of 
            packaging the arguments for the InvocationHandler.invoke( ) method. Its ephemeral
            and their for doesn't need to be copied.
        */

        for (int i=0; i < objects.length; i++){
            objects[i] = copyObj(objects[i]);
        }

        return objects;
    }

    /**
     *
     * @param object
     * @return An equlivilent instance of the original object.
     */
    /* change dereference to copy */
    protected Object copyObj(Object object) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(128);
        ObjectOutputStream out = new ObjectOutputStream(baos);
        out.writeObject(object);
        out.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream in = new ObjectInputStream(bais);
        Object obj = in.readObject();
        return obj;
    }
    
    /**
    * Invalidates this reference so that it can not be used as a proxy for the bean identity. This 
    * method may be called when an InvalidateReferenceException is thrown by the container or when 
    * the bean identity associated with this proxy is explicitly removed, by calling one of the remove( )
    * methods.
    */
    public void invalidateReference(){
        this.container = null;
        this.deploymentInfo = null;
        this.isInvalidReference = true;
    }

    protected static void invalidateAllHandlers(Object key){
        HashSet set = (HashSet)liveHandleRegistry.remove(key);
        if(set==null)return;
        synchronized(set){
            Iterator handlers = set.iterator();
            while(handlers.hasNext()){
                BaseEjbProxyHandler aHandler = (BaseEjbProxyHandler)handlers.next();
                aHandler.invalidateReference();
            }
        }
    }
    
    protected abstract Object _writeReplace(Object proxy) throws ObjectStreamException;
    
    
    protected static void registerHandler(Object key, BaseEjbProxyHandler handler){
        HashSet set = (HashSet)liveHandleRegistry.get(key);
        if(set!=null){
            synchronized(set){
                set.add(handler);   
            }
        }else{
            set = new HashSet();
            set.add(handler);
            liveHandleRegistry.put(key, set);
        }
    }
    
    public abstract org.openejb.ProxyInfo getProxyInfo();
    
	public boolean isLocal() {
		return isLocal;
	}
	public void setLocal(boolean isLocal) {
		this.isLocal = isLocal;
		this.doIntraVmCopy = !isLocal;
	}
}
