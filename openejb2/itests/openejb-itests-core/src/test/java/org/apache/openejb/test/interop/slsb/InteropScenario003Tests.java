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
package org.apache.openejb.test.interop.slsb;

import java.rmi.AccessException;
import javax.rmi.PortableRemoteObject;


/**
 * @version $Revision$ $Date$
 */
public class InteropScenario003Tests extends InteropTestClient {

    public InteropScenario003Tests() {
        super("InteropScenario003.");
    }

    public void testInterop() throws Exception {
        Object obj = initialContext.lookup("interop/003/InteropHome");

        interopHome = (InteropHome) PortableRemoteObject.narrow(obj, InteropHome.class);
        interop = interopHome.create();

        try {
            interop.callNoAccess("FOO");
            fail("Should have thrown an AccessException");
        } catch (AccessException e) {
        }

        try {
            interop.callHighAccess("FOO");
            fail("Should have thrown an AccessException");
        } catch (AccessException e) {
        }

        assertEquals("FOO", interop.callLowAccess("FOO"));

        try {
            interop.callMedAccess("FOO");
            fail("Should have thrown an AccessException");
        } catch (AccessException e) {
        }

        assertEquals("FOO", interop.callAllAccess("FOO"));

        interop.callAllAccessTx("BAR");

        interop.remove();
    }

}
