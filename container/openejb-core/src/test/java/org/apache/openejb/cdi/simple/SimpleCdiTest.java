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
package org.apache.openejb.cdi.simple;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.StatelessSessionContainerInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.cdi.CdiAppScannerService;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.core.ivm.naming.InitContextFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.junit.Before;

import junit.framework.TestCase;

@SuppressWarnings("deprecation")
public class SimpleCdiTest extends TestCase{

    private InitialContext ctx;
    
    @Before
    public void setUp() throws Exception {
	
	CdiAppScannerService.BEANS_XML_LOCATION = "org/apache/openejb/cdi/simple/META-INF/beans.xml";
	CdiAppScannerService.APPEND_PACKAGE_NAME = "org.apache.openejb.cdi.simple";
        ConfigurationFactory config = new ConfigurationFactory();
        Assembler assembler = new Assembler();

        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        assembler.createContainer(config.configureService(StatelessSessionContainerInfo.class));

        EjbModule ejbModule = buildTestApp();
        EjbJarInfo ejbJar = config.configureApplication(ejbModule);       
        
        assertNotNull(ejbJar);

        assembler.createApplication(ejbJar);

        Properties properties = new Properties(System.getProperties());
        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());
        ctx = new InitialContext(properties);
    }
    
    public void testSimple(){
	try {
	    EchoLocal echo = (EchoLocal)ctx.lookup("EchoLocal");
	    String result = echo.echo("Gurkan");
	    assertEquals("Gurkan", result);
	    
	    assertTrue(EchoInterceptor.RUN);
	    assertTrue(NormalEjbInterceptor.RUN);
	    assertTrue(NormalEjbInterceptor.INJECTED);
	    
	} catch (NamingException e) {
	    e.printStackTrace();
	}
	
	
    }
    
    public EjbModule buildTestApp() throws Exception {
        EjbJar ejbJar = new EjbJar();
        ejbJar.setId(this.getClass().getName());
        
        ejbJar.addEnterpriseBean(new StatelessBean(Echo.class));
        EjbModule module = new EjbModule(ejbJar);
        module.getAltDDs().put("beans.xml", SimpleCdiTest.class.getClassLoader().getResource("cdi/beans.xml"));
        
        
        return module;
    }
    
    
}
