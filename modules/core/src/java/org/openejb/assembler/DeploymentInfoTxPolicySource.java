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
package org.openejb.assembler;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.openejb.EJBComponentType;
import org.openejb.dispatch.InterfaceMethodSignature;
import org.openejb.transaction.BeanPolicy;
import org.openejb.transaction.ContainerPolicy;
import org.openejb.transaction.TransactionPolicy;
import org.openejb.transaction.TransactionPolicySource;

public class DeploymentInfoTxPolicySource implements TransactionPolicySource {
    private final Map policyMap = new HashMap();

    public DeploymentInfoTxPolicySource(CoreDeploymentInfo deployment) {
        Class remote = deployment.getRemoteInterface();
        policyMap.put("Remote", buildRemotePolicyMap(deployment, remote));

        Class home = deployment.getHomeInterface();
        policyMap.put("Home", buildHomePolicyMap(deployment, home));
    }

    public TransactionPolicy getTransactionPolicy(String methodIntf, InterfaceMethodSignature signature) {
        Map policies = (Map)policyMap.get(methodIntf);
        return (policies == null)? null: (TransactionPolicy)policies.get(signature);
    }

    private static Map buildRemotePolicyMap(CoreDeploymentInfo deployment, Class remoteInterface) {
        Map policies = new HashMap();

        Method[] methods = remoteInterface.getMethods();

        for (int i = 0; i < methods.length; i++) {
            InterfaceMethodSignature signature = new InterfaceMethodSignature(methods[i], false);
            policies.put(signature, getTransactionPolicy(deployment, methods[i]));
        }
        return policies;
    }

    private static Map buildHomePolicyMap(CoreDeploymentInfo deployment, Class homeInterface) {
        Map policies = new HashMap();

        Method[] methods = homeInterface.getMethods();

        for (int i = 0; i < methods.length; i++) {
            InterfaceMethodSignature signature = new InterfaceMethodSignature(methods[i], true);
            policies.put(signature, getTransactionPolicy(deployment, methods[i]));
        }
        return policies;
    }

    private static TransactionPolicy getTransactionPolicy(CoreDeploymentInfo deployment, Method method) {
        switch (deployment.getTransactionAttribute(method)) {
            case CoreDeploymentInfo.TX_MANDITORY: return ContainerPolicy.Mandatory;
            case CoreDeploymentInfo.TX_NEVER: return ContainerPolicy.Never;
            case CoreDeploymentInfo.TX_NOT_SUPPORTED: return ContainerPolicy.NotSupported;
            case CoreDeploymentInfo.TX_REQUIRED: return ContainerPolicy.Required;
            case CoreDeploymentInfo.TX_REQUIRES_NEW: return ContainerPolicy.RequiresNew;
            case CoreDeploymentInfo.TX_SUPPORTS: return ContainerPolicy.Supports;
            default: return (deployment.getComponentType() == EJBComponentType.STATELESS)?BeanPolicy.Stateless:BeanPolicy.Stateful;
        }
    }

}
