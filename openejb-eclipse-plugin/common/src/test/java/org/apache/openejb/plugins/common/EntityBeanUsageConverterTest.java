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
import org.jmock.Mockery;
import org.jmock.Expectations;
import org.apache.openejb.config.AppModule;

import javax.persistence.PersistenceUnit;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class EntityBeanUsageConverterTest extends TestCase {
    public void testShouldGenerateEntityManagerCodeForUsageOfEntityBean() throws Exception {
        Mockery context = new Mockery();

        final IJDTFacade facade = context.mock(IJDTFacade.class);

        context.checking(new Expectations() {
            {
                one(facade).addField("org.superbiz.StoreBean", "emf", "javax.persistence.EntityManagerFactory");

                Map<String, Object> emfProps = new HashMap<String, Object>();
                emfProps.put("name", "OpenEJBPU");
                one(facade).addFieldAnnotation("org.superbiz.StoreBean", "emf", PersistenceUnit.class, emfProps);

                List<String[]> ejbCreateSigs = new ArrayList<String[]>();
                ejbCreateSigs.add(new String[] { "java.lang.Integer" });
                one(facade).getSignatures("org.superbiz.ProductHome", "create");
                will(returnValue(ejbCreateSigs));

                one(facade).convertMethodToConstructor("org.superbiz.ProductBean", "ejbCreate", new String[] { "java.lang.Integer" });
                one(facade).changeInvocationsToConstructor("org.superbiz.ProductHome", "create", new String[] { "java.lang.Integer" }, "org.superbiz.ProductBean");

                one(facade).getMethodReturnType("org.superbiz.ProductHome", "findAll", new String[0]);
                will(returnValue("java.util.Collection"));

                one(facade).isTypeCollection("java.util.Collection");
                will(returnValue(true));

                one(facade).changeInvocationsTo("org.superbiz.ProductHome", "findAll", new String[0], "javax.persistence.EntityManager entityManager = entityManagerFactory.createEntityManager();\r\njavax.persistence.Query query = entityManager.createQuery(\"SELECT p from Product p\");\r\nquery.getResultList();\r\n");
            }
        });

        AppModule module = new TestFixture().getAppModule("simple-session-and-entity-ejb-jar.xml", null);
        new EntityBeanUsageConverter(facade).convert(module);

        context.assertIsSatisfied();
    }

}
