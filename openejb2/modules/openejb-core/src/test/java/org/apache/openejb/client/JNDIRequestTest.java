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
package org.apache.openejb.client;

import java.io.*;

import junit.framework.TestCase;

public class JNDIRequestTest extends TestCase {
    JNDIRequest jndiRequest;

    public void testExternalize() throws Exception {
        JNDIRequest expected = new JNDIRequest(RequestMethods.JNDI_LOOKUP,"this/is/a/jndi/name");
        JNDIRequest actual = new JNDIRequest();

        externalize(expected, actual);

        assertEquals("Request method not the same",expected.getRequestMethod(),actual.getRequestMethod());
        assertEquals("ClientModuleID not the same",expected.getClientModuleID(),actual.getClientModuleID());
        assertEquals("JNDI Name not the same",expected.getRequestString(),actual.getRequestString());
    }


    public void testExternalize2() throws Exception {
        JNDIRequest expected = new JNDIRequest(RequestMethods.JNDI_LOOKUP,"foobar","this/is/a/jndi/name");
        JNDIRequest actual = new JNDIRequest();

        externalize(expected, actual);

        assertEquals("Request method not the same",expected.getRequestMethod(),actual.getRequestMethod());
        assertEquals("ClientModuleID not the same",expected.getClientModuleID(),actual.getClientModuleID());
        assertEquals("JNDI Name not the same",expected.getRequestString(),actual.getRequestString());
    }


    private void externalize(Externalizable original, Externalizable copy) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);

        original.writeExternal(out);
        out.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream in = new ObjectInputStream(bais);

        copy.readExternal(in);
    }
}