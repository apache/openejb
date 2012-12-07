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
package org.apache.openejb.assembler.classic;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.core.LocalInitialContextFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.SingletonBean;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

public class OpenEJBXmlByModuleTest {
    private Context context = null;

    private UselessBean bean;

    @Before
    public void setUp() throws OpenEJBException, NamingException, IOException {
        ConfigurationFactory config = new ConfigurationFactory();
        Assembler assembler = new Assembler();

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        AppModule app = new AppModule(OpenEJBXmlByModuleTest.class.getClassLoader(), OpenEJBXmlByModuleTest.class.getSimpleName());

        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new SingletonBean(UselessBean.class));
        app.getEjbModules().add(new EjbModule(ejbJar));
        app.getEjbModules().iterator().next().getAltDDs().put("resources.xml", getClass().getClassLoader().getResource("META-INF/resource/appresource.openejb.xml"));

        assembler.createApplication(config.configureApplication(app));

        Properties properties = new Properties();
        properties.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());
        properties.setProperty("openejb.embedded.initialcontext.close", "destroy");

        // some hack to be sure to call destroy()
        context = new InitialContext(properties);

        bean = (UselessBean) context.lookup("UselessBeanLocalBean");
    }

    @After
    public void close() throws NamingException {
        if (context != null) {
            context.close();
            OpenEJB.destroy(); // has to be called manually since we start openejb in a custom way
        }
    }

    @Test
    public void test() throws Exception {
        assertNotNull(bean.datasource());
        assertTrue(bean.datasource() instanceof BasicDataSource);
        BasicDataSource ds = (BasicDataSource) bean.datasource();
        assertEquals("org.hsqldb.jdbcDriver", ds.getDriverClassName());
        assertEquals("not:used:url", ds.getUrl());
        assertEquals("foo", ds.getUsername());
        assertEquals("bar", ds.getPassword());

        assertNotNull(bean.resource());
        assertEquals("ok", bean.resource().attr);
    }

    public static class MyResource {
        public String attr = "ok";
    }

    public static class UselessBean {
        @Resource(name = "DS")
        private DataSource ds;
        @Resource(name = "My Resource", type = MyResource.class)
        private MyResource rs;

        public DataSource datasource() {
            return ds;
        }

        public MyResource resource() {
            return rs;
        }
    }
}
