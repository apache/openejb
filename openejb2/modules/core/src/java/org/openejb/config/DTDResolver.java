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
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.config;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Loads DTDs from disk so you don't have to hit the web to validate
 * configuration files.  This class will fail silently if the files aren't
 * available locally, and you'll end up hitting the web anyway.
 *
 * @author <a href="mailto:ammulder@alumni.princeton.edu">Aaron Mulder</a>
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @version $Revision$
 */
public class DTDResolver implements EntityResolver {
    public static HashMap dtds = new HashMap();

    static {
        byte[] bytes = getDtd("ejb-jar_1_1.dtd");
        if(bytes != null) {
            dtds.put("ejb-jar.dtd",     bytes);
            dtds.put("ejb-jar_1_1.dtd", bytes);
        }
        bytes = getDtd("ejb-jar_2_0.dtd");
        if(bytes != null) {
            dtds.put("ejb-jar_2_0.dtd", bytes);
        }
    }

    public static byte[] getDtd(String dtdName) {
        try{
            // load resource:/openejb/dtds/ejb-jar_1_1.dtd
            URL dtd = new URL("resource:/openejb/dtds/"+dtdName);
            InputStream in = dtd.openStream();
            if (in == null) return null;

            byte[] buf = new byte[512];
            
            in = new BufferedInputStream(in);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            
            int count;
            while((count = in.read(buf)) > -1) out.write(buf, 0, count);
            
            in.close();
            out.close();
            
            return out.toByteArray();
        } catch (Throwable e){
            return null;
        }
    }

    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        
        int pos = systemId.lastIndexOf('/');
        if (pos != -1) {
            systemId = systemId.substring(pos+1);
        }
        
        byte[] data = (byte[])dtds.get(systemId);
        
        if (data != null) {
            return new InputSource(new ByteArrayInputStream(data));
        } else {
            return null;
        }
    }
}
