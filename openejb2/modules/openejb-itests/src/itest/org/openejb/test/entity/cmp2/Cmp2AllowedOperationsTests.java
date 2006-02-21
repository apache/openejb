/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.test.entity.cmp2;

import org.openejb.test.object.OperationsPolicy;
import org.openejb.test.entity.cmp.BasicCmpHome;

/**
 * [9] Should be run as the nineth test suite of the BasicCmpTestClients
 * <p/>
 * <PRE>
 * =========================================================================
 * Operations allowed in the methods of an entity bean
 * =========================================================================
 * <p/>
 * Bean method           | Bean method can perform the following operations
 * ______________________|__________________________________________________
 * |
 * constructor           | -
 * ______________________|__________________________________________________
 * |
 * setEntityContext      |  EntityContext methods:
 * unsetEntityContext    |     - getEJBHome
 * |  JNDI access to java:comp/env
 * ______________________|__________________________________________________
 * |
 * ejbCreate             |  EntityContext methods:
 * |     - getEJBHome
 * |     - getCallerPrincipal
 * |     - getRollbackOnly
 * |     - isCallerInRole
 * |     - setRollbackOnly
 * |  JNDI access to java:comp/env
 * |  Resource manager access
 * |  Enterprise bean access
 * ______________________|__________________________________________________
 * |
 * ejbPostCreate         |  EntityContext methods:
 * |     - getEJBHome
 * |     - getCallerPrincipal
 * |     - getRollbackOnly
 * |     - isCallerInRole
 * |     - setRollbackOnly
 * |     - getEJBObject
 * |     - getPrimaryKey
 * |  JNDI access to java:comp/env
 * |  Resource manager access
 * |  Enterprise bean access
 * ______________________|__________________________________________________
 * |
 * ejbRemove             |  EntityContext methods:
 * |     - getEJBHome
 * |     - getCallerPrincipal
 * |     - getRollbackOnly
 * |     - isCallerInRole
 * |     - setRollbackOnly
 * |     - getEJBObject
 * |     - getPrimaryKey
 * |  JNDI access to java:comp/env
 * |  Resource manager access
 * |  Enterprise bean access
 * ______________________|__________________________________________________
 * |
 * ejbFind*              |  EntityContext methods:
 * ejbSelect*            |     - getEJBHome
 * ejbHome               |     - getCallerPrincipal
 * |     - getRollbackOnly
 * |     - isCallerInRole
 * |     - setRollbackOnly
 * |  JNDI access to java:comp/env
 * |  Resource manager access
 * |  Enterprise bean access
 * ______________________|__________________________________________________
 * |
 * ejbSelectInEntity*    |  EntityContext methods:
 * |     - getEJBHome
 * |     - getCallerPrincipal
 * |     - getRollbackOnly
 * |     - isCallerInRole
 * |     - setRollbackOnly
 * |     - getEJBObject
 * |     - getPrimaryKey
 * |  JNDI access to java:comp/env
 * |  Resource manager access
 * |  Enterprise bean access
 * ______________________|__________________________________________________
 * |
 * ejbActivate           |  EntityContext methods:
 * ejbPassivate          |     - getEJBHome
 * |     - getEJBObject
 * |     - getPrimaryKey
 * |  JNDI access to java:comp/env
 * ______________________|__________________________________________________
 * |
 * ejbLoad               |  EntityContext methods:
 * ejbStore              |     - getEJBHome
 * |     - getCallerPrincipal
 * |     - getRollbackOnly
 * |     - isCallerInRole
 * |     - setRollbackOnly
 * |     - getEJBObject
 * |     - getPrimaryKey
 * |  JNDI access to java:comp/env
 * |  Resource manager access
 * |  Enterprise bean access
 * ______________________|__________________________________________________
 * |
 * business method       |  EntityContext methods:
 * from remote interface |     - getEJBHome
 * |     - getCallerPrincipal
 * |     - getRollbackOnly
 * |     - isCallerInRole
 * |     - setRollbackOnly
 * |     - getEJBObject
 * |     - getPrimaryKey
 * |  JNDI access to java:comp/env
 * |  Resource manager access
 * |  Enterprise bean access
 * ______________________|__________________________________________________
 * </PRE>
 */
