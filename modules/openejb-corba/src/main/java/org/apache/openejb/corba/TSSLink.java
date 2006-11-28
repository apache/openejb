/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.openejb.corba;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.lang.reflect.Method;

import org.apache.openejb.EJBInterfaceType;
import org.apache.openejb.RpcEjbDeployment;
import org.apache.openejb.proxy.ProxyInfo;
import org.apache.openejb.corba.util.Util;
import org.apache.openejb.corba.transaction.ServerTransactionPolicyConfig;
import org.apache.openejb.corba.transaction.OperationTxPolicy;
import org.apache.openejb.corba.transaction.MappedServerTransactionPolicyConfig;
import org.apache.openejb.corba.transaction.nodistributedtransactions.NoDTxServerTransactionPolicies;
import org.apache.openejb.transaction.TransactionPolicyType;
import org.apache.geronimo.gbean.GBeanLifecycle;

/**
 * @version $Rev$ $Date$
 */
public class TSSLink implements GBeanLifecycle {
    private final TSSBean tssBean;
    private final RpcEjbDeployment ejb;
    private final String[] jndiNames;

    public TSSLink() {
        tssBean = null;
        ejb = null;
        jndiNames = null;
    }

    public TSSLink(String[] jndiNames, TSSBean tssBean, RpcEjbDeployment ejb) {
        if (tssBean == null) {
            throw new NullPointerException("No TSSBean supplied");
        }
        if (ejb == null) {
            throw new NullPointerException("No ejb supplied");
        }
        this.jndiNames = jndiNames;
        this.tssBean = tssBean;
        this.ejb = ejb;
    }

    public void doStart() throws Exception {
        if (tssBean != null) {
            tssBean.registerContainer(this);
        }
    }

    public void doStop() throws Exception {
        destroy();
    }

    public void doFail() {
        destroy();
    }

    protected void destroy() {
        if (tssBean != null) {
            tssBean.unregisterContainer(this);
        }
    }

    public RpcEjbDeployment getDeployment() {
        return ejb;
    }

    public String getContainerId() {
        return ejb.getContainerId();
    }

    public String[] getJndiNames() {
        return jndiNames;
    }

    /**
     * CORBA home transaction import policy configuration
     * @return home transaction import policy
     */
    public Serializable getHomeTxPolicyConfig() {
        if (getProxyInfo().getHomeInterface() == null) {
            return null;
        }
        Serializable policy = buildTransactionImportPolicy(EJBInterfaceType.HOME, getProxyInfo().getHomeInterface(), true);
        return policy;
    }

    /**
     * CORBA remote transaction import policy configuration
     * @return remote transaction import policy
     */
    public Serializable getRemoteTxPolicyConfig() {
        if (getProxyInfo().getRemoteInterface() == null) {
            return null;
        }
        Serializable policy = buildTransactionImportPolicy(EJBInterfaceType.REMOTE, getProxyInfo().getRemoteInterface(), false);
        return policy;
    }

    private Serializable buildTransactionImportPolicy(EJBInterfaceType methodIntf, Class intf, boolean isHomeMethod) {

        Map policies = new HashMap();

        Map methodToOperation = Util.mapMethodToOperation(intf);
        for (Iterator iterator = methodToOperation.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            Method method = (Method) entry.getKey();
            String operation = (String) entry.getValue();

            int index = ejb.getMethodIndex(method);
            if (index > -1) {
                TransactionPolicyType transactionPolicyType = ejb.getTransactionPolicyManager().getTransactionPolicyType(methodIntf, index);
                OperationTxPolicy operationTxPolicy = NoDTxServerTransactionPolicies.getTransactionPolicy(transactionPolicyType);
                policies.put(operation, operationTxPolicy);
            }
        }
        ServerTransactionPolicyConfig serverTransactionPolicyConfig = new MappedServerTransactionPolicyConfig(policies);

        return serverTransactionPolicyConfig;
    }

    ProxyInfo getProxyInfo() {
        return ejb.getProxyInfo();
    }

}
