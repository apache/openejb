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
 * Copyright 2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.apache.openejb.corba;

import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.HomeHandle;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import org.omg.CORBA.ORB;
import org.apache.openejb.EJBComponentType;
import org.apache.openejb.proxy.ProxyInfo;
import org.apache.openejb.spi.ApplicationServer;

/**
 * @version $Revision$ $Date$
 */
public class CorbaApplicationServer implements ApplicationServer {
    public EJBObject getEJBObject(ProxyInfo proxyInfo) {
        try {
            RefGenerator refGenerator = AdapterWrapper.getRefGenerator(proxyInfo.getContainerID());
            org.omg.CORBA.Object object = refGenerator.genObjectReference(proxyInfo);
            EJBObject ejbObject = (EJBObject) PortableRemoteObject.narrow(object, EJBObject.class);
            return ejbObject;
        } catch (Throwable e) {
            throw new org.omg.CORBA.MARSHAL(e.getClass().getName() + " thrown while marshaling the reply: " + e.getMessage(), 0, org.omg.CORBA.CompletionStatus.COMPLETED_YES);
        }
    }

    public EJBHome getEJBHome(ProxyInfo proxyInfo) {
        try {
            RefGenerator refGenerator = AdapterWrapper.getRefGenerator(proxyInfo.getContainerID());
            org.omg.CORBA.Object home = refGenerator.genHomeReference(proxyInfo);
            EJBHome ejbHome = (EJBHome) PortableRemoteObject.narrow(home, EJBHome.class);
            return ejbHome;
        } catch (Throwable e) {
            throw new org.omg.CORBA.MARSHAL(e.getClass().getName() + " thrown while marshaling the reply: " + e.getMessage(), 0, org.omg.CORBA.CompletionStatus.COMPLETED_YES);
        }
    }

    public javax.ejb.Handle getHandle(ProxyInfo proxyInfo) {
        org.omg.CORBA.Object ejbObject = (org.omg.CORBA.Object) getEJBObject(proxyInfo);
        String ior = getOrb().object_to_string(ejbObject);
        Handle handle = new CORBAHandle(ior, proxyInfo.getPrimaryKey());
        return handle;
    }

    public javax.ejb.HomeHandle getHomeHandle(ProxyInfo proxyInfo) {
        org.omg.CORBA.Object ejbHome = (org.omg.CORBA.Object) getEJBHome(proxyInfo);
        String ior = getOrb().object_to_string(ejbHome);
        HomeHandle homeHandle = new CORBAHomeHandle(ior);
        return homeHandle;
    }

    public EJBMetaData getEJBMetaData(ProxyInfo proxyInfo) {
        int componentType = proxyInfo.getComponentType();
        byte ejbType;
        if (componentType == EJBComponentType.STATEFUL) {
            ejbType = CORBAEJBMetaData.STATEFUL;
        } else if (componentType == EJBComponentType.STATELESS) {
            ejbType = CORBAEJBMetaData.STATELESS;
        } else if (componentType == EJBComponentType.BMP_ENTITY || componentType == EJBComponentType.CMP_ENTITY) {
            ejbType = CORBAEJBMetaData.ENTITY;
        } else {
            throw new IllegalArgumentException("Unknown component type: " + componentType);
        }

        CORBAEJBMetaData ejbMetaData = new CORBAEJBMetaData(getEJBHome(proxyInfo),
                ejbType,
                proxyInfo.getHomeInterface(),
                proxyInfo.getRemoteInterface(),
                proxyInfo.getPrimaryKeyClass());
        return ejbMetaData;
    }

    private static ORB getOrb() {
        try {
            Context context = new InitialContext();
            ORB orb = (ORB) context.lookup("java:comp/ORB");
            return orb;
        } catch (Throwable e) {
            throw new org.omg.CORBA.MARSHAL("Cound not find ORB in jndi at java:comp/ORB", 0, org.omg.CORBA.CompletionStatus.COMPLETED_YES);
        }
    }
}
