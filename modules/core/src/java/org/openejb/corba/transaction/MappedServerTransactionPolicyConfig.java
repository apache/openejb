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

import java.util.Map;

import org.omg.CORBA.BAD_OPERATION;
import org.omg.CORBA.SystemException;
import org.openejb.corba.idl.CosTransactions.PropagationContext;

/**
 * @version $Rev:  $ $Date$
 */
public class MappedServerTransactionPolicyConfig extends AbstractServerTransactionPolicyConfig {
    private final Map operationToPolicyMap;
    public MappedServerTransactionPolicyConfig(Map operationToPolicyMap) {
        this.operationToPolicyMap = operationToPolicyMap;
    }

    protected void importTransaction(String operation, PropagationContext propagationContext) throws SystemException {
        //TODO TOTAL HACK WARNING FIXME!!
        int pos = operation.indexOf("__");
        if (pos > -1) {
            operation = operation.substring(0, pos);
        }

        OperationTxPolicy operationTxPolicy = (OperationTxPolicy) operationToPolicyMap.get(operation);
        if (operationTxPolicy == null) {
            throw new BAD_OPERATION("Operation " + operation + " not recognized, no tx mapping");
        }
        operationTxPolicy.importTransaction(propagationContext);
     }

}
