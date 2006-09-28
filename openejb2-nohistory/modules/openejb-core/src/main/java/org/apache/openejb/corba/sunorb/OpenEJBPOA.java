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
 * Copyright 2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id: OpenEJBPOA.java 445396 2005-04-12 21:29:56Z adc $
 */
package org.apache.openejb.corba.sunorb;

import com.sun.corba.se.internal.POA.POAImpl;
import com.sun.corba.se.internal.POA.POAManagerImpl;
import com.sun.corba.se.internal.POA.POAORB;
import com.sun.corba.se.internal.POA.Policies;
import com.sun.corba.se.internal.corba.CORBAObjectImpl;
import com.sun.corba.se.internal.core.ClientSubcontract;
import com.sun.corba.se.internal.core.IOR;
import com.sun.corba.se.internal.core.StandardIIOPProfileTemplate;
import com.sun.corba.se.internal.ior.IIOPAddressImpl;
import com.sun.corba.se.internal.ior.IORTemplate;
import com.sun.corba.se.internal.ior.ObjectId;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.omg.CORBA.portable.Delegate;
import org.omg.CORBA.portable.ObjectImpl;
import org.omg.PortableServer.AdapterActivator;

import org.apache.openejb.corba.security.ServerPolicy;
import org.apache.openejb.corba.security.ServerPolicyFactory;


/**
 * This class basically intercepts the creation of IORs and checks to see if
 * CSIv2 transport security has been defined.  If it has, then the port of the
 * profile must be zero, indicating that only the transport defined in the
 * CSIv2 component should be used.
 *
 * @version $Revision$ $Date$
 */
public class OpenEJBPOA extends POAImpl {

    private final static Log log = LogFactory.getLog(OpenEJBPOA.class);

    public OpenEJBPOA(String name, POAManagerImpl manager, Policies policies, POAImpl parent, AdapterActivator activator, POAORB orb) {
        super(name, manager, policies, parent, activator, orb);

        if (log.isDebugEnabled()) log.debug("<init>");
    }

    public POAImpl makePOA(String name, POAManagerImpl manager, Policies policies, POAImpl parent, AdapterActivator activator, POAORB orb) {
        if (log.isDebugEnabled()) log.debug("makePOA()");

        return new OpenEJBPOA(name, manager, policies, parent, activator, orb);
    }

    protected org.omg.CORBA.Object makeObjectReference(String repId, byte[] id, IORTemplate iortemp, int scid) {

        if (log.isDebugEnabled()) log.debug("makeObjectReference()");

        IORTemplate template = null;

        ServerPolicy policy = (ServerPolicy) get_effective_policy(ServerPolicyFactory.POLICY_TYPE);
        if (policy != null
            && policy.getConfig() != null
            && policy.getConfig().getTransport_mech().getRequires() > 0)
        {
            if (log.isDebugEnabled()) log.debug("Found security policy");

            template = new IORTemplate();

            for (int i = 0; i < iortemp.size(); i++) {
                Object obj = iortemp.get(i);

                if (obj instanceof StandardIIOPProfileTemplate) {
                    StandardIIOPProfileTemplate stdTemp = (StandardIIOPProfileTemplate) obj;
                    IIOPAddressImpl primaryAddress = (IIOPAddressImpl) stdTemp.getPrimaryAddress();

                    StandardIIOPProfileTemplate newTemp = new StandardIIOPProfileTemplate(new IIOPAddressImpl(primaryAddress.getHost(), 0),
                                                                                          stdTemp.getMajorVersion(), stdTemp.getMinorVersion(),
                                                                                          stdTemp.getObjectKeyTemplate(),
                                                                                          null,
                                                                                          orb);
                    newTemp.clear();
                    newTemp.addAll(stdTemp);

                    if (stdTemp.isImmutable()) newTemp.makeImmutable();

                    obj = newTemp;
                }

                template.add(obj);
            }
        } else {
            template = iortemp;
        }

        IOR ior = new IOR(orb, repId, template, new ObjectId(id));

        ClientSubcontract csub = orb.getSubcontractRegistry().getClientSubcontract(scid);
        csub.setOrb(orb);
        csub.unmarshal(ior);

        ObjectImpl o = new CORBAObjectImpl();
        o._set_delegate((Delegate) csub);

        return o;
    }
}
