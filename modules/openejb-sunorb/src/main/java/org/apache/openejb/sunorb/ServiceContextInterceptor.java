/**
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package org.apache.openejb.sunorb;

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
        return "org.apache.openejb.sunorb.ServiceContextInterceptor";
    }
}
