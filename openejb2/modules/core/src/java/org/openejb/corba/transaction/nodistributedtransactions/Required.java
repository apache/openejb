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

import java.io.Serializable;

import org.openejb.corba.transaction.OperationTxPolicy;
import org.openejb.corba.idl.CosTransactions.PropagationContext;
import org.omg.CORBA.INVALID_TRANSACTION;

/**
 * Use for:
 * Mandatory
 *
 * @version $Rev:  $ $Date$
 */
public class Required implements OperationTxPolicy, Serializable {

    public static final OperationTxPolicy INSTANCE = new Required();
    
    public void importTransaction(PropagationContext propagationContext) {
        throw new INVALID_TRANSACTION("Transaction cannot be imported"); 
    }
}
