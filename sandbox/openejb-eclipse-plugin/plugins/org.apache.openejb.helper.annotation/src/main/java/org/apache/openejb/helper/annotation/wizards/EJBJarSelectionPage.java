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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class EJBJarSelectionPage extends UserInputWizardPage {

	private static final Status SELECTION_NOT_OK_STATUS = new Status(4, "org.apache.openejb.helper.annotation", 4, "", null); //$NON-NLS-1$ //$NON-NLS-2$
	private static final Status SELECTION_OK_STATUS = new Status(0, "org.eclipse.core.runtime", 0, "", null); //$NON-NLS-1$ //$NON-NLS-2$

	private final EJBMigrationRefactoring refactoring;
	private Text ejbJarXmlText;
	private Text openEjbJarXmlText;
	private Button ejb3Interfaces;
	private Button remoteAnnotations;
	private Button useHomeInterfaces;
	private Button convertEntityBeansToPojos;
	private Button generateEntityManagerCode;
	
	@Override
	public void performHelp() {
		PlatformUI.getWorkbench().getHelpSystem().displayHelp(
				"org.apache.openejb.help.generate-annotations");
	}
	
	public EJBJarSelectionPage(EJBMigrationRefactoring refactoring) {
		super(Messages.getString("org.apache.openejb.helper.annotation.wizards.ejbJarSelectionWzd.pageName")); //$NON-NLS-1$
		setTitle(Messages.getString("org.apache.openejb.helper.annotation.wizards.ejbJarSelectionWzd.pageTitle")); //$NON-NLS-1$
		setDescription(Messages.getString("org.apache.openejb.helper.annotation.wizards.ejbJarSelectionWzd.pageDescription")); //$NON-NLS-1$
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
		ejbJarXmlLabel.setText(Messages.getString("org.apache.openejb.helper.annotation.wizards.ejbJarSelectionWzd.label.ejbJarXmlLocation")); //$NON-NLS-1$

		ejbJarXmlText = new Text(container, SWT.BORDER);
		ejbJarXmlText.setText(refactoring.getEjbJarXmlFile());
		ejbJarXmlText.setLayoutData(textData);
		ejbJarXmlText.addModifyListener(new org.eclipse.swt.events.ModifyListener() {
			public void modifyText(org.eclipse.swt.events.ModifyEvent e) {
				checkPage();
			}
		});
		
		Button browseEjbJarButton = new Button(container, SWT.NONE);
		browseEjbJarButton.setText(Messages.getString("org.apache.openejb.helper.annotation.wizards.ejbJarSelectionWzd.button.browseEjbJar")); //$NON-NLS-1$
		browseEjbJarButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				String filename = handleBrowse(Messages.getString("org.apache.openejb.helper.annotation.wizards.ejbJarSelectionWzd.selectionListener.filename.1")); //$NON-NLS-1$
				if (filename != null && filename.length() > 0) {
					ejbJarXmlText.setText(filename);
					checkPage();
				}
			}
		});
		
		Label openEjbJarXmlLabel = new Label(container, SWT.NONE);
		openEjbJarXmlLabel.setText(Messages.getString("org.apache.openejb.helper.annotation.wizards.ejbJarSelectionWzd.label.openEjbJarXml")); //$NON-NLS-1$
		
		openEjbJarXmlText = new Text(container, SWT.BORDER);
		openEjbJarXmlText.setText(refactoring.getOpenEjbJarXmlFile());
		openEjbJarXmlText.setLayoutData(textData);
		openEjbJarXmlText.addModifyListener(new org.eclipse.swt.events.ModifyListener() {
			public void modifyText(org.eclipse.swt.events.ModifyEvent e) {
				checkPage();
			}
		});

		Button browseOpenEjbJarXmlButton = new Button(container, SWT.NONE);
		browseOpenEjbJarXmlButton.setText(Messages.getString("org.apache.openejb.helper.annotation.wizards.ejbJarSelectionWzd.button.browseEjbJar")); //$NON-NLS-1$
		browseOpenEjbJarXmlButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				String filename = handleBrowse(Messages.getString("org.apache.openejb.helper.annotation.wizards.ejbJarSelectionWzd.selectionListener.filename.2")); //$NON-NLS-1$
				if (filename != null && filename.length() > 0) {
					openEjbJarXmlText.setText(filename);
					checkPage();
				}
			}
		});
		
		GridData checkData = new GridData();
		checkData.horizontalSpan = 3;
		checkData.horizontalAlignment = GridData.FILL;
		
		ejb3Interfaces = new Button(container, SWT.CHECK);
		ejb3Interfaces.setLayoutData(checkData);
		ejb3Interfaces.setText(Messages.getString("org.apache.openejb.helper.annotation.wizards.ejbJarSelectionWzd.button.alterSessionBeanIface")); //$NON-NLS-1$
		ejb3Interfaces.setSelection(true);
		ejb3Interfaces.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				checkPage();
			}
		});
		
		remoteAnnotations = new Button(container, SWT.CHECK);
		remoteAnnotations.setLayoutData(checkData);
		remoteAnnotations.setText(Messages.getString("org.apache.openejb.helper.annotation.wizards.ejbJarSelectionWzd.button.addRemoteAnnotations")); //$NON-NLS-1$
		remoteAnnotations.setSelection(true);
		remoteAnnotations.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				checkPage();
			}
		});
		
		useHomeInterfaces = new Button(container, SWT.CHECK);
		useHomeInterfaces.setLayoutData(checkData);
		useHomeInterfaces.setText(Messages.getString("org.apache.openejb.helper.annotation.wizards.ejbJarSelectionWzd.button.useHomeInterface")); //$NON-NLS-1$
		useHomeInterfaces.setSelection(true);
		useHomeInterfaces.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				checkPage();
			}
		});
		
		convertEntityBeansToPojos = new Button(container, SWT.CHECK);
		convertEntityBeansToPojos.setLayoutData(checkData);
		convertEntityBeansToPojos.setText(Messages.getString("org.apache.openejb.helper.annotation.wizards.ejbJarSelectionWzd.button.convertEntityBeans")); //$NON-NLS-1$
		convertEntityBeansToPojos.setSelection(true);
		convertEntityBeansToPojos.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				checkPage();
			}
		});
		
		generateEntityManagerCode = new Button(container, SWT.CHECK);
		generateEntityManagerCode.setLayoutData(checkData);
		generateEntityManagerCode.setText(Messages.getString("org.apache.openejb.helper.annotation.wizards.ejbJarSelectionWzd.button.generateEntityFactoryCode")); //$NON-NLS-1$
		generateEntityManagerCode.setSelection(true);
		generateEntityManagerCode.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				checkPage();
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
				
				if (element == null) {
					return false;
				}
				
				if (element instanceof IFile) {
					IFile file = ((IFile)element);
					String extension = file.getFileExtension();
					return (extension != null && extension.equals("xml")); //$NON-NLS-1$
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
		
		if (convertEntityBeansToPojos.getSelection() == false) {
			generateEntityManagerCode.setSelection(false);
			generateEntityManagerCode.setEnabled(false);
		} else {
			generateEntityManagerCode.setEnabled(true);
		}
		
		useHomeInterfaces.setEnabled(ejb3Interfaces.getSelection() || remoteAnnotations.getSelection());
		
		refactoring.setEjbJarXmlFile(ejbJarXmlFile);
		refactoring.setOpenEjbJarXmlFile(openEjbJarXmlFile);
		refactoring.setConvertEntityBeansToPojos(convertEntityBeansToPojos.getSelection());
		refactoring.setEjb3Interfaces(ejb3Interfaces.getSelection());
		refactoring.setRemoteAnnotations(remoteAnnotations.getSelection());
		refactoring.setGenerateEntityManagerCode(generateEntityManagerCode.getSelection());
		
		if (refactoring.getProject() == null) {
			setErrorMessage(null);
			setPageComplete(true);
			return;
		}
		
		if (! new File(refactoring.getProject().getLocation().toPortableString() + File.separator + ejbJarXmlFile).exists()) {
			setErrorMessage(Messages.getString("org.apache.openejb.helper.annotation.wizards.ejbJarSelectionWzd.pageErrorMsg.1")); //$NON-NLS-1$
			setPageComplete(false);
			return;
		} 
		
		if (openEjbJarXmlFile  != null && openEjbJarXmlFile.length() > 0) {
			if (! new File(refactoring.getProject().getLocation().toPortableString() + File.separator + openEjbJarXmlFile).exists()) {
				setErrorMessage(Messages.getString("org.apache.openejb.helper.annotation.wizards.ejbJarSelectionWzd.pageErrorMsg.2")); //$NON-NLS-1$
				setPageComplete(false);
				return;
			} 
		}
		
		setErrorMessage(null);
		setPageComplete(true);
	}
}
