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
package org.openejb.corba.security.jgss;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Provider;

import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;
import sun.security.jgss.spi.GSSCredentialSpi;
import sun.security.jgss.spi.GSSNameSpi;

import org.apache.geronimo.security.jaas.UsernamePasswordCredential;


/**
 * @version $Revision$ $Date$
 */
public class GSSUPCredential implements GSSCredentialSpi {

    private GSSNameSpi name;
    private int initLifetime;
    private int acceptLifetime;
    private int usage;
    private UsernamePasswordCredential credential;

    GSSUPCredential(GSSNameSpi name, int initLifetime, int acceptLifetime, int usage) {
        this.name = name;
        this.initLifetime = initLifetime;
        this.acceptLifetime = acceptLifetime;
        this.usage = usage;

        if (isInitiatorCredential()) {
            AccessControlContext acc = AccessController.getContext();
            credential = (UsernamePasswordCredential) AccessController.doPrivileged(new SubjectComber(acc, name.toString()));
        }
    }

    public UsernamePasswordCredential getCredential() {
        return credential;
    }

    public int getAcceptLifetime() throws GSSException {
        return acceptLifetime;
    }

    public int getInitLifetime() throws GSSException {
        return initLifetime;
    }

    public void dispose() throws GSSException {
        credential = null;
    }

    public boolean isAcceptorCredential() {
        return usage == GSSCredential.ACCEPT_ONLY || usage == GSSCredential.INITIATE_AND_ACCEPT;
    }

    public boolean isInitiatorCredential() {
        return usage == GSSCredential.INITIATE_ONLY || usage == GSSCredential.INITIATE_AND_ACCEPT;
    }

    public Provider getProvider() {
        return GSSUPMechanismFactory.PROVIDER;
    }

    public Oid getMechanism() {
        return GSSUPMechanismFactory.MECHANISM_OID;
    }

    public GSSNameSpi getName() throws GSSException {
        return name;
    }
}
