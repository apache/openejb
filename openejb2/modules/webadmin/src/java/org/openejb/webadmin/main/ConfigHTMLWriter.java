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
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import org.openejb.config.Bean;
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
import org.openejb.util.HtmlUtilities;
import org.openejb.util.StringUtilities;

/**
 * This class is a utility for the ConfigBean.  It takes care of printing
 * out all the HTML for the bean.
 * 
 * @see org.openejb.webadmin.config.ConfigBean
 */
public class ConfigHTMLWriter {
	/** create url type */
	public static final String CREATE = "Create";
	/** edit url type */
	public static final String EDIT = "Edit";
	/** delete url type */
	public static final String DELETE = "Delete";
	/** Connector object type */
	public static final String TYPE_CONNECTOR = "Connector";
	/** Container object type */
	public static final String TYPE_CONTAINER = "Container";
	/** Deployments object type */
	public static final String TYPE_DEPLOYMENTS = "Deployments";
	/** JndiProvider object type */
	public static final String TYPE_JNDI_PROVIDER = "JndiProvider";
	/** Resource object type */
	public static final String TYPE_RESOURCE = "Resource";
	/** Connection Manager object type */
	public static final String TYPE_CONNECTION_MANAGER = "ConnectionManager";
	/** Proxy Factory object type */
	public static final String TYPE_PROXY_FACTORY = "ProxyFactory";
	/** Securty Service object type */
	public static final String TYPE_SECURITY_SERVICE = "SecurityService";
	/** Transaction Service object type */
	public static final String TYPE_TRANSACTION_SERVICE = "TransactionService";
	/** type query parameter */
	public static final String QUERY_PARAMETER_TYPE = "type";
	/** method query parameter */
	public static final String QUERY_PARAMETER_METHOD = "method";
	/** handle file form field name */
	public static final String FORM_FIELD_HANDLE_FILE = "handleFile";
	/** id form field name */
	public static final String FORM_FIELD_ID = "id";
	/** jar form field name */
	public static final String FORM_FIELD_JAR = "jar";
	/** provider form field name */
	public static final String FORM_FIELD_PROVIDER = "provider";
	/** container type form field name */
	public static final String FORM_FIELD_CONTAINER_TYPE = "containerType";
	/** index form field name */
	public static final String FORM_FIELD_INDEX = "index";
	/** deployment type form field name */
	public static final String FORM_FIELD_DEPLOYMENT_TYPE = "deploymentType";
	/** deployment text form field name */
	public static final String FORM_FIELD_DEPLOYMENT_TEXT = "deploymentText";
	/** JNDI Parameters form field name */
	public static final String FORM_FIELD_JNDI_PARAMETERS = "jndiParameters";
	/** content form field name */
	public static final String FORM_FIELD_CONTENT = "content";
	/** submit button name for a service page */
	public static final String FORM_FIELD_SUBMIT_SERVICE = "submitService";
	/** submit button name for the main openejb page */
	public static final String FORM_FIELD_SUBMIT_OPENEJB = "submitOpenejb";
	/** submit button value and label for a connector */
	public static final String FORM_VALUE_SUBMIT_CONNECTOR = "Submit Connector";
	/** submit button value and label for a container */
	public static final String FORM_VALUE_SUBMIT_CONTAINER = "Submit Container";
	/** submit button value and label for deployments */
	public static final String FORM_VALUE_SUBMIT_DEPLOYMENTS = "Submit Deployments";
	/** submit button value and label for a JNDI provider */
	public static final String FORM_VALUE_SUBMIT_JNDI_PROVIDER = "Submit JNDI Provider";
	/** submit button value and label for a resource */
	public static final String FORM_VALUE_SUBMIT_RESOURCE = "Submit Resource";
	/** submit button value and label for a connection manager */
	public static final String FORM_VALUE_SUBMIT_CONNECTION_MANAGER = "Submit Connection Manager";
	/** submit button value and label for a proxy factory */
	public static final String FORM_VALUE_SUBMIT_PROXY_FACTORY = "Submit Proxy Factory";
	/** submit button value and label for a security service */
	public static final String FORM_VALUE_SUBMIT_SECURITY_SERVICE = "Submit Security Service";
	/** submit button value and label for a transaction service */
	public static final String FORM_VALUE_SUBMIT_TRANSACTION_SERVICE = "Submit Transaction Service";
	/** drop down value for a jar deployment type */
	public static final String DEPLOYMENT_TYPE_JAR = "jar";
	/** drop down value for a directory deployment type */
	public static final String DEPLOYMENT_TYPE_DIR = "dir";
	//link and display for the pop up help
	private static final String HELP_LINK_HREF = "javascript:popUpHelp('help/config/help.html')";
	private static final String HELP_LINK_DISPLAY = "(?)";

