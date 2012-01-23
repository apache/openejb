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

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ejb.embeddable.EJBContainer;

public class OpenEJBDeploymentCamelTest extends CamelTestSupport {
    private static EJBContainer container;

    @EndpointInject(uri = "mock:deployResult")
    protected MockEndpoint deployResult;

    @EndpointInject(uri = "mock:undeployResult")
    protected MockEndpoint undeployResult;

    @EndpointInject(uri = "mock:autoResult")
    protected MockEndpoint autoResult;

    @Produce(uri = "direct:deploy")
    protected ProducerTemplate deployTemplate;

    @Produce(uri = "direct:auto")
    protected ProducerTemplate autoTemplate;

    @Produce(uri = "direct:undeploy")
    protected ProducerTemplate undeployTemplate;

    @BeforeClass
    public static void start() {
        System.setProperty("openejb.deployments.classpath.filter.systemapps", "false"); // to get the deployer
        container = EJBContainer.createEJBContainer();
    }

    @AfterClass
    public static void close() {
        if (container != null) {
            container.close();
        }
        System.clearProperty("openejb.deployments.classpath.filter.systemapps");
    }

    @Test
    public void deployUndeploy() throws Exception {
        final int appNbr = nbApp();
        final String path = "src/test/additional-resources/test.jar";

        deployResult.expectedMessageCount(1);
        deployResult.expectedPropertyReceived("location", path);
        deployTemplate.sendBody(path);
        deployResult.assertIsSatisfied();
        assertEquals(appNbr + 1, nbApp());

        undeployResult.expectedMessageCount(1);
        undeployResult.expectedPropertyReceived("location", path);
        undeployTemplate.sendBody(path);
        undeployResult.assertIsSatisfied();
        assertEquals(appNbr, nbApp());
    }

    @Test
    public void auto() throws Exception {
        final int appNbr = nbApp();
        final String path = "src/test/additional-resources/test.jar";

        autoResult.expectedMessageCount(1);
        autoResult.expectedPropertyReceived("location", path);
        autoTemplate.sendBodyAndHeader(path, "command", "deploy");
        autoResult.assertIsSatisfied();
        assertEquals(appNbr + 1, nbApp());

        autoResult.reset();
        autoResult.expectedMessageCount(1);
        autoResult.expectedPropertyReceived("location", path);
        autoTemplate.sendBodyAndHeader(path, "command", "undeploy");
        autoResult.assertIsSatisfied();
        assertEquals(appNbr, nbApp());
    }

    private static int nbApp() {
        return SystemInstance.get().getComponent(ContainerSystem.class).getAppContexts().size();
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from("direct:deploy")
                    .to("openejb:deploy")
                    .to("mock:deployResult");

                from("direct:undeploy")
                    .to("openejb:undeploy")
                    .to("mock:undeployResult");

                from("direct:auto")
                        .to("openejb:auto")
                        .to("mock:autoResult");
            }
        };
    }
}
