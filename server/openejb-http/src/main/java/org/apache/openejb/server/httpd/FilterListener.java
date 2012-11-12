/*
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
package org.apache.openejb.server.httpd;

import org.apache.openejb.loader.SystemInstance;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class FilterListener implements HttpListener {
    private final String context;
    private final Filter delegate;

    public FilterListener(final Filter filter, final String contextRoot) {
        delegate = filter;
        context = contextRoot;
    }

    @Override
    public void onMessage(final HttpRequest request, final HttpResponse response) throws Exception {
        HttpRequestImpl req = null;
        if (request instanceof HttpRequestImpl) {
            req = (HttpRequestImpl) request;
        } else if (request instanceof ServletRequestAdapter) {
            final HttpServletRequest delegate = ((ServletRequestAdapter) request).getRequest();
            if (delegate instanceof HttpRequestImpl) {
                req = (HttpRequestImpl) delegate;
            }
        }
        if (req != null) {
            req.initPathFromContext(context);
        }
        delegate.doFilter(request, response, new SimpleFilterChain(this));
    }

    public Filter getDelegate() {
        return delegate;
    }

    private static class SimpleFilterChain implements FilterChain {
        private final FilterListener origin;

        private SimpleFilterChain(final FilterListener origin) {
            this.origin = origin;
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
            final HttpListenerRegistry registry = SystemInstance.get().getComponent(HttpListenerRegistry.class);
            registry.setOrigin(origin);
            try {
                registry.onMessage((HttpRequest) request, (HttpResponse) response);
            } catch (Exception e) {
                throw new ServletException(e);
            } finally {
                registry.setOrigin(origin);
            }
        }
    }
}
