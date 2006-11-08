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
package org.apache.openejb.test.entity.cmp2;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.openejb.test.NamedTestCase;
import org.apache.openejb.test.TestManager;
import org.apache.openejb.test.entity.cmp2.petstore.AddressLocal;
import org.apache.openejb.test.entity.cmp2.petstore.AddressLocalHome;

/**
 * @version $Revision$ $Date$
 */
public class PetstoreTests extends NamedTestCase {
    private InitialContext initialContext;

    private AddressLocalHome ejbHome;

    public PetstoreTests() {
        super("PetstoreTests.");
    }

    public void test00_dummy() {
        // do nothing
        // remove the test after the tests marked with BUG are fixed.
    }

    public void BUG_test01_create() {
        try {
            ejbHome = (AddressLocalHome) javax.rmi.PortableRemoteObject.narrow(initialContext.lookup("client/tests/cmp2/petstore/Address"), AddressLocalHome.class);
            AddressLocal address = ejbHome.create();
            assertNotNull("Couldn't create a Address local instance", address);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void BUG_test02_setterAfterCreate() {
        try {
            ejbHome = (AddressLocalHome) javax.rmi.PortableRemoteObject.narrow(initialContext.lookup("client/tests/cmp2/petstore/Address"), AddressLocalHome.class);
            AddressLocal address = ejbHome.create();
            address.setStreet("dummy");
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    protected void setUp() throws Exception {
        Properties properties = TestManager.getServer().getContextEnvironment();
        properties.put(Context.SECURITY_PRINCIPAL, "ENTITY_TEST_CLIENT");
        properties.put(Context.SECURITY_CREDENTIALS, "ENTITY_TEST_CLIENT");

        initialContext = new InitialContext(properties);
    }

    protected void tearDown() throws Exception {
    }
}
