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

import org.omg.CORBA.Any;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.SystemException;
import org.omg.CosTransactions.PropagationContext;
import org.omg.CosTransactions.PropagationContextHelper;
import org.omg.IOP.Codec;
import org.omg.IOP.CodecPackage.FormatMismatch;
import org.omg.IOP.CodecPackage.TypeMismatch;
import org.omg.IOP.ServiceContext;
import org.omg.IOP.TransactionService;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.openejb.corba.util.Util;


/**
 * @version $Rev:  $ $Date$
 */
public abstract class AbstractServerTransactionPolicyConfig implements ServerTransactionPolicyConfig {

    public void importTransaction(ServerRequestInfo serverRequestInfo) throws SystemException {
        ServiceContext serviceContext = null;
        try {
            serviceContext = serverRequestInfo.get_request_service_context(TransactionService.value);
        } catch (BAD_PARAM e) {
            // do nothing
        }
        PropagationContext propagationContext;
        if (serviceContext == null) {
            propagationContext = null;
        } else {
            byte[] encoded = serviceContext.context_data;
            Codec codec = Util.getCodec();
            Any any;
            try {
                any = codec.decode_value(encoded, PropagationContextHelper.type());
            } catch (FormatMismatch formatMismatch) {
                throw (INTERNAL) new INTERNAL("Could not decode encoded propagation context").initCause(formatMismatch);
            } catch (TypeMismatch typeMismatch) {
                throw (INTERNAL) new INTERNAL("Could not decode encoded propagation context").initCause(typeMismatch);
            }
            propagationContext = PropagationContextHelper.extract(any);
        }
        //figure out what method is being invoked
        //operation name is unique... it contains the mangled operation name + arg types.
        String operation = serverRequestInfo.operation();
        importTransaction(operation, propagationContext);
    }

    protected abstract void importTransaction(String operation, PropagationContext propagationContext) throws SystemException;

}
