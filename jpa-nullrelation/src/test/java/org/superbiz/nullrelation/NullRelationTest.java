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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.superbiz.nullrelation;

import junit.framework.TestCase;
import junit.framework.Assert;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Properties;

/**
 * @version $Revision$ $Date$
 */
public class NullRelationTest extends TestCase {

    public void test() throws Exception {

        Properties p = new Properties();
        p.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
        p.put("dataSource", "new://Resource?type=DataSource");

        Context context = new InitialContext(p);
        TestLocal bean = (TestLocal) context.lookup("TestBeanLocal");
        bean.sql("CREATE MEMORY TABLE ABEAN(ID VARCHAR(255) NOT NULL PRIMARY KEY,NAME VARCHAR(255),VALUE INTEGER)");
        bean.sql("CREATE MEMORY TABLE CBEAN(ID VARCHAR(255) NOT NULL PRIMARY KEY,NAME VARCHAR(255),VALUE INTEGER,FK_FOR_BBEAN VARCHAR(255),FK_FOR_ABEAN VARCHAR(255))");
        bean.sql("CREATE MEMORY TABLE BBEAN(ID VARCHAR(255) NOT NULL PRIMARY KEY,NAME VARCHAR(255),VALUE INTEGER,FK_FOR_ABEAN VARCHAR(255))");

        bean.setup();
        bean.test();
    }

    @Stateless
    public static class TestBean implements TestLocal {

        @PersistenceContext
        private EntityManager em;

        @Resource
        private DataSource dataSource;

        public void sql(String sql) throws Exception {
            Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.execute();
        }

        public void setup() throws Exception {
            ABean aBean = new ABean("1", "a1", 1);
            BBean bBean = new BBean("1", "b1", 1);
            em.persist(aBean);
            em.persist(bBean);
            
            CBean beanBean = new CBean("1", "c1", 1);
            beanBean.setA(aBean);
            beanBean.setB(bBean);
            em.persist(beanBean);
        }

        public void test() throws Exception {

            CBean c = em.find(CBean.class, 1);

            ABean a = c.getA();
            BBean b = c.getB();

            BBean bRef = a.getB();
            ABean aRef = b.getA();

            // In the TCK test a1 and b1 should be null, but a1 is pointing to ABean 1
            Assert.assertNull("A's reference to B should be null", bRef);
            Assert.assertNull("B's reference to A should be null ", aRef);
        }

    }

    public static interface TestLocal {
        public void sql(String sql) throws Exception;
        public void setup() throws Exception;
        public void test() throws Exception;
    }

}
