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
 * $Id: GSSUPContext.java 445421 2005-04-23 18:42:37Z adc $
 */
package org.apache.openejb.corba.security.jgss;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Provider;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.ietf.jgss.ChannelBinding;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.MessageProp;
import org.ietf.jgss.Oid;
import org.omg.GSSUP.InitialContextToken;
import sun.security.jgss.spi.GSSContextSpi;
import sun.security.jgss.spi.GSSCredentialSpi;
import sun.security.jgss.spi.GSSNameSpi;

import org.apache.geronimo.security.jaas.UsernamePasswordCallback;
import org.apache.geronimo.security.jaas.UsernamePasswordCredential;

import org.apache.openejb.corba.util.Util;


/**
 * @version $Revision$ $Date$
 */
public final class GSSUPContext implements GSSContextSpi {

    private long end;
    private int lifetime;
    private UsernamePasswordCredential credential;
    private GSSNameSpi there;
    private GSSNameSpi here;
    private boolean anonymity;
    private boolean conf;
    private boolean credDeleg;
    private boolean integState;
    private boolean mutualAuth;
    private boolean replayDet;
    private boolean sequenceDet;
    private boolean protReady;

    GSSUPContext() {
    }

    GSSUPContext(GSSNameSpi peer, GSSCredentialSpi initiatorCred, int lifetime) {
        this.here = peer;

        this.lifetime = (lifetime == GSSContext.DEFAULT_LIFETIME ? GSSContext.INDEFINITE_LIFETIME : lifetime);
        this.end = System.currentTimeMillis() + ((long) this.lifetime * 1000L);
        this.credential = (initiatorCred == null ? null : ((GSSUPCredential) initiatorCred).getCredential());
    }

    GSSUPContext(GSSCredentialSpi acceptorCred) throws GSSException {
        this.here = acceptorCred.getName();

        lifetime = (acceptorCred.getInitLifetime() == GSSContext.DEFAULT_LIFETIME ? GSSContext.INDEFINITE_LIFETIME : acceptorCred.getInitLifetime());
        this.end = System.currentTimeMillis() + ((long) this.lifetime * 1000L);

        checkLife();
    }

    public int getLifetime() {
        return lifetime;
    }

    public void dispose() {
    }

    public boolean getAnonymityState() {
        if (isProtReady()) return false;
        return anonymity;
    }

    public boolean getConfState() {
        if (isProtReady()) return false;
        return conf;
    }

    public boolean getCredDelegState() {
        if (isProtReady()) return false;
        return credDeleg;
    }

    public boolean getIntegState() {
        if (isProtReady()) return false;
        return integState;
    }

    public boolean getMutualAuthState() {
        if (isProtReady()) return false;
        return mutualAuth;
    }

    public boolean getReplayDetState() {
        if (isProtReady()) return false;
        return replayDet;
    }

    public boolean getSequenceDetState() {
        if (isProtReady()) return false;
        return sequenceDet;
    }

    public boolean isEstablished() {
        return isProtReady();
    }

    public boolean isProtReady() {
        return protReady;
    }

    public boolean isTransferable() {
        return true;
    }

