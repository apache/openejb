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
 * Copyright 2004 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.client;

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

public class JNDIResponseTest extends TestCase {
    private Context context;

    public void setUp() throws Exception {
        Map map = new HashMap();

        map.put("music/jazz/trumpet","Miles Davis");
        map.put("music/jazz/sax","John Coltrane");
        map.put("music/jazz/bebop/year", new Integer(1950));
        map.put("music/country", Boolean.FALSE);
        map.put("music/latin/funk/group", "Los Amigos Invisibles");
        context = new ContextImpl(map);

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
