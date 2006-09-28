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
package org.apache.openejb.corba.compiler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.Map;
import java.util.Iterator;

import junit.framework.TestCase;
import org.apache.openejb.corba.util.Util;

/**
 * @version $Rev$ $Date$
 */
public class PortableStubCompilerTest extends TestCase {
    private static final File basedir = new File(System.getProperty("basedir", System.getProperty("user.dir")));

    public void testBeanPropertiesNameMangler() throws Exception {
        assertMangling("src/test-resources/beanPropertiesNameMangler.properties", BeanProperties.class);
    }

    public void testBasicNameMangler() throws Exception {
        assertMangling("src/test-resources/nameMangler.properties", Foo.class);
    }

    public void testSpecialNameMangler() throws Exception {
        assertMangling("src/test-resources/specialNameMangler.properties", Special.class);
    }

    private void assertMangling(String propertiesFile, Class intf) throws IOException {
        Properties nameManglerProperties = new Properties();
        File file = new File(basedir, propertiesFile);
        nameManglerProperties.load(new FileInputStream(file));

        boolean failed = false;
        Set methodSignatures = new HashSet();
        Map methodToOperation = Util.mapMethodToOperation(intf);
        for (Iterator iterator = methodToOperation.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            Method method = (Method) entry.getKey();
            String operation = (String) entry.getValue();
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

            String expected = nameManglerProperties.getProperty(methodSignature);
            if (expected == null || !expected.equals(operation)) {
                System.out.println("Expected: " + expected);
                System.out.println("  Actual: " + operation);
                System.out.println();
                failed = true;
            }
        }

        if (!nameManglerProperties.keySet().equals(methodSignatures)) {
            Set extraProperties = new HashSet(nameManglerProperties.keySet());
            extraProperties.removeAll(methodSignatures);
            Set missingProperties = new HashSet(methodSignatures);
            missingProperties.removeAll(nameManglerProperties.keySet());
            fail("extraProperties=" + extraProperties + ", missingProperties=" + missingProperties);
        }

        if (failed) {
            fail();
        }
    }
}
