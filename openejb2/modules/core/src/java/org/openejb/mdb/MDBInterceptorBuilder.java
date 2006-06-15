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
package org.openejb.mdb;

import java.io.Serializable;
import javax.security.auth.Subject;
import javax.naming.Context;

import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.naming.java.ComponentContextInterceptor;
import org.apache.geronimo.transaction.TrackedConnectionAssociator;
import org.openejb.ConnectionTrackingInterceptor;
import org.openejb.SystemExceptionInterceptor;
import org.openejb.TwoChains;
import org.openejb.cache.InstancePool;
import org.openejb.dispatch.DispatchInterceptor;
import org.openejb.dispatch.VirtualOperation;
import org.openejb.security.EJBIdentityInterceptor;

/**
 * @version $Revision$ $Date$
 */
public class MDBInterceptorBuilder implements Serializable {
    private String ejbName;
    private VirtualOperation[] vtable;
    private Subject runAs;
    private Context componentContext;
    private boolean doAsCurrentCaller = false;
    private transient TrackedConnectionAssociator trackedConnectionAssociator;
    private transient InstancePool instancePool;

    public void setEJBName(String ejbName) {
        assert (ejbName != null && ejbName.length() > 0) : "ejbName is null or empty";
        this.ejbName = ejbName;
    }

    public void setVtable(VirtualOperation[] vtable) {
        assert (vtable != null && vtable.length > 0) : "vtable is null or empty";
        this.vtable = vtable;
    }

    public void setRunAs(Subject runAs) {
        this.runAs = runAs;
    }

    public void setComponentContext(Context componentContext) {
        assert (componentContext != null) : "componentContext is null";
        this.componentContext = componentContext;
    }

    public void setDoAsCurrentCaller(boolean doAsCurrentCaller) {
        this.doAsCurrentCaller = doAsCurrentCaller;
    }

    public void setTrackedConnectionAssociator(TrackedConnectionAssociator trackedConnectionAssociator) {
        this.trackedConnectionAssociator = trackedConnectionAssociator;
    }

    public void setInstancePool(InstancePool instancePool) {
        this.instancePool = instancePool;
    }

    public Interceptor getLifecycleInterceptorChain() {
        return null;
    }
    public TwoChains buildInterceptorChains() {
        if (instancePool == null) {
            throw new IllegalStateException("Pool must be set before building the interceptor chain");
        }

        Interceptor firstInterceptor;
        firstInterceptor = new DispatchInterceptor(vtable);
        firstInterceptor = new ComponentContextInterceptor(firstInterceptor, componentContext);
        if (doAsCurrentCaller) {
            firstInterceptor = new EJBIdentityInterceptor(firstInterceptor);
        }
        Interceptor systemChain = firstInterceptor;
        if (trackedConnectionAssociator != null) {
            firstInterceptor = new ConnectionTrackingInterceptor(firstInterceptor, trackedConnectionAssociator);
        }
        // firstInterceptor = new TransactionContextInterceptor(firstInterceptor, transactionContextManager, transactionPolicyManager);


        firstInterceptor = new MDBInstanceInterceptor(firstInterceptor, instancePool);
        firstInterceptor = new SystemExceptionInterceptor(firstInterceptor, ejbName);
        return new TwoChains(firstInterceptor, systemChain);
    }
}
