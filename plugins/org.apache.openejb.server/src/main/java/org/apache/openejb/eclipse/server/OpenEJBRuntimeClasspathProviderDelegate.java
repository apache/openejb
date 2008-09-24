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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jst.server.core.RuntimeClasspathProviderDelegate;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;

public class OpenEJBRuntimeClasspathProviderDelegate extends RuntimeClasspathProviderDelegate {

	protected OpenEJBRuntimeDelegate getRuntimeDelegate(IRuntime runtime) {
		IRuntimeWorkingCopy wc = runtime.createWorkingCopy();
		
		return (OpenEJBRuntimeDelegate) wc.loadAdapter(OpenEJBRuntimeDelegate.class, new NullProgressMonitor());
	}
	
	@Override
	public IClasspathEntry[] resolveClasspathContainer(IProject project, IRuntime runtime) {
		return resolveClasspathContainer(runtime);
	}

	@Override
	public IClasspathEntry[] resolveClasspathContainer(IRuntime runtime) {
		IPath installPath = runtime.getLocation();
		boolean ejb31JarIncluded = getRuntimeDelegate(runtime).isEjb31JarIncluded();
		if (installPath == null)
			return new IClasspathEntry[0];
		
		List<IClasspathEntry> list = getClientJars(installPath, ejb31JarIncluded);
		return (IClasspathEntry[])list.toArray(new IClasspathEntry[0]);
	}

	@Override
	public IClasspathEntry[] resolveClasspathContainerImpl(IProject project, IRuntime runtime) {
		return resolveClasspathContainer(runtime);
	}

	@Override
	public IClasspathEntry[] resolveClasspathContainerImpl(IRuntime runtime) {
		return resolveClasspathContainer(runtime);
	}

	private List<IClasspathEntry> getClientJars(IPath installPath, boolean includeEjb31Jar) {
		File libFolder = new File(installPath.toString() + File.separator + "lib");
		if (! libFolder.exists()) {
			return null;
		}
		
		List<IClasspathEntry> classpathEntries = new ArrayList<IClasspathEntry>();
		File[] files = libFolder.listFiles();
		
		for (File file : files) {
			if ((file.getName().startsWith("javaee-api") && file.getName().endsWith(".jar"))
					|| (file.getName().startsWith("openejb-client") && file.getName().endsWith(".jar"))
					|| (includeEjb31Jar && file.getName().startsWith("ejb31-api-experimental") && file.getName().endsWith(".jar"))) {
				Path jar = new Path(file.getAbsolutePath());
				classpathEntries.add(JavaCore.newLibraryEntry(jar, null, null));
			}
		}
		
		return classpathEntries;
	}
}
