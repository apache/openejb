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
package org.openejb.admin.web.config;

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
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import org.openejb.OpenEJBException;
import org.openejb.admin.web.HttpRequest;
import org.openejb.admin.web.HttpResponse;
import org.openejb.admin.web.WebAdminBean;
import org.openejb.alt.config.Bean;
import org.openejb.alt.config.ConfigUtils;
import org.openejb.alt.config.sys.Connector;
import org.openejb.alt.config.sys.Container;
import org.openejb.alt.config.sys.Openejb;
import org.openejb.core.EnvProps;
import org.openejb.util.FileUtils;
import org.openejb.util.StringUtilities;
import org.openorb.compiler.doc.html.content;

/** This bean allows the user to graphicly edit the OpenEJB configuration
 *  TODO: Add validation to all sections
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:tim_urberg@yahoo.com">Tim Urberg</a>
 */
public class ConfigBean extends WebAdminBean {
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
		Openejb openejbConfig;
		ConfigurationDataObject configurationData;
		String configLocation = System.getProperty("openejb.configuration");

		String type = request.getQueryParameter(ConfigHTMLWriter.QUERY_PARAMETER_TYPE);
		String method = request.getQueryParameter(ConfigHTMLWriter.QUERY_PARAMETER_METHOD);
		String handleFile = request.getFormParameter(ConfigHTMLWriter.FORM_FIELD_HANDLE_FILE);
		String submitOpenejb = request.getFormParameter(ConfigHTMLWriter.FORM_FIELD_SUBMIT_OPENEJB);
		String submitConnector = request.getFormParameter(ConfigHTMLWriter.FORM_FIELD_SUBMIT_CONNECTOR);
		String submitContainer = request.getFormParameter(ConfigHTMLWriter.FORM_FIELD_SUBMIT_CONTAINER);
		String containerType = request.getFormParameter(ConfigHTMLWriter.FORM_FIELD_CONTAINER_TYPE);

		if (handleFile == null) {
			configurationData = getConfigurationObject();
			handleFile = createHandle(configurationData);
			try {
				openejbConfig = ConfigUtils.readConfig(configLocation);
			} catch (OpenEJBException e) {
				throw new IOException(e.getMessage());
			}
		} else {
			configurationData = getHandle(handleFile);
			openejbConfig = configurationData.getOpenejb();
		}

		if (submitOpenejb != null) {
			body.println(submitOpenejb(configLocation, openejbConfig));
			return;
		} else if (submitConnector != null) {
			submitConnector(body, openejbConfig, handleFile, configLocation);
		} else if (containerType != null) {
			submitContainer(body, openejbConfig, handleFile, configLocation);
		} else if (ConfigHTMLWriter.TYPE_CONNECTOR.equals(type)) {
			beginConnector(method, handleFile, body, openejbConfig, configLocation);
		} else if (ConfigHTMLWriter.TYPE_CONTAINER.equals(type)) {
			beginContainer(method, handleFile, body, openejbConfig, configLocation);
		} else {
			ConfigHTMLWriter.writeOpenejb(body, openejbConfig, handleFile, configLocation);
		}

