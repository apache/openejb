/**  
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact openejb-group@openejb.sf.net.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.sf.net/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.webadmin.main;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.Properties;

import javax.ejb.Handle;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import org.exolab.castor.xml.ValidationException;
import org.openejb.OpenEJBException;
import org.openejb.config.Bean;
import org.openejb.config.ConfigUtils;
import org.openejb.config.Service;
import org.openejb.config.sys.ConnectionManager;
import org.openejb.config.sys.Connector;
import org.openejb.config.sys.Container;
import org.openejb.config.sys.Deployments;
import org.openejb.config.sys.JndiProvider;
import org.openejb.config.sys.Openejb;
import org.openejb.config.sys.ProxyFactory;
import org.openejb.config.sys.Resource;
import org.openejb.config.sys.SecurityService;
import org.openejb.config.sys.TransactionService;
import org.openejb.util.FileUtils;
import org.openejb.util.StringUtilities;
import org.openejb.webadmin.HttpRequest;
import org.openejb.webadmin.HttpResponse;
import org.openejb.webadmin.WebAdminBean;

/** This bean allows the user to graphicly edit the OpenEJB configuration file usually located at
 *  $OPENEJB_HOME/config/openejb.conf. 
 * 
 * @see org.openejb.config.sys.ConnectionManager
 * @see org.openejb.config.sys.Connector
 * @see org.openejb.config.sys.Container
 * @see org.openejb.config.sys.Deployments
 * @see org.openejb.config.sys.JndiProvider
 * @see org.openejb.config.sys.Openejb
 * @see org.openejb.config.sys.ProxyFactory
 * @see org.openejb.config.sys.Resource
 * @see org.openejb.config.sys.SecurityService
 * @see org.openejb.config.sys.TransactionService
 */
public class ConfigBean extends WebAdminBean {
	/** the handle file name */
	private static final String HANDLE_FILE = System.getProperty("file.separator") + "configurationHandle.obj";

	/** Called when the container creates a new instance of this bean */
	public void ejbCreate() {
		section = "Configuration";
	}

	/** Called before content is written to the browser
	 * @param request the HTTP request object
	 * @param response the HTTP response object
	 * @throws IOException if an exception is thrown
	 */
	public void preProcess(HttpRequest request, HttpResponse response) throws IOException {}

	/** called after content is written to the browser
	 * @param request the HTTP request object
	 * @param response the HTTP response object
	 * @throws IOException if an exception is thrown
	 */
	public void postProcess(HttpRequest request, HttpResponse response) throws IOException {}

	/** Write the TITLE of the HTML document.  This is the part
	 * that goes into the <HEAD><TITLE></TITLE></HEAD> tags
	 * @param body the body to write the title to
	 * @exception IOException if an exception is thrown
	 */
	public void writeHtmlTitle(PrintWriter body) throws IOException {
		body.print(HTML_TITLE);
	}

	/** Write the title of the page.  This is displayed right
	 * above the main block of content.
	 * @param body the body to write the page title to
	 * @exception IOException if an exception is thrown
	 */
	public void writePageTitle(PrintWriter body) throws IOException {
		body.print("Edit your OpenEJB Configuration");
	}

	/** Writes sub menu items for this menu item
	 * @param body the output to write to
	 * @throws IOException if an exception is thrown
	 */
	public void writeSubMenuItems(PrintWriter body) throws IOException {}

