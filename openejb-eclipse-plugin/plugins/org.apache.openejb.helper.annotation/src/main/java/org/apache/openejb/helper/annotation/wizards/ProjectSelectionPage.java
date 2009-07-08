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
package org.apache.openejb.helper.annotation.wizards;

import org.eclipse.core.resources.IProject;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.PlatformUI;

public class ProjectSelectionPage extends UserInputWizardPage {

	private final EJBMigrationRefactoring refactoring;
	private final IProject[] projects;
	private List list;

	@Override
	public void performHelp() {
		PlatformUI.getWorkbench().getHelpSystem().displayHelp(
				"org.apache.openejb.help.select-project");
	}

	public ProjectSelectionPage(EJBMigrationRefactoring refactoring) {
		super(Messages.getString("org.apache.openejb.helper.annotation.wizards.projectSelectionWzd.pageName")); //$NON-NLS-1$
		this.refactoring = refactoring;
		projects = refactoring.getWorkspaceRoot().getProjects();
		
		setTitle(Messages.getString("org.apache.openejb.helper.annotation.wizards.projectSelectionWzd.pageTitle")); //$NON-NLS-1$
		setDescription(Messages.getString("org.apache.openejb.helper.annotation.wizards.projectSelectionWzd.pageDescription")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		
		GridData gridData = new GridData();
		gridData.minimumWidth = 200;
		gridData.minimumHeight = 200;
		
		list = new List(composite, SWT.NONE);
		list.setLayoutData(gridData);
		list.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				checkPage();
			}

			public void widgetSelected(SelectionEvent e) {
				checkPage();
			}
		});

		int initialSelection = -1;
		
		for (int i = 0; i < projects.length; i++) {
			IProject project = projects[i];
			
			if (refactoring.getProject() != null && project.equals(refactoring.getProject())) {
				initialSelection = i;
			}
			
			list.add(project.getName());
		}
		
		if (initialSelection != -1) {
			list.setSelection(initialSelection);
		}
		
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, "org.apache.openejb.help.select-project");
		
		setControl(composite);
		checkPage();
	}
	
	public IProject getSelectedProject() {
		if (list.getSelectionCount() == 1) {
			return projects[list.getSelectionIndex()];
		}
		
		return null;
	}

	protected void checkPage() {
		if (getSelectedProject() == null) {
			setErrorMessage(Messages.getString("org.apache.openejb.helper.annotation.wizards.projectSelectionWzd.pageErrorMsg.1")); //$NON-NLS-1$
			setPageComplete(false);
			return;
		} 
		
		refactoring.setProject(getSelectedProject());
		setErrorMessage(null);
		setPageComplete(true);
	}
}
