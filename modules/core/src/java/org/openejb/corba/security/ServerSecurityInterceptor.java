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

import javax.security.cert.X509Certificate;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLPeerUnverifiedException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.omg.CORBA.Any;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.INV_POLICY;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.UserException;
import org.omg.CSI.MTCompleteEstablishContext;
import org.omg.CSI.MTContextError;
import org.omg.CSI.MTEstablishContext;
import org.omg.CSI.MTMessageInContext;
import org.omg.CSI.SASContextBody;
import org.omg.CSI.SASContextBodyHelper;
import org.omg.IOP.Codec;
import org.omg.IOP.SecurityAttributeService;
import org.omg.IOP.ServiceContext;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;
import org.openorb.orb.net.AbstractServerRequest;

import org.openejb.corba.security.wrappers.EstablishContextWrapper;
import org.openejb.corba.util.Util;


/**
 * @version $Revision$ $Date$
 */
final class ServerSecurityInterceptor extends LocalObject implements ServerRequestInterceptor {

    private final Log log = LogFactory.getLog(ServerSecurityInterceptor.class);

    public ServerSecurityInterceptor() {
        AbstractServerRequest.disableServiceContextExceptions();
    }

    public void receive_request(ServerRequestInfo ri) throws ForwardRequest {
        try {
            SSLSession session = SSLSessionManager.getSSLSession(ri.request_id());
            X509Certificate[] chain = session.getPeerCertificateChain();
            String host = session.getPeerHost();

            ServerPolicy policy = (ServerPolicy) ri.get_server_policy(ServerPolicyFactory.POLICY_TYPE);
            if (policy.getConfig() == null) return;
            ri.toString();

            ServiceContext serviceContext = ri.get_request_service_context(SecurityAttributeService.value);
            if (serviceContext == null) return;

            Codec codec = Util.getCodec();
            Any any = codec.decode_value(serviceContext.context_data, SASContextBodyHelper.type());
            SASContextBody contextBody = SASContextBodyHelper.extract(any);

            short msgType = contextBody.discriminator();
            switch (msgType) {
                case MTEstablishContext.value:
                    EstablishContextWrapper establishMsg = new EstablishContextWrapper(contextBody.establish_msg());

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
            // do nothing
        } catch (UserException ue) {
            log.error("UserException thrown", ue);
            throw new INTERNAL("UserException thrown: " + ue);
        } catch (SSLPeerUnverifiedException e) {
            // do nothing
        }
    }

    public void receive_request_service_contexts(ServerRequestInfo ri) {
    }

    public void send_exception(ServerRequestInfo ri) {
    }

    public void send_other(ServerRequestInfo ri) {
    }

    public void send_reply(ServerRequestInfo ri) {
    }

    public void destroy() {
    }

    public String name() {
        return "org.openejb.corba.security.ServerSecurityInterceptor";
    }
}
