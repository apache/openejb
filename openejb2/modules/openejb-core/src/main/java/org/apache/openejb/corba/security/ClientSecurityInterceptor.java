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
 * $Id: ClientSecurityInterceptor.java 445735 2005-09-25 03:28:19Z djencks $
 */
package org.apache.openejb.corba.security;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.LocalObject;
import org.omg.IOP.ServiceContext;
import org.omg.IOP.TAG_CSI_SEC_MECH_LIST;
import org.omg.IOP.TaggedComponent;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;

import org.apache.openejb.corba.ClientContextManager;
import org.apache.openejb.corba.security.config.css.CSSCompoundSecMechConfig;
import org.apache.openejb.corba.security.config.css.CSSConfig;
import org.apache.openejb.corba.security.config.tss.TSSCompoundSecMechListConfig;
import org.apache.openejb.corba.util.Util;


/**
 * @version $Revision$ $Date$
 */
final class ClientSecurityInterceptor extends LocalObject implements ClientRequestInterceptor {

    private final Log log = LogFactory.getLog(ClientSecurityInterceptor.class);

    public ClientSecurityInterceptor() {
        if (log.isDebugEnabled()) log.debug("Registered");
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
            if (log.isDebugEnabled()) log.debug("Checking if target " + ri.operation() + " has a security policy");

            TaggedComponent tc = ri.get_effective_component(TAG_CSI_SEC_MECH_LIST.value);
            TSSCompoundSecMechListConfig csml = TSSCompoundSecMechListConfig.decodeIOR(Util.getCodec(), tc);

            if (log.isDebugEnabled()) log.debug("Target has a security policy");

            CSSConfig config = ClientContextManager.getClientContext().getSecurityConfig();
            if (config == null) return;

            if (log.isDebugEnabled()) log.debug("Client has a security policy");

            List compat = config.findCompatibleSet(csml);

            if (compat.size() == 0) return;

            if (log.isDebugEnabled()) log.debug("Found compatible policy");

            ServiceContext context = ((CSSCompoundSecMechConfig) compat.get(0)).generateServiceContext();

            if (context == null) return;

            if (log.isDebugEnabled()) {
                log.debug("Msg context id: " + context.context_id);
                log.debug("Encoded msg: 0x" + Util.byteToString(context.context_data));
            }

            ri.add_request_service_context(context, true);
        } catch (BAD_PARAM bp) {
            // do nothing
        } catch (Exception ue) {
            log.error("Exception", ue);
        }
    }

    public void destroy() {
    }

    public String name() {
        return "org.openejb.corba.security.ClientSecurityInterceptor";
    }
}