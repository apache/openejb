/**
 *
 * Copyright 2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.openejb;

import java.io.Serializable;
import java.util.Set;

import javax.security.auth.Subject;
import javax.transaction.TransactionManager;

import org.apache.geronimo.naming.java.ReadOnlyContext;
import org.apache.geronimo.transaction.TrackedConnectionAssociator;
import org.apache.geronimo.core.service.Interceptor;

import org.openejb.dispatch.VirtualOperation;
import org.openejb.transaction.TransactionPolicyManager;
import org.openejb.security.PermissionManager;
import org.openejb.cache.InstancePool;
import org.openejb.cache.InstanceCache;
import org.openejb.cache.InstanceFactory;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public interface InterceptorBuilder extends Serializable {
    void setContainerId(Object contextId);

    void setEJBName(String ejbName);

    void setVtable(VirtualOperation[] vtable);

    void setRunAs(Subject runAs);

    void setComponentContext(ReadOnlyContext componentContext);

    void setTransactionPolicyManager(TransactionPolicyManager transactionPolicyManager);

    void setPermissionManager(PermissionManager permissionManager);

    void setSetIdentityEnabled(boolean setIdentityEnabled);

    void setSecurityEnabled(boolean securityEnabled);

    void setTransactionManager(TransactionManager transactionManager);

    void setTrackedConnectionAssociator(TrackedConnectionAssociator trackedConnectionAssociator);

    void setInstancePool(InstancePool pool);

    void setInstanceCache(InstanceCache instanceCache);

    void setInstanceFactory(InstanceFactory instanceFactory);

    TwoChains buildInterceptorChains();

}
