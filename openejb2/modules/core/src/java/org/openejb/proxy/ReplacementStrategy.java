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
 *    please contact info@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
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
package org.openejb.proxy;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.io.InvalidClassException;
import java.io.InvalidObjectException;
import java.rmi.RemoteException;

import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBObject;

import org.openejb.EJBComponentType;
import org.apache.geronimo.kernel.ClassLoading;


public interface ReplacementStrategy {
    Object writeReplace(Object object, ProxyInfo proxyInfo) throws ObjectStreamException ;
    
    static final ReplacementStrategy COPY = new ReplacementStrategy(){
        public Object writeReplace(Object object, ProxyInfo proxyInfo) {
            return new ImmutableArtifact(object);
        }
    };
    
    static final ReplacementStrategy PASSIVATE = new ReplacementStrategy(){
        public Object writeReplace(Object object, ProxyInfo proxyInfo) {
            return object;
        }
    };
    
    static final ReplacementStrategy REPLACE = new ReplacementStrategy(){
        public Object writeReplace(Object object, ProxyInfo proxyInfo) {
            //TODO: I have plans to clean/speed this up.  This really only happens on serialization to an external VM, so it isn't much of a performance issue anyway.
            
            if (object instanceof EJBObject){
                return org.openejb.OpenEJB.getApplicationServer().getEJBObject(proxyInfo);
            } else if (object instanceof EJBHome){
                return org.openejb.OpenEJB.getApplicationServer().getEJBHome(proxyInfo);
            } else if (object instanceof EJBMetaData){
                return org.openejb.OpenEJB.getApplicationServer().getEJBMetaData(proxyInfo);
            } else if (object instanceof HandleImpl){
                HandleImpl handle = (HandleImpl)object;
                
                if (handle.type == HandleImpl.HANDLE){
                    return org.openejb.OpenEJB.getApplicationServer().getHandle(proxyInfo);
                } else {
                    return org.openejb.OpenEJB.getApplicationServer().getHomeHandle(proxyInfo);
                }
            } else /*should never happen */ {
                return object;
            }
        }
    };


    static final ReplacementStrategy IN_VM_REPLACE = new ReplacementStrategy(){
        public Object writeReplace(Object object, ProxyInfo proxyInfo) {
            if (object instanceof EJBObject){
                return new ProxyMemento(proxyInfo, ProxyMemento.EJB_OBJECT);
            } else if (object instanceof EJBHome){
                return new ProxyMemento(proxyInfo, ProxyMemento.EJB_HOME);
            } else if (object instanceof EJBMetaData){
                return new ProxyMemento(proxyInfo, ProxyMemento.EJB_META_DATA);
            } else if (object instanceof HandleImpl){
                HandleImpl handle = (HandleImpl)object;

                if (handle.type == HandleImpl.HANDLE){
                    return new ProxyMemento(proxyInfo, ProxyMemento.HANDLE);
                } else {
                    return new ProxyMemento(proxyInfo, ProxyMemento.HOME_HANDLE);
                }
            } else /*should never happen */ {
                return object;
            }
        }
    };

    static class ProxyMemento implements Serializable {
        private static final int EJB_OBJECT = 0;
        private static final int EJB_HOME = 1;
        private static final int HANDLE = 2;
        private static final int HOME_HANDLE = 3;
        private static final int EJB_META_DATA = 4;

        private final String containerId;
        private final boolean isSessionBean;
        private final String remoteInterfaceName;
        private final String homeInterfaceName;
        private final Object primayKey;
        private final int type;

        public ProxyMemento(ProxyInfo proxyInfo, int type) {
            this.type = type;
            this.containerId = proxyInfo.getContainerID();
            int componentType = proxyInfo.getComponentType();
            isSessionBean = (componentType == EJBComponentType.STATELESS || componentType == EJBComponentType.STATEFUL);
            this.remoteInterfaceName = proxyInfo.getRemoteInterface().getName();
            this.homeInterfaceName = proxyInfo.getHomeInterface().getName();
            this.primayKey = proxyInfo.getPrimaryKey();
        }

        private Object readResolve() throws ObjectStreamException {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Class remoteInterface = null;
            try {
                remoteInterface = ClassLoading.loadClass(remoteInterfaceName, cl);
            } catch (ClassNotFoundException e) {
                throw new InvalidClassException("Could not load remote interface: " + remoteInterfaceName);
            }
            Class homeInterface = null;
            try {
                homeInterface = ClassLoading.loadClass(homeInterfaceName, cl);
            } catch (ClassNotFoundException e) {
                throw new InvalidClassException("Could not load home interface: " + remoteInterfaceName);
            }

            EJBProxyFactory proxyFactory = new EJBProxyFactory(containerId,
                    isSessionBean,
                    remoteInterface,
                    homeInterface,
                    null,
                    null);

            switch (type) {
                case EJB_OBJECT:
                    return proxyFactory.getEJBObject(primayKey);
                case EJB_HOME:
                    return proxyFactory.getEJBHome();
                case HANDLE:
                    try {
                        return proxyFactory.getEJBObject(primayKey).getHandle();
                    } catch (RemoteException e) {
                        throw (InvalidObjectException) new InvalidObjectException("Error getting handle from ejb object").initCause(e);
                    }
                case HOME_HANDLE:
                    try {
                        return proxyFactory.getEJBHome().getHomeHandle();
                    } catch (RemoteException e) {
                        throw (InvalidObjectException) new InvalidObjectException("Error getting handle from home").initCause(e);
                    }
                case EJB_META_DATA:
                    try {
                        return proxyFactory.getEJBHome().getEJBMetaData();
                    } catch (RemoteException e) {
                        throw (InvalidObjectException) new InvalidObjectException("Error getting ejb meta data from home").initCause(e);
                    }
                default:
                    throw new InvalidObjectException("Unknown type" + type);
            }
        }
    }
}
