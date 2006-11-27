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

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.j2ee.deployment.ActivationSpecInfoLocator;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Jsr77Naming;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.repository.Artifact;

/**
 * @version $Revision$ $Date$
 */
public interface DeploymentTestContants {
    public static final AbstractName CONNECTION_OBJECT_NAME = IGNORE.createConnectionObjectName();

    public static final ActivationSpecInfoLocator ACTIVATION_SPEC_INFO_LOCATOR = new ActivationSpecInfoLocator() {

        public GBeanData locateActivationSpecInfo(AbstractNameQuery resourceAdapterModuleName, String messageListenerInterface, Configuration configuration) {
            return DeploymentHelper.ACTIVATION_SPEC_INFO;
        }
    };

    public static class IGNORE {
        private static AbstractName createConnectionObjectName() {
            Naming naming = new Jsr77Naming();
            Artifact artifact = new Artifact("geronimo", "defaultDatabase", "1.1", "car");
            AbstractName appName = naming.createRootName(artifact, NameFactory.NULL, NameFactory.J2EE_APPLICATION);
            AbstractName moduleName = naming.createChildName(appName, artifact.toString(), NameFactory.RESOURCE_ADAPTER_MODULE);
            AbstractName resourceAdapterName = naming.createChildName(moduleName, artifact.toString(), NameFactory.RESOURCE_ADAPTER);
            AbstractName jcaResourceName = naming.createChildName(resourceAdapterName, artifact.toString(), NameFactory.JCA_RESOURCE);
            AbstractName jcaConnectionFactoryName = naming.createChildName(jcaResourceName, "DefaultDatasource", NameFactory.JCA_CONNECTION_FACTORY);
            return naming.createChildName(jcaConnectionFactoryName, "DefaultDatasource", NameFactory.JCA_MANAGED_CONNECTION_FACTORY);
        }
    }
}
