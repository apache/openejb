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
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact openejb-group@openejb.sf.net.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.sf.net/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.test.stateless;

import java.util.Properties;
import javax.ejb.EJBMetaData;
import javax.ejb.Handle;
import javax.ejb.HomeHandle;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.openejb.test.TestManager;

/**
 * [1] Should be run as the first test suite of the StatelessTestClients
 */
public class StatelessContainerTxTests extends org.openejb.test.NamedTestCase {

    public final static String jndiEJBHomeEntry = "client/tests/stateless/ContainerManagedTransactionTests/EJBHome";

    protected ContainerTxStatelessHome ejbHome;
    protected ContainerTxStatelessObject ejbObject;

    protected EJBMetaData ejbMetaData;
    protected HomeHandle ejbHomeHandle;
    protected Handle ejbHandle;
    protected Integer ejbPrimaryKey;

    protected InitialContext initialContext;

    public StatelessContainerTxTests() {
        super("Stateless.ContainerManagedTransaction.");
    }

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    protected void setUp() throws Exception {

        Properties properties = TestManager.getServer().getContextEnvironment();
        properties.put(Context.SECURITY_PRINCIPAL, "STATELESS_test00_CLIENT");
        properties.put(Context.SECURITY_CREDENTIALS, "STATELESS_test00_CLIENT");

        initialContext = new InitialContext(properties);

        /*[1] Get bean */
        Object obj = initialContext.lookup(jndiEJBHomeEntry);
        ejbHome = (ContainerTxStatelessHome) javax.rmi.PortableRemoteObject.narrow(obj, ContainerTxStatelessHome.class);
        ejbObject = ejbHome.create();

        /*[2] Create database table */
        TestManager.getDatabase().createAccountTable();
    }

    /**
     * Tears down the fixture, for example, close a network connection.
     * This method is called after a test is executed.
     */
    protected void tearDown() throws Exception {
        /*[1] Drop database table */
        TestManager.getDatabase().dropAccountTable();
    }

    public void test01_txMandatory_withoutTx() {
        try {
            String expected = "ping";
            String actual = ejbObject.txMandatoryMethod(expected);
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test02_txNever_withoutTx() {
        try {
            String expected = "ping";
            String actual = ejbObject.txNeverMethod(expected);
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test03_txNotSupported_withoutTx() {
        try {
            String expected = "ping";
            String actual = ejbObject.txNotSupportedMethod(expected);
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test04_txRequired_withoutTx() {
        try {
            String expected = "ping";
            String actual = ejbObject.txRequiredMethod(expected);
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test05_txRequiresNew_withoutTx() {
        try {
            String expected = "ping";
            String actual = ejbObject.txRequiresNewMethod(expected);
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test06_txSupports_withoutTx() {
        try {
            String expected = "ping";
            String actual = ejbObject.txSupportsMethod(expected);
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test07_txMandatory_withTx() {
        try {
            String expected = "ping";
            String actual = ejbObject.txMandatoryMethod(expected);
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test08_txNever_withTx() {
        try {
            String expected = "ping";
            String actual = ejbObject.txNeverMethod(expected);
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test09_txNotSupported_withTx() {
        try {
            String expected = "ping";
            String actual = ejbObject.txNotSupportedMethod(expected);
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test10_txRequired_withTx() {
        try {
            String expected = "ping";
            String actual = ejbObject.txRequiredMethod(expected);
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test11_txRequiresNew_withTx() {
        try {
            String expected = "ping";
            String actual = ejbObject.txRequiresNewMethod(expected);
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test12_txSupports_withTx() {
        try {
            String expected = "ping";
            String actual = ejbObject.txSupportsMethod(expected);
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test01_txMandatory_withoutTx_appException() {
        try {
            String expected = "ping";
            String actual = ejbObject.txMandatoryMethod(expected);
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test02_txNever_withoutTx_appException() {
        try {
            String expected = "ping";
            String actual = ejbObject.txNeverMethod(expected);
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test03_txNotSupported_withoutTx_appException() {
        try {
            String expected = "ping";
            String actual = ejbObject.txNotSupportedMethod(expected);
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test04_txRequired_withoutTx_appException() {
        try {
            String expected = "ping";
            String actual = ejbObject.txRequiredMethod(expected);
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test05_txRequiresNew_withoutTx_appException() {
        try {
            String expected = "ping";
            String actual = ejbObject.txRequiresNewMethod(expected);
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test06_txSupports_withoutTx_appException() {
        try {
            String expected = "ping";
            String actual = ejbObject.txSupportsMethod(expected);
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test07_txMandatory_withTx_appException() {
        try {
            String expected = "ping";
            String actual = ejbObject.txMandatoryMethod(expected);
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test08_txNever_withTx_appException() {
        try {
            String expected = "ping";
            String actual = ejbObject.txNeverMethod(expected);
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test09_txNotSupported_withTx_appException() {
        try {
            String expected = "ping";
            String actual = ejbObject.txNotSupportedMethod(expected);
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test10_txRequired_withTx_appException() {
        try {
            String expected = "ping";
            String actual = ejbObject.txRequiredMethod(expected);
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test11_txRequiresNew_withTx_appException() {
        try {
            String expected = "ping";
            String actual = ejbObject.txRequiresNewMethod(expected);
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test12_txSupports_withTx_appException() {
        try {
            String expected = "ping";
            String actual = ejbObject.txSupportsMethod(expected);
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test01_txMandatory_withoutTx_sysException() {
        try {
            String expected = "ping";
            String actual = ejbObject.txMandatoryMethod(expected);
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test02_txNever_withoutTx_sysException() {
        try {
            String expected = "ping";
            String actual = ejbObject.txNeverMethod(expected);
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test03_txNotSupported_withoutTx_sysException() {
        try {
            String expected = "ping";
            String actual = ejbObject.txNotSupportedMethod(expected);
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test04_txRequired_withoutTx_sysException() {
        try {
            String expected = "ping";
            String actual = ejbObject.txRequiredMethod(expected);
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test05_txRequiresNew_withoutTx_sysException() {
        try {
            String expected = "ping";
            String actual = ejbObject.txRequiresNewMethod(expected);
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test06_txSupports_withoutTx_sysException() {
        try {
            String expected = "ping";
            String actual = ejbObject.txSupportsMethod(expected);
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test07_txMandatory_withTx_sysException() {
        try {
            String expected = "ping";
            String actual = ejbObject.txMandatoryMethod(expected);
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test08_txNever_withTx_sysException() {
        try {
            String expected = "ping";
            String actual = ejbObject.txNeverMethod(expected);
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test09_txNotSupported_withTx_sysException() {
        try {
            String expected = "ping";
            String actual = ejbObject.txNotSupportedMethod(expected);
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test10_txRequired_withTx_sysException() {
        try {
            String expected = "ping";
            String actual = ejbObject.txRequiredMethod(expected);
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test11_txRequiresNew_withTx_sysException() {
        try {
            String expected = "ping";
            String actual = ejbObject.txRequiresNewMethod(expected);
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test12_txSupports_withTx_sysException() {
        try {
            String expected = "ping";
            String actual = ejbObject.txSupportsMethod(expected);
            assertEquals("The method invocation was invalid.", expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }
}

