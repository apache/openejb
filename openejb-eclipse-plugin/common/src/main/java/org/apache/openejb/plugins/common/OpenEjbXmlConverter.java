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

import java.util.HashMap;
import java.util.HashSet;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.CmpJpaConversion;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.InitEjbDeployments;
import org.apache.openejb.config.OpenEjb2Conversion;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.JaxbJavaee;
import org.apache.openejb.jee.jpa.EntityMappings;
import org.apache.openejb.jee.oejb2.JaxbOpenejbJar2;
import org.apache.openejb.jee.oejb2.OpenejbJarType;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Scans an ejb-jar.xml file using a JAXB parser, and adds annotations
 * to source based on the XML.
 *
 * Depends on an implementation of IJavaProjectAnnotationFacade
 *
 */
public class OpenEjbXmlConverter {

	public static final String CLS_TRANSACTION_ATTRIBUTE = "javax.ejb.TransactionAttribute"; //$NON-NLS-1$
	public static final String CLS_APPLICATION_EXCEPTION = "javax.ejb.ApplicationException"; //$NON-NLS-1$
	public static final String CLS_STATEFUL = "javax.ejb.Stateful"; //$NON-NLS-1$
	public static final String CLS_STATELESS = "javax.ejb.Stateless"; //$NON-NLS-1$
	public static final String CLS_MESSAGE_DRIVEN = "javax.ejb.MessageDriven"; //$NON-NLS-1$
	public static final String STATELESS_CLASS = CLS_STATELESS;
	private Converter[] converters;
	private ClassLoader classLoader;



	public OpenEjbXmlConverter(Converter[] converters) {
		super();
		this.converters = converters;
		this.classLoader = getClass().getClassLoader();
	}

	public OpenEjbXmlConverter(Converter[] converters, ClassLoader classLoader) {
		super();
		this.converters = converters;
		this.classLoader = classLoader;
	}

	
	/**
	 * Parses the XML
	 * @param source An input source to the content of ejb-jar.xml
	 * @return Whether or not the parsing was successful
	 * @throws ConversionException
	 */
	public boolean convert(InputSource source) throws ConversionException {
		return convert(source, null);
	}

	/**
	 * Parses the XML
	 * @param ejbJarSrc An input source to the content of ejb-jar.xml
	 * @param openEjbJarSrc An input source to the content of openejb-jar.xml (optional)
	 * @return Whether or not the parsing was successful
	 * @throws ConversionException
	 */
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	public boolean convert(InputSource ejbJarSrc, InputSource openEjbJarSrc) throws ConversionException {
		AppModule appModule = getAppModule(ejbJarSrc, openEjbJarSrc);

		if (appModule.getCmpMappings() == null) {
			appModule.setCmpMappings(new EntityMappings());
		}
		
		for (Converter converter : converters) {
			converter.convert(appModule);
		}

		return true;
	}

	private AppModule getAppModule(InputSource ejbJarSrc, InputSource openEjbJarSrc) throws ConversionException {
		AppModule appModule = new AppModule(classLoader, "ModuleToConvert"); //$NON-NLS-1$
		
		try {
			EjbJar ejbJar = (EjbJar) JaxbJavaee.unmarshal(EjbJar.class, ejbJarSrc.getByteStream());
	        EjbModule ejbModule = new EjbModule(ejbJar, new OpenejbJar());
	        ejbModule.setClassLoader(classLoader);
	        appModule.getEjbModules().add(ejbModule);

			if (openEjbJarSrc != null) {
		        InitEjbDeployments initEjbDeployments = new InitEjbDeployments();
		        initEjbDeployments.deploy(ejbModule);

		        JAXBElement<?> element = (JAXBElement<?>) JaxbOpenejbJar2.unmarshal(OpenejbJarType.class, openEjbJarSrc.getByteStream());
		        OpenejbJarType openejbJarType = (OpenejbJarType) element.getValue();
		        ejbModule.getAltDDs().put("openejb-jar.xml", openejbJarType); //$NON-NLS-1$

		        CmpJpaConversion cmpJpaConversion = new CmpJpaConversion();
		        cmpJpaConversion.deploy(appModule);

		        OpenEjb2Conversion openEjb2Conversion = new OpenEjb2Conversion();
		        openEjb2Conversion.deploy(appModule);
			}

			return appModule;
		} catch (JAXBException e) {
			throw new ConversionException(Messages.getString("org.apache.openejb.helper.annotation.conversionExceptionMsg.1"), e); //$NON-NLS-1$
		} catch (ParserConfigurationException e) {
			throw new ConversionException(Messages.getString("org.apache.openejb.helper.annotation.conversionExceptionMsg.2"), e); //$NON-NLS-1$
		} catch (SAXException e) {
			throw new ConversionException(Messages.getString("org.apache.openejb.helper.annotation.conversionExceptionMsg.3"), e); //$NON-NLS-1$
		} catch (OpenEJBException e) {
			throw new ConversionException(Messages.getString("org.apache.openejb.helper.annotation.conversionExceptionMsg.4")); //$NON-NLS-1$
		}
	}
}