	private ConfigHTMLWriter() {} //no one should instantate this class

	/**
	 * This method takes care of writing the contents of the Openejb configuration object to
	 * the browser.  It takes in an Openejb object, gets all the parts and constructs the HTML
	 * 
	 * @see org.openejb.config.sys.Openejb
	 * @param body writes the HTML to the browser
	 * @param openejb the openejb object to write the contents of
	 * @param handle the location of the handle file string
	 * @param configLocation the location of the configuration file
	 */
	public static void writeOpenejb(PrintWriter body, Openejb openejb, String handle, String configLocation) {
		//get all the parts of the configuration
		Connector[] connectors = openejb.getConnector();
		Container[] containers = openejb.getContainer();
		Deployments[] deploymentsArray = openejb.getDeployments();
		JndiProvider[] jndiProviders = openejb.getJndiProvider();
		Resource[] resources = openejb.getResource();
		ConnectionManager connectionManager = openejb.getConnectionManager();
		ProxyFactory proxyFactory = openejb.getProxyFactory();
		SecurityService securityService = openejb.getSecurityService();
		TransactionService transactionService = openejb.getTransactionService();

		//print instructions
		body.println("This page allows you to configure your system.  The configuration file being used is:");
		body.print(configLocation);
		body.println(". Please pick one of the fields below to continue.  If you need help, click on the");
		body.println("question mark (?) next to the field.<br>");

		body.println(createTableHTMLDecleration());
		body.println(createTableHTML("Connector", "Container", true));

		//create the connectors
		if (connectors != null && connectors.length > 0) {
			body.println(HtmlUtilities.createSelectFormField(TYPE_CONNECTOR, null));
			for (int i = 0; i < connectors.length; i++) {
				body.println(HtmlUtilities.createSelectOption(connectors[i].getId(), connectors[i].getId(), false));
			}
			body.println("</select>");
		} else {
			body.println("No Connectors");
		}
		body.println("</td>\n<td>");

		//create the container list
		if (containers != null && containers.length > 0) {
			body.println(HtmlUtilities.createSelectFormField(TYPE_CONTAINER, null));
			for (int i = 0; i < containers.length; i++) {
				body.println(HtmlUtilities.createSelectOption(containers[i].getId(), containers[i].getId(), false));
			}
			body.println("</select>");
		} else {
			body.println("No Containers");
		}

		body.println("</td>\n</tr>\n<tr>\n<td>");

		//print the create, edit and delete urls
		body.println(createCEDUrl(TYPE_CONNECTOR, CREATE));
		if (connectors != null && connectors.length > 0) {
			body.println(createCEDUrl(TYPE_CONNECTOR, EDIT));
			body.println(createCEDUrl(TYPE_CONNECTOR, DELETE));
		}

		body.println("</td>\n<td>");

		//print the create, edit and delete urls
		body.println(createCEDUrl(TYPE_CONTAINER, CREATE));
		if (containers != null && containers.length > 0) {
			body.println(createCEDUrl(TYPE_CONTAINER, EDIT));
			body.println(createCEDUrl(TYPE_CONTAINER, DELETE));
		}

		body.println(createTableHTML("Deployments", "JNDI Provider", false));

		//print the deployments
		if (deploymentsArray != null & deploymentsArray.length > 0) {
			body.println(HtmlUtilities.createSelectFormField(TYPE_DEPLOYMENTS, null));
			String deployment;
			for (int i = 0; i < deploymentsArray.length; i++) {
				if (deploymentsArray[i].getDir() != null) {
					deployment = deploymentsArray[i].getDir();
				} else {
					deployment = deploymentsArray[i].getJar();
				}
				body.println(HtmlUtilities.createSelectOption(deployment, deployment, false));
			}
			body.println("</select>");
		} else {
			body.println("No Deployments");
		}
		body.println("</td>\n<td>");

		//print the jndi provider list
		if (jndiProviders != null && jndiProviders.length > 0) {
			body.println(HtmlUtilities.createSelectFormField(TYPE_JNDI_PROVIDER, null));
			for (int i = 0; i < jndiProviders.length; i++) {
				body.println(HtmlUtilities.createSelectOption(jndiProviders[i].getId(), jndiProviders[i].getId(), false));
			}
		} else {
			body.println("No JNDI Providers");
		}

		body.println("</td>\n</tr>\n<tr>\n<td>");

		//print the create, edit and delete urls
		body.println(createCEDUrl(TYPE_DEPLOYMENTS, CREATE));
		if (deploymentsArray != null & deploymentsArray.length > 0) {
			body.println(createCEDUrl(TYPE_DEPLOYMENTS, EDIT));
			body.println(createCEDUrl(TYPE_DEPLOYMENTS, DELETE));
		}

		body.println("</td>\n<td>");

		//print the create, edit and delete urls
		body.println(createCEDUrl(TYPE_JNDI_PROVIDER, CREATE));
		if (jndiProviders != null && jndiProviders.length > 0) {
			body.println(createCEDUrl(TYPE_JNDI_PROVIDER, EDIT));
			body.println(createCEDUrl(TYPE_JNDI_PROVIDER, DELETE));
		}

		body.println(createTableHTML("Resource", "Connection Manager", false));

		//print the resources
		if (resources != null && resources.length > 0) {
			body.println(HtmlUtilities.createSelectFormField(TYPE_RESOURCE, null));
			for (int i = 0; i < resources.length; i++) {
				body.println(HtmlUtilities.createSelectOption(resources[i].getId(), resources[i].getId(), false));
			}
			body.println("</select>");
		} else {
			body.println("No Resources");
		}
		body.println("</td>\n<td>");

		if (connectionManager != null) {
			body.println(connectionManager.getId());
		} else {
			body.println("No Connection Manager");
		}

		//print the create, edit and delete urls
		body.println("</td>\n</tr>\n<tr>\n<td>");
		body.println(createCEDUrl(TYPE_RESOURCE, CREATE));
		if (resources != null && resources.length > 0) {
			body.println(createCEDUrl(TYPE_RESOURCE, EDIT));
			body.println(createCEDUrl(TYPE_RESOURCE, DELETE));
		}

		body.println("</td>\n<td>");

		//print the create, edit and delete urls
		if (connectionManager == null) {
			body.println(createCEDUrl(TYPE_CONNECTION_MANAGER, CREATE));
		} else {
			body.println(createCEDUrl(TYPE_CONNECTION_MANAGER, EDIT));
			body.println(createCEDUrl(TYPE_CONNECTION_MANAGER, DELETE));
		}

		body.println(createTableHTML("Proxy Factory", "Security Service", false));

		if (proxyFactory != null) {
			body.println(proxyFactory.getId());
		} else {
			body.println("No Proxy Factory");
		}

		body.println("</td>\n<td>");

		if (securityService != null) {
			body.println(securityService.getId());
		} else {
			body.println("No Security Service");
		}

		body.println("</td>\n</tr>\n<tr>\n<td>");

		//print the create, edit and delete urls
		if (proxyFactory == null) {
			body.println(createCEDUrl(TYPE_PROXY_FACTORY, CREATE));
		} else {
			body.println(createCEDUrl(TYPE_PROXY_FACTORY, EDIT));
			body.println(createCEDUrl(TYPE_PROXY_FACTORY, DELETE));
		}

		//print the create, edit and delete urls
		body.println("</td>\n<td>");
		if (securityService == null) {
			body.println(createCEDUrl(TYPE_SECURITY_SERVICE, CREATE));
		} else {
			body.println(createCEDUrl(TYPE_SECURITY_SERVICE, EDIT));
			body.println(createCEDUrl(TYPE_SECURITY_SERVICE, DELETE));
		}

		body.println(createTableHTML("Transaction Service", "&nbsp;", false));

		if (transactionService != null) {
			body.println(transactionService.getId());
		} else {
			body.println("No Transaction Service");
		}

		//print the create, edit and delete urls
		body.println("</td>\n<td>&nbsp;</td>\n</tr>\n<tr>\n<td>");
		if (transactionService == null) {
			body.println(createCEDUrl(TYPE_TRANSACTION_SERVICE, CREATE));
		} else {
			body.println(createCEDUrl(TYPE_TRANSACTION_SERVICE, EDIT));
			body.println(createCEDUrl(TYPE_TRANSACTION_SERVICE, DELETE));
		}

		//print the buttons and hidden form fields
		body.println("</td><td>&nbsp;</td></tr>\n<tr>\n<td colspan=\"2\">&nbsp;</td>\n</tr>");
		body.println("<tr>\n<td colspan=\"2\">");
		body.println(HtmlUtilities.createSubmitFormButton(FORM_FIELD_SUBMIT_OPENEJB, "Write Changes"));
		body.println(HtmlUtilities.createHiddenFormField(FORM_FIELD_HANDLE_FILE, handle));
		body.println("</td>\n</tr>\n</table>\n</form>");
	}

