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
package org.apache.openejb.server.cxf;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPFaultException;
import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.server.cxf.fault.AuthenticatorService;
import org.apache.openejb.server.cxf.fault.WrongPasswordException;
import org.apache.openejb.server.cxf.fault.WrongPasswordRuntimeException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * @version $Rev$
 */
public class CxfJaxWsProviderTest {
    private static Context initialContext;

    @BeforeClass public static void setUp() {
        Properties properties = new Properties();
        properties.setProperty(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, Boolean.TRUE.toString());
        initialContext = EJBContainer.createEJBContainer(properties).getContext();
    }

    @AfterClass public static void tearDown() throws NamingException {
        initialContext.close();
    }

    @Test public void test00_runCheckedException() {
        try {
            AuthenticatorService withHandler = Service.create(
                new URL("http://localhost:4204/AuthenticatorServiceBean?wsdl"),
                new QName("http://superbiz.org/wsdl", "AuthenticatorServiceBeanService"))
                .getPort(AuthenticatorService.class);
            assertNotNull(withHandler);

            AuthenticatorService noHandler = Service.create(
                new URL("http://localhost:4204/AuthenticatorServiceBeanNoHandler?wsdl"),
                new QName("http://superbiz.org/wsdl", "AuthenticatorServiceBeanNoHandlerService"))
                .getPort(AuthenticatorService.class);
            assertNotNull(noHandler);

            WrongPasswordException wrongPasswordException = null;

            try {
                withHandler.authenticate("John", "Doe");
            } catch (WrongPasswordException e) {
                System.out.println("My lovely checked exception...");
                wrongPasswordException = e;
            } catch (Throwable e) {
                e.printStackTrace();
                fail("A throwable instead of a checked exception...");
            }
            assertNotNull(wrongPasswordException);

            wrongPasswordException = null;
            try {
                noHandler.authenticate("John", "Doe");
            } catch (WrongPasswordException e) {
                System.out.println("My lovely checked exception...");
                wrongPasswordException = e;
            } catch (Throwable e) {
                e.printStackTrace();
                fail("A throwable instead of a checked exception...");
            }
            assertNotNull(wrongPasswordException);

        } catch (MalformedURLException e) {
            fail("?!? invalid URL ???");
        }
    }

    @Test public void test01_runRuntimeException() {
        try {
            AuthenticatorService withHandler = Service.create(
                new URL("http://localhost:4204/AuthenticatorServiceBean?wsdl"),
                new QName("http://superbiz.org/wsdl", "AuthenticatorServiceBeanService"))
                .getPort(AuthenticatorService.class);
            assertNotNull(withHandler);

            AuthenticatorService noHandler = Service.create(
                new URL("http://localhost:4204/AuthenticatorServiceBeanNoHandler?wsdl"),
                new QName("http://superbiz.org/wsdl", "AuthenticatorServiceBeanNoHandlerService"))
                .getPort(AuthenticatorService.class);
            assertNotNull(noHandler);

            Throwable throwable = null;

            assertEquals("openejb", withHandler.authenticateRuntime("openejb", "ok").name);
            try {
                withHandler.authenticateRuntime("John", "Doe");
            } catch (WrongPasswordRuntimeException e) {
                fail("My checked exception instead of a throwableS...");
            } catch (Throwable e) {
                throwable = e;
                System.out.println("A throwable exception...");
            }
            assertTrue(throwable instanceof SOAPFaultException);


            throwable = null;
            try {
                noHandler.authenticateRuntime("John", "Doe");
            } catch (WrongPasswordRuntimeException e) {
                e.printStackTrace();
                fail("My checked exception instead of a throwableS...");
            } catch (Throwable e) {
                throwable = e;
                System.out.println("A throwable exception...");
            }
            assertTrue(throwable instanceof SOAPFaultException);

        } catch (MalformedURLException e) {
            fail("?!? invalid URL ???");
        }
    }

}
