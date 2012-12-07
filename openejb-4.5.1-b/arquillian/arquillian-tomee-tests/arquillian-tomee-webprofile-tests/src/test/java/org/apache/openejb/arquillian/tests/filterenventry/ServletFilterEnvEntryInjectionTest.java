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
package org.apache.openejb.arquillian.tests.filterenventry;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.webapp30.WebAppDescriptor;
import org.jboss.shrinkwrap.descriptor.api.webcommon30.WebAppVersionType;
import org.jboss.shrinkwrap.descriptor.spi.node.Node;
import org.jboss.shrinkwrap.descriptor.spi.node.NodeDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

@RunWith(Arquillian.class)
public class ServletFilterEnvEntryInjectionTest {

    public static final String TEST_NAME = ServletFilterEnvEntryInjectionTest.class.getSimpleName();

    @ArquillianResource
    private URL url;

    @Test
    public void localEjbInjectionShouldSucceed() throws Exception {
        final String expectedOutput = "Local: OpenEJB is employed at TomEE Software Inc.";
        validateTest(expectedOutput);
    }

    @Test
    public void localBeanEjbInjectionShouldSucceed() throws Exception {
        final String expectedOutput = "OpenEJB shops at Apache Marketplace";
        validateTest(expectedOutput);
    }

//    @Test
    public void pojoInjectionShouldSucceed() throws Exception {
        final String expectedOutput = "OpenEJB is on the wheel of a 2011 Lexus IS 350";
        validateTest(expectedOutput);
    }

    @Test
    public void stringEnvEntryInjectionShouldSucceed() throws Exception {
        final String expectedOutput = "tomee@apache.org";
        validateTest(expectedOutput);
    }

    @Test
    public void integerTypeEnvEntryInjectionShouldSucceed() throws Exception {
        final String expectedOutput = "Connection Pool: 20";
        validateTest(expectedOutput);
    }

    @Test
    public void longTypeEnvEntryInjectionShouldSucceed() throws Exception {
        final String expectedOutput = "Start Count: 200000";
        validateTest(expectedOutput);
    }

    @Test
    public void shortTypeEnvEntryInjectionShouldSucceed() throws Exception {
        final String expectedOutput = "Init Size: 5";
        validateTest(expectedOutput);
    }

    @Test
    public void byteTypeEnvEntryInjectionShouldSucceed() throws Exception {
        final String expectedOutput = "Total Quantity: 5";
        validateTest(expectedOutput);
    }

    @Test
    public void booleanTypeEnvEntryInjectionShouldSucceed() throws Exception {
        final String expectedOutput = "Enable Email: true";
        validateTest(expectedOutput);
    }

    @Test
    public void charTypeEnvEntryInjectionShouldSucceed() throws Exception {
        final String expectedOutput = "Option Default: X";
        validateTest(expectedOutput);
    }

//    @Test
    public void classEnvEntryInjectionShouldSucceed() throws Exception {
        final String expectedOutput = "java.lang.String";
        validateTest(expectedOutput);
    }

//    @Test
    public void enumEnvEntryInjectionShouldSucceed() throws Exception {
        final String expectedOutput = "DefaultCode: OK";
        validateTest(expectedOutput);
    }

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        WebAppDescriptor descriptor = Descriptors.create(WebAppDescriptor.class)
                .version(WebAppVersionType._3_0)
                .createFilter()
                    .filterName("filter").filterClass(PojoServletFilter.class.getName()).up()
                .createFilterMapping()
                    .filterName("filter").urlPattern("/" + TEST_NAME).up();

        addEnvEntry(descriptor, "returnEmail", "java.lang.String", "tomee@apache.org");
        addEnvEntry(descriptor, "connectionPool", "java.lang.Integer", "20");
        addEnvEntry(descriptor, "startCount", "java.lang.Long", "200000");
        addEnvEntry(descriptor, "initSize", "java.lang.Short", "5");
        addEnvEntry(descriptor, "enableEmail", "java.lang.Boolean", "true");
        addEnvEntry(descriptor, "totalQuantity", "java.lang.Byte", "5");
        addEnvEntry(descriptor, "optionDefault", "java.lang.Character", "X");
        addEnvEntry(descriptor, "auditWriter", "java.lang.Class", "java.lang.String");
//        addEnvEntry(descriptor, "defaultCode", "java.lang.Enum", "Code.OK");

        WebArchive archive = ShrinkWrap.create(WebArchive.class, TEST_NAME + ".war")
        		.addClass(Code.class)
        		.addClass(PojoServletFilter.class)
                .addClass(Car.class)
                .addClass(CompanyLocal.class)
                .addClass(Company.class)
                .addClass(DefaultCompany.class)
                .addClass(SuperMarket.class)
                .setWebXML(new StringAsset(descriptor.exportAsString()))
                .addAsWebInfResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"));



        return archive;
    }

    private static void addEnvEntry(WebAppDescriptor descriptor, String name, String type, String value) {
        Node appNode = ((NodeDescriptor) descriptor).getRootNode();
        appNode.createChild("/env-entry")
                .createChild("env-entry-name").text(name).getParent()
                .createChild("env-entry-type").text(type).getParent()
                .createChild("env-entry-value").text(value)
/*
                .parent()
                .create("injection-target")
                .create("injection-target-class").text("org.apache.openejb.arquillian.ServletPojoInjectionTest$PojoServletFilter")
                .parent()
                .create("injection-target-name").text(name)
*/
        ;

    }

    private void validateTest(String expectedOutput) throws IOException {
        final InputStream is = new URL(url.toExternalForm() + TEST_NAME).openStream();
        final ByteArrayOutputStream os = new ByteArrayOutputStream();

        int bytesRead;
        byte[] buffer = new byte[8192];
        while ((bytesRead = is.read(buffer)) > -1) {
            os.write(buffer, 0, bytesRead);
        }

        is.close();
        os.close();

        String output = new String(os.toByteArray(), "UTF-8");
        assertNotNull("Response shouldn't be null", output);
        assertTrue("Output should contain: " + expectedOutput, output.contains(expectedOutput));
    }

}



