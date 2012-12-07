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

package org.apache.openejb.config;

import java.io.IOException;
import java.util.Properties;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import junit.framework.TestCase;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.loader.SystemInstance;

public class JndiNameFormatTest extends TestCase {

	private Assembler assembler;

	private void deploy(String format) throws OpenEJBException, IOException, NamingException {
		SystemInstance.get().setProperty("openejb.jndiname.format", format);

		ConfigurationFactory config = new ConfigurationFactory();
        assembler = new Assembler();

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        AppModule app = new AppModule(this.getClass().getClassLoader(), "test-app");

        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(EchoImpl.class));
        app.getEjbModules().add(new EjbModule(ejbJar));

        assembler.createApplication(config.configureApplication(app));
	}

    public void testShouldLookupDeployBeanWithLowercaseInterfaceName() throws Exception {
        deploy("{ejbName}/{interfaceType.annotationName.lc}");
    	
    	Properties p = new Properties();
        p.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.core.LocalInitialContextFactory");

        InitialContext context = new InitialContext(p);
        Echo echo = (Echo) context.lookup("EchoImpl/remote");

        assertEquals("Echoing: This is a test", echo.echo("This is a test"));
        assembler.destroy();
    }

    public void testShouldLookupDeployBeanWithUppercaseInterfaceName() throws Exception {
        deploy("{ejbName}/{interfaceType.annotationName.uc}");
    	
    	Properties p = new Properties();
        p.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.core.LocalInitialContextFactory");

        InitialContext context = new InitialContext(p);
        Echo echo = (Echo) context.lookup("EchoImpl/REMOTE");

        assertEquals("Echoing: This is a test", echo.echo("This is a test"));
        assembler.destroy();
    }

    public void testShouldLookupDeployBeanWithCamelCaseInterfaceName() throws Exception {
        deploy("{ejbName}/{interfaceType.annotationName.cc}");
    	
    	Properties p = new Properties();
        p.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.core.LocalInitialContextFactory");

        InitialContext context = new InitialContext(p);
        Echo echo = (Echo) context.lookup("EchoImpl/Remote");

        assertEquals("Echoing: This is a test", echo.echo("This is a test"));
        assembler.destroy();
    }

    public void testShouldLookupDeployBeanLowerCaseClassNameAndUpperCaseInterfaceName() throws Exception {
        deploy("{ejbName.lc}/{interfaceType.annotationName.uc}");
    	
    	Properties p = new Properties();
        p.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.core.LocalInitialContextFactory");

        InitialContext context = new InitialContext(p);
        Echo echo = (Echo) context.lookup("echoimpl/REMOTE");

        assertEquals("Echoing: This is a test", echo.echo("This is a test"));
        assembler.destroy();
    }

    
    @Remote
    public static interface Echo {
        String echo(String input);
    }

    @Stateless
    public static class EchoImpl implements Echo {

        public EchoImpl() {
        }

        public String echo(String input) {
            return "Echoing: " + input;
        }
    }

}
