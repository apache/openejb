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
 * This class contains all information about a bean.
 *
 * @author Jerome DANIEL ( jdaniel@intalio.com )
 */
public class BeanProfile
{
	private org.openejb.DeploymentInfo info;
	
	private Class beanInterface;
	
	private java.lang.Object primaryKey;
	
	private org.omg.PortableServer.Servant servant;
	
	private java.lang.reflect.Proxy proxy;
	
	private org.openejb.corba.core.HomeProfile home;	
	
	private org.omg.PortableServer.POA poa;
	
	private byte [] corba_id;
	
	private byte [] bean_id;
				
	/**
	 * Constructor
	 */
	public BeanProfile( org.openejb.DeploymentInfo info, 
	                    Class beanInterface, 
	                    java.lang.Object primaryKey, 
	                    org.openejb.corba.core.HomeProfile home,
	                    java.lang.reflect.InvocationHandler handler,
						               org.omg.PortableServer.POA poa,
						               byte [] bean_id )
	{
		Verbose.print( "BeanProfile", "New profile");
		
		this.info = info;
		this.beanInterface = beanInterface;
		this.primaryKey = primaryKey;
		this.home = home;
		this.poa = poa;
		this.bean_id = bean_id;
		
        // -- Set Context ClassLoader
        Thread.currentThread().setContextClassLoader(beanInterface.getClassLoader());

		// -- Create the bean proxy --
		
		createProxy( handler );
		
		// -- Load the servant and export it with the bean proxy --
		
		loadServant( poa );				
	}
	
	/**
	 * Return the home servant. A servant is a CORBA entity that receives invocation. In our cases
	 */
	public org.omg.PortableServer.Servant getServant()
	{
		return servant;
	}

	/**
	 * Return the home proxy.
	 */
	public java.lang.reflect.Proxy getProxy()
	{
		return proxy;
	}

	/** 
	 * Return the bean reference
	 */
	public org.omg.CORBA.Object getReference()
	{
		try {
			return poa.servant_to_reference( getServant() );
		} catch ( Exception ex ) {	
			Verbose.exception( "BeanProfile", "Unable to get a bean reference", ex );
			return null;
		}		
	}
	
	/** 
	 * Return the bean primary key
	 */
	public java.lang.Object getPrimaryKey()
	{
		return primaryKey;
	}
	
	/**
	 * Return the bean's home profile
	 */
	public org.openejb.corba.core.HomeProfile getHomeProfile()
	{
		return home;
	}

	/** 
	 * Return the Deployment ID
	 */
	public java.lang.Object deploymentID()
	{
		return info.getDeploymentID();
	}

	/**
	 * Return the deployment info
	 */
	public org.openejb.DeploymentInfo getDeploymentInfo()
	{
		return info;
	}	
	
	/**
	 * Return the BEAN ID
	 */
 public byte [] getBeanID()
 {
  return bean_id;
 }
 
	/**
	 * Return the CORBA ID.
	 */
	public byte [] getCorbaID()
	{
		try
		{
			return poa.servant_to_id( servant );
		}
		catch ( java.lang.Exception ex )
		{
			Verbose.exception( "BeanProfile", "Unable to retrieve the bean servant id", ex );
		}
		
		return null;
	}
	
	/**
	 * This operation loads the servant for the Home interface.
	 */
	private void loadServant( org.omg.PortableServer.POA poa )
	{
		Verbose.print("BeanProfile", "Load a bean servant");
		
		try
		{
			javax.rmi.PortableRemoteObject.exportObject( ( java.rmi.Remote ) proxy );
		
			servant = ( org.omg.PortableServer.Servant ) javax.rmi.CORBA.Util.getTie( ( java.rmi.Remote ) proxy );
		}
		catch ( java.lang.Exception ex )
		{ 
			Verbose.print( "HomeProfile", "Tie class not found, try a wrapper" );
			
			try
			{
				servant = loadWrapper( ( java.rmi.Remote ) proxy );
				
				poa.activate_object( servant );
			}
			catch ( java.lang.Exception sub_ex )
			{
				Verbose.exception( "HomeProfile", "Unable to load a home servant", ex );
			}
		}
	}

	/**
	 * This operation creates a new pseudo proxy for the home.
	 */
	private void createProxy( java.lang.reflect.InvocationHandler handler )
	{
		
		try
		{
			 proxy =  (java.lang.reflect.Proxy)java.lang.reflect.Proxy.newProxyInstance( beanInterface.getClassLoader(), new Class[] {beanInterface}, handler );
		
		}
		catch ( java.lang.Exception ex )
		{ 
			Verbose.exception("BeanProfile", "Unable to create a bean proxy instance", ex );
		}
	}
	
	/**
	 * This operation tries to load a wrapper for this home interface. The wrapper has to implement the org.openejb.corba.core.Wrapper
	 * interface.
	 * 
	 * Its name must be _[home name]_Wrapper, example : _ShoppingCart_Wrapper.class
	 */
	private org.omg.PortableServer.Servant loadWrapper( java.rmi.Remote remote ) 
		throws java.lang.Exception
	{
		String wrapperName = "_" + info.getRemoteInterface().getName() + "_Wrapper";
		
		java.lang.Class clz = javax.rmi.CORBA.Util.loadClass( wrapperName, null, null );
		
		org.openejb.corba.core.Wrapper wrapper = ( org.openejb.corba.core.Wrapper ) clz.newInstance();
		
		wrapper.setProxy( remote );
		
		return ( org.omg.PortableServer.Servant ) wrapper;
	}
}
