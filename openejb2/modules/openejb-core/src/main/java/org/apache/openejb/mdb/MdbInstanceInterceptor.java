/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.mdb;

import org.apache.geronimo.interceptor.Interceptor;
import org.apache.geronimo.interceptor.Invocation;
import org.apache.geronimo.interceptor.InvocationResult;
import org.apache.openejb.transaction.EjbTransactionContext;
import org.apache.openejb.EJBInterfaceType;
import org.apache.openejb.EjbDeployment;
import org.apache.openejb.EjbInvocation;
import org.apache.openejb.EJBInstanceContext;
import org.apache.openejb.cache.InstancePool;


/**
 * Interceptor for MDB EJBs that obtains an instance
 * from a pool to execute the method.
 *
 * @version $Revision$ $Date$
 */
public final class MdbInstanceInterceptor implements Interceptor {
    private final Interceptor next;

    public MdbInstanceInterceptor(Interceptor next) {
        this.next = next;
    }

    public InvocationResult invoke(final Invocation invocation) throws Throwable {
        EjbInvocation ejbInvocation = (EjbInvocation) invocation;
        EjbDeployment deployment = ejbInvocation.getEjbDeployment();
        if (!(deployment instanceof MdbDeployment)) {
            throw new IllegalArgumentException("NewMdbInstanceInterceptor can only be used with a MdbDeploymentContext: " + deployment.getClass().getName());
        }

        InstancePool pool = ((MdbDeployment) deployment).getInstancePool();

        assert (ejbInvocation.getType() != EJBInterfaceType.HOME && ejbInvocation.getType() != EJBInterfaceType.LOCALHOME) : "Cannot invoke home method on a MDB";

        // get the context
        MdbInstanceContext ctx = (MdbInstanceContext) pool.acquire();
        assert ctx.getInstance() != null: "Got a context with no instance assigned";
        assert !ctx.isInCall() : "Acquired a context already in an invocation";
        ctx.setPool(pool);

        // initialize the context and set it into the invocation
        ejbInvocation.setEJBInstanceContext(ctx);

        EjbTransactionContext ejbTransactionContext = ejbInvocation.getEjbTransactionData();
        EJBInstanceContext oldContext = ejbTransactionContext.beginInvocation(ctx);
        try {
            InvocationResult result = next.invoke(invocation);
            return result;
        } catch (Throwable t) {
            // we must kill the instance when a system exception is thrown
            ctx.die();
            throw t;
        } finally {
            ejbTransactionContext.endInvocation(oldContext);

            // remove the reference to the context from the invocation
            ejbInvocation.setEJBInstanceContext(null);
        }
    }
}
