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
 * $Id$
 */
package org.openejb.corba.security;

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.omg.CORBA.Any;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.INV_POLICY;
import org.omg.CORBA.LocalObject;
import org.omg.CSI.MTCompleteEstablishContext;
import org.omg.CSI.MTContextError;
import org.omg.CSI.MTEstablishContext;
import org.omg.CSI.MTMessageInContext;
import org.omg.CSI.SASContextBody;
import org.omg.CSI.SASContextBodyHelper;
import org.omg.IOP.CodecPackage.FormatMismatch;
import org.omg.IOP.CodecPackage.TypeMismatch;
import org.omg.IOP.SecurityAttributeService;
import org.omg.IOP.ServiceContext;
import org.omg.PortableInterceptor.InvalidSlot;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;
import org.openorb.orb.net.AbstractServerRequest;

import org.apache.geronimo.security.ContextManager;

import org.openejb.corba.security.config.tss.TSSConfig;
import org.openejb.corba.util.Util;


/**
 * @version $Revision$ $Date$
 */
final class ServerSecurityInterceptor extends LocalObject implements ServerRequestInterceptor {

    private final Log log = LogFactory.getLog(ServerSecurityInterceptor.class);

    private final int slotId;
    private final Subject defaultSubject;

    public ServerSecurityInterceptor(int slotId, Subject defaultSubject) {
        this.slotId = slotId;
        this.defaultSubject = defaultSubject;

        if (defaultSubject != null) ContextManager.registerSubject(defaultSubject);
        AbstractServerRequest.disableServiceContextExceptions();
    }

    public void receive_request(ServerRequestInfo ri) {

        Subject identity = null;

        try {
            ServerPolicy serverPolicy = (ServerPolicy) ri.get_server_policy(ServerPolicyFactory.POLICY_TYPE);
            TSSConfig tssPolicy = serverPolicy.getConfig();
            if (tssPolicy == null) return;

            ServiceContext serviceContext = ri.get_request_service_context(SecurityAttributeService.value);
            if (serviceContext == null) return;

            Any any = Util.getCodec().decode_value(serviceContext.context_data, SASContextBodyHelper.type());
            SASContextBody contextBody = SASContextBodyHelper.extract(any);

            short msgType = contextBody.discriminator();
            switch (msgType) {
                case MTEstablishContext.value:
                    identity = tssPolicy.check(SSLSessionManager.getSSLSession(ri.request_id()), contextBody.establish_msg());

                    ContextManager.registerSubject(identity);

                    break;

                case MTCompleteEstablishContext.value:
                    log.error("The CSIv2 TSS is not supposed to receive a CompleteEstablishContext message.");
                    throw new INTERNAL("The CSIv2 TSS is not supposed to receive a CompleteEstablishContext message.");

                case MTContextError.value:
                    log.error("The CSIv2 TSS is not supposed to receive a CompleteEstablishContext message.");
                    throw new INTERNAL("The CSIv2 TSS is not supposed to receive a ContextError message.");

                case MTMessageInContext.value:
                    log.error("The CSIv2 TSS is not supposed to receive a CompleteEstablishContext message.");
                    throw new INTERNAL("MessageInContext is currently not supported by this implementation.");
            }

        } catch (INV_POLICY e) {
            identity = defaultSubject;
        } catch (TypeMismatch tm) {
            log.error("TypeMismatch thrown", tm);
            throw new INTERNAL("TypeMismatch thrown: " + tm);
        } catch (FormatMismatch fm) {
            log.error("FormatMismatch thrown", fm);
            throw new INTERNAL("FormatMismatch thrown: " + fm);
        }

        if (identity != null) {
            try {
                ContextManager.setCurrentCaller(identity);
                ContextManager.setNextCaller(identity);

                Any subjectAny = ri.get_slot(slotId);
                subjectAny.insert_Value(identity);
                ri.set_slot(slotId, subjectAny);

                SubjectManager.setSubject(ri.request_id(), identity);
            } catch (InvalidSlot is) {
                log.error("InvalidSlot thrown", is);
                throw new INTERNAL("InvalidSlot thrown: " + is);
            }
        }
    }

    public void receive_request_service_contexts(ServerRequestInfo ri) {
    }

    public void send_exception(ServerRequestInfo ri) {
        try {
            Any subjectAny = ri.get_slot(slotId);
//            Subject identity = (Subject) subjectAny.extract_Value();
            Subject identity = SubjectManager.clearSubject(ri.request_id());

            if (identity != null && identity != defaultSubject) ContextManager.unregisterSubject(identity);
        } catch (InvalidSlot is) {
            log.error("InvalidSlot thrown", is);
            throw new INTERNAL("InvalidSlot thrown: " + is);
        }
    }

    public void send_other(ServerRequestInfo ri) {
    }

    public void send_reply(ServerRequestInfo ri) {
        try {
            Any subjectAny = ri.get_slot(slotId);
//            Subject identity = (Subject) subjectAny.extract_Value();
            Subject identity = SubjectManager.clearSubject(ri.request_id());

            if (identity != null && identity != defaultSubject) ContextManager.unregisterSubject(identity);
        } catch (InvalidSlot is) {
            log.error("InvalidSlot thrown", is);
            throw new INTERNAL("InvalidSlot thrown: " + is);
        }
    }

    public void destroy() {
        if (defaultSubject != null) ContextManager.unregisterSubject(defaultSubject);
    }

    public String name() {
        return "org.openejb.corba.security.ServerSecurityInterceptor";
    }
}
