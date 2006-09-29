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
