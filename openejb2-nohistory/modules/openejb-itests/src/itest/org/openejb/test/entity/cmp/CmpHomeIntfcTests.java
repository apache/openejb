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
 * [2] Should be run as the second test suite of the BasicCmpTestClients
 */
public class CmpHomeIntfcTests extends BasicCmpTestClient {

    public CmpHomeIntfcTests() {
        super("HomeIntfc.");
    }

    protected void setUp() throws Exception {
        super.setUp();
        Object obj = initialContext.lookup("client/tests/entity/cmp/BasicCmpHome");
        ejbHome = (BasicCmpHome) javax.rmi.PortableRemoteObject.narrow(obj, BasicCmpHome.class);
    }

    //===============================
    // Test home interface methods
    //
    public void test01_create() throws Exception {
        ejbObject = ejbHome.create("First Bean");
        assertNotNull("The EJBObject is null", ejbObject);
    }

    public void test02_findByPrimaryKey() throws Exception {
        ejbPrimaryKey = ejbObject.getPrimaryKey();
        ejbObject = ejbHome.findByPrimaryKey((Integer) ejbPrimaryKey);
        assertNotNull("The EJBObject is null", ejbObject);
    }

    public void test03_findByLastName() throws Exception {
        Integer[] keys = new Integer[3];
        ejbObject = ejbHome.create("David Blevins");
        keys[0] = (Integer) ejbObject.getPrimaryKey();

        ejbObject = ejbHome.create("Dennis Blevins");
        keys[1] = (Integer) ejbObject.getPrimaryKey();

        ejbObject = ejbHome.create("Claude Blevins");
        keys[2] = (Integer) ejbObject.getPrimaryKey();

        java.util.Collection objects = ejbHome.findByLastName("Blevins");
        assertNotNull("The Collection is null", objects);
        assertEquals("The Collection is not the right size.", keys.length, objects.size());
        Object[] objs = objects.toArray();
        for (int i = 0; i < objs.length; i++) {
            ejbObject = (BasicCmpObject) javax.rmi.PortableRemoteObject.narrow(objs[i], BasicCmpObject.class);
            // This could be problematic, it assumes the order of the collection.
            assertEquals("The primary keys are not equal.", keys[i], ejbObject.getPrimaryKey());
        }
    }
    //
    // Test home interface methods
    //===============================

}
