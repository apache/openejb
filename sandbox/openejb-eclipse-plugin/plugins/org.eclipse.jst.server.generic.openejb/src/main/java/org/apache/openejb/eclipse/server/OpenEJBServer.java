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

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jst.server.core.FacetUtil;
import org.eclipse.jst.server.core.IEnterpriseApplication;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.model.ServerDelegate;

public class OpenEJBServer extends ServerDelegate {

	@Override
	public IStatus canModifyModules(IModule[] add, IModule[] remove) {
		if (add != null) {
			for (int i = 0; i < add.length; i++) {
				if (!isSupportedModule(add[i])) {
					return new Status(IStatus.ERROR, "org.eclipse.jst.generic.openejb", "Module not compatible", null);
				}
				if (add[i].getProject() != null) {
					IStatus status = FacetUtil.verifyFacets(add[i].getProject(), getServer());
					if (status != null && !status.isOK())
						return status;
				}
			}
		}
		return Status.OK_STATUS;
	}

	private boolean isSupportedModule(IModule module) {
		return (module.getModuleType() != null 
				&& ("jst.ejb".equals(module.getModuleType().getId())
						|| "jst.ear".equals(module.getModuleType().getId())));
	
	}

	@Override
	public IModule[] getChildModules(IModule[] module) {
		return new IModule[0];
	}

	@Override
	public IModule[] getRootModules(IModule module) throws CoreException {
		if (!isSupportedModule(module))
			return null;
		IStatus status = canModifyModules(new IModule[] { module }, null);
		if (status != null && !status.isOK())
			throw new CoreException(status);

		IModule[] parents = doGetParentModules(module);
		if (parents.length > 0)
			return parents;
		return new IModule[] { module };
	}

	@SuppressWarnings("unchecked")
	public IModule[] doGetParentModules(IModule module) {
		IModule[] ears = ServerUtil.getModules("jst.ear"); //$NON-NLS-1$
		ArrayList list = new ArrayList();
		for (int i = 0; i < ears.length; i++) {
			IEnterpriseApplication ear = (IEnterpriseApplication) ears[i].loadAdapter(IEnterpriseApplication.class, null);
			IModule[] childs = ear.getModules();
			for (int j = 0; j < childs.length; j++) {
				if (childs[j].equals(module))
					list.add(ears[i]);
			}
		}
		return (IModule[]) list.toArray(new IModule[list.size()]);
	}

	@Override
	public void modifyModules(IModule[] add, IModule[] remove, IProgressMonitor monitor) throws CoreException {
	}

	public void setEJBPort(String port) {
		setAttribute("org.apache.openejb.eclipse.server.ejbport", port);
	}
	
	public String getEJBPort() {
		return getAttribute("org.apache.openejb.eclipse.server.ejbport", "4201");
	}

	public void setHTTPEJBPort(String port) {
		setAttribute("org.apache.openejb.eclipse.server.httpejbport", port);
	}
	
	public String getHTTPEJBPort() {
		return getAttribute("org.apache.openejb.eclipse.server.httpejbport", "4204");
	}

	public void setHSQLPort(String port) {
		setAttribute("org.apache.openejb.eclipse.server.hsqlport", port);
	}
	
	public String getHSQLPort() {
		return getAttribute("org.apache.openejb.eclipse.server.hsqlport", "9001");
	}

	public void setTelnetPort(String port) {
		setAttribute("org.apache.openejb.eclipse.server.telnetport", port);
	}
	
	public String getTelnetPort() {
		return getAttribute("org.apache.openejb.eclipse.server.telnetport", "4202");
	}

	public void setAdminPort(String port) {
		setAttribute("org.apache.openejb.eclipse.server.adminport", port);
	}
	
	public String getAdminPort() {
		return getAttribute("org.apache.openejb.eclipse.server.adminport", "4200");
	}

	public void setConfigFile(String configFile) {
		setAttribute("org.apache.openejb.eclipse.server.configfile", configFile);
	}
	
	public String getConfigFile() {
		return getAttribute("org.apache.openejb.eclipse.server.configfile", "");
	}

}