	/**
	 * This method writes out the contents of the Connector object to the
	 * browser
	 * 
	 * @see org.openejb.config.sys.Connector
	 * @param body writes the HTML to the browser
	 * @param connector the connector object to write the contents of
	 * @param handle the location of the handle file string
	 * @param index the index of the connector array
	 */
	public static void writeConnector(PrintWriter body, Connector connector, String handle, int index) throws IOException {
		String id = "";
		String jar = "";
		String provider = "";
		String jdbcDriver = "";
		String jdbcUrl = "";
		String username = "";
		String password = "";
		Properties contentProps = new Properties();

		//set the variables to the connector contents if the connector is
		//not null
		if (connector != null) {
			id = StringUtilities.nullToBlankString(connector.getId());
			jar = StringUtilities.nullToBlankString(connector.getJar());
			provider = StringUtilities.nullToBlankString(connector.getProvider());

			if (connector.getContent() != null) {
				ByteArrayInputStream in =
					new ByteArrayInputStream(StringUtilities.nullToBlankString(connector.getContent()).getBytes());
				contentProps.load(in);
			}

			jdbcDriver = contentProps.getProperty(ConfigBean.JDBC_DRIVER, "");
			jdbcUrl = contentProps.getProperty(ConfigBean.JDBC_URL, "");
			username = contentProps.getProperty(ConfigBean.USER_NAME, "");
			password = contentProps.getProperty(ConfigBean.PASSWORD, "");
		}

		//print instructions
		body.println("Please enter the fields below for a connector.  If you need help, click on the question");
		body.println("mark beside the field.  The bold fields are required.<br>");

		//write out the table rows and the fields
		body.println(createTableHTMLDecleration());
		body.println(printFormRow("Id", FORM_FIELD_ID, id, 30, true));
		body.println(printFormRow("Jar", FORM_FIELD_JAR, jar, 30, false));
		body.println(printFormRow("Provider", FORM_FIELD_PROVIDER, provider, 30, false));
		body.println(printFormRow("JDBC Driver", ConfigBean.JDBC_DRIVER, jdbcDriver, 30, false));
		body.println(printFormRow("JDBC URL", ConfigBean.JDBC_URL, jdbcUrl, 30, false));
		body.println(printFormRow("Username", ConfigBean.USER_NAME, username, 30, false));
		body.println(printFormRow("Password", ConfigBean.PASSWORD, password, 30, false));

		//write out the buttons and hidden form fields
		body.println("<tr>\n<td colspan=\"2\">&nbsp;</td>\n</tr>");
		body.print("<tr>\n<td colspan=\"2\">");
		body.println(HtmlUtilities.createSubmitFormButton(FORM_FIELD_SUBMIT_SERVICE, FORM_VALUE_SUBMIT_CONNECTOR));
		body.println(HtmlUtilities.createHiddenFormField(FORM_FIELD_HANDLE_FILE, handle));
		body.println(HtmlUtilities.createHiddenFormField(FORM_FIELD_INDEX, String.valueOf(index)));
		body.println("</td>\n</tr>\n</table>\n</form>");
	}

