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
 * @author Jerome DANIEL ( jdaniel@intalio.com )
 */
public class Invoker
{
	/**
	 * This operation invokes the container to dispatch an incoming request
	 */
	public static java.lang.Object invoke( org.openejb.corba.core.ContainerAdapter adapter,
										   java.lang.Object deployID, 
										   java.lang.reflect.Method method, 
										   java.lang.Object[] args, 
										   java.lang.Object primaryKey, 
										   java.security.Principal principal  ) throws Throwable
	{		
		Verbose.print("Invoker", "Receive an invocation for " + method.getName() + " ( " + deployID + " ) ");
		
		try
		{
			// -- Invoke the container --
			
			java.lang.Object result = adapter.container().invoke( deployID, method, args, primaryKey, principal );
	
			Verbose.print("Invoker", "Invocation completed for " + method.getName() );
		 
			// -- Check the result --
		
			if ( result instanceof org.openejb.ProxyInfo ) 
			{
				// -- A local reference --
				org.openejb.ProxyInfo pinfo = ( org.openejb.ProxyInfo ) result;
				
				if ( javax.ejb.EJBObject.class.isAssignableFrom( pinfo.getInterface() ) )
				{
					// -- The reference identifies a EJB Object --
					byte [] bean_id = createId( pinfo );
					
					org.openejb.corba.core.BeanProfile bean = ( org.openejb.corba.core.BeanProfile ) adapter.beans_from_ejb_id( pinfo.getBeanContainer(), bean_id );
									
					if ( bean == null )
					{
						// -- This is a new bean --
						bean = adapter.createBeanProfile( pinfo, bean_id );
					}
																
					return bean.getProxy();					
				}
				else
				{
					// -- The reference identifies a EJB Home --
					
					byte [] home_id = createId( pinfo );
					
					org.openejb.corba.core.HomeProfile home = ( org.openejb.corba.core.HomeProfile ) adapter.homes_from_ejb_id( pinfo.getBeanContainer(), home_id );
					
					if ( home == null )
					{
						// Here, there is a problem. A home is created by the container ! so, it must be
						// previously created but was not found...
						
						throw new java.rmi.RemoteException("Invalid home reference...");
					}
					
					return home.getProxy();
				}				
			}
			
			if ( result instanceof java.rmi.Remote )
			{				
				// -- A remote reference --
			
				// In this case we get the associated stub...
				
				result = javax.rmi.PortableRemoteObject.toStub( ( java.rmi.Remote ) result );
				
			}
			
			// other result cases are directly managed by the CORBA Proxies...
			
			return result;
		}
		catch ( org.openejb.OpenEJBException ex )
		{
			Throwable t = ex.getRootCause();			
			if ( t != null )
			   throw t;
			else 
			   throw new java.rmi.RemoteException( ex.toString() );
		}
	}
	
	/**
	 * This operation creates a CORBA ID for a new object reference. The rules are :
	 *
	 * Home :  'deployment id' under a multiple bytes format
	 * Stateless bean : 'deployment id # proxy info hashcode' under a multiple bytes format
	 * Stateful bean + entity : "deployment id # primary key' under a multiple bytes format
	 */
	private static byte [] createId( org.openejb.ProxyInfo pinfo )
	{
		if ( javax.ejb.EJBHome.class.isAssignableFrom( pinfo.getInterface() ) )
		{
			return pinfo.getDeploymentInfo().getDeploymentID().toString().getBytes();
		}
		else
		{
			String id = pinfo.getDeploymentInfo().getDeploymentID().toString() + "#";
			
			if ( pinfo.getPrimaryKey() != null )			
				id = id + pinfo.getPrimaryKey().hashCode();
			else
				id = id + pinfo.hashCode();
			
			return id.getBytes();
		}
	}	
}