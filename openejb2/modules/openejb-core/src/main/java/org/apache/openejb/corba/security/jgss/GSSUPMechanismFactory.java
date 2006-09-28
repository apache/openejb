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
 * $Id: GSSUPMechanismFactory.java 445259 2005-03-08 04:08:36Z adc $
 */
package org.apache.openejb.corba.security.jgss;

import java.security.Provider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;
import sun.security.jgss.spi.GSSContextSpi;
import sun.security.jgss.spi.GSSCredentialSpi;
import sun.security.jgss.spi.GSSNameSpi;
import sun.security.jgss.spi.MechanismFactory;


/**
 * @version $Revision$ $Date$
 */
public final class GSSUPMechanismFactory implements MechanismFactory {

    private final static Log log = LogFactory.getLog(GSSUPMechanismFactory.class);

    final static Oid MECHANISM_OID;
    private final static Oid[] NAME_TYPES = new Oid[]{GSSName.NT_HOSTBASED_SERVICE, GSSName.NT_USER_NAME};
    final static Provider PROVIDER;

    public Provider getProvider() {
        return PROVIDER;
    }

    public Oid getMechanismOid() {
        return MECHANISM_OID;
    }

    public Oid[] getNameTypes() {
        return NAME_TYPES;
    }

    public GSSContextSpi getMechanismContext(byte[] exportedContext) throws GSSException {
        return GSSUPContext.importGSSUPContext(exportedContext);
    }

    public GSSContextSpi getMechanismContext(GSSCredentialSpi myAcceptorCred) throws GSSException {
        return new GSSUPContext(myAcceptorCred);
    }

    public GSSCredentialSpi getCredentialElement(GSSNameSpi name, int initLifetime, int acceptLifetime, int usage) {
        if (name == null) {
            if (usage == GSSCredential.INITIATE_ONLY || usage == GSSCredential.INITIATE_AND_ACCEPT) {
                name = new GSSUPAnonUserName();
            } else {
                name = new GSSUPAnonServerName();
            }
        }
        return new GSSUPCredential(name, initLifetime, acceptLifetime, usage);
    }

    public GSSNameSpi getNameElement(byte[] name, Oid nameType) throws GSSException {
        if (nameType.equals(GSSName.NT_HOSTBASED_SERVICE)) {
            return new GSSUPServerName(name);
        } else if (nameType.equals(GSSName.NT_USER_NAME)) {
            return new GSSUPUserName(name);
        }
        throw new GSSException(GSSException.BAD_NAMETYPE, -1, nameType.toString() + " is an unsupported nametype");
    }

    public GSSContextSpi getMechanismContext(GSSNameSpi peer, GSSCredentialSpi myInitiatorCred, int lifetime) {
        return new GSSUPContext(peer, myInitiatorCred, lifetime);
    }

    public GSSNameSpi getNameElement(String nameStr, Oid nameType) throws GSSException {
        return getNameElement(nameStr.getBytes(), nameType);
    }

    static {
        Oid tempOID = null;
        try {
            tempOID = new Oid("2.23.130.1.1.1");
        } catch (GSSException e) {
            log.fatal("Unable to initialize mechanisms OID: " + e);
        }
        MECHANISM_OID = tempOID;

        PROVIDER = new GSSUPProvider();
    }

}
