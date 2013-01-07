/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.apache.openejb.maven.plugin;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Properties;

public abstract class AbstractCommandMojo extends AbstractAddressMojo {
    protected Object lookup(String name) {
        final Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.RemoteInitialContextFactory");
        props.put(Context.PROVIDER_URL, "http://" + tomeeHost + ":" + tomeeHttpPort + "/tomee/ejb");
        if (user != null) {
            props.put(Context.SECURITY_PRINCIPAL, user);
        }
        if (password != null) {
            props.put(Context.SECURITY_CREDENTIALS, password);
        }
        if (realm != null) {
            props.put("openejb.authentication.realmName", realm);
        }

        try {
            return new InitialContext(props).lookup(name);
        } catch (Exception e) {
            throw new TomEEException(e.getMessage(), e);
        }
    }
}
