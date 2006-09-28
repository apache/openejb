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
 * $Id: SSLSessionManager.java 445359 2005-03-27 02:04:42Z adc $
 */
package org.apache.openejb.corba.security;

import java.util.Hashtable;
import java.util.Map;
import javax.net.ssl.SSLSession;


/**
 * Stores requests' SSL sessions so that they may be shared amongst portable
 * interceptors.  We use this singleton instead of using a ThreadLocal
 * because we cannot guarantee that interceptors will be called under
 * the same thread for a single request.
 * <p/>
 * TODO: There may be an error where the interceptor does not remove the
 * registered session.  We should have a daemon that cleans up old requests.
 *
 * @version $Revision$ $Date$
 */
public final class SSLSessionManager {
    private final static Map requestSSLSessions = new Hashtable();

    public static SSLSession getSSLSession(int requestId) {
        return (SSLSession) requestSSLSessions.get(new Integer(requestId));
    }

    public static void setSSLSession(int requestId, SSLSession session) {
        requestSSLSessions.put(new Integer(requestId), session);
    }

    public static SSLSession clearSSLSession(int requestId) {
        return (SSLSession) requestSSLSessions.remove(new Integer(requestId));
    }
}
