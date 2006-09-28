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
package org.apache.openejb.corba.transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.omg.CORBA.Any;
import org.omg.CORBA.INV_POLICY;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.ORB;
import org.omg.CosTSInteroperation.TAG_INV_POLICY;
import org.omg.CosTransactions.ADAPTS;
import org.omg.CosTransactions.SHARED;
import org.omg.IOP.TAG_INTERNET_IOP;
import org.omg.IOP.TAG_OTS_POLICY;
import org.omg.IOP.TaggedComponent;
import org.omg.PortableInterceptor.IORInfo;
import org.omg.PortableInterceptor.IORInterceptor;
import org.apache.openejb.corba.util.Util;


/**
 * @version $Revision$ $Date$
 */
final class IORTransactionInterceptor extends LocalObject implements IORInterceptor {

    private final Log log = LogFactory.getLog(IORTransactionInterceptor.class);

    public void establish_components(IORInfo info) {

        try {
            Any invAny = ORB.init().create_any();
            invAny.insert_short(SHARED.value);
            byte[] invBytes = Util.getCodec().encode_value(invAny);
            TaggedComponent invocationPolicyComponent = new TaggedComponent(TAG_INV_POLICY.value, invBytes);
            info.add_ior_component_to_profile(invocationPolicyComponent, TAG_INTERNET_IOP.value);

            Any otsAny = ORB.init().create_any();
            otsAny.insert_short(ADAPTS.value);
            byte[] otsBytes = Util.getCodec().encode_value(otsAny);
            TaggedComponent otsPolicyComponent = new TaggedComponent(TAG_OTS_POLICY.value, otsBytes);
            info.add_ior_component_to_profile(otsPolicyComponent, TAG_INTERNET_IOP.value);
        } catch (INV_POLICY e) {
            // do nothing
        } catch (Exception e) {
            log.error("Generating IOR", e);
        }
    }

    public void destroy() {
    }

    public String name() {
        return "org.apache.openejb.corba.transaction.IORTransactionInterceptor";
    }

}
