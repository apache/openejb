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

import java.util.List;

import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.EjbModule;

import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.SessionBean;

public class SessionBeanInterfaceModifier implements Converter {

	private IJDTFacade facade;
	private boolean useHome;

	public SessionBeanInterfaceModifier(IJDTFacade facade) {
		super();
		this.facade = facade;
	}
	
	public boolean isUseHome() {
		return useHome;
	}

	public void setUseHome(boolean useHome) {
		this.useHome = useHome;
	}

	public void convert(AppModule module) {
		List<EjbModule> ejbModules = module.getEjbModules();
		for (EjbModule ejbModule : ejbModules) {
			EjbJar ejbJar = ejbModule.getEjbJar();
			EnterpriseBean[] enterpriseBeans = ejbJar.getEnterpriseBeans();

			for (EnterpriseBean enterpriseBean : enterpriseBeans) {
				if (!(enterpriseBean instanceof SessionBean)) {
					continue;
				}

				SessionBean sessionBean = (SessionBean) enterpriseBean;
				String ejbClass = sessionBean.getEjbClass();

                if (ejbClass == null || ejbClass.length() == 0) continue;

                facade.removeInterface(ejbClass, "javax.ejb.SessionBean"); //$NON-NLS-1$

                if (! useHome) {
					String remoteInterface = sessionBean.getRemote();
					if (remoteInterface != null && remoteInterface.length() > 0) {
						facade.addInterface(ejbClass, remoteInterface);
						facade.removeInterface(remoteInterface, "javax.ejb.EJBObject"); //$NON-NLS-1$
					}

					String localInterface = sessionBean.getLocal();
					if (localInterface != null && localInterface.length() > 0) {
						facade.addInterface(ejbClass, localInterface);
						facade.removeInterface(localInterface, "javax.ejb.EJBLocalObject"); //$NON-NLS-1$
					}
				}
			}
		}
	}

}
