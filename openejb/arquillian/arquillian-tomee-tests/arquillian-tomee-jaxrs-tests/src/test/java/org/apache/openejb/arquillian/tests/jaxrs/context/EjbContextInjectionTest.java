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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.arquillian.tests.jaxrs.context;

import org.apache.openejb.loader.IO;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertEquals;

/**
 * @version $Rev$ $Date$
 */
@RunAsClient
@RunWith(Arquillian.class)
public class EjbContextInjectionTest {


    @ArquillianResource
    private URL url;

    @Deployment
    public static WebArchive archive() {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(EjbContextInjectionTest.class);
    }

    @Test
    public void rest() throws IOException {
        final String response = IO.slurp(new URL(url.toExternalForm() + "injections/check"));
        assertEquals("true", response);
    }


    @Singleton
    @Path("/injections")
    public static class RsInjection {

        @Context
        private HttpHeaders httpHeaders;

        @Context
        private Providers providers;

        @Context
        private HttpServletResponse response;

        @Context
        private Request request;

        @Context
        private HttpServletRequest httpServletRequest;

        @Context
        private UriInfo uriInfo;

        @Context
        private SecurityContext securityContext;

        @Context
        private ContextResolver contextResolver;

        @GET
        @Path("/check")
        public boolean check() {
            // Are they injected?
            Assert.assertNotNull("httpHeaders", httpHeaders);
            Assert.assertNotNull("providers", providers);
            Assert.assertNotNull("response", response);
            Assert.assertNotNull("request", request);
            Assert.assertNotNull("httpServletRequest", httpServletRequest);
            Assert.assertNotNull("uriInfo", uriInfo);
            Assert.assertNotNull("securityContext", securityContext);
            Assert.assertNotNull("contextResolver", contextResolver);

            // Do the thread locals actually point anywhere?
            Assert.assertTrue(httpHeaders.getRequestHeaders().size() > 0);
            Assert.assertTrue(providers.getExceptionMapper(FooException.class) == null);
            Assert.assertTrue(response.getHeaderNames() != null);
            Assert.assertTrue(request.getMethod() != null);
            Assert.assertTrue(httpServletRequest.getMethod() != null);
            Assert.assertTrue(uriInfo.getPath() != null);
            Assert.assertTrue(!securityContext.isUserInRole("Foo"));
            Assert.assertTrue(contextResolver.getContext(null) == null);

            return true;
        }
    }

    public static class FooException extends RuntimeException {
    }
}
