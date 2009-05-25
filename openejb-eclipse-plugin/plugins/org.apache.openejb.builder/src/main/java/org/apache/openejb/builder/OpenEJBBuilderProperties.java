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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

public class OpenEJBBuilderProperties extends PropertyPage implements IWorkbenchPropertyPage {

	protected Button builderEnabled;

	public OpenEJBBuilderProperties() {
	}

	@Override
	protected Control createContents(Composite parent) {
		builderEnabled = new Button(parent, SWT.CHECK);
		builderEnabled.setText("Check EJB 3.1 Dependencies");

		builderEnabled.setSelection(Activator.getPlugin().projectHasOpenEJBNature(getProject()));
		return builderEnabled;
	}

	@Override
	public boolean performOk() {
		if (builderEnabled.getSelection()) {
			Activator.getPlugin().addOpenEJBNature(getProject());
		} else {
			try {
				getProject().deleteMarkers("org.apache.openejb.builder.dependsonMarker", true, IResource.DEPTH_INFINITE);
			} catch (CoreException e) {
			}
			
			Activator.getPlugin().removeOpenEJBNature(getProject());
		}
		
		return true;
	}

	protected IProject getProject() {
		return (IProject) getElement();
	}
}
