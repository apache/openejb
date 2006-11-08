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
package org.openejb.test.stateful;

import java.rmi.RemoteException;

import org.openejb.test.object.OperationsPolicy;

/**
 * [9] Should be run as the nineth test suite of the BasicStatefulTestClients
 * <p/>
 * <PRE>
 * =========================================================================
 * Operations allowed in the methods of a stateful SessionBean with
 * container-managed transaction demarcation
 * =========================================================================
 * <p/>
 * Bean method           | Bean method can perform the following operations
 * ______________________|__________________________________________________
 * |
 * constructor           | -
 * ______________________|__________________________________________________
 * |
 * setSessionContext     |  SessionContext methods:
 * |     - getEJBHome
 * |  JNDI access to java:comp/env
 * ______________________|__________________________________________________
 * |
 * ejbCreate             |  SessionContext methods:
 * ejbRemove             |     - getEJBHome
 * ejbActivate           |     - getCallerPrincipal
 * ejbPassivate          |     - isCallerInRole
 * |     - getEJBObject
 * |  JNDI access to java:comp/env
 * |  Resource manager access
 * |  Enterprise bean access
 * ______________________|__________________________________________________
 * |
 * business method       |  SessionContext methods:
 * from remote interface |     - getEJBHome
 * |     - getCallerPrincipal
 * |     - getRollbackOnly
 * |     - isCallerInRole
 * |     - setRollbackOnly
 * |     - getEJBObject
 * |  JNDI access to java:comp/env
 * |  Resource manager access
 * |  Enterprise bean access
 * ______________________|__________________________________________________
 * |
 * afterBegin            |  SessionContext methods:
 * beforeCompletion      |     - getEJBHome
 * |     - getCallerPrincipal
 * |     - getRollbackOnly
 * |     - isCallerInRole
 * |     - setRollbackOnly
 * |     - getEJBObject
 * |  JNDI access to java:comp/env
 * |  Resource manager access
 * |  Enterprise bean access
 * ______________________|__________________________________________________
 * |
 * afterCompletion       |  SessionContext methods:
 * |     - getEJBHome
 * |     - getCallerPrincipal
 * |     - isCallerInRole
 * |     - getEJBObject
 * |  JNDI access to java:comp/env
 * |  Resource manager access
 * |  Enterprise bean access
 * ______________________|__________________________________________________
 * </PRE>
 */
public class StatefulAllowedOperationsTests extends BasicStatefulTestClient {

    public StatefulAllowedOperationsTests() {
        super("AllowedOperations.");
    }

    protected void setUp() throws Exception {
        super.setUp();
        Object obj = initialContext.lookup("client/tests/stateful/BasicStatefulHome");
        ejbHome = (BasicStatefulHome) javax.rmi.PortableRemoteObject.narrow(obj, BasicStatefulHome.class);
        ejbObject = ejbHome.create("Fourth Bean");
        try {
            ejbObject.doNothing();
        } catch (RemoteException e) {
        }
        ejbHandle = ejbObject.getHandle();
        /* These tests will only work if the specified
         * method has already been called by the container.
         *
         * TO DO:
         * Implement a little application senario to ensure
         * that all methods tested for below have been called
         * by the container.
         */
    }

    protected void tearDown() throws Exception {
        ejbObject.remove();
        super.tearDown();
    }

