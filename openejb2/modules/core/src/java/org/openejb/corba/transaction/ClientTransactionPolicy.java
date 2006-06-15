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
package org.openejb.corba.transaction;

import org.omg.CORBA.LocalObject;
import org.omg.CORBA.Policy;

/**
 * @version $Rev:  $ $Date$
 */
public class ClientTransactionPolicy extends LocalObject implements Policy {

    private final ClientTransactionPolicyConfig clientTransactionPolicyConfig;

    public ClientTransactionPolicy(ClientTransactionPolicyConfig clientTransactionPolicyConfig) {
        this.clientTransactionPolicyConfig = clientTransactionPolicyConfig;
    }


    public int policy_type() {
        return ClientTransactionPolicyFactory.POLICY_TYPE;
    }

    public Policy copy() {
        return new ClientTransactionPolicy(clientTransactionPolicyConfig);
    }

    public void destroy() {

    }

    ClientTransactionPolicyConfig getClientTransactionPolicyConfig() {
        return clientTransactionPolicyConfig;
    }
}
