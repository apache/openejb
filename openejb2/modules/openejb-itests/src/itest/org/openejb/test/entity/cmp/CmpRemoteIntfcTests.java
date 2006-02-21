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
package org.openejb.test.entity.cmp;


/**
 * [5] Should be run as the fifth test suite of the BasicCmpTestClients
 */
public class CmpRemoteIntfcTests extends BasicCmpTestClient {

    public CmpRemoteIntfcTests() {
        super("RemoteIntfc.");
    }

    protected void setUp() throws Exception {
        super.setUp();
        Object obj = initialContext.lookup("client/tests/entity/cmp/BasicCmpHome");
        ejbHome = (BasicCmpHome) javax.rmi.PortableRemoteObject.narrow(obj, BasicCmpHome.class);
        ejbObject = ejbHome.create("Forth Bean");
    }

    //=================================
    // Test remote interface methods
    //
    public void test01_businessMethod() {
        try {
            String expected = "Success";
            String actual = ejbObject.businessMethod("sseccuS");
            assertEquals(expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    /**
     * Throw an application exception and make sure the exception
     * reaches the bean nicely.
     */
    public void test02_throwApplicationException() {
        try {
            ejbObject.throwApplicationException();
        } catch (org.openejb.test.ApplicationException e) {
            //Good.  This is the correct behaviour
            return;
        } catch (Throwable e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
        fail("An ApplicationException should have been thrown.");
    }

    /**
     * After an application exception we should still be able to
     * use our bean
     */
    public void test03_invokeAfterApplicationException() {
        try {
            String expected = "Success";
            String actual = ejbObject.businessMethod("sseccuS");
            assertEquals(expected, actual);
        } catch (Throwable e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test04_throwSystemException() {
        try {
            ejbObject.throwSystemException_NullPointer();
        } catch (java.rmi.RemoteException e) {
            //Good, so far.
            Throwable n = e.detail;
            assertNotNull("Nested exception should not be is null", n);
            assertTrue("Nested exception should be an instance of NullPointerException, but exception is " + n.getClass().getName(), (n instanceof NullPointerException));
            return;
        } catch (Throwable e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
        fail("A NullPointerException should have been thrown.");
    }

    /**
     * After a system exception the intance should be garbage collected
     * and the remote reference should be invalidated.
     * <p/>
     * The Remote Server fails this one, that should be fixed.
     */
    public void BUG_test05_invokeAfterSystemException() {
        try {
            ejbObject.businessMethod("This refernce is invalid");
            fail("A java.rmi.NoSuchObjectException should have been thrown.");
        } catch (java.rmi.NoSuchObjectException e) {
            // Good.
        } catch (Throwable e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }
    //
    // Test remote interface methods
    //=================================

    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
