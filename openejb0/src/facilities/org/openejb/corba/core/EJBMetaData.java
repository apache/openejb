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
 * This class is a simple implementation of the "javax.ejb.EJBMetaData" interface.
 * 
 * @author Jerome DANIEL ( jdaniel@intalio.com )
 */
public class EJBMetaData implements javax.ejb.EJBMetaData, java.io.Serializable 
{	
	private javax.ejb.EJBHome ejbHome;
	private Class homeClass;
	private Class remoteClass;
	private Class keyClass;
	
	private int type;
		
	/**
	 * Constructor
	 */
	public EJBMetaData( Class homeItf, Class remoteItf, Class primaryKey, int type, javax.ejb.EJBHome home )
	{
		homeClass = homeItf;
		remoteClass = remoteItf;
		keyClass = primaryKey;
		ejbHome = home;
		
		switch ( type )
		{
		case org.openejb.DeploymentInfo.STATELESS :
			this.type = org.openejb.Container.STATELESS;
			break;
		case org.openejb.DeploymentInfo.STATEFUL :
			this.type = org.openejb.Container.STATEFUL;
			break;
		default :
			this.type = org.openejb.Container.ENTITY;
			break;
		}
			
	}

	/** 
	 * Return the Home interface description
	 */
	public Class getHomeInterfaceClass() 
	{
        	return homeClass;
    	}
    	
    	/**
    	 * Return the Remote interace description
    	 */
    	public Class getRemoteInterfaceClass() 
    	{
        	return remoteClass;    	
    	}
    	
    	/**
    	 * Return the Primary Key description ( only for entity )
    	 */
    	public Class getPrimaryKeyClass() 
    	{
        	if ( type == org.openejb.Container.ENTITY )
            		return keyClass;
        	else
            		throw new java.lang.UnsupportedOperationException();
    	}
    
    	/** 
    	 * Return TRUE if the component is a Session.
    	 */
    	public boolean isSession() 
    	{
        	return(type == org.openejb.Container.STATEFUL || type == org.openejb.Container.STATELESS);
    	}
    	
    	/**
    	 * Return TRUE if the component is a Session stateless
    	 */
    	public boolean isStatelessSession() 
    	{
        	return type == org.openejb.Container.STATELESS;
    	}
    	
    	/**
    	 * Return the EJB Home reference
    	 */
    	public javax.ejb.EJBHome getEJBHome() 
    	{
        	return ejbHome;
    	}
}