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
package org.apache.openejb.server.soap;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.geronimo.webservices.SoapHandler;
import org.apache.geronimo.webservices.WebServiceContainer;
import org.apache.openejb.server.httpd.HttpListener;
import org.apache.openejb.server.httpd.HttpRequest;
import org.apache.openejb.server.httpd.HttpResponse;

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
