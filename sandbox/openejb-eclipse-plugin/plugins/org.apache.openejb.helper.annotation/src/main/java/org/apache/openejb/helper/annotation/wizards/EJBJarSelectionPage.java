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

import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class EJBJarSelectionPage extends UserInputWizardPage {

	protected String ejbJarXmlFile;
	protected String openEjbJarXmlFile;
	
	protected Text ejbJarXmlFileText;
	protected Text openEjbJarXmlFileText;

	public EJBJarSelectionPage() {
		super("wizardPage");
		setTitle("EJB 3.0 Annotation Wizard");
		setDescription("This wizard analyzes ejb-jar.xml and openejb-jar.xml and adds EJB 3.0 annotations to your source");
	}
	
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		container.setLayout(layout);
		
		Label ejbJarXmlLabel = new Label(container, SWT.NULL);
		ejbJarXmlLabel.setText("&EJB Jar File");
		
		ejbJarXmlFileText = new Text(container, SWT.BORDER | SWT.SINGLE);
		ejbJarXmlFileText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				checkPage();
			}
		});
		
		Button ejbJarBrowseButton = new Button(container, SWT.PUSH);
		ejbJarBrowseButton.setText("&Browse");
		ejbJarBrowseButton.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event event) {
				handleEjbJarBrowse();
			}
		});
		
		setControl(container);
	}
	
	protected void handleEjbJarBrowse() {
		FileDialog fileDialog = new FileDialog(getShell());
		fileDialog.setFilterNames(new String[] {"ejb-jar.xml"});
		String filename = fileDialog.open();
		ejbJarXmlFileText.setText(filename);
		
		checkPage();
	}

	protected void checkPage() {
		ejbJarXmlFile = ejbJarXmlFileText.getText();
		
		if (! new File(ejbJarXmlFile).exists()) {
			setErrorMessage("Please select an ejb-jar.xml file");
			setPageComplete(false);
		} else {
			setErrorMessage(null);
			setPageComplete(true);
		}
	}
}
