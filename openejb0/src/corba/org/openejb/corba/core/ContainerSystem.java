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


/**
 * This class initializes the container system. It creates the POA on which all
 * home references are activated and wraps each RpcContainer with a ContainerAdapter
 *
 * @author Stefan Reich sreich@apple.com
 */
public class ContainerSystem {
    private final org.omg.CORBA.ORB orb;

    private org.omg.PortableServer.POA homePOA;

    private final java.util.HashMap adapters;

    private final static org.openejb.util.Logger logger = org.openejb.util.Logger.getInstance("CORBA-Adapter", "org.openejb.util.resources");

    public ContainerSystem( org.omg.CORBA.ORB orb) {
        this.orb = orb;
        adapters = new java.util.HashMap();
    }

    /**
      * This operation returns the ContainerAdapter that manages the RPC container passed as argument.
     */
    public ContainerAdapter getContainerAdapter( org.openejb.RpcContainer rpc ) {
        return (ContainerAdapter)adapters.get(rpc);
    }


    /**
        * This operation starts the OpenEJB container system. Then it creates a container adapter for each
     * container.
     */
    public void initialize( java.util.Properties props ) throws org.openejb.OpenEJBException {
        // -- Creates the serializer extension --

        SerializationHandler serializer = new SerializationHandler(this);

        // -- Initializes OpenEJB --
        try {
            org.openejb.OpenEJB.init( props, serializer);
        }
        catch ( org.openejb.OpenEJBException ex ) {
            logger.fatal("org.openejb.corba.initializer.run: can't start the EJB container", ex);
            throw ex;
        }

        // -- Creates all Container Adapters --

        org.openejb.Container [] cntrs = org.openejb.OpenEJB.containers();

        // Now hook in our UtilDelegate to intercept RMI object activations
        UtilDelegateImpl.setAppServer(serializer);

        try {
            org.omg.CORBA.Object obj = orb.resolve_initial_references("RootPOA");
            POA rootPOA = org.omg.PortableServer.POAHelper.narrow( obj );
            Policy[] policies= new Policy[] {
                rootPOA.create_lifespan_policy(LifespanPolicyValue.PERSISTENT),
                rootPOA.create_request_processing_policy(RequestProcessingPolicyValue.USE_ACTIVE_OBJECT_MAP_ONLY),
                rootPOA.create_servant_retention_policy(ServantRetentionPolicyValue.RETAIN),
                rootPOA.create_id_assignment_policy(IdAssignmentPolicyValue.USER_ID),
                rootPOA.create_implicit_activation_policy(ImplicitActivationPolicyValue.NO_IMPLICIT_ACTIVATION),
            };
            homePOA = rootPOA.create_POA("EJBHome", null, policies);
        }catch(Exception e) {
            logger.fatal("org.openejb.corba.Server: POA creation failed: ", e);
            throw new org.openejb.OpenEJBException("org.openejb.corba.Server: POA creation failed", e);
        }
        org.omg.CosNaming.NamingContextExt nameServiceRootContext;
        try{
            nameServiceRootContext = org.omg.CosNaming.NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));
        }catch(org.omg.CORBA.ORBPackage.InvalidName nsnf) {
            throw new org.openejb.OpenEJBException("The Name Service is not properly configured on the ORB", nsnf);
        }

        for ( int i=0; i<cntrs.length; i++ ) {
            // -- Create the container adapter --
            ContainerAdapter adapter = AdapterFactory.createAdapter(orb, homePOA, nameServiceRootContext, (org.openejb.RpcContainer)cntrs[i], this );
            adapters.put(cntrs[i], adapter);
        }

        // Let's allow remote requests
        try{
            homePOA.the_POAManager().activate();
        }catch(org.omg.PortableServer.POAManagerPackage.AdapterInactive ai) {
            throw new org.openejb.OpenEJBException(ai);
        }
    }
}