	/** Write the main content to the browser
	 * @param body the output to write the content to
	 * @exception IOException if an exception is thrown
	 */
	public void writeBody(PrintWriter body) throws IOException {
		Openejb openejb;
		ConfigurationDataObject configurationData;
		String configLocation = System.getProperty("openejb.configuration");

		//get all the form parameters
		String type = request.getQueryParameter(ConfigHTMLWriter.QUERY_PARAMETER_TYPE);
		String method = request.getQueryParameter(ConfigHTMLWriter.QUERY_PARAMETER_METHOD);
		String handleFile = request.getFormParameter(ConfigHTMLWriter.FORM_FIELD_HANDLE_FILE);
		String submitOpenejb = request.getFormParameter(ConfigHTMLWriter.FORM_FIELD_SUBMIT_OPENEJB);
		String submitService = request.getFormParameter(ConfigHTMLWriter.FORM_FIELD_SUBMIT_SERVICE);
		String containerType = request.getFormParameter(ConfigHTMLWriter.FORM_FIELD_CONTAINER_TYPE);

		//get the main openejb configuration from ConfigurationData object
		//also create a handle file to get the configuration next time
		if (handleFile == null) {
			configurationData = getConfigurationObject();
			handleFile = createHandle(configurationData);
			try {
				openejb = ConfigUtils.readConfig(configLocation);
			} catch (OpenEJBException e) {
				throw new IOException(e.getMessage());
			}
		} else {
			configurationData = getHandle(handleFile);
			openejb = configurationData.getOpenejb();
		}

		//check for the action we're going to take, these actions are 
		//grouped by "submits" and by "begins"
		if (submitOpenejb != null) {
			body.println(submitOpenejb(configLocation, openejb));
			return;
		} else if (ConfigHTMLWriter.FORM_VALUE_SUBMIT_CONNECTOR.equals(submitService)) {
			submitConnector(body, openejb, handleFile, configLocation);
		} else if (containerType != null) {
			submitContainer(body, openejb, handleFile, configLocation);
		} else if (ConfigHTMLWriter.FORM_VALUE_SUBMIT_DEPLOYMENTS.equals(submitService)) {
			submitDeployments(body, openejb, handleFile, configLocation);
		} else if (submitService != null) {
			submitService(body, openejb, handleFile, configLocation, submitService);
		} else if (ConfigHTMLWriter.TYPE_CONNECTOR.equals(type)) {
			beginConnector(method, handleFile, body, openejb, configLocation);
		} else if (ConfigHTMLWriter.TYPE_CONTAINER.equals(type)) {
			beginContainer(method, handleFile, body, openejb, configLocation);
		} else if (ConfigHTMLWriter.TYPE_DEPLOYMENTS.equals(type)) {
			beginDeployments(method, handleFile, body, openejb, configLocation);
		} else if (type != null) {
			beginService(method, handleFile, body, openejb, configLocation, type);
		} else {
			ConfigHTMLWriter.writeOpenejb(body, openejb, handleFile, configLocation);
		}

		//set the object onto the stateful bean
		configurationData.setOpenejb(openejb);
	}

	/**
	 * Finds the connector from the array based on the id from the form and then
	 * calls a method to create, edit or delete it
	 * 
	 * @param method create, edit or delete for the action of this connector
	 * @param handleFile the handle for the ConfigurationDataBean
	 * @param body the PrintWriter to the browser
	 * @param openejb the main configuration object
	 * @param configLocation the location of the configuration object
	 * @throws IOException when an invalid method is passed in
	 */
	private void beginConnector(String method, String handleFile, PrintWriter body, Openejb openejb, String configLocation)
		throws IOException {
		String connectorId = request.getFormParameter(ConfigHTMLWriter.TYPE_CONNECTOR);
		Connector[] connectors = openejb.getConnector();
		int connectorIndex = -1;

		//make sure the connectorId is not null and then loop to find a match in the array
		connectorId = (connectorId == null) ? "" : connectorId;
		for (int i = 0; i < connectors.length; i++) {
			if (connectorId.equals(connectors[i].getId())) {
				connectorIndex = i;
				break;
			}
		}

		//check for which method we're performing create, edit or delete
		if (ConfigHTMLWriter.CREATE.equals(method)) {
			ConfigHTMLWriter.writeConnector(body, null, handleFile, -1);
		} else if (ConfigHTMLWriter.EDIT.equals(method)) {
			ConfigHTMLWriter.writeConnector(
				body,
				(connectorIndex == -1) ? null : connectors[connectorIndex],
				handleFile,
				connectorIndex);
		} else if (ConfigHTMLWriter.DELETE.equals(method)) {
			if (connectorIndex > -1)
				openejb.removeConnector(connectorIndex);
			ConfigHTMLWriter.writeOpenejb(body, openejb, handleFile, configLocation);
		} else {
			throw new IOException("Invalid method");
		}
	}

