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

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class EJBJarSelectionPage extends UserInputWizardPage {

	private static final Status SELECTION_NOT_OK_STATUS = new Status(4, "org.apache.openejb.helper.annotation", 4, "", null);
	private static final Status SELECTION_OK_STATUS = new Status(0, "org.eclipse.core.runtime", 0, "", null);

	private final EJBMigrationRefactoring refactoring;
	private Text ejbJarXmlText;
	private Text openEjbJarXmlText;
	
	public EJBJarSelectionPage(EJBMigrationRefactoring refactoring) {
		super("wizardPage");
		setTitle("EJB 3.0 Annotation Wizard");
		setDescription("This wizard analyzes ejb-jar.xml and openejb-jar.xml and adds EJB 3.0 annotations to your source");
		this.refactoring = refactoring;
	}

	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridData textData = new GridData();
		textData.widthHint = 200;
		
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		container.setLayout(gridLayout);
		
		Label ejbJarXmlLabel = new Label(container, SWT.NONE);
		ejbJarXmlLabel.setText("ejb-jar.xml location");

		ejbJarXmlText = new Text(container, SWT.BORDER);
		ejbJarXmlText.setText(refactoring.getEjbJarXmlFile());
		ejbJarXmlText.setLayoutData(textData);
		ejbJarXmlText.addModifyListener(new org.eclipse.swt.events.ModifyListener() {
			public void modifyText(org.eclipse.swt.events.ModifyEvent e) {
				checkPage();
			}
		});
		
		Button browseEjbJarButton = new Button(container, SWT.NONE);
		browseEjbJarButton.setText("Browse");
		browseEjbJarButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				String filename = handleBrowse("Select ejb-jar.xml file");
				if (filename != null && filename.length() > 0) {
					ejbJarXmlText.setText(filename);
					checkPage();
				}
			}
		});
		
		Label openEjbJarXmlLabel = new Label(container, SWT.NONE);
		openEjbJarXmlLabel.setText("openejb-jar.xml location (optional)");
		
		openEjbJarXmlText = new Text(container, SWT.BORDER);
		openEjbJarXmlText.setText(refactoring.getOpenEjbJarXmlFile());
		openEjbJarXmlText.setLayoutData(textData);
		openEjbJarXmlText.addModifyListener(new org.eclipse.swt.events.ModifyListener() {
			public void modifyText(org.eclipse.swt.events.ModifyEvent e) {
				checkPage();
			}
		});

		Button browseOpenEjbJarXmlButton = new Button(container, SWT.NONE);
		browseOpenEjbJarXmlButton.setText("Browse");
		browseOpenEjbJarXmlButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				String filename = handleBrowse("Select openejb-jar.xml file");
				if (filename != null && filename.length() > 0) {
					openEjbJarXmlText.setText(filename);
					checkPage();
				}
			}
		});

		
		setControl(container);
		checkPage();
	}
	
	protected String handleBrowse(String title) {
		ElementTreeSelectionDialog etsd = new ElementTreeSelectionDialog(getShell(), new WorkbenchLabelProvider(), new WorkbenchContentProvider());
		etsd.setInput(refactoring.getProject().getWorkspace());
		etsd.addFilter(new ViewerFilter() {

			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				
				if (element instanceof IFile) {
					return ((IFile)element).getFileExtension().equals("xml");
				}
				
				if (element instanceof IProject) {
					return ((IProject)element).equals(refactoring.getProject()); 
				}
				
				return true;
			}
			
		});
		
		etsd.setValidator(new ISelectionStatusValidator() {
			public IStatus validate(Object[] selection) {
				return (selection.length == 1 && selection[0] instanceof IFile) ? SELECTION_OK_STATUS : SELECTION_NOT_OK_STATUS;
			}
		});
		
		etsd.open();
		
		Object[] results = etsd.getResult();
		if (results.length == 1) {
			if (results[0] instanceof IFile) {
				return ((IFile)results[0]).getProjectRelativePath().toPortableString();
			}
		}
		
		return null;
	}

	protected void checkPage() {
		String ejbJarXmlFile = ejbJarXmlText.getText();
		String openEjbJarXmlFile = openEjbJarXmlText.getText();
		
		refactoring.setEjbJarXmlFile(ejbJarXmlFile);
		refactoring.setOpenEjbJarXmlFile(openEjbJarXmlFile);
		
		if (refactoring.getProject() == null) {
			setErrorMessage(null);
			setPageComplete(true);
			return;
		}
		
		if (! new File(refactoring.getProject().getLocation().toPortableString() + File.separator + ejbJarXmlFile).exists()) {
			setErrorMessage("Please select an ejb-jar.xml file");
			setPageComplete(false);
			return;
		} 
		
		if (openEjbJarXmlFile  != null && openEjbJarXmlFile.length() > 0) {
			if (! new File(refactoring.getProject().getLocation().toPortableString() + File.separator + openEjbJarXmlFile).exists()) {
				setErrorMessage("Please select a valid openejb-jar.xml file");
				setPageComplete(false);
				return;
			} 
		}
		
		setErrorMessage(null);
		setPageComplete(true);
	}
}
