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
package org.openejb.test.stateless;

import org.openejb.test.object.OperationsPolicy;

/**
 * [10] Should be run as the nineth test suite of the BasicStatelessTestClients
 * <p/>
 * <PRE>
 * =========================================================================
 * Operations allowed in the methods of a stateless SessionBean with
 * bean-managed transaction demarcation
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
 * |     - getEJBObject
 * |     - getUserTransaction
 * |  JNDI access to java:comp/env
 * ______________________|__________________________________________________
 * |
 * business method       |  SessionContext methods:
 * from remote interface |     - getEJBHome
 * |     - getCallerPrincipal
 * |     - isCallerInRole
 * |     - getEJBObject
 * |     - getUserTransaction
 * |  JNDI access to java:comp/env
 * |  Resource manager access
 * |  Enterprise bean access
 * ______________________|__________________________________________________
 * </PRE>
 */
public class BMTStatelessAllowedOperationsTests extends BasicStatelessTestClient {

    public BMTStatelessAllowedOperationsTests() {
        super("BMTAllowedOperations.");
    }

    protected void setUp() throws Exception {
        super.setUp();
        Object obj = initialContext.lookup("client/tests/stateless/BeanManagedBasicStatelessHome");
        ejbHome = (BasicStatelessHome) javax.rmi.PortableRemoteObject.narrow(obj, BasicStatelessHome.class);
        ejbObject = ejbHome.create();
        ejbHandle = ejbObject.getHandle();
        ejbObject.businessMethod("let's go!");
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
        try {
            ejbObject.remove();
        } catch (Exception e) {
            throw e;
        } finally {
            super.tearDown();
        }
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
            policy.allow(policy.Context_getEJBHome);
            policy.allow(policy.JNDI_access_to_java_comp_env);

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
     * |     - getEJBObject
     * |     - getUserTransaction
     * |  JNDI access to java:comp/env
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void test02_ejbCreate() {
        try {
            OperationsPolicy policy = new OperationsPolicy();
            policy.allow(policy.Context_getEJBHome);
            policy.allow(policy.Context_getEJBObject);
            policy.allow(policy.Context_getUserTransaction);
            policy.allow(policy.JNDI_access_to_java_comp_env);

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
     * |     - getEJBObject
     * |     - getUserTransaction
     * |  JNDI access to java:comp/env
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void TODO_test03_ejbRemove() {
        try {
            /* TO DO:  This test needs unique functionality to work */
            OperationsPolicy policy = new OperationsPolicy();
            policy.allow(policy.Context_getEJBHome);
            policy.allow(policy.Context_getEJBObject);
            policy.allow(policy.Context_getUserTransaction);
            policy.allow(policy.JNDI_access_to_java_comp_env);

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
     * business method       |  SessionContext methods:
     * from remote interface |     - getEJBHome
     * |     - getCallerPrincipal
     * |     - isCallerInRole
     * |     - getEJBObject
     * |     - getUserTransaction
     * |  JNDI access to java:comp/env
     * |  Resource manager access
     * |  Enterprise bean access
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void test04_businessMethod() {
        try {
            OperationsPolicy policy = new OperationsPolicy();
            policy.allow(policy.Context_getEJBHome);
            policy.allow(policy.Context_getCallerPrincipal);
            policy.allow(policy.Context_isCallerInRole);
            policy.allow(policy.Context_getEJBObject);
            policy.allow(policy.Context_getUserTransaction);
            policy.allow(policy.JNDI_access_to_java_comp_env);
            policy.allow(policy.Resource_manager_access);
            policy.allow(policy.Enterprise_bean_access);

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

    public void xtest_xx_userTransactionWorksAfterCallToBMT() throws Exception {
        ejbObject.accessBMTBean();
    }
}


