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
package org.openejb.test.entity.cmp2;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.openejb.test.NamedTestCase;
import org.openejb.test.TestManager;
import org.openejb.test.entity.cmp2.prefetch.PrefetchFacadeHome;
import org.openejb.test.entity.cmp2.prefetch.PrefetchFacadeObject;


/**
 * @version $Revision$ $Date$
 */
public class PrefetchTests extends NamedTestCase {
    private InitialContext initialContext;
    private PrefetchFacadeHome ejbHome;

    public PrefetchTests() {
        super("PrefetchTests.");
    }

    public void testDoesNotOverwriteUpdates() {
        try {
            ejbHome = (PrefetchFacadeHome) javax.rmi.PortableRemoteObject.narrow(initialContext.lookup("cmp2/Prefetch/PrefetchFacade"), PrefetchFacadeHome.class);
            PrefetchFacadeObject prefetchFacade = ejbHome.create();
            prefetchFacade.testDoesNotOverwriteUpdates();
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void testFinderPrefetch() {
        try {
            ejbHome = (PrefetchFacadeHome) javax.rmi.PortableRemoteObject.narrow(initialContext.lookup("cmp2/Prefetch/PrefetchFacade"), PrefetchFacadeHome.class);
            PrefetchFacadeObject prefetchFacade = ejbHome.create();
            prefetchFacade.testFinderPrefetch();
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void testEJBPrefetch() {
        try {
            ejbHome = (PrefetchFacadeHome) javax.rmi.PortableRemoteObject.narrow(initialContext.lookup("cmp2/Prefetch/PrefetchFacade"), PrefetchFacadeHome.class);
            PrefetchFacadeObject prefetchFacade = ejbHome.create();
            prefetchFacade.testEJBPrefetch();
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void testCMPPrefetch() {
        try {
            ejbHome = (PrefetchFacadeHome) javax.rmi.PortableRemoteObject.narrow(initialContext.lookup("cmp2/Prefetch/PrefetchFacade"), PrefetchFacadeHome.class);
            PrefetchFacadeObject prefetchFacade = ejbHome.create();
            prefetchFacade.testCMPPrefetch();
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void testCMRPrefetch() {
        try {
            ejbHome = (PrefetchFacadeHome) javax.rmi.PortableRemoteObject.narrow(initialContext.lookup("cmp2/Prefetch/PrefetchFacade"), PrefetchFacadeHome.class);
            PrefetchFacadeObject prefetchFacade = ejbHome.create();
            prefetchFacade.testCMRPrefetch();
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    protected void setUp() throws Exception {
        Properties properties = TestManager.getServer().getContextEnvironment();
        properties.put(Context.SECURITY_PRINCIPAL, "ENTITY_TEST_CLIENT");
        properties.put(Context.SECURITY_CREDENTIALS, "ENTITY_TEST_CLIENT");

        initialContext = new InitialContext(properties);
    }

    protected void tearDown() throws Exception {
    }
}
