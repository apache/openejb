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

import org.openejb.test.entity.bmp.EncBmpHome;
import org.openejb.test.entity.bmp.EncBmpObject;
import org.openejb.test.entity.cmp.EncCmpHome;
import org.openejb.test.entity.cmp.EncCmpObject;
import org.openejb.test.stateful.EncStatefulHome;
import org.openejb.test.stateful.EncStatefulObject;

/**
 *
 */
public class MiscEjbTests extends BasicStatelessTestClient {

    public MiscEjbTests() {
        super("EJBObject.");
    }

    protected void setUp() throws Exception {
        super.setUp();
        Object obj = initialContext.lookup("client/tests/stateless/BasicStatelessHome");
        ejbHome = (BasicStatelessHome) javax.rmi.PortableRemoteObject.narrow(obj, BasicStatelessHome.class);
        ejbObject = ejbHome.create();
    }

    protected void tearDown() throws Exception {
        try {
            //ejbObject.remove();
        } catch (Exception e) {
            throw e;
        } finally {
            super.tearDown();
        }
    }

    //===============================
    // Test ejb object methods
    //
    public void test01_isIdentical_stateless() {
        try {
            String jndiName = "client/tests/stateless/EncBean";
            EncStatelessHome ejbHome2 = null;
            EncStatelessObject ejbObject2 = null;

            Object obj = initialContext.lookup(jndiName);
            ejbHome2 = (EncStatelessHome) javax.rmi.PortableRemoteObject.narrow(obj, EncStatelessHome.class);
            ejbObject2 = ejbHome2.create();

            //System.out.println("_______________________________________________________");
            //System.out.println(" ejb1 "+ejbObject);
            //System.out.println(" ejb2 "+ejbObject2);
            assertTrue("The EJBObjects should not be identical", !ejbObject.isIdentical(ejbObject2));
            //System.out.println("-------------------------------------------------------");
        } catch (Exception e) {
            //System.out.println("-------------------------------------------------------");
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test02_isIdentical_stateful() {
        try {
            String jndiName = "client/tests/stateful/EncBean";
            EncStatefulHome ejbHome2 = null;
            EncStatefulObject ejbObject2 = null;

            Object obj = initialContext.lookup(jndiName);
            ejbHome2 = (EncStatefulHome) javax.rmi.PortableRemoteObject.narrow(obj, EncStatefulHome.class);
            ejbObject2 = ejbHome2.create("isIdentical test");

            //System.out.println("_______________________________________________________");
            //System.out.println(" ejb1 "+ejbObject);
            //System.out.println(" ejb2 "+ejbObject2);
            assertTrue("The EJBObjects should not be identical", !ejbObject.isIdentical(ejbObject2));
            //System.out.println("-------------------------------------------------------");
        } catch (Exception e) {
            //System.out.println("-------------------------------------------------------");
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test03_isIdentical_bmp() {
        try {
            String jndiName = "client/tests/entity/bmp/EncBean";
            EncBmpHome ejbHome2 = null;
            EncBmpObject ejbObject2 = null;

            Object obj = initialContext.lookup(jndiName);
            ejbHome2 = (EncBmpHome) javax.rmi.PortableRemoteObject.narrow(obj, EncBmpHome.class);
            ejbObject2 = ejbHome2.create("isIdentical test");

            //System.out.println("_______________________________________________________");
            assertTrue("The EJBObjects should not be identical", !ejbObject.isIdentical(ejbObject2));
            //System.out.println(" ejb1 "+ejbObject);
            //System.out.println(" ejb2 "+ejbObject2);
            //System.out.println("-------------------------------------------------------");
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    /**
     * DMB: Calling this now causes an error as the "entity" table doesn't exist yet
     */
    public void _test04_isIdentical_cmp() {
        try {
            String jndiName = "client/tests/entity/cmp/EncBean";
            EncCmpHome ejbHome2 = null;
            EncCmpObject ejbObject2 = null;

            Object obj = initialContext.lookup(jndiName);
            ejbHome2 = (EncCmpHome) javax.rmi.PortableRemoteObject.narrow(obj, EncCmpHome.class);
            ejbObject2 = ejbHome2.create("isIdentical test");

            //System.out.println("_______________________________________________________");
            //System.out.println(" ejb1 "+ejbObject);
            //System.out.println(" ejb2 "+ejbObject2);
            assertTrue("The EJBObjects should not be identical", !ejbObject.isIdentical(ejbObject2));
            //System.out.println("-------------------------------------------------------");
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    //
    // Test ejb object methods
    //===============================
}