public class Cmp2AllowedOperationsTests extends BasicCmp2TestClient {

    public Cmp2AllowedOperationsTests() {
        super("AllowedOperations.");
    }

    protected void setUp() throws Exception {
        super.setUp();
        Object obj = initialContext.lookup("client/tests/entity/cmp2/allowed_operations/EntityHome");
        ejbHome = (BasicCmpHome) javax.rmi.PortableRemoteObject.narrow(obj, BasicCmpHome.class);
        ejbObject = ejbHome.create("Fourth Bean");
        ejbHandle = ejbObject.getHandle();
        /* These tests will only work if the specified
         * method has already been called by the container.
         *
         * TO DO:
         * Implement a little application senario to ensure
         * that all methods tested for below have been called
         * by the container.
         */
        doScenario();


    }

    protected void tearDown() throws Exception {
        try {
            ejbObject.remove();
        } catch (Throwable e) {
            // todo this throws an exception for an unrelated reason....
        }
        super.tearDown();
    }

    /**
     * This method ensures that all thee bean methods have been invoked for correct behaviour
     * of the tests.
     */
    private void doScenario() throws Exception {

//      ejbHome.remove(ejbObject.getPrimaryKey());
        try {
            ejbObject.remove();
        } catch (Throwable e) {
            // todo this throws an exception for an unrelated reason....
        }

        ejbObject = null;
        ejbObject = ejbHome.create("Blah Bean");

        // Call the business method
        ejbObject.businessMethod("Reverse Me");

        // Change the object... ejbStore
//      ejbObject.setLastName("Changed");

      ejbHome.sum(1, 2);
    }

    //=====================================
    // Test EJBContext allowed operations       
    //
    /**
     * <PRE>
     * Bean method           | Bean method can perform the following operations
     * ______________________|__________________________________________________
     * |
     * setEntityContext      |  EntityContext methods:
     * unsetEntityContext    |     - getEJBHome
     * |  JNDI access to java:comp/env
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void test01_setEntityContext() {
        try {

            OperationsPolicy policy = new OperationsPolicy();
            policy.allow(OperationsPolicy.Context_getEJBHome);
            policy.allow(OperationsPolicy.JNDI_access_to_java_comp_env);

            Object expected = policy;
            Object actual = ejbObject.getAllowedOperationsReport("setEntityContext");

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
     * setEntityContext      |  EntityContext methods:
     * unsetEntityContext    |     - getEJBHome
     * |  JNDI access to java:comp/env
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void TODO_test02_unsetEntityContext() {
        try {

            /* TO DO:  This test needs unique functionality to work */
            OperationsPolicy policy = new OperationsPolicy();
            policy.allow(OperationsPolicy.Context_getEJBHome);
            policy.allow(OperationsPolicy.JNDI_access_to_java_comp_env);

