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

import org.apache.geronimo.interceptor.Interceptor;
import org.apache.geronimo.interceptor.Invocation;
import org.apache.geronimo.interceptor.InvocationResult;
import org.apache.openejb.EJBInstanceContext;
import org.apache.openejb.EjbInvocation;

/**
 * 
 * @version $Revision$ $Date$
 */
public class ClusteredInstanceInterceptor implements Interceptor {
    private final Interceptor next;
    
    public ClusteredInstanceInterceptor(Interceptor next) {
        this.next = next;
    }

    public InvocationResult invoke(final Invocation invocation) throws Throwable {
        InvocationResult result = next.invoke(invocation);
        
        EjbInvocation ejbInvocation = (EjbInvocation) invocation;
        EJBInstanceContext context = ejbInvocation.getEJBInstanceContext();
        if (context instanceof ClusteredEJBInstanceContext) {
            ClusteredEJBInstanceContext clusteredContext = (ClusteredEJBInstanceContext) context;
            result = new DefaultClusteredInvocationResult(result, clusteredContext.getServers());
        }
        
        return result;
    }

}
