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
 */
package org.openejb.admin.web;

import java.io.File;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.ejb.SessionContext;

import org.openejb.DeploymentInfo;
import org.openejb.OpenEJB;
import org.openejb.OpenEJBException;
import org.openejb.alt.config.Bean;
import org.openejb.alt.config.ConfigUtils;
import org.openejb.alt.config.EjbJarUtils;
import org.openejb.alt.config.ejb11.EjbDeployment;
import org.openejb.alt.config.ejb11.EjbJar;
import org.openejb.alt.config.ejb11.EjbRef;
import org.openejb.alt.config.ejb11.MethodParams;
import org.openejb.alt.config.ejb11.OpenejbJar;
import org.openejb.alt.config.ejb11.QueryMethod;
import org.openejb.alt.config.ejb11.ResourceLink;
import org.openejb.alt.config.ejb11.ResourceRef;
import org.openejb.alt.config.sys.Connector;
import org.openejb.alt.config.sys.Container;
import org.openejb.alt.config.sys.Openejb;
import org.openejb.util.JarUtils;
import org.openejb.util.StringUtilities;

/**
 * This is a stateless session bean which handles the action of deployment for the
 * web administration.
 *
 * timu:
 *  1. Add better error handling 
 *  2. Add documentation 
 *
 * @author <a href="mailto:tim_urberg@yahoo.com">Tim Urberg</a>
 */
public class DeployerBean implements javax.ejb.SessionBean {
	//private boolean values
	/*  key for boolean values:
	 *  AUTO_ASSIGN              0
	 *  MOVE_JAR                 1
	 *  FORCE_OVERWRITE_JAR      2
	 *  COPY_JAR                 3
	 *  AUTO_CONFIG              4
	 *  GENERATE_DEPLOYMENT_ID   5
	 *  GENERATE_STUBS           6
	 */
	private boolean[] options = new boolean[7];

	//private variables
	private SessionContext context;
	private Openejb config;
	private String configFile = null;
	private boolean configChanged;
	private boolean autoAssign;
	private Container[] containers;
	private Connector[] resources;
	private Bean[] deployerBeans;
	private String jarFile;
	private StringBuffer deploymentHTML = new StringBuffer();
	private String containerDeployIdsHTML = "";
	private OpenejbJar openejbJar;
	private Vector beanList = new Vector();
	private boolean idsWritten = false;
	private ArrayList usedBeanNames = new ArrayList();

	/** Creates a new instance of DeployerBean */
	public void ejbCreate() {
		try {
			if (configFile == null) {
				try {
					configFile = System.getProperty("openejb.configuration");
				} catch (Exception e) {}
			}
			if (configFile == null) {
				configFile = ConfigUtils.searchForConfiguration();
			}
			config = ConfigUtils.readConfig(configFile);

			/* Load container list */
			containers = config.getContainer();

			/* Load resource list */
			resources = config.getConnector();

		} catch (Exception e) {
			// TODO: Better exception handling.
			e.printStackTrace();
		}
	}

	public void setBooleanValues(boolean[] booleanValues) {
		options = booleanValues;
	}

	public boolean[] getBooleanValues() {
		return options;
	}

	public void setJarFile(String jar) {
		jarFile = jar;
	}

	public String getJarFile() {
		return jarFile;
	}

	public String getDeploymentHTML() {
		return deploymentHTML.toString();
	}

	/** method which starts the deployment process */
	public void startDeployment() throws RemoteException {
		EjbJar jar = null;
		try { //test for invalid file
			jar = EjbJarUtils.readEjbJar(this.jarFile);
		} catch (OpenEJBException oe) {
			throw new RemoteException(this.jarFile + " is not a valid jar file. ");
		}

		//we don't want to perform this check if we're forcing overwrite
		if (!this.options[2]) {
			File tempJarFile = new File(this.jarFile);
			//check for existing jar
			File beansDir =
				new File(
					System.getProperty("openejb.home")
						+ System.getProperty("file.separator")
						+ "beans");

			File[] beans = beansDir.listFiles();
			for (int i = 0; i < beans.length; i++) {
				if (tempJarFile.getName().equalsIgnoreCase(beans[i].getName())) {
					throw new RemoteException(
						System.getProperty("openejb.home")
							+ System.getProperty("file.separator")
							+ "beans"
							+ System.getProperty("file.separator")
							+ tempJarFile.getName()
							+ " already exists.");
				}
			}
		}

		openejbJar = new OpenejbJar();
		deployerBeans = getBeans(jar);
	}

