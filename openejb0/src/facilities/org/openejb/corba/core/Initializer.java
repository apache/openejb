/**
 * Redistribution and use of this software and associated
 * documentation ("Software"), with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright statements
 *    and notices.  Redistributions must also contain a copy of this
 *    document.
 *
 * 2. Redistributions in binary form must reproduce the above
 *    copyright notice, this list of conditions and the following
 *    disclaimer in the documentation and/or other materials provided
 *    with the distribution.
 *
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Intalio Inc.  For written permission, please
 *    contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Intalio Inc. Exolab is a registered trademark of
 *    Intalio Inc.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY INTALIO AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL INTALIO OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * Copyright 1999-2001 (c) Intalio,Inc. All Rights Reserved.
 *
 * $Id$
 *
 * Date         Author  Changes
 */

package org.openejb.corba.core;

/** 
 * This class is used to prepare all CORBA adapters. Each EJB Container is associated to a CORBA Adapter.
 *
 * @author Jerome DANIEL ( jdaniel@intalio.com )
 */
public class Initializer
{
	private org.omg.CORBA.ORB orb;
	
	private org.omg.PortableServer.POA poa;
	
	private java.util.Vector adapters;
	
	private String domainPath;
	
	private boolean bindNaming;
	
	/**
	 * Constructor. 
	 * Requires an ORB and a POA reference.
	 */
	public Initializer( org.omg.CORBA.ORB orb, org.omg.PortableServer.POA rootPOA, String domainPath, boolean bindNaming  )
	{
		this.orb = orb;
		this.poa = rootPOA;
		this.domainPath = domainPath;
		this.bindNaming = bindNaming;
		adapters = new java.util.Vector();
	}
	
	/**
	 * This operation returns the ContainerAdapter that manages the RPC container passed as argument.
	 */
	public ContainerAdapter getContainerAdapter( org.openejb.RpcContainer rpc )
	{
		for ( int i=0; i<adapters.size(); i++ )
		{
			ContainerAdapter adapter = ( ContainerAdapter ) adapters.elementAt( i );
			
			if ( adapter.manages( rpc ) )
				return adapter;
		}
		
		return null;
	}

	/**
 	 * This operation returns the ContainerAdapter that manages the CORBA object whom the object id is
	 * passed as parameter
	 */
	public ContainerAdapter getContainerAdapter( byte [] corba_id )
	{
		for ( int i=0; i<adapters.size(); i++ )
		{
			ContainerAdapter adapter = ( ContainerAdapter ) adapters.elementAt( i );

			if ( adapter.beans_from_corba_id( corba_id ) != null )
			  	return adapter;
		}

		return null;
	}

	/**
	 * This operation starts the OpenEJB container system. Then it creates a container adapter for each
	 * container.
	 */
	public void run( java.util.Properties props ) throws org.openejb.OpenEJBException
	{
		// -- Creates the serializer extension --
		
		org.openejb.corba.services.serialization.ApplicationServer serializer = new org.openejb.corba.services.serialization.ApplicationServer();
		
		// -- Starts Tyrex as a local OTS --
		try
		{
		   org.openejb.corba.services.transaction.LocalTyrex tyrex = new org.openejb.corba.services.transaction.LocalTyrex( orb, domainPath, bindNaming );
		   
		   tyrex.start();
		}
		catch ( java.lang.Exception ex )
		{
		   ex.printStackTrace();
		   
		   // -- Unable to start the local tyrex --
		   
		   org.openejb.corba.util.Verbose.fatal( "Initializer::run", ex.toString() );
		}
		
		// -- Initializes OpenEJB --						
		try
		{
			//props.setProperty( org.openejb.EnvProps.CONFIGURATION, props.getProperty("openejb_config_file") );
    
                        //props.setProperty( org.openejb.EnvProps.ASSEMBLER, "org.openejb.tyrex.TyrexClassicAssembler" );
                         
                        org.openejb.OpenEJB.init( props, serializer );
		}
		catch ( org.openejb.OpenEJBException ex )
		{	
			ex.printStackTrace();
			
			// -- Unable to initialize OpenEJB --
			
			org.openejb.corba.util.Verbose.fatal( "Initializer::run", ex.toString() );			
			return;
		}				
					
		// -- Creates all Container Adapters --
		
                org.openejb.Container [] cntrs = org.openejb.OpenEJB.containers();
                
		if(cntrs==null) throw new org.openejb.OpenEJBException("Unable to configure OpenEJB.");
                
                for ( int i=0; i<cntrs.length; i++ )
                    adapters.addElement( createContainerAdapter( cntrs[i] ) );
                    
                // -- Sets any adapter to the serializer --
                
                serializer.setAdapter( ( org.openejb.corba.core.ContainerAdapter ) adapters.elementAt(0) );
	}
	
	/**
	 * This operation creates a container adapter and activates it as a servant manager. It requires the creation of a new sub
	 * POA to set this container adaptet as a servant manager ( Servant Activator ).
	 */
	private ContainerAdapter createContainerAdapter( org.openejb.Container cntr )
	{
		// -- Create the container adapter --
		
		ContainerAdapter adapter = new ContainerAdapter( orb, poa, cntr, this );

		// -- To finish, exports Homes to Naming Service --

		adapter.exportHomeInterfaces();
		
		// -- Return the new container adapter --
		
		return adapter;
	}
}
