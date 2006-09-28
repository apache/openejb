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
 * $Id: EJBObjectHandler.java 445853 2005-12-21 14:21:56Z gdamour $
 */
package org.apache.openejb.client;

import javax.ejb.EJBObject;
import java.lang.reflect.Method;
import java.rmi.RemoteException;

import org.apache.geronimo.security.ContextManager;

import org.apache.openejb.EJBComponentType;


/**
 * @since 11/25/2001
 */
public abstract class EJBObjectHandler extends EJBInvocationHandler {

    protected static final Method GETEJBHOME = getMethod(EJBObject.class, "getEJBHome", null);
    protected static final Method GETHANDLE = getMethod(EJBObject.class, "getHandle", null);
    protected static final Method GETPRIMARYKEY = getMethod(EJBObject.class, "getPrimaryKey", null);
    protected static final Method ISIDENTICAL = getMethod(EJBObject.class, "isIdentical", new Class[]{EJBObject.class});
    protected static final Method REMOVE = getMethod(EJBObject.class, "remove", null);

    protected static final Method GETHANDLER = getMethod(EJBObjectProxy.class, "getEJBObjectHandler", null);

    /*
    * The registryId is a logical identifier that is used as a key when placing EntityEJBObjectHandler into
    * the BaseEjbProxyHanlder's liveHandleRegistry.  EntityEJBObjectHandlers that represent the same
    * bean identity (keyed by the registry id) will be stored together so that they can be removed together
    * when the EJBInvocationHandler.invalidateAllHandlers is invoked. The EntityEJBObjectHandler uses a
    * compound key composed of the entity bean's primary key, deployment id, and
    * container id.  This uniquely identifies the bean identity that is proxied by this handler allowing it
    * to be removed with other handlers bound to the same registry id.
    */
    public Object registryId;


    EJBHomeProxy ejbHome = null;

    // new Class []{javax.ejb.EntityContext.class}

    public EJBObjectHandler() {
    }

    public EJBObjectHandler(EJBMetaDataImpl ejb, ServerMetaData[] servers) {
        super(ejb, servers);
    }

    public EJBObjectHandler(EJBMetaDataImpl ejb, ServerMetaData[] servers, Object primaryKey) {
        super(ejb, servers, primaryKey);
    }

    protected void setEJBHomeProxy(EJBHomeProxy ejbHome) {
        this.ejbHome = ejbHome;
    }

    public static EJBObjectHandler createEJBObjectHandler(EJBMetaDataImpl ejb, ServerMetaData[] servers, Object primaryKey) {

        switch (ejb.type) {
            case EJBComponentType.BMP_ENTITY:
            case EJBComponentType.CMP_ENTITY:

                return new EntityEJBObjectHandler(ejb, servers, primaryKey);

            case EJBComponentType.STATEFUL:

                return new StatefulEJBObjectHandler(ejb, servers, primaryKey);

            case EJBComponentType.STATELESS:

                return new StatelessEJBObjectHandler(ejb, servers, primaryKey);
        }
        return null;
    }

    /**
     * The Registry id is a logical identifier that is used as a key when placing EjbObjectProxyHanlders into
     * the BaseEjbProxyHanlder's liveHandleRegistry.  EjbObjectProxyHanlders that represent the same
     * bean identity (keyed by the registry id) will be stored together so that they can be removed together
     * when the EJBInvocationHandler.invalidateAllHandlers is invoked.
     * <p/>
     * This method is implemented by the subclasses to return an id that logically identifies
     * bean identity for a specific deployment id and container.  For example, the EntityEJBObjectHandler
     * overrides this method to return a compound key composed of the bean's primary key, deployment id, and
     * container id.  This uniquely identifies the bean identity that is proxied by this handler.  Another example
     * is the StatefulEjbObjectHanlder which overrides this method to return the stateful bean's hidden primary key,
     * which is a java.rmi.dgc.VMID.
     */
    public abstract Object getRegistryId();

    public EJBObjectProxy createEJBObjectProxy() {
        Class[] interfaces = new Class[]{EJBObjectProxy.class, ejb.remoteClass};
        return (EJBObjectProxy) ProxyManager.newProxyInstance(interfaces, this, ejb.remoteClass.getClassLoader());
    }


