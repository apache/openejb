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

import org.apache.openejb.config.*;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.JaxbJavaee;
import org.apache.openejb.jee.oejb2.JaxbOpenejbJar2;
import org.apache.openejb.jee.oejb2.OpenejbJarType;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.xml.sax.InputSource;

import javax.xml.bind.JAXBElement;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;

public class TestFixture {

    private class DummyClassLoader extends ClassLoader {

		@Override
		protected synchronized Class<?> loadClass(String className, boolean resolve) throws ClassNotFoundException {
			return Object.class;
		}

	}

    public AppModule getAppModule(String ejbJarFilename, String openejbJarFilename) throws Exception {
        InputStream ejbJarSrc = getClass().getResourceAsStream(ejbJarFilename);
        InputStream openejbJarSrc = null;

        if (openejbJarFilename != null) {
            openejbJarSrc = getClass().getResourceAsStream(openejbJarFilename);
        }

        InputSource openejbJarInputSource = null;

        if (openejbJarSrc != null) {
            openejbJarInputSource = new InputSource(openejbJarSrc);
        }

        return getAppModule(new InputSource(ejbJarSrc), openejbJarInputSource);
    }

    public AppModule getAppModule(InputSource ejbJarSrc, InputSource openEjbJarSrc) throws Exception {
        DummyClassLoader classLoader = new DummyClassLoader();
		AppModule appModule = new AppModule(classLoader, "ModuleToConvert"); //$NON-NLS-1$

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
    }
}
