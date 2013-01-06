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
package org.apache.openejb.web;

import org.apache.openejb.assembler.classic.WebAppBuilder;
import org.apache.openejb.config.WebModule;
import org.apache.openejb.core.WebContext;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Component;
import org.apache.openejb.testing.Module;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(ApplicationComposer.class)
public class LightweightWebAppBuilderTest {
    @Component
    public WebAppBuilder webAppBuilder() {
        return new LightweightWebAppBuilder();
    }

    @Module
    public WebModule war() {
        return new WebModule(new WebApp(), "/foo", Thread.currentThread().getContextClassLoader(), "", "web");
    }

    @Test
    public void checkWebContextExists() {
        final WebContext wc = SystemInstance.get().getComponent(ContainerSystem.class).getWebContext("web");
        assertNotNull(wc);
        assertEquals("web", wc.getId());
    }
}
