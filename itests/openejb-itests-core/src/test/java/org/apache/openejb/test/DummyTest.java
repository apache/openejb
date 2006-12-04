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
package org.apache.openejb.test;


import junit.framework.Test;
import junit.framework.TestResult;

/**
 * 
 */
public class DummyTest implements Test {
    private String method;

    private int count;

    protected static final String standardPrefix = "test##_";

    public DummyTest(String method, int i) {
        this.method = method;
        this.count = i;
    }

    public int countTestCases() {
        return count;
    }

    public void run(TestResult result) {
    }

    public String toString() {
        return name() + removePrefix(method);
    }

    public String getName() {
        return name();
    }

    public String name() {
        return "";
    }


    protected static String removePrefix(String methodname) {
        return removePrefix(standardPrefix, methodname);
    }

    protected static String removePrefix(String prefix, String methodname) {
        if (methodname.startsWith(standardPrefix)) {
            return methodname.substring(prefix.length());
        }
        return methodname;
    }
}

