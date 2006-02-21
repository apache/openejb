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

import javax.ejb.EJBMetaData;

/**
 * [3] Should be run as the third test suite of the BasicStatelessTestClients
 */
public class StatelessEjbHomeTests extends BasicStatelessTestClient {

    public StatelessEjbHomeTests() {
        super("EJBHome.");
    }

    protected void setUp() throws Exception {
        super.setUp();
        Object obj = initialContext.lookup("client/tests/stateless/BasicStatelessHome");
        ejbHome = (BasicStatelessHome) javax.rmi.PortableRemoteObject.narrow(obj, BasicStatelessHome.class);
    }

    //===============================
    // Test ejb home methods
    //
    public void test01_getEJBMetaData() {
        try {
            EJBMetaData ejbMetaData = ejbHome.getEJBMetaData();
            assertNotNull("EJBMetaData is null", ejbMetaData);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test02_getHomeHandle() {
        try {
            ejbHomeHandle = ejbHome.getHomeHandle();
            assertNotNull("The HomeHandle is null", ejbHomeHandle);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    /**
     * ------------------------------------
     * 5.3.2 Removing a session object
     * A client may remove a session object using the remove() method on the javax.ejb.EJBObject
     * interface, or the remove(Handle handle) method of the javax.ejb.EJBHome interface.
     * <p/>
     * Because session objects do not have primary keys that are accessible to clients, invoking the
     * javax.ejb.EJBHome.remove(Object id) method on a session results in the
     * javax.ejb.RemoveException.
     * <p/>
     * ------------------------------------
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
     * ------------------------------------
     * <p/>
     * Sections 5.3.2 and 5.5 conflict.  5.3.2 says to throw javax.ejb.RemoveException, 5.5 says to
     * throw java.rmi.RemoteException.
     * <p/>
     * For now, we are going with java.rmi.RemoteException.
     */
    public void test03_removeByPrimaryKey() {
        try {
            ejbHome.remove("id");
        } catch (javax.ejb.RemoveException e) {
            assertTrue(true);
            return;
        } catch (Exception e) {
            fail("Received " + e.getClass() + " instead of javax.ejb.RemoveException");
        }
        assertTrue("java.rmi.RemoteException should have been thrown", false);
    }
    //
    // Test ejb home methods
    //===============================
}
