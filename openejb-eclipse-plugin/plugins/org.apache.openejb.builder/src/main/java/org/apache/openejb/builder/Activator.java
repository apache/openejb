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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin {

	public static final String OPENEJB_NATURE = "org.apache.openejb.builder.nature";
	public static final String OPENEJB_BUILDER = "org.apache.openejb.builder";
	private static Activator instance;
	
	public Activator() {
		instance = this;
	}

	public void addOpenEJBNature(IProject project) {
		try {
			if (project.hasNature(OPENEJB_NATURE)) {
				return;
			}

			IProjectDescription description = project.getDescription();
			String[] currentNatures = description.getNatureIds();
			String[] newNatures = new String[currentNatures.length + 1];
			System.arraycopy(currentNatures, 0, newNatures, 0, currentNatures.length);
			newNatures[currentNatures.length] = OPENEJB_NATURE;
			description.setNatureIds(newNatures);
			project.setDescription(description, null);

		} catch (CoreException e) {
		}
	}

	public static Activator getPlugin() {
		return instance;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		instance = null;
		super.stop(context);
	}

	public boolean projectHasOpenEJBNature(IProject project) {
		try {
			return project.hasNature(OPENEJB_NATURE);
		} catch (CoreException e) {
			return false;
		}
	}

	public void removeOpenEJBNature(IProject project) {
		try {
			if (! project.hasNature(OPENEJB_NATURE)) {
				return;
			}
			
			IProjectDescription description = project.getDescription();
			String[] currentNatures = description.getNatureIds();
			List<String> newNatures = new ArrayList<String>();
			
			for (String nature : currentNatures) {
				if (! nature.equals(OPENEJB_NATURE)) {
					newNatures.add(nature);
				}
			}
			
			description.setNatureIds(newNatures.toArray(new String[newNatures.size()]));
			project.setDescription(description, null);
			
		} catch (CoreException e) {
		}
	}

	public Model getModel() {
		return new Model();
	}
}