            Object expected = policy;
            Object actual = ejbObject.getAllowedOperationsReport("unsetEntityContext");

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
     * ejbCreate             |  EntityContext methods:
     * |     - getEJBHome
     * |     - getCallerPrincipal
     * |     - getRollbackOnly
     * |     - isCallerInRole
     * |     - setRollbackOnly
     * |  JNDI access to java:comp/env
     * |  Resource manager access
     * |  Enterprise bean access
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void test03_ejbCreate() {
        try {

            OperationsPolicy policy = new OperationsPolicy();
            policy.allow(OperationsPolicy.Context_getEJBHome);
            policy.allow(OperationsPolicy.Context_getCallerPrincipal);
            policy.allow(OperationsPolicy.Context_isCallerInRole);
            policy.allow(OperationsPolicy.Context_getRollbackOnly);
//        policy.allow( OperationsPolicy.Context_setRollbackOnly );
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
     * ejbPostCreate         |  EntityContext methods:
     * |     - getEJBHome
     * |     - getCallerPrincipal
     * |     - getRollbackOnly
     * |     - isCallerInRole
     * |     - setRollbackOnly
     * |     - getEJBObject
     * |     - getPrimaryKey
     * |  JNDI access to java:comp/env
     * |  Resource manager access
     * |  Enterprise bean access
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void test04_ejbPostCreate() {
        try {

            OperationsPolicy policy = new OperationsPolicy();
            policy.allow(OperationsPolicy.Context_getEJBHome);
            policy.allow(OperationsPolicy.Context_getCallerPrincipal);
            policy.allow(OperationsPolicy.Context_getRollbackOnly);
            policy.allow(OperationsPolicy.Context_isCallerInRole);
//        policy.allow( OperationsPolicy.Context_setRollbackOnly );
            policy.allow(OperationsPolicy.Context_getEJBObject);
            policy.allow(OperationsPolicy.Context_getPrimaryKey);
            policy.allow(OperationsPolicy.JNDI_access_to_java_comp_env);
            policy.allow(OperationsPolicy.Resource_manager_access);
            policy.allow(OperationsPolicy.Enterprise_bean_access);

            Object expected = policy;
            Object actual = ejbObject.getAllowedOperationsReport("ejbPostCreate");

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
     * ejbRemove             |  EntityContext methods:
     * |     - getEJBHome
     * |     - getCallerPrincipal
     * |     - getRollbackOnly
     * |     - isCallerInRole
     * |     - setRollbackOnly
     * |     - getEJBObject
     * |     - getPrimaryKey
     * |  JNDI access to java:comp/env
     * |  Resource manager access
     * |  Enterprise bean access
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void test05_ejbRemove() {
        try {

            OperationsPolicy policy = new OperationsPolicy();
            policy.allow(OperationsPolicy.Context_getEJBHome);
            policy.allow(OperationsPolicy.Context_getCallerPrincipal);
            policy.allow(OperationsPolicy.Context_getRollbackOnly);
            policy.allow(OperationsPolicy.Context_isCallerInRole);
//        policy.allow( OperationsPolicy.Context_setRollbackOnly );
            policy.allow(OperationsPolicy.Context_getEJBObject);
            policy.allow(OperationsPolicy.Context_getPrimaryKey);
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
     * ejbFind*              |  EntityContext methods:
     * ejbSelect*            |     - getEJBHome
     * ejbHome               |     - getCallerPrincipal
     * |     - getRollbackOnly
     * |     - isCallerInRole
     * |     - setRollbackOnly
     * |  JNDI access to java:comp/env
     * |  Resource manager access
     * |  Enterprise bean access
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void TODO_test06_ejbFind() {
        //
        // This doesn't seem like a valid test
        //
        try {

            OperationsPolicy policy = new OperationsPolicy();
            policy.allow(OperationsPolicy.Context_getEJBHome);
            policy.allow(OperationsPolicy.Context_getCallerPrincipal);
            policy.allow(OperationsPolicy.Context_getRollbackOnly);
            policy.allow(OperationsPolicy.Context_isCallerInRole);
//        policy.allow( policy.Context_setRollbackOnly );
            policy.allow(OperationsPolicy.JNDI_access_to_java_comp_env);
            policy.allow(OperationsPolicy.Resource_manager_access);
            policy.allow(OperationsPolicy.Enterprise_bean_access);

            Object expected = policy;
            Object actual = ejbObject.getAllowedOperationsReport("ejbFind");

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
     * ejbFind*              |  EntityContext methods:
     * ejbSelect*            |     - getEJBHome
     * ejbHome               |     - getCallerPrincipal
     * |     - getRollbackOnly
     * |     - isCallerInRole
     * |     - setRollbackOnly
     * |  JNDI access to java:comp/env
     * |  Resource manager access
     * |  Enterprise bean access
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void TODO_test07_ejbSelect() {
        //
        // This doesn't seem like a valid test
        //
        try {

            OperationsPolicy policy = new OperationsPolicy();
            policy.allow(OperationsPolicy.Context_getEJBHome);
            policy.allow(OperationsPolicy.Context_getCallerPrincipal);
            policy.allow(OperationsPolicy.Context_getRollbackOnly);
            policy.allow(OperationsPolicy.Context_isCallerInRole);
//        policy.allow( OperationsPolicy.Context_setRollbackOnly );
            policy.allow(OperationsPolicy.JNDI_access_to_java_comp_env);
            policy.allow(OperationsPolicy.Resource_manager_access);
            policy.allow(OperationsPolicy.Enterprise_bean_access);

            Object expected = policy;
            Object actual = ejbObject.getAllowedOperationsReport("ejbSelect");

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
     * ejbFind*              |  EntityContext methods:
     * ejbSelect*            |     - getEJBHome
     * ejbHome               |     - getCallerPrincipal
     * |     - getRollbackOnly
     * |     - isCallerInRole
     * |     - setRollbackOnly
     * |  JNDI access to java:comp/env
     * |  Resource manager access
     * |  Enterprise bean access
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void test08_ejbHome() {
        try {

            OperationsPolicy policy = new OperationsPolicy();
            policy.allow(OperationsPolicy.Context_getEJBHome);
            policy.allow(OperationsPolicy.Context_getCallerPrincipal);
            policy.allow(OperationsPolicy.Context_getRollbackOnly);
            policy.allow(OperationsPolicy.Context_isCallerInRole);
//        policy.allow( OperationsPolicy.Context_setRollbackOnly );
            policy.allow(OperationsPolicy.JNDI_access_to_java_comp_env);
            policy.allow(OperationsPolicy.Resource_manager_access);
            policy.allow(OperationsPolicy.Enterprise_bean_access);

            Object expected = policy;
            Object actual = ejbObject.getAllowedOperationsReport("ejbHome");

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
     * ejbSelectInEntity*    |  EntityContext methods:
     * |     - getEJBHome
     * |     - getCallerPrincipal
     * |     - getRollbackOnly
     * |     - isCallerInRole
     * |     - setRollbackOnly
     * |     - getEJBObject
     * |     - getPrimaryKey
     * |  JNDI access to java:comp/env
     * |  Resource manager access
     * |  Enterprise bean access
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void TODO_test09_ejbSelectInEntity() {
        try {

            OperationsPolicy policy = new OperationsPolicy();
            policy.allow(OperationsPolicy.Context_getEJBHome);
            policy.allow(OperationsPolicy.Context_getCallerPrincipal);
            policy.allow(OperationsPolicy.Context_getRollbackOnly);
            policy.allow(OperationsPolicy.Context_isCallerInRole);
//        policy.allow( OperationsPolicy.Context_setRollbackOnly );
            policy.allow(OperationsPolicy.Context_getEJBObject);
            policy.allow(OperationsPolicy.Context_getPrimaryKey);
            policy.allow(OperationsPolicy.JNDI_access_to_java_comp_env);
            policy.allow(OperationsPolicy.Resource_manager_access);
            policy.allow(OperationsPolicy.Enterprise_bean_access);

            Object expected = policy;
            Object actual = ejbObject.getAllowedOperationsReport("ejbSelectInEntity");

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
     * ejbActivate           |  EntityContext methods:
     * ejbPassivate          |     - getEJBHome
     * |     - getEJBObject
     * |     - getPrimaryKey
     * |  JNDI access to java:comp/env
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void test10_ejbActivate() {
        try {

            OperationsPolicy policy = new OperationsPolicy();
            policy.allow(OperationsPolicy.Context_getEJBHome);
            policy.allow(OperationsPolicy.Context_getEJBObject);
            policy.allow(OperationsPolicy.Context_getPrimaryKey);
            policy.allow(OperationsPolicy.JNDI_access_to_java_comp_env);

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
     * ejbActivate           |  EntityContext methods:
     * ejbPassivate          |     - getEJBHome
     * |     - getEJBObject
     * |     - getPrimaryKey
     * |  JNDI access to java:comp/env
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void test11_ejbPassivate() {
        try {

            OperationsPolicy policy = new OperationsPolicy();
            policy.allow(OperationsPolicy.Context_getEJBHome);
            policy.allow(OperationsPolicy.Context_getEJBObject);
            policy.allow(OperationsPolicy.Context_getPrimaryKey);
            policy.allow(OperationsPolicy.JNDI_access_to_java_comp_env);

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
     * ejbLoad               |  EntityContext methods:
     * ejbStore              |     - getEJBHome
     * |     - getCallerPrincipal
     * |     - getRollbackOnly
     * |     - isCallerInRole
     * |     - setRollbackOnly
     * |     - getEJBObject
     * |     - getPrimaryKey
     * |  JNDI access to java:comp/env
     * |  Resource manager access
     * |  Enterprise bean access
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void test12_ejbLoad() {
        try {

            OperationsPolicy policy = new OperationsPolicy();
            policy.allow(OperationsPolicy.Context_getEJBHome);
            policy.allow(OperationsPolicy.Context_getCallerPrincipal);
            policy.allow(OperationsPolicy.Context_getRollbackOnly);
            policy.allow(OperationsPolicy.Context_isCallerInRole);
//        policy.allow( OperationsPolicy.Context_setRollbackOnly );
            policy.allow(OperationsPolicy.Context_getEJBObject);
            policy.allow(OperationsPolicy.Context_getPrimaryKey);
            policy.allow(OperationsPolicy.JNDI_access_to_java_comp_env);
            policy.allow(OperationsPolicy.Resource_manager_access);
            policy.allow(OperationsPolicy.Enterprise_bean_access);

            Object expected = policy;
            Object actual = ejbObject.getAllowedOperationsReport("ejbLoad");

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
     * ejbLoad               |  EntityContext methods:
     * ejbStore              |     - getEJBHome
     * |     - getCallerPrincipal
     * |     - getRollbackOnly
     * |     - isCallerInRole
     * |     - setRollbackOnly
     * |     - getEJBObject
     * |     - getPrimaryKey
     * |  JNDI access to java:comp/env
     * |  Resource manager access
     * |  Enterprise bean access
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void test13_ejbStore() {
        try {

            OperationsPolicy policy = new OperationsPolicy();
            policy.allow(OperationsPolicy.Context_getEJBHome);
            policy.allow(OperationsPolicy.Context_getCallerPrincipal);
            policy.allow(OperationsPolicy.Context_getRollbackOnly);
            policy.allow(OperationsPolicy.Context_isCallerInRole);
//        policy.allow( OperationsPolicy.Context_setRollbackOnly );
            policy.allow(OperationsPolicy.Context_getEJBObject);
            policy.allow(OperationsPolicy.Context_getPrimaryKey);
            policy.allow(OperationsPolicy.JNDI_access_to_java_comp_env);
            policy.allow(OperationsPolicy.Resource_manager_access);
            policy.allow(OperationsPolicy.Enterprise_bean_access);

            Object expected = policy;
            Object actual = ejbObject.getAllowedOperationsReport("ejbStore");

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
     * business method       |  EntityContext methods:
     * from remote interface |     - getEJBHome
     * |     - getCallerPrincipal
     * |     - getRollbackOnly
     * |     - isCallerInRole
     * |     - setRollbackOnly
     * |     - getEJBObject
     * |     - getPrimaryKey
     * |  JNDI access to java:comp/env
     * |  Resource manager access
     * |  Enterprise bean access
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void test14_businessMethod() {
        try {

            OperationsPolicy policy = new OperationsPolicy();
            policy.allow(OperationsPolicy.Context_getEJBHome);
            policy.allow(OperationsPolicy.Context_getCallerPrincipal);
            policy.allow(OperationsPolicy.Context_getRollbackOnly);
            policy.allow(OperationsPolicy.Context_isCallerInRole);
//        policy.allow( OperationsPolicy.Context_setRollbackOnly );
            policy.allow(OperationsPolicy.Context_getEJBObject);
            policy.allow(OperationsPolicy.Context_getPrimaryKey);
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
    //
    // Test EJBContext allowed operations       
    //=====================================
}


