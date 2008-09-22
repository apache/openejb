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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.TaskModel;
import org.eclipse.wst.server.ui.wizard.IWizardHandle;
import org.eclipse.wst.server.ui.wizard.WizardFragment;

public class OpenEJBRuntimeFragment extends WizardFragment {

	private IWizardHandle handle;

	@Override
	public boolean hasComposite() {
		return true;
	}

	@Override
	public Composite createComposite(Composite parent, IWizardHandle handle) {
		this.handle = handle;
		handle.setTitle(Messages.getString("org.apache.openejb.eclipse.server.runtimeTitle"));
		handle.setDescription(Messages.getString("org.apache.openejb.eclipse.server.runtimeDescription"));
		handle.setImageDescriptor(Activator.getDefault().getImageDescriptor("RUNTIME"));
		final Composite composite = new Composite(parent, SWT.NULL);
		GridLayout gridLayout = new GridLayout(3, false);
		composite.setLayout(gridLayout);
		
		GridData gridData = new GridData();
		gridData.minimumWidth = 200;
		gridData.minimumHeight = 200;
		
		Label locationLabel = new Label(composite, SWT.NONE);
		locationLabel.setText(Messages.getString("org.apache.openejb.eclipse.server.openejbhomelabel"));
		
		final Text locationText = new Text(composite, SWT.BORDER);
		GridData textData = new GridData();
		textData.widthHint = 200;
		locationText.setLayoutData(textData);
		
		try {
			String location = getRuntimeDelegate().getRuntimeWorkingCopy().getLocation().toString();
			locationText.setText(location);
		} catch (Exception e) {
		}
		
		locationText.addModifyListener(new org.eclipse.swt.events.ModifyListener() {
			public void modifyText(org.eclipse.swt.events.ModifyEvent e) {
				getRuntimeDelegate().getRuntimeWorkingCopy().setLocation(new Path(locationText.getText()));
				validate();
			}
		});
		
		Button browseEjbJarButton = new Button(composite, SWT.NONE);
		browseEjbJarButton.setText("Browse");
		
		browseEjbJarButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(composite.getShell());
                dialog.setMessage("Selection installation dir");
                dialog.setFilterPath(locationText.getText());
                String selectedDirectory = dialog.open();
                if (selectedDirectory != null)
                    locationText.setText(selectedDirectory);
			}
		});

		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, "org.apache.openejb.help.runtime");
		return composite;
	}

	protected OpenEJBRuntimeDelegate getRuntimeDelegate() {
		IRuntimeWorkingCopy wc = (IRuntimeWorkingCopy) getTaskModel().getObject(TaskModel.TASK_RUNTIME);
		if (wc == null) {
			return null;
		}
		
		return (OpenEJBRuntimeDelegate) wc.loadAdapter(OpenEJBRuntimeDelegate.class, new NullProgressMonitor());
	}
	
	private void validate() {
		File installationDirectory = getRuntimeDelegate().getRuntimeWorkingCopy().getLocation().toFile();
		if (! installationDirectory.exists()) {
			handle.setMessage("Directory does not exist", IMessageProvider.ERROR);
			return;
		}
		
		IStatus status = getRuntimeDelegate().getRuntimeWorkingCopy().validate(new NullProgressMonitor());
		if (status.getSeverity() != IStatus.OK) {
			handle.setMessage(status.getMessage(), IMessageProvider.ERROR);
			return;
		}
		
		handle.setMessage("", IMessageProvider.NONE);
	}

	@Override
	public boolean isComplete() {
		IRuntimeWorkingCopy wc = getRuntimeDelegate().getRuntimeWorkingCopy();
        IStatus status = wc.validate(null);
        return status == null || status.getSeverity() != IStatus.ERROR;
	}
	
}
