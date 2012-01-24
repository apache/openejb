/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.camel;

import org.apache.camel.CamelExchangeException;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;

import java.util.Map;

public class AutoProducer extends DeploymentProducer {
    public static final String COMMAND = "command";

    private Map<String, Object> parameters;

    public AutoProducer(final Endpoint ep, final Map<String, Object> parameters) {
        super(ep, parameters);
        this.parameters = parameters;
    }

    @Override
    protected void doProcess(final Exchange exchange, final String path) throws CamelExchangeException {
        final String cmd = exchange.getIn().getHeader(COMMAND, String.class);
        final DeploymentProducer producer = (DeploymentProducer) ((OpenEJBEndpoint) getEndpoint()).createOpenEJBProducer(cmd, getEndpoint(), parameters);
        producer.doProcess(exchange, path);
    }
}