	/**
	 * Finds the container from the array based on the id from the form and then
	 * calls a method to create, edit or delete it.  In addition, it uses a ContainerData
	 * object to store the information for the container.
	 * 
	 * @param method create, edit or delete for the action of this container
	 * @param handleFile the handle for the ConfigurationDataBean
	 * @param body the PrintWriter to the browser
	 * @param openejb the main configuration object
	 * @param configLocation the location of the configuration object
	 * @see org.openejb.webadmin.main.ContainerData
	 * @throws IOException when an invalid method is passed in
	 */
	private void beginContainer(String method, String handleFile, PrintWriter body, Openejb openejb, String configLocation)
		throws IOException {
		String containerId = request.getFormParameter(ConfigHTMLWriter.TYPE_CONTAINER);
		Container[] containers = openejb.getContainer();
		int containerIndex = -1;
		Properties properties = new Properties();

		//make sure the containerId is not null then loop through the array
		//to find the matching container
		containerId = (containerId == null) ? "" : containerId;
		for (int i = 0; i < containers.length; i++) {
			if (containerId.equals(containers[i].getId())) {
				containerIndex = i;
				break;
			}
		}

		//based on the method, we want to create a container, edit the existing one
		//or delete the existing one
		if (ConfigHTMLWriter.CREATE.equals(method)) {
			ConfigHTMLWriter.writeContainer(body, new ContainerData(), handleFile);
		} else if (ConfigHTMLWriter.EDIT.equals(method)) {
			//create a new container data object and set the contents
			ContainerData data = new ContainerData();
			if (containerIndex > -1) {
				data.setId(StringUtilities.nullToBlankString(containers[containerIndex].getId()));
				data.setJar(StringUtilities.nullToBlankString(containers[containerIndex].getJar()));
				data.setProvider(StringUtilities.nullToBlankString(containers[containerIndex].getProvider()));
				data.setContainerType(StringUtilities.nullToBlankString(containers[containerIndex].getCtype()));
				ByteArrayInputStream in =
					new ByteArrayInputStream(
						StringUtilities.nullToBlankString(containers[containerIndex].getContent()).getBytes());
				properties.load(in);

				data.setBulkPassivate(properties.getProperty(IM_PASSIVATE_SIZE, ""));
				data.setGlobalTxDatabase(properties.getProperty(GLOBAL_TX_DATABASE, ""));
				data.setIndex(containerIndex);
				data.setLocalTxDatabase(properties.getProperty(LOCAL_TX_DATABASE, ""));
				data.setPassivator(properties.getProperty(IM_PASSIVATOR, ""));
				data.setPoolSize(properties.getProperty(IM_POOL_SIZE, ""));
				data.setStrictPooling(properties.getProperty(IM_STRICT_POOLING, "true"));
				data.setTimeOut(properties.getProperty(IM_TIME_OUT, ""));
				data.setEdit(true);
			}

			ConfigHTMLWriter.writeContainer(body, data, handleFile);
		} else if (ConfigHTMLWriter.DELETE.equals(method)) {
			if (containerIndex > -1)
				openejb.removeContainer(containerIndex);
			ConfigHTMLWriter.writeOpenejb(body, openejb, handleFile, configLocation);
		} else {
			throw new IOException("Invalid method");
		}
	}

	/**
	 * Finds the current deployment from the array based on the jar or the directory 
	 * from the form and then calls a method to create, edit or delete it.  
	 * 
	 * @param method create, edit or delete for the action of this deployment
	 * @param handleFile the handle for the ConfigurationDataBean
	 * @param body the PrintWriter to the browser
	 * @param openejb the main configuration object
	 * @param configLocation the location of the configuration object
	 * @throws IOException when an invalid method is passed in
	 */
	private void beginDeployments(
		String method,
		String handleFile,
		PrintWriter body,
		Openejb openejb,
		String configLocation)
		throws IOException {
		//get the id for the current deployment, if there is one
		String deploymentId = request.getFormParameter(ConfigHTMLWriter.TYPE_DEPLOYMENTS);
		Deployments[] deployments = openejb.getDeployments();
		int deploymentIndex = -1;

		//loop through the deployment list and grab the jar or directory
		deploymentId = StringUtilities.nullToBlankString(deploymentId);
		for (int i = 0; i < deployments.length; i++) {
			if (deploymentId.equals(deployments[i].getDir()) || deploymentId.equals(deployments[i].getJar())) {
				deploymentIndex = i;
				break;
			}
		}

		//check the method and proceed with a create, edit or delete
		if (ConfigHTMLWriter.CREATE.equals(method)) {
			ConfigHTMLWriter.writeDeployments(body, null, handleFile, -1);
		} else if (ConfigHTMLWriter.EDIT.equals(method)) {
			ConfigHTMLWriter.writeDeployments(body, deployments[deploymentIndex], handleFile, deploymentIndex);
		} else if (ConfigHTMLWriter.DELETE.equals(method)) {
			if (deploymentIndex > -1)
				openejb.removeDeployments(deploymentIndex);
			ConfigHTMLWriter.writeOpenejb(body, openejb, handleFile, configLocation);
		} else {
			throw new IOException("Invalid method");
		}
	}

