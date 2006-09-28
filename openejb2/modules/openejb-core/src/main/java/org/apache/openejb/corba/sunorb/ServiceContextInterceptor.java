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
package org.apache.openejb.corba.sunorb;

import java.net.Socket;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import com.sun.corba.se.interceptor.RequestInfoExt;
import com.sun.corba.se.connection.Connection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.omg.CORBA.LocalObject;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;

import org.apache.openejb.corba.security.SSLSessionManager;


/**
 * @version $Revision$ $Date$
 */
final class ServiceContextInterceptor extends LocalObject implements ServerRequestInterceptor {

    private final Log log = LogFactory.getLog(ServiceContextInterceptor.class);

    public ServiceContextInterceptor() {
        if (log.isDebugEnabled()) log.debug("<init>");
    }

    public void receive_request(ServerRequestInfo ri) {
    }

    public void receive_request_service_contexts(ServerRequestInfo ri) {

        if (log.isDebugEnabled()) log.debug("Looking for SSL Session");

        RequestInfoExt riExt = (RequestInfoExt) ri;
        Connection connection = riExt.connection();
        if (connection != null) {
            Socket socket = connection.getSocket();
            if (socket instanceof SSLSocket) {
                if (log.isDebugEnabled()) log.debug("Found SSL Session");
                SSLSocket sslSocket = (SSLSocket) socket;

                SSLSessionManager.setSSLSession(ri.request_id(), sslSocket.getSession());
            }
        }
    }

    public void send_exception(ServerRequestInfo ri) {
        SSLSession old = SSLSessionManager.clearSSLSession(ri.request_id());
        if (log.isDebugEnabled() && old != null) log.debug("Removing SSL Session for send_exception");
    }

    public void send_other(ServerRequestInfo ri) {
        SSLSession old = SSLSessionManager.clearSSLSession(ri.request_id());
        if (log.isDebugEnabled() && old != null) log.debug("Removing SSL Session for send_reply");
    }

    public void send_reply(ServerRequestInfo ri) {
        SSLSession old = SSLSessionManager.clearSSLSession(ri.request_id());
        if (log.isDebugEnabled() && old != null) log.debug("Removing SSL Session for send_reply");
    }

    public void destroy() {
        if (log.isDebugEnabled()) log.debug("Destroy");
    }

    public String name() {
        return "org.apache.openejb.corba.sunorb.ServiceContextInterceptor";
    }
}
