/*
 *     Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.apache.openejb.server.cxf.rs;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.server.cxf.rs.beans.MyFirstRestClass;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@EnableServices("jax-rs")
@RunWith(ApplicationComposer.class)
public class CDIApplicationTest {
    @Module
    @Classes(cdi = true, value = { MyCdiRESTApplication.class, ACdiBeanInjectedInApp.class })
    public WebApp war() {
        return new WebApp()
                .contextRoot("foo")
                .addServlet("REST Application", Application.class.getName())
                .addInitParam("REST Application", "javax.ws.rs.Application", MyCdiRESTApplication.class.getName());
    }

    @Test
    public void isCdi() {
        assertTrue(MyCdiRESTApplication.injection);
        assertEquals("Hi from REST World!", WebClient.create("http://localhost:4204/foo/").path("/first/hi").get(String.class));
    }

    public static class ACdiBeanInjectedInApp {}

    public static class MyCdiRESTApplication extends Application {
        public static boolean injection = false;

        @Inject
        private ACdiBeanInjectedInApp cdi;

        public Set<Class<?>> getClasses() {
            injection = cdi != null;

            if (cdi == null) {
                throw new NullPointerException();
            }

            // if no class are returned we use scanning, since we don't test rest deployment we put a single class
            final Set<Class<?>> clazz = new HashSet<Class<?>>();
            clazz.add(MyFirstRestClass.class);
            return clazz;
        }
    }
}
