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

import java.util.List;

import org.omg.CORBA.LocalObject;
import org.omg.IOP.ServiceContext;
import org.omg.IOP.TAG_CSI_SEC_MECH_LIST;
import org.omg.IOP.TaggedComponent;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.openorb.orb.net.AbstractServerRequest;

import org.openejb.corba.security.config.css.CSSCompoundSecMechConfig;
import org.openejb.corba.security.config.css.CSSConfig;
import org.openejb.corba.security.config.tss.TSSCompoundSecMechListConfig;
import org.openejb.corba.util.Util;


/**
 * @version $Revision$ $Date$
 */
final class ClientSecurityInterceptor extends LocalObject implements ClientRequestInterceptor {

    public ClientSecurityInterceptor() {
        AbstractServerRequest.disableServiceContextExceptions();
    }

    public void receive_exception(ClientRequestInfo ri) {
    }

    public void receive_other(ClientRequestInfo ri) {
    }

    public void receive_reply(ClientRequestInfo ri) {
    }

    public void send_poll(ClientRequestInfo ri) {
    }

    public void send_request(ClientRequestInfo ri) {
        try {
            TaggedComponent tc = ri.get_effective_component(TAG_CSI_SEC_MECH_LIST.value);
            TSSCompoundSecMechListConfig csml = TSSCompoundSecMechListConfig.decodeIOR(Util.getCodec(), tc);

            ClientPolicy policy = (ClientPolicy) ri.get_request_policy(ClientPolicyFactory.POLICY_TYPE);
            if (policy.getConfig() == null) return;

            CSSConfig config = policy.getConfig();
            List compat = config.findCompatibleSet(csml);

            if (compat.size() == 0) return;

            ServiceContext context = ((CSSCompoundSecMechConfig) compat.get(0)).generateServiceContext();

            ri.add_request_service_context(context, true);
        } catch (Exception ue) {
        }
    }

    public void destroy() {
    }

    public String name() {
        return "org.openejb.corba.security.ClientSecurityInterceptor";
    }
}