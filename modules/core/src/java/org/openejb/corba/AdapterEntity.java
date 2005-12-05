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
package org.openejb.corba;

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
import org.openejb.EJBContainer;
import org.openejb.EJBInterfaceType;
import org.openejb.corba.transaction.ServerTransactionPolicyFactory;
import org.openejb.proxy.ProxyInfo;


/**
 * @version $Revision$ $Date$
 */
public final class AdapterEntity extends Adapter {
    private final Log log = LogFactory.getLog(AdapterEntity.class);

    private final POA poa;
    private final String referenceInterface;

    public AdapterEntity(EJBContainer container, ORB orb, POA parentPOA, Policy securityPolicy) throws CORBAException {
        super(container, orb, parentPOA, securityPolicy);

        Any any = orb.create_any();
        any.insert_Value(container.getRemoteTxPolicyConfig());

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
            poa = homePOA.create_POA(container.getContainerID().toString(), homePOA.the_POAManager(), policies);
            poa.set_servant_manager(new ObjectActivator());

            poa.the_POAManager().activate();

            StandardServant servant = new StandardServant(orb, EJBInterfaceType.REMOTE, container);
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

        byte[] bytes = null;
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

                EJBContainer container = getContainer();
                StandardServant servant = new StandardServant(getOrb(), EJBInterfaceType.REMOTE, container, pk);
                return servant;
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
