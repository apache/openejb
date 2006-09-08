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
package org.openejb.corba.security.config.css;

import java.util.Iterator;
import java.util.Set;

import javax.security.auth.Subject;

import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.jaas.NamedUsernamePasswordCredential;
import org.openejb.corba.security.config.tss.TSSASMechConfig;
import org.openejb.corba.security.config.tss.TSSGSSUPMechConfig;
import org.openejb.corba.util.Util;


/**
 * This GSSUP mechanism obtains its username and password from a named username
 * password credential that is stored in the subject associated w/ the call
 * stack.
 *
 * @version $Revision$ $Date$
 */
public class CSSGSSUPMechConfigDynamic implements CSSASMechConfig {

    private final String domain;
    private transient byte[] encoding;

    public CSSGSSUPMechConfigDynamic(String domain) {
        this.domain = domain;
    }

    public short getSupports() {
        return 0;
    }

    public short getRequires() {
        return 0;
    }

    public boolean canHandle(TSSASMechConfig asMech) {
        if (asMech instanceof TSSGSSUPMechConfig) return true;
        if (asMech.getRequires() == 0) return true;

        return false;
    }

    public byte[] encode() {
        if (encoding == null) {
            NamedUsernamePasswordCredential credential = null;
            Subject subject = ContextManager.getNextCaller();

            Set creds = subject.getPrivateCredentials(NamedUsernamePasswordCredential.class);

            if (creds.size() != 0) {
                for (Iterator iter = creds.iterator(); iter.hasNext();) {
                    NamedUsernamePasswordCredential temp = (NamedUsernamePasswordCredential) iter.next();
                    if (temp.getName().equals(domain)) {
                        credential = temp;
                        break;
                    }
                }
                if(credential != null) {
                    encoding = Util.encodeGSSUPToken(Util.getORB(), Util.getCodec(), credential.getUsername(), new String(credential.getPassword()), domain);
                }
            }

            if (encoding == null) encoding = new byte[0];
        }
        return encoding;
    }
}
