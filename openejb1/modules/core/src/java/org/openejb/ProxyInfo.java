/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id$
 */


package org.openejb;

/**
 * The ProxyInfo object is returned by the Container.createEJBObject() and Container.createEJBHome( ) method 
 * These method are invoked in response to client requests for EJBHome and EJBobject references.
 * The ProxyInfo is used by the application server to create a remote stubs that represent the EJBObject 
 * and EJBHome on the client. 
 * <p>
 * The implementation of the remote stub is application server specific, but the ProxyInfo object provides 
 * the application server with helpful information including: The Remote interface to implement 
 * (EJBHome or EJBObject types) the primary key, DeploymentInfo object, and container reference.  
 * <p>
 * @author Richard Monson-Haefel
 * @version 0.1, 3/21/2000
 * @since JDK 1.2
 */
public class ProxyInfo {
    
    protected DeploymentInfo deploymentInfo;
    protected Object primaryKey;
    protected Class type;
    protected RpcContainer beanContainer;
    
    /**
     * Create an instance of ProxyInfo. For use in subclassing.
     */
    protected ProxyInfo(){}
        
    /**
     * Creates a ProxyInfo to represent an EJBHome or EJBObject for a deployed bean in the container system.
     *
     * @param depInfo the DeploymentInfo object connected to the EJBObject or EJBHome that this proxy will represent
     * @param pk the primary key class of the bean or null if the bean does not need a primary key
     * @param intrfc the bean's remote interface if this proxy represents an EJBObject or the bean's home interface if this proxy represents an EJBHome
     * @param cntnr the Container that the deployed bean lives in
     * @see DeploymentInfo
     * @see Container
     */
    public ProxyInfo(DeploymentInfo depInfo, Object pk, Class intrfc, RpcContainer cntnr){
        deploymentInfo = depInfo;
        primaryKey = pk;
        type = intrfc;
        beanContainer = cntnr;
    }

    public ProxyInfo(DeploymentInfo depInfo, Object pk, boolean isLocalInterface, RpcContainer cntnr){
        this.deploymentInfo = depInfo;
        this.primaryKey = pk;
        this.beanContainer = cntnr;
        if (isLocalInterface){
        	this.type = deploymentInfo.getLocalInterface();
        } else {
        	this.type = deploymentInfo.getRemoteInterface();
        }
    }
    /**
     * Gets the DeploymentInfo object of the bean that this proxy represents.
     *
     * @return the DeploymentInfo of the bean deployment
     * @see DeploymentInfo
     */
    public DeploymentInfo getDeploymentInfo() { return deploymentInfo; }

    /**
     * Gets the primary key class of the bean or null if the bean does not need a primary key
     *
     * @return the primary key class of the bean or null if the bean does not need a primary key
     */
    public Object getPrimaryKey() { return primaryKey; }

    /**
     * Gets the bean's remote interface if this proxy represents an EJBObject or the bean's home interface if this proxy represents an EJBHome.
     *
     * @return the class of the bean's remote or home interface
     */
    public Class getInterface() { return type; }
    
    /**
     * Gets the Container that the deployed bean lives in.
     *
     * @return the Container that the bean deployment lives in
     * @see Container
     */
    public RpcContainer getBeanContainer() { return beanContainer; }
}
