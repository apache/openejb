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
package org.apache.openejb.transaction;

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import junit.framework.TestCase;
import org.apache.geronimo.interceptor.Interceptor;
import org.apache.geronimo.interceptor.Invocation;
import org.apache.geronimo.interceptor.InvocationResult;
import org.apache.geronimo.interceptor.SimpleInvocationResult;
import org.apache.openejb.EJBInterfaceType;
import org.apache.openejb.EjbInvocation;
import org.apache.openejb.EjbInvocationImpl;

/**
 * @version $Revision$ $Date$
 */
public class ContainerPolicyTest extends TestCase {
    private MockInterceptor interceptor;
    private EjbInvocation invocation;
    private MockTransactionManager transactionManager;

    public void testNotSupportedNoContext() throws Throwable {
        ContainerPolicy.NotSupported.invoke(interceptor, invocation, transactionManager);
        assertNull(interceptor.transaction);
    }

    public void testRequiredNoContext() throws Throwable {
        ContainerPolicy.Required.invoke(interceptor, invocation, transactionManager);
        assertNotNull(interceptor.transaction);
        assertTrue(transactionManager.wasLastTxCommitted());
        assertFalse(transactionManager.wasLastTxRolledBack());

        transactionManager.clear();
        interceptor.throwException = true;
        try {
            ContainerPolicy.Required.invoke(interceptor, invocation, transactionManager);
        } catch (MockSystemException e) {
        }
        assertNotNull(interceptor.transaction);
        assertFalse(transactionManager.wasLastTxCommitted());
        assertTrue(transactionManager.wasLastTxRolledBack());
    }

    protected void setUp() throws Exception {
        super.setUp();
        transactionManager = new MockTransactionManager();
        interceptor = new MockInterceptor(transactionManager);
        invocation = new EjbInvocationImpl(EJBInterfaceType.LOCAL, 0, null);
    }

    private static class MockInterceptor implements Interceptor {
        private final TransactionManager transactionManager;
        private boolean throwException;
        private Transaction transaction;

        public MockInterceptor(TransactionManager transactionManager) {
            this.transactionManager = transactionManager;
        }

        public InvocationResult invoke(Invocation invocation) throws Throwable {
            transaction = transactionManager.getTransaction();
            if (throwException) {
                throw new MockSystemException();
            } else {
                return new SimpleInvocationResult(true, null);
            }
        }

    }

    public static class MockSystemException extends RuntimeException {
    }
}
