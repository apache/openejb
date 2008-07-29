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
package org.apache.openejb.eclipse.server;


import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;



public class ServerDeployer {
	private String openejbDir;
	
	public ServerDeployer(String openejbDir) {
		super();
		this.openejbDir = openejbDir;
	}

	public String deploy(String filename) {
		Object obj = callDeployerBusinessRemote("deploy", filename);
		if (obj == null) {
			return null;
		}
		
		try {
			return (String) obj.getClass().getDeclaredField("jarPath").get(obj);
		} catch (Exception e) {
			return null;
		}
	}

	private Object callDeployerBusinessRemote(String methodName, String filename) {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		try {
			Properties properties =	 new Properties();
			properties.put(Context.INITIAL_CONTEXT_FACTORY, "org.openejb.client.RemoteInitialContextFactory");
			properties.put(Context.PROVIDER_URL, "ejbd://localhost:4201");
			
			URL[] urls = getUrls(openejbDir);
			URLClassLoader cl = new URLClassLoader(urls, classLoader);
			Thread.currentThread().setContextClassLoader(cl);
			
			InitialContext context = new InitialContext(properties);
			Object ref = context.lookup("openejb/DeployerBusinessRemote");
			
			Method method = ref.getClass().getMethod(methodName, String.class);
			return method.invoke(ref, filename);
		} catch (Exception e) {
			return null;
		} finally {
			Thread.currentThread().setContextClassLoader(classLoader);
		}
	}

	public boolean undeploy(String filename) {
		return callDeployerBusinessRemote("undeploy", filename) != null;
	}

	private URL[] getUrls(String directory) {
		List<URL> urlList = new ArrayList<URL>();
		File openEjbDir = new File(directory + File.separator + "lib");
		File[] files = openEjbDir.listFiles();
		
		for (File file : files) {
			if (file.getName().endsWith(".jar")) {
				try {
					urlList.add(file.getAbsoluteFile().toURL());
				} catch (MalformedURLException e) {
				}
			}
		}

		return urlList.toArray(new URL[urlList.size()]);
	}
}
