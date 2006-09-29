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
