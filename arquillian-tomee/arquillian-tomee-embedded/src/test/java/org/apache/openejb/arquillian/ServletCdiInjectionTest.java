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

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.enterprise.inject.spi.BeanManager;
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
public class ServletCdiInjectionTest {

    public static final String TEST_NAME = ServletCdiInjectionTest.class.getSimpleName();

    @Test
    public void pojoInjectionShouldSucceed() throws Exception {
        validateTest("OpenEJB is on the wheel of a 2011 Lexus IS 350");
    }

    @Test
    public void beanManagerInjectionShouldSucceed() throws Exception {
        validateTest("beanManager");
    }


//    @Test
    public void testNothing() {
    }

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        WebAppDescriptor descriptor = Descriptors.create(WebAppDescriptor.class)
                .version("3.0")
                .servlet(PojoServlet.class, "/" + TEST_NAME);

        WebArchive archive = ShrinkWrap.create(WebArchive.class, TEST_NAME + ".war")
                .addClass(PojoServlet.class)
                .addClass(Car.class)
                .setWebXML(new StringAsset(descriptor.exportAsString()))
                .addAsWebInfResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"));

        System.err.println(descriptor.exportAsString());

        return archive;
    }

    public static class PojoServlet extends HttpServlet {

        @Resource
        private BeanManager beanManager;

        @Inject
        private Car car;

        @PostConstruct
        public void construct() {
            System.out.println("construct");
        }

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String name = req.getParameter("name");
            if (StringUtils.isEmpty(name)) {
                name = "OpenEJB";
            }

            if (car != null) {
                resp.getOutputStream().println(car.drive(name));
            }

            if (beanManager != null) {
                resp.getOutputStream().println("beanManager");
            }
        }
    }

    public static class Car {
        private final String make = "Lexus", model = "IS 350";
        private final int year = 2011;

        public String drive(String name) {
            return name + " is on the wheel of a " + year + " " + make + " " + model;
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



