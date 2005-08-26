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

package org.openejb.core;

import org.openejb.RpcContainer;
import org.openejb.OpenEJBException;
import org.openejb.core.transaction.TransactionContainer;

import javax.ejb.EnterpriseBean;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Properties;

/**
 * @version $Revision$ $Date$
 */
public class RpcContainerWrapper implements RpcContainer, TransactionContainer {

    private final RpcContainer container;

    public RpcContainerWrapper(RpcContainer container) {
        this.container = container;
    }

    public Object invoke(Object deployID, Method callMethod, Object [] args, Object primKey, Object securityIdentity) throws OpenEJBException {
        return container.invoke(deployID, callMethod, args, primKey, securityIdentity);
    }

    public void init(Object containerId, HashMap deployments, Properties properties) throws OpenEJBException {
        container.init(containerId, deployments, properties);
    }

    public int getContainerType( ) {
        return container.getContainerType();
    }

    public Object getContainerID() {
        return container.getContainerID();
    }

    public org.openejb.DeploymentInfo getDeploymentInfo(Object deploymentID) {
        return container.getDeploymentInfo(deploymentID);
    }

    public org.openejb.DeploymentInfo [] deployments() {
        return container.deployments();
    }

    public void deploy(Object deploymentID, org.openejb.DeploymentInfo info) throws OpenEJBException {
        container.deploy(deploymentID, info);
    }

    public void discardInstance(EnterpriseBean instance, ThreadContext context) {
        ((TransactionContainer)container).discardInstance(instance, context);
    }

    public RpcContainer getContainer() {
        return container;
    }

}
