/*
 * Copyright  2002, Apple Computer, Inc. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * 1.  Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.  
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.  
 * 3.  Neither the name of Apple Computer, Inc. ("Apple")
 * nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY APPLE AND ITS CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL APPLE OR ITS
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */

package org.openejb.corba.core;

import org.omg.CORBA.Policy;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.ImplicitActivationPolicyValue;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.ServantRetentionPolicyValue;
import org.openejb.core.entity.EntityEjbHomeHandler;
import org.openejb.core.entity.EntityEjbObjectHandler;
import org.openejb.util.proxy.InvocationHandler;

/**
 * This class manages CORBA references and dispatches method on behalf of an entity container.
 *
 * @author Stefan Reich sreich@apple.com
 */
public class EntityContainerAdapter extends StatefulContainerAdapter{

    public EntityContainerAdapter( org.omg.CORBA.ORB orb, POA homePOA,
                             org.omg.CosNaming.NamingContextExt nameServiceRootContext,
                             org.openejb.RpcContainer cntr,
                             ContainerSystem system ) throws org.openejb.OpenEJBException
    {
        super(orb, homePOA, nameServiceRootContext, cntr, system);
    }

    /**
    * entity bean references are supposed to be long lived, and are therefore activated on a
     * POA with a persistent liefespan policy
     */
    protected Policy[] poaPolicies(POA poa) {
        return new Policy[] {
            poa.create_lifespan_policy(LifespanPolicyValue.PERSISTENT),
            poa.create_request_processing_policy(RequestProcessingPolicyValue.USE_SERVANT_MANAGER),
            poa.create_servant_retention_policy(ServantRetentionPolicyValue.NON_RETAIN),
            poa.create_id_assignment_policy(IdAssignmentPolicyValue.USER_ID),
            poa.create_implicit_activation_policy(ImplicitActivationPolicyValue.NO_IMPLICIT_ACTIVATION),
        };
    }

    protected InvocationHandler createObjectInvocationHandler(Object primaryKey, Object deploymentId) {
        EntityEjbObjectHandler handler = new EntityEjbObjectHandler(cntr, primaryKey, deploymentId);
        // let's not copy argument and return value unnecessarily
        handler.setIntraVmCopyMode(false);
        return handler;
    }
    
    protected InvocationHandler createHomeInvocationHandler(Object deploymentId) {
        EntityEjbHomeHandler handler = new EntityEjbHomeHandler(cntr, null, deploymentId);
        // let's not copy argument and return value unnecessarily
        handler.setIntraVmCopyMode(false);
        return handler;
    }
    
}