	/**
	 * This method writes out the contents of the Container object to the
	 * browser
	 * 
	 * @see org.openejb.config.sys.Container
	 * @param body writes the HTML to the browser
	 * @param container the container to write the contents of
	 * @param handle the location of the handle file string
	 * @param index the index of the connector array
	 */
	public static void writeContainer(PrintWriter body, ContainerData containerData, String handle) throws IOException {
		Properties properties = new Properties();
		String[] containerTypes = { Bean.CMP_ENTITY, Bean.BMP_ENTITY, Bean.STATEFUL, Bean.STATELESS };
		String containerType = containerData.getContainerType();

		//print instructions
		body.println("Please enter the fields below for a container.  If you need help, click on the question");
		body.println("mark beside the field.  The bold fields are required.<br>");
		body.println(createTableHTMLDecleration());
		body.print("<tr>\n<td><b>Container</b> ");
		body.print(HtmlUtilities.createAnchor(HELP_LINK_HREF, HELP_LINK_DISPLAY, HtmlUtilities.ANCHOR_HREF_TYPE));
		body.println("</td>\n<td>");

		//check to see if we're editing or creating a container
		if (containerData.isEdit()) {
			//hard code the container type, it can't be changed
			body.println(containerType);
			body.println(HtmlUtilities.createHiddenFormField(FORM_FIELD_CONTAINER_TYPE, containerType));
		} else {
			//create a drop down of container types
			body.println(
				HtmlUtilities.createSelectFormField(FORM_FIELD_CONTAINER_TYPE, "submitForm(this.form, 'Configuration')"));
			for (int i = 0; i < containerTypes.length; i++) {
				body.println(
					HtmlUtilities.createSelectOption(
						containerTypes[i],
						containerTypes[i],
						containerTypes[i].equals(containerType)));
			}
			body.println("</select>");
			if ("".equals(containerType))
				containerType = Bean.CMP_ENTITY;
		}
		body.println("</td>\n</tr>");

		//print the standard service types
		body.println(printFormRow("Id", FORM_FIELD_ID, containerData.getId(), 30, true));
		body.println(printFormRow("Jar", FORM_FIELD_JAR, containerData.getJar(), 30, false));
		body.println(printFormRow("Provider", FORM_FIELD_PROVIDER, containerData.getProvider(), 30, false));

		//check for which type of container we're writing and print out the field types for each one
		if (Bean.CMP_ENTITY.equals(containerType)) {
			body.println(
				printFormRow(
					"Global Database File",
					ConfigBean.GLOBAL_TX_DATABASE,
					containerData.getGlobalTxDatabase(),
					35,
					true));
			body.println(
				printFormRow(
					"Local Database File",
					ConfigBean.LOCAL_TX_DATABASE,
					containerData.getLocalTxDatabase(),
					35,
					true));
			body.println(printFormRow("Pool Size", ConfigBean.IM_POOL_SIZE, containerData.getPoolSize(), 5, false));
		} else if (Bean.STATEFUL.equals(containerType)) {
			body.println(printFormRow("Passivator", ConfigBean.IM_PASSIVATOR, containerData.getPassivator(), 35, false));
			body.println(printFormRow("Time Out", ConfigBean.IM_TIME_OUT, containerData.getTimeOut(), 5, false));
			body.println(printFormRow("Pool Size", ConfigBean.IM_POOL_SIZE, containerData.getPoolSize(), 5, false));
			body.println(
				printFormRow("Bulk Passivate", ConfigBean.IM_PASSIVATE_SIZE, containerData.getBulkPassivate(), 5, false));
		} else if (Bean.STATELESS.equals(containerType)) {
			body.println(printFormRow("Time Out", ConfigBean.IM_TIME_OUT, containerData.getTimeOut(), 5, false));
			body.println(printFormRow("Pool Size", ConfigBean.IM_POOL_SIZE, containerData.getPoolSize(), 5, false));
			body.print("<tr>\n<td>Strict Pooling ");
			body.print(HtmlUtilities.createAnchor(HELP_LINK_HREF, HELP_LINK_DISPLAY, HtmlUtilities.ANCHOR_HREF_TYPE));
			body.println("</td>\n<td>");
			body.println(HtmlUtilities.createSelectFormField(ConfigBean.IM_STRICT_POOLING, null));
			body.println(HtmlUtilities.createSelectOption("true", "true", "true".equals(containerData.getStrictPooling())));
			body.println(
				HtmlUtilities.createSelectOption("false", "false", "false".equals(containerData.getStrictPooling())));
			body.println("</select>\n</td>\n</tr>");
		}

		//print out the buttons and hidden form fields
		body.println("<tr>\n<td colspan=\"2\">&nbsp;</td>\n</tr>");
		body.print("<tr>\n<td colspan=\"2\">");
		body.println(HtmlUtilities.createSubmitFormButton(FORM_FIELD_SUBMIT_SERVICE, FORM_VALUE_SUBMIT_CONTAINER));
		body.println(HtmlUtilities.createHiddenFormField(FORM_FIELD_HANDLE_FILE, handle));
		body.println(HtmlUtilities.createHiddenFormField(FORM_FIELD_INDEX, String.valueOf(containerData.getIndex())));
		body.println("</table>\n</form>");
	}

