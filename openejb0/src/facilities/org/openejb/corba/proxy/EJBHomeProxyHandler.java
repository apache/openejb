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

package org.openejb.corba.proxy;

import org.openejb.corba.util.Verbose;

/**
 * This EJB Home Proxy Handler is invoked by all pseudo home proxies. 
 *
 * @author Jerome DANIEL ( jdaniel@intalio.com )
 */
public class EJBHomeProxyHandler implements java.lang.reflect.InvocationHandler

{    
	private org.openejb.corba.core.ContainerAdapter adapter;
	
	/**
	 * Constructor
	 */
	public EJBHomeProxyHandler( org.openejb.corba.core.ContainerAdapter adapter )
	{ 
		this.adapter = adapter;		
	}
	
	/**
	 * This operation is invoked by the Home proxies. 
	 */
	public java.lang.Object invoke( java.lang.Object proxy, java.lang.reflect.Method method, java.lang.Object[] args ) throws Throwable
	{
		try
		{
		Verbose.print( "EJBHomeProxyHandler", "New invocation for : " + method.getName());
		
		// -- Retreive HomeProfile --
		
		org.openejb.corba.core.HomeProfile home = null;
		try
		{
			org.omg.CORBA.Object obj = adapter.orb().resolve_initial_references("POACurrent");
			
			org.omg.PortableServer.Current current = org.omg.PortableServer.CurrentHelper.narrow( obj );
			
			byte [] home_id = current.get_object_id();
			
			home = ( org.openejb.corba.core.HomeProfile ) adapter.homes_from_corba_id( home_id  );
			
			if ( home == null )
			{
				Verbose.print("EJBHomeProxyHandler", "Unable to retrieve home for id = " + new String(home_id) );
			}
		}
		catch ( org.omg.CORBA.ORBPackage.InvalidName ex )
		{ 
			Verbose.fatal("EJBHomeProxyHandler", "Unable to retrieve the POACurrent object");
		}
	  
		// -- check if the invoked operation is served here --
		
		String operation = method.getName();
		
		if ( operation.equals( "getEJBMetaData" ) )
			return getEJBMetaData( home, proxy );
		if ( operation.equals( "getHomeHandle" ) )
			return getHomeHandle( proxy );
		if ( operation.equals( "remove" ) )
			return remove( method, args, home );
		
		// -- Invoke the container --
		
		return org.openejb.corba.core.Invoker.invoke( adapter, home.deploymentID(), method, args, null, principal() );	 	
		} catch ( Throwable t )
		{
			Verbose.exception("EJBHomeProxyHandler", "Invoke", t );
			throw t;
		}
                finally 
                {
                   // Dissociate the transaction with the thread.
                   // TODO:0: Needs to be moved to the corba package
                   //org.openejb.util.ThreadTxAssociation.freeAssociation(); 
                }    
	}		
	
	/**
	 * Creates and returns the EJB Server specific meta data for the deployed bean.
	 */
	private java.lang.Object getEJBMetaData( org.openejb.corba.core.HomeProfile home, java.lang.Object proxy )
	{        	
		return new org.openejb.corba.core.EJBMetaData( home.getDeploymentInfo().getHomeInterface(), 
													   home.getDeploymentInfo().getRemoteInterface(),
													   home.getDeploymentInfo().getPrimaryKeyClass(),
													   home.getDeploymentInfo().getComponentType(),
													   ( javax.ejb.EJBHome ) proxy );
	}

	/**
	 * Creates and returns an EJB Server specific handle to the bean's server
	 * specific EJBHome object.     
	 */
	private java.lang.Object getHomeHandle( java.lang.Object proxy )
	{
		return new org.openejb.corba.core.EJBHomeHandle( ( javax.ejb.EJBHome ) proxy );
	}
	
	/**
	 * This operation gets the primary key from the arguments and re-invoke the container.
	 */
	protected java.lang.Object remove( java.lang.reflect.Method method, java.lang.Object[] args, org.openejb.corba.core.HomeProfile home ) 
		throws Throwable
	{				
		Class [] types = method.getParameterTypes();
		
		// -- EJBHome.remove( Handle handle ) --
		if ( types[0] == javax.ejb.Handle.class ) 
		{
			Verbose.print( "EJBHomeProxyHandler", "Remove a bean from its handle" );
			
			// -- Invoke remove on the container --
			
			javax.ejb.Handle handle = ( javax.ejb.Handle ) args[0];
			javax.ejb.EJBObject ejb = handle.getEJBObject();
				
			java.lang.Object primaryKey = null;
			byte [] bean_id = null;
			try
			{
				org.omg.PortableServer.Servant servant = adapter.poa().reference_to_servant( ( org.omg.CORBA.Object ) ejb );
			
				javax.rmi.CORBA.Tie tie = ( javax.rmi.CORBA.Tie ) servant;
				
				java.rmi.Remote proxy = tie.getTarget();
				
				java.util.Hashtable beans = adapter.beans();
				
				java.util.Enumeration enum = beans.elements();
				
				while ( enum.hasMoreElements() )
				{
					org.openejb.corba.core.BeanProfile bean = ( org.openejb.corba.core.BeanProfile ) enum.nextElement();
				
					if ( bean.getProxy() == proxy  )
					{
		    
						primaryKey = bean.getPrimaryKey();
						bean_id = bean.getBeanID();
						break;
					}
							 
				}
				
			}
			catch ( java.lang.Exception ex )
			{ 
				Verbose.fatal("EJBHomeProxyHandler", "Unexpected exception");
			}
						
			org.openejb.corba.core.Invoker.invoke( adapter, home.deploymentID(), method, args, primaryKey, principal() );
			
			// -- Remove the BeanProfile association --
						
			org.openejb.corba.core.BeanProfile bean = ( org.openejb.corba.core.BeanProfile ) adapter.beans_from_ejb_id( adapter.container(), bean_id );
			
			adapter.removeBeanProfile( bean );
			
		}  // EJBHome.remove(Object primaryKey)
		else 
		{
			Verbose.print( "EJBHomeProxyHandler", "Remove a bean from its primary key" );
			
			byte type = home.getDeploymentInfo().getComponentType();
			
			if ( type == org.openejb.DeploymentInfo.BMP_ENTITY || type == org.openejb.DeploymentInfo.CMP_ENTITY ) 
				org.openejb.corba.core.Invoker.invoke( adapter, home.deploymentID(), method, args, args[0], principal() );
			else
				throw new java.rmi.RemoteException("Invalid operation");
			
			byte [] bean_id = null;
			if ( args[0] != null )
			{
				bean_id = ( home.deploymentID().toString() + '#' + args[0].hashCode() ).getBytes();
				org.openejb.corba.core.BeanProfile bean = ( org.openejb.corba.core.BeanProfile ) adapter.beans_from_ejb_id( adapter.container(), bean_id );
				if ( bean.getDeploymentInfo().getComponentType() != org.openejb.DeploymentInfo.STATELESS )
					adapter.removeBeanProfile( bean );
			}				
		}
		
		return null; // No return type
	}
	
	/**
	 * This operation returns the principal's invocation
	 */
	private java.security.Principal principal()
	{
		// TODO : connect this operation to the security service
		
		return new org.openejb.corba.services.security.Principal( "" );
	}
}