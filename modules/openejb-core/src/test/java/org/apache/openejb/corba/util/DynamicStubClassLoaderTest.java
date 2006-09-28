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
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact info@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.apache.openejb.corba.util;

import java.util.Arrays;
import java.util.List;
import javax.rmi.CORBA.Stub;

import junit.framework.TestCase;

/**
 * @version $Revision$ $Date$
 */
public class DynamicStubClassLoaderTest extends TestCase {
    public void testGeneration() throws Exception {
        DynamicStubClassLoader dynamicStubClassLoader = new DynamicStubClassLoader();
        dynamicStubClassLoader.doStart();
        Class c = dynamicStubClassLoader.loadClass("org.omg.stub.org.apache.openejb.corba.compiler._Simple_Stub");
        verifyStub(c);
        verifyStub(c);
        verifyStub(c);
        verifyStub(c);

    }

    private void verifyStub(final Class c) throws Exception {
        final Exception[] exception = new Exception[1];
        Runnable verify = new Runnable() {
            public void run() {
                try {
                    Stub stub = (Stub) c.newInstance();
                    String[] strings = stub._ids();
                    assertNotNull(strings);
                    assertEquals(2, strings.length);
                    List ids = Arrays.asList(strings);
                    assertTrue(ids.contains("RMI:org.apache.openejb.corba.compiler.Simple:0000000000000000"));
                    assertTrue(ids.contains("RMI:org.apache.openejb.corba.compiler.Special:0000000000000000"));
                } catch (Exception e) {
                    exception[0] = e;
                }
            }
        };
        Thread thread = new Thread(verify);
        thread.start();
        thread.join();
        if (exception[0] != null) {
            throw exception[0];
        }
    }
}
