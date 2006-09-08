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

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.openejb.OpenEJB;
import org.openejb.OpenEJBException;
import org.openejb.assembler.DeploymentInfo;
import org.openejb.config.Bean;
import org.openejb.config.ConfigUtils;
import org.openejb.config.EjbJarUtils;
import org.openejb.config.ejb11.EjbDeployment;
import org.openejb.config.ejb11.EjbJar;
import org.openejb.config.ejb11.EjbRef;
import org.openejb.config.ejb11.MethodParams;
import org.openejb.config.ejb11.OpenejbJar;
import org.openejb.config.ejb11.Query;
import org.openejb.config.ejb11.QueryMethod;
import org.openejb.config.ejb11.ResourceLink;
import org.openejb.config.ejb11.ResourceRef;
import org.openejb.config.sys.Connector;
import org.openejb.config.sys.Container;
import org.openejb.config.sys.Openejb;
import org.openejb.util.HtmlUtilities;
import org.openejb.util.JarUtils;
import org.openejb.util.SafeToolkit;
import org.openejb.util.StringUtilities;

/**
 * This is a stateful session bean which handles the action of deployment for the
 * web administration.
 *
 * @see org.openejb.webadmin.deploy.DeployData
 * @see org.openejb.webadmin.deploy.OQLData
 * @see org.openejb.webadmin.deploy.ReferenceData
 */
public class DeployerBean implements SessionBean {
	public static final String ALL_FIELDS_REQUIRED_ERROR =
		"All fields (except OQL parameters) are required, "
			+ "please hit your back button and fill out the required fields.";

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
	private EjbDeployment[] deploymentInfoArray;
	private String jarFile;
	private StringBuffer deploymentHTML = new StringBuffer();
	private String containerDeployIdsHTML = "";
	private OpenejbJar openejbJar;
	private Vector beanList = new Vector();
	private boolean idsWritten = false;
	private ArrayList usedBeanNames = new ArrayList();
	private DeployData[] deployDataArray;

