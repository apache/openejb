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
package org.apache.openejb.test.entity.cmp;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.openejb.test.TestManager;

/**
 * @version $Revision$ $Date$
 */
public class CmpTransactionTests extends org.apache.openejb.test.NamedTestCase {
    private InitialContext initialContext;
    private SessionFacadeHome ejbHome;
    private SessionFacadeObject ejbObject;
    
    public CmpTransactionTests() {
        super("Transaction.");
    }

    protected void setUp() throws Exception {
        super.setUp();

        Properties properties = TestManager.getServer().getContextEnvironment();
        properties.put(Context.SECURITY_PRINCIPAL, "ENTITY_TEST_CLIENT");
        properties.put(Context.SECURITY_CREDENTIALS, "ENTITY_TEST_CLIENT");

        initialContext = new InitialContext(properties);

        Object obj = initialContext.lookup("client/tests/entity/cmp/SessionFacadeBean");
        ejbHome = (SessionFacadeHome) javax.rmi.PortableRemoteObject.narrow(obj, SessionFacadeHome.class);
        ejbObject = ejbHome.create();
    }

    //===============================
    // Test ejb home methods
    //
    public void testInvokeCreateRemoveCreateSameCMP() {
        try {
            ejbObject.invokeCreateRemoveCreateSameCMP();
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }
    
    public void testInvokeCreateCreateSameCMP() {
        try {
            ejbObject.invokeCreateCreateSameCMP();
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }
    
    public void testInvokeCreateFindNoForceCacheFlush() {
        try {
            ejbObject.invokeCreateFindNoForceCacheFlush();
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void testInvokeCreateFindForceCacheFlush() {
        try {
            ejbObject.invokeCreateFindForceCacheFlush();
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }
    //
    // Test ejb home methods
    //===============================
}
