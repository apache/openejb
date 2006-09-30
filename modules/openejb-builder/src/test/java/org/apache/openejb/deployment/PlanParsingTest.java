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
package org.apache.openejb.deployment;

import java.io.File;
import java.util.Collections;

import junit.framework.TestCase;
import org.apache.geronimo.j2ee.deployment.NamingBuilderCollection;
import org.apache.openejb.xbeans.ejbjar.OpenejbOpenejbJarType;

/**
 */
public class PlanParsingTest extends TestCase {
    private OpenEjbModuleBuilder builder;
    File basedir = new File(System.getProperty("basedir", "."));

    protected void setUp() throws Exception {
        super.setUp();
        builder = new OpenEjbModuleBuilder(null, 
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                Collections.EMPTY_LIST,
                Collections.EMPTY_LIST,
                new NamingBuilderCollection(null, null),
                new MockResourceEnvironmentSetter(),
                null,
                null);
    }

    public void testResourceRef() throws Exception {
        File resourcePlan = new File(basedir, "src/test/resources/plans/plan1.xml");
        assertTrue(resourcePlan.exists());
        OpenejbOpenejbJarType openejbJar = builder.getOpenejbJar(resourcePlan, null, true, null, null);
        assertEquals(1, openejbJar.getEnterpriseBeans().getSessionArray()[0].getResourceRefArray().length);
        System.out.println(openejbJar.toString());
    }

}
