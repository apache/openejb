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

import org.openejb.corba.util.Verbose;

/**
 * A CORBA Container Adapter is associated with an EJB Container.
 *
 * @author Jerome DANIEL ( jdaniel@intalio.com )
 */
public class ContainerAdapter
{
	private org.omg.CORBA.ORB orb;
	
	private org.omg.PortableServer.POA poa;
	
	private org.openejb.Container cntr;
	
	private java.util.Hashtable homes_corba;
	
	private java.util.Hashtable homes_ejb;
	
	private java.util.Hashtable beans_corba;
	
	private java.util.Hashtable beans_ejb;
	
	private org.openejb.corba.core.Initializer initializer;
	
	private org.openejb.corba.proxy.EJBHomeProxyHandler home_handler;
	
	private org.openejb.corba.proxy.EJBObjectProxyHandler bean_handler;

	private org.openorb.util.ContextUtilities context_util;
	
	/**
	 * Constructor
	 */
	public ContainerAdapter( org.omg.CORBA.ORB orb, org.omg.PortableServer.POA poa, 
	                         org.openejb.Container cntr,
	                         Initializer initializer )
	{
		// -- Stores all required references --
		
		Verbose.print("ContainerAdapter", "Stores all required references");
		
		this.orb = orb;			
		this.poa = poa;
		this.cntr = cntr;
		this.initializer = initializer;

		// -- Creates the context utilties --

		context_util = new org.openorb.util.ContextUtilities( orb );
		
		// -- Create homes and beans profiles --
		
		Verbose.print("ContainerAdapter", "Creates homes and beans profiles");
		
		homes_corba = new java.util.Hashtable();
		homes_ejb = new java.util.Hashtable();
		beans_corba = new java.util.Hashtable();			
		beans_ejb = new java.util.Hashtable();
		
		// -- Create the EJB Home Proxy Handler --
		
		Verbose.print("ContainerAdapter", "Create the EJB Home Proxy Handler");
		
		home_handler = new org.openejb.corba.proxy.EJBHomeProxyHandler( this );
		
		// -- Create the EJB Object Proxy Handler --
		
		Verbose.print("ContainerAdapter", "Create the EJB Object Proxy Handler");
		
		bean_handler = new org.openejb.corba.proxy.EJBObjectProxyHandler( this );
		
		// -- Prepare profiles for all deployed beans --
		
		prepareHomeProfiles();
	}
		
	/**
	 * Return the container access
	 */
	public org.openejb.RpcContainer container()
	{
		return ( org.openejb.RpcContainer ) cntr;
	}
	
	/**
	 * Return the ORB reference
	 */
	public org.omg.CORBA.ORB orb()
	{
		return orb;
	}
	
	/**
	 * Return the POA reference
	 */
	public org.omg.PortableServer.POA poa()
	{
		return poa;
	}
	
	/**
	 * Return the Home profiles table
	 */
	public org.openejb.corba.core.HomeProfile homes_from_corba_id( byte [] id )
	{
		return ( org.openejb.corba.core.HomeProfile ) homes_corba.get( new String(id) );
	}
	
	/**
	 * Return the Home profiles table from another container
	 */
	public org.openejb.corba.core.HomeProfile homes_from_corba_id( org.openejb.RpcContainer container, byte [] id )
	{
		if ( manages( container ) )		
			return homes_from_corba_id( id );
		
		ContainerAdapter adapter = initializer.getContainerAdapter( container );
		
		if ( adapter != null )
			return adapter.homes_from_corba_id( container, id );
		
		return null;
	}
	
	/**
	 * Return the Home profiles table
	 */
	public org.openejb.corba.core.HomeProfile homes_from_ejb_id( byte [] id )
	{
		return ( org.openejb.corba.core.HomeProfile ) homes_ejb.get( new String(id) );
	}
	
	/**
	 * Return the Home profiles table from another container
	 */
	public org.openejb.corba.core.HomeProfile homes_from_ejb_id( org.openejb.RpcContainer container, byte [] id )
	{
		if ( manages( container ) )		
			return homes_from_ejb_id( id );
		
		ContainerAdapter adapter = initializer.getContainerAdapter( container );
		
		if ( adapter != null )
			return adapter.homes_from_ejb_id( container, id );
		
		return null;
	}
	
	/**
	 * Return the Bean profiles table
	 */	
	public java.util.Hashtable beans()
	{
		return beans_ejb;
	}
	
	/**
	 * Return the Beans profiles table from another container
	 */	
	public org.openejb.corba.core.BeanProfile beans_from_ejb_id( byte [] id )
	{
		return ( org.openejb.corba.core.BeanProfile ) beans_ejb.get( new String(id) );
	}
	
	
	/**
	 * Return the Beans profiles table from another container
	 */
	public org.openejb.corba.core.BeanProfile beans_from_ejb_id( org.openejb.RpcContainer container, byte [] id )
	{
		if ( manages( container ) )		
			return beans_from_ejb_id( id );
		
		ContainerAdapter adapter = initializer.getContainerAdapter( container );
		
		if ( adapter != null )
			return adapter.beans_from_ejb_id( container, id );
		
		return null;
	}
	
	/**
	 * Return the Beans profiles table from another container
	 */	
	public org.openejb.corba.core.BeanProfile beans_from_corba_id( byte [] id )
	{
		return ( org.openejb.corba.core.BeanProfile ) beans_corba.get( new String(id) );
	}
	
	
	/**
	 * Return the Beans profiles table from another container
	 */
	public org.openejb.corba.core.BeanProfile beans_from_corba_id( org.openejb.RpcContainer container, byte [] id )
	{
		if ( manages( container ) )		
			return beans_from_corba_id( id );
		
		ContainerAdapter adapter = initializer.getContainerAdapter( container );
		
		if ( adapter != null )
			return adapter.beans_from_corba_id( container, id );
		
		return null;
	}
	
