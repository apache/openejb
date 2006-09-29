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
package org.apache.openejb.slsb;

import java.lang.reflect.Method;

import junit.framework.TestCase;
import net.sf.cglib.reflect.FastClass;
import org.apache.geronimo.interceptor.SimpleInvocationResult;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public class InvocationTest extends TestCase {
    private FastClass fastClass;
    private int index;

    public void testMethodInvoke() throws Exception {
        MockEJB instance = new MockEJB();
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000000; i++) {
            instance.intMethod(1);
        }
        long end = System.currentTimeMillis();
        System.out.println("Method: "  + ((end - start) * 1000000.0 / 1000000000) + "ns");
    }

    public void testReflectionInvoke() throws Exception {
        Object instance = new MockEJB();
        Object[] args = {new Integer(1)};
        Method m = MockEJB.class.getMethod("intMethod", new Class[]{Integer.TYPE});
        m.invoke(instance, args);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            m.invoke(instance, args);
        }
        long end = System.currentTimeMillis();
        System.out.println("Reflection: " + ((end - start) * 1000000.0 / 1000000) + "ns");
    }

    public void testDirectInvoke() throws Exception {
        Object instance = new MockEJB();
        Object[] args = {new Integer(1)};
        fastClass.invoke(index, instance, args);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            fastClass.invoke(index, instance, args);
        }
        long end = System.currentTimeMillis();
        System.out.println("FastClass: " + ((end - start) * 1000000.0 / 1000000) + "ns");
    }

    public void testDirectInvokeWithResult() throws Exception {
        Object instance = new MockEJB();
        Object[] args = {new Integer(1)};
        fastClass.invoke(index, instance, args);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            new SimpleInvocationResult(true, fastClass.invoke(index, instance, args));
        }
        long end = System.currentTimeMillis();
        System.out.println("FastClass with result: " + ((end - start) * 1000000.0 / 1000000) + "ns");
    }

    protected void setUp() throws Exception {
        super.setUp();
        fastClass = FastClass.create(MockEJB.class);
        index = fastClass.getIndex("intMethod", new Class[]{Integer.TYPE});
    }
}
