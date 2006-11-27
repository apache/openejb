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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.omg.CORBA.Any;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.ImplicitActivationPolicyValue;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantLocator;
import org.omg.PortableServer.ServantLocatorPackage.CookieHolder;
import org.omg.PortableServer.ServantRetentionPolicyValue;
import org.apache.openejb.RpcEjbDeployment;
import org.apache.openejb.EJBInterfaceType;
import org.apache.openejb.corba.transaction.ServerTransactionPolicyFactory;
import org.apache.openejb.proxy.ProxyInfo;


/**
 * @version $Revision$ $Date$
 */
public final class AdapterEntity extends Adapter {
    private final Log log = LogFactory.getLog(AdapterEntity.class);

    private final POA poa;
    private final String referenceInterface;

    public AdapterEntity(TSSLink tssLink, ORB orb, POA parentPOA, Policy securityPolicy) throws CORBAException {
        super(tssLink, orb, parentPOA, securityPolicy);

        Any any = orb.create_any();
        any.insert_Value(tssLink.getRemoteTxPolicyConfig());

        try {
            Policy[] policies = new Policy[]{
                securityPolicy,
                orb.create_policy(ServerTransactionPolicyFactory.POLICY_TYPE, any),
//                homePOA.create_lifespan_policy(LifespanPolicyValue.PERSISTENT),
                homePOA.create_request_processing_policy(RequestProcessingPolicyValue.USE_SERVANT_MANAGER),
                homePOA.create_servant_retention_policy(ServantRetentionPolicyValue.NON_RETAIN),
                homePOA.create_id_assignment_policy(IdAssignmentPolicyValue.USER_ID),
                homePOA.create_implicit_activation_policy(ImplicitActivationPolicyValue.NO_IMPLICIT_ACTIVATION),
            };
            poa = homePOA.create_POA(tssLink.getContainerId(), homePOA.the_POAManager(), policies);
            poa.set_servant_manager(new ObjectActivator());

            poa.the_POAManager().activate();

            StandardServant servant = new StandardServant(orb, EJBInterfaceType.REMOTE, tssLink.getDeployment());
            referenceInterface = servant._all_interfaces(null, null)[0];
        } catch (Exception e) {
            throw new CORBAException(e);
        }
    }

    public POA getPOA() {
        return poa;
    }

    public void stop() throws CORBAException {
        poa.destroy(true, true);
        super.stop();
    }

    public org.omg.CORBA.Object genObjectReference(ProxyInfo proxyInfo) throws CORBAException {

        byte[] bytes;
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(b);

            os.writeObject(proxyInfo.getPrimaryKey());
            bytes = b.toByteArray();

            os.close();
        } catch (IOException e) {
            log.error("Could not serialize deployment info for " + proxyInfo, e);
            throw new CORBAException(e);
        }
        return poa.create_reference_with_id(bytes, referenceInterface);
    }

    protected class ObjectActivator extends LocalObject implements ServantLocator {

        public Servant preinvoke(byte[] oid, POA poa, String operation, CookieHolder cookie) {
            // the byte array can be cached in a weak hash map for performance
            Object pk = null;

            try {
                ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(oid));
                pk = is.readObject();
                is.close();

                RpcEjbDeployment deployment = getDeployment();
                return new StandardServant(getOrb(), EJBInterfaceType.REMOTE, deployment, pk);
            } catch (IOException e) {
                // if we can't deserialize, then this object can't exist in this process
                throw new OBJECT_NOT_EXIST(0, org.omg.CORBA.CompletionStatus.COMPLETED_NO);
            } catch (Exception e) {
                log.error("Exception during dispatch to method " + operation + " in bean " + pk, e);
                return null;
            }
        }

        public void postinvoke(byte[] oid, POA poa, String operation, Object cookie, Servant servant) {
        }
    }
}
