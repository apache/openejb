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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import org.omg.CORBA.Policy;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.ImplicitActivationPolicyValue;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantRetentionPolicyValue;
import org.omg.PortableServer.ServantLocatorPackage.CookieHolder;
import org.openejb.core.stateful.StatefulEjbHomeHandler;
import org.openejb.core.stateful.StatefulEjbObjectHandler;
import org.openejb.util.proxy.InvocationHandler;
import org.openejb.util.proxy.ProxyManager;

/**
 * This class manages CORBA references and dispatches method on behalf of an stateful session container.
 *
 * @author Stefan Reich sreich@apple.com
 */
public class StatefulContainerAdapter extends ContainerAdapter{

    protected class ObjectActivator extends org.omg.CORBA.LocalObject
            implements org.omg.PortableServer.ServantLocator {

        public Servant preinvoke(byte [] oid, POA poa, String operation, CookieHolder cookie) {
            // the byte array can be cached in a weak hash map for performance            
            Object pk =null;
            Object deploymentId =null;;
            try{
                ObjectInputStream is= new ObjectInputStream(new ByteArrayInputStream(oid));
                pk = is.readObject();
                deploymentId = is.readObject();
                is.close();

                InvocationHandler handler =  createObjectInvocationHandler(pk, deploymentId);
                org.openejb.DeploymentInfo di = cntr.getDeploymentInfo(deploymentId);
                Object objHandler = ProxyManager.newProxyInstance( new Class[]{ di.getRemoteInterface()} , handler );
                String remote = di.getRemoteInterface().getName();

                Servant servant = loadTieClass(remote);
                if(servant instanceof javax.rmi.CORBA.Tie) {
                    ((javax.rmi.CORBA.Tie)servant).setTarget((java.rmi.Remote)objHandler );
                }
                return servant;
            }catch(java.io.IOException e){
                // if we can't deserialize, then this object can't exist in this process
                throw new org.omg.CORBA.OBJECT_NOT_EXIST( 0, org.omg.CORBA.CompletionStatus.COMPLETED_NO );
            } catch(Exception e) {
                logger.error("Exception during dispatch to method "+operation+" in bean "+pk, e);
                return null;
            }
        }

        public void postinvoke (byte [] oid, POA poa, String operation, Object cookie, Servant servant) {
        }
    }
    
    protected final POA poa;
    protected final HashMap repositoryidForDeploymentid;

    public StatefulContainerAdapter( org.omg.CORBA.ORB orb, POA homePOA,
                             org.omg.CosNaming.NamingContextExt nameServiceRootContext,
                             org.openejb.RpcContainer cntr,
                             ContainerSystem initializer ) throws org.openejb.OpenEJBException
    {
        super(orb, homePOA, nameServiceRootContext, cntr, initializer);
        try {
            org.omg.CORBA.Object obj = orb.resolve_initial_references("RootPOA");
            POA rootPOA = org.omg.PortableServer.POAHelper.narrow( obj );
            Policy[] policies= poaPolicies(rootPOA);
            
            poa = rootPOA.create_POA(cntr.getContainerID().toString(), null, policies);
            poa.set_servant_manager(new ObjectActivator());

            repositoryidForDeploymentid = new HashMap();

            // initialize repositoryidForDeploymentid
            org.openejb.DeploymentInfo [] dinfo = cntr.deployments();
            for ( int i=0; i<dinfo.length; i++ ) {
                String remote = dinfo[i].getRemoteInterface().getName();
                Servant servant = loadTieClass(remote);
                // register most derived interface
                repositoryidForDeploymentid.put(dinfo[i].getDeploymentID(), servant._all_interfaces(null, null)[0]);
            }            

            poa.the_POAManager().activate();
        }catch(Exception e) {
            logger.fatal("POA creation failed: ", e);
            throw new org.openejb.OpenEJBException("POA creation failed", e);
        }            
    }

    /**
      * returns the POA policies that will be used by the constructor to create the POA.
      * To be overriden by subclasses to specify different POA policies without duplicating POA creation
      */
    protected Policy[] poaPolicies(POA poa) {
        return new Policy[] {
            poa.create_lifespan_policy(LifespanPolicyValue.TRANSIENT),
            poa.create_request_processing_policy(RequestProcessingPolicyValue.USE_SERVANT_MANAGER),
            poa.create_servant_retention_policy(ServantRetentionPolicyValue.NON_RETAIN),
            poa.create_id_assignment_policy(IdAssignmentPolicyValue.USER_ID),
            poa.create_implicit_activation_policy(ImplicitActivationPolicyValue.NO_IMPLICIT_ACTIVATION),
        };
    }

    /**
      * Creates the specific subclass of the invocation handler to be used in the servant locator that dispatches
     * incoming CORBA requests to individual EJBObjects. To be overriden by subclasses.
     */
    protected InvocationHandler createObjectInvocationHandler(Object primaryKey, Object deploymentId) {
        StatefulEjbObjectHandler handler = new StatefulEjbObjectHandler(cntr, primaryKey, deploymentId);
        // let's not copy argument and return value unnecessarily
        handler.setIntraVmCopyMode(false);
        return handler;
    }

    protected InvocationHandler createHomeInvocationHandler(Object deploymentId) {
        StatefulEjbHomeHandler handler = new StatefulEjbHomeHandler(cntr, null, deploymentId);
        // let's not copy argument and return value unnecessarily
        handler.setIntraVmCopyMode(false);
        return handler;
    }
    
    /**
        * This operation is used to create a new bean profile. It creates a servant and a proxy for this bean.
     */
    public java.lang.Object createBeanProfile( org.openejb.ProxyInfo pinfo) throws Exception {
        java.lang.Object deploymentid=pinfo.getDeploymentInfo().getDeploymentID();

        java.lang.Object obj;
        if ( javax.ejb.EJBObject.class.isAssignableFrom( pinfo.getInterface() ) ) {
            byte[] bytes=null;
            try{
                // stuff all info needed to access the bean into the object key
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                ObjectOutputStream os= new ObjectOutputStream(b);
                os.writeObject(pinfo.getPrimaryKey());
                os.writeObject(deploymentid);
                bytes = b.toByteArray();
                os.close();
            }catch(java.io.IOException e) {
                logger.error("Could not serialize deployment info for "+pinfo, e);
                throw e;
            }
            obj = poa.create_reference_with_id(bytes, (String)repositoryidForDeploymentid.get(deploymentid));
            // and narrow it...
            obj = javax.rmi.PortableRemoteObject.narrow(obj, javax.ejb.EJBObject.class);
        } else if (javax.ejb.EJBHome.class.isAssignableFrom( pinfo.getInterface())) {
            obj = homes_ejb.get( deploymentid );
        } else {
            logger.error("Interface "+pinfo.getInterface()+" does not extend either EJBObject or EJBHome.");
            throw new IllegalStateException();
        }
        return obj;
    }
}
