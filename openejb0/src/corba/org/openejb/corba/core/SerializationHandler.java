/*
 * Copyright  2002, Apple Computer, Inc. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * 1.  Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.  
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.  
 * 3.  Neither the name of Apple Computer, Inc. ("Apple")
 * nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY APPLE AND ITS CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL APPLE OR ITS
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */


package org.openejb.corba.core;

import javax.ejb.EJBHome;
import javax.ejb.EJBObject;


/**
 * This class is used by the serizaliation engine to replace the OpenEJB artifact by the 
 * application server corresponding objects.
 *
 * @author sreich@apple.com
 */
public class SerializationHandler implements org.openejb.spi.ApplicationServer {    
    private final static org.openejb.util.Logger logger = org.openejb.util.Logger.getInstance("CORBA-Adapter", "org.openejb.util.resources");
    private final ContainerSystem containerSystem;

    public static class GenericHandle extends org.openejb.core.ivm.IntraVmHandle {
        // we need to keep the pk for later operations such as remove(handle) on a home interface
        private Object _primaryKey;

        public GenericHandle(Object ejb, Object primaryKey) {
            super(ejb);
            _primaryKey = primaryKey;
        }

        protected java.lang.Object writeReplace() {
            return this;
        }
        public EJBHome getEJBHome() {
            return (EJBHome)javax.rmi.PortableRemoteObject.narrow(theProxy, EJBHome.class);
        }
        public EJBObject getEJBObject() {
            return (EJBObject)javax.rmi.PortableRemoteObject.narrow(theProxy, EJBObject.class);
        }
        public Object getPrimaryKey() {
            return _primaryKey;
        }
    }
    
    static class EJBMetaData extends org.openejb.core.ivm.IntraVmMetaData {
        public EJBMetaData(Class homeInterface, Class remoteInterface, Class primaryKeyClass, byte typeOfBean, javax.ejb.EJBHome home) {
            super(homeInterface, remoteInterface, primaryKeyClass, typeOfBean);
            setEJBHome(home);
        }
        
        protected java.lang.Object writeReplace() {
            return this;
        }
        public javax.ejb.EJBHome getEJBHome() {
            return (javax.ejb.EJBHome)javax.rmi.PortableRemoteObject.narrow(homeStub, javax.ejb.EJBHome.class);
        }
    }
    
    /**
     * We need to retain a reference to the container system
     */
    public SerializationHandler( org.openejb.corba.core.ContainerSystem system ) {
        containerSystem = system;
    }

    /**
     * Replaces the OpenEJB artifact for the meta data
     */
    public javax.ejb.EJBMetaData getEJBMetaData(org.openejb.ProxyInfo proxyInfo) {
        javax.ejb.EJBHome home = (javax.ejb.EJBHome)lookup(proxyInfo);
        org.openejb.DeploymentInfo di = proxyInfo.getDeploymentInfo();
        EJBMetaData data = new EJBMetaData( di.getHomeInterface(), di.getRemoteInterface(),
                                            di.getPrimaryKeyClass(), di.getComponentType(), home);
        return data;
    }

    /**
     * Replaces the OpenEJB artifact for the Handle
     */
    public javax.ejb.Handle getHandle(org.openejb.ProxyInfo proxyInfo) {
        return new GenericHandle(lookup(proxyInfo), proxyInfo.getPrimaryKey());
    }

    /**
     * Replaces the OpenEJB artifact for the Handle
     */
    public javax.ejb.HomeHandle getHomeHandle(org.openejb.ProxyInfo proxyInfo) {
        return new GenericHandle(lookup(proxyInfo), null);
    }

    /**
     * Replace the OpenEJB artifact for the Object reference
     */
    public javax.ejb.EJBObject getEJBObject(org.openejb.ProxyInfo proxyInfo) {
        return (javax.ejb.EJBObject) lookup(proxyInfo);
    }

    /**
     * Replaces the OpenEJB artifact for the Home reference
     */
    public javax.ejb.EJBHome getEJBHome(org.openejb.ProxyInfo proxyInfo) {
        return (javax.ejb.EJBHome) lookup(proxyInfo);
    }

    /**
     * Returns a remote object reference for the bean/home corresponding to the proxy info passed as parameter.
     * FIXME: exception handling
     */
    private Object lookup(org.openejb.ProxyInfo pinfo) {
        try{
            ContainerAdapter adapter = containerSystem.getContainerAdapter(pinfo.getBeanContainer());
             return adapter.createBeanProfile(pinfo);
        }catch(Throwable e) {
            throw new org.omg.CORBA.MARSHAL("Internal server error while marshaling the reply", 0, org.omg.CORBA.CompletionStatus.COMPLETED_YES);
        }
    }
}
