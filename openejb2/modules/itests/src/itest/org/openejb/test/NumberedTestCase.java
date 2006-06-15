/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;

import junit.framework.Assert;
import junit.framework.Protectable;
import junit.framework.Test;
import junit.framework.TestResult;

/**
 * 
 */
public class NumberedTestCase extends Assert implements Test {

    Method[] testMethods = new Method[]{};
    protected static final String standardPrefix = "test##_";

    class MethodComparator implements java.util.Comparator {

        public int compare(Object o1, Object o2) {
            Method m1 = (Method) o1;
            Method m2 = (Method) o2;
            return m1.getName().compareTo(m2.getName());
        }

        public boolean eqauls(Object other) {
            if (other instanceof MethodComparator) {
                return true;
            } else {
                return false;
            }
        }
    }


    public NumberedTestCase() {
        try {
            // Get all methods of the subclass
            Method[] methods = getClass().getMethods();
            java.util.TreeSet tm = new java.util.TreeSet(new MethodComparator());

            // Add the ones that start with "test"
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().startsWith("test")) {
                    tm.add(methods[i]);
                }
            }
            testMethods = new Method[tm.size()];
            Iterator orderedMethods = tm.iterator();
            for (int i = 0; orderedMethods.hasNext(); i++) {
                testMethods[i] = (Method) orderedMethods.next();
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    /**
     * Counts the number of test cases that will be run by this test.
     */
    public int countTestCases() {
        return testMethods.length;
    }

    /**
     * Runs a test and collects its result in a TestResult instance.
     */
    public void run(TestResult result) {
        try {
            setUp();
        } catch (Exception e) {
            Test test = new Test() {
                public int countTestCases() {
                    return 0;
                }

                public void run(TestResult result) {
                }

                public String toString() {
                    return name() + ".setUp()";
                }
            };

            result.addError(test, e);
            return;
        }
        for (int i = 0; i < testMethods.length; i++) {
            run(result, testMethods[i]);
        }
        try {
            tearDown();
        } catch (Exception e) {
            Test test = new Test() {
                public int countTestCases() {
                    return 0;
                }

                public void run(TestResult result) {
                }

                public String toString() {
                    return name() + ".tearDown()";
                }
            };

            result.addError(test, e);
            return;
        }
    }

    protected void run(final TestResult result, final Method testMethod) {
        Test test = createTest(testMethod);
        result.startTest(test);
        Protectable p = new Protectable() {
            public void protect() throws Throwable {
                runTestMethod(testMethod);
            }
        };
        result.runProtected(test, p);
        result.endTest(test);
    }


    protected Test createTest(final Method testMethod) {
        Test test = new Test() {
            public int countTestCases() {
                return 1;
            }

            public void run(TestResult result) {
            }

            public String toString() {
                return createTestName(testMethod);
            }
        };
        return test;
    }

    protected void runTestMethod(Method testMethod) throws Throwable {
        try {
            testMethod.invoke(this, new Class[0]);
        } catch (InvocationTargetException e) {
            e.fillInStackTrace();
            throw e.getTargetException();
        } catch (IllegalAccessException e) {
            e.fillInStackTrace();
            throw e;
        }
    }


    public String toString() {
        return name();
    }

    public String name() {
        return "";
    }

    protected String createTestName(Method testMethod) {
        return name() + removePrefix(testMethod.getName());
    }

    protected static String removePrefix(String name) {
        return removePrefix(standardPrefix, name);
    }

    protected static String removePrefix(String prefix, String name) {
        return name.substring(prefix.length());
    }
}


