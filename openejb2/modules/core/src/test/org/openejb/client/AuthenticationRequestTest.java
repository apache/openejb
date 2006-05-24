/**
 *
 * Copyright 2006 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.openejb.client;

import junit.framework.TestCase;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.Binding;
import java.util.Map;
import java.util.HashMap;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.Externalizable;
import java.io.IOException;

public class AuthenticationRequestTest extends AbstractProtocolTest {

    public void testExternalize1() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest(null, null);
        AuthenticationRequest copy = new AuthenticationRequest();

        externalize(request, copy);

        assertEquals("reqType", request.getRequestType(), copy.getRequestType());
        assertEquals("credentials", request.getCredentials(), copy.getCredentials());
        assertEquals("principle", request.getPrinciple(), copy.getPrinciple());
    }

    public void testExternalize2() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest("myuser", "mypass");
        AuthenticationRequest copy = new AuthenticationRequest();

        externalize(request, copy);

        assertEquals("reqType", request.getRequestType(), copy.getRequestType());
        assertEquals("credentials", request.getCredentials(), copy.getCredentials());
        assertEquals("principle", request.getPrinciple(), copy.getPrinciple());
    }
}
