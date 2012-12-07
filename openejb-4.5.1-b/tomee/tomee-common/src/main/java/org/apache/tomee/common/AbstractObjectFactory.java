/**
 *
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
package org.apache.tomee.common;

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.Strings;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;
import java.util.Hashtable;

public abstract class AbstractObjectFactory implements ObjectFactory {
    public Object getObjectInstance(Object object, Name name, Context context, Hashtable environment) throws Exception {
        Reference ref = (Reference) object;

        // the jndi context to use for the lookup (usually null which is the default context)
        String jndiProviderId = NamingUtil.getProperty(ref, NamingUtil.JNDI_PROVIDER_ID);

        // the jndi name
        String jndiName = NamingUtil.getProperty(ref, NamingUtil.JNDI_NAME);
        if (jndiName == null) {
            jndiName = buildJndiName(ref);
        }

        // look up the reference
        try {
            return lookup(jndiProviderId, jndiName);
        } catch (NameNotFoundException nnfe) { // EE.5.18: try using java:module/<shortName> prefix

            // 2nd try
            if (jndiName.startsWith("java:")) {
                try {
                    return new InitialContext().lookup(jndiName);
                } catch (NameNotFoundException ignored) {
                }
            }

            // 3rd try
            if (ref.getClassName() != null) {
                try {
                    return new InitialContext().lookup("java:module/" + Strings.lastPart(ref.getClassName(), '.'));
                } catch (NameNotFoundException ignored) {
                }
            }

            throw nnfe;
        }
    }

    protected abstract String buildJndiName(Reference reference) throws NamingException;

    protected Object lookup(String jndiProviderId, String jndiName) throws NamingException {
        Context externalContext = getContext(jndiProviderId);
        synchronized (externalContext) {
            /* According to the JNDI SPI specification multiple threads may not access the same JNDI
            Context *instance* concurrently. Since we don't know the origines of the federated context we must
            synchonrize access to it.  JNDI SPI Sepecifiation 1.2 Section 2.2
            */
            return externalContext.lookup(jndiName);
        }
    }

    protected Context getContext(String jndiProviderId) throws NamingException {
        if (jndiProviderId != null) {
            String contextJndiName = "java:openejb/remote_jndi_contexts/" + jndiProviderId;
            ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
            Context context = (Context) containerSystem.getJNDIContext().lookup(contextJndiName);
            return context;
        } else {
            ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
            Context context = containerSystem.getJNDIContext();
            return context;
        }
    }

}
