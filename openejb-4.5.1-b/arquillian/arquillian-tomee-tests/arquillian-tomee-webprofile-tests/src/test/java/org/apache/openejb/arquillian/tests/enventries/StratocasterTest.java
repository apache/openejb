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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.arquillian.tests.ext.enventries;

import java.io.IOException;
import java.net.URL;
import org.apache.openejb.arquillian.tests.Tests;
import org.apache.openejb.arquillian.tests.enventry.Code;
import org.apache.openejb.arquillian.tests.enventry.PojoServlet;
import org.apache.ziplock.JarLocation;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.webapp30.WebAppDescriptor;
import org.jboss.shrinkwrap.descriptor.api.webcommon30.WebAppVersionType;
import org.jboss.shrinkwrap.descriptor.spi.node.Node;
import org.jboss.shrinkwrap.descriptor.spi.node.NodeDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @version $Rev$ $Date$
 */
@RunWith(Arquillian.class)
public class StratocasterTest {

    public static final String TEST_NAME = StratocasterTest.class.getSimpleName();

    @ArquillianResource
    private URL url;

    @Test
    public void lookupEnvEntryInjectionShouldSucceed() throws Exception {
        validateTest("[passed]");
    }

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        WebAppDescriptor descriptor = Descriptors.create(WebAppDescriptor.class)
                .version(WebAppVersionType._3_0)
                .createServlet().servletName("servlet").servletClass(Stratocaster.class.getName()).up()
                .createServletMapping().servletName("servlet").urlPattern("/" + TEST_NAME).up();

        addEnvEntry(descriptor, "guitarStringGuages", "java.lang.String", "E1=0.052\nA=0.042\nD=0.030\nG=0.017\nB=0.013\nE=0.010");
        addEnvEntry(descriptor, "certificateOfAuthenticity", "java.lang.String", "/tmp/strat-certificate.txt");
        addEnvEntry(descriptor, "dateCreated", "java.lang.String", "1962-03-01");
        addEnvEntry(descriptor, "pickups", "java.lang.String", "S,S,S");
        addEnvEntry(descriptor, "style", "java.lang.String", "VINTAGE");

        WebArchive archive = ShrinkWrap.create(WebArchive.class, TEST_NAME + ".war")
                .addClass(PojoServlet.class)
                .addClass(Code.class)
                .addClass(Stratocaster.class)
                .addClass(Pickup.class)
                .addClass(PickupEditor.class)
                .addClass(Style.class)
                .addAsLibraries(JarLocation.jarLocation(Test.class))
                .setWebXML(new StringAsset(descriptor.exportAsString()));



        return archive;
    }

    private static void addEnvEntry(WebAppDescriptor descriptor, String name, String type, String value) {
        Node appNode = ((NodeDescriptor) descriptor).getRootNode();
        appNode.createChild("/env-entry")
                .createChild("env-entry-name").text(name).getParent()
                .createChild("env-entry-type").text(type).getParent()
                .createChild("env-entry-value").text(value);

    }

    private void validateTest(String expectedOutput) throws IOException {
        Tests.assertOutput(url.toExternalForm() + TEST_NAME, expectedOutput);
    }

}