	/** sets the deployment and container ids */
	public void setDeployAndContainerIds(
		String deploymentId,
		String containerId,
		String[][] resourceRef,
		String[][] ejbRef,
		int i)
		throws RemoteException {

		//local variables
		EjbDeployment deployment = new EjbDeployment();
		ResourceLink link;

		if (this.usedBeanNames.contains(deploymentId)) {
			throw new RemoteException(
				"The deployment id: "
					+ deploymentId
					+ " is already being used by another bean, please choose another deployment id.");
		}

		this.usedBeanNames.add(deploymentId);

		//set the deployment info
		deployment.setEjbName(deployerBeans[i].getEjbName());
		deploymentHTML.append("<tr>\n<td>").append(deployerBeans[i].getEjbName()).append("</td>\n");
		deployment.setDeploymentId(deploymentId);
		deploymentHTML.append("<td>").append(deploymentId).append("</td>\n");
		deployment.setContainerId(containerId);
		deploymentHTML.append("<td>").append(containerId).append("</td>\n");

		//check for string lengths
		if (ejbRef == null && resourceRef == null) {
			deploymentHTML.append("<td>N/A</td>\n");
		} else {
			deploymentHTML.append("<td>\n");
			deploymentHTML.append(
				"<table cellspacing=\"0\" cellpadding=\"2\" border=\"0\" width=\"100%\">\n");
			deploymentHTML.append("<tr align=\"left\">\n");
			deploymentHTML.append("<th>Name</th>\n");
			deploymentHTML.append("<th>Id</th>\n");
			deploymentHTML.append("</tr>\n");

			//set the resource references
			if (resourceRef != null) {
				for (int j = 0; j < resourceRef.length; j++) {
					link = new ResourceLink();
					link.setResId(resourceRef[j][0]);
					link.setResRefName(resourceRef[j][1]);
					deploymentHTML.append("<tr>\n<td>").append(resourceRef[j][1]).append("</td>\n");
					deploymentHTML.append("<td>").append(resourceRef[j][0]).append(
						"</td>\n</tr>\n");
					deployment.addResourceLink(link);
				}
			}

			//set the ejb references
			if (ejbRef != null) {
				for (int j = 0; j < ejbRef.length; j++) {
					link = new ResourceLink();
					link.setResId(ejbRef[j][0]);
					link.setResRefName(ejbRef[j][1]);
					deploymentHTML.append("<tr>\n<td>").append(ejbRef[j][1]).append("</td>\n");
					deploymentHTML.append("<td>").append(ejbRef[j][0]).append("</td>\n</tr>\n");
					deployment.addResourceLink(link);
				}
			}

			deploymentHTML.append("</table>\n</td>\n");
		}

		deploymentHTML.append("</tr>\n");
		openejbJar.addEjbDeployment(deployment);
	}

	public void finishDeployment() throws RemoteException {
		try {
			if (options[1]) {
				jarFile = moveJar(jarFile);
			} else if (options[3]) {
				jarFile = copyJar(jarFile);
			}

			/* TODO: Automatically updating the users
			config file might not be desireable for
			some people.  We could make this a 
			configurable option. 
			*/
			addDeploymentEntryToConfig(jarFile);
			saveChanges(jarFile, openejbJar);
		} catch (OpenEJBException e) {
			throw new RemoteException(e.getMessage());
		}
	}

	public int getDeployerBeanLength() {
		return deployerBeans.length;
	}

	private String autoAssignDeploymentId(Bean bean) {
		this.resetUsedDeploymentIds();
		String ejbName = bean.getEjbName();
		String newEjbName;

		//first check for the deployment id in the list
		//and make sure that all the bean names are unique
		if (this.usedBeanNames.contains(ejbName)) {
			while (true) {
				newEjbName = ejbName + (Long.MAX_VALUE * Math.random());
				if (!this.usedBeanNames.contains(newEjbName)) {
					this.usedBeanNames.add(newEjbName);
					return newEjbName;
				}
			}
		}

		this.usedBeanNames.add(ejbName);
		return ejbName;
	}

	private String autoAssignContainerId(Bean bean) throws OpenEJBException {
		Container[] cs = getUsableContainers(bean);

		if (cs.length == 0) {
			//we'll fix this later
			throw new OpenEJBException("There are no useable containers for this bean.");
		}

		return cs[0].getId();
	}

	private void saveChanges(String jarFile, OpenejbJar openejbJar) throws OpenEJBException {
		ConfigUtils.writeOpenejbJar("META-INF/openejb-jar.xml", openejbJar);
		JarUtils.addFileToJar(jarFile, "META-INF/openejb-jar.xml");

		if (configChanged) {
			ConfigUtils.writeConfig(configFile, config);
		}
	}

