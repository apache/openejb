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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.ejb.Handle;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import org.openejb.webadmin.HttpRequest;
import org.openejb.webadmin.HttpResponse;
import org.openejb.webadmin.WebAdminBean;
import org.openejb.util.FileUtils;
import org.openejb.util.HtmlUtilities;
import org.openejb.util.Logger;

/**
 * This class takes care of deploying a bean in the web administration. 
 *
 * timu:
 * 1. Add better error handling
 * 2. Finish implementing the writeForm function 
 * 3. Add documentation
 * 4. Fix force overwrite error
 * 5. Internationalize all text 
 *
 */
public class DeployBean extends WebAdminBean {
	private static final String HANDLE_FILE = System.getProperty("file.separator") + "deployerHandle.obj";
	private DeployerObject deployer = null;
	private Logger logger = Logger.getInstance("OpenEJB", "org.openejb");

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

	/** Creates a new instance of DeployBean */
	public void ejbCreate() {
		this.section = "Deployment";
	}

	/** called after all content is written to the browser
	 * @param request the http request
	 * @param response the http response
	 * @throws IOException if an exception is thrown
	 *
	 */
	public void postProcess(HttpRequest request, HttpResponse response) throws IOException {}

	/** called before any content is written to the browser
	 * @param request the http request
	 * @param response the http response
	 * @throws IOException if an exception is thrown
	 *
	 */
	public void preProcess(HttpRequest request, HttpResponse response) throws IOException {}

	/** writes the main body content to the broswer.  This content is inside a <code>&lt;p&gt;</code> block
	 *
	 * @param body the output to write to
	 * @exception IOException if an exception is thrown
	 *
	 */
	public void writeBody(PrintWriter body) throws IOException {
		String deploy = request.getFormParameter("deploy");
		String submitDeployment = request.getFormParameter("submitDeploymentAndContainerIds");

		try {
			//the user has hit the deploy button
			if (deploy != null) {
				String deployerHandleString = createDeployerHandle();
				setOptions();
				deployer.startDeployment();
				body.println(
					"Below is a list of beans in the jar which you have chosen to deploy. Some of the methods in your beans may require "
						+ "OQL statements.  If so, form fields will be displayed for the methods which require these statements. "
						+ "In this case <b>OQL statements are required.</b> "
						+ "Please enter the information requested in the form fields and click \"Continue &gt;&gt;\" "
						+ "to continue.<br>");
				body.println("<form action=\"Deployment\" method=\"post\" onSubmit=\"return checkDeployValues(this)\">");
				body.print(deployer.createIdTable());
				body.println(HtmlUtilities.createHiddenFormField("deployerHandle", deployerHandleString));
				body.println("</form>");
			} else if (submitDeployment != null) {
				deployPartTwo(body);
			} else {
				writeForm(body);
			}
		} catch (Exception e) {
			//timu - Create a generic error screen
			handleException(e, body);
		}
	}

	/** handles exceptions for the page */
	private void handleException(Exception e, PrintWriter body) {
		if (e instanceof UndeclaredThrowableException) {
			UndeclaredThrowableException ue = (UndeclaredThrowableException) e;
			Throwable t = ue.getUndeclaredThrowable();
			if (t != null) {
				body.println(t.getMessage());
				logger.error("Error on web deployment", t);
			} else {
				body.println("An unknown system error occured.");
			}
		} else {
			if (e != null) {
				body.println(e.getMessage());
				logger.error("Error on web deployment", e);
			} else {
				body.println("An unknown system error occured.");
			}
		}
	}

