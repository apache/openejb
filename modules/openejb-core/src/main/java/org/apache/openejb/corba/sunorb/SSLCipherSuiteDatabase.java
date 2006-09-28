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
/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/
package org.apache.openejb.corba.sunorb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.omg.CSIIOP.Confidentiality;
import org.omg.CSIIOP.EstablishTrustInTarget;
import org.omg.CSIIOP.NoProtection;


/**
 * @version $Revision$ $Date$
 */
public final class SSLCipherSuiteDatabase {

    /**
     * A map for stroing all the cipher suites.
     */
    private static final Map SUITES = new HashMap();

    static {
        // No protection
        Integer noProt = new Integer(NoProtection.value);
        SUITES.put("SSL_NULL_WITH_NULL_NULL", noProt);
        SUITES.put("TLS_NULL_WITH_NULL_NULL", noProt);

        // No authentication
        Integer noAuth = new Integer(Confidentiality.value);
        SUITES.put("SSL_DH_anon_EXPORT_WITH_DES40_CBC_SHA", noAuth);
        SUITES.put("SSL_DH_anon_EXPORT_WITH_RC4_40_MD5", noAuth);
        SUITES.put("SSL_DH_anon_WITH_3DES_EDE_CBC_SHA", noAuth);
        SUITES.put("SSL_DH_anon_WITH_RC4_128_MD5", noAuth);
        SUITES.put("SSL_DH_anon_WITH_DES_CBC_SHA", noAuth);

        SUITES.put("TLS_DH_anon_EXPORT_WITH_DES40_CBC_SHA", noAuth);
        SUITES.put("TLS_DH_anon_EXPORT_WITH_RC4_40_MD5", noAuth);
        SUITES.put("TLS_DH_anon_WITH_3DES_EDE_CBC_SHA", noAuth);
        SUITES.put("TLS_DH_anon_WITH_RC4_128_MD5", noAuth);
        SUITES.put("TLS_DH_anon_WITH_DES_CBC_SHA", noAuth);

        // No encryption
        Integer noEnc = new Integer(EstablishTrustInTarget.value);
        SUITES.put("SSL_RSA_WITH_NULL_MD5", noEnc);
        SUITES.put("SSL_RSA_WITH_NULL_SHA", noEnc);

        SUITES.put("TLS_RSA_WITH_NULL_MD5", noEnc);
        SUITES.put("TLS_RSA_WITH_NULL_SHA", noEnc);

        // Auth and encrypt
        Integer authEnc = new Integer(EstablishTrustInTarget.value | Confidentiality.value);
        SUITES.put("SSL_DHE_DSS_WITH_DES_CBC_SHA", authEnc);
        SUITES.put("SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA", authEnc);
        SUITES.put("SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA", authEnc);
        SUITES.put("SSL_RSA_WITH_RC4_128_MD5", authEnc);
        SUITES.put("SSL_RSA_WITH_RC4_128_SHA", authEnc);
        SUITES.put("SSL_RSA_WITH_DES_CBC_SHA", authEnc);
        SUITES.put("SSL_RSA_WITH_3DES_EDE_CBC_SHA", authEnc);
        SUITES.put("SSL_RSA_EXPORT_WITH_RC4_40_MD5", authEnc);

        SUITES.put("TLS_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA", authEnc);
        SUITES.put("TLS_DHE_DSS_WITH_3DES_EDE_CBC_SHA", authEnc);
        SUITES.put("TLS_DHE_DSS_WITH_DES_CBC_SHA", authEnc);
        SUITES.put("TLS_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA", authEnc);
        SUITES.put("TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA", authEnc);
        SUITES.put("TLS_DHE_RSA_WITH_DES_CBC_SHA", authEnc);
        SUITES.put("TLS_DH_DSS_EXPORT_WITH_DES40_CBC_SHA", authEnc);
        SUITES.put("TLS_DH_DSS_WITH_3DES_EDE_CBC_SHA", authEnc);
        SUITES.put("TLS_DH_DSS_WITH_DES_CBC_SHA", authEnc);
        SUITES.put("TLS_DH_RSA_EXPORT_WITH_DES40_CBC_SHA", authEnc);
        SUITES.put("TLS_DH_RSA_WITH_3DES_EDE_CBC_SHA", authEnc);
        SUITES.put("TLS_DH_RSA_WITH_DES_CBC_SHA", authEnc);
        SUITES.put("TLS_KRB5_EXPORT_WITH_DES_CBC_40_MD5", authEnc);
        SUITES.put("TLS_KRB5_EXPORT_WITH_DES_CBC_40_SHA", authEnc);
        SUITES.put("TLS_KRB5_EXPORT_WITH_RC2_CBC_40_MD5", authEnc);
        SUITES.put("TLS_KRB5_EXPORT_WITH_RC2_CBC_40_SHA", authEnc);
        SUITES.put("TLS_KRB5_EXPORT_WITH_RC4_40_MD5", authEnc);
        SUITES.put("TLS_KRB5_EXPORT_WITH_RC4_40_SHA", authEnc);
        SUITES.put("TLS_KRB5_WITH_3DES_EDE_CBC_MD5", authEnc);
        SUITES.put("TLS_KRB5_WITH_3DES_EDE_CBC_SHA", authEnc);
        SUITES.put("TLS_KRB5_WITH_DES_CBC_MD5", authEnc);
        SUITES.put("TLS_KRB5_WITH_DES_CBC_SHA", authEnc);
        SUITES.put("TLS_KRB5_WITH_RC4_128_MD5", authEnc);
        SUITES.put("TLS_KRB5_WITH_RC4_128_SHA", authEnc);
        SUITES.put("TLS_RSA_EXPORT_WITH_DES40_CBC_SHA", authEnc);
        SUITES.put("TLS_RSA_EXPORT_WITH_RC2_CBC_40_MD5", authEnc);
        SUITES.put("TLS_RSA_EXPORT_WITH_RC4_40_MD5", authEnc);
        SUITES.put("TLS_RSA_WITH_3DES_EDE_CBC_SHA", authEnc);
        SUITES.put("TLS_RSA_WITH_DES_CBC_SHA", authEnc);
        SUITES.put("TLS_RSA_WITH_RC4_128_MD5", authEnc);
        SUITES.put("TLS_RSA_WITH_RC4_128_SHA", authEnc);

        // RSA supported cipher suite names differ from Sun's
        SUITES.put("RSA_Export_With_RC2_40_CBC_MD5", authEnc);
        SUITES.put("RSA_With_DES_CBC_SHA", authEnc);
        SUITES.put("RSA_Export_With_RC4_40_MD5", authEnc);
        SUITES.put("RSA_With_RC4_SHA", authEnc);
        SUITES.put("RSA_With_3DES_EDE_CBC_SHA", authEnc);
        SUITES.put("RSA_Export_With_DES_40_CBC_SHA", authEnc);
        SUITES.put("RSA_With_RC4_MD5", authEnc);
    }

