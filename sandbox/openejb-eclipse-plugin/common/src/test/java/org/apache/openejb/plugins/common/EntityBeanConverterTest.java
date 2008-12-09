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
import java.util.HashMap;
import java.util.Map;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.xml.bind.JAXBElement;

import junit.framework.TestCase;

import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.CmpJpaConversion;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.InitEjbDeployments;
import org.apache.openejb.config.OpenEjb2Conversion;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EntityBean;
import org.apache.openejb.jee.JaxbJavaee;
import org.apache.openejb.jee.jpa.Entity;
import org.apache.openejb.jee.oejb2.JaxbOpenejbJar2;
import org.apache.openejb.jee.oejb2.OpenejbJarType;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.xml.sax.InputSource;

public class EntityBeanConverterTest extends TestCase {
	public void testShouldNotThrowAnExceptionWhenProcessingAnEmptyEjbJar() throws Exception {
		Mockery context = new Mockery();
		
		IJDTFacade jdtFacade = context.mock(IJDTFacade.class);
		InputStream is = getClass().getResourceAsStream("empty-ejb-jar.xml");
		InputSource ejbJarSrc = new InputSource(is);
		
		AppModule appModule = getAppModule(ejbJarSrc, null);
		new EntityBeanConverter(jdtFacade).convert(appModule);
	}
	
	public void testShouldNotThrowAnExceptionWhenProcessingAnEmptyEnterpriseBeansElement() throws Exception {
		Mockery context = new Mockery();
		
		IJDTFacade jdtFacade = context.mock(IJDTFacade.class);
		InputStream is = getClass().getResourceAsStream("nobeans-ejb-jar.xml");
		InputSource ejbJarSrc = new InputSource(is);
		
        AppModule appModule = getAppModule(ejbJarSrc, null);		
		new EntityBeanConverter(jdtFacade).convert(appModule);
	}

	
	public void testShouldNotThrowExceptionForEmptyRelationship() throws Exception {
		Mockery context = new Mockery();
		
		IJDTFacade jdtFacade = context.mock(IJDTFacade.class);
		InputStream is = getClass().getResourceAsStream("emptyrelationship-ejb-jar.xml");
		InputSource ejbJarSrc = new InputSource(is);
		
        AppModule appModule = getAppModule(ejbJarSrc, null);
		new EntityBeanConverter(jdtFacade).addRelationshipAnnotations(appModule);
	}
	
	public void testAddAnnotationsForOneToManyRelationship() throws Exception {
		Mockery context = new Mockery();
		
		final IJDTFacade jdtFacade = context.mock(IJDTFacade.class);
		InputStream is = getClass().getResourceAsStream("onetomany-ejb-jar.xml");
		InputSource ejbJarSrc = new InputSource(is);
        
        AppModule appModule = getAppModule(ejbJarSrc, null);		
		
		context.checking(new Expectations() {
			{
				Map<String, Object> oneToManyProperties = new HashMap<String,Object>();
				oneToManyProperties.put("targetEntity", "org.superbiz.OrderLineBean");
				one(jdtFacade).addMethodAnnotation("org.superbiz.OrderBean", "getOrderLine", new String[0], OneToMany.class, oneToManyProperties);

				Map<String, Object> manyToOneProperties = new HashMap<String,Object>();
				manyToOneProperties.put("targetEntity", "org.superbiz.OrderBean");
				one(jdtFacade).addMethodAnnotation("org.superbiz.OrderLineBean", "getOrder", new String[0], ManyToOne.class, manyToOneProperties);
			}
		});
		
		new EntityBeanConverter(jdtFacade).addRelationshipAnnotations(appModule);
	}
	
