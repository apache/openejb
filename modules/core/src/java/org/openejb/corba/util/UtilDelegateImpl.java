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
 * Copyright 2004-2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.corba.util;

import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import javax.rmi.CORBA.Stub;
import javax.rmi.CORBA.Tie;
import javax.rmi.CORBA.UtilDelegate;
import javax.rmi.CORBA.ValueHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.MARSHAL;
import org.omg.CORBA.ORB;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;

import org.openejb.corba.AdapterWrapper;
import org.openejb.corba.CORBAException;
import org.openejb.proxy.BaseEJB;
import org.openejb.proxy.EJBHomeImpl;
import org.openejb.proxy.EJBObjectImpl;
import org.openejb.proxy.ProxyInfo;


/**
 * @version $Revision$ $Date$
 */
public final class UtilDelegateImpl implements UtilDelegate {

    private final Log log = LogFactory.getLog(UtilDelegateImpl.class);
    private final UtilDelegate delegate;

    private final static String DELEGATE_NAME = "org.openejb.corba.UtilDelegateClass";

    public UtilDelegateImpl() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        String value = System.getProperty(DELEGATE_NAME);
        if (value == null) {
            log.error("No delegate specfied via " + DELEGATE_NAME);
            throw new IllegalStateException("The property " + DELEGATE_NAME + " must be defined!");
        }

        if (log.isDebugEnabled()) log.debug("Set delegate " + value);
        delegate = (UtilDelegate) Class.forName(value).newInstance();
    }

    public void unexportObject(Remote target) throws NoSuchObjectException {
        delegate.unexportObject(target);
    }

    public boolean isLocal(Stub stub) throws RemoteException {
        return delegate.isLocal(stub);
    }

    public ValueHandler createValueHandler() {
        return delegate.createValueHandler();
    }

    public Object readAny(InputStream in) {
        return delegate.readAny(in);
    }

    public void writeAbstractObject(OutputStream out, Object obj) {
        delegate.writeAbstractObject(out, obj);
    }

    public void writeAny(OutputStream out, Object obj) {
        delegate.writeAny(out, obj);
    }

    public void writeRemoteObject(OutputStream out, Object obj) {
        try {
            if (obj != null && obj instanceof java.rmi.Remote)
                out.write_Object(handleRemoteObject(out, (Remote) obj));
            else if (obj == null || obj instanceof org.omg.CORBA.Object)
                out.write_Object((org.omg.CORBA.Object) obj);
            else {
                log.error("Encountered unknown object reference of type " + obj.getClass() + ":" + obj);
                throw new MARSHAL("Internal server error while marshaling the reply", 0, CompletionStatus.COMPLETED_YES);
            }
        } catch (Throwable e) {
            log.error("Received unexpected exception while marshaling an object reference:", e);
            throw new MARSHAL("Internal server error while marshaling the reply", 0, CompletionStatus.COMPLETED_YES);
        }
    }

    public String getCodebase(Class clz) {
        return delegate.getCodebase(clz);
    }

    public void registerTarget(Tie tie, Remote target) {
        delegate.registerTarget(tie, target);
    }

    public RemoteException wrapException(Throwable obj) {
        return delegate.wrapException(obj);
    }

    public RemoteException mapSystemException(SystemException ex) {
        return delegate.mapSystemException(ex);
    }

    public Tie getTie(Remote target) {
        return delegate.getTie(target);
    }

    public Object copyObject(Object obj, ORB orb) throws RemoteException {
        return delegate.copyObject(obj, orb);
    }

    public Object[] copyObjects(Object[] obj, ORB orb) throws RemoteException {
        return delegate.copyObjects(obj, orb);
    }

    public Class loadClass(String className, String remoteCodebase, ClassLoader loader) throws ClassNotFoundException {
        if (log.isDebugEnabled()) log.debug("loadClass: " + className + ", " + remoteCodebase + ", " + loader);

        Class result = delegate.loadClass(className, remoteCodebase, loader);
        if (result == null) {
            
        }

        return result;
    }

    /**
     * handle activation
     */
    protected org.omg.CORBA.Object handleRemoteObject(org.omg.CORBA.portable.OutputStream out, Remote remote) {
        BaseEJB proxy;
        if (remote instanceof Tie) {
            proxy = (BaseEJB) ((Tie) remote).getTarget();
        } else if (remote instanceof Stub) {
            return (Stub) remote;
        } else if (BaseEJB.class.isAssignableFrom(remote.getClass())) {
            proxy = (BaseEJB) remote;
        } else {
            log.error("Encountered unknown object reference of type " + remote);
            throw new MARSHAL("Internal server error while marshaling the reply", 0, CompletionStatus.COMPLETED_YES);
        }

        ProxyInfo pi = proxy.getProxyInfo();
        try {
            if (proxy instanceof EJBHomeImpl) {
                return AdapterWrapper.getRefGenerator(pi.getContainerID()).genHomeReference(pi);
            } else if (proxy instanceof EJBObjectImpl) {
                return AdapterWrapper.getRefGenerator(pi.getContainerID()).genObjectReference(pi);
            } else {
                log.error("Encountered unknown local invocation handler of type " + proxy.getClass().getSuperclass() + ":" + pi);
                throw new MARSHAL("Internal server error while marshaling the reply", 0, CompletionStatus.COMPLETED_YES);
            }
        } catch (CORBAException e) {
            log.error("Encountered unknown local invocation handler of type " + proxy.getClass().getSuperclass() + ":" + pi);
            throw new MARSHAL("Internal server error while marshaling the reply", 0, CompletionStatus.COMPLETED_YES);
        }
    }
}
