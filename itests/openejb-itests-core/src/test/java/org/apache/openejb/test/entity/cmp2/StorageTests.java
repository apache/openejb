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
package org.apache.openejb.test.entity.cmp2;

import java.util.Properties;
import java.util.Arrays;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.openejb.test.NamedTestCase;
import org.apache.openejb.test.TestManager;
import org.apache.openejb.test.entity.cmp2.model.StorageHome;
import org.apache.openejb.test.entity.cmp2.model.StorageRemote;


/**
 * @version $Revision$ $Date$
 */
public class StorageTests extends NamedTestCase {
    private InitialContext initialContext;
    private StorageHome ejbHome;
    private StorageRemote storage;
    private byte[] testdata;

    public StorageTests() {
        super("StorageTests.");
    }

    public void testStorageBlob() throws Exception {

        storage.setBlob(testdata);
        byte[] readBlob = storage.getBlob();
        assertTrue(Arrays.equals(testdata, readBlob));
    }

    public void testReadBlob() throws Exception {
        storage.setBlob(testdata);
        byte[] readbytes = storage.getBytes();
        assertTrue(Arrays.equals(testdata, readbytes));
    }

    public void testWriteBlob() throws Exception {
        storage.setBytes(testdata);
        byte[] readblob = storage.getBlob();
        assertTrue(Arrays.equals(testdata, readblob));
    }

    public void testChar() throws Exception {
        char expectedChar = 'c';
        storage.setChar(expectedChar);
        char readChar = storage.getChar();
        assertEquals(expectedChar, readChar);
    }
    
    protected void setUp() throws Exception {
        Properties properties = TestManager.getServer().getContextEnvironment();
        properties.put(Context.SECURITY_PRINCIPAL, "ENTITY_TEST_CLIENT");
        properties.put(Context.SECURITY_CREDENTIALS, "ENTITY_TEST_CLIENT");

        initialContext = new InitialContext(properties);

        ejbHome = (StorageHome) javax.rmi.PortableRemoteObject.narrow(initialContext.lookup("cmp2/Storage"), StorageHome.class);
        storage = ejbHome.create(new Integer(1));

        testdata = "this is a test".getBytes();
    }

    protected void tearDown() throws Exception {
        storage.remove();
    }
}