	/**
	 * This is a generic begin method for services where all we care about is
	 * id, jar, provider and content.  It currently handles any services that
	 * don't have much documentation.  In future implementations this method will
	 * be refactored since all services should have a specalized, specific UI 
	 * 
	 * @param method create, edit or delete for the action of this deployment
	 * @param handleFile the handle for the ConfigurationDataBean
	 * @param body the PrintWriter to the browser
	 * @param openejb the main configuration object
	 * @param configLocation the location of the configuration object
	 * @param type the type of service being passed in (see the "type" variables
	 *               in ConfigHTMLWriter)
	 * @see org.openejb.webadmin.main.ConfigHTMLWriter
	 * @throws IOException - when an invalid method is passed in
	 */
	private void beginService(
		String method,
		String handleFile,
		PrintWriter body,
		Openejb openejb,
		String configLocation,
		String type)
		throws IOException {
		//get the current id
		String serviceId = StringUtilities.nullToBlankString(request.getFormParameter(type));
		Service service = null;
		Service[] services = new Service[0];
		String submit = "";

		/*
		 * TODO: Seperate out the simple services (with just id, jar, provider and content)
		 * into multiple specific services that are specific for the content of each type  
		 */ 

		//instantiate the service type based on the type passed in
		//also set the value of the submit button
		if (ConfigHTMLWriter.TYPE_CONNECTION_MANAGER.equals(type)) {
			service = openejb.getConnectionManager();
			submit = ConfigHTMLWriter.FORM_VALUE_SUBMIT_CONNECTION_MANAGER;
		} else if (ConfigHTMLWriter.TYPE_JNDI_PROVIDER.equals(type)) {
			services = openejb.getJndiProvider();
			submit = ConfigHTMLWriter.FORM_VALUE_SUBMIT_JNDI_PROVIDER;
		} else if (ConfigHTMLWriter.TYPE_PROXY_FACTORY.equals(type)) {
			service = openejb.getProxyFactory();
			submit = ConfigHTMLWriter.FORM_VALUE_SUBMIT_PROXY_FACTORY;
		} else if (ConfigHTMLWriter.TYPE_SECURITY_SERVICE.equals(type)) {
			service = openejb.getSecurityService();
			submit = ConfigHTMLWriter.FORM_VALUE_SUBMIT_SECURITY_SERVICE;
		} else if (ConfigHTMLWriter.TYPE_TRANSACTION_SERVICE.equals(type)) {
			service = openejb.getTransactionService();
			submit = ConfigHTMLWriter.FORM_VALUE_SUBMIT_TRANSACTION_SERVICE;
		} else if (ConfigHTMLWriter.TYPE_RESOURCE.equals(type)) {
			services = openejb.getResource();
			submit = ConfigHTMLWriter.FORM_VALUE_SUBMIT_RESOURCE;
		}

		int serviceIndex = -1;

		//if there is an array of services, loop through them
		for (int i = 0; i < services.length; i++) {
			if (serviceId.equals(services[i].getId())) {
				serviceIndex = i;
				service = services[i];
				break;
			}
		}

		//next check to see if we're doing a create, edit or delete
		if (ConfigHTMLWriter.CREATE.equals(method)) {
			ConfigHTMLWriter.writeService(body, null, handleFile, submit, -1);
		} else if (ConfigHTMLWriter.EDIT.equals(method)) {
			ConfigHTMLWriter.writeService(body, service, handleFile, submit, serviceIndex);
		} else if (ConfigHTMLWriter.DELETE.equals(method)) {
			//here we need to check the type again to remove the proper
			//service
			if (ConfigHTMLWriter.TYPE_CONNECTION_MANAGER.equals(type)) {
				openejb.setConnectionManager(null);
			} else if (ConfigHTMLWriter.TYPE_JNDI_PROVIDER.equals(type) && serviceIndex > -1) {
				openejb.removeJndiProvider(serviceIndex);
			} else if (ConfigHTMLWriter.TYPE_PROXY_FACTORY.equals(type)) {
				openejb.setProxyFactory(null);
			} else if (ConfigHTMLWriter.TYPE_SECURITY_SERVICE.equals(type)) {
				openejb.setSecurityService(null);
			} else if (ConfigHTMLWriter.TYPE_TRANSACTION_SERVICE.equals(type)) {
				openejb.setTransactionService(null);
			} else if (ConfigHTMLWriter.TYPE_RESOURCE.equals(type) && serviceIndex > -1) {
				openejb.removeResource(serviceIndex);
			}
			ConfigHTMLWriter.writeOpenejb(body, openejb, handleFile, configLocation);
		} else {
			throw new IOException("Invalid method");
		}
	}

