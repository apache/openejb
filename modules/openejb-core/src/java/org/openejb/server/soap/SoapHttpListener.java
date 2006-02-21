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
 *    please contact openejb@openejb.org.
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
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.server.soap;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.geronimo.webservices.SoapHandler;
import org.apache.geronimo.webservices.WebServiceContainer;
import org.openejb.server.httpd.HttpListener;
import org.openejb.server.httpd.HttpRequest;
import org.openejb.server.httpd.HttpResponse;

public class SoapHttpListener implements HttpListener, SoapHandler {

    private final Map contextPathToWSMap = new HashMap();



    public void onMessage(HttpRequest req, HttpResponse res) throws IOException {
        //TODO previous behavior of closing streams was inconsistent.
        //Following servlet model, neither in or out is beinc closed.
        //TODO probably returning 500 internal server error would be more appropriate than translating to IOException.

        String path = req.getURI().getPath();
        WebServiceContainer container = (WebServiceContainer) contextPathToWSMap.get(path);

        if (container == null) {
            res.setCode(404);
            res.setResponseString("No such web service");
            return;
        }

        res.setContentType("text/xml");
        RequestAdapter request = new RequestAdapter(req);
        ResponseAdapter response = new ResponseAdapter(res);

        if (req.getQueryParameter("wsdl") != null) {
            try {
                container.getWsdl(request, response);
            } catch (IOException e) {
                throw e;
            } catch (Exception e) {
                throw (IOException) new IOException("Could not fetch wsdl!").initCause(e);
            }
        } else {
            try {
                container.invoke(request, response);
            } catch (IOException e) {
                throw e;
            } catch (Exception e) {
                throw (IOException) new IOException("Could not process message!").initCause(e);
            }
        }
    }


    public void addWebService(String contextPath, String[] virtualHosts, WebServiceContainer webServiceContainer, String securityRealmName, String realmName, String transportGuarantee, String authMethod, ClassLoader classLoader) throws Exception {
        contextPathToWSMap.put(contextPath, webServiceContainer);
    }

    public void removeWebService(String contextPath) {
        contextPathToWSMap.remove(contextPath);
    }


    public static class RequestAdapter implements WebServiceContainer.Request {

        private final HttpRequest request;
        private final HashMap parameters;

        public RequestAdapter(HttpRequest request) {
            this.request = request;
            parameters = new HashMap();
            parameters.putAll(request.getFormParameters());
            parameters.putAll(request.getQueryParameters());
        }

        public String getHeader(String name) {
            return request.getHeader(name);
        }

        public URI getURI() {
            return request.getURI();
        }

        public int getContentLength() {
            return request.getContentLength();
        }

        public String getContentType() {
            return request.getContentType();
        }

        public InputStream getInputStream() throws IOException {
            return request.getInputStream();
        }

        public int getMethod() {
            return request.getMethod();
        }

        public String getParameter(String name) {
            return (String)parameters.get(name);
        }

        public Map getParameters() {
            return (Map)parameters.clone();
        }

        public Object getAttribute(String name) {
            return request.getAttribute(name);
        }

        public void setAttribute(String name, Object value){
            request.setAttribute(name, value);
        }

    }

    public static class ResponseAdapter implements WebServiceContainer.Response {
        private final HttpResponse response;

        public ResponseAdapter(HttpResponse response) {
            this.response = response;
        }

        public void setHeader(String name, String value) {
            response.setHeader(name, value);
        }

        public String getHeader(String name) {
            return response.getHeader(name);
        }

        public OutputStream getOutputStream() {
            return response.getOutputStream();
        }

        public void setStatusCode(int code) {
            response.setCode(code);
        }

        public int getStatusCode() {
            return response.getCode();
        }

        public void setContentType(String type) {
            response.setContentType(type);
        }

        public String getContentType() {
            return response.getContentType();
        }

        public void setStatusMessage(String responseString) {
            response.setResponseString(responseString);
        }

    }

}
