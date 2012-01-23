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
import org.apache.camel.impl.DefaultProducer;
import org.apache.openejb.assembler.Deployer;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Map;
import java.util.Properties;

public abstract class DeploymentProducer extends DefaultProducer {
    public static final String LOCATION_KEY = "location";
    public static final String INITIAL_CONTEXT_PROPERTIES = "properties";

    private String path;
    private Properties properties;

    public DeploymentProducer(final Endpoint ep, final Map<String, Object> parameters) {
        super(ep);
        if (parameters.containsKey(LOCATION_KEY)) {
            path = ep.getCamelContext().getTypeConverter().convertTo(String.class, parameters.get(LOCATION_KEY));
        } else {
            path = null;
        }
        if (parameters.containsKey(INITIAL_CONTEXT_PROPERTIES)) {
            properties = ep.getCamelContext().getTypeConverter().convertTo(Properties.class, parameters.get(INITIAL_CONTEXT_PROPERTIES));
        } else {
            properties = null;
        }
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        String location = exchange.getIn().getHeader(LOCATION_KEY, String.class);
        if (location == null) {
            location = path;
        }
        if (location == null) {
            location = exchange.getIn().getBody(String.class);
        }
        if (location == null) {
            throw new IllegalArgumentException(LOCATION_KEY + " should be provided by header or parameter");
        }
        doProcess(exchange, location);
    }

    protected abstract void doProcess(final Exchange exchange, final String path) throws CamelExchangeException;

    protected <T> T lookup(final Class<T> clazz, final String jndiName, final Properties exchangeProperties) throws NamingException {
        final Properties p = new Properties();
        if (exchangeProperties != null) {
            p.putAll(exchangeProperties);
        } else if (properties != null) {
            p.putAll(properties);
        } else {
            p.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.core.LocalInitialContextFactory");
        }
        return (T) new InitialContext(p).lookup(jndiName);
    }

    protected Deployer deployer(final Exchange exchange) throws NamingException {
        return lookup(Deployer.class, "openejb/DeployerBusinessRemote",
                exchange.getIn().getHeader(INITIAL_CONTEXT_PROPERTIES, Properties.class));
    }
}
