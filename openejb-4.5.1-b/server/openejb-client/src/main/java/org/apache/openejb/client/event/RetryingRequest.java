/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.client.event;

import org.apache.openejb.client.Request;
import org.apache.openejb.client.ServerMetaData;

/**
 * @version $Rev$ $Date$
 */
@Log(Log.Level.FINE)
public class RetryingRequest {

    private final Request request;
    private final ServerMetaData serverMetaData;

    public RetryingRequest(Request request, ServerMetaData serverMetaData) {
        this.request = request;
        this.serverMetaData = serverMetaData;
    }

    public Request getRequest() {
        return request;
    }

    public ServerMetaData getServerMetaData() {
        return serverMetaData;
    }

    @Override
    public String toString() {
        return "RetryingRequest{" +
                "server=" + serverMetaData.getLocation() +
                "} " + request;
    }
}
