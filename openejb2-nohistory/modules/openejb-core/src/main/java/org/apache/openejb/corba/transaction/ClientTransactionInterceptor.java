/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact openejb-group@openejb.sf.net.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.sf.net/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id: ClientTransactionInterceptor.java 445416 2005-04-20 02:28:57Z djencks $
 */
package org.apache.openejb.corba.transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.omg.CORBA.Any;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.LocalObject;
import org.omg.IOP.CodecPackage.FormatMismatch;
import org.omg.IOP.CodecPackage.TypeMismatch;
import org.omg.IOP.TaggedComponent;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.CosTSInteroperation.TAG_OTS_POLICY;
import org.omg.CosTransactions.OTSPolicyValueHelper;
import org.omg.CosTransactions.ADAPTS;

import org.apache.openejb.corba.ClientContextManager;
import org.apache.openejb.corba.util.Util;
import org.apache.openejb.corba.util.TypeCode;


/**
 * @version $Revision$ $Date$
 */
class ClientTransactionInterceptor extends LocalObject implements ClientRequestInterceptor {

    private final Log log = LogFactory.getLog(ClientTransactionInterceptor.class);

    public ClientTransactionInterceptor() {
        if (log.isDebugEnabled()) log.debug("Registered");
    }

    public void receive_exception(ClientRequestInfo ri) throws ForwardRequest {
    }

    public void receive_other(ClientRequestInfo ri) throws ForwardRequest {
    }

    public void receive_reply(ClientRequestInfo ri) {
    }

    public void send_poll(ClientRequestInfo ri) {
    }

    public void send_request(ClientRequestInfo ri) throws ForwardRequest {
        TaggedComponent taggedComponent = null;
        try {
            if (log.isDebugEnabled()) log.debug("Checking if target " + ri.operation() + " has a transaction policy");

            taggedComponent = ri.get_effective_component(TAG_OTS_POLICY.value);
        } catch (BAD_PARAM e) {
            return;
        }

        if (log.isDebugEnabled()) log.debug("Target has a transaction policy");

        byte[] data = taggedComponent.component_data;
        Any any = null;
        try {
            any = Util.getCodec().decode_value(data, TypeCode.SHORT);
        } catch (FormatMismatch formatMismatch) {
            log.error("Mismatched format", formatMismatch);
            throw (INTERNAL) new INTERNAL("Mismatched format").initCause(formatMismatch);
        } catch (TypeMismatch typeMismatch) {
            log.error("Type mismatch", typeMismatch);
            throw (INTERNAL) new INTERNAL("Type mismatch").initCause(typeMismatch);
        }

        short value = OTSPolicyValueHelper.extract(any);
        if (value == ADAPTS.value) {
            ClientTransactionPolicyConfig clientTransactionPolicyConfig = ClientContextManager.getClientContext().getTransactionConfig();
            if (clientTransactionPolicyConfig == null) return;

            if (log.isDebugEnabled()) log.debug("Client has a transaction policy");

            clientTransactionPolicyConfig.exportTransaction(ri);
        }
    }

    public void destroy() {
    }

    /**
     * Returns the name of the interceptor.
     * <p/>
     * Each Interceptor may have a name that may be used administratively
     * to order the lists of Interceptors. Only one Interceptor of a given
     * name can be registered with the ORB for each Interceptor type. An
     * Interceptor may be anonymous, i.e., have an empty string as the name
     * attribute. Any number of anonymous Interceptors may be registered with
     * the ORB.
     *
     * @return the name of the interceptor.
     */
    public String name() {
        return "org.openejb.corba.transaction.ClientTransactionInterceptor";
    }
}