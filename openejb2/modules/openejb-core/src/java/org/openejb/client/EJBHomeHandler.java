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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Method;
import java.rmi.RemoteException;

import javax.ejb.EJBHome;
import javax.ejb.Handle;

import org.apache.geronimo.security.ContextManager;
import org.openejb.EJBComponentType;


/**
 * Handles invocations from an EJBHomeProxy.
 */
public abstract class EJBHomeHandler extends EJBInvocationHandler implements Externalizable {

    protected static final Method GETEJBMETADATA = getMethod(EJBHome.class, "getEJBMetaData", null);
    protected static final Method GETHOMEHANDLE = getMethod(EJBHome.class, "getHomeHandle", null);
    protected static final Method REMOVE_W_KEY = getMethod(EJBHome.class, "remove", new Class[]{Object.class});
    protected static final Method REMOVE_W_HAND = getMethod(EJBHome.class, "remove", new Class[]{Handle.class});
    protected static final Method GETHANDLER = getMethod(EJBHomeProxy.class, "getEJBHomeHandler", null);

    /**
     * Constructs an EJBHomeHandler to handle invocations from an EJBHomeProxy.
     */
    public EJBHomeHandler() {
    }

    public EJBHomeHandler(EJBMetaDataImpl ejb, ServerMetaData[] servers) {
        super(ejb, servers);
    }

    public static EJBHomeHandler createEJBHomeHandler(EJBMetaDataImpl ejb, ServerMetaData[] servers) {

        switch (ejb.type) {
            case EJBComponentType.BMP_ENTITY:
            case EJBComponentType.CMP_ENTITY:

                return new EntityEJBHomeHandler(ejb, servers);

            case EJBComponentType.STATEFUL:

                return new StatefulEJBHomeHandler(ejb, servers);

            case EJBComponentType.STATELESS:

                return new StatelessEJBHomeHandler(ejb, servers);
        }
        return null;

    }

//    protected abstract EJBObjectHandler newEJBObjectHandler();

    public EJBHomeProxy createEJBHomeProxy() {
        Class[] interfaces = new Class[]{EJBHomeProxy.class, ejb.homeClass};
        return (EJBHomeProxy) ProxyManager.newProxyInstance(interfaces, this, ejb.homeClass.getClassLoader());
    }

