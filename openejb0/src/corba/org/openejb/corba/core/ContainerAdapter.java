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

import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.openejb.util.proxy.InvocationHandler;
import org.openejb.util.proxy.ProxyManager;

/**
 * This is a common base class for derived classes that wrap an RPCContainer.
 *
 * @author Stefan Reich sreich@apple.com
 */
public abstract class ContainerAdapter {
    protected final static org.openejb.util.Logger logger = org.openejb.util.Logger.getInstance("CORBA-Adapter", "org.openejb.util.resources");

    protected final org.openejb.RpcContainer cntr;
    /**
      * keeps track of the CORBA object references for EJBhomes. Keys are deployment ids, value
     * is the CORBA object references
     */
    protected final java.util.Hashtable homes_ejb;
    
    protected final org.openejb.corba.core.ContainerSystem system;

    /**
      * Creates a new CORBA adapter class around an OpenEJB container. All home interfaces are activated
      * on homePOA, and exported to the name service specified by nameServiceRootContext.
     */
    public ContainerAdapter( org.omg.CORBA.ORB orb, POA homePOA,
                             org.omg.CosNaming.NamingContextExt nameServiceRootContext,
                             org.openejb.RpcContainer cntr,
                             ContainerSystem system ) throws org.openejb.OpenEJBException
    {
        this.cntr = cntr;
        this.system = system;
        homes_ejb = new java.util.Hashtable();
        exportHomeInterfaces(nameServiceRootContext, homePOA);
    }

    /**
      * Loads the RMI Tie class for a fully qualified class name.
      * @param name fully qualified class name
      * @returns the Servant class
      * @throws java.lang.ClassNotFoundException
     * @throws java.lang.InstantiationException
     * @throws java.lang.IllegalAccessException
     */
    protected Servant loadTieClass(String name) throws java.lang.ClassNotFoundException, java.lang.InstantiationException, java.lang.IllegalAccessException {
        int namepos=name.lastIndexOf('.');
        String packagename, classname;
        if(namepos==-1) {
            packagename="";
            classname=name;
        } else {
            packagename=name.substring(0, namepos+1);
            classname=name.substring(namepos+1);
        }
        String stubName=packagename+"_"+classname+"_Tie";
        return (Servant) this.getClass().forName(stubName).newInstance();
    }

    /**
      * Creates the specific subclass of the invocation handler to be used in the POA that dispatches
      * all CORBA requests to EJBHomes. To be overriden by subclasses.
      */
    protected abstract InvocationHandler createHomeInvocationHandler(Object deploymentId);

    /**
      * This operation exports all home interfaces in this container to the naming service.
     */
    private void exportHomeInterfaces(NamingContextExt ns, POA homePOA) {
        // -- Retrieve all home interfaces and bind them to the naming service --

        org.openejb.DeploymentInfo [] dinfo = cntr.deployments();
        for ( int i=0; i<dinfo.length; i++ ) {
            org.openejb.DeploymentInfo di = dinfo[i];

            String home_name = di.getDeploymentID().toString();
            byte [] home_id = home_name.getBytes();
            InvocationHandler handler = createHomeInvocationHandler(di.getDeploymentID());
            logger.info("ContainerAdapter: Exporting a home to : " + home_name );

            try {
                Object homeProxy = ProxyManager.newProxyInstance( new Class[]{ di.getHomeInterface()} , handler );

                Servant servant = loadTieClass(di.getHomeInterface().getName());
                if(servant instanceof javax.rmi.CORBA.Tie) {
                    ((javax.rmi.CORBA.Tie)servant).setTarget((java.rmi.Remote)homeProxy );
                }
                homePOA.activate_object_with_id( home_id, servant );
                org.omg.CORBA.Object homeReference = homePOA.servant_to_reference( servant );
                // register the reference, and narrow it once, instead of at each access
                homes_ejb.put(di.getDeploymentID(), javax.rmi.PortableRemoteObject.narrow(homeReference, javax.ejb.EJBHome.class));

                NameComponent[] nameComponent = ns.to_name ( home_name );
                NamingContext currentContext = ns;
                NameComponent[] nc = new NameComponent[1];
                int lastComponent=nameComponent.length-1;
                for(int j=0; j<lastComponent; ++j) {
                    nc[0]=nameComponent[j];
                    try{
                        currentContext = NamingContextHelper.narrow(currentContext.resolve(nc));
                    }catch(org.omg.CosNaming.NamingContextPackage.NotFound nf) {
                        currentContext = currentContext.bind_new_context(nc);
                    }
                }
                nc[0]=nameComponent[lastComponent];
                currentContext.rebind( nc, homeReference );
                ns.resolve(nameComponent);
            }catch(Exception e) {
                logger.fatal( "ContainerAdapter: Unable to export home interface " + home_name , e);
            }
        }
    }
    

    /**
     * creates a new CORBA object reference for a EJBHome or EJBObject. This method
     * is used by the {@link org.openejb.corba.core.SerializationHandler} to translate
     * IntraVM data structed into ones that can be marshalled with CORBA.
     * CONTRACT: this method return an implementation of either javax.ejb.EJBHome
     * or javax.ejb.EJBObject narrowed to these types.
     * 
     * @param pinfo the data structure describing the EJBHome or EJBObject
     * @returns a CORBA object reference
     */
    public abstract java.lang.Object createBeanProfile(org.openejb.ProxyInfo pinfo) throws Exception;
}