	private void writeOQLForEntityBeansTable(
		Class bean,
		StringBuffer htmlString)
		throws OpenEJBException {
		org.openejb.alt.config.ejb11.Query query;
		QueryMethod queryMethod;
		MethodParams methodParams;

		Method[] methods = bean.getMethods();
		Class[] parameterList;
		Class[] exceptionList;
		String answer = null;

		if(methods.length > 0) {
			htmlString.append("<tr>\n<td>Method</td>\n<td>OQL Statement</td>\n</tr>\n");
		}

		for (int i = 0; i < methods.length; i++) {
			if (methods[i].getName().startsWith("find")
				&& !methods[i].getName().equals("findByPrimaryKey")) {

				htmlString.append("<tr>\n<td>\n");
				parameterList = methods[i].getParameterTypes();
				exceptionList = methods[i].getExceptionTypes();

				System.out.print(methods[i].getName() + "(");

				for (int j = 0; j < parameterList.length; j++) {
					htmlString.append(
						StringUtilities.getLastToken(parameterList[j].getName(), "."));

					if (j != (parameterList.length - 1)) {
						htmlString.append(", ");
					}
				}
				htmlString.append(") ");

				if (exceptionList.length > 0) {
					htmlString.append("<br> throws ");
				}

				for (int j = 0; j < exceptionList.length; j++) {
					htmlString.append(
						StringUtilities.getLastToken(exceptionList[j].getName(), "."));

					if (j != (exceptionList.length - 1)) {
						htmlString.append(", ");
					}
				}

				htmlString.append("\n</td>\n<td>");
				htmlString.append("<textarea cols=\"20\" rows=\"4\" name=\"something\"></textarea>");

				htmlString.append("<tr>\n");
			}
		}
	}

	public String createIdTable() throws OpenEJBException {
		//string that contains all the html
		StringBuffer htmlString = new StringBuffer();
		String deploymentId;
		String containerId;
		Container[] cs;
		ResourceRef[] refs;
		EjbRef[] ejbRefs;

		htmlString.append("<table cellspacing=\"1\" cellpadding=\"1\" border=\"1\">\n");
		htmlString.append("<tr align=\"left\">\n");
		htmlString.append("<th>Bean Name</th>\n");
		htmlString.append("<th>Deployment Id</th>\n");
		htmlString.append("<th>Container Id</th>\n");
		htmlString.append("<th>Resource References</th>\n");
		htmlString.append("</tr>\n");

		for (int i = 0; i < deployerBeans.length; i++) {
			//in here we check to see if we need to write the different parts or not
			htmlString.append("<tr>\n");
			htmlString.append("<td>" + deployerBeans[i].getEjbName() + "</td>\n");

			//deployment id
			if (options[5]) {
				deploymentId = autoAssignDeploymentId(deployerBeans[i]);
				htmlString.append("<td><input type=\"hidden\" name=\"deploymentId").append(
					i).append(
					"\" value=\"");
				htmlString.append(deploymentId).append("\">").append(deploymentId).append(
					"</td>\n");
			} else {
				htmlString.append("<td><input type=\"text\" name=\"deploymentId").append(i);
				htmlString.append("\" size=\"25\" maxlength=\"50\"></td>\n");
			}

			//container id
			if (options[0]) {
				containerId = autoAssignContainerId(deployerBeans[i]);
				htmlString.append("<td><input type=\"hidden\" name=\"containerId").append(
					i).append(
					"\" value=\"");
				htmlString.append(containerId).append("\">").append(containerId).append("</td>\n");
			} else {
				htmlString.append("<td><select name=\"containerId").append(i).append("\">\n");
				cs = getUsableContainers(deployerBeans[i]);
				//loop through the continer
				for (int j = 0; j < cs.length; j++) {
					htmlString.append("<option value=\"").append(cs[j].getId()).append("\">");
					htmlString.append(cs[j].getId()).append("</option>\n");
				}
				htmlString.append("</select></td>\n");
			}

			//outside references go here - put in a seperate method
			refs = deployerBeans[i].getResourceRef();
			ejbRefs = deployerBeans[i].getEjbRef();

			if ((refs.length > 0) || (ejbRefs.length > 0)) {
				htmlString.append("<td>");
				createIdTableOutsideRef(
					htmlString,
					refs,
					ejbRefs,
					deployerBeans[i].getEjbName(),
					i);
				htmlString.append("</td>");
			} else {
				htmlString.append("<td>N/A</td>\n");
			}

			htmlString.append("</tr>\n");
		}

		htmlString.append(
			"<tr><td colspan=\"4\"><input type=\"submit\" name=\"submitDeploymentAndContainerIds\"");
		htmlString.append(" value=\"Continue &gt;&gt;\"></td></tr></table>\n");

		this.resetUsedDeploymentIds();
		return htmlString.toString();
	}

