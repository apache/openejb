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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jst.server.core.IJavaRuntime;
import org.eclipse.wst.server.core.model.RuntimeDelegate;

public class OpenEJBRuntimeDelegate extends RuntimeDelegate implements IJavaRuntime {
	private static final String INCLUDE_EJB31_JARS = "INCLUDE_EJB31_JARS";
	private static final String PROP_VM_INSTALL_TYPE_ID = "vm-install-type-id"; //$NON-NLS-1$
	private static final String PROP_VM_INSTALL_ID = "vm-install-id"; //$NON-NLS-1$

	@Override
	public IStatus validate() {

		if (this.getRuntimeWorkingCopy() == null) {
			return new Status(IStatus.ERROR, "org.eclipse.jst.generic.openejb", "");
		}
		
		if (this.getRuntimeWorkingCopy().getLocation() == null) {
			return new Status(IStatus.ERROR, "org.eclipse.jst.generic.openejb", "");
		}
		
		File libFolder = new File(this.getRuntimeWorkingCopy().getLocation().toString() + File.separator + "lib");
		if (! (libFolder.exists() && libFolder.isDirectory())) {
			return new Status(IStatus.ERROR, "org.eclipse.jst.generic.openejb", "Invalid lib folder");
		}

		File coreJar = getFileWithPrefix("openejb-core", ".jar");
		File agentJar = getFileWithPrefix("openejb-javaagent", ".jar");
		
		
		if (coreJar == null) {
			return new Status(IStatus.ERROR, "org.eclipse.jst.generic.openejb", "Unable to find openejb-core jar");
		}
		
		if (agentJar == null) {
			return new Status(IStatus.ERROR, "org.eclipse.jst.generic.openejb", "Unable to find openejb-javaagent jar");
		}
		
		return super.validate();
	}

	private File getFileWithPrefix(String prefix, String suffix) {
		File libFolder = new File(this.getRuntime().getLocation().toString() + File.separator + "lib");
		if (! (libFolder.exists() && libFolder.isDirectory())) {
			return null;
		}

		File[] files = libFolder.listFiles();

		for (File file : files) {
			if (! file.getName().endsWith(suffix)) {
				continue;
			}
			
			if (file.getName().startsWith(prefix)) {
				return file;
			}
		}

		return null;
	}
	
	
	
	public String getJavaAgent() {
		return getFileWithPrefix("openejb-javaagent", ".jar").getAbsolutePath();
	}

	public String getCore() {
		return getFileWithPrefix("openejb-core", ".jar").getAbsolutePath();
	}

	public IVMInstall getVMInstall() {
		if (getVMInstallTypeId() == null)
			return JavaRuntime.getDefaultVMInstall();
		try {
			IVMInstallType vmInstallType = JavaRuntime.getVMInstallType(getVMInstallTypeId());
			IVMInstall[] vmInstalls = vmInstallType.getVMInstalls();
			int size = vmInstalls.length;
			String id = getVMInstallId();
			for (int i = 0; i < size; i++) {
				if (id.equals(vmInstalls[i].getId()))
					return vmInstalls[i];
			}
		} catch (Exception e) {
			// ignore
		}
		return null;
		}

	/**
	 * Returns the vm type id
	 * @return id
	 */
	public String getVMInstallTypeId() {
		return getAttribute(PROP_VM_INSTALL_TYPE_ID, (String)null);
	}
	
	/**
	 * Is use default VM selected
	 * @return boolean
	 */
	public boolean isUsingDefaultJRE() {
		return getVMInstallTypeId() == null;
	}


	/**
	 * Returns VM id
	 * @return id
	 */
	public String getVMInstallId() {
		return getAttribute(PROP_VM_INSTALL_ID, (String)null);
	}
}
