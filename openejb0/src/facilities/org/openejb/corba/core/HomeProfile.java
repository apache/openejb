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
 * This class contains all information about a home. A home profile associates a home identity ( deployment id ) and a proxy + a servant.
 *
 * @author Jerome DANIEL ( jdaniel@intalio.com )
 */
public class HomeProfile
{
	private org.openejb.DeploymentInfo info;
	
	private org.omg.PortableServer.Servant servant;
	
	//private org.openejb.util.proxy.Proxy proxy;
	
	private java.lang.reflect.Proxy homeProxy;
				
	/**
	 * Constructor
	 */
	public HomeProfile( org.openejb.DeploymentInfo info, java.lang.reflect.InvocationHandler handler )
	{
		Verbose.print( "HomeProfile", "New home profile");
		
		this.info = info;
		
        // -- Set Context ClassLoader
        Thread.currentThread().setContextClassLoader(info.getHomeInterface().getClassLoader());

		// -- Create the home proxy --
		
		createProxy( handler );
		
		// -- Create the bean proxy --
		
		createBeanProxy( info );
		
		// -- Load the servant and export it with the home proxy --
		
		loadServant();				
	}
	
	/**
	 * Return the home servant. A servant is a CORBA entity that receives invocation.
	 */
	public org.omg.PortableServer.Servant getServant()
	{
		return servant;
	}

	/**
	 * Return the home proxy.
	 */
	//public org.openejb.util.proxy.Proxy getProxy()
	//{
	//	return proxy;
	//}
	public java.lang.reflect.Proxy getProxy()
	{
	 	return homeProxy;
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
	 * This operation loads the servant for the Home interface.
	 */
	private void loadServant()
	{
		Verbose.print( "HomeProfile", "Load a home servant for : " + info.getHomeInterface().getName() );
		
		try
		{
			javax.rmi.PortableRemoteObject.exportObject( (java.rmi.Remote)homeProxy );
		
			servant = ( org.omg.PortableServer.Servant ) javax.rmi.CORBA.Util.getTie( (java.rmi.Remote)homeProxy );
		}
		catch ( java.lang.Exception ex )
		{ 
			Verbose.print( "HomeProfile", "Tie class not found, try a wrapper" );
			
			try
			{
				servant = loadWrapper( (java.rmi.Remote)homeProxy );
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
		Verbose.print( "HomeProfile", "Create a new proxy");
		
		java.lang.Class homeClz = info.getHomeInterface();
		
		// -- Create a proxy --
		
		try
		{
		 
			 homeProxy = (java.lang.reflect.Proxy)java.lang.reflect.Proxy.newProxyInstance( homeClz.getClassLoader(), new Class[]{homeClz}, handler ); 
		}
		catch ( java.lang.Exception ex )
		{ 
			Verbose.exception( "HomeProfile", "Unable to create a home proxy", ex );
		}
	}
	
	/**
	 * This operation creates a new pseudo proxy for the bean.
	 */
	private void createBeanProxy( org.openejb.DeploymentInfo info )
	{		
		Verbose.print("BeanProfile", "Create a new bean proxy");
		
		java.lang.Class beanInterface = info.getRemoteInterface();
		/*
		try
		{
			java.lang.Class proxyClass =  ( java.lang.Class ) org.openejb.util.proxy.ProxyManager.getProxyClass( beanInterface );
			
			org.openejb.corba.core.BeanProfile.proxies.put( beanInterface, proxyClass );
		}
		catch ( java.lang.Exception ex )
		{ 
			Verbose.exception("BeanProfile", "Unable to create a bean proxy", ex );
		}*/
	}
	
	/**
	 * This operation tries to load a wrapper for this home interface. The wrapper has to implement the org.openejb.corba.core.Wrapper
	 * interface.
	 * 
	 * Its name must be _[home name]_Wrapper, example : _ShoppingCartHome_Wrapper.class
	 */
	private org.omg.PortableServer.Servant loadWrapper( java.rmi.Remote remote ) 
		throws java.lang.Exception
	{
		String wrapperName = "_" + info.getHomeInterface().getName() + "_Wrapper";
		
		java.lang.Class clz = javax.rmi.CORBA.Util.loadClass( wrapperName, null, null );
		
		org.openejb.corba.core.Wrapper wrapper = ( org.openejb.corba.core.Wrapper ) clz.newInstance();
		
		wrapper.setProxy( remote );
		
		return ( org.omg.PortableServer.Servant ) wrapper;
	}
}