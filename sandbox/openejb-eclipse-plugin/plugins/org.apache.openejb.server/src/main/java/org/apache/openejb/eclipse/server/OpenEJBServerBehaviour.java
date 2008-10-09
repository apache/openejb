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
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jst.j2ee.application.internal.operations.EARComponentExportDataModelProvider;
import org.eclipse.jst.j2ee.ejb.datamodel.properties.IEJBComponentExportDataModelProperties;
import org.eclipse.jst.j2ee.internal.ejb.project.operations.EJBComponentExportDataModelProvider;
import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;

public class OpenEJBServerBehaviour extends ServerBehaviourDelegate {

	@SuppressWarnings("serial")
	public class ServerStoppedException extends Exception {
	}

	private class ServerMonitor extends Thread {

		private static final int ONE_SECOND = 1000;
		private boolean running = false;
		private int adminPort = getAdminPort();

		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(ONE_SECOND);
				} catch (Exception e) {
				}

				try {
					check();
				} catch (ServerStoppedException e) {
					// break out the loop and stop monitoring
					// a restart will start a new monitor
					break;
				}
			}
		}

		private void check() throws ServerStoppedException {
			// connect to admin interface
			try {
				Socket socket = new Socket("localhost", adminPort);
				socket.close();
				
				// update the server status if this is first time we've connected
				if (! running) {
					running = true;
					setState(IServer.STATE_STARTED);
					
					// republish everything
					doFullPublish();
				}
			} catch (IOException e) {
				if (running) {
					// looks like server has started successfully, but has died
					setServerState(IServer.STATE_STOPPED);
					running = false;
					cleanup();
					throw new ServerStoppedException();
				}
				// server might not be started yet
			}
			// if success, server is running
		}
		
		public void terminate() {
			this.interrupt();
		}

		public ServerMonitor() {
			super();
		}
	}
	
	private ServerMonitor monitor;
	private Map<IModule, String> publishedModules = new HashMap<IModule, String>();

	private int getAdminPort() {
		try {
			OpenEJBServer openEJBServer = (OpenEJBServer) (getServer().getAdapter(OpenEJBServer.class));
			return Integer.parseInt(openEJBServer.getAdminPort());
		} catch (NumberFormatException e) {
			return 4200;
		}
	}

	private int getEJBDPort() {
		try {
			OpenEJBServer openEJBServer = (OpenEJBServer) (getServer().getAdapter(OpenEJBServer.class));
			return Integer.parseInt(openEJBServer.getEJBPort());
		} catch (NumberFormatException e) {
			return 4201;
		}
	}
	
	@Override
	public void stop(boolean force) {
		stopServer();
	}
	
	/*
	 * @see org.apache.openejb.server.admin.AdminDaemon.service(Socket socket)
	 */
	private void stopServer() {
		// connect to admin interface, and send 'Q' to stop the server
		try {
			Socket socket = new Socket("localhost", getAdminPort());
			socket.getOutputStream().write('Q');
			socket.close();
			
			setState(IServer.STATE_STOPPING);
		} catch (IOException e) {
			// we're really stuck
		}
	}


	@Override
	public void setupLaunchConfiguration(ILaunchConfigurationWorkingCopy workingCopy, IProgressMonitor monitor) throws CoreException {
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "org.apache.openejb.cli.Bootstrap");

		OpenEJBRuntimeDelegate runtime = getRuntimeDelegate();
		OpenEJBServer openejbServer = (OpenEJBServer) (getServer().getAdapter(OpenEJBServer.class));
		
		IVMInstall vmInstall = runtime.getVMInstall();
		if (vmInstall != null)
			workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_JRE_CONTAINER_PATH, JavaRuntime.newJREContainerPath(vmInstall).toPortableString());

		String args = " start";
		if (openejbServer.getConfigFile() != null && openejbServer.getConfigFile().length() > 0) {
			args += " --conf=\"" + openejbServer.getConfigFile() + "\"";
		}
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, args);
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "-Dejbd.port=" + openejbServer.getEJBPort() + 
				" -Dhttpejbd.port=" + openejbServer.getHTTPEJBPort() +
				" -Dhsql.port=" + openejbServer.getHSQLPort() + 
				" -Dtelnet.port=" + openejbServer.getTelnetPort() + 
				" -Dadmin.port=" + openejbServer.getAdminPort() + 
				" -Dopenejb.home=\"" +  getServer().getRuntime().getLocation().toString() + 
				"\" -javaagent:\"" + runtime.getJavaAgent() + "\"");
		
		List<IRuntimeClasspathEntry> cp = new ArrayList<IRuntimeClasspathEntry>();
		IPath serverJar = new Path(runtime.getCore());
		cp.add(JavaRuntime.newArchiveRuntimeClasspathEntry(serverJar));
		
		List<String> classPath = new ArrayList<String>();
		for (IRuntimeClasspathEntry entry : cp) {
			classPath.add(entry.getMemento());
		}
		
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, classPath);
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);
	}

	private OpenEJBRuntimeDelegate getRuntimeDelegate() {
		OpenEJBRuntimeDelegate rd = (OpenEJBRuntimeDelegate) getServer().getRuntime().getAdapter(OpenEJBRuntimeDelegate.class);
		if (rd == null)
			rd = (OpenEJBRuntimeDelegate) getServer().getRuntime().loadAdapter(OpenEJBRuntimeDelegate.class, new NullProgressMonitor());
		return rd;
	}

	public void setState(int state) {
		setServerState(state);
	}
	
	public void start(ILaunch launch) {
		monitor = new ServerMonitor();
		monitor.start();
	}

	private void doFullPublish() {
		Iterator<IModule> iterator = publishedModules.keySet().iterator();
		while (iterator.hasNext()) {
			IModule module = (IModule) iterator.next();
			doPublish(module, ADDED);
		}
		
		if (IServer.STATE_STARTED == getServer().getServerState()) {
			setServerPublishState(IServer.PUBLISH_STATE_NONE);
		}
	}
	
	private void cleanup() {
		Iterator<IModule> iterator = publishedModules.keySet().iterator();
		while (iterator.hasNext()) {
			IModule module = (IModule) iterator.next();
			String publishedFile = publishedModules.get(module);
			if (publishedFile != null) {
				new File(publishedFile).delete();
			}
		}
		
		publishedModules.clear();
		setServerPublishState(IServer.PUBLISH_STATE_FULL);
	}
	
	private void doPublish(IModule module, int kind) {
		ServerDeployer serverDeployer = new ServerDeployer(getRuntimeDelegate().getRuntime().getLocation().toFile().getAbsolutePath(), getEJBDPort());
		
		// if module already published, try an undeploy first, and cleanup temp file
		String jarFile = publishedModules.get(module);
		if (jarFile != null) {
			serverDeployer.undeploy(jarFile);
			new File(jarFile).delete();
			publishedModules.remove(module);
		}
		
		if (kind == REMOVED) {
			return;
		}
		
		// now do the export
		String newJarFile = exportModule(module);
		
		// publish the new export
		if (newJarFile != null) {
			String path = serverDeployer.deploy(newJarFile);
			publishedModules.put(module, path);
//			setModulePublishState(new IModule[] { module }, IServer.PUBLISH_STATE_NONE);
		}
	}
	
	@Override
	protected IStatus publishModule(int kind, IModule[] modules, int deltaKind, IProgressMonitor monitor) {
		if (IServer.STATE_STARTED != getServer().getServerState()) {
			for (IModule module : modules) {
				if (deltaKind == REMOVED) {
					String jarFile = publishedModules.get(module);
					if (jarFile != null) {
						new File(jarFile).delete();
					}
					
					publishedModules.remove(module);
				} else {
					publishedModules.put(module, null);
				}
			}
		} else {
			for (IModule module : modules) {
				doPublish(module, deltaKind);
			}
		}
		
		return super.publishModule(kind, modules, deltaKind, monitor);
	}

	protected String exportModule(IModule module) {
		IDataModel model;
		File tempJarFile;
		
		try {
			if ("jst.ear".equals(module.getModuleType().getId())) {
				model = DataModelFactory.createDataModel(new EARComponentExportDataModelProvider());
				tempJarFile = File.createTempFile("oejb", ".ear");
			} else {
				model = DataModelFactory.createDataModel(new EJBComponentExportDataModelProvider());
				tempJarFile = File.createTempFile("oejb", ".jar");
			}

			model.setProperty(IEJBComponentExportDataModelProperties.PROJECT_NAME, module.getProject().getName());
			model.setProperty(IEJBComponentExportDataModelProperties.ARCHIVE_DESTINATION, tempJarFile.getAbsolutePath());
			model.getDefaultOperation().execute(null, null);

			return tempJarFile.getAbsolutePath();
		} catch (Exception e) {
			return null;
		}
	}
}
