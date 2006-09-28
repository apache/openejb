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

import javax.ejb.EJBHome;

/**
 * [8] Should be run as the eigth test suite of the BasicStatelessTestClients
 */
public class StatelessEjbMetaDataTests extends BasicStatelessTestClient {

    public StatelessEjbMetaDataTests() {
        super("EJBMetaData.");
    }

    protected void setUp() throws Exception {
        super.setUp();
        Object obj = initialContext.lookup("client/tests/stateless/BasicStatelessHome");
        ejbHome = (BasicStatelessHome) javax.rmi.PortableRemoteObject.narrow(obj, BasicStatelessHome.class);
        ejbMetaData = ejbHome.getEJBMetaData();
    }

    //=================================
    // Test meta data methods
    //
    public void test01_getEJBHome() {
        try {

            EJBHome home = ejbMetaData.getEJBHome();
            assertNotNull("The EJBHome is null", home);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test02_getHomeInterfaceClass() {
        try {
            Class clazz = ejbMetaData.getHomeInterfaceClass();
            assertNotNull("The Home Interface class is null", clazz);
            assertEquals(clazz, BasicStatelessHome.class);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    /**
     * 5.5 Session object identity
     * <p/>
     * Session objects are intended to be private resources used only by the
     * client that created them. For this reason, session objects, from the
     * client’s perspective, appear anonymous. In contrast to entity objects,
     * which expose their identity as a primary key, session objects hide their
     * identity. As a result, the EJBObject.getPrimaryKey() and
     * EJBHome.remove(Object id) methods result in a java.rmi.RemoteException
     * if called on a session bean. If the EJBMetaData.getPrimaryKeyClass()
     * method is invoked on a EJBMetaData object for a Session bean, the method throws
     * the java.lang.RuntimeException.
     */
    public void test03_getPrimaryKeyClass() {
        try {
            Class clazz = ejbMetaData.getPrimaryKeyClass();
            assertNull("Should not return a primary key.  Method should throw an java.lang.RuntimeException", clazz);
        } catch (RuntimeException e) {
            assertTrue(true);
            return;
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
        assertTrue("Method should throw an java.lang.RuntimeException", false);
    }

    public void test04_getRemoteInterfaceClass() {
        try {
            Class clazz = ejbMetaData.getRemoteInterfaceClass();
            assertNotNull("The Remote Interface class is null", clazz);
            assertEquals(clazz, BasicStatelessObject.class);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test05_isSession() {
        try {
            assertTrue("EJBMetaData says this is not a session bean", ejbMetaData.isSession());
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test06_isStatelessSession() {
        try {
            assertTrue("EJBMetaData says this is not a stateless session bean", ejbMetaData.isStatelessSession());
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }
    //
    // Test meta data methods
    //=================================
}