    public byte[] export() throws GSSException {
        checkLife();

        try {
            protReady = false;

            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bout);

            out.writeLong(end);
            out.writeInt(lifetime);
            out.writeBoolean(anonymity);
            out.writeBoolean(conf);
            out.writeBoolean(credDeleg);
            out.writeBoolean(integState);
            out.writeBoolean(mutualAuth);
            out.writeBoolean(replayDet);
            out.writeBoolean(sequenceDet);

            return bout.toByteArray();
        } catch (IOException e) {
            throw new GSSException(GSSException.FAILURE);
        }
    }

    public void requestLifetime(int i) {
        if (!isProtReady()) lifetime = i;
    }

    public int getWrapSizeLimit(int qop, boolean confReq, int maxTokenSize) {
        return maxTokenSize;
    }

    public void requestAnonymity(boolean b) {
        if (!isProtReady()) anonymity = b;
    }

    public void requestConf(boolean b) {
        if (!isProtReady()) conf = b;
    }

    public void requestCredDeleg(boolean b) {
        if (!isProtReady()) credDeleg = b;
    }

    public void requestInteg(boolean b) {
        if (!isProtReady()) integState = b;
    }

    public void requestMutualAuth(boolean b) {
        if (!isProtReady()) mutualAuth = b;
    }

    public void requestReplayDet(boolean b) {
        if (!isProtReady()) replayDet = b;
    }

    public void requestSequenceDet(boolean b) {
        if (!isProtReady()) sequenceDet = b;
    }

    public byte[] acceptSecContext(InputStream inputStream, int i) throws GSSException {
        checkLife();
        try {
            if (inputStream.available() == 0) {
                there = new GSSUPAnonUserName();
                protReady = true;
                return null;
            }

            InitialContextToken token = new InitialContextToken();
            byte[] buf = new byte[2048];
            inputStream.read(buf, 0, buf.length);

            Util.decodeGSSUPToken(Util.getCodec(), buf, token);

            LoginContext context = new LoginContext(Util.decodeGSSExportName(token.target_name),
                                                    new UsernamePasswordCallback(new String(token.username, "UTF-8"),
                                                                                 new String(token.password, "UTF-8").toCharArray()));
            context.login();

            there = new GSSUPUserName(token.username);

            protReady = true;
            return null;
        } catch (IOException e) {
            throw new GSSException(GSSException.DEFECTIVE_TOKEN);
        } catch (LoginException e) {
            throw new GSSException(GSSException.DEFECTIVE_CREDENTIAL);
        }
    }

    public byte[] initSecContext(InputStream inputStream, int i) throws GSSException {
        checkLife();

        protReady = true;

        if (credential == null) return new byte[0];

        return Util.encodeGSSUPToken(Util.getORB(), Util.getCodec(), credential.getUsername(), new String(credential.getPassword()), "GSSUP-REALM");
    }

    public Provider getProvider() {
        return GSSUPMechanismFactory.PROVIDER;
    }

    public void setChannelBinding(ChannelBinding channelBinding) {
    }

    public byte[] getMIC(byte[] bytes, int offset, int len, MessageProp messageProp) throws GSSException {
        checkLife();
        return new byte[0];
    }

    public byte[] unwrap(byte[] bytes, int offset, int len, MessageProp messageProp) throws GSSException {
        checkLife();
        byte[] result = new byte[len];

        System.arraycopy(bytes, offset, result, 0, len);

        return result;
    }

    public byte[] wrap(byte[] bytes, int offset, int len, MessageProp messageProp) throws GSSException {
        checkLife();
        byte[] result = new byte[len];

        System.arraycopy(bytes, offset, result, 0, len);

        return result;
    }

    public void verifyMIC(byte[] inTok, int tokOffset, int tokLen, byte[] inNsg, int msgOffset, int msgLen, MessageProp messageProp) throws GSSException {
        checkLife();
    }

    public int unwrap(byte inBuf[], int inOffset, int len, byte[] outBuf, int outOffset, MessageProp messageProp) throws GSSException {
        checkLife();

        System.arraycopy(inBuf, inOffset, outBuf, outOffset, len);

        return len;
    }

    public int wrap(byte inBuf[], int inOffset, int len, byte[] outBuf, int outOffset, MessageProp messageProp) throws GSSException {
        checkLife();

        System.arraycopy(inBuf, inOffset, outBuf, outOffset, len);

        return len;
    }

    public Oid getMech() {
        return GSSUPMechanismFactory.MECHANISM_OID;
    }

    public GSSCredentialSpi getDelegCred() throws GSSException {
        if (!this.isProtReady()) throw new GSSException(GSSException.NO_CONTEXT);
        throw new GSSException(GSSException.NO_CRED);
    }

    public GSSNameSpi getSrcName() throws GSSException {
        if (!this.isProtReady()) throw new GSSException(GSSException.NO_CONTEXT);
        return there;
    }

    public GSSNameSpi getTargName() throws GSSException {
        if (!this.isProtReady()) throw new GSSException(GSSException.NO_CONTEXT);
        return here;
    }

    public int unwrap(InputStream inputStream, byte[] outBuf, int outOffset, MessageProp messageProp) throws GSSException {
        checkLife();

        int count = 0;
        int offset = outOffset;
        try {
            byte[] buf = new byte[1024];
            while (true) {
                int read = inputStream.read(buf);
                if (read == -1) return count;

                System.arraycopy(buf, 0, outBuf, offset, read);
                count += read;
            }
        } catch (IndexOutOfBoundsException e) {
            throw new GSSException(GSSException.FAILURE);
        } catch (IOException e) {
            throw new GSSException(GSSException.FAILURE);
        }
    }

    public void wrap(byte inBuf[], int offset, int len, OutputStream outputStream, MessageProp messageProp) throws GSSException {
        checkLife();
        try {
            outputStream.write(inBuf, offset, len);
        } catch (IOException e) {
            throw new GSSException(GSSException.FAILURE);
        }
    }

    public void verifyMIC(InputStream inputStream, InputStream inputStream1, MessageProp messageProp) throws GSSException {
        checkLife();
    }

    public void getMIC(InputStream inputStream, OutputStream outputStream, MessageProp messageProp) throws GSSException {
        checkLife();
    }

    public void unwrap(InputStream inputStream, OutputStream outputStream, MessageProp messageProp) throws GSSException {
        checkLife();
        try {
            byte[] buf = new byte[1024];
            while (true) {
                int read = inputStream.read(buf);
                if (read == -1) return;

                outputStream.write(buf, 0, read);
            }
        } catch (IOException e) {
            throw new GSSException(GSSException.FAILURE);
        }
    }

    public void wrap(InputStream inputStream, OutputStream outputStream, MessageProp messageProp) throws GSSException {
        checkLife();
        try {
            byte[] buf = new byte[1024];
            while (true) {
                int read = inputStream.read(buf);
                if (read == -1) return;

                outputStream.write(buf, 0, read);
            }
        } catch (IOException e) {
            throw new GSSException(GSSException.FAILURE);
        }
    }

    private void checkLife() throws GSSException {
        if (end < System.currentTimeMillis()) throw new GSSException(GSSException.CONTEXT_EXPIRED);
    }

    static GSSUPContext importGSSUPContext(byte[] exportedContext) throws GSSException {
        try {

            ByteArrayInputStream bin = new ByteArrayInputStream(exportedContext);
            DataInputStream in = new DataInputStream(bin);

            GSSUPContext result = new GSSUPContext();

            result.lifetime = in.readInt();
            result.anonymity = in.readBoolean();
            result.conf = in.readBoolean();
            result.credDeleg = in.readBoolean();
            result.integState = in.readBoolean();
            result.mutualAuth = in.readBoolean();
            result.replayDet = in.readBoolean();
            result.sequenceDet = in.readBoolean();

            result.protReady = true;

            return result;
        } catch (IOException e) {
            throw new GSSException(GSSException.FAILURE);
        }
    }
}
