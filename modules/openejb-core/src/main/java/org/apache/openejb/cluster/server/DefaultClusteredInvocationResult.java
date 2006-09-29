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

import org.apache.geronimo.interceptor.InvocationResult;
import org.apache.openejb.client.ServerMetaData;

/**
 * 
 * @version $Revision$ $Date$
 */
class DefaultClusteredInvocationResult implements ClusteredInvocationResult {
    private final InvocationResult result;
    private final ServerMetaData[] servers;

    public DefaultClusteredInvocationResult(InvocationResult result, ServerMetaData[] servers) {
        this.result = result;
        this.servers = servers;
    }

    public ServerMetaData[] getServers() {
        return servers;
    }
    
    public Exception getException() {
        return result.getException();
    }

    public Object getResult() {
        return result.getResult();
    }

    public boolean isException() {
        return result.isException();
    }

    public boolean isNormal() {
        return result.isNormal();
    }
}