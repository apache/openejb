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
package org.apache.openejb.corba.transaction;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.omg.CORBA.SystemException;
import org.omg.CosTransactions.PropagationContext;

/**
 * @version $Rev$ $Date$
 */
public class MappedServerTransactionPolicyConfig extends AbstractServerTransactionPolicyConfig {
    private static Log log = LogFactory.getLog(MappedServerTransactionPolicyConfig.class);
    private final Map operationToPolicyMap;
    public MappedServerTransactionPolicyConfig(Map operationToPolicyMap) {
        this.operationToPolicyMap = operationToPolicyMap;
    }

    protected void importTransaction(String operation, PropagationContext propagationContext) throws SystemException {
        OperationTxPolicy operationTxPolicy = (OperationTxPolicy) operationToPolicyMap.get(operation);
        if (operationTxPolicy == null) {
            //TODO figure out if there is some way to detect if the method should be mapped or shouldn't
            //e.g. _is_a shows up but should not be mapped.
            log.info("No tx mapping for operation: " + operation);
            return;
        }
        operationTxPolicy.importTransaction(propagationContext);
     }

}
