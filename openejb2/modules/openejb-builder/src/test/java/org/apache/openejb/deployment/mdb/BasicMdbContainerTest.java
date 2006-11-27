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
package org.apache.openejb.deployment.mdb;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.repository.Dependency;
import org.apache.geronimo.kernel.repository.ImportType;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.openejb.deployment.DeploymentHelper;
import org.apache.openejb.deployment.MdbBuilder;
import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;

/**
 * @version $Revision$ $Date$
 */
public class BasicMdbContainerTest extends DeploymentHelper {
    protected void setUp() throws Exception {
        super.setUp();

        MdbBuilder builder = new MdbBuilder();
        builder.setContainerId(CONTAINER_NAME.toURI().toString());
        builder.setEjbName("MockEJB");

        builder.setEndpointInterfaceName("javax.jms.MessageListener");
        builder.setBeanClassName(MockEJB.class.getName());

        builder.setActivationSpecName(activationSpecName);

        builder.setEjbContainerName(mdbEjbContainerName);

        GBeanData deployment = builder.createConfiguration();

        //start the ejb container
        ConfigurationData configurationData = new ConfigurationData(TEST_CONFIGURATION_ID, kernel.getNaming());
        configurationData.getEnvironment().addDependency(new Dependency(BOOTSTRAP_ID, ImportType.ALL));
        configurationData.addGBean(deployment);
        ConfigurationManager configurationManager = ConfigurationUtil.getConfigurationManager(kernel);
        configurationManager.loadConfiguration(configurationData);
        configurationManager.startConfiguration(TEST_CONFIGURATION_ID);
    }

    public void testMessage() throws Exception {
        // @todo put a wait limit in here... otherwise this can lock a build
        // Wait for 3 messages to arrive..
        System.out.println("Waiting for message 1");
        assertTrue(MockEJB.messageCounter.tryAcquire(10000, TimeUnit.MILLISECONDS));
        System.out.println("Waiting for message 2");
        assertTrue(MockEJB.messageCounter.tryAcquire(10000, TimeUnit.MILLISECONDS));
        System.out.println("Waiting for message 3");
        assertTrue(MockEJB.messageCounter.tryAcquire(10000, TimeUnit.MILLISECONDS));

        System.out.println("Done.");
        assertTrue("Timer should have fired once by now...", MockEJB.timerFired);
    }
}
