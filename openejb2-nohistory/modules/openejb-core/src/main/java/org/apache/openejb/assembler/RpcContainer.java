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
 * $Id: RpcContainer.java 444624 2004-03-01 07:17:26Z dblevins $
 */

package org.apache.openejb.assembler;


import java.lang.reflect.Method;

import org.apache.openejb.OpenEJBException;

/**
 * The RpcContainer manages enterprise beans at runtime. The RpcContainer is responsible
 * for interposing between the client and the EntepriseBean objects its manages. The
 * RpcContainer applies transaction, security and persistence behavior as described the
 * the deployment attributes for these services.  The RpcContainer works closely with its
 * ContainerManager to obtain references to the appropriate primary services.
 * <p>
 * A RpcContainer may managed one or more bean deployments of a particular kind (stateless,
 * stateful, BMP and CMP entity).  A Stateful container, for example, could manage all the
 * one or more types (deployments) of stateful beans.
 * <p>
 * The Application Server (AS) will map each bean proxy (home and remote)to its container
 * and deployment IDs, which are unique across the container system. The AS delivers
 * client bean requests directly to the RpcContainer that is responsible for the invoked bean.
 * <p>
 * Business method requests as well as create, find, and remove requests are delivered directly to the container.
 * Business methods are those methods defined in the bean's remote interface that
 * are NOT already defined in the EJBObject interface. The container will respond with either a return value or an
 * OpenEJBException type.  Return values are the return values of the bean method (null if void)
 * and must be returned by the bean stub.  Exceptions are handled according to their type (see below).
 * <p>
 * Requests for a EJBHome and EJBObject references are managed by both the application server and the
 * RpcContainer.  The RpcContainer has specific methods for these requests that return a ProxyInfo object. The
 * application server uses the ProxyInfo object returned by these methods to create a remote stub
 * that resides on the client.  The implementation of the remote stub is application server specific, but the
 * ProxyInfo object provides the application server with helpful information including: The Remote interface to
 * implement (EJBHome or EJBObject types), DeplymentInfo, primary key (stateful and entity only), and a reference to the container.
 * This data is associated with the remote reference in an application sever specific way, and delivered with the
 * Method and arguments to the RpcContainer each time the remote stub is invoked.
 * <p>
 * The invoke( ) method all performs access control checks while processing the requests.  Access with out the proper permissions will result in a
 * org.openejb.ApplicaitonException with a nest java.rmi.RemoteException (process this exception as described below).
 * <p>
 * The OpenEJBException is the standard exception thrown by all methods in all type in the
 * RpcContainer Provider Interface (CPI).  The OpenEJBException has 3 subtypes each serving a different
 * purpose.  The CPI will always thrown one of these subtype and never the OpenEJBException itself.
 * <ul>
 * <li><b>org.openejb.ApplicationException</b><br>
 *     This type is thrown when a normal EnterpriseBean exception is thrown.  The ApplicationException's nested
 *     Exception will be either an EJB ApplicationException ( a custom exception defined by the bean developer)
 *     or a RemoteException.  The org.openejb.ApplicationException must be caught and its nested exception rethrown
 *     by the bean proxy to the client.  The org.openejb.ApplicationException is non-system exception; it does NOT
 *     indicate a problem with the contaienr itself.
 * <li><b>org.openejb.InvalidateReferenceException</b><br>
 *     This type is thrown when the EnterpriseBean throws a RuntimeException or system exception that results in the
 *     eviction of the bean instance.  The InvalidateReferenceException's nested exception will be a RemoteException
 *     or possibly an ObjectNotFoundException.  
 *     The Application Server must catch the InvalidateReferenceException and its nested exception rethrown by the bean proxy
 *     After the exception is re-thrown by the bean proxy, the bean proxy must be invalidated so that all subsequent invocations by
 *     the client on that bean proxy throw a RemoteException. The proxy is made invalid. InvalidateReferenceException is non-system
 *     exception; it does NOT indicate a problem with the container itself.
 * <li><b>org.openejb.SystemException</b><br>
 *     This type is thrown when the container has encountered an unresolvable system exception that make this RpcContainer
 *     unable to process requests.  A breakdown in communication with one of the primary services or a RuntimeException
 *     thrown within the container (not by a bean) is are good examples.  The org.openejb.SystemException represents a
 *     serious problem with the RpcContainer.  The RpcContainer should be shut down and not used for any more processing.
 * </ul>
 * <p>
 * The default implementation of this interface is provided by the
 * org.openejb.core.stateful.StatefulContainer, org.openejb.core.stateless.StatelessContainer,
 * and org.openejb.core.entity.EntityContainer.
 * <p>
 * 
 * @version 0.1, 3/21/2000
 * @see org.apache.openejb.core.stateful.StatefulContainer
 * @see org.apache.openejb.core.stateless.StatelessContainer
 * @see org.apache.openejb.core.entity.EntityContainer
 * @see org.apache.openejb.ProxyInfo
 * @since JDK 1.2
 */
public interface RpcContainer extends Container{
    
    /**
     * Invokes a method on an instance of the specified bean deployment.
     *
     * @param deployID the dployment id of the bean deployment
     * @param callMethod the method to be called on the bean instance
     * @param args the arguments to use when invoking the specified method
     * @param primKey the primary key class of the bean or null if the bean does not need a primary key
     * @param prncpl 
     * @return the result of invoking the specified method on the bean instance
     * @throws OpenEJBException 
     * @see org.apache.openejb.core.stateful.StatefulContainer#invoke StatefulContainer.invoke
     * @see org.apache.openejb.core.stateless.StatelessContainer#invoke StatelessContainer.invoke
     */
    public Object invoke(Object deployID, Method callMethod, Object [] args, Object primKey, Object securityIdentity) throws OpenEJBException;
}
