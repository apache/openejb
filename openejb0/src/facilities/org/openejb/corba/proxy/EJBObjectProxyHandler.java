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
 * This EJB Object Proxy Handler is invoked by all pseudo home proxies. 
 *
 * @author Jerome DANIEL ( jdaniel@intalio.com )
 */
public class EJBObjectProxyHandler implements java.lang.reflect.InvocationHandler

{
	private org.openejb.corba.core.ContainerAdapter adapter;

	
	/** 
	 * Constructor
	 */
	public EJBObjectProxyHandler( org.openejb.corba.core.ContainerAdapter adapter )
	{
		this.adapter = adapter;
	}

	/**
	 * This operation is invoked by the EJB object proxies. 
	 */
	public java.lang.Object invoke( java.lang.Object proxy, java.lang.reflect.Method method, java.lang.Object[] args ) throws Throwable
	{
		try {
		// -- Retreive BeanProfile --
		
		org.openejb.corba.core.BeanProfile bean= null;
		try
		{
			org.omg.CORBA.Object obj = adapter.orb().resolve_initial_references("POACurrent");
			
			org.omg.PortableServer.Current current = org.omg.PortableServer.CurrentHelper.narrow( obj );
			
			byte [] bean_id = current.get_object_id();
		
			bean = ( org.openejb.corba.core.BeanProfile ) adapter.beans_from_corba_id( bean_id );			
					
			if ( bean == null )
			{
				throw new java.rmi.NoSuchObjectException("EJBObjectProxyHandler: No bean found.");
			}
		}
		catch ( org.omg.CORBA.ORBPackage.InvalidName ex )
		{ 
			Verbose.fatal("EJBObjectProxyHandler", "Unable to retrieve the POACurrent object");
		}
		
		// -- check if the invoked operation is served here --						
		
		String operation = method.getName();

		if ( operation.equals( "getHandle" ) )
			return getHandle( proxy );
		if ( operation.equals( "getPrimaryKey" ) )
			return getPrimaryKey( bean );
		if ( operation.equals( "isIdentical" ) )
			return isIdentical( args[0], bean );
		if ( operation.equals( "remove" ) )
			return remove( bean, method, args );
		if ( operation.equals( "getEJBHome") )
			return getEJBHome( bean );
		
		// -- Invoke the container --
		
		return org.openejb.corba.core.Invoker.invoke( adapter, bean.deploymentID(), method, args, bean.getPrimaryKey(), principal() );
                } 
		catch ( Throwable t )
		{ 
			Verbose.exception("EJB Object Proxy Handler", "Thrown during service ( " + method.getName() + " )", t );
			throw t; 
		}
                finally 
                {
                   // Dissociate the transaction with the thread.
                   //TODO:0: Move ThreadTxAssociation to the corba package
                   //org.openejb.util.ThreadTxAssociation.freeAssociation(); 
                }    
       }
	
	/**
	 * Return the EJB Object handle
	 */
	private java.lang.Object getHandle( java.lang.Object proxy )
	{
		return new org.openejb.corba.core.EJBHandle( ( javax.ejb.EJBObject ) proxy );
	}

	/**
	 * Return the primary key
	 */
	private java.lang.Object getPrimaryKey( org.openejb.corba.core.BeanProfile bean ) throws java.rmi.RemoteException
	{
		Verbose.print( "EJBObjectProxyHandler", "Return the primary key" );
		
		byte type = bean.getDeploymentInfo().getComponentType();
		
		if ( type == org.openejb.DeploymentInfo.BMP_ENTITY || type == org.openejb.DeploymentInfo.CMP_ENTITY ) 
			return bean.getPrimaryKey();
		else 
			throw new java.rmi.RemoteException("Invalid operation");
	}
	
	/**
	 * Compare two bean to check if they are identicals
	 */
	private java.lang.Object isIdentical( java.lang.Object target, org.openejb.corba.core.BeanProfile bean )
	{		 	 
	  try
	  {
	      org.omg.PortableServer.Servant srv = adapter.poa().reference_to_servant( ( org.omg.CORBA.Object ) target );
	      	      
     	 if ( bean.getDeploymentInfo().getComponentType() == org.openejb.DeploymentInfo.STATELESS )
     	 {
     	      org.omg.CORBA.Object ref = adapter.poa().servant_to_reference( bean.getServant() );
     	      
     	      return new Boolean( ref._is_equivalent( ( org.omg.CORBA.Object ) target ) );
     	 }
     	 else
     	 if ( bean.getDeploymentInfo().getComponentType() == org.openejb.DeploymentInfo.STATEFUL ) 
     	     return new Boolean( srv.equals( bean.getServant() ) );
     	 else
     	 {
     	      javax.ejb.EJBObject obj = ( javax.ejb.EJBObject ) javax.rmi.PortableRemoteObject.narrow( target,javax.ejb.EJBObject.class );
     	      
     	      
     		     return new Boolean( obj.getPrimaryKey().equals( bean.getPrimaryKey() ) );
     		}
   }
   catch ( java.lang.Exception ex )
   {
       return new Boolean( false );
   }
	}
	
	/**
	 * This operation passes to the container the remove operation and remove the associated bean profile.
	 */
	private java.lang.Object remove( org.openejb.corba.core.BeanProfile bean, java.lang.reflect.Method method, java.lang.Object [] args )
		throws Throwable
	{
		org.openejb.corba.core.Invoker.invoke( adapter, bean.deploymentID(), method, args, bean.getPrimaryKey(), principal() );
		
		adapter.removeBeanProfile( bean );
		
		return null;
	}
	
	/**
	 * This operation returns the bean's EJB Home reference 
	 */
	private java.lang.Object getEJBHome( org.openejb.corba.core.BeanProfile bean )
	{
		return bean.getHomeProfile().getProxy();
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