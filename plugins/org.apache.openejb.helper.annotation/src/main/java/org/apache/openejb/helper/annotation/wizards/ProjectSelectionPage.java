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
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

public class ProjectSelectionPage extends UserInputWizardPage {

	private final EJBMigrationRefactoring refactoring;
	private final IProject[] projects;
	private List list;

	public ProjectSelectionPage(EJBMigrationRefactoring refactoring) {
		super("wizardPage");
		this.refactoring = refactoring;
		projects = refactoring.getWorkspaceRoot().getProjects();
		
		setTitle("EJB 3.0 Annotation Wizard");
		setDescription("Please select the project you'd like to work on");
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
			setErrorMessage("Please select a project");
			setPageComplete(false);
			return;
		} 
		
		refactoring.setProject(getSelectedProject());
		setErrorMessage(null);
		setPageComplete(true);
	}
}
