/**
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package org.apache.openejb.sunorb;

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