	/**
	 * This method takes care of submitting a connector.  It grabs the form parameters
	 * and constructs the connector object
	 * 
	 * @param body the output to the browser
	 * @param openejb the openejb object
	 * @param handleFile the file of the handle for the ConfigurationData object
	 * @param configLocation the location of the configuration file
	 * @throws IOException when an exception occurs
	 */
	private void submitConnector(PrintWriter body, Openejb openejb, String handleFile, String configLocation) throws IOException {
		Connector connector;

		//get all the form variables
		String id = request.getFormParameter(ConfigHTMLWriter.FORM_FIELD_ID).trim();
		String jar = request.getFormParameter(ConfigHTMLWriter.FORM_FIELD_JAR).trim();
		String provider = request.getFormParameter(ConfigHTMLWriter.FORM_FIELD_PROVIDER).trim();
		String jdbcDriver = request.getFormParameter(JDBC_DRIVER).trim();
		String jdbcUrl = request.getFormParameter(JDBC_URL).trim();
		String userName = request.getFormParameter(USER_NAME).trim();
		String password = request.getFormParameter(PASSWORD).trim();
		int index = Integer.parseInt(request.getFormParameter(ConfigHTMLWriter.FORM_FIELD_INDEX).trim());
		StringBuffer contentBuffer = new StringBuffer(125);
		StringBuffer validationError = new StringBuffer(50);

		//if the index is bigger than -1 then we want to get the current connector
		//otherwise create a new one
		if (index > -1) {
			connector = openejb.getConnector(index);
		} else {
			connector = new Connector();
		}

		//check for blank fields in the different parts of the content
		if (!"".equals(jdbcDriver))
			contentBuffer.append(JDBC_DRIVER).append(" ").append(jdbcDriver).append('\n');
		if (!"".equals(jdbcUrl))
			contentBuffer.append(JDBC_URL).append(" ").append(jdbcUrl).append('\n');
		if (!"".equals(userName))
			contentBuffer.append(USER_NAME).append(" ").append(userName).append('\n');
		if (!"".equals(password))
			contentBuffer.append(PASSWORD).append(" ").append(password).append('\n');
		if (!"".equals(id.trim())) 
			connector.setId(id.trim());
		if (!"".equals(jar.trim()))
			connector.setJar(jar);
		if (!"".equals(provider.trim()))
			connector.setProvider(provider);

		connector.setContent((contentBuffer.length() > 0) ? contentBuffer.toString() : null);

		try { //perform validation
			connector.validate();
		} catch (ValidationException e) {
			//print the error message
			body.print("<font color=\"red\">You must fix the following errors before proceeding:<br>\n<b>");
			body.print(e.getMessage());
			body.print("</b></font>\n<br><br>");

			ConfigHTMLWriter.writeConnector(body, connector, handleFile, index);
			return;
		}

		//if the connector is new, add it after validation
		if (index == -1) {
			openejb.addConnector(connector);
		}

		ConfigHTMLWriter.writeOpenejb(body, openejb, handleFile, configLocation);
	}

