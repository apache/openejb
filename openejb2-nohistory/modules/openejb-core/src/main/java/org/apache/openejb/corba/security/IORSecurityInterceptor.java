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
 * $Id: IORSecurityInterceptor.java 445359 2005-03-27 02:04:42Z adc $
 */
package org.apache.openejb.corba.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.omg.CORBA.INV_POLICY;
import org.omg.CORBA.LocalObject;
import org.omg.IOP.TAG_INTERNET_IOP;
import org.omg.PortableInterceptor.IORInfo;
import org.omg.PortableInterceptor.IORInterceptor;

import org.apache.openejb.corba.util.Util;


/**
 * @version $Revision$ $Date$
 */
final class IORSecurityInterceptor extends LocalObject implements IORInterceptor {

    private final Log log = LogFactory.getLog(IORSecurityInterceptor.class);

    public void establish_components(IORInfo info) {

        try {
            ServerPolicy policy = (ServerPolicy) info.get_effective_policy(ServerPolicyFactory.POLICY_TYPE);

            if (policy == null || policy.getConfig() == null) return;

            info.add_ior_component_to_profile(policy.getConfig().generateIOR(Util.getORB(), Util.getCodec()), TAG_INTERNET_IOP.value);
        } catch (INV_POLICY e) {
            // do nothing
        } catch (Exception e) {
            log.error("Generating IOR", e);
        }
    }

    public void destroy() {
    }

    public String name() {
        return "org.openejb.corba.security.IORSecurityInterceptor";
    }

}
