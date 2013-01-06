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
package org.apache.openejb.core.stateful;

import java.util.Properties;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatefulBean;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.jee.jpa.unit.TransactionType;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ApplicationComposer.class)
public class PassivationWithEmTest {
    @Test
    public void passivationTest() throws Exception {
        final PassivationWithEm ejb = (PassivationWithEm) SystemInstance.get()
                .getComponent(ContainerSystem.class).getJNDIContext()
                .lookup("global/PassivationWithEmTest/PassivationWithEmTest/PassivationWithEm");
        for (int i = 0; i < 5; i++) {
            Thread.sleep(400); // wait for passivation
            ejb.nothing();
        }
    }

    @Test
    public void passivationExtendedTest() throws Exception {
        final PassivationWithEmExtended ejb = (PassivationWithEmExtended) SystemInstance.get()
                .getComponent(ContainerSystem.class).getJNDIContext()
                .lookup("global/PassivationWithEmTest/PassivationWithEmTest/PassivationWithEmExtended");
        for (int i = 0; i < 5; i++) {
            Thread.sleep(400); // wait for passivation
            ejb.nothing();
        }
    }

    @Module
    public EjbJar bean() {
        final EjbJar ejbJar = new EjbJar(getClass().getSimpleName());
        ejbJar.addEnterpriseBean(new StatefulBean("PassivationWithEm", PassivationWithEm.class));
        ejbJar.addEnterpriseBean(new StatefulBean("PassivationWithEmExtended", PassivationWithEmExtended.class));
        return ejbJar;
    }

    @Module
    public Persistence persistence() throws Exception {
        PersistenceUnit unit = new PersistenceUnit("passivation-unit");
        unit.addClass(MyEntity.class);
        unit.setTransactionType(TransactionType.JTA);
        unit.setProperty("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
        unit.getProperties().setProperty("openjpa.RuntimeUnenhancedClasses", "supported");
        unit.setExcludeUnlistedClasses(true);

        Persistence persistence = new org.apache.openejb.jee.jpa.unit.Persistence(unit);
        persistence.setVersion("2.0");
        return persistence;
    }

    @Configuration
    public Properties config() {
        final Properties p = new Properties();
        p.put("PassivationWithEm Stateful Container", "new://Container?type=STATEFUL");
        p.put("PassivationWithEm Stateful Container.TimeOut", "1 seconds");
        p.put("PassivationWithEm Stateful Container.Capacity", "1");
        p.put("PassivationWithEm Stateful Container.Frequency", "0");

        p.put("PassivationWithEm", "new://Resource?type=DataSource");
        p.put("PassivationWithEm.JdbcDriver", "org.hsqldb.jdbcDriver");
        p.put("PassivationWithEm.JdbcUrl", "jdbc:hsqldb:mem:pwe");
        return p;
    }

    @Entity
    public static class MyEntity {
        @Id
        @GeneratedValue
        private long id;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }
    }

    public static class PassivationWithEm {
        @PersistenceContext
        private EntityManager em;

        public void nothing() {
            // no-op
        }
    }

    public static class PassivationWithEmExtended {
        @PersistenceContext(type = PersistenceContextType.EXTENDED)
        private EntityManager em;

        public void nothing() {
            // no-op
        }
    }
}