	/** the second part of the deployment which loops through the
	 * array of deployments and sets the values 
	 */
	private void deployPartTwo(PrintWriter body) throws Exception {
		ReferenceData[] referenceDataArray;
		OQLData[] oqlDataArray;
		StringTokenizer oqlParameterToken;

		String deployerHandleString = request.getFormParameter("deployerHandle");
		getDeployerHandle(deployerHandleString); //gets the deployment handle
		DeployData[] deployDataArray = deployer.getDeployDataArray();

		//loop through all the beans and set the ids
		for (int i = 0; i < deployDataArray.length; i++) {
			//set the containerId value and the deployment id value 
			deployDataArray[i].setContainerIdValue(request.getFormParameter(deployDataArray[i].getContainerIdName()));
			deployDataArray[i].setDeploymentIdValue(request.getFormParameter(deployDataArray[i].getDeploymentIdName()));

			//get the ejb and resource references and loop through them
			referenceDataArray = deployDataArray[i].getReferenceDataArray();
			for (int j = 0; j < referenceDataArray.length; j++) {
				referenceDataArray[j].setReferenceIdValue(
					request.getFormParameter(referenceDataArray[j].getReferenceIdName()));
				referenceDataArray[j].setReferenceValue(request.getFormParameter(referenceDataArray[j].getReferenceName()));
			}

			//get the oql methods and set them
			oqlDataArray = deployDataArray[i].getOqlDataArray();
			for (int j = 0; j < oqlDataArray.length; j++) {
				oqlDataArray[j].setOqlStatementValue(request.getFormParameter(oqlDataArray[j].getOqlStatementName()));

				//next set up the tokens for the OQL parameters
				if (request.getFormParameter(oqlDataArray[j].getOqlParameterName()) != null) {
					oqlParameterToken =
						new StringTokenizer(request.getFormParameter(oqlDataArray[j].getOqlParameterName()), ",");
					//loop through the tokens
					while (oqlParameterToken.hasMoreTokens()) {
						oqlDataArray[j].getOqlParameterValueList().add(oqlParameterToken.nextToken());
					}

				}
			}
		}

		deployer.setDeployAndContainerIds(deployDataArray);

		//print out a message to the user to let them know thier bean was deployed
		body.println(
			"You jar is now deployed.  If you chose to move or copy your jar"
				+ "from it's original location, you will now find it in: "
				+ System.getProperty("openejb.home")
				+ System.getProperty("file.separator")
				+ "beans. You will need to restart OpenEJB for this "
				+ "deployment to take affect.  Once you restart, you should see your bean(s) in the "
				+ HtmlUtilities.createAnchor("DeploymentList", "list of beans", HtmlUtilities.ANCHOR_HREF_TYPE)
				+ "on this console.  Below is a table of "
				+ "the bean(s) you deployed.<br><br>");

		printDeploymentHtml(body);
		deployer.remove();
	}

	/** prints a table of deployment HTML */
	private void printDeploymentHtml(PrintWriter body) throws Exception {
		deployer.finishDeployment();
		body.println("<table cellspacing=\"1\" cellpadding=\"2\" border=\"1\">\n");
		body.println("<tr align=\"left\">\n");
		body.println("<th>Bean Name</th>\n");
		body.println("<th>Deployment Id</th>\n");
		body.println("<th>Container Id</th>\n");
		body.println("<th>Resource References</th>\n");
		body.println("</tr>\n");
		body.println(deployer.getDeploymentHTML());
		body.println("</table>");
	}

	/** gets an object reference and handle */
	private String createDeployerHandle() throws Exception {

		//lookup the bean
		InitialContext ctx = new InitialContext();
		Object obj = ctx.lookup("deploy/webadmin/Deployer");
		//create a new instance
		DeployerHome home = (DeployerHome) PortableRemoteObject.narrow(obj, DeployerHome.class);
		deployer = home.create();

		//get the handle for that instance
		Handle deployerHandle = deployer.getHandle();

		//write the handle out to a file
		File myHandleFile = new File(FileUtils.createTempDirectory().getAbsolutePath() + HANDLE_FILE);
		if (!myHandleFile.exists()) {
			myHandleFile.createNewFile();
		}

		ObjectOutputStream objectOut = new ObjectOutputStream(new FileOutputStream(myHandleFile));
		objectOut.writeObject(deployerHandle); //writes the handle to the file
		objectOut.flush();
		objectOut.close();

		return myHandleFile.getAbsolutePath();
	}

	/** this function gets the deployer handle */
	private void getDeployerHandle(String handleFile) throws Exception {
		File myHandleFile = new File(handleFile);

		//get the object
		ObjectInputStream objectIn = new ObjectInputStream(new FileInputStream(myHandleFile));
		//get the handle
		Handle deployerHandle = (Handle) objectIn.readObject();
		this.deployer = (DeployerObject) deployerHandle.getEJBObject();
	}

	/** starts the deployment process */
	private void setOptions() throws Exception {
		//the the form values
		String jarFile = request.getFormParameter("jarFile");
		String force = request.getFormParameter("force");
		File testForValidFile = null;

		if (jarFile == null) {
			//do this for now, needs better exception handling
			throw new IOException("No jar file was provided, please try again.");
		}
		//set the jar file
		this.deployer.setJarFile(jarFile);

		//force overwrite
		if (force != null) {
			options[2] = true;
		}

		testForValidFile = null;
		this.deployer.setBooleanValues(options);
	}

