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
 * $Id: EJBInvocationHandler.java 445853 2005-12-21 14:21:56Z gdamour $
 */
package org.apache.openejb.client;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.rmi.NoSuchObjectException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;


/**
 * TODO: Add comments
 * 
 */
public abstract class EJBInvocationHandler implements MethodInterceptor, InvocationHandler, Serializable, ResponseCodes, RequestMethods  {
    
    protected static final Method EQUALS    = getMethod(Object.class, "equals", null);
    protected static final Method HASHCODE  = getMethod(Object.class, "hashCode", null);
    protected static final Method TOSTRING  = getMethod(Object.class, "toString", null);
    
    /**
     * Keeps track of live EJBInvocationHandler in this VM. So that 
     * related handlers can be removed if necessary.
     * 
     * Currently this registry is only used to track live 
     * EJBInvocationHandlers. The EJBObjectHandlers are tracked to allow
     * remove() operations and invalidate exceptions to be propagated to
     * the proper handler instances.
     * 
     * There are several scenarios where this is useful:
     * <ul>
     * <li>
     * If an EJBHome.remove( )method is invoked, the EJBHomeHandler will
     * use this registry to notify the EJBObjectHandler associated with 
     * the removed identity that the EJBObjectHandler should invalidate 
     * itself.
     * <li>
     * When two EjbObjectProxy handlers are both associated with the same
     * bean identity and one is  removed, the EJBObjectHandler executing 
     * the remove will notify the other EJBObjectHandler objects 
     * associated with the same identity that they are to be invalidated.
     * <li>
     * When an EjbObjectProxyHanlder performs an operation that results 
     * in a an InvalidateReferenceException the EJBObjectHandler will use
     * the registry to ensure that all EJBObjectHandlers associated the 
     * identity are invalidated.
     */
    protected static Hashtable liveHandleRegistry = new Hashtable();

    /**
     * TODO: Add comments
     */
    protected transient boolean inProxyMap = false;
    
    /**
     * TODO: Add comments
     */
    protected transient boolean isInvalidReference = false;
    
    
    //-------------------------------------------------------//
    // All of the following are not serialized as objects.
    // The data in them is written to the stream in the 
    // writeExternal method.  The raw data is read in the 
    // readExternal method, the data is used to create new
    // instances of the objects.
    //-------------------------------------------------------//

    /**
     * TODO: Add comments
     */
    protected transient EJBRequest request;
    
    /**
     * The EJBMetaDataImpl object of the bean deployment that this 
     * invocation handler represents.
     */
    protected transient EJBMetaDataImpl ejb;
    /**
     * The ServerMetaData object containing the information needed to 
     * send invokations to the EJB Server.
     * This array identifies all the EJB servers running the target bean
     * deployment.
     */
    protected transient ServerMetaData[] servers;

    /**
     * The primary key of the bean deployment or null if the deployment
     * is a bean type that doesn't require a primary key
     */
    protected transient Object primaryKey;

    /**
     * TODO: Add comments
     */
    public EJBInvocationHandler(){
    }
    
    public EJBInvocationHandler(EJBMetaDataImpl ejb, ServerMetaData[] servers){
        this.ejb        = ejb;
        this.servers     = servers;
    }
    
    public EJBInvocationHandler(EJBMetaDataImpl ejb, ServerMetaData[] servers, Object primaryKey){
        this(ejb, servers);
        this.primaryKey = primaryKey;
    }

    protected static Method getMethod(Class c, String method, Class[] params){
        try{
            return c.getMethod(method, params );
        }catch(NoSuchMethodException nse){
            //TODO:3: If this happens, it would cause bizarre problems
            // Do something to handle it, just in case.
        }
        return null;
    }
    
    public Object intercept(Object proxy, Method method, Object[] args, MethodProxy mProxy) throws Throwable {
        return invoke(proxy, method, args);
    }
    /**
     * TODO: Add comments
     * 
     * @param proxy  The Proxy object that represents this bean deployment's EJBObject
     *               or EJBHome.
     * @param method The EJBHome or EJBObject method the caller is invoking.
     * @param args   The parameters to the mehtod being invoked
     * @return The result of invoking the appropriate method on the bean instance.
     * @exception Throwable
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable{
        if (isInvalidReference) throw new NoSuchObjectException("reference is invalid");
        
        Object returnObj = null;
        returnObj = _invoke(proxy,method,args);
        return returnObj;    
    }

    /**
     * Overridden by subclasses and called by {@link #invoke}.  
     * Subclasses implement the main behavior of calling invoke on the 
     * Container that the bean deployment lives in.
     * 
     * @param proxy  The Proxy subclass that is the bean's EJBObject or EJBHome.
     * @param method The bean method that the caller is attempting to invoke.
     * @param args   The arguments to the method being invoked.
     * @return The result of invoking the appropriate method on the bean instance.
     * @exception Throwable
     */
    protected abstract Object _invoke(Object proxy, Method method, Object[] args) throws Throwable;

    public static void print(String s){
        //System.out.println();
    }
    
    public static void println(String s){
        //System.out.print(s+'\n');
    }

    protected EJBResponse request(EJBRequest req) throws Exception {
        RequestInfo reqInfo = new RequestInfo(req, servers);
        EJBResponse res = new EJBResponse();
        ResponseInfo resInfo = new ResponseInfo(res);
        Client.request(reqInfo, resInfo);
        servers = resInfo.getServers();
        return res;
    }
    
    /**
     * Invalidates this reference so that it can not be used as a proxy 
     * for the bean identity. This method may be called when an 
     * InvalidateReferenceException is thrown by the container or when
     * the bean identity associated with this proxy is explicitly 
     * removed, by calling one of the remove( ) methods.
     */
    protected void invalidateReference(){
        this.servers     = null;
        this.ejb        = null;
        this.inProxyMap = false;
        this.isInvalidReference = true;
        this.primaryKey = null;
    }

    /**
     * TODO: Add comments
     * 
     * @param key
     */
    protected static void invalidateAllHandlers(Object key){
        
        HashSet set = (HashSet)liveHandleRegistry.remove( key );
        if ( set == null ) return;
        
        synchronized ( set ) {
            Iterator handlers = set.iterator();
            while(handlers.hasNext()){
                EJBInvocationHandler handler = (EJBInvocationHandler)handlers.next();
                handler.invalidateReference();
            }
            set.clear();
        }
    }
    
    /**
     * TODO: Add comments
     * 
     * @param key
     * @param handler
     */
    protected static void registerHandler(Object key, EJBInvocationHandler handler){
        HashSet set = (HashSet)liveHandleRegistry.get(key);
        
        if ( set == null ) {
            set = new HashSet();
            liveHandleRegistry.put( key, set );
        }

        synchronized (set) {
            set.add(handler);   
        }
    }
}