	/**
	 * This method takes care of submitting a container.  It constructs a ContainerData
	 * object, puts all the info into it checks to see if we've submitted the form
	 * or just switched the container type
	 * 
	 * @see org.openejb.webadmin.main.ContainerData
	 * @param body the output to the browser
	 * @param openejb the openejb object
	 * @param handleFile the file of the handle for the ConfigurationData object
	 * @param configLocation the location of the configuration file
	 * @throws IOException when an exception occurs
	 */
	private void submitContainer(PrintWriter body, Openejb openejb, String handleFile, String configLocation)
		throws IOException {
		ContainerData data = new ContainerData();
		int index = Integer.parseInt(request.getFormParameter(ConfigHTMLWriter.FORM_FIELD_INDEX));
		String submit = request.getFormParameter(ConfigHTMLWriter.FORM_FIELD_SUBMIT_SERVICE);
		Container container;
		StringBuffer contentBuffer = new StringBuffer(125);
		StringBuffer errorBuffer = new StringBuffer(100);

		//set all the form data onto the ContainerData object
		data.setBulkPassivate(
			StringUtilities.nullToBlankString(request.getFormParameter(IM_PASSIVATE_SIZE)).trim());
		data.setContainerType(
			StringUtilities.nullToBlankString(request.getFormParameter(ConfigHTMLWriter.FORM_FIELD_CONTAINER_TYPE)).trim());
		data.setGlobalTxDatabase(
			StringUtilities.nullToBlankString(request.getFormParameter(GLOBAL_TX_DATABASE)).trim());
		data.setId(StringUtilities.nullToBlankString(request.getFormParameter(ConfigHTMLWriter.FORM_FIELD_ID)).trim());
		data.setIndex(index);
		data.setJar(StringUtilities.nullToBlankString(request.getFormParameter(ConfigHTMLWriter.FORM_FIELD_JAR)).trim());
		data.setLocalTxDatabase(
			StringUtilities.nullToBlankString(request.getFormParameter(LOCAL_TX_DATABASE)).trim());
		data.setPassivator(StringUtilities.nullToBlankString(request.getFormParameter(IM_PASSIVATOR)).trim());
		data.setPoolSize(StringUtilities.nullToBlankString(request.getFormParameter(IM_POOL_SIZE)).trim());
		data.setProvider(
			StringUtilities.nullToBlankString(request.getFormParameter(ConfigHTMLWriter.FORM_FIELD_PROVIDER)).trim());
		data.setStrictPooling(
			StringUtilities.nullToBlankString(request.getFormParameter(IM_STRICT_POOLING)).trim());
		data.setTimeOut(StringUtilities.nullToBlankString(request.getFormParameter(IM_TIME_OUT)).trim());

		//here we have submitted the form
		if (ConfigHTMLWriter.FORM_VALUE_SUBMIT_CONTAINER.equals(submit)) {
			if (index > -1) {
				container = openejb.getContainer(index);
			} else {
				container = new Container();
			}

			//set all the common data
			container.setCtype(data.getContainerType().trim());
			if (!"".equals(data.getId().trim()))
				container.setId(data.getId().trim());
			if (!"".equals(data.getJar().trim()))
				container.setJar(data.getJar().trim());
			if ("".equals(data.getProvider().trim()))
				container.setProvider(data.getProvider().trim());

			//construct the contents based on type
			if (Bean.CMP_ENTITY.equals(data.getContainerType())) {
				if (!"".equals(data.getPoolSize()))
					contentBuffer.append(IM_POOL_SIZE).append(" ").append(data.getPoolSize()).append('\n');
				if (!"".equals(data.getGlobalTxDatabase()))
					contentBuffer.append(GLOBAL_TX_DATABASE).append(" ").append(data.getGlobalTxDatabase()).append(
						'\n');
				else
					errorBuffer.append("Global Database File is a required field.<br>\n");
				if (!"".equals(data.getLocalTxDatabase()))
					contentBuffer.append(LOCAL_TX_DATABASE).append(" ").append(data.getLocalTxDatabase()).append(
						'\n');
				else
					errorBuffer.append("Local Database File is a required field.<br>\n");
			} else if (Bean.STATEFUL.equals(data.getContainerType())) {
				if (!"".equals(data.getPassivator()))
					contentBuffer.append(IM_PASSIVATOR).append(" ").append(data.getPassivator()).append('\n');
				if (!"".equals(data.getTimeOut()))
					contentBuffer.append(IM_TIME_OUT).append(" ").append(data.getTimeOut()).append('\n');
				if (!"".equals(data.getPoolSize()))
					contentBuffer.append(IM_POOL_SIZE).append(" ").append(data.getPoolSize()).append('\n');
				if (!"".equals(data.getBulkPassivate()))
					contentBuffer.append(IM_PASSIVATE_SIZE).append(" ").append(data.getBulkPassivate()).append(
						'\n');
			} else if (Bean.STATELESS.equals(data.getContainerType())) {
				if (!"".equals(data.getTimeOut()))
					contentBuffer.append(IM_TIME_OUT).append(" ").append(data.getTimeOut()).append('\n');
				if (!"".equals(data.getPoolSize()))
					contentBuffer.append(IM_POOL_SIZE).append(" ").append(data.getPoolSize()).append('\n');
				if (!"".equals(data.getStrictPooling()))
					contentBuffer.append(IM_STRICT_POOLING).append(" ").append(data.getStrictPooling()).append(
						'\n');

				container.setContent((contentBuffer.length() > 0) ? contentBuffer.toString() : null);
			}

			try { //validate the container
				container.validate();
			} catch (ValidationException e) {
				errorBuffer.insert(0, e.getMessage() + "<br>");
			}

			//check for an error message
			if (errorBuffer.length() > 0) {
				errorBuffer.insert(0, "<font color=\"red\">You must fix the following errors: <br><b>").append(
					"</b></font><br>");
				body.println(errorBuffer.toString());
				ConfigHTMLWriter.writeContainer(body, data, handleFile);
				return;
			}

			ConfigHTMLWriter.writeOpenejb(body, openejb, handleFile, configLocation);
		} else {
			//in this case we just switched the container type
			ConfigHTMLWriter.writeContainer(body, data, handleFile);
		}
	}

