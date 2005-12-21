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
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact info@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.cluster.sfsb;

import java.util.Set;

import javax.ejb.EnterpriseBean;
import javax.ejb.SessionBean;

import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.geronimo.transaction.context.UserTransactionImpl;
import org.openejb.EJBInstanceContext;
import org.openejb.cluster.server.EJBInstanceContextRecreator;
import org.openejb.dispatch.SystemMethodIndices;
import org.openejb.proxy.EJBProxyFactory;
import org.openejb.sfsb.StatefulInstanceContext;

/**
 * 
 * @version $Revision$ $Date$
 */
class SFInstanceContextRecreator implements EJBInstanceContextRecreator {
    private final Object containerId;
    private final UserTransactionImpl userTransaction;
    private final Set unshareableResources;
    private final Set applicationManagedSecurityResources;
    private transient EJBProxyFactory proxyFactory;
    private transient Interceptor systemChain;
    private transient SystemMethodIndices systemMethodIndices;
    private transient TransactionContextManager transactionContextManager;
    
    public SFInstanceContextRecreator(Object containerId,
            UserTransactionImpl userTransaction,
            Set unshareableResources,
            Set applicationManagedSecurityResources,
            EJBProxyFactory proxyFactory,
            Interceptor systemChain,
            SystemMethodIndices systemMethodIndices,
            TransactionContextManager transactionContextManager) {
        this.containerId = containerId;
        this.userTransaction = userTransaction;
        this.unshareableResources = unshareableResources;
        this.applicationManagedSecurityResources = applicationManagedSecurityResources;
        this.proxyFactory = proxyFactory;
        this.systemChain = systemChain;
        this.systemMethodIndices = systemMethodIndices;
        this.transactionContextManager = transactionContextManager;
    }

    public EJBInstanceContext recreate(Object id, EnterpriseBean bean) {
        if (false == bean instanceof SessionBean) {
            throw new IllegalArgumentException("bean must be a " + 
                    SessionBean.class + ". Was :" + bean.getClass());
        }

        return new StatefulInstanceContext(
                containerId,
                proxyFactory,
                (SessionBean) bean,
                id,
                transactionContextManager,
                userTransaction,
                systemMethodIndices,
                systemChain,
                unshareableResources,
                applicationManagedSecurityResources);
    }
}