	/** 
	 * Return TRUE if the parameter is the same RPC container which is managed by this container adapter.
	 */
	public boolean manages( org.openejb.RpcContainer container )
	{
		if ( container.equals( cntr ) )
			return true;
		
		return false;
	}
		
	/**
	 * This operation is used by the initializer to export all home interfaces to the naming service. 
	 */
	public void exportHomeInterfaces()
	{	
		Verbose.print("ContainerAdapter", "Export home interfaces to naming service");
		
		// -- Retrieve all home interfaces and bind them to the naming service --
		
		org.openejb.DeploymentInfo [] dinfo = cntr.deployments();
		try
		{
			for ( int i=0; i<dinfo.length; i++ )
			{
				String home_name = dinfo[i].getDeploymentID().toString();
				
				byte [] home_id = home_name.getBytes();
				
				HomeProfile home = ( HomeProfile ) homes_ejb.get( dinfo[i].getDeploymentID().toString() );
				
				Verbose.print("ContainerAdapter", "Export home to : " + home_name );
				
				// -- Store the Home id --
				
				byte [] corba_id = poa.servant_to_id( home.getServant() );
				
				homes_corba.put( new String(corba_id), home );
				
				// -- Then, add to the naming service the home --

				home_name = home_name.replace('\\','/');
				
				org.omg.CORBA.Object home_ref = poa.servant_to_reference( home.getServant() );
			
				if ( !context_util.rebind( home_name, home_ref ) )
				{
					Verbose.fatal( "ContainerAdapter", "Unable to export a home interface : " + home_name );
				}
			}
		}	
		catch ( java.lang.Exception ex )
		{ 
			Verbose.exception( "ContainerAdapter", "Unable to export home interfaces", ex );			
		}	
	}
	
	/**
	 * This operation is used to create a new bean profile. It creates a servant and a proxy for this bean. 
	 */
	public org.openejb.corba.core.BeanProfile createBeanProfile( org.openejb.ProxyInfo pinfo, byte [] bean_id )
		throws java.rmi.RemoteException
	{
		if ( manages( pinfo.getBeanContainer() ) )
		{			
			Verbose.print("ContainerAdapter", "Create a new bean profile");
			
			// -- Look for the bean's home profile --
			
			byte [] home_id = pinfo.getDeploymentInfo().getDeploymentID().toString().getBytes();
			
			org.openejb.corba.core.HomeProfile home = ( org.openejb.corba.core.HomeProfile ) homes_ejb.get( new String(home_id) );
			
			if ( home == null )
				throw new java.rmi.RemoteException("Unable to find a Home profile");
			
			// -- Create the bean profile --
			
			org.openejb.corba.core.BeanProfile bean = new org.openejb.corba.core.BeanProfile( pinfo.getDeploymentInfo(), 
													  pinfo.getInterface(),
													  pinfo.getPrimaryKey(),
													  home,
													  bean_handler,
													  poa,
													  bean_id );
			
			// -- Retrieve the bean id --
			
			byte [] id = null;
			try
			{
				id = poa.servant_to_id( bean.getServant() );
			}
			catch ( java.lang.Exception ex )
			{
				org.openejb.corba.util.Verbose.exception( "ContainerAdapter::createBeanProfile", "Unable to retrieve bean servant id", ex );
			}

			// -- Add the bean profile to the bean profiles list --
						
			beans_corba.put( new String( id ), bean );		
			
			beans_ejb.put( new String(bean_id), bean );
				
			return bean;
		}
		else
		{
			ContainerAdapter adapter = initializer.getContainerAdapter( pinfo.getBeanContainer() );
			
			if ( adapter == null )
				throw new java.rmi.RemoteException("Unknown container...");
			
			return adapter.createBeanProfile( pinfo, bean_id );
		}
	}
	
	/**
	 * Remove a bean profile
	 */
	public void removeBeanProfile( org.openejb.corba.core.BeanProfile bean )
	{
		Verbose.print( "ContainerAdapter", "Remove a bean profile" );
		
		// -- Remove the bean from the bean list --
			
	 beans_corba.remove( new String(bean.getCorbaID()) );
		beans_ejb.remove( new String(bean.getBeanID()) );
		
		// -- Deactivate the bean's servant --
		
		try
		{
			poa.deactivate_object( bean.getCorbaID() );
		}
		catch ( java.lang.Exception ex )
		{ 
			Verbose.exception( "ContainerAdapter", "Unable to deactivate a bean servant", ex );
		}
	}	
	
	/**
	 * This operation is used to prepare the home and bean profiles.
	 */
	private void prepareHomeProfiles()
	{
		Verbose.print( "ContainerAdapter", "Prepare home profiles");
		
		org.openejb.DeploymentInfo [] dinfo = cntr.deployments();
		
		for ( int i=0; i<dinfo.length; i++ )
		{
			byte [] id = dinfo[i].getDeploymentID().toString().getBytes();
			
			HomeProfile home_prof = new HomeProfile( dinfo[i], home_handler );
			
			homes_ejb.put( dinfo[i].getDeploymentID().toString(), home_prof );			
			
			Verbose.print( "ContainerAdapter", "Home available with ID = " + new String( id ) );
		}
	}
}
