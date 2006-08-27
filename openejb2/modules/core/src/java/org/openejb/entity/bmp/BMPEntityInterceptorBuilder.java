/* ====================================================================
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce this list of
 *    conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact openejb-group@openejb.sf.net.
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
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the OpenEJB Project.  For more information
 * please see <http://openejb.org/>.
 *
 * ====================================================================
 */
package org.openejb.entity.bmp;

import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.naming.java.ComponentContextInterceptor;

import org.openejb.AbstractInterceptorBuilder;
import org.openejb.ConnectionTrackingInterceptor;
import org.openejb.SystemExceptionInterceptor;
import org.openejb.TwoChains;
import org.openejb.entity.EntityInstanceInterceptor;
import org.openejb.dispatch.DispatchInterceptor;
import org.openejb.security.EJBIdentityInterceptor;
import org.openejb.security.EJBRunAsInterceptor;
import org.openejb.security.EJBSecurityInterceptor;
import org.openejb.security.PolicyContextHandlerEJBInterceptor;
import org.openejb.transaction.TransactionContextInterceptor;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public class BMPEntityInterceptorBuilder extends AbstractInterceptorBuilder {
    private boolean reentrant;

    public boolean isReentrant() {
        return reentrant;
    }

    public void setReentrant(boolean reentrant) {
        this.reentrant = reentrant;
    }

    public TwoChains buildInterceptorChains() {
        if (transactionContextManager == null) {
            throw new IllegalStateException("Transaction context manager must be set before building the interceptor chain");
        }
        if (instancePool == null) {
            throw new IllegalStateException("Pool must be set before building the interceptor chain");
        }

        Interceptor firstInterceptor;
        firstInterceptor = new DispatchInterceptor(vtable);
        if (doAsCurrentCaller) {
            firstInterceptor = new EJBIdentityInterceptor(firstInterceptor);
        }

        // system interceptor only gets only dispatch, ejbIdentity, and component context
        Interceptor systemChain = firstInterceptor;
        systemChain = new ComponentContextInterceptor(systemChain, componentContext);

        if (securityEnabled) {
            firstInterceptor = new EJBSecurityInterceptor(firstInterceptor, policyContextId, permissionManager);
        }

        firstInterceptor = new EJBRunAsInterceptor(firstInterceptor, runAs);

        if (useContextHandler) {
            firstInterceptor = new PolicyContextHandlerEJBInterceptor(firstInterceptor);
        }
        firstInterceptor = new ComponentContextInterceptor(firstInterceptor, componentContext);
        if (trackedConnectionAssociator != null) {
            firstInterceptor = new ConnectionTrackingInterceptor(firstInterceptor, trackedConnectionAssociator);
        }
        firstInterceptor = new EntityInstanceInterceptor(firstInterceptor, containerId, instancePool, reentrant);
        firstInterceptor = new TransactionContextInterceptor(firstInterceptor, transactionContextManager, transactionPolicyManager);
        firstInterceptor = new SystemExceptionInterceptor(firstInterceptor, ejbName);
        return new TwoChains(firstInterceptor, systemChain);
    }
}
