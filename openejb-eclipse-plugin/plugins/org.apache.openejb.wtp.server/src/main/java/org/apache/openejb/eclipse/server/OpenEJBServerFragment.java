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
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.TaskModel;
import org.eclipse.wst.server.ui.wizard.IWizardHandle;
import org.eclipse.wst.server.ui.wizard.WizardFragment;

public class OpenEJBServerFragment extends WizardFragment implements ModifyListener {

	private IWizardHandle handle;
	private Label portLabel;
	private Text portText;
	private Label httpEjbPortLabel;
	private Text httpEjbPort;
	private Label hsqlPortLabel;
	private Text hsqlPort;
	private Label telnetPortLabel;
	private Text telnetPort;
	private Label adminPortLabel;
	private Text adminPort;
	private Label configFileLabel;
	private Text configFile;
	private Button configFileBrowseButton;
	
	@Override
	public Composite createComposite(Composite parent, IWizardHandle handle) {
		OpenEJBServer server = getServer();
		
		this.handle = handle;
		handle.setTitle("OpenEJB Server");
		handle.setImageDescriptor(Activator.getDefault().getImageDescriptor("RUNTIME"));
		final Composite serverComposite = new Composite(parent, SWT.NULL);
		
		portLabel = new Label(serverComposite, SWT.NONE);
		portLabel.setText(Messages.getString("org.apache.openejb.eclipse.server.ejbPort"));
		portText = new Text(serverComposite, SWT.BORDER);
		portText.addModifyListener(this);
		Label filler5 = new Label(serverComposite, SWT.NONE);
		httpEjbPortLabel = new Label(serverComposite, SWT.NONE);
		httpEjbPortLabel.setText("HTTP EJB Port");
		httpEjbPort = new Text(serverComposite, SWT.BORDER);
		httpEjbPort.addModifyListener(this);
		Label filler4 = new Label(serverComposite, SWT.NONE);
		hsqlPortLabel = new Label(serverComposite, SWT.NONE);
		hsqlPortLabel.setText("HSQL Port");
		hsqlPort = new Text(serverComposite, SWT.BORDER);
		hsqlPort.addModifyListener(this);
		Label filler3 = new Label(serverComposite, SWT.NONE);
		telnetPortLabel = new Label(serverComposite, SWT.NONE);
		telnetPortLabel.setText("Telnet Port");
		telnetPort = new Text(serverComposite, SWT.BORDER);
		telnetPort.addModifyListener(this);
		Label filler2 = new Label(serverComposite, SWT.NONE);
		adminPortLabel = new Label(serverComposite, SWT.NONE);
		adminPortLabel.setText("Admin Port");
		adminPort = new Text(serverComposite, SWT.BORDER);
		adminPort.addModifyListener(this);
		Label filler1 = new Label(serverComposite, SWT.NONE);
		configFileLabel = new Label(serverComposite, SWT.NONE);
		configFileLabel.setText("Configuration file");
		configFile = new Text(serverComposite, SWT.BORDER);
		configFile.addModifyListener(this);
		configFileBrowseButton = new Button(serverComposite, SWT.NONE);
		configFileBrowseButton.setText("...");
		configFileBrowseButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(serverComposite.getShell());
                dialog.setFilterExtensions(new String[] {"*.xml", "*.*"});
                dialog.setFilterNames(new String[] {"XML files", "All files"});
                dialog.setText("Selection openejb.xml configuration file");
                String selectedFile = dialog.open();
                if (selectedFile != null)
                    configFile.setText(selectedFile);
			}
			
		});
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		serverComposite.setLayout(gridLayout);

		String ejbPortNum = "4201";
		String httpEjbPortNum = "4204";
		String hsqlPortNum = "9001";
		String telnetPortNum = "4202";
		String adminPortNum = "4200";
		String alternateConfigFile = "";
		
		try {
			ejbPortNum = server.getEJBPort();
			httpEjbPortNum = server.getHTTPEJBPort();
			hsqlPortNum = server.getHSQLPort();
			telnetPortNum = server.getTelnetPort();
			adminPortNum = server.getAdminPort();
			alternateConfigFile = server.getConfigFile();
		} catch (Exception e) {
		}
			
		portText.setText(ejbPortNum);
		httpEjbPort.setText(httpEjbPortNum);		
		hsqlPort.setText(hsqlPortNum);
		telnetPort.setText(telnetPortNum);
		adminPort.setText(adminPortNum);
		configFile.setText(alternateConfigFile);
		
		validate();
		
		return serverComposite;
	}

	@Override
	public boolean hasComposite() {
		return true;
	}

	@Override
	public void performFinish(IProgressMonitor monitor) throws CoreException {
		super.performFinish(monitor);
	}

	private OpenEJBServer getServer() {
		IServerWorkingCopy wc = (IServerWorkingCopy) getTaskModel().getObject(TaskModel.TASK_SERVER);
		if (wc == null) {
		}
		
		OpenEJBServer server = (OpenEJBServer) wc.loadAdapter(OpenEJBServer.class, new NullProgressMonitor());
		return server;
	}

	private void validate() {
		if (configFile.getText() != null && configFile.getText().length() > 0) {
			File config = new File(configFile.getText());
			if (! config.exists()) {
				handle.setMessage("Directory does not exist", IMessageProvider.ERROR);
				return;
			}
		}
		
		String ejbdPortNum = portText.getText();
		if (! checkPort(ejbdPortNum)) {
			handle.setMessage("Invalid EJBD port", IMessageProvider.ERROR);
			return;
		}
			

		String httpEjbdPortNum = httpEjbPort.getText();
		if (! checkPort(httpEjbdPortNum)) {
			handle.setMessage("Invalid HTTP EJB port", IMessageProvider.ERROR);
			return;
		}

		String hsqlPortNum = hsqlPort.getText();
		if (! checkPort(hsqlPortNum)) {
			handle.setMessage("Invalid HSQL port", IMessageProvider.ERROR);
			return;
		}

		String telnetPortNum = telnetPort.getText();
		if (! checkPort(telnetPortNum)) {
			handle.setMessage("Invalid Telnet port", IMessageProvider.ERROR);
			return;
		}

		String adminPortNum = adminPort.getText();
		if (! checkPort(adminPortNum)) {
			handle.setMessage("Invalid Admin port", IMessageProvider.ERROR);
			return;
		}
		
		Set<String> ports = new HashSet<String>();
		ports.add(ejbdPortNum);
		ports.add(httpEjbdPortNum);
		ports.add(hsqlPortNum);
		ports.add(telnetPortNum);
		ports.add(adminPortNum);
		
		if (ports.size() != 5) {
			handle.setMessage("Ports must be unique", IMessageProvider.ERROR);
			return;
		}

		OpenEJBServer server = getServer();
		server.setEJBPort(portText.getText());
		server.setHTTPEJBPort(httpEjbPort.getText());
		server.setHSQLPort(hsqlPort.getText());
		server.setTelnetPort(telnetPort.getText());
		server.setAdminPort(adminPort.getText());
		server.setConfigFile(configFile.getText());

		handle.setMessage("", IMessageProvider.NONE);
	}

	private boolean checkPort(String portNum) {
		try {
			int port = Integer.parseInt(portNum);
			return (port > 0 && port < 65535);
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public void modifyText(ModifyEvent e) {
		validate();
	}

	@Override
	public boolean isComplete() {
		return IMessageProvider.NONE == handle.getMessageType();
	}
	
	
}