    /**
     * Do not allow instances of this class.
     */
    private SSLCipherSuiteDatabase() {
    }

    /**
     * Return an array of cipher suites that match the assocRequires and
     * assocSupports options.
     *
     * @param assocRequires         The required associations.
     * @param assocSupports         The supported associations.
     * @param supportedCipherSuites The overall supported cipher suites.
     * @return The cipher suites that matches the two options.
     */
    public static String[] getCipherSuites(int assocRequires, int assocSupports, String[] supportedCipherSuites) {

        assocRequires = assocRequires & (EstablishTrustInTarget.value | Confidentiality.value | NoProtection.value);
        assocSupports = assocSupports & (EstablishTrustInTarget.value | Confidentiality.value | NoProtection.value);

        ArrayList col = new ArrayList();
        for (int i = 0; i < supportedCipherSuites.length; ++i) {
            Integer val = (Integer) SUITES.get(supportedCipherSuites[i]);

            if (val != null && ((assocRequires & ~val.intValue()) == 0 && (val.intValue() & ~assocSupports) == 0)) {
                col.add(supportedCipherSuites[i]);
            }
        }

        String[] ret = new String[col.size()];
        col.toArray(ret);

        return ret;
    }

    /**
     * Return the options values for a cipher suite.
     *
     * @param cypherSuite The cipher suite to get the options value for.
     * @return The int value for the cipher suite.
     */
    public static int getAssociaionOptions(String cypherSuite) {
        return ((Integer) SUITES.get(cypherSuite)).intValue();
    }
}

