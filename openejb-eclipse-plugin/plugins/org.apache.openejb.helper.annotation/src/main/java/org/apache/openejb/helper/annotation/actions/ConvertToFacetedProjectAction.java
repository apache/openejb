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

package org.apache.openejb.helper.annotation.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class ConvertToFacetedProjectAction implements IObjectActionDelegate, IWorkbenchWindowActionDelegate{

	private static final String FACET_NATURE = "org.eclipse.wst.common.project.facet.core.nature";
	protected IProject project;

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	public void run(IAction action) {
		if (project == null) {
			return;
		}

		try {
			IProjectDescription projectDescription = project.getDescription();
			String[] natureIds = projectDescription.getNatureIds();
			for (String natureId : natureIds) {
				if (FACET_NATURE.equals(natureId)) {
					return;
				}
			}
			
			String[] newNatureIds = new String[natureIds.length + 1];
			
			System.arraycopy(natureIds, 0, newNatureIds, 0, natureIds.length);
			newNatureIds[natureIds.length] = FACET_NATURE;
			projectDescription.setNatureIds(newNatureIds);
			project.setDescription(projectDescription, new NullProgressMonitor());
		} catch (CoreException e) {
		}
	}


	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			Object firstElement = ((IStructuredSelection) selection).getFirstElement();
			
			if (firstElement instanceof IProject) {
				project = (IProject) firstElement;
			}
		}
		
	}

	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub
		
	}

}
