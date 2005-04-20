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

import org.apache.geronimo.transaction.context.TransactionContext;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.omg.CORBA.Any;
import org.omg.CORBA.INTERNAL;
import org.omg.CosTransactions.PropagationContext;
import org.omg.CosTransactions.PropagationContextHelper;
import org.omg.CosTransactions.TransIdentity;
import org.omg.CosTransactions.otid_t;
import org.omg.IOP.Codec;
import org.omg.IOP.CodecPackage.InvalidTypeForEncoding;
import org.omg.IOP.ServiceContext;
import org.omg.IOP.TransactionService;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.openejb.corba.transaction.ClientTransactionPolicyConfig;
import org.openejb.corba.util.Util;

/**
 * @version $Rev:  $ $Date$
 */
public class NoDTxClientTransactionPolicyConfig implements ClientTransactionPolicyConfig {

    private static final TransIdentity[] NO_PARENTS = new TransIdentity[0];
    private static final otid_t NULL_XID = new otid_t(0, 0, new byte[0]);

    private final TransactionContextManager transactionContextManager;

    public NoDTxClientTransactionPolicyConfig(TransactionContextManager transactionContextManager) {
        if (transactionContextManager == null) {
            throw new IllegalArgumentException("transactionContextManager must not be null");
        }
        this.transactionContextManager = transactionContextManager;
    }

    public void exportTransaction(ClientRequestInfo ri) {
        TransactionContext transactionContext = transactionContextManager.getContext();
        if (transactionContext != null && transactionContext.isInheritable() && transactionContext.isActive()) {
            //19.6.2.1 (1) propagate an "empty" transaction context.
            //but, it needs an xid!
            TransIdentity transIdentity = new TransIdentity(null, null, NULL_XID);
            int timeout = 0;
            Any implementationSpecificData = Util.getORB().create_any();
            PropagationContext propagationContext = new PropagationContext(timeout, transIdentity, NO_PARENTS, implementationSpecificData);
            Codec codec = Util.getCodec();
            Any any = Util.getORB().create_any();
            PropagationContextHelper.insert(any, propagationContext);
            byte[] encodedPropagationContext;
            try {
                encodedPropagationContext = codec.encode_value(any);
            } catch (InvalidTypeForEncoding invalidTypeForEncoding) {
                throw (INTERNAL)new INTERNAL("Could not encode propagationContext").initCause(invalidTypeForEncoding);
            }
            ServiceContext otsServiceContext = new ServiceContext(TransactionService.value, encodedPropagationContext);
            ri.add_request_service_context(otsServiceContext, true);
        }

    }
}
