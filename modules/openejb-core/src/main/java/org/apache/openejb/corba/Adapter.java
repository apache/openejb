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
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextHelper;
import org.omg.CosNaming.NamingContextPackage.NotEmpty;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.ImplicitActivationPolicyValue;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.ServantRetentionPolicyValue;
import org.apache.openejb.RpcEjbDeployment;
import org.apache.openejb.EJBInterfaceType;
import org.apache.openejb.corba.transaction.ServerTransactionPolicyFactory;
import org.apache.openejb.proxy.ProxyInfo;

/**
 * @version $Revision$ $Date$
 */
public abstract class Adapter implements RefGenerator {
    private final RpcEjbDeployment deployment;
    protected final POA homePOA;
    protected final ORB orb;
    private final NamingContextExt initialContext;
    private final byte[] home_id;
    private final org.omg.CORBA.Object homeReference;

    protected Adapter(RpcEjbDeployment deployment, ORB orb, POA parentPOA, Policy securityPolicy) throws CORBAException {
        this.deployment = deployment;
        this.home_id = deployment.getContainerId().toString().getBytes();
        this.orb = orb;

        Any any = orb.create_any();
        any.insert_Value(deployment.getHomeTxPolicyConfig());

        try {
            Policy[] policies = new Policy[]{
                securityPolicy,
                orb.create_policy(ServerTransactionPolicyFactory.POLICY_TYPE, any),
//                parentPOA.create_lifespan_policy(LifespanPolicyValue.TRANSIENT),
                parentPOA.create_request_processing_policy(RequestProcessingPolicyValue.USE_ACTIVE_OBJECT_MAP_ONLY),
                parentPOA.create_servant_retention_policy(ServantRetentionPolicyValue.RETAIN),
                parentPOA.create_id_assignment_policy(IdAssignmentPolicyValue.USER_ID),
                parentPOA.create_implicit_activation_policy(ImplicitActivationPolicyValue.NO_IMPLICIT_ACTIVATION),
            };
            homePOA = parentPOA.create_POA(deployment.getContainerId().toString(), parentPOA.the_POAManager(), policies);

            homePOA.the_POAManager().activate();

            StandardServant servant = new StandardServant(orb, EJBInterfaceType.HOME, deployment);

            homePOA.activate_object_with_id(home_id, servant);
            homeReference = homePOA.servant_to_reference(servant);

            org.omg.CORBA.Object obj = orb.resolve_initial_references("NameService");
            initialContext = NamingContextExtHelper.narrow(obj);
            String[] names = deployment.getJndiNames();
            for (int i = 0; i < names.length; i++) {
                NameComponent[] nameComponent = initialContext.to_name(names[i]);
                NamingContext currentContext = initialContext;
                NameComponent[] nc = new NameComponent[1];
                int lastComponent = nameComponent.length - 1;
                for (int j = 0; j < lastComponent; ++j) {
                    nc[0] = nameComponent[j];
                    try {
                        currentContext = NamingContextHelper.narrow(currentContext.resolve(nc));
                    } catch (NotFound nf) {
                        currentContext = currentContext.bind_new_context(nc);
                    }
                }
                nc[0] = nameComponent[lastComponent];
                currentContext.rebind(nc, homeReference);
            }
        } catch (Exception e) {
            throw new CORBAException(e);
        }

    }

    public RpcEjbDeployment getDeployment() {
        return deployment;
    }

    public NamingContextExt getInitialContext() {
        return initialContext;
    }

    public org.omg.CORBA.Object getHomeReference() {
        return homeReference;
    }

    public ORB getOrb() {
        return orb;
    }

    public void stop() throws CORBAException {
        try {
            String[] names = deployment.getJndiNames();
            for (int i = 0; i < names.length; i++) {
                NameComponent[] nameComponent = initialContext.to_name(names[i]);
                initialContext.unbind(nameComponent);

                for (int j = nameComponent.length - 1; 0 < j; --j) {
                    NameComponent[] nc = new NameComponent[j];
                    System.arraycopy(nameComponent, 0, nc, 0, j);
                    NamingContext currentContext = NamingContextHelper.narrow(initialContext.resolve(nc));
                    try {
                        currentContext.destroy();
                    } catch (NotEmpty ne) {
                        break;
                    }
                }
            }

            homePOA.deactivate_object(home_id);
            homePOA.destroy(true, true);
        } catch (Exception e) {
            throw new CORBAException(e);
        }
    }

    public org.omg.CORBA.Object genHomeReference(ProxyInfo proxyInfo) throws CORBAException {
        return this.getHomeReference();
    }
}