	public void testAddAnnotationsForManyToManyRelationship() throws Exception {
		Mockery context = new Mockery();
		
		final IJDTFacade jdtFacade = context.mock(IJDTFacade.class);
		InputStream is = getClass().getResourceAsStream("manytomany-ejb-jar.xml");
		InputSource ejbJarSrc = new InputSource(is);
		
        AppModule appModule = getAppModule(ejbJarSrc, null);
		
		context.checking(new Expectations() {
			{
				Map<String, Object> oneToManyProperties = new HashMap<String,Object>();
				oneToManyProperties.put("targetEntity", "org.superbiz.ProductBean");
				one(jdtFacade).addMethodAnnotation("org.superbiz.OrderLineBean", "getProduct", new String[0], ManyToMany.class, oneToManyProperties);
			}
		});
		
		new EntityBeanConverter(jdtFacade).addRelationshipAnnotations(appModule);
	}

	public void testAddAnnotationsForOneToOneRelationship() throws Exception {
		Mockery context = new Mockery();
		
		final IJDTFacade jdtFacade = context.mock(IJDTFacade.class);
		InputStream is = getClass().getResourceAsStream("onetoone-ejb-jar.xml");
		InputSource ejbJarSrc = new InputSource(is);
		
        AppModule appModule = getAppModule(ejbJarSrc, null);		
		context.checking(new Expectations() {
			{
				Map<String, Object> oneToManyProperties = new HashMap<String,Object>();
				oneToManyProperties.put("targetEntity", "org.superbiz.ProductBean");
				one(jdtFacade).addMethodAnnotation("org.superbiz.OrderLineBean", "getProduct", new String[0], OneToOne.class, oneToManyProperties);
			}
		});
		
		new EntityBeanConverter(jdtFacade).addRelationshipAnnotations(appModule);
	}
	
	public void testAddAnnotationsForManyToOneRelationship() throws Exception {
		Mockery context = new Mockery();
		
		final IJDTFacade jdtFacade = context.mock(IJDTFacade.class);
		InputStream is = getClass().getResourceAsStream("manytoone-ejb-jar.xml");
		InputSource ejbJarSrc = new InputSource(is);
		
        AppModule appModule = getAppModule(ejbJarSrc, null);
        
		context.checking(new Expectations() {
			{
				Map<String, Object> oneToManyProperties = new HashMap<String,Object>();
				oneToManyProperties.put("targetEntity", "org.superbiz.ProductBean");
				one(jdtFacade).addMethodAnnotation("org.superbiz.OrderLineBean", "getProduct", new String[0], ManyToOne.class, oneToManyProperties);

				Map<String, Object> manyToOneProperties = new HashMap<String,Object>();
				manyToOneProperties.put("targetEntity", "org.superbiz.OrderLineBean");
				one(jdtFacade).addMethodAnnotation("org.superbiz.ProductBean", "getOrderLine", new String[0], OneToMany.class, manyToOneProperties);
			}
		});
		
		new EntityBeanConverter(jdtFacade).addRelationshipAnnotations(appModule);
		
		context.assertIsSatisfied();
	}
	
	public void testShouldNotThrowExceptionIfTableNameIsMissing() throws Exception {
		Mockery context = new Mockery();
		IJDTFacade jdtFacade = context.mock(IJDTFacade.class);
		
		InputStream ejbJarSrc = getClass().getResourceAsStream("notablename-ejb-jar.xml");
		InputStream openejbJarSrc = getClass().getResourceAsStream("notablename-openejb-jar.xml");

		AppModule appModule = getAppModule(new InputSource(ejbJarSrc), new InputSource(openejbJarSrc));
		Entity entity = appModule.getCmpMappings().getEntityMap().get("openejb.java.lang.Product");
		EntityBean entityBean = (EntityBean) appModule.getEjbModules().get(0).getEjbJar().getEnterpriseBean("ProductEJB");
		
		new EntityBeanConverter(jdtFacade).addTableAnnotation(entityBean, entity);
	}
	