	/**
	 * This method takes care of submitting deployments.  It simply sets the jar
	 * or directory based on which one is chosen.
	 * 
	 * @param body the output to the browser
	 * @param openejb the openejb object
	 * @param handleFile the file of the handle for the ConfigurationData object
	 * @param configLocation the location of the configuration file
	 */
	private void submitDeployments(PrintWriter body, Openejb openejb, String handleFile, String configLocation) {
		String deploymentType = request.getFormParameter(ConfigHTMLWriter.FORM_FIELD_DEPLOYMENT_TYPE);
		String deploymentText = request.getFormParameter(ConfigHTMLWriter.FORM_FIELD_DEPLOYMENT_TEXT);
		int index = Integer.parseInt(request.getFormParameter(ConfigHTMLWriter.FORM_FIELD_INDEX));
		Deployments deployments;

		//get the right deployment from the array
		if (index > -1) {
			deployments = openejb.getDeployments(index);
		} else {
			deployments = new Deployments();
			openejb.addDeployments(deployments);
		}

		//set the directory or jar based on which one was chosen
		if (ConfigHTMLWriter.DEPLOYMENT_TYPE_DIR.equals(deploymentType.trim())) {
			deployments.setDir(deploymentText.trim());
		} else {
			deployments.setJar(deploymentText.trim());
		}

		ConfigHTMLWriter.writeOpenejb(body, openejb, handleFile, configLocation);
	}

	/**
	 * This is a general, "catch all" method for submitting service.  It goes through
	 * and checks to see which type of service is being submitted and then goes from
	 * there
	 * 
	 * @param body the output to the browser
	 * @param openejb the openejb object
	 * @param handleFile the file of the handle for the ConfigurationData object
	 * @param configLocation the location of the configuration file
	 * @param submit the string to be shown on the submit button
	 * @throws IOException when an exception occurs
	 */
	private void submitService(
		PrintWriter body,
		Openejb openejb,
		String handleFile,
		String configLocation,
		String submit)
		throws IOException {
		String id = request.getFormParameter(ConfigHTMLWriter.FORM_FIELD_ID);
		String jar = request.getFormParameter(ConfigHTMLWriter.FORM_FIELD_JAR);
		String provider = request.getFormParameter(ConfigHTMLWriter.FORM_FIELD_PROVIDER);
		String content = request.getFormParameter(ConfigHTMLWriter.FORM_FIELD_CONTENT);
		int index = Integer.parseInt(request.getFormParameter(ConfigHTMLWriter.FORM_FIELD_INDEX));
		Service service = null;

		//check to see which type of service we're using then check for null
		if (ConfigHTMLWriter.FORM_VALUE_SUBMIT_CONNECTION_MANAGER.equals(submit)) {
			if (openejb.getConnectionManager() == null) {
				service = new ConnectionManager();
				openejb.setConnectionManager((ConnectionManager) service);
			} else {
				service = openejb.getConnectionManager();
			}
		} else if (ConfigHTMLWriter.FORM_VALUE_SUBMIT_PROXY_FACTORY.equals(submit)) {
			if (openejb.getProxyFactory() == null) {
				service = new ProxyFactory();
				openejb.setProxyFactory((ProxyFactory) service);
			} else {
				service = openejb.getProxyFactory();
			}
		} else if (ConfigHTMLWriter.FORM_VALUE_SUBMIT_SECURITY_SERVICE.equals(submit)) {
			if (openejb.getSecurityService() == null) {
				service = new SecurityService();
				openejb.setSecurityService((SecurityService) service);
			} else {
				service = openejb.getSecurityService();
			}
		} else if (ConfigHTMLWriter.FORM_VALUE_SUBMIT_TRANSACTION_SERVICE.equals(submit)) {
			if (openejb.getTransactionService() == null) {
				service = new TransactionService();
				openejb.setTransactionService((TransactionService) service);
			} else {
				service = openejb.getTransactionService();
			}
		} else if (ConfigHTMLWriter.FORM_VALUE_SUBMIT_JNDI_PROVIDER.equals(submit)) {
			if (index > -1) {
				service = openejb.getJndiProvider(index);
			} else {
				service = new JndiProvider();
			}
		} else if (ConfigHTMLWriter.FORM_VALUE_SUBMIT_RESOURCE.equals(submit)) {
			if (index > -1) {
				service = openejb.getResource(index);
			} else {
				service = new Resource();
			}
		} else {
			throw new IOException("Invalid Service type");
		}

		//set the data on the service
		if (!"".equals(content.trim())) {
			service.setContent(content.trim());
		}
		if (!"".equals(id.trim())) {
			service.setId(id.trim());
		}
		if (!"".equals(jar.trim())) {
			service.setJar(jar.trim());
		}
		if (!"".equals(provider.trim())) {
			service.setProvider(provider.trim());
		}

		try { //validate the service
			service.validate();
		} catch (ValidationException e) {
			//print out the error message
			body.print("<font color=\"red\">You must fix the following errors before continuing.<br>\n<b>");
			body.print(e.getMessage());
			body.println("</b></font><br><br>");
			ConfigHTMLWriter.writeService(body, service, handleFile, submit, index);
			return;
		}

		//after validation, add new services to the array
		if (ConfigHTMLWriter.FORM_VALUE_SUBMIT_JNDI_PROVIDER.equals(submit)) {
			if (index == -1) 
				openejb.addJndiProvider((JndiProvider) service);
		} else if (ConfigHTMLWriter.FORM_VALUE_SUBMIT_RESOURCE.equals(submit)) {
			if (index == -1) 
				openejb.addResource((Resource) service);
		}

		ConfigHTMLWriter.writeOpenejb(body, openejb, handleFile, configLocation);
	}

