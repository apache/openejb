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
 * Copyright 2004-2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.apache.openejb.corba;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.ImplicitActivationPolicyValue;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAPackage.ObjectNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.ServantRetentionPolicyValue;
import org.apache.openejb.RpcEjbDeployment;
import org.apache.openejb.EJBInterfaceType;
import org.apache.openejb.corba.transaction.ServerTransactionPolicyFactory;
import org.apache.openejb.proxy.ProxyInfo;

/**
 * @version $Revision$ $Date$
 */
public final class AdapterStateless extends Adapter {
    private final POA poa;
    private final byte[] object_id;
    private final org.omg.CORBA.Object objectReference;

    public AdapterStateless(RpcEjbDeployment deployment, ORB orb, POA parentPOA, Policy securityPolicy) throws CORBAException {
        super(deployment, orb, parentPOA, securityPolicy);
        Any any = orb.create_any();
        any.insert_Value(deployment.getRemoteTxPolicyConfig());

        try {
            Policy[] policies = new Policy[]{
                securityPolicy,
                orb.create_policy(ServerTransactionPolicyFactory.POLICY_TYPE, any),
//                homePOA.create_lifespan_policy(LifespanPolicyValue.TRANSIENT),
                homePOA.create_request_processing_policy(RequestProcessingPolicyValue.USE_ACTIVE_OBJECT_MAP_ONLY),
                homePOA.create_servant_retention_policy(ServantRetentionPolicyValue.RETAIN),
                homePOA.create_id_assignment_policy(IdAssignmentPolicyValue.USER_ID),
                homePOA.create_implicit_activation_policy(ImplicitActivationPolicyValue.NO_IMPLICIT_ACTIVATION),
            };
            poa = homePOA.create_POA(deployment.getContainerId().toString(), homePOA.the_POAManager(), policies);

            poa.the_POAManager().activate();

            StandardServant servant = new StandardServant(orb, EJBInterfaceType.REMOTE, deployment);

            poa.activate_object_with_id(object_id = deployment.getContainerId().toString().getBytes(), servant);
            objectReference = poa.servant_to_reference(servant);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CORBAException(e);
        }
    }

    public void stop() throws CORBAException {
        try {
            poa.deactivate_object(object_id);
            poa.destroy(true, true);
            super.stop();
        } catch (ObjectNotActive e) {
            throw new CORBAException(e);
        } catch (WrongPolicy e) {
            throw new CORBAException(e);
        }
    }

    public org.omg.CORBA.Object genObjectReference(ProxyInfo proxyInfo) {
        return objectReference;
    }
}
