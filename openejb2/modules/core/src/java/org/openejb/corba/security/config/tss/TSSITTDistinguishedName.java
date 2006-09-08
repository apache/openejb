/* ====================================================================
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce this list of
 *    conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
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
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the OpenEJB Project.  For more information
 * please see <http://openejb.org/>.
 *
 * ====================================================================
 */
package org.openejb.corba.security.config.tss;

import java.security.Principal;
import javax.security.auth.Subject;
import javax.security.auth.x500.X500Principal;

import org.omg.CORBA.Any;
import org.omg.CSI.ITTDistinguishedName;
import org.omg.CSI.IdentityToken;
import org.omg.CSI.X501DistinguishedNameHelper;
import org.omg.IOP.CodecPackage.FormatMismatch;
import org.omg.IOP.CodecPackage.TypeMismatch;

import org.apache.geronimo.security.DomainPrincipal;
import org.apache.geronimo.security.PrimaryDomainPrincipal;
import org.apache.geronimo.security.PrimaryRealmPrincipal;
import org.apache.geronimo.security.RealmPrincipal;

import org.openejb.corba.security.SASException;
import org.openejb.corba.util.Util;


/**
 * @version $Rev: $ $Date$
 */
public class TSSITTDistinguishedName extends TSSSASIdentityToken {

    public static final String OID = "";
    private final String realmName;
    private final String domainName;

    public TSSITTDistinguishedName(String realmName, String domainName) {
        this.realmName = realmName;
        this.domainName = domainName;
    }

    public short getType() {
        return ITTDistinguishedName.value;
    }

    public String getOID() {
        return OID;
    }

    public Subject check(IdentityToken identityToken) throws SASException {
        byte[] distinguishedNameToken = identityToken.dn();
        Any any = null;
        try {
            any = Util.getCodec().decode_value(distinguishedNameToken, X501DistinguishedNameHelper.type());
        } catch (FormatMismatch formatMismatch) {
            throw new SASException(1, formatMismatch);
        } catch (TypeMismatch typeMismatch) {
            throw new SASException(1, typeMismatch);
        }

        byte[] principalNameBytes = X501DistinguishedNameHelper.extract(any);
        Subject subject = new Subject();
        X500Principal x500Principal = new X500Principal(principalNameBytes);
        subject.getPrincipals().add(x500Principal);

        if (realmName != null && domainName != null) {
            subject.getPrincipals().add(new RealmPrincipal(realmName, domainName, x500Principal));
            subject.getPrincipals().add(new PrimaryRealmPrincipal(realmName, domainName, x500Principal));
        }
        if (domainName != null) {
            subject.getPrincipals().add(new DomainPrincipal(domainName, x500Principal));
            subject.getPrincipals().add(new PrimaryDomainPrincipal(domainName, x500Principal));
        }

        return subject;
    }
}
