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
package org.apache.openejb.corba.transaction.nodistributedtransactions;

import java.io.Serializable;

import org.apache.openejb.corba.transaction.OperationTxPolicy;
import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CosTransactions.PropagationContext;

/**
 * Use for:
 * Required
 * Never
 * Supports
 *
 * @version $Rev$ $Date$
 */
public class NotRequired implements OperationTxPolicy, Serializable {
    public static final OperationTxPolicy INSTANCE = new NotRequired();

    public void importTransaction(PropagationContext propagationContext) {
        if (propagationContext != null) {
            throw new INVALID_TRANSACTION("Transactions cannot be imported, and might also be not allowed for this method");
        }
    }
}