	/**
	 * This method writes out the contents of the Resource object to the
	 * browser
	 * 
	 * @see org.openejb.config.sys.Container
	 * @param body writes the HTML to the browser
	 * @param resource the resource to write the contents of
	 * @param handle the location of the handle file string
	 * @param index the index of the connector array
	 */
	public static void writeResource(PrintWriter body, Resource resource, String handle, int index) {
		String id = "";
		String jar = "";
		String provider = "";
		String jndi = "";
		String content = "";

		if (resource != null) {
			id = StringUtilities.nullToBlankString(resource.getId());
			jar = StringUtilities.nullToBlankString(resource.getJar());
			provider = StringUtilities.nullToBlankString(resource.getProvider());
			jndi = StringUtilities.nullToBlankString(resource.getJndi());
			content = StringUtilities.nullToBlankString(resource.getContent());
		}

		//print instructions and form fields
		body.println("Please enter the fields below for a resource.  If you need help, click on the question");
		body.println("mark beside the field.  The bold fields are required.<br>");
		body.println(createTableHTMLDecleration());
		body.println(printFormRow("Id", FORM_FIELD_ID, id, 30, true));
		body.println(printFormRow("Jar", FORM_FIELD_JAR, jar, 30, false));
		body.println(printFormRow("Provider", FORM_FIELD_PROVIDER, provider, 30, false));
		body.println(printFormRow("JNDI", FORM_FIELD_JNDI_PARAMETERS, jndi, 30, false));
		body.print("<tr>\n<td valign=\"top\">Content ");
		body.print(HtmlUtilities.createAnchor(HELP_LINK_HREF, HELP_LINK_DISPLAY, HtmlUtilities.ANCHOR_HREF_TYPE));
		body.println("</td>\n<td>");
		body.println(HtmlUtilities.createTextArea(FORM_FIELD_CONTENT, content, 5, 40, null, null, null));
		body.println("</td>\n</tr>\n<tr>\n<td colspan\"2\">&nbsp;</td>\n</tr>\n<tr>\n<td colspan=\"2\">");
		body.println(HtmlUtilities.createSubmitFormButton(FORM_FIELD_SUBMIT_SERVICE, FORM_VALUE_SUBMIT_RESOURCE));
		body.println(HtmlUtilities.createHiddenFormField(FORM_FIELD_HANDLE_FILE, handle));
		body.println(HtmlUtilities.createHiddenFormField(FORM_FIELD_INDEX, String.valueOf(index)));
		body.println("</table>\n</form>");

	}

