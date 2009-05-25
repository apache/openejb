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

package org.apache.openejb.builder;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

public class ProjectNature implements IProjectNature {

	private IProject project;

	public void configure() throws CoreException {
		IProjectDescription description = project.getDescription();
		ICommand[] commands = description.getBuildSpec();
		
		for (ICommand command : commands) {
			if (command.getBuilderName().equals(Activator.OPENEJB_BUILDER)) {
				return;
			}
		}
		
		ICommand[] newCommands = new ICommand[commands.length + 1];
		System.arraycopy(commands, 0, newCommands, 0, commands.length);
		
		ICommand newBuilder = description.newCommand();
		newBuilder.setBuilderName(Activator.OPENEJB_BUILDER);
		newCommands[commands.length] = newBuilder;
		description.setBuildSpec(newCommands);
		project.setDescription(description, null);
	}

	public void deconfigure() throws CoreException {
		IProjectDescription description = project.getDescription();
		ICommand[] commands = description.getBuildSpec();

		List<ICommand> newCommands = new ArrayList<ICommand>();
		for (ICommand command : commands) {
			if (! command.getBuilderName().equals(Activator.OPENEJB_BUILDER)) {
				newCommands.add(command);
			}
		}
		
		description.setBuildSpec(newCommands.toArray(new ICommand[newCommands.size()]));
		project.setDescription(description, null);
	}

	public IProject getProject() {
		return project;
	}

	public void setProject(IProject project) {
		this.project = project;

	}

}
