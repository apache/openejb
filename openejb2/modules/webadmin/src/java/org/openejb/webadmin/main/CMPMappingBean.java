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

import javax.ejb.Handle;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import org.exolab.castor.jdo.conf.Database;
import org.exolab.castor.jdo.conf.Driver;
import org.exolab.castor.jdo.conf.Jndi;
import org.exolab.castor.jdo.conf.Mapping;
import org.exolab.castor.jdo.conf.Param;
import org.exolab.castor.xml.ValidationException;
import org.openejb.util.FileUtils;
import org.openejb.webadmin.HttpRequest;
import org.openejb.webadmin.HttpResponse;
import org.openejb.webadmin.WebAdminBean;

/**
 */
public class CMPMappingBean extends WebAdminBean {
	/** the handle file name */
	private static final String HANDLE_FILE = System.getProperty("file.separator") + "configurationHandle.obj";

	/** Creates a new instance of HomeBean */
	public void ejbCreate() {
		this.section = "CMPMapping";
	}

	/** called before any content is written to the browser
	 * @param request the http request
	 * @param response the http response
	 * @throws IOException if an exception is thrown
	 */
	public void preProcess(HttpRequest request, HttpResponse response) throws IOException {}

	/** called after all content is written to the browser
	 * @param request the http request
	 * @param response the http response
	 * @throws IOException if an exception is thrown
	 */
	public void postProcess(HttpRequest request, HttpResponse response) throws IOException {}

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
	 */
	public void writePageTitle(PrintWriter body) throws IOException {
		body.println("Container Managed Persistance Mapping");
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

	/** 
	 * writes the main body content to the broswer.  This content is inside 
	 * a <code>&lt;p&gt;</code> block 
	 * 
	 * @param body the output to write to
	 * @exception IOException if an exception is thrown
	 */
	public void writeBody(PrintWriter body) throws IOException {
//		CMPMappingDataObject dataObject;
//		String submitDBInfo = request.getFormParameter(CMPMappingWriter.FORM_FIELD_SUBMIT_DB_INFO);
//		String handleFile = request.getFormParameter(CMPMappingWriter.FORM_FIELD_HANDLE_FILE);
//		
//		//get or create a new handle
//		if (handleFile == null) {
//			dataObject = getCMPMappingDataObject();
//			handleFile = createHandle(dataObject);
//		} else {
//			dataObject = getHandle(handleFile);
//		}
//
//		//check for which type of action we're taking
//		if (submitDBInfo != null && submitDatabaseInformation(body, dataObject, handleFile)) {
//			CMPMappingWriter.printMappingInfo(body, handleFile, new MappingRoot());
//		} else {
//			CMPMappingWriter.printDBInfo(body, "", new DatabaseData(), handleFile);
//		}

		body.println("Coming soon...");
	}

	/**
	 * takes care of the submission of database information
	 */
	private boolean submitDatabaseInformation(PrintWriter body, CMPMappingDataObject dataObject, String handleFile) throws IOException {
		/* TODO: 
		 * 1. Check to see if files exist
		 * 2. Validate required fields 
		 */
		DatabaseData databaseData = new DatabaseData();
		databaseData.setDbEngine(request.getFormParameter(CMPMappingWriter.FORM_FIELD_DB_ENGINE));
		databaseData.setDriverClass(request.getFormParameter(CMPMappingWriter.FORM_FIELD_DRIVER_CLASS));
		databaseData.setDriverUrl(request.getFormParameter(CMPMappingWriter.FORM_FIELD_DRIVER_URL));
		databaseData.setFileName(request.getFormParameter(CMPMappingWriter.FORM_FIELD_FILE_NAME));
		databaseData.setJndiName(request.getFormParameter(CMPMappingWriter.FORM_FIELD_JNDI_NAME));
		databaseData.setPassword(request.getFormParameter(CMPMappingWriter.FORM_FIELD_PASSWORD));
		databaseData.setUsername(request.getFormParameter(CMPMappingWriter.FORM_FIELD_USERNAME));

		//validate the required fields
		try {
			databaseData.validate();
		} catch (ValidationException e) {
			CMPMappingWriter.printDBInfo(body, e.getMessage(), databaseData, handleFile);
			return false;
		}

		//assemble the file names
		String path =
			FileUtils.getBase().getDirectory("conf").getAbsolutePath()
				+ System.getProperty("file.separator")
				+ databaseData.getFileName();

		//create the file paths and names
		String localDBFileName = path + ".cmp_local_database.xml";
		String globalDBFileName = path + ".cmp_global_database.xml";
		String mappingFileName = path + ".cmp_or_mapping.xml";

		//set the standard variables for the global and local databases
		Database globalDatabase = new Database();
		Database localDatabase = new Database();
		globalDatabase.setName(ConfigBean.GLOBAL_TX_DATABASE);
		globalDatabase.setEngine(databaseData.getDbEngine());
		localDatabase.setName(ConfigBean.LOCAL_TX_DATABASE);
		localDatabase.setEngine(databaseData.getDbEngine());

		//create and set the mapping for the db's
		Mapping mapping = new Mapping();
		mapping.setHref(mappingFileName);
		globalDatabase.addMapping(mapping);
		localDatabase.addMapping(mapping);

		//set up the global specific fields
		Jndi jndi = new Jndi();
		jndi.setName(databaseData.getJndiName());
		globalDatabase.setJndi(jndi);

		//set up the local specific fields
		Driver driver = new Driver();
		Param userNameParam = new Param();
		Param passwordParam = new Param();

		//set up the user and password
		userNameParam.setName("user");
		userNameParam.setValue(databaseData.getUsername());
		passwordParam.setName("password");
		passwordParam.setValue(databaseData.getPassword());

		//set up the driver
		driver.setClassName(databaseData.getDriverClass());
		driver.setUrl(databaseData.getDriverUrl());
		driver.addParam(userNameParam);
		driver.addParam(passwordParam);

		localDatabase.setDriver(driver);

		//validate the two database types again just in case
		try {
			localDatabase.validate();
			globalDatabase.validate();
		} catch (ValidationException e) {
			CMPMappingWriter.printDBInfo(body, e.getMessage(), databaseData, handleFile);
			return false;
		}

		//here we want to move the jdbc driver over to the bin dir
		File jdbcDriverSource = new File(request.getFormParameter(CMPMappingWriter.FORM_FIELD_JDBC_DRIVER));
		String libDir =
			FileUtils.getBase().getDirectory("lib").getAbsolutePath()
				+ System.getProperty("file.separator")
				+ jdbcDriverSource.getName();
		File destFile = new File(libDir);

		//copy the file if it exists
		if (jdbcDriverSource.isFile()) {
			if (!destFile.exists() && !destFile.createNewFile()) {
				throw new IOException("Could not create file: " + libDir);
			}
			
			FileUtils.copyFile(destFile, jdbcDriverSource);
		}

		//put the data into the data object
		dataObject.setGlobalDatabase(globalDatabase);
		dataObject.setGlobalDatabaseFileName(globalDBFileName);
		dataObject.setLocalDatabase(localDatabase);
		dataObject.setLocalDatabaseFileName(localDBFileName);
		dataObject.setMappingRootFileName(mappingFileName);

		//this is part of the mapping in the next section
		//MappingRoot root = new MappingRoot();
		//ClassMapping map = new ClassMapping();

		return true;
	}

	/** 
	 * gets an object reference and handle 
	 * 
	 * @param mappingData the object to create a handle from
	 * @return an absolute path of the handle file
	 * @throws IOException if the file cannot be created
	 */
	private String createHandle(CMPMappingDataObject mappingData) throws IOException {
		//write the handle out to a file
		File myHandleFile = new File(FileUtils.createTempDirectory().getAbsolutePath() + HANDLE_FILE);
		if (!myHandleFile.exists()) {
			myHandleFile.createNewFile();
		}

		ObjectOutputStream objectOut = new ObjectOutputStream(new FileOutputStream(myHandleFile));
		objectOut.writeObject(mappingData.getHandle()); //writes the handle to the file
		objectOut.flush();
		objectOut.close();

		return myHandleFile.getAbsolutePath();
	}

	/** 
	 * creates a new CMPMappingDataObject 
	 * 
	 * @return a new CMPMappingDataObject
	 * @throws IOException if the object cannot be created
	 */
	private CMPMappingDataObject getCMPMappingDataObject() throws IOException {
		//lookup the bean
		try {
			InitialContext ctx = new InitialContext();
			Object obj = ctx.lookup("mapping/webadmin/CMPMappingData");
			//create a new instance
			CMPMappingDataHome home = (CMPMappingDataHome) PortableRemoteObject.narrow(obj, CMPMappingDataHome.class);
			return home.create();
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		}
	}

	/** 
	 * this method gets the handle 
	 * 
	 * @param handleFile the handle to the object
	 * @return the configuration data object
	 * @throws IOException if the file is not found
	 */
	private CMPMappingDataObject getHandle(String handleFile) throws IOException {
		File myHandleFile = new File(handleFile);

		//get the object
		ObjectInputStream objectIn = new ObjectInputStream(new FileInputStream(myHandleFile));
		//get the handle
		Handle mappingHandle;
		try {
			mappingHandle = (Handle) objectIn.readObject();
			return (CMPMappingDataObject) mappingHandle.getEJBObject();
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		}
	}
}