	private void createIdTableOutsideRef(
		StringBuffer htmlString,
		ResourceRef[] refs,
		EjbRef[] ejbRefs,
		String deploymentName,
		int index)
		throws OpenEJBException {

		//this will create the html for outside references
		htmlString.append(
			"<table cellspacing=\"0\" cellpadding=\"2\" border=\"0\" width=\"100%\">\n");
		htmlString.append("<tr align=\"left\">\n");
		htmlString.append("<th>Name</th>\n");
		htmlString.append("<th>Type</th>\n");
		htmlString.append("<th>Id</th>\n");
		htmlString.append("</tr>\n");

		if (refs.length > 0) {
			for (int i = 0; i < refs.length; i++) {
				htmlString.append("<tr>\n");
				htmlString.append("<td>").append(refs[i].getResRefName()).append("</td>\n");
				htmlString.append("<td>").append(refs[i].getResType()).append("</td>\n");
				htmlString.append("<td>\n<select name=\"resourceRefId_").append(index).append(
					"_").append(
					i).append(
					"\">");

				//loop through the available resources
				for (int j = 0; j < this.resources.length; j++) {
					htmlString.append("<option value=\"").append(this.resources[j].getId());
					htmlString.append("\">").append(this.resources[j].getId()).append(
						"</option>\n");
				}

				htmlString.append("</select>\n");
				htmlString
					.append("<input type=\"hidden\" name=\"resourceRefName_")
					.append(index)
					.append("_")
					.append(i);
				htmlString.append("\" value=\"").append(refs[i].getResRefName()).append("\">");
				htmlString.append("</td>\n</tr>\n");
			}
		}

		if (ejbRefs.length > 0) {
			String ejbLink;
			for (int i = 0; i < ejbRefs.length; i++) {
				htmlString.append("<tr>\n");
				htmlString.append("<td>").append(ejbRefs[i].getEjbRefName()).append("</td>\n");
				htmlString.append("<td>").append(ejbRefs[i].getEjbRefType()).append("</td>\n");

				//check for an available link
				ejbLink = ejbRefs[i].getEjbLink();
				if (ejbLink == null) {
					htmlString.append("<td>\n<select name=\"ejbRefId_").append(index).append(
						"_").append(
						i).append(
						"\">");
					//loop through the available beans in the jar
					for (int j = 0; j < deployerBeans.length; j++) {
						if (!deployerBeans[j].getEjbName().equals(deploymentName)) {
							htmlString.append("<option value=\"").append(
								deployerBeans[j].getEjbName());
							htmlString.append("\">").append(deployerBeans[j].getEjbName()).append(
								"</option>\n");
						}
					}

					htmlString.append("</select>\n");
				} else {
					htmlString.append("<td><input type=\"hidden\" name=\"ejbRefId_").append(
						index).append(
						"_").append(
						i);
					htmlString.append("\" value=\"").append(ejbLink).append("\">\n").append(
						ejbLink);
				}

				htmlString.append("<input type=\"hidden\" name=\"ejbRefName_").append(
					index).append(
					"_").append(
					i);
				htmlString.append("\" value=\"").append(ejbRefs[i].getEjbRefName()).append("\">");
				htmlString.append("</td>\n</tr>\n");
			}
		}

		htmlString.append("</table>\n");
	}

	private void resetUsedDeploymentIds() {
		this.usedBeanNames = new ArrayList();

		//put all the used deployments into the array
		DeploymentInfo[] deployments = OpenEJB.deployments();
		for (int i = 0; i < deployments.length; i++) {
			this.usedBeanNames.add(deployments[i].getDeploymentID());
		}
	}

	/*------------------------------------------------------*/
	/*    Refactored Methods                                */
	/*------------------------------------------------------*/
	private Bean[] getBeans(EjbJar jar) {
		return EjbJarUtils.getBeans(jar);
	}

	private Container[] getUsableContainers(Bean bean) {
		return EjbJarUtils.getUsableContainers(containers, bean);
	}

	private String moveJar(String jar) throws OpenEJBException {
		return EjbJarUtils.moveJar(jar, options[2]);
	}

	private String copyJar(String jar) throws OpenEJBException {
		return EjbJarUtils.copyJar(jar, options[2]);
	}

	private void addDeploymentEntryToConfig(String jarLocation) {
		configChanged = ConfigUtils.addDeploymentEntryToConfig(jarLocation, config);
	}

	//api callback methods
	public void ejbActivate() throws javax.ejb.EJBException, java.rmi.RemoteException {}

	public void ejbPassivate() throws javax.ejb.EJBException, java.rmi.RemoteException {}

	public void ejbRemove() throws javax.ejb.EJBException, java.rmi.RemoteException {}

	public void setSessionContext(SessionContext sessionContext)
		throws javax.ejb.EJBException, java.rmi.RemoteException {
		this.context = sessionContext;
	}
}