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

import java.util.Hashtable;

import org.omg.CORBA.Policy;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.ImplicitActivationPolicyValue;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantRetentionPolicyValue;
import org.openejb.core.stateless.StatelessEjbHomeHandler;
import org.openejb.core.stateless.StatelessEjbObjectHandler;
import org.openejb.util.proxy.InvocationHandler;
import org.openejb.util.proxy.ProxyManager;

/**
 * This class manages CORBA references and dispatches method on behalf of an entity container.
 *
 * @author Stefan Reich sreich@apple.com
 */
public class StatelessContainerAdapter extends ContainerAdapter{
    private final org.omg.PortableServer.POA poa;
    private final Hashtable beans_ejb;


    /**
     * This class overrides the remove() operation of StatelessEjbObjectHandler. For this adapter we
     * don't want that the remove() method invalidates the handler, because the handlers are activated
     * directly on the POA, and are used for the lifetime of the container.
     */
    protected static class ObjectHandler extends StatelessEjbObjectHandler {
        public ObjectHandler(org.openejb.RpcContainer container, Object pk, Object depID){
            super(container, pk, depID);
        }

        /**
         * Checks authoriziation and simply returns.
         */
        protected Object remove(java.lang.reflect.Method method, Object[] args, Object proxy) throws Throwable{
            checkAuthorization(method);
            return null;
        }        
    }

    public StatelessContainerAdapter( org.omg.CORBA.ORB orb, POA homePOA,
                             org.omg.CosNaming.NamingContextExt nameServiceRootContext,
                             org.openejb.RpcContainer cntr,
                             ContainerSystem system ) throws org.openejb.OpenEJBException
    {
        super(orb, homePOA, nameServiceRootContext, cntr, system);
        try {
            org.omg.CORBA.Object obj = orb.resolve_initial_references("RootPOA");
            POA rootPOA = org.omg.PortableServer.POAHelper.narrow( obj );
            Policy[] policies= new Policy[] {
                rootPOA.create_lifespan_policy(LifespanPolicyValue.TRANSIENT),
                rootPOA.create_request_processing_policy(RequestProcessingPolicyValue.USE_ACTIVE_OBJECT_MAP_ONLY),
                rootPOA.create_servant_retention_policy(ServantRetentionPolicyValue.RETAIN),
                rootPOA.create_id_assignment_policy(IdAssignmentPolicyValue.USER_ID),
                rootPOA.create_implicit_activation_policy(ImplicitActivationPolicyValue.NO_IMPLICIT_ACTIVATION),
            };
            poa = rootPOA.create_POA(cntr.getContainerID().toString(), null, policies);

            beans_ejb=new Hashtable();
            // activate all beans. We handle all beans with only one servant.
            org.openejb.DeploymentInfo [] dinfo = cntr.deployments();
            for ( int i=0; i<dinfo.length; i++ ) {
                org.openejb.DeploymentInfo di = dinfo[i];
                byte[] objectId = di.getDeploymentID().toString().getBytes();
                String remote = di.getRemoteInterface().getName();
                InvocationHandler handler = createObjectInvocationHandler(null, di.getDeploymentID());
                Object objHandler = ProxyManager.newProxyInstance( new Class[]{ di.getRemoteInterface()} , handler );
                Servant servant = loadTieClass(remote);
                if(servant instanceof javax.rmi.CORBA.Tie) {
                    ((javax.rmi.CORBA.Tie)servant).setTarget((java.rmi.Remote)objHandler );
                }
                poa.activate_object_with_id( objectId, servant );
                org.omg.CORBA.Object corbaRef = poa.servant_to_reference(servant);
                // narrow here once to avoid many narrows later
                Object ejbObject = javax.rmi.PortableRemoteObject.narrow(corbaRef, javax.ejb.EJBObject.class);
                beans_ejb.put(di.getDeploymentID(), ejbObject);
            }            

            poa.the_POAManager().activate();
        }catch(Exception e) {
            logger.fatal("POA creation failed: ", e);
            throw new org.openejb.OpenEJBException("POA creation failed", e);
        }            
    }

    /**
     * Creates the specific subclass of the invocation handler to be used in the servant locator that dispatches
     * incoming CORBA requests to individual EJBObjects. To be overriden by subclasses.
     */
    protected InvocationHandler createObjectInvocationHandler(Object primaryKey, Object deploymentId) {
        ObjectHandler handler = new ObjectHandler(cntr, primaryKey, deploymentId);
        // let's not copy argument and return value unnecessarily
        handler.setIntraVmCopyMode(false);
        return handler;
    }
    
    protected InvocationHandler createHomeInvocationHandler(Object deploymentId) {
        StatelessEjbHomeHandler handler =  new StatelessEjbHomeHandler(cntr, null, deploymentId);
        // let's not copy argument and return value unnecessarily
        handler.setIntraVmCopyMode(false);
        return handler;
    }    

    public java.lang.Object createBeanProfile( org.openejb.ProxyInfo pinfo) throws Exception {
        java.lang.Object deploymentid=pinfo.getDeploymentInfo().getDeploymentID();
        java.lang.Object obj;
        if ( javax.ejb.EJBObject.class.isAssignableFrom( pinfo.getInterface() ) ) {
            obj = beans_ejb.get(deploymentid);
        } else if (javax.ejb.EJBHome.class.isAssignableFrom( pinfo.getInterface())) {
            obj = homes_ejb.get( deploymentid );
        } else {
            logger.error("Interface "+pinfo.getInterface()+" does not extend either EJBObject or EJBHome.");
            throw new IllegalStateException();
        }
        return obj;   
    }


}