	/**
	 * This method writes out the contents of the Deployments object to the
	 * browser
	 * 
	 * @see org.openejb.config.sys.Deployments
	 * @param body writes the HTML to the browser
	 * @param deployments the deployment info to write to the browser
	 * @param handle the location of the handle file string
	 * @param index the index of the connector array
	 */
	public static void writeDeployments(PrintWriter body, Deployments deployments, String handle, int index)
		throws IOException {
		String jarOrDir = null;
		boolean isDir = true;

		//loop through the deployments and see if we're printing
		//a jar or directory
		if (deployments != null) {
			if (deployments.getDir() != null) {
				jarOrDir = deployments.getDir();
				isDir = true;
			} else if (deployments.getJar() != null) {
				jarOrDir = deployments.getJar();
				isDir = false;
			}
		}

		jarOrDir = StringUtilities.nullToBlankString(jarOrDir);

		//print instructions and deployments information
		body.println("Please select a Jar or Directory below.  This field is required.<br><br>");
		body.println(createTableHTMLDecleration());

		body.println("<tr>\n<td>");
		body.println(HtmlUtilities.createSelectFormField(FORM_FIELD_DEPLOYMENT_TYPE, null));
		body.println(HtmlUtilities.createSelectOption(DEPLOYMENT_TYPE_JAR, "Jar File", !isDir));
		body.println(HtmlUtilities.createSelectOption(DEPLOYMENT_TYPE_DIR, "Directory", isDir));
		body.println("</select>\n</td>\n<td>");
		body.println(HtmlUtilities.createTextFormField(FORM_FIELD_DEPLOYMENT_TEXT, jarOrDir, 30, 0));
		body.println("</td>\n<tr>\n<td colspan=\"2\">&nbsp;</td>\n</tr>");
		body.println("</td>\n<tr>\n<td colspan=\"2\">");
		body.println(HtmlUtilities.createSubmitFormButton(FORM_FIELD_SUBMIT_SERVICE, FORM_VALUE_SUBMIT_DEPLOYMENTS));
		body.println(HtmlUtilities.createHiddenFormField(FORM_FIELD_HANDLE_FILE, handle));
		body.println(HtmlUtilities.createHiddenFormField(FORM_FIELD_INDEX, String.valueOf(index)));
		body.println("</td>\n</tr>\n</table>\n</form>");
	}

