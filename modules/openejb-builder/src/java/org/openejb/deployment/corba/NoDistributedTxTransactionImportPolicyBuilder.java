/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.openejb.deployment.corba;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Method;

import org.openejb.transaction.TransactionPolicySource;
import org.openejb.transaction.TransactionPolicyType;
import org.openejb.dispatch.InterfaceMethodSignature;
import org.openejb.corba.transaction.OperationTxPolicy;
import org.openejb.corba.transaction.ServerTransactionPolicyConfig;
import org.openejb.corba.transaction.MappedServerTransactionPolicyConfig;
import org.openejb.corba.transaction.nodistributedtransactions.NoDTxServerTransactionPolicies;

/**
 * @version $Rev:  $ $Date$
 */
public class NoDistributedTxTransactionImportPolicyBuilder implements TransactionImportPolicyBuilder {

    public Serializable buildTransactionImportPolicy(String methodIntf, Class intf, boolean isHomeMethod, TransactionPolicySource transactionPolicySource) {
        Map policies = new HashMap();
        Method[]  methods = intf.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            InterfaceMethodSignature interfaceMethodSignature = new InterfaceMethodSignature(method, isHomeMethod);
            TransactionPolicyType transactionPolicyType = transactionPolicySource.getTransactionPolicy(methodIntf, interfaceMethodSignature);
            OperationTxPolicy operationTxPolicy = NoDTxServerTransactionPolicies.getTransactionPolicy(transactionPolicyType);
            String IDLOperationName = getIDLOperationName(interfaceMethodSignature);
            policies.put(IDLOperationName, operationTxPolicy);
        }
        ServerTransactionPolicyConfig serverTransactionPolicyConfig = new MappedServerTransactionPolicyConfig(policies);

        return serverTransactionPolicyConfig;
    }

    private String getIDLOperationName(InterfaceMethodSignature interfaceMethodSignature) {
        return interfaceMethodSignature.getMethodName();
    }
}