	/** 
	 * Creates a new instance of DeployerBean 
	 * @throws CreateException if this bean cannot be created 
	 */
	public void ejbCreate() throws CreateException {
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

	/**
	 * sets the boolean values for options
	 * @param booleanValues the array of boolean values
	 */
	public void setBooleanValues(boolean[] booleanValues) {
		options = booleanValues;
	}

	/**
	 * gets the boolean values for options
	 * @return the array of boolean values
	 */
	public boolean[] getBooleanValues() {
		return options;
	}

	/**
	 * sets the jar file
	 * @param jar the location of the jar file
	 */
	public void setJarFile(String jar) {
		jarFile = jar;
	}

	/**
	 * gets the jar file
	 * @return jar the location of the jar file
	 */
	public String getJarFile() {
		return jarFile;
	}

	/**
	 * gets the HTML for this deployment
	 * @return the HTML for deployment
	 */
	public String getDeploymentHTML() {
		return deploymentHTML.toString();
	}

	/**
	 * gets the array of deploy data objects
	 * @return the array of deploy data objects
	 */
	public DeployData[] getDeployDataArray() {
		return this.deployDataArray;
	}

	/**
	 * starts the deployment process by getting information from the
	 * jar, getting a list of beans and other init functions
	 * @throws OpenEJBException if something goes wrong
	 */
	public void startDeployment() throws OpenEJBException {
		EjbJar jar = null;
		OpenejbJar initialOpenejbJar = null;
		try { //test for invalid file
			jar = EjbJarUtils.readEjbJar(this.jarFile);

			//check for an openejb-jar.xml file
			if (ConfigUtils.checkForOpenejbJar(this.jarFile)) {
				initialOpenejbJar = ConfigUtils.readOpenejbJar(this.jarFile);
			}
		} catch (OpenEJBException oe) {
			throw new OpenEJBException(this.jarFile + " is not a valid jar file. ");
		}

		//we don't want to perform this check if we're forcing overwrite
		if (!this.options[2]) {
			File tempJarFile = new File(this.jarFile);
			//check for existing jar
			File beansDir = new File(System.getProperty("openejb.home") + System.getProperty("file.separator") + "beans");

			File[] beans = beansDir.listFiles();
			for (int i = 0; i < beans.length; i++) {
				if (tempJarFile.getName().equalsIgnoreCase(beans[i].getName())) {
					throw new OpenEJBException(
						System.getProperty("openejb.home")
							+ System.getProperty("file.separator")
							+ "beans"
							+ System.getProperty("file.separator")
							+ tempJarFile.getName()
							+ " already exists.");
				}
			}
		}

		//check for an
		if (initialOpenejbJar != null) {
			deploymentInfoArray = initialOpenejbJar.getEjbDeployment();
		} else {
			deploymentInfoArray = new EjbDeployment[0];
		}

		openejbJar = new OpenejbJar();
		deployerBeans = getBeans(jar);
	}

	/**
	 * This method takes care of setting the deploy information onto the deployment and 
	 * creating the HTML for the second page of the deployment
	 * @param deployDataArray the array of deployment data
	 */
	public void setDeployAndContainerIds(DeployData[] deployDataArray) throws OpenEJBException {
		//local variables
		EjbDeployment deployment;
		ResourceLink link;
		ReferenceData[] referenceDataArray;
		OQLData[] oqlDataArray;
		Query query;
		MethodParams methodParams;
		QueryMethod queryMethod;
		String[] parameterArray;

		for (int i = 0; i < deployDataArray.length; i++) {
			if (this.usedBeanNames.contains(deployDataArray[i].getDeploymentIdValue())) {
				throw new OpenEJBException(
					"The deployment id: "
						+ deployDataArray[i].getDeploymentIdValue()
						+ " is already being used by another bean, please choose another deployment id.");
			} else if ("".equals(deployDataArray[i].getDeploymentIdValue())) {
				throw new OpenEJBException(ALL_FIELDS_REQUIRED_ERROR);
			}

			deployment = new EjbDeployment();
			this.usedBeanNames.add(deployDataArray[i].getDeploymentIdValue());

			//set the deployment info
			deployment.setEjbName(deployDataArray[i].getEjbName());
			deploymentHTML.append("<tr>\n<td>").append(deployerBeans[i].getEjbName()).append("</td>\n");
			deployment.setDeploymentId(deployDataArray[i].getDeploymentIdValue());
			deploymentHTML.append("<td>").append(deployDataArray[i].getDeploymentIdValue()).append("</td>\n");

			if ("".equals(deployDataArray[i].getContainerIdValue())) {
				throw new OpenEJBException(ALL_FIELDS_REQUIRED_ERROR);
			}
			deployment.setContainerId(deployDataArray[i].getContainerIdValue());
			deploymentHTML.append("<td>").append(deployDataArray[i].getContainerIdValue()).append("</td>\n");

			//check for string lengths
			if (deployDataArray[i].getReferenceDataArray().length == 0) {
				deploymentHTML.append("<td>N/A</td>\n");
			} else {
				deploymentHTML.append("<td>\n");
				deploymentHTML.append("<table cellspacing=\"0\" cellpadding=\"2\" border=\"0\" width=\"100%\">\n");
				deploymentHTML.append("<tr align=\"left\">\n");
				deploymentHTML.append("<th>Name</th>\n");
				deploymentHTML.append("<th>Id</th>\n");
				deploymentHTML.append("</tr>\n");

				//set the resource references
				referenceDataArray = deployDataArray[i].getReferenceDataArray();
				for (int j = 0; j < referenceDataArray.length; j++) {
					if ("".equals(referenceDataArray[j].getReferenceIdValue())) {
						throw new OpenEJBException(ALL_FIELDS_REQUIRED_ERROR);
					}

					if ("".equals(referenceDataArray[j].getReferenceValue())) {
						throw new OpenEJBException(ALL_FIELDS_REQUIRED_ERROR);
					}

					link = new ResourceLink();
					link.setResId(referenceDataArray[j].getReferenceIdValue());

					link.setResRefName(referenceDataArray[j].getReferenceValue());
					deploymentHTML.append("<tr>\n<td>").append(referenceDataArray[j].getReferenceValue()).append("</td>\n");
					deploymentHTML.append("<td>").append(referenceDataArray[j].getReferenceIdValue()).append(
						"</td>\n</tr>\n");
					deployment.addResourceLink(link);
				}

				deploymentHTML.append("</table>\n</td>\n");
			}

			deploymentHTML.append("</tr>\n");

			//add in the oql methods
			oqlDataArray = deployDataArray[i].getOqlDataArray();
			if (oqlDataArray.length > 0) {
				deploymentHTML.append("<tr>\n<td colspan=\"4\">&nbsp;</td>\n</tr>\n");
				deploymentHTML.append("<tr>\n<td colspan=\"2\">").append(deployDataArray[i].getEjbName()).append(" - ");
				deploymentHTML.append(
					"Method</td>\n<td>OQL Statement</td>\n<td>OQL Parameters (comma seperated)</td>\n</tr>\n");
			}

			for (int j = 0; j < oqlDataArray.length; j++) {
				if ("".equals(oqlDataArray[j].getOqlStatementValue())) {
					throw new OpenEJBException(ALL_FIELDS_REQUIRED_ERROR);
				}

				//create the new instances
				query = new Query();
				methodParams = new MethodParams();
				queryMethod = new QueryMethod();

				queryMethod.setMethodName(oqlDataArray[j].getMethodName());
				query.setObjectQl(oqlDataArray[j].getOqlStatementValue());

				deploymentHTML.append("<tr>\n<td colspan=\"2\">");
				deploymentHTML.append(oqlDataArray[j].getMethodString()).append("</td>\n");
				deploymentHTML.append("<td>").append(oqlDataArray[j].getOqlStatementValue()).append("</td>\n");
				deploymentHTML.append("<td>");

				//get the list of parameters
				parameterArray = (String[]) oqlDataArray[j].getOqlParameterValueList().toArray(new String[0]);
				for (int k = 0; k < parameterArray.length; k++) {
					deploymentHTML.append(k + 1).append(". ").append(parameterArray[k]).append("<br>");
					methodParams.addMethodParam(k, parameterArray[k]);
				}

				if (parameterArray.length == 0) {
					deploymentHTML.append("N/A");
				}

				queryMethod.setMethodParams(methodParams);
				query.setQueryMethod(queryMethod);
				deployment.addQuery(query);
				deploymentHTML.append("</td>\n</tr>\n");
			}

			openejbJar.addEjbDeployment(deployment);
		}
	}

	/**
	 * finishes the deployment by adding the deployment entry
	 * and saving the changes
	 * @throws OpenEJBException if something goes wrong
	 */
	public void finishDeployment() throws OpenEJBException {
		jarFile = moveJar(jarFile);

		/* TODO: Automatically updating the users
		config file might not be desireable for
		some people.  We could make this a 
		configurable option. 
		*/
		addDeploymentEntryToConfig(jarFile);
		saveChanges(jarFile, openejbJar);
	}

	/**
	 * Assigns a deployment id based on the ejb name
	 * @param bean the EJB which we get the name from
	 * @return the EJB name of this bean
	 */
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

	/**
	 * Assigns a container id based on the ejb name
	 * @param bean the EJB which we get the name from
	 * @return the the best container for this bean
	 */
	private String autoAssignContainerId(Bean bean) throws OpenEJBException {
		Container[] cs = getUsableContainers(bean);

		if (cs.length == 0) {
			//we'll fix this later
			throw new OpenEJBException("There are no useable containers for this bean.");
		}

		return cs[0].getId();
	}

	/**
	 * Assigns a container id based on the ejb name
	 * @param bean the EJB which we get the name from
	 * @return the the best container for this bean
	 */
	private void saveChanges(String jarFile, OpenejbJar openejbJar) throws OpenEJBException {
		ConfigUtils.writeOpenejbJar("META-INF/openejb-jar.xml", openejbJar);
		JarUtils.addFileToJar(jarFile, "META-INF/openejb-jar.xml");

		if (configChanged) {
			ConfigUtils.writeConfig(configFile, config);
		}
	}

	/**
	 * creates an HTML table with form fields for deployment
	 * information
	 * @throws OpenEJBException when something goes wrong
	 * @return the HTML table with the form fields 
	 */
	public String createIdTable() throws OpenEJBException {
		//string that contains all the html
		StringBuffer htmlString = new StringBuffer();
		String deploymentId;
		String containerId;
		Container[] cs;
		ResourceRef[] refs;
		EjbRef[] ejbRefs;
		Class tempBean;
		this.deployDataArray = new DeployData[deployerBeans.length];
		EjbDeployment ejbDeployment = null;

		htmlString.append("<table cellspacing=\"0\" cellpadding=\"0\" border=\"1\">\n");
		htmlString.append("<tr align=\"left\">\n");
		htmlString.append("<th>Bean Name</th>\n");
		htmlString.append("<th>Deployment Id</th>\n");
		htmlString.append("<th>Container Id</th>\n");
		htmlString.append("<th>Resource References</th>\n");
		htmlString.append("</tr>\n");

		for (int i = 0; i < deployerBeans.length; i++) {
			//set up the data for the next step
			this.deployDataArray[i] = new DeployData();
			this.deployDataArray[i].setEjbName(deployerBeans[i].getEjbName());
			this.deployDataArray[i].setDeploymentIdName("deploymentId" + i);
			this.deployDataArray[i].setContainerIdName("containerId" + i);

			//in here we check to see if we need to write the different parts or not
			htmlString.append("<tr>\n");
			htmlString.append("<td>" + deployerBeans[i].getEjbName() + "</td>\n");

			for (int j = 0; j < this.deploymentInfoArray.length; j++) {
				if (deployerBeans[i].getEjbName().equals(deploymentInfoArray[j].getEjbName())) {
					ejbDeployment = deploymentInfoArray[j];
					break;
				}
			}

			//deployment id
			if (ejbDeployment != null) {
				deploymentId = ejbDeployment.getDeploymentId();
			} else {
				deploymentId = autoAssignDeploymentId(deployerBeans[i]);
			}

			htmlString
				.append("<td>")
				.append(HtmlUtilities.createTextFormField(deployDataArray[i].getDeploymentIdName(), deploymentId, 25, 50))
				.append("</td>\n");

			//container id
			if (ejbDeployment != null) {
				containerId = ejbDeployment.getContainerId();
			} else {
				containerId = autoAssignContainerId(deployerBeans[i]);
			}

			htmlString.append("<td>").append(
				HtmlUtilities.createSelectFormField(deployDataArray[i].getContainerIdName(), null)).append(
				'\n');
			cs = getUsableContainers(deployerBeans[i]);
			//loop through the continer
			for (int j = 0; j < cs.length; j++) {
				htmlString.append(
					HtmlUtilities.createSelectOption(
						cs[j].getId(),
						cs[j].getId(),
						containerId.equals(cs[j].getId()))).append(
					'\n');
			}
			htmlString.append("</select></td>\n");

			//outside references go here - put in a seperate method
			refs = deployerBeans[i].getResourceRef();
			ejbRefs = deployerBeans[i].getEjbRef();

			if ((refs.length > 0) || (ejbRefs.length > 0)) {
				htmlString.append("<td>");
				htmlString.append(
					createIdTableOutsideRef(
						refs,
						ejbRefs,
						deployerBeans[i].getEjbName(),
						deployDataArray[i],
						ejbDeployment,
						i));
				htmlString.append("</td>");
			} else {
				htmlString.append("<td>N/A</td>\n");
			}

			htmlString.append("</tr>\n");
		}

		//loop through the beans again to get the OQL statements 
		//(they go in a different part of the table)
		for (int i = 0; i < deployerBeans.length; i++) {
			//check for entity beans here
			if ("CMP_ENTITY".equals(deployerBeans[i].getType())) {
				tempBean = SafeToolkit.loadTempClass(deployerBeans[i].getHome(), this.jarFile);
				htmlString.append(
					writeOQLForEntityBeansTable(
						tempBean,
						deployerBeans[i].getEjbName(),
						deployDataArray[i],
						i,
						ejbDeployment));
			}
		}

		htmlString.append("<tr><td colspan=\"4\">");
		htmlString.append(HtmlUtilities.createSubmitFormButton("submitDeploymentAndContainerIds", "Continue &gt;&gt;"));
		htmlString.append("</td></tr></table>\n");

		this.resetUsedDeploymentIds();
		return htmlString.toString();
	}

	/**
	 * creates an HTML table/form for outside references, ejb and resource
	 * @param refs an array of resource references
	 * @param ejbRefs an array of ejb references
	 * @param deploymentName the name of this deployment
	 * @param deployData the DeploymentData object for this deployment
	 * @param index the index of this bean
	 * @return the HTML table/form for these outside references
	 */
	private String createIdTableOutsideRef(
		ResourceRef[] refs,
		EjbRef[] ejbRefs,
		String deploymentName,
		DeployData deployData,
		EjbDeployment ejbDeployment,
		int index)
		throws OpenEJBException {
		StringBuffer htmlString = new StringBuffer();
		String resourceId = "";
		String ejbId = "";
		ResourceLink[] resourceLinks =
			(ejbDeployment != null && ejbDeployment.getResourceLink() != null)
				? ejbDeployment.getResourceLink()
				: new ResourceLink[0];
		ReferenceData[] referenceDataArray = new ReferenceData[(refs.length + ejbRefs.length)];

		//this will create the html for outside references
		htmlString.append("<table cellspacing=\"0\" cellpadding=\"2\" border=\"0\" width=\"100%\">\n");
		htmlString.append("<tr align=\"left\">\n");
		htmlString.append("<th>Name</th>\n");
		htmlString.append("<th>Type</th>\n");
		htmlString.append("<th>Id</th>\n");
		htmlString.append("</tr>\n");

		//loop through the resource references
		if (refs.length > 0) {
			for (int i = 0; i < refs.length; i++) {
				//create a new ReferenceData object and set the values
				referenceDataArray[i] = new ReferenceData();
				referenceDataArray[i].setReferenceType(ReferenceData.RESOURCE_REFERENCE);
				referenceDataArray[i].setReferenceIdName("resourceRefId_" + index + "_" + i);
				referenceDataArray[i].setReferenceName("resourceRefName_" + index + "_" + i);

				htmlString.append("<tr>\n");
				htmlString.append("<td>").append(refs[i].getResRefName()).append("</td>\n");
				htmlString.append("<td>").append(refs[i].getResType()).append("</td>\n");
				htmlString.append("<td>");
				htmlString.append(HtmlUtilities.createSelectFormField(referenceDataArray[i].getReferenceIdName(), null));

				//loop through the available resources
				for (int j = 0; j < this.resources.length; j++) {
					//check for selected values
					boolean selected = false;
					for (int k = 0; k < resourceLinks.length; k++) {
						if (resources[j].getId().equals(resourceLinks[k].getResId())) {
							selected = true;
							break;
						}
					}
					htmlString.append(
						HtmlUtilities.createSelectOption(resources[j].getId(), resources[j].getId(), selected)).append(
						'\n');
				}

				htmlString.append("</select>\n");
				htmlString.append(
					HtmlUtilities.createHiddenFormField(referenceDataArray[i].getReferenceName(), refs[i].getResRefName()));
				htmlString.append("</td>\n</tr>\n");
			}
		}

		if (ejbRefs.length > 0) {
			String ejbLink;
			for (int i = 0; i < ejbRefs.length; i++) {
				referenceDataArray[i] = new ReferenceData();
				referenceDataArray[i].setReferenceType(ReferenceData.EJB_REFERENCE);
				referenceDataArray[i].setReferenceIdName("ejbRefId_" + index + "_" + i);
				referenceDataArray[i].setReferenceName("ejbRefName_" + index + "_" + i);

				htmlString.append("<tr>\n");
				htmlString.append("<td>").append(ejbRefs[i].getEjbRefName()).append("</td>\n");
				htmlString.append("<td>").append(ejbRefs[i].getEjbRefType()).append("</td>\n");

				//check for an available link
				ejbLink = ejbRefs[i].getEjbLink();
				if (ejbLink == null) {
					htmlString.append("<td>").append(
						HtmlUtilities.createSelectFormField("ejbRefId_" + index + "_" + i, null));
					//loop through the available beans in the jar
					for (int j = 0; j < deployerBeans.length; j++) {
						if (!deployerBeans[j].getEjbName().equals(deploymentName)) {
							//check for selected values
							boolean selected = false;
							for (int k = 0; k < resourceLinks.length; k++) {
								if (deployerBeans[j].getEjbName().equals(resourceLinks[k].getResId())) {
									selected = true;
									break;
								}
							}
							htmlString.append(
								HtmlUtilities.createSelectOption(
									deployerBeans[j].getEjbName(),
									deployerBeans[j].getEjbName(),
									selected));
						}
					}

					htmlString.append("</select>\n");
				} else {
					htmlString
						.append("<td>")
						.append(HtmlUtilities.createHiddenFormField(referenceDataArray[i].getReferenceIdName(), ejbLink))
						.append(ejbLink)
						.append('\n');
				}

				htmlString.append(
					HtmlUtilities.createHiddenFormField(
						referenceDataArray[i].getReferenceName(),
						ejbRefs[i].getEjbRefName()));
				htmlString.append("</td>\n</tr>\n");
			}
		}

		deployData.setReferenceDataArray(referenceDataArray);
		htmlString.append("</table>\n");

		return htmlString.toString();
	}

	private String writeOQLForEntityBeansTable(
		Class bean,
		String beanName,
		DeployData deployData,
		int index,
		EjbDeployment ejbDeployment)
		throws OpenEJBException {

		StringBuffer htmlString = new StringBuffer();
		int methodCount = 0;
		Method[] methods = bean.getMethods();
		List oqlDataList = new ArrayList();
		OQLData oqlData;
		String methodString;

		Query[] queries =
			(ejbDeployment != null && ejbDeployment.getQuery() != null) ? ejbDeployment.getQuery() : new Query[0];
		Query query = null;

		htmlString.append("<tr>\n<td colspan=\"4\">&nbsp;</td>\n</tr>\n");
		htmlString.append("<tr>\n<td colspan=\"2\">").append(beanName).append(" - ");
		htmlString.append("Method</td>\n<td>OQL Statement</td>\n<td>OQL Parameters (comma seperated)</td>\n</tr>\n");

		for (int i = 0; i < methods.length; i++) {
			if (methods[i].getName().startsWith("find") && !methods[i].getName().equals("findByPrimaryKey")) {
				//loop through the queries and get method name
				for (int j = 0; j < queries.length; j++) {
					if (queries[j].getQueryMethod().getMethodName().equals(methods[i].getName())) {
						query = queries[j];
						break;
					}
				}

				methodString = StringUtilities.createMethodString(methods[i], "<br>");

				methodCount++;
				oqlData = new OQLData();
				htmlString.append("<tr>\n<td colspan=\"2\">\n");
				htmlString.append(methodString);
				oqlData.setMethodString(methodString);

				//put the data into the oqlData object
				oqlData.setMethodName(methods[i].getName());
				oqlData.setOqlStatementName("oqlStatement_" + beanName + "_" + methods[i].getName());
				oqlData.setOqlParameterName("oqlParameters_" + beanName + "_" + methods[i].getName());

				//create the textarea for the OQL statement and parameters
				htmlString.append("\n</td>\n<td>");
				//append the oql statement if there is one
				htmlString.append(
					HtmlUtilities.createTextArea(
						oqlData.getOqlStatementName(),
						(query == null) ? "" : query.getObjectQl(),
						4,
						20,
						null,
						null,
						null));

				htmlString.append("</td>\n<td>");

				//get the parameters and append them if needed
				htmlString.append(
					HtmlUtilities.createTextArea(
						oqlData.getOqlParameterName(),
						(query == null)
							? ""
							: StringUtilities.stringArrayToCommaDelimitedStringList(
								query.getQueryMethod().getMethodParams().getMethodParam()),
						4,
						20,
						null,
						null,
						null));

				htmlString.append("</td>\n</tr>\n");

				oqlDataList.add(oqlData);
			}
		}

		deployData.setOqlDataArray((OQLData[]) oqlDataList.toArray(new OQLData[0]));

		//if there were no methods return a blank string
		if (methodCount > 0) {
			return htmlString.toString();
		} else {
			return "";
		}
	}

	private void resetUsedDeploymentIds() {
		this.usedBeanNames = new ArrayList();

//TODO
        //put all the used deployments into the array
//		DeploymentInfo[] deployments = OpenEJB.deployments();
//		for (int i = 0; i < deployments.length; i++) {
//			this.usedBeanNames.add(deployments[i].getDeploymentID());
//		}
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
	public void ejbActivate() {}
	public void ejbPassivate() {}
	public void ejbRemove() {}
	public void setSessionContext(SessionContext sessionContext) {
		this.context = sessionContext;
	}
}