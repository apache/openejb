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
package org.apache.openejb.eclipse;

import java.io.File;
import java.util.Properties;

import javax.naming.Context;

import org.apache.openejb.NoSuchApplicationException;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.UndeployException;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.loader.SystemInstance;

public class OpenEjbServer {

	public void init(File baseDir) throws OpenEJBException {

		File homeDir = new File(System.getProperty("user.home"));
		homeDir = new File(homeDir, "openejb");
		if (!homeDir.exists()) {
			homeDir.mkdir();
		}

		File metaInfDir = new File(baseDir, "META-INF");
		File configFile = new File(metaInfDir, "openejb.xml");

		// Setup environment
		Properties properties = new Properties();
		properties.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");

		properties.put("openejb.home", homeDir.getAbsolutePath());
		properties.put("openejb.base", baseDir.getAbsolutePath());
		properties.put("openejb.configuration", configFile.getAbsolutePath());
		properties.put("openejb.deployments.classpath", "false");

		OpenEJB.init(properties);
	}

	public void destroy() throws NoSuchApplicationException, UndeployException {
		Assembler assembler = SystemInstance.get().getComponent(Assembler.class);
		for (AppInfo appInfo : assembler.getDeployedApplications()) {
			assembler.destroyApplication(appInfo.jarPath);
		}
		OpenEJB.destroy();
	}
}
