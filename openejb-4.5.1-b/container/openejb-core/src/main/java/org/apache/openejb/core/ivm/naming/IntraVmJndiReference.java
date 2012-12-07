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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.core.ivm.naming;

import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.Strings;

public class IntraVmJndiReference extends Reference {

    private String jndiName;

    public IntraVmJndiReference(String jndiName) {
        this.jndiName = jndiName;
    }

    public String getJndiName() {
        return jndiName;
    }

    public Object getObject() throws NamingException {
        ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        try {
            return containerSystem.getJNDIContext().lookup(jndiName);
        } catch (NameNotFoundException e) { // EE.5.18: try using java:module/<shortName> prefix
            return containerSystem.getJNDIContext().lookup("java:module/" + Strings.lastPart(getClassName(), '.'));
        } catch (NamingException e) {
            throw (NamingException)new NamingException("could not look up " + jndiName).initCause(e);
        }
    }

    @Override
    public String toString() {
        return "IntraVmJndiReference{" +
                "jndiName='" + jndiName + '\'' +
                '}';
    }
}
