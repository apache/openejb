/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.plugins.common;

import junit.framework.TestCase;
import org.apache.openejb.config.AppModule;
import org.jmock.Mockery;
import org.jmock.Expectations;

import javax.ejb.Remote;
import javax.ejb.RemoteHome;
import java.util.HashMap;
import java.util.Map;

public class SessionBeanRemoteAnnotationAdderTest extends TestCase {
    public void testShouldAddRemoteAndRemoteHomeAnnotations() throws Exception {
        Mockery context = new Mockery();
        final IJDTFacade facade = context.mock(IJDTFacade.class);


        context.checking(new Expectations() {
            {
                one(facade).addClassAnnotation("org.superbiz.Store", Remote.class, null);

                Map<String, Object> remoteHomeProps = new HashMap<String, Object>();
                remoteHomeProps.put("value", "org.superbiz.StoreHome");
            }
        });

        AppModule module = new TestFixture().getAppModule("single-session-bean.xml", null);
        new SessionBeanRemoteAnnotationAdder(facade).convert(module);

        context.assertIsSatisfied();
    }

    public void testNotShouldAddRemoteAndRemoteHomeAnnotationsForLocalBeans() throws Exception {
        Mockery context = new Mockery();
        final IJDTFacade facade = context.mock(IJDTFacade.class);

        AppModule module = new TestFixture().getAppModule("single-session-bean-local.xml", null);
        new SessionBeanRemoteAnnotationAdder(facade).convert(module);

        context.assertIsSatisfied();
    }

    public void testNotShouldAddRemoteAndRemoteHomeAnnotationsForBadSessionBean() throws Exception {
        Mockery context = new Mockery();
        final IJDTFacade facade = context.mock(IJDTFacade.class);

        AppModule module = new TestFixture().getAppModule("badsession-ejb-jar.xml", null);
        new SessionBeanRemoteAnnotationAdder(facade).convert(module);

        context.assertIsSatisfied();
    }

    public void testNotShouldAddRemoteAndRemoteHomeAnnotationsForEmptySessionBean() throws Exception {
        Mockery context = new Mockery();
        final IJDTFacade facade = context.mock(IJDTFacade.class);

        AppModule module = new TestFixture().getAppModule("emptysession-ejb-jar.xml", null);
        new SessionBeanRemoteAnnotationAdder(facade).convert(module);

        context.assertIsSatisfied();
    }
}