		configurationData.setOpenejb(openejbConfig);
	}

	private void beginConnector(String method, String handleFile, PrintWriter body, Openejb openejb, String configLocation)
		throws IOException {
		String connectorId = request.getFormParameter(ConfigHTMLWriter.TYPE_CONNECTOR);
		Connector[] connectors = openejb.getConnector();
		int connectorIndex = -1;

		connectorId = (connectorId == null) ? "" : connectorId;
		for (int i = 0; i < connectors.length; i++) {
			if (connectorId.equals(connectors[i].getId())) {
				connectorIndex = i;
				break;
			}
		}

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

	private void beginContainer(String method, String handleFile, PrintWriter body, Openejb openejb, String configLocation)
		throws IOException {
		String containerId = request.getFormParameter(ConfigHTMLWriter.TYPE_CONTAINER);
		Container[] containers = openejb.getContainer();
		int containerIndex = -1;
		Properties properties = new Properties();

		containerId = (containerId == null) ? "" : containerId;
		for (int i = 0; i < containers.length; i++) {
			if (containerId.equals(containers[i].getId())) {
				containerIndex = i;
				break;
			}
		}

		if (ConfigHTMLWriter.CREATE.equals(method)) {
			ConfigHTMLWriter.writeContainer(body, new ContainerData(), handleFile);
		} else if (ConfigHTMLWriter.EDIT.equals(method)) {
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

				data.setBulkPassivate(properties.getProperty(EnvProps.IM_PASSIVATE_SIZE, ""));
				data.setGlobalTxDatabase(properties.getProperty(EnvProps.GLOBAL_TX_DATABASE, ""));
				data.setIndex(containerIndex);
				data.setLocalTxDatabase(properties.getProperty(EnvProps.LOCAL_TX_DATABASE, ""));
				data.setPassivator(properties.getProperty(EnvProps.IM_PASSIVATOR, ""));
				data.setPoolSize(properties.getProperty(EnvProps.IM_POOL_SIZE, ""));
				data.setStrictPooling(properties.getProperty(EnvProps.IM_STRICT_POOLING, "true"));
				data.setTimeOut(properties.getProperty(EnvProps.IM_TIME_OUT, ""));
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

	private void submitConnector(PrintWriter body, Openejb openejbConfig, String handleFile, String configLocation) {
		Connector connector;

		String id = request.getFormParameter(ConfigHTMLWriter.FORM_FIELD_ID).trim();
		String jar = request.getFormParameter(ConfigHTMLWriter.FORM_FIELD_JAR).trim();
		String provider = request.getFormParameter(ConfigHTMLWriter.FORM_FIELD_PROVIDER).trim();
		String jdbcDriver = request.getFormParameter(EnvProps.JDBC_DRIVER).trim();
		String jdbcUrl = request.getFormParameter(EnvProps.JDBC_URL).trim();
		String userName = request.getFormParameter(EnvProps.USER_NAME).trim();
		String password = request.getFormParameter(EnvProps.PASSWORD).trim();
		int index = Integer.parseInt(request.getFormParameter(ConfigHTMLWriter.FORM_FIELD_INDEX).trim());
		StringBuffer contentBuffer = new StringBuffer(125);

		jar = ("".equals(jar)) ? null : jar;
		provider = ("".equals(provider)) ? null : provider;

		if (index > -1) {
			connector = openejbConfig.getConnector(index);
		} else {
			connector = new Connector();
			openejbConfig.addConnector(connector);
		}

		if (!"".equals(jdbcDriver))
			contentBuffer.append(EnvProps.JDBC_DRIVER).append(" ").append(jdbcDriver).append('\n');
		if (!"".equals(jdbcUrl))
			contentBuffer.append(EnvProps.JDBC_URL).append(" ").append(jdbcUrl).append('\n');
		if (!"".equals(userName))
			contentBuffer.append(EnvProps.USER_NAME).append(" ").append(userName).append('\n');
		if (!"".equals(password))
			contentBuffer.append(EnvProps.PASSWORD).append(" ").append(password).append('\n');

		connector.setId(id);
		connector.setJar(jar);
		connector.setProvider(provider);
		connector.setContent((contentBuffer.length() > 0) ? contentBuffer.toString() : null);

		body.print("<font color=\"red\"><b>");
		body.print("Connector ");
		body.print(id);
		body.print(
			" was saved.  Note: if the id was changed, all beans that use this connector will also need to be changed.</b></font><br><br>");

		ConfigHTMLWriter.writeOpenejb(body, openejbConfig, handleFile, configLocation);
	}

	private void submitContainer(PrintWriter body, Openejb openejbConfig, String handleFile, String configLocation)
		throws IOException {
		ContainerData data = new ContainerData();
		int index = Integer.parseInt(request.getFormParameter(ConfigHTMLWriter.FORM_FIELD_INDEX));
		String submit = request.getFormParameter(ConfigHTMLWriter.FORM_FIELD_SUBMIT_CONTAINER);
		Container container;
		StringBuffer contentBuffer = new StringBuffer(125);

		data.setBulkPassivate(StringUtilities.nullToBlankString(request.getFormParameter(EnvProps.IM_PASSIVATE_SIZE)).trim());
		data.setContainerType(
			StringUtilities.nullToBlankString(request.getFormParameter(ConfigHTMLWriter.FORM_FIELD_CONTAINER_TYPE)).trim());
		data.setGlobalTxDatabase(
			StringUtilities.nullToBlankString(request.getFormParameter(EnvProps.GLOBAL_TX_DATABASE)).trim());
		data.setId(StringUtilities.nullToBlankString(request.getFormParameter(ConfigHTMLWriter.FORM_FIELD_ID)).trim());
		data.setIndex(index);
		data.setJar(StringUtilities.nullToBlankString(request.getFormParameter(ConfigHTMLWriter.FORM_FIELD_JAR)).trim());
		data.setLocalTxDatabase(
			StringUtilities.nullToBlankString(request.getFormParameter(EnvProps.LOCAL_TX_DATABASE)).trim());
		data.setPassivator(StringUtilities.nullToBlankString(request.getFormParameter(EnvProps.IM_PASSIVATOR)).trim());
		data.setPoolSize(StringUtilities.nullToBlankString(request.getFormParameter(EnvProps.IM_POOL_SIZE)).trim());
		data.setProvider(
			StringUtilities.nullToBlankString(request.getFormParameter(ConfigHTMLWriter.FORM_FIELD_PROVIDER)).trim());
		data.setStrictPooling(StringUtilities.nullToBlankString(request.getFormParameter(EnvProps.IM_STRICT_POOLING)).trim());
		data.setTimeOut(StringUtilities.nullToBlankString(request.getFormParameter(EnvProps.IM_TIME_OUT)).trim());

		if (submit == null) {
			ConfigHTMLWriter.writeContainer(body, data, handleFile);
		} else {
			if (index > -1) {
				container = openejbConfig.getContainer(index);
			} else {
				container = new Container();
				openejbConfig.addContainer(container);
			}

			container.setCtype(data.getContainerType());
			container.setId(data.getId());
			if (!"".equals(data.getJar()))
				container.setJar(data.getJar());
			if ("".equals(data.getProvider()))
				container.setProvider(data.getProvider());

			//construct the contents based on type
			if (Bean.CMP_ENTITY.equals(data.getContainerType())) {
				if (!"".equals(data.getPoolSize()))
					contentBuffer.append(EnvProps.IM_POOL_SIZE).append(" ").append(data.getPoolSize()).append('\n');
				if (!"".equals(data.getGlobalTxDatabase()))
					contentBuffer.append(EnvProps.GLOBAL_TX_DATABASE).append(" ").append(data.getGlobalTxDatabase()).append('\n');
				if (!"".equals(data.getLocalTxDatabase()))
					contentBuffer.append(EnvProps.LOCAL_TX_DATABASE).append(" ").append(data.getLocalTxDatabase()).append('\n');
			} else if (Bean.STATEFUL.equals(data.getContainerType())) {
				if (!"".equals(data.getPassivator()))
					contentBuffer.append(EnvProps.IM_PASSIVATOR).append(" ").append(data.getPassivator()).append('\n');
				if (!"".equals(data.getTimeOut()))
					contentBuffer.append(EnvProps.IM_TIME_OUT).append(" ").append(data.getTimeOut()).append('\n');
				if (!"".equals(data.getPoolSize()))
					contentBuffer.append(EnvProps.IM_POOL_SIZE).append(" ").append(data.getPoolSize()).append('\n');
				if (!"".equals(data.getBulkPassivate()))
					contentBuffer.append(EnvProps.IM_PASSIVATE_SIZE).append(" ").append(data.getBulkPassivate()).append('\n');
			} else if (Bean.STATELESS.equals(data.getContainerType())) {
				if (!"".equals(data.getTimeOut()))
					contentBuffer.append(EnvProps.IM_TIME_OUT).append(" ").append(data.getTimeOut()).append('\n');
				if (!"".equals(data.getPoolSize()))
					contentBuffer.append(EnvProps.IM_POOL_SIZE).append(" ").append(data.getPoolSize()).append('\n');
				if (!"".equals(data.getStrictPooling()))
					contentBuffer.append(EnvProps.IM_STRICT_POOLING).append(" ").append(data.getStrictPooling()).append('\n');

				container.setContent((contentBuffer.length() > 0) ? contentBuffer.toString() : null);
			}

			ConfigHTMLWriter.writeOpenejb(body, openejbConfig, handleFile, configLocation);
		}
	}

	private String submitOpenejb(String configLocation, Openejb openejb) throws IOException {
		String content = request.getFormParameter(ConfigHTMLWriter.TYPE_OPENEJB_CONTENT);
		FileWriter writer = new FileWriter(configLocation);
		openejb.setContent(content);

		//TODO: validate object and give the user an error message for invalid info
		try {
			openejb.marshal(writer);
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		}

		return "Your changes were written to: " + configLocation;
	}

	/** gets an object reference and handle */
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

	private ConfigurationDataObject getConfigurationObject() throws IOException {
		Properties p = new Properties();
		p.put(Context.INITIAL_CONTEXT_FACTORY, "org.openejb.core.ivm.naming.InitContextFactory");

		//lookup the bean
		try {
			InitialContext ctx = new InitialContext(p);
			Object obj = ctx.lookup("webadmin/ConfigurationData");
			//create a new instance
			ConfigurationDataHome home = (ConfigurationDataHome) PortableRemoteObject.narrow(obj, ConfigurationDataHome.class);
			return home.create();
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		}
	}

	/** this function gets the deployer handle */
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
}