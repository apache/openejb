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
package org.apache.openejb.deployment;

import org.apache.geronimo.j2ee.deployment.NamingBuilder;
import org.apache.geronimo.j2ee.deployment.ActivationSpecInfoLocator;
import org.apache.geronimo.naming.deployment.ResourceEnvironmentSetter;

public abstract class XmlBeanBuilder {
    protected final OpenEjbModuleBuilder moduleBuilder;

    protected XmlBeanBuilder(OpenEjbModuleBuilder moduleBuilder) {
        this.moduleBuilder = moduleBuilder;
    }

    public OpenEjbModuleBuilder getModuleBuilder() {
        return moduleBuilder;
    }

    public NamingBuilder getNamingBuilders() {
        return moduleBuilder.getNamingBuilders();
    }

    public ResourceEnvironmentSetter getResourceEnvironmentSetter() {
        return moduleBuilder.getResourceEnvironmentSetter();
    }

    public ActivationSpecInfoLocator getActivationSpecInfoLocator() {
        return moduleBuilder.getActivationSpecInfoLocator();
    }
    protected String getStringValue(String in) {
        if (in == null) {
            return null;
        }
        return in.trim();
    }

    protected String getStringValue(org.apache.geronimo.xbeans.j2ee.String value) {
        if (value == null) {
            return null;
        }
        return value.getStringValue().trim();
    }
}