    protected Object _invoke(Object proxy, Method method, Object[] args) throws Throwable {

        String methodName = method.getName();

        try {

            if (method.getDeclaringClass() == Object.class) {
                if (method.equals(TOSTRING)) {
                    return "proxy=" + this;
                } else if (method.equals(EQUALS)) {
                    //TODO
                    return Boolean.FALSE;
                    // Maybe turn this into Externalizable
                } else if (method.equals(HASHCODE)) {
                    return new Integer(this.hashCode());
                    //TODO
                    // Maybe turn this into Externalizable
                } else {
                    throw new UnsupportedOperationException("unknown method: " + method);
                }
            } else if (method.getDeclaringClass() == EJBHomeProxy.class) {
                if (method.equals(GETHANDLER)) {
                    return this;
                } else if (methodName.equals("writeReplace")) {
                    return new EJBHomeProxyHandle(this);
                } else if (methodName.equals("readResolve")) {
                    //TODO
                    throw new UnsupportedOperationException("unknown method: " + method);
                    // Maybe turn this into Externalizable
                } else {
                    throw new UnsupportedOperationException("unknown method: " + method);
                }
            }
            /*-------------------------------------------------------*/
            //         Process the specific method invoked           //


            /*-- CREATE ------------- <HomeInterface>.create(<x>) ---*/
            if (methodName.equals("create")) {
                return create(method, args, proxy);

                /*-- FIND X --------------- <HomeInterface>.find<x>() ---*/
            } else if (methodName.startsWith("find")) {
                return findX(method, args, proxy);


                /*-- GET EJB METADATA ------ EJBHome.getEJBMetaData() ---*/

            } else if (method.equals(GETEJBMETADATA)) {
                return getEJBMetaData(method, args, proxy);


                /*-- GET HOME HANDLE -------- EJBHome.getHomeHandle() ---*/

            } else if (method.equals(GETHOMEHANDLE)) {
                return getHomeHandle(method, args, proxy);


                /*-- REMOVE ------------------------ EJBHome.remove() ---*/

            } else if (method.equals(REMOVE_W_HAND)) {
                return removeWithHandle(method, args, proxy);

            } else if (method.equals(REMOVE_W_KEY)) {
                return removeByPrimaryKey(method, args, proxy);

            } else if (method.getDeclaringClass() == ejb.homeClass) {
                return homeMethod(method, args, proxy);

                /*-- unknown ---------------------------------------------*/
            } else {

                throw new UnsupportedOperationException("unknown method: " + method);

            }
            //TODO:1: Catch this in the server-side and return an OpenEJB specific
            // exception class.
        } catch (org.openejb.SystemException se) {
            invalidateReference();
            throw new RemoteException("Container has suffered a SystemException", se.getCause());
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
     * @return Returns an new EJBObject proxy and handler
     */
    protected Object create(Method method, Object[] args, Object proxy) throws Throwable {
        EJBRequest req = new EJBRequest(EJB_HOME_CREATE);

        req.setClientIdentity(ContextManager.getThreadPrincipal());
        req.setContainerCode(ejb.deploymentCode);
        req.setContainerID(ejb.deploymentID);
        req.setMethodInstance(method);
        req.setMethodParameters(args);

        EJBResponse res = request(req);

        switch (res.getResponseCode()) {
            case EJB_SYS_EXCEPTION:
                throw (Throwable) res.getResult();
            case EJB_APP_EXCEPTION:
                throw (Throwable) res.getResult();
            case EJB_ERROR:
                throw (Throwable) res.getResult();
            case EJB_OK:
                // Create the EJBObject proxy
                Object primKey = res.getResult();
                EJBObjectHandler handler = EJBObjectHandler.createEJBObjectHandler(ejb, servers, primKey);
                handler.setEJBHomeProxy((EJBHomeProxy) proxy);
                //TODO:1: Add the proxy to the handler registry
                return handler.createEJBObjectProxy();
            default:
                throw new RemoteException("Received invalid response code from server: " + res.getResponseCode());
        }
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
     * @return Returns an new EJBObject proxy and handler
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
     * @return Returns an EjbMetaDataImpl
     * @see javax.ejb.EJBHome
     * @see javax.ejb.EJBHome#getEJBMetaData
     */
//    protected abstract Object getEJBMetaData(Method method, Object[] args, Object proxy) throws Throwable;
    protected Object getEJBMetaData(Method method, Object[] args, Object proxy) throws Throwable {
        return ejb;
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
     * @return Returns an IntraVmHandle
     * @see javax.ejb.EJBHome
     * @see javax.ejb.EJBHome#getHomeHandle
     */
    protected Object getHomeHandle(Method method, Object[] args, Object proxy) throws Throwable {
        //return new EJBHomeHandle(this);
        return new EJBHomeHandle((EJBHomeProxy) proxy);
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
     * @return Returns null
     * @see javax.ejb.EJBHome
     * @see javax.ejb.EJBHome#remove
     */
    protected abstract Object removeWithHandle(Method method, Object[] args, Object proxy) throws Throwable;

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
     * @return Returns null
     * @see javax.ejb.EJBHome
     * @see javax.ejb.EJBHome#remove
     */
    protected abstract Object removeByPrimaryKey(Method method, Object[] args, Object proxy) throws Throwable;


    /**
     * The object implements the readExternal method to restore its
     * contents by calling the methods of DataInput for primitive
     * types and readObject for objects, strings and arrays.  The
     * readExternal method must read the values in the same sequence
     * and with the same types as were written by writeExternal.
     *
     * @param in the stream to read data from in order to restore the object
     * @throws IOException if I/O errors occur
     * @throws ClassNotFoundException If the class for an object being
     * restored cannot be found.
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    }

    /**
     * The object implements the writeExternal method to save its contents
     * by calling the methods of DataOutput for its primitive values or
     * calling the writeObject method of ObjectOutput for objects, strings,
     * and arrays.
     *
     * @param out the stream to write the object to
     * @throws IOException Includes any I/O exceptions that may occur
     * @serialData Overriding methods should use this tag to describe
     * the data layout of this Externalizable object.
     * List the sequence of element types and, if possible,
     * relate the element to a public/protected field and/or
     * method of this Externalizable class.
     */
    public void writeExternal(ObjectOutput out) throws IOException {
    }

    protected Object homeMethod(Method method, Object[] args, Object proxy) throws Throwable {
        EJBRequest req = new EJBRequest(EJB_HOME_METHOD);

        req.setClientIdentity(ContextManager.getThreadPrincipal());
        req.setContainerCode(ejb.deploymentCode);
        req.setContainerID(ejb.deploymentID);
        req.setMethodInstance(method);
        req.setMethodParameters(args);

        EJBResponse res = request(req);

        switch (res.getResponseCode()) {
            case EJB_ERROR:
                throw (Throwable) res.getResult();
            case EJB_SYS_EXCEPTION:
                throw (Throwable) res.getResult();
            case EJB_APP_EXCEPTION:
                throw (Throwable) res.getResult();
            case EJB_OK:
                return res.getResult();
            default:
                throw new RemoteException("Received invalid response code from server: " + res.getResponseCode());
        }
    }
}


