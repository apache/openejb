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

package org.openejb.corba.services.serialization;

/**
 * This class is used by the serizaliation engine to replace the OpenEJB artifact by the 
 * application server corresponding objects.
 *
 * @author Jerome DANIEL ( jdaniel@intalio.com )
 */
public class ApplicationServer implements org.openejb.spi.ApplicationServer
{    
	private org.openejb.corba.core.ContainerAdapter adapter;
		
	/**
	 * This operation is used to set a reference to a container adapter. We just need any kind of
	 * container adapter.
	 */
	public void setAdapter( org.openejb.corba.core.ContainerAdapter adapter )
	{
		this.adapter = adapter;
	}
		
	/**
	 * Replaces the OpenEJB artifact for the meta data
	 */
	public javax.ejb.EJBMetaData getEJBMetaData(org.openejb.ProxyInfo proxyInfo)
	{
		Object proxy = lookup( proxyInfo );
		
		org.openejb.corba.core.HomeProfile home = lookupHomeProfile( proxyInfo );
		
		return new org.openejb.corba.core.EJBMetaData( home.getDeploymentInfo().getHomeInterface(), 
							       home.getDeploymentInfo().getRemoteInterface(),
							       home.getDeploymentInfo().getPrimaryKeyClass(),
							       home.getDeploymentInfo().getComponentType(),
							       ( javax.ejb.EJBHome ) proxy );
	}
	
	/**
	 * Replaces the OpenEJB artifact for the Handle
	 */
	public javax.ejb.Handle getHandle(org.openejb.ProxyInfo proxyInfo)
	{				
		return new org.openejb.corba.core.EJBHandle( ( javax.ejb.EJBObject ) lookup( proxyInfo ) );
	}

	/**
	 * Replaces the OpenEJB artifact for the Handle
	 */
	public javax.ejb.HomeHandle getHomeHandle(org.openejb.ProxyInfo proxyInfo)
	{				
	    return null;
	}
	
	/**
	 * Replace the OpenEJB artifact for the Object reference
	 */
	public javax.ejb.EJBObject getEJBObject(org.openejb.ProxyInfo proxyInfo)
	{
		return ( javax.ejb.EJBObject ) lookup( proxyInfo );
	}
	
	/**
	 * Replaces the OpenEJB artifact for the Home reference
	 */
	public javax.ejb.EJBHome getEJBHome(org.openejb.ProxyInfo proxyInfo)
	{
		return ( javax.ejb.EJBHome ) lookup( proxyInfo );
	}
	
	/**
	 * Returns the bean/home corresponding to the proxy info passed as parameter.
	 */
	private Object lookup( org.openejb.ProxyInfo pinfo )
	{
		if ( javax.ejb.EJBObject.class.isAssignableFrom( pinfo.getInterface() ) )
		{
			// -- The reference identifies a EJB Object --
			
			byte [] bean_id = createId( pinfo );
			
			org.openejb.corba.core.BeanProfile bean = ( org.openejb.corba.core.BeanProfile ) adapter.beans_from_ejb_id( pinfo.getBeanContainer(), bean_id );						
			
			if ( bean == null ) {
                          try {
                            bean = adapter.createBeanProfile(pinfo, bean_id);
		          }
                          catch(java.rmi.RemoteException ex) {
                            org.openejb.corba.util.Verbose.fatal("ApplicationServer", "Invalid bean reference..." );
                          } 
                        }													
			//if ( !proxy )
			//   return ( java.lang.Object ) javax.rmi.PortableRemoteObject.narrow( bean.getReference(), bean.getDeploymentInfo().getRemoteInterface() );					
			//else
			return bean.getProxy();
		}
		else
		{
			// -- The reference identifies a EJB Home --
			
			byte [] home_id = createId( pinfo );
			
			org.openejb.corba.core.HomeProfile home = ( org.openejb.corba.core.HomeProfile ) adapter.homes_from_ejb_id( pinfo.getBeanContainer(), home_id );
			
			if ( home == null )
				org.openejb.corba.util.Verbose.fatal( "ApplicationServer", "Invalid home reference..." );	
			
			return home.getProxy();
		}	
	}
	
	/**
	 * Returns the home profile corresponding to the proxy info passed as parameter.
	 */
	private org.openejb.corba.core.HomeProfile lookupHomeProfile( org.openejb.ProxyInfo pinfo )
	{
		if ( javax.ejb.EJBObject.class.isAssignableFrom( pinfo.getInterface() ) )		
		{
			org.openejb.corba.util.Verbose.fatal( "ApplicationServer", "Bad reference type..." );				
			return null;
		}
		else
		{
			// -- The reference identifies a EJB Home --
			
			byte [] home_id = createId( pinfo );
			
			org.openejb.corba.core.HomeProfile home = ( org.openejb.corba.core.HomeProfile ) adapter.homes_from_ejb_id( pinfo.getBeanContainer(), home_id );
			
			if ( home == null )
				org.openejb.corba.util.Verbose.fatal( "ApplicationServer", "Invalid home reference..." );				
			
			return home;
		}		
	}
    
    	/**
	 * This operation creates a CORBA ID for a new object reference. The rules are :
	 *
	 * Home :  'deployment id' under a multiple bytes format
	 * Stateless bean : 'deployment id #' under a multiple bytes format
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
				id = id + "" + pinfo.getPrimaryKey().hashCode();
			else
				id = id + "" + pinfo.hashCode();	
			
			return id.getBytes();
		}
	}
}