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

import org.xml.sax.InputSource;

public class ConverterTest extends TestCase {
	
	private Converter[] getConverter(IJDTFacade facade) {
		return new Converter[] {
			new SessionBeanConverter(facade),
			new EjbReferencesConverter(facade),
			new EntityBeanConverter(facade),
			new EntityBeanPojoConverter(facade),
			new SessionBeanInterfaceModifier(facade),
			new SessionBeanRemoteAnnotationAdder(facade)
		};
	}
	
	// dirty hack to get around the CmpJpaConverter trying to load classes
	// mean we don't need to include classes along with our xml files
	private class DummyClassLoader extends ClassLoader {

		@Override
		protected synchronized Class<?> loadClass(String className, boolean resolve) throws ClassNotFoundException {
			return Object.class;
		}
		
	}
	
	public void testShouldGenerateAnnotationsWithoutThrowingAnError1() throws Exception {
		convert("ejb-jar1.xml", "openejb-jar1.xml");
	}

	public void testShouldGenerateAnnotationsWithoutThrowingAnError2() throws Exception {
		convert("ejb-jar2.xml", "openejb-jar2.xml");
	}

	public void testShouldGenerateAnnotationsWithoutThrowingAnError3() throws Exception {
		convert("ejb-jar3.xml", "openejb-jar3.xml");
	}

	
	private void convert(String ejbJarXml, String openEjbJarXml) throws ConversionException {
		IJDTFacade stub = new JDTFacadeStub();
		InputSource ejbJarSrc = null;
		if (ejbJarXml != null)
			ejbJarSrc = new InputSource(getClass().getResourceAsStream(ejbJarXml));
		
		InputSource openEjbJarSrc = null;
		if (openEjbJarXml != null) {
			openEjbJarSrc = new InputSource(getClass().getResourceAsStream(openEjbJarXml));
		}

		OpenEjbXmlConverter converter = new OpenEjbXmlConverter(getConverter(stub), new DummyClassLoader());
		converter.convert(ejbJarSrc, openEjbJarSrc);
	}
}