    // The EJBObject stub is synchronized to prevent multiple client threads from accessing the
    // stub concurrently.  This is required by session beans (6.5.6 Serializing session bean methods) but
    // not required by entity beans. Implementing synchronization on the stub prohibits multiple threads from currently
    // invoking methods on the same stub, but doesn't prohibit a client from having multiple references to the same
    // entity identity. Synchronizing the stub is a simple and elegant solution to a difficult problem.
    //
    public synchronized Object _invoke(Object p, Method m, Object[] a) throws Throwable {

        Object retValue = null;
        /*
         * This section is to be replaced by a more appropriate solution.
         * This code is very temporary.
         */

        try {

            String methodName = m.getName();

            if (m.getDeclaringClass() == Object.class) {
                if (m.equals(TOSTRING)) {
                    return "proxy=" + this;
                } else if (m.equals(EQUALS)) {
                    //TODO
                    return Boolean.FALSE;
                    // Maybe turn this into Externalizable
                } else if (m.equals(HASHCODE)) {
                    return new Integer(this.hashCode());
                } else {
                    throw new UnsupportedOperationException("unknown method: " + m);
                }
            } else if (m.getDeclaringClass() == EJBObjectProxy.class) {
                if (m.equals(GETHANDLER)) {
                    return this;
                } else if (methodName.equals("writeReplace")) {
                    return new EJBObjectProxyHandle(this);
                } else if (methodName.equals("readResolve")) {
                    //TODO
                    // Maybe turn this into Externalizable
                } else {
                    throw new UnsupportedOperationException("unknown method: " + m);
                }
            } else if (m.getDeclaringClass() == javax.ejb.EJBObject.class) {
                if (m.equals(GETHANDLE)) {
                    retValue = getHandle(m, a, p);
                } else if (m.equals(GETPRIMARYKEY)) {
                    retValue = getPrimaryKey(m, a, p);
                } else if (m.equals(ISIDENTICAL)) {
                    retValue = isIdentical(m, a, p);
                } else if (m.equals(GETEJBHOME)) {
                    retValue = getEJBHome(m, a, p);
                } else if (m.equals(REMOVE)) {
                    retValue = remove(m, a, p);
                } else {
                    throw new UnsupportedOperationException("unknown method: " + m);
                }
            } else if (m.getDeclaringClass().isAssignableFrom(ejb.remoteClass)) {
                retValue = businessMethod(m, a, p);
            } else {
                throw new UnsupportedOperationException("unknown method: " + m);
            }


            /*
             * The ire is thrown by the container system and propagated by
             * the server to the stub.
             */
        } catch (org.apache.openejb.InvalidateReferenceException ire) {
            invalidateAllHandlers(getRegistryId());
            return ire.getCause();
            /*
             * Application exceptions must be reported dirctly to the client. They
             * do not impact the viability of the proxy.
             */
        } catch (org.apache.openejb.ApplicationException ae) {
            throw ae.getCause();
            /*
             * A system exception would be highly unusual and would indicate a sever
             * problem with the container system.
             */
        } catch (org.apache.openejb.SystemException se) {
            invalidateReference();
            throw new RemoteException("Container has suffered a SystemException", se.getCause());
        } catch (org.apache.openejb.OpenEJBException oe) {
            throw new RemoteException("Unknown Container Exception", oe.getCause());
        }
        return retValue;
    }


    protected Object getEJBHome(Method method, Object[] args, Object proxy) throws Throwable {
        if (ejbHome == null) {
            ejbHome = EJBHomeHandler.createEJBHomeHandler(ejb, servers).createEJBHomeProxy();
        }
        return ejbHome;
    }

    protected Object getHandle(Method method, Object[] args, Object proxy) throws Throwable {
        return new EJBObjectHandle((EJBObjectProxy) proxy);
    }


    protected abstract Object getPrimaryKey(Method method, Object[] args, Object proxy) throws Throwable;

    protected abstract Object isIdentical(Method method, Object[] args, Object proxy) throws Throwable;

    protected abstract Object remove(Method method, Object[] args, Object proxy) throws Throwable;

    protected Object businessMethod(Method method, Object[] args, Object proxy) throws Throwable {
//      checkAuthorization(method);
//      return container.invoke(deploymentID, method, args, primaryKey, getThreadSpecificSecurityIdentity());

        EJBRequest req = new EJBRequest(EJB_OBJECT_BUSINESS_METHOD);

        req.setMethodParameters(args);
        req.setMethodInstance(method);
        req.setClientIdentity(ContextManager.getThreadPrincipal());
        req.setContainerCode(ejb.deploymentCode);
        req.setContainerID(ejb.deploymentID);
        req.setPrimaryKey(primaryKey);

        EJBResponse res = request(req);

//        if (method.getName().equals("test36_returnEJBHome2")) {
//          System.out.println("\n\n----------------------------------------------------------");
//          System.out.println(method.getName());
//          Object obj = res.getResult();
//          System.out.println("obj="+(obj==null));
//          System.out.println("obj="+(obj.getClass()));
//          System.out.println("obj="+(obj.getClass().getDeclaringClass()));
//          Class[] ifs = obj.getClass().getInterfaces();
//          for (int i=0; i < ifs.length; i++){
//              System.out.println("ifs["+i+"] "+ifs[i]);
//          }
//        }
        switch (res.getResponseCode()) {
            case EJB_ERROR:
//            System.out.println("ERROR "+res.getResult());
                throw (Throwable) res.getResult();
            case EJB_SYS_EXCEPTION:
//            System.out.println("SYS EXEPTION "+res.getResult());
                throw (Throwable) res.getResult();
            case EJB_APP_EXCEPTION:
//            System.out.println("APP EXEPTION "+res.getResult());
                throw (Throwable) res.getResult();
            case EJB_OK:
                return res.getResult();
            default:
                throw new RemoteException("Received invalid response code from server: " + res.getResponseCode());
        }
    }
}
