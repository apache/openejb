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

package org.apache.openejb.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * OpenEJB Enterprise Javabean Protocol (OEJP)
 * 
 * OEJP uses a "<major>.<minor>" numbering scheme to indicate versions of the protocol.
 *
 *     Protocol-Version   = "OEJP" "/" 1*DIGIT "." 1*DIGIT
 *
 * Some compatability is guaranteed with the major part of the version number.
 *
 * @version $Revision$ $Date$
 */
public class ProtocolMetaData {

    private static final String OEJB = "OEJP";
    private String id;
    private int major;
    private int minor;

    public ProtocolMetaData() {
    }

    public ProtocolMetaData(String version) {
        init(OEJB+"/"+version);
    }

    private void init(String spec) {
        assert spec.matches("^OEJP/[0-9]\\.[0-9]$"): "Protocol version spec must follow format [ \"OEJB\" \"/\" 1*DIGIT \".\" 1*DIGIT ]";

        char[] chars = new char[8];
        spec.getChars(0, chars.length, chars, 0);

        this.id = new String(chars, 0, 4);
        this.major = Integer.parseInt(new String(chars, 5,1));
        this.minor = Integer.parseInt(new String(chars, 7,1));
    }

    public String getId() {
        return id;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public String getVersion() {
        return major+"."+minor;
    }

    public String getSpec() {
        return id+"/"+major+"."+minor;
    }

    public void writeExternal(OutputStream out) throws IOException {
        out.write(getSpec().getBytes("UTF-8"));
    }

    public void readExternal(InputStream in) throws IOException {
        byte[] spec = new byte[8];
        for (int i = 0; i < spec.length; i++) {
            spec[i] = (byte) in.read();
            if (spec[i] == -1){
                throw new IOException("Unable to read protocol version.  Reached the end of the stream.");
            }
        }
        init(new String(spec,"UTF-8"));
    }
}
