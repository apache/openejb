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
package org.openejb;

import javax.security.auth.Subject;
import javax.naming.Context;

import org.apache.geronimo.transaction.TrackedConnectionAssociator;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.openejb.cache.InstanceCache;
import org.openejb.cache.InstanceFactory;
import org.openejb.cache.InstancePool;
import org.openejb.dispatch.VirtualOperation;
import org.openejb.security.PermissionManager;
import org.openejb.transaction.TransactionPolicyManager;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public abstract class AbstractInterceptorBuilder implements InterceptorBuilder {
    protected Object containerId;
    protected String ejbName;
    protected VirtualOperation[] vtable;
    protected Context componentContext;
    protected TransactionPolicyManager transactionPolicyManager;
    protected PermissionManager permissionManager;
    protected Subject runAs;
    protected boolean doAsCurrentCaller = false;
    protected boolean securityEnabled = false;
    protected boolean useContextHandler = false;
    protected String policyContextId;
    protected transient TransactionContextManager transactionContextManager;
    protected transient TrackedConnectionAssociator trackedConnectionAssociator;
    protected transient InstancePool instancePool;
    protected InstanceCache instanceCache;
    protected InstanceFactory instanceFactory;

    public void setContainerId(Object containerID) {
        assert (containerID != null) : "containerID is null!";
        this.containerId = containerID;
    }

    public void setEJBName(String ejbName) {
        assert (ejbName != null && ejbName.length() > 0) : "ejbName is null or empty";
        this.ejbName = ejbName;
    }

    public void setVtable(VirtualOperation[] vtable) {
        assert (vtable != null && vtable.length > 0) : "vtable is null or empty";
        this.vtable = vtable;
    }

    public void setComponentContext(Context componentContext) {
        assert (componentContext != null) : "componentContext is null";
        this.componentContext = componentContext;
    }

    public void setTransactionPolicyManager(TransactionPolicyManager transactionPolicyManager) {
        assert (transactionPolicyManager != null) : "transactionPolicyManager is null";
        this.transactionPolicyManager = transactionPolicyManager;
    }

    public void setPermissionManager(PermissionManager permissionManager) {
        assert (permissionManager != null) : "permissionManager is null";
        this.permissionManager = permissionManager;
    }

    public void setDoAsCurrentCaller(boolean doAsCurrentCaller) {
        this.doAsCurrentCaller = doAsCurrentCaller;
    }

    public void setPolicyContextId(String policyContextID) {
        this.policyContextId = policyContextID;
    }

    public void setRunAs(Subject runAs) {
        this.runAs = runAs;
    }

    public void setSecurityEnabled(boolean securityEnabled) {
        this.securityEnabled = securityEnabled;
    }

    public void setUseContextHandler(boolean useContextHandler) {
        this.useContextHandler = useContextHandler;
    }

    public void setTransactionContextManager(TransactionContextManager transactionContextManager) {
        this.transactionContextManager = transactionContextManager;
    }

    public void setTrackedConnectionAssociator(TrackedConnectionAssociator trackedConnectionAssociator) {
        this.trackedConnectionAssociator = trackedConnectionAssociator;
    }

    public void setInstancePool(InstancePool instancePool) {
        this.instancePool = instancePool;
    }

    public void setInstanceCache(InstanceCache instanceCache) {
        this.instanceCache = instanceCache;
    }

    public void setInstanceFactory(InstanceFactory instanceFactory) {
        this.instanceFactory = instanceFactory;
    }

}