    //=====================================
    // Test EJBContext allowed operations
    //
    /**
     * <PRE>
     * Bean method           | Bean method can perform the following operations
     * ______________________|__________________________________________________
     * |
     * setSessionContext     |  SessionContext methods:
     * |     - getEJBHome
     * |  JNDI access to java:comp/env
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void test01_setSessionContext() {
        try {
            OperationsPolicy policy = new OperationsPolicy();
            policy.allow(OperationsPolicy.Context_getEJBHome);
            policy.allow(OperationsPolicy.JNDI_access_to_java_comp_env);

            Object expected = policy;
            Object actual = ejbObject.getAllowedOperationsReport("setSessionContext");

            assertNotNull("The OperationsPolicy is null", actual);
            assertEquals(expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    /**
     * <PRE>
     * Bean method           | Bean method can perform the following operations
     * ______________________|__________________________________________________
     * |
     * ejbCreate             |  SessionContext methods:
     * ejbRemove             |     - getEJBHome
     * ejbActivate           |     - getCallerPrincipal
     * ejbPassivate          |     - isCallerInRole
     * |     - getEJBObject
     * |  JNDI access to java:comp/env
     * |  Resource manager access
     * |  Enterprise bean access
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void test02_ejbCreate() {
        try {
            OperationsPolicy policy = new OperationsPolicy();
            policy.allow(OperationsPolicy.Context_getEJBHome);
            policy.allow(OperationsPolicy.Context_getCallerPrincipal);
            policy.allow(OperationsPolicy.Context_isCallerInRole);
            policy.allow(OperationsPolicy.Context_getEJBObject);
            policy.allow(OperationsPolicy.JNDI_access_to_java_comp_env);
            policy.allow(OperationsPolicy.Resource_manager_access);
            policy.allow(OperationsPolicy.Enterprise_bean_access);

            Object expected = policy;
            Object actual = ejbObject.getAllowedOperationsReport("ejbCreate");

            assertNotNull("The OperationsPolicy is null", actual);
            assertEquals(expected, actual);

        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    /**
     * <PRE>
     * Bean method           | Bean method can perform the following operations
     * ______________________|__________________________________________________
     * |
     * ejbCreate             |  SessionContext methods:
     * ejbRemove             |     - getEJBHome
     * ejbActivate           |     - getCallerPrincipal
     * ejbPassivate          |     - isCallerInRole
     * |     - getEJBObject
     * |  JNDI access to java:comp/env
     * |  Resource manager access
     * |  Enterprise bean access
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void TODO_test03_ejbRemove() {
        try {
            //
            // The only way to tests remove it to store the operations report
            // in a static when calling the bean
            //
            OperationsPolicy policy = new OperationsPolicy();
            policy.allow(OperationsPolicy.Context_getEJBHome);
            policy.allow(OperationsPolicy.Context_getCallerPrincipal);
            policy.allow(OperationsPolicy.Context_isCallerInRole);
            policy.allow(OperationsPolicy.Context_getEJBObject);
            policy.allow(OperationsPolicy.JNDI_access_to_java_comp_env);
            policy.allow(OperationsPolicy.Resource_manager_access);
            policy.allow(OperationsPolicy.Enterprise_bean_access);

            Object expected = policy;
            Object actual = ejbObject.getAllowedOperationsReport("ejbRemove");

            assertNotNull("The OperationsPolicy is null", actual);
            assertEquals(expected, actual);

        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    /**
     * <PRE>
     * Bean method           | Bean method can perform the following operations
     * ______________________|__________________________________________________
     * |
     * ejbCreate             |  SessionContext methods:
     * ejbRemove             |     - getEJBHome
     * ejbActivate           |     - getCallerPrincipal
     * ejbPassivate          |     - isCallerInRole
     * |     - getEJBObject
     * |  JNDI access to java:comp/env
     * |  Resource manager access
     * |  Enterprise bean access
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void TODO_test04_ejbActivate() {
        try {
            //
            // This test is not really possible as it requires a forced passivation and activation
            //
            OperationsPolicy policy = new OperationsPolicy();
            policy.allow(OperationsPolicy.Context_getEJBHome);
            policy.allow(OperationsPolicy.Context_getCallerPrincipal);
            policy.allow(OperationsPolicy.Context_isCallerInRole);
            policy.allow(OperationsPolicy.Context_getEJBObject);
            policy.allow(OperationsPolicy.JNDI_access_to_java_comp_env);
            policy.allow(OperationsPolicy.Resource_manager_access);
            policy.allow(OperationsPolicy.Enterprise_bean_access);

            Object expected = policy;
            Object actual = ejbObject.getAllowedOperationsReport("ejbActivate");

            assertNotNull("The OperationsPolicy is null", actual);
            assertEquals(expected, actual);

        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    /**
     * <PRE>
     * Bean method           | Bean method can perform the following operations
     * ______________________|__________________________________________________
     * |
     * ejbCreate             |  SessionContext methods:
     * ejbRemove             |     - getEJBHome
     * ejbActivate           |     - getCallerPrincipal
     * ejbPassivate          |     - isCallerInRole
     * |     - getEJBObject
     * |  JNDI access to java:comp/env
     * |  Resource manager access
     * |  Enterprise bean access
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void TODO_test05_ejbPassivate() {
        try {
            //
            // This test is not really possible as it requires a forced passivation and activation
            //
            OperationsPolicy policy = new OperationsPolicy();
            policy.allow(OperationsPolicy.Context_getEJBHome);
            policy.allow(OperationsPolicy.Context_getCallerPrincipal);
            policy.allow(OperationsPolicy.Context_isCallerInRole);
            policy.allow(OperationsPolicy.Context_getEJBObject);
            policy.allow(OperationsPolicy.JNDI_access_to_java_comp_env);
            policy.allow(OperationsPolicy.Resource_manager_access);
            policy.allow(OperationsPolicy.Enterprise_bean_access);

            Object expected = policy;
            Object actual = ejbObject.getAllowedOperationsReport("ejbPassivate");

            assertNotNull("The OperationsPolicy is null", actual);
            assertEquals(expected, actual);

        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    /**
     * <PRE>
     * Bean method           | Bean method can perform the following operations
     * ______________________|__________________________________________________
     * |
     * business method       |  SessionContext methods:
     * from remote interface |     - getEJBHome
     * |     - getCallerPrincipal
     * |     - getRollbackOnly
     * |     - isCallerInRole
     * |     - setRollbackOnly
     * |     - getEJBObject
     * |  JNDI access to java:comp/env
     * |  Resource manager access
     * |  Enterprise bean access
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void test06_businessMethod() {
        try {
            OperationsPolicy policy = new OperationsPolicy();
            policy.allow(OperationsPolicy.Context_getEJBHome);
            policy.allow(OperationsPolicy.Context_getCallerPrincipal);
            policy.allow(OperationsPolicy.Context_getRollbackOnly);
            policy.allow(OperationsPolicy.Context_isCallerInRole);
            //policy.allow(OperationsPolicy.Context_setRollbackOnly);
            policy.allow(OperationsPolicy.Context_getEJBObject);
            policy.allow(OperationsPolicy.JNDI_access_to_java_comp_env);
            policy.allow(OperationsPolicy.Resource_manager_access);
            policy.allow(OperationsPolicy.Enterprise_bean_access);

            Object expected = policy;
            Object actual = ejbObject.getAllowedOperationsReport("businessMethod");

            assertNotNull("The OperationsPolicy is null", actual);
            assertEquals(expected, actual);

        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    /**
     * <PRE>
     * Bean method           | Bean method can perform the following operations
     * ______________________|__________________________________________________
     * |
     * afterBegin            |  SessionContext methods:
     * beforeCompletion      |     - getEJBHome
     * |     - getCallerPrincipal
     * |     - getRollbackOnly
     * |     - isCallerInRole
     * |     - setRollbackOnly
     * |     - getEJBObject
     * |  JNDI access to java:comp/env
     * |  Resource manager access
     * |  Enterprise bean access
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void test07_afterBegin() {
        try {
            //
            // todo should this be deleated?
            // This is a container managed bean so this should never be called
            //
            OperationsPolicy policy = new OperationsPolicy();
            policy.allow(OperationsPolicy.Context_getEJBHome);
            policy.allow(OperationsPolicy.Context_getCallerPrincipal);
            policy.allow(OperationsPolicy.Context_getRollbackOnly);
            policy.allow(OperationsPolicy.Context_isCallerInRole);
            //policy.allow(OperationsPolicy.Context_setRollbackOnly);
            policy.allow(OperationsPolicy.Context_getEJBObject);
            policy.allow(OperationsPolicy.JNDI_access_to_java_comp_env);
            policy.allow(OperationsPolicy.Resource_manager_access);
            policy.allow(OperationsPolicy.Enterprise_bean_access);

            Object expected = policy;
            Object actual = ejbObject.getAllowedOperationsReport("afterBegin");

            assertNotNull("The OperationsPolicy is null", actual);
            assertEquals(expected, actual);

        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    /**
     * <PRE>
     * Bean method           | Bean method can perform the following operations
     * ______________________|__________________________________________________
     * |
     * afterBegin            |  SessionContext methods:
     * beforeCompletion      |     - getEJBHome
     * |     - getCallerPrincipal
     * |     - getRollbackOnly
     * |     - isCallerInRole
     * |     - setRollbackOnly
     * |     - getEJBObject
     * |  JNDI access to java:comp/env
     * |  Resource manager access
     * |  Enterprise bean access
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void test08_beforeCompletion() {
        try {
            //
            // todo should this be deleated?
            // This is a container managed bean so this should never be called
            //
            OperationsPolicy policy = new OperationsPolicy();
            policy.allow(OperationsPolicy.Context_getEJBHome);
            policy.allow(OperationsPolicy.Context_getCallerPrincipal);
            policy.allow(OperationsPolicy.Context_getRollbackOnly);
            policy.allow(OperationsPolicy.Context_isCallerInRole);
            //policy.allow(OperationsPolicy.Context_setRollbackOnly);
            policy.allow(OperationsPolicy.Context_getEJBObject);
            policy.allow(OperationsPolicy.JNDI_access_to_java_comp_env);
            policy.allow(OperationsPolicy.Resource_manager_access);
            policy.allow(OperationsPolicy.Enterprise_bean_access);

            Object expected = policy;
            Object actual = ejbObject.getAllowedOperationsReport("beforeCompletion");

            assertNotNull("The OperationsPolicy is null", actual);
            assertEquals(expected, actual);

        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    /**
     * <PRE>
     * Bean method           | Bean method can perform the following operations
     * ______________________|__________________________________________________
     * |
     * afterCompletion       |  SessionContext methods:
     * |     - getEJBHome
     * |     - getCallerPrincipal
     * |     - isCallerInRole
     * |     - getEJBObject
     * |  JNDI access to java:comp/env
     * |  Resource manager access
     * |  Enterprise bean access
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void test09_afterCompletion() {
        try {
            //
            // todo should this be deleated?
            // This is a container managed bean so this should never be called
            //
            OperationsPolicy policy = new OperationsPolicy();
            policy.allow(OperationsPolicy.Context_getEJBHome);
            policy.allow(OperationsPolicy.Context_getCallerPrincipal);
            policy.allow(OperationsPolicy.Context_isCallerInRole);
            policy.allow(OperationsPolicy.Context_getEJBObject);
            policy.allow(OperationsPolicy.JNDI_access_to_java_comp_env);
            policy.allow(OperationsPolicy.Resource_manager_access);
            policy.allow(OperationsPolicy.Enterprise_bean_access);

            Object expected = policy;
            Object actual = ejbObject.getAllowedOperationsReport("afterCompletion");

            assertNotNull("The OperationsPolicy is null", actual);
            assertEquals(expected, actual);

        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }
    //
    // Test EJBContext allowed operations
    //=====================================
}


