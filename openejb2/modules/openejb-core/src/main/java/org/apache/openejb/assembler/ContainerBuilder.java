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
 * $Id: ContainerBuilder.java 445025 2004-11-09 22:23:37Z dblevins $
 */
package org.apache.openejb.assembler;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.transaction.UserTransaction;

import org.apache.geronimo.naming.java.ReadOnlyContext;
import org.apache.openejb.EJBComponentType;
import org.apache.openejb.OpenEJBException;

public class ContainerBuilder implements RpcContainer {

    private Object containerId = null;
    private HashMap deployments = new HashMap();

    public void init(Object containerId, HashMap deploymentsMap, Properties properties)
            throws OpenEJBException {

        setupJndi();


        this.containerId = containerId;

        Object[] deploys = deploymentsMap.values().toArray();

        for (int i = 0; i < deploys.length; i++) {
            CoreDeploymentInfo info = (CoreDeploymentInfo) deploys[i];
            deploy(info.getDeploymentID(), info);
        }
    }

    private void setupJndi() {
        /* Add Geronimo JNDI service ///////////////////// */
        String str = System.getProperty(javax.naming.Context.URL_PKG_PREFIXES);
        if (str == null)
            str = ":org.apache.geronimo.naming";
        else
            str = str + ":org.apache.geronimo.naming";
        System.setProperty(javax.naming.Context.URL_PKG_PREFIXES, str);
    }

    public Object invoke(
            Object deployID,
            Method callMethod,
            Object[] args,
            Object primKey,
            Object securityIdentity)
            throws OpenEJBException {
        return null;
    }

    public int getContainerType() {
        return EJBComponentType.STATELESS;
    }

    public org.apache.openejb.assembler.DeploymentInfo getDeploymentInfo(Object deploymentID) {
        return (DeploymentInfoWrapper) deployments.get(deploymentID);
    }

    public org.apache.openejb.assembler.DeploymentInfo[] deployments() {
        return (CoreDeploymentInfo[]) deployments.values().toArray(new DeploymentInfoWrapper[0]);

    }

    public void deploy(Object deploymentID, org.apache.openejb.assembler.DeploymentInfo info)
            throws OpenEJBException {
        ((org.apache.openejb.assembler.CoreDeploymentInfo) info).setContainer(this);
        deployments.put(info.getDeploymentID(), new DeploymentInfoWrapper(info));
    }

    public Object getContainerID() {
        return containerId;
    }

    static class DeploymentInfoWrapper extends CoreDeploymentInfo {

        public DeploymentInfoWrapper(org.apache.openejb.assembler.DeploymentInfo deploymentInfo) {
            this((CoreDeploymentInfo) deploymentInfo);
        }

        public DeploymentInfoWrapper(CoreDeploymentInfo di) {
        }

    }


    static class ReadOnlyContextWrapper extends ReadOnlyContext {
        public ReadOnlyContextWrapper(Context ctx, UserTransaction userTransaction) throws NamingException {
            super();
            NamingEnumeration e = ctx.list("");

            while (e.hasMoreElements()) {
                NameClassPair pair = (NameClassPair) e.next();

                String name = pair.getName();
                Object value = ctx.lookup(name);

                internalBind(name, value);
            }

            if (userTransaction != null) {
                internalBind("UserTransaction", userTransaction);
            }
        }
    }
}
