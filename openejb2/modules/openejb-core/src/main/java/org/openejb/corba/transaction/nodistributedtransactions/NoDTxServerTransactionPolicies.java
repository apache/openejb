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
package org.openejb.corba.transaction.nodistributedtransactions;

import org.openejb.corba.transaction.OperationTxPolicy;
import org.openejb.transaction.TransactionPolicyType;

/**
 * @version $Rev$ $Date$
 */
public class NoDTxServerTransactionPolicies {
    private static final OperationTxPolicy[] policies = new OperationTxPolicy[TransactionPolicyType.size()];
    static {
        policies[TransactionPolicyType.Mandatory.getIndex()] = Required.INSTANCE;
        policies[TransactionPolicyType.Never.getIndex()] = NotRequired.INSTANCE;
        policies[TransactionPolicyType.NotSupported.getIndex()] = Ignore.INSTANCE;
        policies[TransactionPolicyType.Required.getIndex()] = NotRequired.INSTANCE;
        policies[TransactionPolicyType.RequiresNew.getIndex()] = Ignore.INSTANCE;
        policies[TransactionPolicyType.Supports.getIndex()] = NotRequired.INSTANCE;
        policies[TransactionPolicyType.Bean.getIndex()] = Ignore.INSTANCE;
    }

    public static OperationTxPolicy getTransactionPolicy(TransactionPolicyType transactionPolicyType) {
        return policies[transactionPolicyType.getIndex()];
    }

}
