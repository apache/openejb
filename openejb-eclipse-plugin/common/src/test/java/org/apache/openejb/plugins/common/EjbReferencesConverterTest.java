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

import java.io.InputStream;

import javax.ejb.EJB;

import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.JaxbJavaee;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.xml.sax.InputSource;

import junit.framework.TestCase;

public class EjbReferencesConverterTest extends TestCase {
	public void testShouldNotThrowAnExceptionWhenProcessingAnEmptyEjbJar() throws Exception {
		Mockery context = new Mockery();
		
		IJDTFacade jdtFacade = context.mock(IJDTFacade.class);
		InputStream is = getClass().getResourceAsStream("empty-ejb-jar.xml");
		ClassLoader classLoader = getClass().getClassLoader();
		
		InputSource ejbJarSrc = new InputSource(is);
		EjbJar ejbJar = (EjbJar) JaxbJavaee.unmarshal(EjbJar.class, ejbJarSrc.getByteStream());
		EjbModule ejbModule = new EjbModule(ejbJar, new OpenejbJar());
        ejbModule.setClassLoader(classLoader);
		
		AppModule appModule = new AppModule(classLoader, "ModuleToConvert"); //$NON-NLS-1$
		appModule.getEjbModules().add(ejbModule);
		
		new EjbReferencesConverter(jdtFacade).convert(appModule);
	}
	
	public void testShouldNotThrowAnExceptionWhenProcessingAnEmptyEnterpriseBeansElement() throws Exception {
		Mockery context = new Mockery();
		
		IJDTFacade jdtFacade = context.mock(IJDTFacade.class);
		InputStream is = getClass().getResourceAsStream("nobeans-ejb-jar.xml");
		ClassLoader classLoader = getClass().getClassLoader();
		
		InputSource ejbJarSrc = new InputSource(is);
		EjbJar ejbJar = (EjbJar) JaxbJavaee.unmarshal(EjbJar.class, ejbJarSrc.getByteStream());
		EjbModule ejbModule = new EjbModule(ejbJar, new OpenejbJar());
        ejbModule.setClassLoader(classLoader);
		
		AppModule appModule = new AppModule(classLoader, "ModuleToConvert"); //$NON-NLS-1$
		appModule.getEjbModules().add(ejbModule);
		
		new EjbReferencesConverter(jdtFacade).convert(appModule);
	}

	public void testShouldNotThrowAnExceptionIfSessionBeanHasNoRemoteAndNoLocalClasses() throws Exception {
		Mockery context = new Mockery();
		
		IJDTFacade jdtFacade = context.mock(IJDTFacade.class);
		InputStream is = getClass().getResourceAsStream("badsession-ejb-jar.xml");
		ClassLoader classLoader = getClass().getClassLoader();
		
		InputSource ejbJarSrc = new InputSource(is);
		EjbJar ejbJar = (EjbJar) JaxbJavaee.unmarshal(EjbJar.class, ejbJarSrc.getByteStream());
		EjbModule ejbModule = new EjbModule(ejbJar, new OpenejbJar());
        ejbModule.setClassLoader(classLoader);
		
		AppModule appModule = new AppModule(classLoader, "ModuleToConvert"); //$NON-NLS-1$
		appModule.getEjbModules().add(ejbModule);
		
		new EjbReferencesConverter(jdtFacade).convert(appModule);
	}
	
	public void testShouldNotThrowAnExceptionIfSessionBeanIsEmpty() throws Exception {
		Mockery context = new Mockery();
		
		IJDTFacade jdtFacade = context.mock(IJDTFacade.class);
		InputStream is = getClass().getResourceAsStream("emptysession-ejb-jar.xml");
		ClassLoader classLoader = getClass().getClassLoader();
		
		InputSource ejbJarSrc = new InputSource(is);
		EjbJar ejbJar = (EjbJar) JaxbJavaee.unmarshal(EjbJar.class, ejbJarSrc.getByteStream());
		EjbModule ejbModule = new EjbModule(ejbJar, new OpenejbJar());
        ejbModule.setClassLoader(classLoader);
		
		AppModule appModule = new AppModule(classLoader, "ModuleToConvert"); //$NON-NLS-1$
		appModule.getEjbModules().add(ejbModule);
		
		new EjbReferencesConverter(jdtFacade).convert(appModule);
	}
	
	public void testShouldAddDIAnnotationForRemoteInterface() throws Exception {
		Mockery context = new Mockery();
		
		final IJDTFacade jdtFacade = context.mock(IJDTFacade.class);
		InputStream is = getClass().getResourceAsStream("single-session-bean.xml");
		ClassLoader classLoader = getClass().getClassLoader();
		
		InputSource ejbJarSrc = new InputSource(is);
		EjbJar ejbJar = (EjbJar) JaxbJavaee.unmarshal(EjbJar.class, ejbJarSrc.getByteStream());
		EjbModule ejbModule = new EjbModule(ejbJar, new OpenejbJar());
        ejbModule.setClassLoader(classLoader);
		
		AppModule appModule = new AppModule(classLoader, "ModuleToConvert"); //$NON-NLS-1$
		appModule.getEjbModules().add(ejbModule);
		
		context.checking(new Expectations() {
			{
				one(jdtFacade).addAnnotationToFieldsOfType("org.superbiz.Store", EJB.class,  null);
			}
		});
		
		new EjbReferencesConverter(jdtFacade).convert(appModule);
		context.assertIsSatisfied();
	}

	public void testShouldAddDIAnnotationForLocalInterface() throws Exception {
		Mockery context = new Mockery();
		
		final IJDTFacade jdtFacade = context.mock(IJDTFacade.class);
		InputStream is = getClass().getResourceAsStream("single-session-bean-local.xml");
		ClassLoader classLoader = getClass().getClassLoader();
		
		InputSource ejbJarSrc = new InputSource(is);
		EjbJar ejbJar = (EjbJar) JaxbJavaee.unmarshal(EjbJar.class, ejbJarSrc.getByteStream());
		EjbModule ejbModule = new EjbModule(ejbJar, new OpenejbJar());
        ejbModule.setClassLoader(classLoader);
		
		AppModule appModule = new AppModule(classLoader, "ModuleToConvert"); //$NON-NLS-1$
		appModule.getEjbModules().add(ejbModule);
		
		context.checking(new Expectations() {
			{
				one(jdtFacade).addAnnotationToFieldsOfType("org.superbiz.Store", EJB.class,  null);
			}
		});
		
		new EjbReferencesConverter(jdtFacade).convert(appModule);
		context.assertIsSatisfied();
	}
}
