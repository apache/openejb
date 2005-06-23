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
 *    please contact openejb-group@openejb.sf.net.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.sf.net/).
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
package org.openejb.corba.compiler;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class PortableStubCompilerTest extends TestCase {
    private static final File basedir = new File(System.getProperty("basedir", System.getProperty("user.dir")));

    public void testBasicNameMangler() throws Exception {
        Properties nameManglerProperties = new Properties();
        File file = new File(basedir, "src/test-resources/nameMangler.properties");
        nameManglerProperties.load(new FileInputStream(file));

        Set methodSignatures = new HashSet();
        IiopOperation[] iiopOperations = PortableStubCompiler.createIiopOperations(Foo.class);
        for (int i = 0; i < iiopOperations.length; i++) {
            IiopOperation iiopOperation = iiopOperations[i];
            Method method = iiopOperation.getMethod();
            String methodSignature = method.getName() + "(";

            Class[] parameterTypes = method.getParameterTypes();
            for (int j = 0; j < parameterTypes.length; j++) {
                Class parameterType = parameterTypes[j];
                String arrayBrackets = "";
                while (parameterType.isArray()) {
                    arrayBrackets += "[]";
                    parameterType = parameterType.getComponentType();
                }
                methodSignature += parameterType.getName() + arrayBrackets;
            }
            methodSignature += ")";
            methodSignatures.add(methodSignature);

            assertTrue("Method not present in name mangler properties: " + methodSignature, nameManglerProperties.containsKey(methodSignature));
            assertEquals(nameManglerProperties.getProperty(methodSignature), iiopOperation.getName());
        }

        assertEquals("Did not match all methods", nameManglerProperties.keySet(), methodSignatures);
    }

    public void testSpecialNameMangler() throws Exception {
        Properties nameManglerProperties = new Properties();
        File file = new File(basedir, "src/test-resources/specialNameMangler.properties");
        nameManglerProperties.load(new FileInputStream(file));

        Set methodSignatures = new HashSet();
        IiopOperation[] iiopOperations = PortableStubCompiler.createIiopOperations(Special.class);
        for (int i = 0; i < iiopOperations.length; i++) {
            IiopOperation iiopOperation = iiopOperations[i];
            Method method = iiopOperation.getMethod();
            String methodSignature = method.getName() + "(";

            Class[] parameterTypes = method.getParameterTypes();
            for (int j = 0; j < parameterTypes.length; j++) {
                Class parameterType = parameterTypes[j];
                String arrayBrackets = "";
                while (parameterType.isArray()) {
                    arrayBrackets += "[]";
                    parameterType = parameterType.getComponentType();
                }
                methodSignature += parameterType.getName() + arrayBrackets;
            }
            methodSignature += ")";
            methodSignatures.add(methodSignature);

            assertTrue("Method not present in name mangler properties: " + methodSignature, nameManglerProperties.containsKey(methodSignature));
            assertEquals(nameManglerProperties.getProperty(methodSignature), iiopOperation.getName());
        }

        assertEquals("Did not match all methods", nameManglerProperties.keySet(), methodSignatures);
    }
}