	/**
	 * This method writes out the contents of a generic service object to the
	 * browser
	 * 
	 * @see org.openejb.config.Service
	 * @param body writes the HTML to the browser
	 * @param service the service to write to the browser
	 * @param handle the location of the handle file string
	 * @param submitValue the value/display for the submit button
	 * @param index the index of the connector array
	 */
	public static void writeService(PrintWriter body, Service service, String handle, String submitValue, int index) {
		String id = "";
		String jar = "";
		String provider = "";
		String content = "";
		if (service != null) {
			id = StringUtilities.nullToBlankString(service.getId());
			jar = StringUtilities.nullToBlankString(service.getJar());
			provider = StringUtilities.nullToBlankString(service.getProvider());
			content = StringUtilities.nullToBlankString(service.getContent());
		}

		//print instructions and information to the browser
		body.println("Please enter the fields below.  If you need help, click on the question");
		body.println("mark beside the field.  The bold fields are required.<br>");
		body.println(createTableHTMLDecleration());
		body.println(printFormRow("Id", FORM_FIELD_ID, id, 30, true));
		body.println(printFormRow("Jar", FORM_FIELD_JAR, jar, 30, false));
		body.println(printFormRow("Provider", FORM_FIELD_PROVIDER, provider, 30, false));
		body.print("<tr>\n<td valign=\"top\">Content ");
		body.print(HtmlUtilities.createAnchor(HELP_LINK_HREF, HELP_LINK_DISPLAY, HtmlUtilities.ANCHOR_HREF_TYPE));
		body.println("</td>\n<td>");
		body.println(HtmlUtilities.createTextArea(FORM_FIELD_CONTENT, content, 5, 40, null, null, null));
		body.println("</td>\n</tr>\n<tr>\n<td colspan\"2\">&nbsp;</td>\n</tr>\n<tr>\n<td colspan=\"2\">");
		body.println(HtmlUtilities.createSubmitFormButton(FORM_FIELD_SUBMIT_SERVICE, submitValue));
		body.println(HtmlUtilities.createHiddenFormField(FORM_FIELD_HANDLE_FILE, handle));
		body.println(HtmlUtilities.createHiddenFormField(FORM_FIELD_INDEX, String.valueOf(index)));
		body.println("</table>\n</form>");
	}