	/**
	 * This method submits the main Openejb object and writes it to the file
	 * 
	 * @param openejb the openejb object
	 * @param configLocation the location of the configuration file
	 * @throws IOException if the changes could not be written
	 * @return the message of where the changes were written to
	 */
	private String submitOpenejb(String configLocation, Openejb openejb) throws IOException {
		FileWriter writer = new FileWriter(configLocation);

		try {
			openejb.marshal(writer);
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		}

		return "Your changes were written to: " + configLocation;
	}

	/** 
	 * gets an object reference and handle 
	 * 
	 * @param configurationData the object to create a handle from
	 * @return an absolute path of the handle file
	 * @throws IOException if the file cannot be created
	 */
	private String createHandle(ConfigurationDataObject configurationData) throws IOException {
		//write the handle out to a file
		File myHandleFile = new File(FileUtils.createTempDirectory().getAbsolutePath() + HANDLE_FILE);
		if (!myHandleFile.exists()) {
			myHandleFile.createNewFile();
		}

		ObjectOutputStream objectOut = new ObjectOutputStream(new FileOutputStream(myHandleFile));
		objectOut.writeObject(configurationData.getHandle()); //writes the handle to the file
		objectOut.flush();
		objectOut.close();

		return myHandleFile.getAbsolutePath();
	}

	/** 
	 * creates a new ConfigurationDataObject 
	 * 
	 * @return a new configuration data object
	 * @throws IOException if the object cannot be created
	 */
	private ConfigurationDataObject getConfigurationObject() throws IOException {
		//lookup the bean
		try {
			InitialContext ctx = new InitialContext();
			Object obj = ctx.lookup("config/webadmin/ConfigurationData");
			//create a new instance
			ConfigurationDataHome home =
				(ConfigurationDataHome) PortableRemoteObject.narrow(obj, ConfigurationDataHome.class);
			return home.create();
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		}
	}

	/** 
	 * this method gets the deployer handle 
	 * 
	 * @param handleFile the handle to the object
	 * @return the configuration data object
	 * @throws IOException if the file is not found
	 */
	private ConfigurationDataObject getHandle(String handleFile) throws IOException {
		File myHandleFile = new File(handleFile);

		//get the object
		ObjectInputStream objectIn = new ObjectInputStream(new FileInputStream(myHandleFile));
		//get the handle
		Handle configurationHandle;
		try {
			configurationHandle = (Handle) objectIn.readObject();
			return (ConfigurationDataObject) configurationHandle.getEJBObject();
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		}
	}
    public final static String CB_CLASS_NAME = "org.openejb.core.jms.JmsConnectionBuilder";
    public final static String IM_CLASS_NAME = "InstanceManager";
    public final static String IM_TIME_OUT = "TimeOut";
    public final static String IM_PASSIVATOR_PATH_PREFIX = "org/openejb/core/InstanceManager/PASSIVATOR_PATH_PREFIX";
    public final static String IM_POOL_SIZE = "PoolSize";
    public final static String IM_PASSIVATE_SIZE = "BulkPassivate";
    public final static String IM_PASSIVATOR = "Passivator";
    public final static String IM_CONCURRENT_ATTEMPTS = "org/openejb/core/InstanceManager/CONCURRENT_ATTEMPTS";
    public final static String IM_STRICT_POOLING = "StrictPooling";
    public final static String THREAD_CONTEXT_IMPL = "org/openejb/core/ThreadContext/IMPL_CLASS";
    public final static String INTRA_VM_COPY = "org/openejb/core/ivm/BaseEjbProxyHandler/INTRA_VM_COPY";
    public static final String JDBC_DRIVER = "JdbcDriver";
    public static final String JDBC_URL = "JdbcUrl";
    public static final String USER_NAME = "UserName";
    public static final String PASSWORD = "Password";
    public static final String GLOBAL_TX_DATABASE = "Global_TX_Database";
    public static final String LOCAL_TX_DATABASE = "Local_TX_Database";
}