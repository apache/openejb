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

import java.io.PrintWriter;

import org.exolab.castor.mapping.xml.MappingRoot;
import org.openejb.util.HtmlUtilities;
import org.openejb.util.StringUtilities;

/**
 */
public class CMPMappingWriter {
	public static final String FORM_FIELD_JDBC_DRIVER = "jdbcDriver";
	public static final String FORM_FIELD_FILE_NAME = "fileName";
	public static final String FORM_FIELD_JNDI_NAME = "jndiName";
	public static final String FORM_FIELD_DB_ENGINE = "dbEngine";
	public static final String FORM_FIELD_DRIVER_CLASS = "driverClass";
	public static final String FORM_FIELD_DRIVER_URL = "driverUrl";
	public static final String FORM_FIELD_USERNAME = "mappingUsername";
	public static final String FORM_FIELD_PASSWORD = "mappingPassword";
	public static final String FORM_FIELD_SUBMIT_DB_INFO = "submitDBInfo";
	public static final String FORM_FIELD_HANDLE_FILE = "handleFile";
	public static final String FORM_FIELD_CLASS_NAME = "className";
	public static final String FORM_FIELD_IDENTITY_FIELD = "identityField";

	/**
	 * writes the form for database information to the screen
	 * @param body the PrintWriter to write the information to
	 * @param errorMessage an error message to display
	 * @param databaseData the database information to be printed
	 */
	public static void printDBInfo(PrintWriter body, String errorMessage, DatabaseData databaseData, String handleFile) {
		body.println("<form action=\"CMPMapping\" method=\"post\" enctype=\"multipart/form-data\">");
		body.println("<table border=\"0\" cellspacing=\"1\" cellpadding=\"1\">");
		body.print("<tr>\n<td colspan=\"2\"><strong>Step 1:</strong> Choose database options.");
		body.println("The bold fields are required.</td>\n</tr>");

		//if there is an error message, display it here
		if (!"".equals(errorMessage)) {
			body.print("<tr>\n<td colspan=\"2\"><font color=\"red\"><b>");
			body.print(errorMessage);
			body.println("</b></font></td>\n</tr>");
		}

		body.println("<tr>\n<td colspan=\"2\">&nbsp;</td>\n</tr>");
		body.print("<tr>\n<td>JDBC Driver ");
		body.print(createHelpLink());
		body.println("</td>\n<td>");
		body.println(HtmlUtilities.createFileFormField(FORM_FIELD_JDBC_DRIVER, "", 25));
		body.print("</td>\n</tr>\n<tr>\n<td><b>File Names</b> ");
		body.print(createHelpLink());
		body.println("</td>\n<td>");
		body.println(
			HtmlUtilities.createTextFormField(
				FORM_FIELD_FILE_NAME,
				StringUtilities.nullToBlankString(databaseData.getFileName()),
				10,
				25));
		body.println(".cmp_local/global_database.xml</td>\n</tr>");
		body.print("<tr>\n<td><b>DB Engine</b> ");
		body.print(createHelpLink());
		body.println("</td>\n<td>");
		body.println(
			HtmlUtilities.createTextFormField(
				FORM_FIELD_DB_ENGINE,
				StringUtilities.nullToBlankString(databaseData.getDbEngine()),
				20,
				0));
		body.print("</td>\n</tr>\n<tr>\n<td><b>JNDI Name</b> ");
		body.print(createHelpLink());
		body.println("</td>\n<td>");
		body.println(
			HtmlUtilities.createTextFormField(
				FORM_FIELD_JNDI_NAME,
				StringUtilities.nullToBlankString(databaseData.getJndiName()),
				20,
				0));
		body.print("</td>\n</tr>\n<tr>\n<td><b>Driver Class</b> ");
		body.print(createHelpLink());
		body.println("</td>\n<td>");
		body.println(
			HtmlUtilities.createTextFormField(
				FORM_FIELD_DRIVER_CLASS,
				StringUtilities.nullToBlankString(databaseData.getDriverClass()),
				25,
				0));
		body.print("</td>\n</tr>\n<tr>\n<td><b>Driver URL</b> ");
		body.print(createHelpLink());
		body.println("</td>\n<td>");
		body.println(
			HtmlUtilities.createTextFormField(
				FORM_FIELD_DRIVER_URL,
				StringUtilities.nullToBlankString(databaseData.getDriverUrl()),
				25,
				0));
		body.print("</td>\n</tr>\n<tr>\n<td>Username ");
		body.print(createHelpLink());
		body.println("</td>\n<td>");
		body.println(
			HtmlUtilities.createTextFormField(
				FORM_FIELD_USERNAME,
				StringUtilities.nullToBlankString(databaseData.getUsername()),
				20,
				0));
		body.print("</td>\n</tr>\n<tr>\n<td>Password ");
		body.print(createHelpLink());
		body.println("</td>\n<td>");
		body.println(
			HtmlUtilities.createTextFormField(
				FORM_FIELD_PASSWORD,
				StringUtilities.nullToBlankString(databaseData.getPassword()),
				20,
				0));
		body.println("</td>\n</tr>\n<tr>\n<td colspan=\"2\">&nbsp;</td>\n</tr>");
		body.println("<tr>\n<td colspan=\"2\">");
		body.println(HtmlUtilities.createSubmitFormButton(FORM_FIELD_SUBMIT_DB_INFO, "Continue"));
		body.println(HtmlUtilities.createHiddenFormField(FORM_FIELD_HANDLE_FILE, handleFile));
		body.println("</td>\n</tr>\n</table>\n</form>");
	}
	
	/**
	 * prints out initial html for the mapping section
	 */
	public static void printMappingInfo(PrintWriter body, String handleFile, MappingRoot mappingRoot) {
		body.println("Step 2: Map the fields in the database to the fields in your entity bean. ");
		body.println("Once you map the field, the page will reload and your mapping will appear in");
		body.println("the table at the bottom of the page. The bold fields are required.");
		body.println("<form action=\"CMPMapping\" method=\"post\">");
		body.println("<table border=\"0\" cellspacing=\"1\" cellpadding=\"1\">");
		
		body.print("<tr>\n<td><b>Class Name</b> ");
		body.print(createHelpLink());		
		body.print("</td>\n<td>");
		body.print(HtmlUtilities.createTextFormField(FORM_FIELD_CLASS_NAME, "", 30, 0));
		body.print("</td>\n</tr>\n<tr>\n<td><b>Identity Field</b> ");
		body.print(createHelpLink());
		body.print("</td>\n<td>");
		body.print(HtmlUtilities.createTextFormField(FORM_FIELD_IDENTITY_FIELD, "", 20, 0));
		body.println("</table>\n</form>");
	}
	
	/**
	 * creates a help link for this section
	 * @return the help link
	 */
	private static String createHelpLink() {
		return HtmlUtilities.createAnchor(
			"javascript:popUpHelp('help/mapping/help.html')",
			"(?)",
			HtmlUtilities.ANCHOR_HREF_TYPE);
	}

}
