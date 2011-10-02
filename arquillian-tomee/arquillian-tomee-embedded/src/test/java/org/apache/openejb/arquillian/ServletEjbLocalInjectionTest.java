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
package org.apache.openejb.arquillian;

import org.apache.commons.lang.StringUtils;
import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.Node;
import org.jboss.shrinkwrap.descriptor.api.spec.servlet.web.WebAppDescriptor;
import org.jboss.shrinkwrap.descriptor.spi.NodeProvider;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

@RunWith(Arquillian.class)
public class ServletEjbLocalInjectionTest {

    public static final String TEST_NAME = ServletEjbLocalInjectionTest.class.getSimpleName();

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

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        WebAppDescriptor descriptor = Descriptors.create(WebAppDescriptor.class)
                .version("3.0")
                .servlet(PojoServlet.class, "/" + TEST_NAME);

        WebArchive archive = ShrinkWrap.create(WebArchive.class, TEST_NAME + ".war")
                .addClass(PojoServlet.class)
                .addClass(CompanyLocal.class)
                .addClass(Company.class)
                .addClass(DefaultCompany.class)
                .addClass(OtherCompany.class)
                .addClass(SuperMarket.class)
                .setWebXML(new StringAsset(descriptor.exportAsString()));

        return archive;
    }

    public static class PojoServlet extends HttpServlet {

        @EJB(beanName = "DefaultCompany")
        private CompanyLocal localCompany;

        @EJB
        private SuperMarket market;

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String name = req.getParameter("name");
            if (StringUtils.isEmpty(name)) {
                name = "OpenEJB";
            }

            if (localCompany != null) {
                resp.getOutputStream().println("Local: " + localCompany.employ(name));
            }
            if (market != null) {
                resp.getOutputStream().println(market.shop(name));
            }
        }
    }

    public static interface Company {
    }

    @Local
    public static interface CompanyLocal extends Company {
        public String employ(String employeeName);
    }

    @Stateless
    public static class DefaultCompany implements CompanyLocal {

        private final String name = "TomEE Software Inc.";

        public String employ(String employeeName) {
            return employeeName + " is employed at " + name;
        }

    }

    @Stateless
    public static class OtherCompany implements CompanyLocal {

        private final String name = "Other Software Inc.";

        public String employ(String employeeName) {
            return employeeName + " is employed at " + name;
        }

    }

    @Stateless
    @LocalBean
    public static class SuperMarket {

        private final String name = "Apache Marketplace";

        public String shop(String employeeName) {
            return employeeName + " shops at " + name;
        }

    }

    private void validateTest(String expectedOutput) throws IOException {
        final InputStream is = new URL("http://localhost:9080/" + TEST_NAME + "/" + TEST_NAME).openStream();
        final ByteArrayOutputStream os = new ByteArrayOutputStream();

        int bytesRead = -1;
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



