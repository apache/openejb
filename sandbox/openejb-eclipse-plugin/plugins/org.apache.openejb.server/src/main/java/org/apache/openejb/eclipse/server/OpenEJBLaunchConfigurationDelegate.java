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
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.ExecutionArguments;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.eclipse.jst.server.core.ServerProfilerDelegate;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.eclipse.wst.server.core.util.SocketUtil;

public class OpenEJBLaunchConfigurationDelegate extends AbstractJavaLaunchConfigurationDelegate {

	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		IServer server = ServerUtil.getServer(configuration);
		if (server == null) {
			abort("Missing server", null,
					IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		}
		OpenEJBServerBehaviour genericServer = (OpenEJBServerBehaviour) server.loadAdapter(ServerBehaviourDelegate.class, null);

		try {
//			genericServer.setupLaunch(launch, mode, monitor);
			if(genericServer.getServer().getServerType().supportsRemoteHosts() && !SocketUtil.isLocalhost(genericServer.getServer().getHost())){
			// no launch for remote servers
				return;
			}

			String mainTypeName = "org.apache.openejb.cli.Bootstrap";
			IVMInstall vm = verifyVMInstall(configuration);
			IVMRunner runner = vm.getVMRunner(mode);
			
			if(runner == null && ILaunchManager.PROFILE_MODE.equals(mode)){
				runner = vm.getVMRunner(ILaunchManager.RUN_MODE);
			}
			if(runner == null){
				throw new RuntimeException();
			}
			
			File workingDir = verifyWorkingDirectory(configuration);
			String workingDirName = null;
			if (workingDir != null)
				workingDirName = workingDir.getAbsolutePath();

			// Program & VM args
			String pgmArgs = getProgramArguments(configuration);
			String vmArgs = getVMArguments(configuration);
			String[] envp = getEnvironment(configuration);
			
			ExecutionArguments execArgs = new ExecutionArguments(vmArgs, pgmArgs);

			// VM-specific attributes
			Map vmAttributesMap = getVMSpecificAttributesMap(configuration);

			// Classpath
			String[] classpath = getClasspath(configuration);

			// Create VM config
			VMRunnerConfiguration runConfig = new VMRunnerConfiguration(
					mainTypeName, classpath);
			runConfig.setProgramArguments(execArgs.getProgramArgumentsArray());
			runConfig.setVMArguments(execArgs.getVMArgumentsArray());
			runConfig.setWorkingDirectory(workingDirName);
			runConfig.setEnvironment(envp);
			runConfig.setVMSpecificAttributesMap(vmAttributesMap);
			
			// Bootpath
			String[] bootpath = getBootpath(configuration);
			if (bootpath != null && bootpath.length > 0)
				runConfig.setBootClassPath(bootpath);
			
			setDefaultSourceLocator(launch, configuration);
			
			if (ILaunchManager.PROFILE_MODE.equals(mode)) {
				try {
					ServerProfilerDelegate.configureProfiling(launch, vm, runConfig, monitor);
				} catch (CoreException ce) {
//					genericServer.stopImpl();
					throw ce;
				}
			}
			
			// Launch the configuration
//			genericServer.startPingThread();
			genericServer.setState(IServer.STATE_STARTING);
			genericServer.start(launch);
			runner.run(runConfig, launch, monitor);
//			genericServer.setProcess(launch.getProcesses()[0]);
		} catch (CoreException e) {
//			genericServer.terminate();
			throw e;
		}

	}
}