	public void testShouldAddTableAnnotation() throws Exception {
		Mockery context = new Mockery();
		final IJDTFacade jdtFacade = context.mock(IJDTFacade.class);
		
		InputStream ejbJarSrc = getClass().getResourceAsStream("basicentity-ejb-jar.xml");
		InputStream openejbJarSrc = getClass().getResourceAsStream("basicentity-openejb-jar.xml");

		AppModule appModule = getAppModule(new InputSource(ejbJarSrc), new InputSource(openejbJarSrc));
		Entity entity = appModule.getCmpMappings().getEntityMap().get("openejb.java.lang.Product");
		EntityBean entityBean = (EntityBean) appModule.getEjbModules().get(0).getEjbJar().getEnterpriseBean("ProductEJB");
		
		context.checking(new Expectations() {
			{
				Map<String,Object> properties = new HashMap<String, Object>();
				properties.put("name", "products");
				one(jdtFacade).addClassAnnotation("org.superbiz.ProductBean", Table.class, properties);
			}
		});
		
		new EntityBeanConverter(jdtFacade).addTableAnnotation(entityBean, entity);
	}
	
	// columns

	// named queries
	
	// native named queries
	
	// id
	public void testShouldAddIdAnnotation() throws Exception {
		Mockery context = new Mockery();
		final IJDTFacade jdtFacade = context.mock(IJDTFacade.class);
		
		InputStream ejbJarSrc = getClass().getResourceAsStream("basicentity-ejb-jar.xml");
		InputStream openejbJarSrc = getClass().getResourceAsStream("basicentity-openejb-jar.xml");

		AppModule appModule = getAppModule(new InputSource(ejbJarSrc), new InputSource(openejbJarSrc));
		Entity entity = appModule.getCmpMappings().getEntityMap().get("openejb.java.lang.Product");
		EntityBean entityBean = (EntityBean) appModule.getEjbModules().get(0).getEjbJar().getEnterpriseBean("ProductEJB");
		
		context.checking(new Expectations() {
			{
				one(jdtFacade).addMethodAnnotation("org.superbiz.ProductBean", "getId", new String[0], Id.class, null);
				Map<String, Object> generatedvalueProps = new HashMap<String, Object>();
				generatedvalueProps.put("strategy", GenerationType.IDENTITY);
				one(jdtFacade).addMethodAnnotation("org.superbiz.ProductBean", "getId", new String[0], GeneratedValue.class, generatedvalueProps );
			}
		});
		
		new EntityBeanConverter(jdtFacade).addIdAnnotation(entityBean, entity.getAttributes().getId().get(0));
	}
	
	private class DummyClassLoader extends ClassLoader {

		@Override
		protected synchronized Class<?> loadClass(String className, boolean resolve) throws ClassNotFoundException {
			return Object.class;
		}
		
	}
	
	private AppModule getAppModule(InputSource ejbJarSrc, InputSource openEjbJarSrc) throws Exception {
		DummyClassLoader classLoader = new DummyClassLoader();
		AppModule appModule = new AppModule(classLoader, "ModuleToConvert"); //$NON-NLS-1$

		EjbJar ejbJar = (EjbJar) JaxbJavaee.unmarshal(EjbJar.class, ejbJarSrc.getByteStream());
		EjbModule ejbModule = new EjbModule(ejbJar, new OpenejbJar());
		ejbModule.setClassLoader(classLoader);
		appModule.getEjbModules().add(ejbModule);

		if (openEjbJarSrc != null) {
			InitEjbDeployments initEjbDeployments = new InitEjbDeployments();
			initEjbDeployments.deploy(ejbModule, new HashMap<String, String>());

			JAXBElement<?> element = (JAXBElement<?>) JaxbOpenejbJar2.unmarshal(OpenejbJarType.class, openEjbJarSrc.getByteStream());
			OpenejbJarType openejbJarType = (OpenejbJarType) element.getValue();
			ejbModule.getAltDDs().put("openejb-jar.xml", openejbJarType); //$NON-NLS-1$

			CmpJpaConversion cmpJpaConversion = new CmpJpaConversion();
			cmpJpaConversion.deploy(appModule);

			OpenEjb2Conversion openEjb2Conversion = new OpenEjb2Conversion();
			openEjb2Conversion.deploy(appModule);
		}

		return appModule;
	}

}
