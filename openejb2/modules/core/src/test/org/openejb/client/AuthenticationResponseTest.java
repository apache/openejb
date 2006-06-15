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

public class AuthenticationResponseTest extends AbstractProtocolTest {

    public void testExternalize1() throws Exception {
        AuthenticationResponse response = new AuthenticationResponse(ResponseCodes.AUTH_DENIED);
        AuthenticationResponse copy = new AuthenticationResponse();

        externalize(response, copy);

        assertEquals("resType", response.getResponseCode(), copy.getResponseCode());
    }

    public void testExternalize2() throws Exception {
        AuthenticationResponse response = new AuthenticationResponse(ResponseCodes.AUTH_REDIRECT);
        response.setServer(new ServerMetaData("localhost", 123));
        response.setIdentity(new ClientMetaData("FOO.SECURITY.TOKEN"));

        AuthenticationResponse copy = new AuthenticationResponse();

        externalize(response, copy);

        assertEquals("resType", response.getResponseCode(), copy.getResponseCode());
        assertNotNull("server", copy.getServer());
        assertEquals("server.address", response.getServer().getAddress(), copy.getServer().getAddress());
        assertEquals("server.port", response.getServer().getPort(), copy.getServer().getPort());
    }
}
