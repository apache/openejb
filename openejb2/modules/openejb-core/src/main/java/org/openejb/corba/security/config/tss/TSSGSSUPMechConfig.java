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

import java.io.UnsupportedEncodingException;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.omg.CORBA.ORB;
import org.omg.CSI.EstablishContext;
import org.omg.CSIIOP.AS_ContextSec;
import org.omg.CSIIOP.EstablishTrustInClient;
import org.omg.GSSUP.GSSUPMechOID;
import org.omg.GSSUP.InitialContextToken;
import org.omg.IOP.Codec;

import org.apache.geronimo.security.jaas.UsernamePasswordCallback;
import org.apache.geronimo.security.ContextManager;

import org.openejb.corba.security.SASException;
import org.openejb.corba.util.Util;


/**
 * @version $Rev$ $Date$
 */
public class TSSGSSUPMechConfig extends TSSASMechConfig {

    private String targetName;
    private boolean required;

    public TSSGSSUPMechConfig() {
    }

    public TSSGSSUPMechConfig(AS_ContextSec context) {
        targetName = Util.decodeGSSExportName(context.target_name);
        required = (context.target_requires == EstablishTrustInClient.value);
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public short getSupports() {
        return EstablishTrustInClient.value;
    }

    public short getRequires() {
        return (required ? EstablishTrustInClient.value : 0);
    }

    public AS_ContextSec encodeIOR(ORB orb, Codec codec) throws Exception {
        AS_ContextSec result = new AS_ContextSec();

        result.target_supports = EstablishTrustInClient.value;
        result.target_requires = (required ? EstablishTrustInClient.value : 0);
        result.client_authentication_mech = Util.encodeOID(GSSUPMechOID.value);
        result.target_name = Util.encodeGSSExportName(GSSUPMechOID.value, targetName);

        return result;
    }

    public Subject check(EstablishContext msg) throws SASException {
        Subject result = null;

        try {
            if (msg.client_authentication_token != null && msg.client_authentication_token.length > 0) {
                InitialContextToken token = new InitialContextToken();

                if (!Util.decodeGSSUPToken(Util.getCodec(), msg.client_authentication_token, token)) throw new SASException(2);

                if (token.target_name == null) return null;

                String tokenTargetName = (token.target_name == null ? targetName : new String(token.target_name, "UTF8"));

                if (!targetName.equals(tokenTargetName)) throw new SASException(2);

                LoginContext context = new LoginContext(tokenTargetName,
                                                        new UsernamePasswordCallback(new String(token.username, "UTF8"),
                                                                                     new String(token.password, "UTF8").toCharArray()));
                context.login();
                result = ContextManager.getServerSideSubject(context.getSubject());
            }
        } catch (UnsupportedEncodingException e) {
            throw new SASException(1, e);
        } catch (LoginException e) {
            throw new SASException(1, e);
        }


        return result;
    }
}
