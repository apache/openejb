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
import java.net.URL;
import java.util.Properties;

import org.apache.openejb.OpenEJBException;
import org.eclipse.core.runtime.FileLocator;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class OpenEjbDeploymentService {

	private ServiceTracker deploymentTracker;
	private BundleContext bundleContext;
	private OpenEjbServer ejbServer;

	public OpenEjbDeploymentService(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
		this.deploymentTracker = null;
		this.ejbServer = null;
	}

	public void open() {

		try {
			if (ejbServer == null) {
				Bundle bundle = bundleContext.getBundle();
				URL baseUrl = bundle.getEntry("/");
				baseUrl = FileLocator.toFileURL(baseUrl);
				File baseDir = new File(baseUrl.getPath());

				ejbServer = new OpenEjbServer();
				ejbServer.init(baseDir);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		deploymentTracker = new ServiceTracker(bundleContext, OpenEjbApplication.class.getName(), null);
		deploymentTracker.open();

		ServiceListener sl = new ServiceListener() {

			public void serviceChanged(ServiceEvent ev) {
				ServiceReference sr = ev.getServiceReference();
				switch (ev.getType()) {

				case ServiceEvent.REGISTERED:
					deployApplication(sr);
					break;

				case ServiceEvent.UNREGISTERING:
					undeployApplication(sr);
					break;
				}
			}
		};

		// Generate a ServiceEvent for any existing OpenEJbApplication services.

		String filter = "(objectclass=" + OpenEjbApplication.class.getName() + ")";
		try {
			bundleContext.addServiceListener(sl, filter);
			ServiceReference[] srl = bundleContext.getServiceReferences(null, filter);
			if (srl != null) {
				for (ServiceReference sr : srl) {
					sl.serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED, sr));
				}
			}
		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
		}
	}

	public void close() throws Exception {
		deploymentTracker.close();
		ejbServer.destroy();
	}

	private void deployApplication(ServiceReference sr) {
		OpenEjbApplication application = (OpenEjbApplication) bundleContext.getService(sr);
		Properties properties = new Properties();
		for (String key : sr.getPropertyKeys()) {
			Object value = sr.getProperty(key);
			if (value instanceof String) {
				properties.put(key, value);
			}
		}
		try {
			application.deploy(properties);
		} catch (OpenEJBException e) {
			e.printStackTrace();
		}
	}

	private void undeployApplication(ServiceReference sr) {
		try {
			OpenEjbApplication application = (OpenEjbApplication) bundleContext.getService(sr);
			application.undeploy();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
