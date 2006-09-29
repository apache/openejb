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
package org.apache.openejb.cluster.server;

import org.codehaus.wadi.InvocationContext;
import org.codehaus.wadi.InvocationException;
import org.codehaus.wadi.PoolableInvocationWrapper;
import org.codehaus.wadi.Session;
import org.apache.openejb.EJBInstanceContext;
import org.apache.openejb.cache.InstanceCache;
import org.apache.openejb.sfsb.StatefulInstanceContext;

/**
 * 
 * @version $Revision$ $Date$
 */
public class EJBInvocationContext implements InvocationContext {
    private final boolean proxiedInvocation;
    private final InstanceCache cache;

    public EJBInvocationContext(InstanceCache cache) {
        this.cache = cache;
        this.proxiedInvocation = false;
    }

    public EJBInvocationContext() {
        cache = null;
        proxiedInvocation = true;
    }

    public void invoke(PoolableInvocationWrapper wrapper) throws InvocationException {
        if (!(wrapper instanceof EJBInvocationWrapper)) {
            throw new IllegalArgumentException(EJBInvocationProxy.class +
                    " is expected.");
        }
        EJBInvocationWrapper invWrap = (EJBInvocationWrapper) wrapper;

        Session session = invWrap.getSession();
        EJBSessionUtil sessionUtil = new EJBSessionUtil(session);
        Object id = sessionUtil.getId();
        EJBInstanceContext context = invWrap.getInstanceContext();

        if (!(context instanceof StatefulInstanceContext)) {
            throw new IllegalStateException("Context should be a " +
                    StatefulInstanceContext.class +
                    ". Was " + context.getClass());
        }
        StatefulInstanceContext sfContext = (StatefulInstanceContext) context;

        sfContext.setCache(cache);
        cache.putInactive(id, context);
    }

    public void invoke() throws InvocationException {
        throw new AssertionError();
    }

    public boolean isProxiedInvocation() {
        return proxiedInvocation;
    }
}