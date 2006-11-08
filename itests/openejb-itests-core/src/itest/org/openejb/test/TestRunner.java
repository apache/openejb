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
package org.openejb.test;

import java.io.PrintStream;

import junit.framework.TestResult;

/**
 *
 */
public class TestRunner extends junit.textui.TestRunner {

    /**
     * Constructs a TestRunner.
     */
    public TestRunner() {
        this(System.out);
    }

    /**
     * Constructs a TestRunner using the given stream for all the output
     */
    public TestRunner(PrintStream writer) {
        this(new ResultPrinter(writer));
    }

    /**
     * Constructs a TestRunner using the given ResultPrinter all the output
     */
    public TestRunner(ResultPrinter printer) {
        super(printer);
    }

    /**
     * main entry point.
     */
    public static void main(String args[]) {

        try {
//            org.openejb.util.ClasspathUtils.addJarsToPath("lib");
//            org.openejb.util.ClasspathUtils.addJarsToPath("dist");
//            org.openejb.util.ClasspathUtils.addJarsToPath("beans");

            TestRunner aTestRunner = new TestRunner();
            TestResult r = aTestRunner.start(args);
            if (!r.wasSuccessful()) System.exit(FAILURE_EXIT);
            System.exit(SUCCESS_EXIT);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(EXCEPTION_EXIT);
        }
    }


    public TestResult start(String args[]) throws Exception {
        TestResult result = null;
        try {

            TestManager.init(null);
            TestManager.start();
        } catch (Exception e) {
            System.out.println("Cannot initialize the test environment: " + e.getClass().getName() + " " + e.getMessage());
            //e.printStackTrace();
            //System.exit(-1);
            throw e;
        }

        try {
            result = super.start(args);
        } catch (Exception ex) {
        } finally {
            try {
                TestManager.stop();
            } catch (Exception e) {
                ;   // ignore it
            }
        }
        //System.exit(0);
        return result;
    }
}
