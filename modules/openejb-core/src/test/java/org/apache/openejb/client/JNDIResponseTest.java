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
package org.apache.openejb.client;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.Binding;

import junit.framework.TestCase;
import org.apache.xbean.naming.context.ImmutableContext;

public class JNDIResponseTest extends TestCase {
    private Context context;

    public void setUp() throws Exception {
        Map map = new HashMap();

        map.put("music/jazz/trumpet","Miles Davis");
        map.put("music/jazz/sax","John Coltrane");
        map.put("music/jazz/bebop/year", new Integer(1950));
        map.put("music/country", Boolean.FALSE);
        map.put("music/latin/funk/group", "Los Amigos Invisibles");
        context = new ImmutableContext(map);

    }

    public void testExternalize() throws Exception {
        JNDIResponse response = new JNDIResponse();
        response.setResponseCode(ResponseCodes.JNDI_CONTEXT_TREE);
        response.setResult(context);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);
        response.writeExternal(out);
        out.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream in = new ObjectInputStream(bais);

        response = new JNDIResponse();
        response.readExternal(in);

        assertEquals("Response codes are not equal",response.getResponseCode(),ResponseCodes.JNDI_CONTEXT_TREE);
        Object result = response.getResult();

        assertTrue("Result not instance of Context", result instanceof Context);
        compare(context, (Context)result);
    }

    private void compare(Context original, Context copy) throws Exception{
        NamingEnumeration en = original.listBindings("");
        while (en.hasMoreElements()) {
            Binding binding = (Binding) en.nextElement();
            String name = binding.getName();
            Object expected = binding.getObject();

            Object actual = copy.lookup(name);

            if (expected instanceof Context){
                assertTrue("Object at "+name+" should be an instance of Context", actual instanceof Context);
                compare((Context)expected, (Context)actual);
            } else {
                assertEquals("Objects at "+name+" are not equal", expected, actual);
            }
        }
    }

}