	/**
	 * This method prints a two column table row with a label in the first
	 * column and a text form field in the second column 
	 * 
	 * @param label the label for the first column
	 * @param name the name of the text form field
	 * @param value the value of the text form field
	 * @param size the size of the text form field
	 * @param required true if this form field is required
	 * @return the construted HTML table row
	 */
	private static String printFormRow(String label, String name, String value, int size, boolean required) {
		StringBuffer temp = new StringBuffer(125).append("<tr>\n<td>");

		if (required) {
			temp.append("<b>").append(label).append("</b>");
		} else {
			temp.append(label);
		}

		return temp
			.append(HtmlUtilities.createAnchor(HELP_LINK_HREF, HELP_LINK_DISPLAY, HtmlUtilities.ANCHOR_HREF_TYPE))
			.append("</td>\n<td>")
			.append(HtmlUtilities.createTextFormField(name, value, size, 0))
			.append("</td>\n</tr>\n")
			.toString();
	}

	/** 
	 * This method creates a url for create, edit or delete.  
	 * 
	 * @param type the type of url (object type)
	 * @param method the method of the url (create, edit or delete)
	 * @return a HTML url string
	 */
	private static String createCEDUrl(String type, String method) {
		StringBuffer temp = new StringBuffer(150);

		//if we're deleting we want to confirm the delete with a Javascript confirm box
		if (DELETE.equals(method)) {
			temp
				.append("javascript:confirmSubmitForm(document.configForm, 'Configuration?")
				.append(QUERY_PARAMETER_TYPE)
				.append("=")
				.append(type)
				.append("&")
				.append(QUERY_PARAMETER_METHOD)
				.append("=")
				.append(method)
				.append("', 'Are you sure you want to delete this ")
				.append(type)
				.append("?\\nNote: changes will not be written until you click the Write Changes button.')");
		} else {
			temp
				.append("javascript:submitForm(document.configForm, 'Configuration?")
				.append(QUERY_PARAMETER_TYPE)
				.append("=")
				.append(type)
				.append("&")
				.append(QUERY_PARAMETER_METHOD)
				.append("=")
				.append(method)
				.append("')");
		}

		return HtmlUtilities.createAnchor(temp.toString(), method, HtmlUtilities.ANCHOR_HREF_TYPE);
	}

	/**
	 * This is a "helper" method for writing the main Openejb config section
	 * it creates HTML for the "in between" and label sections of the table
	 * 
	 * @param label1 the label of the first column
	 * @param label2 the label for the second column
	 * @param isTop this is only true for the first call to this method
	 * @return an HTML string for this table
	 */
	private static String createTableHTML(String label1, String label2, boolean isTop) {
		StringBuffer temp = new StringBuffer(225);

		//don't append this for the first call to this method
		if (!isTop) {
			temp.append("</td>\n</tr>\n");
		}

		temp.append("<tr>\n<td colspan=\"2\">&nbsp;</td>\n</tr>\n<tr>\n<td>");

		//we want a blank table cell
		if ("&nbsp;".equals(label1)) {
			temp.append(label1);
		} else {
			temp.append("<b>").append(label1).append("</b>").append(
				HtmlUtilities.createAnchor(HELP_LINK_HREF, HELP_LINK_DISPLAY, HtmlUtilities.ANCHOR_HREF_TYPE));
		}

		temp.append("</td>\n<td>");
		//we want a blank table cell
		if ("&nbsp;".equals(label2)) {
			temp.append(label2);
		} else {
			temp.append("<b>").append(label2).append("</b>").append(
				HtmlUtilities.createAnchor(HELP_LINK_HREF, HELP_LINK_DISPLAY, HtmlUtilities.ANCHOR_HREF_TYPE));
		}
		temp.append("</td>\n</tr>\n<tr>\n<td>");

		return temp.toString();
	}

	/**
	 * This method returns the table and form decleration
	 * @return  the table and form decleration
	 */
	private static String createTableHTMLDecleration() {
		return "<form action=\"Configuration\" method=\"post\" name=\"configForm\">\n"
			+ "<table border=\"0\" cellpadding=\"1\" cellspacing=\"1\" width=\"430\">";
	}
}