	/** writes the form for this page 
	 *
	 * TODO - finish the sections that are not implemented
	 */
	private void writeForm(PrintWriter body) throws IOException {
		//the form decleration
		body.println(
			"<form action=\"Deployment\" method=\"post\" enctype=\"multipart/form-data\" onsubmit=\"return checkDeploy(this)\">");
		//the start table
		body.println("<table border=\"0\" cellspacing=\"0\" cellpadding=\"2\">");

		//info about CMP mapping - not yet implemented
		/*body.println("<tr>");
		body.println("<td colspan=\"2\">");
		body.println("<strong>Important Note:</strong> If you are deploying a Container Managed Persistance");
		body.println("bean, you must first <a href=\"\">map the fields</a> and then deploy.  Once that step is completed");
		body.println("you will be sent to this page and your configuration files will be set up for you.");
		body.println("(see the help section for more information).");
		body.println("</td>");
		body.println("</tr>");
		body.println("<tr>");
		body.println("<td colspan=\"2\">&nbsp;</td>");
		body.println("</tr>"); */

		//info about step 1
		body.println("<tr>\n<td colspan=\"2\">");
		body.println("<strong>Step 1:</strong> Browse your file system and select your bean to deploy.");
		body.println("</td>\n</tr>\n<tr>\n<td colspan=\"2\">&nbsp;</td>\n</tr>");

		//the file upload for the jar file (this may need to be changed)
		body.println("<tr>");
		body.println("<td><nobr>Jar File</nobr></td>\n<td>");
		body.println(HtmlUtilities.createFileFormField("jarFile", "", 35));
		body.println("</td>\n</tr>\n<tr>\n<td colspan=\"2\">&nbsp;</td>\n</tr>");

		/***************************
		 * Deployment options
		 ***************************/
//		body.println("<tr>\n<td colspan=\"2\"><strong>Step 2:</strong> Choose options for deployment.");
//		body.println("</td>\n</tr>\n<tr>\n<td colspan=\"2\">&nbsp;</td>\n</tr>");

		/* force over write of the bean - this will have to wait for now until the bug gets fixed
		body.println("<tr>");
		body.println("<td colspan=\"2\">");
		body.println("<input type=\"checkbox\" name=\"force\" value=\"-f\">");
		body.println(
		    "Forces a move or a copy, overwriting any previously existing jar with the same name.");
		body.println("</td>");
		body.println("</tr>");
		body.println("<tr>");
		body.println("<td colspan=\"2\">&nbsp;</td>");
		body.println("</tr>");
		*/

		// sets the OpenEJB configuration file 
//		body.println("<tr>");
//		body.println(
//			"<td colspan=\"2\">Sets the OpenEJB configuration to the specified file. (leave blank for non-use) "
//				+ "Note: you will need to make sure the configuration files are in this location on the server.</td>");
//		body.println("</tr>\n<tr>\n<td><nobr>Config File</nobr></td>\n<td>");
//		body.println(HtmlUtilities.createTextFormField("configFile", "", 35, 0));
//		body.println("</td>\n</tr>\n<tr>\n<td colspan=\"2\">&nbsp;</td>\n</tr>");
//
		//deploy the bean
		body.println("<tr>\n<td colspan=\"2\">&nbsp;</td>\n</tr>");
		body.println("<tr><td colspan=\"2\">");
		body.println(HtmlUtilities.createSubmitFormButton("deploy", "Deploy"));
		body.println("</td>\n</tr>");

		//the end...
		/* we don't have help yet
		body.println("<tr>");
		body.println("<td colspan=\"2\">Note: see the help section for examples on how to deploy beans.</td>");
		body.println("</tr>"); */
		body.println("</table>");
		//the handle file name
		body.println(HtmlUtilities.createHiddenFormField("handleFile", ""));
		body.println("</form>");
	}

	/** Write the TITLE of the HTML document.  This is the part
	 * that goes into the <code>&lt;head&gt;&lt;title&gt;
	 * &lt;/title&gt;&lt;/head&gt;</code> tags
	 *
	 * @param body the output to write to
	 * @exception IOException of an exception is thrown
	 *
	 */
	public void writeHtmlTitle(PrintWriter body) throws IOException {
		body.println(HTML_TITLE);
	}

	/** Write the title of the page.  This is displayed right
	 * above the main block of content.
	 *
	 * @param body the output to write to
	 * @exception IOException if an exception is thrown
	 *
	 */
	public void writePageTitle(PrintWriter body) throws IOException {
		body.println("EJB Deployment");
	}

	/** Write the sub items for this bean in the left navigation bar of
	* the page.  This should look somthing like the one below:
	*
	*      <code>
	*      &lt;tr&gt;
	*       &lt;td valign="top" align="left"&gt;
	*        &lt;a href="system?show=deployments"&gt;&lt;span class="subMenuOff"&gt;
	*        &nbsp;&nbsp;&nbsp;Deployments
	*        &lt;/span&gt;
	*        &lt;/a&gt;&lt;/td&gt;
	*      &lt;/tr&gt;
	*      </code>
	*
	* Alternately, the bean can use the method formatSubMenuItem(..) which
	* will create HTML like the one above
	*
	* @param body the output to write to
	* @exception IOException if an exception is thrown
	*
	*/
	public void writeSubMenuItems(PrintWriter body) throws IOException {}
}